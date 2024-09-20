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

    private ChessGame.TeamColor color;
    private ChessPiece.PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.pieceType = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
        PieceType currentType = currentPiece.pieceType;
        List<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        if (currentType == PieceType.BISHOP) {
            moves = diagonal(board, row, col, color, currentType);
        } else if (currentType == PieceType.ROOK) {
            moves = straight(board, row, col, color, currentType);
        } else if (currentType == PieceType.QUEEN) {
            moves.addAll(diagonal(board, row, col, color, currentType));
            moves.addAll(straight(board, row, col, color, currentType));
        } else if (currentType == PieceType.KING) {
            moves.addAll(diagonal(board, row, col, color, currentType));
            moves.addAll(straight(board, row, col, color, currentType));
        } else if (currentType == PieceType.KNIGHT) {
            moves = knightMove(board, row, col, color, currentType);
        } else if (currentType == PieceType.PAWN) {
            moves = pawnMoves(board, row, col, color, currentType);
        } else {
            throw new IllegalArgumentException("Wrong piece_type: " + currentType);
        }
        return moves;
    }

    public static List<ChessMove> diagonal(ChessBoard chessBoard, int row, int col, ChessGame.TeamColor currentColor, PieceType currentType) {
        ChessPosition s_Position = new ChessPosition(row, col);
        List<ChessMove> valid_moves = new ArrayList<>();

        int[][] directions = {
                {1, 1},    // right up
                {1, -1},   // right down
                {-1, 1},   // left up
                {-1, -1}   // left down
        };

        for (int[] direction : directions) {
            int newRow = row;
            int newCol = col;

            // Move in the specified diagonal direction
            while (true) {
                newRow += direction[0];
                newCol += direction[1];

                // Break the loop if out of bounds
                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                    break;
                }

                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtPos = chessBoard.getPiece(newPos);

                // Stop if there is a piece of the same color or capture if opponent's piece
                if (pieceAtPos != null) {
                    if (pieceAtPos.getTeamColor() == currentColor) {
                        break;  // Stop if it's our own piece
                    } else {
                        valid_moves.add(new ChessMove(s_Position, newPos, null));  // Capture opponent's piece
                        break;  // And then stop
                    }
                }

                // Add the move as it's valid and continue
                valid_moves.add(new ChessMove(s_Position, newPos, null));

                if (currentType == PieceType.KING) {
                    break;
                }
            }
        }

        return valid_moves;
    }

    public static List<ChessMove> straight(ChessBoard chessBoard, int row, int col, ChessGame.TeamColor currentColor, PieceType currentType) {
        ChessPosition s_Position = new ChessPosition(row, col);
        List<ChessMove> valid_moves = new ArrayList<>();

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};  // up, down, right, left

        for (int[] direction : directions) {
            int newRow = row;
            int newCol = col;

            while (true) {
                newRow += direction[0];
                newCol += direction[1];

                // Break the loop if out of bounds
                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                    break;
                }

                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtPos = chessBoard.getPiece(newPos);

                // Stop if there is a piece of the same color or capture if opponent's piece
                if (pieceAtPos != null) {
                    if (pieceAtPos.getTeamColor() == currentColor) {
                        break;  // Stop if it's our own piece
                    } else {
                        valid_moves.add(new ChessMove(s_Position, newPos, null));  // Capture opponent's piece
                        break;  // And then stop
                    }
                }

                // Add the move as it's valid and continue
                valid_moves.add(new ChessMove(s_Position, newPos, null));

                if (currentType == PieceType.KING) {
                    break;
                }
            }
        }

        return valid_moves;
    }

    public static List<ChessMove> knightMove(ChessBoard chessBoard, int row, int col, ChessGame.TeamColor currentColor, PieceType currentType) {
        ChessPosition s_Position = new ChessPosition(row, col);
        List<ChessMove> valid_moves = new ArrayList<>();
        int[][] directions = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            // Check if the new position is within the bounds of the board
            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtPos = chessBoard.getPiece(newPos);

                if (pieceAtPos != null && pieceAtPos.getTeamColor() == currentColor) {
                    continue;
                }

                // Add the move if the position is unoccupied or occupied by an enemy piece
                valid_moves.add(new ChessMove(s_Position, newPos, null));

                // If it is occupied by an enemy piece, we also stop checking further in this direction
                if (pieceAtPos != null && pieceAtPos.getTeamColor() != currentColor) {
                    continue;
                }
            }
        }

        return valid_moves;
    }

    public static List<ChessMove> pawnMoves(ChessBoard chessBoard, int row, int col, ChessGame.TeamColor currentColor, PieceType currentType) {
        List<ChessMove> valid_moves = new ArrayList<>();
        int direction = (currentColor == ChessGame.TeamColor.WHITE) ? 1 : -1; // White moves up (1), Black moves down (-1)
        int startRow = (currentColor == ChessGame.TeamColor.WHITE) ? 2 : 7; // Starting row for pawns
        int nextRow = row + direction;

        // Check simple move forward
        if (chessBoard.getPiece(new ChessPosition(nextRow, col)) == null) {
            if (nextRow == 1 || nextRow == 8) { // Promotion row for pawns
                valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, col), PieceType.QUEEN));
                valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, col), PieceType.ROOK));
                valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, col), PieceType.BISHOP));
                valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, col), PieceType.KNIGHT));
            } else {
                valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, col), null));
                // Check if it's the initial two-square move
                boolean isStartingRow = row == startRow;
                boolean isNextSquareEmpty = chessBoard.getPiece(new ChessPosition(nextRow + direction, col)) == null;
                // If both conditions are met, add the two-square move as a valid option
                if (isStartingRow && isNextSquareEmpty) {
                    ChessPosition startPosition = new ChessPosition(row, col);
                    ChessPosition targetPosition = new ChessPosition(nextRow + direction, col);
                    ChessMove twoSquareMove = new ChessMove(startPosition, targetPosition, null);
                    valid_moves.add(twoSquareMove);
                }
            }
        }

        // Check captures on diagonals
        int[] cols = {col - 1, col + 1}; // Check left and right diagonal squares
        for (int nextCol : cols) {
            if (nextCol >= 1 && nextCol <= 8) {
                ChessPiece target = chessBoard.getPiece(new ChessPosition(nextRow, nextCol));
                if (target != null && target.getTeamColor() != currentColor) {
                    if (nextRow == 1 || nextRow == 8) { // Promotion row
                        valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, nextCol), PieceType.QUEEN));
                        valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, nextCol), PieceType.ROOK));
                        valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, nextCol), PieceType.BISHOP));
                        valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, nextCol), PieceType.KNIGHT));
                    } else {
                        valid_moves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(nextRow, nextCol), null));
                    }
                }
            }
        }
        return valid_moves;
    }
}