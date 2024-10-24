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
    void testListGames_Success() throws DataAccessException {
        // Create some games
        int gameID1 = gameService.createGame(testAuth.authToken());
        int gameID2 = gameService.createGame(testAuth.authToken());

        // List games
        List<GameData> games = gameService.listGames(testAuth.authToken());
        assertNotNull(games, "Games list should not be null");
        assertEquals(2, games.size(), "There should be 2 games in the list");
    }
}