public class EnPassant extends Move {

    // Consider En Passant as two moves; move taken pawn back one, then take it
    public final PartialMove shiftOpponentBack;

    public EnPassant(Piece piece, Square to) {
        super(piece, to, piece.board.contentsAt(piece.square().row, to.col));
        shiftOpponentBack = new
                PartialMove(board.contentsAt(piece.square().row, to.col), to);
        assert board.contentsAt(piece.square().row, to.col) != null;
    }


    // Copy  constructor for new board
    public EnPassant(EnPassant enPassant, Board newBoard) {
        super(enPassant, newBoard);
        this.shiftOpponentBack = new PartialMove(
                enPassant.shiftOpponentBack, newBoard);
    }

    @Override
    public Move makeCopy(Board newBoard) {
        return new EnPassant(this, newBoard);
    }

    @Override
    public void make() {
        shiftOpponentBack.make();
        super.make();
    }

    @Override
    public void undo() {
        super.undo();
        shiftOpponentBack.undo();
    }
}
