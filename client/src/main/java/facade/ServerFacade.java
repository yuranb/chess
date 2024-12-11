package facade;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import ui.HttpCommunicator;
import ui.ServerMessageObserver;
import ui.WebSocketCommunicator;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;

import java.util.List;

public class ServerFacade {
    private final HttpCommunicator httpCommunicator;
    private WebSocketCommunicator wsCommunicator;
    private String authToken;

    public ServerFacade(String baseUrl) {
        this.httpCommunicator = new HttpCommunicator(baseUrl);
    }

    public void register(String username, String password, String email) throws ResponseException {
        AuthData auth = httpCommunicator.register(username, password, email);
        this.authToken = auth.authToken();
        httpCommunicator.setAuthToken(this.authToken);
    }

    public void login(String username, String password) throws ResponseException {
        AuthData auth = httpCommunicator.login(username, password);
        this.authToken = auth.authToken();
        httpCommunicator.setAuthToken(this.authToken);
    }

    public void logout() throws ResponseException {
        httpCommunicator.logout();
        this.authToken = null;
        httpCommunicator.setAuthToken(null);
    }

    public GameData createGame(String gameName) throws ResponseException {
        return httpCommunicator.createGame(gameName);
    }

    public List<GameData> listGames() throws ResponseException {
        return httpCommunicator.listGames();
    }

    public void connectAsPlayer(int gameID, String playerColor) {
        UserGameCommand connect = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                this.authToken,
                gameID,
                UserGameCommand.Role.PLAYER,
                playerColor
        );
        sendCommand(connect);
    }

    public void connectAsObserver(int gameID) {
        UserGameCommand connect = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                this.authToken,
                gameID,
                UserGameCommand.Role.OBSERVER,
                null
        );
        sendCommand(connect);
    }

    public void playGame(int gameID, String playerColor) throws ResponseException {
        httpCommunicator.playGame(gameID, playerColor);
    }

    public String observeGame(int gameID) throws ResponseException {
        // Fetch the list of games to ensure the gameID is valid
        List<GameData> games = listGames();
        boolean gameExists = games.stream().anyMatch(game -> game.gameID() == gameID);

        if (!gameExists) {
            throw new ResponseException(400, "Game with ID " + gameID + " not found.");
        }

        // Create a ConnectCommand with role OBSERVER
        UserGameCommand connectCommand = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                this.authToken,
                gameID,
                UserGameCommand.Role.OBSERVER,
                null // No playerColor for observers
        );

        // Send the ConnectCommand via WebSocket
        sendCommand(connectCommand);

        return "Now observing game: " + gameID;
    }

    public void connectWebSocket(String baseUrl, ServerMessageObserver observer) {
        wsCommunicator = new WebSocketCommunicator(baseUrl, observer);
    }

    public void sendCommand(UserGameCommand command) {
        if (wsCommunicator == null) {
            throw new IllegalStateException("WebSocket not connected");
        }
        wsCommunicator.sendCommand(command);
    }

    public void makeMove(int gameID, ChessMove move) {
        MakeMoveCommand moveCommand = new MakeMoveCommand(this.authToken, gameID, move);
        sendCommand(moveCommand);
    }

    public void leaveGame(Integer gameID) {
        UserGameCommand leave = new UserGameCommand(UserGameCommand.CommandType.LEAVE, this.authToken, gameID);
        sendCommand(leave);
    }

    public void resignGame(int gameID) {
        UserGameCommand resign = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        sendCommand(resign);
    }
}
