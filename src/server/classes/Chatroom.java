package server.classes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import server.database.DBResult;

public class Chatroom  extends UnicastRemoteObject implements IChatroom {
	
	private static final long serialVersionUID = -2189704605740771328L;
	
	private int m_chat_id;
	private int m_owner_id;
	private String m_name;
	private int m_rating;
	private String m_subject;
	private boolean m_is_closed;
	
	public Chatroom(int chat_id, int owner_id, String name, int rating, String subject, boolean is_closed) throws RemoteException {
		super();
		
		this.m_chat_id 		= chat_id;
		this.m_owner_id 	= owner_id;
		this.m_name 		= name;
		this.m_rating 		= rating;
		this.m_subject 		= subject;
		this.m_is_closed 	= is_closed;
	}

	public int getChat_id() throws RemoteException {
		return m_chat_id;
	}

	public void setChat_id(int chat_id) {
		this.m_chat_id = chat_id;
	}

	public int getOwner_id() throws RemoteException {
		return m_owner_id;
	}

	public void setOwner_id(int owner_id) {
		this.m_owner_id = owner_id;
	}

	public String getName() throws RemoteException {
		return m_name;
	}

	public void setName(String name) {

		String query = "UPDATE chatroom SET name = ? WHERE chatid = ?";
		
		try {
			Socialmore.instance.dbc.noResponseQuery(query, name, this.m_chat_id);
			this.m_name = name;
		} catch (SQLException e) {
		}
	}

	public int getRating() throws RemoteException {
		return m_rating;
	}

	public void setRating(int rating) {
		this.m_rating = rating;
	}

	public String getSubject() throws RemoteException {
		return m_subject;
	}

	public void setSubject(String subject) {
		this.m_subject = subject;
	}

	public boolean isClosed() throws RemoteException {
		return m_is_closed;
	}

	public void isClosed(boolean is_closed)  throws RemoteException {
		
		String query = "UPDATE chatroom SET isclosed = ? WHERE chatid = ?";
		
		try {
			Socialmore.instance.dbc.noResponseQuery(query, is_closed, this.m_chat_id);
			this.m_is_closed = is_closed;
		} catch (SQLException e) {
		}
	}
	
	public ArrayList<IMessage> getMessages() throws RemoteException {
		
		ArrayList<IMessage> messages = new ArrayList<>();
		
		String query = "select * from message where receiverid = ? and type = 'public' order by sending";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_chat_id);
			
			while (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int message_id 	= rs.getInt("messageid");
				int sender_id	= rs.getInt("senderid");
				Date sending	= rs.getTimestamp("sending");
				String content	= rs.getString("content");
				int chat_id		= rs.getInt("receiverid");
				
				messages.add(new PublicMessage(message_id, sender_id, sending, content, chat_id));
			}
			
			res.close();
			
		} catch (Exception e) {
			return null;
		}
		
		return messages;
	}
	
	public ArrayList<IUser> getUsers() throws RemoteException {
		ArrayList<IUser> users = new ArrayList<>();
		
		String query = "select * from users u, connection c where u.userid = c.userid and c.chatid = ?";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_chat_id);
			
			while (res.getResultSet().next()) {
				
				ResultSet rs 	 = res.getResultSet();
				int userid 		 = rs.getInt("userid");
				String name 	 = rs.getString("name");
				String _email 	 = rs.getString("email");
				String city 	 = rs.getString("city");
				String country 	 = rs.getString("country");
				Date bday 		 = rs.getDate("bday");
				char sex		 = (rs.getString("sex")).charAt(0);
				String _password = rs.getString("password");
				String fbuser	 = rs.getString("facebookid");
				boolean isActive = rs.getBoolean("isActive");
				boolean isPublic = rs.getBoolean("isPublic");
				
				users.add(new User(userid, name, _email, city, country, bday, sex, _password, fbuser, isActive, isPublic));
			}
		} catch (SQLException e) {
		}
		
		return users;
	}

	public String getUserRole(int user_id) throws RemoteException {
		String role = null;
		
		if (this.m_owner_id == user_id)
			return "owner";

		String query = "select type from connection where userid = ? and chatid = ?";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, user_id, this.m_chat_id);
			
			if (res.getResultSet().next()) {
				role = res.getResultSet().getString("type");
			}
			
			if (role != null)
				return role;
			
			query = "INSERT INTO connection(chatid, userid, type) VALUES (?,?,'watcher')";
			Socialmore.instance.dbc.noResponseQuery(query, this.m_chat_id, user_id);
			role = "watcher";
		} catch (SQLException e) {
			
		}
		
		return role;
	}

	public boolean setUserRole(IUser owner, int user_id, String role) throws RemoteException {
		
		if (owner.getID() != this.m_owner_id)
			return false;
		
		String query = "UPDATE connection SET type = ? WHERE chatid = ? AND userid = ?";
		
		try {
			Socialmore.instance.dbc.noResponseQuery(query, role, this.m_chat_id, user_id);
		} catch (SQLException e) {
			return false;
		}
		
		return false;
	}
}
