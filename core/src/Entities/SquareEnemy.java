package Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;

public class SquareEnemy extends Enemy {

    public SquareEnemy(){
        Preferences prefs = Gdx.app.getPreferences("My Preferences");
        this.texture = new Texture("squareEnemy.png");

        setWidth(prefs.getInteger("SquareEnemyWidth"));
        setHeight(prefs.getInteger("SquareEnemyHeight"));
        setHealth(prefs.getInteger("SquareEnemyHealth"));
        setSpeed(prefs.getInteger("SquareEnemySpeed"));
    }
}
