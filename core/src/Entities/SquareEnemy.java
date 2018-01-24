package Entities;

import com.badlogic.gdx.graphics.Texture;

public class SquareEnemy extends Enemy {

    public SquareEnemy(){
        this.health = 10;
        this.walkingSpeed = 100;
        this.texture = new Texture("squareEnemy.png");
        height = 32;
        width = 32;
    }
}
