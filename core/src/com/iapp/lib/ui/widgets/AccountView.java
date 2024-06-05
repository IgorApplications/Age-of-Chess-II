package com.iapp.lib.ui.widgets;

import com.badlogic.gdx.utils.Disposable;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.web.Account;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.web.Client;

public class AccountView extends RdTable implements Disposable {

    private final AccountViewStyle style;
    private final Account account;
    private final AvatarView avatarView;
    private final Client client;

    public AccountView(AccountViewStyle style, Client client,
                       Account account, OnChangeListener onChangeListener) {
        this.style = style;
        this.account = account;
        this.client = client;
        avatarView = new AvatarView(style.avatarViewStyle);
        avatarView.addListener(onChangeListener);
        init();
    }

    public AccountView(String styleName, Client client,
                       Account account, OnChangeListener onChangeListener) {
        this(RdAssetManager.current().getSkin().get(styleName, AccountViewStyle.class),
            client, account, onChangeListener);
    }

    public AccountView(Client client,
                       Account account, OnChangeListener onChangeListener) {
        this("default", client, account, onChangeListener);
    }

    private void init() {
        add(avatarView);
        add(new RdLabel(account.getFullName(), style.labelStyle)).padLeft(7);

        client.requireAvatar(account,
            bytes -> avatarView.update(account, bytes));
    }

    @Override
    public void dispose() {
        avatarView.dispose();
    }

    public static class AccountViewStyle {
        public AvatarView.AvatarViewStyle avatarViewStyle;
        public RdLabel.RdLabelStyle labelStyle;
    }
}
