package com.iapp.ageofchess.multiplayer;

import com.badlogic.gdx.Gdx;
import com.github.czyzby.websocket.CommonWebSockets;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iapp.lib.chess_engine.Color;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.lib.web.*;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdLogger;
import com.iapp.lib.util.CallListener;
import com.iapp.lib.util.Pair;
import com.iapp.lib.util.SystemValidator;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Web socket multiplayer controller based
 * on requests and update listeners
 * @author Igor Ivanov
 * <p>
 * the binary protocol works in the following format:
 * send:
 * 0 (update avatar): [0] - request, [1] - id size, [2:n] - id, [n+1:] - avatar
 * 1 (get avatar): [0] - request, [1] - id size, [2:] - id
 * update server:
 * 0 (result update avatar): [0] - request, [1] - RequestStatus ordinal
 * 1 (result get avatar): [0] - request, [1] - RequestStatus ordinal, [2] - id size, [2:n] - id, [n+1:] - avatar
 *
 * */
public class MultiplayerEngine implements Client {

    private WebSocket socket;

    private final Gson gson;
    private final AtomicBoolean initEngine = new AtomicBoolean(false);

    /** punish callbacks */
    private volatile Consumer<String> onPunishError;
    private volatile Consumer<String> onInactivePunishError;

    /** returns a list of users who are online or in a match */
    private volatile Consumer<List<LobbyMessage>> onMainLobbyList;
    private volatile Consumer<List<LobbyMessage>> onGameLobbyList;

    /** returns the status of an attempt to connect to the server */
    private volatile BiConsumer<Boolean, String> onTryConnect;

    /** login listeners */
    private volatile Consumer<Account> loginAccount;
    private volatile Consumer<String> loginError;
    private final AtomicLong loginTime = new AtomicLong(-1);

    /** signup listeners */
    private volatile CallListener onSignup;
    private volatile Consumer<String> signupError;

    /** get account listener */
    private final Map<Long, Consumer<Account>> getAccounts = RdApplication.self().getLauncher().concurrentHashMap();

    /** account search listeners */
    private volatile Consumer<List<Account>> onSearch;

    /** change account listener */
    private volatile Consumer<RequestStatus> onChangeAccount;

    /** rank listeners */
    private volatile Consumer<List<Account>> onBullet;
    private volatile Consumer<List<Account>> onBlitz;
    private volatile Consumer<List<Account>> onRapid;
    private volatile Consumer<List<Account>> onLong;

    /** listener on update main chat messages */
    private volatile Pair<List<Message>, Map<Long, Account>> lastMessages;
    private volatile Consumer<Pair<List<Message>, Map<Long, Account>>> mainChatMessages;

    /** listener main lobby */
    private volatile Consumer<List<LobbyMessage>> onMainLobby;
    private volatile List<LobbyMessage> lastMainLobby;

    /** listener on update games */
    private final List<Consumer<List<Match>>> listOnMatches = RdApplication.self().getLauncher().copyOnWriteArrayList();
    private volatile List<Match> lastMatches;

    /** avatar listeners */
    private final Map<Long, Pair<Long, byte[]>> accountCache = RdApplication.self().getLauncher().concurrentHashMap();
    private final Map<Long, List<Consumer<byte[]>>> onAvatars = RdApplication.self().getLauncher().concurrentHashMap();
    private volatile Consumer<RequestStatus> onChangeAvatar;

    /** enter & remove match listeners */
    private volatile CallListener onSuccessEnter;
    private volatile Consumer<String> onErrorEnter;
    private volatile Consumer<String> onErrorRemoveMatch;

    /** current entered match */
    private volatile Consumer<Match> createdMatch;
    private volatile Consumer<Match> onUpdateMatch;
    private volatile Consumer<String> createdError;
    private volatile long matchId;
    private volatile Match lastMatch;

    private static final MultiplayerEngine INSTANCE = new MultiplayerEngine();

    public static MultiplayerEngine self() {
        return INSTANCE;
    }

    private MultiplayerEngine() {
        gson = new Gson();
    }

    // server requests -------------------------------------------------------------------------------------------------

    public void restartServer() {
        // only developers
        socket.send(new SocketRequest("/api/v1/server/restart"));
    }

