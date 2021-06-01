package com.xpcf.mall.product.web;

import com.xpcf.mall.product.entity.CategoryEntity;
import com.xpcf.mall.product.service.CategoryService;
import com.xpcf.mall.product.vo.Catalog2VO;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/26/2020 6:19 PM
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        // TODO 查出分类
        List<CategoryEntity> categories = categoryService.getLevel1Categories();
        model.addAttribute("categories",categories);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2VO>> getCatalogJson(){

        Map<String, List<Catalog2VO>> map = categoryService.getCatalogJson();
        return map;

    }


    @ResponseBody
    @GetMapping("/hello")
    public String hello(){

        RLock lock = redissonClient.getLock("my-lock");
        // 一般指定时间,不会自动续期 若业务超时代表出现问题 及时修复
        lock.lock();
        try {
            System.out.println("执行业务:  "+Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(60);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放锁:  "+Thread.currentThread().getName());
            lock.unlock();
        }
        return "hello";
    }


    @ResponseBody
    @GetMapping("/write")
    public String write(){

        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");

        // 指定时间,不会自动续期 若业务超时代表出现问题 及时修复
        RLock lock = readWriteLock.writeLock();
        lock.lock();
        try {
            System.out.println("write 执行业务:  "+Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("write 释放锁:  "+Thread.currentThread().getName());
            lock.unlock();
        }
        return "hello";
    }


    @ResponseBody
    @GetMapping("/read")
    public String read() throws InterruptedException {

        TimeUnit.SECONDS.sleep(1);
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");

        // 一般指定时间,不会自动续期 若业务超时代表出现问题 及时修复
        RLock lock = readWriteLock.readLock();
        lock.lock();
        try {
            System.out.println("read 执行业务:  "+Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            System.out.println("read 释放锁:  "+Thread.currentThread().getName());
            lock.unlock();
        }
        return "hello";
    }

}
