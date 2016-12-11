package com.zenwraight.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * Created by kavitmht on 12/10/2016.
 */
public class Ghost extends Entity implements PhysicsBody<Ghost>{

    public static final String TAG = "Ghost";
    private TextureRegion[] frames;
    private int frame = 0;
    private float frameTimer = 0f;
    private float frameSpeed = 0.2f;

    private Body body;

    public Ghost(GameStage stage, MapObject obj) {
        super(stage, Res.createSprite("ghost_0"));
        frames = new TextureRegion[]{
                Res.findRegion("ghost_0"),
                Res.findRegion("ghost_1"),
                Res.findRegion("ghost_2"),
                Res.findRegion("ghost_3")
        };

        createPhysicsBody();
        body.setUserData(this);

        float x = (Float) obj.getProperties().get("x");
        float y = (Float) obj.getProperties().get("y");
        setPosition(x, y);
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        body = gameStage.getWorld().createBody(bodyDef);
        body.setGravityScale(0f);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.4f);
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
    public void act(float delta) {
        super.act(delta);

        handleAi(delta);

        super.setPosition(body.getPosition().x * Constants.BOX2SCR - 8f, body.getPosition().y * Constants.BOX2SCR - 8f);
        updateAnimation(delta);

        if (body.getLinearVelocity().x > 0f) {
            sprite.setFlip(false, false);
        } else if (body.getLinearVelocity().x < 0f) {
            sprite.setFlip(true, false);
        }
    }

    private float startAttackDistance = 5f * 5f;
    private float stopAttackDistance = 8f * 8f;
    private float idleTimer = 0f;
    private boolean attacking = false;
    private boolean idleWalking = true;
    private boolean idleRight = true;

    private void handleAi(float delta) {
        Player player = gameStage.getPlayer();
        Vector2 v = new Vector2(player.getBodyX(), player.getBodyY());
        v = v.sub(body.getPosition());
        float d2 = v.len2();

        if (attacking) {

            v = v.nor();
            v = v.scl(3f);
            body.setLinearVelocity(v);

            if (d2 > stopAttackDistance) {
                attacking = false;
            }
        } else {
            idleTimer += delta;
            if (idleTimer >= 2f) {
                idleTimer = 0f;
                idleWalking = !idleWalking;
                idleRight = MathUtils.randomBoolean();
                sprite.setFlip(idleRight, false);
            }

            if (idleWalking) {
                body.setLinearVelocity(idleRight ? 2f : -2f, 0f);
            } else {
                body.setLinearVelocity(0f, 0f);
            }

            if (d2 < startAttackDistance) {
                attacking = true;
            }
        }

    }

    private void updateAnimation(float delta) {
        frameTimer += delta;
        if (frameTimer >= frameSpeed) {
            frameTimer -= frameSpeed;
            frame++;
            frame %= frames.length;
            sprite.setRegion(frames[frame]);
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Ghost getObject() {
        return this;
    }

    @Override
    public void beginContact(Contact contact, PhysicsBody other) {

    }

    @Override
    public Body getBody() {
        return body;
    }
}
