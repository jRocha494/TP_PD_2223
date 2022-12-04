package Server;

import Data.ServerData;
import Data.ServerPersistentData;
import utils.Request;
import utils.RequestEnum;

import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

import static Data.Utils.*;

public class ThreadServerHeartbeat extends Thread{
    MulticastSocket ms;
    ServerPersistentData serverPersistentData;
    ServerData serverData;

    public ThreadServerHeartbeat(MulticastSocket ms, ServerData serverData, ServerPersistentData serverPersistentData) {
        this.ms = ms;
        this.serverPersistentData = serverPersistentData;
        this.serverData = serverData;
        this.start();
    }

    @Override
    public void run() {
        ReceiveHeartbeats rh = new ReceiveHeartbeats(ms, serverData.getPort(), serverPersistentData);
        UpdateDatabase ud = new UpdateDatabase(serverData);
        Timer timerSendHeartbeat = new Timer();
        Timer timerCheckServersToRemove = new Timer();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);){

            timerSendHeartbeat.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (Thread.currentThread().isInterrupted()) {
                        timerSendHeartbeat.cancel();
                        timerSendHeartbeat.purge();
                    }else{
                        try {
                            serverData.setLastSentHeartbeat(System.currentTimeMillis());
                            oos.writeObject(serverData);
                            DatagramPacket dpSend = new DatagramPacket(
                                    baos.toByteArray(),
                                    baos.toByteArray().length,
                                    InetAddress.getByName(IP_MULTICAST),
                                    PORT_MULTICAST
                            );
                            ms.send(dpSend);
                            System.out.println("Sent: " + serverData);
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
                            if (System.currentTimeMillis() - entryServerData.getLastSentHeartbeat() > 30000){
                                synchronized (serverPersistentData) {
                                    serverPersistentData.getServers().remove(entryServerData.getPort());
                                }
                            }
                        }
                    }
                }
            }, 0, 30000);
        }catch (IOException e) {
            System.out.println("Error creating dependencies to send heartbeats to running servers.");
        }finally {
            try {
                rh.join();
                ud.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class UpdateDatabase extends Thread {
        ServerData serverData;

        public UpdateDatabase(ServerData serverData) {
            this.serverData = serverData;
            this.start();
        }

        @Override
        public void run() {
            int nBytes;
            byte[] readBytes = new byte[4*1024];
            try {
                ServerSocket ss = new ServerSocket(serverData.getPort());
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = ss.accept();
                    
                    OutputStream os = socket.getOutputStream();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    Request msgRec = (Request) ois.readObject();

                    System.out.println("RECEBIDO NA THREAD PARA ATUALIZAR BASE DADOS" + msgRec);

                    if (msgRec.getRequestMessage().equals(RequestEnum.MSG_UPDATE_DATABASE)) {
                        File f = new File(DATABASE_FILENAME);
                        if (f.isFile() && f.canRead()) {
                            FileInputStream fis = new FileInputStream(f);
                            do {
                                nBytes = fis.read(readBytes, 0, 4 * 1024);
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
                    DatagramPacket dpRecDifusao = new DatagramPacket(new byte[256], 256);
                    ms.receive(dpRecDifusao);

                    ByteArrayInputStream bais = new ByteArrayInputStream(dpRecDifusao.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Object objRec = ois.readObject();

                    System.out.println("Object received: " + objRec);

                    if (objRec instanceof ServerData){
                        // if object received wasn't sent by the thread's own server
                        if (((ServerData) objRec).getPort() != selfPort) {
                            serverPersistentData.getServers().put(((ServerData) objRec).getPort(), (ServerData) objRec);
                        }
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
