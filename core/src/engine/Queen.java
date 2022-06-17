package engine;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Queen extends Piece {
    private static final Texture BLACK_IMAGE = new Texture("pieces/black/queen.png");
    private static final Texture WHITE_IMAGE = new Texture("pieces/white/queen.png");
    private static final int value = 9;

    public Queen(Side side, int row, int col, boolean unmoved) {
        super(side, value, row, col, unmoved,
                (side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // For promotion
    public Queen(Piece piece, Square to) {
        super(piece, value, to,
                (piece.side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // Copy constructor
    public Queen(Queen queen, Board newBoard) {
        super(queen, newBoard);
    }

    public static void dispose() {
        BLACK_IMAGE.dispose();
        WHITE_IMAGE.dispose();
    }

    @Override
    protected Piece makeCopy(Board newBoard) {
        return new Queen(this, newBoard);
    }

    @Override
    public boolean isChecking() {
        return isCheckingDiagonally() || isCheckingOrthogonally();
    }

    @Override
    public ArrayList<Move> getValidMoves() {
        ArrayList<Move> validMoves = getValidOrthogonalMoves();
        validMoves.addAll(getValidDiagonalMoves());
        return validMoves;
    }

    @Override
    public boolean validMoveExists() {
        return validDiagonalMoveExists() || validOrthogonalMoveExists();
    }

}
