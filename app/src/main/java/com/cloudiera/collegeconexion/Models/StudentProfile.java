package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 08-Dec-17.
 */
public class StudentProfile {

    private String profile_name;
    private String profile_image;
    private String profile_img_thumb;
    private String bio;
    private String course;
    private String branch;
    private String roll_no;
    private String dob;
    private String gender;
    private String phone_number;
    private String email;
    private String college_id;
    private String user_id;
    private String device_token;
    private boolean verified;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }


    public StudentProfile(String profile_name, String profile_image, String profile_img_thumb, String bio, String course,
                          String branch, String roll_no, String dob, String gender, String phone_number, String email,
                          String college_id, String user_id,
                          String device_token,boolean verified) {
        this.profile_name = profile_name;
        this.profile_image = profile_image;
        this.profile_img_thumb = profile_img_thumb;
        this.bio = bio;
        this.course = course;
        this.branch = branch;
        this.roll_no = roll_no;
        this.dob = dob;
        this.gender = gender;
        this.phone_number = phone_number;
        this.email = email;
        this.college_id = college_id;
        this.user_id = user_id;
        this.device_token = device_token;
        this.verified = verified;
    }

    public StudentProfile() {

    }

    public String getCollege_id() {
        return college_id;
    }

    public void setCollege_id(String college_id) {
        this.college_id = college_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getProfile_img_thumb() {
        return profile_img_thumb;
    }

    public void setProfile_img_thumb(String profile_img_thumb) {
        this.profile_img_thumb = profile_img_thumb;
    }

    public String getProfile_name() {
        return profile_name;
    }

    public void setProfile_name(String profile_name) {
        this.profile_name = profile_name;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public String getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(String roll_no) {
        this.roll_no = roll_no;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "StudentProfile{" +
                "profile_name='" + profile_name + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", profile_img_thumb='" + profile_img_thumb + '\'' +
                ", bio='" + bio + '\'' +
                ", course='" + course + '\'' +
                ", branch='" + branch + '\'' +
                ", roll_no='" + roll_no + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", email='" + email + '\'' +
                ", college_id='" + college_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", device_token='" + device_token + '\'' +
                ", verified=" + verified +
                '}';
    }
}
