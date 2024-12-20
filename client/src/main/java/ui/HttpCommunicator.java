package ui;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpCommunicator {

    private final String serverUrl;
    private String authToken = null;

    public HttpCommunicator(String url) {
        serverUrl = url;
    }

    //Register a new user
    public AuthData register(String username, String password, String email) throws ResponseException {
        var path = "/user";
        UserData userData = new UserData(username, password, email);
        AuthData authData = this.makeRequest("POST", path, userData, AuthData.class);
        this.authToken = authData.authToken();
        return authData;
    }

    //Login
    public AuthData login(String username, String password) throws ResponseException {
        var path = "/session";
        UserData userData = new UserData(username, password, null);
        AuthData authData = this.makeRequest("POST", path, userData, AuthData.class);
        this.authToken = authData.authToken();
        return authData;
    }

    //Logout
    public void logout() throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null);
        this.authToken = null;
    }

    //CreateGame
    public GameData createGame(String gameName) throws ResponseException {
        var path = "/game";
        var request = new HashMap<String, String>();
        request.put("gameName", gameName);
        return this.makeRequest("POST", path, request, GameData.class);
    }

    //ListGames
    public List<GameData> listGames() throws ResponseException {
        var path = "/game";
        GameList gamesList = this.makeRequest("GET", path, null, GameList.class);
        return gamesList.getGames();
    }

    //Join Game
    public GameData playGame(int gameID, String playerColor) throws ResponseException {
        var path = "/game";
        Map<String, Object> request = new HashMap<>();
        request.put("gameID", gameID);
        if (playerColor != null) {
            request.put("playerColor", playerColor);
        }

        return this.makeRequest("PUT", path, request, GameData.class);
    }


    public void clear() throws Exception {
        this.makeRequest("DELETE", "/db", null, null);
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (this.authToken != null) {
                http.setRequestProperty("Authorization", this.authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
