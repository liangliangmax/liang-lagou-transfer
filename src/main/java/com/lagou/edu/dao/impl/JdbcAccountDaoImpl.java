package com.lagou.edu.dao.impl;

import com.lagou.edu.pojo.Account;
import com.lagou.edu.dao.AccountDao;
import com.liang.spring.core.util.ConnectionUtils;
import com.liang.spring.core.annotation.Autowired;
import com.liang.spring.core.annotation.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author 应癫
 */
@Repository
public class JdbcAccountDaoImpl implements AccountDao {

    @Autowired
    private ConnectionUtils connectionUtils;

    @Override
    public Account queryAccountByCardNo(String cardNo) throws Exception {
        //从连接池获取连接
        Connection con = connectionUtils.getCurrentThreadConn();
        String sql = "select * from account where cardNo=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1,cardNo);
        ResultSet resultSet = preparedStatement.executeQuery();

        Account account = new Account();
        while(resultSet.next()) {
            account.setCardNo(resultSet.getString("cardNo"));
            account.setName(resultSet.getString("name"));
            account.setMoney(resultSet.getInt("money"));
        }

        resultSet.close();
        preparedStatement.close();

        return account;
    }

    @Override
    public int updateAccountByCardNo(Account account) throws Exception {

        Connection con = connectionUtils.getCurrentThreadConn();
        String sql = "update account set money=? where cardNo=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1,account.getMoney());
        preparedStatement.setString(2,account.getCardNo());
        int i = preparedStatement.executeUpdate();

        preparedStatement.close();

        return i;
    }
}
