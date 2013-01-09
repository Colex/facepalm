package server.classes;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IPost extends Remote, IMessage {
	public boolean edit(String s) throws RemoteException;
	public ArrayList<Comment> getComments() throws RemoteException;
	public boolean delete(int userid) throws RemoteException;
	public boolean edit(int userid, String content) throws RemoteException;
}
