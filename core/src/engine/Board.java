package engine;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.chessbot.ChessGame;
import com.chessbot.FontLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;

public class Board {
    private static final Texture IMAGE = new Texture(
            "board/chess_board_" + ChessGame.SIZE + ".png");
    private static final Texture RED_SQUARE = new Texture(
            "board/red_square_" + ChessGame.SIZE + ".png");
    private static final Texture PREVMOVE_SQUARE = new Texture(
            "board/prevmove_square_" + ChessGame.SIZE + ".png");
    private static final Texture BLUE_SQUARE = new Texture(
            "board/blue_square_" + ChessGame.SIZE + ".png");
    private static final Texture PINK_SQUARE = new Texture(
            "board/pink_square_" + ChessGame.SIZE + ".png");
    private static final Texture GREEN_SQUARE = new Texture(
            "board/green_square_" + ChessGame.SIZE + ".png");
    private static final int SIZE = 8;
    private static final int NUM_PIECE_TYPES = 6;
    public static boolean flipBoard = false;
    private static final int fontSize = ChessGame.SIZE / 40;
    private static final int fontSpacing = ChessGame.SIZE / 50;
    private static final BitmapFont font =
            FontLoader.load("font/Lotuscoder-0WWrG.ttf", fontSize);
    private static final SpriteBatch batch = new SpriteBatch();
    public static final OrthographicCamera cam = new OrthographicCamera();
    public static final StretchViewport viewport =
            new StretchViewport(
                    (float) (ChessGame.SIZE * ChessGame.ASPECT_RATIO),
                    ChessGame.SIZE,
                    cam
            );
    private int numBlack = 0;
    private int numWhite = 0;
    private Piece[] whitePieces;
    private Piece[] blackPieces;
    private final Piece[][] contents = new Piece[SIZE][SIZE];
    public final ArrayList<Move> moveHistory = new ArrayList<>();
    private King whiteKing;
    private King blackKing;
    private Side nextTurn = Side.WHITE;
    private Piece focusedOn;
    private ArrayList<Move> possibleMoves;
    private Move clickedMove;
    private ArrayList<String> clickedMoveAnalysis = new ArrayList<>();
    private boolean stalemated = false;
    private boolean checkmated = false;
    private String endOfGameMessage;
    private int halfmoveClock = 0;
    private int fullmoveNumber = 1;
    public int getAllValidMoveCalls = 0;
    private final long[][][] zobristTable = initialiseZobristTable();
    private long zobrist = 0;

    public Board() {
        viewport.apply();
        cam.position.set(
                (float) (ChessGame.SIZE * ChessGame.ASPECT_RATIO / 2),
                ChessGame.SIZE / 2,
                0
        );
        cam.update();
    }

    // copy constructor, for doing lookahead
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
        focusedOn = board.focusedOn;
        possibleMoves = board.possibleMoves;
        assert !board.stalemated;
        assert !board.checkmated;
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

    public void render() {
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        batch.draw(IMAGE, 0, 0);
        Move previousMove = getLastMove();
        if (previousMove != null) {
            Point from = previousMove.from.getLocation(false);
            Point to = previousMove.to.getLocation(false);
            batch.draw(PREVMOVE_SQUARE, from.x, from.y);
            batch.draw(PREVMOVE_SQUARE, to.x, to.y);
        }
        if (isInCheck(nextTurn)) {
            Texture squareMarker = checkmated ? GREEN_SQUARE : BLUE_SQUARE;
            Point checkedLoc = getKing(nextTurn).square().getLocation(false);
            batch.draw(squareMarker, checkedLoc.x, checkedLoc.y);
            // in rare cases, multiple pieces can check; highlight them all
            for (Piece checkingPiece : checkingPieces(nextTurn)) {
                Point checkedByLoc = checkingPiece.square().getLocation(false);
                batch.draw(squareMarker, checkedByLoc.x, checkedByLoc.y);
            }
        }
        if (focusedOn != null) {
            highlightPossibleMoves();
            Point focusedOnLocation = focusedOn.square().getLocation(false);
            batch.draw(RED_SQUARE, focusedOnLocation.x, focusedOnLocation.y);
        }
        for (Piece piece : whitePieces) {
            piece.render(batch);
        }
        for (Piece piece : blackPieces) {
            piece.render(batch);
        }
        displayInfo();
        batch.end();
    }

