package Client;

import Models.*;
import utils.errorHandling.CustomException;
import utils.InputUtils;
import utils.errorHandling.ResponseMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.valueOf;

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
                    "Edit login information",
                    "Consult Bookings",
                    "Consult Shows"/*,
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
                case 2 -> {
                    int option = InputUtils.chooseOption("Do you want to visualize the bookings with or without confirmed payment?",
                            "With confirmed payment",
                                    "Without confirmed payment");
                    Response response = Client.readBookings(option == 1);
                    System.out.println(response.getMessage().getDescription());
                    List<Booking> bookingList = (List<Booking>) response.getData();
                    System.out.println(Arrays.toString(bookingList.toArray()));
                }
                case 3 -> {
                    System.out.println("Press enter if you don't wish to use a certain filter");
                    Response response = Client.readShows(
                            InputUtils.readString("Description: ", false, true),
                            InputUtils.readString("Type: ", false, true),
                            InputUtils.readString("Data e hora: ", false, true),
                            valueOf(InputUtils.readInt("Duration: ")),
                            InputUtils.readString("Local: ", false, true),
                            InputUtils.readString("Place: ", false, true),
                            InputUtils.readString("Country: ", false, true),
                            InputUtils.readString("Age Rating: ", false, true)
                    );
                    System.out.println(response.getMessage().getDescription());
                    List<Show> showList = (List<Show>) response.getData();
                    System.out.println(Arrays.toString(showList.toArray()));


                    ClientSelectionData csd = new ClientSelectionData();
                    while (InputUtils.chooseOption("Do you want to select a certain show?", "Yes", "No") == 1 || csd.getSelectedShow() == null) {
                        response = Client.selectShow(InputUtils.readInt("Insert the Id of the Show you want to select: "));
                        System.out.println(response.getMessage().getDescription());
                        if(response.getMessage().getCode() == 200){
                            csd.setSelectedShow((Show) response.getData());
                            List<Seat> freeSeatList = (List<Seat>) Client.readShowFreeSeats(csd.getSelectedShow().getId()).getData();
                            Collections.sort(freeSeatList);
                            System.out.println("Available Seats:\n" + Arrays.toString(freeSeatList.toArray()));
                            break;
                        }
                    }

                    while (InputUtils.chooseOption("Do you want to add a seat to your booking?", "Yes", "No") == 1 &&
                            (csd.getSelectedSeats().isEmpty() || csd.getSelectedShow() != null)){
                            response = Client.selectSeat(
                                    InputUtils.readString("Insert the Row of the Seat you want to select: ", true, false),
                                    InputUtils.readString("Insert the the Seat you want to select: ", true, false),
                                    csd.getSelectedShow().getId()
                            );
                            System.out.println(response.getMessage().getDescription());
                            if(response.getMessage().getCode() == 200){
                                csd.addSeat((Seat) response.getData());
                                break;
                            }
                    }

                    if(InputUtils.chooseOption("Do you want to finish your booking request?", "Yes", "No") == 1 ||
                            (csd.getSelectedSeats().isEmpty() && csd.getSelectedShow() != null)){
                        System.out.println("Details:\n" + Booking.fullDetails(csd.getSelectedShow(), csd.getSelectedSeats()));
                        if(InputUtils.chooseOption("Do you confirm these details and request a new Booking?", "Yes", "No") == 1){
                            response = Client.confirmBooking(csd.getSelectedShow().getId(),csd.getSelectedSeats());
                            System.out.println(response.getMessage().getDescription());
                            if(response.getMessage().getCode()==200 &&
                            InputUtils.chooseOption("Do you want to proceed to the Booking Payment?", "Yes", "No") == 1){

                            }
                        }
                    }
                }
            }
        } while (!exit);
    }
}
