import java.util.Comparator;

// Scorer class for performing minimax - copies state to a clean board
public class Scorer implements Runnable {
    private static final long TIME_CAP_MILLIS = 5000;
    private static final int MIN_DEPTH = 2;
    private static final int BONUS_DEPTH = 6;
    private static int totalComputeMoveDepth = 0;
    private static int numComputedMoves = 0;

    private final Node originalNode;
    private final Node scoringNode;
    private final long startTime;

    public Scorer(Node node) {
        this.originalNode = node;
        this.scoringNode = new Node(new Board(node.board));
        startTime = System.currentTimeMillis();
    }

    public Move getBestMove() {
        assert scoringNode.isRoot;
        performLookahead();
        if (originalNode.getBestMove() == null) {
            System.out.println("No successor was found ; using heuristic");
            Move bestMove = originalNode.board.getAllValidMoves().stream()
                    .max(Comparator.comparingInt(Move::heuristicScore))
                    .get();
            bestMove.setScore(bestMove.heuristicScore());
            originalNode.setBestMove(bestMove);
        }
        updateStats();
        return originalNode.getBestMove();
    }

    private void performLookahead() {
        Thread thread = new Thread(this);
        thread.start();
        // before time limit is exceeded or max depth is reached
        try {
            Thread.sleep(TIME_CAP_MILLIS);
            thread.interrupt();
            // sleep in case we interrupted while best move is being recorded
            Thread.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            System.out.format("Depth reached: %d\n",
                    originalNode.getBestMove().getScoreDepth()
            );
            System.out.format("Best move: %s\n", originalNode.getBestMove());
        } catch (NullPointerException e) {
            System.out.println("Depth reached: 0");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void updateStats() {
        long endTime = System.currentTimeMillis();
        double timeToScoreLastMoveSecs = (endTime - startTime) / 1000.0;
        originalNode.getBestMove().setTimeToScoreSecs(timeToScoreLastMoveSecs);
        totalComputeMoveDepth += originalNode.getBestMove().getScoreDepth();
        numComputedMoves ++;
        System.out.format("Scored move %d: %.3fs\n",
                numComputedMoves,
                timeToScoreLastMoveSecs);
        if (originalNode.board.moveHistory.size() % 10 == 9) {
            System.out.format("Average move compute depth: %.2f\n",
                    totalComputeMoveDepth / (double) numComputedMoves);
        }
    }

    @Override
    public void run() {
        Move bestMove;
        long moveStartTime;
        int depth = MIN_DEPTH, negamax;
        while (!Thread.currentThread().isInterrupted()) {
            System.out.format("Depth: %d\n", depth);

            moveStartTime = System.currentTimeMillis();
            Node.resetStartNodeEvaluationCount();
            negamax = scoringNode.negamax(
                    depth,
                    -Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    BONUS_DEPTH
            );
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("NUM CAPS:" + Node.numCaps);
                return;
            }
            bestMove = scoringNode.getBestMove().makeCopy(originalNode.board);
            assert negamax == bestMove.getScore();
            bestMove.setLeafNodesSearchedToScore(Node.getLeafNodes());
            originalNode.setBestMove(bestMove);

            System.out.format("\tMove time: %.3f, Total time: %.3f\n",
                    (System.currentTimeMillis() - moveStartTime)/1000.0,
                    (System.currentTimeMillis() - startTime)/1000.0);
            System.out.format("\tLeaf-nodes searched: %d\n",
                    bestMove.getLeafNodesSearchedToScore());
            depth++;
        }
    }

}
