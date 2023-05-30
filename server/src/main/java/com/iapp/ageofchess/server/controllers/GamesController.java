package com.iapp.ageofchess.server.controllers;

import com.google.gson.Gson;
import com.iapp.lib.chess_engine.Color;
import com.iapp.lib.chess_engine.Game;
import com.iapp.lib.chess_engine.Result;
import com.iapp.ageofchess.multiplayer.*;
import com.iapp.lib.web.*;
import com.iapp.ageofchess.server.dao.AccountDAO;
import com.iapp.ageofchess.server.dao.GamesDAO;
import com.iapp.lib.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Rest controller matches
 * @author Igor Ivanov
 * @version 1.0
 * */
@RestController
@RequestMapping("/api/v1/games")
public class GamesController {

    private static final Logger gamesLogger = LoggerFactory.getLogger(GamesController.class);
    private final AccountDAO accountDAO;
    private final GamesDAO gamesDAO;
    private final Gson gson;
    private Consumer<Match> onUpdate;

    // threads have access!
    private final List<MatchChessEngine> engineList = new CopyOnWriteArrayList<>();

    @Autowired
    public GamesController(GamesDAO gamesDAO, AccountDAO accountDAO) {
        this.gamesDAO = gamesDAO;
        this.accountDAO = accountDAO;
        gson = new Gson();
    }

    public void setOnUpdateMatch(Consumer<Match> onUpdate) {
        this.onUpdate = onUpdate;
    }

    // no auth --------------------------------------------------------------------------------------------------------

    public List<Match> getGames() {
        List<Match> matches = gamesDAO.readGames();
        for (Match match : matches) {
            match.getLobbyMessages().clear();
            match.getLobbyMessages().addAll(match.getLobby().readMainLobby());
        }
        return matches;
    }

    public Pair<RequestStatus, Match> getMatch(long gameId) {
        var op = gamesDAO.getGame(gameId);
        if (op.isEmpty()) return new Pair<>(RequestStatus.NOT_FOUND, null);
        Match match = op.get();
        match.getLobbyMessages().clear();
        match.getLobbyMessages().addAll(match.getLobby().readMainLobby());
        return new Pair<>(RequestStatus.DONE, op.get());
    }

    // only auth -------------------------------------------------------------------------------------------------------

    /** Also, make enter */
    public Pair<RequestStatus, String> create(long authId, String matchData) {

        var unsafetyUserMatch = gson.fromJson(matchData, Match.class);
        unsafetyUserMatch.setCreatorId(authId);

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return new Pair<>(accounts.getKey(), null);
        var acc = accounts.getValue();

        if (engineList.size() >= 5) return new Pair<>(RequestStatus.DENIED, "more than 5 matches already");
        var state = isCorrectMatch(acc, unsafetyUserMatch);
        if (!state.getKey()) return new Pair<>(RequestStatus.DENIED, state.getValue());

        acc.setCoins(acc.getCoins() - unsafetyUserMatch.getSponsored());
        accountDAO.updateServerAccount(acc);

        var safetyServerMatch = gamesDAO.createGame(unsafetyUserMatch);
        safetyServerMatch.setLobby(new Lobby(accountDAO::getServerAccount));
        enter(authId, safetyServerMatch.getId());
        safetyServerMatch.getLobbyMessages().clear();
        safetyServerMatch.getLobbyMessages().addAll(safetyServerMatch.getLobby().readMainLobby());
        gamesLogger.warn("Created match " + unsafetyUserMatch);

        return new Pair<>(RequestStatus.DONE, gson.toJson(safetyServerMatch));
    }

    public RequestStatus sendMessage(long authId, long gameId, String message) {

        var op = gamesDAO.getGame(gameId);
        if (op.isEmpty()) return RequestStatus.NOT_FOUND;

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        var acc = accounts.getValue();

        // only entered in users can send messages
        if (!op.get().getEntered().contains(acc.getId())) return RequestStatus.DENIED;

        Match match = op.get();
        return match.getLobby().sendLobby(acc, message);

    }

