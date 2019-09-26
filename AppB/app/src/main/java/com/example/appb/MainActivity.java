package com.example.appb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

public class MainActivity extends Activity implements View.OnClickListener {
    final static private String TAG = "MainActivity";
    BroadcastReceiver myReceiver;
    CheckBox serviceStopCheckBox;
    Intent intent;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceStopCheckBox = findViewById(R.id.serviceStopCheckBox);
        if(((MyApp)getApplicationContext()).downloading){
            serviceStopCheckBox.setClickable(false);
        }else{
            serviceStopCheckBox.setOnClickListener(this);

        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.appb.SNED_BROADCAST");
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver,filter);

        prefs = getSharedPreferences("PrefName",MODE_PRIVATE);
        editor = prefs.edit();

        if(prefs.getBoolean("sharedPreferenceValue",false)==true){
            Log.d(TAG,"태그설정온");
            serviceStopCheckBox.setChecked(true);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.serviceStopCheckBox:

                intent = new Intent();
                intent.setAction("com.example.assignment");

                String value = "SERVICE";
                intent.putExtra("serviceStop",value);

                //눌렀을때 활성화 시킨것
                if(serviceStopCheckBox.isChecked() ==true){
                    Log.d(TAG,"if");
                    editor.putBoolean("sharedPreferenceValue",true);
                    intent.putExtra("isServiceStopCheck",true);
                }
                //체크 비활성화 시킨
                else{
                    Log.d(TAG,"else");
                    editor.putBoolean("sharedPreferenceValue",false);
                    intent.putExtra("isServiceStopCheck",false);
                }

                editor.commit();
                sendBroadcast(intent);
                break;
        }
    }
}
