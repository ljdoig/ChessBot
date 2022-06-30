package engine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class RenderedBoard extends Board {
    private static final String standardSetup =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final Texture IMAGE = new Texture("board/board.png");
    private static final Texture PINK_SQR = new Texture("board/pink_sqr.png");
    private static final Texture BLUE_SQR = new Texture("board/blue_sqr.png");
    private static final Texture GREEN_SQR = new Texture("board/green_sqr.png");
    private static final Texture NTRL_SQR = new Texture("board/ntrl_sqr.png");
    private static final Texture BLUE_SQR2 = new Texture("board/blue_sqr+.png");
    private static final Texture GREEN_SQR2 = new Texture("board/green_sqr+.png");
    private static final Texture NTRL_SQR2 = new Texture("board/ntrl_sqr+.png");
    private String endOfGameMessage;
    private Piece focusedOn;
    private Move clickedMove;
    private ArrayList<String> clickedMoveAnalysis = new ArrayList<>();
    private boolean stalemated = false;
    private boolean checkmated = false;

    public RenderedBoard() {
        super();
        initialise(standardSetup);
    }

    public RenderedBoard(String fen) {
        super();
        initialise(fen);
    }

    public void render(SpriteBatch batch, boolean invert) {
        batch.begin();
        batch.draw(IMAGE, 0, 0);
        Move previousMove = getLastMove();
        if (previousMove != null) {
            renderSquare(batch, NTRL_SQR, previousMove.from, invert);
            renderSquare(batch, NTRL_SQR2, previousMove.to, invert);
        }
        if (isInCheck(nextTurn)) {
            renderSquare(
                    batch, checkmated ? GREEN_SQR : BLUE_SQR,
                    getKing(nextTurn).square(), invert
            );
            // in rare cases, multiple pieces can check; highlight them all
            Texture checkingSqr = checkmated ? GREEN_SQR2 : BLUE_SQR2;
            for (Piece checkingPiece : checkingPieces(nextTurn)) {
                renderSquare(batch, checkingSqr, checkingPiece.square(), invert);
            }
        }
        if (focusedOn != null) {
            renderSquare(batch, PINK_SQR, focusedOn.square(), invert);
        }
        for (Piece piece : super.getPieces(Side.WHITE)) {
            piece.render(batch, invert);
        }
        for (Piece piece : super.getPieces(Side.BLACK)) {
            piece.render(batch, invert);
        }
        batch.end();
    }

    // pieces that are putting this side in check
    private ArrayList<Piece> checkingPieces(Side side) {
        ArrayList<Piece> checkingPieces = new ArrayList<>();
        for (Piece piece : getPieces(side.opponent())) {
            if (!piece.hasBeenTaken() && piece.isChecking()) {
                checkingPieces.add(piece);
            }
        }
        return checkingPieces;
    }

    private void renderSquare(SpriteBatch batch, Texture image, Square square,
                              boolean invert) {
        Point location = square.getLocation(false, invert);
        batch.draw(image, location.x, location.y);
    }

    public Move processLeftClick(Square clickedSquare) {
        Piece clickedPiece = contentsAt(clickedSquare);
        // player has selected a piece
        if (clickedPiece != null && clickedPiece.side == nextTurn) {
            focusedOn = clickedPiece;
        }
        // player has attempted a move
        if (focusedOn != null && (clickedPiece == null || clickedPiece.side != focusedOn.side)) {
            clickedMove = Move.getClickedMove(focusedOn, clickedSquare);
            return clickedMove;
        }
        return null;
    }

    public Move processRightClick(Square clickedSquare) {
        if (focusedOn == null)
            return null;
        Piece clickedPiece = contentsAt(clickedSquare);
        // player has attempted a move
        if (focusedOn != null &&
                (clickedPiece == null || clickedPiece.side != focusedOn.side)) {
            clickedMove = Move.getClickedMove(focusedOn, clickedSquare);
            if (clickedMove != null) {
                return clickedMove;
            }
        }
        return null;
    }

    public void undoLastMove() {
        if (getLastMove() != null) {
            getLastMove().undo();
            focusedOn = null;
            stalemated = false;
            checkmated = false;
        } else {
            System.out.println("Nothing to undo");
        }
    }

    public void computeAndMakeMove() {
        finaliseMove(super.computeMove());
    }

    public void finaliseMove(Move move) {
        move.make();
        focusedOn = null;
        clickedMove = null;
        checkEndOfGame();
        System.out.format("Zobrist hash: %d\n\n", zobristTracker.getVal());
    }

    public void checkEndOfGame() {
        if (noValidMoveExists()) {
            if (isInCheck(nextTurn)) {
                declareCheckmate();
            } else {
                declareStalemate();
            }
        } else if (!stalemated) {
            checkImplicitStalemate();
            if (halfmoveClock >= 50) {
                declareStalemate();
            }
        }
    }


    private void checkImplicitStalemate() {
        if (numBlack + numWhite == 2) {
            declareStalemate();
        } else if (numBlack == 2 || numWhite == 2) {
            for (Piece piece : getPieces(Side.WHITE)) {
                if (piece.hasBeenTaken()) continue;
                if (piece instanceof Queen || piece instanceof Rook ||
                        piece instanceof Pawn) {
                    return;
                }
            }
            for (Piece piece : getPieces(Side.BLACK)) {
                if (piece.hasBeenTaken()) continue;
                if (piece instanceof Queen || piece instanceof Rook ||
                        piece instanceof Pawn) {
                    return;
                }
            }
            declareStalemate();
        }
    }

    private void declareCheckmate() {
        checkmated = true;

        endOfGameMessage = String.format(
                "Checkmate! Victory to %s in %d moves",
                nextTurn.opponent(),
                // if black just checkmated, it won't really be 'next turn'
                nextTurn == Side.WHITE ? fullmoveNumber - 1 : fullmoveNumber
        );
    }

    private void declareStalemate() {
        stalemated = true;
        endOfGameMessage = String.format(
                "Stalemate... How boring. In %d moves",
                fullmoveNumber
        );
    }

    public boolean gameFinished() {
        return checkmated || stalemated;
    }

    private void initialise(String fen) {
        String[] fenComponents = fen.split(" ");
        String[] boardRows = fenComponents[0].split("/");
        int charInRow;
        ArrayList<Piece> whitePieces = new ArrayList<>(16);
        ArrayList<Piece> blackPieces = new ArrayList<>(16);
        for (int row = 0; row < SIZE; row++) {
            charInRow = 0;
            for (int col = 0; col < SIZE; col++) {
                char currentChar = boardRows[row].charAt(charInRow);
                if (Character.isDigit(currentChar)) {
                    col += Character.getNumericValue(currentChar) - 1;
                } else {
                    Side pieceSide = (Character.isUpperCase(currentChar)) ?
                            Side.WHITE : Side.BLACK;
                    String pieceType = Character
                            .toString(currentChar)
                            .toLowerCase();
                    switch (pieceType) {
                        case "k":
                            King newKing;
                            if (pieceSide == Side.WHITE) {
                                newKing = new King(Side.WHITE, row, col, this);
                                whitePieces.add(newKing);
                                setWhiteKing(newKing);
                            } else {
                                newKing = new King(Side.BLACK, row, col, this);
                                blackPieces.add(newKing);
                                setBlackKing(newKing);
                            }
                            break;
                        case "q":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Queen(
                                        Side.WHITE, row, col, true, this
                                ));
                            } else {
                                blackPieces.add(new Queen(
                                        Side.BLACK, row, col, true, this
                                ));
                            }
                            break;
                        case "r":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Rook(Side.WHITE, row, col, this));
                            } else {
                                blackPieces.add(new Rook(Side.BLACK, row, col, this));
                            }
                            break;
                        case "b":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Bishop(
                                        Side.WHITE, row, col, true, this
                                ));
                            } else {
                                blackPieces.add(new Bishop(
                                        Side.BLACK, row, col, true, this
                                ));
                            }
                            break;
                        case "n":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Knight(
                                        Side.WHITE, row, col, true, this
                                ));
                            } else {
                                blackPieces.add(new Knight(
                                        Side.BLACK, row, col, true, this
                                ));
                            }
                            break;
                        case "p":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Pawn(Side.WHITE, row, col, this));
                            } else {
                                blackPieces.add(new Pawn(Side.BLACK, row, col, this));
                            }
                            break;
                        default:
                            assert false : "bad fen ; "+ pieceType + " invalid";
                            break;
                    }
                }
                charInRow++;
            }
        }
        this.whitePieces = new Piece[whitePieces.size()];
        this.blackPieces = new Piece[blackPieces.size()];
        for (int i = 0; i < whitePieces.size(); i++) {
            this.whitePieces[i] = whitePieces.get(i);
        }
        for (int i = 0; i < blackPieces.size(); i++) {
            this.blackPieces[i] = blackPieces.get(i);
        }
        nextTurn = (fenComponents[1].equals("w")) ? Side.WHITE : Side.BLACK;
        // what castling is allowed
        if (!fenComponents[2].contains("K")) {
            if (contentsAt(7, 7) instanceof Rook) {
                contentsAt(7, 7).setUnmoved(false);
            }
        } else {
            assert contentsAt(7, 7) instanceof Rook;
            assert contentsAt(7, 4) instanceof King;
            assert contentsAt(7, 7).isUnmoved();
            assert contentsAt(7, 4).isUnmoved();
        }
        if (!fenComponents[2].contains("Q")) {
            if (contentsAt(7, 0) instanceof Rook) {
                contentsAt(7, 0).setUnmoved(false);
            }
        } else {
            assert contentsAt(7, 0) instanceof Rook;
            assert contentsAt(7, 4) instanceof King;
            assert contentsAt(7, 0).isUnmoved();
            assert contentsAt(7, 4).isUnmoved();
        }
        if (!fenComponents[2].contains("k")) {
            if (contentsAt(0, 7) instanceof Rook) {
                contentsAt(0, 7).setUnmoved(false);
            }
        } else {
            assert contentsAt(0, 7) instanceof Rook;
            assert contentsAt(0, 4) instanceof King;
            assert contentsAt(0, 7).isUnmoved();
            assert contentsAt(0, 4).isUnmoved();
        }
        if (!fenComponents[2].contains("q")) {
            if (contentsAt(0, 0) instanceof Rook) {
                contentsAt(0, 0).setUnmoved(false);
            }
        } else {
            assert contentsAt(0, 0) instanceof Rook;
            assert contentsAt(0, 4) instanceof King;
            assert contentsAt(0, 0).isUnmoved();
            assert contentsAt(0, 4).isUnmoved();
        }
        if (!fenComponents[3].equals("-")) {
            // previous move allows en passant
            // we need to fabricate the previous move, so en passant is allowed
            char colChar = fenComponents[3].charAt(0);
            char rowChar = fenComponents[3].charAt(1);
            int col = colChar - 'a';
            int row = 8 - Character.getNumericValue(rowChar);
            int lastMoveToRow = (row == 2) ? 3 : 4;
            int lastMoveFromRow = (row == 2) ? 1 : 6;
            Square movedTo = new Square(lastMoveToRow, col);
            Square movedFrom = new Square(lastMoveFromRow, col);
            Piece movedPiece = contentsAt(lastMoveToRow, col);
            movedPiece.setSquare(movedFrom);
            setContents(movedFrom, movedPiece);
            setContents(movedTo, null);
            movedPiece.setUnmoved(true);
            Move lastMove = new Move(movedPiece, movedTo);
            updateNextTurn();
            finaliseMove(lastMove);
        }
        halfmoveClock = Integer.parseInt(fenComponents[4]);
        fullmoveNumber = Integer.parseInt(fenComponents[5]);
        assert toFen().equals(fen) :
                "\nResulting fen: " + fen +
                        "\nExpected fen:  " + toFen();
    }

    public static void dispose() {
        IMAGE.dispose();
        PINK_SQR.dispose();
        NTRL_SQR.dispose();
        BLUE_SQR.dispose();
        GREEN_SQR.dispose();
        NTRL_SQR2.dispose();
        BLUE_SQR2.dispose();
        GREEN_SQR2.dispose();
    }

    public String getEndOfGameMessage() {
        return endOfGameMessage;
    }
}
