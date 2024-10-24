package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request req, Response resp) throws DataAccessException {
        UserData userData;
        try {
            userData = gson.fromJson(req.body(), UserData.class);
        } catch (JsonSyntaxException e) {
            throw new DataAccessException("Bad request: invalid JSON format");
        }

        if (userData.username() == null || userData.password() == null || userData.email() == null) {
            throw new DataAccessException("Bad request: missing username, password, or email");
        }

        AuthData authData = userService.createUser(userData);
        resp.status(200);
        resp.type("application/json");
        return gson.toJson(authData);
    }

    public Object login(Request req, Response resp) throws DataAccessException {
        UserData userData;
        try {
            userData = gson.fromJson(req.body(), UserData.class);
        } catch (JsonSyntaxException e) {
            throw new DataAccessException("Bad request: invalid JSON format");
        }

        if (userData.username() == null || userData.password() == null) {
            throw new DataAccessException("Bad request: missing username or password");
        }

        AuthData authData = userService.loginUser(userData);
        resp.status(200);
        resp.type("application/json");
        return gson.toJson(authData);
    }

    public Object logout(Request req, Response resp) throws DataAccessException {
        String authToken = req.headers("authorization");

        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("Unauthorized: missing auth token");
        }

        userService.logoutUser(authToken);

        resp.status(200);
        resp.type("application/json");
        return "{}";
    }
}
