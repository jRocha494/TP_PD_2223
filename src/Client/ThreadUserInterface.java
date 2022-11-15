package Client;

import utils.InputUtils;

public class ThreadUserInterface extends Thread{
    public ThreadUserInterface() {
        this.start();
    }

    @Override
    public void run(){
        switch(InputUtils.chooseOption("What do you pretend to do?",
                "Signup",
                "Login")){
            case 1 -> { while(!Client.registerUser(
                    InputUtils.readString("Name", false),
                    InputUtils.readString("Username", false),
                    InputUtils.readString("Password", true)
            )); }
        }
    }
}
