package engine;

import java.util.ArrayList;

public class Queen extends Piece {
    public static final int value = 9;
    private static final int pieceIndex = 1;

    public Queen(Side side, int row, int col, boolean unmoved, Board board) {
        super(side, value, pieceIndex, row, col, unmoved, board);
    }

    // For promotion
    public Queen(Piece piece, Square to) {
        super(piece, value, pieceIndex, to);
    }

    // Copy constructor
    public Queen(Queen queen, Board newBoard) {
        super(queen, newBoard);
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
