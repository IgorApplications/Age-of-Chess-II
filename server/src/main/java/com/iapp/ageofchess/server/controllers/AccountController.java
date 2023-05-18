package com.iapp.ageofchess.server.controllers;

import com.google.gson.Gson;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.Login;
import com.iapp.lib.web.RequestStatus;
import com.iapp.ageofchess.server.dao.AccountDAO;
import com.iapp.lib.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Rest account controller
 * @author Igor Ivanov
 * @version 1.0
 * */
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private static final Logger accountLogger = LoggerFactory.getLogger(AccountController.class);

    private final AccountDAO accountDAO;
    private final Gson gson;

    @Autowired
    public AccountController(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
        gson = new Gson();
    }

    // no auth -------------------------------------------------------------------------------------------------------

    public RequestStatus signup(String name, String userName, String password) {
        return accountDAO.signup(name, userName, password);
    }

    public Pair<RequestStatus, Account> login(String name, String password, Login login) {
        Pair<RequestStatus, Account> acc = accountDAO.login(name, password, login);
        if (acc.getKey() == RequestStatus.DONE) acc.getValue().setPassword("");
        return acc;
    }

    public Pair<RequestStatus, Account> see(long id) {
        return accountDAO.getAccount(id);
    }

    public Pair<RequestStatus, List<Account>> seeAccounts(String stringIds) {
        long[] ids = gson.fromJson(stringIds, long[].class);

        List<Account> general = new ArrayList<>();
        for (long id : ids) {


            Pair<RequestStatus, Account> accounts = accountDAO.getAccount(id);
            if (accounts.getKey() != RequestStatus.DONE) {
                accountLogger.error("seeAccounts get Account by id, status = " + accounts.getKey());
                continue;
            }

            general.add(accounts.getValue());
        }

        return new Pair<>(RequestStatus.DONE, general);
    }

    public Pair<RequestStatus, List<Account>> searchAccounts(String partName) {
        return accountDAO.searchAccounts(partName);
    }

    public void updateTop() {
        accountDAO.updateTop();
    }

    public Pair<RequestStatus, List<Account>> getBulletTop() {
        return accountDAO.getBulletTop();
    }

    public Pair<RequestStatus, List<Account>> getBlitzTop() {
        return accountDAO.getBlitzTop();
    }

    public Pair<RequestStatus, List<Account>> getRapidTop() {
        return accountDAO.getRapidTop();
    }

    public Pair<RequestStatus, List<Account>> getLongTop() {
        return accountDAO.getLongTop();
    }

    // auth only ------------------------------------------------------------------------------------------------------

    public RequestStatus updateAvatar(Account auth, long updatedId, byte[] avatar) {
        Pair<RequestStatus, Account> updated = see(updatedId);
        if (updated.getKey() != RequestStatus.DONE) return updated.getKey();
        return accountDAO.updateAvatar(updated.getValue(), auth.getType(),auth.getId() == updatedId, avatar);
    }

    public Pair<RequestStatus, byte[]> getAvatar(long id) {
        return accountDAO.getAvatar(id);
    }

    public RequestStatus change(Account auth, String updated) {
        Account updatedAcc = gson.fromJson(updated, Account.class);

        return accountDAO.updateAccount(updatedAcc, auth.getType(),
                auth.getId() == updatedAcc.getId());
    }

    // developer authority --------------------------------------------------------------------------------------------
    public List<Account> getAllData() {
        return accountDAO.getAllData();
    }
}
