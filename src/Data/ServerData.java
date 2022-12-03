package Data;

import java.io.Serial;
import java.io.Serializable;

public class ServerData implements Serializable, Comparable<ServerData> {
    @Serial
    static final long serialVersionUID = 2L;
    int nmrConnections;
    int port;
    String ip;
    int databaseVersion;
    boolean availability;
    long lastSentHeartbeat;

    public ServerData(int connections, int port, String ip) {
        this.nmrConnections = connections;
        this.port = port;
        this.ip = ip;
        this.availability = true;
        this.databaseVersion = 1;
    }

    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public int getNmrConnections() {
        return nmrConnections;
    }

    public long getLastSentHeartbeat() {
        return lastSentHeartbeat;
    }

    public void setLastSentHeartbeat(long lastSentHeartbeat) {
        this.lastSentHeartbeat = lastSentHeartbeat;
    }

    @Override
    public int compareTo(ServerData serverData) {
        return this.nmrConnections - serverData.getNmrConnections();
    }

    @Override
    public String toString() {
        return "ServerData{" +
                "connections=" + nmrConnections +
                ", port=" + port +
                ", ip='" + ip + '\'' +
                '}';
    }
}
