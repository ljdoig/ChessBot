import bagel.Image;

import java.util.ArrayList;

public class Bishop extends Piece {
    private static final Image blackImage = new Image("res/pieces/black/bishop.png");
    private static final Image whiteImage = new Image("res/pieces/white/bishop.png");
    private static final int value = 3;

    public Bishop(Side side, int row, int col, boolean unmoved) {
        super(side, value, row, col, unmoved,
                (side == Side.WHITE) ? whiteImage : blackImage);
    }

    // For promotion
    public Bishop(Piece piece, Square to) {
        super(piece, value, to,
                (piece.side == Side.WHITE) ? whiteImage : blackImage);
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