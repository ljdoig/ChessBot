package com.chessbot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;
import engine.Board;

public class MenuScreen implements Screen {
    private final ChessGame game;
    private final Board board;

    public MenuScreen(ChessGame game) {
        this.game = game;
        board = new Board();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        board.render(game.getBatch());
        game.drawCentredText(
                "WELCOME! PRESS ENTER TO BEGIN",
                (ChessGame.HEIGHT + ChessGame.WIDTH) / 2.0f,
                ChessGame.HEIGHT / 2.0f,
                true
        );

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            System.out.println("Switching to game screen");
            game.setScreen(new GameScreen(game, board, false, true));
        }
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
