import bagel.Image;

import java.util.ArrayList;

public class Queen extends Piece {
    private static final Image blackImage = new Image("res/pieces/black/queen.png");
    private static final Image whiteImage = new Image("res/pieces/white/queen.png");
    private static final int value = 9;

    public Queen(Side side, int row, int col, boolean unmoved) {
        super(side, value, row, col, unmoved,
                (side == Side.WHITE) ? whiteImage : blackImage);
    }

    // For promotion
    public Queen(Piece piece, Square to) {
        super(piece, value, to,
                (piece.side == Side.WHITE) ? whiteImage : blackImage);
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
