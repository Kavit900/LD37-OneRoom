package com.zenwraight.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Created by kavitmht on 12/10/2016.
 */
public class Gui extends Stage{

    private final GameScreen screen;

    private Group health;
    private Entity heart0;
    private Entity heart1;
    private Entity heart2;

    private TextureRegion[] heartRegions;

    private Group moths;

    public Gui(GameScreen screen) {
        super(new ExtendViewport(Gdx.graphics.getBackBufferWidth() / 3, Gdx.graphics.getBackBufferHeight() / 3));
        this.screen = screen;

        heartRegions = new TextureRegion[]{Res.findRegion("heart_0"), Res.findRegion("heart_1")};

        health = new Group();
        heart0 = new Entity(null, Res.createSprite("heart_0"));
        heart1 = new Entity(null, Res.createSprite("heart_0"));
        heart2 = new Entity(null, Res.createSprite("heart_0"));

        heart0.setPosition(0f, 0f);
        heart1.setPosition(heart0.getWidth() + 4f, 0f);
        heart2.setPosition(heart1.getX() + heart1.getWidth() + 4f, 0f);

        health.addActor(heart0);
        health.addActor(heart1);
        health.addActor(heart2);

        addActor(health);

        moths = new Group();
        moths.setPosition(0f, getHeight() - 20f);
        addActor(moths);
    }

    public void mothCollected(Moth moth) {
        int c = moths.getChildren().size;
        Entity e = new Entity(null, moth.sprite);
        e.setPosition(c * (moth.sprite.getWidth()), 0f);
        moths.addActor(e);
    }

    public void setHealth(int health) {
        switch (health) {
            case 0:
                this.health.setVisible(false);
                break;
            case 1:
                this.heart0.setRegion(heartRegions[1]);
                this.heart0.setVisible(true);
                this.heart1.setVisible(false);
                this.heart2.setVisible(false);
                break;
            case 2:
                this.heart0.setRegion(heartRegions[0]);
                this.heart0.setVisible(true);
                this.heart1.setVisible(false);
                this.heart2.setVisible(false);
                break;
            case 3:
                this.heart0.setRegion(heartRegions[0]);
                this.heart1.setRegion(heartRegions[1]);
                this.heart0.setVisible(true);
                this.heart1.setVisible(true);
                this.heart2.setVisible(false);
                break;
            case 4:
                this.heart0.setRegion(heartRegions[0]);
                this.heart1.setRegion(heartRegions[0]);
                this.heart0.setVisible(true);
                this.heart1.setVisible(true);
                this.heart2.setVisible(false);
                break;
            case 5:
                this.heart0.setRegion(heartRegions[0]);
                this.heart1.setRegion(heartRegions[0]);
                this.heart2.setRegion(heartRegions[1]);
                this.heart0.setVisible(true);
                this.heart1.setVisible(true);
                this.heart2.setVisible(true);
                break;
            case 6:
                this.heart0.setRegion(heartRegions[0]);
                this.heart1.setRegion(heartRegions[0]);
                this.heart2.setRegion(heartRegions[0]);
                this.heart0.setVisible(true);
                this.heart1.setVisible(true);
                this.heart2.setVisible(true);
                break;
        }
    }
}
