package com.example.assignment;

public class Item {
    String title;
    String contentURL;
    //-1 : 다운안됨 , 0~99 : 다운중 , 100 : 다운완료 , 101 : 다운실패
    int downloadStatus;
    int currentPhysicalSize;
    int totalPhysicalSize;
    boolean isStopServiceCheck;

    public Item(String title, String contentURL, int downloadStatus, int currentPhysicalSize, int totalPhysicalSize) {
        this.title = title;
        this.contentURL = contentURL;
        this.downloadStatus = downloadStatus;
        this.currentPhysicalSize = currentPhysicalSize;
        this.totalPhysicalSize = totalPhysicalSize;
        this.isStopServiceCheck = false;
    }

    public String getTitle() {
        return title;
    }




    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentURL() {
        return contentURL;
    }

    public void setContentURL(String contentURL) {
        this.contentURL = contentURL;
    }

    public int getCurrentPhysicalSize() {
        return currentPhysicalSize;
    }

    public void setCurrentPhysicalSize(int currentPhysicalSize) {
        this.currentPhysicalSize = currentPhysicalSize;
    }

    public int getTotalPhysicalSize() {
        return totalPhysicalSize;
    }

    public void setTotalPhysicalSize(int totalPhysicalSize) {
        this.totalPhysicalSize = totalPhysicalSize;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public boolean isStopServiceCheck() {
        return isStopServiceCheck;
    }

    public void setStopServiceCheck(boolean stopServiceCheck) {
        isStopServiceCheck = stopServiceCheck;
    }
}

