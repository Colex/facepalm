package controllers;
import java.io.*;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.*;

import server.classes.Util;

import rest.HttpRest;
import server.classes.ISocialmore;
import server.classes.IUser;
import server.classes.Socialmore;
import server.classes.User;


public class Users_controller extends HttpRest {

	private static final long serialVersionUID = 3364780532571929206L;

	public Users_controller() {
		super();
		params_count = 0;
	}
	
	@Override
	public void init() throws ServletException
	{
	}

	@Override
	protected void index(HttpServletRequest request, HttpServletResponse response) {
		ISocialmore sm = Socialmore.connect(this);
		IUser user = User.getSession(request);
		ArrayList<IUser> users = null;
		
		if (sm == null || user == null) {
			forwardTo(request, response, "StaticPages/index");
			return;
		}
		
		String search = request.getParameter("search");
		if (search == null || search.trim().isEmpty()) {
			forwardTo(request, response, "Users/index");
			return;
		}
		
		String filter = request.getParameter("filter");
		filter = filter == null ? "all" : filter.toLowerCase();
		
		try {
			if (filter.equals("all")) users = sm.searchUser(search);
			if (filter.equals("name")) users = sm.searchUserByName(search);
			if (filter.equals("email")) users = sm.searchUserByEmail(search);
			if (filter.equals("city")) users = sm.searchUserByCity(search);
			if (filter.equals("country")) users = sm.searchUserByCountry(search);
			
			request.setAttribute("users", users);
			
			forwardTo(request, response, "Users/search");
			return;
		} catch (RemoteException e) {
			
		}
		
		forwardTo(request, response, "Users/index");
	}
	
	@Override
	protected void show(String [] params, HttpServletRequest request, HttpServletResponse response) {
		ISocialmore sm = Socialmore.connect(this);
		IUser u = User.getSession(request);
		
		if (params[0].equals("signup")) {
			forwardTo(request, response, "Users/new");
			return;
		}
		
		if (sm == null || u == null) {
			forwardTo(request, response, "StaticPages/index");
			return;
		}
		
		
		int userid = -1;
		IUser view;
		try {	
			userid = Integer.parseInt(params[1]);
			view = sm.getUserByID(userid);
			if (view != null) {
				if (!view.isActive()) {
					request.setAttribute("flash", "The user you tried to access has canceled his account! How dare " + (view.getSex().equalsIgnoreCase("m") ? "him" : "her") + "?!");
					forwardTo(request, response, "Users/index");
					return;
				} else if (view.isPublic() || view.getID() == u.getID()) {
					request.setAttribute("view", view);
					forwardTo(request, response, "Users/show");
				} else {
					request.setAttribute("flash", "This user has set its profile page as private! :/");
					forwardTo(request, response, "Users/index");
				}
				return;
			}
		} catch (NumberFormatException | RemoteException e) {
			
		}
		
		forwardTo(request, response, "Users/index");
	}
	
