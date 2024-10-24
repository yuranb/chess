package service;

import dataaccess.*;
import model.*;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }
public AuthData createUser(UserData userData) throws DataAccessException {
    // Check if the username already exists
    UserData existingUser = userDAO.getUser(userData.username());
    if (existingUser != null) {
        throw new DataAccessException("Forbidden: Username already taken");
    }

    userDAO.createUser(userData);

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, userData.username());

    authDAO.createAuth(authData);

    return authData;
}

public AuthData loginUser(UserData userData) throws DataAccessException {
    // Authenticate the user
    boolean isAuthenticated = userDAO.authenticateUser(userData.username(), userData.password());
    if (!isAuthenticated) {
        throw new DataAccessException("Unauthorized: Invalid username or password");
    }

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, userData.username());

    // Save the authToken
    authDAO.createAuth(authData);

    return authData;
}
public void logoutUser(String authToken) throws DataAccessException {
    // Verify that the authToken exists
    AuthData authData = authDAO.getAuth(authToken);
    if (authData == null) {
        throw new DataAccessException("Error: Invalid auth token");
    }

    authDAO.deleteAuth(authToken);
}

public void clear() throws DataAccessException {
    userDAO.clear();
    authDAO.clear();
}
}