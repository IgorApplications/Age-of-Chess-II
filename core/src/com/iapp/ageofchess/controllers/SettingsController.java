package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.activity.SettingsActivity;
import com.iapp.ageofchess.services.*;
import com.iapp.lib.ui.actors.RdDialog;
import com.iapp.lib.ui.actors.RdDialogBuilder;
import com.iapp.lib.ui.screens.Controller;
import com.iapp.lib.ui.screens.GrayAssetManager;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.Pair;
import com.iapp.lib.util.RdI18NBundle;

import java.util.Locale;
import java.util.function.BiConsumer;

public class SettingsController extends Controller {

    private final SettingsActivity activity;

    public SettingsController(SettingsActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void goToMenu() {
        startActivity(new MenuActivity());
    }

    public void updateLang(int index) {
        if (RdApplication.self().getLanguageCodes()[index]
                .equals(ChessConstants.localData.getLangCode())) return;

        ChessConstants.localData.setLocale(RdApplication.self().getLanguageCodes()[index]);
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

    public void updateVolumeEffects(String text) {
        float volume = Integer.parseInt(text.substring(0, text.length() - 1)) / 100f;
        ChessConstants.localData.setEffectsVolume(volume);
        Sounds.self().setVolumeEffects(volume);
    }

    public void updateVolumeMusic(String text) {
        float volume = Integer.parseInt(text.substring(0, text.length() - 1)) / 100f;
        ChessConstants.localData.setMusicVolume(volume);
        Sounds.self().setVolumeMusic(volume);
    }

    public void updateFPS(String selected) {
        if (selected.equals(strings.get("[i18n]infinity"))) ChessConstants.localData.setFps(LocalData.Fps.INFINITY);
        else ChessConstants.localData.setFps(LocalData.Fps.of(Integer.parseInt(selected.replaceAll(" fps", ""))));
        RdApplication.self().setFps(ChessConstants.localData.getFps().getValue());
    }

    public void updateSysProperties(boolean checked) {
        ChessConstants.localData.setEnableSysProperties(checked);
        ChessApplication.self().getLoggingView().setVisible(checked);
    }

    public void resetSettings() {
        ChessApplication.self().showConf(strings.get("[i18n]Are you sure you want to reset the application preferences?"),
            (dialog, result) -> {
            ChessConstants.localData = new LocalData();
            ChessConstants.localData.setLocale(RdApplication.self().getDefaultLanguage());
            ChessApplication.self().initialize();

            startActivity(new SettingsActivity());
        });
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
