package ui;

import com.google.gson.Gson;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketCommunicator extends Endpoint {
    private Session session;
    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    public WebSocketCommunicator(String baseUrl, ServerMessageObserver observer) {
        this.observer = observer;
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(baseUrl.replace("http", "ws") + "/ws"));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to connect to WebSocket", ex);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        session.addMessageHandler(String.class, this::handleMessage);
    }

    private void handleMessage(String message) {
        ServerMessage baseMessage = gson.fromJson(message, ServerMessage.class);
        switch (baseMessage.getServerMessageType()) {
            case NOTIFICATION -> {
                NotificationMessage notif = gson.fromJson(message, NotificationMessage.class);
                observer.notify(notif);
            }
            case ERROR -> {
                ErrorMessage err = gson.fromJson(message, ErrorMessage.class);
                observer.notify(err);
            }
            case LOAD_GAME -> {
                LoadGameMessage load = gson.fromJson(message, LoadGameMessage.class);
                observer.notify(load);
            }
            default -> System.out.println(" unknown message type");
        }
    }

    public void sendMessage(String json) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to send message", ex);
            }
        } else {
            throw new IllegalStateException("Session not connected");
        }
    }

    public void sendCommand(UserGameCommand command) {
        if (session != null && session.isOpen()) {
            try {
                String jsonCommand = gson.toJson(command);
                session.getBasicRemote().sendText(jsonCommand);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to send command", ex);
            }
        } else {
            throw new IllegalStateException("Session not connected");
        }
    }

    public void close() {
        if (session != null) {
            try {
                session.close();
            } catch (IOException ex) {
                System.err.println("Failed to close session: " + ex.getMessage());
            }
        }
    }
}
