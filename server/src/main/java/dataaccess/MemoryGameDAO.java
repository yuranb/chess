package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDAO implements GameDAO{
    private final Map<Integer, GameData> gameDataMap;

    public MemoryGameDAO() {
        this.gameDataMap = new HashMap<>();
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        if (gameDataMap.containsKey(game.gameID())) {
            throw new DataAccessException("Game ID already exists");
        }
        gameDataMap.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDataMap.get(gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(gameDataMap.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!gameDataMap.containsKey(game.gameID())) {
            throw new DataAccessException("Game not found");
        }
        gameDataMap.put(game.gameID(), game);
    }

    @Override
    public void clear() throws DataAccessException {
        gameDataMap.clear();
    }
}
