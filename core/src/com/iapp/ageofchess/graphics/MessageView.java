package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.iapp.ageofchess.ChessApplication;
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

public class MessageView extends Table implements Disposable {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy hh:mm",
            ChessConstants.localData.getLocale());
    private final AvatarView avatarView;

    public MessageView(Message message, Account account, OnChangeListener onAvatar,
                       OnChangeListener onDelete) {
        setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                        10,10,10,10)));
        align(Align.topLeft);

        avatarView = new AvatarView(ChessAssetManager.current().getAvatarStyle());

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
}
