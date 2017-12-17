package com.dk.models;

import java.util.ArrayList;
import java.util.Arrays;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

/**
 * Created by dk on 18/12/17.
 */
@Entity

public class Poll {
    public ToOne<User> subject;
    @Id
    private long id;
    private String question;
    private String optionString;
    private String pollHash;
    private int type;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void insertOption(String s) {
        this.optionString += s + ",";
    }

    public ArrayList<String> getOptionsList() {
        return new ArrayList<String>(Arrays.asList(optionString.split(",")));

    }

    public String getOptionString() {
        return this.optionString;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPollHash() {
        return pollHash;
    }

    public void setPollHash(String pollHash) {
        this.pollHash = pollHash;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
