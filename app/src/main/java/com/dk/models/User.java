package com.dk.models;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by dk on 21/08/17.
 */
@Entity
public class User implements Serializable {
    public String name;

    @Id
    private long id;
    private String contact;
    private Boolean knows_me;
    private String avatar;


    public void setKnows_me(Boolean knows_me) {
        this.knows_me = knows_me;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getKnows_me() {
        return knows_me;
    }

    public void setKnows_me(boolean knows_me) {
        this.knows_me = knows_me;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


}