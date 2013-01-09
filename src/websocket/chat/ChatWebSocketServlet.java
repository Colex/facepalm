/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package websocket.chat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import server.classes.Attach;
import server.classes.IChatroom;
import server.classes.IMessage;
import server.classes.ISocialmore;
import server.classes.IUser;
import server.classes.PublicMessage;
import server.classes.Socialmore;
import server.classes.User;


/**
 * Example web socket servlet for chat.
 */
public class ChatWebSocketServlet extends WebSocketServlet {

    private static final long serialVersionUID = 1L;


    private final AtomicInteger connectionIds = new AtomicInteger(0);
    private final Set<ChatMessageInbound> connections =
            new CopyOnWriteArraySet<ChatMessageInbound>();

    
    @Override
    protected StreamInbound createWebSocketInbound(String subProtocol,
            HttpServletRequest request) {
    	int chatid;
    	String url = request.getRequestURI();
    	String [] params = url.split("/");
    	IUser user;
    	ISocialmore sm;
    	
    	user 	= User.getSession(request);
    	sm 		= Socialmore.connect(this);
    	if (sm == null || user == null) {
    		return null;
    	}
    	
    	try {
    		chatid = Integer.parseInt(params[2]);
    	} catch (NumberFormatException e) {
    		return null;
    	}
    	
        return new ChatMessageInbound(chatid, user, sm);
    }

    private final class ChatMessageInbound extends MessageInbound {
    	
    	private int 		chatid;
    	private int 		userid;
    	private String		picture;
    	private String 		name;
    	private IUser 		user;
    	private ISocialmore	sm;

        private ChatMessageInbound(int id, IUser user, ISocialmore sm) {
        	
        	try {
	        	this.chatid 	= id;
	        	this.user 		= user;
	        	this.sm			= sm;
	        	this.userid 	= user.getID();
				this.name 		= user.getName();
				this.picture 	= user.getPicture() + "?s=25";
				
				
				
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
        
        private int getChatID() {
        	return chatid;
        }
        
        @Override
        protected void onOpen(WsOutbound outbound) {
            String message;
            
            try {
            	DateFormat df;
            	String filteredMessage;
				IChatroom chat = sm.getChatroom(chatid);
				
				if (chat.isClosed()) {
					df = new SimpleDateFormat("hh:mm, dd MMM");
					for (IMessage m : chat.getMessages()) {
			            filteredMessage = String.format(
			            		"<div class=\"row-fluid\" style=\"margin-top: 5px;\"><div class=\"span10\" style=\"word-wrap: break-word; word-break: break-all;\"><a href=\"\\user/%d\"><img src=\"%s\" width=\"25px\" height=\"25px\" /></a><c style=\"position: relative; top: 2px;\"> <a href=\"\\user/%d\"><b>%s:</b></a> %s</c></div><div class=\"span2\" style=\"float: right; font-size: 10px; color: gray;\">%s</div></div>",
			                    m.getID(), m.getSender().getPicture(), m.getSenderID(), m.getSender().getName(), filter(m.getContent()), df.format(m.getSending()));
			            filteredMessage += "<table><tbody><tr>";
			            for (Attach a : m.getAttachments()) {
			            	filteredMessage += "<td><a href=\""+a.getFile_path()+"\"><img class=\"attach_img\" src=\""+a.getFile_path()+"\" width=\"60px\" /><br></a></td>";
			            }
			            filteredMessage += "</tr></tbody></table>";
			            this.send(filteredMessage);
					}
					return;
				}
			} catch (RemoteException e) {
				return;
			}
            
            
            for (ChatMessageInbound conn : connections) {
            	if (this.chatid == conn.getChatID()) {
            		message = String.format("+%d;%s;%s", conn.userid, conn.name, conn.picture);
                    this.send(message);
            	}
            }
            
            /* Silent connection (whenever the same user connects from a different client) */
            for (ChatMessageInbound conn : connections) {
            	if (conn.getChatID() == this.chatid) {
            		if (conn.userid == this.userid) {
            			connections.add(this);
            			return;
            		}
            	}
            }
            
            connections.add(this);
            
            String new_user = String.format("+%d;%s;%s",
            		userid, name, picture);
            broadcast(new_user);
            
            message = String.format("<b><i>%s %s</i></b>",
                    name, "has joined.");
            broadcast(message);
        }

        @Override
        protected void onClose(int status) {
            connections.remove(this);
            
            for (ChatMessageInbound conn : connections) {
            	if (conn.getChatID() == this.chatid) {
            		if (conn.userid == this.userid)
            			return;
            	}
            }
            
            String message = String.format("-%d",userid);
            broadcast(message);
            
            message = String.format("<b><i>%s has left.</i></b>", name);
            broadcast(message);
        }

        @Override
        protected void onBinaryMessage(ByteBuffer message) throws IOException {
            throw new UnsupportedOperationException(
                    "Binary message not supported.");
        }

        @Override
        protected void onTextMessage(CharBuffer message) throws IOException {
            // Never trust the client
        	DateFormat df = new SimpleDateFormat("hh:mm, dd MMM");
        	String [] msgs = message.toString().split(";");
            String filteredMessage = String.format(
            		"<div class=\"row-fluid\" style=\"margin-top: 5px;\"><div class=\"span10\" style=\"word-wrap: break-word; word-break: break-all;\"><a href=\"\\user/%d\"><img src=\"%s\" width=\"25px\" height=\"25px\" /></a><c style=\"position: relative; top: 2px;\"> <a href=\"\\user/%d\"><b>%s:</b></a> %s</c></div><div class=\"span2\" style=\"float: right; font-size: 10px; color: gray;\">%s</div></div>",
                    userid, picture, userid, name, filter(msgs[0]), df.format(new Date()));
            
            ArrayList<String> attaches = new ArrayList<>();
            filteredMessage += "<table><tbody><tr>";
            for (int i = 1; i < msgs.length; i++) {
            	filteredMessage += "<td><a href=\""+msgs[i]+"\"><img class=\"attach_img\" src=\""+msgs[i]+"\" width=\"60px\" /><br></a></td>";
            	attaches.add(msgs[i]);
            }
            filteredMessage += "</tr></tbody></table>";
            
            try {
            	IChatroom	room 	= sm.getChatroom(chatid);
            	if (!room.getUserRole(userid).equalsIgnoreCase("WATCHER") && !room.isClosed())
					sm.addPublicMessage(chatid, userid, new Date(), filter(message.toString()), attaches);
            	else
            		return;
            } catch (Exception e) {
				System.out.println("[Error] Could not access Socialmore...");
			}
            
            broadcast(filteredMessage);
        }

        private void broadcast(String message) {
            for (ChatMessageInbound connection : connections) {
                try {
                	if (this.chatid == connection.getChatID()) {
	                    CharBuffer buffer = CharBuffer.wrap(message);
	                    connection.getWsOutbound().writeTextMessage(buffer);
                	}
                } catch (IOException ignore) {
                    // Ignore
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
}