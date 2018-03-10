package com.dk.queue;

import org.json.JSONArray;

/**
 * Created by dk on 08/03/18.
 */

public class AddParticipants {

    public final JSONArray mutual;
    public final String unknown;
    public AddParticipants(JSONArray mutual, String unknown) {

        this.mutual = mutual;
        this.unknown = unknown;
    }
}
