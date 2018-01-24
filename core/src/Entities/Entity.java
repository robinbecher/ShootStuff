package Entities;

import com.badlogic.gdx.graphics.Texture;

import java.awt.*;

public class Entity extends Rectangle {
    public int health;
    public Texture texture;

    public void takeDamage(int damage){
        this.health-=damage;
    }
}
