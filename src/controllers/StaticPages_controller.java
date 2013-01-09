package controllers;
import javax.servlet.*;
import javax.servlet.http.*;

import rest.HttpRest;
import server.classes.User;

public class StaticPages_controller extends HttpRest {

	private static final long serialVersionUID = 3364780532571929206L;


	@Override
	public void init() throws ServletException
	{
	}

	protected void index(HttpServletRequest request, HttpServletResponse response) {
		
		System.out.println("Index!");
		
		if (User.getSession(request) == null)
			forwardTo(request, response, "StaticPages/index");
		else 
			forwardTo(request, response, "Users/index");
	}

	protected void show(String [] params, HttpServletRequest request, HttpServletResponse response) {
		forwardTo(request, response, "StaticPages/index");
	}
}
