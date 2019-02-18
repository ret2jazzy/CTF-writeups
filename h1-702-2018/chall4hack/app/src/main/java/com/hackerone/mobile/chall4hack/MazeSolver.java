package com.hackerone.mobile.chall4hack;

import java.util.ArrayList;


public class MazeSolver {

    private static boolean [][] curMaze = new boolean[1][1];

    public static char [][] solvedMaze;

    private static boolean [][] visited;

    public static void setMaze(boolean [][] maze){
        curMaze = maze;
    }

    public static boolean isNewMaze(boolean [][] newMaze){
        return newMaze.length != curMaze.length || newMaze[0].length != curMaze[0].length;
    }

    public static boolean canMove(Point cur){
        return cur.y > 0 && cur.y < curMaze.length && cur.x > 0 && cur.x < curMaze[cur.y].length && curMaze[cur.y][cur.x];
    }

    public static void solveMaze(Point startPoint, Point endPoint){

        solvedMaze = new char[curMaze.length][curMaze[0].length];
        visited = new boolean[curMaze.length][curMaze[0].length];
        for(int i = 0; i < curMaze.length; ++i){
            for(int y = 0; y < curMaze[i].length; ++y){
                solvedMaze[i][y] = 'N';
                visited[i][y] = false;
            }
        }

        dfsSolve(startPoint, endPoint, null, 'N');

    }

    public static boolean dfsSolve(Point curPoint, Point endPoint, Point parentPoint, char move){

        if(!canMove(curPoint) || visited[curPoint.y][curPoint.x])return false;

        if(curPoint.x == endPoint.x && curPoint.y == endPoint.y){
            return true;
        }

        visited[curPoint.y][curPoint.x] = true;

        if(parentPoint != null)solvedMaze[parentPoint.y][parentPoint.x] = move;

        return (dfsSolve(new Point(curPoint.x+1, curPoint.y), endPoint, curPoint, (char)108) || dfsSolve(new Point(curPoint.x-1, curPoint.y), endPoint, curPoint, (char)104) || dfsSolve(new Point(curPoint.x, curPoint.y+1), endPoint, curPoint, (char)106) || dfsSolve(new Point(curPoint.x, curPoint.y-1), endPoint, curPoint, (char)107));


    }

}
