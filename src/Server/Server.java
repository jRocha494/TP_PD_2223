package Server;

import Models.CustomException;
import Models.User;
import Server.jdbc.ConnDB;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Server {
    private ConnDB connDB;
    public Server() {
        try {
            this.connDB = new ConnDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerUser(User user) throws CustomException {
        try {
            connDB.createUser(user.getName(), user.getUsername(), user.getPassword());
        } catch (SQLException e) {
            throw new CustomException("Error registering user", e);
        }
        finally {
            try {
                connDB.close();
            } catch (SQLException e) {
                throw new CustomException("Error closing dependencies", e);
            }
        }
    }
}
