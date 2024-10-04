package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor turn;

    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && turn == chessGame.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, turn);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        List<ChessMove> legalMoves = new ArrayList<>();
        ChessPiece movingPiece = board.getPiece(startPosition);

        if (movingPiece == null) {
            return null;
        }

        Collection<ChessMove> potentialMoves = movingPiece.pieceMoves(board, startPosition);

        for (ChessMove move : potentialMoves) {
            // 模拟执行
            ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
            board.clearPiece(startPosition);
            board.addPiece(move.getEndPosition(), movingPiece);

            // 如果移动后未将军，则添加为合法移动
            if (!isInCheck(movingPiece.getTeamColor())) {
                legalMoves.add(move);
            }

            // 撤销移动
            board.addPiece(startPosition, movingPiece);
            if (capturedPiece != null) {
                board.addPiece(move.getEndPosition(), capturedPiece);
            } else {
                board.clearPiece(move.getEndPosition());
            }
        }
        return legalMoves;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece currentPiece = this.board.getPiece(move.getStartPosition());
        if (currentPiece == null) {
            throw new InvalidMoveException("No piece at the start position.");
        }
        if (currentPiece.getTeamColor() != turn) {
            throw new InvalidMoveException("This is not your turn.");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Illegal movement.");
        }

        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
        board.clearPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            //处理升变
            ChessPiece promotedPiece = new ChessPiece(turn, move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), promotedPiece);
        } else {
            board.addPiece(move.getEndPosition(), currentPiece);
        }

        // 检查移动后是否使自己被将军
        if (isInCheck(turn)) {
            // 撤销移动
            board.addPiece(move.getStartPosition(), currentPiece);
            if (capturedPiece != null) {
                board.addPiece(move.getEndPosition(), capturedPiece);
            } else {
                board.clearPiece(move.getEndPosition());
            }
            throw new InvalidMoveException("Your side remains in General after moving.");
        }

        // 切换回合
        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        // 遍历棋盘，寻找己方国王的位置
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = position;
                    break;
                }
            }
            if (kingPosition != null) {
                break;
            }
        }

        if (kingPosition == null) {
            return false;
        }

        TeamColor enemyColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        // 遍历棋盘，检查敌方棋子是否威胁到国王
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == enemyColor) {
                    // 获取敌方棋子的所有可能移动
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true; // 己方被将军
                        }
                    }
                }
            }
        }//add more
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having     * no valid moves     *     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}