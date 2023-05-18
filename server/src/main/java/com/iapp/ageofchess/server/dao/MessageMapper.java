package com.iapp.ageofchess.server.dao;

import com.iapp.lib.web.Message;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageMapper implements RowMapper<Message> {

    @Override
    public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
        Message message = new Message();

        message.setId(rs.getLong("id"));
        message.setPinned(rs.getBoolean("pinned"));
        message.setTime(rs.getLong("time"));
        message.setText(rs.getString("text"));
        message.setSenderId(rs.getLong("senderId"));

        return message;
    }
}
