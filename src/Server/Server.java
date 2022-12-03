package Server;

import Data.ServerData;
import Data.ServerPersistentData;
import utils.Message;
import utils.MessageEnum;
import utils.Response;
import Data.User;
import Server.jdbc.ConnDB;
import utils.ResponseMessageEnum;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {
    final static int PORT_MULTICAST = 4004;
    final static String IP_MULTICAST = "239.39.39.39";
    final static String DATABASE_FILENAME = "PD-2022-23-TP.db";
    private static ConnDB connDB;
    private ServerData serverData;
    private ServerPersistentData serverPersistentData;
    private ArrayList<Thread> serverThreadList;
    private String databaseLocation;

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

    public static void main(String[] args) throws InterruptedException {
        // receives port to receive connections from clients and path to database
        if (args.length != 2) {
            System.out.println("Missing server port and database path");
            return;
        }

        try {
            System.out.println("IP: " + InetAddress.getLocalHost());
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
            ms.setSoTimeout(30000);
            server.populateRunningServers(ms);
            // sets multicast socket's timeout back to 0 (doesn't time out)
            ms.setSoTimeout(0);

            // if server's database is out of date
            if (server.outOfDateDatabase()){
                server.updateDatabase();
            }

            ThreadServerHeartbeat tsh = new ThreadServerHeartbeat(ms, server.serverData, server.serverPersistentData);
            server.serverThreadList.add(tsh);

            ThreadClientConnection tcn = new ThreadClientConnection(Integer.parseInt(args[0]));
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
        }
    }

    private void updateDatabase() throws IOException, ClassNotFoundException {
        System.out.println("Updating database");
        ServerData eligibleServer = null;

        for(ServerData entryServerData : serverPersistentData.getServers().values())
            if (entryServerData.getDatabaseVersion() > serverData.getDatabaseVersion())
                if (eligibleServer == null)
                    eligibleServer = entryServerData;
                else{
                    if (entryServerData.getDatabaseVersion() >= eligibleServer.getDatabaseVersion()
                            && entryServerData.getNmrConnections() < eligibleServer.getNmrConnections())
                        eligibleServer = entryServerData;
                }

        byte[] readBytes = new byte[4*1024];
        int nBytes;
        //TODO: Must get ip and port from somewhere
        String fileDirectory = DATABASE_FILENAME;
        File filename = new File(fileDirectory);
        filename.createNewFile();
        FileOutputStream fos = new FileOutputStream(filename, false);

        Socket socket = new Socket(eligibleServer.getIp(), eligibleServer.getPort());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        InputStream is = socket.getInputStream();

        oos.writeObject(new Message(MessageEnum.MSG_UPDATE_DATABASE.getMessage()));
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
        long end = t+30000;
        while(System.currentTimeMillis() < end) {
            try {
                DatagramPacket dpRecDifusao = new DatagramPacket(new byte[256], 256);
                ms.receive(dpRecDifusao);

                ByteArrayInputStream bais = new ByteArrayInputStream(dpRecDifusao.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object objRec = ois.readObject();

                System.out.println("[populate server data] Object received: " + objRec);

                if (objRec instanceof ServerData) {
                    synchronized (serverPersistentData) {
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


    }


    public static Response registerUser(User user) {
        try {
            connDB.createUser(user.getName(), user.getUsername(), user.getPassword());
            return new Response(ResponseMessageEnum.SUCCESS, null);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.USER_NOT_FOUND, null);
            //throw new CustomException("Error registering user", e);
        }
    }

    public static Response authenticateUser(User user) {
        try {
            User userAux = connDB.authenticateUser(user.getUsername(), user.getPassword());
            if(userAux == null)
                return new Response(ResponseMessageEnum.USER_NOT_FOUND, null);
            return new Response(ResponseMessageEnum.SUCCESS, userAux);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.USER_NOT_FOUND, null);
            //throw new CustomException("Error authenticating user", e);
        }
    }

    public static Response editLoginData(int id, HashMap<String, String> editLoginMap) {
        try {
            connDB.updateUser(id, editLoginMap);
            return new Response(ResponseMessageEnum.SUCCESS, null);
        } catch (SQLException e) {
            return new Response(ResponseMessageEnum.USER_NOT_FOUND, null);
            //throw new CustomException("Error editing user's data", e);
        }
    }
}
