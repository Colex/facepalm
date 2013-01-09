<div class="container-fluid">
  <div class="row-fluid">
  	<div class="span3 well">
   		 <%@include file="/WEB-INF/views/Messages/_inbox.jspf" %>
    </div>
    <div class="span9">
    	<div class="span8">
    		<%@include file="/WEB-INF/views/Messages/_chat.jspf" %>
    	</div>
	    <%@include file="/WEB-INF/views/Users/_notifications.jspf" %>
    </div>
  </div>
</div>