package com.app.first;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends Activity {

    private static final String TAG = "a_app_test";
    private static String requestListUrl = Const.REQUEST_CONTENTS_LIST_URL;

    DBHelper mDatabase;
    MsgHandler mhandler;

    private ListView mListView;

    private ArrayList<ItemRow> mContentItems;
    private ListAdapter mAdapter;

    //handler messages define
    private final static int MSG_LIST_UI_UPDATE = 0;
    private final static int MSG_ADD_EVENT_1 = 1;  //이벤트 추가필요할시 여분 defined
    private final static int MSG_ADD_EVENT_2 = 2;  //이벤트 추가필요할시 여분 defined

    private DBHelper db() {
        return DBHelper.getInstance(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = db();
        mhandler= new MsgHandler();

        mListView = (ListView)findViewById(R.id.lv_list);
        mContentItems = new ArrayList<ItemRow>();
        mAdapter = new ListAdapter(this,R.layout.listview_item,mContentItems);
        mListView.setAdapter(mAdapter);


        if(mDatabase.isExist_DB()){
            loadListData();
        }else {
            //TODO: xml parsing -> make db
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    xmlParsingToMakeDB();
                }
            });
            thread.start();
        }
    }


    private void xmlParsingToMakeDB() {
        try {
            DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
            Document doc = dBuilder.parse(requestListUrl);

            // Parse tag
            NodeList nList = doc.getElementsByTagName("content");
            List<ItemRow> mList = new ArrayList<>();
            for(int temp = 0; temp < nList.getLength(); temp++){
                Node nNode = nList.item(temp);
                if(nNode.getNodeType() == Node.ELEMENT_NODE){
                    Element eElement = (Element) nNode;
                    String title =getTagValue("title", eElement);
                    String urltxt = getTagValue("url", eElement);

                    long TotalFileSize = -1L;
                    /*URL url = new URL(urltxt);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    int responseCode = conn.getResponseCode();
                    conn.setRequestMethod("GET");
                    if(responseCode == HttpsURLConnection.HTTP_OK ){ //200
                        TotalFileSize = conn.getContentLength();
                    }*/ //파싱단계서 파일사이즈 GET할 필요 없어서 해당 코드 주석처리
                    mList.add(new ItemRow(title, urltxt, Const.STATUS_ENABLE_DOWNLOAD,-1L,TotalFileSize));
                }
            }

            mDatabase.setDelete();
            for (ItemRow item : mList) {
                mDatabase.setItem(item.title, item.url, item.status ,item.cur_size, item.total_size );
            }

            Message msg = mhandler.obtainMessage(MSG_LIST_UI_UPDATE);
            mhandler.sendMessage(msg);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadListData(){
        mContentItems.clear();
        List<ItemRow> row = mDatabase.getItem();
        for (ItemRow item : row) {
            mContentItems.add(item);
        }
        mAdapter.notifyDataSetChanged();

    }
    private void updateListItem(String title,int status,long cur,long total){
        int index = 0 ;
        for (int i=0;i<mContentItems.size()-1;i++){
            if(mContentItems.get(i).title.trim().equalsIgnoreCase(title.trim())){
                index = i;
                break;
            }
        }
        String url =mContentItems.get(index).url;
        mContentItems.set(index,new ItemRow(title, url, status,cur,total));
        mAdapter.notifyDataSetChanged();

    }


    @Override
    protected void onResume() {
        super.onResume();

        //TODO: REGIST Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.app.sencond.downloading");
        registerReceiver(BR,filter);

        //TODO: db 값 가져와 List UI(상태) 갱신
        loadListData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabase != null)
            mDatabase.close();
        unregisterReceiver(BR);
    }



    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if(nValue == null)
            return null;
        return nValue.getNodeValue();
    }


    private class MsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //Log.d(TAG,"[Handler]-> "+msg.what);
            switch (msg.what) {
                case MSG_LIST_UI_UPDATE:
                    loadListData();
                    break;
                case MSG_ADD_EVENT_1:

                    break;
                case MSG_ADD_EVENT_2:
                    break;
            }
        }

        public MsgHandler() {
            super();
        }
    }

    //TODO: BroadcastReceiver 동적리시버 구현. B앱으로부터 데이터 수신받아 리스트Item UI변경.
    BroadcastReceiver BR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title"); //selection
            int status = intent.getIntExtra("status",0);
            long cursize = intent.getLongExtra("cursize",0);
            long totalsize = intent.getLongExtra("totalsize",0);

            updateListItem(title,status,cursize,totalsize);
        }
    };
}

class ItemRow {
    public String title;
    public String url;
    public int status;
    public long cur_size;
    public long total_size;

    public ItemRow(String title, String url, int status ,long cursize , long totalsize) {
        this.title = title;
        this.url = url;
        this.status = status;
        this.cur_size =cursize;
        this.total_size = totalsize;
    }
}


