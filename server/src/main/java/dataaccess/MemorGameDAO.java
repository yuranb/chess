package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemorGameDAO implements GameDAO{
    private Map<Integer, GameData> gameMap;
    private int gameIDCounter;

    public void MemoryGameDAO() {
        gameMap = new HashMap<>();
        gameIDCounter = 1;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        game = new GameData(gameIDCounter++, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        gameMap.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameMap.get(gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(gameMap.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!gameMap.containsKey(game.gameID())) {
            throw new DataAccessException("Game not found");
        }
        gameMap.put(game.gameID(), game);
    }

    @Override
    public void clear() throws DataAccessException {
        gameMap.clear();
        gameIDCounter = 1;
    }
}
