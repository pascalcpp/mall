package com.xpcf.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xpcf.mall.product.service.CategoryBrandRelationService;
import com.xpcf.mall.product.vo.Catalog2VO;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.common.utils.Query;

import com.xpcf.mall.product.dao.CategoryDao;
import com.xpcf.mall.product.entity.CategoryEntity;
import com.xpcf.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);

        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0L)
                .map((menu)->{
                    menu.setChildren(getChildren(menu,entities));
                    return menu;
                })
                .sorted((m1,m2)->{
                    // use mybatis if Integer = 0. it will as null
                    return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
                })
                .collect(Collectors.toList());

        return level1Menus;
    }

    /**
     * @CacheEvict cache失效模式
     * @param category
     */

//    @Caching(evict = {
//            @CacheEvict(value = {"category"},key = "'level1Categories'"),
//            @CacheEvict(value = {"category"},key = "'getCatalogJson'")
//    })与下 注解equal
    @CacheEvict(value = {"category"},allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    //spel 中使用 '' 表明其是字符串 否则会当做spel 来处理，会发生500错误
    //Property or field 'level1Categories' cannot be found on object of type
    // 使用redis时 若key有space 必须使用"" 否则发生error
    @Cacheable(value = {"category"},key = "'level1Categories'")
    @Override
    public List<CategoryEntity> getLevel1Categories() {
//        long l = System.currentTimeMillis();
        System.out.println("查询数据库");
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
//        System.out.println("consume time: "+ (System.currentTimeMillis() - l));
        return entities;
    }

    /**
     *
     * @return
     */
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catalog2VO>> getCatalogJson() {
        System.out.println("开始查询mysql");
        // 将业务中三次数据库查询 优化为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);


        Map<String, List<Catalog2VO>> map = level1Categories.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    List<CategoryEntity> entities = getParent_cid(selectList, v.getCatId());
                    List<Catalog2VO> catalog2VOS = null;
                    if (null != entities) {
                        catalog2VOS = entities.stream().map(item -> {
                            Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, item.getName(), item.getCatId().toString());
                            List<CategoryEntity> level3Categories = getParent_cid(selectList, item.getCatId());

                            if (null != level3Categories) {
                                List<Catalog2VO.Catalog3VO> catalog3VOS = level3Categories.stream().map(l3 -> {
                                    Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(item.getCatId().toString(), l3.getName(), l3.getCatId().toString());
                                    return catalog3VO;
                                }).collect(Collectors.toList());
                                catalog2VO.setCatalog3List(catalog3VOS);
                            }


                            return catalog2VO;
                        }).collect(Collectors.toList());
                    }
                    return catalog2VOS;
                }));

        return map;
    }

    public Map<String, List<Catalog2VO>> getCatalogJson2(){

        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");

        if(StringUtils.isEmpty(catalogJSON)){
            Map<String, List<Catalog2VO>> catalogJsonFromDB = getCatalogJsonFromDBWithRedisLock();

            return catalogJsonFromDB;
        }

        Map<String, List<Catalog2VO>> result = JSON.parseObject(catalogJSON,new TypeReference<Map<String, List<Catalog2VO>>>(){});

        System.out.println("return redis-cache data");
        return result;

//        return getCatalogJsonFromDB();
    }


    // 解决缓存击穿问题
    // TODO 使用分布式锁(redis)
    public synchronized Map<String, List<Catalog2VO>> getCatalogJsonFromDBWithLocalLock() {


        return getCatalogJsonFromDB();
    }


    public Map<String, List<Catalog2VO>> getCatalogJsonFromDBWithRedissonLock() {

        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();

        Map<String, List<Catalog2VO>> catalogJsonFromDB = null;
        try{
            catalogJsonFromDB = getCatalogJsonFromDB();

        }catch (Exception e){
            log.error("设置cache前查询db出现错误 {}",e);
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

        return catalogJsonFromDB;

    }


    /**
     * 以下保证原子性
     * 加锁
     * 解锁(lua)
     * @return
     */
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDBWithRedisLock() {

        String value = UUID.randomUUID().toString();
        Boolean b = redisTemplate.opsForValue().setIfAbsent("lock", value,300,TimeUnit.SECONDS);

        if(b){
            Map<String, List<Catalog2VO>> catalogJsonFromDB = null;
            try{
                catalogJsonFromDB = getCatalogJsonFromDB();

            }catch (Exception e){
                log.error("设置cache前查询db出现错误 {}",e);
                e.printStackTrace();
            }finally {

                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long lock = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock"), value);
            }

            return catalogJsonFromDB;
        }else {
            // 使用spinlock 改进 避免函数stack overflow
            try {
                TimeUnit.MICROSECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }

    }

    private Map<String, List<Catalog2VO>> getCatalogJsonFromDB() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
        }

        System.out.println("开始查询mysql");
        // 将业务中三次数据库查询 优化为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);


        Map<String, List<Catalog2VO>> map = level1Categories.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    List<CategoryEntity> entities = getParent_cid(selectList, v.getCatId());
                    List<Catalog2VO> catalog2VOS = null;
                    if (null != entities) {
                        catalog2VOS = entities.stream().map(item -> {
                            Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, item.getName(), item.getCatId().toString());
                            List<CategoryEntity> level3Categories = getParent_cid(selectList, item.getCatId());

                            if (null != level3Categories) {
                                List<Catalog2VO.Catalog3VO> catalog3VOS = level3Categories.stream().map(l3 -> {
                                    Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(item.getCatId().toString(), l3.getName(), l3.getCatId().toString());
                                    return catalog3VO;
                                }).collect(Collectors.toList());
                                catalog2VO.setCatalog3List(catalog3VOS);
                            }


                            return catalog2VO;
                        }).collect(Collectors.toList());
                    }
                    return catalog2VOS;
                }));

        String jsonString = JSON.toJSONString(map);
        redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
        return map;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> entities , Long parent_cid) {


        return entities.stream()
                .filter(item->item.getParentCid().equals(parent_cid)).collect(Collectors.toList());

        //        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", parent_cid));
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        path = findParentPath(catelogId, path);
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }


    private List<Long> findParentPath(Long catelogId,List<Long> path){
        path.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if(!categoryEntity.getParentCid().equals(0L)){
            findParentPath(categoryEntity.getParentCid(),path);
        }

        return path;

    }


    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  check if have other refer
        baseMapper.deleteBatchIds(asList);
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all){
        return all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                })
                .sorted((m1, m2) -> {
                    return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
                })
                .collect(Collectors.toList());
    }

}