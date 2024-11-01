package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest {

    private static SQLAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        authDAO.clear();
    }

    @Test
    public void testCreateAuthSuccess() throws DataAccessException {
        VerifyAuth("token123", "pp");
    }

    @Test
    public void testCreateAuthFailure() throws DataAccessException {
        AuthData authData1 = new AuthData("token123", "pp");
        AuthData authData2 = new AuthData("token123", "pp2");

        authDAO.createAuth(authData1);
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(authData2),
                "Error: they are duplicate authToken");
    }

    @Test
    public void testGetAuthSuccess() throws DataAccessException {
        VerifyAuth("token123", "pp");
    }

    @Test
    public void testGetAuthFailure() throws DataAccessException {
        AuthData retrievedAuth = authDAO.getAuth("nonExistentToken");
        assertNull(retrievedAuth, "non-existent authToken");
    }

    @Test
    public void testDeleteAuthSuccess() throws DataAccessException {
        AuthData authData = new AuthData("token123", "pp");
        authDAO.createAuth(authData);
        authDAO.deleteAuth("token123");

        AuthData retrievedAuth = authDAO.getAuth("token123");
        assertNull(retrievedAuth, "auth data be deleted");
    }

    @Test
    public void testDeleteAuthFailure() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonExistentToken"),
                "failed to delete auth");
    }

    @Test
    public void testClear() throws DataAccessException {
        AuthData authData = new AuthData("token123", "pp");
        authDAO.createAuth(authData);
        authDAO.clear();

        AuthData retrievedAuth = authDAO.getAuth("token123");
        assertNull(retrievedAuth, "data be cleared");
    }

    private void VerifyAuth(String token, String username) throws DataAccessException {
        AuthData authData = new AuthData(token, username);
        authDAO.createAuth(authData);

        AuthData retrievedAuth = authDAO.getAuth(token);
        assertNotNull(retrievedAuth);
        assertEquals(token, retrievedAuth.authToken());
        assertEquals(username, retrievedAuth.username());
    }
}
