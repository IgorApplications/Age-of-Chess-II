package com.iapp.ageofchess.server.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iapp.lib.web.*;
import com.iapp.lib.util.DataChecks;
import com.iapp.lib.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Igor Ivanov
 * */
@Component
public class MetaDAO {

    private final JdbcTemplate jdbcTemplate;
    private final Gson gson;
    // 24 hours
    private static final long MAX_TIME_STORED_LOGIN = 24 * 60 * 60 * 1000;

    public MetaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        gson = new Gson();
    }

    public Pair<RequestStatus, Set<Flag>> getFlags(long accountId) {
        List<List<Punishment>> general = getPunishments(accountId);
        if (DataChecks.isBadList(general)) return new Pair<>(DataChecks.getBadStatus(general), null);

        Set<Flag> flags = general.get(0).stream()
                .map(Punishment::getType)
                .collect(Collectors.toSet());
        return new Pair<>(RequestStatus.DONE, flags);
    }

    public Pair<RequestStatus, Login> getLastLogin(long accountId) {
        List<List<Login>> general = getLogins(accountId);
        if (DataChecks.isBadList(general)) return new Pair<>(DataChecks.getBadStatus(general), null);
        List<Login> data = general.get(0);
        return new Pair<>(RequestStatus.DONE, data.get(data.size() - 1));
    }

    public RequestStatus addLogin(long accountId, Login login) {
        List<List<Login>> general = getLogins(accountId);
        if (general.size() == 0) general.add(new ArrayList<>());
        if (DataChecks.isBadList(general)) return DataChecks.getBadStatus(general);
        List<Login> logins = general.get(0);

        List<Login> result = new ArrayList<>();
        for (Login el : logins) {
            if (System.currentTimeMillis() - el.getTime() <= MAX_TIME_STORED_LOGIN) {
                result.add(el);
            }
        }

        result.add(login);
        jdbcTemplate.update("UPDATE Account SET logins=? WHERE id=?",
                gson.toJson(result), accountId);

        return RequestStatus.DONE;
    }

    public RequestStatus updateAvatar(long accountId, byte[] avatar) {
        jdbcTemplate.update("UPDATE Account SET avatar=? WHERE id=?",
                avatar, accountId);

        return RequestStatus.DONE;
    }

    public RequestStatus addPunishment(long accountId, Punishment punishment) {
        List<List<Punishment>> general = getPunishments(accountId);
        if (DataChecks.isBadList(general)) return DataChecks.getBadStatus(general);
        List<Punishment> punishments = general.get(0);
        // IMPORTANT!!!
        punishment.setId(getNewID(punishments));
        punishments.add(punishment);

        jdbcTemplate.update("UPDATE Account SET punishments=? WHERE id=?",
                gson.toJson(punishments), accountId);

        return RequestStatus.DONE;
    }

    public List<List<Login>> getLogins(long accountId) {
        return jdbcTemplate.query("SELECT * FROM Account WHERE id=?",
                new Object[]{accountId},
                (rs, rowNum) -> gson.fromJson(rs.getString("logins"),
                        new TypeToken<List<Login>>() {}.getType()));
    }

    public List<List<Punishment>> getPunishments(long accountId) {
        return jdbcTemplate.query("SELECT * FROM Account Where id=?",
                new Object[]{accountId},
                (rs, rowNum) ->
                        gson.fromJson(rs.getString("punishments"),
                                new TypeToken<List<Punishment>>() {}.getType()));
    }

    public List<byte[]> getAvatar(long accountId) {
        return jdbcTemplate.query("SELECT * FROM Account Where id=?",
                new Object[]{accountId},
                (rs, rowNum) -> rs.getBytes("avatar"));
    }

    private long getNewID(List<Punishment> punishments) {
        long maxId = -1;
        for (Punishment punishment : punishments) {
            maxId = Math.max(punishment.getId(), maxId);
        }
        return maxId + 1;
    }
}
