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
        } /*else if (currentType == PieceType.KNIGHT) {
            moves = knightMove(board, row, col, color, currentType);
        } else if (currentType == PieceType.PAWN) {
            moves = pawnMoves(board, row, col, color, currentType);
        } else {
            throw new IllegalArgumentException("Wrong piece_type: " + currentType);
        }*/
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
    }