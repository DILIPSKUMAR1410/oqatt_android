package com.dk.models;

import com.stfalcon.chatkit.commons.models.IUser;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

/**
 * Created by dk on 26/03/18.
 */
@Entity

public class Anonymous implements IUser, Serializable {

    public ToOne<Thread> thread;
    @Id
    private long a_id;

    private String uid;

    public Anonymous(ToOne<Thread> thread, String uid) {
        this.thread = thread;
        this.uid = uid;
    }

    public Anonymous() {
    }

    public String getId() {
        return uid;
    }

    public long getA_id() {
        return a_id;
    }

    public void setA_id(long a_id) {
        this.a_id = a_id;
    }

    @Override
    public String getName() {
        return uid;
    }

    public String getAvatar() {
        return "https://api.adorable.io/avatars/285/" + uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
