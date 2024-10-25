package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object listGames(Request req, Response resp) throws DataAccessException {
        String authToken = req.headers("authorization");
        List<GameData> games = (List<GameData>) gameService.listGames(authToken);
        resp.status(200);
        resp.type("application/json");
        // Return the list of games as a JSON object
        return gson.toJson(new ListGamesResponse(games));
    }
    public Object createGame(Request req, Response resp) throws DataAccessException {
        CreateGameRequest createRequest;
        try {
            createRequest = gson.fromJson(req.body(), CreateGameRequest.class);
        } catch (JsonSyntaxException e) {
            throw new DataAccessException("Bad request: invalid JSON format");
        }

        if (createRequest.getGameName() == null || createRequest.getGameName().isEmpty()) {
            throw new DataAccessException("Bad request: no gameName provided");
        }

        String authToken = req.headers("authorization");
        int gameID = gameService.createGame(authToken, createRequest.getGameName());

        resp.status(200);
        resp.type("application/json");
        return gson.toJson(Map.of("gameID", gameID));
    }

    public Object joinGame(Request req, Response resp) throws DataAccessException {
        JoinGameRequest joinRequest;
        try {
            joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
        } catch (JsonSyntaxException e) {
            throw new DataAccessException("Bad request: invalid JSON format");
        }

        if (joinRequest.getGameID() <= 0 || joinRequest.getPlayerColor() == null) {
            throw new DataAccessException("Bad request: missing gameID or playerColor");
        }

        String authToken = req.headers("authorization");
        gameService.joinGame(authToken, joinRequest.getGameID(), joinRequest.getPlayerColor());

        resp.status(200);
        resp.type("application/json");
        return "{}";
    }

    private record ListGamesResponse(List<GameData> games) {
    }

    private static class CreateGameRequest {
        private String gameName;

        public CreateGameRequest() {}
        public String getGameName() { return gameName; }
    }

    private static class JoinGameRequest {
        private String playerColor;
        private int gameID;

        public JoinGameRequest() {}
        public String getPlayerColor() { return playerColor; }
        public int getGameID() { return gameID; }
    }
}

