package com.cab404.wifu.modules;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.cab404.wifu.base.WifiLoginModule;

/**
 * Sorry for no comments!
 * Created at 10:25 on 28/11/15
 *
 * @author cab404
 */
public class MgupiConnect implements WifiLoginModule {
    @Override
    public boolean handle(WifiContextInfo info, Log log) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("http://1.1.1.10/login.html").openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.addRequestProperty("Content-Length", "92");
            connection.addRequestProperty("Location", "http://1.1.1.10/login.html?redirect=ya.ru/");
            PrintWriter in = new PrintWriter(connection.getOutputStream());
            in.write("buttonClicked=4&err_flag=0&err_msg=&info_flag=0&info_msg=&redirect_url=http%3A%2F%2Fya.ru%2F");
            in.flush();
            connection.connect();
            /* Waiting for response */
            connection.getResponseCode();
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    @Override
    public long repeatDelay() {
        return 60000;
    }

    @Override
    public int canHandle(WifiContextInfo info) {
        return "MGUPI-WiFi".equals(info.ssid()) ? 100 : 0;
    }
}
