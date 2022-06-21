package engine;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Rook extends Piece {
    private static final Texture BLACK_IMAGE = new Texture("pieces/black/rook.png");
    private static final Texture WHITE_IMAGE = new Texture("pieces/white/rook.png");
    private static final int value = 5;
    private static final int pieceIndex = 3;

    public Rook(Side side, int row, int col) {
        super(side, value, pieceIndex, row, col,
                ((side == Side.WHITE) ? (row == 7) : (row == 0)) && (col == 0 || col == 7),
                (side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // For promotion
    public Rook(Piece piece, Square to) {
        super(piece, value, pieceIndex, to,
                (piece.side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // Copy constructor
    public Rook(Rook rook, Board newBoard) {
        super(rook, newBoard);
    }

    public static void dispose() {
        BLACK_IMAGE.dispose();
        WHITE_IMAGE.dispose();
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