    public RequestStatus makeMove(long authId, long gameId, String fenMove) {

        var engines = findMatch(gameId);
        if (engines.size() > 1) {
            gamesLogger.error("security breach");
            return RequestStatus.SECURITY_BREACH;
        } else if (engines.isEmpty()) {
            return RequestStatus.NOT_FOUND;
        }
        var engine = engines.get(0);

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        var acc = accounts.getValue();

        // if the game is not started, the moves are disabled!
        if (!engine.getMatch().isStarted()) {
            return RequestStatus.DENIED;
        }

        // if you have not joined the match, then the moves are disabled!
        if (engine.getMatch().getWhitePlayerId() == acc.getId()
                || engine.getMatch().getBlackPlayerId() == acc.getId()) {
            return engine.makeMove(fenMove);
        }

        return RequestStatus.DENIED;

    }

    public RequestStatus enter(long authId, long gameId) {

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        var acc = accounts.getValue();

        var op = gamesDAO.getGame(gameId);
        if (op.isEmpty()) return RequestStatus.NOT_FOUND;

        var match = op.get();
        match.getEntered().add(acc.getId());
        match.getLobby().sendConnect(acc);

        return RequestStatus.DONE;
    }

    public RequestStatus exit(long authId, long gameId) {

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        var acc = accounts.getValue();

        var op = gamesDAO.getGame(gameId);
        if (op.isEmpty()) return RequestStatus.NOT_FOUND;

        var match = op.get();
        match.getEntered().remove(acc.getId());
        match.getLobby().sendDisconnect(acc);

        return RequestStatus.DONE;
    }

    public RequestStatus join(long authId, long gameId, String color) {

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        var acc = accounts.getValue();

        var op = gamesDAO.getGame(gameId);
        if (op.isEmpty()) return RequestStatus.NOT_FOUND;
        var match = op.get();

        // You can't join a match if you haven't entered in yet!
        if (!match.getEntered().contains(acc.getId())) return RequestStatus.DENIED;
        // if the match is running, then you can not join
        if (match.isStarted()) return RequestStatus.DENIED;

        Color colorObj;
        if (match.isRandom()) colorObj = getRandColor(match);
        else colorObj = Color.valueOf(color);

        // enters the selected color
        // the server sends the "join" command, indicates the color " [color]" if not random
        if (colorObj == Color.WHITE && match.getWhitePlayerId() == -1) {

            var colorStr = match.isRandom() ? "" : " white";
            match.setWhitePlayerId(authId);
            match.getLobby().sendJoin(acc, colorStr);

        } else if (colorObj == Color.BLACK && match.getBlackPlayerId() == -1) {

            var colorStr = match.isRandom() ? "" : " black";
            match.setBlackPlayerId(authId);
            match.getLobby().sendLobby(acc, colorStr);

        } else {
            return RequestStatus.DENIED;
        }

        return RequestStatus.DONE;

    }

    public RequestStatus disjoin(long authId, long  gameId) {

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        var acc = accounts.getValue();

        var op = gamesDAO.getGame(gameId);
        if (op.isEmpty()) return RequestStatus.NOT_FOUND;
        var match = op.get();
        if (match.isStarted()) return RequestStatus.DENIED;

        if (acc.getId() == match.getWhitePlayerId()) {
            match.setWhitePlayerId(-1);
        } else if (acc.getId() == match.getBlackPlayerId()) {
            match.setBlackPlayerId(-1);
        }
        match.getLobby().sendDisjoin(acc);

        return RequestStatus.DONE;
    }

    public RequestStatus start(long authId, long gameId) {

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        var acc = accounts.getValue();

        var op = gamesDAO.getGame(gameId);
        if (op.isEmpty()) return RequestStatus.NOT_FOUND;
        var match = op.get();

        // if the match is already running
        if (match.isStarted()) return RequestStatus.DENIED;

        // if the match is not complete or the user is not the creator or moderator
        if (match.getWhitePlayerId() == -1 || match.getBlackPlayerId() == -1
                || (match.getCreatorId() != acc.getId() && acc.getType().ordinal() < AccountType.MODERATOR.ordinal())) {
            return RequestStatus.DENIED;
        }

        var engine = new MatchChessEngine(match, accountDAO);
        engine.start();
        engineList.add(engine);

        // the server will send the command "start [fullname]"
        match.getLobby().sendStart(acc);

        return RequestStatus.DONE;

    }

