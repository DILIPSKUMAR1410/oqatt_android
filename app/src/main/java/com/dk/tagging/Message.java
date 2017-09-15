package com.dk.tagging;

import com.hootsuite.nachos.NachoTextView;

/**
 * Created by dk on 05/09/17.
 */

public class Message {
    private String message;
    private NachoTextView nachoTextView;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NachoTextView getNachoTextView() {
        return nachoTextView;
    }

    public void setNachoTextView(NachoTextView nachoTextView) {
        this.nachoTextView = nachoTextView;
    }
}
