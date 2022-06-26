package engine;

import java.util.Random;

public class ZobristTracker {
    private static final int NUM_PIECE_TYPES = 6;
    private final Board board;
    private final long[][][] zTable;
    private final long[] enPassantKeys;
    private final long BQCastle;
    private final long BKCastle;
    private final long WQCastle;
    private final long WKCastle;
    private final long turnKey;
    private long incrementalVal;

    public ZobristTracker(Board board) {
        this.board = board;
        Random random = new Random(0);
        zTable = new long[Board.SIZE][Board.SIZE][NUM_PIECE_TYPES*2];
        populateZTable(random);
        enPassantKeys = new long[Board.SIZE];
        populateEnPassantKeys(random);
        BQCastle = random.nextLong();
        BKCastle = random.nextLong();
        WQCastle = random.nextLong();
        WKCastle = random.nextLong();
        turnKey = random.nextLong();
        incrementalVal = 0;
    }

    private void populateZTable(Random random) {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                for (int k = 0; k < NUM_PIECE_TYPES * 2; k++) {
                    zTable[i][j][k] = random.nextLong();
                }
            }
        }
    }

    private void populateEnPassantKeys(Random random) {
        for (int i = 0; i < Board.SIZE; i++) {
            enPassantKeys[i] = random.nextLong();
        }
    }

    public void update(Square square, Piece piece) {
        int pieceIndex = piece.pieceIndex +
                (piece.side == Side.WHITE ? NUM_PIECE_TYPES : 0);
        incrementalVal ^= zTable[square.col][square.row][pieceIndex];
        // System.out.format("zTable[%d][%d][%d] = %d\n", square.col, square.row, pieceIndex, zTable[square.col][square.row][pieceIndex]);
    }

    public void updateTurn() {
        incrementalVal ^= turnKey;
    }

    public long getVal() {
        return incrementalVal ^ enPassantPossibleHash() ^ castlingRightsHash();
    }

    private long enPassantPossibleHash() {
        Move lastMove = board.getLastMove();
        if (lastMove != null && lastMove.piece instanceof Pawn &&
                lastMove.rowDiff() == 2) {
            return enPassantKeys[lastMove.from.col];
        } else {
            return 0;
        }
    }

    private long castlingRightsHash() {
        long val = 0;
        if (board.getKing(Side.WHITE).isUnmoved()) {
            // White King-side
            if (board.contentsAt(7, 7) != null &&
                    board.contentsAt(7, 7).isUnmoved()) {
                val ^= WKCastle;
            }
            // White Queen-side
            if (board.contentsAt(7, 0) != null &&
                    board.contentsAt(7, 0).isUnmoved()) {
                val ^= WQCastle;
            }
        }
        if (board.getKing(Side.BLACK).isUnmoved()) {
            // Black King-side
            if (board.contentsAt(0, 7) != null &&
                    board.contentsAt(0, 7).isUnmoved()) {
                val  ^= BKCastle;
            }
            // Black Queen-side
            if (board.contentsAt(0, 0) != null &&
                    board.contentsAt(0, 0).isUnmoved()) {
                val ^= BQCastle;
            }
        }
        return val;
    }
}
