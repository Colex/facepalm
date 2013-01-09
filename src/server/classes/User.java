package server.classes;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletRequest;

import server.database.DBResult;

public class User extends UnicastRemoteObject implements IUser {

	private static final long serialVersionUID = -1270814636624657757L;
	
	private static SecureRandom random = new SecureRandom();

	private ConcurrentLinkedQueue<ISocialClient>	m_client;
	private int m_user_id;
	private String m_name;
	private String m_email;
	private String m_city;
	private String m_country;
	private Date m_bday;
	private char m_sex;
	private String m_password;
	private boolean m_is_active;
	private boolean m_is_public;
	private boolean m_facepalm;
	private String m_facebook;
	private String m_token;
	
	public User(int userid, String name, String email, String city,
			String country, Date bday, char sex, String password, String fbuser,
			boolean isActive, boolean isPublic) throws RemoteException {
		this.m_user_id 		= userid;
		this.m_name 		= name;
		this.m_email 		= email;
		this.m_city 		= city;
		this.m_country 		= country;
		this.m_bday 		= bday;
		this.m_sex 			= sex;
		this.m_password 	= password;
		this.m_is_active 	= isActive;
		this.m_is_public 	= isPublic;
		this.m_facebook		= fbuser;
		this.m_facepalm		= false;
		this.m_client		= new ConcurrentLinkedQueue<ISocialClient>();
		this.m_token		= null;
	}
	
	public void setFacepalm(Boolean is_fp) throws RemoteException {
		m_facepalm = is_fp;
	}

	public Boolean isFacepalm() throws RemoteException {
		return m_facepalm;
	}
	
	public int getID() throws RemoteException {
		return m_user_id;
	}

	public void setUserid(int userid) {
		this.m_user_id = userid;
	}


