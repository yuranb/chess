package websocket.messages;

public class NotificationMessage extends ServerMessage {
    private final String message;

    public NotificationMessage(ServerMessageType serverMessageType, String message) {
        super(serverMessageType);
        this.message = message;
    }

    public String getContent() {
        return message;
    }
}
