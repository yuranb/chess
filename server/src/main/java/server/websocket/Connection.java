package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    public Session session;
    public String username;
    public Integer gameID;

    public Connection(String username, Integer gameID, Session session) {
        this.username = username;
        this.gameID = gameID;
        this.session = session;
    }

    public void send(String message) throws IOException {
        session.getRemote().sendString(message);
    }
}