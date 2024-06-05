package com.iapp.lib.chess_engine;

import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;
import com.badlogic.gdx.Gdx;
import com.iapp.lib.ui.screens.RdApplication;

import java.util.function.Consumer;

public class CarballoChessEngine implements ChessEngine {

    private SearchEngine searchEngine;

    public void start() {
        Config config = new Config();
        config.setTranspositionTableSize(10);
        searchEngine = new SearchEngine(config);
    }

    public void setFen(String fen) {
        searchEngine.getBoard().setFen(fen);
    }

    @SuppressWarnings("DefaultLocale")
    public void getBestMoves(int depth, Consumer<String> onGetting, long minimumDelayMillis) {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setDepth(depth);

        Runnable task = () -> {
            long start = System.currentTimeMillis();
            searchEngine.go(searchParams);
            int move = searchEngine.getBestMove();
            searchEngine.getBoard().doMove(move);
            String textMoves = searchEngine.getBoard().getMoves();
            if (textMoves.equals("")) return;

            Gdx.app.debug("Carballo get best move",
                String.format("Depth-%d got move in %d milliseconds%n", depth, (System.currentTimeMillis() - start)));
            Gdx.app.debug("Carballo get best move",
                String.format("Text move = %s%n", textMoves));

            long left = minimumDelayMillis - (System.currentTimeMillis() - start);
            try {
                if (left > 0) Thread.sleep(left);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            RdApplication.postRunnable(() -> onGetting.accept(textMoves));
        };
        RdApplication.self().execute(task);
    }

    @Override
    public void stop() {
        searchEngine.stop();
        searchEngine.destroy();
    }
}
