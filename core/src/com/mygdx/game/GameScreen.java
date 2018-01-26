package com.mygdx.game;

import Entities.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    final ShootStuff game;
    private final OrthographicCamera camera;
    private final Music gameMusic;
    private Texture backgroundTexture;
    private float w;
    private float h;
    Array levels = new Array();
    Level currentLevel;
    Array<Enemy> enemies = new Array();
    long timeSinceLastEnemySpawn = 0;

    static final int WORLD_HEIGHT = 600;
    static final int WORLD_WIDTH = 1000;
    Stage stage, backStage;
    ExtendViewport backViewport;
    private FitViewport viewport;

    Player player;

    private static final int PLAYERAREA_WIDTH=MathUtils.round(WORLD_WIDTH*0.15f);

    private long timeSinceLastShot = 0;
    private Array<Projectile> projectiles=new Array<>();
    private Batch batch;

    //TODO: What happens if the screen resolution changes while playing?
    //TODO: Level counter in top left corner
    //TODO: Add HUD stage

    public GameScreen(final ShootStuff game) {
        this.game = game;
        Helper.initiatePreferences();
        Preferences prefs = Gdx.app.getPreferences("My Preferences");
        Gdx.input.setInputProcessor(stage);
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(WORLD_WIDTH,WORLD_HEIGHT);

        //set FitViewport to the size of the game world, add that viewport to the stage
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        stage = new Stage();
        stage.setViewport(viewport);
        batch = stage.getBatch();

        //set ExtendViewport to the size of the Screen, add that viewport to the new background stage
        backViewport = new ExtendViewport( Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
        backStage = new Stage();
        backStage.setViewport( backViewport );

        //draw Background
        backgroundTexture = new Texture(new FileHandle("background.png"));
//        backStage.addActor(new Image(backgroundTexture));

        //set Camera Position to be exactly the size of the FitViewport, which is the size of the game world
        camera.position.set(camera.viewportWidth/2f,camera.viewportHeight/2f,0);

        initializeLevels();

        currentLevel = (Level)levels.get(0);

        gameMusic = Gdx.audio.newMusic(new FileHandle("WorkerSquares.wav"));


        player = new Player(PLAYERAREA_WIDTH/2-(prefs.getInteger("playerWidth"))/2,WORLD_HEIGHT/2-(prefs.getInteger("playerHeight"))/2,
                prefs.getInteger("playerWidth"),prefs.getInteger("playerHeight"));
        player.texture=new Texture(new FileHandle("player.png"));
        stage.addActor(player);
        
    }

    private void initializeLevels() {

        XmlReader xmlReader = new XmlReader();

        XmlReader.Element root = xmlReader.parse(new FileHandle("levels.xml"));

        Array<XmlReader.Element> levelsArray = root.getChildrenByName("level");
        for (XmlReader.Element child : levelsArray){
            int numOfSquares = Integer.valueOf(child.getChildByName("sq").getAttribute("numOfSquares"));
            int numOfCircles = Integer.valueOf(child.getChildByName("ci").getAttribute("numOfCircles"));
            int numOfHexagons = Integer.valueOf(child.getChildByName("he").getAttribute("numOfHexagons"));
            levels.add(new Level(numOfSquares,numOfCircles,numOfHexagons));
        }
    }

    @Override
    public void show() {
        gameMusic.setLooping(true);
        gameMusic.play();
        Pixmap pm = new Pixmap(Gdx.files.internal("crosshairCursor.png"));
        //TODO: Change Crosshair settings to fix click inaccuracy. Hotspot?
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
        //let actors on the stage act
        stage.act(delta);
        //TODO whats going on yo
        stage.draw();
        //do camera stuff
        camera.update();
        batch.setProjectionMatrix(camera.combined);

//        batch.begin();
//        Helper.drawDebugLine(new Vector2((float)player.getCenterX()-10,(float) player.getCenterY()+10),
//                new Vector2((float)player.getCenterX()+10,(float) player.getCenterY()-10),1, Color.RED, batch.getProjectionMatrix());
//        Helper.drawDebugLine(new Vector2((float)player.getCenterX()-10,(float) player.getCenterY()-10),
//                new Vector2((float)player.getCenterX()+10,(float) player.getCenterY()+10),1, Color.RED, batch.getProjectionMatrix());
//        batch.end();

        if (TimeUtils.nanoTime() - timeSinceLastEnemySpawn > 1000000000){
            spawnRandomEnemy();
        }

        checkForInputAndSpawnProjectile();

        //Check if enemies died
        for (Enemy enemy : enemies){
            if (enemy.getX()<PLAYERAREA_WIDTH){
                enemy.setX(PLAYERAREA_WIDTH);
            }
            if (enemy.getHealth()<=0){
                enemies.removeValue(enemy, true);
                stage.getActors().removeValue(enemy,true);
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
                        stage.getActors().removeValue(projectile,true);
                    }
                }
                if (projectile.getX()>WORLD_WIDTH || projectile.getX()<0
                        || projectile.getY()<0 || projectile.getY()>WORLD_HEIGHT){
                    projectiles.removeValue(projectile,true);
                    stage.getActors().removeValue(projectile,true);
                }
            }
        }
        if (currentLevel.numOfEnemies==0){
            currentLevel.setCompleted(true);
        }
        if (currentLevel.isCompleted()){
            startNextLevel();
            showNewLevelPopup(currentLevel);
        }
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
            Vector3 touch = new Vector3(Gdx.input.getX(),Gdx.input.getY(),0);
            Vector3 scaledTouch = stage.getViewport().unproject(touch);

            System.out.println(touch.toString());

            float xClick = scaledTouch.x;
            float yClick = scaledTouch.y;
            double xPlayer = player.getCenterX();
            double yPlayer = player.getCenterY();

