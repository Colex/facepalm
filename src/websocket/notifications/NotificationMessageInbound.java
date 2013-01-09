package websocket.notifications;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;

import server.classes.ISocialmore;
import server.classes.IUser;
import websocket.notifications.NotificationsWebSocketServlet;
import com.google.gson.Gson;

public class NotificationMessageInbound extends MessageInbound {
    	
	private int 		userid;
	private String		picture;
	private String		picture50;
	private String 		name;
	private IUser 		user;
	private ISocialmore	sm;
	private HttpSession	session;
	private NotificationsWebSocketServlet mgmt;

    NotificationMessageInbound(NotificationsWebSocketServlet mgmt, HttpServletRequest request, IUser user, ISocialmore sm) {
    	
    	try {
    		this.mgmt		= mgmt;
        	this.user 		= user;
        	this.sm			= sm;
        	this.userid 	= user.getID();
			this.name 		= user.getName();
			this.picture 	= user.getPicture() + "?s=25";
			this.picture50 	= user.getPicture() + "?s=50";
			if (request != null) {
				this.session	= request.getSession();	
				this.session.setAttribute("socket", this);
			} else {
				this.session = null;
			}
		} catch (RemoteException e) {
			try {
				this.getWsOutbound().close(0, null);
			} catch (IOException e1) {
			}
			
		}
    }
    
    
    private void send(String message) {
    	try {	
    		CharBuffer buffer 	= CharBuffer.wrap(message);
			this.getWsOutbound().writeTextMessage(buffer);
		} catch (IOException e) {
		}
    }
    
    private static String notification(String data, String message, String extra, String method) {
    	return String.format("{\"data\": \"%s\", \"message\": \"%s\", \"extra\": \"%s\", \"method\": \"%s\"}", 
    			data, message, extra, method);
    }
    
    private static String notification(String data, String message, String extra, String extra2, String date, String method) {
    	return String.format("{\"data\": \"%s\", \"message\": \"%s\", \"extra\": \"%s\", \"extra2\": \"%s\", \"date\": \"%s\", \"method\": \"%s\"}", 
    			data, message, extra, extra2, date, method);
    }
    
    public void newPost(int id) {
    	String message = this.notification(id+"", name, picture, "post");
    	broadcast(message);
    }
    
    public void newComment(int postid, int ownerid) {
    	String message = this.notification(postid+"", name, picture, "comment");
    	broadcast(message, ownerid);
    	
    	message = this.notification(postid+"", name, picture, "mycomment");
    	sendTo(message, ownerid);
    }

    
    public static void connectOutsider(ISocialmore sm, NotificationsWebSocketServlet mgmt, IUser user) {
    	String message;
    	int userid ;
    	try {
    		userid = user.getID();
    	} catch (Exception e) {
    		return;
    	}
    	
        /* Silent connection (whenever the same user connects from a different client) */
        for (NotificationMessageInbound conn : mgmt.connections) {
    		if (conn.userid == userid) {
    			mgmt.connections.add(new NotificationMessageInbound(mgmt, null, user, sm));
    			return;
    		}
        }
        
        mgmt.connections.add(new NotificationMessageInbound(mgmt, null, user, sm));

        try {
			message = notification(userid+"", user.getName(), user.getPicture(), "online"); 
			broadcast(mgmt, message);
        } catch (RemoteException e) {
		}
    }
    
    @Override
    protected void onOpen(WsOutbound outbound) {
        String message;
        
        for (NotificationMessageInbound conn : mgmt.connections) {
            message = this.notification(conn.userid+"", conn.name, conn.picture, "online");
            this.send(message);
        }
        
        try {
			int unread = user.getUnreadMessages();
			if (unread == -1) unread = 0;
			message = this.notification(unread+"", "", "", "unread");
			this.send(message);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			
		}
        
        /* Silent connection (whenever the same user connects from a different client) */
        for (NotificationMessageInbound conn : mgmt.connections) {
    		if (conn.userid == this.userid) {
    			mgmt.connections.add(this);
    			return;
    		}
        }
        
        mgmt.connections.add(this);
        try {
			this.sm.addOnlineUser(user);
		} catch (RemoteException e) {
		}
        
        message = this.notification(userid+"", name, picture, "online");
        broadcast(message);
        
    }

