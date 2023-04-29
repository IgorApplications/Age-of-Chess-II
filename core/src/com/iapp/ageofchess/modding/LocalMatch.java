package com.iapp.ageofchess.modding;

import com.iapp.ageofchess.chess_engine.Color;
import com.iapp.ageofchess.multiplayer.RankType;
import com.iapp.ageofchess.multiplayer.TurnMode;
import com.iapp.ageofchess.util.ChessAssetManager;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Backwards compatible!
 * @version 1.0
 * @author Igor Ivanov
 * */
public class LocalMatch {

    private final long id;
    private final long mapDataId;
    private final String mapName;
    private final String scenarioName;
    private final String name;
    private final Color upperColor;
    private final boolean randomColor;
    private final GameMode gameMode;
    private final boolean flippedPieces;
    private final boolean infiniteMoves;
    private final int maxMoves;
    private final boolean infiniteTimeByTurn;
    private final long timeByTurn;
    private final boolean matchDescription;
    private final boolean blockedHints;
    private final boolean infiniteTimeByGame;
    private final long timeByGame;
    private final TurnMode turnMode;
    private final long createdTimeUTC;
    private final int numberScenario;
    private final boolean ranked;
    private final RankType rankType;
    private final long sponsored;

    public LocalMatch(long id, GameBuilder builder) {
        this.id = id;
        name = builder.name;
        upperColor = builder.upperColor;
        randomColor = builder.randomColor;
        mapDataId = builder.mapData.getId();
        ranked = builder.mapData.isRatingScenario(builder.numberScenario);
        var mapData = builder.mapData;
        mapName = mapData.getStrings().get("name");
        if (builder.mapData.getScenarios().length == 1) scenarioName = "null";
        else scenarioName = mapData.getStrings().get("title_scenario_" + (builder.numberScenario + 1));

        gameMode = builder.gameMode;
        flippedPieces = builder.flippedPieces;
        infiniteMoves = builder.infiniteTurns;
        maxMoves = builder.maxTurns;
        infiniteTimeByTurn = builder.infiniteTimeByTurn;
        timeByTurn = builder.timeByTurn;
        infiniteTimeByGame = builder.infiniteTimeByGame;
        timeByGame = builder.timeByGame;
        matchDescription = builder.matchDescription;
        blockedHints = builder.blockedHints;
        turnMode = builder.turnMode;
        numberScenario = builder.numberScenario;
        rankType = builder.rankType;
        sponsored = builder.sponsored;

        createdTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
    }

    public boolean isRandomColor() {
        return randomColor;
    }

    public RankType getRankType() {
        return rankType;
    }

    public long getSponsored() {
        return sponsored;
    }

    public long getId() {
        return id;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public int getNumberScenario() {
        return numberScenario;
    }

    public String getMapName() {
        return mapName;
    }

    public String getName() {
        return name;
    }

    public Color getUpperColor() {
        return upperColor;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public boolean isFlippedPieces() {
        return flippedPieces;
    }

    public boolean isInfiniteMoves() {
        return infiniteMoves;
    }

    public boolean isRanked() {
        return ranked;
    }

    public MapData getMatchData() {
        for (var mapData : ChessAssetManager.current().getDataMaps()) {
            if (mapData.getId() == mapDataId) {
                return mapData;
            }
        }
        throw new IllegalStateException("map data don't found by id!");
    }

    public boolean containsMatchData() {
        for (var mapData : ChessAssetManager.current().getDataMaps()) {
            if (mapData.getId() == mapDataId) {
                return true;
            }
        }
        return false;
    }

    public int getMaxMoves() {
        return maxMoves;
    }

    public boolean isInfiniteTimeByTurn() {
        return infiniteTimeByTurn;
    }

    public long getTimeByTurn() {
        return timeByTurn;
    }

    public boolean isMatchDescription() {
        return matchDescription;
    }

    public boolean isBlockedHints() {
        return blockedHints;
    }

    public boolean isInfiniteTimeByGame() {
        return infiniteTimeByGame;
    }

    public long getTimeByGame() {
        return timeByGame;
    }

    public TurnMode getTurnMode() {
        return turnMode;
    }


    public long getCreatedTimeUTC() {
        return createdTimeUTC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalMatch localMatch = (LocalMatch) o;
        return id == localMatch.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class GameBuilder {

        private final String name;
        private final Color upperColor;
        private boolean randomColor;
        private final MapData mapData;
        private GameMode gameMode;
        private boolean flippedPieces = false;
        private boolean infiniteTurns = true;
        private int maxTurns;
        private boolean infiniteTimeByTurn = true;
        private long timeByTurn = -1;
        private boolean infiniteTimeByGame;
        private long timeByGame = -1;
        private boolean matchDescription;
        private boolean blockedHints;
        private TurnMode turnMode;
        private int numberScenario = 0;
        private RankType rankType;
        private long sponsored;

        public GameBuilder(String name, Color upperColor, MapData mapData) {
            this.name = name;
            this.upperColor = upperColor;
            this.mapData = mapData;
        }

        public GameBuilder sponsored(long sponsored) {
            this.sponsored = sponsored;
            return this;
        }

        public GameBuilder gameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public GameBuilder flippedPieces(boolean flippedPieces) {
            this.flippedPieces = flippedPieces;
            return this;
        }

        public GameBuilder infiniteTurns(boolean infiniteMoves) {
            this.infiniteTurns = infiniteMoves;
            return this;
        }

        public GameBuilder maxTurns(int maxTurns) {
            this.maxTurns = maxTurns;
            return this;
        }

        public GameBuilder infiniteTimeByTurn(boolean infiniteTimeByTurn) {
            this.infiniteTimeByTurn = infiniteTimeByTurn;
            return this;
        }

        public GameBuilder timeByTurn(long timeByTurn) {
            this.timeByTurn = timeByTurn;
            return this;
        }

        public GameBuilder matchInfo(boolean matchInfo) {
            this.matchDescription = matchInfo;
            return this;
        }

        public GameBuilder blockedHints(boolean blockedHints) {
            this.blockedHints = blockedHints;
            return this;
        }

        public GameBuilder infiniteTimeByGame(boolean infiniteTimeByGame) {
            this.infiniteTimeByGame = infiniteTimeByGame;
            return this;
        }

        public GameBuilder timeByGame(long timeByGame) {
            this.timeByGame = timeByGame;
            return this;
        }

        public GameBuilder turnMode(TurnMode turnMode) {
            this.turnMode = turnMode;
            return this;
        }

        public GameBuilder numberScenario(int numberScenario) {
            this.numberScenario = numberScenario;
            return this;
        }

        public GameBuilder rankType(RankType rankType) {
            this.rankType = rankType;
            return this;
        }

        public GameBuilder randomColor(boolean randomColor) {
            this.randomColor = randomColor;
            return this;
        }
    }
}
