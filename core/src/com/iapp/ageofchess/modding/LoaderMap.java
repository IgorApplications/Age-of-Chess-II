package com.iapp.ageofchess.modding;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.DataManager;
import com.iapp.ageofchess.util.ResourcesLoader;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.RdLogger;
import com.iapp.rodsher.util.CallListener;
import com.iapp.rodsher.util.TaskLoad;

public class LoaderMap {

    private static final LoaderMap INSTANCE  = new LoaderMap();

    public static LoaderMap self() {
        return INSTANCE;
    }

    /**
     * Returns a task for parallel loading of resources from disk,
     * due to 3 threads it requires large processor resources at the start,
     * but is smoother and faster
     * */
    public TaskLoad getTaskLoadDiskMaps(CallListener onFinish) {
        return new TaskLoad() {
            @Override
            public void load() {
                Runnable task = () -> {
                    var internal = new Array<MapData>();
                    var external = new Array<MapData>();

                    internal.add(new MapData(
                            1,
                            Files.FileType.Internal,
                            null,
                            new String[]{}, new String[]{},
                            "maps/map1/icon.png",
                            "maps/map1/lang", TypeMap.TWO_D,
                            720, 720,0,0, 0,0,
                            new String[]{"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq"},
                            new String[1] // scenarios.length
                    ));
                    internal.add(new MapData(
                            2,
                            Files.FileType.Internal,
                            "maps/map2/atlas.atlas",
                            new String[]{}, new String[]{},
                            "maps/map2/icon.png",
                            "maps/map2/lang", TypeMap.TWO_D,
                            720, 720,0,0, 0,0,
                            new String[]{"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq",
                                    "rnbqkbnr/8/8/pppppppp/PPPPPPPP/8/8/RNBQKBNR w KQkq"},
                            new String[]{"maps/map2/scenario1.png",
                                    "maps/map2/scenario2.png"}
                    ));

                    external.addAll(DataManager.self().readDataMaps());

                    var internalLoader = new ResourcesLoader(Files.FileType.Internal);
                    var externalLoader = new ResourcesLoader(Files.FileType.External);

                    var internal2 = new Array<MapData>();
                    var external2 = new Array<MapData>();

                    for (var mapData : internal) {
                        if (mapData.loadStrings()) {
                            internal2.add(mapData);
                        }
                    }
                    for (var madData : external) {
                        if (madData.loadStrings()) {
                            external2.add(madData);
                        }
                    }

                    internal.clear();
                    external.clear();
                    internal.addAll(internal2);
                    external.addAll(external2);

                    var internal3 = new Array<MapData>();
                    var external3 = new Array<MapData>();

                    for (var dataMap : internal) {
                        try {
                            if (dataMap.loadTextures(internalLoader.getAssetManager())) {
                                internal3.add(dataMap);
                            }
                        } catch (Throwable t) {
                            Gdx.app.error("loadDiskMaps",
                                "Bad map (loadTextures) " + RdLogger.getDescription(t));
                        }
                    }
                    for (var dataMap : external) {
                        try {
                            if (dataMap.loadTextures(externalLoader.getAssetManager())) {
                                external3.add(dataMap);
                            }
                        } catch (Throwable t) {
                            Gdx.app.error("loadDiskMaps",
                                "Bad map (loadTextures) " + RdLogger.getDescription(t));
                        }
                    }

                    internal.clear();
                    external.clear();
                    internal.addAll(internal3);
                    external.addAll(external3);

                    internalLoader.setOnFinish(() -> {
                        int pushIndex = 0;
                        for (var mapData : internal) {
                            mapData.initTextures(
                                    internalLoader.getAssetManager());
                            ChessAssetManager.current().getDataMaps().add(pushIndex++, mapData);
                        }

                        setFinished(internalLoader.getAssetManager().isFinished()
                                && externalLoader.getAssetManager().isFinished());
                        if (isFinished()) onFinish.call();
                    });

                    externalLoader.setOnFinish(() -> {
                        for (var mapData : external) {
                            mapData.initTextures(
                                    externalLoader.getAssetManager());
                            ChessAssetManager.current().getDataMaps().add(mapData);
                        }

                        setFinished(internalLoader.getAssetManager().isFinished()
                                && externalLoader.getAssetManager().isFinished());
                        if (isFinished()) onFinish.call();
                    });

                    internalLoader.launchLoad();
                    externalLoader.launchLoad();
                };
                RdApplication.self().execute(task);
            }
        };
    }

    private AssetDescriptor<TextureAtlas> atlasDesc;
    private AssetDescriptor<Texture>[] textureDesc;

    @SuppressWarnings("unchecked")
    public void loadIntoRam(MapData mapData, CallListener onFinish) {
        // clear old links
        atlasDesc = null;
        textureDesc = null;

        var loader = new ResourcesLoader(mapData.getType());

        if (mapData.getAtlasPath() != null) {
            atlasDesc = new AssetDescriptor<>(Gdx.files.getFileHandle(
                    mapData.getAtlasPath(), mapData.getType()), TextureAtlas.class);
            loader.getAssetManager().load(atlasDesc);
        } else {
            mapData.setAtlas(new TextureAtlas());
        }

        textureDesc = new AssetDescriptor[mapData.getNameTextures().length];
        for (int i = 0; i < mapData.getNameTextures().length; i++) {
            textureDesc[i] = new AssetDescriptor<>(
                    Gdx.files.getFileHandle(mapData.getTexturePaths()[i],
                            mapData.getType()), Texture.class);
            loader.getAssetManager().load(textureDesc[i]);
        }

        loader.setOnFinish(() -> {
            if (atlasDesc != null) mapData.setAtlas(loader.getAssetManager().get(atlasDesc));
            for (int i = 0; i < textureDesc.length; i++) {
                mapData.getAtlas().addRegion(
                        mapData.getNameTextures()[i],
                        new TextureRegion(loader.getAssetManager().get(textureDesc[i])));
            }

            onFinish.call();
        });
        loader.launchLoad();
    }

    private LoaderMap() {}
}
