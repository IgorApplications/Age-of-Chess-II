package com.iapp.ageofchess.util;

import com.badlogic.gdx.audio.Music;

public class Sounds {

    private static final Sounds sounds = new Sounds();

    public static Sounds self() {
        return sounds;
    }

    private Sounds() {}

    public void playMove() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getMove().play(1.0f);
    }

    public void playCastle() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getCastle().play(1.0f);
    }

    public void playCheck() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getCheck().play(1.0f);
    }

    public void playBell() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getBell().play(1.0f);
    }

    public void startWin() {
        if (!ChessConstants.localData.isEnableSounds()) return;

        stopBackgroundMusic();
        var win = ChessAssetManager.current().getWin();
        win.play();
        win.setOnCompletionListener(music -> startBackgroundMusic());
    }

    public void playClick() {
        if (!ChessConstants.localData.isEnableSounds()) return;

        ChessAssetManager.current().getClick().play(1.0f);
    }

    public void stopWin() {
        ChessAssetManager.current().getWin().stop();
    }

    public void startLose() {
        if (!ChessConstants.localData.isEnableSounds()) return;

        stopBackgroundMusic();
        var lose = ChessAssetManager.current().getLose();
        lose.play();
        lose.setOnCompletionListener(music -> startBackgroundMusic());
    }

    public void stopLose() {
        ChessAssetManager.current().getLose().stop();
    }

    public void startBackgroundMusic() {
        if (!ChessConstants.localData.isEnableBackgroundMusic()) return;
        var music = ChessAssetManager.current().getBackgroundMusic();
        music.play();
        music.setOnCompletionListener(Music::play);
    }

    public void stopBackgroundMusic() {
        ChessAssetManager.current().getBackgroundMusic().stop();
    }
}
