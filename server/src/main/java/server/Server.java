package server;

import dataaccess.*;
import service.GameService;
import service.UserService;
import spark.*;

import com.google.gson.Gson;
import java.util.Map;

public class Server {

    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    UserService userService;
    GameService gameService;

    UserHandler userHandler;
    private final Gson gson = new Gson();
    public Server() {

        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        userHandler = new UserHandler(userService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.delete("/db", this::clear);
        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);

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
        resp.body(gson.toJson(Map.of("message", "error: " + ex.getMessage())));
    }
}