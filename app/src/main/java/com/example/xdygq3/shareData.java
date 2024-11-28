package com.example.xdygq3;

import android.annotation.SuppressLint;
import android.util.Log;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class shareData {
    public static final int ACTION_HOME = 1;
    public static final int ACTION_OTHER = 2;
    public static final int ACTION_ABOUT = 3;
    public final static int JobInfo_ID = 3378;
    public static final int NewThreadFlag = -1;
    public static Classes.SettingsData config = null;
    public static String DATAFILE = "data.json";

    @SuppressLint("CustomX509TrustManager")
    static X509TrustManager trustAllCerts = new X509TrustManager() {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };
    /**
     * @noinspection unused
     */
    static HostnameVerifier hostnameVerifier = (hostname, session) -> true;
    static SSLContext sslContext = null;

    public static int getActionFromItemId(int itemId) {
        if (itemId == R.id.navigation_item1) return ACTION_HOME;
        else if (itemId == R.id.navigation_item2) return ACTION_OTHER;
        else if (itemId == R.id.navigation_item3) return ACTION_ABOUT;
        return 0;
    }

    public static Classes.SettingsData getConfig() {
        if (config == null) {
            config = new Classes.SettingsData();
        }
        return config;
    }

    static SSLContext getSSLContext() {
        if (sslContext == null) {
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustAllCerts}, new java.security.SecureRandom());
            } catch (Exception e) {
                Log.e("SSLContext", "getSSLContext", e);
            }
        }
        return sslContext;
    }
}
