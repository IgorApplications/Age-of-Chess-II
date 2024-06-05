package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.lib.chess_engine.Result;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerGameController;
import com.iapp.ageofchess.graphics.ControlGameView;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.widgets.BoardView;
import com.iapp.lib.util.WindowUtil;

class MultiplayerGameActivityV extends MultiplayerGameActivity {

    private Table information;
    private Cell<BoardView> boardCell;
    private RdLabel playerInfo;
    private RdTable buttons;

    public MultiplayerGameActivityV(LocalMatch localMatch, Match match) {
        super(localMatch, match);
    }

    MultiplayerGameActivityV(MultiplayerGameActivity oldState, MultiplayerGameController controller) {
        super(oldState, controller);
    }

    @Override
    public void update() {
        super.update();
        buttons.setVisible(controller.getCurrentMatch().isStarted());

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
        }
    }

    @Override
    public void initActors() {
        super.initActors();

        controlGame = new ControlGameView(controller, controlMenu);
        controlGame.align(Align.bottom);
        controlGame.setFillParent(true);

        playerInfo = new RdLabel("");
        RdTable first = new RdTable();
        first.add(timeByTurnLabel);
        RdTable second = new RdTable();
        second.add(whiteTime).row();
        second.add(blackTime).row();
        RdTable third = new RdTable();
        third.add(playerInfo).row();
        third.add(turnsLabel);

        information = new Table();
        information.setBackground(new TextureRegionDrawable(ChessAssetManager.current().getBlackTexture()));
        information.align(Align.center);
        information.add(first).padLeft(10);
        information.add(second).padLeft(30);
        information.add(third).expandX().padRight(210);

        getStage().addActor(information);
        getStage().addActor(controlGame);
        getStage().addActor(blackout);

        buttons = new RdTable();
        buttons.add(menu).padRight(5).size(125);
        buttons.add(settings).padRight(5).size(125);

        boardCell = content.add(gameBoard).padTop(120).center();
        content.row();
        content.add(buttons).padTop(10).center();
        buttons.setVisible(false);

        if (!WindowUtil.isHidden(selectionDialog)) getStage().addActor(selectionDialog);
        if (!WindowUtil.isHidden(resultDialog))    getStage().addActor(resultDialog);
        if (!WindowUtil.isHidden(infoDialog))      getStage().addActor(infoDialog);
        if (!WindowUtil.isHidden(selectionDialog)) getStage().addActor(statisticDialog);

        controller.setActivity(this);
        updateLabels();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

}
