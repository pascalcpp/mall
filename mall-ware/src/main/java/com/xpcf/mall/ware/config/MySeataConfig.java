//package com.xpcf.mall.ware.config;
//
//import com.zaxxer.hikari.HikariDataSource;
//import io.seata.rm.datasource.DataSourceProxy;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.StringUtils;
//
//import javax.sql.DataSource;
//
///**
// * TODO
// *
// * @author XPCF
// * @version 1.0
// * @date 2/6/2021 8:40 PM
// */
//@Configuration
//public class MySeataConfig {
//
//    @Bean
//    public DataSource dataSource(DataSourceProperties properties) {
//        HikariDataSource dataSource = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
//        if (StringUtils.hasText(properties.getName())) {
//            dataSource.setPoolName(properties.getName());
//        }
//        return new DataSourceProxy(dataSource);
//    }
//
//
//
//}
