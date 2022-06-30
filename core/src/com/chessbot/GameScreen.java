package com.chessbot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;
import engine.*;

import java.util.ArrayList;

public class GameScreen implements Screen {
    private final ChessGame game;
    private final RenderedBoard board;
    private boolean whiteBot;
    private boolean blackBot;
    private final boolean invert;
    private final boolean training;
    private final float displayedRowSpacing;
    private Move testingMove = null;
    private boolean firstFrame = true;
    private boolean planningFrame = false;
    private boolean testingFrame = false;

    public GameScreen(ChessGame game, RenderedBoard board, boolean whiteBot,
                      boolean blackBot, boolean invert, boolean training) {
        this.game = game;
        this.board = board;
        this.whiteBot = whiteBot;
        this.blackBot = blackBot;
        this.invert = invert;
        this.training = training;
        Node.setTraining(training);
        displayedRowSpacing = training ? 0.03f :0.035f;
    }

    @Override
    public void render(float delta) {
        refreshDisplay(false);

        // ensure board is rendered before computer starts thinking
        if (firstFrame) {
            firstFrame = false;
            return;
        }

        // allow move take-back
        if (training && Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            testingMove = null;
            board.undoLastMove();
            if (whiteBot ^ blackBot) board.undoLastMove();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            System.out.println(board.toFen());
        }
        // Toggle white player between automated and human
        if (training && Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            whiteBot = !whiteBot;
        }
        // Toggle black player between automated and human
        if (training && Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            blackBot = !blackBot;
        }
        // restart game
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }
        if (board.gameFinished()) return;
        // computer's turn (or someone pressed A to automate their next move)
        if ((board.getNextTurn() == Side.WHITE && whiteBot) ||
            (board.getNextTurn() == Side.BLACK && blackBot) ||
            (training && Gdx.input.isKeyJustPressed(Input.Keys.A)) ||
            planningFrame) {
            // ensures we print "thinking" to the screen before planning move
            if (!planningFrame) {
                planningFrame = true;
                refreshDisplay(true);
                return;
            }
            planningFrame = false;
            board.computeAndMakeMove();
            testingMove = null;
            refreshDisplay(false);
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Square clickedSquare = getClickedSquare();
            Move clickedMove = null;
            if (clickedSquare != null) {
                clickedMove = board.processLeftClick(clickedSquare);
            }
            if (clickedMove != null) {
                board.finaliseMove(clickedMove);
                testingMove = null;
                refreshDisplay(false);
            }
        } else if ((training && Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT))) {
            Square clickedSquare = getClickedSquare();
            if (clickedSquare != null) {
                testingMove = board.processRightClick(clickedSquare);
                if (testingMove != null) {
                    refreshDisplay(true);
                    testingFrame = true;
                }
            }
        } else if (testingFrame) {
            testingFrame = false;
            testingMove.analyse();
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

    private void refreshDisplay(boolean thinking) {
        ScreenUtils.clear(0, 0, 0, 1);
        board.render(game.getBatch(), invert);
        displayInfo(thinking);
    }

    private void displayInfo(boolean thinking) {
        int row = 0;
        int col = ChessGame.HEIGHT + 15;
        String delimiter = "-------------------------------------------";
        game.getBatch().begin();

        // display instructions
        for (String s : getInstructions()) {
            game.drawText(s, col, yCoordForText(row++));
        }
        game.drawText(delimiter, col, yCoordForText(row++));

        // display general game stats
        for (String s : getGameStats()) {
            game.drawText(s, col, yCoordForText(row++));
        }
        game.drawText(delimiter, col, yCoordForText(row++));

        // display cost stats of last move scored
        Move relevantMove = null;
        if (testingMove != null && testingMove.getEvaluationTracker() != null) {
            relevantMove = testingMove;
            game.drawText(
                    String.format("Analysis of %s", testingMove),
                    col, yCoordForText(row++)
            );
        } else if (!thinking) {
            relevantMove = board.getLastMove();
        }
        if (relevantMove != null && relevantMove.getEvaluationTracker() != null) {
            ArrayList<String> evaluationInfo = relevantMove.evaluationInfo();
            for (String s : evaluationInfo) {
                game.drawText(s, col, yCoordForText(row++));
            }
            // in training mode, display anticipated sequence after relevant move
            if (training) {
                ArrayList<String> moveInfo = relevantMove.anticipatedSequence();
                if (moveInfo.size() > 0) {
                    game.drawText("", col, yCoordForText(row++));
                    for (String s : moveInfo) {
                        game.drawText(s, col, yCoordForText(row++));
                    }
                }
            }
            game.drawText(delimiter, col, yCoordForText(row++));
        }

        // display final message, or "Thinking..."
        if (board.gameFinished()) {
            game.drawText(board.getEndOfGameMessage(), col, yCoordForText(row));
        } else if (thinking) {
            game.drawText("Thinking...", col, yCoordForText(row++));
        }

        game.getBatch().end();
    }

    private ArrayList<String> getInstructions() {
        return new ArrayList<String>(){{
            if (!board.gameFinished()) {
                if (!(whiteBot && blackBot)) {
                    add("Click to move");
                    add("Hold R while promoting for a Rook, etc.");
                }
                if (training) {
                    add("Right-click to analyse move");
                    add("Press Z to take-back a move");
                    add("Press A to use the bot for your move");
                    add("Press W to toggle WHITE bot");
                    add("Press L to toggle BLACK bot");
                }
            }
            if (training) {
                add("Press Z to take-back a move");
            }
            add("Press BACKSPACE for new game");
        }};
    }

    private ArrayList<String> getGameStats() {
        int displayedTurn = (
              board.gameFinished() && board.getNextTurn() == Side.WHITE ?
                      board.getFullmoveNumber() - 1 : board.getFullmoveNumber());
        return new ArrayList<String>(){{
            add(String.format("Turn:            %3d", displayedTurn));
            add(String.format("Half-move clock: %3d", board.getHalfmoveClock()));
            add(String.format("Zobrist hash: %s",board.zobristTracker.getVal()));
        }};
    }

    private float yCoordForText(int row) {
        return ChessGame.HEIGHT * (0.98f - displayedRowSpacing * row);
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
