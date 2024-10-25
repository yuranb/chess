package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class GameServiceTest {
    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;

    // Test data
    private final UserData testUser = new UserData("pp", "123456", "pp@gmail.com");
    private AuthData testAuth;

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();
        gameService = new GameService(gameDAO, authDAO);

        // Clear data before each test
        gameDAO.clear();
        authDAO.clear();
        userDAO.clear();

        // Create a test user and authenticate
        userDAO.createUser(testUser);
        testAuth = new AuthData("testAuthToken", testUser.username());
        authDAO.createAuth(testAuth);
    }

    @Test
    void testCreateGameInvalidAuthToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame("invalidToken","Test Game");
        });
        assertEquals("Unauthorized: Invalid auth token", exception.getMessage());
    }
    @Test
    void testJoinGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame(testAuth.authToken(),"Test Game");

        // Join the game as WHITE
        gameService.joinGame(testAuth.authToken(), gameID, "WHITE");

        GameData game = gameDAO.getGame(gameID);
        assertEquals(testUser.username(), game.whiteUsername(), "The White player shou be 'pp'");
        assertNull(game.blackUsername(), "The Black player should be null");
    }
    @Test
    void testJoinGameSpotAlreadyTaken() throws DataAccessException {
        int gameID = gameService.createGame(testAuth.authToken(),"Test Game");

        // First user joins as WHITE
        gameService.joinGame(testAuth.authToken(), gameID, "WHITE");

        // Create a second user
        UserData secondUser = new UserData("qq", "654321", "qq@gmail.com");
        userDAO.createUser(secondUser);
        AuthData secondAuth = new AuthData("secondAuthToken", secondUser.username());
        authDAO.createAuth(secondAuth);

        // Second user attempts to join WHITE
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(secondAuth.authToken(), gameID, "WHITE");
        });
        assertEquals("Forbidden: WHITE spot is already taken", exception.getMessage());
    }
    @Test
    void testJoinGameInvalidColor() throws DataAccessException {
        int gameID = gameService.createGame(testAuth.authToken(),"Test Game");

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(testAuth.authToken(), gameID, "Red");
        });
        assertEquals("Bad request: Invalid color Choice", exception.getMessage());
    }
    @Test
    void testJoinGameGameDNE() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(testAuth.authToken(), 999, "WHITE");
        });
        assertEquals("Bad request: Game does not exist", exception.getMessage());
    }
    @Test
    void testJoinGameInvalidAuthToken() throws DataAccessException {
        int gameID = gameService.createGame(testAuth.authToken(),"Test Game");

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("invalidToken", gameID, "WHITE");
        });
        assertEquals("Unauthorized: Invalid auth token", exception.getMessage());
    }
    @Test
    void testListGamesSuccess() throws DataAccessException {
        int gameID1 = gameService.createGame(testAuth.authToken(),"Test Game");
        int gameID2 = gameService.createGame(testAuth.authToken(),"Test Game");

        List<GameData> games = gameService.listGames(testAuth.authToken());
        assertNotNull(games, "The games list should not null");
        assertEquals(2, games.size(), "There should be exactly 2 games in the list.");

        // Verify that the game IDs are present in the list
        boolean containsGame1 = games.stream().anyMatch(game -> game.gameID() == gameID1);
        boolean containsGame2 = games.stream().anyMatch(game -> game.gameID() == gameID2);
        assertTrue(containsGame1, "The games list should contain the first created game");
        assertTrue(containsGame2, "The games list should contain the second created game");
    }
    @Test
    void testListGamesInvalidAuthToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.listGames("invalidToken");
        });
        assertEquals("Unauthorized: Invalid auth token", exception.getMessage());
    }
    @Test
    void testClearData() throws DataAccessException {
        int gameId = gameService.createGame(testAuth.authToken(),"Test Game");
        gameService.joinGame(testAuth.authToken(), gameId, "WHITE");

        gameService.clear();

        GameData game = gameDAO.getGame(gameId);
        assertNull(game, "After clearing, the game data should be null");

        List<GameData> games = gameService.listGames(testAuth.authToken());
        assertTrue(games.isEmpty(), "After clearing, the game list should be null");
    }
}