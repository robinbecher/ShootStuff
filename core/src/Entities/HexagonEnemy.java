package Entities;

import com.badlogic.gdx.graphics.Texture;

public class HexagonEnemy extends Enemy {

    public HexagonEnemy(){
        health=15;
        walkingSpeed=80;
        texture=new Texture("hexagonEnemy.png");
        height = 32;
        width = 32;
    }
}
