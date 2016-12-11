package com.zenwraight.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Created by kavitmht on 12/10/2016.
 */
public class Res {

    private static TextureAtlas atlas;
    public static Sound hit, key, jump, flap, death;

    private static Music m;

    public static BitmapFont font;

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("graphics/game_assets.atlas"));
        m = Gdx.audio.newMusic(Gdx.files.internal("sfx/music.mp3"));
        m.setLooping(true);
        m.setVolume(0.8f);
        m.play();

        hit = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.ogg"));
        key = Gdx.audio.newSound(Gdx.files.internal("sfx/key.ogg"));
        jump = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.ogg"));
        flap = Gdx.audio.newSound(Gdx.files.internal("sfx/flap.ogg"));
        death = Gdx.audio.newSound(Gdx.files.internal("sfx/death.ogg"));

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("graphics/pressstart.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter par = new FreeTypeFontGenerator.FreeTypeFontParameter();
        par.size = 8;
        par.kerning = true;
        par.minFilter = Texture.TextureFilter.Nearest;
        par.magFilter = Texture.TextureFilter.Nearest;
        font = gen.generateFont(par);
    }


    public static Sprite createSprite(String name) {
        return atlas.createSprite(name);
    }

    public static TextureRegion findRegion(String name) {
        return atlas.findRegion(name);
    }

    public static void setMusicVolume(float vol) {
        m.setVolume(vol);
    }

    public static float getMusicVolume() {
        return m.getVolume();
    }
}