package dataaccess;

import model.UserData;

import java.util.HashSet;
import java.util.Set;

public class MemoryUserDAO implements UserDAO{
    private Set<UserData> userSet;
    public MemoryUserDAO() {
        userSet = new HashSet<>();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (userSet.contains(user)) {
            throw new DataAccessException("User already exists");
        }
        userSet.add(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData user : userSet) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
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
        userSet.clear();
    }
}
