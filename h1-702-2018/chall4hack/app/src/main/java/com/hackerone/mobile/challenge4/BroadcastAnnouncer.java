package com.hackerone.mobile.challenge4;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BroadcastAnnouncer extends StateController implements Serializable {
    private static final long serialVersionUID = 1;
    private String destUrl;
    private String stringRef;
    private String stringVal;

    public BroadcastAnnouncer(String str, String str2, String str3) {
        super(str);
        this.stringRef = str2;
        this.destUrl = str3;
    }

    public void save(Context context, Object obj) {

    }

    public Object load(Context context) {
        return null;
    }

    public void setStringRef(String str) {
        this.stringRef = str;
    }

    public String getStringRef() {
        return this.stringRef;
    }
}
