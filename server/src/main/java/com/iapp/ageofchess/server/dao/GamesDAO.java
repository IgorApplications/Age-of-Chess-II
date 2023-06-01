package com.iapp.ageofchess.server.dao;

import com.google.gson.Gson;
import com.iapp.ageofchess.multiplayer.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Match database management
 * @author Igor Ivanov
 * All matches are stored in RAM
 * */
@Component
public class GamesDAO {

    private final JdbcTemplate jdbcTemplate;
    private final Gson gson;
    /** list of all created matches */
    private final List<Match> fastGames = new CopyOnWriteArrayList<>();

    @Autowired
    public GamesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        gson = new Gson();
    }

    /** creates a match */
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

    /** deletes the match */
    public void removeGame(long gameId) {
        fastGames.removeIf(match -> match.getId() == gameId);
    }

    /** returns a list of all matches */
    public List<Match> readGames() {
        return new CopyOnWriteArrayList<>(fastGames);
    }

    /** returns a specific match */
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
