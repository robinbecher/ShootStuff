package Entities;

import com.badlogic.gdx.graphics.Texture;

public class CircleEnemy extends Enemy {

    public CircleEnemy(){
        health=8;
        walkingSpeed=120;
        texture= new Texture("circleEnemy.png");
        height = 32;
        width = 32;
    }
}
