package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.*;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerMenuActivity;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.Controller;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.util.OnChangeListener;

public class MenuController extends Controller {

    private final MenuActivity activity;
    private RdDialog loginDialog;

    public MenuController(MenuActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void showLoginDialog() {
        if (ChessConstants.loggingAcc != null) {
            goToMultiplayerMenu();
            return;
        }

        if (ChessConstants.localData.getNameAcc() != null && ChessConstants.localData.getPassword() != null) {
            login(ChessConstants.localData.getNameAcc(), ChessConstants.localData.getPassword());
            return;
        }

        loginDialog = new RdDialog(strings.get("login"), ChessAssetManager.current().getSkin());
        loginDialog.removeActor(loginDialog.getButtonTable());
        loginDialog.padBottom(3);

        var content = new Table();
        var scroll = new RdScrollPane(content);

        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setScrollingDisabled(true, false);

        loginDialog.getContentTable().align(Align.topLeft);
        loginDialog.getContentTable().add(scroll).expand().fill();

        content.add(getLoginTable()).expand().align(Align.topLeft).fillX().row();
        content.add(getRegistrationTable()).expand().align(Align.topLeft).fillX();

        loginDialog.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("icon_conf")));
        loginDialog.getIcon().setScaling(Scaling.fit);
        activity.setLoginDialog(loginDialog);
        loginDialog.show(activity.getStage());
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void goToScenario() {
        RdApplication.self().setScreen(new ScenariosActivity());
    }

    public void goToSettings() {
        RdApplication.self().setScreen(new SettingsActivity());
    }

    public void goToModding() {
        RdApplication.self().setScreen(new ModdingActivity());
    }

    public void goToGuide() {
        RdApplication.self().setScreen(new GuideActivity());
    }

    public void exit() {
        Gdx.app.exit();
    }

    private void goToMultiplayerMenu() {
        RdApplication.self().setScreen(new MultiplayerMenuActivity());
    }

    private RdTable getLoginTable() {
        var loginTable = new LineTable(strings.get("login"), ChessAssetManager.current().getSkin());
        loginTable.align(Align.topLeft);
        loginTable.pad(40, 8, 5, 10);

        var label1 = new RdLabel(strings.get("login_name"));
        var label2 = new RdLabel(strings.get("password"));
        var field1 = new RdTextArea("");
        field1.setPrefLines(1);
        field1.setMessageText(strings.get("enter_hint"));

        var field2 = new RdTextArea("");
        field2.setPrefLines(1);
        field2.setMessageText(strings.get("enter_hint"));
        field2.setPasswordMode(true);
        field2.setPasswordCharacter('*');

        var login = new RdTextButton(strings.get("login"));

        loginTable.add(label1).left();
        loginTable.add(field1).padLeft(10).expandX().fillX().padBottom(5).row();
        loginTable.add(label2).left();
        loginTable.add(field2).padLeft(10).expandX().fillX().padBottom(5).row();
        loginTable.add(login).expandX().left().colspan(2).minWidth(300);
        field2.setPasswordMode(true);

        login.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
               login(field1.getText(), field2.getText());
            }
        });

        return loginTable;
    }

    private RdTable getRegistrationTable() {
        var registrationTable = new LineTable(strings.get("registration"), ChessAssetManager.current().getSkin());
        registrationTable.align(Align.topLeft);
        registrationTable.pad(40, 8, 5, 10);

        var label1 = new RdLabel(strings.get("name_acc"));
        var label2 = new RdLabel(strings.get("login_name"));
        var label3 = new RdLabel(strings.get("password"));
        var label4 = new RdLabel(strings.get("conf_password"));

        var userName = new RdTextArea("");
        userName.setPrefLines(1);
        userName.setMessageText(strings.get("enter_hint"));
        var login = new RdTextArea("");
        login.setPrefLines(1);
        login.setMessageText(strings.get("enter_hint"));
        var password1 = new RdTextArea("");
        password1.setPrefLines(1);
        password1.setMessageText(strings.get("enter_hint"));
        password1.setPasswordMode(true);
        password1.setPasswordCharacter('*');
        var password2 = new RdTextArea("");
        password2.setPrefLines(1);
        password2.setMessageText(strings.get("enter_hint"));
        password2.setPasswordMode(true);
        password2.setPasswordCharacter('*');

        var registration = new RdTextButton(strings.get("register"));

        registrationTable.add(label1).left();
        registrationTable.add(userName).expandX().fillX().padBottom(5).row();
        registrationTable.add(label2).left();
        registrationTable.add(login).expandX().fillX().padBottom(5).row();
        registrationTable.add(label3).left();
        registrationTable.add(password1).expandX().fillX().padBottom(5).row();
        registrationTable.add(label4).left();
        registrationTable.add(password2).expandX().fillX().row();
        registrationTable.add(registration).minWidth(300);

        registration.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (!password1.getText().equals(password2.getText())) {
                    ChessApplication.self().showInfo(strings.get("password_mismatch"));
                    return;
                }

                if (password1.getText().length() < 6) {
                    ChessApplication.self().showInfo(strings.get("min_password"));
                    return;
                }

                if (login.getText().length() < 2) {
                    ChessApplication.self().showInfo(strings.get("min_login"));
                    return;
                }

                if (userName.getText().length() < 2) {
                    ChessApplication.self().showInfo(strings.get("min_username"));
                    return;
                }

                if (!isASCII(login.getText())) {
                    ChessApplication.self().showInfo(strings.get("login_letters"));
                    return;
                }


                activity.showBlackout();
                MultiplayerEngine.self().signup(login.getText(), userName.getText(), password1.getText(),
                        () -> {
                            login(login.getText(), password1.getText());
                        },
                        error -> {
                            ChessApplication.self().showError(strings.get("signup_failed") + error);
                            activity.hideBlackout();
                        });
            }
        });

        return registrationTable;
    }

    private void login(String name, String password) {

        activity.showBlackout();
        MultiplayerEngine.self().login(name, password,
                account -> {
                    ChessConstants.localData.setNameAcc(account.getUsername());
                    ChessConstants.localData.setPassword(password);
                    ChessConstants.loggingAcc = account;

                    ChessApplication.self().getAccountPanel().setVisible(true);
                    MultiplayerEngine.self().getAvatar(account, bytes ->
                        ChessApplication.self().getAccountPanel().update(account, bytes));

                    activity.hideBlackout();
                    Runnable task = this::goToMultiplayerMenu;
                    if (loginDialog == null) task.run();
                    else loginDialog.hide(Actions.run(task));

                },
                (error) -> {
                    activity.hideBlackout();
                    if (activity.getLoginDialog() != null && !activity.getLoginDialog().isHidden()) {
                        ChessApplication.self().showError(strings.get("login_failed") + error);
                        return;
                    }
                    ChessConstants.localData.setNameAcc(null);
                    ChessConstants.localData.setPassword(null);
                    showLoginDialog();
                    ChessApplication.self().showError(strings.get("login_failed") + error);
                });

    }

    private boolean isASCII(String s) {
        for (char c : s.toCharArray()) {
            if (c > 127) return false;
        }
        return true;
    }
}
