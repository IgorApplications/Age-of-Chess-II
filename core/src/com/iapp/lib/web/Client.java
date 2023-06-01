package com.iapp.lib.web;

import java.util.List;
import java.util.function.Consumer;

public interface Client {

    void requireGameLobbyList(long matchId, Consumer<List<LobbyMessage>> onGameLobbyList);

    void requireMainLobbyList(Consumer<List<LobbyMessage>> onMainLobbyList);

    void sendMainLobbyMessage(String message);

    void sendGameLobbyMessage(long matchId, String message);

    void getAvatar(Account account, Consumer<byte[]> getAvatar);
}
