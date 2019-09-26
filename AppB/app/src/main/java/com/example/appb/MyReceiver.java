package com.example.appb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive() 메소드 호출됨.");
        Log.d(TAG,"intent.getAction() : " + intent.getAction());
        Bundle bundle = intent.getExtras();

        ((MyApp)context.getApplicationContext()).globalSetValue(
                bundle.getInt("index"),
                bundle.getString("title"),
                ((MyApp)context.getApplicationContext()).downloading,
                bundle.getString("CONTENT_URL"));

        Intent myIntent = new Intent(context, PopUPActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(myIntent);

    }

}
