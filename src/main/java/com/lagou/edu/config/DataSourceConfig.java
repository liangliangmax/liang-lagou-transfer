package com.lagou.edu.config;


import com.alibaba.druid.pool.DruidDataSource;
import com.liang.spring.core.annotation.Bean;
import com.liang.spring.core.annotation.Configuration;
import com.liang.spring.core.annotation.PropertySource;
import com.liang.spring.core.annotation.Value;

import javax.sql.DataSource;


@Configuration
@PropertySource("classpath:jdbc.properties")
public class DataSourceConfig {


    @Value("${jdbc.driver}")
    private String driverClassName;
    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.username}")
    private String username;
    @Value("${jdbc.password}")
    private String password;


    @Bean
    public DataSource getDataSource(){

        DruidDataSource druidDataSource = new DruidDataSource();

        druidDataSource.setDriverClassName(driverClassName);
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);

        return druidDataSource;
    }

}
