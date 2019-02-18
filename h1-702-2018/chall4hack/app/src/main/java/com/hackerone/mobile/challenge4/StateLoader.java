package com.hackerone.mobile.challenge4;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class StateLoader extends StateController implements Serializable {
    private static final long serialVersionUID = 1;

    public StateLoader(String str) {
        super(str);
    }

    public void save(Context context, Object obj) {

    }

    public Object load(Context context) {
        return null;
    }
}
