package com.ranita.BabyHunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by crystal on 6/18/18.
 */

public class BeaconUtils {
    private static final String TAG = "BeaconList";
    public static final String SELECTED_MAC = "SELECTED_MAC";
    public static final String SELECTED_BEACON_NAME = "SELECTED_BEACON_NAME";
    public static final String SELECTED_USER_NAME = "SELECTED_USER_NAME";
    public static final String SELECTED_USER_UUID = "SELECTED_USER_UUID";
    public static final String SELECTED_USER_MAJOT = "SELECTED_USER_MAJOT";
    public static final String SELECTED_USER_MINOR = "SELECTED_USER_MINOR";
    public static final String SELECTED_BEACON_NOTIFICATION_ENABLED = "SELECTED_BEACON_NOTIFICATION_ENABLED";
    public static final String SELECTED_DISTANCE_DETECT_ENABLED = "SELECTED_DISTANCE_DETECT_ENABLED";
    public static final String TARGET_NOTIFY_DISTANCE = "TARGET_NOTIFY_DISTANCE";
    public static final String TARGET_NOTIFY_DISTANCE_ID = "TARGET_NOTIFY_DISTANCE_ID";
    public static final String TARGET_TX_POWER = "TARGET_TX_POWER";
    public static final String TARGET_TX_POWER_ID = "TARGET_TX_POWER_ID";

    public static String getSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getString(key, "");
    }

    public static boolean getBooleanSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getBoolean(key, false);
    }

    public static int getIntPowerSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getInt(key, -51);
    }

    public static int getIntSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getInt(key, 0);
    }

    public static int getIntDistSharedPref(String key, Context ctx){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPref.getInt(key, 8);
    }

    public static void setSharedPref(String key, String value, Context ctx){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setBoolenSharedPref(String key, boolean value, Context ctx){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setIntSharedPref(String key, int value, Context ctx){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

//    public static double getDistance(int rssi, Context ctx) {
//        int txPower = getIntPowerSharedPref(TARGET_TX_POWER, ctx); // dBm
//        double coefficient1 = 0.42093;
//        double coefficient2 = 6.9476;
//        double coefficient3 = 0.54992;
//
//        if (rssi == 0) {
//            return -1.0; // if we cannot determine accuracy, return -1.
//        }
//
//        Log.d(TAG, "calculating distance based on mRssi of " + rssi  + " and txPower of: " +  txPower);
//
//
//        double ratio = rssi * 1.0 / txPower;
//        double distance;
//        if (ratio < 1.0) {
//            distance =  Math.pow(ratio,10);
//        }
//        else {
//            distance =  (coefficient1)*Math.pow(ratio,coefficient2) + coefficient3;
//        }
//        Log.d(TAG, "average rssi: " + rssi + ", distance: " + distance);
//        return distance / 1000.0;
//    }

    public static void clearNotificationFlags(Context ctx) {
        BeaconUtils.setBoolenSharedPref(BeaconUtils.SELECTED_DISTANCE_DETECT_ENABLED, false, ctx);
    }

    public static double round(double value, int digits) {
        if (digits < 0) return 0;

        long factor = (long) Math.pow(10, digits);
        value *= factor;
        return (double) Math.round(value) / factor;
    }
}
