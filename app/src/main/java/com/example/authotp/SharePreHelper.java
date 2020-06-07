package com.example.authotp;

public class SharePreHelper {

    private static String KEY_DEMO_NAME = "Demo Name";

    public static void setName(String value) {
        App.preferences.edit().putString(KEY_DEMO_NAME, value ).apply();
    }
    public static String getName() {
        return App.preferences.getString(KEY_DEMO_NAME,"1233211231");
    }
}
