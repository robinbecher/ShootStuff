package Entities;

public abstract class Enemy extends Entity{

    public int speed;

    @Override
    public void act(float delta) {
        setX(getX()- speed *delta);
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
