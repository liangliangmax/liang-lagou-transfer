package com.liang.spring.core.transaction;


public interface TransactionManager {


    void beginTransaction();


    void commit();

    void rollback();


}
