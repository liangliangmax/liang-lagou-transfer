package com.liang.spring.core.transaction;


import javax.sql.DataSource;

public class DataSourceTransactionManager extends AbstractTransactionManager {

    private DataSource dataSource;


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }
}
