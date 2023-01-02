package rmi_service.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObservableInterface extends Remote {
    String getServersList() throws RemoteException;

    void addObserver(rmi.RemoteObserverInterface observer) throws RemoteException;
    void removeObserver(rmi.RemoteObserverInterface observer) throws RemoteException;
}
