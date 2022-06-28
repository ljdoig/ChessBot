package engine;

import java.util.ArrayList;

public class Rook extends Piece {
    private static final int value = 5;
    private static final int pieceIndex = 4;

    public Rook(Side side, int row, int col, Board board) {
        super(side, value, pieceIndex, row, col,
                side == Side.WHITE ? row == 7 : row == 0 && (col == 0 || col == 7),
                board);
    }

    // For promotion
    public Rook(Piece piece, Square to) {
        super(piece, value, pieceIndex, to);
    }

    // Copy constructor
    public Rook(Rook rook, Board newBoard) {
        super(rook, newBoard);
    }

    @Override
    protected Piece makeCopy(Board newBoard) {
        return new Rook(this, newBoard);
    }

    @Override
    public boolean isChecking() {
        return isCheckingOrthogonally();
    }

    @Override
    public ArrayList<Move> getValidMoves() {
        return getValidOrthogonalMoves();
    }

    @Override
    public boolean validMoveExists() {
        return validOrthogonalMoveExists();
    }
}