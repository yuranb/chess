package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import java.util.List;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private int gameCounter =1;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

public List<GameData> listGames(String authToken) throws DataAccessException {
    // Verify authToken exists
    AuthData authData = validateAuthToken(authToken);

    return gameDAO.listGames();
    }

public int createGame(String authToken, String gameName) throws DataAccessException {
    // Verify authToken exists
    AuthData authData = validateAuthToken(authToken);

    int gameID = gameCounter++;
    GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
    gameDAO.createGame(newGame);

    return gameID;
}

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        // Verify that the authToken exists
        AuthData authData = validateAuthToken(authToken);
        String username = authData.username();

        GameData existingGame  = gameDAO.getGame(gameID);
        if (existingGame  == null) {
            throw new DataAccessException("Bad request: Game does not exist");
        }

        String whitePlayer = existingGame.whiteUsername();
        String blackPlayer = existingGame.blackUsername();

        // Check if the color spot is available
        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (whitePlayer != null) {
                throw new DataAccessException("Forbidden: WHITE spot is already taken");
            }
            whitePlayer = username;
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if ( blackPlayer != null) {
                throw new DataAccessException("Forbidden: BLACK spot is already taken");
            }
            blackPlayer = username;
        } else {
            throw new DataAccessException("Bad request: Invalid color Choice");
        }

        // Update the game data with the new player
        GameData updatedGame = new GameData(
                gameID,
                whitePlayer,
                blackPlayer,
                existingGame.gameName(),
                existingGame.game()
        );
        gameDAO.updateGame(updatedGame);
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
    }

    private AuthData validateAuthToken(String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Unauthorized: Invalid auth token");
        }
        return authData;
    }
}
