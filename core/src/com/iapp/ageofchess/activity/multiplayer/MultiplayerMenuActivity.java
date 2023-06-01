package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerMenuController;
import com.iapp.ageofchess.graphics.MessageView;
import com.iapp.lib.ui.screens.RdLogger;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.LobbyMessage;
import com.iapp.lib.web.Message;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.Pair;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MultiplayerMenuActivity extends Activity {

    private WindowGroup windowGroup;
    private final MultiplayerMenuController controller;
    private RdImageTextButton back, games, createGame, rank;
    private RdTable messagesTable;
    private final Array<MessageView> views = new Array<>();
    private RdTextArea messageInput;
    private Spinner spinner;
    private RdDialog conf;

    public MultiplayerMenuActivity() {
        controller = new MultiplayerMenuController(this);
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    @Override
    public void initActors() {
        var avatarView = ChessConstants.accountPanel.getAvatarView();

        OnChangeListener last = null;
        for (var listener : avatarView.getListeners()) {
            if (listener instanceof OnChangeListener) {
                last = (OnChangeListener) listener;
                break;
            }
        }
        if (last != null) {
            avatarView.getListeners().removeValue(last, true);
        }

        // replace activity
        avatarView.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.accountController.editAccount(ChessConstants.loggingAcc.getId());
            }
        });

        back = new RdImageTextButton(strings.get("back"), "red_screen");
        back.setImage("ib_back");

        games = new RdImageTextButton(strings.get("play"), "yellow_screen");
        games.setImage("ib_play");

        createGame = new RdImageTextButton(strings.get("create_online"), "white_screen");
        createGame.setImage("ib_games");

        rank = new RdImageTextButton(strings.get("rating"), "white_screen");
        rank.setImage("ib_rating");

        messageInput = new RdTextArea("", ChessAssetManager.current().getSkin());
        messageInput.setMessageText(strings.get("enter_hint"));
        messageInput.setMaxLength(300);
    }

    @Override
    public void initListeners() {
        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToMenu();
            }
        });
        ChessApplication.self().getLauncher().setOnFinish(controller::goToMenu);

        messageInput.addListener(new InputListener() {

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (Input.Keys.ENTER == keycode) {
                    var data = messageInput.getText();
                    MultiplayerEngine.self().sendMessage(data);
                    messageInput.setText("");
                }
                return super.keyUp(event, keycode);
            }
        });

        games.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToGames();
            }
        });

        createGame.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToScenarios();
            }
        });

        rank.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToRank();
            }
        });
    }


    @Override
    public void show(Stage stage, Activity last) {
        ChessApplication.self().getLineContent().setVisible(true);
        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        getStage().addActor(background);
        background.setScaling(Scaling.fill);

        var window = new RdWindow("","screen_window");
        window.setMovable(false);
        stage.addActor(window);

        var properties = new PropertyTable(400, ChessAssetManager.current().getSkin());
        window.add(properties).expand().fill();
        properties.setVisibleBackground(false);

        properties.add(new PropertyTable.Title(strings.get("multiplayer")));

        messagesTable = new RdTable("loading");
        messagesTable.align(Align.topLeft);
        var scroll = new RdScrollPane(messagesTable);

        properties.getContent().add(messageInput)
                .pad(10, 10, 10, 10)
                .expandX().fillX().row();
        properties.getContent().add(scroll).pad(5, 5, 5,5).expand().fill();

        windowGroup = new WindowGroup(window, back, games, createGame, rank);
        ChessApplication.self().updateTitle(windowGroup, strings.get("multiplayer"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        messagesTable.getLoading().setVisible(true);

        // listener update messages
        Consumer<Pair<List<Message>, Map<Long, Account>>> onMessage =
                listMapPair -> updateMessages(listMapPair.getKey(), listMapPair.getValue());
        MultiplayerEngine.self().setOnMainChatMessages(onMessage);

        if (last instanceof MultiplayerGameActivity) {
            TransitionEffects.alphaShow(getStage().getRoot(), ChessConstants.localData.getScreenDuration());
        } else {
            TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
        }

        MultiplayerEngine.self().setOnMainLobby(strings -> {
            if (ChessConstants.chatView == null) return;
            try {
                LobbyMessage online = strings.remove(0);
                if (online != null) {
                    ChessConstants.chatView.updateOnline(Integer.parseInt(online.getText()));
                }
            } catch (Throwable t) {
                Gdx.app.error("Server wrong format!", RdLogger.self().getDescription(t));
            }
            ChessConstants.chatView.updateLobbyMessages(strings);
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        dispose(views);

        // remove listener!
        MultiplayerEngine.self().setOnMainChatMessages(null);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if (windowGroup != null) windowGroup.update();
        WindowUtil.resizeCenter(spinner);
    }

    public void updateMessages(List<Message> messages, Map<Long, Account> accounts) {
        dispose(views);
        messagesTable.clear();
        if (messages.size() > 50) {
            messages = messages.subList(0, 50);
        }

        Map<Long, List<MessageView>> idByMessages = new HashMap<>();
        for (var message : messages) {
            var view = new MessageView(message, accounts.get(message.getSenderId()),
                    new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    ChessConstants.accountController.seeAccount(message.getSenderId());
                }
            }, new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {

                   ChessApplication.self().showConf(strings.get("conf_del"),
                       (dialog, s) -> {
                           MultiplayerEngine.self().removeMessage(message.getId());
                           dialog.hide();
                       });

                }
            });

            if (!idByMessages.containsKey(message.getSenderId())) {
                idByMessages.put(message.getSenderId(), new ArrayList<>());
            }
            idByMessages.get(message.getSenderId()).add(view);

            views.add(view);
            messagesTable.add(view)
                    .pad(8, 3, 8, 3).expandX().fillX().row();
        }

        for (Account account : accounts.values()) {

            MultiplayerEngine.self().getAvatar(account, bytes -> {
                List<MessageView> res = idByMessages.get(account.getId());
                if (res == null) return;
                for (MessageView message : res) {
                    message.getAvatarView().update(account, bytes);
                }

            });
        }

        messagesTable.getLoading().setVisible(false);
    }

    private void dispose(Array<MessageView> views) {
        for (var view : views) {
            view.dispose();
        }
        views.clear();
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
        return windowGroup;
    }
}
