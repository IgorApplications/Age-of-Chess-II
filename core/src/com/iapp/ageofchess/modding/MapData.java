package com.iapp.ageofchess.modding;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.util.RdI18NBundle;
import com.iapp.lib.util.StreamUtil;

import java.util.*;

/**
 * Backwards compatible!
 * @version 1.0
 * @author Igor Ivanov
 * */
public class MapData implements Disposable {

    private long id;
    private Files.FileType type;
    private @Null String atlasPath;
    private String[] nameTextures;
    private String[] texturePaths;

    private @Null String mapIconPath;
    private @Null String stringsPath;
    private TypeMap typeMap;
    private float width, height;
    private float padLeft, padRight, padBottom, padTop;
    private String[] scenarioIconPaths;
    private String[] scenarios;

    private transient final Map<Drawable, Texture> disposableMap;
    private transient @Null Drawable mapIcon;
    private transient Drawable[] scenarioIcons;
    private transient @Null RdI18NBundle strings;
    private transient @Null TextureAtlas atlas;

    // GSON constructor
    MapData() {
         disposableMap = new HashMap<>();
    }

    public MapData(MapData mapData) {
        this();
        id = mapData.id;
        type = mapData.type;
        atlasPath = mapData.atlasPath;
        nameTextures = mapData.nameTextures.clone();
        texturePaths = mapData.texturePaths.clone();
        mapIconPath = mapData.mapIconPath;
        stringsPath = mapData.stringsPath;
        typeMap = mapData.typeMap;
        width = mapData.width;
        height = mapData.height;
        padLeft = mapData.padLeft;
        padRight = mapData.padRight;
        padBottom = mapData.padBottom;
        padTop = mapData.padTop;
        scenarioIconPaths = mapData.scenarioIconPaths.clone();
        scenarios = mapData.scenarios.clone();

        mapIcon = mapData.mapIcon;
        scenarioIcons = mapData.scenarioIcons;
        strings = mapData.strings;
        atlas = mapData.atlas;
    }

    public MapData(long id, Files.FileType type, String atlasPath, String[] nameTextures, String[] texturePaths,
                   @Null String mapIconPath, @Null String stringsPath, TypeMap typeMap, float width, float height,
                   float padLeft, float padRight, float padBottom, float padTop,
                   String[] scenarios, String[] scenarioIconPaths) {
        this();
        this.id = id;
        this.type = type;
        this.atlasPath = atlasPath;
        this.nameTextures = nameTextures;
        this.texturePaths = texturePaths;
        this.mapIconPath = mapIconPath;
        this.stringsPath = stringsPath;
        this.typeMap = typeMap;
        this.width = width;
        this.height = height;
        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padBottom = padBottom;
        this.padTop = padTop;
        this.scenarios = scenarios;
        this.scenarioIconPaths = scenarioIconPaths;
    }

    public MapData(long id, Files.FileType type, TypeMap typeMap, float width, float height,
                   float padLeft, float padRight, float padBottom, float padTop) {
        this();
        this.id = id;
        this.type = type;
        this.typeMap = typeMap;
        this.width = width;
        this.height = height;
        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padBottom = padBottom;
        this.padTop = padTop;
        scenarios = new String[]{"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq"};
        nameTextures = new String[]{};
        texturePaths = new String[]{};
        scenarioIconPaths = new String[scenarios.length];
        scenarioIcons = new Drawable[scenarios.length];
    }

    private transient AssetDescriptor<Texture> iconDesc;
    private transient AssetDescriptor<Texture>[] scenarioIconDesc;

    @SuppressWarnings("unchecked")
    public boolean loadTextures(AssetManager assetManager) {
        // loading of resources in RAM
        if (mapIconPath != null) {
            var handle = Gdx.files.getFileHandle(mapIconPath, type);
            if (!handle.exists()) return false;
            iconDesc = new AssetDescriptor<>(Gdx.files.getFileHandle(mapIconPath, type), Texture.class);
            assetManager.load(iconDesc);
        } else {
            mapIcon = new TextureRegionDrawable(
                    ChessAssetManager.current().findChessRegion("cross"));
        }

        scenarioIcons = new Drawable[scenarios.length];
        scenarioIconDesc = new AssetDescriptor[scenarios.length];
        for (int i = 0; i < scenarios.length; i++) {
            if (scenarioIconPaths[i] == null) {
                scenarioIcons[i] = new TextureRegionDrawable(
                        ChessAssetManager.current().findChessRegion("cross"));
                continue;
            }

            var handle = Gdx.files.getFileHandle(scenarioIconPaths[i], type);
            if (!handle.exists()) return false;
            scenarioIconDesc[i] = new AssetDescriptor<>(
                    Gdx.files.getFileHandle(scenarioIconPaths[i], type), Texture.class);
            assetManager.load(scenarioIconDesc[i]);
        }

        return true;
    }

    public void initTextures(AssetManager assetManager) {
        if (iconDesc != null) {
            var texture = assetManager.get(iconDesc);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            mapIcon = new TextureRegionDrawable(texture);
            disposableMap.put(mapIcon, texture);
        }

        for (int i = 0; i < scenarioIconDesc.length; i++) {
            if (scenarioIconDesc[i] == null) continue;
            var texture = assetManager.get(scenarioIconDesc[i]);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            scenarioIcons[i] = new TextureRegionDrawable(texture);
            disposableMap.put(scenarioIcons[i], texture);
        }
    }

