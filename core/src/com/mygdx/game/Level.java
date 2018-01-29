package com.mygdx.game;

import com.badlogic.gdx.utils.Array;

public class Level {
    public int levelIndex;

    //Number of enemies that still have to spawn
    int numOfEnemies;
    int numOfSquares;
    int numOfCircles;
    int numOfHexagons;
    int numOfEnemiesAlive;

    boolean completed = false;


    public Level(int numOfSquares,int numOfCircles, int numOfHexagons, int levelIndex){

        this.numOfSquares = numOfSquares;
        this.numOfCircles = numOfCircles;
        this.numOfHexagons = numOfHexagons;

        this.numOfEnemies = numOfSquares+numOfCircles+numOfHexagons;
        this.numOfEnemiesAlive = this.numOfEnemies;

        this.levelIndex = levelIndex;

    }
    public void decreaseNumOfEnemiesAlive(){
        numOfEnemiesAlive--;
    }

    public void setLevelIndex(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    public void setNumOfEnemies(int numOfEnemies) {
        this.numOfEnemies = numOfEnemies;
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
