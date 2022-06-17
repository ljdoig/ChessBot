package engine;

import java.util.PriorityQueue;

// A conceptual node in the minimax tree representing a state of the board
public class Node {
    private static int leafNodes = 0;

    public final boolean isRoot;
    public final Board board;
    public final Move precedingMove;
    private Move bestMove;

    public static int numCaps = 0;

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

    public int negamax(int depth, int alpha, int beta, int bonusDepth) {
        if (Thread.currentThread().isInterrupted()) {
            return Integer.MAX_VALUE;
        }
        // don't stop looking ahead if something interesting happens
        if (depth == 0 && precedingMove != null && precedingMove.isInteresting()) {
            if (bonusDepth-- > 0) {
                depth = 1;
            } else {
                numCaps++;
            }
        }
        if (depth == 0 || board.noValidMoveExists()) {
            leafNodes++;
            int evaluation = board.evaluate();
            updatePrediction(null, evaluation, depth);
            return evaluation;
        }
        PriorityQueue<Move> pq = new PriorityQueue<>(board.getAllValidMoves());
        int nodeValue;
        Node child;
        Move subsequentMove;
        while ((subsequentMove = pq.poll()) != null) {
            subsequentMove.make();
            child = new Node(subsequentMove);
            nodeValue = -child.negamax(
                    depth - 1, -beta, -alpha, bonusDepth
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
        return alpha;
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
