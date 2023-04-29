package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ObjectMap;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.EditMapActivity;
import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.activity.ModdingActivity;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.modding.MapResources;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.Spinner;
import com.iapp.rodsher.screens.Controller;
import com.iapp.rodsher.screens.RdApplication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ModdingController extends Controller {

    @SuppressWarnings("SimpleDateFormat")
    private static final DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    private final ModdingActivity activity;

    public ModdingController(ModdingActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void goToMenu() {
        startActivity(new MenuActivity(), ChessConstants.localData.getScreenDuration());
    }

    public void goToEdit(MapData mapData, boolean newMap) {
        var spinner = new Spinner(strings.get("loading"));
        activity.setSpinner(spinner);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // if developer map
        if (mapData.getType() == Files.FileType.Internal) {
            mapData.setId(MapData.generateModdingId(ChessAssetManager.current().getDataMaps()));
        }

        LoaderMap.self().loadIntoRam(mapData, () -> {
            var resources = new MapResources();

            Runnable task = () -> {
                loadStrings(mapData, resources);
                loadBytes(mapData, resources);
                replacePaths(mapData);
                mapData.setType(Files.FileType.External);

                startActivityAlpha(new EditMapActivity(mapData, resources, newMap),
                        ChessConstants.localData.getScreenDuration());
            };
            RdApplication.self().execute(task);
        });
    }

    private void replacePaths(MapData mapData) {
        if (mapData.getType() != Files.FileType.Internal) return;

        if (mapData.getAtlasPath() != null) {
            mapData.setAtlasPath(ChessConstants.STORAGE_DIRECTORY + "/" +
                    mapData.getAtlasPath().replaceAll("map\\d+", "map" + mapData.getId()));
        }

        for (int i = 0; i < mapData.getTexturePaths().length; i++) {
            mapData.getTexturePaths()[i] = ChessConstants.STORAGE_DIRECTORY + "/" +
                    mapData.getTexturePaths()[i].replaceAll("map\\d+", "map" + mapData.getId());
        }

        if (mapData.getMapIconPath() != null) {
            mapData.setMapIconPath(ChessConstants.STORAGE_DIRECTORY + "/" +
                    mapData.getMapIconPath().replaceAll("map\\d+", "map" + mapData.getId()));
        }

        for (int i = 0; i < mapData.getScenarioIconPaths().length; i++) {
            if (mapData.getScenarioIconPaths()[i] == null) continue;
            mapData.getScenarioIconPaths()[i] = ChessConstants.STORAGE_DIRECTORY + "/" +
                    mapData.getScenarioIconPaths()[i].replaceAll("map\\d+", "map" + mapData.getId());
        }

        if (mapData.getStringsPath() != null) {
            mapData.setStringsPath(ChessConstants.STORAGE_DIRECTORY + "/" +
                    mapData.getStringsPath().replaceAll("map\\d+", "map" + mapData.getId()));
        }
    }

    private void loadStrings(MapData mapData, MapResources resources) {
        var currentDate = getCurrentDate();

        if (mapData.getStrings() != null) {
            for (var lang : ChessApplication.self().getLanguages()) {

                if (Gdx.files.getFileHandle(mapData.getStringsPath(), mapData.getType())
                        .parent().child("lang_" + lang + ".properties").exists()) {

                    mapData.updateLang(lang);
                    var properties = mapData.getStrings().getProperties();
                    properties.put("updated", currentDate);
                    resources.getStrings().put(lang, properties);
                }
            }
        } else {
            mapData.setStringsPath(ChessConstants.MAPS_DIRECTORY + "/map" + mapData.getId() + "/lang");

            var stringsEn = new ObjectMap<String, String>();
            stringsEn.put("name", "null");
            stringsEn.put("description", "null");
            stringsEn.put("author", "null");
            stringsEn.put("created", currentDate);
            stringsEn.put("updated", currentDate);

            for (int i = 0; i < mapData.getScenarios().length; i++) {
                stringsEn.put("title_scenario_" + (i + 1), "null");
                stringsEn.put("desc_scenario_" + (i + 1), "null");
            }

            resources.getStrings().put("en", stringsEn);
        }
    }

    private void loadBytes(MapData mapData, MapResources resources) {
        // textures loading
        var textureNames = new ArrayList<String>();
        var textures = new ArrayList<byte[]>();

        for (int i = 0; i < mapData.getTexturePaths().length; i++) {
            var handle = Gdx.files.getFileHandle(mapData.getTexturePaths()[i], mapData.getType());
            if (!handle.exists()) {
                continue;
            }
            var texture = handle.readBytes();

            textureNames.add(handle.name());
            textures.add(texture);
        }
        if (mapData.getMapIconPath() != null) {
            var handle = Gdx.files.getFileHandle(mapData.getIconHandle(), mapData.getType());
            if (handle.exists()) {
                textureNames.add(handle.name());
                textures.add(handle.readBytes());
            }
        }
        for (int i = 0; i < mapData.getScenarioIconPaths().length; i++) {
            if (mapData.getScenarioIconPaths()[i] == null) continue;
            var handle = Gdx.files.getFileHandle(mapData.getScenarioIconPaths()[i], mapData.getType());
            textureNames.add(handle.name());
            textures.add(handle.readBytes());
        }

        resources.setTextureNames(textureNames.toArray(new String[0]));
        resources.setTextures(textures.toArray(new byte[0][]));

        // atlas loading
        if (mapData.getAtlasPath() == null) return;
        var atlasHandle = Gdx.files.getFileHandle(mapData.getAtlasPath(), mapData.getType());
        if (!atlasHandle.exists()) return;
        byte[] atlasDesc = atlasHandle.readBytes();
        var atlasPngNames = getAtlasPngNames(atlasDesc);
        byte[][] atlasPng = new byte[atlasPngNames.length][];
        for (int i = 0; i < atlasPngNames.length; i++) {
            var atlasPngHandle = atlasHandle.parent().child(atlasPngNames[i]);
            if (!atlasPngHandle.exists()) continue;
            atlasPng[i] = atlasPngHandle.readBytes();
        }
        resources.setAtlasPngNames(atlasPngNames);
        resources.setAtlasPng(atlasPng);
        resources.setAtlasDescName(atlasHandle.name());
        resources.setAtlasDesc(atlasDesc);
    }

    private String[] getAtlasPngNames(byte[] atlasDesc) {
        var result = new ArrayList<String>();
        var data = new String(atlasDesc);
        for (var el : data.split("\n")) {
            if (el.endsWith(".png")) {
                result.add(el);
            }
        }
        return result.toArray(new String[0]);
    }

    private String getCurrentDate() {
        return formatter.format(new Date());
    }
}
