package com.iapp.lib.chess_engine;

import com.badlogic.gdx.Gdx;
import com.iapp.lib.ui.screens.RdApplication;

import java.util.function.Consumer;

public class IgorChessEngineAdapter implements ChessEngine {

    private static final java.util.Map<Integer, Character> vertical = java.util.Map.of(
            0, 'a', 1, 'b',
            2,'c',  3,'d',
            4,'e',  5,'f',
            6,'g',  7, 'h');

    private IgorChessEngine engine;
    private Game game;

    @Override
    public void start() {
        engine = new IgorChessEngine();
    }

    @Override
    public void setFen(String fen) {
        game = new Game(Color.BLACK, fen);
    }

    @SuppressWarnings("DefaultLocale")
    @Override
    public void getBestMoves(int depth, Consumer<String> onGetting, long minDelayMillis) {
        Runnable task = () -> {
            long start = System.currentTimeMillis();

            engine.getMove(game, depth, game.getColorMove(), (move, typePiece) -> {
                String textMoves = getFenMove(
                        Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
                        move.getMoveX(), 7 - move.getMoveY()));

                // updating a piece always occurs on the queen
                if (game.isUpdated(move)) {
                    textMoves += game.getColorMove() == Color.BLACK ? "q" : "Q";
                }

                Gdx.app.debug("IgorChessEngine get best move",
                    String.format("Depth-%d got move in %d milliseconds%n", depth, (System.currentTimeMillis() - start)));
                Gdx.app.debug("IgorChessEngine get best move",
                    String.format("Text move = %s%n", textMoves));

                long left = minDelayMillis - (System.currentTimeMillis() - start);

                try {
                    if (left > 0) Thread.sleep(left);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }

                String finalTextMoves = textMoves;
                RdApplication.postRunnable(() -> onGetting.accept(finalTextMoves));
            });
        };
        RdApplication.self().execute(task);
    }

    @Override
    public void stop() {
        if (engine != null) engine.interrupt();
        engine = null;
    }

    private String getFenMove(Move move) {
        String transition = "";

        transition += vertical.get((int) move.getPieceX());
        transition += String.valueOf(move.getPieceY() + 1);
        transition += vertical.get((int) move.getMoveX());
        transition += String.valueOf(move.getMoveY() + 1);

        return transition;
    }
}
