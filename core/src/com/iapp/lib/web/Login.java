package com.iapp.lib.web;

import java.util.Date;

public class Login {

    private String ip;
    private long time;
    private String localeAppLang, localeSysLang, localeSysCountry, system;

    public Login(String ip, String localeAppLang, String localeSysLang,
                 String localeSysCountry, String system) {
        this.ip = ip;
        this.time = new Date().getTime();
        this.localeAppLang = localeAppLang;
        this.localeSysLang = localeSysLang;
        this.localeSysCountry = localeSysCountry;
        this.system = system;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLocaleAppLang() {
        return localeAppLang;
    }

    public void setLocaleAppLang(String localeAppLang) {
        this.localeAppLang = localeAppLang;
    }

    public String getLocaleSysLang() {
        return localeSysLang;
    }

    public void setLocaleSysLang(String localeSysLang) {
        this.localeSysLang = localeSysLang;
    }

    public String getLocaleSysCountry() {
        return localeSysCountry;
    }

    public void setLocaleSysCountry(String localeSysCountry) {
        this.localeSysCountry = localeSysCountry;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    @Override
    public String toString() {
        return "Login{" +
                "ip='" + ip + '\'' +
                ", time=" + time +
                '}';
    }
}
