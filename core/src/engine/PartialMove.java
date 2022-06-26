package engine;
/* Used for En Passant and Castling, where multiple pieces are 'moved' around
   but these mini-moves shouldn't be recorded as a move in the full sense */

public class PartialMove extends Move{

    public PartialMove(Piece piece, Square to) {
        super(piece, to);
    }

    // Copy constructor for new board
    public PartialMove(PartialMove partialMove, Board newBoard) {
        super(partialMove, newBoard);
    }

    @Override
    public void register() {
        board.zobristTracker.update(from, piece);
        board.zobristTracker.update(to, piece);
    }

    @Override
    public void deregister() {
        board.zobristTracker.update(from, piece);
        board.zobristTracker.update(to, piece);
    }
}

