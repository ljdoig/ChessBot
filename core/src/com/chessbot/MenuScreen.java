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
    private boolean training = false;

    public MenuScreen(ChessGame game) {
        this.game = game;
        board = new RenderedBoard();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        board.render(game.getBatch(), false);
        displayIntroMessage();
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            training = !training;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            // WHITE vs computer
            game.setScreen(
                    new GameScreen(game, board, false, true, false, training)
            );
        } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            // computer vs BLACK
            game.setScreen(
                    new GameScreen(game, board, true, false, true, training)
            );
        } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
            // computer vs computer
            game.setScreen(
                    new GameScreen(game, board, true, true, false, training)
            );
        } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
            // human only
            game.setScreen(
                    new GameScreen(game, board, false, false, false, training)
            );
        }
    }

    private void displayIntroMessage() {
        float column = ChessGame.HEIGHT + 20;
        float initialProportion = 0.89f;
        float spacingProportion = 0.065f;
        game.getBatch().begin();
        game.drawText(
                "WELCOME! SELECT MODE WITH KEYBOARD:",
                column,
                ChessGame.HEIGHT * 0.96f
        );
        ArrayList<String> gameModes = new ArrayList<String>(){{
            add(" 1: Play as WHITE against the computer");
            add(" 2: Play as BLACK against the computer");
            add(" 3: Spectate (computer vs computer)");
            add(" 4: Play a human vs human game");
            add("");
        }};
        for (int i = 0; i < gameModes.size(); i++) {
            game.drawText(
                    gameModes.get(i),
                    column,
                    ChessGame.HEIGHT * (initialProportion - i * spacingProportion)
            );
        }
        ArrayList<String> trainingInfo = new ArrayList<String>(){{
            add("Press SPACE to toggle training mode:");
            add(" - prints anticipated sequence");
            add(" - right-click to test a move");
            add(" - press Z to take-back a move");
            add(" - press A to use the bot for your move");
            add(" - press W to toggle WHITE bot");
            add(" - press L to toggle BLACK bot");
            add("Training mode: " + (training ? "ON" : "OFF"));
        }};
        for (int i = 0; i < trainingInfo.size(); i++) {
            game.drawText(
                    trainingInfo.get(i),
                    column,
                    ChessGame.HEIGHT * (
                            initialProportion - (1 + gameModes.size() + i) *
                                    0.75f * spacingProportion
                    )
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
