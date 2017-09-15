package com.dk.models;

import org.json.JSONObject;

/**
 * Created by dk on 01/09/17.
 */

public class Me {
    private static final Me ourInstance = new Me();
    private String uid;
    private String name;
    private String contact;
    private String email;
    private JSONObject tags;

    private Me() {


    }

    static Me getInstance() {
        return ourInstance;
    }

    public static Me getOurInstance() {
        return ourInstance;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JSONObject getTags() {
        return tags;
    }

    public void setTags(JSONObject tags) {
        this.tags = tags;
    }
}
