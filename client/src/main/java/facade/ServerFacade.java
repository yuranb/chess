package facade;

import chess.ChessMove;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import ui.HttpCommunicator;
import ui.ServerMessageObserver;
import ui.WebSocketCommunicator;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

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

    public void playGame(int gameID, String playerColor) throws ResponseException {
        httpCommunicator.playGame(gameID, playerColor);
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
        MakeMoveCommand moveCommand = new MakeMoveCommand(authToken, gameID, move);
        sendCommand(moveCommand);
    }

    /*public void leaveGame(int gameID) {
        UserGameCommand leave = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        sendCommand(leave);
    }*/

    public void resignGame(int gameID) {
        UserGameCommand resign = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        sendCommand(resign);
    }
}
