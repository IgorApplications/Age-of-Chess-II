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

public final class SettingsUtil {

    public String defineDefaultGameMode(GameMode mode) {
        var strings = RdApplication.self().getStrings();

        if (mode == GameMode.TWO_PLAYERS) return strings.get("two_players");
        else if (mode == GameMode.NOVICE) return strings.get("novice");
        else if (mode == GameMode.EASY) return strings.get("easy");
        else if (mode == GameMode.AVERAGE) return strings.get("average");
        else if (mode == GameMode.HARD) return strings.get("hard");
        else if (mode == GameMode.EPIC) return strings.get("epic");
        else if (mode == GameMode.MASTER_CANDIDATE) return strings.get("candidate_master");
        else if (mode == GameMode.MASTER) return strings.get("master");
        else if (mode == GameMode.GRADMASTER) return strings.get("grandmaster");
        else throw new IllegalArgumentException("unknown game mode");
    }

    public static String defineFPS() {
        var strings = RdApplication.self().getStrings();

        var selectedFps = ChessConstants.localData.getFps();
        if (selectedFps == LocalData.Fps.INFINITY) return strings.get("infinity");
        return ChessConstants.localData.getFps().getValue() + " fps";
    }

    public static String defineColor() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.getPieceColor() == com.iapp.lib.chess_engine.Color.BLACK) return strings.get("black");
        return strings.get("white");
    }

    public static String defineColor(com.iapp.lib.chess_engine.Color color) {
        var strings = RdApplication.self().getStrings();

        if (color == com.iapp.lib.chess_engine.Color.BLACK) return strings.get("black");
        return strings.get("white");
    }

    public static String defineTimeByTurn() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.isInfinityByTurn()) return strings.get("infinity");
        return strings.format("min_by_move", ChessConstants.localData.getTimeByTurn() / 1000 / 60);
    }

    public static String defineTurnMode() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.getTurnMode() == TurnMode.ALTERNATELY) return strings.get("concurrent");
        else return strings.get("concurrent_fast");
    }

    public static String defineMaxTurns() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.isInfinityTurns()) return strings.get("infinity");
        return strings.format("turns", ChessConstants.localData.getMaxTurns());
    }

    public static String defineTimeByGame() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.isInfinityTimeGame()) return strings.get("infinity");
        long minutes = ChessConstants.localData.getTimeByGame() / 1000 / 60;
        if (minutes > 60) return strings.format("hours", minutes / 60);
        return strings.format("minutes", minutes);
    }

    public static com.iapp.lib.chess_engine.Color defineColor(String upperColor) {
        var strings = RdApplication.self().getStrings();

        if (upperColor.contains(strings.get("white")))
            return com.iapp.lib.chess_engine.Color.WHITE;
        else if (upperColor.contains(strings.get("black")))
            return com.iapp.lib.chess_engine.Color.BLACK;
        throw new IllegalArgumentException("unknown color pieces");
    }

    public static Pair<Boolean, Integer> defineMaxTurns(String maxTurns) {
        var strings = RdApplication.self().getStrings();

        if (maxTurns.equals(strings.get("infinity"))) return new Pair<>(true, -1);
        return new Pair<>(false, Integer.parseInt(maxTurns.replaceAll("\\D+", "")));
    }

    public static Pair<Boolean, Long> defineTimeByTurn(String timeByTurn) {
        var strings = RdApplication.self().getStrings();

        if (timeByTurn.equals(strings.get("infinity"))) return new Pair<>(true, 0L);
        return new Pair<>(false, Long.parseLong(timeByTurn.replaceAll("\\D+", "")) * 60 * 1000);
    }

    public static TurnMode defineTurnMode(String turnMode) {
        var strings = RdApplication.self().getStrings();

        if (turnMode.equals(strings.get("concurrent"))) return TurnMode.ALTERNATELY;
        else return TurnMode.ALTERNATELY_FAST;
    }

    public static Pair<Boolean, Long> defineTimeByGame(String timeByGame) {
        var strings = RdApplication.self().getStrings();

        var minutes = strings.format("minutes", 5)
                .replaceAll("\\d*\\s", "");
        var hours = strings.format("hours", 1)
                .replaceAll("\\d*\\s", "");;

        if (timeByGame.equals(strings.get("infinity"))) {
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

        if (gameMode == GameMode.TWO_PLAYERS) return strings.get("two_players");
        else if (gameMode == GameMode.NOVICE) return strings.get("novice");
        else if (gameMode == GameMode.EASY) return strings.get("easy");
        else if (gameMode == GameMode.AVERAGE) return strings.get("average");
        else if (gameMode == GameMode.HARD) return strings.get("hard");
        else if (gameMode == GameMode.EPIC) return strings.get("epic");
        else if (gameMode == GameMode.MASTER_CANDIDATE) return strings.get("candidate_master");
        else if (gameMode == GameMode.MASTER) return strings.get("master");
        else if (gameMode == GameMode.GRADMASTER) return strings.get("grandmaster");
        else if (gameMode == GameMode.MULTIPLAYER) return strings.get("multiplayer");
        else throw new IllegalArgumentException("unknown game mode");
    }

    public static String defineDefaultGameMode() {
        var strings = RdApplication.self().getStrings();

        if (ChessConstants.localData.getGameMode() == GameMode.TWO_PLAYERS) return strings.get("two_players");
        else if (ChessConstants.localData.getGameMode() == GameMode.NOVICE) return strings.get("novice");
        else if (ChessConstants.localData.getGameMode() == GameMode.EASY) return strings.get("easy");
        else if (ChessConstants.localData.getGameMode() == GameMode.AVERAGE) return strings.get("average");
        else if (ChessConstants.localData.getGameMode() == GameMode.HARD) return strings.get("hard");
        else if (ChessConstants.localData.getGameMode() == GameMode.EPIC) return strings.get("epic");
        else if (ChessConstants.localData.getGameMode() == GameMode.MASTER_CANDIDATE) return strings.get("candidate_master");
        else if (ChessConstants.localData.getGameMode() == GameMode.MASTER) return strings.get("master");
        else if (ChessConstants.localData.getGameMode() == GameMode.GRADMASTER) return strings.get("grandmaster");
        else throw new IllegalArgumentException("unknown game mode");
    }

    public static Pair<String, Color> defineResult(Result result) {
        var strings = RdApplication.self().getStrings();

        switch (result) {
            case VICTORY: return new Pair<>(strings.get("victory"), Color.GOLD);
            case DRAWN: return new Pair<>(strings.get("drawn"), Color.GREEN);
            case LOSE: return new Pair<>(strings.get("lose"), Color.RED);
            case WHITE_VICTORY: return new Pair<>(strings.get("white_victory"), Color.GOLD);
            case BLACK_VICTORY: return new Pair<>(strings.get("black_victory"), Color.GOLD);
            case NONE: return new Pair<>(strings.get("none"), Color.WHITE);
        }
        throw new IllegalArgumentException("game result type is unknown");
    }

    public static com.iapp.lib.chess_engine.Color reverse(com.iapp.lib.chess_engine.Color source) {
        return source == com.iapp.lib.chess_engine.Color.BLACK ? com.iapp.lib.chess_engine.Color.WHITE : com.iapp.lib.chess_engine.Color.BLACK;
    }

    public static String getGenderText(Gender gender) {
        var strings = RdApplication.self().getStrings();

        if (Gender.MALE == gender) return strings.get("male");
        else if (Gender.FEMALE == gender) return strings.get("female");
        else if (Gender.ANOTHER == gender) return strings.get("another");
        return strings.get("nd");
    }

    public static Gender getGender(String text) {
        var strings = RdApplication.self().getStrings();

        if (text.equals(strings.get("male"))) return Gender.MALE;
        else if (text.equals(strings.get("female"))) return Gender.FEMALE;
        else if (text.equals(strings.get("another"))) return Gender.ANOTHER;
        return Gender.ND;
    }

    public static String[] getGendersText() {
        var strings = RdApplication.self().getStrings();
        String[] genders = new String[4];

        genders[0] = strings.get("male");
        genders[1] =  strings.get("female");
        genders[2] = strings.get("another");
        genders[3] =  strings.get("nd");

        return genders;
    }

    public static String[] getDisplayCountries() {
        String[] displayCountries = new String[ChessApplication.self().getCountryLocales().size() + 1];
        displayCountries[0] = "n/d";
        for (int i = 0; i < displayCountries.length - 1; i++) {
            displayCountries[i + 1] = ChessApplication.self().getCountryLocales().get(i)
                    .getDisplayCountry(ChessConstants.localData.getLocale());
        }
        return displayCountries;
    }

    public static String getRank(RankType type) {
        var strings = RdApplication.self().getStrings();

        if (type == RankType.BULLET) return strings.get("bullet");
        else if (type == RankType.BLITZ) return strings.get("blitz");
        else if (type == RankType.RAPID) return strings.get("rapid");
        else if (type == RankType.UNRANKED) return strings.get("non_ranked");
        else return strings.get("long");
    }

    public static RankType getRankType(String rankType) {
        var strings = RdApplication.self().getStrings();

        if (rankType.equals(strings.get("bullet"))) return RankType.BULLET;
        else if (rankType.equals(strings.get("blitz"))) return RankType.BLITZ;
        else if (rankType.equals(strings.get("rapid"))) return RankType.RAPID;
        else if (rankType.equals(strings.get("long"))) return RankType.LONG;
        return RankType.UNRANKED;
    }
}