    public void tryConnect(BiConsumer<Boolean, String> onTryConnect) {
        if (socket.isOpen()) {
            onTryConnect.accept(true, "");
        } else {
            this.onTryConnect = onTryConnect;
        }
    }

    // account requests -----------------------------------------------------------------------------------------------

    /** updates online and gets the user */
    public void login(String name, String password, Consumer<Account> loginAccount,
                      Consumer<String> loginError) {
        if (!socket.isOpen()) loginError.accept("socket is not open");

        this.loginAccount = loginAccount;
        this.loginError = loginError;
        loginTime.set(RdApplication.self().getLauncher().currentMillis());

        var login = new Login("",
            ChessConstants.localData.getLocale().getLanguage(),
            Locale.getDefault().getLanguage(),
            Locale.getDefault().getCountry(),
            SystemValidator.getOperationSystem().replaceAll("\\s", ""));

        socket.send(new SocketRequest("/api/v1/accounts/login",
            name, password, gson.toJson(login)));
    }

    /** creates a new user account on the server */
    public void signup(String name, String userName, String password,
                       CallListener onSignup, Consumer<String> signupError) {

        this.onSignup = onSignup;
        this.signupError = signupError;
        socket.send(new SocketRequest("/api/v1/accounts/signup", name, userName, password));
    }

    public void changeAccount(Account updated, Consumer<RequestStatus> onResult) {
        onChangeAccount = onResult;
        socket.send(new SocketRequest("/api/v1/accounts/change", gson.toJson(updated)));
    }

    public void changeAvatar(long accountId, byte[] avatar, Consumer<RequestStatus> onChangeAvatar) {
        this.onChangeAvatar = onChangeAvatar;
        socket.send(BinaryRequests.updateAvatar((byte) 0, accountId, avatar));
    }

    public void getAccount(long id, Consumer<Account> getAccount) {
        getAccounts.put(id, getAccount);
        socket.send(new SocketRequest("/api/v1/accounts/see", String.valueOf(id)));
    }

    public void getAvatar(Account account, Consumer<byte[]> getAvatar) {
        putOnAvatar(account.getId(), getAvatar);
        socket.send(BinaryRequests.getAvatar((byte) 1, account.getId()));
    }

    private byte[] getCacheAvatar(long id) {
        var pair = accountCache.get(id);
        if (pair == null || RdApplication.self().getLauncher().currentMillis() - pair.getKey() > 3 * 60 * 1000) return null;
        return pair.getValue();
    }

    private void putOnAvatar(long accountId, Consumer<byte[]> getAvatar) {
        List<Consumer<byte[]>> list = onAvatars.get(accountId);
        if (list == null) onAvatars.put(accountId, new CopyOnWriteArrayList<>());
        onAvatars.get(accountId).add(getAvatar);
    }

    public void searchAccounts(String partName, Consumer<List<Account>> onSearch) {
        this.onSearch = onSearch;
        socket.send(new SocketRequest("/api/v1/accounts/search", partName));
    }

    public void getBulletTop(Consumer<List<Account>> onBullet) {
        this.onBullet = onBullet;
        socket.send(new SocketRequest("/api/v1/accounts/bullet"));
    }

    public void getBlitzTop(Consumer<List<Account>> onBlitz) {
        this.onBlitz = onBlitz;
        socket.send(new SocketRequest("/api/v1/accounts/blitz"));
    }

    public void getRapidTop(Consumer<List<Account>> onRapid) {
        this.onRapid = onRapid;
        socket.send(new SocketRequest("/api/v1/accounts/rapid"));
    }

    public void getLongTop(Consumer<List<Account>> onLong) {
        this.onLong = onLong;
        socket.send(new SocketRequest("/api/v1/accounts/long"));
    }

    public void punish(long punishableId, Punishment punishment, Consumer<String> onPunishError) {
        this.onPunishError = onPunishError;
        socket.send(new SocketRequest("/api/v1/accounts/punish",
            String.valueOf(punishableId), gson.toJson(punishment)));
    }

    public void makeInactivePunish(long punishableId, long punishId, Consumer<String> onInactivePunishError) {
        this.onInactivePunishError = onInactivePunishError;
        socket.send(new SocketRequest("/api/v1/accounts/makeInactive",
            String.valueOf(punishableId), String.valueOf(punishId)));
    }

