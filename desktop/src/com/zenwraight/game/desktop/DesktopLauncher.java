package com.zenwraight.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.zenwraight.game.LD37;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.title = "One Room";
		config.width = 1080;
		config.height = 720;

		new LwjglApplication(new LD37(), config);
	}
}
