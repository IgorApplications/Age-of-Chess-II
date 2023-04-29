package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.chess_engine.Result;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerGameController;
import com.iapp.ageofchess.graphics.MultiplayerBoardView;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.screens.RdApplication;

class MultiplayerGameActivityV extends MultiplayerGameActivity {

    private Table information;
    private Cell<MultiplayerBoardView> boardCell;
    private RdLabel playerInfo;

    public MultiplayerGameActivityV(LocalMatch localMatch, Match match) {
        super(localMatch, match);
    }

    MultiplayerGameActivityV(MultiplayerGameActivity oldState, MultiplayerGameController controller) {
        super(oldState, controller);
    }

    @Override
    public void update() {
        super.update();

        var firstPlayer = controller.getFirstPlayer();
        var secondPlayer = controller.getSecondPlayer();

        String result;
        if (!controller.isInside() && controller.getCurrentMatch().isStarted()) {
            if (firstPlayer == null || secondPlayer == null) return;

            result = controller.getFirstPlayer().getFullName() +
                    " VS " + controller.getSecondPlayer().getFullName();
            playerInfo.setText(result);

        } else {
            if (secondPlayer == null) return;
            result = secondPlayer.getFullName();
        }
        playerInfo.setText(result);

        var userColor = controller.getUserColor();
        if (userColor.isPresent() && controller.getCurrentMatch().isStarted()) {
            var color = userColor.get();
            if (color != controller.getColorMove() && !controller.getCurrentMatch().isAlternately()
                    && controller.getCurrentMatch().getResult() == Result.NONE) {
                turnsLabel.setText(controller.getTurn() + "... " + controller.defineColorMove());
            } else {
                turnsLabel.setText(controller.getTurn() + ". " + controller.defineColorMove());
            }
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        updateLabels();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        var viewport = RdApplication.self().getViewport();

        var rectSize = Math.min(viewport.getWorldWidth(), viewport.getWorldHeight() - 330);
        if (rectSize > ChessConstants.localData.getMaxBoardSize()) rectSize = ChessConstants.localData.getMaxBoardSize();
        if (boardCell != null) {
            resizeBoard(boardCell, rectSize);
        }

        if (infoDialog != null) {
            infoDialog.setPosition(
                    getStage().getWidth() / 2f - infoDialog.getWidth() / 2,
                    getStage().getHeight() / 2f + rectSize / 2f - infoDialog.getHeight());
        }

        if (information != null) {
            information.setPosition(0, viewport.getWorldHeight() - 150);
            information.setSize(viewport.getWorldWidth(), 120);

            timeByTurnLabel.setPosition(20, viewport.getWorldHeight() - 150);
            whiteTime.setPosition(viewport.getWorldWidth() - 150, viewport.getWorldHeight() - 100);
            blackTime.setPosition(viewport.getWorldWidth() - 150, viewport.getWorldHeight() - 150);
        }
    }

    @Override
    public void initActors() {
        super.initActors();

        information = new Table();
        information.setBackground(new TextureRegionDrawable(ChessAssetManager.current().getBlackTexture()));
        information.align(Align.center);

        playerInfo = new RdLabel("");

        information.add(playerInfo).row();
        information.add(turnsLabel);

        getStage().addActor(information);
        getStage().addActor(timeByTurnLabel);
        getStage().addActor(whiteTime);
        getStage().addActor(blackTime);
        // Control Game
        initControlTable();
        getStage().addActor(chatView);
        getStage().addActor(blackout);

        var buttons = new Table();
        buttons.add(menu).padRight(5).size(125);
        boardCell = content.add(gameBoard).padTop(120).center();
        content.row();
        content.add(buttons).padTop(10).center();

        if (selectionDialog != null) getStage().addActor(selectionDialog);
        if (resultDialog != null) getStage().addActor(resultDialog);
        if (infoDialog != null) getStage().addActor(infoDialog);
        if (menuDialog != null) getStage().addActor(menuDialog);
        if (statisticDialog != null) getStage().addActor(statisticDialog);

        controller.setActivity(this);
        updateLabels();
    }

}