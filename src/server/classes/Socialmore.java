package server.classes;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServlet;

import server.database.DBConnection;
import server.database.DBResult;
import server.rest.FacebookAPI;
import server.rest.FacebookUser;
import websocket.notifications.INotifications;

public class Socialmore extends UnicastRemoteObject implements ISocialmore, Runnable {
	
	private static final long serialVersionUID = 5832342171635360768L;
	
	public static Socialmore instance = null;
	
	Thread t;
	DBConnection dbc;
	EmailDispatcher maildisp;
	private LinkedBlockingQueue<Object[]> pool;
	private CopyOnWriteArrayList<IUser> users;
	private INotifications m_notifier;
	
	public Socialmore() throws RemoteException, SQLException {	
		
		Socialmore.instance = this;
		this.users = new CopyOnWriteArrayList<>();
		this.dbc = new DBConnection("postgres", "postgres", "---");
		this.maildisp = new EmailDispatcher("facepalmserver", "---");
		this.pool = new LinkedBlockingQueue<>();
		this.t = new Thread(this);
		t.start();
		m_notifier = null;
	}
	
	public void addPublicMessage(int chatid, int userid, Date date, String content, ArrayList<String> urls) throws RemoteException {
		
		Object[] data = new Object[6];
		
		data[0] = chatid;
		data[1] = userid;
		data[2] = date;
		data[3] = Util.filter(content);
		data[4] = "public";
		data[5] = urls;
		
		try {
			this.pool.put(data);
		} catch (InterruptedException e) {
		}
	}
	
public void addPrivateMessage(int userid, int targetid, Date date, String content, String [] urls) throws RemoteException {
		
		Object[] data = new Object[6];
		
		ArrayList<String> attaches = new ArrayList<>();
		for (String a : urls)
			attaches.add(a);
			
		data[0] = targetid;
		data[1] = userid;
		data[2] = date;
		data[3] = Util.filter(content);
		data[4] = "private";
		data[5] = attaches;
		
		try {
			this.pool.put(data);
		} catch (InterruptedException e) {
		}
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		
		Object[] data = null;
		
		while(true) {
			try {
				data = this.pool.take();
				this.send((int)data[0], (int)data[1], (Date)data[2], (String)data[3], (String)data[4], (ArrayList<String>) data[5]);
			} catch (Exception e) {
				
			}
		}
	}
	
