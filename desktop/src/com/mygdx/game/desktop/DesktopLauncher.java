package com.mygdx.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.mygdx.game.ShootStuff;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon( "icon128.png", Files.FileType.Internal);
		config.addIcon( "icon32.png", Files.FileType.Internal);
		config.addIcon( "icon16.png", Files.FileType.Internal);
		new LwjglApplication(new ShootStuff(), config);
	}
}
