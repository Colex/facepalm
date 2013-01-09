<%@page import="server.classes.ISocialmore"%>

<% request.setAttribute("in_chat", true); %>

<div class="container-fluid">
  	<div class="row-fluid">
    	<div class="span3">
    		<%@include file="/WEB-INF/views/Users/_online.jspf" %>
    	</div>
    	<div class="span9">
	    	<div class="span8 well">
	    		<%@include file="/WEB-INF/views/Chats/_chat.jspf" %>
	    	</div>
      		<div class="span4">
      			<% request.setAttribute("hide-class", true); %>
      			<%@include file="/WEB-INF/views/Users/_notifications.jspf" %>
      			<br>
      			<div class="span12 well">
      			<b>[Chat] Online users:</b>
      			<%@include file="/WEB-INF/views/Chats/_online.jspf" %>
      			</div>
    		</div>
    	</div>

	</div>
</div>