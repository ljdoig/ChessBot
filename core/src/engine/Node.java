package engine;

import java.util.HashMap;
import java.util.PriorityQueue;

// A node in the minimax tree representing a state of the board
public class Node {
    private static final HashMap<Long, TranspositionEntry> transpositionTable =
            new HashMap<>();
    private static EvaluationTracker evaluationTracker = new EvaluationTracker(0);
    private static boolean training;
    public final boolean isRoot;
    public final Board board;
    public final Move precedingMove;
    private Move bestMove;

    public Node(Board board) {
        this.isRoot = true;
        this.board = board;
        this.precedingMove = null;
    }

    public Node(Move precedingMove) {
        this.isRoot = false;
        this.board = precedingMove.board;
        this.precedingMove = precedingMove;
    }

    public int negamax(int depth, int alpha, int beta) {
        if (Thread.currentThread().isInterrupted()) {
            return Integer.MAX_VALUE;
        }

        int alphaOrig = alpha;
        TranspositionEntry entry = transpositionTable.get(board.zobristTracker.getVal());
        if (entry != null && entry.depth >= depth) {
            evaluationTracker.incrementTranspositions();
            if (entry.flag == 'E') {
                // Exact match found
                return entry.value;
            } else if (entry.flag == 'L') {
                // Lower-bound found: increase α if possible
                alpha = Math.max(entry.value, alpha);
            } else if (entry.flag == 'U') {
                // Upper-bound found: decrease β if possible
                beta = Math.min(entry.value, beta);
            }
            // prune
            if (alpha >= beta) {
                return entry.value;
            }
        }

        if (depth == 0 || board.noValidMoveExists()) {
            evaluationTracker.incrementLeafNodes();
            int evaluation = quiesce(alpha, beta);
            updatePrediction(null, evaluation);
            return evaluation;
        }
        PriorityQueue<Move> pq = new PriorityQueue<>(board.getAllValidMoves());
        int nodeValue = -Integer.MAX_VALUE;
        Node child;
        Move subsequentMove;
        while ((subsequentMove = pq.poll()) != null) {
            subsequentMove.make();
            child = new Node(subsequentMove);
            nodeValue = Math.max(
                    nodeValue,
                    -child.negamax(depth - 1, -beta, -alpha)
            );
            subsequentMove.undo();
            if (nodeValue > alpha) {
                alpha = nodeValue;
                updatePrediction(subsequentMove, nodeValue);
            }
            if (alpha >= beta) {
                break;
            }
        }

        if (nodeValue <= alphaOrig) {
            entry = new TranspositionEntry(nodeValue, depth, 'U');
        } else if (nodeValue >= beta) {
            entry = new TranspositionEntry(nodeValue, depth, 'L');
        } else {
            entry = new TranspositionEntry(nodeValue, depth, 'E');
        }
        transpositionTable.put(board.zobristTracker.getVal(), entry);

        return nodeValue;
    }

    private int quiesce(int alpha, int beta) {
        evaluationTracker.incrementEvaluations();
        int baseline = board.evaluate();
        if (baseline >= beta)
            return beta;
        if (alpha < baseline)
            alpha = baseline;
        int score;
        PriorityQueue<Move> pq = new PriorityQueue<>(board.getInterestingMoves());
        Move interestingMove;
        while ((interestingMove = pq.poll()) != null) {
            interestingMove.make();
            score = -quiesce(-beta, -alpha);
            interestingMove.undo();
            if (score >= beta)
                return beta;
            if (score > alpha)
                alpha = score;
        }
        return alpha;
    }

    private void updatePrediction(Move subsequentMove, int nodeValue) {
        if (isRoot) {
            bestMove = subsequentMove;
            bestMove.setScore(nodeValue);
        }
        if (training && precedingMove != null) {
            precedingMove.setSuccessor(subsequentMove);
            precedingMove.setScore(-nodeValue);
        }
    }

    public Move getBestMove() {
        return bestMove;
    }

    public void setBestMove(Move bestMove) {
        this.bestMove = bestMove;
    }

    public static void resetEvaluationTracker(int depth) {
        evaluationTracker = new EvaluationTracker(depth);
        transpositionTable.clear();
    }

    public static EvaluationTracker getEvaluationTracker() {
        return evaluationTracker;
    }

    public static void setTraining(boolean training) {
        Node.training = training;
    }
}
