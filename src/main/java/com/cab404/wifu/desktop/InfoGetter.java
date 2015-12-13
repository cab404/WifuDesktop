package com.cab404.wifu.desktop;

import com.cab404.wifu.base.WifiLoginModule;
import com.cab404.wifu.util.WifiInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by cab404 on 13.12.15.
 */
public class InfoGetter {


    public WifiLoginModule.WifiContextInfo getCurrentContext() throws IOException {
        WifiInfo info = new WifiInfo(null, null, null);
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux"))
            fillLinuxInfo(info);
        if (os.contains("windows"))
            fillWindowsInfo(info);

        return info;
    }

    void fillWindowsInfo(WifiInfo info) throws IOException {
        Process process = new ProcessBuilder("netsh", "wlan", "show", "interfaces").start();
        BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = out.readLine()) != null) {
            if (line.contains("BSSID")) {
                info.bssid = line.substring(line.indexOf(':') + 1).trim();
            } else if (line.contains("SSID")) {
                info.ssid = line.substring(line.indexOf(':') + 1).trim();
            }
        }
    }

    void fillLinuxInfo(WifiInfo info) throws IOException {
        Process process = new ProcessBuilder("iwconfig").start();
        BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = out.readLine()) != null) {
            if (line.contains("ESSID")) {
                info.ssid = line.substring(line.indexOf(':') + 1).trim();
            }
            if (line.contains("Access Point")) {
                info.bssid = line.substring(line.indexOf("t:") + 2).trim();
            }
        }
        if (info.ssid.charAt(0) == '"'){
            info.ssid = info.ssid.substring(1, info.ssid.length() - 1);
        }
        if ("off/any".equals(info.ssid)){
            info.bssid = null;
            info.ssid = null;
        }

    }

}
