package engine;

public class TranspositionEntry {
    public final int value;
    public final int depth;
    public final char flag;
    public final String fen;

    public TranspositionEntry(int value, int depth, char flag) {
        this.value = value;
        this.depth = depth;
        this.flag = flag;
        this.fen = null;
    }

}
