package Server;

import Data.ServerData;
import Data.ServerPersistentData;
import utils.Request;
import utils.RequestEnum;
import utils.Response;
import utils.ResponseMessageEnum;

import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

import static Data.Utils.*;

public class ThreadServerHeartbeat extends Thread{
    MulticastSocket ms;
    ServerPersistentData serverPersistentData;

    public ThreadServerHeartbeat(MulticastSocket ms) {
        this.ms = ms;
        this.serverPersistentData = ServerPersistentData.getInstance();
        this.start();
    }

    @Override
    public void run() {
        ReceiveHeartbeats rh = new ReceiveHeartbeats(ms, Server.getServerData().getPort(), serverPersistentData);
        UpdateDatabase ud = new UpdateDatabase(Server.getServerData().getPortDatabaseUpdate());
        Timer timerSendHeartbeat = new Timer();
        Timer timerCheckServersToRemove = new Timer();

        timerSendHeartbeat.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Thread.currentThread().isInterrupted()) {
                    timerSendHeartbeat.cancel();
                    timerSendHeartbeat.purge();
                }else{
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        Server.getServerData().setLastSentHeartbeat(System.currentTimeMillis());
                        oos.writeUnshared(Server.getServerData());
                        oos.reset();

                        byte[] msgBytes = baos.toByteArray();
                        DatagramPacket dpSend = new DatagramPacket(
                                msgBytes,
                                msgBytes.length,
                                InetAddress.getByName(IP_MULTICAST),
                                PORT_MULTICAST
                        );
                        ms.send(dpSend);
                    } catch (IOException e) {
                        System.out.println("Error writing data to be sent to running servers.");
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
        }, 0, 10000);

        timerCheckServersToRemove.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Thread.currentThread().isInterrupted()) {
                    timerCheckServersToRemove.cancel();
                    timerCheckServersToRemove.purge();
                }else{
                    for (ServerData entryServerData: serverPersistentData.getServers().values()) {
                        if (System.currentTimeMillis() - entryServerData.getLastSentHeartbeat() > 35000){
                            synchronized (serverPersistentData) {
                                serverPersistentData.getServers().remove(entryServerData.getPort());
                            }
                        }
                    }
                }
            }
        }, 0, 35000);
        try {
            rh.join();
            ud.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class UpdateDatabase extends Thread {
        int port;

        public UpdateDatabase(int port) {
            this.port = port;
            this.start();
        }

        @Override
        public void run() {
            int nBytes;
            byte[] readBytes = new byte[MAX_BYTES];
            try {
                ServerSocket ss = new ServerSocket(port);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = ss.accept();

                    OutputStream os = socket.getOutputStream();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    Request msgRec = (Request) ois.readObject();

                    if (msgRec.getRequestMessage().equals(RequestEnum.MSG_UPDATE_DATABASE)) {
                        File f = new File(DATABASE_FILENAME);
                        if(f.isFile() && f.canRead()) {
                            FileInputStream fis = new FileInputStream(f);
                            do {
                                nBytes = fis.read(readBytes, 0, MAX_BYTES);
                                if (nBytes > -1)
                                    os.write(readBytes, 0, nBytes);
                            } while (nBytes > -1);
                            fis.close();
                        }
                    }
                    socket.close();
                }
                ss.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class ReceiveHeartbeats extends Thread{
        MulticastSocket ms;
        int selfPort;
        ServerPersistentData serverPersistentData;
        public ReceiveHeartbeats(MulticastSocket ms, int selfPort, ServerPersistentData serverPersistentData) {
            this.ms = ms;
            this.selfPort = selfPort;
            this.serverPersistentData = serverPersistentData;
            this.start();
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket dpRecDifusao = new DatagramPacket(new byte[MAX_BYTES], MAX_BYTES);
                    ms.receive(dpRecDifusao);

                    ByteArrayInputStream bais = new ByteArrayInputStream(dpRecDifusao.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
//                    ServerData objRec = new ServerData((ServerData) ois.readUnshared());
                    Object objRec = ois.readUnshared();

                    System.out.println("\nRECEIVED: " + objRec);

                    if (objRec instanceof ServerData) {
                        synchronized (serverPersistentData) {
                            serverPersistentData.addServer((ServerData) objRec);
                        }
                    }
                    //TODO: make sure this request wasn't sent by this server itself
                    else if (objRec instanceof Request &&
                            ((Request) objRec).getRequestMessage() == RequestEnum.PREPARE){
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);

                        Response response = new Response(ResponseMessageEnum.SUCCESS, ((Request) objRec).getRequestData());
                        oos.writeUnshared(response);
                        oos.reset();

                        String ip = dpRecDifusao.getAddress().getHostAddress();
                        int port = dpRecDifusao.getPort();
                        byte[] msgBytes = baos.toByteArray();
                        DatagramPacket dpSendPrepare = new DatagramPacket(
                                msgBytes,
                                msgBytes.length,
                                InetAddress.getByName(ip),
                                port
                        );
                        ms.send(dpSendPrepare);
                    }
                    else if (objRec instanceof Request &&
                            ((Request) objRec).getRequestMessage() == RequestEnum.COMMIT) {
                        String ip = dpRecDifusao.getAddress().getHostAddress();
                        int port = dpRecDifusao.getPort();

                        Server.connectWithServerToUpdateDatabase(ip, port);
                        Server.getServerData().setDatabaseVersion((Integer) ((Request) objRec).getRequestData());
                    }
                }
            } catch (IOException e) {
                System.out.println("Error receiving heartbeats from running servers.");
            } catch (ClassNotFoundException e) {
                System.out.println("Error reading heartbeat sent by running servers.");
            }
        }
    }
}
