package Server;

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

    public boolean registerUser(User user){
        try {
            connDB.createUser(user.getName(), user.getUsername(), user.getPassword());
            connDB.readUser();
        } catch (SQLException e) {
            return false;
//            throw new RuntimeException(e);
        }
        try {
            connDB.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
