<%
	String name 	= request.getParameter("name");
	String email 	= request.getParameter("email");
	String city 	= request.getParameter("city");
	String country 	= request.getParameter("country");
	String bday 	= request.getParameter("bday");
	String sex 		= request.getParameter("sex");

	if (name == null) {
		name = email = city = country = bday = "";
		sex = "m";
	}
%>
<div style="margin-top: -10px">		

	<form class="form-signup" name="register" method="post">
		<input type="hidden" name="method" value="put">
		<script>
		var msgs = [
			'Goodbye real life!',
			'Welcome to the dark side!',
			'We were expecting you...'
		];
		//$_SESSION['welcome'] = $msg = (rand(0, count($msgs) + $dec - 1) + $offset) % count($msgs);
		document.write('<h2 class="form-signin-heading">' + msgs[Math.round(Math.random() * 10) % 3] + '</h2><br>'); 
		</script>
		
		<input type="hidden" name="nounce" value="${nounce}">
		Name*: <input type="text" name="name" class="input-block-level" value="<%= name %>"><br>
		Email*: <input type="text" name="email" class="input-block-level" value="<%= email %>"><br>
		<div class="well">
			Password*: <input type="password" name="pass" class="input-block-level"><br>
			Confirm Password*: <input type="password" name="pass2" class="input-block-level"><br>
		</div>
		City: <input type="text" name="city" class="input-block-level" value="<%= city %>"><br>
		Country: <input type="text" name="country" id="countries_list"  value="<%= country %>" data-provide="typeahead" data-items="4" class="input-block-level"><br>
		Birthday: <input value="<%= bday %>" type="text" placeholder="dd/mm/aaaa" name="bday"  value="" class="input-block-level"><br>
		I am: <select name="sex" class="input-block-level">
				<option value="male" <%= (sex == "male") ? "checked" : "" %>)>Male</option>
				<option value="female" <%= (sex == "female") ? "checked" : "" %>>Female</option>
			</select><br>
		<label class="input-block-level"><input type="checkbox" name="is_public" checked > Do you wish to have a public profile?</label><br>
		<input type="submit" class="btn btn-large btn-primary" value="Register!">
	</form>

</div>
