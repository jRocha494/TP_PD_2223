package rmi_service;

import rmi_service.resources.RmiConstants;
import Server.rmi_service.rmi.RemoteObservableInterface;
import rmi_service.rmi.RemoteObserver;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class RmiObserver {
    public static final Logger logger = Logger.getLogger(RmiObserver.class.getName());

    public static void main(String[] args) {
        if (args.length != 1){
            logger.severe("Invalid number of arguments.");
            return;
        }
        setupLogger();

        String registryBindName = RmiConstants.RMI_SERVICE_NAME + args[0];
        Scanner scanner = new Scanner(System.in);
        String cmd = "";
        RemoteObserver remoteObserver = null;
        RemoteObservableInterface remoteObservable = null;
        try {
            remoteObserver = new RemoteObserver();

            Registry r = LocateRegistry.getRegistry(null, Registry.REGISTRY_PORT);
            remoteObservable = (RemoteObservableInterface) r.lookup(registryBindName);

            while (!cmd.equalsIgnoreCase("s")) {
                System.out.println("OPTIONS:");
                System.out.println("\t'l' -> To (l)ist all running servers");
                System.out.println("\t'r' -> To (r)egister listener to receive notifications");
                System.out.println("\t'd' -> To (d)elete listener to receive notifications");
                System.out.println("\t's' -> To (s)top the program");

                cmd = scanner.nextLine();
                if (cmd.equalsIgnoreCase("l")) {
                    System.out.println(remoteObservable.getServersList());
                }else if (cmd.equalsIgnoreCase("r")) {
                    remoteObservable.addObserver(remoteObserver);
                }else if (cmd.equalsIgnoreCase("d")) {
                    remoteObservable.removeObserver(remoteObserver);
                }
            }

        } catch (RemoteException e) {
            logger.log(Level.SEVERE,"Error connecting to registry. -> {0}", e.toString());
        } catch (NotBoundException e) {
            logger.log(Level.SEVERE, "Error connecting to remote service. There might be no servers running on the received port. -> {0}", e.toString());
        } finally {
        if (remoteObservable != null) {
            try {
                remoteObservable.removeObserver(remoteObserver);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (remoteObserver != null) {
            try {
                // removes observer from rmi registry
                UnicastRemoteObject.unexportObject(remoteObserver, true);
            } catch (NoSuchObjectException e) {
                logger.log(Level.SEVERE, "Error removing observer from registry. -> {0}", e.toString());
            }
        }
    }
    }

    private static void setupLogger(){
        // suppress the logging output to the file
        logger.setUseParentHandlers(false);

        logger.setLevel(Level.INFO);
        ConsoleHandler consoleHandler = new ConsoleHandler();

        // create a TXT formatter
        SimpleFormatter formatterTxt = new SimpleFormatter();
        consoleHandler.setFormatter(formatterTxt);
        logger.addHandler(consoleHandler);
    }
}
