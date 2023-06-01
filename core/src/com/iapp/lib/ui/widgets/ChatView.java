package com.iapp.lib.ui.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.SettingsUtil;
import com.iapp.lib.chess_engine.Color;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.RdI18NBundle;
import com.iapp.lib.web.Client;
import com.iapp.lib.web.Lobby;
import com.iapp.lib.web.LobbyMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatView extends RdTable {

    private static final String SERVER_STATE_COLOR = "[#d7d7d7]";
    private static final String COMMAND_STATE_COLOR = "[#ff8c00]";
    private static final String META_STATE_COLOR = "[a8a8a8]";

    private String defColor = "", defGameColor = "";
    /** main and game lobby */
    private long localLobbyId = -1, localGameLobbyId = -1;
    private static final long MIN_ID = -100;

    private final RdI18NBundle strings;
    private final Client client;
    private final List<LobbyMessage> lastLobby = new ArrayList<>();
    private final List<LobbyMessage> lastGames = new ArrayList<>();
    private final float maxWidth;
    private final String textGames, textLobby;

    private RdTable messagesContent;
    private RdTable vertTable;
    private RdScrollPane scrollMessages;
    private boolean hidden = true;
    private RdLabel onlineLabel;
    private ImageButton showLobby;
    private RdTextButton games, lobby;
    private RdTextArea lobbyField;
    private Mode globalMode, messagesMode;
    private long matchId;

    public ChatView(Client client, Mode mode, String textGame, String textLobby, float maxWidth) {
        this.client = client;
        globalMode = mode;
        this.maxWidth = maxWidth;
        this.textGames = textGame;
        this.textLobby = textLobby;
        setFillParent(true);
        align(Align.topRight);
        messagesMode = Mode.LOBBY;
        strings = RdApplication.self().getStrings();

        initGeneral();
        sendMetaMessages(Mode.LOBBY);
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
        sendMetaMessages(Mode.GAMES);
    }

    public void clearMessages() {
        lastLobby.clear();
        lastGames.clear();
    }

    public void updateMode(Mode mode) {
        if (this.globalMode == mode) return;
        this.globalMode = mode;

        if (mode == Mode.LOBBY) {
            messagesMode = Mode.LOBBY;
            initLobby();
            updateMessages(lastLobby);
            lastGames.clear();
            if (hidden) showLobby.setStyle(ChessAssetManager.current().getChatLobbyUp());
            else showLobby.setStyle(ChessAssetManager.current().getChatLobbyDown());
        } else {
            if (mode == Mode.LOBBY_GAMES) {
                messagesMode = Mode.LOBBY;
                initGames();
                updateMessages(lastLobby);
            } else {
                messagesMode = Mode.GAMES;
                games.setChecked(true);
                initGames();
                updateMessages(lastGames);
            }
            showLobby.setStyle(ChessAssetManager.current().getHideChatStyle());
        }
        if (hidden) {
            if (mode == Mode.LOBBY) {
                setX(getX() - maxWidth - 10);
                vertTable.setVisible(false);
            } else {
                setX(maxWidth + 10);
            }
        }
    }

    public void updateOnline(int online) {
        onlineLabel.setText(String.valueOf(online));
    }

    public void updateGameOnline(int online) {
        games.setText(textGames + " [GREEN](" + online + ")[]");
    }

    public void updateLocalGameMessages(String message) {
        lastGames.add(new LobbyMessage(generateLocalGameId(), Lobby.SERVER_USER_ID, message));
        if (messagesMode == Mode.GAMES) updateMessages(lastGames);
    }

    public void updateLocalLobbyMessages(String message) {
        lastLobby.add(new LobbyMessage(generateLocalLobbyId(), Lobby.SERVER_USER_ID, message));
        if (messagesMode == Mode.LOBBY) updateMessages(lastLobby);
    }

    public void updateGameMessages(List<LobbyMessage> messages) {
        updateServerMessages(messages, lastGames);
        if (messagesMode == Mode.GAMES) updateMessages(lastGames);
    }

    public void updateLobbyMessages(List<LobbyMessage> messages) {
        updateServerMessages(messages, lastLobby);
        if (messagesMode == Mode.LOBBY) updateMessages(lastLobby);
    }

    private void updateMessages(List<LobbyMessage> messages) {
        messagesContent.clear();

        for (LobbyMessage message : messages) {
            RdLabel label = new RdLabel(defineState(message));
            label.setWrap(true);

            RdTable table = new RdTable();
            table.align(Align.topLeft);
            table.add(label).expandX().fillX();
            messagesContent.add(table).width(maxWidth).row();
        }

        scrollMessages.layout();
        scrollMessages.scrollTo(0, 0, 0, 0);
    }

    private void initGeneral() {
        ButtonGroup<RdTextButton> group = new ButtonGroup<>();
        showLobby = new ImageButton(ChessAssetManager.current().getHideChatStyle());
        lobbyField = new RdTextArea("", ChessAssetManager.current().getSkin());
        onlineLabel = new RdLabel("");
        onlineLabel.setAlignment(Align.right);

        lobbyField.setMaxLength(300);
        lobbyField.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // mobile virtual keyboard
                RdApplication.self().getLauncher().addOnKeyboard(
                    visible -> {
                        double k = visible ? 1 : 0;
                        RdApplication.self().getStage().getRoot().setY(
                            (float) (RdApplication.self().getLauncher().getKeyboardHeight() * k));
                    }
                );
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (Input.Keys.ENTER == keycode) {

                    String message = lobbyField.getText();
                    if (!sendLocalCommand(message, messagesMode)) {
                        if (messagesMode == Mode.GAMES) {
                            String tint = message.startsWith("/") ? "" : defGameColor;
                            client.sendGameLobbyMessage(matchId, tint + message);
                        } else {
                            String tint = message.startsWith("/") ? "" : defColor;
                            client.sendMainLobbyMessage(tint + message);
                        }
                    }
                    lobbyField.setText("");

                }
                return super.keyUp(event, keycode);
            }
        });

        messagesContent = new RdTable();
        messagesContent.align(Align.topLeft);

        scrollMessages = new RdScrollPane(messagesContent,
            RdAssetManager.current().getSkin(), "sample");

        Mode localMessages = messagesMode;
        games = new RdTextButton(textGames, ChessAssetManager.current().getModeChatStyle());
        games.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                messagesMode = Mode.GAMES;
                updateMessages(lastGames);
            }
        });

        lobby = new RdTextButton(textLobby, ChessAssetManager.current().getModeChatStyle());
        lobby.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                messagesMode = Mode.LOBBY;
                updateMessages(lastLobby);
            }
        });
        group.add(games);
        group.add(lobby);

        // keeping the value from accidentally triggering the listener
        messagesMode = localMessages;
        if (globalMode == Mode.LOBBY) initLobby();
        else initGames();

        if (globalMode == Mode.GAMES) {
            if (hidden) setX(maxWidth + 10);
        } else {
            if (hidden) {
                vertTable.setVisible(false);
                showLobby.setStyle(ChessAssetManager.current().getChatLobbyUp());
            } else {
                showLobby.setStyle(ChessAssetManager.current().getChatLobbyDown());
            }
        }

        showLobby.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {

                if (hidden) {
                    hidden = false;
                    if (globalMode == Mode.LOBBY) {
                        showLobby.setStyle(ChessAssetManager.current().getChatLobbyDown());
                        vertTable.setVisible(true);
                    } else {
                        addAction(Actions.moveBy(-vertTable.getWidth(), 0.0f, 0.2f));
                        showLobby.setStyle(ChessAssetManager.current().getHideChatStyle());
                    }
                } else {
                    hidden = true;
                    if (globalMode == Mode.LOBBY) {
                        showLobby.setStyle(ChessAssetManager.current().getChatLobbyUp());
                        vertTable.setVisible(false);
                    } else {
                        addAction(Actions.moveBy(vertTable.getWidth(), 0.0f, 0.2f));
                        showLobby.setStyle(ChessAssetManager.current().getHideChatStyle());
                    }
                }

            }
        });
    }

    private void initLobby() {
        clear();

        onlineLabel.setRotation(0);
        vertTable = new RdTable();
        vertTable.pad(15, 15, 0, 0);
        vertTable.add(scrollMessages).width(maxWidth).expandY().fillY().colspan(2).row();
        vertTable.setBackground(new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findChessRegion("chat_bg"),
                10,10,10,10)));
        showLobby.getImage().setScaling(Scaling.fill);

        RdTable showTable = new RdTable();
        showTable.pad(0);
        showTable.add(onlineLabel).expandY().bottom().padRight(10);
        showTable.add(showLobby).maxHeight(90).padBottom(10).minWidth(120).expandY().bottom();

        RdTable column1 = new RdTable();
        RdTable column2 = new RdTable();
        ChessConstants.accountPanel.getAvatarView().align(Align.topRight);
        ChessConstants.accountPanel.getControls().align(Align.topLeft);
        column2.align(Align.topLeft);

        column1.add(ChessConstants.accountPanel.getAvatarView()).expand().right().top().row();
        column1.add(showTable).expandY().bottom().maxHeight(90).pad(0);

        column2.add(ChessConstants.accountPanel.getControls()).expandX().left().fillX().row();
        column2.add(vertTable).expandY().fillY().row();
        column2.add(lobbyField).fillX().bottom().prefHeight(90).pad(0);

        add(column1).expandY().fill();
        add(column2).expandY().fill();
    }

    private void initGames() {
        clear();

        onlineLabel.setRotation(90);
        RdTable buttonTable = new RdTable();
        buttonTable.add(games).expandX().padRight(3).fillX();
        buttonTable.add(lobby).expandX().padLeft(3).fillX();

        vertTable = new RdTable();
        vertTable.align(Align.topLeft);
        vertTable.setBackground(new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findChessRegion("chat_bg"),
                10,10,10,10)));
        vertTable.pad(0, 15, 15, 0);
        ChessConstants.accountPanel.getControls().align(Align.topLeft);

        vertTable.add(ChessConstants.accountPanel.getControls()).fill().top().left().row();
        if (globalMode != Mode.LOBBY_GAMES) vertTable.add(buttonTable).width(maxWidth).expandX().fillX().row();
        vertTable.add(scrollMessages).width(maxWidth).expandY().fillY().row();
        vertTable.add(lobbyField).width(maxWidth).row();

        RdTable column1 = new RdTable();
        column1.add(ChessConstants.accountPanel.getAvatarView()).size(128, 128).row();
        column1.add(showLobby).padTop(80).right().row();
        column1.add(onlineLabel).expandX().right().width(-35);

        add(column1).expandY().top();
        add(vertTable).expandY().fillY();
    }

    private void updateServerMessages(List<LobbyMessage> serverMessages, List<LobbyMessage> lastLocal) {
        int count = 0;
        for (LobbyMessage message : serverMessages) {
            if (!lastLocal.contains(message)) {
                count++;
            }
        }
        lastLocal.addAll(serverMessages.subList(serverMessages.size() - count, serverMessages.size()));
    }

    public enum Mode {
        LOBBY, GAMES, LOBBY_GAMES
    }

    private Color getColorEn(String en) {
        if (en.contains("black")) return Color.BLACK;
        return Color.WHITE;
    }

    private String defineState(LobbyMessage lobbyMessage) {
        String message = lobbyMessage.getText();
        String[] tokens = message.split(" ");

        if (message.startsWith("join")) {

            // check random
            if (tokens.length < 3) {
                return SERVER_STATE_COLOR  + strings.format("join_rand_state", tokens[1]);
            }
            return SERVER_STATE_COLOR + strings.format("join_state", tokens[1],
                SettingsUtil.defineColor(getColorEn(tokens[2])));

        } else if (message.startsWith("disjoin")) {
            return SERVER_STATE_COLOR + strings.format("disjoin_state", tokens[1]);
        } else if (message.startsWith("start")) {
            return SERVER_STATE_COLOR + strings.format("start_state", tokens[1]);
        } else if (message.startsWith("connect")) {
            return SERVER_STATE_COLOR + strings.format("connect_state", tokens[1]);
        } else if (message.startsWith("disconnect")) {
            return SERVER_STATE_COLOR + strings.format("disconnect_state", tokens[1]);
        } else if (message.startsWith("error")) {
            return SERVER_STATE_COLOR + strings.get("lobby_error");
        } else if (message.startsWith("restored")) {
            return SERVER_STATE_COLOR + strings.get("connection_restored");
        } else if (message.startsWith("unknown")) {
            return COMMAND_STATE_COLOR + strings.get("unknown_command");
        } else if (message.startsWith("banned")) {
            return COMMAND_STATE_COLOR + strings.format("banned_command",
                tokens[1], tokens[2], getTime(Long.parseLong(tokens[3])));
        } else if (message.startsWith("denied")) {
            return COMMAND_STATE_COLOR + strings.get("denied_command");
        } else if (message.startsWith("self_banned")) {
            return COMMAND_STATE_COLOR + strings.get("self_banned");
        } else if (message.startsWith("wrong")) {
            return COMMAND_STATE_COLOR + strings.get("wrong_command");
        }

        return message;
    }

    private boolean sendLocalCommand(String message, Mode messagesMode) {
        RdI18NBundle strings = RdApplication.self().getStrings();

        String result = null;
        String[] tokens = message.split(" ");
        if (message.equals("/h") || message.equals("/help")) {
            result =  SERVER_STATE_COLOR + strings.get("help_command");
        } else if (message.equals("/tintreset")) {
            if (messagesMode == Mode.GAMES) defGameColor = "";
            else defColor = "";
            result = SERVER_STATE_COLOR + strings.get("tint_command");
        } else if (message.startsWith("/tint")) {
            if (tokens.length != 2) {
                result = SERVER_STATE_COLOR + strings.get("wrong_command");
            } else {
                if (messagesMode == Mode.LOBBY) defColor = "[" + tokens[1] + "]";
                else defGameColor = "[" + tokens[1] + "]";
                result = SERVER_STATE_COLOR + strings.get("tintreset_command");
            }
        } else if (message.equals("/list")) {
            if (messagesMode == Mode.LOBBY) {
                client.requireMainLobbyList(lobbyMessages ->
                    updateLocalLobbyMessages(getListCommand(lobbyMessages)));
            }
            else {
                client.requireGameLobbyList(matchId, lobbyMessages ->
                    updateLocalGameMessages(getListCommand(lobbyMessages)));
            }
            return true;
        }

        if (result != null) {
            if (messagesMode == Mode.LOBBY) {
                updateLocalLobbyMessages(result);
            } else {
                updateLocalGameMessages(result);
            }
            return true;
        }
        return false;
    }

    private String getTime(long millis) {
        RdI18NBundle strings = RdApplication.self().getStrings();

        long minutes = millis / 1000 / 60 % 60;
        long hours = millis / 1000 / 60 / 60;
        if (hours > 0) return strings.format("hours", hours);
        return strings.format("minutes", minutes);
    }

    private long generateLocalLobbyId() {
        if (localLobbyId == MIN_ID) localLobbyId = -1;
        return localLobbyId--;
    }

    private long generateLocalGameId() {
        if (localGameLobbyId == MIN_ID) localGameLobbyId = -1;
        return localGameLobbyId--;
    }

    private String getListCommand(List<LobbyMessage> lobbyMessages) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lobbyMessages.size() - 1; i++) {
            builder.append(lobbyMessages.get(i).getText());
            builder.append(", ");
        }
        if (!lobbyMessages.isEmpty()) {
            builder.append(lobbyMessages.get(lobbyMessages.size() - 1).getText());
        }
        return SERVER_STATE_COLOR + strings.format("list_command", builder.toString());
    }

    private void sendMetaMessages(Mode mode) {
        if (mode == Mode.LOBBY) {
            updateLocalLobbyMessages(META_STATE_COLOR + strings.get("hint_help"));
        } else {
            updateLocalGameMessages(META_STATE_COLOR + strings.get("hint_help"));
        }
    }
}
