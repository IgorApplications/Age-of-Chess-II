package com.iapp.ageofchess.chess_engine;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Move implements Serializable {

    private static final long serialVersionUID = 5;
    private static final Move[][][][] cache = new Move[10][10][10][10];

    private final byte pieceX, pieceY;
    private final byte moveX, moveY;

    public static Move valueOf(int pieceX, int pieceY, int moveX, int moveY) {
        if (cache[0][0][0][0] == null) {
            for (int i = -1; i < 9; i++) {
                for (int j = -1; j < 9; j++) {
                    for (int k = -1; k < 9; k++) {
                        for (int m = -1; m < 9; m++) {
                            cache[i + 1][j + 1][k + 1][m + 1] = new Move(j, i, m, k);
                        }
                    }
                }
            }
        }

        return cache[pieceY + 1][pieceX + 1][moveY + 1][moveX + 1];
    }

    private Move(int pieceX, int pieceY, int moveX, int moveY) {
        this.pieceX = (byte) pieceX;
        this.pieceY = (byte) pieceY;
        this.moveX = (byte) moveX;
        this.moveY = (byte) moveY;
    }

    public byte getPieceX() {
        return pieceX;
    }

    public byte getPieceY() {
        return pieceY;
    }

    public byte getMoveX() {
        return moveX;
    }

    public byte getMoveY() {
        return moveY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return pieceX == move.pieceX && pieceY == move.pieceY && moveX == move.moveX && moveY == move.moveY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceX, pieceY, moveX, moveY);
    }

    @Override
    public String toString() {
        return "Move{" +
                "pieceX=" + pieceX +
                ", pieceY=" + pieceY +
                ", moveX=" + moveX +
                ", moveY=" + moveY +
                '}';
    }
}
