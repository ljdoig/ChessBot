package engine;

public class EvaluationTracker {
    private int leafNodes;
    private int evaluations;
    private final int depth;
    private int transpositions;
    private double timeTaken;

    public EvaluationTracker(int depth) {
        this.depth = depth;
    }

    public int getTranspositions() {
        return transpositions;
    }

    public void setTranspositions(int transpositions) {
        this.transpositions = transpositions;
    }

    public int getLeafNodes() {
        return leafNodes;
    }

    public void incrementLeafNodes() {
        leafNodes++;
    }

    public int getEvaluations() {
        return evaluations;
    }

    public void incrementEvaluations() {
        evaluations++;
    }

    public int getDepth() {
        return depth;
    }

    public double getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(double timeTaken) {
        this.timeTaken = timeTaken;
    }
}
