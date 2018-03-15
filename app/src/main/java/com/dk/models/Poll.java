package com.dk.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

/**
 * Created by dk on 18/12/17.
 */
@Entity

public class Poll implements Serializable {
    public ToOne<User> subject;
    @Id
    private long id;
    private String question;
    private String optionString;
    private String resultString;
    private String pollHash;
    private long type;


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void insertOption(String s) {
        if (this.optionString == null)
            this.optionString = s + "~";
        else
            this.optionString += s + "~";
    }

    public ArrayList<String> getOptionsList() {
        if (this.optionString != null)
            return new ArrayList<String>(Arrays.asList(optionString.split("~")));
        return null;
    }

    public String getOptionString() {
        return this.optionString;
    }

    public long getType() {
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

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }
}
