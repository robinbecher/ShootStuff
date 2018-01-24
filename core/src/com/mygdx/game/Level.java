package com.mygdx.game;

import com.badlogic.gdx.utils.Array;

public class Level {



    public int levelIndex;

    //Number of enemies that still have to spawn
    int numOfEnemies;
    int numOfSquares;
    int numOfCircles;
    int numOfHexagons;

    boolean completed = false;

//    Array allEnemies = new Array();


    public Level(int numOfSquares,int numOfCircles, int numOfHexagons){

        this.numOfSquares = numOfSquares;
        this.numOfCircles = numOfCircles;
        this.numOfHexagons = numOfHexagons;

        this.numOfEnemies = numOfSquares+numOfCircles+numOfHexagons;

//        for (int i = 0;i<numOfSquares;i++){
//            allEnemies.add(new SquareEnemy());
//        }
//        for (int i = 0;i<numOfCircles; i++){
//            allEnemies.add(new CircleEnemy());
//        }
//        for (int i = 0;i<numOfHexagons; i++){
//            allEnemies.add(new HexagonEnemy());
//        }

    }

    public int getNumOfSquares() {
        return numOfSquares;
    }

    public void setNumOfSquares(int numOfSquares) {
        this.numOfSquares = numOfSquares;
    }

    public int getNumOfCircles() {
        return numOfCircles;
    }

    public void setNumOfCircles(int numOfCircles) {
        this.numOfCircles = numOfCircles;
    }

    public int getNumOfHexagons() {
        return numOfHexagons;
    }

    public void setNumOfHexagons(int numOfHexagons) {
        this.numOfHexagons = numOfHexagons;
    }

//    public Array getAllEnemies() {
//        return allEnemies;
//    }
//
//    public void setAllEnemies(Array allEnemies) {
//        this.allEnemies = allEnemies;
//    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean b){
        this.completed = b;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    public int getNumOfEnemies() {
        return numOfEnemies;
    }
}
