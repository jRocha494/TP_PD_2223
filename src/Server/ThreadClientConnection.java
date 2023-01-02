package Server;

import Data.ServerPersistentData;
import Data.User;
import Server.rmi_service.rmi.RemoteObservable;
import utils.Request;
import utils.RequestEnum;
import utils.Response;
import utils.ResponseMessageEnum;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

import static Data.Utils.*;

public class ThreadClientConnection extends Thread{
    static ServerPersistentData serverPersistentData = ServerPersistentData.getInstance();
    RemoteObservable remoteObservable;
    int port;
    MulticastSocket ms;
    public ThreadClientConnection(int port, MulticastSocket ms, RemoteObservable remoteObservable) {
        this.port = port;
        this.ms = ms;
        this.remoteObservable = remoteObservable;
        this.start();
    }

    @Override
    public void run(){
        AcceptClients ac = new AcceptClients(port, ms, remoteObservable);

        try (DatagramSocket ds = new DatagramSocket(port)){
            while (!Thread.currentThread().isInterrupted()){
                DatagramPacket dpRec = new DatagramPacket(new byte[MAX_BYTES], MAX_BYTES);
                ds.receive(dpRec);

                ByteArrayInputStream bais = new ByteArrayInputStream(dpRec.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Request msgRec = (Request) ois.readObject();

                if (msgRec.getRequestMessage().equals(RequestEnum.MSG_CONNECT_SERVER)){
                    remoteObservable.notifyClientConnectionAttempt(Server.getServerData());
                    Response msgResp = new Response(ResponseMessageEnum.SUCCESS, ServerPersistentData.getInstance().getServersList());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeUnshared(msgResp);

                    byte[] msgToSend = baos.toByteArray();
                    InetAddress ipClient = dpRec.getAddress();
                    int portClient = dpRec.getPort();
                    DatagramPacket dpSend = new DatagramPacket(
                            msgToSend,
                            msgToSend.length,
                            ipClient,
                            portClient
                    );

                    System.out.println("Sending running servers list to client");
                    ds.send(dpSend);
                }
            }
        } catch (SocketException e) {
            System.out.println("Error creating resources to await client's connections");
        } catch (IOException e) {
            System.out.println("Error getting data from client");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Error reading data received from client");
        }finally {
            try {
                ac.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class AcceptClients extends Thread {
        int port;
        MulticastSocket ms;
        ArrayList<Thread> clientCommunicationThread;
        RemoteObservable remoteObservable;

        public AcceptClients(int port, MulticastSocket ms, RemoteObservable remoteObservable) {
            this.port = port;
            this.ms = ms;
            this.clientCommunicationThread = new ArrayList<>();
            this.remoteObservable = remoteObservable;
            this.start();
        }

        @Override
        public void run() {
            try (ServerSocket ss = new ServerSocket(port);){
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = ss.accept();
                    CommunicateWithClient cwc = new CommunicateWithClient(socket, port, ms, remoteObservable);
                    clientCommunicationThread.add(cwc);
                    Server.getServerData().incrementNmrConnections();
                    remoteObservable.notifyClientAcception(Server.getServerData());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                // waits threads to conclude execution
                for (Thread t : clientCommunicationThread) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    static class CommunicateWithClient extends Thread{
        Socket socket;
        MulticastSocket ms;
        int port;
        RemoteObservable remoteObservable;
        public CommunicateWithClient(Socket socket, int port, MulticastSocket ms, RemoteObservable remoteObservable) {
            this.port = port;
            this.socket = socket;
            this.ms = ms;
            this.remoteObservable = remoteObservable;
            this.start();
        }

        @Override
        public void run() {
            try(ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                while (!Thread.currentThread().isInterrupted()) {
                    Request msgRec = (Request) ois.readObject();
                    Response response = null;
                    switch (msgRec.getRequestMessage()){
                        case REQUEST_AUTHENTICATE_USER -> {
                            User user = (User) msgRec.getRequestData();
                            response = Server.authenticateUser(user);
                            if (response.getResponseMessage().getCode() == 200)
                                remoteObservable.notifyClientAuthentication(Server.getServerData(), user);
                        }
                        case REQUEST_REGISTER_USER -> {
                            response = Server.registerUser((User) msgRec.getRequestData());
                        }
                        case REQUEST_EDIT_LOGIN_DATA -> {
                            response = Server.editLoginData((ArrayList) msgRec.getRequestData());
                        }
                        case REQUEST_READ_BOOKINGS -> {
                            response = Server.readBookings((Boolean) msgRec.getRequestData());
                        }
                        case REQUEST_READ_SHOWS -> {
                            response = Server.readShows((HashMap<String, String>) msgRec.getRequestData());
                        }
                        case REQUEST_SELECT_SHOW -> {
                            response = Server.selectShow((Integer) msgRec.getRequestData());
                        }
                        case REQUEST_READ_SHOW_AVAILABLE_SEATS -> {
                            response = Server.readShowAvailableSeats((Integer) msgRec.getRequestData());
                        }
                        case REQUEST_SELECT_SEAT -> {
                            response = Server.selectSeat((ArrayList) msgRec.getRequestData());
                        }
                        case REQUEST_CONFIRM_BOOKING -> {
                            response = Server.confirmBooking((ArrayList) msgRec.getRequestData());
                        }
                        case REQUEST_DELETE_BOOKING -> {
                            response = Server.deleteBooking((ArrayList) msgRec.getRequestData());
                        }
                        case REQUEST_PAY_BOOKING -> {
                            response = Server.payBooking((ArrayList) msgRec.getRequestData());
                        }
                        case REQUEST_MAKE_SHOW_VISIBLE -> {
                            response = Server.makeShowVisible((Integer) msgRec.getRequestData());
                        }
                        case REQUEST_DELETE_SHOW -> {
                            response = Server.deleteShow((Integer) msgRec.getRequestData());
                        }
                        case REQUEST_LOGOUT -> {
                            User user = (User) msgRec.getRequestData();
                            response = Server.logout(user);
                            if (response.getResponseMessage().getCode() == 200)
                                remoteObservable.notifyClientLogout(Server.getServerData(), user);
                        }
                    }
                    if (response != null){
                        if (response.getResponseMessage().getCode() == 200) {
                            Server.getServerData().incrementDatabaseVersion();
                            prepareRunningServers(ms);
                        }
                        oos.writeUnshared(response);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    socket.close();
                    this.interrupt();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void prepareRunningServers(MulticastSocket ms){
            try (DatagramSocket ds = new DatagramSocket();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)){

                ds.setSoTimeout(3000);
                ds.setBroadcast(true);

                Request request = new Request(RequestEnum.PREPARE, Server.getServerData().getDatabaseVersion());
                oos.writeUnshared(request);
                oos.reset();

                byte[] msgBytes = baos.toByteArray();
                DatagramPacket dpSend = new DatagramPacket(
                        msgBytes,
                        msgBytes.length,
                        InetAddress.getByName(IP_MULTICAST),
                        PORT_MULTICAST
                );
                ds.send(dpSend);

                while(true) {
                    try {
                        DatagramPacket dpRecDifusao = new DatagramPacket(new byte[MAX_BYTES], MAX_BYTES);
                        ds.receive(dpRecDifusao);

                        ByteArrayInputStream bais = new ByteArrayInputStream(dpRecDifusao.getData());
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        Object objRec = ois.readUnshared();

                        System.out.println("\nRECEIVED: " + objRec);

                        if (objRec instanceof Response){
                            // TODO: check if all servers sent response
                        }
                    } catch (SocketTimeoutException e) {
                        // when datagramsocket times out
                        break;
                    }
                }

                request = new Request(RequestEnum.COMMIT, Server.getServerData().getDatabaseVersion());
                oos.writeUnshared(request);
                oos.reset();

                msgBytes = baos.toByteArray();
                dpSend = new DatagramPacket(
                        msgBytes,
                        msgBytes.length,
                        InetAddress.getByName(IP_MULTICAST),
                        PORT_MULTICAST
                );
                ds.send(dpSend);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
