package com.iapp.lib.chess_engine;

@FunctionalInterface
public interface OnGettingMove {

    void onGetting(Move move, TypePiece typePiece);
}
