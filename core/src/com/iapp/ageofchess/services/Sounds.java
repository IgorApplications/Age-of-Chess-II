package com.iapp.ageofchess.services;

import com.badlogic.gdx.audio.Music;

public class Sounds {

    private float effectsVolume = 1;
    private float musicVolume = 1;

    private static final Sounds sounds = new Sounds();

    public static Sounds self() {
        return sounds;
    }

    private Sounds() {}

    public void setVolumeEffects(float volume) {
        effectsVolume = volume;
    }

    public void setVolumeMusic(float volume) {
        musicVolume = volume;
    }

    public void playShowError() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getShowError().play(effectsVolume);
    }

    public void playMove() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getMove().play(effectsVolume);
    }

    public void playCastle() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getCastle().play(effectsVolume);
    }

    public void playCheck() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getCheck().play(effectsVolume);
    }

    public void playBell() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getBell().play(effectsVolume);
    }

    public void playLock() {
        if (!ChessConstants.localData.isEnableSounds()) return;
        ChessAssetManager.current().getLock().play(effectsVolume);
    }

    public void startWin() {
        if (!ChessConstants.localData.isEnableBackgroundMusic()) return;

        stopBackgroundMusic();
        var win = ChessAssetManager.current().getWin();
        win.setVolume(musicVolume);
        win.play();
        win.setOnCompletionListener(music -> startBackgroundMusic());
    }

    public void stopWin() {
        ChessAssetManager.current().getWin().stop();
    }

    public void startLose() {
        if (!ChessConstants.localData.isEnableBackgroundMusic()) return;

        stopBackgroundMusic();
        var lose = ChessAssetManager.current().getLose();
        lose.setVolume(musicVolume);
        lose.play();
        lose.setOnCompletionListener(music -> startBackgroundMusic());
    }

    public void stopLose() {
        ChessAssetManager.current().getLose().stop();
    }

    public void startBackgroundMusic() {
        if (!ChessConstants.localData.isEnableBackgroundMusic()) return;
        var music = ChessAssetManager.current().getBackgroundMusic();
        music.setVolume(musicVolume);
        music.play();
        music.setOnCompletionListener(Music::play);
    }

    public void stopBackgroundMusic() {
        ChessAssetManager.current().getBackgroundMusic().stop();
    }
}
