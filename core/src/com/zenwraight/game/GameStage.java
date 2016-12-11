package com.zenwraight.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kavitmht on 12/10/2016.
 */
public class GameStage extends Stage implements ContactListener{

    private final GameScreen gameScreen;

    private World world;
    private Box2DDebugRenderer debugRenderer;

    private Player player;
    private boolean debugDraw = false;
    private boolean renderLights = false;

    private int health = 6;

    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMap tiledMap;
    private List<Body> wallBodies = new ArrayList<Body>();
    private List<Body> spikeBodies = new ArrayList<Body>();

    private int[] foregroundLayers;
    private int[] backgroundLayers;

    private List<Ghost> ghosts = new ArrayList<Ghost>();
    private List<Moth> moths = new ArrayList<Moth>(3);

    private int mothsCollected = 0;
    private int mothsTotal = 3;

    private List<Body> bodiesToDestroy = new ArrayList<Body>(10);

    private float gameOverTimer = 4f;
    private boolean gameOver = false;


    private boolean gameStarted = true;
    public boolean completed = false;

    public GameStage(GameScreen gameScreen) {
        super(new ExtendViewport(Gdx.graphics.getBackBufferWidth()/3, Gdx.graphics.getBackBufferHeight()/3));
        this.gameScreen = gameScreen;

        world = new World(new Vector2(0f, -10f), true);
        world.setContactListener(this);
        debugRenderer = new Box2DDebugRenderer();

        player = new Player(this);
        addActor(player);

        tiledMap = new TmxMapLoader().load("one_room.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        createPhysicsWalls();
        createSpikes();
        splitLayers();
        handleSpawns();
    }

    private void handleSpawns() {
        MapLayer spawns = tiledMap.getLayers().get("Spawns");
        for(MapObject obj : spawns.getObjects()) {
            float px = (Float) obj.getProperties().get("x");
            float py = (Float) obj.getProperties().get("y");

            if(obj.getName().equals("Player")) {
                player.setPosition(px,py);
            } else if(obj.getName().equals("Ghost")) {
                Ghost g = new Ghost(this, obj);
                ghosts.add(g);
                addActor(g);
            }else if (obj.getName().equals("Moth")) {
                Moth k = new Moth(this, obj);
                moths.add(k);
                addActor(k);
            } else if (obj.getName().equals("Door")) {
                MapProperties p = obj.getProperties();
                float x = (Float) p.get("x");
                float y = (Float) p.get("y");
                float w = (Float) p.get("width");
                float h = (Float) p.get("height");
                x *= Constants.SCR2BOX;
                y *= Constants.SCR2BOX;
                w *= Constants.SCR2BOX;
                h *= Constants.SCR2BOX;

                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                final Body body = world.createBody(bodyDef);
                FixtureDef fixtureDef = new FixtureDef();
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(w / 2f, h / 2f);
                fixtureDef.shape = shape;
                Fixture f = body.createFixture(fixtureDef);
                f.setSensor(true);
                body.setTransform(x + w / 2f, y + h / 2f, 0f);
                body.setUserData(new PhysicsBody<Object>() {
                    @Override
                    public String getTag() {
                        return "Door";
                    }

                    @Override
                    public Object getObject() {
                        return null;
                    }

                    @Override
                    public void beginContact(Contact contact, PhysicsBody other) {

                    }

                    @Override
                    public Body getBody() {
                        return body;
                    }
                });
                shape.dispose();
            }
        }
    }

    private void splitLayers() {
        List<Integer> fg = new ArrayList<Integer>();
        List<Integer> bg = new ArrayList<Integer>();

        MapLayers layers = tiledMap.getLayers();
        for(int i=0; i < layers.getCount(); i++) {
            if(layers.get(i).getName().startsWith("FG")) {
                if(layers.get(i).isVisible()) {
                    fg.add(i);
                }
            } else {
                if(layers.get(i).isVisible()) {
                    bg.add(i);
                }
            }
        }
        foregroundLayers = new int[fg.size()];
        for (int i = 0; i < fg.size(); ++i) foregroundLayers[i] = fg.get(i);
        backgroundLayers = new int[bg.size()];
        for (int i = 0; i < bg.size(); ++i) backgroundLayers[i] = bg.get(i);
    }

    private void createSpikes() {
        MapLayer spikesLayer = tiledMap.getLayers().get("Spikes");
        for (MapObject obj : spikesLayer.getObjects()) {
            MapProperties p = obj.getProperties();
            float x = (Float) p.get("x");
            float y = (Float) p.get("y");
            float w = (Float) p.get("width");
            float h = (Float) p.get("height");
            x *= Constants.SCR2BOX;
            y *= Constants.SCR2BOX;
            w *= Constants.SCR2BOX;
            h *= Constants.SCR2BOX;

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            final Body body = world.createBody(bodyDef);
            FixtureDef fixtureDef = new FixtureDef();
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(w / 2f, h / 2f);
            fixtureDef.shape = shape;
            Fixture f = body.createFixture(fixtureDef);
            body.setTransform(x + w / 2f, y + h / 2f, 0f);
            body.setUserData(new PhysicsBody<Object>() {
                @Override
                public String getTag() {
                    return "Spike";
                }

                @Override
                public Object getObject() {
                    return null;
                }

                @Override
                public void beginContact(Contact contact, PhysicsBody other) {

                }

                @Override
                public Body getBody() {
                    return body;
                }
            });
            spikeBodies.add(body);
            shape.dispose();
        }

    }

    private void createPhysicsWalls() {
        MapLayers layers = tiledMap.getLayers();
        MapLayer physicsLayer = layers.get("Walls");
        for (MapObject mapObject : physicsLayer.getObjects()) {
            MapProperties properties = mapObject.getProperties();
            float x = (Float) properties.get("x");
            float y = (Float) properties.get("y");
            float w = (Float) properties.get("width");
            float h = (Float) properties.get("height");

            float r = (Float) properties.get("rotation", 0f, Float.class);

            x *= Constants.SCR2BOX;
            y *= Constants.SCR2BOX;
            w *= Constants.SCR2BOX;
            h *= Constants.SCR2BOX;

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            Body body = world.createBody(bodyDef);
            FixtureDef fixtureDef = new FixtureDef();
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(w / 2f, h / 2f);
            fixtureDef.shape = shape;
            Fixture fixture = body.createFixture(fixtureDef);
            body.setTransform(x + w / 2f, y + h / 2f, 0f);
            wallBodies.add(body);
            // body.setUserData(m_wallPhysicsBody);
            shape.dispose();
        }
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void endContact(Contact contact) {
        player.resetHugs();
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public void queueForDestruction(Body body) {
        bodiesToDestroy.add(body);
    }

    public void cleanUp() {
        world.dispose();
    }

    @Override
    public void act(float delta) {
        for(Body body : bodiesToDestroy) {
            world.destroyBody(body);
        }
        bodiesToDestroy.clear();

        if(gameStarted) {
            gameOverTimer -= delta;
            if(gameOverTimer < 0f) {
                gameStarted = false;
            }
        }

        super.act(delta);
        if(health > 0) {
            getCamera().position.x = player.getBodyX() * Constants.BOX2SCR;
            getCamera().position.y = player.getBodyY() * Constants.BOX2SCR;
            Res.setMusicVolume(Res.getMusicVolume() + (0.8f - Res.getMusicVolume()) * delta);
        } else {
            gameOverTimer += delta;
            Res.setMusicVolume(0.8f - 0.8f * (gameOverTimer / 4f));

            if(gameOverTimer > 4f) {
                Res.setMusicVolume(0.8f);
                gameScreen.reset();
                health = 1;
            }
        }
        world.step(delta, 10, 10);
    }

    @Override
    public void draw() {
        if(gameOverTimer > 4f) return;

        mapRenderer.setView((OrthographicCamera) getCamera());
        mapRenderer.render(backgroundLayers);

        super.draw();

        mapRenderer.render(foregroundLayers);

        if(renderLights) {
            Matrix4 p = getCamera().projection.cpy();
            Matrix4 v = getCamera().view.cpy();
            v.scl(Constants.BOX2SCR);
            p.mul(v);
        }

        if(debugDraw) {
            Matrix4 m = getCamera().combined.cpy();
            m.scl(Constants.BOX2SCR);
            debugRenderer.render(world, m);
        }
    }

    private void handlePlayerContact(PhysicsBody other) {
        String tag = other.getTag();
        if (tag.equals(Ghost.TAG) || tag.equals("Spike")) {
            if (!player.isDamaged() && health > 0) {

                health--;
                player.doDamage(other.getBody().getPosition());
                gameScreen.getGui().setHealth(health);
                if (health == 0) {
                    Res.death.play();
                    initGameOver();
                } else {
                    Res.hit.play();
                }
            }
        } else if (tag.equals(Moth.TAG)) {
            Res.key.play();
            mothsCollected++;
            Moth moth = (Moth) other;
            moth.remove();
            gameScreen.getGui().mothCollected(moth);
            System.out.println("Moth " + mothsCollected + "/" + mothsTotal + " collected!");
        } else if (tag.equals("Door")) {
            if (mothsCollected == 3) {
                System.out.println("CONGRATULATIONS!");
                completed = true;
                gameScreen.showCongratulations();
            } else {
                System.out.println("Still " + (3 - mothsCollected) + " moths to go...");
            }
        }
    }

    private void initGameOver() {
        player.kill();
        gameOverTimer = 0f;
        gameOver = true;
    }

    @Override
    public void beginContact(Contact contact) {
        PhysicsBody o1 = (PhysicsBody) contact.getFixtureA().getBody().getUserData();
        PhysicsBody o2 = (PhysicsBody) contact.getFixtureB().getBody().getUserData();

        if (o1 != null && o2 != null) {
            o1.beginContact(contact, o2);
            String s1 = o1.getTag();
            String s2 = o2.getTag();
            if (s1.equals(Player.TAG)) {
                handlePlayerContact(o2);

            } else if (s2.equals(Player.TAG)) {
                handlePlayerContact(o1);
            }
        } else if (o1 != null) {
            o1.beginContact(contact, null);
        } else if (o2 != null) {
            o2.beginContact(contact, null);
        }
    }
}
