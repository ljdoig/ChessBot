import bagel.Image;
import bagel.util.Point;

import java.util.ArrayList;

public abstract class Piece {
    public final Board board;
    public final Side side;
    public final int value;
    private Square square;
    private boolean unmoved;
    private final Image image;
    private boolean taken = false;
    public final int index;

    public Piece(Side side, int value, int row, int col, boolean unmoved, Image image) {
        this.side = side;
        this.value = value;
        this.square = new Square(row, col);
        this.unmoved = unmoved;
        this.image = image;
        this.board = ChessGame.getCurrentBoard();
        this.index = board.getIndex(this);
        board.addPiece(this);
    }

    // For promoted pawns
    public Piece(Piece promotedFrom, int value, Square to, Image image) {
        this.side = promotedFrom.side;
        this.value = value;
        square = to;
        unmoved = false;
        this.index = promotedFrom.index;
        this.image = image;
        this.board = promotedFrom.board;
    }

    // For creating copies, just for lookahead on move scoring, not rendering
    public Piece(Piece piece, Board newBoard) {
        this.side = piece.side;
        this.value = piece.value;
        this.square = piece.square;
        this.unmoved = piece.unmoved;
        this.image = piece.image;
        this.board = newBoard;
        this.taken = piece.taken;
        this.index = piece.index;
    }

    protected abstract Piece makeCopy(Board newBoard);

    protected Square opponentKingSquare() {
        return board.getKing(side.opponent()).square();
    }

    protected abstract boolean isChecking();

    public abstract boolean validMoveExists();

    public abstract ArrayList<Move> getValidMoves();

