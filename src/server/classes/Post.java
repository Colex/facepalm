package server.classes;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import server.database.DBResult;

public class Post extends Message implements IPost {

	private static final long serialVersionUID = 2556549160715651860L;

	public Post(int ID, int sender, int receiver, String content, Date sending) throws RemoteException {
		super(ID, sender, receiver, content, sending, null);
	}
	
	public Post(int ID, int sender, int receiver, String content, Date sending, String facebook) throws RemoteException {
		super(ID, sender, receiver, content, sending, facebook);
	}

	public boolean edit(String s) throws RemoteException {
		
		String query = "UPDATE message SET content = ? where messageid = ?";
		
		try {
			Socialmore.instance.dbc.noResponseQuery(query, s, this.m_message_id);
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	public ArrayList<Comment> getComments() throws RemoteException {
		ArrayList<Comment> comments = new ArrayList<>();

		String query = "select * from comment where postid = ? order by sending";
		
		try {
			DBResult res = Socialmore.instance.dbc.query(query, this.m_message_id);
			
			while (res.getResultSet().next()) {
				ResultSet rs 	= res.getResultSet();
				int comment_id	= rs.getInt("commentid");
				int post_id 	= rs.getInt("postid");
				int user_id 	= rs.getInt("userid");
				Date sending 	= rs.getTimestamp("sending");
				String content 	= rs.getString("content");

				comments.add(new Comment(comment_id, post_id, user_id, sending, content));
			}
			
			res.close();
			
		} catch (SQLException e) {
			return null;
		}
		
		return comments;
	}
	
	public boolean delete(int userid) throws RemoteException {
		
		if (this.m_sender_id != userid) {
			System.out.println("cant delete: you aint owner!");
			return false;
		}
		
		String query;

		try {
			Socialmore.instance.dbc.noResponseQuery("BEGIN;");
			
			query = "DELETE FROM comment WHERE postid = ?";
			Socialmore.instance.dbc.noResponseQuery(query, this.m_message_id);
			
			query = "DELETE FROM attach WHERE messageid = ?";
			Socialmore.instance.dbc.noResponseQuery(query, this.m_message_id);
			
			query = "DELETE FROM message WHERE messageid = ?";
			Socialmore.instance.dbc.noResponseQuery(query, this.m_message_id);
			
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
	
	public boolean edit(int userid, String content) throws RemoteException {
		
		if (this.m_sender_id != userid) {
			System.out.println("cant edit: you aint owner!");
			return false;
		}

		try {			
			String query = "UPDATE message SET content = ? WHERE messageid = ?";
			Socialmore.instance.dbc.noResponseQuery(query, content, this.m_message_id);
		} catch (SQLException e) {
			return false;
		}		
		return true;
	}
	
	@Override
	public String toString() {
		return "Post [m_message_id=" + m_message_id + ", m_sender_id="
				+ m_sender_id + ", m_receiver_id=" + m_receiver_id
				+ ", m_content=" + m_content + ", m_sending=" + m_sending + "]";
	}
}
