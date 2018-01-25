package Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;

public class HexagonEnemy extends Enemy {

    public HexagonEnemy(){
        Preferences prefs = Gdx.app.getPreferences("My Preferences");
        texture=new Texture("hexagonEnemy.png");

        setWidth(prefs.getInteger("HexagonEnemyWidth"));
        setHeight(prefs.getInteger("HexagonEnemyHeight"));
        setHealth(prefs.getInteger("HexagonEnemyHealth"));
        setSpeed(prefs.getInteger("HexagonEnemySpeed"));
    }



}
