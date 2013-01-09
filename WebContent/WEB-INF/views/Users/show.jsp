<%@page import="server.classes.ISocialmore"%>

<%
	IUser view = (IUser)request.getAttribute("view");
	IUser me = (IUser)request.getSession().getAttribute("user");
	Boolean owner = view.getID() == me.getID();
	String sex = view.getSex();
	ArrayList<IChatroom> rooms = view.getChatrooms();
%>

<% if (owner) { %>
<div id="delete_user_modal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="profileRemovalLabel" aria-hidden="true">
	<form id="delete_form" method="post" action="/user/<%= me.getID() %>">	
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
			<h3 id="myModalLabel">Delete Profile</h3>
		</div>
		<div class="modal-body">
			<p>You are about to delete your profile, all of your posts <b>are not</b> going to be removed. If you still wish to proceed, enter your password:</p>
			<input name="password" type="password" placeholder="Password">
		</div>
		<div class="modal-footer">
			
				<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
				<input type="hidden" name="method" value="delete">
				<input type="hidden" name="nounce" value="${nounce}">
				<button class="btn btn-danger">Delete</button>
		</div>
	</form>
</div>


<div id="update_password" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="profileRemovalLabel" aria-hidden="true">
	<form id="delete_form" method="post" action="/user/<%= me.getID() %>">	
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
			<h3 id="myModalLabel">Change password</h3>
		</div>
		<div class="modal-body">
			Current password:
			<input name="pass" class="input-block-level" type="password" placeholder="Password">
			New password:
			<input name="newpass" class="input-block-level" type="password" placeholder="Password">
			Confirm password:
			<input name="conf" class="input-block-level" type="password" placeholder="Password">
		</div>
		<div class="modal-footer">
			
				<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
				<input type="hidden" name="nounce" value="${nounce}">
				<button class="btn btn-primary">Set password!</button>
		</div>
	</form>
</div>

<div id="update_email" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="profileRemovalLabel" aria-hidden="true">
	<form id="update_form" method="post" action="/user/<%= me.getID() %>">	
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
			<h3 id="myModalLabel">Change password</h3>
		</div>
		<div class="modal-body">
			New e-mail:
			<input name="newemail" class="input-block-level" type="text" placeholder="john.doe@mail.com">
			Current password:
			<input name="pass" class="input-block-level" type="password" placeholder="Password">
		</div>
		<div class="modal-footer">
			
				<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
				<input type="hidden" name="nounce" value="${nounce}">
				<button class="btn btn-primary">Set email!</button>
		</div>
	</form>
</div>

<% } %>

<div class="container-fluid">
  	<div class="row-fluid">
    	<div class="span3">
    		<div style="text-align:center;">	
    			<img src="<%= view.getPicture() %>?s=150" height="150px" width="150px" style="border: 2px solid white;"/>
    		</div>
    		<%@include file="/WEB-INF/views/Users/_online.jspf" %>
    	</div>
    	<div class="span9">
	    	<div class="span8 well">
	    		<div style="margin: 20px; letter-spacing: 5px;">
	    			<% if (owner) { %>
	    				<a id="edit_profile" href="#" class="pull-right"><i class="icon-edit" title="Edit profile"></i></a>
	    				<form id="edit_profile_form" method="post" action="\user/<%= me.getID() %>">
	    					<input type="hidden" name="nounce" value="${nounce}">
							<input type="text" name="name" class="input-block-level" value="<%= me.getName() %>"><br>
							<div class="row-fluid">
								<div class="span6"><input type="text" name="city" class="input-block-level" value="<%= me.getCity() %>"></div>
								<div class="span6"><input type="text" name="country" class="input-block-level" value="<%= me.getCountry() %>"></div><br>
							</div>	
							<div class="row-fluid">
								<div class="span4">	
									<select name="sex" class="input-block-level">
										<option value="male" <%= sex.trim().equals("m") ? "selected" : "" %>>Male</option>
										<option value="female" <%= sex.trim().equals("f") ? "selected" : "" %>>Female</option>
									</select>
								</div>
								<div class="span8"><input value="<%= view.getBday("dd/MM/yyyy") %>" type="text" placeholder="dd/mm/aaaa" name="bday"  value="" class="input-block-level"></div>
	    					</div>
	    					<div class="row-fluid">	
	    						<div class="span4">
	    							<label class="input-block-level"><input type="checkbox" name="is_public" <%= me.isPublic() ? "checked" : "" %>> Public profile</label><br>
	    						</div>
	    						<div class="span8"><input type="submit" class="btn btn-primary pull-right" value="Update profile!"></div>
	    					</div>
	    				</form>
	    			<% } %>
	    			<div id="profile_info">
		    			<h1><%= view.getName() %></h1>
		    			<h4><%= view.getCity() %>, <%= view.getCountry() %></h2>
		    			<h4><img src="\assets/img/<%= sex %>.png" width="40px" /> <%= view.getBday() %></h2>
		    		</div>
	    		</div>
	    		<% request.setAttribute("posts", view.getPosts()); %>
	    		<%@include file="/WEB-INF/views/Posts/_posts.jspf" %>
	    	</div>
      		<div class="span4">
      			<% request.setAttribute("hide-class", true); %>
      			<%@include file="/WEB-INF/views/Users/_notifications.jspf" %>
      			
      			<% if (owner) { %>
	    		<ul class="nav nav-pills nav-stacked well">
	    			<li><a href="#update_email" role="button" data-toggle="modal"><i class="icon-envelope"></i> Change email</a></li>
					<li><a href="#update_password" role="button" data-toggle="modal"><i class="icon-lock"></i> Reset password</a></li>
					<li><a href="#delete_user_modal" style="color: red !important;" role="button" data-toggle="modal"><i class="icon-remove"></i> Delete Account</a></li>
				</ul>
				<% } %>
				
				<ul class="nav nav-pills nav-stacked well">
				<% for (IChatroom c : rooms) { %>
				<li><a href="/chats/<%= c.getChat_id() %>"><%= c.getSubject().toUpperCase().charAt(0) + c.getSubject().substring(1) %> > <b><%= c.getName() %></b></a></li>
				<% } %>
				
				<%= rooms.size() <= 0 ? "No chat rooms..." : "" %>
				</ul>
      		</div>
    	</div>

	</div>
</div>