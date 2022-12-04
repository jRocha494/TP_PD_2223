package Data;

import java.io.Serial;
import java.io.Serializable;

public class ServerData implements Serializable, Comparable<ServerData> {
    @Serial
    static final long serialVersionUID = 3L;
    int nmrConnections;
    int port;
    int portDatabaseUpdate;
    String ip;
    int databaseVersion;
    boolean availability;
    long lastSentHeartbeat;

    public ServerData(int connections, int port, String ip) {
        this.nmrConnections = connections;
        this.port = port;
        this.portDatabaseUpdate = port+1;
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

    public int getPortDatabaseUpdate() {
        return portDatabaseUpdate;
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

    public void incrementDatabaseVersion() {
        this.databaseVersion += 1;
    }

    public void incrementNmrConnections() {
        this.nmrConnections += 1;
    }

    @Override
    public int compareTo(ServerData serverData) {
        return this.nmrConnections - serverData.getNmrConnections();
    }

    @Override
    public String toString() {
        return "ServerData{" +
                "nmrConnections=" + nmrConnections +
                ", port=" + port +
                ", portDatabaseUpdate=" + portDatabaseUpdate +
                ", ip='" + ip + '\'' +
                ", databaseVersion=" + databaseVersion +
                ", availability=" + availability +
                ", lastSentHeartbeat=" + lastSentHeartbeat +
                '}';
    }
}
