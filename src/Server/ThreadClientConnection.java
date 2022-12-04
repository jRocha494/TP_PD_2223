package Server;

import Data.ServerPersistentData;
import Data.User;
import utils.Request;
import utils.RequestEnum;
import utils.Response;
import utils.ResponseMessageEnum;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ThreadClientConnection extends Thread{
    int port;

    public ThreadClientConnection(int port) {
        this.port = port;
        this.start();
    }

    @Override
    public void run(){
        AcceptClients ac = new AcceptClients(port);

        try (DatagramSocket ds = new DatagramSocket(port)){
            while (!Thread.currentThread().isInterrupted()){
                DatagramPacket dpRec = new DatagramPacket(new byte[256], 256);
                System.out.println("Waiting for clients");
                ds.receive(dpRec);

                ByteArrayInputStream bais = new ByteArrayInputStream(dpRec.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Request msgRec = (Request) ois.readObject();

                System.out.println(msgRec);
                if (msgRec.getRequestMessage().equals(RequestEnum.MSG_CONNECT_SERVER)){
                    Response msgResp = new Response(ResponseMessageEnum.SUCCESS, ServerPersistentData.getInstance().getServers());
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
        ArrayList<Thread> clientCommunicationThread;
        public AcceptClients(int port) {
            this.port = port;
            this.start();
        }

        @Override
        public void run() {
            try (ServerSocket ss = new ServerSocket(port);){
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = ss.accept();
                    CommunicateWithClient cwc = new CommunicateWithClient(socket);
                    clientCommunicationThread.add(cwc);
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
        public CommunicateWithClient(Socket socket) {
            this.socket = socket;
            this.start();
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    Request msgRec = (Request) ois.readObject();
                    //TODO: read commands from client and send responses
                    Response response = null;
                    switch (msgRec.getRequestMessage()){
                        case REQUEST_AUTHENTICATE_USER -> {
                            response = Server.authenticateUser((User) msgRec.getRequestData());
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
                    }
                    if (response != null){
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
