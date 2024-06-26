package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.MenuController;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.DataManager;
import com.iapp.lib.ui.actors.RdDialog;
import com.iapp.lib.ui.actors.RdImageTextButton;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.SplashActivity;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;
import com.iapp.lib.web.AccountType;

public class MenuActivity extends Activity {

    private final MenuController controller;
    private RdTable content;
    private Image title;
    private RdImageTextButton multiplayer, singlePlayer, modding, settings, guide, exit;
    private ImageButton login, server;
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
        blackout.setVisible(true);
        blackout.setTouchable(Touchable.enabled);
    }

    public void hideBlackout() {
        blackout.setVisible(false);
        blackout.setTouchable(Touchable.disabled);
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

        multiplayer = new RdImageTextButton(strings.get("[i18n]Multiplayer"),"white_screen");
        multiplayer.setImage("ib_group");

        singlePlayer = new RdImageTextButton(strings.get("[i18n]Single Player"), "white_screen");
        singlePlayer.setImage("ib_person");

        modding = new RdImageTextButton(strings.get("[i18n]Modding"), "yellow_screen");
        modding.setImage("ib_construction");

        settings = new RdImageTextButton(strings.get("[i18n]Settings"), "blue_screen");
        settings.setImage("ib_settings");

        guide = new RdImageTextButton(strings.get("[i18n]Guide"), "blue_screen");
        guide.setImage("ib_guide");

        if (ChessConstants.loggingAcc == null) {
            login = new ImageButton(ChessAssetManager.current().getLoginStyle());
        } else {
            login = new ImageButton(ChessAssetManager.current().getLogoutStyle());
            if (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal()) {
                server = new ImageButton(ChessAssetManager.current().getAdminStyle());
            }
        }

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            exit = new RdImageTextButton(strings.get("[i18n]Exit"), "red_screen");
            exit.setImage("ib_close");
        }

        blackout = new Image(new TextureRegionDrawable(
                ChessAssetManager.current().getBlackTexture()));
        blackout.getColor().a = 0.25f;
        blackout.setFillParent(true);
        blackout.setVisible(false);
        blackout.setTouchable(Touchable.disabled);
    }

    @Override
    public void initListeners() {
        ChessApplication.self().getLauncher().setOnFinish(null);
        multiplayer.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {

                if (ChessConstants.loggingAcc == null) showBlackout();
                MultiplayerEngine.self().tryConnect((res, s) -> {
                    if (res) {
                        controller.showLoginDialog();
                    } else {
                        hideBlackout();
                        ChessApplication.self().showError(strings.format("[i18n]Failed to connect to the server '{0}'. Please try again later", s));
                    }
                });

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
                    showBlackout();
                    MultiplayerEngine.self().tryConnect((res, s) -> {
                        if (res) {
                            controller.showLoginDialog();
                        } else {
                            hideBlackout();
                            ChessApplication.self().showError(strings.format("[i18n]Failed to connect to the server '{0}'. Please try again later", s));
                        }
                    });

                } else {

                    RdApplication.self().getTopActors().remove(ChessConstants.chatView);
                    RdApplication.self().getStage().getActors().removeValue(ChessConstants.chatView, true);
                    ChessConstants.loggingAcc = null;
                    ChessConstants.chatView = null;
                    if (server != null) server.setVisible(false);

                    MultiplayerEngine.self().resetConnection();
                    login.setStyle(ChessAssetManager.current().getLoginStyle());
                }

            }
        });

        if (server != null) {
            server.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    ChessApplication.self().showConf(
                        strings.get("[i18n]Restart"),
                        strings.get("[i18n]Are you sure you want to restart the server?"),
                        (dialog, s) -> {
                            dialog.hide();
                            MultiplayerEngine.self().restartServer();
                        });

                }
            });
        }

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
        getStage().addActor(blackout);

        RdTable buttons = new RdTable();
        buttons.align(Align.center);

        buttons.add(multiplayer).fillX().minWidth(400).row();
        buttons.add(singlePlayer).fillX().minWidth(400).row();
        buttons.add(modding).fillX().minWidth(400).row();
        buttons.add(settings).fillX().minWidth(400).row();
        buttons.add(guide).fillX().minWidth(400).row();
        if (exit != null) buttons.add(exit).fillX().minWidth(400).row();

        RdTable commands = new RdTable();
        commands.add(login).minSize(100).row();
        commands.add(server).minSize(100).padTop(3);

        var table = new RdTable();
        table.align(Align.topLeft);
        table.add(commands).align(Align.topLeft);
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
