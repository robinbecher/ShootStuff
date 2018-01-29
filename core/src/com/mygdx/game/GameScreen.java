package com.mygdx.game;

import Entities.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    final ShootStuff game;
    private final OrthographicCamera camera;
    private final Music gameMusic;
    private final Stage hudStage;
    private Batch hudStageBatch;
    private Texture backgroundTexture;
    Array levels = new Array();
    Level currentLevel;
    Array<Enemy> enemies = new Array();
    long timeSinceLastEnemySpawn = 0;

    static final int WORLD_HEIGHT = 600;
    static final int WORLD_WIDTH = 1000;
    Stage gameStage, backStage;
    ExtendViewport backViewport;
    private FitViewport viewport;

    Player player;

    private static final int PLAYERAREA_WIDTH=MathUtils.round(WORLD_WIDTH*0.15f);

    private long timeSinceLastShot = 0;
    private Array<Projectile> projectiles=new Array<>();
    private Batch batch;
    private boolean gameOver;

    //TODO: What happens if the screen resolution changes while playing?
    //TODO: Level counter in top left corner
    //TODO: Add HUD gameStage

    public GameScreen(final ShootStuff game) {
        this.game = game;
        Helper.initiatePreferences();
        Preferences prefs = Gdx.app.getPreferences("My Preferences");
        Gdx.input.setInputProcessor(gameStage);
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(WORLD_WIDTH,WORLD_HEIGHT);

        //set FitViewport to the size of the game world, add that viewport to the gameStage
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        gameStage = new Stage();
        gameStage.setViewport(viewport);
        batch = gameStage.getBatch();

        //set ExtendViewport to the size of the Screen, add that viewport to the new background gameStage
        backViewport = new ExtendViewport( Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
        backStage = new Stage();
        backStage.setViewport( backViewport );

        //draw Background
        backgroundTexture = new Texture(new FileHandle("background.png"));

        hudStage = new Stage();
        hudStage.setViewport(viewport);
        hudStageBatch = hudStage.getBatch();


        //set Camera Position to be exactly the size of the FitViewport, which is the size of the game world
        camera.position.set(camera.viewportWidth/2f,camera.viewportHeight/2f,0);

        initializeLevels();

        currentLevel = (Level)levels.get(0);

        gameMusic = Gdx.audio.newMusic(new FileHandle("WorkerSquares.wav"));


        player = new Player(PLAYERAREA_WIDTH/2-(prefs.getInteger("playerWidth"))/2,WORLD_HEIGHT/2-(prefs.getInteger("playerHeight"))/2,
                prefs.getInteger("playerWidth"),prefs.getInteger("playerHeight"));
        player.texture=new Texture(new FileHandle("player.png"));
        gameStage.addActor(player);

        gameOver=false;
        
    }

    private void initializeLevels() {

        XmlReader xmlReader = new XmlReader();

        XmlReader.Element root = xmlReader.parse(new FileHandle("levels.xml"));

        Array<XmlReader.Element> levelsArray = root.getChildrenByName("level");
        int i = 1;
        for (XmlReader.Element child : levelsArray){
            int numOfSquares = Integer.valueOf(child.getChildByName("sq").getAttribute("numOfSquares"));
            int numOfCircles = Integer.valueOf(child.getChildByName("ci").getAttribute("numOfCircles"));
            int numOfHexagons = Integer.valueOf(child.getChildByName("he").getAttribute("numOfHexagons"));
            levels.add(new Level(numOfSquares,numOfCircles,numOfHexagons,i));
            i++;
        }
    }

    @Override
    public void show() {
        gameMusic.setLooping(true);
        gameMusic.play();
        Pixmap pm = new Pixmap(Gdx.files.internal("crosshairCursor.png"));
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 16, 16));
        pm.dispose();
        Gdx.gl.glClearColor(0,0,0,1);

    }

    @Override
    public void render(float delta) {
        //Clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //draw Background
        drawBackground();
        //let actors on the gameStage act
        gameStage.act(delta);
        //all actors on the gamestage draw themselves
        gameStage.draw();
        //let actors on the hudStage act
        hudStage.act(delta);
        //all actors on the hudStage draw themselves
        hudStage.draw();

        //do camera stuff
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (TimeUtils.nanoTime() - timeSinceLastEnemySpawn > 1000000000){
            spawnLevelEnemy();
        }

        checkForInputAndSpawnProjectile();

        //Check if enemies died
        for (Enemy enemy : enemies){
            if (enemy.getX()<PLAYERAREA_WIDTH){
                enemy.setX(PLAYERAREA_WIDTH);
            }
            if (enemy.getHealth()<=0){
                enemies.removeValue(enemy, true);
                gameStage.getActors().removeValue(enemy,true);
                currentLevel.decreaseNumOfEnemiesAlive();
            }
        }

        //projectile collision checks
        if(projectiles.size>0){
            for (Projectile projectile : projectiles){

                for (Enemy enemy : enemies){
                    Rectangle rect1 = projectile.getBounds();
                    Rectangle rect2 = enemy.getBounds();
                    if (rect1.overlaps(rect2)){
                        enemy.takeDamage(projectile.getDamage());
                        projectiles.removeValue(projectile,true);
                        gameStage.getActors().removeValue(projectile,true);
                    }
                }
                if (projectile.getX()>WORLD_WIDTH || projectile.getX()<0
                        || projectile.getY()<0 || projectile.getY()>WORLD_HEIGHT){
                    projectiles.removeValue(projectile,true);
                    gameStage.getActors().removeValue(projectile,true);
                }
            }
        }
        if (currentLevel.numOfEnemiesAlive==0){
            currentLevel.setCompleted(true);
        }
        if (currentLevel.isCompleted() && !gameOver){
            if(levels.size>currentLevel.getLevelIndex()){
                startNextLevel();
                showNewLevelPopup(currentLevel);
            }else{
                System.out.println("YOU WIN!!!!!!!!!!");
                gameOver=true;
                showEndingScreen();
            }
        }
    }

    private void showEndingScreen() {

    }

    private void drawBackground() {
        backStage.getBatch().begin();
        backStage.getBatch().disableBlending();
        backStage.getBatch().draw(backgroundTexture,0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        backStage.getBatch().enableBlending();
        backStage.getBatch().end();
    }

    private void checkForInputAndSpawnProjectile() {
        if (Gdx.input.isTouched() && (TimeUtils.nanoTime()-timeSinceLastShot)>100000000){
            //Actual touch Vector
            Vector3 touch = new Vector3(Gdx.input.getX(),Gdx.input.getY(),0);
            //Scaled touch Vector - Taking Screen scaling into account
            Vector3 scaledTouch = gameStage.getViewport().unproject(touch);

            //touch position and player position
            float xClick = scaledTouch.x;
            float yClick = scaledTouch.y;
            double xPlayer = player.getCenterX();
            double yPlayer = player.getCenterY();

            //determine vector of possible new projectile
            Vector2 v = new Vector2((float)(xClick-xPlayer),(float)(yClick-yPlayer));

            //if click is not too far left
            if (xClick>PLAYERAREA_WIDTH){
                //spawn a projectile
                Projectile p = new Projectile((int) Math.round(xPlayer), (int) Math.round(yPlayer), v);
                projectiles.add(p);
                gameStage.addActor(p);
                timeSinceLastShot = TimeUtils.nanoTime();
            }
        }
    }

    private void showNewLevelPopup(Level currentLevel) {
        Skin skin = new Skin();
        hudStage.addActor(new Label("Level: "+currentLevel.getLevelIndex(), skin));
    }

    private void spawnLevelEnemy() {

        if (currentLevel.numOfEnemies!=0){
            float rand = MathUtils.random();
            float squareRange=(float)currentLevel.numOfSquares / currentLevel.numOfEnemies;
            float circleRange=Float.sum(squareRange,((float) currentLevel.numOfCircles/ (float) currentLevel.numOfEnemies));

            if (rand<squareRange){
                SquareEnemy newSquare = new SquareEnemy();
                newSquare.setX(WORLD_WIDTH+20);
                newSquare.setY(new MathUtils().random(0,WORLD_HEIGHT-newSquare.getHeight()));
                enemies.add(newSquare);
                gameStage.addActor(newSquare);
                currentLevel.numOfSquares-=1;
                currentLevel.numOfEnemies-=1;
            }else if (rand<circleRange){
                CircleEnemy newCircle = new CircleEnemy();
                newCircle.setX(WORLD_WIDTH+20);
                newCircle.setY(new MathUtils().random(0,WORLD_HEIGHT-newCircle.getHeight()));
                enemies.add(newCircle);
                gameStage.addActor(newCircle);
                currentLevel.numOfCircles-=1;
                currentLevel.numOfEnemies-=1;
            }else{
                HexagonEnemy newHexagon = new HexagonEnemy();
                newHexagon.setX(WORLD_WIDTH+20);
                newHexagon.setY(new MathUtils().random(0,WORLD_HEIGHT-newHexagon.getHeight()));
                enemies.add(newHexagon);
                gameStage.addActor(newHexagon);
                currentLevel.numOfHexagons-=1;
                currentLevel.numOfEnemies-=1;
            }
            timeSinceLastEnemySpawn= TimeUtils.nanoTime();
        }
    }

    private void startNextLevel() {
        currentLevel = (Level) levels.get(currentLevel.getLevelIndex());
        System.out.println("Level: "+currentLevel.levelIndex);
    }

    @Override
    public void resize(int width, int height) {
            gameStage.getViewport().update(width, height, false);
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
