package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class SQLGameDAOTest {

    private SQLGameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    @Test
    public void testCreateGameSuccess() throws DataAccessException {
        GameData gameData = new GameData(1, "pp", "pp2", "Test", new ChessGame());
        gameDAO.createGame(gameData);

        GameData retrievedGame = gameDAO.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals(1, retrievedGame.gameID());
        assertEquals("pp", retrievedGame.whiteUsername());
        assertEquals("pp2", retrievedGame.blackUsername());
        assertEquals("Test", retrievedGame.gameName());
        assertNotNull(retrievedGame.game());
    }

    // Create duplicate gameIDs
    @Test
    public void testCreateGameFailure() throws DataAccessException {
        GameData gameData1 = new GameData(1, "pp", "pp2", "Game 1", null);
        GameData gameData2 = new GameData(1, "pp3", "pp4", "Game 2", null);

        gameDAO.createGame(gameData1);
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(gameData2),
                "Error: they are duplicate gameID");
    }

    @Test
    public void testGetGameSuccess() throws DataAccessException {
        GameData gameData = new GameData(2, "pp", "pp2", "Game 2", new ChessGame());
        gameDAO.createGame(gameData);

        GameData retrievedGame = gameDAO.getGame(2);
        assertNotNull(retrievedGame);
        assertEquals(2, retrievedGame.gameID());
        assertEquals("pp", retrievedGame.whiteUsername());
        assertEquals("pp2", retrievedGame.blackUsername());
        assertEquals("Game 2", retrievedGame.gameName());
    }

    // Getting games that don't exist
    @Test
    public void testGetGameFailure() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(99999999),
                "non-existent gameID");
    }

    @Test
    public void testListGamesSuccess() throws DataAccessException {
        GameData game1 = new GameData(1, "pp", null, "Game 1", null);
        GameData game2 = new GameData(2, null, "pp2", "Game 2", null);
        GameData game3 = new GameData(3, "pp3", "pp4", "Game 3", null);

        gameDAO.createGame(game1);
        gameDAO.createGame(game2);
        gameDAO.createGame(game3);

        List<GameData> games = gameDAO.listGames();
        assertEquals(3, games.size(), "Game 3 in the list");
    }

    // List no games
    @Test
    public void testListGamesFailure() throws DataAccessException {
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "no games in the list");
    }

    @Test
    public void testUpdateGameSuccess() throws DataAccessException {
        GameData gameData = new GameData(1, "pp", null, "Original Game", null);
        gameDAO.createGame(gameData);

        GameData updatedGame = new GameData(1, "pp", "pp2", "Updated Game", new ChessGame());
        gameDAO.updateGame(updatedGame);

    }

    // Updating games that don't exist
    @Test
    public void testUpdateGameFailure() throws DataAccessException {
        GameData gameData = new GameData(99999999, "pp", "pp2", "Non-existent Game", null);

    }

    @Test
    public void testClear() throws DataAccessException {
        GameData gameData = new GameData(1, "pp", "pp2", "Game to Clear", null);
        gameDAO.createGame(gameData);
        gameDAO.clear();

        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "games be cleared");
    }
}
