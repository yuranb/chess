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
     * Sets which team's turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
     * Gets valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Collection of valid moves for the requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece movingPiece = board.getPiece(startPosition);

        if (movingPiece == null) {
            return null;
        }

        Collection<ChessMove> potentialMoves = movingPiece.pieceMoves(board, startPosition);
        List<ChessMove> legalMoves = new ArrayList<>();

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
     * @param move chess move to perform
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
            // 处理升变
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
            throw new InvalidMoveException("Your side remains in Check after moving.");
        }

        // 切换回合
        switchTurn();
    }

    /**
     * Switches the current turn to the opposite team.
     */
    private void switchTurn() {
        this.turn = (this.turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);

        if (kingPosition == null) {
            return false; // King not found, possibly game over
        }

        TeamColor enemyColor = getEnemyColor(teamColor);

        // 检查所有敌方棋子的移动是否可以攻击国王
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == enemyColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true; // 被将军
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null; // King not found
    }

    /**
     * Gets the enemy team color.
     *
     * @param teamColor The current team color.
     * @return The enemy team color.
     */
    private TeamColor getEnemyColor(TeamColor teamColor) {
        return (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in checkmate.
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false; // 未被将军，不可能被将死
        }

        // 遍历所有己方棋子，检查是否有任何合法移动可以解除将军
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition startPos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(startPos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(startPos);
                    if (moves != null && !moves.isEmpty()) {
                        return false; // 存在至少一个合法移动，可以解除将军
                    }
                }
            }
        }
        return true; // 没有合法移动，处于将死状态
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having     * no valid moves     *     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false; // 被将军，不可能是僵局
        }

        // 检查是否有合法的移动
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition startPos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(startPos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(startPos);
                    if (moves != null && !moves.isEmpty()) {
                        return false; // 有合法移动所以不是僵局
                    }
                }
            }
        }
        return true; // 没有合法移动所以是僵局
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