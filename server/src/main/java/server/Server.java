package server;

import dataaccess.*;
import service.*;
import spark.*;
import com.google.gson.Gson;
import java.util.Map;

public class Server {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    private final UserService userService;
    private final GameService gameService;

    private final UserHandler userHandler;
    private final GameHandler gameHandler;

    private final Gson gson = new Gson();

    public Server() {

        this.userDAO = new MemoryUserDAO();
        this.authDAO = new MemoryAuthDAO();
        this.gameDAO = new MemoryGameDAO();

        this.userService = new UserService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);

        this.userHandler = new UserHandler(userService);
        this.gameHandler = new GameHandler(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.delete("/db", this::clear);
        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);

        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

        Spark.exception(DataAccessException.class, this::dataAccessExceptionHandler);
        Spark.exception(Exception.class, this::genericExceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clear(Request req, Response resp) throws DataAccessException {

        userService.clear();
        gameService.clear();

        resp.status(200);
        resp.type("application/json");
        return gson.toJson(Map.of("message", "All data cleared successfully"));
    }

    private void dataAccessExceptionHandler(DataAccessException ex, Request req, Response resp) {
        String message = ex.getMessage().toLowerCase();
        if (message.contains("unauthorized") || message.contains("invalid auth token")) {
            resp.status(401);
        } else if (message.contains("bad request") || message.contains("invalid") || message.contains("missing")) {
            resp.status(400);
        } else if (message.contains("forbidden") || message.contains("already taken")) {
            resp.status(403);
        } else {
            resp.status(500);
        }
        resp.type("application/json");
        resp.body(gson.toJson(Map.of("message", "error: " + ex.getMessage())));
    }

    private void genericExceptionHandler(Exception ex, Request req, Response resp) {
        resp.status(500);
        resp.type("application/json");
        resp.body(gson.toJson(Map.of("message", "error: " + ex.getMessage())));
    }
}