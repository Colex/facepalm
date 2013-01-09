<%@page import="server.classes.ISocialmore"%>

<% request.setAttribute("in_wall", true); %>

<div class="container-fluid">
  	<div class="row-fluid">
    	<div class="span3">
    		<%@include file="/WEB-INF/views/Users/_online.jspf" %>
    	</div>
    	<div class="span9">
	    	<div class="span8 well">
	    		<% request.setAttribute("posts", ((ISocialmore)session.getAttribute("sm")).getPosts()); %>
	    		<%@include file="/WEB-INF/views/Posts/_new.jspf" %>
	    		<%@include file="/WEB-INF/views/Posts/_posts.jspf" %>
	    	</div>
	    	<%@include file="/WEB-INF/views/Users/_notifications.jspf" %>
    	</div>

	</div>
</div>