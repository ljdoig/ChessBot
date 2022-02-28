public enum Side {
    BLACK,
    WHITE;

    public Side opponent() {
        if (this == BLACK) {
            return WHITE;
        } else {
            return BLACK;
        }
    }
}
