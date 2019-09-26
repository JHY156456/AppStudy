package com.example.appb;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


public class PopUPActivity extends Activity implements Button.OnClickListener{
    final static String TAG = "PopUPActivity";
    Button okButton, cancelButton;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.no:
                ((MyApp)getApplicationContext()).downloading = false;
                finish();
                break;
        }
    }
    TextView titleTextView;
    RadioGroup radioGroup;
    RadioButton radioIn;
    String contentURL;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_activity);
        MyApp my = (MyApp)getApplicationContext();
        contentURL = my.title;

        if(my.downloading) finish();


        titleTextView = findViewById(R.id.titleTextView);
        radioGroup = findViewById(R.id.radioGroup);
        okButton = findViewById(R.id.ok);
        radioIn = findViewById(R.id.radioIn);
        radioGroup.check(radioIn.getId());
        cancelButton=findViewById(R.id.no);
        cancelButton.setOnClickListener(this);
        titleTextView.setText(my.title);


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (grantExternalStoragePermission()) {
                    //final선언 안하고 적용시키는 방법이 밖으로 뺴는방법밖에없나..?
                    Intent downloadActivityIntent = new Intent(getApplicationContext(), DownloadActivity.class);
                    // 플래그를 이용합니다.
                    downloadActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(downloadActivityIntent);
                    finish();
                }
            }
        });


    }


    private boolean grantExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                return false;
            }
        } else {
            Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "External Storage Permission is Grant ");
            return true;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= 23) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission
            }
        }


    }

}
