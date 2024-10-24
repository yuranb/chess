package service;

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
    AuthData authData = authDAO.getAuth(authToken);
    if (authData == null) {
        throw new DataAccessException("Invalid auth token");
    }

    return gameDAO.listGames();
    }

public int createGame(String authToken, String gameName) throws DataAccessException {
    // Verify authToken exists
    AuthData authData = authDAO.getAuth(authToken);
    if (authData == null) {
        throw new DataAccessException("Invalid auth token");
    }

    int GameID = gameCounter++;
    GameData newGame = new GameData(GameID, null, null, gameName, null);
    gameDAO.createGame(newGame);

    return GameID;
}

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        // Verify that the authToken exists
        AuthData authData = authDAO.getAuth(authToken);

        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }
        String username = authData.username();


        GameData existingGame  = gameDAO.getGame(gameID);
        if (existingGame  == null) {
            throw new DataAccessException("Game does not exist");
        }

        String whitePlayer = existingGame.whiteUsername();
        String blackPlayer = existingGame.blackUsername();

        // Check if the color spot is available
        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (whitePlayer != null) {
                throw new DataAccessException("WHITE spot is already taken");
            }
            whitePlayer = username;
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if ( blackPlayer != null) {
                throw new DataAccessException("BLACK spot is already taken");
            }
            blackPlayer = username;
        } else {
            throw new DataAccessException("Invalid color Choice");
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
}
