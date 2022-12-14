package Server;

import Data.*;
import Server.rmi_service.rmi.RemoteObservable;
import rmi_service.resources.RmiConstants;
import utils.Request;
import utils.RequestEnum;
import utils.Response;
import Server.jdbc.ConnDB;
import utils.ResponseMessageEnum;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static Data.Utils.*;


public class Server {
    private static ConnDB connDB;
    private static ServerData serverData;
    private ServerPersistentData serverPersistentData;
    private ArrayList<Thread> serverThreadList;
    private String databaseLocation;
    public static final Logger logger = Logger.getLogger(Server.class.getName());
    private RemoteObservable remoteObservable = null;

    public Server(int port, String ip, String databaseLocation) {
        this.databaseLocation = databaseLocation;
        this.serverThreadList = new ArrayList<>();

        serverData = new ServerData(0, port, ip);
        serverPersistentData = ServerPersistentData.getInstance();
        synchronized (serverPersistentData) {
            serverPersistentData.addServer(serverData);
        }

//        this.connDB = connDB;
    }

    public static ServerData getServerData() {
        return serverData;
    }

    public static void main(String[] args) throws InterruptedException {
        // receives port to receive connections from clients and path to database
        if (args.length != 2) {
            System.out.println("Missing server port and database path");
            return;
        }

        try {
            setupLogger();
            System.out.println("IP: " + InetAddress.getLocalHost());
            System.out.println("PORT: " + args[0]);
            Server server = new Server(
                    Integer.parseInt(args[0]),
                    InetAddress.getLocalHost().getHostAddress(),
                    args[1]);

            MulticastSocket ms = new MulticastSocket(PORT_MULTICAST);
            InetAddress ipGroup = InetAddress.getByName(IP_MULTICAST);
            SocketAddress sa = new InetSocketAddress(ipGroup, PORT_MULTICAST);
            NetworkInterface ni = NetworkInterface.getByName("wlan1");
            ms.joinGroup(sa, ni);

            // set timeout to receive heartbeats to 30 seconds
            ms.setSoTimeout(15000);
//            ms.setSoTimeout(30000);
            server.populateRunningServers(ms);
            // sets multicast socket's timeout back to 0 (doesn't time out)
            ms.setSoTimeout(0);

            // if server's database is out of date
            if (server.outOfDateDatabase()){
                server.updateDatabase();
            }
            connDB = new ConnDB(server.databaseLocation);

            server.registerRemoteService(String.valueOf(serverData.getPort()));

            ThreadServerHeartbeat tsh = new ThreadServerHeartbeat(ms);
            server.serverThreadList.add(tsh);

            ThreadClientConnection tcn = new ThreadClientConnection(Integer.parseInt(args[0]), ms, server.remoteObservable);
            server.serverThreadList.add(tcn);

            // waits threads to conclude execution
            for (Thread t : server.serverThreadList)
                t.join();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerRemoteService(String port) {
        try {
            Registry r;
            try {
                r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            }catch(RemoteException e) {
                r = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            }
            logger.log(Level.INFO, "RMI service <{0}> created and running.", RmiConstants.RMI_SERVICE_NAME+port);

            remoteObservable = new RemoteObservable();
            r.rebind(RmiConstants.RMI_SERVICE_NAME + port, remoteObservable);
            logger.log(Level.INFO, "RMI service <{0}> registered.", RmiConstants.RMI_SERVICE_NAME+port);
        } catch(RemoteException e) {
            logger.log(Level.SEVERE, "RMI remote exception. -> {0}", e.toString());
        }
    }

    private static void setupLogger(){
        // suppress the logging output to the file
        logger.setUseParentHandlers(false);

        logger.setLevel(Level.INFO);
        ConsoleHandler consoleHandler = new ConsoleHandler();

        // create a TXT formatter
        SimpleFormatter formatterTxt = new SimpleFormatter();
        consoleHandler.setFormatter(formatterTxt);
        logger.addHandler(consoleHandler);
    }

    private void updateDatabase() throws IOException, ClassNotFoundException {
        System.out.println("Updating database");
        ServerData eligibleServer = null;

        for(ServerData entryServerData : serverPersistentData.getServers().values()) {
            if (entryServerData.getDatabaseVersion() > serverData.getDatabaseVersion()) {
                if (eligibleServer == null)
                    eligibleServer = entryServerData;
                else {
                    if (entryServerData.getDatabaseVersion() >= eligibleServer.getDatabaseVersion()
                            && entryServerData.getNmrConnections() < eligibleServer.getNmrConnections())
                        eligibleServer = entryServerData;
                }
            }
        }
        connectWithServerToUpdateDatabase(eligibleServer.getIp(), eligibleServer.getPortDatabaseUpdate());
    }

    public static void connectWithServerToUpdateDatabase(String ip, int port) throws IOException {
        byte[] readBytes = new byte[MAX_BYTES];
        int nBytes;
        FileOutputStream fos = new FileOutputStream(DATABASE_FILENAME);

        Socket socket = new Socket(ip, port);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        InputStream is = socket.getInputStream();

        oos.writeUnshared(new Request(RequestEnum.MSG_UPDATE_DATABASE, null));
        do{
            nBytes = is.read(readBytes);
            if (nBytes > -1)
                fos.write(readBytes, 0, nBytes);
        } while (nBytes > -1);
    }

    private boolean outOfDateDatabase() {
        for(Map.Entry<Integer,ServerData> entry : serverPersistentData.getServers().entrySet())
            if (entry.getValue().getDatabaseVersion() > serverData.getDatabaseVersion())
                return true;
        return false;
    }

    private void populateRunningServers(MulticastSocket ms){
        System.out.println("Populating running servers");
        long t= System.currentTimeMillis();
        long end = t+5000;
//        long end = t+30000;
        while(System.currentTimeMillis() < end) {
            try {
                DatagramPacket dpRecDifusao = new DatagramPacket(new byte[MAX_BYTES], MAX_BYTES);
                ms.receive(dpRecDifusao);

                ByteArrayInputStream bais = new ByteArrayInputStream(dpRecDifusao.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object objRec = ois.readObject();

                System.out.println("[populate server data] Object received: " + objRec);

                if (objRec instanceof ServerData) {
                    synchronized (serverPersistentData) {
                        if (!serverPersistentData.serverExists(((ServerData) objRec).getPort()))
                            serverPersistentData.addServer((ServerData) objRec);
                    }
                }
            } catch (SocketTimeoutException e) {
                // when the multicast socket times out
                break;
            } catch (IOException e) {
                System.out.println("Error receiving heartbeats from running servers.");
            } catch (ClassNotFoundException e) {
                System.out.println("Error reading heartbeat sent by running servers.");
            }
        }
        System.out.println("Finished populating running servers");
    }


    public static Response registerUser(User user) {
        try {
            connDB.createUser(user.getName(), user.getUsername(), user.getPassword());
            return new Response(ResponseMessageEnum.SUCCESS, null);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response authenticateUser(User user) {
        try {
            User userAux = connDB.authenticateUser(user.getUsername(), user.getPassword());
            if(userAux == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, userAux);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response editLoginData(ArrayList data) {
        try {
            int id = (int) data.get(0);
            HashMap<String, String> editLoginMap = (HashMap<String, String>) data.get(1);
            connDB.updateUser(id, editLoginMap);
            return new Response(ResponseMessageEnum.SUCCESS, null);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response readBookings(boolean withConfirmedPayment) {
        try {
            List<Booking> bookingList = connDB.readBookings(withConfirmedPayment);
            if(bookingList == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, bookingList);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response readShows(HashMap<String, String> filtersMap) {
        try {
            List<Show> showList = connDB.readShows(filtersMap);
            if(showList == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, showList);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response selectShow(int showId) {
        try {
            Show show = connDB.selectShow(showId);
            if(show == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, show);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response readShowAvailableSeats(int showId) {
        try {
            List<Seat> seatList = connDB.readShowFreeSeats(showId);
            if(seatList == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, seatList);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public Response readShowSeats(int showId) {
        try {
            List<Seat> seatList = connDB.readShowSeats(showId);
            if(seatList == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, seatList);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response selectSeat(ArrayList data) {
        try {
            String chosenRow = (String) data.get(0);
            String chosenSeat = (String) data.get(1);
            int showId = (int) data.get(2);
            Seat seat = connDB.selectSeat(chosenRow, chosenSeat, showId);
            if(seat == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, seat);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response confirmBooking(ArrayList data) {
        try {
            int showId = (int) data.get(0);
            List<Seat> selectedSeats = (List<Seat>) data.get(1);
            int userId = (int) data.get(2);
            Booking booking = connDB.confirmBooking(showId, selectedSeats, userId);
            if(booking == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, booking);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response deleteBooking(ArrayList data) {
        try {
            int bookingId = (int) data.get(0);
            int userId = (int) data.get(1);
            connDB.deleteBooking(bookingId, userId);
            return new Response(ResponseMessageEnum.SUCCESS, null);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response payBooking(ArrayList data) {
        try {
            int bookingId = (int) data.get(0);
            int userId = (int) data.get(1);
            Booking booking = connDB.payBooking(bookingId, userId);
            if(booking == null)
                return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
            return new Response(ResponseMessageEnum.SUCCESS, booking);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response makeShowVisible(int selectedShow) {
        try {
            connDB.makeShowVisible(selectedShow);
            return new Response(ResponseMessageEnum.SUCCESS, null);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response deleteShow(int selectedShow) {
        try {
            connDB.deleteShow(selectedShow);
            return new Response(ResponseMessageEnum.SUCCESS, null);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }

    public static Response logout(User user) {
        try {
            connDB.logout(user.getId());
            return new Response(ResponseMessageEnum.SUCCESS, null);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.ERROR_OCCURRED, null);
        }
    }
}
