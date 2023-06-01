package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.lib.chess_engine.Result;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerGameController;
import com.iapp.ageofchess.graphics.ControlGameView;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.lib.ui.widgets.BoardView;
import com.iapp.lib.util.WindowUtil;
import com.iapp.lib.web.Account;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.ui.screens.RdApplication;

class
MultiplayerGameActivityH extends MultiplayerGameActivity {

    private Cell<BoardView> boardCell;
    private RdLabel playerInfo;

    public MultiplayerGameActivityH(LocalMatch localMatch, Match match) {
        super(localMatch, match);
    }

    MultiplayerGameActivityH(MultiplayerGameActivity activity, MultiplayerGameController controller) {
        super(activity, controller);
    }

    @Override
    public void update() {
        super.update();

        Account firstPlayer = controller.getFirstPlayer();
        Account secondPlayer = controller.getSecondPlayer();

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
            if (color != controller.getColorMove() && controller.getCurrentMatch().isAlternately()
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
        var rectSize = Math.min(viewport.getWorldWidth() - 300, viewport.getWorldHeight() - 40);
        if (rectSize > ChessConstants.localData.getMaxBoardSize()) rectSize = ChessConstants.localData.getMaxBoardSize();

        if (boardCell != null) {
            resizeBoard(boardCell, rectSize);
        }
        if (infoDialog != null) {
            infoDialog.setPosition(
                    getStage().getWidth() / 2 - infoDialog.getWidth() / 2,
                    getStage().getHeight() - infoDialog.getHeight() - 70);
        }
    }

    @Override
    public void initActors() {
        super.initActors();

        controlGame = new ControlGameView(controller);
        controlGame.align(Align.bottom);
        controlGame.setFillParent(true);

        boolean infinity = controller.getCurrentMatch().getTimeByBlack() != -1
            && controller.getCurrentMatch().getTimeByWhite() != -1;

        var buttons = new Table();
        buttons.align(Align.topLeft);
        buttons.pad(30, 7, 0, 7);
        buttons.setFillParent(true);
        getStage().addActor(buttons);

        buttons.add(menu).size(125).expandX().left().padTop(infinity ? 100 : 0);
        buttons.row();
        buttons.add(settings).size(125).expandX().left();
        buttons.row();

        var generalTime = new RdTable();
        generalTime.align(Align.topLeft);
        generalTime.setFillParent(true);
        getStage().addActor(generalTime);

        var contentTime = new RdTable();
        contentTime.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findChessRegion("chat_bg"),
                        5,5,5,5)));
        contentTime.add(whiteTime).row();
        contentTime.add(blackTime).row();
        if (infinity) {
            generalTime.add(contentTime).pad(5, 5, 5, 5);
        }

        getStage().addActor(controlGame);
        getStage().addActor(blackout);

        var player = controller.getSecondPlayer();
        playerInfo = new RdLabel(player != null ? player.getFullName() : "");

        content.add(timeByTurnLabel).padRight(10);
        content.add(playerInfo).padRight(10);
        content.add(turnsLabel);
        content.row();
        content.align(Align.center);
        boardCell = content.add(gameBoard).colspan(3);

        if (!WindowUtil.isHidden(selectionDialog)) getStage().addActor(selectionDialog);
        if (!WindowUtil.isHidden(resultDialog))    getStage().addActor(resultDialog);
        if (!WindowUtil.isHidden(infoDialog))      getStage().addActor(infoDialog);
        if (!WindowUtil.isHidden(menuDialog))      getStage().addActor(menuDialog);
        if (!WindowUtil.isHidden(selectionDialog)) getStage().addActor(statisticDialog);

        controller.setActivity(this);
        updateLabels();
    }
}
