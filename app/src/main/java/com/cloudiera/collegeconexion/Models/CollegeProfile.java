package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 08-Dec-17.
 */

public class CollegeProfile {

    private String college_logo;
    private String college_name;
    private String college_code;

    public CollegeProfile(String college_logo, String college_name, String college_code) {
        this.college_logo = college_logo;
        this.college_name = college_name;
        this.college_code = college_code;
    }
    public CollegeProfile() {

    }

    public String getCollege_logo() {
        return college_logo;
    }

    public void setCollege_logo(String college_logo) {
        this.college_logo = college_logo;
    }

    public String getCollege_Name() {
        return college_name;
    }

    public void setCollege_Name(String college_name) {
        this.college_name = college_name;
    }

    public String getCollege_code() {
        return college_code;
    }

    public void setCollege_code(String college_code) {
        this.college_code = college_code;
    }

    @Override
    public String toString() {
        return "CollegeProfile{" +
                "college_logo='" + college_logo + '\'' +
                ", college_Name='" + college_name + '\'' +
                ", college_code='" + college_code + '\'' +
                '}';
    }
}
