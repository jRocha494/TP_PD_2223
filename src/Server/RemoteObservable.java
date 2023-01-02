package Server;

import Data.ServerData;
import Data.ServerPersistentData;
import Data.User;
import rmi_service.rmi.RemoteObservableInterface;
import rmi_service.rmi.RemoteObserverInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import static Server.Server.logger;

public class RemoteObservable extends UnicastRemoteObject implements RemoteObservableInterface {
    private final Set<RemoteObserverInterface> observersSet = new HashSet<>();

    protected RemoteObservable() throws RemoteException {
    }

    public void notifyClientAcception(ServerData serverData){
        notifyObservers(
                "Server [" + serverData.getIp() + ":" + serverData.getPort() +
                        "] accepted a connection from a client."
        );
    }

    public void notifyClientConnectionAttempt(ServerData serverData) {
        notifyObservers(
                "Server [" + serverData.getIp() + ":" + serverData.getPort() +
                        "] received a connection attempt from a client."
        );
    }

    public void notifyClientAuthentication(ServerData serverData, User user) {
        notifyObservers(
                "User " + user.getUsername() + " connected to Server [" +
                        serverData.getIp() + ":" + serverData.getPort() + "]"
        );
    }

    public void notifyClientLogout(ServerData serverData, User user) {
        notifyObservers(
                "User " + user.getUsername() + " logged out of Server [" +
                        serverData.getIp() + ":" + serverData.getPort() + "]"
        );
    }

    private synchronized void notifyObservers(String notification) {
        Iterator<RemoteObserverInterface> it = observersSet.iterator();

        while (it.hasNext()) {
            try {
                it.next().notify(notification);
            } catch (RemoteException e) {
                logger.log(Level.WARNING, "Connection lost with an observer.");
                it.remove();
            }
        }
    }

    @Override
    public synchronized String getServersList() throws RemoteException {
        ServerPersistentData serverPersistentData = ServerPersistentData.getInstance();
        return serverPersistentData.getServersListString();
    }

    @Override
    public synchronized void addObserver(RemoteObserverInterface observer) throws RemoteException {
        if (observersSet.add(observer)) {
            logger.log(Level.INFO, "New observer registered.");
        } else {
            logger.log(Level.WARNING, "Observer already registered.");
        }
    }

    @Override
    public synchronized void removeObserver(RemoteObserverInterface observer) throws RemoteException {
        if (observersSet.remove(observer)) {
            logger.log(Level.INFO, "Observer unregistered.");
        } else {
            logger.log(Level.WARNING, "Observer already unregistered.");
        }
    }
}
