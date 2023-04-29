package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.iapp.ageofchess.multiplayer.Account;
import com.iapp.ageofchess.util.LoadAvatarUtil;
import com.iapp.rodsher.util.DisposeUtil;

import java.util.Arrays;

public class AvatarView extends ImageButton implements Disposable {

    private Account lastAccount;
    private Drawable avatar;
    private Image online;
    private Texture avatarTexture;
    private float size;

    public AvatarView(ImageButtonStyle style) {
        super(style);
    }

    public AvatarView(AvatarView avatarView) {
        super(avatarView.getStyle());
        lastAccount = avatarView.lastAccount;
        avatar = avatarView.avatar;
        online = avatarView.online;
        avatarTexture = avatarView.avatarTexture;
        size = avatarView.size;
    }

    public void update(Account account, float size) {
        if (lastAccount != null && Arrays.equals(lastAccount.getAvatar(), account.getAvatar())) {
            return;
        }

        var lastTexture = avatarTexture;
        lastAccount = account;

        var avatarPair = LoadAvatarUtil.loadAvatar(account);
        var avatarDrawable = avatarPair.getKey();
        avatarTexture = avatarPair.getValue();
        avatar = new TextureRegionDrawable(avatarDrawable);
        this.size = size;
        online = LoadAvatarUtil.getOnline(account);

        DisposeUtil.dispose(lastTexture);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (avatar != null) {
            avatar.draw(batch, getX() + (getWidth() - size) / 2,
                    getY() + (getHeight() - size) / 2,
                    size, size);
        }

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
}
