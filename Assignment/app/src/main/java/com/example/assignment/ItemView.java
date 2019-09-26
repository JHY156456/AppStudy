package com.example.assignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemView extends LinearLayout {

    TextView titleName;
    TextView progress;

    public ItemView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.listitem, this, true);

        titleName = findViewById(R.id.titleName);
        progress = findViewById(R.id.downProgress);

    }

    public void setTitle(String title) {
        titleName.setText(title);
    }

    public void setProgress(int proga) {
        progress.setText(String.valueOf(proga));
    }

}
