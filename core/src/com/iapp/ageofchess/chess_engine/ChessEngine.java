package com.iapp.ageofchess.chess_engine;

import java.util.function.Consumer;

public interface ChessEngine {

    void start();

    void setFen(String fen);

    void getBestMoves(int depth, Consumer<String> onGetting, long minDelayMillis);

    void stop();
}