//            Helper.drawDebugLine(new Vector2(xClick-10,yClick+10),
//                    new Vector2(xClick+10,yClick-10),1,Color.RED,batch.getProjectionMatrix());
//            Helper.drawDebugLine(new Vector2(xClick-10,yClick-10),
//                    new Vector2(xClick+10,yClick+10),1,Color.RED,batch.getProjectionMatrix());

            Vector2 v = new Vector2((float)(xClick-xPlayer),(float)(yClick-yPlayer));
            Projectile p = new Projectile((int) Math.round(xPlayer), (int) Math.round(yPlayer), v);

            projectiles.add(p);
            stage.addActor(p);

            timeSinceLastShot = TimeUtils.nanoTime();
        }
    }

    private void showNewLevelPopup(Level currentLevel) {

    }

    private void spawnRandomEnemy() {

        if (currentLevel.numOfEnemies!=0){
            float rand = MathUtils.random();
            float squareRange=(float)currentLevel.numOfSquares / currentLevel.numOfEnemies;
            float circleRange=Float.sum(squareRange,((float) currentLevel.numOfCircles/ (float) currentLevel.numOfEnemies));

            if (rand<squareRange){
                SquareEnemy newSquare = new SquareEnemy();
                newSquare.setX(WORLD_WIDTH+20);
                newSquare.setY(new MathUtils().random(0,WORLD_HEIGHT-20));
                enemies.add(newSquare);
                stage.addActor(newSquare);
                currentLevel.numOfSquares-=1;
                currentLevel.numOfEnemies-=1;
            }else if (rand<circleRange){
                CircleEnemy newCircle = new CircleEnemy();
                newCircle.setX(WORLD_WIDTH+20);
                newCircle.setY(new MathUtils().random(0,WORLD_HEIGHT-20));
                enemies.add(newCircle);
                stage.addActor(newCircle);
                currentLevel.numOfCircles-=1;
                currentLevel.numOfEnemies-=1;
            }else{
                HexagonEnemy newHexagon = new HexagonEnemy();
                newHexagon.setX(WORLD_WIDTH+20);
                newHexagon.setY(new MathUtils().random(0,WORLD_HEIGHT-20));
                enemies.add(newHexagon);
                stage.addActor(newHexagon);
                currentLevel.numOfHexagons-=1;
                currentLevel.numOfEnemies-=1;
            }
            timeSinceLastEnemySpawn= TimeUtils.nanoTime();
        }
    }

    private void startNextLevel() {
        if (levels.size>0){
            currentLevel = (Level) levels.get(currentLevel.getLevelIndex()+1);
        }
    }

    @Override
    public void resize(int width, int height) {
            stage.getViewport().update(width, height, false);
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
