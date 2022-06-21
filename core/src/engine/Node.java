package engine;

import java.util.HashMap;
import java.util.PriorityQueue;

// A node in the minimax tree representing a state of the board
public class Node {
    private static int leafNodes = 0;

    public final boolean isRoot;
    public final Board board;
    public final Move precedingMove;
    private Move bestMove;
    private static HashMap<Integer, TranspositionEntry> transpositionTable;

    public static int numCaps = 0;

    public Node(Board board) {
        this.isRoot = true;
        this.board = board;
        this.precedingMove = null;
        transpositionTable = new HashMap<>();
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

        TranspositionEntry entry = transpositionTable.get(board.get_zobrist());
        if (entry != null && entry.depth >= depth) {
            if (entry.flag == 'E') {
                // Exact match found
                return entry.value;
            } else if (entry.flag == 'L') {
                // Lower-bound found: increase α if possible
                if (entry.value > alpha) {
                    alpha = entry.value;
                }
            } else if (entry.flag == 'U') {
                // Upper-bound found: decrease β if possible
                if (entry.value < beta) {
                    beta = entry.value;
                }
            }
            // prune
            if (alpha >= beta) {
                return entry.value;
            }
        }
        PriorityQueue<Move> pq;
        if (depth <= 0) {
            if (precedingMove == null || !precedingMove.isInteresting()
                    || board.noValidMoveExists()) {
                leafNodes++;
                int evaluation = board.evaluate();
                updatePrediction(null, evaluation, depth);
                return evaluation;
            }
            pq = new PriorityQueue<>(board.getInterestingMoves());
        } else {
            pq = new PriorityQueue<>(board.getAllValidMoves());
        }
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
                updatePrediction(subsequentMove, nodeValue, depth);
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
        transpositionTable.put(board.get_zobrist(), entry);

        return nodeValue;
    }

    private void updatePrediction(Move subsequentMove, int nodeValue, int depth) {
        if (isRoot) {
            bestMove = subsequentMove;
            bestMove.setScore(nodeValue);
            bestMove.setScoreDepth(depth);
        }
        if (precedingMove != null){
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

    public static int getLeafNodes() {
        return leafNodes;
    }

    public static void resetStartNodeEvaluationCount() {
        leafNodes = 0;
    }

}
