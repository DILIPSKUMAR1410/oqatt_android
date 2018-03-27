package com.dk.models;

import com.dk.App;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.io.Serializable;
import java.util.Date;

import io.objectbox.Box;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

/**
 * Created by dk on 26/03/18.
 */
@Entity
public class Message implements IMessage,Serializable,
        MessageContentType.Image, /*this is for default image messages implementation*/
        MessageContentType /*and this one is for custom content type (in this case - voice message)*/ {

    @Id
    private long m_id;

    private String text;

    public long getM_id() {
        return m_id;
    }

    public void setM_id(long m_id) {
        this.m_id = m_id;
    }

    private Date createdAt;

    public ToOne<Anonymous> anon_user;

//    @Transient
//    private Image image;
//    @Transient
//    private Voice voice;

    public Message() {};

    public ToOne<Thread> getThread() {
        return thread;
    }

    public void setThread(ToOne<Thread> thread) {
        this.thread = thread;
    }

    public ToOne<Thread> thread;

    public Message(String text) {
        this( text, new Date());
    }

    public Message( String text, Date createdAt) {
        this.text = text;
        Anonymous anon = new Anonymous();
        this.anon_user.setTarget(anon);
        Box<Anonymous> anon_userBox = App.getInstance().getBoxStore().boxFor(Anonymous.class);
        anon_userBox.put(anon);
        this.createdAt = createdAt;
    }

    @Override
    public String getId() {
        return String.valueOf(m_id);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }


    @Override
    public Anonymous getUser() {
        return this.anon_user.getTarget();
    }

    @Override
    public String getImageUrl() {
        return null;
    }


//    @Override
//    public String getImageUrl() {
//        return image == null ? null : image.url;
//    }

//    public Voice getVoice() {
//        return voice;
//    }

//    public String getStatus() {
//        return "Sent";
//    }


//    public void setImage(Image image) {
//        this.image = image;
//    }

//    public void setVoice(Voice voice) {
//        this.voice = voice;
//    }

//    public static class Image {
//
//        private String url;
//
//        public Image(String url) {
//            this.url = url;
//        }
//    }

//    public static class Voice {
//
//        private String url;
//        private int duration;
//
//        public Voice(String url, int duration) {
//            this.url = url;
//            this.duration = duration;
//        }
//
//        public String getUrl() {
//            return url;
//        }
//
//        public int getDuration() {
//            return duration;
//        }
//    }
}
