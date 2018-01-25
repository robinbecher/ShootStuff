package Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;



public class Entity extends Actor {
    public int health;
    public Texture texture;

    public void takeDamage(int damage){
        this.health-=damage;
    }


    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Rectangle getBounds(){
        return new Rectangle(Math.round(getX()),Math.round(getY()),Math.round(getWidth()),Math.round(getHeight()));
    }
}
