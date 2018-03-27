package com.dk.queue;

import com.dk.models.Thread;

/**
 * Created by dk on 27/03/18.
 */

public class UpdateThread {

        public final String message;
        public final Thread thread;

        public UpdateThread(String message, Thread thread) {

            this.message = message;
            this.thread = thread;
        }

}
