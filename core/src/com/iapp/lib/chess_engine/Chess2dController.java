package com.iapp.lib.chess_engine;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.iapp.lib.util.Pair;

public interface Chess2dController {

    byte[][] getMatrix();

    TextureAtlas.AtlasRegion getRegion(byte type);

    TextureAtlas.AtlasRegion getRegion(String name);

    Pair<Integer, Integer> getCheckKing();

    boolean isCastleMove(Move move);

    boolean isCage(byte type);

    Array<Move> getMoves(int pieceX, int pieceY);

    void makeMove(Move move, TypePiece updated);

    float getPadLeft();

    float getPadRight();

    float getPadBottom();

    float getPadTop();

    float getWidth();

    float getHeight();
}
