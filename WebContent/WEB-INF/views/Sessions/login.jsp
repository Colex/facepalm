
<div style="margin-top: -10px">	
	

	<% if (session.getAttribute("user") != null) { %>
		<div class="alert alert-success" style="margin-top: 8px; z-index: 10 !important;">
			Welcome back, <b>${user.getName()}</b>
		</div>
	<% } %>


	<%@include file="/WEB-INF/views/Sessions/_form.jspf" %>
</div>
