package com.iapp.ageofchess.server.dao;

import com.iapp.lib.web.Account;
import com.iapp.lib.web.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author Igor Ivanov
 * @version 1.0
 * */
@Component
public class MainChatDAO {

    private static final int maxRows = 200;
    private static final int returnRows = 70;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MainChatDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clearOldMessages() {
        // TODO
        //jdbcTemplate.update("DELETE FROM Message " +
          //  "WHERE id < (SELECT MAX(id) - 200 FROM (SELECT * FROM Message) tmp)",
            //maxRows);
    }

    /** sends a message */
    public void send(Account account, String text) {
        jdbcTemplate.update("INSERT INTO Message VALUES" +
                        "(?, ?, ?, ?, ?)",
                getNewID(), false,
                new Date().getTime(), text,
                account.getId());
    }

    /** reads all messages */
    public List<Message> readMessages() {
        return jdbcTemplate.query("SELECT * FROM Message LIMIT " + returnRows,
            new MessageMapper());
    }

    /** get message by id */
    public List<Message> getMessage(long id) {
        return jdbcTemplate.query("SELECT * FROM Message WHERE id=?",
                new Object[]{id}, new MessageMapper());
    }

    /** delete message by id */
    public void removeMessage(long id) {
        jdbcTemplate.update("DELETE FROM Message WHERE id=?", id);
    }

    private long getNewID() {
        return jdbcTemplate.query("SELECT * FROM Message ORDER BY id DESC LIMIT 1", new MessageMapper())
                .stream().map(Message::getId).findFirst().orElse(-1L) + 1;
    }
}
