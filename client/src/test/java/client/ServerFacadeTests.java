package client;

import facade.ServerFacade;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    // Test successful user registration
    @Test
    public void registerPositive() throws Exception {
        Assertions.assertNotNull(facade.register("user1", "password", "email@gmail.com"));
    }

    // Test failed registration with duplicate username
    @Test
    public void registerNegative() throws Exception {
        facade.register("user1", "password", "email@gmail.com");
        Assertions.assertThrows(Exception.class, () -> facade.register("user1", "password", "email@gmail.com"));
    }

    // Test successful login
    @Test
    public void loginPositive() throws Exception {
        facade.register("user1", "password", "email@gmail.com");
        Assertions.assertNotNull(facade.login("user1", "password"));
    }

    // Test failed login with wrong password
    @Test
    public void loginNegative() {
        Assertions.assertThrows(Exception.class, () -> facade.login("user1", "wrongpass"));
    }

    // Test successful logout
    @Test
    public void logoutPositive() throws Exception {
        facade.register("user1", "password", "email@gmail.com");
        Assertions.assertDoesNotThrow(() -> facade.logout());
    }

    // Test failed logout when not logged in
    @Test
    public void logoutNegative() {
        Assertions.assertThrows(Exception.class, () -> facade.logout());
    }

    // Test successful game creation
    @Test
    public void createGamePositive() throws Exception {
        facade.register("user1", "password", "email@gmail.com");
        Assertions.assertNotNull(facade.createGame("game1"));
    }

    // Test failed game creation when not logged in
    @Test
    public void createGameNegative() {
        Assertions.assertThrows(Exception.class, () -> facade.createGame("game1"));
    }

    // Test successful game listing
    @Test
    public void listGamesPositive() throws Exception {
        facade.register("user1", "password", "email@gmail.com");
        facade.createGame("game1");
        Assertions.assertFalse(facade.listGames().isEmpty());
    }

    // Test failed game listing when not logged in
    @Test
    public void listGamesNegative() {
        Assertions.assertThrows(Exception.class, () -> facade.listGames());
    }

    // Test successful game join
    @Test
    public void playGamePositive() throws Exception {
        facade.register("user1", "password", "email@gmail.com");
        GameData game = facade.createGame("game1");
        Assertions.assertDoesNotThrow(() -> facade.playGame(game.gameID(), "WHITE"));
    }

    // Test failed game play when not logged in
    @Test
    public void playGameNegative() {
        Assertions.assertThrows(Exception.class, () -> facade.playGame(1, "WHITE"));
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }
}