    // main chat ------------------------------------------------------------------------------------------------------

    @Override
    public void requireMainLobbyList(Consumer<List<LobbyMessage>> onMainLobbyList) {
        this.onMainLobbyList = onMainLobbyList;
        socket.send(new SocketRequest("/api/v1/mainChat/list"));
    }

    @Override
    public void sendMainLobbyMessage(String message) {
        socket.send(new SocketRequest("/api/v1/mainChat/sendLobby", message));
    }

    public void setOnMainLobby(Consumer<List<LobbyMessage>> onMainLobby) {
        this.onMainLobby = onMainLobby;
        if (onMainLobby == null) return;
        if (lastMainLobby == null) {
            socket.send(new SocketRequest("/api/v1/mainChat/readLobby"));
        } else {
            onMainLobby.accept(new ArrayList<>(lastMainLobby));
        }
    }

    public void setOnMainChatMessages(Consumer<Pair<List<Message>, Map<Long, Account>>> mainChatMessages) {
        this.mainChatMessages = mainChatMessages;

        if (mainChatMessages == null) return;
        if (lastMessages != null) {
            mainChatMessages.accept(lastMessages);
        } else {
            socket.send(new SocketRequest("/api/v1/mainChat/readAll"));
        }
    }

    public void sendMessage(String text) {
        socket.send(new SocketRequest("/api/v1/mainChat/send", text));
    }

    @SuppressWarnings("DefaultLocale")
    public void removeMessage(long messageId) {
        socket.send(new SocketRequest("/api/v1/mainChat/remove", String.valueOf(messageId)));
    }

    // match requests -------------------------------------------------------------------------------------------------
    @Override
    public void requireGameLobbyList(long matchId, Consumer<List<LobbyMessage>> onGameLobbyList) {
        this.onGameLobbyList = onGameLobbyList;
        socket.send(new SocketRequest("/api/v1/games/list", String.valueOf(matchId)));
    }

    public void addOnMatches(Consumer<List<Match>> onMatches) {
        listOnMatches.add(onMatches);

        if (lastMatches != null) {
            onMatches.accept(lastMatches);
        } else {
            socket.send(new SocketRequest("/api/v1/games/getGames"));
        }
    }

    public void removeOnMatches(Consumer<List<Match>> onMatches) {
        listOnMatches.remove(onMatches);
    }

    public void removeMatch(long matchId, Consumer<String> onErrorRemoveMatch) {
        if (!socket.isOpen()) {
            onErrorRemoveMatch.accept("no connection");
            return;
        }
        this.onErrorRemoveMatch = onErrorRemoveMatch;
        socket.send(new SocketRequest("/api/v1/games/removeMatch", String.valueOf(matchId)));
    }

    public void createMatch(LocalMatch localMatch, Consumer<Match> onMatch,
                            Consumer<String> onError) {
        createdMatch = onMatch;
        createdError = onError;

        var match = new Match(-1,
            localMatch.getName(),
            localMatch.getSponsored(),
            localMatch.getRankType(),
            -1,
            -1,
            -1,
            localMatch.getTimeByGame(),
            localMatch.getTimeByGame(),
            localMatch.getTimeByTurn(),
            localMatch.getTurnMode(),
            localMatch.getMaxMoves(),
            localMatch.isRandomColor(),
            localMatch.getMatchData().getScenarios()[localMatch.getNumberScenario()]);

        socket.send(new SocketRequest("/api/v1/games/create", gson.toJson(match)));
    }

    public void enterMatch(long matchId, CallListener done, Consumer<String> error) {
        if (!socket.isOpen()) {
            error.accept("no connection");
            return;
        }

        onSuccessEnter = done;
        onErrorEnter = error;
        socket.send(new SocketRequest("/api/v1/games/enter", String.valueOf(matchId)));
    }

    public void exitMatch(long matchId) {
        socket.send(new SocketRequest("/api/v1/games/exit", String.valueOf(matchId)));
    }

    public void makeMove(long matchId, String fen) {
        socket.send(new SocketRequest("/api/v1/games/makeMove", String.valueOf(matchId), fen));
    }

