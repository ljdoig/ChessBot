package engine;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Pawn extends Piece {
    private static final Texture BLACK_IMAGE = new Texture("pieces/black/pawn.png");
    private static final Texture WHITE_IMAGE = new Texture("pieces/white/pawn.png");
    private static final int value = 1;
    private static final int pieceIndex = 0;

    public Pawn(Side side, int row, int col) {
        super(side, value, pieceIndex, row, col,
                (side == Side.WHITE) ? (row == 6) : (row == 1),
                (side == Side.WHITE) ? WHITE_IMAGE : BLACK_IMAGE);
    }

    // Copy constructor
    public Pawn(Pawn pawn, Board board) {
        super(pawn, board);
    }

    @Override
    protected Piece makeCopy(Board newBoard) {
        return new Pawn(this, newBoard);
    }

    @Override
    public boolean isChecking() {
        Square kingSquare = opponentKingSquare();
        if (kingSquare.colDiff(square()) == 1) {
            if (side == Side.WHITE) {
                return kingSquare.row == square().row - 1;
            } else {
                return kingSquare.row == square().row + 1;
            }
        }
        return false;
    }

    private boolean canReach(Square to) {
        int oldRow = square().row;
        int oldCol = square().col;
        int newRow = to.row;
        int newCol = to.col;
        // check moving in the right direction
        if ((side == Side.WHITE && newRow >= oldRow) ||
            (side == Side.BLACK && newRow <= oldRow)) {
            return false;
        }
        // pawn can be moved 2 squares forward if unmoved
        if (isUnmoved() && newCol == oldCol && square().rowDiff(to) == 2) {
            // intervening square and destination square must be empty
            return (board.contentsAt(to) == null &&
                    board.contentsAt((oldRow + newRow) / 2, oldCol) == null);
        }
        // otherwise, must be moved one square forward
        if (square().rowDiff(to) != 1) {
            return false;
        }
        // check if pawn can be moved 1 square forward; destination must be empty
        if (newCol == oldCol && board.contentsAt(newRow, newCol) == null) {
            return true;
        }
        // otherwise, must be moved one col sideways to take
        if (square().colDiff(to) != 1) {
            return false;
        }
        // check if pawn can take another piece
        return board.contentsAt(newRow, newCol) != null &&
                board.contentsAt(newRow, newCol).side != side;
    }

    @Override
    public ArrayList<Move> getValidMoves() {
        ArrayList<Move> validMoves = new ArrayList<>();
        Move move;
        Square to;
        int toRow = (side == Side.WHITE) ? square().row - 1 : square().row + 1;
        for (int toCol = square().col - 1; toCol <= square().col + 1; toCol++) {
            if (!(0 <= toCol && toCol <= 7)) {
                continue;
            }
            to = new Square(toRow, toCol);
            if (promotionPossible(to)) {
                // if promotion is possible, no other move type is possible
                validMoves.addAll(getValidPromotions(to));
            } else if (enPassantPossible(to)) {
                Move enPassant = new EnPassant(this, to);
                if (!board.wouldBeInCheck(enPassant, side)) {
                    validMoves.add(enPassant);
                }
            } else {
                if ((square().row == 1 && side == Side.WHITE) ||
                    (square().row == 6 && side == Side.BLACK)) {
                    continue;
                }
                if (canReach(to)) {
                    move = new Move(this, to);
                    if (!board.wouldBeInCheck(move, side)) {
                        validMoves.add(move);
                    }
                }
            }
        }
        if (isUnmoved()) {
            toRow = side == Side.WHITE ? square().row - 2 : square().row + 2;
            to = new Square(toRow, square().col);
            if (canReach(to)) {
                move = new Move(this, to);
                if (!board.wouldBeInCheck(move, side)) {
                    validMoves.add(move);
                }
            }
        }
        return validMoves;
    }

    @Override
    public boolean validMoveExists() {
        Move move;
        Square to;
        int toRow = (side == Side.WHITE) ? square().row - 1 : square().row + 1;
        for (int toCol = square().col - 1; toCol <= square().col + 1; toCol++) {
            if (!(0 <= toCol && toCol <= 7)) {
                continue;
            }
            to = new Square(toRow, toCol);
            if (promotionPossible(to)) {
                // if promotion is possible, no other move type is possible
                if (getValidPromotions(to).size() > 0) {
                    return true;
                }
            } else if (enPassantPossible(to)) {
                Move enPassant = new EnPassant(this, to);
                if (!board.wouldBeInCheck(enPassant, side)) {
                    return true;
                }
            } else {
                if ((square().row == 1 && side == Side.WHITE) ||
                        (square().row == 6 && side == Side.BLACK)) {
                    continue;
                }
                if (canReach(to)) {
                    move = new Move(this, to);
                    if (!board.wouldBeInCheck(move, side)) {
                        return true;
                    }
                }
            }
        }
        if (isUnmoved()) {
            toRow = side == Side.WHITE ? square().row - 2 : square().row + 2;
            to = new Square(toRow, square().col);
            if (canReach(to)) {
                move = new Move(this, to);
                return !board.wouldBeInCheck(move, side);
            }
        }
        return false;
    }

    public ArrayList<Promotion> getValidPromotions(Square to) {
        ArrayList<Promotion> validPromotions = new ArrayList<>();
        if (square().colDiff(to) == 0 && board.contentsAt(to) != null) {
            return validPromotions;
        }
        if (square().colDiff(to) == 1 && (board.contentsAt(to) == null ||
            board.contentsAt(to).side == side)) {
            return validPromotions;
        }
        Promotion samplePromotion =
                new Promotion(this, to, new Queen(this, to));
        if (!board.wouldBeInCheck(samplePromotion, side)) {
            validPromotions.add(samplePromotion);
            validPromotions.add(new Promotion(this, to, new Rook(this, to)));
            validPromotions.add(new Promotion(this, to, new Knight(this, to)));
            validPromotions.add(new Promotion(this, to, new Bishop(this, to)));
        }
        return validPromotions;
    }

    @Override
    public boolean enPassantPossible(Square to) {
        if ((side == Side.WHITE && square().row == 3 && to.row == 2) ||
            (side == Side.BLACK && square().row == 4 && to.row == 5)) {
            Piece beingTaken = board.contentsAt(square().row, to.col);
            // can only en passant a pawn that is 1 rank and 1 file away
            if (beingTaken instanceof Pawn &&
                    square().rowDiff(to) == 1 && square().colDiff(to) == 1) {
                Move lastMove = board.getLastMove();
                // pawn must be behind the new square
                if (lastMove != null && lastMove.to.col == to.col &&
                        lastMove.to.row == square().row) {
                    // previous move must have been 2 squares
                    return lastMove.rowDiff() == 2;
                }
            }
        }
        return false;
    }

    /* preliminary check to assess if promotion is possible based solely on
       rank */
    @Override
    public boolean promotionPossible(Square to) {
        if (side == Side.WHITE && (!(square().row == 1 && to.row == 0))) {
                return false;
        }
        if (side == Side.BLACK && (!(square().row == 6 && to.row == 7))) {
                return false;
        }
        return true;
    }

    public static void dispose() {
        BLACK_IMAGE.dispose();
        WHITE_IMAGE.dispose();
    }

}
