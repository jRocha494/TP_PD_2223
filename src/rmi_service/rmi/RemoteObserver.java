package rmi_service.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;

import static rmi_service.RmiObserver.logger;

public class RemoteObserver extends UnicastRemoteObject implements RemoteObserverInterface {

    public RemoteObserver() throws RemoteException {}

    @Override
    public void notify(String notification) throws RemoteException {
        logger.log(Level.INFO, notification);
    }
}