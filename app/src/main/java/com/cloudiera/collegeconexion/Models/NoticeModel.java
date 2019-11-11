package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 18-Feb-18.
 */

public class NoticeModel {

    private String description;
    private String heading;
    private String imageUri;
    private String push_key;
    private String stamp_time;
    private String user_id;

    public NoticeModel(String description, String heading,
                       String imageUri, String push_key,
                       String stamp_time, String user_id) {
        this.description = description;
        this.heading = heading;
        this.imageUri = imageUri;
        this.push_key = push_key;
        this.stamp_time = stamp_time;
        this.user_id = user_id;
    }

    public NoticeModel() {

    }

    public String getStamp_time() {
        return stamp_time;
    }

    public void setStamp_time(String stamp_time) {
        this.stamp_time = stamp_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getPush_key() {
        return push_key;
    }

    public void setPush_key(String push_key) {
        this.push_key = push_key;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
