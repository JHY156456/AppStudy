package com.example.appb;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class DownloadActivity extends Activity implements Button.OnClickListener {
    final static private String TAG = "DownLoadActivity";
    DownloadManager.Request r;
    DownloadManager dm;
    String contentURL, title;
    Button button;
    TextView textView, titleTextView;
    Timer timer;
    TimerTask timerTask;
    Intent intent;
    NotificationManager manager;
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_activity);

        titleTextView = findViewById(R.id.titleTextView);
        textView = findViewById(R.id.downloadStatus);
        button = findViewById(R.id.ok);
        button.setOnClickListener(this);

        MyApp my = (MyApp) getApplicationContext();
        my.downloading = true;
        contentURL = my.contentURL;

        title = ((MyApp) getApplicationContext()).title;
        titleTextView.setText(title);

        r = new DownloadManager.Request(Uri.parse(contentURL));
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "fileName");
        r.allowScanningByMediaScanner();

        // <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />
        // manifest에 이거 추가해야 VISIBILITY_HIDDEN 사용가능
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        goDownload();
    }


    private void goDownload() {
        //다운로드 시작코드
        final long downloadId = dm.enqueue(r);

        //진행상황 앱A에 전달하기 위한 인텐트
        intent = new Intent();
        intent.setAction("com.example.assignment");
        intent.putExtra("index", ((MyApp) getApplicationContext()).index);
        intent.putExtra("isServiceStopCheck", false);


        new Thread(new Runnable() {
            @Override
            public void run() {
                //원래 타이머태스크 여기다가 놨었음
                timer = new Timer();
                timer.schedule(timerTask, 0, 1000);
            }
        }).start();

        //타이머는 캔슬전까지 1초에 한번씩 계속 돈다.
        timerTask = new TimerTask() {
            @Override
            public void run() {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadId);
                Cursor cursor = dm.query(q);
                cursor.moveToFirst();
                int bytes_downloaded = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));


                //다운로드 진행중
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    ((MyApp) getApplicationContext()).downloading = false;
                }
                //final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                final int dl_progress;
                if (statusMessage(cursor).equals("다운로드 오류")) {
                    dl_progress = 101;
                } else {
                    dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        intent.putExtra("progress", dl_progress);
                        if (dl_progress == 100) {
                            textView.setText("다운로드 완료");
                            ((MyApp) getApplicationContext()).downloading = false;
                            timer.cancel();
                        } else if (dl_progress == 101) {
                            ((MyApp) getApplicationContext()).downloading = false;
                            textView.setText("다운로드 실패");
                            timer.cancel();

                        } else {
                            textView.setText("다운로드 진행중 : " + dl_progress + "%");
                        }
                        showNoti(title, dl_progress);
                        sendBroadcast(intent);
                    }
                });


                //Log.d(TAG, statusMessage(cursor));
                cursor.close();
            }
        };
    }


    private String statusMessage(Cursor c) {
        String msg = "???";

        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            case DownloadManager.STATUS_FAILED:
                msg = "다운로드 오류";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg = "Download paused!";
                break;

            case DownloadManager.STATUS_PENDING:
                msg = "Download pending!";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg = "다운로드 중";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg = "다운로드 완료";
                break;

            default:
                msg = "Download is nowhere in sight";
                break;
        }

        return (msg);
    }

    private void showNoti(String title, int progress) {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            ));
        } else {
            builder = new Notification.Builder(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        }
        if (progress == 100) {
            builder.setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText("다운로드 완료")
                    .setSmallIcon(android.R.drawable.ic_menu_view);
        } else if (progress == 101) {
            builder.setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText("다운로드  실패")
                    .setSmallIcon(android.R.drawable.ic_menu_view);
        } else {
            builder.setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText("다운로드 진행중 : " + progress + "%")
                    .setSmallIcon(android.R.drawable.ic_menu_view);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manager.notify(1, builder.build());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                finish();
                break;
        }
    }
}
