import bagel.*;
import java.util.ArrayList;

/**
 * author: L. Doig, using Bagel from OOSD Nov 2021
 */
public class ChessGame extends AbstractGame {
    private static Image background;
    public static final int SIZE = 1000;
    public static final double ASPECT_RATIO = 18/10.0;
    public static final ArrayList<Side> automaticSides = new ArrayList<>(){{
        //add(Side.WHITE);
        add(Side.BLACK);
    }};
    private static Board board;
    private int pausedFrameCount = 0;
    private boolean firstFrame = true;
    private static final String standardSetup =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String testSetup1 =
            "4k3/1p6/8/8/8/8/PPPB4/2b1K3 b - - 3 3";
    private static final String testSetup2 =
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2n2Q2/PPPBBPpP/R3K2R w KQkq - 0 20";
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

    public ChessGame() {
        super((int)(ASPECT_RATIO*SIZE), SIZE, "ChessGame");
        board = new Board();
        board.initialise(standardSetup);
        background = new Image("res/board/black_background.png");
    }

    /**
     * The entry point for the program.
     */
    public static void main(String[] args) {
        ChessGame game = new ChessGame();
        game.run();
    }

    /**
     * Performs a state update.
     */
    @Override
    public void update(Input input) {

        background.draw(Window.getWidth()/2, Window.getHeight()/2);
        render();

        // ensures board is rendered before computer starts thinking
        if (firstFrame) {
            firstFrame = false;
            return;
        }

        if (input.isDown(Keys.RIGHT_SHIFT)) {
            if (input.wasPressed(Keys.NUM_1)) {
                board = new Board();
                board.initialise(testSetup1);
            } else if (input.wasPressed(Keys.NUM_2)) {
                board = new Board();
                board.initialise(testSetup2);
            } else if (input.wasPressed(Keys.NUM_3)) {
                board = new Board();
                board.initialise(testSetup3);
            } else if (input.wasPressed(Keys.NUM_4)) {
                board = new Board();
                board.initialise(testSetup4);
            } else if (input.wasPressed(Keys.NUM_5)) {
                board = new Board();
                board.initialise(testSetup5);
            } else if (input.wasPressed(Keys.NUM_6)) {
                board = new Board();
                board.initialise(testSetup6);
            } else if (input.wasPressed(Keys.NUM_7)) {
                board = new Board();
                board.initialise(testSetup7);
            } else if (input.wasPressed(Keys.NUM_8)) {
                board = new Board();
                board.initialise(testSetup8);
            } else if (input.wasPressed(Keys.NUM_9)) {
                board = new Board();
                board.initialise(testSetup9);
            } else if (input.wasPressed(Keys.NUM_0)) {
                board = new Board();
                board.initialise(testSetup0);
            }
        }

        if (input.wasPressed(Keys.D)) {
            System.out.format("%s's material advantage: %d\n",
                    board.getNextTurn(),
                    board.nextTurnAdvantage()
            );
            System.out.format("Board evaluation: %d\n",
                    board.evaluate()
            );
        }
        if (input.wasPressed(Keys.F)) {
            System.out.println(board.toFen());
        }
        if (input.wasPressed(Keys.I)) {
            board.flip();
        }
        if ((automaticSides.size() == 2 && input.isDown(Keys.W)) ||
                input.wasPressed(Keys.W)) {
            if (automaticSides.contains(Side.WHITE)) {
                automaticSides.remove(Side.WHITE);
            } else {
                automaticSides.add(Side.WHITE);
            }
        }
        if ((automaticSides.size() == 2 && input.isDown(Keys.L)) ||
                input.wasPressed(Keys.L)) {
            if (automaticSides.contains(Side.BLACK)) {
                automaticSides.remove(Side.BLACK);
            } else {
                automaticSides.add(Side.BLACK);
            }
        }
        // exit on Esc
        if (input.wasPressed(Keys.ESCAPE)) {
            Window.close();
        } else if (input.wasPressed(Keys.BACKSPACE)) {
            // restart game
            pausedFrameCount = 0;
            board = new Board();
            board.initialise(standardSetup);
            return;
        } else if (pausedFrameCount > 0) {
            // pause for this frame
            pausedFrameCount--;
            return;
        } else if (board.gameFinished()) {
            if (automaticSides.size() == 2) {
                board = new Board();
                board.initialise(standardSetup);
            }
            return;
        }
        // press A to automate your next move
        if (automaticSides.contains(board.getNextTurn())
                || input.wasPressed(Keys.A)) {
            board.computeMove();
            render();
        } else if (input.wasPressed(MouseButtons.LEFT) ||
                   input.wasPressed(MouseButtons.RIGHT)) {
            board.processClick(input);
            render();
        }
    }

    public static Board getCurrentBoard() {
        return board;
    }

    private void render() {
        background.draw(Window.getWidth()/2, Window.getHeight()/2);
        board.render();
    }
}