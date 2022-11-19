package Client;

import utils.errorHandling.CustomException;
import utils.InputUtils;

public class ThreadUserInterface extends Thread{
    public ThreadUserInterface() {
        this.start();
    }

    @Override
    public void run(){
        boolean exit = false;
        do {
            switch (InputUtils.chooseOption("What do you pretend to do?",
                    "Signup",
                    "Login"/*,
                    "Exit"*/)) {
                case 1 -> {
                    try {
                        Client.registerUser(
                                InputUtils.readString("Name: ", false, false),
                                InputUtils.readString("Username: ", true, false),
                                InputUtils.readString("Password: ", false, false)
                        );
                    } catch (CustomException e) {
                        InputUtils.logException(e.getMessage(), e.getCause());
                    }
                }
                case 2 -> {
                    try {
                        Client.authenticateUser(
                                InputUtils.readString("Username", true, false),
                                InputUtils.readString("Password", false, false)
                        );
                        exit = true;
                    }catch (CustomException e){
                        InputUtils.logException(e.getMessage(), e.getCause());
                    }
                }
//                case 3 -> exit = true;
            }
        } while (!exit);

        exit = false;
        do {
            switch (InputUtils.chooseOption("What do you pretend to do?",
                    "Edit login information"/*,
                    "Exit"*/)) {
                case 1 -> {
                    try {
                        System.out.println("Press enter if you don't wish to update a certain field");
                        Client.editLoginData(
                                InputUtils.readString("Name: ", false, true),
                                InputUtils.readString("Username: ", true, true),
                                InputUtils.readString("Password: ", false, true)
                        );
                    } catch (CustomException e) {
                        InputUtils.logException(e.getMessage(), e.getCause());
                    }
                }
            }
        } while (!exit);
    }
}
