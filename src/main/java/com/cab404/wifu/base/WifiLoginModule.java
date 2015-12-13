package com.cab404.wifu.base;

/**
 * Does a thing with network, gaining access to it.
 *
 * @author cab404
 */
public interface WifiLoginModule {

    interface WifiContextInfo {
        /**
         * Returns BSSID of AP
         */
        String bssid();

        /**
         * Returns SSID of AP
         */
        String ssid();

        /**
         * Returns mac address of AP
         */
        String mac();
    }

    interface Log {
        /**
         * Registers error
         */
        void e(Throwable t, String message);

        /**
         * Verboses something into log.
         */
        void v(String message);

        /**
         * Try to avoid using this, since nobody likes unlocalized output
         * This methods shows user a message
         */
        void alert(String message);
    }

    /**
     * Invoked then we need to connect to network.
     */
    boolean handle(WifiContextInfo info, Log log);

    /**
     * Returns delay (in ms) between 'handle' invocations, if network logs you out over time.
     * Returns -1 if network don't do that.
     */
    long repeatDelay();

    /**
     * Returns number, usually from 0 to 100, how well this network is supported.
     * I've done this because some types of network authentication methods are depending on
     * router itself, such as messing with Cisco's 1.1.1.1/login.html. Those methods may or may not
     * work on some of those.
     */
    int canHandle(WifiContextInfo info);
}
