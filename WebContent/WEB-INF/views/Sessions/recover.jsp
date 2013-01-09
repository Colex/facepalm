<%
String email = (String)request.getParameter("email");
String code = (String)request.getAttribute("key");
email = email == null ? "" : email;
%>

<% if (code == null) { %>
<form class="form-signin" method="post" action="/recover" >
	<input name="method" type="hidden" value="put" />
	<h4 class="form-signin-heading">Always with your head on the clouds...</h4>
	<i>Let me help you <b>recover</b> your <b>password</b> :)</a></i>
	<input name="email" type="text" class="input-block-level" value="<%= email %>" placeholder="Email address">
	<input name="nounce" type="hidden" value="${nounce}">
	<button class="btn btn-info" type="submit">Recover password</button>
</form>
<% } else { %>
<form class="form-signin" method="post" action="/recover" >
	<h4 class="form-signin-heading">Write a post-it this time...</h4>
	<input name="pass" type="password" class="input-block-level" value="" placeholder="New password">
	<input name="pass_confirm" type="password" class="input-block-level" value="" placeholder="New password confirmation">
	<input name="nounce" type="hidden" value="${nounce}">
	<input name="key" type="hidden" value="<%= code %>">
	<button class="btn btn-info" type="submit">Reset password</button>
</form>
<% } %>