package dataaccess;

import model.*;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        String username = user.username();
        String password = user.password();
        String email = user.email();

        if (username == null || username.isBlank()) {
            throw new DataAccessException("Invalid username");
        }
        if (password == null || password.isBlank()) {
            throw new DataAccessException("Invalid password");
        }

        String hashedPassword = hashPassword(password);
        DatabaseManager.executeUpdate(statement, username, hashedPassword, email);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean passwordMatches(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statement = "SELECT username, password, email FROM user WHERE username=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to read data:" + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        UserData user = getUser(username);
        if (user == null) {
            return false;
        }
        return passwordMatches(password, user.password());
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE user";
        DatabaseManager.executeUpdate(statement);
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user (
                username varchar(256) NOT NULL,
                password varchar(256) NOT NULL,
                email varchar(256),
                PRIMARY KEY (username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """
    };
}
