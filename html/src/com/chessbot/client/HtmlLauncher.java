package com.chessbot.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.chessbot.ChessGame;
import com.badlogic.gdx.graphics.g2d.freetype.gwt.inject.OnCompletion;
import com.badlogic.gdx.graphics.g2d.freetype.gwt.FreetypeInjector;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(ChessGame.WIDTH, ChessGame.HEIGHT);
        }

        @Override
        public ApplicationListener createApplicationListener () {
                return new ChessGame();
        }

        @Override
        public void onModuleLoad () {
                FreetypeInjector.inject(new OnCompletion() {
                        public void run () {
                                // Replace HtmlLauncher with the class name
                                // If your class is called FooBar.java than the line should be FooBar.super.onModuleLoad();
                                HtmlLauncher.super.onModuleLoad();
                        }
                });
        }
}
