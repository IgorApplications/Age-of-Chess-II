package com.iapp.rodsher.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

/**
 * Abstract resource loader.
 * If you do not need ready-made resources, then extend this class.
 * Otherwise, extend GrayAssetManager or similar.
 * @author Igor Ivanov
 * @version 1.0
 * */
public abstract class RdAssetManager implements Disposable {

    /** resource loader */
    private final AssetManager assetManager;
    /** true if resources are ready to be used */
    private boolean initialized;

    /** returns a single reference to the application's resource manager */
    public static RdAssetManager current() {
        return RdApplication.self().getAssetManager();
    }

    public RdAssetManager() {
        assetManager = new AssetManager();
    }

    /** returns true if resources are ready to be used */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * load part of the resources,
     * if the resources are loaded then initialization
     * */
    public void update() {
        assetManager.update();
        if (assetManager.isFinished() && !initialized) {
            initialized = true;
            initialize(assetManager);
        }
    }

    /**
     * load part of the resources within milliseconds,
     * if the resources are loaded then initialization
     * @param millis - time allotted for loading
     * */
    public void update(int millis) {
        assetManager.update(millis);
        if (assetManager.isFinished() && !initialized) {
            initialized = true;
            initialize(assetManager);
        }
    }

    /** Load resources to the end and initialise */
    public void finishLoading() {
        assetManager.finishLoading();
        if (assetManager.isFinished() && !initialized) {
            initialized = true;
            initialize(assetManager);
        }
    }

    /** Ready styles for actors! Override this method if you need. */
    public Skin getSkin() {
        return null;
    }

    /** returns true if resource loading is complete */
    public boolean isFinished() {
        return assetManager.isFinished();
    }

    /**
     * loading additional resources in AssetManager, called automatically
     * @param assetManager - resource loader
     * */
    protected void load(AssetManager assetManager) {}

    /**
     * obtaining resources from AssetManager and using, called automatically
     * @param assetManager - resource loader
     * */
    protected void initialize(AssetManager assetManager) {}

    /** Returns atlas texture override this method if you need */
    protected TextureAtlas getAtlas() {
        return null;
    }

    /**
     *  returns atlas region by name
     * @param name - atlas region name
     * @throws IllegalArgumentException - if atlas == null
     * @throws IllegalStateException - if the region is not found
     * */
    public TextureAtlas.AtlasRegion findRegion(String name) {
        if (getAtlas() == null) throw new IllegalStateException("Atlas == null");
        var region = getAtlas().findRegion(name);
        if (region == null) throw new IllegalArgumentException("Region can't' be null! Title of region = " + name);
        return region;
    }

    /**
     *  returns atlas region by name and index
     * @param name - atlas region name
     * @param index - atlas region index
     * @throws IllegalArgumentException - if atlas == null
     * @throws IllegalStateException - if the region is not found
     * */
    public TextureAtlas.AtlasRegion findRegion(String name, int index) {
        if (getAtlas() == null) throw new IllegalStateException("Atlas == null");
        var region = getAtlas().findRegion(name, index);
        if (region == null) throw new IllegalArgumentException("Region can't' be null! Title of region = " + name);
        return region;
    }

    void load() {
        load(assetManager);
    }
}
