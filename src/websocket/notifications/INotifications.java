package websocket.notifications;

import java.rmi.Remote;
import java.rmi.RemoteException;

import server.classes.IUser;

public interface INotifications extends Remote {
	public void notifyOnlineUser(IUser u) throws RemoteException;
}
