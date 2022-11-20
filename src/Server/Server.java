package Server;

import Models.Response;
import utils.errorHandling.CustomException;
import Models.User;
import Server.jdbc.ConnDB;
import utils.errorHandling.ResponseMessage;

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
    public Response registerUser(User user) {
        try {
            connDB.createUser(user.getName(), user.getUsername(), user.getPassword());
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, null);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error registering user", e);
        }
    }

    public Response authenticateUser(User user) {
        try {
            User userAux = connDB.authenticateUser(user.getUsername(), user.getPassword());
            if(userAux == null)
                return new Response(ResponseMessage.USER_NOT_FOUND, null);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, userAux);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error authenticating user", e);
        }
    }

    public Response editLoginData(int id, HashMap<String, String> editLoginMap) {
        try {
            connDB.updateUser(id, editLoginMap);
            return new Response(ResponseMessage.O_PEDRO_E_PARVO, null);
        } catch (SQLException e) {
            return new Response(ResponseMessage.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }
}
