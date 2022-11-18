package Client;

import Models.User;
import Server.Server;

import java.util.ArrayList;

public class Client {
    //TODO: Remove this
    private static Server server = new Server();
    public static void main(String[] args) {

        ArrayList<Thread> threadList = new ArrayList<>();

        ThreadUserInterface tcl = new ThreadUserInterface();
        threadList.add(tcl);
    }

    public static boolean registerUser(String name, String username, String password){
        User user = new User(username, name, password);
        return server.registerUser(user);
    }

    public static boolean authenticateUser(String username, String password){
        User user = new User(username, password);
        server.authenticateUser(user);
        return true;
    }
}