package com.iapp.ageofchess.server.dao;

import com.iapp.lib.util.DataChecks;
import com.iapp.lib.util.Pair;
import com.iapp.lib.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Account database management
 * @author Igor Ivanov
 * */
@Component
public class AccountDAO {

    private static final Logger accountLogger = LoggerFactory.getLogger(AccountDAO.class);

    private final MetaDAO metaDAO;
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder cipher;

    /** top lists in different rank modes */
    private volatile List<Account> bulletTop = new ArrayList<>();
    private volatile List<Account> blitzTop = new ArrayList<>();
    private volatile List<Account> rapidTop = new ArrayList<>();
    private volatile List<Account> longTop = new ArrayList<>();

    public AccountDAO(JdbcTemplate jdbcTemplate) {
        metaDAO = new MetaDAO(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
        cipher = new BCryptPasswordEncoder(12);
    }

    /**
     * update all tops from the database
     * */
    public void updateTop() {

        bulletTop = jdbcTemplate.query("SELECT * FROM account ORDER BY bullet DESC FETCH FIRST 50 ROWS WITH TIES",
                new AccountMapper());

        blitzTop = jdbcTemplate.query("SELECT * FROM account ORDER BY blitz DESC FETCH FIRST 50 ROWS WITH TIES",
                new AccountMapper());

        rapidTop = jdbcTemplate.query("SELECT * FROM account ORDER BY rapid DESC FETCH FIRST 50 ROWS WITH TIES",
                new AccountMapper());

        longTop = jdbcTemplate.query("SELECT * FROM account ORDER BY long DESC FETCH FIRST 50 ROWS WITH TIES",
                new AccountMapper());
    }

    /**
     * Get the latest updated bullet mode top
     * @see AccountDAO#updateTop()
     * */
    public Pair<RequestStatus, List<Account>> getBulletTop() {
        RequestStatus status = updateAccounts(bulletTop);
        if (status != RequestStatus.DONE) return new Pair<>(status, null);
        return new Pair<>(RequestStatus.DONE, bulletTop);
    }

    /**
     * Get the latest updated blitz mode top
     * @see AccountDAO#updateTop()
     * */
    public Pair<RequestStatus, List<Account>> getBlitzTop() {
        RequestStatus status = updateAccounts(blitzTop);
        if (status != RequestStatus.DONE) return new Pair<>(status, null);
        return new Pair<>(RequestStatus.DONE, blitzTop);
    }

    /**
     * Get the latest updated rapid mode top
     * @see AccountDAO#updateTop()
     * */
    public Pair<RequestStatus, List<Account>> getRapidTop() {
        RequestStatus status = updateAccounts(rapidTop);
        if (status != RequestStatus.DONE) return new Pair<>(status, null);
        return new Pair<>(RequestStatus.DONE, rapidTop);
    }

    /**
     * Get the latest updated long mode top
     * @see AccountDAO#updateTop()
     * */
    public Pair<RequestStatus, List<Account>> getLongTop() {
        RequestStatus status = updateAccounts(longTop);
        if (status != RequestStatus.DONE) return new Pair<>(status, null);
        return new Pair<>(RequestStatus.DONE, longTop);
    }

    /**
     * searches for all accounts whose username
     * contains an argument
     * */
    public Pair<RequestStatus, List<Account>> searchAccounts(String partName) {
        List<Account> searched = jdbcTemplate.query("SELECT * FROM account WHERE username LIKE "
                + String.format("'%%%s%%'", partName), new AccountMapper());
        RequestStatus status = updateAccounts(searched);
        if (status != RequestStatus.DONE) return new Pair<>(status, null);
        return new Pair<>(status, searched);
    }

    /**
     * returns the avatar in the pair value
     * RequestStatus.DONE - if successful and value != null
     * */
    public Pair<RequestStatus, byte[]> getAvatar(long id) {
        List<byte[]> result = metaDAO.getAvatar(id);
        if (DataChecks.isBadList(result)) {
            return new Pair<>(DataChecks.getBadStatus(result), null);
        }
        return new Pair<>(RequestStatus.DONE, result.get(0));
    }

    /**
     * updates the avatar
     * RequestStatus.DONE - if successful and value != null
     * */
    public RequestStatus updateAvatar(Account account, AccountType sender, boolean self, byte[] avatar) {
        if (!self && (sender.ordinal() <= account.getType().ordinal()
                || sender.ordinal() < AccountType.EXECUTOR.ordinal())) {
            return RequestStatus.DENIED;
        }

        return metaDAO.updateAvatar(account.getId(), avatar);
    }

    /**
     * adds a new punishment to the account.
     * If it applies to yourself, or you are not a higher role than the punished - DENIED
     * */
    public RequestStatus addPunishment(Account account, AccountType sender, boolean self, Punishment punishment) {
        if (self || sender.ordinal() <= account.getType().ordinal()) {
            return RequestStatus.DENIED;
        }

        return metaDAO.addPunishment(account.getId(), punishment);
    }

    /**
     * checks the password, if correct,
     * then updates the metadata and returns the DONE status
     * */
    public Pair<RequestStatus, Account> login(String name, String password, Login login) {

        List<Account> accounts = getServerAccount(name);
        if (DataChecks.isBadList(accounts)) return new Pair<>(DataChecks.getBadStatus(accounts),null);
        Account account = accounts.get(0);
        if (!cipher.matches(password, account.getPassword())) return new Pair<>(RequestStatus.DENIED, null);

        RequestStatus status = metaDAO.addLogin(account.getId(), login);
        if (status != RequestStatus.DONE) return new Pair<>(status, null);

        return getAccount(account.getId());
    }

    /**
     * returns all data from the database
     * */
    public List<Account> getAllData() {
        throw new UnsupportedOperationException();
        // TODO
        // return jdbcTemplate.query("SELECT * FROM Account", new AccountMapper());
    }

    /**
     * @param id - accountId
     * returns a ready account
     * */
    public Pair<RequestStatus, Account> getAccount(long id) {
        List<Account> accounts = getServerAccount(id);
        return updateGettingAccount(accounts);
    }

    /**
     * Creates a new account (signup), checks the name for uniqueness (as it is a login),
     * generates a unique identifier
     * @return
     * RequestStatus.EXISTS - if such a login already exists;
     * RequestStatus.DONE - if successful;
     * RequestStatus.INCORRECT - if contains non-asci characters
     * */
    public RequestStatus signup(String name, String userName, String password) {
        if (containsName(name)) return RequestStatus.EXISTS;
        if (!isASCII(name)) return RequestStatus.INCORRECT_DATA;

        long newId = getNewID();
        jdbcTemplate.update("INSERT INTO Account VALUES" +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                newId, name, userName, cipher.encode(password),
                "", Gender.ND.toString(), "", 0,
                AccountType.USER.toString(), 100, 1_000, 1_000,
                1_000, 1_000, new byte[0], "[]", "[]", System.currentTimeMillis());


        return RequestStatus.DONE;
    }

    /**
     * The server updates the account if necessary
     * @param account - updated account
     * */
    public void updateServerAccount(Account account) {
        jdbcTemplate.update(
                "UPDATE Account SET coins=?, bullet=?, blitz=?, rapid=?, long=?, name=?, username=?," +
                        " country=?, gender=?, quote=?, datebirth=? WHERE id=?",
                account.getCoins(), account.getBullet(), account.getBlitz(),
                account.getRapid(), account.getLongRank(),
                account.getUsername(), account.getFullName(),
                account.getCountry(), account.getGender().toString(),
                account.getQuote(), account.getDateBirth(),
                account.getId());
    }

    /**
     * Updates an account
     * @param account - updated account
     * @param sender - the role of the account that is requesting this action
     * @param self - whether the account belongs to the person requesting this action
     * @return
     * RequestStatus.DENIED if sender <= account.getType() or unknown role
     * RequestStatus.SECURITY_BREACH if there are several such accounts
     * RequestStatus.DONE - if successful
     * */
    public RequestStatus updateAccount(Account account, AccountType sender, boolean self) {
        // attempt to change someone else's account if your role is smaller or the same
        if (!self && account.getType().ordinal() >= sender.ordinal()) return RequestStatus.DENIED;
        // applying to oneself a role higher than one's own
        if (self && sender.ordinal() < account.getType().ordinal()) return RequestStatus.DENIED;

        List<Account> found = getServerAccount(account.getId());
        if (DataChecks.isBadList(found)) return DataChecks.getBadStatus(found);

        if (sender == AccountType.DEVELOPER) {

            if (self) {

                if (account.getPassword().equals("")) {
                    jdbcTemplate.update(
                            "UPDATE Account SET coins=?, bullet=?, blitz=?, rapid=?, long=?, name=?, username=?," +
                                    " country=?, gender=?, quote=?, datebirth=? WHERE id=?",

                            account.getCoins(), account.getBullet(), account.getBlitz(),
                            account.getRapid(), account.getLongRank(),
                            account.getUsername(), account.getFullName(),
                            account.getCountry(), account.getGender().toString(),
                            account.getQuote(), account.getDateBirth(),
                            account.getId());
                } else {
                    jdbcTemplate.update(
                            "UPDATE Account SET coins=?, bullet=?, blitz=?, rapid=?, long=?, name=?, username=?," +
                                    " password=?, country=?, gender=?, quote=?, datebirth=? WHERE id=?",

                            account.getCoins(), account.getBullet(), account.getBlitz(),
                            account.getRapid(), account.getLongRank(),
                            account.getUsername(),
                            account.getFullName(), cipher.encode(account.getPassword()),
                            account.getCountry(), account.getGender().toString(),
                            account.getQuote(), account.getDateBirth(),
                            account.getId());
                }

            } else {

                if (account.getPassword().equals("")) {
                    jdbcTemplate.update(
                        "UPDATE Account SET coins=?, bullet=?, blitz=?, rapid=?, long=?, name=?, username=?," +
                            " country=?, gender=?, quote=?, datebirth=?, " +
                            " type=? WHERE id=?",

                        account.getCoins(), account.getBullet(), account.getBlitz(),
                        account.getRapid(), account.getLongRank(),
                        account.getUsername(), account.getFullName(),
                        account.getCountry(), account.getGender().toString(),
                        account.getQuote(), account.getDateBirth(),
                        account.getType().toString(), account.getId());
                } else {
                    jdbcTemplate.update(
                        "UPDATE Account SET coins=?, bullet=?, blitz=?, rapid=?, long=?, name=?, username=?," +
                            " password=?, country=?, gender=?, quote=?, datebirth=?, " +
                            " type=? WHERE id=?",

                        account.getCoins(), account.getBullet(), account.getBlitz(),
                        account.getRapid(), account.getLongRank(),
                        account.getUsername(),
                        account.getFullName(), cipher.encode(account.getPassword()),
                        account.getCountry(), account.getGender().toString(),
                        account.getQuote(), account.getDateBirth(),
                        account.getType().toString(), account.getId());
                }

            }

        } else if (sender == AccountType.EXECUTOR) {

            if (self) {

                if (account.getPassword().equals("")) {
                    jdbcTemplate.update(
                            "UPDATE Account SET coins=?, bullet=?, blitz=?, rapid=?, long=?, name=?, username=?," +
                                    " country=?, gender=?, quote=?, datebirth=? WHERE id=?",

                            account.getCoins(), account.getBullet(), account.getBlitz(),
                            account.getRapid(), account.getLongRank(),
                            account.getUsername(), account.getFullName(),
                            account.getCountry(), account.getGender().toString(),
                            account.getQuote(), account.getDateBirth(), account.getId());
                } else {
                    jdbcTemplate.update(
                            "UPDATE Account SET coins=?, bullet=?, blitz=?, rapid=?, long=?, name=?, username=?," +
                                    " password=?, country=?, gender=?, quote=?, datebirth=? WHERE id=?",

                            account.getCoins(), account.getBullet(), account.getBlitz(),
                            account.getRapid(), account.getLongRank(),
                            account.getUsername(),
                            account.getFullName(), cipher.encode(account.getPassword()),
                            account.getCountry(), account.getGender().toString(),
                            account.getQuote(), account.getDateBirth(), account.getId());
                }

            } else {

                jdbcTemplate.update(
                        "UPDATE Account SET coins=?, bullet=?, blitz=?, rapid=?, long=?, name=?, username=?," +
                                " country=?, gender=?, quote=?, datebirth=?, type=? WHERE id=?",

                        account.getCoins(), account.getBullet(), account.getBlitz(),
                        account.getRapid(), account.getLongRank(),
                        account.getUsername(), account.getFullName(),
                        account.getCountry(), account.getGender().toString(),
                        account.getQuote(), account.getDateBirth(),
                        account.getType().toString(), account.getId());

            }

        } else if (self) { // Moderator == self

            if (account.getPassword().equals("")) {
                jdbcTemplate.update(
                        "UPDATE Account SET country=?, gender=?, quote=?, datebirth=? WHERE id=?",
                        account.getCountry(), account.getGender().toString(),
                        account.getQuote(), account.getDateBirth(), account.getId());
            } else {
                jdbcTemplate.update(
                        "UPDATE Account SET password=?, country=?, gender=?, quote=?, datebirth=? WHERE id=?",
                        cipher.encode(account.getPassword()),
                        account.getCountry(), account.getGender().toString(),
                        account.getQuote(), account.getDateBirth(), account.getId());
            }

        } else {
            return RequestStatus.DENIED;
        }

        return RequestStatus.DONE;
    }

    public RequestStatus makeInactive(Account sender, Account punishable, long punishmentId) {
        if (sender.getType().ordinal() <= punishable.getType().ordinal()) return RequestStatus.DENIED;
        List<List<Punishment>> punishments = metaDAO.getPunishments(punishable.getId());
        for (List<Punishment> list : punishments) {
            if (DataChecks.isBadList(list)) return DataChecks.getBadStatus(list);
            Punishment punishment = list.get(0);
            if (punishment.getId() == punishmentId) {
                punishment.setActive(false);
                return RequestStatus.DONE;
            }
        }

        return RequestStatus.NOT_FOUND;
    }

    public RequestStatus punish(Account sender, Account punishable, Punishment punishment) {
        if (punishable.getType().ordinal() >= sender.getType().ordinal()) {
            return RequestStatus.DENIED;
        }
        metaDAO.addPunishment(punishable.getId(), punishment);

        return RequestStatus.DONE;
    }

    /**
     * returns an account directly from the database,
     * contains only permanent data, such as: name, password and other data from the table
     * Cannot be returned to the client, as there is no online status!
     * @param name - account name
     * */
    public List<Account> getServerAccount(String name) {
        return jdbcTemplate.query("SELECT * FROM ACCOUNT WHERE name=?",
            new Object[]{name}, new AccountMapper());
    }

    /**
     * returns an account directly from the database,
     * contains only permanent data, such as: name, password and other data from the table
     * Cannot be returned to the client, as there is no online status!
     * @param id - account id
     * */
    public List<Account> getServerAccount(long id) {
        return jdbcTemplate.query("SELECT * FROM ACCOUNT WHERE id=?",
            new Object[]{id}, new AccountMapper());
    }

    private long getNewID() {
        return jdbcTemplate.query("SELECT * FROM Account ORDER BY id DESC LIMIT 1", new AccountMapper())
                .stream().map(Account::getId).findFirst().orElse(-1L) + 1;
    }

    private Pair<RequestStatus, Account> updateGettingAccount(List<Account> accounts) {
        if (DataChecks.isBadList(accounts)) return new Pair<>(DataChecks.getBadStatus(accounts), null);
        Account account = accounts.get(0);

        Pair<RequestStatus, Login> lastLogin = metaDAO.getLastLogin(account.getId());
        if (lastLogin.getKey() != RequestStatus.DONE) return new Pair<>(lastLogin.getKey(), null);
        Pair<RequestStatus, Set<Flag>> flags = metaDAO.getFlags(account.getId());
        if (flags.getKey() != RequestStatus.DONE) return new Pair<>(flags.getKey(), null);

        account.setLogin(lastLogin.getValue());
        account.getFlags().addAll(flags.getValue());
        account.setPassword("");

        return new Pair<>(RequestStatus.DONE, account);
    }

    private RequestStatus updateAccounts(List<Account> accounts) {
        for (Account account : accounts) {
            RequestStatus status = updateAccount(account);
            if (status != RequestStatus.DONE) {
                return status;
            }
        }
        return RequestStatus.DONE;
    }

    private RequestStatus updateAccount(Account account) {
        Pair<RequestStatus, Login> lastLogin = metaDAO.getLastLogin(account.getId());
        if (lastLogin.getKey() != RequestStatus.DONE) return lastLogin.getKey();
        Pair<RequestStatus, Set<Flag>> flags = metaDAO.getFlags(account.getId());
        if (flags.getKey() != RequestStatus.DONE) return flags.getKey();

        account.setLogin(lastLogin.getValue());
        account.getFlags().clear();
        account.getFlags().addAll(flags.getValue());
        account.setPassword("");

        return RequestStatus.DONE;
    }

    private boolean containsName(String name) {
        List<Account> accounts = jdbcTemplate.query("SELECT * FROM ACCOUNT WHERE name=?",
                new Object[]{name}, new AccountMapper());
        return !accounts.isEmpty();
    }

    private boolean isASCII(String s) {
        for (char c : s.toCharArray()) {
            if (c > 127) return false;
        }
        return true;
    }
}
