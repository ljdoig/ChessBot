package engine;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Bishop extends Piece {
    private static final Texture BLACK_IMAGE = new Texture("pieces/black/bishop.png");
    private static final Texture WHITE_IMAGE = new Texture("pieces/white/bishop.png");
    private static final int value = 3;

    public Bishop(Side side, int row, int col, boolean unmoved) {
        super(side, value, row, col, unmoved,
                (side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // For promotion
    public Bishop(Piece piece, Square to) {
        super(piece, value, to,
                (piece.side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // Copy constructor
    public Bishop(Bishop bishop, Board newBoard) {
        super(bishop, newBoard);
    }

    public static void dispose() {
        BLACK_IMAGE.dispose();
        WHITE_IMAGE.dispose();
    }

    @Override
    protected Piece makeCopy(Board newBoard) {
        return new Bishop(this, newBoard);
    }

    @Override
    public boolean isChecking() {
        return isCheckingDiagonally();
    }

    @Override
    public ArrayList<Move> getValidMoves() {
        return getValidDiagonalMoves();
    }

    @Override
    public boolean validMoveExists() {
        return validDiagonalMoveExists();
    }
}