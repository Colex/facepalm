package server.classes;

import java.rmi.*;
import java.util.ArrayList;
import java.util.Date;

import websocket.notifications.INotifications;

public interface ISocialmore extends Remote {
	public IUser login(String email, String password) throws RemoteException;
	public IUser getUserByEmail(String email) throws RemoteException;
	IUser register(String name, String email, String city, String country, Date bday, char sex, String password, boolean isPublic) throws RemoteException;
	public IUser getUserByID(int m_receiver_id) throws RemoteException;
	public ArrayList<IPost> getPosts() throws RemoteException;
	public ArrayList<IUser> getOnlineUsers() throws RemoteException;
	public IPost getPost(int postid) throws RemoteException;
	public Boolean ping() throws RemoteException;
	public ArrayList<IUser> searchUser(String stuff) throws RemoteException;
	public ArrayList<IChatroom> getChatrooms(String subject) throws RemoteException;
	public ArrayList<IChatroom> getChatrooms() throws RemoteException;
	public IChatroom getChatroom(int chatid) throws RemoteException;
	public void addPrivateMessage(int userid, int targetid, Date date, String content, String [] urls) throws RemoteException;
	public void addPublicMessage(int chatid, int userid, Date date, String content, ArrayList<String> urls) throws RemoteException;
	public IUser signup(String token) throws RemoteException;
	public IUser login(String token) throws RemoteException;
	public boolean resetPassword(String email) throws RemoteException;
	public IUser recoveryLogin(String hash, String password) throws RemoteException;
	public boolean isRecoveryKeyValid(String hash) throws RemoteException;
	public void addOnlineUser(IUser user) throws RemoteException;
	public void removeOnlineUser(IUser user) throws RemoteException;
	public void setSocialNotifier(INotifications notifier) throws RemoteException;
	public ArrayList<IUser> searchUserByCity(String stuff) throws RemoteException;
	public ArrayList<IUser> searchUserByCountry(String stuff) throws RemoteException;
	public ArrayList<IUser> searchUserByEmail(String stuff) throws RemoteException;
	public ArrayList<IUser> searchUserByName(String stuff) throws RemoteException;
	public ArrayList<IChatroom> searchChatrooms(String name) throws RemoteException;
}