    private void displayInfo() {
        int col = IMAGE.getWidth() + fontSpacing;
        float rowOneY = ChessGame.SIZE - (fontSpacing + fontSize) * (0.75f);
        float rowTwoY = ChessGame.SIZE - (fontSpacing + fontSize) * (1.75f);
        float rowThreeY = ChessGame.SIZE - (fontSpacing + fontSize) * (2.75f);
        font.draw(
                batch, String.format("Turn:           %3d", fullmoveNumber),
                col, rowOneY
        );
        font.draw(
                batch, String.format("Half-move clock:%3d", halfmoveClock),
                col, rowTwoY
        );
        font.draw(
                batch, String.format("Zobrist hash:     %s", zobrist),
                col, rowThreeY
        );
        ArrayList<String> other = otherInfo();
        for (int i = 0; i < other.size(); i++) {
            font.draw(
                    batch, other.get(i),
                    col,
                    ChessGame.SIZE - ((fontSpacing + fontSize) * (i + 4.75f))
            );
        }
    }

    public ArrayList<String> otherInfo() {
        final Move lastMove = getLastMove();
        return new ArrayList<String>(){{
            if (lastMove != null && clickedMove == null) {
                if (lastMove.getScore() != null) {
                    add(String.format("Last move: (%d leaf-nodes, %.2fs, depth: %d)",
                            lastMove.getLeafNodesSearchedToScore(),
                            lastMove.getTimeToScoreSecs(),
                            lastMove.getScoreDepth()));
                    add(lastMove.toString());
                    addAll(lastMove.getAnticipatedSequence());
                } else {
                    add("Last move:");
                    add(lastMove.toString());
                }
            }
            if (gameFinished()) {
                add(endOfGameMessage);
            }
            if (clickedMove != null) {
                addAll(clickedMoveAnalysis);
            }
        }};
    }

