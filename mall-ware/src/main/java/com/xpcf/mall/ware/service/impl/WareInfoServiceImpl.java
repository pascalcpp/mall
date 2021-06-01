package com.xpcf.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.xpcf.common.utils.R;
import com.xpcf.common.vo.MemberRespVo;
import com.xpcf.mall.ware.feign.MemberFeignService;
import com.xpcf.mall.ware.vo.FareVo;
import com.xpcf.mall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.common.utils.Query;

import com.xpcf.mall.ware.dao.WareInfoDao;
import com.xpcf.mall.ware.entity.WareInfoEntity;
import com.xpcf.mall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {


    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long id) {

        FareVo fareVo = new FareVo();
        R info = memberFeignService.addrInfo(id);
        MemberAddressVo memberAddressVo = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });

        if (null != memberAddressVo) {
            String phone = memberAddressVo.getPhone();
            String substring = phone.substring(phone.length() - 1, phone.length());
            fareVo.setAddress(memberAddressVo);
            fareVo.setFare(new BigDecimal(substring));
            return fareVo;
        }

        return null;
    }

}