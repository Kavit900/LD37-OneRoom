package com.zenwraight.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Created by kavitmht on 12/10/2016.
 */
public class Player extends Group implements PhysicsBody{

    public static final String TAG = "Player";

    private static final float FLICKER_SPEED = 0.06f;
    private static final float BAT_HORIZONTAL_POWER = 4f;
    private static final float BAT_MAX_VEL_X = 9f;
    private static final float BAT_FLAP_POWER = 6f;
    private static final float BAT_MAX_VEL_Y = 10f;

    private final GameStage stage;
    private Entity bat;

    private Body body;

    private TextureRegion[] batFrames;

    private float frameTimer = 0f;
    private float batFrameSpeed = 0.3f;
    private int frame = 0;
    private boolean damaged = false;
    private float damageTimer = 0f;
    private boolean shouldDraw = true;
    private float drawTimer = 0f;
    private boolean lastRight = true;

    BodyDef bodyDef;

    public Player(GameStage stage) {
        this.stage = stage;
        bat = new Entity(stage, Res.createSprite("bat_0"));
        bat.setSize(32f, 32f);
        batFrames = new TextureRegion[]{Res.findRegion("bat_0"), Res.findRegion("bat_1")};

        addActor(bat);

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;

        createBatPhysics();
        body.setUserData(this);
    }

    public float getShapeX() {
        return getX() + 16f;
    }

    public float getShapeY() {
        return getY() + 16f;
    }

    private void createBatPhysics() {
        body = stage.getWorld().createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius(0.52f);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        Fixture f = body.createFixture(fd);
        shape.dispose();
    }

    @Override
    public void setPosition(float x, float y) {
        body.setTransform(x * Constants.SCR2BOX, y * Constants.SCR2BOX, 0f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shouldDraw) {
            bat.sprite.setFlip(!lastRight, false);
            super.draw(batch, parentAlpha);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (damaged) {
            drawTimer += delta;
            if (drawTimer >= FLICKER_SPEED) {
                drawTimer -= FLICKER_SPEED;
                shouldDraw = !shouldDraw;
            }
            damageTimer += delta;
            if (damageTimer >= 0.3f) {
                damaged = false;
                shouldDraw = true;
            }
        }

        float ax = 16f;
        float ay = 16f;

        super.setPosition(body.getPosition().x * Constants.BOX2SCR - ax, body.getPosition().y * Constants.BOX2SCR - ay);
        updateAnimation(delta);

        handleBatInput();
    }

    private void handleBatInput() {
        if (!damaged) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                Res.flap.play(0.65f);
                if (body.getLinearVelocity().y < 0f) {
                    body.setLinearVelocity(body.getLinearVelocity().x, body.getLinearVelocity().y * 0.2f);
                }
                body.applyLinearImpulse(0f, BAT_FLAP_POWER, 0f, 0f, true);
                if (body.getLinearVelocity().y > BAT_MAX_VEL_Y) {
                    body.setLinearVelocity(body.getLinearVelocity().x, BAT_MAX_VEL_Y);
                }
                flapping = true;
                bat.sprite.setRegion(batFrames[0]);
                frameTimer = 0f;
            }

            float vx = 0f;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                vx = BAT_HORIZONTAL_POWER;
                lastRight = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                vx = -BAT_HORIZONTAL_POWER;
                lastRight = false;
            }

            if (vx != 0f) {
                body.applyLinearImpulse(vx, 0f, 0f, 0f, true);
                if (Math.abs(body.getLinearVelocity().x) > BAT_MAX_VEL_X) {
                    body.setLinearVelocity(body.getLinearVelocity().x < 0f ? -BAT_MAX_VEL_X : BAT_MAX_VEL_X, body.getLinearVelocity().y);
                }
            } else {
                vx = body.getLinearVelocity().x;
                vx *= 0.5f;
                body.setLinearVelocity(vx, body.getLinearVelocity().y);
            }
        }
    }

    private boolean flapping = false;

    public void updateAnimation(float delta) {
        if (flapping) {
            frameTimer += delta;
            if (frameTimer >= 0.1f) {
                bat.sprite.setRegion(batFrames[1]);
            }
            if (frameTimer >= 0.7f) {
                flapping = false;
                frame = 0;
                frameTimer = 0;
            }
        } else {
            if (body.getLinearVelocity().y >= -0.05f) {
                bat.sprite.setRegion(batFrames[1]);
            } else {
                bat.sprite.setRegion(batFrames[0]);
            }
        }
    }

    public float getBodyX() {
        return body.getPosition().x;
    }

    public float getBodyY() {
        return body.getPosition().y;
    }

    public void doDamage(Vector2 position) {
        Vector2 v = body.getPosition().sub(position);
        v = v.nor();
        v = v.scl(16f);
        body.applyLinearImpulse(v.x, v.y, 0f, 0f, true);
        damageTimer = 0f;
        drawTimer = 0f;
        damaged = true;
    }


    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Player getObject() {
        return this;
    }

    int jCounter = 0;

    boolean huggingRight = false, huggingLeft = false;

    public void resetHugs() {
        huggingLeft = huggingRight = false;
    }

    @Override
    public void beginContact(Contact contact, PhysicsBody other) {

        boolean hitGround = false;
        int numContacts = contact.getWorldManifold().getNumberOfContactPoints();
        if (numContacts > 0) {
            Vector2[] points = contact.getWorldManifold().getPoints();
            Vector2 mid = points[0];
            if (numContacts > 1) {
                mid.x = (mid.x + points[1].x) / 2f;
                mid.y = (mid.y + points[1].y) / 2f;
            }

            if (mid.x > body.getPosition().x - 0.45f && mid.x < body.getPosition().x + 0.45f &&
                    mid.y < body.getPosition().y) {
                hitGround = true;
                frameTimer = 0f;
            } else if (mid.x > body.getPosition().x + 0.45f) {
                huggingRight = true;
            } else if (mid.x < body.getPosition().x - 0.45f) {
                huggingLeft = true;
            }
        }

    }

    @Override
    public Body getBody() {
        return body;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public void kill() {
        bat.remove();
    }


}
