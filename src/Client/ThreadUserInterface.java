package Client;

import Models.CustomException;
import utils.InputUtils;

public class ThreadUserInterface extends Thread{
    public ThreadUserInterface() {
        this.start();
    }

    @Override
    public void run(){
        boolean exit = false;
        do{
            switch (InputUtils.chooseOption("What do you pretend to do?",
                    "Signup",
                    "Login",
                    "Exit")) {
                case 1 -> {
                    try {
                        Client.registerUser(
                                    InputUtils.readString("Name: ", false),
                                    InputUtils.readString("Username: ", false),
                                    InputUtils.readString("Password: ", true)
                        );
                    } catch (CustomException e) {
                        InputUtils.logException(e.getMessage(), e.getCause());
                    }
                }
                case 3 -> exit = true;
            }
        } while(!exit);
    }
}
