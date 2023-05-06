package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.activity.SettingsActivity;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.util.*;
import com.iapp.rodsher.actors.RdDialog;
import com.iapp.rodsher.actors.RdDialogBuilder;
import com.iapp.rodsher.screens.Controller;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.Pair;
import com.iapp.rodsher.util.RdI18NBundle;

import java.util.Locale;

public class SettingsController extends Controller {

    private final SettingsActivity activity;
    private RdDialog dialog;

    public SettingsController(SettingsActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void goToMenu() {
        startActivity(new MenuActivity());
    }

    public void updateLang(int index) {
        if (ChessApplication.self().getLanguageLocales().get(index).getLanguage()
                .equals(ChessConstants.localData.getLocale().getLanguage())) return;

        ChessConstants.localData.setLocale(ChessApplication.self().getLanguageLocales().get(index));

        RdApplication.self().setStrings(
                RdI18NBundle.createBundle(
                        Gdx.files.internal("languages/lang"),
                        ChessConstants.localData.getLocale()));
        ChessApplication.self().initialize();

        startActivity(new SettingsActivity(),
                Actions.run(() -> {
                    for (var dataMap : ChessAssetManager.current().getDataMaps()) {
                        dataMap.updateLang();
                    }
                }));
    }

    public void updateBackgroundMusic(boolean checked) {
        ChessConstants.localData.setEnableBackgroundMusic(checked);
        if (checked) Sounds.self().startBackgroundMusic();
        else Sounds.self().stopBackgroundMusic();
    }

    public void updateSounds(boolean checked) {
        ChessConstants.localData.setEnableSounds(checked);
    }

    public void updateFPS(String selected) {
        if (selected.equals(strings.get("infinity"))) ChessConstants.localData.setFps(LocalData.Fps.INFINITY);
        else ChessConstants.localData.setFps(LocalData.Fps.of(Integer.parseInt(selected.replaceAll(" fps", ""))));
        RdApplication.self().setFps(ChessConstants.localData.getFps().getValue());
    }

    public void updateSysProperties(boolean checked) {
        ChessConstants.localData.setEnableSysProperties(checked);
        ChessApplication.self().getLoggingView().setVisible(checked);
    }

    public RdDialog resetSettings() {
        dialog = new RdDialogBuilder()
                .title(strings.get("confirmation"))
                .text(strings.get("reset_question"))
                .cancel(strings.get("cancel"))
                .accept(strings.get("accept"), new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        ChessConstants.localData = new LocalData();
                        ChessConstants.localData.setLocale(Locale.getDefault());
                        ChessApplication.self().initialize();

                        startActivity(new SettingsActivity());
                    }
                })
                .build(ChessAssetManager.current().getSkin(), "input");

        dialog.getIcon().setDrawable(new TextureRegionDrawable(
                GrayAssetManager.current().findRegion("icon_warn")));
        dialog.getIcon().setScaling(Scaling.fit);
        dialog.show(activity.getStage());
        dialog.setSize(800, 550);

        return dialog;
    }

    public void updateFullScreen(boolean checked) {
        ChessConstants.localData.setFullScreen(checked);
        var windowSize = ChessConstants.localData.getWindowSize();
        if (checked) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        else Gdx.graphics.setWindowedMode(windowSize.getKey(), windowSize.getValue());
    }

    public void updateWindowSize(boolean checked) {
        ChessConstants.localData.setSaveWindowSize(checked);
        if (!checked) {
            ChessConstants.localData.setWindowSize(new Pair<>(1530, 850));
        }
    }
}
