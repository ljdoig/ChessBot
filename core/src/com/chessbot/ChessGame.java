package com.chessbot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import engine.RenderedBoard;

// TODO: ADD JAR TO GIT REPO AND UPDATE READ ME
// TODO: COMMENT AND CLEAN

public class ChessGame extends Game {
	public static final int WIDTH = 1440;
	public static final int HEIGHT = 800;
	public static final float ASPECT_RATIO = WIDTH / (float) HEIGHT;
	private SpriteBatch batch;
	private StretchViewport viewport;
	private BitmapFont font;
	private GlyphLayout glyphLayout;

	@Override
	public void create () {
		batch = new SpriteBatch();
		OrthographicCamera cam = new OrthographicCamera();
		viewport = new StretchViewport(ChessGame.WIDTH, ChessGame.HEIGHT, cam);
		viewport.apply();
		cam.position.set(ChessGame.WIDTH / 2.0f,ChessGame.HEIGHT / 2.0f, 0);
		cam.update();
		font = FontLoader.load("font/Lotuscoder-0WWrG.ttf", 23);
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
		font.dispose();
		RenderedBoard.dispose();
	}

	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	public void drawText(String s, float x, float y) {
		font.draw(batch, s, x, y);
	}

	public void drawCentredText(String s, float x, float y) {
		glyphLayout.setText(font, s);
		float drawX = x - glyphLayout.width / 2;
		float drawY = y + glyphLayout.height / 2;
		font.draw(batch, glyphLayout, drawX, drawY);
	}

	public StretchViewport getViewport() {
		return viewport;
	}

	public SpriteBatch getBatch() {
		return batch;
	}

}