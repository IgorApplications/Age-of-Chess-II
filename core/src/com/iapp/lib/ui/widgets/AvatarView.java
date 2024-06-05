package com.iapp.lib.ui.widgets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.util.LoadAvatarUtil;
import com.iapp.lib.web.Account;
import com.iapp.lib.ui.actors.AnimatedImage;
import com.iapp.lib.util.DisposeUtil;

import java.util.Arrays;

public class AvatarView extends ImageButton implements Disposable {

    // TODO
    private final Drawable loadingBg;
    private byte[] lastByteAvatar;
    private AnimatedImage avatar;
    private Image online;
    private Texture avatarTexture;
    private boolean init;

    public AvatarView(AvatarViewStyle style) {
        super(style);
        loadingBg = style.loadingBg;
        avatar = style.loadingAnim;
    }

    public AvatarView(String skinName) {
        this(RdAssetManager.current().getSkin().get(skinName, AvatarViewStyle.class));
    }

    public AvatarView() {
        this("default");
    }

    public AvatarView(AvatarView avatarView) {
        super(avatarView.getStyle());

        lastByteAvatar = avatarView.lastByteAvatar;
        avatar = avatarView.avatar;
        online = avatarView.online;
        avatarTexture = avatarView.avatarTexture;
        loadingBg = avatarView.loadingBg;
        init = avatarView.init;
    }

    public void update(Account account, byte[] byteAvatar) {
        if (lastByteAvatar != null && Arrays.equals(lastByteAvatar, byteAvatar)) {
            return;
        }

        var disposedTexture = avatarTexture;
        lastByteAvatar = byteAvatar;

        var avatarPair = LoadAvatarUtil.loadAvatar(byteAvatar);
        var avatarDrawable = avatarPair.getKey();
        avatarTexture = avatarPair.getValue();
        avatar = new AnimatedImage(Long.MAX_VALUE, new TextureRegionDrawable(avatarDrawable));
        online = LoadAvatarUtil.getOnline(account);
        init = true;

        DisposeUtil.dispose(disposedTexture);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        if (!init) {
            loadingBg.draw(batch, getX() + getWidth() * 0.1f, getY() +  getHeight() * 0.1f,
                getWidth() * 0.8f, getHeight() * 0.8f);
        }

        float size = Math.min(getWidth(), getHeight());
        float margin = size * 0.109375f;
        size -= margin * 2;
        if (!init) {
            // loading 72x72
            size = 72;
            margin = 28;
        }

        avatar.setBounds(getX() + margin, getY() + margin, size, size);
        avatar.draw(batch, parentAlpha);

        super.draw(batch, parentAlpha);

        if (online != null) {
            online.setPosition(getX() + getWidth() - online.getPrefWidth(),
                    getY() + getHeight() - online.getPrefHeight());
            online.draw(batch, parentAlpha);
        }
    }

    @Override
    public void dispose() {
        DisposeUtil.dispose(avatarTexture);
    }

    public static class AvatarViewStyle extends ImageButtonStyle {
        public AnimatedImage loadingAnim;
        public Drawable loadingBg;
    }
}
