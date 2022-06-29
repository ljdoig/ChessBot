package com.chessbot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;
import engine.RenderedBoard;

import java.util.ArrayList;

public class MenuScreen implements Screen {
    private final ChessGame game;
    private final RenderedBoard board;

    public MenuScreen(ChessGame game) {
        this.game = game;
        board = new RenderedBoard();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        board.render(game.getBatch(), false);
        displayIntroMessage();
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            // WHITE vs computer
            game.setScreen(
                    new GameScreen(game, board, false, true, false)
            );
            return;
        } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            // computer vs BLACK
            game.setScreen(
                    new GameScreen(game, board, true, false, true)
            );
            return;
        } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
            // computer vs computer
            game.setScreen(
                    new GameScreen(game, board, true, true, false)
            );
            return;
        } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
            // human only
            game.setScreen(
                    new GameScreen(game, board, false, false, false)
            );
            return;
        }
    }

    private void displayIntroMessage() {
        float column = ChessGame.HEIGHT + 20;
        float initialProportion = 0.9f;
        float spacingPropotion = 0.07f;
        game.getBatch().begin();
        game.drawText(
                "WELCOME! SELECT MODE WITH KEYBOARD:",
                column,
                ChessGame.HEIGHT * 0.96f
        );
        ArrayList<String> gameModes = new ArrayList<String>(){{
            add("1: Play as WHITE against the computer");
            add("2: Play as BLACK against the computer");
            add("3: Spectate (computer vs computer)");
            add("4: Play a human vs human game (cowardly)");
        }};
        for (int i = 0; i < gameModes.size(); i++) {
            game.drawText(
                    gameModes.get(i),
                    column,
                    ChessGame.HEIGHT * (initialProportion - i * spacingPropotion)
            );
        }
        game.getBatch().end();
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
