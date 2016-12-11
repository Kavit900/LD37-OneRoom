package com.zenwraight.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Created by kavitmht on 12/10/2016.
 */
public class GameScreen implements Screen {

    private final LD37 game;
    private GameStage gameStage;
    private Gui gui;
    private boolean shouldReset = false;
    private boolean shouldShowCongrats = false;
    private Stage finalStage;

    public GameScreen(LD37 game) {
        this.game = game;
        this.gameStage = new GameStage(this);
        this.gui = new Gui(this);
        this.finalStage = new FinalStage(this);
    }

    public void reset() {
        shouldReset = true;
    }

    private void resetInternal() {
        shouldReset = false;
        this.gameStage.cleanUp();
        Moth.KEYS_COUNT = 0;
        this.gameStage = new GameStage(this);
        this.gui = new Gui(this);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0f, 0f, 0f, 1.f);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (shouldReset) {
            resetInternal();
        }

        if (!shouldShowCongrats) {
            gameStage.act(delta);
            gameStage.draw();
            gui.act(delta);
            gui.draw();
        } else {
            finalStage.act(delta);
            finalStage.draw();
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

    public LD37 getGame() {
        return game;
    }

    public GameStage getGameStage() {
        return gameStage;
    }

    public Gui getGui() {
        return gui;
    }

    public void showCongratulations() {
        shouldShowCongrats = true;
    }

}
