package com.iapp.ageofchess.services;

import com.badlogic.gdx.graphics.Color;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.lib.chess_engine.Result;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.lib.web.Gender;
import com.iapp.lib.web.RankType;
import com.iapp.ageofchess.multiplayer.TurnMode;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.Pair;

import java.util.Locale;

public final class SettingsUtil {

    public String defineDefaultGameMode(GameMode mode) {
        var strings = RdApplication.self().getStrings();

        if (mode == GameMode.TWO_PLAYERS) return strings.get("[i18n]Two Players Mode");
        else if (mode == GameMode.NOVICE) return strings.get("[i18n]Novice Level");
        else if (mode == GameMode.EASY) return strings.get("[i18n]Level Easy");
        else if (mode == GameMode.AVERAGE) return strings.get("[i18n]Average level");
        else if (mode == GameMode.HARD) return strings.get("[i18n]Hard level");
        else if (mode == GameMode.EPIC) return strings.get("[i18n]Level Epic");
        else if (mode == GameMode.MASTER_CANDIDATE) return strings.get("[i18n]Candidate Master");
        else if (mode == GameMode.MASTER) return strings.get("[i18n]Master");
        else if (mode == GameMode.GRADMASTER) return strings.get("[i18n]Grandmaster");
        else throw new IllegalArgumentException("unknown game mode");
    }

    public static String defineFPS() {
        var strings = RdApplication.self().getStrings();

        var selectedFps = ChessConstants.localData.getFps();
        if (selectedFps == LocalData.Fps.INFINITY) return strings.get("[i18n]infinity");
        return ChessConstants.localData.getFps().getValue() + " fps";
    }

