package com.iapp.ageofchess.chess_engine;

import com.badlogic.gdx.utils.Array;
import com.iapp.rodsher.screens.RdApplication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

class IgorChessEngine {

    public static final short PAWN = 10;
    public static final short ROOK = 50;
    public static final short KNIGHT = 30;
    public static final short BISHOP = 30;
    public static final short QUEEN = 90;
    public static final short KING = 900;

    private final float[][] whitePawnEval = {
            {0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            {5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f},
            {1.0f, 1.0f, 2.0f, 3.0f, 3.0f, 2.0f, 1.0f, 1.0f},
            {0.5f, 0.5f, 1.0f, 2.5f, 2.5f, 1.0f, 0.5f, 0.5f},
            {0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f},
            {0.5f, -0.5f, -1.0f, 0.0f, 0.0f, -1.0f, -0.5f, 0.5f},
            {0.5f, 1.0f, 1.0f, -2.0f, -2.0f, 1.0f, 1.0f, 0.5f},
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}
    };

    private final float[][] blackPawnEval = reverseMatrix(whitePawnEval);

    private final float[][] knightEval = {
            {-5.0f, -4.0f, -3.0f, -3.0f, -3.0f, -3.0f, -4.0f, -5.0f},
            {-4.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, -2.0f, -4.0f},
            {-3.0f, 0.0f, 1.0f, 1.5f, 1.5f, 1.0f, 0.0f, -3.0f},
            {-3.0f, 0.5f, 1.5f, 2.0f, 2.0f, 1.5f, 0.5f, -3.0f},
            {-3.0f, 0.0f, 1.5f, 2.0f, 2.0f, 1.5f, 0.0f, -3.0f},
            {3.0f, 0.5f, 1.0f, 1.5f, 1.5f, 1.0f, 0.5f, -3.0f},
            {-4.0f, -2.0f, 0.0f, 0.5f, 0.5f, 0.0f, -2.0f, -4.0f},
            {-5.0f, -4.0f, -3.0f, -3.0f, -3.0f, -3.0f, -4.0f, -5.0f}
    };

    private final float[][] whiteBishopEval = {
            {-2.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -2.0f},
            {-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f},
            {-1.0f, 0.0f, 0.5f, 1.0f, 1.0f, 0.5f, 0.0f, -1.0f},
            {-1.0f, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, 0.5f, -1.0f},
            {-1.0f, 0.0f, 1.0f,  1.0f,  1.0f, 1.0f, 0.0f, -1.0f},
            {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f},
            {-1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, -1.0f},
            {-2.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -2.0f}
    };

    private final float[][] blackBishopEval = reverseMatrix(whiteBishopEval);

    private final float[][] whiteRookEval = {
            {0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            {0.5f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.5f},
            {-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.5f},
            {-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.5f},
            {-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.5f},
            {-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.5f},
            {-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.5f},
            {0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, 0.0f, 0.0f}
    };

    private final float[][] blackRookEval = reverseMatrix(whiteRookEval);

    private final float[][] queenEval = {
            {-2.0f, -1.0f, -1.0f, -0.5f, -0.5f, -1.0f, -1.0f, -2.0f},
            {-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f},
            {-1.0f, 0.0f, 0.5f, 0.5f, 0.5f, 0.5f, 0.0f, -1.0f},
            {-0.5f, 0.0f, 0.5f, 0.5f, 0.5f, 0.5f, 0.0f, -0.5f},
            {0.0f, 0.0f, 0.5f, 0.5f, 0.5f, 0.5f, 0.0f, -0.5f},
            {-1.0f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.0f, -1.0f},
            {-1.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f},
            {-2.0f, -1.0f, -1.0f, -0.5f, -0.5f, -1.0f, -1.0f, -2.0f}
    };

    private final float[][] whiteKingEval = {
            {-3.0f, -4.0f, -4.0f, -5.0f, -5.0f, -4.0f, -4.0f, -3.0f},
            {-3.0f, -4.0f, -4.0f, -5.0f, -5.0f, -4.0f, -4.0f, -3.0f},
            {-3.0f, -4.0f, -4.0f, -5.0f, -5.0f, -4.0f, -4.0f, -3.0f},
            {-3.0f, -4.0f, -4.0f, -5.0f, -5.0f, -4.0f, -4.0f, -3.0f},
            {-2.0f, -3.0f, -3.0f, -4.0f, -4.0f, -3.0f,-3.0f, -2.0f},
            {-1.0f, -2.0f, -2.0f, -2.0f, -2.0f, -2.0f, -2.0f, -1.0f},
            {2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 2.0f},
            {2.0f, 3.0f, 1.0f, 0.0f, 0.0f, 1.0f, 3.0f, 2.0f}
    };

    private final float[][] blackKingEval = reverseMatrix(whiteKingEval);

    private final AtomicInteger countMinimaxThreads;
    private final Map<Move, Integer> minimaxResult;
    private Color aiColor, userColor;

    public IgorChessEngine() {
        countMinimaxThreads = new AtomicInteger(0);
        minimaxResult = RdApplication.self().getLauncher().concurrentHashMap();
    }

    public void getMove(Game game, int depth, Color aiColor, OnGettingMove callback) {
        this.aiColor = aiColor;
        userColor = reverse(aiColor);
        countMinimaxThreads.set(0);
        minimaxResult.clear();

        Game clonedGame = game.cloneGame();
        Runnable task = () -> {
            try {
                int bestMove = Integer.MIN_VALUE;
                Move virtualAIMove = null;

                for (Move move : getAllMoves(clonedGame, aiColor)) {
                    clonedGame.makeMove(move);
                    getParallelMiniMax(clonedGame.cloneGame(), move, depth - 1, -10_000, 10_000, userColor);
                    countMinimaxThreads.incrementAndGet();
                    clonedGame.cancelMove();
                }

                while (countMinimaxThreads.get() != 0) {
                    Thread.yield();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                        return;
                    }
                }

                for (Map.Entry<Move, Integer> pair : minimaxResult.entrySet()) {
                    if (pair.getValue() >= bestMove) {
                        bestMove = pair.getValue();
                        virtualAIMove = pair.getKey();
                    }
                }

                if (virtualAIMove != null) {
                    callback.onGetting(virtualAIMove, TypePiece.QUEEN);
                }
            } catch (RejectedExecutionException e) {
                e.printStackTrace(System.out);
            }
        };
        RdApplication.self().execute(task);
    }