    @Override
    protected void onClose(int status) {
        mgmt.connections.remove(this);
        try {
			this.sm.removeOnlineUser(user);
		} catch (RemoteException e) {
		}
        
        for (NotificationMessageInbound conn : mgmt.connections) {
    		if (conn.userid == this.userid)
    			return;
        }
        
        String message = this.notification(this.userid+"", "", "", "offline");
        broadcast(message);
       
    }

    @Override
    protected void onBinaryMessage(ByteBuffer message) throws IOException {
        throw new UnsupportedOperationException(
                "Binary message not supported.");
    }

    @Override
    protected void onTextMessage(CharBuffer message) throws IOException {
    	System.out.println(message.toString());
    	Gson g = new Gson();
    	DataObject a = g.fromJson(message.toString(), DataObject.class);
    	
    	try {
    		a.data = filter(a.data);
    		if (!a.data.trim().isEmpty()) {
    			DateFormat	df		= new SimpleDateFormat("hh:mm, d MMM yyyy");
    			DateFormat	df2		= new SimpleDateFormat("hh:mm dd/MM/yyyy");
    			Date 		date, now = new Date();
    			String []	attach 	= a.attaches.split(";");
    			
    			try {
					date = df2.parse(a.schedule);
					if (date.before(now))
						date = now;
				} catch (ParseException e) {
					
					date = now;
				}
    			
    			sm.addPrivateMessage(userid, a.target, date, a.data, attach);
    			if (date != now) return; 
    			
    			String msg = notification(userid+"", a.data, name, picture50, df.format(date), "private");
    			sendTo(msg, a.target);
    			
    			for (int i = 0; i < attach.length; i++)
    				a.data += "<br><a href='http://localhost:8080" + attach[i] +"'>"+ attach[i] +"</a>";
    			
    			System.out.println("Sending back: " + a.data);
    			
    			msg = notification(a.target+"", a.data, name, picture50, df.format(date), "myprivate");
    			sendTo(msg, userid);
    			
    			updateUnread(a.target);
    		}
    	} catch (RemoteException e) {
    		return;
    	}
    	
    }
    

    private void broadcast(String message) {
        for (NotificationMessageInbound connection : mgmt.connections) {
        	try {
        		if (connection.session != null) {
	                CharBuffer buffer = CharBuffer.wrap(message);
	                connection.getWsOutbound().writeTextMessage(buffer);
        		}
            } catch (IOException ignore) {
            }
        }
    }
    
    private static void broadcast(NotificationsWebSocketServlet  mgmt, String message) {
        for (NotificationMessageInbound connection : mgmt.connections) {
        	try {
        		if (connection.session != null) {
                	CharBuffer buffer = CharBuffer.wrap(message);
                	connection.getWsOutbound().writeTextMessage(buffer);
        		}
            } catch (IOException ignore) {
            }
        }
    }
    
    private void broadcast(String message, int except) {
        for (NotificationMessageInbound connection : mgmt.connections) {
        	try {
        		if (connection.userid != except) {
                	CharBuffer buffer = CharBuffer.wrap(message);
                	connection.getWsOutbound().writeTextMessage(buffer);
        		}
            } catch (IOException ignore) {
            }
        }
    }
    
    private void sendTo(String message, int userid) {
        for (NotificationMessageInbound connection : mgmt.connections) {
        	try {
        		if (connection.userid == userid) {
                	CharBuffer buffer = CharBuffer.wrap(message);
                	connection.getWsOutbound().writeTextMessage(buffer);
        		}
            } catch (IOException ignore) {
            }
        }
    }
    
    private void updateUnread(int userid) {
        for (NotificationMessageInbound connection : mgmt.connections) {
    		if (connection.userid == userid) {
    			try {
    				int unread = connection.user.getUnreadMessages();
    				if (unread == -1) unread = 0;
    				String message = this.notification(unread+"", "", "", "unread");
    				connection.send(message);
    			} catch (RemoteException e) {
    			}
    		}
        }
    }
    
    public String filter(String message) {
		if (message == null)
			return (null);
		// filter characters that are sensitive in HTML
		char content[] = new char[message.length()];
		message.getChars(0, message.length(), content, 0);
		StringBuilder result = new StringBuilder(content.length + 50);
		for (int i = 0; i < content.length; i++) {
			switch (content[i]) {
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '"':
				result.append("&quot;");
				break;
			default:
				result.append(content[i]);
			}
		}
		return (result.toString());
	}
}