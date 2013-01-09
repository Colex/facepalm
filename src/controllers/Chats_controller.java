package controllers;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;

import rest.HttpRest;
import server.classes.*;

public class Chats_controller extends HttpRest {

	private static final long serialVersionUID = 3364780532571929206L;

	public Chats_controller() {
		super();
		params_count = 1;
	}
	

	@Override
	public void init() throws ServletException
	{
	}

	protected void index(HttpServletRequest request, HttpServletResponse response) {
		ISocialmore sm = Socialmore.connect(this);
				
		if (sm == null || User.getSession(request) == null)
			forwardTo(request, response, "StaticPages/index");
		else {
			try {
				ArrayList<IChatroom> rooms = sm.getChatrooms();
				request.setAttribute("rooms", rooms);
				System.out.println(rooms);
				forwardTo(request, response, "Chats/index");
			} catch (RemoteException e) {
				
				forwardTo(request, response, "Users/index");
			}
		}
	}

	protected void show(String [] params, HttpServletRequest request, HttpServletResponse response) {
		int 	chatid = -1;
		IUser 	user;
		ISocialmore sm;
		IChatroom 	room;
		IUser		owner;
		
		System.out.println("im here");
		
		sm		= Socialmore.connect(this);
		user 	= User.getSession(request);
		if (sm == null || user == null) {
			forwardTo(request, response, "StaticPages/index");
			return;
		}
		
		try {
			chatid = (int)request.getAttribute("chatid");
		} catch (NullPointerException e) {
			chatid = -1;
		}
		
		try {
			if (chatid == -1)
    			chatid 	= Integer.parseInt(params[1]);
    		room	= sm.getChatroom(chatid);
    		if (room == null) {
    			request.setAttribute("flash", "Chat room not found :'(");
    			forwardTo(request, response, "Chats/index");
    			return;
    		}
    		owner 	= sm.getUserByID(room.getOwner_id());
    		request.setAttribute("chatid", chatid);
    		request.setAttribute("owner_name", owner.getName());
    		request.setAttribute("owner", owner);
    		request.setAttribute("ownerid", room.getOwner_id());
    		request.setAttribute("title", room.getName());
    		request.setAttribute("room", room);
    		request.setAttribute("role", room.getUserRole(user.getID()));
    	} catch (NumberFormatException | RemoteException e) {
    		forwardTo(request, response, "Chats/index");
    		return;
    	} 
		
    	
		forwardTo(request, response, "Chats/show");
	}
	
	@Override
	protected void create(String[] params, HttpServletRequest request,
			HttpServletResponse response) {
		int 	chatid;
		IUser 	user;
		ISocialmore sm;
		String title, category;
		
		sm		= Socialmore.connect(this);
		user 	= User.getSession(request);
		if (sm == null || user == null) {
			forwardTo(request, response, "StaticPages/index");
			return;
		}
		
		title 		= request.getParameter("title");
		category	= request.getParameter("category");
		
		if (title.trim().isEmpty() || category.trim().isEmpty()) {
			request.setAttribute("flash", "Could <b>not</b> create the chat room! (Every field is mandatory)");
			forwardTo(request, response, "Chats/index");
			return;
		}
		
		try {
			chatid = user.createChatroom(category, title);
			if (chatid > -1) {
				request.setAttribute("flash", "Your chat room was sucessfuly created! :D Have fun!");
				request.setAttribute("flash_type", "alert-success");
				request.setAttribute("chatid", chatid);
				show(params, request, response);
				return;
			}
		} catch (RemoteException e) {
		}
		
		request.setAttribute("flash", "There was a problem creating the chat room! :( Please try again...");
		forwardTo(request, response, "Chats/index");
	}
	
	@Override
	protected void update(String[] params, HttpServletRequest request,
			HttpServletResponse response) {
		System.out.println("Updating: " + request.getParameter("userid") + " to " + request.getParameter("role") + " at " + request.getParameter("chatid"));
		ISocialmore sm;
		IUser owner, user;
		IChatroom room;
		
		sm 		= Socialmore.connect(this);
		owner 	= User.getSession(request);
		Integer chatid 	= Integer.parseInt(request.getParameter("chatid"));
		
		if (sm == null || owner == null || chatid == null) return;
		
		String vote_str = request.getParameter("vote");
		if (vote_str != null) {
			try {
				Integer vote = Integer.parseInt(vote_str);
				if (vote < 1 || vote > 3) return;
				owner.vote(chatid, vote);
			} catch (Exception e) {
			}
			return;
		}
		
		String title = request.getParameter("title");
		if (title != null) {
			if (title.trim().isEmpty()) return;
			try {
				room = sm.getChatroom(chatid);
				if (room == null || owner.getID() != room.getOwner_id()) return;
				room.setName(title);
			} catch (RemoteException e) {
			}
			return;
		}
		
		Integer userid 	= Integer.parseInt(request.getParameter("userid"));
		String role		= request.getParameter("role");
		
		if (userid == null || role == null) return;
		
		try {
			room = sm.getChatroom(chatid);
			user = sm.getUserByID(userid);
			if (room == null || user == null || owner.getID() != room.getOwner_id() || owner.getID() == userid) return;
			room.setUserRole(owner, userid, role);
		} catch (RemoteException e) {
		}
		
		System.out.println("Updating: " + request.getParameter("userid") + " to " + request.getParameter("role") + " at " + request.getParameter("chatid"));
	}

	@Override
	protected void delete(String[] params, HttpServletRequest request,
			HttpServletResponse response) {
		ISocialmore sm;
		IUser owner;
		IChatroom room;
		
		sm 		= Socialmore.connect(this);
		owner 	= User.getSession(request);
		Integer chatid 	= Integer.parseInt(request.getParameter("chatid"));
		
		if (sm == null || owner == null || chatid == null) return;
		try {
			room = sm.getChatroom(chatid);
			if (room == null || room.getOwner_id() != owner.getID()) return;
			owner.closeChatroom(chatid);
		} catch (Exception e) {
		}
		
	
			
	}
}

