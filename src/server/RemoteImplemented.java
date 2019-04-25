package server;

import model.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class RemoteImplemented extends UnicastRemoteObject implements IRemote {
    private int idCounter = -1;
    private Map<Integer, Game> games = new HashMap<>();

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            IRemote remote = new RemoteImplemented();
            Registry registry = LocateRegistry.createRegistry(8080);
            registry.rebind("IRemote", remote);
            System.out.println("IRemote started");
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private RemoteImplemented() throws RemoteException {
    }

    public int createGame() {
        idCounter++;
        games.put(idCounter, new Game());
        return idCounter;
    }

    public CellState[][] initializeFields(int gameID, int N) {
        return games.get(gameID).initializeFields(N);
    }

    public int getShipsCount(int gameID) {
        return games.get(gameID).getShipsCount();
    }

    public boolean isPlayerFirst(int gameID) {
        return games.get(gameID).isPlayerFirst();
    }

    public Answer sendShot(int gameID, int x, int y) {
        return games.get(gameID).sendShot(x, y);
    }

    public ServerShot receiveShot(int gameID) {
        return games.get(gameID).receiveShot();
    }

    public Cell[] destroyShip(int gameID, int x, int y, boolean isPlayerShip) {
        return games.get(gameID).destroyShip(x, y, isPlayerShip);
    }
}
