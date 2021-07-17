package com.lagou.edu.config;

import com.liang.spring.core.annotation.Autowired;
import com.liang.spring.core.annotation.Bean;
import com.liang.spring.core.annotation.Configuration;
import com.liang.spring.core.transaction.DataSourceTransactionManager;
import com.liang.spring.core.transaction.TransactionManager;
import com.liang.spring.core.util.ConnectionUtils;

import javax.sql.DataSource;

@Configuration
public class TransactionConfig {

    @Autowired
    private DataSource dataSource;

    public ConnectionUtils connectionUtils(){
        ConnectionUtils connectionUtils = new ConnectionUtils();
        connectionUtils.setDataSource(dataSource);

        return connectionUtils;
    }

    @Bean
    public TransactionManager transactionManager(){
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setConnectionUtils(connectionUtils());
        return transactionManager;
    }

}
