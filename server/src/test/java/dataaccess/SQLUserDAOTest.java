package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {

    private SQLUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    @Test
    public void testCreateUserSuccess() throws DataAccessException {
        UserData user = new UserData("pp", "123456", "pp@gmail.com");
        userDAO.createUser(user);

        UserData retrievedUser = userDAO.getUser("pp");
        assertNotNull(retrievedUser);
        assertEquals("pp", retrievedUser.username());
        assertEquals("pp@gmail.com", retrievedUser.email());
        assertNotNull(retrievedUser.password());
    }

    @Test
    public void testCreateUserFailure() throws DataAccessException {
        UserData user1 = new UserData("pp", "123456", "pp@gmail.com");
        UserData user2 = new UserData("pp", "654321", "pp2@gmail.com");

        userDAO.createUser(user1);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user2),
                "Error: they are duplicate username");
    }

    @Test
    public void testGetUserSuccess() throws DataAccessException {
        UserData user = new UserData("pp", "123456", "pp@gmail.com");
        userDAO.createUser(user);

        UserData retrievedUser = userDAO.getUser("pp");
        assertNotNull(retrievedUser);
        assertEquals("pp", retrievedUser.username());
        assertEquals("pp@gmail.com", retrievedUser.email());
    }
    @Test
    public void testGetUserFailure() throws DataAccessException {
        UserData retrievedUser = userDAO.getUser("nonExistentUser");
        assertNull(retrievedUser, "Expected null user");
    }

    @Test
    public void testAuthenticateUserSuccess() throws DataAccessException {
        UserData user = new UserData("pp", "123456", "pp@gmail.com");
        userDAO.createUser(user);

        boolean isAuthenticated = userDAO.authenticateUser("pp", "123456");
        assertTrue(isAuthenticated, "correct credentials");
    }

    @Test
    public void testAuthenticateUserFailure() throws DataAccessException {
        UserData user = new UserData("pp", "123456", "pp@gmail.com");
        userDAO.createUser(user);

        boolean isAuthenticated = userDAO.authenticateUser("pp", "wrongPassword");
        assertFalse(isAuthenticated, "incorrect password");
    }

    @Test
    public void testClear() throws DataAccessException {
        UserData user = new UserData("pp", "123456", "pp@gmail.com");
        userDAO.createUser(user);
        userDAO.clear();

        UserData retrievedUser = userDAO.getUser("pp");
        assertNull(retrievedUser, "user data be cleared");
    }
}
