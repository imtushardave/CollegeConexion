package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 12-Jan-18.
 */

public class HappeningsPost {

    private String desc;
    private String image_uri;
    private long timestamp;
    private String uid;

    public HappeningsPost(String desc, String image_uri, long timestamp, String uid) {
        this.desc = desc;
        this.image_uri = image_uri;
        this.timestamp = timestamp;
        this.uid = uid;
    }
    public HappeningsPost() {

    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
