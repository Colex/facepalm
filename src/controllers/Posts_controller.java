package controllers;

import java.io.File;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;

import rest.HttpRest;
import server.classes.*;
import server.rest.FacebookAPI;
import websocket.notifications.NotificationMessageInbound;

import java.util.ArrayList;

@MultipartConfig(location = "/EWorkspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/facepalm/assets/attaches", maxFileSize = 10485760L)
public class Posts_controller extends HttpRest {

	private static final long serialVersionUID = 3364780532571929206L;

	public Posts_controller() {
		super();
		params_count = 1;
	}
	
	@Override
	public void init() throws ServletException
	{
		
	}

	protected void index(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("Indexing page");
		if (request.getParameter("json") != null) {
			System.out.println(request.getParameter("json"));
		}
		User.matchNounce(request);
		
		
		if (User.getSession(request) != null) {
			forwardTo(request, response, "Users/index");
			return;
		}
		
		forwardTo(request, response, "Sessions/login");
	}

	protected void show(String [] params, HttpServletRequest request, HttpServletResponse response) {
		System.out.println("Showing page");
		if (request.getParameter("json") != null) {
			System.out.println(request.getParameter("json"));
		}
		
		User.matchNounce(request);
		
		
		if (User.getSession(request) != null) {
			forwardTo(request, response, "Users/index");
			return;
		}
		
		forwardTo(request, response, "Sessions/login");	
	}
	
	
	protected void create(String [] params, HttpServletRequest request, HttpServletResponse response) {
		IUser user;
		ISocialmore sm;
		MultipartMap map = null;
		Boolean right_nounce;
		
		sm		= Socialmore.connect(this);
		user 	= User.getSession(request);
		
		if (sm == null || user == null) {
			User.matchNounce(request);
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		try {
			map = new MultipartMap(request, this);
			String nounce 		= map.getParameter("nounce");
			String old_nounce	= (String)request.getSession().getAttribute("nounce");
			right_nounce 		= nounce.equals(old_nounce); 
			System.out.println(nounce + " - " + old_nounce);
		} catch (Exception e) {
			
			right_nounce = User.matchNounce(request);
		}
		
		User.matchNounce(request);
		
		if (!right_nounce) {
			System.out.println("I dislike that nounce!");
			forwardTo(request, response, "Users/index");
			return;
		}
		
		int		post_id = -1;
		try {
			post_id = Integer.parseInt(params[1]);
		} catch (Exception e) {}
		

		try {
			if (post_id > -1) {
				System.out.println("This is a comment");
				String 	content = request.getParameter("content");
				IPost post = sm.getPost(post_id);
				if (post != null && !content.isEmpty()) {
					user.reply(content, post_id);
					
					String token = (String)request.getSession().getAttribute("token");
					
					if (token != null && post.getFacebook() != null ) {
						FacebookAPI.comment(post.getFacebook(), token, content);
					}
				
					NotificationMessageInbound socket = (NotificationMessageInbound)request.getSession().getAttribute("socket");
					socket.newComment(post_id, post.getSenderID());
				}
			} else {
				ArrayList<String> urls = new ArrayList<>();
				int count = 0, i = 0;
				
				String 	content = map.getParameter("content");
				
				int attach_count;
				try {
					attach_count = Integer.parseInt(request.getParameter("attach_count"));
				} catch (Exception e) {
					attach_count = 0;
				}
				
				File file;
				for (i = 0; i < attach_count; i++) {
					System.out.println("Hello attach");
					try {
						file = map.getFile("attach" + i);
						urls.add("/assets/attaches/" + file.getName());
						count++;
					} catch (Exception e) {
					}
				}
				
				if (count > 0 || !content.isEmpty()) {
					System.out.println("Trying to post: " + content);
					int 	id = user.post(content, urls);
					String	token = (String)request.getSession().getAttribute("token");
					if (id > -1) {
						IPost post = sm.getPost(id);
						NotificationMessageInbound socket = (NotificationMessageInbound)request.getSession().getAttribute("socket");
						if (token != null) {
							System.out.println("Facebook Post");
							post.setFacebook(FacebookAPI.post("me", token, content));
							System.out.println("ID: " + post.getFacebook());
						}
						socket.newPost(id);
					}
				}
			}
		} catch (Exception e) {
			
			System.out.println("Exception on Create:Post");
		}

		forwardTo(request, response, "Users/index");
	}
	 
	protected void update(String [] params, HttpServletRequest request, HttpServletResponse response) {
		ISocialmore sm;
		IUser user;
		IPost post;
		
		sm		= Socialmore.connect(this);
		user 	= User.getSession(request);
		
		if (sm == null || user == null) {
			User.matchNounce(request);
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		if (!User.matchNounce(request)) {
			forwardTo(request, response, "Users/index");
			return;
		}
		
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
		int post_id = -1;
		
		HttpSession session = request.getSession();
		
		sm 		= Socialmore.connect(this);
		user 	= User.getSession(request);
		
		if (sm == null || user == null) {
			User.matchNounce(request);
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		try {
			post_id = Integer.parseInt(params[1]);
		} catch (Exception e) {}
		
		
		try {
			post = sm.getPost(post_id);
			if (post != null) {
				post.delete(user.getID());
				System.out.println("Deleted " + post_id);
				String fbid 	= post.getFacebook();
				String token 	= (String)session.getAttribute("token"); 
				if (token != null && fbid != null && !fbid.isEmpty()) {
					FacebookAPI.deletePost(fbid, token);
				}
			}
		} catch (RemoteException e) {
			
		}
		
		forwardTo(request, response, "Users/index");
	}
	
}
