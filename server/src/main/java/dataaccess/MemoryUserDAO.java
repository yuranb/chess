package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO{
    private final Map<String , UserData> userMap;
    public MemoryUserDAO() {
        this.userMap = new HashMap<>();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (userMap.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        userMap.put(user.username(),user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userMap.get(username);
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        UserData user = getUser(username);
        if (user != null && user.password().equals(password)) {
            return true;
        }
        return false;
    }

    @Override
    public void clear() throws DataAccessException {
        userMap.clear();
    }
}
