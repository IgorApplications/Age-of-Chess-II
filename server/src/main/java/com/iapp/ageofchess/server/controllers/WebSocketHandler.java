package com.iapp.ageofchess.server.controllers;

import com.google.gson.Gson;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.AccountType;
import com.iapp.lib.web.Login;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.lib.web.BinaryRequests;
import com.iapp.lib.web.RequestStatus;
import com.iapp.lib.web.SocketRequest;
import com.iapp.lib.web.SocketResult;
import com.iapp.lib.util.Pair;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger websocketLogger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final Object MUTEX = new Object();

    private final Gson gson = new Gson();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private long lastUpdateTop;

    private final AccountController accountController;
    private final MainChatController mainChatController;
    private final GamesController gamesController;
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
                            GamesController gamesController) {
        this.accountController = accountController;
        this.mainChatController = mainChatController;
        this.gamesController = gamesController;
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
                var res = requireFromAccounts(
                        session,
                        request.getRequest().replaceAll("/api/v1/accounts", ""),
                        request);
                updateClient(session, res);

                return;
            } else if (request.getRequest().startsWith("/api/v1/mainChat")) {

                var res = requireFromMainChat(
                        session,
                        request.getRequest().replaceAll("/api/v1/mainChat", ""),
                        request);
                updateClient(session, res);

                return;
            } else if (request.getRequest().startsWith("/api/v1/games")) {

                var res = requireFromGames(
                        session,
                        request.getRequest().replaceAll("/api/v1/games", ""),
                        request);
                updateClient(session, res);

                return;
            }

            updateClient(session, new SocketResult(RequestStatus.SOCKET_NOT_FOUND, request));

        } catch (Throwable t) {
            websocketLogger.error("handleTextMessage", t);
        }

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

        try {

            var data = message.getPayload().array();
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
        if (account != null) {
            for (Match match : gamesController.getGames()) {
                if (canExitAndUpdate(account.getId(), match.getId(), session.getId())) {
                    gamesController.exit(logins.get(session.getId()).getId(), match.getId());
                }
            }

            if (getSession(account.getId()).size() == 1) {
                countSessionEntered.remove(account.getId());
            }
        }

        sessions.remove(session.getId());
        logins.remove(session.getId());
    }

    private SocketResult requireFromAccounts(WebSocketSession session, String accountReq, SocketRequest socketRequest) {
        var params = socketRequest.getParameters();
        var loginAcc = logins.get(session.getId());
        if (loginAcc != null) socketRequest.setId(loginAcc.getId());

        switch (accountReq) {

            case "/login":

                var ip = session.getRemoteAddress() != null ?
                        session.getRemoteAddress().toString() : "null";
                var login = gson.fromJson(params[2], Login.class);
                login.setIp(ip);

                var res = accountController.login(
                        params[0], params[1], login);

                if (res.getKey() == RequestStatus.DONE) {
                    res.getValue().setOnlineNow(true);
                    logins.put(session.getId(), res.getValue());
                }
                updateMainChatForClients(socketRequest);

                return new SocketResult(res.getKey(), gson.toJson(res.getValue()), socketRequest);

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

                var pair = accountController.seeAccounts(params[0]);
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

        }

        return new SocketResult(RequestStatus.SOCKET_NOT_FOUND, socketRequest);
    }

    private SocketResult requireFromMainChat(WebSocketSession session, String mainChatReq, SocketRequest socketRequest) {
        var params = socketRequest.getParameters();
        var loginAcc = logins.get(session.getId());
        if (loginAcc != null) socketRequest.setId(loginAcc.getId());

        switch (mainChatReq) {

            case "/readAll": {

                var pair = mainChatController.readAll();
                updateOnline(pair.getValue().values());
                return new SocketResult(RequestStatus.DONE, gson.toJson(pair), socketRequest);
            }

            case "/send": {

                if (loginAcc == null) return new SocketResult(RequestStatus.DENIED, socketRequest);
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

        }

        return new SocketResult(RequestStatus.SOCKET_NOT_FOUND, socketRequest);
    }

    private SocketResult requireFromGames(WebSocketSession session, String gamesReq, SocketRequest socketRequest) {
        var params = socketRequest.getParameters();
        var loginAcc = logins.get(session.getId());
        if (loginAcc != null) socketRequest.setId(loginAcc.getId());

        switch (gamesReq) {

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

                var pair = gamesController.create(loginAcc.getId(), params[0]);
                if (pair.getKey() == RequestStatus.DONE) {

                    updateClients(new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                            gson.toJson(gamesController.getGames()), socketRequest));
                }

                return new SocketResult(pair.getKey(), pair.getValue(), socketRequest);
            }

            // entered
            case "/sendMessage": {
                if (loginAcc == null)
                    return new SocketResult(RequestStatus.DENIED, socketRequest);

                long matchId = Long.parseLong(params[0]);
                var sendMessageRes = gamesController.sendMessage(loginAcc.getId(),
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

                var enteredResult = gamesController.enter(loginAcc.getId(), matchId);
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

                var exitResult = gamesController.exit(loginAcc.getId(), matchId);
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
            } catch (IOException e) {
                removeClient(session.getId());
                websocketLogger.error(e + " Error update client, session id = " + session.getId());
            }
        }
    }

    private void updateClient(WebSocketSession session, SocketResult result) {
        try {
            synchronized (MUTEX) {
                session.sendMessage(new TextMessage(gson.toJson(result)));
            }
        } catch (IOException e) {
            removeClient(session.getId());
            websocketLogger.error(e + " Error update client, session id = " + session.getId());
        }

    }

    private void updateClient(WebSocketSession session, byte[] result) {
        try {
            synchronized (MUTEX) {
                session.sendMessage(new BinaryMessage(result));
            }
        } catch (IOException e) {
            removeClient(session.getId());
            websocketLogger.error(e + " Error update client, session id = " + session.getId());
        }
    }

    private void removeClient(String sessionId) {
        sessions.remove(sessionId);
        var account = logins.remove(sessionId);
        if (account == null) return;
        for (var match : gamesController.getGames()) {
            if (match.getId() == account.getId()) {
                gamesController.exit(account.getId(), match.getId());
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
                gamesController.exit(id, matchId);
            }

            for (var session : sessions) {
                updateClient(session, new SocketResult(RequestStatus.UPDATE_FROM_SERVER,
                        gson.toJson(match.getValue()), req));
            }
        }

    }

    private void updateJoinedUsers(long matchId, SocketRequest req) {
        var match = gamesController.getMatch(matchId);
        if (match.getKey() != RequestStatus.DONE) {
            websocketLogger.error("match not found to update joined, matchId = " + match);
            return;
        }

        for (var id : List.of(match.getValue().getWhitePlayerId(), match.getValue().getBlackPlayerId())) {
            var sessions = getSession(id);

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
            while (true) {

                try {

                    // updates the state of the matches
                    // synchronized
                    // 700 milliseconds sleep
                    gamesController.updateGames();

                    // every 30 minutes
                    if (System.currentTimeMillis() - lastUpdateTop > 30 * 60 * 1000) {
                        accountController.updateTop();
                        lastUpdateTop = System.currentTimeMillis();
                    }

                } catch (Throwable t) {
                    websocketLogger.error("launchParallel ", t);
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
}
