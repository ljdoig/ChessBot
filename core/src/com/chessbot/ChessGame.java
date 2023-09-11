package com.chessbot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import engine.RenderedBoard;

public class ChessGame extends Game {
    public static final int WIDTH = 1440;
    public static final int HEIGHT = 800;
    public static final float ASPECT_RATIO = WIDTH / (float) HEIGHT;
    private SpriteBatch batch;
    private StretchViewport viewport;
    private BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        OrthographicCamera cam = new OrthographicCamera();
        viewport = new StretchViewport(ChessGame.WIDTH, ChessGame.HEIGHT, cam);
        viewport.apply();
        cam.position.set(ChessGame.WIDTH / 2.0f, ChessGame.HEIGHT / 2.0f, 0);
        cam.update();
        font = new BitmapFont();
        // font = FontLoader.load("font/Lotuscoder-0WWrG.ttf", 23);

        System.out.println("Opening to menu screen");
        this.setScreen(new MenuScreen(this));
        // this.setScreen(
        // new GameScreen(this, new RenderedBoard(), false, true, false, false)
        // );
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

    public StretchViewport getViewport() {
        return viewport;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

}
