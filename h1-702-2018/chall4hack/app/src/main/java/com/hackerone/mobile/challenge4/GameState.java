package com.hackerone.mobile.challenge4;

import android.content.Context;
import android.util.Log;
import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1;
    public String cleanupTag;
    private Context context;
    public int levelsCompleted;
    public int playerX;
    public int playerY;
    public long seed;
    public StateController stateController;

    public GameState(int i, int i2, long j, int i3) {
        this.playerX = i;
        this.playerY = i2;
        this.seed = j;
        this.levelsCompleted = i3;
    }

    public GameState(String str, StateController stateController) {
        this.cleanupTag = str;
        this.stateController = stateController;
    }

    public void initialize(Context context) {
        this.context = context;
        GameState gameState = (GameState) this.stateController.load(context);
        if (gameState != null) {
            this.playerX = gameState.playerX;
            this.playerY = gameState.playerY;
            this.seed = gameState.seed;
            this.levelsCompleted = gameState.levelsCompleted;
        }
    }

    public void finalize() {

    }
}
