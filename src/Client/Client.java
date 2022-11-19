package Client;

import utils.errorHandling.CustomException;
import Models.User;
import Server.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class Client {
    //TODO: Remove this
    private static Server server = new Server();
    private static User currentUser;
    public static void main(String[] args) {

        ArrayList<Thread> threadList = new ArrayList<>();

        ThreadUserInterface tcl = new ThreadUserInterface();
        threadList.add(tcl);
    }

    public static void registerUser(String name, String username, String password) throws CustomException {
        User user = new User(name, username, password);
        server.registerUser(user);
    }

    public static void authenticateUser(String username, String password) throws CustomException {
        User user = new User(username, password);
        currentUser = server.authenticateUser(user);
    }

    public static void editLoginData(String name, String username, String password) throws CustomException {
        HashMap<String, String> editLoginMap = new HashMap<String, String>();
        if (!name.isBlank())
            editLoginMap.put("nome", name);
        if (!username.isBlank())
            editLoginMap.put("username", username);
        if (!password.isBlank())
            editLoginMap.put("password", password);
        server.editLoginData(currentUser.getId(), editLoginMap);
    }
}