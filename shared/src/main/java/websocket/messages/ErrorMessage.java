package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private final String errorMessage;

    public ErrorMessage(ServerMessageType serverMessageType, String errorMessage) {
        super(serverMessageType);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
