package com.iapp.ageofchess.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.github.czyzby.websocket.CommonWebSockets;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import com.iapp.ageofchess.chess_engine.Color;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.multiplayer.webutil.RequestStatus;
import com.iapp.ageofchess.multiplayer.webutil.SocketRequest;
import com.iapp.ageofchess.multiplayer.webutil.SocketResult;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.RdLogger;
import com.iapp.rodsher.util.CallListener;
import com.iapp.rodsher.util.Pair;
import com.iapp.rodsher.util.SystemValidator;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class MultiplayerEngine {

    private WebSocket socket;

    private final Json gson;
    private boolean multiplayerThread;

    // current match
    private volatile Consumer<Match> onUpdate;
    private volatile long matchId;

    /** login listeners */
    private volatile Consumer<Account> loginAccount;
    private volatile Consumer<String> loginError;

    /** signup listeners */
    private volatile CallListener onSignup;
    private volatile Consumer<String> signupError;

    /** get account listener */
    private volatile Consumer<Account> getAccount;

    /** listener on update main chat messages */
    private volatile Pair<List<Message>, Map<Long, Account>> lastMessages;
    private volatile Consumer<Pair<List<Message>, Map<Long, Account>>> mainChatMessages;

    /** listener on update games */
    private volatile Consumer<List<Match>> onMatches;
    private volatile List<Match> lastMatches;


    private static final MultiplayerEngine INSTANCE = new MultiplayerEngine();

    public static MultiplayerEngine self() {
        return INSTANCE;
    }

    private MultiplayerEngine() {
        gson = new Json();
    }

    // account requests -----------------------------------------------------------------------------------------------

    /** updates online and gets the user */
    public void login(String name, String password, Consumer<Account> loginAccount,
                      Consumer<String> loginError) {
        if (!socket.isOpen()) loginError.accept("socket is not open");

        this.loginAccount = loginAccount;
        this.loginError = loginError;

        var login = new Login("",
            ChessConstants.localData.getLocale().getLanguage(),
            Locale.getDefault().getLanguage(),
            Locale.getDefault().getCountry(),
            SystemValidator.getOperationSystem().replaceAll("\\s", ""));

        socket.send(new SocketRequest("/api/v1/accounts/login", name, password, gson.toJson(login)));
    }

    /** creates a new user account on the server */
    public void signup(String name, String userName, String password,
                       CallListener onSignup, Consumer<String> signupError) {

        this.onSignup = onSignup;
        this.signupError = signupError;
        socket.send(new SocketRequest("/api/v1/accounts/signup", name, userName, password));
    }

    @SuppressWarnings("DefaultLocale")
    public void changeAccount(Account updated) {
        socket.send(new SocketRequest("/api/v1/accounts/change", gson.toJson(updated)));
    }

    public void getAccount(long id, Consumer<Account> getAccount) {
        this.getAccount = getAccount;
        socket.send(new SocketRequest("/api/v1/accounts/see", String.valueOf(id)));
    }

    // main chat ------------------------------------------------------------------------------------------------------

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

    public void setOnMatches(Consumer<List<Match>> onMatches) {
        this.onMatches = onMatches;

        if (onMatches == null) return;
        if (lastMatches != null) {
            onMatches.accept(lastMatches);
        } else {
            socket.send(new SocketRequest("/api/v1/games/getGames"));
        }
    }

    public void removeMatch(long matchId) {


    }

    public void createMatch(LocalMatch localMatch, Consumer<Match> onMatch,
                            Consumer<String> onError) {

    }

    public void enterMatch(long matchId) {

    }

    public void makeMove(long matchId, String fen) {

    }

    public void join(long matchId, Color color) {

    }

    public void disjoin(long matchId) {

    }

    public void setOnUpdateMatch(long matchId, Consumer<Match> onUpdate) {
        this.matchId = matchId;
        this.onUpdate = onUpdate;
    }

    private void updateMatch(long matchId, Consumer<Match> onNewMatch) {

    }

    public void start(long matchId) {

    }

    public void sendLobbyMessage(long matchId, String message) {

    }

    // ----------------------------------------------------------------------------------------------------------------

    public void launchMultiplayerThread() {
        if (multiplayerThread) return;
        multiplayerThread = true;

        CommonWebSockets.initiate();
        socket = WebSockets.newSocket("ws://localhost:8082/ws");
        socket.setSerializeAsString(true);
        socket.addListener(new WebSocketListener() {

            @Override
            public boolean onOpen(WebSocket webSocket) {
                Gdx.app.log("Websocket Open", webSocket.getUrl());
                return false;
            }

            @Override
            public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
                Gdx.app.log("Websocket Close", "reason - " + reason + ", closeCode - " + closeCode);
                return false;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, String packet) {
                var socketRes = gson.fromJson( SocketResult.class, packet);
                var request = socketRes.getRequest();
                Gdx.app.log("Websocket onStringMessage", socketRes.getRequest());

                if (request.startsWith("/api/v1/accounts")) {
                    onMessageAccounts(request.replaceAll("/api/v1/accounts", ""), socketRes);
                }

                else if (request.startsWith("/api/v1/mainChat")) {
                    onMessageMainChat(request.replaceAll("/api/v1/mainChat", ""), socketRes);
                }

                else if (request.startsWith("/api/v1/games")) {
                    onMessageGames(request.replaceAll("/api/v1/games", ""), socketRes);
                }

                return false;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, byte[] packet) {
                Gdx.app.log("Websocket onByteMessage", " - Nothing!");
                return false;
            }

            @Override
            public boolean onError(WebSocket webSocket, Throwable error) {
                Gdx.app.error("Websocket onError", RdLogger.getDescription(error));
                return false;
            }

        });
        socket.connect();
    }

    private void onMessageAccounts(String reqAccounts, SocketResult socketRes) {

        switch (reqAccounts) {
            case "/login":

                if (socketRes.getStatus() == RequestStatus.DONE) {
                    parseJson(socketRes.getResult(), Account.class, loginAccount, loginError);
                } else {
                    RdApplication.postRunnable(() ->
                        loginError.accept(socketRes.getStatus().toString()));
                }

                break;

            case "/signup":

                if (socketRes.getStatus() == RequestStatus.DONE) {
                    RdApplication.postRunnable(onSignup::call);
                } else {
                    RdApplication.postRunnable(() ->
                        signupError.accept(socketRes.getStatus().toString()));
                }

                break;

            case "/see":

                if (socketRes.getStatus() == RequestStatus.DONE) {
                    parseJson(socketRes.getResult(), Account.class, getAccount);
                } else {
                    Gdx.app.error("error get account", socketRes.getStatus().toString());
                }

                break;

            case "/change":

                if (socketRes.getStatus() != RequestStatus.DONE) {
                    Gdx.app.error("error change account", socketRes.getStatus().toString());
                }

                break;

        }

    }

    private void onMessageMainChat(String reqMainChat, SocketResult socketRes) {

        if (reqMainChat.equals("/readAll") || (socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER
            && (reqMainChat.equals("/send") || reqMainChat.equals("/remove")) )) {

            if (socketRes.getStatus() == RequestStatus.DONE
                || socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER) {

                parseJson(socketRes.getResult(), Pair.class,
                    res -> {
                        // TODO
                        lastMessages = res;
                        mainChatMessages.accept(res);
                    });

            } else {
                Gdx.app.error("error read messages", socketRes.getStatus().toString());
            }

        }

        else if (reqMainChat.equals("/send")) {

            if (socketRes.getStatus() != RequestStatus.DONE) {
                Gdx.app.error("error send message", socketRes.getStatus().toString());
            }

        }

        else if (reqMainChat.equals("/remove")) {

            if (socketRes.getStatus() != RequestStatus.DONE) {
                Gdx.app.error("error remove message", socketRes.getStatus().toString());
            }

        }

    }

    private void onMessageGames(String reqGames, SocketResult socketRes) {

        if (reqGames.equals("/getGames") || (socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER
            && reqGames.equals("/create"))) {

            if (socketRes.getStatus() == RequestStatus.DONE
                || socketRes.getStatus() == RequestStatus.UPDATE_FROM_SERVER) {


                parseJson(socketRes.getResult(), List.class,
                    matches -> {
                        // TODO
                        lastMatches = matches;
                        onMatches.accept(matches);
                    }
                );

            } else {
                Gdx.app.error("error get matches", socketRes.getStatus().toString());
            }

        }

    }

    private <T> void parseJson(String json, Class<T> clazz, Consumer<T> onResult) {
        parseJson(json, clazz, onResult, s -> {});
    }

    private <T> void parseJson(String json, Class<T> clazz, Consumer<T> onResult, Consumer<String> onError) {
        try {

            T data = gson.fromJson(clazz, json);
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
}
