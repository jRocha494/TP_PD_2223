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
                "Logic")){
            case 1 -> { while(!Client.registerUser(
                    InputUtils.readString("Nome", false),
                    InputUtils.readString("Username", false),
                    InputUtils.readString("Password", true)
            )); }
        }
    }
}
