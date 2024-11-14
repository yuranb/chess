package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class ChessBoardUI {
    private final ChessGame game;

    public ChessBoardUI() {
        this.game = new ChessGame();
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        game.setBoard(board);
    }

    public void display() {
        System.out.print(ERASE_SCREEN);

        System.out.println(SET_TEXT_BOLD + "White's Perspective:");
        printBoard(false);

        System.out.println("\n");

        System.out.println("Black's Perspective:");
        printBoard(true);

        System.out.print(RESET_TEXT_BOLD_FAINT);
    }

    private void printBoard(boolean b) {

    }

}