    public RequestStatus removeMatch(long authId, long gameId) {

        var accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        var acc = accounts.getValue();

        var op = gamesDAO.getGame(gameId);
        if (op.isEmpty()) return RequestStatus.NOT_FOUND;
        var game = op.get();

        // it is forbidden for the creators of the match to stop the match
        // when it is already running,if it is not a moderator
        if (game.isStarted() && acc.getType().ordinal() < AccountType.MODERATOR.ordinal())
            return RequestStatus.DENIED;

        // the match can be deleted either by the creator of the match or the moderator
        if (acc.getId() != game.getCreatorId()
                && acc.getType().ordinal() < AccountType.MODERATOR.ordinal()) {
            return RequestStatus.DENIED;
        }

        engineList.removeIf(el -> el.getMatch().getId() == gameId);
        gamesDAO.removeGame(gameId);

        return RequestStatus.DONE;

    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * updates the state of the matches
     * */
    public void updateGames() {
        try {
            TimeUnit.MILLISECONDS.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        engineList.removeIf(engine -> {

            synchronized (engine.getMatch()) {

                // 10 minute after finish
                boolean result = engine.getMatch().getResult() != Result.NONE
                        && engine.getMatch().getFinishTime() != -1 &&
                        System.currentTimeMillis() - engine.getMatch().getFinishTime() > 600_000;

                // 5 minute after create
                result = result || (engine.getMatch().getWhitePlayerId() == -1
                        && engine.getMatch().getBlackPlayerId() == -1 && engine.getMatch().getEntered().isEmpty()
                        && System.currentTimeMillis() - engine.getMatch().getCreatedTime() > 300_000);

                if (result) {
                    gamesDAO.removeGame(engine.getMatch().getId());
                    gamesLogger.warn("Deleted match id = " + engine.getMatch().getId());
                }

                return result;
            }

        });

        for (var engine : engineList) {
            if (engine.getMatch().getResult() != Result.NONE) continue;

            synchronized (engine.getMatch()) {
                engine.updateTimer();
                engine.getMatch().getLobby().updateTime();
                onUpdate.accept(engine.getMatch());
            }
        }
    }

    private Color getRandColor(Match match) {
        if (match.getBlackPlayerId() != -1) return Color.WHITE;
        if (match.getWhitePlayerId() != -1) return Color.BLACK;

        var value = ThreadLocalRandom.current().nextInt(2);
        if (value == 0) return Color.BLACK;
        else return Color.WHITE;
    }

    private Pair<Boolean, String> isCorrectMatch(Account acc, Match match) {
        if (match.getRankType() == RankType.LONG)
            return new Pair<>(false, " temporarily banned slow matches");

        if (match.getRankType() != RankType.UNRANKED && !isCorrectRankType(match.getRankType(), match.getTimeByWhite()))
            return new Pair<>(false, " wrong time per game for rating type");

        if (match.getTimeByBlack() != match.getTimeByWhite())
            return new Pair<>(false, " unbalanced game time");

        if (match.getRankType() != RankType.UNRANKED
                && !match.getFen().equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq")) {
            return new Pair<>(false, "non-standard arrangement of chess pieces, this match must be non-rated!");
        }

        if (!Game.isValidFEN(match.getFen()))
            return new Pair<>(false, " incorrect match state, check FEN!");

        if (match.getSponsored() > acc.getCoins())
            return new Pair<>(false, "not enough coins");

        if (match.getMaxTurn() != -1 && match.getMaxTurn() < 5)
            return new Pair<>(false, " less than five moves per game is disabled");

        if (match.getRankType() == RankType.BULLET && match.getTimeByTurn() != -1)
            return new Pair<>(false, " in bullet mode, time per move is disabled");

        if (match.getRankType() == RankType.LONG && match.getTimeByWhite() != -1)
            return new Pair<>(false, " in long mode, time per game is disabled");

        if (match.getRankType() != RankType.LONG && match.getRankType() != RankType.RAPID
                && match.getTurnMode() == TurnMode.ALTERNATELY)
            return new Pair<>(false, " alternate mode is disabled in bullet and blitz mode");

        return new Pair<>(true, "");
    }

    private boolean isCorrectRankType(RankType rankType, long timeByPlayer) {
        if (timeByPlayer <= 180_000) { // 3 minutes
            return rankType == RankType.BULLET;
        } else if (timeByPlayer <= 600_000) { // 10 minutes
            return rankType == RankType.BLITZ;
        } else if (timeByPlayer <= 3_600_000) { // 60 minutes
            return rankType == RankType.RAPID;
        } else {
            return rankType == RankType.LONG;
        }
    }

    private List<MatchChessEngine> findMatch(long matchId) {
        return engineList.stream()
                .filter(engine -> engine.getMatch().getId() == matchId)
                .collect(Collectors.toList());
    }
}


