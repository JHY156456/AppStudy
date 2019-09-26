package com.example.assignment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemAdapter extends BaseAdapter {
    final String TAG = "ItemAdapter";
    ArrayList<Item> items = new ArrayList<Item>();
    Button button;
    RelativeLayout listItemLayout;
    TextView progressTextView;
    Context mContext;
    boolean sharedValue;

    public boolean isSharedValue() {
        return sharedValue;
    }

    public void setSharedValue(boolean sharedValue) {
        this.sharedValue = sharedValue;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void setListItems(ArrayList<Item> lit) {
        items = lit;
    }

    public void stopService() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).isStopServiceCheck = true;
        }
    }

    public void setStatus(int index, int status) {
        items.get(index).setDownloadStatus(status);
    }

    public void goService() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).isStopServiceCheck = false;
        }
    }


    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("WrongConstant")
    @Override
    public View getView(final int position, View convertView, final ViewGroup viewGroup) {

        ItemView view = new ItemView(viewGroup.getContext());
        button = view.findViewById(R.id.downButton);
        progressTextView = view.findViewById(R.id.downProgress);
        listItemLayout = view.findViewById(R.id.listItemLayout);
        progressTextView = view.findViewById(R.id.downProgress);


        Item item = items.get(position);
        final String title = item.getTitle();
        final String contentUrl = item.getContentURL();
        view.setTitle(item.getTitle());

        if (sharedValue) {
            listItemLayout.setBackgroundColor(Color.rgb(189, 189, 189));
            button.setEnabled(true);
        } else {
            button.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction("com.example.appb.SNED_BROADCAST");
                    intent.putExtra("CONTENT_URL", contentUrl)
                            .putExtra("index", position)
                            .putExtra("title", title);
                    viewGroup.getContext().sendBroadcast(intent);
                }
            });
        }



        if (item.getDownloadStatus() == -1) {
            progressTextView.setText(" ");
        } else if (item.getDownloadStatus() > -1 && item.getDownloadStatus() < 100) {
            progressTextView.setText("다운로드 중 (" + item.getDownloadStatus() + "%)");
        } else if (item.getDownloadStatus() == 100) {
            progressTextView.setText("다운로드 완료");
            button.setVisibility(View.INVISIBLE);
        } else if (item.getDownloadStatus() == 101) {
            progressTextView.setText("다운로드 실패");
        }


        return view;
    }
}