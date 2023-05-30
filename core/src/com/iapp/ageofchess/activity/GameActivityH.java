package com.iapp.ageofchess.activity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.controllers.GameController;
import com.iapp.lib.ui.widgets.BoardView;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.SettingsUtil;
import com.iapp.ageofchess.services.Sounds;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.WindowUtil;

class GameActivityH extends GameActivity {

    private RdLabel timeByTurnLabel, turnsLabel, blackTime, whiteTime;
    private boolean fewTime;
    private Cell<BoardView> boardCell;

    public GameActivityH(LocalMatch localMatch) {
        super(localMatch);
    }

    public GameActivityH(MatchState state) {
        super(state);
    }

    GameActivityH(GameActivity activity, GameController controller) {
        super(activity, controller);
    }

    @Override
    public void onMadeMove() {
        super.onMadeMove();
        turnsLabel.setText(controller.getTurn() + ". " + controller.defineColorMove());
    }

    @Override
    public void onMakeMove() {
        turnsLabel.setText(controller.getTurn() + "... " + controller.defineColorMove());
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

        RdTable generalTime = new RdTable();
        whiteTime = new RdLabel(controller.getWhiteTime());
        blackTime = new RdLabel(controller.getBlackTime());
        generalTime.add(whiteTime).row();
        generalTime.add(blackTime).row();

        RdTable buttons = new RdTable();
        buttons.align(Align.top);
        buttons.pad(30, 7, 0, 7);
        buttons.setFillParent(true);

        getStage().addActor(buttons);
        getStage().addActor(blackout);

        RdTable column1 = new RdTable();
        RdTable column2 = new RdTable();

        buttons.add(column1).left().top();
        if (ChessConstants.chatView != null) {
            buttons.add(column2).expand().align(Align.topRight).padRight(125);
        } else {
            buttons.add(column2).expand().align(Align.topRight);
        }

        column1.add(menu).size(125).row();
        column1.add(replay).size(125).row();
        column1.add(generalTime).width(125).row();

        column2.add(undo).size(125).row();
        column2.add(hint).size(125).row();
        column2.add(info).size(125).row();

        timeByTurnLabel = new RdLabel(controller.getTimeByTurn());
        var gameModeLabel = new RdLabel(SettingsUtil.defineGameMode(controller.getMatch().getGameMode()).toUpperCase());
        turnsLabel = new RdLabel(controller.getTurn() + ". " + controller.defineColorMove());

        content.add(timeByTurnLabel).padRight(10);
        content.add(gameModeLabel).padRight(10);
        content.add(turnsLabel);
        content.row();
        content.align(Align.center);
        boardCell = content.add(gameBoard).colspan(3);

        if (!WindowUtil.isHidden(selectionDialog)) getStage().addActor(selectionDialog);
        if (!WindowUtil.isHidden(resultDialog))    getStage().addActor(resultDialog);
        if (!WindowUtil.isHidden(infoDialog))      getStage().addActor(infoDialog);
        if (!WindowUtil.isHidden(menuDialog))      getStage().addActor(menuDialog);
        if (!WindowUtil.isHidden(replayDialog))    getStage().addActor(replayDialog);
        if (!WindowUtil.isHidden(selectionDialog)) getStage().addActor(statisticDialog);

        controller.setActivity(this);
        updateLabels();
    }

    @Override
    public void initListeners() {
        super.initListeners();
    }

    private void updateLabels() {
        timeByTurnLabel.setText(controller.getTimeByTurn());
        blackTime.setText(controller.getBlackTime());
        whiteTime.setText(controller.getWhiteTime());

        timeByTurnLabel.setColor(Color.WHITE);
        blackTime.setColor(Color.WHITE);
        whiteTime.setColor(Color.WHITE);

        if (controller.isFewTimeByTurn()) timeByTurnLabel.setColor(Color.RED);
        if (controller.isFewBlackTime()) blackTime.setColor(Color.RED);
        if (controller.isFewWhiteTime()) whiteTime.setColor(Color.RED);

        if ((controller.isFewTimeByTurn() || controller.isFewBlackTime() || controller.isFewWhiteTime())) {
            if (!fewTime) {
                Sounds.self().playBell();
                fewTime = true;
            }
        } else {
            fewTime = false;
        }
    }
}
