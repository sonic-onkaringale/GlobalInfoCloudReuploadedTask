package com.onkaringale.shoptask.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPref {

    public static void setdata(Activity activity, String key, String value) {

//        activity.getApplication().getSharedPreferences("GlobalSharedPref",Context.MODE_PRIVATE);
//        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = activity.getApplication().getSharedPreferences("GlobalSharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
//        editor.apply();
    }

    public static void setdataBackground(Context context, String key, String value) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("GlobalSharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getdata(Activity activity, String key) {
        SharedPreferences sharedPreferences = activity.getApplication().getSharedPreferences("GlobalSharedPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static String getdataBackground(Application application, String key) {
        SharedPreferences sharedPreferences = application.getSharedPreferences("GlobalSharedPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static String getdataBackground(Context context, String key) {

        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("GlobalSharedPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }


    public static void deleteall(Activity activity) {
        SharedPreferences sharedPreferences = activity.getApplication().getSharedPreferences("GlobalSharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
//        editor.apply();
    }

    public static void deleteallBackground(Context appContext) {
        SharedPreferences sharedPreferences = appContext.getSharedPreferences("GlobalSharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
//        editor.apply();
    }

    public static void viewall(Activity activity, String[] keys) {
        SharedPreferences sharedPreferences = activity.getApplication().getSharedPreferences("GlobalSharedPref", Context.MODE_PRIVATE);

        for (String key : keys) {
            Log.d("SharedPrefs", (key + " : " + sharedPreferences.getString(key, null)));
        }


    }


}
