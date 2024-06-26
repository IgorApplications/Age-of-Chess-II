package com.iapp.ageofchess.services;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.google.gson.Gson;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.modding.MapResources;
import com.iapp.lib.ui.screens.RdLogger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DataManager {

    private static final DataManager INSTANCE = new DataManager();
    private final Gson gson;

    public static DataManager self() {
        return INSTANCE;
    }

    public void loadMapData(MapData mapData, MapResources mapResources) {
        var folderHandle = Gdx.files.local(ChessConstants.MAPS_DIRECTORY + ("/map" + mapData.getId()));
        if (folderHandle.exists()) folderHandle.deleteDirectory();

        if (mapResources.getAtlasDesc() != null && mapResources.getAtlasDescName() != null) {
            folderHandle.child(mapResources.getAtlasDescName())
                    .writeBytes(mapResources.getAtlasDesc(), false);
        }

        if (mapResources.getAtlasPng() != null && mapResources.getAtlasPngNames() != null) {
            for (int i = 0; i < mapResources.getAtlasPngNames().length; i++) {
                folderHandle.child(mapResources.getAtlasPngNames()[i])
                        .writeBytes(mapResources.getAtlasPng()[i], false);
            }
        }

        if (mapResources.getTextures() != null && mapResources.getTextureNames() != null) {
            for (int i = 0; i < mapResources.getTextureNames().length; i++) {
                folderHandle.child(mapResources.getTextureNames()[i])
                        .writeBytes(mapResources.getTextures()[i], false);
            }
        }

        // strings
        // INFO PLIST REMOVED!

        for (var entry : mapResources.getStrings().entries()) {
            folderHandle.child("lang_" + entry.key + ".properties")
                    .writeString(getStrings(entry.value), false, StandardCharsets.UTF_8.toString());
        }

        // map
        folderHandle.child("mapData").writeString(gson.toJson(mapData), false);
    }

    public void removeMapData(MapData mapData) {
        if (mapData.getType() == Files.FileType.Internal) throw new IllegalArgumentException("Can't be deleted! Internal path!");
        var handler = Gdx.files.getFileHandle(
            ChessConstants.MAPS_DIRECTORY + "/map" + mapData.getId(), ChessConstants.FILE_TYPE);
        if (!handler.exists()) throw new IllegalStateException("Folder not found! Path = " + handler);
        handler.deleteDirectory();
    }

    public Array<MapData> readDataMaps() {
        var dataMaps = new Array<MapData>();
        var handler = Gdx.files.getFileHandle(ChessConstants.MAPS_DIRECTORY, ChessConstants.FILE_TYPE);

        for (var child1 : handler.list()) {
            if (child1.name().startsWith("map")) {
                var child2 = child1.child("mapData");
                if (child2.exists()) {
                    try {
                        dataMaps.add(gson.fromJson(child2.readString(),
                            MapData.class));
                    } catch (Throwable t) {
                        Gdx.app.error("readDataMaps", child2.path());
                    }
                }
            }
        }

        return dataMaps;
    }

    public void saveLocalData(LocalData localData) {
        var accountHandler = Gdx.files.getFileHandle(ChessConstants.SETTINGS, ChessConstants.FILE_TYPE);
        if (localData == null) return;
        accountHandler.writeString(gson.toJson(localData), false);
    }

    public LocalData readLocalData() {
        var accountHandler = Gdx.files.getFileHandle(ChessConstants.SETTINGS, ChessConstants.FILE_TYPE);
        if (!accountHandler.exists()) return new LocalData();
        try {
            var localData = gson.fromJson(
                accountHandler.readString(StandardCharsets.UTF_8.toString()),
                LocalData.class);

            // remove bad links! backward compatibility
            localData.getReferences().removeIf(ref -> ref.getMatch() == null);
            return localData;
        } catch (Throwable t) {
            Gdx.app.error("readLocalData", RdLogger.self().getDescription(t));
            return new LocalData();
        }
    }

    private DataManager() {
        gson = new Gson();
    }

    private byte[] readAbsBytes(String path) {
        return Gdx.files.absolute(path).readBytes();
    }

    private String getStrings(ObjectMap<String, String> stringMap) {
        var result = new StringBuilder();
        for (var entry : stringMap.entries()) {
            result.append(entry.key)
                    .append("=")
                    .append(entry.value)
                    .append("\n");
        }
        return result.toString();
    }
}
