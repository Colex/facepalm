package server.classes;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISocialClient extends Remote {
	public Boolean ping() throws RemoteException;
}
