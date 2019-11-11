package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 16-Jan-18.
 */

public class BlockList {

    private long timestamp;

    public BlockList(long timestamp) {
        this.timestamp = timestamp;
    }

    public BlockList() {

    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
