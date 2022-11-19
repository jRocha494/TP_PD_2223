package Server;

import utils.errorHandling.CustomException;
import Models.User;
import Server.jdbc.ConnDB;
import utils.errorHandling.Errors;

import java.sql.SQLException;
import java.util.HashMap;

public class Server {
    private ConnDB connDB;
    public Server() {
        try {
            this.connDB = new ConnDB();
            connDB.initializeDatabase();
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
    }

    public User authenticateUser(User user) throws CustomException {
        try {
            return connDB.authenticateUser(user.getUsername(), user.getPassword());
        } catch (SQLException e) {
            throw new CustomException("Error authenticating user", e);
        }
    }

    public void editLoginData(int id, HashMap<String, String> editLoginMap) throws CustomException {
        try {
            connDB.updateUser(id, editLoginMap);
        } catch (SQLException e) {
            throw new CustomException("Error editing user's data", e);
        }
    }
}
