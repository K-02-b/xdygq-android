package com.example.xdygq3;

public class shareData {
    public static final int ACTION_HOME = 1;
    public static final int ACTION_OTHER = 2;
    public static final int ACTION_ABOUT = 3;
    public final static int JobInfo_ID = 3378;
    public static final int NewThreadFlag = -1;
    public static Classes.SettingsData config = null;
    public static String DATAFILE = "data.json";
    public static int getActionFromItemId(int itemId) {
        if (itemId == R.id.navigation_item1) return ACTION_HOME;
        else if (itemId == R.id.navigation_item2) return ACTION_OTHER;
        else if (itemId == R.id.navigation_item3) return ACTION_ABOUT;
        return 0;
    }
    public static Classes.SettingsData getConfig() {
        if(config == null) {
            config = new Classes.SettingsData();
        }
        return config;
    }
}
