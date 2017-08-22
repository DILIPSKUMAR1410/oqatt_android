package models;

/**
 * Created by dk on 21/08/17.
 */

public class User {
    public String uid;
    public String name;
    public Boolean claimed;
    public Boolean bidirectional;
    public String contact;
    public String contact_list[];

    public User(String name, boolean claimed, boolean bidirectional) {
        this.name = name;
        this.claimed = claimed;
        this.bidirectional = bidirectional;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getClaimed() {
        return claimed;
    }

    public void setClaimed(Boolean claimed) {
        this.claimed = claimed;

    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String[] getContact_list() {
        return contact_list;
    }

    public void setContact_list(String[] contact_list) {
        this.contact_list = contact_list;
    }

    public Boolean getBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(Boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}