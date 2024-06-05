package com.iapp.ageofchess.server.controllers;

import com.google.gson.Gson;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.server.config.WebSocketConfig;
import com.iapp.lib.ui.screens.RdLogger;
import com.iapp.lib.util.Pair;
import com.iapp.lib.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final long ACCOUNT_UPDATE_TIME = 30 * 60 * 1000;
    private static final long MAIN_CHAT_UPDATE_TIME = 60 * 1000;
    private static final Logger websocketLogger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final Object MUTEX = new Object();

    private final Gson gson = new Gson();
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private final AccountController accountController;
    private final MainChatController mainChatController;
    private final GamesController gamesController;
    private final SysAdminController sysAdminController;

    /**
     * One account can log in from multiple devices
     * how many sessions are in a match
     * account id -> match id -> session ids
     * */
    private final Map<Long, Map<Long, Set<String>>> countSessionEntered = new ConcurrentHashMap<>();

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Account> logins = new ConcurrentHashMap<>();


    @Autowired
    public WebSocketHandler(AccountController accountController,
                            MainChatController mainChatController,
                            GamesController gamesController,
                            SysAdminController sysAdminController) {
        this.accountController = accountController;
        this.mainChatController = mainChatController;
        this.gamesController = gamesController;
        this.sysAdminController = sysAdminController;
        launchParallel();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {

        gamesController.setOnUpdateMatch(match -> {
            updateEnteredUsers(match.getId(), new SocketRequest("/api/v1/games/updateTime"));
        });

        try {

            SocketRequest request;
            request = gson.fromJson(message.getPayload(), SocketRequest.class);
            if (request == null) {
                websocketLogger.error("request == null");
                return;
            }

            if (request.getRequest().startsWith("/api/v1/accounts")) {
                SocketResult res = requireFromAccounts(
                        session,
                        request.getRequest().replaceAll("/api/v1/accounts", ""),
                        request);
                updateClient(session, res);

                return;
            } else if (request.getRequest().startsWith("/api/v1/mainChat")) {

                SocketResult res = requireFromMainChat(
                        session,
                        request.getRequest().replaceAll("/api/v1/mainChat", ""),
                        request);
                updateClient(session, res);

                return;

            } else if (request.getRequest().startsWith("/api/v1/games")) {

                SocketResult res = requireFromGames(
                        session,
                        request.getRequest().replaceAll("/api/v1/games", ""),
                        request);
                updateClient(session, res);

                return;

            } else if (request.getRequest().startsWith("/api/v1/server")) {
                SocketResult res = requireFromServer(
                    session,
                    request.getRequest().replaceAll("/api/v1/server", ""),
                    request);
                updateClient(session, res);
                return;
            }

            updateClient(session, new SocketResult(RequestStatus.SOCKET_NOT_FOUND, request));

        } catch (Throwable t) {
            websocketLogger.error("[handleTextMessage] " + RdLogger.self().getDescription(t));
        }

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        try {

            byte[] data = message.getPayload().array();
            switch (data[0]) {

                case 0: {
                    var loginAccount = logins.get(session.getId());
                    RequestStatus requestStatus = RequestStatus.DENIED;
                    if (loginAccount != null) {
                        var pair = BinaryRequests.parseUpdateAvatar(message.getPayload().array());
                        requestStatus = accountController.updateAvatar(loginAccount, pair.getKey(), pair.getValue());
                    }

                    // only the sender receives the transaction status in real time, the rest is delayed
                    updateClient(session, BinaryRequests.resultUpdateAvatar((byte) 0, requestStatus));

                    break;
                }

                case 1: {
                    var loginAccount = logins.get(session.getId());
                    if (loginAccount != null) {
                        var id = BinaryRequests.parseGetAvatar(data);
                        var pair = accountController.getAvatar(id);
                        updateClient(session, BinaryRequests.resultGetAvatar((byte) 1, pair.getKey(), id, pair.getValue()));
                        break;
                    }
                    updateClient(session, BinaryRequests.resultGetAvatar((byte) 1, RequestStatus.DENIED, -1, null));
                    break;

                }

            }
        } catch (Throwable t) {
            websocketLogger.error("handleBinaryMessage", t);
        }

        super.handleBinaryMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        Account account = logins.get(session.getId());
        boolean update = false;
        if (account != null) {
            for (Match match : gamesController.getGames()) {
                if (canExitAndUpdate(account.getId(), match.getId(), session.getId())) {
                    gamesController.disconnect(logins.get(session.getId()).getId(), match.getId());
                }
            }

            if (getSession(account.getId()).size() == 0) {
                countSessionEntered.remove(account.getId());
                mainChatController.sendDisconnect(account);
                update = true;
            }
        }

        sessions.remove(session.getId());
        logins.remove(session.getId());

        if (update) {
            updateMainLobbyForClients();
        }
    }

    private SocketResult requireFromServer(WebSocketSession session, String serverReq, SocketRequest socketRequest) {
        String[] params = socketRequest.getParameters();
        Account loginAcc = logins.get(session.getId());
        if (loginAcc != null) socketRequest.setSenderId(loginAcc.getId());

        switch (serverReq) {
            case "/restart": {
                if (loginAcc == null || loginAcc.getType().ordinal() < AccountType.EXECUTOR.ordinal()) {
                    return new SocketResult(RequestStatus.DENIED, socketRequest);
                }

                sysAdminController.restart();
                return new SocketResult(RequestStatus.DONE, socketRequest);
            }

            case "/readData": {
                if (loginAcc == null || loginAcc.getType().ordinal() < AccountType.DEVELOPER.ordinal()) {
                    return new SocketResult(RequestStatus.DENIED, socketRequest);
                }
                if (params.length != 2) {
                    return new SocketResult(RequestStatus.INCORRECT_DATA, socketRequest);
                }

                byte id = Byte.parseByte(params[0]);
                try {
                    byte[] data = sysAdminController.readData(params[1]);

                    for (byte[] part : BinaryRequests.splitArray((byte) 2, id, data,
                        (int) (WebSocketConfig.BYTE_BUFFER_SIZE * 0.9f))) {
                        updateClient(session, part);
                    }

                    return new SocketResult(RequestStatus.DONE, socketRequest);
                } catch (IOException e) {
                    return new SocketResult(RequestStatus.INCORRECT_DATA, e.toString(), socketRequest);
                }
            }
        }

        return new SocketResult(RequestStatus.SOCKET_NOT_FOUND, socketRequest);
    }

    private SocketResult requireFromAccounts(WebSocketSession session, String accountReq, SocketRequest socketRequest) {
        String[] params = socketRequest.getParameters();
        Account loginAcc = logins.get(session.getId());
        if (loginAcc != null) socketRequest.setSenderId(loginAcc.getId());

        switch (accountReq) {

            case "/online": {
                return new SocketResult(RequestStatus.DONE, String.valueOf(getOnline()), socketRequest);
            }

            case "/login": {

                // multiple logins
                if (logins.get(session.getId()) != null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                var ip = session.getRemoteAddress() != null ?
                    session.getRemoteAddress().toString() : "null";
                var login = gson.fromJson(params[2], Login.class);
                login.setIp(ip);

                var res = accountController.login(
                    params[0], params[1], login);

                if (res.getKey() == RequestStatus.DONE) {
                    // blocked users are prevented from logging in
                    if (res.getValue().isBanned()) {
                        return new SocketResult(RequestStatus.BANNED, socketRequest);
                    }

                    res.getValue().setOnlineNow(true);
                    logins.put(session.getId(), res.getValue());
                    updateMainChatForClients(socketRequest);

                    if (getSession(res.getValue().getId()).size() == 1) {
                        mainChatController.sendConnect(res.getValue());
                        updateMainLobbyForClients();
                    }
                }

                return new SocketResult(res.getKey(), gson.toJson(res.getValue()), socketRequest);
            }

            case "/signup": {

                return new SocketResult(accountController.signup(params[0], params[1], params[2]),
                        socketRequest);
            }

            case "/see": {

                var pair = accountController.see(Long.parseLong(params[0]));
                updateOnline(pair.getValue());

                return new SocketResult(pair.getKey(), gson.toJson(pair.getValue()),
                        socketRequest);
            }

            case "/seeAccounts": {

                long[] ids = gson.fromJson(params[0], long[].class);
                var pair = accountController.seeAccounts(ids);
                updateOnline(pair.getValue());

                return new SocketResult(pair.getKey(), gson.toJson(pair.getValue()),
                        socketRequest);
            }

            case "/change": {

                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                return new SocketResult(accountController.change(loginAcc, params[0]),
                        socketRequest);
            }

            case "/getAllData": {

                if (loginAcc == null || loginAcc.getType() != AccountType.DEVELOPER)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                return new SocketResult(RequestStatus.DONE, gson.toJson(accountController.getAllData()),
                        socketRequest);
            }

            case "/search": {

                var accounts = accountController.searchAccounts(params[0]);
                if (accounts.getKey() != RequestStatus.DONE) return new SocketResult(accounts.getKey(), socketRequest);
                updateOnline(accounts.getValue());

                return new SocketResult(RequestStatus.DONE,
                        gson.toJson(accounts.getValue()), socketRequest);
            }

            case "/bullet": {
                Pair<RequestStatus, List<Account>> pair = accountController.getBulletTop();
                updateOnline(pair.getValue());

                return new SocketResult(pair.getKey(), gson.toJson(pair.getValue()), socketRequest);
            }

            case "/blitz": {
                Pair<RequestStatus, List<Account>> pair = accountController.getBlitzTop();
                updateOnline(pair.getValue());

                return new SocketResult(pair.getKey(), gson.toJson(pair.getValue()), socketRequest);
            }

            case "/rapid": {
                Pair<RequestStatus, List<Account>> pair = accountController.getRapidTop();
                updateOnline(pair.getValue());

                return new SocketResult(pair.getKey(), gson.toJson(pair.getValue()), socketRequest);
            }

            case "/long": {
                Pair<RequestStatus, List<Account>> pair = accountController.getLongTop();
                updateOnline(pair.getValue());

                return new SocketResult(pair.getKey(), gson.toJson(pair.getValue()), socketRequest);
            }

            case "/punish": {
                if (loginAcc == null) return new SocketResult(RequestStatus.DENIED, socketRequest);
                long id = Long.parseLong(params[0]);
                Punishment punishment = gson.fromJson(params[1], Punishment.class);
                return new SocketResult(accountController.punish(loginAcc, id, punishment), socketRequest);
            }

            case "/makeInactive": {
                if (loginAcc == null) return new SocketResult(RequestStatus.DENIED, socketRequest);
                long punishableId = Long.parseLong(params[0]);
                long punishmentId = Long.parseLong(params[1]);
                return new SocketResult(accountController.makeInactive(loginAcc, punishableId, punishmentId),
                    socketRequest);
            }

        }

        return new SocketResult(RequestStatus.SOCKET_NOT_FOUND, socketRequest);
    }

    private SocketResult requireFromMainChat(WebSocketSession session, String mainChatReq, SocketRequest socketRequest) {
        var params = socketRequest.getParameters();
        var loginAcc = logins.get(session.getId());
        if (loginAcc != null) socketRequest.setSenderId(loginAcc.getId());

        switch (mainChatReq) {

            case "/list": {
                return new SocketResult(RequestStatus.DONE, gson.toJson(
                    getListCommand(logins.values())), socketRequest);
            }

            case "/readAll": {

                var pair = mainChatController.readAll();
                updateOnline(pair.getValue().values());
                return new SocketResult(RequestStatus.DONE, gson.toJson(pair), socketRequest);
            }

            case "/send": {

                if (loginAcc == null) return new SocketResult(RequestStatus.DENIED, socketRequest);
                if (loginAcc.isMuted()) return new SocketResult(RequestStatus.MUTED, socketRequest);

                var result = mainChatController.send(loginAcc.getId(), params[0]);
                if (result == RequestStatus.DONE) {
                    updateMainChatForClients(socketRequest);
                }
                return new SocketResult(result, socketRequest);
            }

            case "/remove": {

                if (loginAcc == null) return new SocketResult(RequestStatus.DENIED, socketRequest);
                var result2 = mainChatController.remove(loginAcc.getId(), Long.parseLong(params[0]));
                if (result2 == RequestStatus.DONE) {
                    updateMainChatForClients(socketRequest);
                }
                return new SocketResult(result2, socketRequest);
            }

            case "/sendLobby": {
                if (loginAcc == null) return new SocketResult(RequestStatus.DENIED, socketRequest);
                if (loginAcc.isMuted()) return new SocketResult(RequestStatus.MUTED, socketRequest);
                RequestStatus requestStatus = mainChatController.sendMainLobby(loginAcc, params[0]);

                if (requestStatus == RequestStatus.DONE) {
                    updateMainLobbyForClients();
                }
                return new SocketResult(requestStatus, socketRequest);
            }

            case "/readLobby": {
                List<LobbyMessage> lobby = mainChatController.readMainLobby();
                // transmission of the number of people in the network, to reduce requests to the server
                lobby.add(0, new LobbyMessage(-1, -1, String.valueOf(getOnline())));
                return new SocketResult(RequestStatus.DONE,
                    gson.toJson(lobby), socketRequest);
            }
        }

        return new SocketResult(RequestStatus.SOCKET_NOT_FOUND, socketRequest);
    }

    private SocketResult requireFromGames(WebSocketSession session, String gamesReq, SocketRequest socketRequest) {
        var params = socketRequest.getParameters();
        var loginAcc = logins.get(session.getId());
        if (loginAcc != null) socketRequest.setSenderId(loginAcc.getId());

        switch (gamesReq) {

            case "/list": {
                Pair<RequestStatus, Match> result = gamesController.getMatch(Long.parseLong(params[0]));
                if (result.getKey() != RequestStatus.DONE) return new SocketResult(result.getKey(), socketRequest);
                System.out.println(getListCommand(
                    getLoginAccounts(result.getValue().getEntered())
                ));

                return new SocketResult(RequestStatus.DONE,
                    gson.toJson(getListCommand(
                        getLoginAccounts(result.getValue().getEntered())
                    )), socketRequest);
            }

            case "/getGames": {
                return new SocketResult(RequestStatus.DONE,
                        gson.toJson(gamesController.getGames()), socketRequest);
            }

            case "/getMatch": {
                var pair = gamesController.getMatch(Long.parseLong(params[0]));
                return new SocketResult(pair.getKey(), gson.toJson(pair.getValue()), socketRequest);
            }

            // all
            case "/create": {

                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                // make enter too
                var pair = gamesController.create(loginAcc.getId(), params[0]);
                if (pair.getKey() == RequestStatus.DONE) {

                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(pair.getKey(), pair.getValue(), socketRequest);
            }

            // entered
            case "/sendMessage": {
                if (loginAcc == null) return new SocketResult(RequestStatus.DENIED, socketRequest);
                if (loginAcc.isMuted()) return new SocketResult(RequestStatus.MUTED, socketRequest);

                long matchId = Long.parseLong(params[0]);
                var sendMessageRes = gamesController.sendLobby(loginAcc.getId(),
                        matchId, params[1]);
                if (sendMessageRes == RequestStatus.DONE) {
                    updateEnteredUsers(matchId, socketRequest);
                }

                return new SocketResult(sendMessageRes, socketRequest);
            }

            // joined
            case "/makeMove": {
                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                long matchId = Long.parseLong(params[0]);
                var matchMoveRes = gamesController.makeMove(loginAcc.getId(),
                        matchId, params[1]);
                if (matchMoveRes == RequestStatus.DONE) {
                    updateEnteredUsers(matchId, socketRequest);
                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(matchMoveRes, params[1], socketRequest);
            }

            // entered
            case "/enter": {
                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                long matchId = Long.parseLong(params[0]);
                updateEnteredSessions(loginAcc.getId(), matchId, session.getId());

                var enteredResult = gamesController.connect(loginAcc.getId(), matchId);
                if (enteredResult == RequestStatus.DONE) {
                    updateEnteredUsers(matchId, socketRequest);
                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(enteredResult, socketRequest);
            }

            // entered
            case "/exit": {
                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                long matchId = Long.parseLong(params[0]);
                // connect two device
                if (!canExitAndUpdate(loginAcc.getId(), matchId, session.getId())) {
                    return new SocketResult(RequestStatus.DONE, socketRequest);
                }

                var exitResult = gamesController.disconnect(loginAcc.getId(), matchId);
                if (exitResult == RequestStatus.DONE) {
                    updateEnteredUsers(matchId, socketRequest);
                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(exitResult, socketRequest);
            }

            // entered
            case "/join": {
                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                long matchId = Long.parseLong(params[0]);
                var joinRes = gamesController.join(loginAcc.getId(), matchId, params[1]);
                if (joinRes == RequestStatus.DONE) {
                    updateEnteredUsers(matchId, socketRequest);
                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(joinRes, socketRequest);
            }

            // entered
            case "/disjoin": {
                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                long matchId = Long.parseLong(params[0]);
                var disjoinRes = gamesController.disjoin(loginAcc.getId(), matchId);
                if (disjoinRes == RequestStatus.DONE) {
                    updateEnteredUsers(matchId, socketRequest);
                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(disjoinRes, socketRequest);
            }

            // entered
            case "/start": {
                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                long matchId = Long.parseLong(params[0]);
                var startRes = gamesController.start(loginAcc.getId(), matchId);
                if (startRes == RequestStatus.DONE) {
                    updateEnteredUsers(matchId, socketRequest);
                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(startRes, socketRequest);
            }

            // all
            case "/removeMatch": {
                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                var requestStatus = gamesController.removeMatch(loginAcc.getId(), Long.parseLong(params[0]));
                if (requestStatus == RequestStatus.DONE) {
                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(requestStatus, socketRequest);
            }

        }

        return new SocketResult(RequestStatus.SOCKET_NOT_FOUND, socketRequest);
    }

    private void updateClients(SocketResult result) {

        for (var session : sessions.values()) {

            try {
                synchronized (MUTEX) {
                    session.sendMessage(new TextMessage(gson.toJson(result)));
                }
            } catch (Throwable t) {
                removeClient(session.getId());
                websocketLogger.error("Error update client, session id = " + session.getId()
                    + ", " + RdLogger.self().getDescription(t));
            }
        }
    }

    private void updateClient(WebSocketSession session, SocketResult result) {
        try {
            synchronized (MUTEX) {
                session.sendMessage(new TextMessage(gson.toJson(result)));
            }
        } catch (Throwable t) {
            removeClient(session.getId());
            websocketLogger.error("Error update client, session id = " + session.getId()
                + ", " + RdLogger.self().getDescription(t));
        }

    }

    private void updateClient(WebSocketSession session, byte[] result) {
        try {
            synchronized (MUTEX) {
                session.sendMessage(new BinaryMessage(result));
            }
        } catch (Throwable t) {
            removeClient(session.getId());
            websocketLogger.error("Error update client, session id = " + session.getId()
                + ", " + RdLogger.self().getDescription(t));
        }
    }

    private void removeClient(String sessionId) {
        sessions.remove(sessionId);
        var account = logins.remove(sessionId);
        if (account == null) return;
        for (var match : gamesController.getGames()) {
            if (match.getId() == account.getId()) {
                gamesController.disconnect(account.getId(), match.getId());
            }
        }
    }

    private void updateOnline(Account account) {
        account.setOnlineNow(logins.containsValue(account));
    }

    private void updateOnline(Iterable<Account> accounts) {
        for (var acc : accounts) {
            acc.setOnlineNow(logins.containsValue(acc));
        }
    }

    private void updateEnteredUsers(long matchId, SocketRequest req) {
        var match = gamesController.getMatch(matchId);
        if (match.getKey() != RequestStatus.DONE) {
            websocketLogger.error("match not found to update entered, matchId = " + match);
            return;
        }

        for (var id : match.getValue().getEntered()) {
            var sessions = getSession(id);
            if (sessions.isEmpty()) {
                gamesController.disconnect(id, matchId);
            }

            for (var session : sessions) {
                updateClient(session, new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                        gson.toJson(match.getValue()), req));
            }
        }

    }

    public List<WebSocketSession> getSession(long accountId) {
        return logins.entrySet().stream()
                .filter(entry -> entry.getValue().getId() == accountId)
                .map(entry -> sessions.get(entry.getKey()))
                .collect(Collectors.toList());
    }

    private void updateMainChatForClients(SocketRequest socketRequest) {
        var pair = mainChatController.readAll();
        updateOnline(pair.getValue().values());
        updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                gson.toJson(pair), socketRequest));
    }

    private void launchParallel() {

        service.execute(() -> {

            long lastUpdateAccounts = -1, lastUpdateMainChat = -1;
            while (true) {

                try {

                    // updates the state of the matches
                    // synchronized
                    // 700 milliseconds sleep
                    gamesController.updateGames();

                    // every 30 minutes
                    if (System.currentTimeMillis() - lastUpdateAccounts > ACCOUNT_UPDATE_TIME) {
                        accountController.update();
                        lastUpdateAccounts = System.currentTimeMillis();
                    }

                    // every 1 minute
                    if (System.currentTimeMillis() - lastUpdateMainChat > MAIN_CHAT_UPDATE_TIME) {
                        mainChatController.update();
                        lastUpdateMainChat = System.currentTimeMillis();
                    }

                } catch (Throwable t) {
                    websocketLogger.error("launchParallel - " + RdLogger.self().getDescription(t));
                }

            }
        });

    }

    private void updateEnteredSessions(long accId, long matchId, String sessionId) {
        if (!countSessionEntered.containsKey(accId)) {
            countSessionEntered.put(accId, new ConcurrentHashMap<>());
        }
        if (!countSessionEntered.get(accId).containsKey(matchId)) {
            countSessionEntered.get(accId).put(matchId, new ConcurrentSkipListSet<>());
        }
        countSessionEntered.get(accId).get(matchId).add(sessionId);
    }

    private boolean canExitAndUpdate(long accId, long matchId, String sessionId) {
        if (!countSessionEntered.containsKey(accId)) return true;
        if (!countSessionEntered.get(accId).containsKey(matchId)) return true;
        Set<String> sessions = countSessionEntered.get(accId).get(matchId);
        sessions.remove(sessionId);

        boolean res = sessions.isEmpty();
        if (res) countSessionEntered.get(accId).remove(matchId);
        return res;
    }

    private void updateMainLobbyForClients() {
        List<LobbyMessage> lobby = mainChatController.readMainLobby();
        lobby.add(0, new LobbyMessage(-1, -1, String.valueOf(getOnline())));
        updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER, gson.toJson(lobby),
            new SocketRequest("/api/v1/mainChat/sendLobby")));
    }

    private int getOnline() {
        Set<Long> accountSet = new HashSet<>();
        for (Account account : logins.values()) {
            accountSet.add(account.getId());
        }
        return accountSet.size();
    }

    private List<LobbyMessage> getListCommand(Iterable<Account> accounts) {
        List<LobbyMessage> messages = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (Account account : accounts) {
            if (ids.contains(account.getId())) continue;
            ids.add(account.getId());
            messages.add(new LobbyMessage(-1, account.getId(), account.getFullName()));
        }
        return messages;
    }

    private List<Account> getLoginAccounts(List<Long> ids) {
        List<Account> accounts = new ArrayList<>();
        for (Account account : logins.values()) {
            if (ids.contains(account.getId())) {
                accounts.add(account);
            }

        }
        return accounts;
    }
}
