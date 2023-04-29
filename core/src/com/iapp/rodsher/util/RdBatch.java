package com.iapp.rodsher.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;

public class RdBatch implements Batch {

    private final Batch batch;

    public RdBatch(Batch batch) {
        this.batch = batch;
    }

    @Override
    public void begin() {
        batch.begin();
    }

    @Override
    public void end() {
        batch.end();
    }

    @Override
    public void setColor(Color tint) {
        batch.setColor(tint);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        batch.setColor(r, g, b, a);
    }

    @Override
    public Color getColor() {
        return batch.getColor();
    }

    @Override
    public void setPackedColor(float packedColor) {
        batch.setPackedColor(packedColor);
    }

    @Override
    public float getPackedColor() {
        return batch.getPackedColor();
    }

    @Override
    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        batch.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        batch.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
    }

    @Override
    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        batch.draw(texture, x, y, srcX, srcY, srcWidth, srcHeight);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        batch.draw(texture, x, y, width, height, u, v, u2, v2);
    }

    @Override
    public void draw(Texture texture, float x, float y) {
        batch.draw(texture, x, y);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height) {

    }

    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {

    }

    @Override
    public void draw(TextureRegion region, float x, float y) {

    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {

    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {

    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, boolean clockwise) {

    }

    @Override
    public void draw(TextureRegion region, float width, float height, Affine2 transform) {

    }

    @Override
    public void flush() {

    }

    @Override
    public void disableBlending() {

    }

    @Override
    public void enableBlending() {

    }

    @Override
    public void setBlendFunction(int srcFunc, int dstFunc) {

    }

    @Override
    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {

    }

    @Override
    public int getBlendSrcFunc() {
        return 0;
    }

    @Override
    public int getBlendDstFunc() {
        return 0;
    }

    @Override
    public int getBlendSrcFuncAlpha() {
        return 0;
    }

    @Override
    public int getBlendDstFuncAlpha() {
        return 0;
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return null;
    }

    @Override
    public Matrix4 getTransformMatrix() {
        return null;
    }

    @Override
    public void setProjectionMatrix(Matrix4 projection) {

    }

    @Override
    public void setTransformMatrix(Matrix4 transform) {

    }

    @Override
    public void setShader(ShaderProgram shader) {

    }

    @Override
    public ShaderProgram getShader() {
        return batch.getShader();
    }

    @Override
    public boolean isBlendingEnabled() {
        return batch.isBlendingEnabled();
    }

    @Override
    public boolean isDrawing() {
        return batch.isDrawing();
    }

    @Override
    public void dispose() {

    }
}
