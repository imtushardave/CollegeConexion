package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 22-Dec-17.
 */

public class AddFriendsModel {
    private String profile_name;
    private String profile_img_thumb;
    private String course;
    private String roll_no;
    private String branch;
    private String user_id;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public AddFriendsModel(String profile_name, String profile_img_thumb,
                           String course,  String branch, String user_id) {
        this.profile_name = profile_name;
        this.profile_img_thumb = profile_img_thumb;
        this.course = course;
        this.branch = branch;
        this.user_id = user_id;
    }

    public String getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(String roll_no) {
        this.roll_no = roll_no;
    }

    public AddFriendsModel() {

    }

    public String getProfile_name() {
        return profile_name;
    }

    public void setProfile_name(String profile_name) {
        this.profile_name = profile_name;
    }

    public String getProfile_img_thumb() {
        return profile_img_thumb;
    }

    public void setProfile_img_thumb(String profile_img_thumb) {
        this.profile_img_thumb = profile_img_thumb;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
