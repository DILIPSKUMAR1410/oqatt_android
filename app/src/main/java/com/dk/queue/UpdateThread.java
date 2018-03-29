package com.dk.queue;

import com.dk.models.Thread;

/**
 * Created by dk on 27/03/18.
 */

public class UpdateThread {

    public final int type;
    public final Thread thread;

    public UpdateThread(int type, Thread thread) {

        this.type = type;
        this.thread = thread;
    }

}
