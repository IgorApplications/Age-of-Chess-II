package com.iapp.ageofchess.server.controllers;

import com.google.gson.Gson;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.AccountType;
import com.iapp.lib.web.Message;
import com.iapp.lib.web.RequestStatus;
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
 * */
@RestController
@RequestMapping("/api/v1/mainChat")
public class MainChatController {

    private static final Logger mainChatLogger = LoggerFactory.getLogger(MainChatController.class);

    private final MainChatDAO mainChatDAO;
    private final AccountDAO accountDAO;
    private final Gson gson;

    @Autowired
    public MainChatController(MainChatDAO mainChatDAO, AccountDAO accountDAO) {
        this.mainChatDAO = mainChatDAO;
        this.accountDAO = accountDAO;
        gson = new Gson();
    }

    // no auth ------------------------------------------------------------------------------------------------------

    public Pair<List<Message>, Map<Long, Account>> readAll() {
        List<Message> messages = mainChatDAO.readMessages();
        Set<Long> ids = new HashSet<Long>();
        for (Message message : messages) ids.add(message.getSenderId());
        Map<Long, Account> idByAcc = new HashMap<>();

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
