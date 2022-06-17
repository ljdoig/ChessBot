package engine;

public class Castle extends Move {

    public final PartialMove rookMove;

    public Castle(Piece main, Square mainTo) {
        super(main, mainTo);
        int rookFromCol = (mainTo.col == 2) ? 0 : 7;
        int rookToCol   = (mainTo.col == 2) ? 3 : 5;
        Piece rook = board.contentsAt(mainTo.row, rookFromCol);
        if (rook != null) {
            rookMove = new PartialMove(rook, new Square(mainTo.row, rookToCol));
        } else {
            rookMove = null;
        }
    }

    // Copy  constructor for new board
    public Castle(Castle castle, Board newBoard) {
        super(castle, newBoard);
        this.rookMove = new PartialMove(castle.rookMove, newBoard);
    }

    @Override
    public Move makeCopy(Board newBoard) {
        return new Castle(this, newBoard);
    }

    @Override
    public void make() {
        ((King) piece).setHasCastled(true);
        rookMove.make();
        assert board.pieceArraysMatchBoard(this);
        super.make();
        assert board.pieceArraysMatchBoard(this);
    }

    @Override
    public void undo() {
        ((King) piece).setHasCastled(false);
        rookMove.undo();
        assert board.pieceArraysMatchBoard(this);
        super.undo();
        assert board.pieceArraysMatchBoard(this);
    }

    @Override
    public String toString() {
        return piece + ": " + "Cstl> " + to
                + super.scoreString()
                + (taken != null ? " t: " + taken.getClass().getSimpleName() : "");
    }
}