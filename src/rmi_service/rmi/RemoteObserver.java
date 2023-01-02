package rmi_service.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class RemoteObserver extends UnicastRemoteObject implements rmi.RemoteObserverInterface {

    public RemoteObserver() throws RemoteException {}
}