	@Override
	protected void create(String[] params, HttpServletRequest request,
			HttpServletResponse response) {
		
		String name, email, pass, pass2, city, country, sex, birth, error = "";
		Date bday = null;
		IUser u = null;
		DateFormat df;
		Boolean ispublic;
		
		if (User.getSession(request) != null) {
			forwardTo(request, response, "Users/index");
			return;
		}
		
		if (!User.matchNounce(request)) {
			forwardTo(request, response, "Users/new");
			return;
		}
		
		ISocialmore sm = Socialmore.connect(this);
		
		df = new SimpleDateFormat("dd/MM/yyyy");
		
		name 		= request.getParameter("name");
		email		= request.getParameter("email");
		pass		= request.getParameter("pass");
		pass2		= request.getParameter("pass2");
		city		= request.getParameter("city");
		country		= request.getParameter("country");
		sex			= request.getParameter("sex");
		ispublic	= request.getParameter("is_public") != null;
		
		System.out.println("Is public? " + ispublic);
		
		Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		
		try {
			u = sm.getUserByEmail(email);
		} catch (RemoteException e1) {
			error = "<li>Username is already taken.</li>";
		}
		
		if (name.isEmpty())
			error += "<li>The name field <b>cannot</b> be empty.</li>";
		if (!p.matcher(email).matches())
			error += "<li>The email provided is <b>not</b> valid. <i>(e.g.: john.doe@email.com)</i></li>";
		if (pass.length() < 6) 
			error += "<li>Password must contain at least 6 characters.</li>";				
		if (!pass.equals(pass2)) 
			error += "<li>Both passwords <b>must</b> be exactly the same.</li>";

		
		try {
			birth = request.getParameter("bday");
			if (!birth.isEmpty())
				bday  = df.parse(birth);
			else
				bday = new Date();
		} catch (ParseException e) {
			error += "<li>Your birthday format is wrong. <i>(Should be <b>dd/mm/yyyy)</i></li>";
		}
		
		if (!error.isEmpty()) {
			request.setAttribute("flash_type", "alert-error");
			request.setAttribute("flash", "There are errors in your submitions:<ul>" + error + "</ul>");
			forwardTo(request, response, "Users/new");
			return;
		}
		
		
		try {
			MessageDigest md5 = MessageDigest.getInstance("md5");
			md5.reset();
			md5.update(pass.getBytes());
			BigInteger bigint = new BigInteger(1, md5.digest());
			String hash = bigint.toString(16);
			while (hash.length() < 32) hash = "0" + hash;
					
			u = sm.register(name, email, city, country, bday, sex.charAt(0), hash, ispublic);
			if (u != null) {
				u.setFacepalm(true);
				request.getSession().setAttribute("user", u);
				request.getSession().setAttribute("sm", sm);
				forwardTo(request, response, "Users/index");
				return;
			}
		} catch (Exception e) {
			
		}
		
		request.setAttribute("flash_type", "alert-error");
		request.setAttribute("flash", "Could not complete registration! Please try again...");
		forwardTo(request, response, "Users/new");
	}
	
