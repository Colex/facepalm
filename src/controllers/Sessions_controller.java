package controllers;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.scribe.builder.api.FacebookApi;

import rest.HttpRest;
import server.classes.*;
import server.rest.FacebookAPI;
import server.rest.FacebookUser;

import java.util.ArrayList;
import java.util.Date;

public class Sessions_controller extends HttpRest {

	private static final long serialVersionUID = 3364780532571929206L;

	public Sessions_controller() {
		super();
		params_count = 1;
	}
	
	@Override
	public void init() throws ServletException
	{
		
	}

	protected void index(HttpServletRequest request, HttpServletResponse response) {
		ISocialmore sm = Socialmore.connect(this);
		
		if (sm == null) {
			request.getSession().setAttribute("user", null);
			request.getSession().setAttribute("token", null);
			request.getSession().setAttribute("sm", null);
			request.getSession().setAttribute("token_date", null);
			request.setAttribute("flash", "We are sorry! :( The server is temporarily unavailable...");
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		String [] urls = request.getRequestURI().split("/");
		if (urls[1].equalsIgnoreCase("recover")) {
			try {
				String code = request.getParameter("code");
				if (code != null) {
					if (sm.isRecoveryKeyValid(code)) {
						request.setAttribute("key", code);
					} else {
						request.setAttribute("flash_type", "alert-error");
						request.setAttribute("flash", "The recovery key is invalid!");
					}
				}
			} catch (RemoteException e) {
			}
			
			forwardTo(request, response, "Sessions/recover");
			return;
		}
	
		IUser 	user 		= null;
		String 	token 		= null;
		String code = request.getParameter("code");
		
		if (User.getSession(request) != null) {
			if (code != null && request.getSession().getAttribute("token") != null) {
				token 	= FacebookAPI.getToken(code);
				user 	= (IUser)request.getSession().getAttribute("user");
				try {
					user.setToken(token);
				} catch (RemoteException e) {
				}
				request.getSession().setAttribute("token", token);
				request.getSession().setAttribute("token_date", new Date());
			}
			forwardTo(request, response, "Users/index");
			return;
		}
		
		
		
		
		System.out.println("Code: " + code);
		
		
		
		if (code != null) {
			try {	
				token 	= FacebookAPI.getToken(code);
				user 	= sm.login(token); 
				System.out.println("Token: " + token);
				if (user == null) {
					user = sm.signup(token);
					user.setFacepalm(true);
					System.out.println("User: " + user);
				} else {
					user.setFacepalm(true);
				}
			} catch (Exception e) {
				
				forwardTo(request, response, "Sessions/login");
				return;
			}
		}
		
		if (token != null && user != null) {
			request.getSession().setAttribute("token", token);
			request.getSession().setAttribute("token_date", new Date());
			request.getSession().setAttribute("sm", sm);
			request.getSession().setAttribute("user", user);
			User.matchNounce(request);
			try {
				user.setToken(token);
			} catch (RemoteException e) {
			}
			forwardTo(request, response, "Users/index");
		} else {
			forwardTo(request, response, "Sessions/login");
		}
	}

	protected void show(String [] params, HttpServletRequest request, HttpServletResponse response) {
		if (User.getSession(request) != null) {
			forwardTo(request, response, "Users/index");
			return;
		}
		
		System.out.println("Show Login!");
		
		if (params[0].equals("login")) {
			String code = request.getParameter("code");
			System.out.println("Code: " + code);
			if (code != null) {
				String token = FacebookAPI.getToken(code);
				System.out.println(token);
			}
			forwardTo(request, response, "Sessions/login");	
		}
	}
	
	
	protected void create(String [] params, HttpServletRequest request, HttpServletResponse response) {
		ISocialmore socialmore;
		IUser user = null;
		HttpSession session = request.getSession();
		params = request.getRequestURI().split("/");
		if (User.getSession(request) != null) {
			forwardTo(request, response, "Users/index");
			return;
		}
		
		if (!User.matchNounce(request)) {
			forwardTo(request, response, "Users/new");
			return;
		}
		
		System.out.println("Register or recover!");
		
		String email = request.getParameter("email");
		String pass = request.getParameter("pass");
		
		socialmore = Socialmore.connect(this);
		
		if (socialmore == null) {
			System.out.println("Socialmore not found!");
			forwardTo(request, response, "Sessions/login");
			return;
		}
	
		
		if (email != null && params[1].equalsIgnoreCase("recover")) {
			try {
				if (!socialmore.resetPassword(email)) {
					request.setAttribute("flash_type", "alert-error");
					request.setAttribute("flash", "E-mail provided is invalid!");
				} else {
					request.setAttribute("flash_type", "alert-success");
					request.setAttribute("flash", "A <b>recovery link</b> was sent to your e-mail. ("+email.toLowerCase()+")");
				}
			} 	
			catch (RemoteException e) {
				request.setAttribute("flash", "Could not recover your password! Please try again...");
			}
			forwardTo(request, response, "Sessions/login");	
			return;
		}
		

		
		 try {
			 
			MessageDigest md5;
			try {
				md5 = MessageDigest.getInstance("MD5");
			
				md5.reset();
				md5.update(pass.getBytes());
				BigInteger bigint = new BigInteger(1, md5.digest());
				String hash = bigint.toString(16);
				while (hash.length() < 32) hash = "0" + hash;
				
				System.out.println("Try to login " + email + " with " + hash);
				
				user = socialmore.login(email, hash);
				if (user != null)
					user.setFacepalm(true);
				
				System.out.println("Logged as " + user);
			} catch (NoSuchAlgorithmException e) {
			}

			if (user != null) {
				User.matchNounce(request);
				session.setAttribute("user", user);
				session.setAttribute("sm", socialmore);
				forwardTo(request, response, "Users/index");
			} else {
				request.setAttribute("flash", "Wrong username or password!");
				forwardTo(request, response, "Sessions/login");
			}
			
		} catch (RemoteException e) {
			request.getSession().setAttribute("user", null);
			request.setAttribute("flash", "Could not login! Please try again...");
			forwardTo(request, response, "Sessions/login");
			return;
		}

	}
	
	
	protected void update(String [] params, HttpServletRequest request, HttpServletResponse response) {
		ISocialmore sm = Socialmore.connect(this);
		IUser user = null;
		String pass, pass_conf, key;
		
		if (sm == null) {
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		if (User.getSession(request) != null) {
			forwardTo(request, response, "Users/index");
			return;
		}
		
		
		
		pass 		= request.getParameter("pass");
		pass_conf 	= request.getParameter("pass_confirm");
		key 		= request.getParameter("key");
		request.setAttribute("key", key);
		
		if (!User.matchNounce(request)) {
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		if (pass == null || pass_conf == null || key == null || pass.isEmpty() || pass_conf.isEmpty()) {
			request.setAttribute("flash", "Please fill both fields (minimum 6 characters)...");
			forwardTo(request, response, "Sessions/recover");
			return;
		}
		
		if (pass.length() < 6) {
			request.setAttribute("flash", "The password has too be at least 6 characters long!");
			forwardTo(request, response, "Sessions/recover");
			return;
		}
		
		if (!pass.equals(pass_conf)) {
			request.setAttribute("flash", "Both passwords <b>must</b> match!");
			forwardTo(request, response, "Sessions/recover");
			return;
		}
		
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		
			md5.reset();
			md5.update(pass.getBytes());
			BigInteger bigint = new BigInteger(1, md5.digest());
			String hash = bigint.toString(16);
			while (hash.length() < 32) hash = "0" + hash;
			
			user = sm.recoveryLogin(key, hash);
			if (user == null) {
				request.setAttribute("flash", "The recovery key provided is wrong!");
				forwardTo(request, response, "Sessions/login");
				return;
			}
			user.setFacepalm(true);
		} catch (RemoteException | NoSuchAlgorithmException e) {
			request.setAttribute("flash", "Could not recover password! Please try again...");
			forwardTo(request, response, "Sessions/recover");
			return;
		}
		
		request.getSession().setAttribute("sm", sm);
		request.getSession().setAttribute("user", user);
		
		
		request.setAttribute("flash_type", "alert-success");
		request.setAttribute("flash", "Password successfuly recovered!");
		forwardTo(request, response, "Users/index");
	}
	
	
	

	protected void delete(String [] params, HttpServletRequest request, HttpServletResponse response) {
		request.getSession().setAttribute("user", null);
		request.getSession().setAttribute("token", null);
		request.getSession().setAttribute("sm", null);
		request.getSession().setAttribute("token_date", null);
		request.setAttribute("flash", "You have signed out successfuly!! <i>(not for long though)</i>");
		forwardTo(request, response, "Sessions/login");
	}
	
}
