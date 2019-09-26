package com.example.assignment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    DBHelper helper;

    String urlString = "https://qa-m.onestorebooks.co.kr/resources/nbook10/test/contentList.xml";
    ItemAdapter adapter;
    ListView listView;
    Handler handler = new Handler();
    ArrayList<Item> itemList;
    boolean threadFlag = true;
    //현재 다운로드중인지 확인을 위한 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        grantExternalStoragePermission();

        adapter = new ItemAdapter();
        getShareValue();
        listView.setAdapter(adapter);

        helper = new DBHelper();
        openDatabase();


        if (helper.isExistTable(db)) {
            itemList = helper.getAllData(db);
            adapter.setListItems(itemList);
            adapter.notifyDataSetChanged();
            Log.d(TAG,"이프");

        } else {
            Log.d(TAG,"엘스");
            helper.createTable(db);
            httpConnectAndRequest();
        }

    }

    @SuppressLint("WrongConstant")
    private void getShareValue(){
        try {
            Context myContext;
            myContext = createPackageContext("com.example.appb", Context.MODE_PRIVATE);
            SharedPreferences pref = myContext.getSharedPreferences("PrefName", MODE_MULTI_PROCESS);
            if (pref.getBoolean("sharedPreferenceValue", false)) {
                adapter.setSharedValue(true);
            } else {
                adapter.setSharedValue(false);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void httpConnectAndRequest() {
        if (threadFlag) {
            Log.d(TAG, "threadFlag 진입");
            itemList = new ArrayList<Item>();

            try {
                RefreshThread thread = new RefreshThread(urlString);
                thread.start();
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }
        }
    }

    private boolean grantExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "External Storage Permission is Grant ");
            return true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.assignment");
        registerReceiver(MyReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(MyReceiver);
    }

    private boolean openDatabase() {
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        return true;
    }


    public InputStream getInputStreamUsingHttp(URL url) throws Exception {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        int resCode = conn.getResponseCode();
        Log.d(TAG, "Response Code : " + resCode);

        InputStream inStream = conn.getInputStream();

        return inStream;
    }


    private void processDocument(Document doc) {
        Element docEle = doc.getDocumentElement();
        NodeList nodelist = docEle.getElementsByTagName("content");

        if ((nodelist != null) && (nodelist.getLength() > 0)) {
            for (int i = 0; i < nodelist.getLength(); i++) {
                Item item = dissectNode(nodelist, i);
                if (item != null) {
                    itemList.add(item);
                    helper.insertRecordParam(item, db);
                }
            }
        }
    }

    //dissect : 해부하다
    private Item dissectNode(NodeList nodelist, int index) {
        Item eachItem = null;
        Log.d(TAG, "diseectNode() 실행");
        try {
            Element entry = (Element) nodelist.item(index);

            Element title = (Element) entry.getElementsByTagName("title").item(0);
            Element url = (Element) entry.getElementsByTagName("url").item(0);


            String titleValue = null;
            if (title != null) {
                Node firstChild = title.getFirstChild();
                if (firstChild != null) {
                    titleValue = firstChild.getNodeValue();
                }
            }
            String linkValue = null;
            if (url != null) {
                Node firstChild = url.getFirstChild();
                if (firstChild != null) {
                    linkValue = firstChild.getNodeValue();
                }
            }
            eachItem = new Item(titleValue, linkValue, -1, 1, 2);
        } catch (DOMException e) {
            e.printStackTrace();
        }

        return eachItem;
    }


    class RefreshThread extends Thread {
        String urlStr;

        public RefreshThread(String str) {
            urlStr = str;
        }
        public void run() {
            try {
                Log.d(TAG, "run() 실행");
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                URL urlForHttp = new URL(urlString);
                InputStream inStream = getInputStreamUsingHttp(urlForHttp);
                Document document = builder.parse(inStream);
                processDocument(document);
                handler.post(update);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }

        }
    }
    Runnable update = new Runnable() {
        public void run() {
            try {
                for (int i = 0; i < itemList.size(); i++) {
                    Item item = (Item) itemList.get(i);
                    adapter.addItem(item);
                }
                adapter.notifyDataSetChanged();
                helper.executeRawQuery(db);
                threadFlag = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    BroadcastReceiver MyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MyReceiver", "onReceive() 호출됨");
            Bundle bundle = intent.getExtras();
            int index = bundle.getInt("index");

            //프로그레스 리시버랑 서비스중지 리시버 두개를 구분한다
            if (bundle.getString("serviceStop")!= null) {
                boolean isServiceStopCheck = bundle.getBoolean("isServiceStopCheck");
                if (isServiceStopCheck == true) {
                    adapter.setSharedValue(true);
                } else{
                    adapter.setSharedValue(false);
                }
            } else {
                int progress = bundle.getInt("progress");
                Log.d(TAG, "받은 프로그레스 : " + progress);
                //100 : 다운로드완료 , 101 : 다운로드 오류
                helper.statusChange(adapter.getItem(index).getTitle(), progress, db);
                adapter.setStatus(index, progress);
            }
            adapter.notifyDataSetChanged();


        }
    };
}
