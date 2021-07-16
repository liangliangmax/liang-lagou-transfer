package com.liang.spring.core.transaction;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceTransactionManager extends AbstractTransactionManager {


    private ThreadLocal<Connection> threadLocal = new ThreadLocal<>(); // 存储当前线程的连接

    private DataSource dataSource;


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void beginTransaction() {

        /**
         * 判断当前线程中是否已经绑定连接，如果没有绑定，需要从连接池获取一个连接绑定到当前线程
         */
        Connection connection = threadLocal.get();
        if(connection == null) {
            // 从连接池拿连接并绑定到线程
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);

            } catch (SQLException e) {
                e.printStackTrace();
            }
            // 绑定到当前线程
            threadLocal.set(connection);
        }

    }

    @Override
    public void commit() {

        Connection connection = threadLocal.get();
        if(connection!=null){
            try {
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void rollback() {

        Connection connection = threadLocal.get();
        if(connection!=null){
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
