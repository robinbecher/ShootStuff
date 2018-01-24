package com.mygdx.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;

import java.awt.*;

public class Projectile extends Rectangle {
    public float xVelocity;
    public float yVelocity;
    private int damage=2;
    public Texture texture;


    public Projectile(Vector2 v){
        this.x=Math.round(v.x);
        this.y=Math.round(v.y);
        this.texture=new Texture(new FileHandle("projectile.png"));
        this.width = 10;
        this.height = 5;
    }


    public int getDamage() {
        return damage;
    }
}
