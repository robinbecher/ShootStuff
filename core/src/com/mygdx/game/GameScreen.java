package com.mygdx.game;

import Entities.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.XmlReader;

public class GameScreen implements Screen {
    final ShootStuff game;
    private final OrthographicCamera camera;
    private final Music gameMusic;
    Array levels = new Array();
    Level currentLevel;
    Array<Enemy> enemies = new Array();
    long timeSinceLastEnemySpawn = 0;
    int screenHeight = 600;
    int screenWidth = 1000;
    private int playerAreaWidth = 150;
    Player player = new Player();
    private int enemiesSpawnedThisLevel;
    private long timeSinceLastShot = 0;
    private Array<Projectile> projectiles=new Array<>();


    public GameScreen(final ShootStuff game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        
        initializeLevels();

        currentLevel = (Level)levels.get(0);

        gameMusic = Gdx.audio.newMusic(new FileHandle("WorkerSquares.wav"));

        player.x=playerAreaWidth/2;
        player.y=screenHeight/2;
        player.texture=new Texture(new FileHandle("player.png"));
        
    }

    private void initializeLevels() {

        XmlReader xmlReader = new XmlReader();

        XmlReader.Element root = xmlReader.parse(new FileHandle("levels.xml"));

        Array<XmlReader.Element> levelsArray = root.getChildrenByName("level");
        int i = 1;
        for (XmlReader.Element child : levelsArray){
//            int numOfSquares = Integer.valueOf(child.getChildByName("level".concat(String.valueOf(i))).getAttribute("numOfSquares"));
            int numOfSquares = Integer.valueOf(child.getChildByName("sq").getAttribute("numOfSquares"));
//            int numOfCircles = Integer.valueOf(child.getChildByName("level".concat(String.valueOf(i))).getAttribute("numOfCircles"));
            int numOfCircles = Integer.valueOf(child.getChildByName("ci").getAttribute("numOfCircles"));
//            int numOfHexagons = Integer.valueOf(child.getChildByName("level".concat(String.valueOf(i))).getAttribute("numOfHexagons"));
            int numOfHexagons = Integer.valueOf(child.getChildByName("he").getAttribute("numOfHexagons"));
            levels.add(new Level(numOfSquares,numOfCircles,numOfHexagons));
        }
    }

    @Override
    public void show() {
        gameMusic.setLooping(true);
        gameMusic.play();


    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(179/255f, 224/255f, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        //Draw everything onto the screen
        game.batch.begin();
        for (Enemy enemy : enemies) {
            game.batch.draw(enemy.texture, enemy.x, enemy.y);
        }
        for (Projectile projectile : projectiles){
            game.batch.draw(projectile.texture,projectile.x,projectile.y);
        }
        game.batch.draw(player.texture,player.x,player.y);
        game.batch.end();

        if (TimeUtils.nanoTime() - timeSinceLastEnemySpawn > 1000000000){
            spawnRandomEnemy();
        }

        //Update enemies' position and check if they died
        for (Enemy enemy : enemies){
            enemy.x -= enemy.walkingSpeed * Gdx.graphics.getDeltaTime();
            if (enemy.x<playerAreaWidth){
                enemy.x=playerAreaWidth;
            }
            if (enemy.health<=0){
                enemies.removeValue(enemy, true);
            }
        }

        //Update Projectile positions and check if they collided with an enemy
        if(projectiles.size>0){
            for (Projectile projectile : projectiles){
                int i=0;

                projectile.x+=Gdx.graphics.getDeltaTime()*projectile.xVelocity;
                projectile.y+=Gdx.graphics.getDeltaTime()*projectile.yVelocity;

                for (Enemy enemy : enemies){
                    if (projectile.intersects(enemy)){
                        enemy.takeDamage(projectile.getDamage());
                    }
                }
                if (projectile.x>screenWidth || projectile.x<projectile.width
                        || projectile.y<projectile.height || projectile.y>screenHeight-projectile.height){
                    projectiles.removeIndex(i);
                }
                i++;
            }
        }

        if (Gdx.input.isTouched() && (TimeUtils.nanoTime()-timeSinceLastShot)>100000000){
            float x = Gdx.input.getX();
            float y = Gdx.input.getY();

            Projectile p = new Projectile(new Vector2((float) player.getCenterX(), (float) player.getCenterY()));
            p.xVelocity=(float) ((y-player.getCenterY())/(x-player.getCenterX()));
            p.yVelocity=1/p.xVelocity;
            projectiles.add(p);

            timeSinceLastShot = TimeUtils.nanoTime();
        }

        if (currentLevel.isCompleted()){
            startNextLevel();
        }

    }

    private void spawnRandomEnemy() {

        if (currentLevel.numOfEnemies!=0){
            float rand = MathUtils.random();
            float squareRange=(float)currentLevel.numOfSquares / currentLevel.numOfEnemies;
            float circleRange=Float.sum(squareRange,((float) currentLevel.numOfCircles/ (float) currentLevel.numOfEnemies));

            if (rand<squareRange){
                SquareEnemy newSquare = new SquareEnemy();
                newSquare.x = screenWidth+20;
                newSquare.y = new MathUtils().random(0,screenHeight-20);
                enemies.add(newSquare);
                currentLevel.numOfSquares-=1;
                currentLevel.numOfEnemies-=1;
            }else if (rand<circleRange){
                CircleEnemy newCircle = new CircleEnemy();
                newCircle.x = screenWidth+20;
                newCircle.y = new MathUtils().random(0,screenHeight-20);
                enemies.add(newCircle);
                currentLevel.numOfCircles-=1;
                currentLevel.numOfEnemies-=1;
            }else{
                HexagonEnemy newHexagon = new HexagonEnemy();
                newHexagon.x = screenWidth+20;
                newHexagon.y = new MathUtils().random(0,screenHeight-20);
                enemies.add(newHexagon);
                currentLevel.numOfHexagons-=1;
                currentLevel.numOfEnemies-=1;
            }
            timeSinceLastEnemySpawn= TimeUtils.nanoTime();
        }else{
            currentLevel.setCompleted(true);
        }
    }

    private void startNextLevel() {
        if (levels.size>0){
            currentLevel = (Level) levels.get(currentLevel.getLevelIndex()+1);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {


    }
}
