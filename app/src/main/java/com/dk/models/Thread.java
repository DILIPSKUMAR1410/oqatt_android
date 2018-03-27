package com.dk.models;

/**
 * Created by dk on 26/03/18.
 */

import com.stfalcon.chatkit.commons.models.IDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;
import io.objectbox.relation.ToOne;

@Entity
public class Thread implements Serializable, IDialog<Message> {

    public ToOne<User> subject;

    //    @Backlink
    public ToMany<Message> messages;

    public long getT_id() {
        return t_id;
    }

    public void setT_id(long t_id) {
        this.t_id = t_id;
    }

    @Id
    private long t_id;

    private String dialogPhoto;

    private String dialogName;

    private boolean isNameMentioned;

    public ToMany<Anonymous> users;

    public ToOne<Message> lastMessage;

    private String threadHash;

    private int unreadCount;

    public Thread() {
    }

    public Thread(String name,Message message) {

        this.dialogName = name;
//        this.dialogPhoto = photo;
//        this.users = users;
        Anonymous anon = new Anonymous();
        message.anon_user.setTarget(anon);
//        Box<Message> messageBox = App.getInstance().getBoxStore().boxFor(Message.class);
//        messageBox.put(message);
//        this.lastMessageForCK = message;
        this.lastMessage.setTarget(message);
    }

    @Override
    public String getId() {
        return String.valueOf(t_id);
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public ArrayList<Anonymous> getUsers() {
        ListIterator<Anonymous> itr = this.users.listIterator();
        ArrayList<Anonymous> users = new ArrayList<>();
        while (itr.hasNext()){
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
        this.dialogPhoto = "https://api.adorable.io/avatars/285/"+threadHash;
    }

    public boolean isNameMentioned() {
        return isNameMentioned;
    }

    public void setNameMentioned(boolean nameMentioned) {
        isNameMentioned = nameMentioned;
    }
}
