package com.chessbot;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(30);
		config.setWindowedMode(ChessGame.WIDTH,ChessGame.HEIGHT);
		config.setTitle("ChessBot");
		new Lwjgl3Application(new ChessGame(), config);
	}
}
