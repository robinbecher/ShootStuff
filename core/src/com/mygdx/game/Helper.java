package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class Helper {
    private static ShapeRenderer debugRenderer = new ShapeRenderer();
    static Preferences prefs;

    public static void drawDebugLine(Vector2 start, Vector2 end, int lineWidth, Color color, Matrix4 projectionMatrix)
    {
        Gdx.gl.glLineWidth(lineWidth);
        debugRenderer.setProjectionMatrix(projectionMatrix);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(color);
        debugRenderer.line(start, end);
        debugRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public static void initiatePreferences() {
        prefs = Gdx.app.getPreferences("My Preferences");

        prefs.putInteger("playerWidth",64);
        prefs.putInteger("playerHeight",64);
        prefs.putInteger("playerHealth",500);
        //SquareEnemy
        prefs.putInteger("SquareEnemyWidth",32);
        prefs.putInteger("SquareEnemyHeight",32);
        prefs.putInteger("SquareEnemyHealth",4);
        prefs.putInteger("SquareEnemySpeed",100);
        //CircleEnemy
        prefs.putInteger("CircleEnemyWidth",32);
        prefs.putInteger("CircleEnemyHeight",32);
        prefs.putInteger("CircleEnemyHealth",8);
        prefs.putInteger("CircleEnemySpeed",50);
        //HexagonEnemy
        prefs.putInteger("HexagonEnemyWidth",32);
        prefs.putInteger("HexagonEnemyHeight",32);
        prefs.putInteger("HexagonEnemyHealth",16);
        prefs.putInteger("HexagonEnemySpeed",25);
        //Projectile
        prefs.putInteger("ProjectileWidth",10);
        prefs.putInteger("ProjectileHeight", 5);
        prefs.putInteger("ProjectileSpeed", 30);
        prefs.putInteger("ProjectileDamage", 2);

        prefs.flush();
    }
}
