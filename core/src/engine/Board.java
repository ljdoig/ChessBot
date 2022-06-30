package engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class Board {
    public static final int SIZE = 8;
    protected int numBlack = 0;
    protected int numWhite = 0;
    protected Piece[] whitePieces;
    protected Piece[] blackPieces;
    private final Piece[][] contents = new Piece[SIZE][SIZE];
    public final ArrayList<Move> moveHistory = new ArrayList<>();
    private King whiteKing;
    private King blackKing;
    protected Side nextTurn = Side.WHITE;
    protected int halfmoveClock = 0;
    protected int fullmoveNumber = 1;
    public final ZobristTracker zobristTracker = new ZobristTracker(this);

    public Board() {}

    // copy constructor: duplicates true state before lookahead
    public Board(Board board) {
        whitePieces = new Piece[board.whitePieces.length];
        blackPieces = new Piece[board.blackPieces.length];
        deepCopy(board.whitePieces);
        deepCopy(board.blackPieces);
        // shallow copy is fine, we only really care about the squares (to/from)
        moveHistory.addAll((Collection<? extends Move>) board.moveHistory.clone());
        assert numWhite == board.numWhite;
        assert numBlack == board.numBlack;
        whiteKing = (King) contentsAt(board.whiteKing.square());
        blackKing = (King) contentsAt(board.blackKing.square());
        nextTurn = board.nextTurn;
        halfmoveClock = board.halfmoveClock;
        fullmoveNumber = board.fullmoveNumber;
    }

    private void deepCopy(Piece[] pieces) {
        // this will populate the new piece array and new board 'contents'
        Piece pieceCopy;
        for (Piece piece : pieces) {
            pieceCopy = piece.makeCopy(this);
            getPieces(piece.side)[piece.arrayIndex] = pieceCopy;
            if (!piece.hasBeenTaken()) {
                addPiece(pieceCopy);
            }
        }
    }

    public ArrayList<Move> getAllValidMoves() {
        return new ArrayList<Move>(40) {{
            for (Piece piece : getPieces(nextTurn)) {
                if (!piece.hasBeenTaken()) {
                    addAll(piece.getValidMoves());
                }
            }
        }};
    }

    public ArrayList<Move> getInterestingMoves() {
        return getAllValidMoves().stream()
                .filter(Move::isInteresting)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Move computeMove() {
        System.out.println("Computing move:");
        Node rootNode = new Node(this);
        Move bestMove = new Scorer(rootNode).getBestMove();
        bestMove.printAnticipatedSequence();
        return bestMove;
    }

    public boolean isInCheck(Side side) {
        for (Piece piece : getPieces(side.opponent())) {
            if (!piece.hasBeenTaken() && piece.isChecking()) {
                return true;
            }
        }
        return false;
    }

    public boolean wouldBeInCheck(Move move, Side side) {
        // simulate move
        move.make();
        boolean result = isInCheck(side);
        // reverse move
        move.undo();
        return result;
    }

    public boolean noValidMoveExists() {
        if (halfmoveClock >= 50) {
            return true;
        }
        for (Piece piece : getPieces(nextTurn)) {
            if (!piece.hasBeenTaken() && piece.validMoveExists()) {
                return false;
            }
        }
        return true;
    }

    public String toFen() {
        StringBuilder fen = new StringBuilder();
        StringBuilder rowStr;
        String pieceStr;
        int blank;
        for (int row = 0; row < SIZE; row++) {
            rowStr = new StringBuilder();
            blank = 0;
            for (int col = 0; col < SIZE; col++) {
                Piece piece = contentsAt(row, col);
                if (piece == null) {
                    blank++;
                    continue;
                } else if (piece instanceof Knight) {
                    pieceStr = "N";
                } else {
                    pieceStr = piece.getClass().getSimpleName().substring(0, 1);
                }
                if (piece.side == Side.BLACK) {
                    pieceStr = pieceStr.toLowerCase();
                }
                if (blank > 0) {
                    rowStr.append(blank);
                    blank = 0;
                }
                rowStr.append(pieceStr);
            }
            if (blank > 0) {
                rowStr.append(blank);
            }
            fen.append(rowStr).append("/");
        }
        // remove final "/"
        fen = new StringBuilder(fen.substring(0, fen.length() - 1));
        fen.append(" ").append(
                nextTurn.toString().substring(0, 1).toLowerCase());
        String castlingNotation = "";
        // check if white can castle either side
        if (getKing(Side.WHITE).isUnmoved()) {
            if (contentsAt(7, 7) != null &&
                    contentsAt(7, 7).isUnmoved()) {
                castlingNotation += "K";
            }
            if (contentsAt(7, 0) != null &&
                    contentsAt(7, 0).isUnmoved()) {
                castlingNotation += "Q";
            }
        }
        if (getKing(Side.BLACK).isUnmoved()) {
            if (contentsAt(0, 7) != null &&
                    contentsAt(0, 7).isUnmoved()) {
                castlingNotation += "k";
            }
            if (contentsAt(0, 0) != null &&
                    contentsAt(0, 0).isUnmoved()) {
                castlingNotation += "q";
            }
        }
        if (castlingNotation.length() == 0) {
            fen.append(" -");
        } else {
            fen.append(" ").append(castlingNotation);
        }
        Move lastMove = getLastMove();
        if (lastMove != null && lastMove.piece instanceof Pawn &&
                lastMove.rowDiff() == 2) {
            Square intermediateSquare = new Square(
                    (lastMove.to.row + lastMove.from.row) / 2,
                    lastMove.from.col);
            fen.append(" ").append(intermediateSquare);
        } else {
            fen.append(" -");
        }
        fen.append(" ").append(halfmoveClock);
        fen.append(" ").append(fullmoveNumber);
        return fen.toString();
    }

    public int evaluate() {
        if (noValidMoveExists()) {
            if (isInCheck(nextTurn)) {
                // reward longer games, encourages quicker checkmate
                return -Integer.MAX_VALUE + 1 + moveHistory.size();
            } else {
                // stalemate
                return 0;
            }
        }
        return oneSidedEval(nextTurn) - oneSidedEval(nextTurn.opponent());
    }

    public int oneSidedEval(Side side) {
        int evaluation = 0;
        boolean[] pawnCols = new boolean[SIZE];
        for (Piece piece : getPieces(side)) {
            if (piece.hasBeenTaken()) {
                continue;
            }
            evaluation += 1000 * piece.value;
            if (piece instanceof Pawn) {
                // moving from rank 6 to 7 is better than rank 3 to 4, hence pow
                evaluation += Math.pow(piece.progressFrom0thRank() - 1, 1.5);
                // penalise doubled pawns
                if (pawnCols[piece.square().col]) {
                    evaluation -= 3;
                } else {
                    pawnCols[piece.square().col] = true;
                }
                if (!piece.isUnmoved()) {
                    evaluation += 1;
                }
            } else if (piece instanceof King) {
                // castling is good, otherwise moving King out of position is bad
                if (((King) piece).hasCastled()) {
                    evaluation += 20;
                } else if (!piece.isUnmoved()) {
                    evaluation -= 10;
                }
            } else {
                // encourage moving up the board at the start of the game
                if (moveHistory.size() < 20) {
                    evaluation += piece.progressFrom0thRank();
                }
                if (!piece.isUnmoved() && piece.progressFrom0thRank() > 0) {
                    evaluation += 3;
                }
            }
        }
        // if only your king is left, keep away from edges and opponent's king
        if (numPieces(side) == 1) {
            evaluation += 3 * getKing(side).distanceFromEdge();
            evaluation += distanceBetweenKings();
        }
        return evaluation;
    }

    private int distanceBetweenKings() {
        Square whiteKingSquare = whiteKing.square();
        Square blackKingSquare = blackKing.square();
        return whiteKingSquare.rowDiff(blackKingSquare) +
               whiteKingSquare.colDiff(blackKingSquare);
    }

    public ArrayList<Square> pawnAttackedSquares() {
        ArrayList<Square> pawnAttackedSquares = new ArrayList<>();
        int attackedRow;
        if (nextTurn == Side.WHITE) {
            for (Piece piece : getPieces(Side.BLACK)) {
                if (!piece.hasBeenTaken() && piece instanceof Pawn) {
                    attackedRow = piece.square().row + 1;
                    if (piece.square().col == 0) {
                        pawnAttackedSquares.add(new Square(attackedRow, 1));
                    } else if (piece.square().col == 7) {
                        pawnAttackedSquares.add(new Square(attackedRow, 6));
                    } else {
                        pawnAttackedSquares.add(
                                new Square(attackedRow, piece.square().col-1)
                        );
                        pawnAttackedSquares.add(
                                new Square(attackedRow, piece.square().col+1)
                        );
                    }
                }
            }
        } else {
            for (Piece piece : getPieces(Side.WHITE)) {
                if (!piece.hasBeenTaken() && piece instanceof Pawn) {
                    attackedRow = piece.square().row - 1;
                    if (piece.square().col == 0) {
                        pawnAttackedSquares.add(new Square(attackedRow, 1));
                    } else if (piece.square().col == 7) {
                        pawnAttackedSquares.add(new Square(attackedRow, 6));
                    } else {
                        pawnAttackedSquares.add(
                                new Square(attackedRow, piece.square().col-1)
                        );
                        pawnAttackedSquares.add(
                                new Square(attackedRow, piece.square().col+1)
                        );
                    }
                }
            }
        }
        return pawnAttackedSquares;
    }

    public Piece contentsAt(int row, int col) {
        return contents[row][col];
    }

    public Piece contentsAt(Square square) {
        return contents[square.row][square.col];
    }

    public void setContents(Square square, Piece piece) {
        contents[square.row][square.col] = piece;
    }

    public Move getLastMove() {
        if (moveHistory.size() > 0) {
            return moveHistory.get(moveHistory.size() - 1);
        } else {
            return null;
        }
    }

    public Side getNextTurn() {
        return nextTurn;
    }

    public void updateNextTurn() {
        nextTurn = nextTurn.opponent();
    }

    public int getIndex(Piece piece) {
        if (piece.side == Side.WHITE) {
            return numWhite;
        } else {
            return numBlack;
        }
    }

    public void addPiece(Piece piece) {
        if (piece.side == Side.WHITE) {
            numWhite++;
        } else {
            numBlack++;
        }
        setContents(piece.square(), piece);
    }

    public Piece[] getPieces(Side side) {
        return (side == Side.WHITE) ? whitePieces : blackPieces;
    }

    public int numPieces(Side side) {
        return side == Side.WHITE ? numWhite : numBlack;
    }

    public void incrementNumPieces(Side side) {
        if (side == Side.WHITE) {
            numWhite++;
        } else {
            numBlack++;
        }
    }

    public void decrementNumPieces(Side side) {
        if (side == Side.WHITE) {
            numWhite--;
        } else {
            numBlack--;
        }
    }

    public void setWhiteKing(King whiteKing) {
        this.whiteKing = whiteKing;
    }

    public void setBlackKing(King blackKing) {
        this.blackKing = blackKing;
    }

    public King getKing(Side side) {
        return (side == Side.WHITE) ? whiteKing : blackKing;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public void setHalfmoveClock(int halfmoveClock) {
        this.halfmoveClock = halfmoveClock;
    }

    public int getFullmoveNumber() {
        return fullmoveNumber;
    }

    public void setFullmoveNumber(int fullmoveNumber) {
        this.fullmoveNumber = fullmoveNumber;
    }
}

