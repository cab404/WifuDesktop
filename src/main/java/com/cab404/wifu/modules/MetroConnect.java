package com.cab404.wifu.modules;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.cab404.wifu.base.WifiLoginModule;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 06:37 on 29/11/15
 *
 * @author cab404
 */
public class MetroConnect implements WifiLoginModule {

    @Override
    public boolean handle(WifiContextInfo info, Log log) {
        try {

            // Fetching redirect page
            HttpURLConnection o = (HttpURLConnection) new URL("http://ya.ru/404").openConnection();
            o.setUseCaches(false);
            o.setDefaultUseCaches(false);
            o.connect();
            o.getResponseCode();

            final String location = /*loginSite;*/o.getHeaderField("Location");
            if (location == null) return false;

            URL base = new URL(location);

            // Fetching main page with csrf code
            HttpURLConnection main_page = (HttpURLConnection) base.openConnection();
            main_page.addRequestProperty("Host", "login.wi-fi.ru");
            main_page.addRequestProperty("Cookie", "device=desktop");
            main_page.connect();

            // Because URLConnections are retarded
            try {
                main_page.getInputStream();
            } catch (IOException e) {
                main_page.getErrorStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(main_page.getErrorStream()));
            String next;

            while ((next = reader.readLine()) != null)
                if (next.contains("csrf.sign"))
                    break;

            String payload = next;
            if (payload == null) throw new RuntimeException("CSRF not found!");

            // Some assembly required, may (and will) break on template changes
            payload = "promogoto="
                    + payload
                    .replace("</form>", "")
                    .replace("\"/>", "")
                    .replace("<input type=\"hidden\" name=\"", "&")
                    .replace("\" value=\"", "=")
                    .trim();

            // Now checking out cookies
            StringBuilder cookie = new StringBuilder();
            for (String cookieLine : main_page.getHeaderFields().get("Set-Cookie"))
                for (String coo : cookieLine.split(","))
                    cookie.append("; ").append(coo.substring(0, coo.indexOf(';')).trim());

            cookie.delete(0, 2);

            // Requesting
            HttpURLConnection login = (HttpURLConnection) new URL(location).openConnection();
            login.setRequestMethod("POST");
            login.setDefaultUseCaches(false);
            login.setUseCaches(false);
            login.setDoOutput(true);
            login.setDoInput(true);

            // Regular params
            login.addRequestProperty("Host", "login.wi-fi.ru");
            login.setRequestProperty("Referer", location);

            // Some content related headers
            login.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            login.setRequestProperty("Content-Length", payload.length() + "");

            // And cookies
            login.setRequestProperty("Cookie", cookie.toString());

            // And payload
            PrintWriter writer = new PrintWriter(login.getOutputStream());
            writer.write(payload);
            writer.flush();
            login.connect();

            // IMPORTANT, BECAUSE URLCONNECTIONS SUCKS >:|
            // No, srsly, that forces wait for request to finish,
            // and prevents input stream errors
            login.getResponseCode();

            try {
                login.getInputStream();
            } catch (IOException e) {
                login.getErrorStream();
            }

            reader = new BufferedReader(new InputStreamReader(login.getInputStream()));
            while ((next = reader.readLine()) != null)
                if (next.contains("<title>"))
                    System.out.println(next);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public long repeatDelay() {
        return -1;
    }

    @Override
    public int canHandle(WifiContextInfo info) {
        return "MosMetro_Free".equals(info.ssid()) ? 100 : 0;
    }
}
