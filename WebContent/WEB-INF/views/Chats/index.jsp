<%@page import="server.classes.ISocialmore"%>

<% request.setAttribute("in_chat", true); %>

<div class="container-fluid">
  	<div class="row-fluid">
    	<div class="span3">
    		<%@include file="/WEB-INF/views/Users/_online.jspf" %>
    	</div>
    	<div class="span9">
	    	<div class="span8 well">
	    		<%@include file="/WEB-INF/views/Chats/_chats.jspf" %>
	    	</div>
      		<%@include file="/WEB-INF/views/Users/_notifications.jspf" %>
    	</div>

	</div>
</div>