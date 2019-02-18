package com.hackerone.mobile.challenge4;

import android.content.Context;

public abstract class StateController {
    private String location;

    Object load(Context context) {
        return null;
    }

    void save(Context context, Object obj) {
    }

    public StateController(String str) {
        this.location = str;
    }

    String getLocation() {
        return this.location;
    }
}
