package engine;

public class TranspositionEntry {
    public final double value;
    public final int depth;
    public final String flag;

    public TranspositionEntry(double value, int depth, String flag) {
        this.value = value;
        this.depth = depth;
        this.flag = flag;
    }
}
