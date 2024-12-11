package ui;

import chess.*;

import java.util.Set;

import static ui.EscapeSequences.*;

public class ChessBoardUI {
    public ChessBoardUI() {
    }

    public void printBoardWithHighlights(ChessGame game, ChessPosition startPosition, boolean isBlackView, Set<ChessPosition> highlightedPositions) {
        System.out.print(ERASE_SCREEN);
        ChessBoard board = game.getBoard();


        if (startPosition != null) {
            highlightedPositions.add(startPosition);
        }

        System.out.println(SET_TEXT_BOLD + (isBlackView ? "Black's Perspective:" : "White's Perspective:"));
        showBoardView(board, isBlackView, highlightedPositions);

        System.out.print(RESET_TEXT_BOLD_FAINT);
    }

    public void display(ChessGame game, boolean isBlackView) {
        System.out.print(ERASE_SCREEN);

        if (isBlackView) {
            System.out.println(SET_TEXT_BOLD + "Black's Perspective:");
        } else {
            System.out.println(SET_TEXT_BOLD + "White's Perspective:");
        }

        ChessBoard board = game.getBoard();

        showBoardView(board, isBlackView, Set.of());
        System.out.print(RESET_TEXT_BOLD_FAINT);
    }

    private void showBoardView( ChessBoard board, boolean isBlackView, Set<ChessPosition> highlightedPositions) {
        drawColumnLabels(isBlackView);
        if (isBlackView) {
            // 黑棋视角：行号从1到8，从上到下
            for (int row = 1; row <= 8; row++) {
                int actualRow = row;
                drawBoardRow(board, actualRow, isBlackView, highlightedPositions);
            }
        } else {
            // 白棋视角：行号从8到1，从上到下
            for (int row = 8; row >= 1; row--) {
                int actualRow = row;
                drawBoardRow(board, actualRow, isBlackView, highlightedPositions);
            }
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
    private void drawBoardRow(ChessBoard board, int actualRow, boolean isBlackView, Set<ChessPosition> highlightedPositions) {

        int displayRow = isBlackView ? 9 - actualRow : actualRow;
        // Draw left row number
        System.out.print(SET_TEXT_COLOR_LIGHT_GREY + String.format(" %d ", actualRow) + RESET_TEXT_COLOR);

        // Draw each square in the row
        for (int col = 1; col <= 8; col++) {
            int actualCol = col;
            if (isBlackView) {
                actualCol = 9 - col;
            }
            ChessPosition position = new ChessPosition(actualRow, actualCol);

            // Set square background color
            String squareColor;
            if (highlightedPositions.contains(position)) {
                squareColor = SET_BG_COLOR_YELLOW;
            }
              else if ((actualRow + actualCol) % 2 != 0) {
                squareColor = SET_BG_COLOR_WHITE;
            } else {
                squareColor = SET_BG_COLOR_DARK_GREY;
            }

            // Draw piece
            String pieceStr = getPieceSymbol(board, position);
            System.out.print(squareColor + pieceStr + RESET_BG_COLOR);
        }

        System.out.print(SET_TEXT_COLOR_LIGHT_GREY + String.format(" %d ", displayRow) + RESET_TEXT_COLOR);
        System.out.println();
    }

    private String getPieceSymbol(ChessBoard board, ChessPosition position) {
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

