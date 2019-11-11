package com.cloudiera.collegeconexion.Models;

/**
 * Created by HP on 29-Jan-18.
 */

public class CollegeCommunity {

    private String college_id;
    private String community_logo;
    private String community_name;
    private String description;
    private String who_can_join;
    private String push_id;
    private long timestamp;

    public CollegeCommunity( String college_id,
                            String community_logo, String community_name,
                            String description, String who_can_join, long timestamp,String push_id) {

        this.college_id = college_id;
        this.community_logo = community_logo;
        this.community_name = community_name;
        this.description = description;
        this.who_can_join = who_can_join;
        this.timestamp = timestamp;
        this.push_id = push_id;
    }

    public CollegeCommunity() {


    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }

    public String getCollege_id() {
        return college_id;
    }

    public void setCollege_id(String college_id) {
        this.college_id = college_id;
    }

    public String getCommunity_logo() {
        return community_logo;
    }

    public void setCommunity_logo(String community_logo) {
        this.community_logo = community_logo;
    }

    public String getCommunity_name() {
        return community_name;
    }

    public void setCommunity_name(String community_name) {
        this.community_name = community_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWho_can_join() {
        return who_can_join;
    }

    public void setWho_can_join(String who_can_join) {
        this.who_can_join = who_can_join;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
