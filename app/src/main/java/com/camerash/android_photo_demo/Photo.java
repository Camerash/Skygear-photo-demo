package com.camerash.android_photo_demo;

import java.util.Date;

public class Photo {

    Photo(String url, Date time) {
        this.url = url;
        this.time = time;
    }

    private String url;
    private Date time;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
