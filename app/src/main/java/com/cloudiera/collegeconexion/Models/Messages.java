package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 29-Dec-17.
 */

public class Messages {

    private String message;
    private boolean seen;
    private String type,from;
    private long time;
    private String push_id;

    public Messages(String message, boolean seen, String type, long time, String from,String push_id) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.from = from;
        this.seen = seen;
        this.push_id = push_id;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }

    public Messages() {

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

}
