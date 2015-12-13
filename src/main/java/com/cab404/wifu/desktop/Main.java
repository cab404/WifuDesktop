package com.cab404.wifu.desktop;

import com.cab404.wifu.base.WifiLoginModule;
import com.cab404.wifu.modules.MetroConnect;
import com.cab404.wifu.modules.MgupiConnect;
import com.cab404.wifu.util.Butler;
import com.cab404.wifu.util.LogManager;
import com.cab404.wifu.util.PluginManager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Pretty basic stuff, so I won't even bother making instance of Main

        PluginManager.getInstance().addModule("MosMetro_Free", new MetroConnect());
        PluginManager.getInstance().addModule("MGUPI-WiFi", new MgupiConnect());
        PluginManager.getInstance().loadModule(
                new File("./").listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File file, String name) {
                                return name.endsWith(".jar");
                            }
                        }
                ));
        LogManager.init(new File("./"));
        InfoGetter ig = new InfoGetter();

        String bssid = "";
        while (true) {
            WifiLoginModule.WifiContextInfo ci = ig.getCurrentContext();
            if (!bssid.equals(ci.bssid())) {
                Butler.getInstance().onNetworkConnect(ig.getCurrentContext());
                Thread.sleep(3000);
            } else {
                Thread.sleep(1000);
            }
        }
    }
}