    public void interrupt() {}

    private void getParallelMiniMax(Game cloneGame, Move move, int depth, int alpha, int beta, Color userColor) {
        Runnable task = () -> {
            try {
                minimaxResult.put(move, getMiniMax(cloneGame, depth, alpha, beta, userColor));
            } catch (InterruptedException | RejectedExecutionException e) {
                e.printStackTrace(System.out);
            }
            countMinimaxThreads.decrementAndGet();
        };
        RdApplication.self().execute(task);
    }

    private int getMiniMax(Game clonedGame, int depth, int alpha, int beta, Color color) throws InterruptedException  {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

        if (depth == 0) {
            return evaluateBoard(clonedGame);
        }

        Array<Move> moves = getAllMoves(clonedGame, color);

        int bestMove;
        if (color == aiColor) {
            bestMove = Integer.MIN_VALUE;

            for (Move move : moves) {
                clonedGame.makeMove(move);

                int value = getMiniMax(clonedGame,depth - 1, alpha, beta, clonedGame.reverse(color));
                bestMove = Math.max(bestMove, value);

                clonedGame.cancelMove();

                alpha = Math.max(alpha, bestMove);
                if (beta <= alpha) {
                    return bestMove;
                }
            }
        } else {
            bestMove = Integer.MAX_VALUE;

            for (Move move : moves) {
                clonedGame.makeMove(move);

                int value = getMiniMax(clonedGame, depth - 1, alpha, beta, clonedGame.reverse(color));
                bestMove = Math.min(bestMove, value);

                clonedGame.cancelMove();

                beta = Math.min(beta, bestMove);
                if (beta <= alpha) {
                    return bestMove;
                }
            }
        }

        return bestMove;
    }

    private int evaluateBoard(Game game) {
        int totalEvaluation = 0;

        byte[][] matrix = game.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                totalEvaluation += evaluateFigure(game, j, i);
            }
        }
        return totalEvaluation;
    }

    private float evaluateFigure(Game game, int x, int y) {
        float eval = 0;

        if (game.isPawn(x, y)) {
            if (y == 7 || y == 0) eval =  90 + queenEval[y][x];
            else eval = PAWN + (game.getColor(x, y) == Color.BLACK ? blackPawnEval[y][x] : whitePawnEval[y][x]);
        } else if (game.isRook(x, y)) {
            eval = ROOK + (game.getColor(x, y) == Color.BLACK ? blackRookEval[y][x] : whiteRookEval[y][x]);
        } else if (game.isKnight(x, y)) {
            eval = KNIGHT + knightEval[y][x];
        } else if (game.isBishop(x, y)) {
            eval = BISHOP + (game.getColor(x, y) == Color.BLACK ? blackBishopEval[y][x] : whiteBishopEval[y][x]);
        } else if (game.isQueen(x, y)) {
            eval =  QUEEN + queenEval[y][x];
        } else if (game.isKing(x, y)) {
            eval = KING + (game.getColor(x, y) == Color.BLACK ? blackKingEval[y][x] : whiteKingEval[y][x]);
        }

        return game.getColor(x, y) == aiColor ? eval : -eval;
    }

    private Array<Move> getAllMoves(Game game, Color color) {
        Array<Move> moves = new Array<>();
        byte[][] matrix = game.getMatrix();

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (game.getColor(j, i) == color) {
                    moves.addAll(game.getMoves(j, i));
                }
            }
        }

        return moves;
    }

    private float[][] reverseMatrix(float[][] matrix) {
        float[][] reversedMatrix = new float[matrix.length][matrix[0].length];

        for (int i = matrix.length - 1; i >= 0; i--) {
            System.arraycopy(matrix[i], 0, reversedMatrix[matrix.length - 1 - i], 0, matrix[i].length);
        }
        return reversedMatrix;
    }

    private Color reverse(Color first) {
        return first == Color.BLACK ? Color.WHITE : Color.BLACK;
    }
}
