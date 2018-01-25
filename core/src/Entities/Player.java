package Entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player extends Entity {
    Vector2 center = new Vector2(0,0);

public Player(int x, int y, int width, int height){

    setBounds(x,y,width,height);

}
public double getCenterX(){
    Rectangle bounds = new Rectangle(getX(),getY(),getWidth(),getHeight());
    center = bounds.getCenter(center);
    return center.x;
}

public double getCenterY() {
    Rectangle bounds = new Rectangle(getX(),getY(),getWidth(),getHeight());
    center = bounds.getCenter(center);
    return center.y;

}
}
