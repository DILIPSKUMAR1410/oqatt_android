package com.dk.models;

/**
 * Created by dk on 26/03/18.
 */

import com.stfalcon.chatkit.commons.models.IDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;
import io.objectbox.relation.ToOne;

@Entity
public class Thread implements Serializable, IDialog<Message> {


    @Backlink
    public ToMany<Message> messages;
    @Backlink
    public ToMany<Anonymous> users;

    public ToOne<User> subject;
    public ToOne<Message> lastMessage;

    @Id
    private long t_id;

    private String dialogPhoto;

    private String dialogName;

    private boolean isNameMentioned;
    private String threadHash;
    private String passkey;
    private int unreadCount;
    private String optionString;
    private String resultString;
    private int vote_counts;

    public Thread() {
    }

    // temp just for polls
    public Thread(String name, Message message) {
        this.dialogName = name;
        this.setLastMessage(message);
        this.vote_counts = 0;
    }

    //  just for threads
    public Thread(String name, Message message, String passkey) {
        this.dialogName = name;
        this.passkey = passkey;
//        this.dialogPhoto = photo;
//        this.users = users;
        this.setLastMessage(message);
    }

    public long getT_id() {
        return t_id;
    }

    public void setT_id(long t_id) {
        this.t_id = t_id;
    }

    @Override
    public String getId() {
        return String.valueOf(t_id);
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    public void setDialogPhoto(String dialogPhoto) {
        this.dialogPhoto = "https://api.adorable.io/avatars/285/" + dialogPhoto;
        ;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public ArrayList<Anonymous> getUsers() {
        ListIterator<Anonymous> itr = this.users.listIterator();
        ArrayList<Anonymous> users = new ArrayList<>();
        while (itr.hasNext()) {
            users.add(itr.next());
        }
        return users;
    }

    @Override
    public Message getLastMessage() {
        return lastMessage.getTarget();
    }

    @Override
    public void setLastMessage(Message message) {
        this.messages.add(message);
        this.lastMessage.setTarget(message);

    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getThreadHash() {
        return threadHash;
    }

    public void setThreadHash(String threadHash) {
        this.threadHash = threadHash;
        this.dialogPhoto = "https://api.adorable.io/avatars/285/" + threadHash;
    }

    public boolean isNameMentioned() {
        return isNameMentioned;
    }

    public void setNameMentioned(boolean nameMentioned) {
        isNameMentioned = nameMentioned;
    }

    public String getPasskey() {
        return passkey;
    }

    public void setPasskey(String passkey) {
        this.passkey = passkey;
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

    public void setOptionString(String optionString) {
        this.optionString = optionString;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    public int getResultCount() {
        String[] results = resultString.split(",");
        int count = 0;
        for (String result : results) {
            count += Integer.parseInt(result);
        }
        return count;
    }

    public int getVote_counts() {
        return vote_counts;
    }

    public void increamentVote_counts() {
        this.vote_counts++;
    }
}
