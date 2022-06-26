package engine;

import java.util.ArrayList;

public class Knight extends Piece {
    private static final int value = 3;
    private static final int pieceIndex = 3;
    // these arrays show the possible move directions at any point
    private static final int[] up    =
            new int[]{2,  2, 1,  1, -1, -1, -2, -2};
    private static final int[] right =
            new int[]{1, -1, 2, -2,  2, -2,  1, -1};

    public Knight(Side side, int row, int col, boolean unmoved) {
        super(side, value, pieceIndex, row, col, unmoved);
    }

    // For promotion
    public Knight(Piece piece, Square to) {
        super(piece, value, pieceIndex, to);
    }

    // Copy constructor
    public Knight(Knight knight, Board newBoard) {
        super(knight, newBoard);
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
