/* Used for En Passant and Castling, where multiple pieces are 'moved' around
   but these mini-moves shouldn't be considered a move in the full sense */

public class PartialMove extends Move{

    public PartialMove(Piece piece, Square to) {
        super(piece, to);
    }

    // Copy constructor for new board
    public PartialMove(PartialMove partialMove, Board newBoard) {
        super(partialMove, newBoard);
    }

    @Override
    public void register() {}

    @Override
    public void deregister() {}
}

