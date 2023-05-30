package com.iapp.ageofchess.server.controllers;

import com.iapp.lib.web.*;
import com.iapp.ageofchess.server.dao.AccountDAO;
import com.iapp.ageofchess.server.dao.MainChatDAO;
import com.iapp.lib.util.DataChecks;
import com.iapp.lib.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Rest controller of the main chat
 * @author Igor Ivanov
 * @version 1.0
 *
 * The lobby maintains the following server states:
 * connect username, disconnect username, banned admin_username username time(millis)
 * */
@RestController
@RequestMapping("/api/v1/mainChat")
public class MainChatController {

    private static final Logger mainChatLogger = LoggerFactory.getLogger(MainChatController.class);

    private final MainChatDAO mainChatDAO;
    private final AccountDAO accountDAO;
    private final Lobby lobby;

    @Autowired
    public MainChatController(MainChatDAO mainChatDAO, AccountDAO accountDAO) {
        this.mainChatDAO = mainChatDAO;
        this.accountDAO = accountDAO;
        lobby = new Lobby(accountDAO::getServerAccount);
    }

    // no auth ------------------------------------------------------------------------------------------------------
    public List<LobbyMessage> readMainLobby() {
        return lobby.readMainLobby();
    }

    public void update() {
        mainChatDAO.clearOldMessages();
        lobby.updateTime();
    }

    public Pair<List<Message>, Map<Long, Account>> readAll() {
        List<Message> messages = mainChatDAO.readMessages();
        Set<Long> ids = new HashSet<>();
        for (Message message : messages) ids.add(message.getSenderId());
        Map<Long, Account> idByAcc = new HashMap<>();
        messages.sort(Comparator.comparing(Message::getTime));

        for (long id : ids) {

            Pair<RequestStatus, Account> accounts = accountDAO.getAccount(id);
            if (accounts.getKey() != RequestStatus.DONE) {
                mainChatLogger.error("message get account by id, status = " + accounts.getKey());
                continue;
            }

            Account acc = accounts.getValue();
            idByAcc.put(id, acc);
        }

        return new Pair<>(messages, idByAcc);
    }

    // only auth -----------------------------------------------------------------------------------------------------
    public RequestStatus sendMainLobby(Account account, String message) {
        return lobby.sendLobby(account, message);
    }

    public void sendConnect(Account account) {
        lobby.sendConnect(account);
    }

    public void sendDisconnect(Account account) {
        lobby.sendDisconnect(account);
    }

    public RequestStatus send(long authId, String text) {
        Pair<RequestStatus, Account> pair = accountDAO.getAccount(authId);
        if (pair.getKey() != RequestStatus.DONE) return pair.getKey();
        mainChatDAO.send(pair.getValue(), text);

        return RequestStatus.DONE;
    }

    public RequestStatus remove(long authId, long messageId) {

        Pair<RequestStatus, Account> accounts = accountDAO.getAccount(authId);
        if (accounts.getKey() != RequestStatus.DONE) return accounts.getKey();
        // Only after sync in can we know that this is really the right user!
        Account sender = accounts.getValue();

        // moderator authority
        if (sender.getType().ordinal() >= AccountType.MODERATOR.ordinal()) {
            mainChatDAO.removeMessage(messageId);
            return RequestStatus.DONE;
        } else {
            List<Message> messages = mainChatDAO.getMessage(messageId);
            if (DataChecks.isBadList(messages)) return DataChecks.getBadStatus(messages);
            Message message = messages.get(0);

            // user authority and self message
            if (message.getSenderId() == sender.getId()) {
                mainChatDAO.removeMessage(messageId);
                return RequestStatus.DONE;
            }
        }

        return RequestStatus.DENIED;
    }
}
