package Client;

import Data.ServerData;
import Data.ServerPersistentData;
import utils.Message;
import utils.MessageEnum;
import utils.Response;
import Data.User;
import Models.*;
import utils.InputUtils;
import utils.errorHandling.CustomException;
import Server.Server;
import utils.ResponseMessageEnum;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.stream.Stream;

public class Client {
    private static User currentUser;
    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length != 2){
            System.out.println("Missing server port and server ip");
            return;
        }

        try(DatagramSocket ds = new DatagramSocket()) {
            Message msg = new Message(MessageEnum.MSG_CONNECT_SERVER.getMessage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(msg);

            byte[] msgToSend = baos.toByteArray();
            DatagramPacket dpSend = new DatagramPacket(
                    msgToSend,
                    msgToSend.length,
                    InetAddress.getByName(args[1]),
                    Integer.parseInt(args[0])
            );

            System.out.println("Sending to server");
            ds.send(dpSend);


            DatagramPacket dpRec = new DatagramPacket(new byte[256], 256);
            ds.receive(dpRec);

            ByteArrayInputStream bais = new ByteArrayInputStream(dpRec.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Response msgRec = (Response) ois.readObject();

            Map<Integer, ServerData> runningServers = (Map) msgRec.getData();
            Map.Entry<Integer, ServerData> entry = runningServers.entrySet().iterator().next();
            Socket socket = new Socket(entry.getValue().getIp(), entry.getValue().getPort());
            // TODO: send commands to server
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
//
//        ArrayList<Thread> clientThreadList = new ArrayList<>();
//
//        ThreadUserInterface tcl = new ThreadUserInterface();
//        clientThreadList.add(tcl);
//
//        for (Thread t : clientThreadList) // waits thread to conclude execution
//            t.join();
    }

    public static ResponseMessageEnum registerUser(String name, String username, String password) {
        User user = new User(name, username, password);
        return Server.registerUser(user).getMessage();
    }

    public static ResponseMessageEnum authenticateUser(String username, String password) {
        User user = new User(username, password);
        Response response = Server.authenticateUser(user);
        currentUser = (User) response.getData();
        return response.getMessage();
    }

    public static ResponseMessageEnum editLoginData(String name, String username, String password) {
        HashMap<String, String> editLoginMap = new HashMap<String, String>();
        if (!name.isBlank())
            editLoginMap.put("nome", name);
        if (!username.isBlank())
            editLoginMap.put("username", username);
        if (!password.isBlank())
            editLoginMap.put("password", password);
        return Server.editLoginData(currentUser.getId(), editLoginMap).getMessage();
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

    public static Response deleteBooking(int selectedBooking) {
        return server.deleteBooking(selectedBooking, currentUser.getId());
    }

    public static Response payBooking(int selectedBooking) {
        return server.payBooking(selectedBooking, currentUser.getId());
    }

    public static Response makeShowVisible(int selectedShow) {
        return server.makeShowVisible(selectedShow);
    }
}