package com.iapp.ageofchess.util;

import com.badlogic.gdx.utils.Array;
import com.iapp.ageofchess.chess_engine.Color;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.multiplayer.TurnMode;
import com.iapp.rodsher.util.Pair;

import java.util.*;

public class LocalData {

    private Locale language;
    private boolean enableSounds = true;
    private boolean enableBackgroundMusic = true;
    private Fps fps = Fps.INFINITY;
    private boolean enableSysProperties;
    private float screenSpeed = 0.2f;

    private String nameAcc, password;
    private long timeByTurn;
    private boolean infinityByTurn = true;
    private int maxTurns;
    private boolean infinityTurns = true;
    private GameMode gameMode = GameMode.NOVICE;
    private String name = "Game";
    private boolean infinityTimeGame = true;
    private long timeByGame;
    private Color pieceColor = Color.WHITE;
    private boolean randomColor;
    private boolean flippedPieces = true;
    private boolean matchDescription = true;
    private boolean blockedHints;
    private final List<MatchState> references = new ArrayList<>();

    private boolean fullScreen = true;
    private boolean saveWindowSize = false;
    private Pair<Integer, Integer> windowSize = new Pair<>(1530, 850);
    private final Map<GameMode, Integer> bestResultByLevel = new HashMap<>();
    private GameMode userLevel = GameMode.NOVICE;
    private TurnMode turnMode = TurnMode.ALTERNATELY_FAST;
    private float boardMaxSize = 1200;
    private float piecesSpeed = 0.0030f;

    public LocalData() {
        for (var gameMode : GameMode.values()) {
            bestResultByLevel.put(gameMode, Integer.MAX_VALUE);
        }
        language = Locale.getDefault();
    }

    public void setRandomColor(boolean random) {
        randomColor = random;
    }

    public boolean isRandomColor() {
        return randomColor;
    }

    public String getNameAcc() {
        return nameAcc;
    }

    public void setNameAcc(String nameAcc) {
        this.nameAcc = nameAcc;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getMaxBoardSize() {
        return boardMaxSize;
    }

    public void setBoardMaxSize(float boardMaxSize) {
        this.boardMaxSize = boardMaxSize;
    }

    public float getPiecesSpeed() {
        return piecesSpeed;
    }

    public void setPiecesSpeed(float piecesSpeed) {
        this.piecesSpeed = piecesSpeed;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public boolean isSaveWindowSize() {
        return saveWindowSize;
    }

    public void setSaveWindowSize(boolean saveWindowSize) {
        this.saveWindowSize = saveWindowSize;
    }

    public Pair<Integer, Integer> getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Pair<Integer, Integer> windowSize) {
        this.windowSize = windowSize;
    }

    public Locale getLocale() {
        return language;
    }

    public void setLocale(Locale language) {
        this.language = language;
    }

    public boolean isEnableSounds() {
        return enableSounds;
    }

    public void setEnableSounds(boolean enableSounds) {
        this.enableSounds = enableSounds;
    }

    public boolean isEnableBackgroundMusic() {
        return enableBackgroundMusic;
    }

    public void setEnableBackgroundMusic(boolean enableBackgroundMusic) {
        this.enableBackgroundMusic = enableBackgroundMusic;
    }

    public float getScreenDuration() {
        return screenSpeed;
    }

    public void setScreenSpeed(float screenSpeed) {
        this.screenSpeed = screenSpeed;
    }


    public long getTimeByTurn() {
        return timeByTurn;
    }

    public void setTimeByTurn(long timeByTurn) {
        this.timeByTurn = timeByTurn;
    }

    public boolean isInfinityByTurn() {
        return infinityByTurn;
    }

    public void setInfinityByTurn(boolean infinityByTurn) {
        this.infinityByTurn = infinityByTurn;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public void setMaxTurns(int maxTurns) {
        this.maxTurns = maxTurns;
    }

    public boolean isInfinityTurns() {
        return infinityTurns;
    }

    public void setInfinityTurns(boolean infinityTurns) {
        this.infinityTurns = infinityTurns;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public String getGameName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInfinityTimeGame() {
        return infinityTimeGame;
    }

    public void setInfinityTimeGame(boolean infinityTimeGame) {
        this.infinityTimeGame = infinityTimeGame;
    }

    public long getTimeByGame() {
        return timeByGame;
    }

    public void setTimeByGame(long timeByGame) {
        this.timeByGame = timeByGame;
    }

    public Color getPieceColor() {
        return pieceColor;
    }

    public void setPieceColor(Color pieceColor) {
        this.pieceColor = pieceColor;
    }

    public boolean isFlippedPieces() {
        return flippedPieces;
    }

    public void setFlippedPieces(boolean flippedPieces) {
        this.flippedPieces = flippedPieces;
    }

    public boolean isMatchDescription() {
        return matchDescription;
    }

    public void setMatchDescription(boolean matchDescription) {
        this.matchDescription = matchDescription;
    }


    public Fps getFps() {
        return fps;
    }

    public void setFps(Fps fps) {
        this.fps = fps;
    }

    public void setEnableSysProperties(boolean enableSysProperties) {
        this.enableSysProperties = enableSysProperties;
    }

    public boolean isEnableSysProperties() {
        return enableSysProperties;
    }

    public Map<GameMode, Integer> getBestResultByLevel() {
        return bestResultByLevel;
    }

    public GameMode getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(GameMode userLevel) {
        this.userLevel = userLevel;
    }

    public TurnMode getTurnMode() {
        return turnMode;
    }

    public void setTurnMode(TurnMode turnMode) {
        this.turnMode = turnMode;
    }

    public boolean isBlockedHints() {
        return blockedHints;
    }

    public void setBlockedHints(boolean blockedHints) {
        this.blockedHints = blockedHints;
    }

    public void saveState(MatchState ref) {

        if (references.size() == 50) return;
        for (int i = 0; i < references.size(); i++) {
            if (references.get(i).getMatch().getId() == ref.getMatch().getId()) {
                references.set(i, ref);
                return;
            }
        }

        references.add(0, ref);
    }

    public List<MatchState> getReferences() {
        return references;
    }

    public Array<GameMode> getAvailableLevels() {
        var storage = new Array<GameMode>();
        for (int i = 0; i < ChessConstants.localData.getUserLevel().ordinal() + 1; i++) {
            storage.add(GameMode.values()[i]);
        }
        return storage;
    }

    public enum Fps {
        TWENTY_FIVE(25),
        THIRTY(30),
        FORTY_FIVE(45),
        SIXTY(60),
        NINETY(90),
        ONE_HUNDRED_TWENTY(120),
        INFINITY(1000);

        private final int fps;

        Fps(int fps) {
            this.fps = fps;
        }

        public int getValue() {
            return fps;
        }

        public static Fps of(int fps) {
            switch (fps) {
                case 25: return TWENTY_FIVE;
                case 30: return THIRTY;
                case 45: return FORTY_FIVE;
                case 60: return SIXTY;
                case 90: return NINETY;
                case 120: return ONE_HUNDRED_TWENTY;
            }
            throw new IllegalArgumentException("there is no such value!");
        }
    }
}
