package engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Move implements Comparable<Move> {
    public final Board board;
    public final Piece piece;
    public final Square from;
    public final Square to;
    public final Piece taken;
    public final Side side;
    public final int halfmoveClockPrior;
    public final boolean pieceFirstMove;
    private Move successor;
    private Integer score;
    private Integer heuristic;
    private int scoreDepth;
    private int leafNodesSearchedToScore;
    private double timeToScoreSecs;

    public Move(Piece piece, Square to) {
        this.board = piece.board;
        this.piece = piece;
        this.from = piece.square();
        this.to = to;
        this.taken = board.contentsAt(to);
        this.side = piece.side;
        this.pieceFirstMove = piece.isUnmoved();
        this.halfmoveClockPrior = board.getHalfmoveClock();
    }

    // For En Passant, where 'taken' piece is not located at 'to'
    public Move(Piece piece, Square to, Piece taken) {
        this.board = piece.board;
        this.piece = piece;
        this.from = piece.square();
        this.to = to;
        this.taken = taken;
        this.side = piece.side;
        this.pieceFirstMove = piece.isUnmoved();
        this.halfmoveClockPrior = board.getHalfmoveClock();
    }

    public Move(Move move, Board newBoard) {
        this.board = newBoard;
        this.piece = newBoard.contentsAt(move.piece.square());
        this.from = move.from;
        this.to = move.to;
        if (move.taken != null) {
            this.taken = newBoard.contentsAt(move.taken.square());
        } else {
            this.taken = null;
        }
        this.side = move.side;
        this.pieceFirstMove = move.pieceFirstMove;
        this.halfmoveClockPrior = move.halfmoveClockPrior;
        this.successor = move.successor;
        this.score = move.score;
        this.leafNodesSearchedToScore = move.leafNodesSearchedToScore;
        this.timeToScoreSecs = move.timeToScoreSecs;
        this.scoreDepth = move.scoreDepth;
    }

    public Move makeCopy(Board newBoard) {
        return new Move(this, newBoard);
    }

    public void make() {
        assert piece.square() == from;
        if (taken != null) {
            taken.setTaken(true);
            board.decrementNumPieces(taken.side);
        }
        piece.setUnmoved(false);
        piece.setSquare(to);
        board.setContents(from, null);
        board.setContents(to, piece);
        register();
        assert board.pieceArraysMatchBoard(this);
    }

    public void undo() {
        assert piece.square() == to;
        if (taken != null) {
            taken.setTaken(false);
            board.incrementNumPieces(taken.side);
        }
        board.setContents(to, taken);
        board.setContents(from, piece);
        piece.setSquare(from);
        piece.setUnmoved(pieceFirstMove);
        deregister();
        assert board.pieceArraysMatchBoard(this);
    }

    public void register() {
        updateZobrist();
        if (side == Side.BLACK) {
            board.setFullmoveNumber(board.getFullmoveNumber() + 1);
        }
        if (piece instanceof Pawn || taken != null) {
            board.setHalfmoveClock(0);
        } else {
            board.setHalfmoveClock(board.getHalfmoveClock() + 1);
        }
        assert side == board.getNextTurn();
        assert board.getLastMove() == null || board.getLastMove().side == side.opponent();
        board.moveHistory.add(0, this);
        board.updateNextTurn();
    }

    public void deregister() {
        updateZobrist();
        if (side == Side.BLACK) {
            board.setFullmoveNumber(board.getFullmoveNumber() - 1);
        }
        board.setHalfmoveClock(halfmoveClockPrior);
        board.updateNextTurn();
        assert board.moveHistory.get(0) == this : this;
        board.moveHistory.remove(0);
    }

    protected void updateZobrist() {
        board.updateZobrist(from, piece);
        board.updateZobrist(to, piece);
        if (taken != null) {
            board.updateZobrist(to, taken);
        }
    }

    public int rowDiff() {
        return from.rowDiff(to);
    }

    public int rowProgress() {
        if (side == Side.WHITE) {
            return from.row - to.row;
        } else {
            return to.row - from.row;
        }
    }

    public Move getSuccessor() {
        return successor;
    }

    public void setSuccessor(Move successor) {
        this.successor = successor;
    }

    public void printAnticipatedSequence() {
        for (String string : getAnticipatedSequence()) {
            System.out.println(string);
        }
        System.out.println();
    }

    public ArrayList<String> getAnticipatedSequence() {
        if (successor == null) {
            return new ArrayList<>();
        }
        return new ArrayList<String>() {{
            add("Anticipated sequence:");
            Move nextSuccessor = successor;
            while (nextSuccessor != null) {
                add(nextSuccessor.toString());
                nextSuccessor = nextSuccessor.getSuccessor();
            }
        }};
    }

    @Override
    public String toString() {
        return piece + ": " + from + " -> " + to
                + scoreString()
                + (taken != null ? " t: " + taken.getClass().getSimpleName() : "");
    }

    protected String scoreString() {
        if (score != null) {
            String scoreString;
            // arbitrary boundary, games usually shorter than 1000 moves
            if (score > Integer.MAX_VALUE*0.9) {
                scoreString = "win";
            } else if (score < -Integer.MAX_VALUE*0.9) {
                scoreString = "loss";
            } else {
                scoreString = score.toString();
            }
            String openBracket = (score >= 0) ? " ( " : " (";
            return openBracket + String.format("%s", scoreString) + ")";
        } else {
            return "";
        }
    }

    public String longToString() {
        return "Move{" +
                "board=" + board +
                ", piece=" + piece +
                ", from=" + from +
                ", to=" + to +
                ", taken=" + taken +
                ", side=" + side +
                ", halfmoveClockPrior=" + halfmoveClockPrior +
                ", pieceFirstMove=" + pieceFirstMove +
                ", successor=" + successor +
                ", score=" + score +
                ", heuristic=" + heuristic +
                ", scoreDepth=" + scoreDepth +
                ", leafNodesSearchedToScore=" + leafNodesSearchedToScore +
                ", timeToScoreSecs=" + timeToScoreSecs +
                '}';
    }

    // For humans
    public static Move getClickedMove(Piece piece, Square to) {
        ArrayList<Move> possibleMoves = piece.getValidMoves();
        if (piece.promotionPossible(to)) {
            Predicate<Promotion> correctPromotion =
                    promotion -> promotion.to.equals(to);
            if (Gdx.input.isKeyPressed(Input.Keys.R)) {
                correctPromotion = correctPromotion.and(
                        promotion -> promotion.promoteTo instanceof Rook);
            } else if (Gdx.input.isKeyPressed(Input.Keys.B)) {
                correctPromotion = correctPromotion.and(
                        promotion -> promotion.promoteTo instanceof Bishop);
            } else if (Gdx.input.isKeyPressed(Input.Keys.K)) {
                correctPromotion = correctPromotion.and(
                        promotion -> promotion.promoteTo instanceof Knight);
            } else {
                correctPromotion = correctPromotion.and(
                        promotion -> promotion.promoteTo instanceof Queen);
            }
            return ((Pawn) piece).getValidPromotions(to).stream()
                    .filter(correctPromotion)
                    .findAny()
                    .orElse(null);
        } else {
            // find any move that moves the focused on piece to the correct square
            return possibleMoves.stream()
                    .filter(move -> move.to.equals(to))
                    .findAny()
                    .orElse(null);
        }
    }

    public int getScoreDepth() {
        return scoreDepth;
    }

    public void setScoreDepth(int scoreDepth) {
        this.scoreDepth = scoreDepth;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLeafNodesSearchedToScore() {
        return leafNodesSearchedToScore;
    }

    public void setLeafNodesSearchedToScore(int leafNodesSearchedToScore) {
        this.leafNodesSearchedToScore = leafNodesSearchedToScore;
    }

    public double getTimeToScoreSecs() {
        return timeToScoreSecs;
    }

    public void setTimeToScoreSecs(double timeToScoreSecs) {
        this.timeToScoreSecs = timeToScoreSecs;
    }

    public ArrayList<String> analysis() {
        ArrayList<String> analysis = new ArrayList<>();
        make();
        Node moveNode = new Node(board);
        successor = new Scorer(moveNode).getBestMove();
        if (successor != null) {
            score = -successor.score;
            analysis.add(String.format("Test move: (%d leaf-nodes, %.2fs, depth: %d)",
                    successor.leafNodesSearchedToScore,
                    successor.timeToScoreSecs,
                    successor.scoreDepth));
        } else {
            score = -board.evaluate();
            analysis.add("Test move: (leaf-node)");
        }
        undo();
        analysis.add(this.toString());
        analysis.addAll(getAnticipatedSequence());
        score = null;
        return analysis;
    }

    public boolean isInteresting() {
        return taken != null || this instanceof Promotion;
    }

    @Override
    public int compareTo(Move o) {
        int heuristicComparison = heuristicScore() - o.heuristicScore();
        // sort highest to lowest heuristic
        return -heuristicComparison;
    }

    public int heuristicScore() {
        // ensure heuristic only calculated once
        if (heuristic == null) {
            calculateHeuristic();
        }
        return heuristic;
    }

    private void calculateHeuristic() {
        heuristic = 0;
        if (taken != null) {
            heuristic += taken.value * 10 - piece.value;
        }
        if (this instanceof Promotion) {
            heuristic += (((Promotion) this).promoteTo.value - 1);
        }
        if (pieceFirstMove && !(piece instanceof King)) {
            heuristic += rowProgress();
        }
        if (piece instanceof Pawn) {
            heuristic += rowProgress();
        } else if (board.pawnAttackedSquares().contains(to)) {
            heuristic -= piece.value;
        }
    }

}