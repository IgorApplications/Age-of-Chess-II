package com.iapp.ageofchess.server.dao;

import com.google.gson.Gson;
import com.iapp.ageofchess.multiplayer.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GamesDAO {

    private final JdbcTemplate jdbcTemplate;
    private final List<Match> fastGames = new ArrayList<>();
    private final Gson gson;

    @Autowired
    public GamesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        gson = new Gson();
    }

    public Match createGame(Match match) {

        var newMatch = new Match(
                getNewID(),
                match.getName(),
                match.getSponsored(),
                match.getRankType(),
                match.getCreatorId(),
                -1,
                -1,
                match.getTimeByBlack(),
                match.getTimeByWhite(),
                match.getTimeByTurn(),
                match.getTurnMode(),
                match.getMaxTurn(),
                match.isRandom(),
                match.getFen()
        );
        fastGames.add(newMatch);

        return newMatch;
    }

    public void removeGame(long gameId) {
        fastGames.removeIf(match -> match.getId() == gameId);
    }

    public List<Match> readGames() {
        return fastGames;
    }

    public Optional<Match> getGame(long id) {
        for (var game : fastGames) {
            if (game.getId() == id) {
                return Optional.of(game);
            }
        }
        return Optional.empty();
    }

    private long getNewID() {
        long max = 1;
        for (var game : fastGames) {
            if (game.getId() > max) {
                max = game.getId();
            }
        }
        return max + 1;
    }
}
