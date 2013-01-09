package server.classes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import server.database.DBResult;

public class Message extends UnicastRemoteObject implements IMessage {
	
	private static final long serialVersionUID = -5955821739667754153L;
	
	public static ISocialmore socialmore = null;
	
	protected int m_message_id;
	protected int m_sender_id;
	protected int m_receiver_id;
	protected String m_content;
	protected Date m_sending;
	protected String m_facebook;

	public Message(int ID, int sender, int receiver, String content, Date sending, String facebook) throws RemoteException {
		this.m_message_id 	= ID;
		this.m_sender_id	= sender;
		this.m_receiver_id 	= receiver;
		this.m_content 		= content;
		this.m_sending		= sending;
		this.m_facebook		= facebook;
	}

	public int getID() throws RemoteException {
		return this.m_message_id;		
	}
	
	public int getSenderID() throws RemoteException {
		return m_sender_id;
	}
	
	public int getReceiverID() throws RemoteException {
		return m_receiver_id;
	}

	public Date getSending() throws RemoteException {
		return m_sending;
	}

	public String getContent() throws RemoteException {
		return m_content;
	}
	
	public String read() throws RemoteException {
		
		String query = "UPDATE message SET receiving = now() WHERE receiving is null and messageid = ?";
		
		try {
			Socialmore.instance.dbc.noResponseQuery(query, this.m_message_id);
		} catch (SQLException e) {
		}
		
		return this.m_content;
	}

	public IUser getSender() throws RemoteException {
		return Socialmore.instance.getUserByID(this.m_sender_id);
	}

	public ArrayList<Attach> getAttachments() throws RemoteException {
		ArrayList<Attach> attaches = new ArrayList<Attach>();

		String query = "select * from attach where messageid = ?";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_message_id);
			
			while (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int attach_id	= rs.getInt("attachid");
				int message_id 	= rs.getInt("messageid");
				String filepath	= rs.getString("file_path");

				attaches.add(new Attach(attach_id, message_id, filepath));
			}
			
			res.close();
			
		} catch (SQLException e) {
		}
		
		return attaches;	
	}
	
	public IUser getContact(int userid) throws RemoteException {
		
		if (userid == this.m_receiver_id)
			return Socialmore.instance.getUserByID(this.m_sender_id);
		
		return Socialmore.instance.getUserByID(this.m_receiver_id);
		
	}
	
	public String getPreview() throws RemoteException {
		if (this.m_content.length() > 24)
			return this.m_content.substring(0, 21)+"...";
		return this.m_content;
	}

	@Override
	public String toString() {
		return "Message [m_message_id=" + m_message_id + ", m_sender_id="
				+ m_sender_id + ", m_receiver_id=" + m_receiver_id
				+ ", m_content=" + m_content + ", m_sending=" + m_sending + "]";
	}
	
	public Date getReceiving() throws RemoteException {
		return null;
	}
	
	@Override
	public void setFacebook(String fbid) throws RemoteException {
		String query = "UPDATE message SET facebookid = ? where messageid = ?";
		
		try {
			Socialmore.instance.dbc.noResponseQuery(query, fbid, this.m_message_id);
			this.m_facebook = fbid;
		} catch (SQLException e) {
		}
		
		return;
	}

	@Override
	public String getFacebook() throws RemoteException {
		return this.m_facebook;
	}
}
