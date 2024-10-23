package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO{
    private Map<String, AuthData> authMap;
    public MemoryAuthDAO() {
        authMap = new HashMap<>();
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authMap.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return  authMap.get(authToken);    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authMap.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        authMap.clear();
    }
}
