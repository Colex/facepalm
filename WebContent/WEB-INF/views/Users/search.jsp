<%@page import="java.util.ArrayList"%>
<%@page import="server.classes.ISocialmore"%>

<%
ArrayList<IUser> users = (ArrayList<IUser>)request.getAttribute("users");
%>

<div class="container-fluid">
  	<div class="row-fluid">
    	<div class="span3">
    		<ul class="nav nav-pills nav-stacked well">
    		<b>Search filters:</b>
				<li><a href="?filter=all&search=<%= request.getParameter("search") %>">All</a></li>
				<li><a href="?filter=name&search=<%= request.getParameter("search") %>">Name</a></li>
				<li><a href="?filter=email&search=<%= request.getParameter("search") %>">E-mail</a></li>
				<li><a href="?filter=city&search=<%= request.getParameter("search") %>">City</a></li>
				<li><a href="?filter=country&search=<%= request.getParameter("search") %>">Country</a></li>
			</ul>
    		<%@include file="/WEB-INF/views/Users/_online.jspf" %>
    	</div>
    	<div class="span9">
	    	<div class="span8 well">
	    		<% for (IUser u : users) { %>
	    			<div>
	    				<hr>
	    				<a href="/user/<%= u.getID() %>"><img src="<%= u.getPicture() %>?s=50" width="50px" height="50px" /> <b><%= u.getName() %></b></a>
	    			</div>
	    		<% } %>
	    		<% if (users.size() <= 0) { %>
	    			<b>No results found...</b>
	    		<% } %>
	    	</div>
	    	<%@include file="/WEB-INF/views/Users/_notifications.jspf" %>
    	</div>

	</div>
</div>