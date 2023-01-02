package rmi_service.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteObserverInterface extends Remote {
    void notify(String notification) throws RemoteException;

}