    protected boolean isCheckingOrthogonally() {
        Square kingSquare = opponentKingSquare();
        int rowDiff = kingSquare.rowDiff(square);
        int colDiff = kingSquare.colDiff(square);
        if (rowDiff == 0) {
            // checking horizontally
            int lowerCol = Math.min(kingSquare.col, square.col);
            for (int i = 1; i < kingSquare.colDiff(square); i++) {
                if (board.contentsAt(square.row, lowerCol + i) != null) {
                    return false;
                }
            }
            return true;
        } else if (colDiff == 0) {
            // checking vertically
            int lowerRow = Math.min(kingSquare.row, square.row);
            for (int i = 1; i < kingSquare.rowDiff(square); i++) {
                if (board.contentsAt(lowerRow + i, square.col) != null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected boolean isCheckingDiagonally() {
        Square kingSquare = opponentKingSquare();
        int rowDiff = kingSquare.rowDiff(square());
        int colDiff = kingSquare.colDiff(square());
        if (colDiff == rowDiff) {
            int checkRow, checkCol;
            for (int i = 1; i < rowDiff; i++) {
                // check all intervening squares on the diagonal are empty
                checkRow = (i*(kingSquare.row - square.row)/rowDiff) + square.row;
                checkCol = (i*(kingSquare.col - square.col)/colDiff) + square.col;
                if (board.contentsAt(checkRow, checkCol) != null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public ArrayList<Move> getValidOrthogonalMoves() {
        ArrayList<Move> validMoves = new ArrayList<>();
        Move move;
        Square to;
        // moving down or up
        int testRow;
        for (int i = 0; i < 2; i++) {
            testRow = square.row + ((i==0) ? 1 : -1);
            while (0 <= testRow && testRow <= 7 &&
                    (board.contentsAt(testRow, square.col) == null)) {
                to = new Square(testRow, square.col);
                move = new Move(this, to);
                if (!board.wouldBeInCheck(move, side)) {
                    validMoves.add(move);
                }
                testRow += (i==0) ? 1 : -1;
            }
            if (0 <= testRow && testRow <= 7 &&
                    board.contentsAt(testRow, square.col) != null &&
                    board.contentsAt(testRow, square.col).side != side) {
                to = new Square(testRow, square.col);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    validMoves.add(move);
                }
            }
        }
        // moving right or left
        int testCol;
        for (int i = 0; i < 2; i++) {
            testCol = square.col + ((i == 0) ? 1 : -1);
            while (0 <= testCol && testCol <= 7 &&
                    (board.contentsAt(square.row, testCol) == null)) {
                to = new Square(square.row, testCol);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    validMoves.add(move);
                }
                testCol += (i==0) ? 1 : -1;
            }
            if (0 <= testCol && testCol <= 7 &&
                    board.contentsAt(square.row, testCol) != null &&
                    board.contentsAt(square.row, testCol).side != side) {
                to = new Square(square.row, testCol);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    validMoves.add(move);
                }
            }
        }
        return validMoves;
    }

    public ArrayList<Move> getValidDiagonalMoves() {
        ArrayList<Move> validMoves = new ArrayList<>();
        Move move;
        Square to;
        int testRow, testCol;
        // each loop considers one diagonal
        // boolean expressions decide which direction piece will 'move'
        for (int i = 0; i < 4; i++) {
            int verticalMovement   = (  (i <= 1)   ? 1 : -1);
            int horizontalMovement = ((i % 2 == 0) ? 1 : -1);
            testRow = square.row + verticalMovement;
            testCol = square.col + horizontalMovement;
            while (0 <= testRow && testRow <= 7 && 0 <= testCol && testCol <= 7 &&
                    (board.contentsAt(testRow, testCol) == null)) {
                to = new Square(testRow, testCol);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    validMoves.add(move);
                }
                testRow += verticalMovement;
                testCol += horizontalMovement;
            }
            // once loop is broken, check if it can reach that square on which
            // the loop broke, might be able to take that piece
            if (0 <= testRow && testRow <= 7 && 0 <= testCol && testCol <= 7 &&
                    board.contentsAt(testRow, testCol) != null &&
                    board.contentsAt(testRow, testCol).side != side) {
                to = new Square(testRow, testCol);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    validMoves.add(move);
                }
            }
        }
        return validMoves;
    }

    public boolean validOrthogonalMoveExists() {
        Move move;
        Square to;
        // moving down or up
        int testRow;
        for (int i = 0; i < 2; i++) {
            testRow = square.row + ((i==0) ? 1 : -1);
            while (0 <= testRow && testRow <= 7 &&
                    (board.contentsAt(testRow, square.col) == null)) {
                to = new Square(testRow, square.col);
                move = new Move(this, to);
                if (!board.wouldBeInCheck(move, side)) {
                    return true;
                }
                testRow += (i==0) ? 1 : -1;
            }
            if (0 <= testRow && testRow <= 7 &&
                    board.contentsAt(testRow, square.col) != null &&
                    board.contentsAt(testRow, square.col).side != side) {
                to = new Square(testRow, square.col);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    return true;
                }
            }
        }
        // moving right or left
        int testCol;
        for (int i = 0; i < 2; i++) {
            testCol = square.col + ((i == 0) ? 1 : -1);
            while (0 <= testCol && testCol <= 7 &&
                    (board.contentsAt(square.row, testCol) == null)) {
                to = new Square(square.row, testCol);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    return true;
                }
                testCol += (i==0) ? 1 : -1;
            }
            if (0 <= testCol && testCol <= 7 &&
                    board.contentsAt(square.row, testCol) != null &&
                    board.contentsAt(square.row, testCol).side != side) {
                to = new Square(square.row, testCol);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean validDiagonalMoveExists() {
        Move move;
        Square to;
        int testRow, testCol;
        // each loop considers one diagonal
        // boolean expressions decide which direction piece will 'move'
        for (int i = 0; i < 4; i++) {
            int verticalMovement   = (  (i <= 1)   ? 1 : -1);
            int horizontalMovement = ((i % 2 == 0) ? 1 : -1);
            testRow = square.row + verticalMovement;
            testCol = square.col + horizontalMovement;
            while (0 <= testRow && testRow <= 7 && 0 <= testCol && testCol <= 7 &&
                    (board.contentsAt(testRow, testCol) == null)) {
                to = new Square(testRow, testCol);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    return true;
                }
                testRow += verticalMovement;
                testCol += horizontalMovement;
            }
            if (0 <= testRow && testRow <= 7 && 0 <= testCol && testCol <= 7 &&
                    board.contentsAt(testRow, testCol) != null &&
                    board.contentsAt(testRow, testCol).side != side) {
                to = new Square(testRow, testCol);
                move = new Move(this, to) ;
                if (!board.wouldBeInCheck(move, side)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void render() {
        if (!taken) {
            Point location = square.getLocation();
            image.draw(location.x, location.y);
        }
    }

    public boolean castlingPossible(Square to) {
        return false;
    }

    public boolean enPassantPossible(Square to) {
        return false;
    }

    public boolean promotionPossible(Square to) {
        return false;
    }

    public boolean isUnmoved() {
        return unmoved;
    }

    public void setUnmoved(boolean unmoved) {
        this.unmoved = unmoved;
    }

    public void setSquare(Square square) {
        this.square = square;
    }

    public int progressFrom0thRank() {
        if (side == Side.WHITE) {
            return 7 - square.row;
        } else {
            return square.row;
        }
    }

    public boolean hasBeenTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    @Override
    public String toString() {
        return side + " " + String.format("%-6s", getClass().getName());
    }

    public String longToString() {
        return "Piece{" +
                "side=" + side +
                ", value=" + value +
                ", square=" + square +
                ", unmoved=" + unmoved +
                ", taken=" + taken +
                ", index=" + index +
                '}';
    }

    public Square square() {
        return square;
    }

}