	private boolean send(int chatid, int userid, Date date, String content, String type, ArrayList<String> urls) throws RemoteException {
		
		content = Util.filter(content);
		int messageid = -1;
		String query = "INSERT INTO message(senderid, sending, content, receiverid, type, last_activity) VALUES (?, ?, ?, ?, ?, ?) returning messageid";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, userid, date, content, chatid, type, date);
			
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
			return false;
		}		
		return true;
	}

	public IUser login(String email, String password) throws RemoteException {
		
		try {
			String query = "select * from users where lower(email) = lower(?) and password = ? and isActive = true";
			
			DBResult res = this.dbc.query(query, email, password);
			
			if (res.getResultSet().next()) {

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
				
				res.close();
				
				IUser u = (IUser)new User(userid, name, _email, city, country, bday, sex, _password, fbuser, isActive, isPublic);

				return u;
			}
			
			res.close();
			
		} catch (SQLException e) {
		}
		
		return null;
	}

	public IUser register(String name, String email, String city, String country, Date bday, char sex,
						String password, boolean isPublic) throws RemoteException {
		
		DBResult res;
		int userid = -1;
		User u = null;
		
		name = Util.filter(name);
		city = Util.filter(city);
		country = Util.filter(country);
		
		String query = "insert into users (name, email, city, country, bday, sex, password, isActive, isPublic) " +
						"values (?, ?, ?, ?, ?, ?, ?, true, ?) returning userid;";
		
		try {
			
			res = this.dbc.query(query, name, email, city, country, bday, sex+"", password, isPublic);
			
			if (res.getResultSet().next())
				userid = res.getResultSet().getInt("userid");
			
			if (userid != -1)
				u = new User(userid, name, email, city, country, bday, sex, password, null, true, isPublic);
			
			res.close();
			
			this.maildisp.add(email, "Welcome!", Util.getWelcomeMessage(name));
			return u;
			
		} catch (SQLException e) {
			return null;
		}
	}

	public IUser getUserByEmail(String email) throws RemoteException {
		
		try {
			String query = "select * from users where upper(email) = upper(?)";
			
			DBResult res = this.dbc.query(query, email);
			
			if (res.getResultSet().next()) {

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
				
				res.close();
				
				return new User(userid, name, _email, city, country, bday, sex, _password, fbuser, isActive, isPublic);
			}
		} catch (SQLException e) {
		}
		
		return null;
	}
	
	public IUser getUserByID(int user_id) throws RemoteException {
		
		try {
			String query = "select * from users where userid = ?";
			
			DBResult res = this.dbc.query(query, user_id);
			
			if (res.getResultSet().next()) {

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
				
				res.close();
				
				return new User(userid, name, _email, city, country, bday, sex, _password, fbuser, isActive, isPublic);
			}
		} catch (SQLException e) {
			
		}
		
		return null;
	}
	
	public ArrayList<IPost> getPosts() throws RemoteException {
		
		ArrayList<IPost> posts = new ArrayList<IPost>();
		
		String query = "select * from message where type = 'post' order by last_activity desc";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query);
			
			while (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int ID 			= rs.getInt("messageid");
				int sender		= rs.getInt("senderid");
				Date sending	= rs.getTimestamp("sending");
				String content	= rs.getString("content");
				int receiver	= rs.getInt("receiverid");
				String facebook	= rs.getString("facebookid");
				
				posts.add(new Post(ID, sender, receiver, content, sending, facebook));
			}
			
			res.close();
			
		} catch (SQLException e) {
			return null;
		}
		
		return posts;
	}
	
	
	public IPost getPost(int postid) throws RemoteException {
		
		String query = "select * from message where messageid = ?";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, postid);
			
			if (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int ID 			= rs.getInt("messageid");
				int sender		= rs.getInt("senderid");
				Date sending	= rs.getTimestamp("sending");
				String content	= rs.getString("content");
				int receiver	= rs.getInt("receiverid");
				String facebook	= rs.getString("facebookid");
				
				return new Post(ID, sender, receiver, content, sending, facebook);
			}
			
			res.close();
			
		} catch (SQLException e) {
		}
		return null;
	}

	public Post getPostFromFacebook(String fbid) {
		
		String query = "select * from message where facebookid = ?";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, fbid);
			
			if (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int ID 			= rs.getInt("messageid");
				int sender		= rs.getInt("senderid");
				Date sending	= rs.getTimestamp("sending");
				String content	= rs.getString("content");
				int receiver	= rs.getInt("receiverid");
				String facebook	= rs.getString("facebookid");
				
				try {
					return new Post(ID, sender, receiver, content, sending, facebook);
				} catch (RemoteException e) {
					return null;
				}
			}
			
			res.close();
			
		} catch (SQLException e) {
		}
		return null;
	}
	
	public Boolean ping() throws RemoteException {
		return true;
	}
	
	/*
	 *  Returns existing connection or a new connection if one does not exist.
	 */
	public static ISocialmore connect(HttpServlet req) {
		
		ISocialmore sm = (ISocialmore)req.getServletContext().getAttribute("sm");
		if (sm != null) {
			try {
				sm.ping();
			} catch (RemoteException e) {
				sm = null;
			}
		}
		
		if (sm == null) {
			try {
				sm = (ISocialmore)Naming.lookup ("rmi://localhost:7000/Socialmore");
				req.getServletContext().setAttribute("sm", sm);
			} catch (MalformedURLException | RemoteException
					| NotBoundException e) {
				req.getServletContext().setAttribute("sm", null);
				return null; //Could not connect to server
			}
		}
		
		return sm;
	}
	
	public ArrayList<IUser> searchUser(String stuff) throws RemoteException {
		
		ArrayList<IUser> users = new ArrayList<>();
		
		try {
			String query = "select distinct * from users where (upper(email) like upper(?) or upper(name) like upper(?) or upper(city) like upper(?)) and isActive = true and isPublic = true";
			
			stuff = Util.filter(stuff);

			stuff = "%"+stuff+"%";
			
			DBResult res = this.dbc.query(query, stuff, stuff, stuff);
			
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
			return null;
		}
		
		return users;
	}

	public ArrayList<IUser> searchUserByName(String stuff) throws RemoteException {
	
		ArrayList<IUser> users = new ArrayList<>();
		
		try {
			String query = "select distinct * from users where upper(name) like upper(?)  and isActive = true and isPublic = true";
			
			stuff = Util.filter(stuff);
	
			stuff = "%"+stuff+"%";
			
			DBResult res = this.dbc.query(query, stuff);
			
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
			return null;
		}
	
		return users;
	}
	
	public ArrayList<IUser> searchUserByEmail(String stuff) throws RemoteException {
		
		ArrayList<IUser> users = new ArrayList<>();
		
		try {
			String query = "select distinct * from users where upper(email) like upper(?)  and isActive = true and isPublic = true";
			
			stuff = Util.filter(stuff);
	
			stuff = "%"+stuff+"%";
			
			DBResult res = this.dbc.query(query, stuff);
			
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
			return null;
		}
		
		return users;
	}
	
	public ArrayList<IUser> searchUserByCountry(String stuff) throws RemoteException {
		
		ArrayList<IUser> users = new ArrayList<>();
		
		try {
			String query = "select distinct * from users where upper(country) like upper(?) and isActive = true and isPublic = true";
			
			stuff = Util.filter(stuff);
	
			stuff = "%"+stuff+"%";
			
			DBResult res = this.dbc.query(query, stuff);
			
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
			return null;
		}
		
		return users;
	}
	
	public ArrayList<IUser> searchUserByCity(String stuff) throws RemoteException {
		
		ArrayList<IUser> users = new ArrayList<>();
		
		try {
			String query = "select distinct * from users where upper(city) like upper(?) and isActive = true and isPublic = true";
			
			stuff = Util.filter(stuff);
	
			stuff = "%"+stuff+"%";
			
			DBResult res = this.dbc.query(query, stuff);
			
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
			return null;
		}
		
		return users;
	}

	public ArrayList<IChatroom> getChatrooms() throws RemoteException {
		
		ArrayList<IChatroom> chatrooms = new ArrayList<>();
		
		try {
			String query = "select * from chatroom";
			
			DBResult res = this.dbc.query(query);
			
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
	
	public ArrayList<IChatroom> getChatrooms(String subject) throws RemoteException {
		
		ArrayList<IChatroom> chatrooms = new ArrayList<>();
		
		try {
			String query = "select * from chatroom where subject = ?";
			
			DBResult res = this.dbc.query(query, subject);
			
			while (res.getResultSet().next()) {
				ResultSet rs 	 = res.getResultSet();
				int chat_id 	 = rs.getInt("chatid");
				int owner_id	 = rs.getInt("ownerid");
				String name 	 = rs.getString("name");
				int rating 		 = rs.getInt("rating");
				boolean is_closed= rs.getBoolean("isClosed");
				
				chatrooms.add(new Chatroom(chat_id, owner_id, name, rating, subject, is_closed));
			}
		} catch (SQLException e) {
			
			return null;
		}
		
		return chatrooms;
	}
	
	public Chatroom getChatroom(int chatid) throws RemoteException {
		
		try {
			String query = "select * from chatroom where chatid = ?";
			
			DBResult res = this.dbc.query(query, chatid);
			
			if (res.getResultSet().next()) {
				ResultSet rs 	 = res.getResultSet();
				int chat_id 	 = rs.getInt("chatid");
				int owner_id	 = rs.getInt("ownerid");
				String name 	 = rs.getString("name");
				String subject 	 = rs.getString("subject");
				int rating 		 = rs.getInt("rating");
				boolean is_closed= rs.getBoolean("isClosed");
				
				return new Chatroom(chat_id, owner_id, name, rating, subject, is_closed);
			}
			
		} catch (SQLException e) {
			
			return null;
		}
		return null;
	}
	
	public boolean resetPassword(String email) throws RemoteException {
		
		String query, name = null, hash;
		
		try {
			while(true) {
				
				hash = Util.generateString();
				query = "SELECT userid FROM users WHERE recovering = ?";
				DBResult res = this.dbc.query(query, hash);
				if (res.getResultSet().next()) {
					System.out.println("got conflict! USERID:[" + res.getResultSet().getInt("userid") + "]");
				} else {
					break;
				}
			}
			
			query = "SELECT name FROM users WHERE email = ?";
			DBResult res = this.dbc.query(query, email);
			
			
			
			if (res.getResultSet().next()) 
				name = res.getResultSet().getString("name");
			
			if (name == null)
				return false;
			
			query = "UPDATE users SET recovering = ? WHERE email = ?";
			this.dbc.noResponseQuery(query, hash, email);
			this.maildisp.add(email, "Password Recovery", Util.getChangePassword(name, hash));
			Util.print("Mail sent to recover password from " + email);
			
			
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	public IUser login(String token) throws RemoteException {
		
		System.out.println("Token: " + token);
		FacebookUser u = FacebookAPI.getUser("me", token);
		
		System.out.println("FB User: " + u);
		String fbusername = u.getId();
		
		try {
			String query = "select * from users where lower(facebookid) = lower(?)";
			
			DBResult res = this.dbc.query(query, fbusername);
			
			if (res.getResultSet().next()) {

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
				
				res.close();
				
				IUser user = (IUser)new User(userid, name, _email, city, country, bday, sex, _password, fbuser, isActive, isPublic);
				user.setFacebook(fbusername);
				
				return user;
			}
			
			res.close();
			
		} catch (SQLException e) {
		}
		
		return null;
	}
	
	public IUser signup(String token) throws RemoteException {
		
		FacebookUser u = FacebookAPI.getUser("me", token);
		
		try {
			IUser user = register(u.getName(), u.getId() + "@facebook.com", u.getCity(), u.getCountry(), new Date(), u.getGender().charAt(0), "", true);
			
			if (user == null) {
				return null;
			}
			
			user.setFacebook(u.getId());
			return user;
		} catch (RemoteException e) {	
		}
		
		return null;
	}
	
	public ArrayList<IUser> getOnlineUsers() throws RemoteException {
		ArrayList<IUser> online = new ArrayList<>();
		System.out.println("Check if there are any users online");
		for (IUser u : users) {
			if (u.ping()) {
				online.add(u);
			} 
		}
		System.out.println("Return online users");
		return online;
	}
	
	public IUser recoveryLogin(String hash, String password) throws RemoteException {
		
		String query;
		DBResult res;
		int id = -1;
		
		try {
			this.dbc.noResponseQuery("BEGIN");
			query = "SELECT userid FROM users WHERE recovering = ?";
			res = this.dbc.query(query, hash);
		
			if (res.getResultSet().next()) {
				id = res.getResultSet().getInt("userid");
			}
			
			query = "UPDATE users SET recovering = NULL WHERE userid = ?";
			IUser u = this.getUserByID(id);
			this.dbc.noResponseQuery(query, id);
			
			query = "UPDATE users SET password = ? WHERE userid = ?";
			this.dbc.noResponseQuery(query, password, id);
			this.dbc.noResponseQuery("COMMIT");
			return u;
		
		} catch (SQLException e) {
			try {
				this.dbc.noResponseQuery("ROLLBACK");
			} catch (SQLException e1) {
				System.out.println("Holy crap :c");
			}
		}
		
		return null;
	}
	
	public boolean isRecoveryKeyValid(String hash) throws RemoteException {
		
		int id = -1;
		
		String query = "SELECT userid FROM users WHERE recovering = ?";
		try {
			DBResult res = this.dbc.query(query, hash);
			if (res.getResultSet().next()) {
				id = res.getResultSet().getInt("userid");
			}
			if (id != -1)
				return true;
			
		} catch (SQLException e) {
		}
		
		return false;
	}

	@Override
	public void addOnlineUser(IUser user) throws RemoteException {
		users.add(user);
		System.out.println("Notifier: " + m_notifier);
		if (!user.isFacepalm() && m_notifier != null)
			m_notifier.notifyOnlineUser((IUser)user);
	}

	@Override
	public void removeOnlineUser(IUser user) throws RemoteException {
		users.remove(user);
	}

	@Override
	public void setSocialNotifier(INotifications notifier)
			throws RemoteException {
		m_notifier = notifier;
	}
	
	@Override
	public ArrayList<IChatroom> searchChatrooms(String name) throws RemoteException {
		ArrayList<IChatroom> chatrooms = new ArrayList<>();
		
		try {
			String query = "select * from chatroom where name like ?";
			
			DBResult res = this.dbc.query(query, "%"+name+"%");
			
			while (res.getResultSet().next()) {
				ResultSet rs 	 = res.getResultSet();
				int chat_id 	 = rs.getInt("chatid");
				int owner_id	 = rs.getInt("ownerid");
				name 			 = rs.getString("name");
				String subject	 = rs.getString("subject");
				int rating 		 = rs.getInt("rating");
				boolean is_closed= rs.getBoolean("isClosed");
				
				chatrooms.add(new Chatroom(chat_id, owner_id, name, rating, subject, is_closed));
			}
		} catch (SQLException e) {
			
			return null;
		}
		
		return chatrooms;
	}
	

	public static void main(String[] args) {
		try {
			Socialmore s = new Socialmore();
			
			IUser u = s.login("jooaooferreira@gmail.com",Util.crypt("123456"));
			
			if (u == null) {
				System.out.println("error loggin in");
				return;
			}
			
			u.vote(2, 1);
			
		} catch (Exception e) {
			
			return;
		}
	}

}
