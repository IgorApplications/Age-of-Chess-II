package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.ui.widgets.AvatarView;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.AccountType;
import com.iapp.lib.web.Message;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.DisposeUtil;
import com.iapp.lib.util.OnChangeListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageView extends Table implements Disposable {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy HH:mm",
        new Locale(ChessConstants.localData.getLangCode()));
    private final AvatarView avatarView;

    public MessageView(MessageViewStyle style, Message message, Account account,
                       OnChangeListener onAvatar, OnChangeListener onDelete) {
        setBackground(style.background);
        align(Align.topLeft);

        avatarView = new AvatarView(style.avatarViewStyle);
        avatarView.align(Align.topLeft);
        avatarView.addListener(onAvatar);

        var table2 = new RdTable();
        table2.align(Align.topLeft);
        table2.add(getTitleTable(account, message, onDelete)).expandX().fillX().row();
        var text = new RdLabel(message.getText());
        text.setWrap(true);

        table2.add(text).expandX().fillX().left();

        add(avatarView).align(Align.topLeft).padRight(12);
        add(table2).expand().fill();
    }

    public MessageView(String styleName, Message message, Account account,
                       OnChangeListener onAvatar, OnChangeListener onDelete) {
        this(RdAssetManager.current().getSkin().get(styleName, MessageViewStyle.class),
            message, account, onAvatar, onDelete);
    }

    public MessageView(Message message, Account account,
                       OnChangeListener onAvatar, OnChangeListener onDelete) {
        this("default", message, account, onAvatar, onDelete);
    }

    public AvatarView getAvatarView() {
        return avatarView;
    }

    private RdTable getTitleTable(Account account, Message message, OnChangeListener onDelete) {
        var name = new RdLabel(account.getFullName());
        name.setWrap(true);
        name.setColor(defineColor(account));

        var time = new RdLabel("[%75]" + formatter.format(new Date(message.getTime())));
        time.setAlignment(Align.right);

        var del = new ImageButton(ChessAssetManager.current().getDeleteStyle());
        del.addListener(onDelete);

        var table = new RdTable();
        table.align(Align.topLeft);
        table.add(name);

        if (ChessConstants.loggingAcc.getId() == message.getSenderId()
                || ChessConstants.loggingAcc.getType().ordinal() >= AccountType.MODERATOR.ordinal()) {
            table.add(del).expandX().size(48, 48).right();
            table.add(time);
        } else {
            table.add(time).expandX().right();
        }

        return table;
    }

    private Color defineColor(Account account) {
        switch (account.getType()) {
            case DEVELOPER: return Color.valueOf("80A6FF");
            case MODERATOR:
            case EXECUTOR: return Color.RED;
        }
        return Color.WHITE;
    }

    @Override
    public void dispose() {
        DisposeUtil.dispose(avatarView);
    }

    public static class MessageViewStyle {
        public AvatarView.AvatarViewStyle avatarViewStyle;
        public Drawable background;

    }
}
