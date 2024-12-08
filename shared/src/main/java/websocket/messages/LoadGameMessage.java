package websocket.messages;
import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
    private final ChessGame game;

    public LoadGameMessage(ServerMessageType serverMessageType, ChessGame game) {
        super(serverMessageType);
        this.game = game;
    }

    public ChessGame getGame() {
        return game;
    }
}
