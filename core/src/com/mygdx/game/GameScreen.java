package com.mygdx.game;

import Entities.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
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

    private static final int PLAYERAREA_WIDTH=MathUtils.round(WORLD_WIDTH*0.15f);

    Player player = new Player();
    private long timeSinceLastShot = 0;
    private Array<Projectile> projectiles=new Array<>();

    //TODO: What happens if the screen resolution changes while playing?

    public GameScreen(final ShootStuff game) {
        this.game = game;
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(WORLD_WIDTH,WORLD_HEIGHT*(h/w));

        //set FitViewport to the size of the game world, add that viewport to the stage
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        stage = new Stage();
        stage.setViewport(viewport);
        Gdx.input.setInputProcessor(stage);

        //set ExtendViewport to the size of the Screen, add that viewport to the new background stage
        backViewport = new ExtendViewport( Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
        backStage = new Stage();
        backStage.setViewport( backViewport );

        //draw Background
        Texture backgroundTexture = new Texture(new FileHandle("background.png"));
        backStage.addActor(new Image(backgroundTexture));

        //set Camera Position to be exactly the size of the FitViewport, which is the size of the game world
        camera.position.set(camera.viewportWidth/2f,camera.viewportHeight/2f,0);

        initializeLevels();

        currentLevel = (Level)levels.get(0);

        gameMusic = Gdx.audio.newMusic(new FileHandle("WorkerSquares.wav"));

        player.x=PLAYERAREA_WIDTH/2-player.width/2;
        player.y=WORLD_HEIGHT/2-player.height/2;
        player.texture=new Texture(new FileHandle("player.png"));
        
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
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
        pm.dispose();

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        backStage.act();

        backStage.draw(); //backStage first - we want it under stage
        stage.draw();

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        //Draw everything onto the screen
        game.batch.begin();
        game.batch.draw(player.texture,player.x,player.y);
        for (Enemy enemy : enemies) {
            game.batch.draw(enemy.texture, enemy.x, enemy.y);
        }
        for (Projectile projectile : projectiles){
            game.batch.draw(projectile.texture,projectile.x,projectile.y);
        }
        //Draw an X at 0,0
        Helper.drawDebugLine(new Vector2((float)player.getCenterX()-10,(float) player.getCenterY()+10),
                new Vector2((float)player.getCenterX()+10,(float) player.getCenterY()-10),1, Color.RED, game.batch.getProjectionMatrix());
        Helper.drawDebugLine(new Vector2((float)player.getCenterX()-10,(float) player.getCenterY()-10),
                new Vector2((float)player.getCenterX()+10,(float) player.getCenterY()+10),1, Color.RED, game.batch.getProjectionMatrix());
        game.batch.end();

        if (TimeUtils.nanoTime() - timeSinceLastEnemySpawn > 1000000000){
            spawnRandomEnemy();
        }

        //Update enemies' position and check if they died
        for (Enemy enemy : enemies){
            enemy.x -= enemy.walkingSpeed * Gdx.graphics.getDeltaTime();
            if (enemy.x<PLAYERAREA_WIDTH){
                enemy.x=PLAYERAREA_WIDTH;
            }
            if (enemy.health<=0){
                enemies.removeValue(enemy, true);
            }
        }

        //Update Projectile positions and check if they collided with an enemy
        if(projectiles.size>0){
            for (Projectile projectile : projectiles){
                int i=0;
                float distance = (projectile.speed/Gdx.graphics.getDeltaTime()/100);
                Vector2 vector2 = projectile.getDirection().nor().scl(distance);

                projectile.x+=vector2.x;
                projectile.y+=vector2.y;

                for (Enemy enemy : enemies){
                    if (projectile.intersects(enemy)){
                        enemy.takeDamage(projectile.getDamage());
                    }
                }
                if (projectile.x>WORLD_WIDTH || projectile.x<0
                        || projectile.y<0 || projectile.y>WORLD_HEIGHT){
                    projectiles.removeIndex(i);
                }
                i++;
            }
        }

        if (Gdx.input.isTouched() && (TimeUtils.nanoTime()-timeSinceLastShot)>100000000){
            Vector3 touch = new Vector3(Gdx.input.getX(),Gdx.input.getY(),0);
            Vector3 scaledTouch = camera.unproject(touch);

            float xClick = scaledTouch.x;
            float yClick = scaledTouch.y;
            double xPlayer = player.getCenterX();
            double yPlayer = player.getCenterY();

            System.out.println("Click Pos: "+(xClick)+", "+yClick);

            Helper.drawDebugLine(new Vector2(xClick-10,yClick+10),
                                new Vector2(xClick+10,yClick-10),1,Color.RED,game.batch.getProjectionMatrix());
            Helper.drawDebugLine(new Vector2(xClick-10,yClick-10),
                    new Vector2(xClick+10,yClick+10),1,Color.RED,game.batch.getProjectionMatrix());


            Vector2 v = new Vector2((float)(xClick-xPlayer),(float)(yClick-yPlayer));
            Projectile p = new Projectile((int) Math.round(xPlayer), (int) Math.round(yPlayer), v);


            projectiles.add(p);

            timeSinceLastShot = TimeUtils.nanoTime();
        }

        if (currentLevel.isCompleted()){
            startNextLevel();
            showNewLevelPopup(currentLevel);
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
                newSquare.x = WORLD_WIDTH+20;
                newSquare.y = new MathUtils().random(0,WORLD_HEIGHT-20);
                enemies.add(newSquare);
                currentLevel.numOfSquares-=1;
                currentLevel.numOfEnemies-=1;
            }else if (rand<circleRange){
                CircleEnemy newCircle = new CircleEnemy();
                newCircle.x = WORLD_WIDTH+20;
                newCircle.y = new MathUtils().random(0,WORLD_HEIGHT-20);
                enemies.add(newCircle);
                currentLevel.numOfCircles-=1;
                currentLevel.numOfEnemies-=1;
            }else{
                HexagonEnemy newHexagon = new HexagonEnemy();
                newHexagon.x = WORLD_WIDTH+20;
                newHexagon.y = new MathUtils().random(0,WORLD_HEIGHT-20);
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
            viewport.update(width, height);
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
