package com.chessbot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;
import engine.RenderedBoard;
import engine.Side;
import engine.Square;

import java.util.ArrayList;

public class GameScreen implements Screen {
    private final ChessGame game;
    private RenderedBoard board;
    private boolean whiteBot;
    private boolean blackBot;
    private final boolean invert;
    private boolean firstFrame = true;

    public GameScreen(ChessGame game, RenderedBoard board,
                      boolean whiteBot, boolean blackBot, boolean invert) {
        this.game = game;
        this.board = board;
        this.whiteBot = whiteBot;
        this.blackBot = blackBot;
        this.invert = invert;
    }

    @Override
    public void render(float delta) {
        refreshDisplay();

        // ensure board is rendered before computer starts thinking
        if (firstFrame) {
            firstFrame = false;
            return;
        }

        // allow move take-back
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            board.undoLastMove();
            if (whiteBot ^ blackBot) board.undoLastMove();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            System.out.println(board.toFen());
        }
        // Toggle white player between automated and human
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            whiteBot = !whiteBot;
        }
        // Toggle black player between automated and human
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            blackBot = !blackBot;
        }
        // restart game
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }
        if (board.gameFinished()) return;
        // computer's turn (or someone pressed A to automate their next move)
        if ((board.getNextTurn() == Side.WHITE && whiteBot) ||
            (board.getNextTurn() == Side.BLACK && blackBot) ||
            Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            board.computeAndMakeMove();
            refreshDisplay();
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Square clickedSquare = getClickedSquare();
            if (clickedSquare != null) {
                board.processClick(
                        clickedSquare.row, clickedSquare.col, false
                );
            }
            refreshDisplay();
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            Square clickedSquare = getClickedSquare();
            if (clickedSquare != null) {
                board.processClick(
                        clickedSquare.row, clickedSquare.col, true
                );
            }
        }
    }

    private Square getClickedSquare() {
        int row = Gdx.input.getY() * 8 / game.getViewport().getScreenHeight();
        int col = (int) (
                (Gdx.input.getX() * ChessGame.ASPECT_RATIO * 8) /
                game.getViewport().getScreenWidth()
        );
        if (invert) {
            row = 7 - row;
            col = 7 - col;
        }
        if (0 <= row && row < 8 && 0 <= col && col < 8) {
            return new Square(row, col);
        } else {
            return null;
        }
    }

    private void refreshDisplay() {
        ScreenUtils.clear(0, 0, 0, 1);
        board.render(game.getBatch(), invert);
        displayInfo();
    }

    private void displayInfo() {
        game.getBatch().begin();
        int row = 0;
        int col = ChessGame.HEIGHT + 10;
        // display general game stats
        game.drawText(
                String.format(
                        "Turn:            %3d",
                        board.getFullmoveNumber()
                ),
                col, yCoordForText(row++)
        );
        game.drawText(
                String.format(
                        "Half-move clock: %3d",
                        board.getHalfmoveClock()
                ),
                col, yCoordForText(row++)
        );
        game.drawText(
                String.format(
                        "Zobrist hash: %s",
                        board.zobristTracker.getVal()),
                col, yCoordForText(row++)
        );
        String delimiter = "-----------------------------------------";
        game.drawText(delimiter, col, yCoordForText(row++));

        // display cost stats of last move scored
        ArrayList<String> scorerInfo = board.scorerInfo();
        for (String s : scorerInfo) {
            game.drawText(s, col, yCoordForText(row++));
        }
        if (scorerInfo.size() > 0) {
            game.drawText(delimiter, col, yCoordForText(row++));
        }

        // display chosen move and anticipated sequence
        ArrayList<String> moveInfo = board.moveInfo();
        for (String s : moveInfo) {
            game.drawText(s, col, yCoordForText(row++));
        }
        if (moveInfo.size() > 0) {
            game.drawText(delimiter, col, yCoordForText(row++));
        }

        if (board.gameFinished()) {
            game.drawText(board.getEndOfGameMessage(), col, yCoordForText(row));
        }
        game.getBatch().end();
    }

    private float yCoordForText(int row) {
        return ChessGame.HEIGHT * (0.98f - 0.04f * row);
    }

    @Override
    public void dispose() {}

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

}
