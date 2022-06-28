package engine;

import java.util.ArrayList;

public class King extends Piece {
    private static final int value = 0;
    private static final int pieceIndex = 0;
    private boolean hasCastled = false;

    public King(Side side, int row, int col, Board board) {
        super(side, value, pieceIndex, row, col,
                (side == Side.WHITE ? row == 7 : row == 0) && col == 4,
                board);
    }

    // Copy constructor
    public King(King king, Board newBoard) {
        super(king, newBoard);
    }

    @Override
    protected Piece makeCopy(Board newBoard) {
        return new King(this, newBoard);
    }

    @Override
    public boolean isChecking() {
        return opponentKingSquare().rowDiff(square()) <= 1 &&
               opponentKingSquare().colDiff(square()) <= 1;
    }

    @Override
    public boolean castlingPossible(Square to) {
        if (!isUnmoved() || square().colDiff(to) != 2 ||
                square().rowDiff(to) != 0 || board.isInCheck(side)) {
            return false;
        }
        // rook must be unmoved
        int castleWithCol = (to.col == 2) ? 0 : 7;
        Piece castleWith = board.contentsAt(to.row, castleWithCol);
        if (castleWith == null || !castleWith.isUnmoved()) {
            return false;
        }
        // can't castle through check
        Square intermediateSquare = new Square(to.row, (square().col+to.col)/2);
        Move intermediateMove = new Move(this, intermediateSquare);
        if (board.wouldBeInCheck(intermediateMove, side)) {
            return false;
        }
        // intervening squares must be empty
        for (int i = Math.min(castleWithCol, square().col) + 1;
                 i < Math.max(castleWithCol, square().col); i++) {
            if (board.contentsAt(square().row, i) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ArrayList<Move> getValidMoves() {
        ArrayList<Move> validMoves = new ArrayList<>();
        Move move;
        int toRow, toCol;
        Square to;
        // for all surrounding squares
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                toRow = square().row + i;
                toCol = square().col + j;
                // on the board
                if (0 <= toRow && toRow <= 7 && 0 <= toCol && toCol <= 7) {
                    to = new Square(toRow, toCol);
                    move = new Move(this, to);
                    // if square is empty or opponent-occupied
                    if ((board.contentsAt(to) == null || board.contentsAt(to).side != side)
                            && !board.wouldBeInCheck(move, side)) {
                        validMoves.add(move);
                    }
                }
            }
        }
        // only check castling if unmoved
        if (!isUnmoved()) {
            return validMoves;
        }
        Square queenCastlingSquare = new Square(square().row, square().col - 2);
        Square kingCastlingSquare = new Square(square().row, square().col + 2);
        if (castlingPossible(queenCastlingSquare)) {
            Move queenSideCastle = new Castle(this, queenCastlingSquare);
            if (!board.wouldBeInCheck(queenSideCastle, side)) {
                validMoves.add(queenSideCastle);
            }
        }
        if (castlingPossible(kingCastlingSquare)) {
            Move kingSideCastle = new Castle(this, kingCastlingSquare);
            if (!board.wouldBeInCheck(kingSideCastle, side)) {
                validMoves.add(kingSideCastle);
            }
        }
        return validMoves;
    }

    @Override
    public boolean validMoveExists() {
        Move move;
        int toRow, toCol;
        Square to;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                toRow = square().row + i;
                toCol = square().col + j;
                if (0 <= toRow && toRow <= 7 && 0 <= toCol && toCol <= 7) {
                    to = new Square(toRow, toCol);
                    move = new Move(this, to);
                    if ((board.contentsAt(to) == null || board.contentsAt(to).side != side)
                            && !board.wouldBeInCheck(move, side)) {
                        return true;
                    }
                }
            }
        }
        if (!isUnmoved()) {
            return false;
        }
        Square queenCastlingSquare = new Square(square().row, square().col - 2);
        Square kingCastlingSquare = new Square(square().row, square().col + 2);
        if (castlingPossible(queenCastlingSquare)) {
            Move queenSideCastle = new Castle(this, queenCastlingSquare);
            if (!board.wouldBeInCheck(queenSideCastle, side)) {
                return true;
            }
        }
        if (castlingPossible(kingCastlingSquare)) {
            Move kingSideCastle = new Castle(this, kingCastlingSquare);
            if (!board.wouldBeInCheck(kingSideCastle, side)) {
                return true;
            }
        }
        return false;
    }

    public int distanceFromEdge() {
        int verticalEdgeDistance =
                Math.min(square().row, 7 - square().row);
        int horizontalEdgeDistance =
                Math.min(square().col, 7 - square().col);
        return Math.min(verticalEdgeDistance, horizontalEdgeDistance);
    }

    public boolean hasCastled() {
        return hasCastled;
    }

    public void setHasCastled(boolean hasCastled) {
        this.hasCastled = hasCastled;
    }

}
