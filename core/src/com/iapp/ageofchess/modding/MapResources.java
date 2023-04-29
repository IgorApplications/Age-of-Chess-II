package com.iapp.ageofchess.modding;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.HashMap;
import java.util.Map;

public class MapResources implements Disposable {

    private @Null String atlasDescName;
    private @Null byte[] atlasDesc;
    private @Null String[] atlasPngNames;
    private @Null byte[][] atlasPng;
    private @Null String[] textureNames;
    private @Null byte[][] textures;
    private final ObjectMap<String, ObjectMap<String, String>> strings = new ObjectMap<>();

    public MapResources() {}

    public @Null byte[] getAtlasDesc() {
        return atlasDesc;
    }

    public void setAtlasDesc(byte[] atlasDesc) {
        this.atlasDesc = atlasDesc;
    }

    public @Null byte[][] getAtlasPng() {
        return atlasPng;
    }

    public void setAtlasPng(byte[][] atlasPng) {
        this.atlasPng = atlasPng;
    }

    public @Null byte[][] getTextures() {
        return textures;
    }

    public void setTextures(byte[][] textures) {
        this.textures = textures;
    }

    public ObjectMap<String, ObjectMap<String, String>> getStrings() {
        return strings;
    }

    public @Null String getAtlasDescName() {
        return atlasDescName;
    }

    public void setAtlasDescName(String atlasDescName) {
        this.atlasDescName = atlasDescName;
    }

    public @Null String[] getAtlasPngNames() {
        return atlasPngNames;
    }

    public void setAtlasPngNames(String[] atlasPngNames) {
        this.atlasPngNames = atlasPngNames;
    }

    public @Null String[] getTextureNames() {
        return textureNames;
    }

    public void setTextureNames(String[] textureNames) {
        this.textureNames = textureNames;
    }

    @Override
    public void dispose() {
        atlasDescName = null;
        atlasDesc = null;
        atlasPngNames = null;
        textureNames = null;
        textures = null;
        strings.clear();
        System.gc();
    }
}
