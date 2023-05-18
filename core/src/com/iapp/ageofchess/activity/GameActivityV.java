package com.iapp.ageofchess.activity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.controllers.GameController;
import com.iapp.ageofchess.graphics.BoardView;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.SettingsUtil;
import com.iapp.ageofchess.services.Sounds;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.screens.RdApplication;

class GameActivityV extends GameActivity {

    private RdLabel timeByTurnLabel, turnsLabel, whiteTime, blackTime;
    private boolean fewTime;
    private Table information;
    private Cell<BoardView> boardCell;

    public GameActivityV(LocalMatch localMatch) {
        super(localMatch);
    }

    public GameActivityV(MatchState state) {
        super(state);
    }

    GameActivityV(GameActivity activity, GameController controller) {
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

        var gameModeLabel = new RdLabel(SettingsUtil.defineGameMode(controller.getMatch().getGameMode()).toUpperCase());
        turnsLabel = new RdLabel(controller.getTurn() + ". " + controller.defineColorMove());

        information.add(gameModeLabel).row();
        information.add(turnsLabel);

        timeByTurnLabel = new RdLabel(controller.getTimeByTurn());
        whiteTime = new RdLabel(controller.getWhiteTime());
        blackTime = new RdLabel(controller.getBlackTime());

        getStage().addActor(information);
        getStage().addActor(timeByTurnLabel);
        getStage().addActor(whiteTime);
        getStage().addActor(blackTime);
        getStage().addActor(blackout);

        var buttons = new Table();
        buttons.add(menu).padRight(5).size(125);
        buttons.add(undo).padRight(5).size(125);
        buttons.add(replay).padRight(5).size(125);
        buttons.add(hint).padRight(5).size(125);
        buttons.add(info).size(125);

        boardCell = content.add(gameBoard).padTop(120).center();
        content.row();
        content.add(buttons).padTop(10).center();

        if (selectionDialog != null) getStage().addActor(selectionDialog);
        if (resultDialog != null) getStage().addActor(resultDialog);
        if (infoDialog != null) getStage().addActor(infoDialog);
        if (menuDialog != null) getStage().addActor(menuDialog);
        if (replayDialog != null) getStage().addActor(replayDialog);
        if (statisticDialog != null) getStage().addActor(statisticDialog);

        controller.setActivity(this);
        updateLabels();
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
