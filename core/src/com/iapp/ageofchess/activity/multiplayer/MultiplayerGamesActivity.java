package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerGamesController;
import com.iapp.ageofchess.graphics.MultiplayerMatchView;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MultiplayerGamesActivity extends Activity {

    private final MultiplayerGamesController controller;
    private RdImageTextButton back;
    private RdTable messagesTable;
    private WindowGroup windowGroup;
    private Spinner spinner;

    private final List<RdDialog> errors = new ArrayList<>();
    private final List<RdDialog> info = new ArrayList<>();
    private RdDialog conf;
    private Consumer<List<Match>> onMatches;

    public MultiplayerGamesActivity() {
        controller = new MultiplayerGamesController(this);
    }

    public void addError(RdDialog error) {
        errors.add(error);
    }

    public void setConf(RdDialog conf) {
        this.conf = conf;
    }

    public void addInfo(RdDialog dialog) {
        info.add(dialog);
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    @Override
    public void initActors() {
        back = new RdImageTextButton(strings.get("back"), "red_screen");
        back.setImage("ib_back");
    }

    @Override
    public void initListeners() {
        ChessApplication.self().getLauncher().setOnFinish(controller::goToMultiplayerMenuActivity);
        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToMultiplayerMenuActivity();
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        getStage().addActor(background);
        background.setScaling(Scaling.fill);

        RdTable panel = new RdTable();
        panel.align(Align.topLeft);
        panel.setFillParent(true);
        getStage().addActor(panel);
        panel.add(ChessConstants.accountPanel)
            .expandX().fillX();

        var window = new RdWindow("","screen_window");
        window.setMovable(false);
        stage.addActor(window);

        var properties = new PropertyTable(400, ChessAssetManager.current().getSkin());
        window.add(properties).expand().fill();
        properties.setVisibleBackground(false);

        properties.add(new PropertyTable.Title(strings.get("online_games")));

        messagesTable = new RdTable("loading");
        messagesTable.align(Align.topLeft);
        var scroll = new ScrollPane(messagesTable);
        properties.getContent().add(scroll).pad(5, 5, 5,5).expand().fill();

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("multiplayer"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();
        messagesTable.getLoading().setVisible(true);

        // listener update matches
        onMatches = this::updateGames;
        MultiplayerEngine.self().addOnMatches(onMatches);

        TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
    }

    @Override
    public void dispose() {
        super.dispose();

        // clear listener
        MultiplayerEngine.self().removeOnMatches(onMatches);
    }

    private RdDialog confMatch;

    public void updateGames(List<Match> matches) {
        messagesTable.clear();

        for (var match : matches) {
            var gameView = new MultiplayerMatchView(match, new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    controller.goToMultiplayerScenariosActivity(match);
                }
            }, new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    ChessApplication.self().showConf(strings.get("conf_remove_match"),
                        (dialog, s) -> {
                            confMatch.hide();
                            MultiplayerEngine.self().removeMatch(match.getId(),
                                error -> ChessApplication.self().showError(strings.format("error_remove_match",
                                    error)));
                        });
                }
            });

            messagesTable.add(gameView).expandX()
                    .fillX().pad(5, 5, 5,5).row();
        }
        messagesTable.getLoading().setVisible(false);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
        for (var dialog : info)
            WindowUtil.resizeCenter(dialog);
        for (var error : errors)
            WindowUtil.resizeCenter(error);
        WindowUtil.resizeCenter(conf);
        WindowUtil.resizeCenter(spinner);
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        if (next instanceof MultiplayerGameActivity) {
            TransitionEffects.alphaHide(action, ChessConstants.localData.getScreenDuration());
            return getStage().getRoot();
        }
        TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
        return windowGroup;
    }
}
