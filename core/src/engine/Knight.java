package engine;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Knight extends Piece {
    private static final Texture BLACK_IMAGE = new Texture("pieces/black/knight.png");
    private static final Texture WHITE_IMAGE = new Texture("pieces/white/knight.png");
    private static final int value = 3;
    private static final int pieceIndex = 1;
    // these arrays show the possible move directions at any point
    private static final int[] up    =
            new int[]{2,  2, 1,  1, -1, -1, -2, -2};
    private static final int[] right =
            new int[]{1, -1, 2, -2,  2, -2,  1, -1};

    public Knight(Side side, int row, int col, boolean unmoved) {
        super(side, value, pieceIndex, row, col, unmoved,
                (side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // For promotion
    public Knight(Piece piece, Square to) {
        super(piece, value, pieceIndex, to,
                (piece.side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // Copy constructor
    public Knight(Knight knight, Board newBoard) {
        super(knight, newBoard);
    }

    public static void dispose() {
        BLACK_IMAGE.dispose();
        WHITE_IMAGE.dispose();
    }

    @Override
    protected Piece makeCopy(Board newBoard) {
        return new Knight(this, newBoard);
    }

    @Override
    public boolean isChecking() {
        int rowDiff = opponentKingSquare().rowDiff(square());
        int colDiff = opponentKingSquare().colDiff(square());
        if (colDiff == 0 || rowDiff == 0) {
            return false;
        }
        return (rowDiff + colDiff == 3);
    }

    @Override
    public ArrayList<Move> getValidMoves() {
        ArrayList<Move> validMoves = new ArrayList<>();
        Move move;
        Square to;
        int toRow, toCol;
        for (int i = 0; i < up.length; i++) {
            toRow = square().row + up[i];
            toCol = square().col + right[i];
            if (0 <= toRow && toRow <= 7 && 0 <= toCol && toCol <= 7 ) {
                to = new Square(toRow, toCol);
                if (board.contentsAt(to) == null || board.contentsAt(to).side != side) {
                    move = new Move(this, to);
                    if (!board.wouldBeInCheck(move, side)) {
                        validMoves.add(move);
                    }
                }
            }
        }
        return validMoves;
    }

    @Override
    public boolean validMoveExists() {
        Move move;
        Square to;
        int toRow, toCol;
        for (int i = 0; i < up.length; i++) {
            toRow = square().row + up[i];
            toCol = square().col + right[i];
            if (0 <= toRow && toRow <= 7 && 0 <= toCol && toCol <= 7 ) {
                to = new Square(toRow, toCol);
                if (board.contentsAt(to) == null || board.contentsAt(to).side != side) {
                    move = new Move(this, to);
                    if (!board.wouldBeInCheck(move, side)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
