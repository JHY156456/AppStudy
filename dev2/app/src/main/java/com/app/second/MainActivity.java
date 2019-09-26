package com.app.second;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.graphics.Color;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.util.SharedPreferenceUtil;
import com.app.util.StatusData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


//TODO: MainActivity -> 코드가독성을 위해, UI StringResource를 따로 xml로 작성하지 않음.
public class MainActivity extends Activity {

    private static final boolean DEBUG = false; //DEBUG MODE
    private static final String TAG = "b_app_test";

    static final String ACTION_GO_DOWNLOAD = "com.app.second.download";
    static final String EXTRA_KEY_FILE_NAME = "content_file_name";
    static final String EXTRA_KEY_FILE_DOWNLOAD_URL = "content_file_url";

    static final String authority = "com.app.first.myContentProvider";

    static final String SEND_BROADCAST_ACTION = "com.app.sencond.downloading";

    private boolean saveExternal = false;

    public static boolean beService;
    public static SharedPreferenceUtil sp;

    private DownloadDialog downloadDialog;
    private NotificationManager notificationManager;

    public static CheckBox ch_service;

    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS = {"android.permission.WRITE_EXTERNAL_STORAGE"};


    static final int REQUEST_CODE_STORAGE_ACCESS = 42;


    @Override
    protected void onResume() {
        super.onResume();
        //TODO: checkbox status update

        beService = sp.getA_app_Service_mode();
        ch_service.setChecked(!beService);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String NOTIFICATION_CHANNEL_ID = "kr.minwoo.kim";
        String channelName = "A,B앱 노티피케이션";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(chan);


        downloadDialog =new DownloadDialog(MainActivity.this);
        downloadDialog.setCancelable(false);


        //TODO: checkbox init..
        ch_service = (CheckBox) findViewById(R.id.checkBox);
        sp = new SharedPreferenceUtil(this);



        if (getIntent().getAction() == ACTION_GO_DOWNLOAD) { //A앱에서 호출되었을때
            String title = getIntent().getStringExtra(EXTRA_KEY_FILE_NAME);
            String downURL = getIntent().getStringExtra(EXTRA_KEY_FILE_DOWNLOAD_URL);

            if(StatusData.isDownloading() == false) { //현재 다운로드 중이 아닐때만
                showConfirmDialog(title, downURL);
            }

        } else { //유저가직접실행시
            //do nothing
        }

        //TODO: CheckBox 클릭시 상태 저장 -> SharedPreference
        ch_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (beService) {
                    beService = false;
                } else {
                    beService = true;
                }
                sp.setA_app_Service(beService);
            }
        });



        if(DEBUG)
            showDBtableFromA();


    }


    //TODO: 디버깅 개발용 함수() : A앱의 DB내용을 가져와 확인. if(DEBUG == true) 일때만 동작. 다운로드실시간 DB모니터링.
    private void showDBtableFromA() {
        Cursor c = getContentResolver().query(Uri.parse("content://" + authority + "/getAll"), null, null, null, null);
        if (c == null)
            return;

        StringBuilder sb = new StringBuilder();
        while (c.moveToNext()) {
            String str = c.getLong(0) + " , " //index
                    + c.getString(1)+" , "  //title
                    + c.getString(2) + " , "//download url
                    + c.getInt(3) + " , "   //status
                    + c.getLong(4) + " , "  //downloaded cursize
                    + c.getLong(5);          //file totalsize
            //System.out.println(str);
            sb.append(str + "\n");
        }
        c.close();
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(sb);
    }



    private void showConfirmDialog(final String Title, final String URL) {


        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View Viewlayout = inflater.inflate(R.layout.dlg, null);
        alert.setView(Viewlayout);
        alert.setCancelable(false);

        final AlertDialog dialog = alert.show();

        TextView tv_title = (TextView) Viewlayout.findViewById(R.id.dlg_title);
        TextView tv_body = (TextView) Viewlayout.findViewById(R.id.dlg_body);
        RadioGroup rg = (RadioGroup) Viewlayout.findViewById(R.id.radioGroup);
        Button btn_ok = (Button) Viewlayout.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) Viewlayout.findViewById(R.id.btn_cancel);

        tv_title.setText(Title);
        tv_body.setText("다운로드하시겠습니까?");


        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton:
                        saveExternal = false;
                        break;
                    case R.id.radioButton2:
                        saveExternal = true;
                        break;
                }

            }
        });

        //TODO: android os 4.4(KITKAT) 이거나 SDcard가 UnMounted 인경우 외장을 비활성, 내장으로 고정
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT
                || isExtSdCard(this)==false) {

           RadioButton rb1 = (RadioButton) rg.findViewById(R.id.radioButton);
           RadioButton rb2 = (RadioButton) rg.findViewById(R.id.radioButton2);
           rb1.toggle();
           rb2.setEnabled(false);
           saveExternal =false;
        }

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
                    requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
                } else {
                 //이미 사용자에게 퍼미션 허가를 받음.

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && saveExternal && sp.getSharedPreferenceUri()==null){//SAF StorageAccessFramework 필요한 조건
                    //TODO: Android OS 5.0(롤리팝) 이상 부터 SAF(스토리지 액세스 프레임워크)로 이동식 SDCard 루트 Acess 필요.
                        if(sp.getSharedPreferenceUri()==null){
                            showDialogforGetStorageAccessFramework("다음 나타나는 화면에서 이동식디스크(SDCard)를 선택해주세요.");
                        }else{
                            final DownloadFilesTask downloadTask = new DownloadFilesTask(MainActivity.this,Title,saveExternal);
                            downloadTask.execute(URL);

                            dialog.dismiss();
                        }

                    }else{
                        final DownloadFilesTask downloadTask = new DownloadFilesTask(MainActivity.this,Title,saveExternal);
                        downloadTask.execute(URL);

                        dialog.dismiss();
                    }

                }

            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    private void showDialogforGetStorageAccessFramework(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  MainActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                triggerStorageAccessFramework();
            }
        });
        myDialog.show();
    }


    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {
            Uri treeUri = null;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = resultData.getData();
                //매번 SAF호출하지않고 처음한번 Uri Get한후 SharedPreference에 save해놓고 계속 활용
                sp.setSharedPreferenceUri("key_internal_uri_extsdcard", treeUri);

                // Persist access permissions.
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            }

        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void deleteFile(Uri uri, String filename) {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        DocumentFile file = pickedDir.findFile(filename);

        if(file!=null){
            if(DEBUG)Log.d(TAG,"file Exist!");
            if(file.delete())
                if(DEBUG)Log.d(TAG, "Delete successful");
                else
                if(DEBUG)Log.d(TAG, "Delete unsuccessful");
        }else{
            if(DEBUG)Log.d(TAG,"File Not Exist!");
        }

    }




    //TODO: 다운로드 결과값을 A앱의 db에 update하기 위한 컨텐트리졸버호출. A앱의 컨텐트프로바이더에서 이를 받아 db 갱신
    private void updateContentResolveToA(String title,int status,long cursize,long totalsize) {

        ContentResolver contentResolver = getContentResolver();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("status", status); //status: -2(다운로드 실패) // 100(다운로드 완료)
        values.put("cursize", cursize);
        values.put("totalsize", totalsize);

        // ContentProviderData A어플리케이션 ContentProvider.update() 메서드에 접근
        contentResolver.update(Uri.parse("content://" + authority + "/updateItem"), values, null, null);

        if(DEBUG)
            showDBtableFromA();
    }

    //TODO: A앱에 현재 다운로드받는 컨텐츠의 다운로드 진행상황 알림. Broadcast
    private void sendBroadcastToA(String title,int status,long cursize,long totalsize) {
        Intent intent = new Intent();
        intent.setAction(SEND_BROADCAST_ACTION);
        intent.putExtra("title",title);
        intent.putExtra("status",status);
        intent.putExtra("cursize",cursize);
        intent.putExtra("totalsize",totalsize);

        sendBroadcast(intent);
    }

    private void sendNotification(String Title,String msg) {




        //TODO :Notification 만들기 //주의 4.1이하에선 Notification.builder 지원안됨. 하위호환되도록 NotificationCompat.Builder 사용
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setContentTitle(Title);
        builder.setContentText(msg);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);

        //컨텐트별로 각각 1개씩 노티를 유지하기 위해, 노티를 tag로 구분 (tag = Title)
        notificationManager.notify(Title,0, builder.build());
    }


    //TODO: 사용자가 추가장착하는 물리적인 SdCard가 있는지 체크
    private boolean isExtSdCard(Context context) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) { //android os 4.4 미만
            File[] file = ContextCompat.getExternalFilesDirs(context,null);
            if (file.length < 2) {
                //Log.d(TAG, "내장메모리 경로 : " + file[0].getPath());
                //Log.d(TAG, "SD카드가 존재하지 않습니다.");
                return false; //no SdCard
            } else {
                //Log.d(TAG, "내장메모리 경로 : " + file[0].getPath());
                //Log.d(TAG, "외장메모리 경로 : " + file[1].getPath());
                //Log.d(TAG, "SD카드가 존재합니다.");
                return true;
            }
        }else{ //android os 4.4 이상
            File[] file = context.getExternalFilesDirs(null);
            if (file.length < 2) {
                //Log.d(TAG, "내장메모리 경로 : " + file[0].getPath());
                //Log.d(TAG, "SD카드가 존재하지 않습니다.");
                return false; //no SdCard
            } else {
                //Log.d(TAG, "내장메모리 경로 : " + file[0].getPath());
                //Log.d(TAG, "외장메모리 경로 : " + file[1].getPath());
                //Log.d(TAG, "SD카드가 존재합니다.");
                return true;
            }


        }
    }

    /**
     * @param External CheckBox selected value
     * @return "/storage/emulated/0"
     * //TODO: 파일을 다운로드 받을 Path 를 구한다. 주. TopLevel 디렉토리 계산필요.
     * */
    private String getFilesSaveDir(Context context,boolean External) throws IOException {
        String file = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {  //android os 4.4미만
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // compatible for ALL the versions
                File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
                if(External){
                    if (dirs.length > 0) {
                        file  = dirs[dirs.length -1].getPath();
                    }
                }else{
                        file  = dirs[0].getPath();
                }
            }
        } else { //android os 4.4 이후
            File[] dirs = context.getExternalFilesDirs(null);
            if(External) {
                if (dirs.length > 0) {
                    file = dirs[dirs.length - 1].getPath();
                }
            }else{
                file  = dirs[0].getPath();
            }

        }
        /***** 기본적으로 API Call하면, 리턴값에 앱데이터경로까지 포함됨. /Android/data앞부분경로가 TopLevel Dir.
         * fielder[0] : 내장 메모리
         *     return-> /storage/emulated/0/Android/data/패키지명/files/
         * fielder[1] : 외장 메모리
         *     return-> /storage/sdcard폴더명/Android/data/패키지명/files/
         *****/
        if (file == null) {
            return null;
        } else {
            // 최상위 디렉토리 Path 계산
            String filePass = file.substring(0,file.lastIndexOf("/Android/data"));
            if(DEBUG)Log.d(TAG,"Call getFilesSaveDir(): return-> filePass : "+filePass);

            return filePass;
        }
    }


    private boolean hasPermissions(String[] permissions) {
        int res = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (int i=0;i<permissions.length;i++){
            res = checkCallingOrSelfPermission(permissions[i]);
        }
        for (String perms : permissions) {

            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                //퍼미션 허가 안된 경우
                return false;
            }

        }
        //퍼미션이 허가된 경우
        return true;
    }


    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (!writeAccepted  )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }
                    }
                }
                break;
        }
    }


    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  MainActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                    requestNecessaryPermissions(PERMISSIONS);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        myDialog.show();
    }



    //TODO: 파일 다운로드 작업 AsyncTask로 처리 구현. innerClass로 작성되어야 속도&메모리,코드가속성 측면에서 유리.
    private class DownloadFilesTask extends AsyncTask<String, String, Long> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        private String Title;
        private long TotalSize;
        private boolean SaveExternal;

        //1초마다 UI변경을 위해 체크하기위한 변수 추가
        private boolean becheck;
        private Long oldtime;


        public DownloadFilesTask(Context context ,String Title,Boolean saveExternal) { //1
            this.context = context;
            this.Title =Title;
            SaveExternal =saveExternal;
        }


        //파일 다운로드를 시작하기 직전에 다운로드진행창을 화면에 보여줍니다.
        @Override
        protected void onPreExecute() { //2
            super.onPreExecute();

            //사용자가 다운로드 중 파워 버튼을 누르더라도 CPU가 잠들지 않도록 해서
            //다시 파워버튼 누르면 그동안 다운로드가 진행되고 있게 됩니다.
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();


            StatusData.setDownloading(true);
            ch_service.setEnabled(false);

            downloadDialog.show();
            downloadDialog.setTitle(Title);
            downloadDialog.setMessage("다운로드 준비중");
            sendNotification(Title,"다운로드 준비중");

            oldtime = System.currentTimeMillis();
            becheck = false;

        }


        //파일 다운로드를 진행합니다.
        @Override
        protected Long doInBackground(String... string_url) { //3
            int count;
            long totalSize = -1;
            long retFileSize = -1;
            InputStream input = null;
            OutputStream output = null;
            URLConnection connection = null;


            try {

                URL url = new URL(string_url[0]);
                connection = url.openConnection();
                connection.connect();


                //파일 크기를 가져옴
                totalSize = connection.getContentLength();
                TotalSize = totalSize;

                //URL 주소로부터 파일다운로드하기 위한 input stream
                input = new BufferedInputStream(url.openStream(), 8192);

                //URL 주소로부터 파일명(확장자가 포함된) 획득함 ex)100MB_1.dat
                String fileName = string_url[0].substring(string_url[0].lastIndexOf('/')+1,string_url[0].length());

               if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && saveExternal){
                    if(sp.getSharedPreferenceUri()!=null) {
                        Uri uri = Uri.parse(sp.getSharedPreferenceUri());
                        deleteFile(uri,fileName); // SAF.. 덮어쓰기가 안되므로 동일 파일 존재하면 먼저 삭제

                        DocumentFile pickedDir = DocumentFile.fromTreeUri(getApplicationContext(), uri);
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        DocumentFile file = pickedDir.createFile("application/com.app.first", fileName); //mimeType -> 다운받는파일 확장자가 ".dat"라 임의로 설정.
                        output = getContentResolver().openOutputStream(file.getUri());
                    }
                }else{
                    String path = getFilesSaveDir(getApplicationContext(), saveExternal);

                    File outputFile = new File(path, fileName); //파일명까지 포함함 경로의 File 객체 생성

                    if (DEBUG) Log.d(TAG, "outputFile : " + outputFile.getAbsolutePath());
                    //파일을 저장하기 위한 Output stream
                    output = new FileOutputStream(outputFile, false); //append 가 true면 이어쓰기, false면 덮어쓰기
                }
                byte data[] = new byte[1024];
                long downloadedSize = 0;
                while ((count = input.read(data)) != -1) {
                    /*//BACK 버튼 눌러도 진행되도록 코드 주석 처리
                    if (isCancelled()) { //사용자가 BACK 버튼 누르면 취소가능
                        //사용자가 BACK 버튼 누르면 취소가능
                        input.close();
                        return Long.valueOf(-1);
                    }*/

                    downloadedSize += count;

                    if (downloadedSize > 0) {

                        float per = ((float) downloadedSize / totalSize) * 100;

                        //String str = "다운로드 중:" + (int) per + "%"/*+"\n("+downloadedSize + "KB / " + FileSize + "KB)"*/;
                        String str = "다운로드 중: " + (int) per + "%";

                        /* UI 변경을 위한 핸들러: 다양한 자료형(int,long,string...)을 전달해야하므로 스트링으로 타잎캐스팅
                        String....values ->( str, S <-<integer>0~99 per , S <-Long curSize, S <-<Long> TotalSize) */
                        publishProgress(str,String.valueOf((int)per),String.valueOf(downloadedSize),String.valueOf(totalSize));
                    }

                    //파일에 데이터를 기록합니다.
                    output.write(data, 0, count);
                    retFileSize = downloadedSize;
                }
                // Flush output
                output.flush();

                // Close streams
                output.close();
                input.close();


            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                mWakeLock.release();

            }
            return retFileSize;
        }


        //다운로드 중 진행률 업데이트
        @Override
        protected void onProgressUpdate(String... progress) { //4
            super.onProgressUpdate(progress);

            if((System.currentTimeMillis()-oldtime)>1000 )// 1sec 체크 (1000 = 1sec)
                becheck =true;

            if(becheck) //1초마다 UI를 update
                update((String) progress[0],Integer.parseInt(progress[1]),Long.parseLong(progress[2]),TotalSize);


        }
        private void update(String msg,int per,long cur,long total){
            sendNotification(Title,msg);
            downloadDialog.setMessage(msg);

            oldtime = System.currentTimeMillis(); //oldtime reset
            becheck = false; //boolean reset
            ch_service.setEnabled(false);

            //TODO sendBroadcast B app -> A app
            sendBroadcastToA(Title,per,cur,total);
            //TODO  ContentProvier -> Aapp db update
            updateContentResolveToA(Title, per, cur, total);
        }


        //파일 다운로드 완료 후
        @Override
        protected void onPostExecute(Long downloadSize) { //5
            super.onPostExecute(downloadSize);


            StatusData.setDownloading(false); //다운로드중인 상태 해제
            ch_service.setEnabled(true); //checkBox 다시 활성화


            if (downloadSize == TotalSize & (TotalSize !=-1L)) {

                sendNotification(Title,"다운로드 완료");

                //TODO sendBroadcast B app -> A app
                sendBroadcastToA(Title,100,downloadSize,TotalSize);
                //TODO  ContentProvier -> Aapp db update
                updateContentResolveToA(Title,100,downloadSize,TotalSize);


                if(downloadDialog.isShowing()) {
                    downloadDialog.setMessage("다운로드 완료");
                }else {
                    if(DEBUG)Toast.makeText(context.getApplicationContext(), "다운로드 완료되었습니다. 파일 크기=" + downloadSize.toString(), Toast.LENGTH_LONG).show();
                }

            }else { //완료 이외는 모두 오류(실패)처리 // 재 다운로드가 필요한 요건사항

                sendNotification(Title, "다운로드 오류");

                //TODO sendBroadcast B app -> A app
                sendBroadcastToA(Title, -2, downloadSize, TotalSize);
                //TODO  ContentProvier -> Aapp db update
                updateContentResolveToA(Title,-2, downloadSize, TotalSize);

                if (downloadDialog.isShowing()){
                    downloadDialog.setMessage("다운로드 오류");
                }else {
                    if(DEBUG)Toast.makeText(context.getApplicationContext(), "다운로드 에러", Toast.LENGTH_LONG).show();
                }


            }


        }
    }



}







