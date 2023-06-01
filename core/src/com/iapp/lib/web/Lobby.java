package com.iapp.lib.web;

import com.iapp.lib.util.DataChecks;
import com.iapp.lib.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Lobby {

    public static final long SERVER_USER_ID = -1;
    private static final Pattern INTEGER_PATTERN = Pattern.compile("[0-9]+");
    private static final int MAX_LOBBY_SIZE = 10;
    private static final int MAX_ID = 20;
    private static final long MAX_LOBBY_BANNED_TIME = 24 * 60 * 60 * 1000;

    private final Function<String, List<Account>> onGetAccount;
    private final List<LobbyMessage> lobby = new CopyOnWriteArrayList<>();
    private final Map<Long, Pair<Long, Long>> banned = new ConcurrentHashMap<>();
    private int lastId;

    public Lobby(Function<String, List<Account>> onGetAccount) {
        this.onGetAccount = onGetAccount;
    }

    public List<LobbyMessage> readMainLobby() {
        return new CopyOnWriteArrayList<>(lobby);
    }

    /**
     * Supports ban command by login
     * /ban time [m,h] login OR
     * /ban login (30 minutes)
     * */
    public RequestStatus sendLobby(Account account, String message) {
        if (message.startsWith("/")) {
            RequestStatus status = sendCommand(account, message);
            updateLobbySize();
            return status;
        }

        if (banned.containsKey(account.getId())) return RequestStatus.BANNED;
        String color = account.getType().ordinal() >= AccountType.MODERATOR.ordinal() ? "[RED]" : "";
        lobby.add(new LobbyMessage(generateId(), account.getId(), color + "[_]" + account.getFullName() + "[_]: " + message));
        updateLobbySize();
        return RequestStatus.DONE;
    }



    public void sendConnect(Account account) {
        lobby.add(new LobbyMessage(generateId(), SERVER_USER_ID, "connect " + account.getFullName()));
        updateLobbySize();
    }

    public void sendDisconnect(Account account) {
        lobby.add(new LobbyMessage(generateId(), SERVER_USER_ID, "disconnect " + account.getFullName()));
        updateLobbySize();
    }

    public void sendJoin(Account account, String pieceColor) {
        lobby.add(new LobbyMessage(generateId(), SERVER_USER_ID, "join " + account.getFullName() + " " + pieceColor));
        updateLobbySize();
    }

    public void sendDisjoin(Account account) {
        lobby.add(new LobbyMessage(generateId(), SERVER_USER_ID, "disjoin " + account.getFullName()));
        updateLobbySize();
    }

    public void sendStart(Account account) {
        lobby.add(new LobbyMessage(generateId(), SERVER_USER_ID,"start " + account.getFullName()));
        updateLobbySize();
    }

    /**
     * Updates the status of blocked users
     * */
    public void updateTime() {
        for (Map.Entry<Long, Pair<Long, Long>> entry : banned.entrySet()) {
            Pair<Long, Long> timeBan = entry.getValue();
            if (System.currentTimeMillis() - timeBan.getKey() > timeBan.getValue()) {
                banned.remove(entry.getKey());
            }
        }
    }

    private void sendBan(Account mod, Account banned, long time) {
        lobby.add(new LobbyMessage(generateId(), SERVER_USER_ID, "banned " + mod.getFullName() + " " + banned.getFullName() + " " + time));
        updateLobbySize();
    }

    private void updateLobbySize() {
        if (lobby.size() > MAX_LOBBY_SIZE) {
            lobby.subList(0, lobby.size() - MAX_LOBBY_SIZE).clear();
        }
    }

    private long generateId() {
        if (lastId == MAX_ID) lastId = 0;
        return lastId++;
    }

    private RequestStatus sendCommand(Account account, String command) {
        String[] tokens = command.split(" ");
        switch (tokens[0]) {

            case "/ban": {

                if (tokens.length == 2) {
                    return ban(account, tokens[1], 1000 * 60 * 30);

                } else if (tokens.length == 3) {

                    if (tokens[2].length() < 2) return RequestStatus.INCORRECT_DATA;
                    long unitSize = getUnitSize(tokens[2].substring(tokens[2].length() - 1));
                    String time = tokens[2].substring(0, tokens[2].length() - 1);
                    if (time.length() > 5) return RequestStatus.INCORRECT_DATA;

                    if (!INTEGER_PATTERN.matcher(time).matches() || unitSize == -1) {
                        return RequestStatus.INCORRECT_DATA;
                    }
                    return ban(account, tokens[1], Long.parseLong(time) * unitSize);

                } else {
                    return RequestStatus.INCORRECT_DATA;
                }
            }
        }
        return RequestStatus.NOT_FOUND_COMMAND;
    }

    private RequestStatus ban(Account account, String login, long timeMillis) {
        List<Account> accounts = onGetAccount.apply(login);
        if (DataChecks.isBadList(accounts)) {
            return DataChecks.getBadStatus(accounts);
        }
        Account loginAccount = accounts.get(0);
        if (loginAccount.getType().ordinal() >= account.getType().ordinal()) {
            return RequestStatus.DENIED;
        }
        if (timeMillis > MAX_LOBBY_BANNED_TIME) {
            return RequestStatus.INCORRECT_DATA;
        }

        banned.put(loginAccount.getId(), new Pair<>(System.currentTimeMillis(), timeMillis));
        sendBan(account, loginAccount, timeMillis);
        return RequestStatus.DONE;
    }

    private long getUnitSize(String unit) {
        switch (unit) {
            case "m": return 1000 * 60;
            case "h": return 1000 * 60 * 60;
        }
        return -1;
    }
}