    public void processClick(double x_prop, double y_prop, boolean rightClick) {
        int clickedRow = (int) (y_prop * 8);
        int clickedCol = (int) (x_prop * 8);
        // invert axes
        if (flipBoard) {
            clickedRow = 7 - clickedRow;
            clickedCol = 7 - clickedCol;
        }
        // clicked out of bounds
        if (clickedRow < 0 || 7 < clickedRow ||
                clickedCol < 0 || 7 < clickedCol) {
            focusedOn = null;
            return;
        }
        Piece clickedPiece = contents[clickedRow][clickedCol];
        // player has selected a piece
        if (clickedPiece != null && clickedPiece.side == nextTurn) {
            focusedOn = clickedPiece;
            possibleMoves = focusedOn.getValidMoves();
        }
        // player has attempted a move
        if (focusedOn != null &&
                (clickedPiece == null || clickedPiece.side != focusedOn.side)) {
            clickedMove = Move.getClickedMove(
                    focusedOn, new Square(clickedRow, clickedCol)
            );
            if (clickedMove != null) {
                if (rightClick) {
                    System.out.println("Clicked move:");
                    System.out.println(clickedMove);
                    System.out.println("Analysing: ");
                    clickedMoveAnalysis = clickedMove.analysis();
                    clickedMove.printAnticipatedSequence();
                } else {
                    finaliseMove(clickedMove);
                }
            }
        }
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

    public ArrayList<Move> getAllValidMoves() {
        getAllValidMoveCalls++;
        return new ArrayList<Move>() {{
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

    public void computeMove() {
        System.out.println("Computing move:");
        Node rootNode = new Node(this);
        Move bestMove = new Scorer(rootNode).getBestMove();
        bestMove.printAnticipatedSequence();
        finaliseMove(bestMove);
    }

    public void finaliseMove(Move move) {
        move.make();
        focusedOn = null;
        clickedMove = null;
        checkEndOfGame();
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
        }
    }

    // pieces that are putting this side in check
    public ArrayList<Piece> checkingPieces(Side side) {
        ArrayList<Piece> checkingPieces = new ArrayList<>();
        for (Piece piece : getPieces(side.opponent())) {
            if (!piece.hasBeenTaken() && piece.isChecking()) {
                checkingPieces.add(piece);
            }
        }
        return checkingPieces;
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
        for (Piece piece : getPieces(nextTurn)) {
            if (!piece.hasBeenTaken() && piece.validMoveExists()) {
                return false;
            }
        }
        return true;
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
                "\nCheckmate! Victory to %s in %d moves.\n",
                nextTurn.opponent(), fullmoveNumber);
    }

    private void declareStalemate() {
        stalemated = true;
        endOfGameMessage = String.format(
                "\nStalemate... How boring. In %d moves.\n",
                fullmoveNumber);
    }

    public String toFen() {
        StringBuilder board = new StringBuilder();
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
            board.append(rowStr).append("/");
        }
        board = new StringBuilder(board.substring(0, board.length() - 1));
        board.append(" ").append(
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
            board.append(" -");
        } else {
            board.append(" ").append(castlingNotation);
        }
        Move lastMove = getLastMove();
        if (lastMove != null && lastMove.piece instanceof Pawn &&
                lastMove.rowDiff() == 2) {
            Square intermediateSquare = new Square(
                    (lastMove.to.row + lastMove.from.row) / 2,
                    lastMove.from.col);
            board.append(" ").append(intermediateSquare);
        } else {
            board.append(" -");
        }
        board.append(" ").append(halfmoveClock);
        board.append(" ").append(fullmoveNumber);
        return board.toString();
    }

    public void initialise(String fen) {
        String[] fenComponents = fen.split(" ");
        String[] boardRows = fenComponents[0].split("/");
        int charInRow;
        ArrayList<Piece> whitePieces = new ArrayList<>();
        ArrayList<Piece> blackPieces = new ArrayList<>();
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
                                newKing = new King(Side.WHITE, row, col);
                                whitePieces.add(newKing);
                                setWhiteKing(newKing);
                            } else {
                                newKing = new King(Side.BLACK, row, col);
                                blackPieces.add(newKing);
                                setBlackKing(newKing);
                            }
                            break;
                        case "q":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Queen(
                                        Side.WHITE, row, col, true
                                ));
                            } else {
                                blackPieces.add(new Queen(
                                        Side.BLACK, row, col, true
                                ));
                            }
                            break;
                        case "r":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Rook(Side.WHITE, row, col));
                            } else {
                                blackPieces.add(new Rook(Side.BLACK, row, col));
                            }
                            break;
                        case "b":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Bishop(
                                        Side.WHITE, row, col, true
                                ));
                            } else {
                                blackPieces.add(new Bishop(
                                        Side.BLACK, row, col, true
                                ));
                            }
                            break;
                        case "n":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Knight(
                                        Side.WHITE, row, col, true
                                ));
                            } else {
                                blackPieces.add(new Knight(
                                        Side.BLACK, row, col, true
                                ));
                            }
                            break;
                        case "p":
                            if (pieceSide == Side.WHITE) {
                                whitePieces.add(new Pawn(Side.WHITE, row, col));
                            } else {
                                blackPieces.add(new Pawn(Side.BLACK, row, col));
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
            // is en passant possible
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
            return moveHistory.get(0);
        } else {
            return null;
        }
    }

    public boolean gameFinished() {
        return checkmated || stalemated;
    }

    private void highlightPossibleMoves() {
        for (Move move : possibleMoves) {
            Point location = move.to.getLocation(false);
            batch.draw(PINK_SQUARE, location.x, location.y);
        }
    }

    public Side getNextTurn() {
        return nextTurn;
    }

    public void updateNextTurn() {
        nextTurn = nextTurn.opponent();
    }

    public long numPossiblePaths(int depth) {
        long total = 0;
        if (depth == 1) {
            for (Piece piece : getPieces(nextTurn)) {
                if (piece.hasBeenTaken()) continue;
                total += piece.getValidMoves().size();
            }
        } else {
            for (Piece piece : getPieces(nextTurn)) {
                if (piece.hasBeenTaken()) continue;
                for (Move move : piece.getValidMoves()) {
                    move.make();
                    total += numPossiblePaths(depth - 1);
                    move.undo();
                }
            }
        }
        return total;
    }

    public void flip() {
        flipBoard = !flipBoard;
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

    public static void dispose() {
        IMAGE.dispose();
        RED_SQUARE.dispose();
        PREVMOVE_SQUARE.dispose();
        BLUE_SQUARE.dispose();
        PINK_SQUARE.dispose();
        GREEN_SQUARE.dispose();
        Pawn.dispose();
        Knight.dispose();
        Bishop.dispose();
        Rook.dispose();
        Queen.dispose();
        King.dispose();
        batch.dispose();
        font.dispose();
    }

    public boolean pieceArraysMatchBoard(Move move) {
        Piece piece;
        int whiteCount = 0;
        int blackCount = 0;
        try {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    piece = contentsAt(row, col);
                    if (piece != null) {
                        assert piece.square().col == col :
                                "Wrong col internally " + piece +
                                        " located on board at " +
                                        new Square(row, col);
                        assert piece.square().row == row :
                                "Wrong row internally " + piece +
                                        " located on board at " +
                                        new Square(row, col);
                        if (piece.side == Side.WHITE) {
                            whiteCount++;
                        } else {
                            blackCount++;
                        }
                    }
                }
            }
            assert numWhite == whiteCount : "not all white pieces present";
            assert numBlack == blackCount : "not all black pieces present";
            for (Piece whitePiece : getPieces(Side.WHITE)) {
                assert whitePiece.hasBeenTaken() ||
                        contentsAt(whitePiece.square()) == whitePiece :
                        "Board square mismatched at " +
                                whitePiece.square() + "; " +
                                whitePiece + " vs board contents " +
                                contentsAt(whitePiece.square());
            }
            for (Piece blackPiece : getPieces(Side.BLACK)) {
                assert blackPiece.hasBeenTaken() ||
                        contentsAt(blackPiece.square()) == blackPiece :
                        "Board square mismatched at " +
                                blackPiece.square() + "; " +
                                blackPiece + " vs board contents " +
                                contentsAt(blackPiece.square());
            }
        } catch (AssertionError e) {
            System.out.println("Piece arrays don't match board. Move: " + move);
            printSnapShot();
            System.out.println("Board white count: " + whiteCount);
            System.out.println("Board black count: " + blackCount);
            System.out.println("White pieces: ");
            for (Piece whitePiece : getPieces(Side.WHITE)) {
                System.out.println(whitePiece);
            }
            System.out.println("Black pieces: ");
            for (Piece blackPiece : getPieces(Side.BLACK)) {
                System.out.println(blackPiece);
            }
            System.out.println("Array white count: " + numWhite);
            System.out.println("Array black count: " + numBlack);
            System.out.println("Move history: (most recent first)");
            for (Move previousMove : moveHistory) {
                System.out.println(previousMove);
            }
            e.printStackTrace();
            System.exit(1);
        }
        return true;
    }

    public void printSnapShot() {
        System.out.println("Board: " + this);
        Piece piece;
        String pieceStr;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                piece = contentsAt(row, col);
                if (piece == null) {
                    System.out.print("|   ");
                    continue;
                } else if (piece instanceof Knight) {
                    pieceStr = "N";
                } else {
                    pieceStr = piece.getClass().getSimpleName().substring(0, 1);
                }
                if (piece.side == Side.BLACK) {
                    pieceStr = pieceStr.toLowerCase();
                }
                System.out.format("| %s ", pieceStr);
                assert (piece.square().col == col) :
                        piece + " should be at " + new Square(row, col);
                assert (piece.square().row == row) :
                        piece + " should be at " + new Square(row, col);
            }
            System.out.print("|\n");
        }
        System.out.format("%s's material advantage: %d\n",
                nextTurn,
                nextTurnAdvantage());
        //System.out.format("Evaluation: %d\n", evaluate());
        System.out.println(toFen());
        System.out.println();
    }

    public int nextTurnAdvantage() {
        return material(nextTurn) - material(nextTurn.opponent());
    }

    public int material(Side side) {
        int material = 0;
        for (Piece myPiece : getPieces(side)) {
             if (!myPiece.hasBeenTaken()) {
                material += myPiece.value;
            }
        }
        return material;
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
        int evaluation = 1000 * material(side);
        boolean[] pawnCols = new boolean[SIZE];
        for (Piece piece : getPieces(side)) {
            if (piece.hasBeenTaken()) {
                continue;
            }
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
                if (!piece.isUnmoved()) {
                    evaluation += 3;
                }
            }
        }
        // if only your king is left, keep away from edges and opponent's king
        if (numPieces(side) == 1) {
            evaluation += 3 * kingDistanceFromEdge(side);
            evaluation += distanceBetweenKings();
        }
        return evaluation;
    }

    private int kingDistanceFromEdge(Side side) {
        Square kingSquare = getKing(side).square();
        int verticalEdgeDistance =
                Math.min(kingSquare.row, 7 - kingSquare.row);
        int horizontalEdgeDistance =
                Math.min(kingSquare.col, 7 - kingSquare.col);
        return Math.min(verticalEdgeDistance, horizontalEdgeDistance);
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


    private long[][][] initialiseZobristTable() {
        long[][][] table = new long[SIZE][SIZE][NUM_PIECE_TYPES * 2];
        Random random = new Random(0);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                for (int k = 0; k < NUM_PIECE_TYPES * 2; k++) {
                    table[i][j][k] = random.nextLong();
                }
            }
        }
        return table;
    }

    public void updateZobrist(Square square, Piece piece) {
        int pieceIndex = piece.pieceIndex +
                (piece.side == Side.WHITE ? NUM_PIECE_TYPES : 0);
        zobrist ^= zobristTable[square.col][square.row][pieceIndex];
    }

    public Long getZobrist() {
        return zobrist;
    }
}