    public boolean loadStrings() {
        if (stringsPath != null) {
            var handle = Gdx.files.getFileHandle(stringsPath, type);
            if (!handle.parent().child(handle.name() + "_en.properties").exists()) return false;
            strings = RdI18NBundle.createBundle(handle, new Locale(ChessConstants.localData.getLangCode()));
        } else {
            return false;
        }
        return true;
    }

    public static long generateModdingId(List<MapData> array) {
        // user modding maps always < 0
        long minimum = 0;
        for (var mapData : array) {
            if (minimum > mapData.getId()) {
                minimum = mapData.getId();
            }
        }
        return minimum - 1;
    }

    public String getMapIconPath() {
        return mapIconPath;
    }

    public long getId() {
        return id;
    }

    public void updateLang() {
        strings = RdI18NBundle.createBundle(Gdx.files.getFileHandle(stringsPath, type),
            new Locale(ChessConstants.localData.getLangCode()));
    }

    public void updateLang(String language) {
        var locale = new Locale(language);
        strings = RdI18NBundle.createBundle(Gdx.files.getFileHandle(stringsPath, type), locale);
    }

    public boolean isRatingScenario(int number) {
        return scenarios[number].equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
    }

    public Map<Drawable, Texture> getDisposableMap() {
        return disposableMap;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(Files.FileType type) {
        this.type = type;
    }

    public void setAtlasPath(String atlasPath) {
        this.atlasPath = atlasPath;
    }

    public void setNameTextures(String[] nameTextures) {
        this.nameTextures = nameTextures;
    }

    public void setTexturePaths(String[] texturePaths) {
        this.texturePaths = texturePaths;
    }

    public void setMapIconPath(String mapIconPath) {
        this.mapIconPath = mapIconPath;
    }

    public void setStringsPath(String stringsPath) {
        this.stringsPath = stringsPath;
    }

    public void setTypeMap(TypeMap typeMap) {
        this.typeMap = typeMap;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setPadLeft(float padLeft) {
        this.padLeft = padLeft;
    }

    public void setPadRight(float padRight) {
        this.padRight = padRight;
    }

    public void setPadBottom(float padBottom) {
        this.padBottom = padBottom;
    }

    public void setPadTop(float padTop) {
        this.padTop = padTop;
    }

    public void setScenarioIconPaths(String[] scenarioIconPaths) {
        this.scenarioIconPaths = scenarioIconPaths;
    }

    public void setScenarios(String[] scenarios) {
        this.scenarios = scenarios;
    }

    public void setMapIcon(Texture mapIconTexture) {
        if (mapIconTexture == null) {
            mapIcon = new TextureRegionDrawable(
                    ChessAssetManager.current().findChessRegion("cross"));
            return;
        }

        mapIcon = new TextureRegionDrawable(mapIconTexture);
        disposableMap.put(mapIcon, mapIconTexture);
    }

    public void setScenarioIcons(Drawable[] scenarioIcons) {
        this.scenarioIcons = scenarioIcons;
    }

    public void setStrings(RdI18NBundle strings) {
        this.strings = strings;
    }

    public String[] getNameTextures() {
        return nameTextures;
    }

    public String[] getTexturePaths() {
        return texturePaths;
    }

    public String[] getScenarioIconPaths() {
        return scenarioIconPaths;
    }

    public Drawable[] getScenarioIcons() {
        return scenarioIcons;
    }

    public @Null TextureAtlas getAtlas() {
        return atlas;
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public Files.FileType getType() {
        return type;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getPadLeft() {
        return padLeft;
    }

    public float getPadRight() {
        return padRight;
    }

    public float getPadBottom() {
        return padBottom;
    }

    public float getPadTop() {
        return padTop;
    }

    public String getAtlasPath() {
        return atlasPath;
    }

    public @Null String getIconHandle() {
        return mapIconPath;
    }

    public TypeMap getTypeMap() {
        return typeMap;
    }

    public String[] getScenarios() {
        return scenarios;
    }

    public @Null String getStringsPath() {
        return stringsPath;
    }

    public @Null Drawable getMapIcon() {
        return mapIcon;
    }

    public @Null RdI18NBundle getStrings() {
        return strings;
    }

    @Override
    public String toString() {
        return "MapData{" +
                "id=" + id +
                ", type=" + type +
                ", atlasPath='" + atlasPath + '\'' +
                ", nameTextures=" + Arrays.toString(nameTextures) +
                ", texturePaths=" + Arrays.toString(texturePaths) +
                ", mapIconPath='" + mapIconPath + '\'' +
                ", stringsPath='" + stringsPath + '\'' +
                ", typeMap=" + typeMap +
                ", width=" + width +
                ", height=" + height +
                ", padLeft=" + padLeft +
                ", padRight=" + padRight +
                ", padBottom=" + padBottom +
                ", padTop=" + padTop +
                ", scenarioIconPaths=" + Arrays.toString(scenarioIconPaths) +
                ", scenarios=" + Arrays.toString(scenarios) +
                ", mapIcon=" + mapIcon +
                ", scenarioIcons=" + Arrays.toString(scenarioIcons) +
                ", strings=" + strings +
                ", atlas=" + atlas +
                '}';
    }

    @Override
    public void dispose() {
        if (disposableMap != null) {
            StreamUtil.streamOf(disposableMap.values())
                    .forEach(Disposable::dispose);
        }
    }
}
