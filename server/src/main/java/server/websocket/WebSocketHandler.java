package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public WebSocketHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connection opened: " + session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket connection closed: " + session);
        connections.cleanUpClosedConnections();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> {
                    handleConnect(command, session);
                }
                case MAKE_MOVE -> {
                    MakeMoveCommand makeMoveCommand = gson.fromJson(message, MakeMoveCommand.class);
                    handleMakeMove(session, makeMoveCommand);
                }
                case LEAVE -> {
                    handleLeave(command, session);
                }
                case RESIGN -> {
                    handleResign(command, session);
                }
                default -> sendError(session, "Invalid command type");
            }
        } catch (Exception e) {
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }

    private void handleConnect(UserGameCommand command, Session session) throws Exception {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        // Validate auth token
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            sendError(session, "Error: invalid auth token");
            return;
        }

        // Validate game ID
        if (gameID == null || gameID < 0 || gameID > 1) {
            sendError(session, "Error: invalid game ID");
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "Error: game not found");
            return;
        }

        String username = authData.username();
        String playerRole = determinePlayerRole(gameData, username);

        connections.add(username, gameID, session);
        LoadGameMessage gameMessage = new LoadGameMessage(
            ServerMessage.ServerMessageType.LOAD_GAME,
            gameData.game()
        );
        session.getRemote().sendString(gson.toJson(gameMessage));

        // Notify other players
        NotificationMessage notification = new NotificationMessage(
            ServerMessage.ServerMessageType.NOTIFICATION,
            username + " has joined the game as " + playerRole
        );
        connections.broadcast(gameID, null, notification);
    }

    private String determinePlayerRole(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) {
            return "WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            return "BLACK";
        } else {
            return "OBSERVER";
        }
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) throws IOException, InvalidMoveException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();
        ChessMove move = command.getMove();

        // Validate auth token
        AuthData authData;
        try {
            authData = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            sendError(session, "Error: invalid auth token");
            return;
        }

        if (authData == null) {
            sendError(session, "Error: invalid auth token");
            return;
        }

        String username = authData.username();

        // Validate game
        GameData gameData;
        try {
            gameData = gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            sendError(session, "Error: could not load game");
            return;
        }

        if (gameData == null) {
            sendError(session, "Error: game not found");
            return;
        }

        // Check if the game is already over
        if (gameData.game().isGameOver()) {
            sendError(session, "Game has already ended");
            return;
        }

        // Determine current turn
        ChessGame.TeamColor turn = gameData.game().getTeamTurn();
        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();
        String currentPlayer = (turn == ChessGame.TeamColor.WHITE) ? whiteUser : blackUser;
        String opponentPlayer = (turn == ChessGame.TeamColor.WHITE) ? blackUser : whiteUser;

        if (!username.equals(currentPlayer)) {
            sendError(session, "Error: it is not your turn");
            return;
        }

        // Check if the move is legal before calling makeMove()
        var legalMoves = gameData.game().validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            // Move is not legal
            sendError(session, "Error: invalid move");
            return;
        }

        gameData.game().makeMove(move);
        turn = gameData.game().getTeamTurn();

        String notificationMsg;
        if (gameData.game().isInCheckmate(turn)) {
            notificationMsg = opponentPlayer + " is checkmated! " + username + " wins!";
            gameData.game().setGameOver();
        } else if (gameData.game().isInStalemate(turn)) {
            notificationMsg = "Stalemate caused by " + username + "'s move! It's a tie!";
            gameData.game().setGameOver();
        } else if (gameData.game().isInCheck(turn)) {
            notificationMsg = username + " made a move, " + opponentPlayer + " is now in check!";
        } else {
            notificationMsg = username + " made a move";
        }


        try {
            // Update the game in the database
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            sendError(session, "Error: could not update game");
            return;
        }

        // Broadcast notification
        NotificationMessage notif = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationMsg);
        connections.broadcast(gameID, null, notif);

        // Broadcast updated board
        LoadGameMessage loadMsg = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        connections.broadcast(gameID, null, loadMsg);
    }

    private void handleLeave(UserGameCommand command, Session session) {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        AuthData authData;
        try {
            authData = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            sendError(session, "Error: invalid auth token");
            return;
        }
        if (authData == null) {
            sendError(session, "Error: invalid auth token");
            return;
        }

        String username = authData.username();

        GameData gameData;
        try {
            gameData = gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            sendError(session, "Error: could not load game");
            return;
        }

        if (gameData == null) {
            sendError(session, "Error: game not found");
            return;
        }

        // Determine which player is leaving and update game state
        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();
        if (username.equals(whiteUser)) {
            whiteUser = null;  // remove white player
        }
        if (username.equals(blackUser)) {
            blackUser = null;  // remove black player
        }

        // Create updated game data with the player removed
        gameData = new GameData(gameID, whiteUser, blackUser, gameData.gameName(), gameData.game());
        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            sendError(session, "Error: could not update game after leave");
            return;
        }

        connections.remove(gameID, username);

        NotificationMessage notification = new NotificationMessage(
                ServerMessage.ServerMessageType.NOTIFICATION,
                username + " has left the game."
        );
        try {
            connections.broadcast(gameID, username, notification);
        } catch (IOException e) {
            System.err.println("Error broadcasting leave notification: " + e.getMessage());
        }
    }

    private void handleResign(UserGameCommand command, Session session) {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        AuthData authData;
        try {
            authData = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            sendError(session, "Error: invalid auth token");
            return;
        }
        if (authData == null) {
            sendError(session, "Error: invalid auth token");
            return;
        }

        String username = authData.username();

        GameData gameData;
        try {
            gameData = gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            sendError(session, "Error: could not load game");
            return;
        }

        if (gameData == null) {
            sendError(session, "Error: game not found");
            return;
        }

        // Verify that the user is actually a player in the game
        boolean isPlayer = username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername());
        if (!isPlayer) {
            sendError(session, "You can't resign as an observer.");
            return;
        }

        // Check if the game is already over
        if (gameData.game().isGameOver()) {
            sendError(session, "Game is already over");
            return;
        }

        // Game over due to resignation
        gameData.game().setGameOver();

        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            sendError(session, "Error: could not update game after resign");
            return;
        }

        // Broadcast resignation notification to all players
        NotificationMessage notificationMessage = new NotificationMessage(
                ServerMessage.ServerMessageType.NOTIFICATION,
                username + " resigned."
        );
        try {
            connections.broadcast(gameID, null, notificationMessage);
        } catch (IOException e) {
            System.err.println("Error broadcasting resign notification: " + e.getMessage());
        }
    }

    private void sendError(Session session, String errorMessage) {
        try {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            session.getRemote().sendString(gson.toJson(error));
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}