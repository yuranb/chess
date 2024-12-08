package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    public Session session;

    public Connection(Session session) {
        this.session = session;
    }

    public void send(String message) throws IOException {
        session.getRemote().sendString(message);
    }
}