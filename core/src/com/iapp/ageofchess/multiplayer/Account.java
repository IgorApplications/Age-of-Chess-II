package com.iapp.ageofchess.multiplayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Account {

    public static final Account NONE = new Account();

    private long id = -1;

    private long coins = 100;
    private double bullet = 1_000, blitz = 1_000,
            rapid = 1_000, longRank = 1_000;
    private byte[] avatar;
    private String name;
    private String userName;
    private String password;
    private String country = "n/d";
    private Gender gender = Gender.ND;
    private String quote = "";
    private long dateBirth;
    private AccountType type = AccountType.USER;
    private final List<Login> logins = new ArrayList<>();
    private final List<Punishment> punishments = new ArrayList<>();
    private boolean onlineNow;

    public Account() {}

    public Account(Account account) {
        id = account.id;
        avatar = account.avatar;
        name = account.name;
        userName = account.userName;
        password = account.password;
        country = account.country;
        gender = account.gender;
        quote = account.quote;
        dateBirth = account.dateBirth;
        type = account.type;
        logins.addAll(account.logins);
        punishments.addAll(account.punishments);
        onlineNow = account.onlineNow;
        coins = account.coins;
        bullet = account.bullet;
        blitz = account.blitz;
        rapid = account.rapid;
        longRank = account.longRank;
    }

    public Account(long id, long coins, double bullet, double blitz, double rapid, double longRank,
                   String name, String password, String userName, String country,
                   Gender gender, long dateBirth) {
        this.id = id;
        this.coins = coins;
        this.bullet = bullet;
        this.blitz = blitz;
        this.rapid = rapid;
        this.longRank = longRank;
        this.name = name;
        this.password = password;
        this.userName = userName;
        this.country = country;
        this.gender = gender;
        this.dateBirth = dateBirth;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public double getBullet() {
        return bullet;
    }

    public void setBullet(double bullet) {
        this.bullet = bullet;
    }

    public double getBlitz() {
        return blitz;
    }

    public void setBlitz(double blitz) {
        this.blitz = blitz;
    }

    public double getRapid() {
        return rapid;
    }

    public void setRapid(double rapid) {
        this.rapid = rapid;
    }

    public double getLongRank() {
        return longRank;
    }

    public void setLongRank(double longRank) {
        this.longRank = longRank;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public List<Login> getLogins() {
        return logins;
    }

    public List<Punishment> getPunishments() {
        return punishments;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return userName;
    }

    public void setFullName(String userName) {
        this.userName = userName;
    }

    //

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public long getDateBirth() {
        return dateBirth;
    }

    public void setDateBirth(long dateBirth) {
        this.dateBirth = dateBirth;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public boolean isOnlineNow() {
        return onlineNow;
    }

    public void setOnlineNow(boolean onlineNow) {
        this.onlineNow = onlineNow;
    }


    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", avatar=" + Arrays.toString(avatar) +
                ", name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", country='" + country + '\'' +
                ", gender=" + gender +
                ", quote='" + quote + '\'' +
                ", dateBirth=" + dateBirth +
                ", type=" + type +
                ", logins=" + logins +
                ", punishments=" + punishments +
                ", onlineNow=" + onlineNow +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id);
        result = 31 * result + Arrays.hashCode(avatar);
        return result;
    }
}
