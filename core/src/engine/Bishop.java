package engine;

import java.util.ArrayList;

public class Bishop extends Piece {
    private static final int value = 3;
    private static final int pieceIndex = 2;

    public Bishop(Side side, int row, int col, boolean unmoved, Board board) {
        super(side, value, pieceIndex, row, col, unmoved, board);
    }

    // For promotion
    public Bishop(Piece piece, Square to) {
        super(piece, value, pieceIndex, to);
    }

    // Copy constructor
    public Bishop(Bishop bishop, Board newBoard) {
        super(bishop, newBoard);
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