	protected void update(String [] params, HttpServletRequest request, HttpServletResponse response) {
		IUser user, dest = null;
		ISocialmore sm;
		String name, bday, sex, city, country, newpass, pass, conf, newemail;
		boolean is_public;
		Date bdate = null;
		int destid = -1;
		boolean error = false;
		
		sm		= Socialmore.connect(this);
		user 	= User.getSession(request);
		
		
		if (user == null || sm == null) {
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		request.setAttribute("view", user);
		
		try {
			
			newemail = (String)request.getParameter("newemail");
			if (newemail != null) {
				pass = (String)request.getParameter("pass");
				if (pass == null) pass = "";
				
				if (newemail.isEmpty() || pass.isEmpty()) {
					request.setAttribute("flash_type", "alert-error");
					request.setAttribute("flash", "Please, fill both fields to update your email!");
					forwardTo(request, response, "Users/show");
					return;
				}
				
				if (sm.getUserByEmail(newemail) != null) {
					request.setAttribute("flash_type", "alert-error");
					request.setAttribute("flash", "The email provided is already associated to another account!");
					forwardTo(request, response, "Users/show");
					return;
				}
				
				if (sm.login(user.getEmail(), Util.crypt(pass)) == null) {
					request.setAttribute("flash_type", "alert-error");
					request.setAttribute("flash", "The password provided is <b>not</b> correct!");
					forwardTo(request, response, "Users/show");
					return;
				}
				
				Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
				
				if (!p.matcher(newemail).matches()) {
					request.setAttribute("flash_type", "alert-error");
					request.setAttribute("flash", "The email's format is not accpted. (e.g.: john.doe@mail.com)");
					forwardTo(request, response, "Users/show");
					return;
				}
				
				user.setEmail(newemail);
				request.setAttribute("flash_type", "alert-success");
				request.setAttribute("flash", "Your email was successfuly updated!");
				forwardTo(request, response, "Users/show");
				return;
			}
			
			newpass = (String)request.getParameter("newpass");
			if (newpass != null) {
					pass = (String)request.getParameter("pass");
					conf = (String)request.getParameter("conf");
					if (pass == null) pass = "";
					if (conf == null) conf = "";
					
					if (newpass.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
						request.setAttribute("flash_type", "alert-error");
						request.setAttribute("flash", "Please, fill every field to update your password!");
						forwardTo(request, response, "Users/show");
						return;
					}
					
					if (sm.login(user.getEmail(), Util.crypt(pass)) == null) {
						request.setAttribute("flash_type", "alert-error");
						request.setAttribute("flash", "The current password provided is <b>not</b> correct!");
						forwardTo(request, response, "Users/show");
						return;
					}
					
					if (newpass.length() < 6) {
						request.setAttribute("flash_type", "alert-error");
						request.setAttribute("flash", "The password <b>must be</b> at least 6 characters long!");
						forwardTo(request, response, "Users/show");
						return;
					}
					
					if (!newpass.equals(conf)) {
						request.setAttribute("flash_type", "alert-error");
						request.setAttribute("flash", "Both passwords <b>must</b> match!");
						forwardTo(request, response, "Users/show");
						return;
					}
					
					user.setPassword(Util.crypt(newpass));
					request.setAttribute("flash_type", "alert-success");
					request.setAttribute("flash", "Your password was successfuly updated!");
					forwardTo(request, response, "Users/show");
					return;
			}
		} catch (RemoteException e) {
			forwardTo(request, response, "Users/show");
			return;
		}
		
		name = (String)request.getParameter("name");
		bday = (String)request.getParameter("bday");
		sex = (String)request.getParameter("sex");
		city = (String)request.getParameter("city");
		country = (String)request.getParameter("country");
		is_public = request.getParameter("is_public") != null;
		System.out.println("Sexo? " + sex.charAt(0));
		
		if (name == null || name.trim().isEmpty()) {
			request.setAttribute("flash_type", "alert-error");
			request.setAttribute("flash", "Name cannot be blank!");
			forwardTo(request, response, "Users/show");
			return;
		}
		
		try {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			bdate = df.parse(bday);
		} catch (ParseException e) {
			bdate = null;
		}
		
		try {
			user.setName(name);
			if (bdate != null) user.setBday(bdate);
			if (sex != null) user.setSex(sex.charAt(0));
			if (city != null) user.setCity(city);
			if (country != null) user.setCountry(country);
			user.setPublic(is_public);
		} catch (RemoteException e) {
			request.setAttribute("flash", "Could not update information! Please try again...");
			forwardTo(request, response, "Users/show");
			return;
		}
			
		request.setAttribute("flash", "Your profile was sucessfuly updated!");
		forwardTo(request, response, "Users/show");
	}
	
	protected void delete(String [] params, HttpServletRequest request, HttpServletResponse response) {
		String password;
		IUser user, conf;
		ISocialmore sm;
			
		sm 		= Socialmore.connect(this);
		user 	= User.getSession(request);
		if (sm == null || user == null) {
			forwardTo(request, response, "Sessions/login");
			return;
		}
		
		password = (String)request.getParameter("password");
		if (password == null) password = "";
		
		if (!User.matchNounce(request)) {
			request.setAttribute("view", user);
			forwardTo(request, response, "Users/show");
			return;
		}
		
		
		
		try {
			conf = sm.login(user.getEmail(), Util.crypt(password));
			if (conf == null) {
				request.setAttribute("flash_type", "alert-error");
				request.setAttribute("flash", "The password given is not correct!");
				request.setAttribute("view", user);
				forwardTo(request, response, "Users/show");
				return;
			} 
			user.setActive(false);
			request.getSession().setAttribute("user", null);
		} catch (RemoteException e) {
		}
		
		request.setAttribute("flash_type", "alert-success");
		request.setAttribute("flash", "Your account has been successfuly canceled! Someday you will regret this choice and <i>Facepalm</i> &gt:(");
		forwardTo(request, response, "Sessions/login");
	}

	
}