    public void join(long matchId, Color color) {
        socket.send(new SocketRequest("/api/v1/games/join", String.valueOf(matchId), color.toString()));
    }

    public void disjoin(long matchId) {
        socket.send(new SocketRequest("/api/v1/games/disjoin", String.valueOf(matchId)));
    }

    public void setOnUpdateMatch(long matchId, Consumer<Match> onUpdate) {
        this.matchId = matchId;
        onUpdateMatch = onUpdate;
    }

    public void setOnUpdateMatch(long matchId, Consumer<Match> onUpdate, boolean realtime) {
        this.matchId = matchId;
        onUpdateMatch = onUpdate;
        if (realtime && onUpdateMatch != null && lastMatch != null
            && (this.matchId == matchId || this.matchId == -1)) {
            onUpdateMatch.accept(lastMatch);
        }
    }

    public void start(long matchId) {
        socket.send(new SocketRequest("/api/v1/games/start", String.valueOf(matchId)));
    }

    @Override
    public void sendGameLobbyMessage(long matchId, String message) {
        socket.send(new SocketRequest("/api/v1/games/sendMessage", String.valueOf(matchId), message));
    }

    // ----------------------------------------------------------------------------------------------------------------

    public void resetConnection() {
        socket.close();
    }

    public void launchMultiplayerEngine() {
        if (initEngine.getAndSet(true)) return;

        CommonWebSockets.initiate();
        socket = WebSockets.newSocket(ChessConstants.serverAPI);
        socket.setSerializeAsString(true);
        socket.addListener(new WebSocketListener() {

            @Override
            public boolean onOpen(WebSocket webSocket) {
                Gdx.app.log("Websocket Open", webSocket.getUrl());

                if (ChessConstants.chatView != null) {
                    ChessConstants.chatView.clearMessages();
                    RdApplication.postRunnable(() ->
                        ChessConstants.chatView.updateLocalLobbyMessages("restored"));
                }

                if (onTryConnect != null) {
                    BiConsumer<Boolean, String> localConnect = onTryConnect;
                    onTryConnect = null;
                    RdApplication.postRunnable(() -> localConnect.accept(true, ""));
                }

                return false;
            }

            @Override
            public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
                Gdx.app.log("Websocket Close", "reason - " + reason + ", closeCode - " + closeCode);
                if (ChessConstants.chatView != null) {
                    RdApplication.postRunnable(() ->
                        ChessConstants.chatView.updateLocalLobbyMessages("error"));
                }
                return false;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, String packet) {
                var socketRes = gson.fromJson(packet, SocketResult.class);
                var request = socketRes.getRequest();

                Gdx.app.log("Websocket onStringMessage", socketRes.getRequest()
                    + ", status = " + socketRes.getStatus() + ", senderId = " + socketRes.getId());

                if (request.startsWith("/api/v1/accounts")) {
                    onMessageAccounts(request.replaceAll("/api/v1/accounts", ""), socketRes);
                } else if (request.startsWith("/api/v1/mainChat")) {
                    onMessageMainChat(request.replaceAll("/api/v1/mainChat", ""), socketRes);
                } else if (request.startsWith("/api/v1/games")) {
                    onMessageGames(request.replaceAll("/api/v1/games", ""), socketRes);
                }

                return false;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, byte[] packet) {
                Gdx.app.log("Websocket onByteMessage", "Binary request - " + packet[0]);

                switch (packet[0]) {

                    case 0: {
                        RequestStatus result = BinaryRequests.parseResultUpdateAvatar(packet);
                        if (result != RequestStatus.DONE) {
                            Gdx.app.error("Update avatar", result.toString());
                        }
                        RdApplication.postRunnable(() ->
                            onChangeAvatar.accept(result));

                        break;
                    }

                    case 1: {

                        Pair<RequestStatus, Pair<Long, byte[]>> result = BinaryRequests.parseResultGetAvatar(packet);
                        var avatarId = result.getValue().getKey();
                        var avatar = result.getValue().getValue();

                        if (result.getKey() != RequestStatus.DONE) {
                            Gdx.app.error("Get avatar", "for id - " + result.getValue().getKey()
                                + ", status - "+  result.getKey().toString());
                            break;
                        }

                        RdApplication.postRunnable(() -> {
                            List<Consumer<byte[]>> list = onAvatars.get(avatarId);
                            for (Consumer<byte[]> onAvatar : list) {
                                onAvatar.accept(avatar);
                            }
                            list.clear();
                        });

                    }
                }

                return false;
            }

            @Override
            public boolean onError(WebSocket webSocket, Throwable error) {
                Gdx.app.error("Websocket onError", error.toString());

                if (onTryConnect != null) {
                    BiConsumer<Boolean, String> localConnect = onTryConnect;
                    onTryConnect = null;
                    RdApplication.postRunnable(() -> localConnect.accept(false, error.toString()));
                }

                return false;
            }

        });
        launchConnectListener();
    }

    private void onMessageAccounts(String reqAccounts, SocketResult socketRes) {

        // main chat update
        if (socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER) {

            parseJson(socketRes.getResult(), new TypeToken<Pair<List<Message>, Map<Long, Account>>>() {}.getType(),
                (Consumer<Pair<List<Message>, Map<Long, Account>>>) res -> {
                    lastMessages = res;
                    Collections.reverse(res.getKey());
                    if (mainChatMessages != null) mainChatMessages.accept(res);
            });
            return;
        }

        switch (reqAccounts) {

            case "/login": {

                if (socketRes.getStatus() == RequestStatus.DONE) {
                    parseJson(socketRes.getResult(), Account.class, loginAccount, loginError);
                    loginTime.set(-1);
                } else {
                    RdApplication.postRunnable(() ->
                        loginError.accept(socketRes.getStatus().toString()));
                }

                break;
            }

            case "/signup": {

                if (socketRes.getStatus() == RequestStatus.DONE) {
                    RdApplication.postRunnable(onSignup::call);
                } else {
                    RdApplication.postRunnable(() ->
                        signupError.accept(socketRes.getStatus().toString()));
                }

                break;
            }

            case "/see": {

                if (socketRes.getStatus() == RequestStatus.DONE) {
                    parseJson(socketRes.getResult(), Account.class, account -> {
                        var callback = getAccounts.remove(account.getId());
                        if (callback != null) {
                            callback.accept(account);
                        } else {
                            Gdx.app.error("get account consumer don't found", socketRes.getStatus().toString());
                        }
                    });
                } else {
                    Gdx.app.error("error get account", socketRes.getStatus().toString());
                }

                break;
            }

            case "/change": {

                if (onChangeAccount != null) {
                    RdApplication.postRunnable(() ->
                        onChangeAccount.accept(socketRes.getStatus()));
                }

                if (socketRes.getStatus() != RequestStatus.DONE) {
                    Gdx.app.error("error change account", socketRes.getStatus().toString());
                }

                break;
            }

            case "/search": {

                if (socketRes.getStatus() == RequestStatus.DONE) {

                    parseJson(socketRes.getResult(), new TypeToken<List<Account>>() {}.getType(),
                        (Consumer<List<Account>>) accounts -> {
                            if (onSearch != null) {
                                onSearch.accept(accounts);
                                onSearch = null;
                            }
                        });

                } else {
                    Gdx.app.error("error search accounts", socketRes.getStatus().toString());
                }

                break;
            }

            case "/bullet": {

                if (socketRes.getStatus() == RequestStatus.DONE){
                    parseJson(socketRes.getResult(), new TypeToken<List<Account>>() {}.getType(),
                        (Consumer<List<Account>>) accounts -> {
                            if (onBullet != null) {
                                RdApplication.postRunnable(() -> onBullet.accept(accounts));
                            }
                        });
                } else {
                    Gdx.app.error("error get bullet top", socketRes.getStatus().toString());
                }

                break;
            }

            case "/blitz": {

                if (socketRes.getStatus() == RequestStatus.DONE){
                    parseJson(socketRes.getResult(), new TypeToken<List<Account>>() {}.getType(),
                        (Consumer<List<Account>>) accounts -> {
                            if (onBullet != null) {
                                RdApplication.postRunnable(() -> onBlitz.accept(accounts));
                            }
                        });
                } else {
                    Gdx.app.error("error get blitz top", socketRes.getStatus().toString());
                }

                break;
            }

            case "/rapid": {

                if (socketRes.getStatus() == RequestStatus.DONE){
                    parseJson(socketRes.getResult(), new TypeToken<List<Account>>() {}.getType(),
                        (Consumer<List<Account>>) accounts -> {
                            if (onRapid != null) {
                                RdApplication.postRunnable(() -> onRapid.accept(accounts));
                            }
                        });
                } else {
                    Gdx.app.error("error get rapid top", socketRes.getStatus().toString());
                }

                break;
            }

            case "/long": {

                if (socketRes.getStatus() == RequestStatus.DONE){
                    parseJson(socketRes.getResult(), new TypeToken<List<Account>>() {}.getType(),
                        (Consumer<List<Account>>) accounts -> {
                            if (onLong != null) {
                                RdApplication.postRunnable(() -> onLong.accept(accounts));
                            }
                        });
                } else {
                    Gdx.app.error("error get long top", socketRes.getStatus().toString());
                }

                break;
            }

            case "/punish": {

                if (socketRes.getStatus() != RequestStatus.DONE) {
                    RdApplication.postRunnable(() ->
                        onPunishError.accept(socketRes.toString()));
                }

                break;
            }

            case "/makeInactive": {

                if (socketRes.getStatus() != RequestStatus.DONE) {
                    RdApplication.postRunnable(() ->
                        onInactivePunishError.accept(socketRes.toString()));
                }

                break;
            }

            default: {
                Gdx.app.error("client unknown accounts request", socketRes.getRequest());
            }

        }

    }

    private void onMessageMainChat(String reqMainChat, SocketResult socketRes) {

        if (reqMainChat.equals("/readAll") || (socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER
            && (reqMainChat.equals("/send") || reqMainChat.equals("/remove")))) {

            if (socketRes.getStatus() == RequestStatus.DONE
                || socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER) {

                parseJson(socketRes.getResult(), new TypeToken<Pair<List<Message>, Map<Long, Account>>>() {}.getType(),
                    (Consumer<Pair<List<Message>, Map<Long, Account>>>) res -> {
                        lastMessages = res;
                        Collections.reverse(res.getKey());
                        if (mainChatMessages != null) mainChatMessages.accept(res);
                    });

            } else {
                Gdx.app.error("error read messages", socketRes.getStatus().toString());
            }

        }
        else if (reqMainChat.equals("/readLobby") || (socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER
            && reqMainChat.equals("/sendLobby"))) {

            if (socketRes.getStatus() == RequestStatus.DONE
                || socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER) {

                parseJson(socketRes.getResult(), new TypeToken<List<LobbyMessage>>() {}.getType(),
                    (Consumer<List<LobbyMessage>>) result -> {
                        lastMainLobby = result;
                        if (onMainLobby != null) {
                            RdApplication.postRunnable(() ->
                                onMainLobby.accept(new ArrayList<>(result)));
                        }
                });

            } else {
                Gdx.app.error("error read main lobby", socketRes.getStatus().toString());
            }

        } else if (reqMainChat.equals("/send")) {

            if (socketRes.getStatus() != RequestStatus.DONE) {
                Gdx.app.error("error send message", socketRes.getStatus().toString());
            }

        }
        else if (reqMainChat.equals("/sendLobby")) {

            if (socketRes.getStatus() != RequestStatus.DONE) {
                Gdx.app.error("error send lobby", socketRes.getStatus().toString());
                if (socketRes.getStatus() == RequestStatus.BANNED) {
                    ChessConstants.chatView.updateLocalLobbyMessages("self_banned");
                } else if (socketRes.getStatus() == RequestStatus.INCORRECT_DATA || socketRes.getStatus() == RequestStatus.DENIED) {
                    ChessConstants.chatView.updateLocalLobbyMessages("denied");
                } else if (socketRes.getStatus() == RequestStatus.NOT_FOUND) {
                    ChessConstants.chatView.updateLocalLobbyMessages("unknown");
                }
            }

        }
        else if (reqMainChat.equals("/remove")) {

            if (socketRes.getStatus() != RequestStatus.DONE) {
                Gdx.app.error("error remove message", socketRes.getStatus().toString());
            }

        }
        else if (reqMainChat.equals("/list")) {

            if (socketRes.getStatus() == RequestStatus.DONE) {
                RdApplication.postRunnable(() -> {
                    if (onMainLobbyList != null) {
                        parseJson(socketRes.getResult(), new TypeToken<List<LobbyMessage>>() {}.getType(),
                            onMainLobbyList);
                    }
                });
            } else {
                Gdx.app.error("error list command", socketRes.getStatus().toString());
            }

        }
        else {
            Gdx.app.error("client unknown main chat request", socketRes.getRequest());
        }

    }

    private void onMessageGames(String reqGames, SocketResult socketRes) {

        if (socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER) {

            if (socketRes.getResult().startsWith("[")) {
                parseJson(socketRes.getResult(), new TypeToken<List<Match>>() {}.getType(),
                    (Consumer<List<Match>>) matches -> {
                        lastMatches = matches;
                        Collections.reverse(lastMatches);
                        for (Consumer<List<Match>> onMatches : listOnMatches) {
                            onMatches.accept(matches);
                        }
                    }
                );
            } else {

                // update current match
                parseJson(socketRes.getResult(), Match.class,
                    match -> {
                    // > 1 sessions
                    if (match.getId() != matchId) return;
                    lastMatch = match;
                    if (onUpdateMatch != null)
                        onUpdateMatch.accept(match);
                });

            }

        } else {

            switch (reqGames) {

                case "/getGames": {

                    if (socketRes.getStatus() == RequestStatus.DONE) {

                        parseJson(socketRes.getResult(), new TypeToken<List<Match>>() {}.getType(),
                            (Consumer<List<Match>>) matches -> {
                                lastMatches = matches;
                                Collections.reverse(lastMatches);
                                for (Consumer<List<Match>> onMatches : listOnMatches) {
                                    onMatches.accept(matches);
                                }
                            }
                        );

                    } else {
                        Gdx.app.error("error get matches", socketRes.getStatus().toString());
                    }

                    break;
                }

                case "/create": {

                    if (socketRes.getStatus() == RequestStatus.DONE) {

                        parseJson(socketRes.getResult(), Match.class,
                            match -> {
                                lastMatch = match;
                                if (createdMatch != null) {
                                    createdMatch.accept(match);
                                    createdMatch = null;
                                }
                            });

                    } else {
                        Gdx.app.error("error create match", socketRes.getStatus().toString()
                            + ": " + socketRes.getResult());

                        if (createdError != null && socketRes.getId() == ChessConstants.loggingAcc.getId()) {
                            createdError.accept(socketRes.getStatus().toString() + ": " + socketRes.getResult());
                        }
                    }

                    break;
                }

                case "/sendMessage": {
                    if (socketRes.getStatus() != RequestStatus.DONE) {
                        Gdx.app.error("error send message in match", socketRes.getStatus().toString());
                        if (socketRes.getStatus() == RequestStatus.BANNED) {
                            ChessConstants.chatView.updateLocalGameMessages("self_banned");
                        } else if (socketRes.getStatus() == RequestStatus.INCORRECT_DATA || socketRes.getStatus() == RequestStatus.DENIED) {
                            ChessConstants.chatView.updateLocalGameMessages("denied");
                        } else if (socketRes.getStatus() == RequestStatus.NOT_FOUND) {
                            ChessConstants.chatView.updateLocalGameMessages("unknown");
                        }
                    }

                    break;
                }

                case "/makeMove": {
                    if (socketRes.getStatus() != RequestStatus.DONE) {
                        Gdx.app.error("error make move", socketRes.getStatus().toString());
                    }

                    break;
                }

                case "/enter": {
                    if (socketRes.getStatus() != RequestStatus.DONE) {
                        Gdx.app.error("error enter match", socketRes.getStatus().toString());
                        String res = socketRes.getResult() != null ? socketRes.getResult() : "";
                        if (onErrorEnter != null) {
                            RdApplication.postRunnable(() ->
                                onErrorEnter.accept(socketRes.getStatus() + res));
                        }
                    } else {
                        RdApplication.postRunnable(onSuccessEnter::call);
                    }
                }

                case "/exit": {
                    if (socketRes.getStatus() != RequestStatus.DONE) {
                        Gdx.app.error("error exit match", socketRes.getStatus().toString());
                    }

                    break;
                }

                case "/join": {
                    if (socketRes.getStatus() != RequestStatus.DONE) {
                        Gdx.app.error("error join match", socketRes.getStatus().toString());
                    }

                    break;
                }

                case "/disjoin": {
                    if (socketRes.getStatus() != RequestStatus.DONE) {
                        Gdx.app.error("error disjoin match", socketRes.getStatus().toString());
                    }

                    break;
                }

                case "/start": {
                    if (socketRes.getStatus() != RequestStatus.DONE) {
                        Gdx.app.error("error start match", socketRes.getStatus().toString());
                    }

                    break;
                }

                case "/removeMatch": {
                    if (socketRes.getStatus() != RequestStatus.DONE) {
                        Gdx.app.error("error remove match", socketRes.getStatus().toString());

                        if (onErrorRemoveMatch != null) {
                            String res = socketRes.getResult() != null ? socketRes.getResult() : "";
                            RdApplication.postRunnable(() ->
                                onErrorRemoveMatch.accept(socketRes.getStatus() + res));
                        }
                    }

                    break;
                }

                case "/list": {

                    if (socketRes.getStatus() == RequestStatus.DONE) {
                        if (onGameLobbyList != null) {
                            RdApplication.postRunnable(() ->
                                parseJson(socketRes.getResult(), new TypeToken<List<LobbyMessage>>() {}.getType(),
                                    onGameLobbyList));
                        }
                    } else {
                        Gdx.app.error("error list command", socketRes.getStatus().toString());
                    }

                    break;
                }

                default: {
                    Gdx.app.error("client unknown games request", socketRes.getRequest());
                }

            }

        }
    }

    private <T> void parseJson(String json, Class<T> clazz, Consumer<T> onResult) {
        parseJson(json, clazz, onResult, s -> {});
    }

    private <T> void parseJson(String json, Class<T> clazz, Consumer<T> onResult, Consumer<String> onError) {
        try {
            T data = gson.fromJson(json, clazz);
            if (data == null) {
                Gdx.app.error("parseJson", clazz + " == null");
                RdApplication.postRunnable(() -> onError.accept(clazz + " == null"));
            }
            RdApplication.postRunnable(() -> onResult.accept(data));

        } catch (Throwable t) {
            Gdx.app.error("parseJson", clazz + ": " + t);
            RdApplication.postRunnable(() -> onError.accept(clazz + ": " + t));
        }
    }

    private <T> void parseJson(String json, Type type, Consumer<T> onResult) {
        parseJson(json, type, onResult, s -> {});
    }

    private <T> void parseJson(String json, Type type, Consumer<T> onResult, Consumer<String> onError) {
        try {
            T data = gson.fromJson(json, type);
            if (data == null) {
                Gdx.app.error("parseJson", type + " == null");
                RdApplication.postRunnable(() -> onError.accept(type + " == null"));
            }
            RdApplication.postRunnable(() -> onResult.accept(data));

        } catch (Throwable t) {
            Gdx.app.error("parseJson", RdLogger.self().getDescription(t));
            RdApplication.postRunnable(() -> onError.accept(RdLogger.self().getDescription(t)));
        }
    }

    /** reconnects to the server */
    private void launchConnectListener() {

        Runnable listener = () -> {
            // non final
            AtomicBoolean reset = new AtomicBoolean(false);

            while (true) {

                if (loginTime.get() != -1 && RdApplication.self().getLauncher().currentMillis() - loginTime.get() > 15_000) {
                    loginError.accept("Timeout");
                }

                if (!socket.isConnecting() && !socket.isOpen()) {
                    Gdx.app.log("multiplayerThread","Try connect");
                    socket.connect();
                    reset.set(ChessConstants.loggingAcc != null);
                } else if (reset.get()) {

                    login(ChessConstants.localData.getNameAcc(), ChessConstants.localData.getPassword(),
                        account -> {
                            Gdx.app.log("multiplayerThread", "account successfully logged in again");
                            reset.set(false);
                        },
                        s -> {
                            Gdx.app.error("multiplayerThread", "account could not login; " + s);
                        });
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Gdx.app.error("multiplayerThread fatal", RdLogger.self().getDescription(e));
                }

            }

        };
        RdApplication.self().execute(listener);
    }
}
