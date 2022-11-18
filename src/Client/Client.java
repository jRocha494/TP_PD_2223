package Client;

import Models.CustomException;
import Models.User;
import Server.Server;

import java.sql.SQLException;
import java.util.ArrayList;

public class Client {
    //TODO: Remove this
    private static Server server = new Server();
    public static void main(String[] args) {

        ArrayList<Thread> threadList = new ArrayList<>();

        ThreadUserInterface tcl = new ThreadUserInterface();
        threadList.add(tcl);
    }

    public static void registerUser(String name, String username, String password) throws CustomException {
        User user = new User(name, username, password);
        server.registerUser(user);
    }
}