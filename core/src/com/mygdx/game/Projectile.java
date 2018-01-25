package com.mygdx.game;

import Entities.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile extends Entity {
    private final Vector2 direction;
    private int damage=2;
    public int speed;


    public Projectile(int x, int y, Vector2 direction){
        Preferences prefs = Gdx.app.getPreferences("My Preferences");

        setX(x);
        setY(y);

        int rand = MathUtils.random(0,6);
        texture=new Texture(new FileHandle("projectile"+rand+".png"));
        setWidth(prefs.getInteger("ProjectileWidth"));
        setHeight(prefs.getInteger("ProjectileHeight"));
        setSpeed(prefs.getInteger("ProjectileSpeed"));
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

    public Rectangle getBounds() {
        return new Rectangle(Math.round(getX()),Math.round(getY()),Math.round(getWidth()),Math.round(getHeight()));
    }

    @Override
    public void act(float delta) {
        float distance = (speed/delta/100);
        Vector2 vector2 = getDirection().nor().scl(distance);

        setX(getX()+vector2.x);
        setY(getY()+vector2.y);
    }
}

