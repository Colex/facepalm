package controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;

import rest.HttpRest;
import server.classes.*;

import java.util.ArrayList;
import java.util.Date;

@MultipartConfig(location = "/EWorkspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/facepalm/assets/attaches", maxFileSize = 10485760L)
public class Messages_controller extends HttpRest {

	private static final long serialVersionUID = 3364780532571929206L;

	public Messages_controller() {
		super();
		params_count = 1;
	}
	
	@Override
	public void init() throws ServletException
	{
		
	}

	protected void index(HttpServletRequest request, HttpServletResponse response) {
		ISocialmore	sm		= Socialmore.connect(this);
		IUser 		user 	= User.getSession(request);
		
		
		if (sm == null || user == null) {
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		try {
			ArrayList<IMessage> inbox = (ArrayList<IMessage>)user.getInbox();
			request.setAttribute("inbox", inbox);
		} catch (RemoteException e) {
			request.setAttribute("flash", "We are sorry! :( Your inbox is temporaly unavailable...");
			forwardTo(request, response, "Users/index");
		}
		
		forwardTo(request, response, "Messages/index");
	}

	protected void show(String [] params, HttpServletRequest request, HttpServletResponse response) {
		
		ISocialmore sm = Socialmore.connect(this);
		if (sm == null || User.getSession(request) == null) {
			forwardTo(request, response, "Sessions/login");
			return;
		}
	
		IUser dest;
		try {
			dest = sm.getUserByID(Integer.parseInt(params[1]));
		} catch (NumberFormatException | RemoteException e) {
			forwardTo(request, response, "Users/index");
			return;
		}
		request.setAttribute("dest", dest);
		
		
		index(request, response);
	}
	
	
	protected void create(String [] params, HttpServletRequest request, HttpServletResponse response) {
		PrintWriter writer;		
		IUser user;
		ISocialmore sm;
		Integer count;
		MultipartMap map;
		ArrayList<String> attaches = new ArrayList<>();
		
		sm		= Socialmore.connect(this);
		user 	= User.getSession(request);
		
		try {
			map = new MultipartMap(request, this);
			
			writer = response.getWriter();

			count	= Integer.parseInt(map.getParameter("count"));
			
			if (count == null || user == null || sm == null) {
				writer.write("");
				return;
			}
			
			
			File file;
			for (int i = 0; i < count; i++) {
				file = map.getFile("attach" + i);
				writer.write("/assets/attaches/" + file.getName() + ";");
			}
		} catch (IOException | ServletException e1) {
		}
	}
	 
	protected void update(String [] params, HttpServletRequest request, HttpServletResponse response) {
		ISocialmore sm;
		IUser user;
		IPost post;
		HttpSession session = request.getSession();
		
		if (session.getAttribute("user") == null) {
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		sm		= Socialmore.connect(this);
		user 	= User.getSession(request);
		
		int		post_id = -1;
		try {
			post_id = Integer.parseInt(params[1]);
		} catch (Exception e) {}
		
		String 	content = request.getParameter("content");

		try {
			post = sm.getPost(post_id);
			
			if (post != null && !content.isEmpty()) {
				System.out.println("Edit: " + user.getID() + " with " + content);
				post.edit(user.getID(), content);
				System.out.println("Edited!!! :D");
			}
		} catch (RemoteException e) {
			System.out.println("Fail!");
		}
		
		forwardTo(request, response, "Users/index");
	}
	
	protected void delete(String [] params, HttpServletRequest request, HttpServletResponse response) {
		IUser user;
		ISocialmore sm;
		IPost post;
		IMessage msg;
		int msg_id, post_id = -1;
		
		HttpSession session = request.getSession();
		
		sm 		= Socialmore.connect(this);
		user 	= User.getSession(request);
		
		
		if (sm == null || user == null) {
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		if (params[0].equalsIgnoreCase("cancel-message")) {
			try {
				ArrayList<IMessage> inbox = (ArrayList<IMessage>)user.getInbox();
				request.setAttribute("inbox", inbox);
			} catch (RemoteException e1) {
				forwardTo(request, response, "Users/index");
				return;
			}
			
			try {
				msg_id = Integer.parseInt(params[1]);
				msg = user.getSentMessage(msg_id);
				if (user.getID() == msg.getSenderID() && msg.getReceiving() == null) {
					if (user.removeMessage(msg_id)) {
						request.setAttribute("flash_type", "alert-success");
						request.setAttribute("flash", "Your scheduled message was cancelled!");
						forwardTo(request, response, "Messages/index");
						return;
					}
				}
			} catch (Exception e) {
				
			}
			
			request.setAttribute("flash_type", "alert-error");
			request.setAttribute("flash", "It was <b>not</b> possible to remove the schedule message specified");
			forwardTo(request, response, "Messages/index");
			return;
		}
		
		try {
			post_id = Integer.parseInt(params[1]);
		} catch (Exception e) {}
		
		
		
		try {
			post = sm.getPost(post_id);
			if (post != null) {
				post.delete(user.getID());
				System.out.println("Delete " + post_id);
			}
		} catch (RemoteException e) {
			
		}
		
		forwardTo(request, response, "Users/index");
	}
	
}
