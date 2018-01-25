package com.mygdx.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;

import java.awt.*;

public class Projectile extends Rectangle {
    private final Vector2 direction;
    private int damage=2;
    public int speed=10;
    public Texture texture;


    public Projectile(int x, int y, Vector2 direction){
        this.x=x;
        this.y=y;

        int rand = MathUtils.random(0,6);
        this.texture=new Texture(new FileHandle("projectile"+rand+".png"));
        this.width = 10;
        this.height = 5;
        this.direction=direction;
    }


    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Vector2 getDirection() {
        return direction;
    }
}

