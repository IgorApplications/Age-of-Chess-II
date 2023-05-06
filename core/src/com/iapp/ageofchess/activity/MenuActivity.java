package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.MenuController;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.DataManager;
import com.iapp.rodsher.actors.RdDialog;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.actors.RdTable;
import com.iapp.rodsher.screens.Activity;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.SplashActivity;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.TextureUtil;
import com.iapp.rodsher.util.TransitionEffects;
import com.iapp.rodsher.util.WindowUtil;

public class MenuActivity extends Activity {

    private final MenuController controller;
    private RdTable content;
    private Image title;
    private RdImageTextButton multiplayer, singlePlayer, modding, settings, guide, exit;
    private ImageButton login;
    private RdDialog loginDialog;
    private Image blackout;

    public MenuActivity() {
        controller = new MenuController(this);
    }

    public void setLoginDialog(RdDialog loginDialog) {
        this.loginDialog = loginDialog;
    }

    public RdDialog getLoginDialog() {
        return loginDialog;
    }

    public void showBlackout() {
        getStage().addActor(blackout);
    }

    public void hideBlackout() {
        getStage().getActors().removeValue(blackout, true);
    }

    @Override
    public void initActors() {
        Gdx.graphics.setResizable(true);
        content = new RdTable();
        content.setFillParent(true);

        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        getStage().addActor(background);

        title = new Image(ChessAssetManager.current().findChessRegion("app_text_logo"));
        title.setScaling(Scaling.fit);

        multiplayer = new RdImageTextButton(strings.get("multiplayer"),"white_screen");
        multiplayer.setImage("ib_group");

        singlePlayer = new RdImageTextButton(strings.get("single-player"), "white_screen");
        singlePlayer.setImage("ib_person");

        modding = new RdImageTextButton(strings.get("modding"), "yellow_screen");
        modding.setImage("ib_construction");

        settings = new RdImageTextButton(strings.get("settings"), "blue_screen");
        settings.setImage("ib_settings");

        guide = new RdImageTextButton(strings.get("guide"), "blue_screen");
        guide.setImage("ib_guide");

        if (ChessConstants.loggingAcc == null) {
            login = new ImageButton(ChessAssetManager.current().getLoginStyle());
        } else {
            login = new ImageButton(ChessAssetManager.current().getLogoutStyle());
        }

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            exit = new RdImageTextButton(strings.get("exit"), "red_screen");
            exit.setImage("ib_close");
        }

        blackout = new Image(new TextureRegionDrawable(
                ChessAssetManager.current().getBlackTexture()));
        blackout.getColor().a = 0.25f;
        blackout.setFillParent(true);
    }

    @Override
    public void initListeners() {
        ChessApplication.self().getLauncher().setOnFinish(null);
        multiplayer.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.showLoginDialog();
            }
        });
        singlePlayer.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToScenario();
            }
        });
        modding.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToModding();
            }
        });
        settings.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToSettings();
            }
        });
        guide.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToGuide();
            }
        });

        login.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (ChessConstants.loggingAcc == null) {
                    controller.showLoginDialog();
                } else {
                    ChessConstants.loggingAcc = null;
                    ChessApplication.self().getAccountPanel().setVisible(false);

                    MultiplayerEngine.self().resetConnection();
                    login.setStyle(ChessAssetManager.current().getLoginStyle());
                }
            }
        });

        if (exit != null) {
            exit.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    DataManager.self().saveLocalData(ChessConstants.localData);
                    controller.exit();
                }
            });
        }
    }

    @Override
    public void show(Stage stage, Activity last) {
        stage.addActor(content);
        var buttons = new RdTable();
        buttons.align(Align.center);

        if (ChessConstants.loggingAcc != null) {
            RdTable panel = new RdTable();
            panel.align(Align.topLeft);
            panel.setFillParent(true);
            getStage().addActor(panel);
            panel.add(ChessApplication.self().getAccountPanel())
                .expandX().fillX();
        }

        buttons.add(multiplayer).fillX().minWidth(400).row();
        buttons.add(singlePlayer).fillX().minWidth(400).row();
        buttons.add(modding).fillX().minWidth(400).row();
        buttons.add(settings).fillX().minWidth(400).row();
        buttons.add(guide).fillX().minWidth(400).row();
        if (exit != null) buttons.add(exit).fillX().minWidth(400).row();

        var table = new RdTable();
        table.align(Align.topLeft);
        table.add(login).align(Align.topLeft);
        table.add(buttons).expandX().align(Align.center)
            .fillX().padRight(75);

        content.add(title).row();
        content.add(table).padLeft(50).padRight(50).fillX();

        if (last instanceof SplashActivity) return;
        TransitionEffects.transitionBottomShow(content, ChessConstants.localData.getScreenDuration());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        blackout.setSize(getStage().getWidth(), getStage().getHeight());

        float loginWidth = RdApplication.self().getViewport().getWorldWidth() > 1000 ? 1000
                : RdApplication.self().getViewport().getWorldWidth();

        if (loginDialog != null) loginDialog.setSize(loginWidth, 700);
        WindowUtil.resizeCenter(loginDialog);
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        TransitionEffects.transitionBottomHide(action, content, ChessConstants.localData.getScreenDuration());
        return content;
    }
}
