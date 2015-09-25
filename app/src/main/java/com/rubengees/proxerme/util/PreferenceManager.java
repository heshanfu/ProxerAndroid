package com.rubengees.proxerme.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Todo: Describe Class
 *
 * @author Ruben Gees
 */
public class PreferenceManager {

    public static final String PREFERENCE_NEW_NEWS = "news_new";
    public static final String PREFERENCE_NEWS_NOTIFICATIONS = "pref_news_notifications";
    private static final String PREFERENCE_NEWS_LAST_ID = "news_last_id";

    public static boolean areNotificationsEnabled(@NonNull Context context) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);

        return preferences.getBoolean(PREFERENCE_NEWS_NOTIFICATIONS, false);
    }

    public static void setNotificationsEnabled(@NonNull Context context, boolean enabled) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);

        preferences.edit().putBoolean(PREFERENCE_NEWS_NOTIFICATIONS, enabled).apply();
    }

    public static int getLastId(@NonNull Context context) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);

        return preferences.getInt(PREFERENCE_NEWS_LAST_ID, -1);
    }

    public static void setLastId(@NonNull Context context, int id) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);

        preferences.edit().putInt(PREFERENCE_NEWS_LAST_ID, id).apply();
    }

    public static int getNewNews(@NonNull Context context) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);

        return preferences.getInt(PREFERENCE_NEW_NEWS, 0);
    }

    public static void setNewNews(@NonNull Context context, int amount) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);

        preferences.edit().putInt(PREFERENCE_NEW_NEWS, amount).apply();
    }
}
