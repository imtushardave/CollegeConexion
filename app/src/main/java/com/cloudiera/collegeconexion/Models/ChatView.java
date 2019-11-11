package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 01-Jan-18.
 */

public class ChatView {

    private boolean seen ;
    private long timestamp;

    public ChatView(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public ChatView() {

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
