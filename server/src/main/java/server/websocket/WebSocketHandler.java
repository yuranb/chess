package server.websocket;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
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
                case CONNECT -> handleConnect(command, session);
                case MAKE_MOVE -> handleMakeMove(command, session);
                case LEAVE -> handleLeave(command, session);
                case RESIGN -> handleResign(command, session);
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

        connections.add(gameID, session);
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
        connections.broadcast(gameID, gson.toJson(notification));
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

    private void handleMakeMove(UserGameCommand command, Session session) {
        sendError(session, "..");
    }

    private void handleLeave(UserGameCommand command, Session session) {
        sendError(session, "...");
    }

    private void handleResign(UserGameCommand command, Session session) {
        sendError(session, "...");
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