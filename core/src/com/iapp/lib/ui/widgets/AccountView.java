package com.iapp.lib.ui.widgets;

import com.badlogic.gdx.utils.Disposable;
import com.iapp.lib.web.Account;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.web.Client;

public class AccountView extends RdTable implements Disposable {

    private final Account account;
    private final AvatarView avatarView;
    private final Client client;

    public AccountView(Client client, Account account, OnChangeListener onChangeListener) {
        this.account = account;
        this.client = client;
        avatarView = new AvatarView(ChessAssetManager.current().getAvatarStyle());
        avatarView.addListener(onChangeListener);
        init();
    }

    private void init() {
        add(avatarView);
        add(new RdLabel(account.getFullName())).padLeft(7);

        client.getAvatar(account,
            bytes -> avatarView.update(account, bytes));
    }

    @Override
    public void dispose() {
        avatarView.dispose();
    }
}
