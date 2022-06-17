package engine;


import com.chessbot.ChessGame;

public class Square {
    public final byte row;
    public final byte col;

    public Square(int row, int col) {
        assert 0 <= row && row <= 7;
        assert 0 <= col && col <= 7;
        this.row = (byte) row;
        this.col = (byte) col;
    }

    public Point getLocation(boolean centred) {
        Square renderAt;
        if (Board.flipBoard) {
            renderAt = new Square(row, 7 - col);
        } else {
            renderAt = new Square(7 - row, col);
        }
        float squareWidth = (float) (ChessGame.SIZE / 8.0);
        float x = squareWidth * renderAt.col;
        float y = squareWidth * renderAt.row;
        if (centred) {
            x += squareWidth / 2;
            y += squareWidth / 2;
        }
        return new Point(x, y);
    }

    public int colDiff(Square other) {
        return Math.abs(col - other.col);
    }

    public int rowDiff(Square other) {
        return Math.abs(row - other.row);
    }

    @Override
    public String toString() {
        char colChar = (char) (col + 97);
        return String.format("%c%d", colChar, 8 - row);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Square square = (Square) o;
        return row == square.row && col == square.col;
    }
}
