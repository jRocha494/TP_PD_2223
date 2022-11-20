package Client;

import Models.Response;
import utils.errorHandling.CustomException;
import Models.User;
import Server.Server;
import utils.errorHandling.ResponseMessage;

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

    public static ResponseMessage registerUser(String name, String username, String password) {
        User user = new User(name, username, password);
        return server.registerUser(user).getMessage();
    }

    public static ResponseMessage authenticateUser(String username, String password) {
        User user = new User(username, password);
        Response response = server.authenticateUser(user);
        currentUser = (User) response.getData();
        return response.getMessage();
    }

    public static ResponseMessage editLoginData(String name, String username, String password) {
        HashMap<String, String> editLoginMap = new HashMap<String, String>();
        if (!name.isBlank())
            editLoginMap.put("nome", name);
        if (!username.isBlank())
            editLoginMap.put("username", username);
        if (!password.isBlank())
            editLoginMap.put("password", password);
        return server.editLoginData(currentUser.getId(), editLoginMap).getMessage();
    }
}