package com.chessbot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import engine.Board;
import engine.Side;

public class GameScreen implements Screen {
    private static final String testSetup1 =
            "r1b1kb1r/pp1ppppp/1qn2n2/2pP4/8/2N1PN2/PPP2PPP/R1BQKB1R b KQkq - 0 5";
    private static final String testSetup2 =
            "r1b1kbnr/pp1pp1pp/2n2p2/2q3B1/8/2N2N2/PPP1PPPP/R2QKB1R w KQkq - 0 6";
    private static final String testSetup3 =
            "7r/3P1k1p/4Nn1n/2Q2pp1/p2P4/7P/3K1B2/5BNR w - g6 0 44";
    private static final String testSetup4 =
            "k7/8/q6B/8/2P5/P6R/8/7K b - - 1 30";
    private static final String testSetup5 =
            "1nq1kbnr/1p2pppp/8/3p4/2rP4/2NQ1N2/PBP2PPP/R4RK1 b k - 5 13";
    private static final String testSetup6 =
            "k7/2P5/8/8/8/8/3K4/8 b - - 0 17";
    private static final String testSetup7 =
            "5n2/5k2/2r3p1/3pppP1/Pp1P1P2/1P2R3/1K6/8 w - - 0 33";
    private static final String testSetup8 =
            "2b2rk1/1pppqppp/r1n2n2/pB2p3/P3P3/P1N2N2/2PPQPPP/2B2RK1 w - - 1 10";
    private static final String testSetup9 =
            "5n2/5k2/2r1p1p1/3p1pP1/Pp1P1P2/1P2R3/1K6/8 b - - 1 32";
    private static final String testSetup0 =
            "k7/8/8/8/q4BK1/8/8/8 w - - 0 35";
    private final ChessGame game;
    private Board board;
    private boolean whiteBot;
    private boolean blackBot;

    public GameScreen(ChessGame game, Board board,
                      boolean whiteBot, boolean blackBot) {
        this.game = game;
        this.board = board;
        this.whiteBot = whiteBot;
        this.blackBot = blackBot;
    }

    @Override
    public void render(float delta) {
        refreshDisplay();

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                board = new Board(testSetup1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                board = new Board(testSetup2);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                board = new Board(testSetup3);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
                board = new Board(testSetup4);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
                board = new Board(testSetup5);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
                board = new Board(testSetup6);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
                board = new Board(testSetup7);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) {
                board = new Board(testSetup8);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {
                board = new Board(testSetup9);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
                board = new Board(testSetup0);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        // allow move take-back for human against bot
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            board.undoLastMove();
            if (whiteBot ^ blackBot) board.undoLastMove();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            System.out.format("%s's material advantage: %d\n",
                    board.getNextTurn(),
                    board.nextTurnAdvantage()
            );
            System.out.format("Board evaluation: %d\n",
                    board.evaluate()
            );
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            System.out.println(board.toFen());
        }
        // Invert the board (visual only)
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            board.flip();
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
            board = new Board();
            return;
        }
        if (board.gameFinished()) return;
        // computer's turn (or someone pressed A to automate their next move)
        if ((board.getNextTurn() == Side.WHITE && whiteBot) ||
            (board.getNextTurn() == Side.BLACK && blackBot) ||
            Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            board.computeMove();
            refreshDisplay();
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2 click = new Vector2(
                    Gdx.input.getX() - game.getViewport().getLeftGutterWidth(),
                    Gdx.input.getY() - game.getViewport().getBottomGutterHeight()
            );
            board.processClick(
                    click.x / game.getViewport().getScreenHeight(),
                    click.y / game.getViewport().getScreenHeight(),
                    false
            );
            refreshDisplay();
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            Vector2 click = new Vector2(
                    Gdx.input.getX() - game.getViewport().getLeftGutterWidth(),
                    Gdx.input.getY() - game.getViewport().getBottomGutterHeight()
            );
            board.processClick(
                    click.x / game.getViewport().getScreenHeight(),
                    click.y / game.getViewport().getScreenHeight(),
                    true
            );
        }
    }

    private void refreshDisplay() {
        ScreenUtils.clear(0, 0, 0, 1);
        board.render(game.getBatch());
        board.displayInfo(
                game.getBatch(), game.getLargeFont(), game.getSmallFont()
        );
    }

    @Override
    public void dispose() {
        Board.dispose();
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
}
