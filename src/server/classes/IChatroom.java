package server.classes;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IChatroom extends Remote {
	public ArrayList<IMessage> getMessages() throws RemoteException;
	public ArrayList<IUser> getUsers() throws RemoteException;
	public String getUserRole(int user_id) throws RemoteException;
	public boolean setUserRole(IUser owner, int user_id, String role) throws RemoteException;
	public int getChat_id() throws RemoteException;
	public int getOwner_id() throws RemoteException;
	public String getName() throws RemoteException;
	public void setName(String name) throws RemoteException;
	public int getRating() throws RemoteException;
	public String getSubject() throws RemoteException;
	public boolean isClosed() throws RemoteException;
	public void isClosed(boolean is_closed) throws RemoteException;
}
