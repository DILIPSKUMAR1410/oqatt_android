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

public class Anonymous implements IUser,Serializable {

    public String name;

    @Id
    private long a_id;

    private String avatar;

    public String getId() {
        return String.valueOf(a_id);
    }

    public ToOne<Thread> thread;

    public long getA_id() {
        return a_id;
    }

    public void setA_id(long a_id) {
        this.a_id = a_id;
    }

    @Override
    public String getName() {
        return "Anonymous";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
