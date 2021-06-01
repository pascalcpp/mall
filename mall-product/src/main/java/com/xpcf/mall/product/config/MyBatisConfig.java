package com.xpcf.mall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/19/2020 4:28 PM
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.xpcf.mall.product.dao")
public class MyBatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        paginationInterceptor.setOverflow(true);
        paginationInterceptor.setLimit(1000L);
        return paginationInterceptor;
    }
}
