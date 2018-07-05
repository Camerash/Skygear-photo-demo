package com.camerash.android_photo_demo;

import java.util.Date;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Record;

public class Photo {

    Photo(Record record, Asset asset) {
        this.url = asset.getUrl();
        this.time = record.getCreatedAt();
        this.record = record;
    }

    private String url;
    private Date time;
    private Record record;

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

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

}
