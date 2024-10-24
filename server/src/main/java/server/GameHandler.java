package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.List;

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

    private record ListGamesResponse(List<GameData> games) {
    }

}

