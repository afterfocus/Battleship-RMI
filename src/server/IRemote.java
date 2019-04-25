package server;

import model.Answer;
import model.Cell;
import model.CellState;
import model.ServerShot;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemote extends Remote {
    CellState[][] initializeFields(int N) throws RemoteException;
    int getShipsCount() throws RemoteException;
    boolean isPlayerFirst() throws RemoteException;
    Answer sendShot(int x, int y) throws RemoteException;
    ServerShot receiveShot() throws RemoteException;
    boolean isShipHorizontal() throws RemoteException;
    Cell[] destroyShip(int x, int y, boolean isPlayerShip) throws RemoteException;
}
