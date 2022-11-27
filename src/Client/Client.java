package Client;

import Models.*;
import utils.InputUtils;
import utils.errorHandling.CustomException;
import Server.Server;
import utils.errorHandling.ResponseMessage;

import java.util.*;
import java.util.stream.Stream;

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

    public static Response readBookings(boolean withConfirmedPayment) {
        return server.readBookings(withConfirmedPayment);
    }

    public static Response readShows(String description, String type, String dateTime, String duration, String local, String place, String country, String ageRating) {
        HashMap<String, String> filtersMap = new HashMap<String, String>();
        if (!description.isBlank())
            filtersMap.put("descricao", description);
        if (!type.isBlank())
            filtersMap.put("tipo", type);
        if (!dateTime.isBlank())
            filtersMap.put("data_hora", dateTime);
        if (!duration.isBlank())
            filtersMap.put("duracao", duration);
        if (!local.isBlank())
            filtersMap.put("local", local);
        if (!place.isBlank())
            filtersMap.put("localidade", place);
        if (!country.isBlank())
            filtersMap.put("pais", country);
        if (!ageRating.isBlank())
            filtersMap.put("classificacao_etaria", ageRating);

        return server.readShows(filtersMap);
    }

    public static Response selectShow(int chosenId){
        return server.selectShow(chosenId);
    }

    public static Response readShowFreeSeats(int chosenId){
        return server.readShowFreeSeats(chosenId);
    }

    public static Response selectSeat(String chosenRow, String chosenSeat, int showId){
        return server.selectSeat(chosenRow, chosenSeat, showId);
    }

    public static Response confirmBooking(int selectedShow, List<Seat> selectedSeats) {
        return server.confirmBooking(selectedShow, selectedSeats, currentUser.getId());
    }
}