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
import java.util.Optional;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public WebSocketHandler(GameDAO gameDAO, AuthDAO authDAO) {
        System.out.println("Initializing WebSocketHandler");
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connection opened: " + session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket connection closed: " + session + " with status code " + statusCode + " and reason " + reason);
        connections.cleanUpClosedConnections();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("Received message: " + message);
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            System.out.println("Command type: " + command.getCommandType());
            switch (command.getCommandType()) {
                case CONNECT -> {
                    System.out.println("Handling CONNECT command");
                    handleConnect(command, session);
                }
                case MAKE_MOVE -> {
                    System.out.println("Handling MAKE_MOVE command");
                    MakeMoveCommand makeMoveCommand = gson.fromJson(message, MakeMoveCommand.class);
                    handleMakeMove(session, makeMoveCommand);
                }
                case LEAVE -> {
                    System.out.println("Handling LEAVE command");
                    handleLeave(command, session);
                }
                case RESIGN -> {
                    System.out.println("Handling RESIGN command");
                    handleResign(command, session);
                }
                default -> {
                    System.out.println("Invalid command type received");
                    sendError(session, "Invalid command type");
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing command: " + e.getMessage());
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }


    private Optional<UserGameContext> validateSession(String authToken, Integer gameID, Session session) {
        try {
            // Validate auth token
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                System.out.println("Invalid auth token");
                sendError(session, "Error: invalid auth token");
                return Optional.empty();
            }

            // Validate game ID
            if (gameID == null || gameID < 0) {
                System.out.println("Invalid game ID");
                sendError(session, "Error: invalid game ID");
                return Optional.empty();
            }

            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                System.out.println("Game not found");
                sendError(session, "Error: game not found");
                return Optional.empty();
            }

            return Optional.of(new UserGameContext(authData, gameData));
        } catch (DataAccessException e) {
            System.out.println("Data access error: " + e.getMessage());
            sendError(session, "Error accessing data");
            return Optional.empty();
        }
    }

    private void handleConnect(UserGameCommand command, Session session) throws Exception {
        System.out.println("Executing handleConnect");
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        Optional<UserGameContext> contextOpt = validateSession(authToken, gameID, session);
        if (contextOpt.isEmpty()){
            return;
        }

        UserGameContext context = contextOpt.get();
        AuthData authData = context.getAuthData();
        GameData gameData = context.getGameData();

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
        connections.broadcast(gameID, username, notification);
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
        System.out.println("Executing handleMakeMove");
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();
        ChessMove move = command.getMove();

        Optional<UserGameContext> contextOpt = validateSession(authToken, gameID, session);
        if (contextOpt.isEmpty()){
            return;
        }

        UserGameContext context = contextOpt.get();
        AuthData authData = context.getAuthData();
        GameData gameData = context.getGameData();

        String username = authData.username();

        // Check if the game is already over
        if (gameData.game().isGameOver()) {
            System.out.println("Game has already ended");
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
            System.out.println("It is not your turn");
            sendError(session, "Error: it is not your turn");
            return;
        }

        // Check if the move is legal before calling makeMove()
        var legalMoves = gameData.game().validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            System.out.println("Invalid move");
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

        System.out.println("Move made: " + move + ", notification: " + notificationMsg);

        try {
            // Update the game in the database
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            System.out.println("Could not update game");
            sendError(session, "Error: could not update game");
            return;
        }

        // Broadcast notification
        NotificationMessage notif = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationMsg);
        connections.broadcast(gameID, username, notif);

        // Broadcast updated board
        LoadGameMessage loadMsg = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        connections.broadcast(gameID, null, loadMsg);

        System.out.println("Move and board update broadcasted");
    }

    private void handleLeave(UserGameCommand command, Session session) {
        System.out.println("Executing handleLeave");
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        Optional<UserGameContext> contextOpt = validateSession(authToken, gameID, session);
        if (contextOpt.isEmpty()){
            return;
        }

        UserGameContext context = contextOpt.get();
        AuthData authData = context.getAuthData();
        GameData gameData = context.getGameData();

        String username = authData.username();

        // Determine which player is leaving and update game state
        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();
        boolean wasPlayer = false;

        if (username.equals(whiteUser)) {
            whiteUser = null;  // Remove white player
            wasPlayer = true;
        }
        if (username.equals(blackUser)) {
            blackUser = null;  // Remove black player
            wasPlayer = true;
        }

        if (wasPlayer) {
            // Create updated game data with the player removed
            gameData = new GameData(gameID, whiteUser, blackUser, gameData.gameName(), gameData.game());
            try {
                gameDAO.updateGame(gameData);
            } catch (DataAccessException e) {
                System.out.println("Could not update game after leave");
                sendError(session, "Error: could not update game after leave");
                return;
            }
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

        System.out.println("Player " + username + " left the game");
    }

    private void handleResign(UserGameCommand command, Session session) {
        System.out.println("Executing handleResign");
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        Optional<UserGameContext> contextOpt = validateSession(authToken, gameID, session);
        if (contextOpt.isEmpty()) {
            return;
        }

        UserGameContext context = contextOpt.get();
        AuthData authData = context.getAuthData();
        GameData gameData = context.getGameData();

        String username = authData.username();

        // Verify that the user is actually a player in the game
        boolean isPlayer = username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername());
        if (!isPlayer) {
            System.out.println("User is not a player, cannot resign");
            sendError(session, "You can't resign as an observer.");
            return;
        }

        // Check if the game is already over
        if (gameData.game().isGameOver()) {
            System.out.println("Game is already over");
            sendError(session, "Game is already over");
            return;
        }

        // Game over due to resignation
        gameData.game().setGameOver();

        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            System.out.println("Could not update game after resign");
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

        System.out.println("Player " + username + " resigned");
    }

    private void sendError(Session session, String errorMessage) {
        try {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            session.getRemote().sendString(gson.toJson(error));
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }


    private static class UserGameContext {
        private final AuthData authData;
        private final GameData gameData;

        public UserGameContext(AuthData authData, GameData gameData) {
            this.authData = authData;
            this.gameData = gameData;
        }

        public AuthData getAuthData() {
            return authData;
        }

        public GameData getGameData() {
            return gameData;
        }
    }
}