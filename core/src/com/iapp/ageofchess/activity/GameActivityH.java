package com.iapp.ageofchess.activity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.controllers.GameController;
import com.iapp.ageofchess.graphics.BoardView;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.SettingsUtil;
import com.iapp.ageofchess.util.Sounds;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.screens.RdApplication;

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

        var buttons = new Table();
        buttons.align(Align.top);
        buttons.pad(30, 7, 0, 7);
        buttons.setFillParent(true);

        var generalTime = new Table();
        whiteTime = new RdLabel(controller.getWhiteTime());
        blackTime = new RdLabel(controller.getBlackTime());
        generalTime.add(whiteTime).row();
        generalTime.add(blackTime).row();

        getStage().addActor(buttons);
        getStage().addActor(blackout);

        buttons.add(menu).size(125).expandX().left();
        buttons.add(undo).size(125).expandX().right();
        buttons.row();
        buttons.add(replay).size(125).expandX().left();
        buttons.add(hint).size(125).expandX().right();
        buttons.row();
        buttons.add(generalTime).width(125).expandX().left();
        buttons.add(info).size(125).expandX().right();

        timeByTurnLabel = new RdLabel(controller.getTimeByTurn());
        var gameModeLabel = new RdLabel(SettingsUtil.defineGameMode(controller.getMatch().getGameMode()).toUpperCase());
        turnsLabel = new RdLabel(controller.getTurn() + ". " + controller.defineColorMove());

        content.add(timeByTurnLabel).padRight(10);
        content.add(gameModeLabel).padRight(10);
        content.add(turnsLabel);
        content.row();
        content.align(Align.center);
        boardCell = content.add(gameBoard).colspan(3);

        if (selectionDialog != null && selectionDialog.hasParent()) getStage().addActor(selectionDialog);
        if (resultDialog != null && resultDialog.hasParent()) getStage().addActor(resultDialog);
        if (infoDialog != null && infoDialog.hasParent()) getStage().addActor(infoDialog);
        if (menuDialog != null && menuDialog.hasParent()) getStage().addActor(menuDialog);
        if (replayDialog != null && replayDialog.hasParent()) getStage().addActor(replayDialog);

        controller.setActivity(this);
        updateLabels();
    }

    private void updateLabels() {
        timeByTurnLabel.setSafetyText(controller.getTimeByTurn());
        blackTime.setSafetyText(controller.getBlackTime());
        whiteTime.setSafetyText(controller.getWhiteTime());

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
