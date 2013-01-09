package rest;

import java.io.*;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;

import controllers.MultipartMap;

@MultipartConfig(location = "/WebContent/assets/attaches", maxFileSize = 10485760L)

public abstract class HttpRest extends HttpServlet {

	
	private static final long serialVersionUID = -1333411378555707025L;

	protected int params_count = 0;
	
	protected void index(HttpServletRequest request, HttpServletResponse response) {};
	protected void show(String [] params, HttpServletRequest request, HttpServletResponse response) {};
	protected void create(String [] params, HttpServletRequest request, HttpServletResponse response) {};
	protected void update(String [] params, HttpServletRequest request, HttpServletResponse response) {};
	protected void delete(String [] params, HttpServletRequest request, HttpServletResponse response) {};
	
	
	@Override
	public void init() throws ServletException
	{
	}
	
	protected void forwardTo(HttpServletRequest request, HttpServletResponse response, String page) {
		try {
			request.getRequestDispatcher("/WEB-INF/views/" + page + ".jsp").forward(request, response);
			
		} catch (ServletException | IOException e) {
		}
	}

	private void redirectGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		String [] params, params_aux; 
		String url = request.getRequestURI();
		String method;

	
		
		method = getServletConfig().getInitParameter("method");
		if (method == null)
			method = (String)request.getParameter("method");
		method = method != null ? method : "get";
		
		params_aux 	= url.split("/");
		
		if (params_aux.length > (params_count + 1)) {
			params 		= new String[params_aux.length - 1];
			
			for (int i = 1; i < params_aux.length; i++) {
				params[i-1] = params_aux[i];
			}
			
			
			if (method.toUpperCase().equals("INDEX"))
				index(request, response);
			else if (method.toUpperCase().equals("DELETE"))
				delete(params, request, response);
			else
				show(params, request, response);
		} else {
			if (method.toUpperCase().equals("DELETE"))
				delete(null, request, response);
			else
				index(request, response);
		}	
	}
	
	
	private void redirectPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		String [] params = null, params_aux; 
		String method;
		String url = request.getRequestURI();
		
		params_aux 	= url.split("/");
		
		if (params_aux.length > (params_count + 1)) {
			params 		= new String[params_aux.length - 1];
			
			for (int i = 1; i < params_aux.length; i++) {
				params[i-1] = params_aux[i];
			}
	
		}
		
		
		method = getServletConfig().getInitParameter("method");
		if (method == null)
			method = (String)request.getParameter("method");
		if (method == null && request.getContentType().toUpperCase().contains("MULTIPART"))
			method = "put";
		method = method != null ? method : "post";
		
		if (method.toUpperCase().compareTo("DELETE") == 0)
			delete(params, request, response);
		else if (method.toUpperCase().compareTo("PUT") == 0)
			create(params, request, response);
		else
			update(params, request, response);	
			
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		redirectGet(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		redirectPost(request, response);
	}
}
