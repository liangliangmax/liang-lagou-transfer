package com.lagou.edu.config;

import com.liang.spring.core.annotation.Autowired;
import com.liang.spring.core.annotation.Bean;
import com.liang.spring.core.annotation.Configuration;
import com.liang.spring.core.transaction.DataSourceTransactionManager;
import com.liang.spring.core.transaction.TransactionManager;

import javax.sql.DataSource;

@Configuration
public class TransactionConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public TransactionManager transactionManager(){

        TransactionManager transactionManager = new DataSourceTransactionManager();

        ((DataSourceTransactionManager) transactionManager).setDataSource(dataSource);

        return transactionManager;
    }

}
