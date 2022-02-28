import bagel.Image;

import java.util.ArrayList;

public class Rook extends Piece {
    private static final Image blackImage = new Image("res/pieces/black/rook.png");
    private static final Image whiteImage = new Image("res/pieces/white/rook.png");
    private static final int value = 5;

    public Rook(Side side, int row, int col) {
        super(side, value, row, col,
                ((side == Side.WHITE) ? (row == 7) : (row == 0)) && (col == 0 || col == 7),
                (side == Side.WHITE) ? whiteImage : blackImage);
    }

    // For promotion
    public Rook(Piece piece, Square to) {
        super(piece, value, to,
                (piece.side == Side.WHITE) ? whiteImage : blackImage);
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