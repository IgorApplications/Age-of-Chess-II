package com.iapp.ageofchess.server.controllers;

import com.google.gson.Gson;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.Login;
import com.iapp.lib.web.Punishment;
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
 * Account management
 * @author Igor Ivanov
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

    /** register a new account */
    public RequestStatus signup(String name, String userName, String password) {
        return accountDAO.signup(name, userName, password);
    }

    /** sign in account */
    public Pair<RequestStatus, Account> login(String name, String password, Login login) {
        Pair<RequestStatus, Account> acc = accountDAO.login(name, password, login);
        if (acc.getKey() == RequestStatus.DONE) acc.getValue().setPassword("");
        return acc;
    }

    /** get account information without a password */
    public Pair<RequestStatus, Account> see(long id) {
        return accountDAO.getAccount(id);
    }

    /** get information about multiple accounts, without a password */
    public Pair<RequestStatus, List<Account>> seeAccounts(long ids[]) {
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

    /** try to find accounts whose name contains the specified string */
    public Pair<RequestStatus, List<Account>> searchAccounts(String partName) {
        return accountDAO.searchAccounts(partName);
    }

    /**
     * forced update of the top of the best players in all ratings
     * Beware it can be very costly! call rarely
     * */
    public void update() {
        accountDAO.updateTop();
    }

    /**
     * Get the latest updated bullet mode top
     * @see AccountController#update()
     * */
    public Pair<RequestStatus, List<Account>> getBulletTop() {
        return accountDAO.getBulletTop();
    }

    /**
     * Get the latest updated top in blitz mode
     * @see AccountController#update()
     * */
    public Pair<RequestStatus, List<Account>> getBlitzTop() {
        return accountDAO.getBlitzTop();
    }

    /**
     * Get the latest updated top in rapid mode
     * @see AccountController#update()
     * */
    public Pair<RequestStatus, List<Account>> getRapidTop() {
        return accountDAO.getRapidTop();
    }

    /**
     * Get the latest updated top in long mode
     * @see AccountController#update()
     * */
    public Pair<RequestStatus, List<Account>> getLongTop() {
        return accountDAO.getLongTop();
    }

    // auth only ------------------------------------------------------------------------------------------------------

    /**
     * remove activity from punishment, will not affect the account,
     * but will remain in the history
     * */
    public RequestStatus makeInactive(Account auth, long punishableId, long punishmentId) {
        Pair<RequestStatus, Account> pair = see(punishableId);
        if (pair.getKey() != RequestStatus.DONE) return pair.getKey();
        return accountDAO.makeInactive(auth, pair.getValue(), punishmentId);
    }

    /**
     * add a new punishment to the account
     * */
    public RequestStatus punish(Account auth, long punishableId, Punishment punishment) {
        Pair<RequestStatus, Account> pair = see(punishableId);
        if (pair.getKey() != RequestStatus.DONE) return pair.getKey();
        return accountDAO.punish(auth, pair.getValue(), punishment);
    }

    /**
     * update account avatar in binary form
     * */
    public RequestStatus updateAvatar(Account auth, long updatedId, byte[] avatar) {
        Pair<RequestStatus, Account> updated = see(updatedId);
        if (updated.getKey() != RequestStatus.DONE) return updated.getKey();
        return accountDAO.updateAvatar(updated.getValue(), auth.getType(),auth.getId() == updatedId, avatar);
    }

    /**
     * get account avatar in binary form
     * */
    public Pair<RequestStatus, byte[]> getAvatar(long id) {
        return accountDAO.getAvatar(id);
    }

    /**
     * Update account, special features available for moderators,
     * blocks dangerous activities
     * */
    public RequestStatus change(Account auth, String updated) {
        Account updatedAcc = gson.fromJson(updated, Account.class);

        return accountDAO.updateAccount(updatedAcc, auth.getType(),
                auth.getId() == updatedAcc.getId());
    }

    // developer authority --------------------------------------------------------------------------------------------
    /**
     * returns all accounts from the database with encrypted passwords
     * */
    public List<Account> getAllData() {
        return accountDAO.getAllData();
    }
}
