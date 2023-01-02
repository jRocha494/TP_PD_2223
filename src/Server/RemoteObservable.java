package Server;

import Data.ServerPersistentData;
import rmi.RemoteObserverInterface;
import rmi_service.rmi.RemoteObservableInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static Server.Server.logger;

public class RemoteObservable extends UnicastRemoteObject implements RemoteObservableInterface {
    private final Set<RemoteObserverInterface> observersSet = new HashSet<>();

    protected RemoteObservable() throws RemoteException {
    }

    @Override
    public String getServersList() throws RemoteException {
        ServerPersistentData serverPersistentData = ServerPersistentData.getInstance();
        return serverPersistentData.getServersListString();
    }

    @Override
    public void addObserver(RemoteObserverInterface observer) throws RemoteException {
        if (observersSet.add(observer)) {
            logger.log(Level.INFO, "New observer registered.");
        } else {
            logger.log(Level.WARNING, "Observer already registered.");
        }
    }

    @Override
    public void removeObserver(RemoteObserverInterface observer) throws RemoteException {
        if (observersSet.remove(observer)) {
            logger.log(Level.INFO, "Observer unregistered.");
        } else {
            logger.log(Level.WARNING, "Observer already unregistered.");
        }
    }
}
