package com.liang.spring.core.transaction;


import com.liang.spring.core.util.ConnectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceTransactionManager extends AbstractTransactionManager {


    private ConnectionUtils connectionUtils;

    public void setConnectionUtils(ConnectionUtils connectionUtils) {
        this.connectionUtils = connectionUtils;
    }

    @Override
    public void beginTransaction() {

        /**
         * 判断当前线程中是否已经绑定连接，如果没有绑定，需要从连接池获取一个连接绑定到当前线程
         */
        try {
            Connection connection = connectionUtils.getCurrentThreadConn();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commit() {

        try {
            Connection currentThreadConn = connectionUtils.getCurrentThreadConn();
            currentThreadConn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void rollback() {

        try {
            Connection currentThreadConn = connectionUtils.getCurrentThreadConn();
            currentThreadConn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
