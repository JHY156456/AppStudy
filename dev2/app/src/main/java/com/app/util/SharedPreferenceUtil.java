package com.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class SharedPreferenceUtil {

    public static final  String APP_SHARED_PREFS = "secondApp.SharedPreference";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferenceUtil(Context context){
        this.sharedPreferences  = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public void setA_app_Service(Boolean beAble){
        editor.putBoolean("service",beAble);
        editor.commit();
    }
    public boolean getA_app_Service_mode(){
        return sharedPreferences.getBoolean("service",true);
    }

    //SAF uri save
    public void setSharedPreferenceUri(String key_internal_uri_extsdcard, Uri treeUri) {
        editor.putString("key_internal_uri_extsdcard",treeUri.toString());
        editor.commit();
    }
    public String getSharedPreferenceUri(){
        return sharedPreferences.getString("key_internal_uri_extsdcard",null);
    }
}
