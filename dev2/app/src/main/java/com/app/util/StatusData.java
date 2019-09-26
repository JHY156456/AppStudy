package com.app.util;

public class StatusData {

    private static boolean bDownloading = false;

    public static boolean isDownloading() {
        return bDownloading;
    }
    public static  void setDownloading(boolean b) {
        bDownloading = b;
    }
}
