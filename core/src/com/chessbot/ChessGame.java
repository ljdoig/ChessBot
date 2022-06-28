package com.chessbot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class ChessGame extends Game {
	public static final int WIDTH = 1440;
	public static final int HEIGHT = 800;
	private SpriteBatch batch;
	private OrthographicCamera cam;
	private FitViewport viewport;
	private BitmapFont smallFont;
	private BitmapFont largeFont;
	private GlyphLayout glyphLayout;

	@Override
	public void create () {
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		viewport = new FitViewport(ChessGame.WIDTH, ChessGame.HEIGHT, cam);
		viewport.apply();
		cam.position.set(ChessGame.WIDTH / 2.0f,ChessGame.HEIGHT / 2.0f, 0);
		cam.update();
		smallFont = FontLoader.load("font/Lotuscoder-0WWrG.ttf", 20);
		largeFont = FontLoader.load("font/Lotuscoder-0WWrG.ttf", 25);
		glyphLayout = new GlyphLayout();

		System.out.println("Opening to menu screen");
		this.setScreen(new MenuScreen(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		batch.dispose();
		smallFont.dispose();
		largeFont.dispose();
	}

	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	public void drawText(String s, float x, float y, boolean large) {
		batch.begin();
		(large ? largeFont : smallFont).draw(batch, s, x, y);
		batch.end();
	}

	public void drawCentredText(String s, float x, float y, boolean large) {
		glyphLayout.setText(large ? largeFont : smallFont, s);
		float drawX = x - glyphLayout.width / 2;
		float drawY = y + glyphLayout.height / 2;
		batch.begin();
		largeFont.draw(batch, glyphLayout, drawX, drawY);
		batch.end();
	}

	public FitViewport getViewport() {
		return viewport;
	}

	public SpriteBatch getBatch() {
		return batch;
	}

	public BitmapFont getSmallFont() {
		return smallFont;
	}

	public BitmapFont getLargeFont() {
		return largeFont;
	}
}