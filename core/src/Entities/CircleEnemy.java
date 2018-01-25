package Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;

public class CircleEnemy extends Enemy {

    public CircleEnemy(){
        Preferences prefs = Gdx.app.getPreferences("My Preferences");
        texture= new Texture("circleEnemy.png");

        setWidth(prefs.getInteger("CircleEnemyWidth"));
        setHeight(prefs.getInteger("CircleEnemyHeight"));
        setHealth(prefs.getInteger("CircleEnemyHealth"));
        setSpeed(prefs.getInteger("CircleEnemySpeed"));
    }
}
