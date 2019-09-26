package com.example.appb;

import android.app.Application;

public class MyApp extends Application {

    public int index;
    public String title;
    public boolean downloading;
    public String contentURL;

    public void globalSetValue(int index, String title, boolean downloading, String contentURL) {
        this.index = index;
        this.title = title;
        this.downloading = downloading;
        this.contentURL = contentURL;
    }
}
