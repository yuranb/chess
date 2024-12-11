package websocket.commands;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;

    private final String authToken;

    private final Integer gameID;

    private final Role role;
    private final String playerColor;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this(commandType, authToken, gameID, null, null);
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, Role role, String playerColor) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.role = role;
        this.playerColor = playerColor;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public enum Role {
        OBSERVER,
        PLAYER
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public Role getRole() {
        return role;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGameCommand)) return false;
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID()) &&
                getRole() == that.getRole() &&
                Objects.equals(getPlayerColor(), that.getPlayerColor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID(), getRole(), getPlayerColor());
    }
}
