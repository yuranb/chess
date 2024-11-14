package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class ChessBoardUI {
    private final ChessGame game;
    private final ChessBoard board;

    public ChessBoardUI() {
        this.game = new ChessGame();
        this.board = new ChessBoard();
        board.resetBoard();
        game.setBoard(board);
    }

    public void display() {
        System.out.print(ERASE_SCREEN);

        // Show white's view
        System.out.println(SET_TEXT_BOLD + "White's Perspective:");
        showBoardView(false);

        System.out.println("\n");

        // Show black's view
        System.out.println(SET_TEXT_BOLD + "Black's Perspective:");
        showBoardView(true);

        System.out.print(RESET_TEXT_BOLD_FAINT);
    }

    private void showBoardView(boolean isBlackView) {
        drawColumnLabels(isBlackView);

        for (int row = 8; row >= 1; row--) {
            int actualRow = row;
            if (isBlackView) {
                actualRow = 9 - row;
            }
            drawBoardRow(actualRow, isBlackView);
        }

        drawColumnLabels(isBlackView);
    }

    // Draw column labels (a-h or h-a)
    private void drawColumnLabels(boolean isBlackView) {
        System.out.print("   "); // fill the left
        String[] columns;
        if (isBlackView) {
            columns = new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};
        } else {
            columns = new String[]{"a", "b", "c", "d", "e", "f", "g", "h"};
        }

        for (String col : columns) {
            System.out.print(SET_TEXT_COLOR_LIGHT_GREY + " " + col + " " + RESET_TEXT_COLOR);
        }
        System.out.println();
    }

    // Draw a single row of the board
    private void drawBoardRow(int row, boolean isBlackView) {
        // Draw left row number
        System.out.print(SET_TEXT_COLOR_LIGHT_GREY + String.format(" %d ", row) + RESET_TEXT_COLOR);

        // Draw each square in the row
        for (int col = 1; col <= 8; col++) {
            int actualCol = col;
            if (isBlackView) {
                actualCol = 9 - col;
            }
            ChessPosition position = new ChessPosition(row, actualCol);

            // Set square background color
            String squareColor;
            if ((row + actualCol) % 2 != 0) {
                squareColor = SET_BG_COLOR_WHITE;
            } else {
                squareColor = SET_BG_COLOR_DARK_GREY;
            }

            // Draw piece
            String pieceStr = getPieceSymbol(position);
            System.out.print(squareColor + pieceStr + RESET_BG_COLOR);
        }

        System.out.print(SET_TEXT_COLOR_LIGHT_GREY + String.format(" %d ", row) + RESET_TEXT_COLOR);
        System.out.println();
    }

    private String getPieceSymbol(ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return "   "; // empty square
        }

        // Get piece letter symbol
        String pieceChar= "   ";
        switch (piece.getPieceType()) {
            case KING:
                pieceChar = " K ";
                break;
            case QUEEN:
                pieceChar = " Q ";
                break;
            case ROOK:
                pieceChar = " R ";
                break;
            case BISHOP:
                pieceChar = " B ";
                break;
            case KNIGHT:
                pieceChar = " N ";
                break;
            case PAWN:
                pieceChar = " P ";
                break;
        }

        // Set piece color
        String pieceColor;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            pieceColor = SET_TEXT_COLOR_RED; // White piece
        } else {
            pieceColor = SET_TEXT_COLOR_BLUE; // Black piece
        }

        return pieceColor + pieceChar + RESET_TEXT_COLOR;
    }
}

