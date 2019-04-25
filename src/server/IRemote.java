package server;

import model.Answer;
import model.Cell;
import model.CellState;
import model.ServerShot;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemote extends Remote {
    int createGame() throws RemoteException;
    CellState[][] initializeFields(int gameID, int N) throws RemoteException;
    int getShipsCount(int gameID) throws RemoteException;
    boolean isPlayerFirst(int gameID) throws RemoteException;
    Answer sendShot(int gameID, int x, int y) throws RemoteException;
    ServerShot receiveShot(int gameID) throws RemoteException;
    Cell[] destroyShip(int gameID, int x, int y, boolean isPlayerShip) throws RemoteException;
}
