package com.dk.queue;

import org.json.JSONArray;

/**
 * Created by dk on 08/03/18.
 */

public class AddParticipants {

    public final JSONArray mutual;
    public final int unknown;
    public AddParticipants(JSONArray mutual, int unknown) {

        this.mutual = mutual;
        this.unknown = unknown;
    }
}
