package com.cab404.wifu.util;


import com.cab404.wifu.base.WifiLoginModule;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * «Oatmeal, sir.»
 * <p/>
 * Basically, that's central coordination center of everything.
 * It receives network status changes, as well as performing initialization.
 *
 * @author cab404
 */
public class Butler {

    private static Butler ourInstance = new Butler();

    public static Butler getInstance() {
        return ourInstance;
    }

    private Butler() {
    }

    private static final String TAG = "Butler";
    private Timer scheduled = new Timer(TAG, true);
    private TimerTask scheduledRepeater;

    private synchronized void replaceRepeater(final WifiLoginModule module, final WifiLoginModule.WifiContextInfo info, final WifiLoginModule.Log log) {
        if (scheduledRepeater != null) {
            log.v("Cancelling previous log-in task");
            scheduledRepeater.cancel();
        }
        scheduledRepeater = new TimerTask() {
            @Override
            public void run() {
                try {
                    module.handle(info, log);
                } catch (Exception e) {
                    log.e(e, "Module " + module.getClass() + " had failed with exception");
                }
            }
        };
        scheduled.schedule(
                scheduledRepeater,
                module.repeatDelay(),
                module.repeatDelay()
        );
    }

    String bssid = "";

    public void onNetworkConnect(final WifiLoginModule.WifiContextInfo info) {

        final LogManager lman = LogManager.getInstance();
        final WifiLoginModule.Log log = lman.generateLog("Main-Butler");

        if (info.bssid() == null){
            if (bssid != null)
                log.v("We are disconnected.");
            bssid = null;
            if (scheduledRepeater != null){
                scheduledRepeater.cancel();
                scheduledRepeater = null;
            }
            return;
        }

        final List<Plugin> ways = new LinkedList<>();
        final List<Plugin> plugins = PluginManager.getInstance().getPlugins();

        if (info.bssid().equals(bssid))
            return;
        bssid = info.bssid();

        log.v("SSID: " + info.ssid() + "; BSSID: " + bssid);
        log.v("Checking " + plugins.size() + " loaded plugin(s)");

        for (Plugin plugin : plugins)
            if (plugin.module.canHandle(info) > 0)
                ways.add(plugin);

        // TODO: Add plugin availability checking in repository
        if (ways.isEmpty()) {
            log.v("New connection detected, but either it's open, " +
                    "or we've not equipped properly to deal with it.");
            return;
        }

        // Sorting in descending order
        Collections.sort(ways, new Comparator<Plugin>() {
            @Override
            public int compare(Plugin wk1, Plugin wk2) {
                return wk2.module.canHandle(info) - wk1.module.canHandle(info);
            }
        });


        // Trying
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.v("Starting dispatching thread for " + info.ssid());
                log.v("Got " + ways.size() + " module(s) to try");

                boolean handle = false;
                WifiLoginModule module = null;

                while (!ways.isEmpty()) {

                    final Plugin plugin = ways.remove(0);
                    final WifiLoginModule top = plugin.module;
                    WifiLoginModule.Log wifiLog = lman.generateLog(plugin.name);

                    log.v("Checking " + top.getClass());
                    try {
                        if (handle = (module = top).handle(info, wifiLog)) {
                            log.v("Success, proceeding with " + module.getClass());
                            if (module.repeatDelay() > 0) {
                                log.v("Module asked to relog later, added timer task, delay is " + module.repeatDelay() + "ms");
                                replaceRepeater(module, info, wifiLog);
                            }
                            return;
                        }
                    } catch (Exception e) {
                        log.e(e, "Module " + top.getClass() + " had failed with exception");
                    }

                }
                if (module == null)
                    log.v("No module found for handling " + info.ssid() + ", assuming that is fine.");
                else if (!handle)
                    log.v("All modules we tried have failed");


            }
        }).start();
    }

}
