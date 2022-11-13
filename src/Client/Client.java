package Client;

import java.util.ArrayList;

public class Client {
    public static void main(String[] args) {
        ArrayList<Thread> threadList = new ArrayList<>();

        ThreadUserInterface tcl = new ThreadUserInterface();
        threadList.add(tcl);
    }

    public static boolean registerUser(String nome, String username, String password){
        return true;
    }
}