	public boolean setName(String name) throws RemoteException {
		
		name = Util.filter(name);
		
		String query = "UPDATE users SET name = ? WHERE userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, name, this.m_user_id);
			this.m_name = name;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	public String getEmail() throws RemoteException {
		return m_email;
	}

	public boolean setEmail(String email) throws RemoteException {
		
		email = Util.filter(email);
		
		String query = "UPDATE users SET email = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, email, this.m_user_id);
			this.m_email = email;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	public String getCity() throws RemoteException {
		return m_city;
	}

	public boolean setCity(String city) throws RemoteException {
		
		city = Util.filter(city);
		
		String query = "UPDATE users SET city = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, city, this.m_user_id);
			this.m_city = city;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	public String getCountry() throws RemoteException {
		return m_country;
	}

	public boolean setCountry(String country) throws RemoteException {

		country = Util.filter(country);
		
		String query = "UPDATE users SET country = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, country, this.m_user_id);
			this.m_country = country;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	public String getBday(String format) throws RemoteException {
		
		DateFormat df = new SimpleDateFormat(format);
		return df.format(m_bday);
	}
	
	public String getBday() throws RemoteException {
		return getBday("dd MMMMM yyyy");
	}

	public boolean setBday(Date bday) throws RemoteException {
		
		String query = "UPDATE users SET bday = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, bday, this.m_user_id);
			this.m_bday = bday;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	public String getSex() {
		return m_sex+"";
	}

	public boolean setSex(char sex) throws RemoteException {
		
		String query = "UPDATE users SET sex = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, sex, this.m_user_id);
			this.m_sex = sex;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	public String getPassword() {
		return m_password;
	}

	public boolean setPassword(String password) throws RemoteException {
		
		String query = "UPDATE users SET password = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, password, this.m_user_id);
			this.m_password = password;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	public boolean isActive() throws RemoteException {
		return m_is_active;
	}

	public boolean setActive(boolean isActive) throws RemoteException {
		
		String query = "UPDATE users SET isActive = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, isActive, this.m_user_id);
			this.m_is_active = isActive;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	
	
	public boolean isPublic() throws RemoteException {
		return m_is_public;
	}

	public boolean setPublic(boolean isPublic) throws RemoteException {
		
		String query = "UPDATE users SET isPublic = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, isPublic, this.m_user_id);
			this.m_is_public = isPublic;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	public ConcurrentLinkedQueue<ISocialClient> getClient() {
		return this.m_client;
	}

	@Override
	public void addConnection(ISocialClient sm) throws RemoteException {
		m_client.add(sm);
	}

	@Override
	public void logout(ISocialClient sm) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean ping() throws RemoteException {
		if (m_facepalm)	
			return true;
		
		for (ISocialClient a : m_client) {
			try {	
				if (a.ping()) return true;
			} catch (RemoteException e) {
				try {
					m_client.remove(a);
				} catch (Exception er) {}
			}
		}
		
		return false;
	}
	
	public ArrayList<PrivateMessage> getMyMessages() throws RemoteException {
		
		ArrayList<PrivateMessage> messages = new ArrayList<PrivateMessage>();
		
		String query = "select * from message where receiverid = ? and type = 'private' and sending < now() order by last_activity desc";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_user_id);
			
			while (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int message_id 	= rs.getInt("messageid");
				int sender_id	= rs.getInt("senderid");
				Date sending	= rs.getDate("sending");
				String content	= rs.getString("content");
				int receiver_id	= rs.getInt("receiverid");
				Date receiving	= rs.getDate("receiving");
				
				messages.add(new PrivateMessage(message_id, sender_id, sending, content, receiver_id, receiving));
			}
			
			res.close();
			
		} catch (SQLException e) {
			return null;
		}

		return messages;
	}
	
	public int createChatroom(String subject, String name) throws RemoteException {
		
		name = Util.filter(name);

		String query = "INSERT INTO chatroom(ownerid, name, subject, isclosed) VALUES (?, ?, ?, 'false') returning chatid";
		int chatid = -1;
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_user_id, name, subject);
			if (res.getResultSet().next()) {
				chatid = res.getResultSet().getInt("chatid");
			}
		} catch (SQLException e) {
		}
		
		return chatid;
	}
	
	public boolean sendPrivateMessage(int receiverID, String content) throws RemoteException {

		content = Util.filter(content);
		
		if (Socialmore.instance.getUserByID(receiverID) == null || content.trim().length() == 0)
			return false;
		
		return send(receiverID, content, "private");
	}
	
	private boolean send(int id, String content, String type) throws RemoteException {
		
		content = Util.filter(content);
		
		String query = "INSERT INTO message(senderid, sending, content, receiverid, type, last_activity) VALUES (?, now(), ?, ?, ?, now())";
		
		try {
			Socialmore.instance.dbc.noResponseQuery(query, this.m_user_id, content, id, type);
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	public int post(String content) throws RemoteException {
		DBResult res;
		int messageid = -1;
		
		content = Util.filter(content);
		
		String query = "INSERT INTO message(senderid, sending, content, type, last_activity) VALUES (?, now(), ?, 'post', now()) returning messageid";
		
		try {
			res = Socialmore.instance.dbc.query(query, this.m_user_id, content);
			if (res.getResultSet().next())
				messageid = res.getResultSet().getInt("messageid");
		} catch (SQLException e) {
		}
		
		return messageid;
	}
	
	@Override
	public String toString() {
		return "User [m_user_id=" + m_user_id + ", m_name=" + m_name
				+ ", m_email=" + m_email + ", m_city=" + m_city
				+ ", m_country=" + m_country + "]";
	}

	public int post(String content, ArrayList<String> urls) throws RemoteException {
		
		content = Util.filter(content);
		
		String query = "INSERT INTO message(senderid, sending, content, type, last_activity) VALUES (?, now(), ?, 'post', now()) returning messageid";
		DBResult res;
		int messageid = -1;
		
		try {
			Socialmore.instance.dbc.noResponseQuery("BEGIN;");
			res = Socialmore.instance.dbc.query(query, this.m_user_id, content);
			
			if (res.getResultSet().next())
				messageid = res.getResultSet().getInt("messageid");
			
			for (String url : urls) {
				query = "INSERT INTO attach(messageid, file_path) VALUES (?, ?)";
				Socialmore.instance.dbc.noResponseQuery(query, messageid, url);
			}
			
			Socialmore.instance.dbc.noResponseQuery("COMMIT;");
		} catch (SQLException e) {
			try {
				Socialmore.instance.dbc.noResponseQuery("ROLLBACK;");
			} catch (SQLException e1) {
				System.out.println("holy crap :x");
			}
			messageid = -1;
		}		
		return messageid;
	}
	
	
	public boolean joinChatroom(int chatid, String type) throws RemoteException {
		
		String query = "INSERT INTO connection(chatid, userid, type) VALUES (?, ?, ?)";
		
		try {
			Socialmore.instance.dbc.noResponseQuery(query, chatid, this.m_user_id, type);
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	public boolean amIowner (IPost p) throws RemoteException {
		return p.getSenderID() == this.m_user_id;
	}

	@Override
	public String getName() throws RemoteException {
		return m_name;
	}

	@Override
	public String getEmailHash() throws RemoteException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(this.m_email.getBytes());
			BigInteger bigInt = new BigInteger(1,digest);
	
			String s = bigInt.toString(16);
			
			while (s.length() < 32) {
				s = "0"+s;
			}
			
			return s;
		} catch (NoSuchAlgorithmException e) {
		}
		return null;
	}
	
	public boolean reply(String content, int post_id) throws RemoteException {
		
		String query; 
		
		content = Util.filter(content);
		
		try {
			Socialmore.instance.dbc.noResponseQuery("BEGIN;");
			query = "INSERT INTO comment(postid, userid, sending, content) VALUES (?, ?, now(), ?)";
			Socialmore.instance.dbc.noResponseQuery(query, post_id, this.m_user_id, content);
			query = "UPDATE message SET last_activity = now() where messageid = ?";
			Socialmore.instance.dbc.noResponseQuery(query, post_id);
			Socialmore.instance.dbc.noResponseQuery("COMMIT;");
		} catch (SQLException e) {
			  
			try {
				Socialmore.instance.dbc.noResponseQuery("ROLLBACK;");
			} catch (SQLException e1) {
				System.out.println("holy crap :x");
			}
			return false;
		}
		
		return true;
	}

	public ArrayList<IPost> getPosts() throws RemoteException {

		ArrayList<IPost> posts = new ArrayList<IPost>();
		
		String query = "select * from message where type = 'post' and senderid = ? order by last_activity desc";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_user_id );
			
			while (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int ID 			= rs.getInt("messageid");
				int sender		= rs.getInt("senderid");
				Date sending	= rs.getTimestamp("sending");
				String content	= rs.getString("content");
				int receiver	= rs.getInt("receiverid");
				
				posts.add(new Post(ID, sender, receiver, content, sending));
			}
			
			res.close();
			
		} catch (SQLException e) {
			return null;
		}
		
		return posts;
	}
	
	public boolean vote(int chatid, int rate) throws RemoteException {
		
		String query;
		DBResult res;
		
		try {
			Socialmore.instance.dbc.noResponseQuery("BEGIN");
			Socialmore.instance.dbc.noResponseQuery("LOCK TABLE VOTE");
				
			query = "select count(distinct(senderid)) from message where type = 'public' and receiverid = ?";
			res = Socialmore.instance.dbc.query(query, chatid);
			
			int max = -1, current = -1;
			
			if (res.getResultSet().next())
				max = res.getResultSet().getInt("count");
			
			query = "select count(*) from vote where chatid = ?";
			res = Socialmore.instance.dbc.query(query, chatid);
			
			if (res.getResultSet().next())
				current = res.getResultSet().getInt("count");
			
			System.out.println("MAX: " + max + " current: " + current);
			
			if (max == -1 || current == -1) {
				Socialmore.instance.dbc.noResponseQuery("COMMIT");
				return false;
			}

			
			if (current < max) {
				query = "INSERT INTO vote(chatid, userid, rate) VALUES (?, ?, ?)";
				Socialmore.instance.dbc.noResponseQuery(query, chatid, this.m_user_id, rate);
			} else {
				query = "UPDATE vote SET rate = ? WHERE userid = ? AND chatid = ?";
				Socialmore.instance.dbc.noResponseQuery(query, rate, this.m_user_id, chatid);
			}
			
			Socialmore.instance.dbc.noResponseQuery("COMMIT");
		
		} catch (SQLException e) {
			  
			try {
				Socialmore.instance.dbc.noResponseQuery("ROLLBACK");
				query = "UPDATE vote SET rate = ? WHERE userid = ? AND chatid = ?";
				Socialmore.instance.dbc.noResponseQuery(query, rate, this.m_user_id, chatid);
			} catch (SQLException e1) {
				try {
					Socialmore.instance.dbc.noResponseQuery("ROLLBACK");
				} catch (SQLException e2) {
					System.out.println("holy crap :x");
				}
				return false;
			}
		}
		
		return true;
	}
	
	public static IUser getSession(HttpServletRequest req) {
		IUser u;
		
		u = (IUser)req.getSession().getAttribute("user");
		if (u == null) return null;
		
		try {
			u.ping();
		} catch (RemoteException e) {
			req.getSession().setAttribute("user", null);
			return null;
		}
		
		return u;
	}
	
	public static Boolean matchNounce(HttpServletRequest request) {
		String nounce 		= request.getParameter("nounce");
		String old_nounce	= (String)request.getSession().getAttribute("nounce");
		if (old_nounce == null) old_nounce = "";
		String new_nounce	= new BigInteger(64, random).toString(32);
		request.getSession().setAttribute("nounce", new_nounce);
		return old_nounce.equals(nounce);
	}
	
	public ArrayList<IMessage> getMessages(int userid) throws RemoteException {
		
		ArrayList<IMessage> messages = new ArrayList<>();
		String query = "select * from message where type = 'private' and ((senderid = ? and receiverid = ?) or (senderid = ? and receiverid = ? and sending < now())) order by sending desc";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_user_id, userid, userid, this.m_user_id);
		
			while (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int message_id	= rs.getInt("messageid");
				int sender_id	= rs.getInt("senderid");
				Date sending	= rs.getTimestamp("sending");
				String content	= rs.getString("content");
				int receiver_id	= rs.getInt("receiverid");
				Date receiving	= rs.getTimestamp("receiving");
				
				messages.add(new PrivateMessage(message_id, sender_id, sending, content, receiver_id, receiving));
			}
			
			res.close();
			
		} catch (SQLException e) {
			return null;
		}
		
		return messages;
	}
	
	public ArrayList<IMessage> getInbox() throws RemoteException {
		
		ArrayList<IMessage> messages = new ArrayList<>();
		String query = "SELECT distinct(CASE WHEN receiverid = ? THEN senderid ELSE receiverid END) as contact FROM (SELECT * FROM message where type = 'private' and (receiverid = ? or senderid = ?) and sending < now() ORDER BY sending DESC) as mymsg;";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_user_id, this.m_user_id, this.m_user_id);
		
			while (res.getResultSet().next())
				messages.add(this.getMessages(res.getResultSet().getInt("contact")).get(0));
			
			res.close();
			
		} catch (SQLException e) {
			return null;
		}
		
		return messages;
	}
	
	public void closeChatroom(int chatid) throws RemoteException {

		try {
			Chatroom c = Socialmore.instance.getChatroom(chatid);
			if (c.getOwner_id() != this.m_user_id)
				return;
			
			String query = "UPDATE chatroom SET isclosed = true WHERE chatid = ?";
			Socialmore.instance.dbc.noResponseQuery(query, chatid);
		} catch (Exception e) {
		}
	}
	
	public int getUnreadMessages() throws RemoteException {
		
		String query = "SELECT COUNT(*) FROM message WHERE type = 'private' and receiverid = ? and receiving is null and sending < now()";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_user_id);
			if (res.getResultSet().next())
				return res.getResultSet().getInt("count");
		} catch (SQLException e) {
		}

