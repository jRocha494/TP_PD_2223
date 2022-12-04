package Data;

import java.util.*;

public class ServerPersistentData {
    private static ServerPersistentData instance = null;
    private static Map<Integer, ServerData> runningServers = new TreeMap<>();
    public static ServerPersistentData getInstance(){
        if (instance == null)
            instance = new ServerPersistentData();
        return instance;
    }

    public void addServer(ServerData serverData){
        if (instance == null)
            instance = new ServerPersistentData();
        instance.runningServers.put(serverData.getPort(), serverData);
    }

    public boolean serverExists(int port){
        return runningServers.containsKey(port);
    }

    public void incrementNmrConnections(int port){
        runningServers.get(port).incrementNmrConnections();
    }

    public void incrementDatabaseVersion(int port){
        runningServers.get(port).incrementDatabaseVersion();
    }

    public Map<Integer, ServerData> getServers(){
        if (instance == null)
            instance = new ServerPersistentData();
        // sorts treemap before returning
        return valueSort(instance.runningServers);
    }

    public List getServersList(){
        if (instance == null)
            instance = new ServerPersistentData();
        return new ArrayList<>(instance.runningServers.values());
    }

    public static <K, V extends Comparable<V>> Map<K, V> valueSort(final Map<K, V> map){
        Comparator<K> valueComparator = (k1, k2) -> {
            int comp = map.get(k1).compareTo(
                    map.get(k2));
            if (comp == 0)
                return 1;
            else
                return comp;
        };

        // SortedMap created using the comparator
        Map<K, V> sorted = new TreeMap<K, V>(valueComparator);
        sorted.putAll(map);
        return sorted;
    }
}
