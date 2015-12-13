package com.cab404.wifu.util;

import com.cab404.wifu.base.WifiLoginModule;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:28 on 01/12/15
 *
 * @author cab404
 */
public class WifiInfo implements WifiLoginModule.WifiContextInfo {

    public String bssid;
    public String ssid;
    public String mac;

    public WifiInfo(String bssid, String ssid, String mac) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.mac = mac;
    }

    @Override
    public String bssid() {
        return bssid;
    }

    @Override
    public String ssid() {
        return ssid;
    }

    @Override
    public String mac() {
        return mac;
    }

}