    public static String defineColor() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.getPieceColor() == com.iapp.lib.chess_engine.Color.BLACK) return strings.get("[i18n]Black");
        return strings.get("[i18n]White");
    }

    public static String defineColor(com.iapp.lib.chess_engine.Color color) {
        var strings = RdApplication.self().getStrings();

        if (color == com.iapp.lib.chess_engine.Color.BLACK) return strings.get("[i18n]Black");
        return strings.get("[i18n]White");
    }

    public static String defineTimeByTurn() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.isInfinityByTurn()) return strings.get("[i18n]infinity");
        return strings.format("[i18n]{0,choice,1#1 minute|1<{0,number} minutes}/move", ChessConstants.localData.getTimeByTurn() / 1000 / 60);
    }

    public static String defineTurnMode() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.getTurnMode() == TurnMode.ALTERNATELY) return strings.get("[i18n]Alternately");
        else return strings.get("[i18n]Alternately & Fast");
    }

    public static String defineMaxTurns() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.isInfinityTurns()) return strings.get("[i18n]infinity");
        return strings.format("[i18n]{0,choice,1#1 turn|1<{0,number,integer} turns}", ChessConstants.localData.getMaxTurns());
    }

    public static String defineTimeByGame() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.isInfinityTimeGame()) return strings.get("[i18n]infinity");
        long minutes = ChessConstants.localData.getTimeByGame() / 1000 / 60;
        if (minutes > 60) return strings.format("[i18n]{0,choice,1#1 hour|1<{0,number} hours}", minutes / 60);
        return strings.format("[i18n]{0,choice,1#1 minute|1<{0,number} minutes}", minutes);
    }

    public static com.iapp.lib.chess_engine.Color defineColor(String upperColor) {
        var strings = RdApplication.self().getStrings();

        if (upperColor.contains(strings.get("[i18n]White")))
            return com.iapp.lib.chess_engine.Color.WHITE;
        else if (upperColor.contains(strings.get("[i18n]Black")))
            return com.iapp.lib.chess_engine.Color.BLACK;
        throw new IllegalArgumentException("unknown color pieces");
    }

    public static Pair<Boolean, Integer> defineMaxTurns(String maxTurns) {
        var strings = RdApplication.self().getStrings();

        if (maxTurns.equals(strings.get("[i18n]infinity"))) return new Pair<>(true, -1);
        return new Pair<>(false, Integer.parseInt(maxTurns.replaceAll("\\D+", "")));
    }

    public static Pair<Boolean, Long> defineTimeByTurn(String timeByTurn) {
        var strings = RdApplication.self().getStrings();

        if (timeByTurn.equals(strings.get("[i18n]infinity"))) return new Pair<>(true, 0L);
        return new Pair<>(false, Long.parseLong(timeByTurn.replaceAll("\\D+", "")) * 60 * 1000);
    }

    public static TurnMode defineTurnMode(String turnMode) {
        var strings = RdApplication.self().getStrings();

        if (turnMode.equals(strings.get("[i18n]Alternately"))) return TurnMode.ALTERNATELY;
        else return TurnMode.ALTERNATELY_FAST;
    }

    public static Pair<Boolean, Long> defineTimeByGame(String timeByGame) {
        var strings = RdApplication.self().getStrings();

        var minutes = strings.format("[i18n]{0,choice,1#1 minute|1<{0,number} minutes}/move", 5)
                .replaceAll("\\d*\\s", "");
        var hours = strings.format("[i18n]{0,choice,1#1 hour|1<{0,number} hours}", 1)
                .replaceAll("\\d*\\s", "");;

        if (timeByGame.equals(strings.get("[i18n]infinity"))) {
            return new Pair<>(true, 0L);

        } else if (timeByGame.contains(minutes)) {
            var time = Long.parseLong(timeByGame
                    .replaceAll("\\D+", "")) * 60 * 1000;

            return new Pair<>(false, time);

        } else if (timeByGame.contains(hours)) {
            var time = Long.parseLong(timeByGame
                    .replaceAll("\\D+", "")) * 60 * 60 * 1000;

            return new Pair<>(false, time);

        }
        throw new IllegalArgumentException("unknown time unit");
    }

    public static String[] getAvailableLevels() {
        var storage = ChessConstants.localData.getAvailableLevels();
        var items = new String[storage.size];

        for (int i = 0; i < storage.size; i++) {
            items[i] = defineGameMode(storage.get(i));
        }
        return items;
    }

    public static String defineGameMode(GameMode gameMode) {
        var strings = RdApplication.self().getStrings();

        if (gameMode == GameMode.TWO_PLAYERS) return strings.get("[i18n]Two Players Mode");
        else if (gameMode == GameMode.NOVICE) return strings.get("[i18n]Novice Level");
        else if (gameMode == GameMode.EASY) return strings.get("[i18n]Level Easy");
        else if (gameMode == GameMode.AVERAGE) return strings.get("[i18n]Average level");
        else if (gameMode == GameMode.HARD) return strings.get("[i18n]Hard level");
        else if (gameMode == GameMode.EPIC) return strings.get("[i18n]Level Epic");
        else if (gameMode == GameMode.MASTER_CANDIDATE) return strings.get("[i18n]Candidate Master");
        else if (gameMode == GameMode.MASTER) return strings.get("[i18n]Master");
        else if (gameMode == GameMode.GRADMASTER) return strings.get("[i18n]Grandmaster");
        else if (gameMode == GameMode.MULTIPLAYER) return strings.get("[i18n]Multiplayer");
        else throw new IllegalArgumentException("unknown game mode");
    }

    public static Pair<String, Color> defineResult(Result result) {
        var strings = RdApplication.self().getStrings();

        switch (result) {
            case VICTORY: return new Pair<>(strings.get("[i18n]Victory"), Color.GOLD);
            case DRAWN: return new Pair<>(strings.get("[i18n]Drawn"), Color.GREEN);
            case LOSE: return new Pair<>(strings.get("[i18n]Lose"), Color.RED);
            case WHITE_VICTORY: return new Pair<>(strings.get("[i18n]White Victory"), Color.GOLD);
            case BLACK_VICTORY: return new Pair<>(strings.get("[i18n]Black Victory"), Color.GOLD);
            case NONE: return new Pair<>(strings.get("[i18n]Game not over"), Color.WHITE);
        }
        throw new IllegalArgumentException("game result type is unknown");
    }

    public static com.iapp.lib.chess_engine.Color reverse(com.iapp.lib.chess_engine.Color source) {
        return source == com.iapp.lib.chess_engine.Color.BLACK ? com.iapp.lib.chess_engine.Color.WHITE : com.iapp.lib.chess_engine.Color.BLACK;
    }

    public static String getGenderText(Gender gender) {
        var strings = RdApplication.self().getStrings();

        if (Gender.MALE == gender) return strings.get("[i18n]Male");
        else if (Gender.FEMALE == gender) return strings.get("[i18n]Female");
        else if (Gender.ANOTHER == gender) return strings.get("[i18n]Another");
        return strings.get("[i18n]n/d");
    }

    public static Gender getGender(String text) {
        var strings = RdApplication.self().getStrings();

        if (text.equals(strings.get("[i18n]Male"))) return Gender.MALE;
        else if (text.equals(strings.get("[i18n]Female"))) return Gender.FEMALE;
        else if (text.equals(strings.get("[i18n]Another"))) return Gender.ANOTHER;
        return Gender.ND;
    }

    public static String[] getGendersText() {
        var strings = RdApplication.self().getStrings();
        String[] genders = new String[4];

        genders[0] = strings.get("[i18n]Male");
        genders[1] =  strings.get("[i18n]Female");
        genders[2] = strings.get("[i18n]Another");
        genders[3] =  strings.get("[i18n]n/d");

        return genders;
    }

    public static String[] getDisplayCountries() {
        String[] displayCountries = new String[ChessApplication.self().getCountryLocales().size() + 1];
        displayCountries[0] = "n/d";
        for (int i = 0; i < displayCountries.length - 1; i++) {
            displayCountries[i + 1] = ChessApplication.self().getCountryLocales().get(i)
                    .getDisplayCountry(new Locale(ChessConstants.localData.getLangCode()));
        }
        return displayCountries;
    }

    public static String getRank(RankType type) {
        var strings = RdApplication.self().getStrings();

        if (type == RankType.BULLET) return strings.get("[i18n]Bullet");
        else if (type == RankType.BLITZ) return strings.get("[i18n]Blitz");
        else if (type == RankType.RAPID) return strings.get("[i18n]Rapid");
        else if (type == RankType.UNRANKED) return strings.get("[i18n]Not ranked");
        else return strings.get("[i18n]Long");
    }

    public static RankType getRankType(String rankType) {
        var strings = RdApplication.self().getStrings();

        if (rankType.equals(strings.get("[i18n]Bullet"))) return RankType.BULLET;
        else if (rankType.equals(strings.get("[i18n]Blitz"))) return RankType.BLITZ;
        else if (rankType.equals(strings.get("[i18n]Rapid"))) return RankType.RAPID;
        else if (rankType.equals(strings.get("[i18n]Long"))) return RankType.LONG;
        return RankType.UNRANKED;
    }
}
