package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 30-Dec-17.
 */

public class FriendReqModel {

    private String request_type;

    public FriendReqModel(String request_type) {
        this.request_type = request_type;
    }

    public FriendReqModel() {

    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