		return -1;
	}

	@Override
	public Boolean setFacebook(String username) throws RemoteException {
		String query = "UPDATE users SET facebookid = ? where userid = ?";
		try {
			Socialmore.instance.dbc.noResponseQuery(query, username, this.m_user_id);
			this.m_facebook = username;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	public String getPicture() throws RemoteException {
		
		if (m_facebook != null)
			return "http://graph.facebook.com/" + m_facebook + "/picture";
		return "http://gravatar.com/avatar/" + this.getEmailHash();
	}

	@Override
	public void setToken(String token) throws RemoteException {
		m_token = token;
	}

	@Override
	public String getToken() throws RemoteException {
		return m_token;
	}

	public IMessage getSentMessage(int messageid) throws RemoteException {
		
		String query = "select * from message where messageid = ? and senderid = ?";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, messageid, this.m_user_id);
		
			if (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int message_id	= rs.getInt("messageid");
				int sender_id	= rs.getInt("senderid");
				Date sending	= rs.getTimestamp("sending");
				String content	= rs.getString("content");
				int receiver_id	= rs.getInt("receiverid");
				Date receiving	= rs.getTimestamp("receiving");
				
				return new PrivateMessage(message_id, sender_id, sending, content, receiver_id, receiving);
			}
			
			res.close();
			
		} catch (SQLException e) {
			return null;
		}
		return null;
	}

	public boolean removeMessage(int messageid) throws RemoteException {

		String query; 

		try {
			Socialmore.instance.dbc.noResponseQuery("BEGIN");
			query = "DELETE FROM attach WHERE messageid = ?";
			Socialmore.instance.dbc.noResponseQuery(query, messageid);
			
			query = "DELETE FROM message WHERE messageid = ? and senderid = ?";
			Socialmore.instance.dbc.noResponseQuery(query, messageid, this.m_user_id);
			Socialmore.instance.dbc.noResponseQuery("COMMIT");
		} catch (SQLException e) {
			try {
				Socialmore.instance.dbc.noResponseQuery("ROLLBACK");
			} catch (SQLException e1) {
			}
			return false;
		}

		return true;
	}

	public ArrayList<IChatroom> getChatrooms() throws RemoteException {
		
		ArrayList<IChatroom> chatrooms = new ArrayList<>();
		
		try {
			String query = "select * from chatroom where ownerid = ?";
			
			DBResult res = Socialmore.instance.dbc.query(query, this.m_user_id);
			
			while (res.getResultSet().next()) {
				ResultSet rs 	 = res.getResultSet();
				int chat_id 	 = rs.getInt("chatid");
				int owner_id	 = rs.getInt("ownerid");
				String name 	 = rs.getString("name");
				int rating 		 = rs.getInt("rating");
				String subject 	 = rs.getString("subject");
				boolean is_closed= rs.getBoolean("isClosed");
				
				chatrooms.add(new Chatroom(chat_id, owner_id, name, rating, subject, is_closed));
			}
		} catch (SQLException e) {
			  
			return null;
		}
		
		return chatrooms;
	}
}
