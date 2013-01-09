package server.classes;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

public interface IMessage extends Remote {
	
	public int getSenderID() throws RemoteException;
	public String read() throws RemoteException;
	public String getContent() throws RemoteException;
	public int getReceiverID() throws RemoteException;
	public Date getSending() throws RemoteException;
	public IUser getSender() throws RemoteException;
	public ArrayList<Attach> getAttachments() throws RemoteException;
	public int getID() throws RemoteException;
	public IUser getContact(int userid) throws RemoteException;
	public String getPreview() throws RemoteException;
	public Date getReceiving() throws RemoteException;
	public void setFacebook(String fbid) throws RemoteException;
	public String getFacebook() throws RemoteException;
}
