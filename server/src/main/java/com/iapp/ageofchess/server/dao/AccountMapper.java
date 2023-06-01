package com.iapp.ageofchess.server.dao;

import com.iapp.lib.web.Account;
import com.iapp.lib.web.AccountType;
import com.iapp.lib.web.Gender;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * converts the database representation of an account into an object
 * Doesn't take into account avatar, online status, punishments, logins
 * */
public class AccountMapper implements RowMapper<Account> {

    @Override
    public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
        Account account = new Account();

        account.setId(rs.getLong("id"));
        account.setCoins(rs.getLong("coins"));
        account.setBullet(rs.getLong("bullet"));
        account.setRapid(rs.getLong("rapid"));
        account.setBlitz(rs.getLong("blitz"));
        account.setLongRank(rs.getLong("long"));
        account.setUsername(rs.getString("name"));
        account.setFullName(rs.getString("userName"));
        account.setPassword(rs.getString("password"));
        account.setCountry(rs.getString("country"));
        account.setGender(Gender.valueOf(rs.getString("gender")));
        account.setQuote(rs.getString("quote"));
        account.setDateBirth(rs.getLong("dateBirth"));
        account.setType(AccountType.valueOf(rs.getString("type")));
        account.setCreated(rs.getLong("created"));

        return account;
    }
}
