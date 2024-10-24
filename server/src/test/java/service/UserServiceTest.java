package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    UserData user = new UserData("pp", "123456", "pp@gmail.com");
    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);

        // Clear data before each test
        userDAO.clear();
        authDAO.clear();
    }

    @Test
    void testCreateUser_Success() throws DataAccessException {
        AuthData auth = userService.createUser(user);

        assertNotNull(auth);
        assertEquals("pp", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void testCreateUser_ExistsUser() throws DataAccessException {
        userService.createUser(user);

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.createUser(user);
        });
        assertEquals("Forbidden: Username already taken", exception.getMessage());
    }

    @Test
    void testLoginUser_Success() throws DataAccessException {
        userService.createUser(user);

        AuthData auth = userService.loginUser(new UserData("pp", "123456",  null));

        assertNotNull(auth);
        assertEquals("pp", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void testLoginUser_InvalidCredentials() {

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.loginUser(user);
        });
        assertEquals("Unauthorized: Invalid username or password", exception.getMessage());
    }

    @Test
    void testLogoutUser_Success() throws DataAccessException {
        AuthData auth = userService.createUser(user);

        userService.logoutUser(auth.authToken());

        AuthData retrievedAuth = authDAO.getAuth(auth.authToken());
        assertNull(retrievedAuth);
    }


    @Test
    void testClearData() throws DataAccessException {
        userService.createUser(user);

        userService.clear();

        // Verify auth data has been cleared
        UserData retrievedUser = userDAO.getUser("frank");
        assertNull(retrievedUser);

        AuthData authData = authDAO.getAuth("any-token");
        assertNull(authData);
    }
}