package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, ArrayList<Connection>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameId, Session session) {
        connections.computeIfAbsent(gameId, k -> new ArrayList<>()).add(new Connection(session));
    }

    public void remove(Integer gameId) {
        connections.remove(gameId);
    }

    public void broadcast(Integer gameId, String message) throws IOException {
        var gameConnections = connections.get(gameId);
        if (gameConnections != null) {
            var removeList = new ArrayList<Connection>();
            for (var connection : gameConnections) {
                if (connection.session.isOpen()) {
                    connection.send(message);
                } else {
                    removeList.add(connection);
                }
            }
            gameConnections.removeAll(removeList);
            if (gameConnections.isEmpty()) {
                connections.remove(gameId);
            }
        }
    }

    public void cleanUpClosedConnections() {
        for (var gameId : connections.keySet()) {
            var gameConnections = connections.get(gameId);
            if (gameConnections != null) {
                gameConnections.removeIf(connection -> !connection.session.isOpen());
                if (gameConnections.isEmpty()) {
                    connections.remove(gameId);
                }
            }
        }
    }
}