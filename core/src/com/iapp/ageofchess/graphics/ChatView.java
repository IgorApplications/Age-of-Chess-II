package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.SettingsUtil;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.actors.RdScrollPane;
import com.iapp.rodsher.actors.RdTable;
import com.iapp.rodsher.actors.RdTextField;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.RdAssetManager;
import com.iapp.rodsher.util.OnChangeListener;

import java.util.List;
import java.util.function.Consumer;

public class ChatView extends RdTable {

    private RdTable messagesContent;
    private RdTable titleContent;
    private RdScrollPane scrollMessages;
    private final float maxWidth;
    private boolean hidden;

    public ChatView(float maxWidth, Consumer<String> onSend) {
        this.maxWidth = maxWidth;
        setFillParent(true);
        align(Align.topRight);

        initialize(onSend);
    }

    public void updateMessages(List<String> defineMessages, String... titleMessages) {
        messagesContent.clear();
        titleContent.clear();

        for (var el : titleMessages) {
            var label = new RdLabel(el);
            label.setWrap(true);

            var table = new RdTable();
            table.align(Align.topLeft);
            table.add(label).expandX().fillX();
            titleContent.add(table).width(maxWidth).row();
        }

        for (var message : defineMessages) {

            var label = new RdLabel(defineState(message));
            label.setWrap(true);

            var table = new RdTable();
            table.align(Align.topLeft);
            table.add(label).expandX().fillX();
            messagesContent.add(table).width(maxWidth).row();
        }

        scrollMessages.layout();
        scrollMessages.scrollTo(0, 0, 0, 0);
    }

    private String defineState(String message) {
        var strings = RdApplication.self().getStrings();

        var tokens = message.split(" ");
        if (message.startsWith("enter")) {
            return "[#d7d7d7]# " + strings.format("enter_state", tokens[1]);
        } else if (message.startsWith("exit")) {
            return "[#d7d7d7]# " + strings.format("exit_state", tokens[1]);
        } else if (message.startsWith("join")) {

            // check random
            if (tokens.length < 3) {
                return "[#d7d7d7]# " + strings.format("join_rand_state", tokens[1]);
            }
            return "[#d7d7d7]# " + strings.format("join_state", tokens[1],
                    SettingsUtil.defineColor(getColorEn(tokens[2])));

        } else if (message.startsWith("disjoin")) {
            return "[#d7d7d7]# " + strings.format("disjoin_state", tokens[1]);
        } else if (message.startsWith("start")) {
            return "[#d7d7d7]# " + strings.format("start_state", tokens[1]);
        }
        return message;
    }

    private void initialize(Consumer<String> onSend) {

        var showLobby = new ImageButton(ChessAssetManager.current().getHideChatStyle());
        var lobbyField = new RdTextField("");
        lobbyField.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (Input.Keys.ENTER == keycode) {
                    RdApplication.postRunnable(() -> {
                        onSend.accept(lobbyField.getText());
                        lobbyField.setText("");
                    });
                }
                return super.keyUp(event, keycode);
            }
        });

        messagesContent = new RdTable();
        messagesContent.align(Align.topLeft);

        scrollMessages = new RdScrollPane(messagesContent,
                RdAssetManager.current().getSkin(), "sample");
        scrollMessages.setFadeScrollBars(false);
        scrollMessages.setOverscroll(false, false);
        scrollMessages.setScrollingDisabled(true, false);
        scrollMessages.setForceScroll(false, false);

        titleContent = new RdTable();

        var vertTable = new RdTable();
        vertTable.add(titleContent).row();
        vertTable.add(scrollMessages).width(maxWidth).expandY().fillY().row();
        vertTable.add(lobbyField).width(maxWidth).row();
        vertTable.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findChessRegion("chat_bg"),
                        5,5,5,5)));

        add(ChessApplication.self().getAccountPanel().getAvatarView()).size(96, 96);
        add(ChessApplication.self().getAccountPanel().getControls()).left();
        row();
        add(showLobby).right();
        add(vertTable).expandY().fillY();

        if (hidden) addAction(Actions.moveBy(vertTable.getWidth(), 0.0f, 0.2f));
        showLobby.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {

                if (hidden) {
                    hidden = false;
                    addAction(Actions.moveBy(-vertTable.getWidth(), 0.0f, 0.2f));
                } else {
                    hidden = true;
                    addAction(Actions.moveBy(vertTable.getWidth(), 0.0f, 0.2f));
                }

            }
        });

    }

    private com.iapp.ageofchess.chess_engine.Color getColorEn(String en) {
        if (en.contains("black")) return com.iapp.ageofchess.chess_engine.Color.BLACK;
        return com.iapp.ageofchess.chess_engine.Color.WHITE;
    }
}
