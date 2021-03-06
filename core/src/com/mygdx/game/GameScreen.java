package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Velkonost
 */
public class GameScreen extends BaseScreen {

    private final float UPDATE_TIME = 1/60f;
    private float timer = 0, timer2 = 0, timer3 = 0, timer4 = 0;
    private boolean secTimer3 = false;

    private Stage stage;

    private World world;
    public MainGame game;

    private Box2DDebugRenderer renderer;

    BitmapFont font;
    SpriteBatch sp;
    CharSequence str;

    private ArrayList<WallEntity> wall;
    private ArrayList<LightEntity> light;

    public int amountResources = 0;

    private OrthographicCamera camera;

    private boolean gravityChanged = false;

    private RocketEntity rocket;
    private EarthEntity earth;
    private MarsEntity mars;
    private FireballEntity fireball;
//    private static FireballEntity2 fireball2;

    private Texture rocketTexture;
    private Texture earthTexture;
    private Texture marsTexture;
    private Texture background;
    private Texture acceptHelp;
    private Texture fireballTexture;
    private Texture lightTexture;
    private Texture changeGravityTexture;

    private ArrayList<FireballEntity> fireballs;
    private ArrayList<Integer> fireballs_del;
    private ArrayList<Integer> lights_del;

    private float showFireball = 2f, showZeus = 15;

    private boolean isGravityChanged = false;
    private boolean haveResource = false;

    private Music music, musicFlickMars, musicFlickEarth,musicZeus, musicShot, musicFlickLight, signal;
    public LoseScreen lose;
    public WinScreen win;

    private boolean isLose = false;
    private boolean isWon = false;

    public GameScreen(MainGame game) {
        super(game);
        this.game = game;
        music = Gdx.audio.newMusic(Gdx.files.internal("audio.mp3"));
        signal = Gdx.audio.newMusic(Gdx.files.internal("signal.mp3"));
        musicZeus = Gdx.audio.newMusic(Gdx.files.internal("musicZeus.mp3"));
        musicShot = Gdx.audio.newMusic(Gdx.files.internal("shot.mp3"));
        musicFlickMars = Gdx.audio.newMusic(Gdx.files.internal("soundFlick.mp3"));
        musicFlickEarth = Gdx.audio.newMusic(Gdx.files.internal("soundFlick2.mp3"));
        musicFlickLight = Gdx.audio.newMusic(Gdx.files.internal("soundLight.mp3"));
        stage = new Stage(new FitViewport(1280, 720));
        world = new World(new Vector2(0, -25), true);

        wall = new ArrayList<WallEntity>();


    }

