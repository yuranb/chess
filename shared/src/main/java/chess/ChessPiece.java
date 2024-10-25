package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.List;
/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor color;
    private final ChessPiece.PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.pieceType = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, pieceType);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        PieceType currentType = currentPiece.getPieceType();
        List<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        switch (currentType) {
            case BISHOP:
                moves.addAll(generateMoves(board, row, col, color, currentType, getDiagonalDirections()));
                break;
            case ROOK:
                moves.addAll(generateMoves(board, row, col, color, currentType, getStraightDirections()));
                break;
            case QUEEN:
                moves.addAll(generateMoves(board, row, col, color, currentType, getDiagonalDirections()));
                moves.addAll(generateMoves(board, row, col, color, currentType, getStraightDirections()));
                break;
            case KING:
                moves.addAll(generateMoves(board, row, col, color, currentType, getKingDirections()));
                break;
            case KNIGHT:
                moves.addAll(generateKnightMoves(board, row, col, color));
                break;
            case PAWN:
                moves.addAll(generatePawnMoves(board, row, col, color));
                break;
            default:
                throw new IllegalArgumentException("Wrong piece_type: " + currentType);
        }

        return moves;
    }


    private List<ChessMove> generateMoves(ChessBoard chessBoard, int row, int col, ChessGame.TeamColor currentColor,
                                          PieceType pieceType, int[][] directions) {
        ChessPosition startPosition = new ChessPosition(row, col);
        List<ChessMove> validMoves = new ArrayList<>();

        for (int[] direction : directions) {
            int newRow = row;
            int newCol = col;

            while (true) {
                newRow += direction[0];
                newCol += direction[1];

                // Break the loop if out of bounds
                if (!isWithinBounds(newRow, newCol)) {
                    break;
                }

                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtPos = chessBoard.getPiece(newPos);

                if (pieceAtPos != null) {
                    if (pieceAtPos.getTeamColor() == currentColor) {
                        break; // Stop if it's our own piece
                    } else {
                        validMoves.add(new ChessMove(startPosition, newPos, null)); // Capture opponent's piece
                        break; // And then stop
                    }
                }

                // Add the move as it's valid and continue
                validMoves.add(new ChessMove(startPosition, newPos, null));

                if (pieceType == PieceType.KING) {
                    break; // King can only move one square
                }
            }
        }

        return validMoves;
    }

    private List<ChessMove> generateKnightMoves(ChessBoard chessBoard, int row, int col, ChessGame.TeamColor currentColor) {
        ChessPosition startPosition = new ChessPosition(row, col);
        List<ChessMove> validMoves = new ArrayList<>();
        int[][] directions = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            // Check if the new position is within the bounds of the board
            if (isWithinBounds(newRow, newCol)) {
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtPos = chessBoard.getPiece(newPos);

                if (pieceAtPos != null && pieceAtPos.getTeamColor() == currentColor) {
                    continue;
                }

                validMoves.add(new ChessMove(startPosition, newPos, null));
            }
        }

        return validMoves;
    }


    private List<ChessMove> generatePawnMoves(ChessBoard chessBoard, int row, int col, ChessGame.TeamColor currentColor) {
        List<ChessMove> validMoves = new ArrayList<>();
        int direction = (currentColor == ChessGame.TeamColor.WHITE) ? 1 : -1; // White moves up (1), Black moves down (-1)
        int startRow = (currentColor == ChessGame.TeamColor.WHITE) ? 2 : 7; // Starting row for pawns
        int nextRow = row + direction;

        ChessPosition startPosition = new ChessPosition(row, col);

        // Check simple move forward
        if (isWithinBounds(nextRow, col) && chessBoard.getPiece(new ChessPosition(nextRow, col)) == null) {
            if (nextRow == 1 || nextRow == 8) { // Promotion row for pawns
                addPromotionMoves(validMoves, startPosition, nextRow, col);
            } else {
                validMoves.add(new ChessMove(startPosition, new ChessPosition(nextRow, col), null));

                // Check if it's the initial two-square move
                if (row == startRow && isWithinBounds(nextRow + direction, col)
                        && chessBoard.getPiece(new ChessPosition(nextRow + direction, col)) == null) {
                    ChessPosition targetPosition = new ChessPosition(nextRow + direction, col);
                    validMoves.add(new ChessMove(startPosition, targetPosition, null));
                }
            }
        }

        // Check captures on diagonals
        int[] colsToCheck = {col - 1, col + 1}; // Check left and right diagonal squares
        for (int nextCol : colsToCheck) {
            if (isWithinBounds(nextRow, nextCol)) {
                ChessPiece target = chessBoard.getPiece(new ChessPosition(nextRow, nextCol));
                if (target != null && target.getTeamColor() != currentColor) {
                    if (nextRow == 1 || nextRow == 8) { // Promotion row
                        addPromotionMoves(validMoves, startPosition, nextRow, nextCol);
                    } else {
                        validMoves.add(new ChessMove(startPosition, new ChessPosition(nextRow, nextCol), null));
                    }
                }
            }
        }

        return validMoves;
    }

    private void addPromotionMoves(List<ChessMove> validMoves, ChessPosition startPosition, int newRow, int newCol) {
        validMoves.add(new ChessMove(startPosition, new ChessPosition(newRow, newCol), PieceType.QUEEN));
        validMoves.add(new ChessMove(startPosition, new ChessPosition(newRow, newCol), PieceType.ROOK));
        validMoves.add(new ChessMove(startPosition, new ChessPosition(newRow, newCol), PieceType.BISHOP));
        validMoves.add(new ChessMove(startPosition, new ChessPosition(newRow, newCol), PieceType.KNIGHT));
    }

    private int[][] getDiagonalDirections() {
        return new int[][]{
                {1, 1},    // right up
                {1, -1},   // right down
                {-1, 1},   // left up
                {-1, -1}   // left down
        };
    }


    private int[][] getStraightDirections() {
        return new int[][]{
                {0, 1}, {0, -1}, {1, 0}, {-1, 0} // up, down, right, left
        };
    }


    private int[][] getKingDirections() {
        return new int[][]{
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                {0, 1}, {0, -1}, {1, 0}, {-1, 0}
        };
    }

    private boolean isWithinBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
