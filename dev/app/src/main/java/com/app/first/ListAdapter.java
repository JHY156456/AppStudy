package com.app.first;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<ItemRow> {
    private Context mContext;
    private int mResource;
    private ArrayList<ItemRow> mList;
    private LayoutInflater mInflater;

    public ListAdapter(@NonNull Context context, int layoutResource, @NonNull ArrayList<ItemRow> objects ) {
        super(context, layoutResource, objects);
        this.mContext = context;
        this.mResource = layoutResource;
        this.mList = objects;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Nullable
    @Override
    public ItemRow getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final ItemRow row = mList.get(position);
        ViewHolder holder;
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.listview_item, null);

            holder.mTitle = (TextView) convertView.findViewById(R.id.mTitle);
            holder.mStatus = (TextView) convertView.findViewById(R.id.mStatus);
            holder.mBtn_download = (Button) convertView.findViewById(R.id.btn_down);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        if(row != null)
        {
            View ll = (View) convertView.findViewById(R.id.ll_layout);

            try {
                if(read_B_app_Preference()){
                    ll.setBackgroundColor(Color.TRANSPARENT);
                    holder.mBtn_download.setEnabled(true);
                }else{ //B앱에서 서비스중지 했을때
                    ll.setBackgroundColor(Color.GRAY);
                    holder.mBtn_download.setEnabled(false);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.mTitle.setText(row.title);
            final String file_status = makeText(row.status);
            holder.mStatus.setText(file_status);

            //B앱 기동시 전달하기 위한 Param
            final String title = row.title;
            final String url = row.url;

            holder.mBtn_download.setText("다운로드");
            holder.mBtn_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: B앱 기동 다운로드요청

                    Intent sendIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.app.second");

                    if(sendIntent !=null) { //B 앱 설치되어있다면 B앱 실행

                        sendIntent.setAction(Const.ACTION_GO_DOWNLOAD);
                        sendIntent.putExtra(Const.EXTRA_KEY_FILE_NAME, title);
                        sendIntent.putExtra(Const.EXTRA_KEY_FILE_DOWNLOAD_URL, url);
                        mContext.startActivity(sendIntent);

                    }else{ //B 앱 설치안되었을때
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("B 앱의 설치가 필요합니다.")
                            .setPositiveButton("확인",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                            .show();
                    }


                }
            });

            if(row.status == Const.STATUS_ENABLE_DOWNLOAD
                    || row.status == Const.STATUS_FAIL_DOWNLOAD){
                holder.mBtn_download.setVisibility(View.VISIBLE);
            }else{
                holder.mBtn_download.setVisibility(View.INVISIBLE);
            }

            if(row.status == Const.STATUS_ENABLE_DOWNLOAD){//레이아웃 정렬 이쁘게 하기위해 코드추가
                holder.mStatus.setVisibility(View.GONE);
            }else{
                holder.mStatus.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    private String makeText(int status) {

        if(status>0 && status<100)
            return "다운로드 중 ("+status+"%)";
        else {
            switch (status) {
                case Const.STATUS_ENABLE_DOWNLOAD:
                    return "";//"다운로드 가능";
                case Const.STATUS_FAIL_DOWNLOAD:
                    return "다운로드 실패";
                case Const.STATUS_COMPLETE_DOWNLOAD:
                    return "다운로드 완료";
            }
        }
        return "";
    }


    private boolean read_B_app_Preference() throws Exception {
        // get Context of other application
        try{
            Context c = mContext.createPackageContext("com.app.second",Context.MODE_PRIVATE);
            SharedPreferences pref = c.getSharedPreferences("secondApp.SharedPreference", Context.MODE_MULTI_PROCESS);
            Boolean value = pref.getBoolean("service", true);
            return value;
        }catch(PackageManager.NameNotFoundException e){
            //b앱이 설치가 안되었을 가능성이 있을때 : default return true;
            return true;
        }


    }

    private  class ViewHolder{
        public TextView mTitle;
        public TextView mStatus;
        public Button mBtn_download;
    }

}