    @Override
    public void show() {
        lose = new LoseScreen(game);
        win = new WinScreen(game);
        music.setVolume(0.1f);
        musicShot.setVolume(0.5f);
        musicFlickEarth.setVolume(0.5f);
        musicFlickMars.setVolume(0.5f);
        musicFlickLight.setVolume(0.5f);
        signal.setVolume(0.5f);
        musicZeus.setVolume(1f);
        music.setLooping(true);

        music.play();
        fireballs = new ArrayList<FireballEntity>();
        fireballs_del = new ArrayList<Integer>();
        lights_del = new ArrayList<Integer>();
        light = new ArrayList<LightEntity>();

        renderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera(64, 36);
        camera.translate(0, 1);

        getTextures();

        rocket = new RocketEntity(rocketTexture, this, world, 1f, 5f);
        earth = new EarthEntity(earthTexture, this, world, 1f, 0f);
        mars = new MarsEntity(marsTexture, this, world, 12f, 7f);

        sp = new SpriteBatch();

        wall.add(new WallEntity(world, 10f, -2f, 30f, 1f, true, false));
        wall.add(new WallEntity(world, -1f, 0f, 1f, 30f, false, false));
        wall.add(new WallEntity(world, 14f, 0f, 1f, 30f, false, false));
        wall.add(new WallEntity(world, 10f, 8f, 30f, 1f, false, true));

        rocket.boom(true);
        stage.addActor(rocket);
        stage.addActor(earth);
        stage.addActor(mars);

        for (WallEntity aWall : wall) {
            stage.addActor(aWall);
        }

        font = new BitmapFont();

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                if ((fixtureA.getUserData().equals("wallDown") && fixtureB.getUserData().equals("rocket"))
                        || (fixtureA.getUserData().equals("rocket") && fixtureB.getUserData().equals("wallDown"))) {
                    isLose = true;
                }
                if ((fixtureA.getUserData().equals("wallUp") && fixtureB.getUserData().equals("rocket"))
                        || (fixtureA.getUserData().equals("rocket") && fixtureB.getUserData().equals("wallUp"))) {
                    if (gravityChanged) isLose = true;
                }

                if ((fixtureA.getUserData().equals("mars") && fixtureB.getUserData().equals("rocket"))
                        || (fixtureA.getUserData().equals("rocket") && fixtureB.getUserData().equals("mars"))) {
                    if(!haveResource) {
                        haveResource = true;
                        musicFlickMars.stop();
                        musicFlickMars.play();
                    }
                }

                if ((fixtureA.getUserData().equals("earth") && fixtureB.getUserData().equals("rocket"))
                        || (fixtureA.getUserData().equals("rocket") && fixtureB.getUserData().equals("earth"))) {

                    if (haveResource) {
                        musicFlickEarth.stop();
                        musicFlickEarth.play();
                        haveResource = false;
                        amountResources ++;
                        if (amountResources < 5) {
                            showFireball -= 0.3f;
                            rocket.speed += 0.3f;
                        }
                        else {
                            showFireball-=0.1f;
                            rocket.speed += 0.2f;
                        }

                        showZeus+=1f;
                    }
                    if(amountResources==9){
                        isGravityChanged = true;
                    }

                    if (amountResources == 11) {
                        isWon = true;
                    }

                }
                for(int i = 0; i<fireballs.size(); i++) {
                    if ((fixtureA.getUserData().equals("fireball"+i) && fixtureB.getUserData().equals("wall"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } else if ((fixtureB.getUserData().equals("fireball"+i) && fixtureA.getUserData().equals("wall"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } if ((fixtureA.getUserData().equals("fireball"+i) && fixtureB.getUserData().equals("wallDown"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } else if ((fixtureB.getUserData().equals("fireball"+i) && fixtureA.getUserData().equals("wallDown"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    }
                    if ((fixtureA.getUserData().equals("fireball"+i) && fixtureB.getUserData().equals("wallUp"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } else if ((fixtureB.getUserData().equals("fireball"+i) && fixtureA.getUserData().equals("wallUp"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    }
                    else if ((fixtureA.getUserData().equals("fireball"+i) && fixtureB.getUserData().equals("rocket"))) {
                        musicShot.stop();
                        musicShot.play();
                        rocket.health -= 10;
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } else if ((fixtureB.getUserData().equals("fireball"+i) && fixtureA.getUserData().equals("rocket"))) {
                        musicShot.stop();
                        musicShot.play();
                        rocket.health -= 10;
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } else if ((fixtureA.getUserData().equals("fireball"+i) && fixtureB.getUserData().equals("earth"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } else if ((fixtureB.getUserData().equals("fireball"+i) && fixtureA.getUserData().equals("earth"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } else if ((fixtureA.getUserData().equals("fireball"+i) && fixtureB.getUserData().equals("mars"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    } else if ((fixtureB.getUserData().equals("fireball"+i) && fixtureA.getUserData().equals("mars"))) {
                        fireballs_del.add(i);
                        System.out.println("fireball"+i);
                    }

                    for(int j = 0; j<light.size(); j++){
                        if ((fixtureA.getUserData().equals("fireball"+i) && fixtureB.getUserData().equals("light"+j))) {
                            fireballs_del.add(i);
                            lights_del.add(j);
                        } else if ((fixtureB.getUserData().equals("fireball"+i) && fixtureA.getUserData().equals("light"+j))) {
                            fireballs_del.add(i);
                            lights_del.add(j);
                        }
                    }
                }

                for(int i = 0; i<light.size(); i++) {
                    if ((fixtureA.getUserData().equals("light" + i) && fixtureB.getUserData().equals("wall"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    } else if ((fixtureB.getUserData().equals("light" + i) && fixtureA.getUserData().equals("wall"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    } if ((fixtureA.getUserData().equals("light" + i) && fixtureB.getUserData().equals("wallDown"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    } else if ((fixtureB.getUserData().equals("light" + i) && fixtureA.getUserData().equals("wallDown"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    }
                    if ((fixtureA.getUserData().equals("light" + i) && fixtureB.getUserData().equals("wallUp"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    } else if ((fixtureB.getUserData().equals("light" + i) && fixtureA.getUserData().equals("wallUp"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    }

                    else if ((fixtureA.getUserData().equals("light" + i) && fixtureB.getUserData().equals("rocket"))) {
                        if(rocket.health<100) {
                            rocket.health += 10;
                            if (rocket.health > 100) rocket.health = 100;
                        }
                        musicFlickLight.stop();
                        musicFlickLight.play();

                        lights_del.add(i);
                        System.out.println("light" + i);
                    } else if ((fixtureB.getUserData().equals("light" + i) && fixtureA.getUserData().equals("rocket"))) {
                        if(rocket.health<100) {
                            rocket.health += 10;
                            if (rocket.health > 100) rocket.health = 100;
                        }


                        musicFlickLight.stop();
                        musicFlickLight.play();

                        lights_del.add(i);
                        System.out.println("light" + i);
                    } else if ((fixtureA.getUserData().equals("light" + i) && fixtureB.getUserData().equals("earth"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    } else if ((fixtureB.getUserData().equals("light" + i) && fixtureA.getUserData().equals("earth"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    } else if ((fixtureA.getUserData().equals("light" + i) && fixtureB.getUserData().equals("mars"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    } else if ((fixtureB.getUserData().equals("light" + i) && fixtureA.getUserData().equals("mars"))) {
                        lights_del.add(i);
                        System.out.println("light" + i);
                    }

                    for(int j = 0; j<light.size(); j++){
                        if ((fixtureA.getUserData().equals("light"+i) && fixtureB.getUserData().equals("light"+j))) {
                            lights_del.add(i);
                            lights_del.add(j);
                        } else if ((fixtureB.getUserData().equals("light"+i) && fixtureA.getUserData().equals("light"+j))) {
                            lights_del.add(i);
                            lights_del.add(j);
                        }
                    }
                }



            }
            @Override
            public void endContact(Contact contact) {

                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

            }
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {


            }
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    @Override
    public void render(float delta) {
        if (isWon) {
            music.stop();
            signal.stop();
            game.setScreen(win);
        }
        if (amountResources >= 9) {
            world.setGravity(new Vector2(0, 25));
            gravityChanged = true;
        }


        lose = new LoseScreen(game, amountResources);
        if (rocket.health <= 0 || isLose) {
            music.stop();
            signal.stop();  
            game.setScreen(lose);
        }

        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (rocket.health <= 30) {
            rocket.texture = game.getManager().get("player_main_crash2.png");
        } else if (rocket.health <= 70) {
            rocket.texture  = game.getManager().get("player_main_crash.png");
        } else {
            rocket.texture  = game.getManager().get("player_main.png");
        }

        if(fireballs_del.size()>0){
            for(int i = 0; i<fireballs_del.size(); i++){
                if(!fireballs.get(fireballs_del.get(i)).isRemoved) {
                    fireballs.get(fireballs_del.get(i)).addAction(Actions.removeActor());
                    fireballs.get(fireballs_del.get(i)).removeFixture();
                    world.destroyBody(fireballs.get(fireballs_del.get(i)).getFixture().getBody());
                }
            }
        }
        if(lights_del.size()>0){
            for(int i = 0; i<lights_del.size(); i++){
                if(!light.get(lights_del.get(i)).isRemoved) {
                    light.get(lights_del.get(i)).addAction(Actions.removeActor());
                    light.get(lights_del.get(i)).removeFixture();
                    world.destroyBody(light.get(lights_del.get(i)).getFixture().getBody());
                }
            }
        }
        timer+=delta;
        timer2+=delta;

        if(timer>showFireball){
            Map<String, FireballEntity> fireballNames = new HashMap<String, FireballEntity>();
            fireballNames.put("fireball"+(fireballs.size()-1),new FireballEntity(fireballTexture, this, world, 5.5f, 6.8f, rocket.getX(), rocket.getY()));
            fireballs.add(fireballNames.get("fireball"+(fireballs.size()-1)));
            fireballs.get(fireballs.size()-1).setUserData(fireballs.size()-1);
            stage.addActor(fireballs.get(fireballs.size()-1));
            timer = 0;
        }




        if(timer2>showZeus){
            light.clear();
            lights_del.clear();
            light.add(new LightEntity(lightTexture, this, world, 2.5f, 6.5f, "top left", 0));
            light.add(new LightEntity(lightTexture, this, world, 10.5f, 5.5f, "top right", 1));
            light.add(new LightEntity(lightTexture, this, world, 1.5f, 1f, "bot left", 2));
            light.add(new LightEntity(lightTexture, this, world, 11.5f, 1.5f, "bot right", 3));

            for (LightEntity aLight : light) {
                stage.addActor(aLight);
            }
            timer2 = 0;
            secTimer3 = true;
            musicZeus.play();
        }
        if(timer3>3){
            timer3 = 0;
            secTimer3 = false;
            musicZeus.stop();
        }
        stage.act();

        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, 1280, 720);
        font.getData().setScale(1, 1);
        font.draw(stage.getBatch(), "Destination: " + (haveResource ? "Earth" : "Mars"), Gdx.graphics.getWidth()-200,  Gdx.graphics.getHeight()-25);
        font.draw(stage.getBatch(), "Resources: " + String.valueOf(amountResources), Gdx.graphics.getWidth()-200,  Gdx.graphics.getHeight()-50);
        font.draw(stage.getBatch(), "Energy: " + String.valueOf(rocket.health), Gdx.graphics.getWidth()-200,  Gdx.graphics.getHeight()-75);


        if(secTimer3){
            timer3+=delta;
            stage.getBatch().draw(acceptHelp, 0, 0, 1280, 720);
        }
        if(isGravityChanged){
            timer4+=delta;
            signal.play();
            stage.getBatch().draw(changeGravityTexture, 0, 0, 1280, 720);
            if(timer4>3){
                isGravityChanged = false;
                signal.stop();
                timer4 = 0;
            }
        }
        stage.getBatch().end();
        rocket.processInput();
        world.step(delta, 6, 2);
        camera.update();
//        renderer.render(world, camera.combined);

        stage.draw();


    }

    private void getTextures() {

        rocketTexture = game.getManager().get("player_main.png");
        earthTexture = game.getManager().get("earth.png");
        marsTexture = game.getManager().get("mars.png");
        fireballTexture = game.getManager().get("fireball.png");
        background = game.getManager().get("background.png");
        lightTexture = game.getManager().get("light.png");
        acceptHelp = game.getManager().get("accepthelp.png");
        changeGravityTexture = game.getManager().get("gravityChange.png");



    }

    public void hide() {
        rocket.detach();
        earth.detach();
        mars.detach();
      //  fireball.detach();
//        fireball2.detach();

        rocket.remove();
        mars.remove();
        earth.remove();
    //    fireball.remove();
//        fireball2.remove();
    }

    @Override
    public void dispose() {
        stage.dispose();
        world.dispose();
        renderer.dispose();
    }

 
}
