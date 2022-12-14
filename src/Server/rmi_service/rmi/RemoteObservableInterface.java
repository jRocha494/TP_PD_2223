package Server.rmi_service.rmi;

import rmi_service.rmi.RemoteObserverInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObservableInterface extends Remote {
    String getServersList() throws RemoteException;

    void addObserver(RemoteObserverInterface observer) throws RemoteException;
    void removeObserver(RemoteObserverInterface observer) throws RemoteException;
}
