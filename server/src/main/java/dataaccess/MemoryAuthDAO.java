package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO{
    private final Map<String, AuthData> authTokenMap;
    public MemoryAuthDAO() {
        this.authTokenMap = new HashMap<>();
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authTokenMap.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return  authTokenMap.get(authToken);    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokenMap.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        authTokenMap.clear();
    }
}
