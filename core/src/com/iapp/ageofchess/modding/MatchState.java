package com.iapp.ageofchess.modding;

import com.iapp.lib.chess_engine.Game;
import com.iapp.lib.chess_engine.Move;
import com.iapp.lib.chess_engine.Result;

import java.util.LinkedList;

/**
 * Backwards compatible!
 * @version 1.0
 * @author Igor Ivanov
 * */
public class MatchState {

    private LocalMatch localMatch;
    private Result result;
    private Game game;
    private LinkedList<Move> lastMoves;
    private long timeBlack, timeWhite, timeByTurn;
    private boolean moveDone;

    public MatchState(LocalMatch localMatch, Game game, Result result, LinkedList<Move> lastMoves,
                      long timeBlack, long timeWhite, long timeByTurn, boolean moveDone) {
        this.localMatch = localMatch;
        this.game = game;
        this.result = result;
        this.lastMoves = lastMoves;
        this.timeBlack = timeBlack;
        this.timeWhite = timeWhite;
        this.timeByTurn = timeByTurn;
        this.moveDone = moveDone;
    }

    public void setMatch(LocalMatch localMatch) {
        this.localMatch = localMatch;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void setTimeBlack(long timeBlack) {
        this.timeBlack = timeBlack;
    }

    public void setTimeWhite(long timeWhite) {
        this.timeWhite = timeWhite;
    }

    public void setTimeByTurn(long timeByTurn) {
        this.timeByTurn = timeByTurn;
    }

    public LocalMatch getMatch() {
        return localMatch;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Result getResult() {
        return result;
    }

    public LinkedList<Move> getLastMoves() {
        return lastMoves;
    }

    public void setLastMoves(LinkedList<Move> lastMoves) {
        this.lastMoves = lastMoves;
    }

    public long getTimeBlack() {
        return timeBlack;
    }

    public long getTimeWhite() {
        return timeWhite;
    }

    public long getTimeByTurn() {
        return timeByTurn;
    }

    public boolean isMoveDone() {
        return moveDone;
    }

    public void setMoveDone(boolean moveDone) {
        this.moveDone = moveDone;
    }

    @Override
    public String toString() {
        return "MapState{" +
                "match=" + localMatch +
                ", result=" + result +
                ", game=" + game +
                ", lastMoves=" + lastMoves +
                '}';
    }
}
