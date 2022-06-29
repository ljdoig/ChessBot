package engine;

public class Promotion extends Move {

    public final Piece promoteTo;

    public Promotion(Piece piece, Square to, Piece promotedTo) {
        super(piece, to);
        this.promoteTo = promotedTo;
    }

    // Copy  constructor for new board
    public Promotion(Promotion promotion, Board newBoard) {
        super(promotion, newBoard);
        this.promoteTo = promotion.promoteTo.makeCopy(newBoard);
    }

    @Override
    public Move makeCopy(Board newBoard) {
        return new Promotion(this, newBoard);
    }

    @Override
    public void make() {
        super.make();
        board.setContents(piece.square(), promoteTo);
        board.getPieces(side)[piece.arrayIndex] = promoteTo;
        board.zobristTracker.update(to, piece);
        board.zobristTracker.update(to, promoteTo);
    }

    @Override
    public void undo() {
        board.getPieces(side)[piece.arrayIndex] = piece;
        super.undo();
        board.zobristTracker.update(to, piece);
        board.zobristTracker.update(to, promoteTo);
        assert board.getLastMove() == null || board.getLastMove().side == side.opponent();
    }

    @Override
    public String toString() {
        return super.toString() + " p: " + promoteTo.getClass().getSimpleName();
    }
}
