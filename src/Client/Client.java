package Client;

import Data.Seat;
import Data.ServerData;
import utils.Request;
import utils.RequestEnum;
import utils.Response;
import Data.User;
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

import static Data.Utils.*;

public class Client {
    private static User currentUser;
    private static Socket socket = null;
    private static ObjectInputStream _ois;
    private static ObjectOutputStream _oos;
    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length != 2){
            System.out.println("Missing server port and server ip");
            return;
        }

        try(DatagramSocket ds = new DatagramSocket()) {
            Request msg = new Request(RequestEnum.MSG_CONNECT_SERVER, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeUnshared(msg);

            byte[] msgToSend = baos.toByteArray();
            DatagramPacket dpSend = new DatagramPacket(
                    msgToSend,
                    msgToSend.length,
                    InetAddress.getByName(args[1]),
                    Integer.parseInt(args[0])
            );

            ds.send(dpSend);

            DatagramPacket dpRec = new DatagramPacket(new byte[MAX_BYTES], MAX_BYTES);
            ds.receive(dpRec);

            ByteArrayInputStream bais = new ByteArrayInputStream(dpRec.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Response msgRec = (Response) ois.readObject();

            List<ServerData> runningServers = (List<ServerData>) msgRec.getResponseData();
            ServerData entry = runningServers.get(0);
            System.out.println("Connecting to: " + entry.getIp() + ":" + entry.getPort());
            socket = new Socket(entry.getIp(), entry.getPort());
            _oos = new ObjectOutputStream(socket.getOutputStream());
            _ois = new ObjectInputStream(socket.getInputStream());

            ArrayList<Thread> clientThreadList = new ArrayList<>();
            ThreadUserInterface tcl = new ThreadUserInterface();
            clientThreadList.add(tcl);

            for (Thread t : clientThreadList) // waits thread to conclude execution
                t.join();
            socket.close();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Response registerUser(String name, String username, String password) {
        User user = new User(name, username, password);
        Request request = new Request(RequestEnum.REQUEST_REGISTER_USER, user);
        return sendRequest(request);
    }

    public static Response authenticateUser(String username, String password) {
        User user = new User(username, password);
        Request request = new Request(RequestEnum.REQUEST_AUTHENTICATE_USER, user);
        Response response = sendRequest(request);
        currentUser = (User) response.getResponseData();
        return response;
    }

    public static Response editLoginData(String name, String username, String password) {
        HashMap<String, String> editLoginMap = new HashMap<>();
        if (!name.isBlank())
            editLoginMap.put("nome", name);
        if (!username.isBlank())
            editLoginMap.put("username", username);
        if (!password.isBlank())
            editLoginMap.put("password", password);
        ArrayList data = new ArrayList();
        data.add(currentUser.getId());
        data.add(editLoginMap);
        Request request = new Request(RequestEnum.REQUEST_EDIT_LOGIN_DATA, data);
        return sendRequest(request);
    }

    public static Response readBookings(boolean withConfirmedPayment) {
        Request request = new Request(RequestEnum.REQUEST_READ_BOOKINGS, withConfirmedPayment);
        return sendRequest(request);
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

        Request request = new Request(RequestEnum.REQUEST_READ_SHOWS, filtersMap);
        return sendRequest(request);
    }

    public static Response selectShow(int chosenId){
        Request request = new Request(RequestEnum.REQUEST_SELECT_SHOW, chosenId);
        return sendRequest(request);
    }

    public static Response readShowAvailableSeats(int chosenId){
        Request request = new Request(RequestEnum.REQUEST_READ_SHOW_AVAILABLE_SEATS, chosenId);
        return sendRequest(request);
    }

    public static Response selectSeat(String chosenRow, String chosenSeat, int showId){
        ArrayList data = new ArrayList();
        data.add(chosenRow);
        data.add(chosenSeat);
        data.add(showId);
        Request request = new Request(RequestEnum.REQUEST_SELECT_SEAT, data);
        return sendRequest(request);
    }

    public static Response confirmBooking(int selectedShow, List<Seat> selectedSeats) {
        ArrayList data = new ArrayList();
        data.add(selectedShow);
        data.add(selectedSeats);
        data.add(currentUser.getId());
        Request request = new Request(RequestEnum.REQUEST_CONFIRM_BOOKING, data);
        return sendRequest(request);
    }

    public static Response deleteBooking(int selectedBooking) {
        ArrayList data = new ArrayList();
        data.add(selectedBooking);
        data.add(currentUser.getId());
        Request request = new Request(RequestEnum.REQUEST_DELETE_BOOKING, data);
        return sendRequest(request);
    }

    public static Response payBooking(int selectedBooking) {
        ArrayList data = new ArrayList();
        data.add(selectedBooking);
        data.add(currentUser.getId());
        Request request = new Request(RequestEnum.REQUEST_PAY_BOOKING, data);
        return sendRequest(request);
    }

    public static Response makeShowVisible(int selectedShow) {
        Request request = new Request(RequestEnum.REQUEST_MAKE_SHOW_VISIBLE, selectedShow);
        return sendRequest(request);
    }

    public static Response deleteShow(int selectedShow) {
        Request request = new Request(RequestEnum.REQUEST_DELETE_SHOW, selectedShow);
        return sendRequest(request);
    }

    public static Response logout() {
        Request request = new Request(RequestEnum.REQUEST_LOGOUT, currentUser);
        return sendRequest(request);
    }

    public static Response sendRequest(Request request){
        try{
            _oos.writeUnshared(request);
            return (Response) _ois.readObject();
        } catch (IOException e) {
            return new Response(ResponseMessageEnum.FAILED_DEPENDENCY, null);
        } catch (ClassNotFoundException e) {
            return new Response(ResponseMessageEnum.UNEXPECTED_DATA, null);
        }
    }
}