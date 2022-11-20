package Client;

import utils.errorHandling.CustomException;
import utils.InputUtils;
import utils.errorHandling.ResponseMessage;

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
                    ResponseMessage response = Client.registerUser(
                            InputUtils.readString("Name: ", false, false),
                            InputUtils.readString("Username: ", true, false),
                            InputUtils.readString("Password: ", false, false)
                    );
                    System.out.println(response.getDescription());
                }
                case 2 -> {
                    ResponseMessage response = Client.authenticateUser(
                            InputUtils.readString("Username", true, false),
                            InputUtils.readString("Password", false, false)
                    );
                    System.out.println(response.getDescription());
                    if(response.getCode() == 200)
                        exit = true;
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
                    System.out.println("Press enter if you don't wish to update a certain field");
                    ResponseMessage response = Client.editLoginData(
                            InputUtils.readString("Name: ", false, true),
                            InputUtils.readString("Username: ", true, true),
                            InputUtils.readString("Password: ", false, true)
                    );
                    System.out.println(response.getDescription());
                }
            }
        } while (!exit);
    }
}
