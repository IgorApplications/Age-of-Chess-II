package com.iapp.ageofchess.multiplayer;

import com.iapp.lib.chess_engine.Move;
import com.iapp.lib.chess_engine.Result;
import com.iapp.lib.chess_engine.TypePiece;
import com.iapp.lib.util.Pair;
import com.iapp.lib.web.Lobby;
import com.iapp.lib.web.LobbyMessage;
import com.iapp.lib.web.RankType;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Match {

    private long id;
    private String name;
    private long sponsored;
    private final List<Long> entered = new CopyOnWriteArrayList<>();
    private long creatorId;
    private long whitePlayerId;
    private long blackPlayerId;
    private long timeByWhite;
    private long timeByBlack;
    private long timeByTurn;
    private TurnMode turnMode;
    private int turn;
    private int maxTurn;
    private boolean started;
    private Result result = Result.NONE;
    private String fen;
    private List<Pair<Move, TypePiece>> moves = new CopyOnWriteArrayList<>();
    private long finishTime = -1, createdTime = -1;
    private RankType rankType;
    private double rankPlus, rankMinus;
    private boolean random;
    private final List<LobbyMessage> lobbyMessages = new CopyOnWriteArrayList<>();
    private boolean alternately;
    private transient Lobby lobby;

    public Match() {}

    public Match(long id, String name, long sponsored, RankType rankType,
                 long creatorId, long whitePlayerId, long blackPlayerId, long timeByWhite,
                 long timeByBlack, long timeByTurn, TurnMode turnMode, int maxTurn, boolean random,
                 String fen) {
        this.id = id;
        this.name = name;
        this.rankType = rankType;
        this.sponsored = sponsored;
        this.creatorId = creatorId;
        this.whitePlayerId = whitePlayerId;
        this.blackPlayerId = blackPlayerId;
        this.timeByWhite = timeByWhite;
        this.timeByBlack = timeByBlack;
        this.timeByTurn = timeByTurn;
        this.turnMode = turnMode;
        createdTime = System.currentTimeMillis();
        turn = 1;
        this.maxTurn = maxTurn;
        started = false;
        this.random = random;
        result = Result.NONE;
        this.fen = fen;
    }

    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public void setAlternately(boolean alternately) {
        this.alternately = alternately;
    }

    public boolean isAlternately() {
        return alternately;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public List<LobbyMessage> getLobbyMessages() {
        return lobbyMessages;
    }

    public void setRankPlus(double rankPlus) {
        this.rankPlus = rankPlus;
    }

    public void setRankMinus(double rankMinus) {
        this.rankMinus = rankMinus;
    }

    public double getRankPlus() {
        return rankPlus;
    }

    public double getRankMinus() {
        return rankMinus;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public RankType getRankType() {
        return rankType;
    }

    public void setRankType(RankType rankType) {
        this.rankType = rankType;
    }

    public long getSponsored() {
        return sponsored;
    }

    public void setSponsored(long coins) {
        this.sponsored = sponsored;
    }

    public List<Long> getEntered() {
        return entered;
    }

    public long getTimeByWhite() {
        return timeByWhite;
    }

    public void setTimeByWhite(long timeByWhite) {
        this.timeByWhite = timeByWhite;
    }

    public long getTimeByBlack() {
        return timeByBlack;
    }

    public void setTimeByBlack(long timeByBlack) {
        this.timeByBlack = timeByBlack;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWhitePlayerId() {
        return whitePlayerId;
    }

    public void setWhitePlayerId(long whitePlayerId) {
        this.whitePlayerId = whitePlayerId;
    }

    public long getBlackPlayerId() {
        return blackPlayerId;
    }

    public void setBlackPlayerId(long blackPlayerId) {
        this.blackPlayerId = blackPlayerId;
    }

    public long getTimeByTurn() {
        return timeByTurn;
    }

    public void setTimeByTurn(long timeByTurn) {
        this.timeByTurn = timeByTurn;
    }

    public TurnMode getTurnMode() {
        return turnMode;
    }

    public void setTurnMode(TurnMode turnMode) {
        this.turnMode = turnMode;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getMaxTurn() {
        return maxTurn;
    }

    public void setMaxTurn(int maxTurn) {
        this.maxTurn = maxTurn;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public List<Pair<Move, TypePiece>> getMoves() {
        return moves;
    }

    public void setMoves(List<Pair<Move, TypePiece>> moves) {
        this.moves = moves;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return "Match{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sponsored=" + sponsored +
                ", entered=" + entered +
                ", creatorId=" + creatorId +
                ", whitePlayerId=" + whitePlayerId +
                ", blackPlayerId=" + blackPlayerId +
                ", timeByWhite=" + timeByWhite +
                ", timeByBlack=" + timeByBlack +
                ", timeByTurn=" + timeByTurn +
                ", turnMode=" + turnMode +
                ", turn=" + turn +
                ", maxTurn=" + maxTurn +
                ", started=" + started +
                ", result=" + result +
                ", fen='" + fen + '\'' +
                ", moves=" + moves +
                ", finishTime=" + finishTime +
                ", createdTime=" + createdTime +
                ", rankType=" + rankType +
                ", rankPlus=" + rankPlus +
                ", rankMinus=" + rankMinus +
                ", random=" + random +
                ", lobby=" + lobbyMessages +
                ", alternately=" + alternately +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return id == match.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
