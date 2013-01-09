<div class="container-fluid">
  <div class="row-fluid">
    <div class="span8 well" style="text-align: center">
      <img src="/assets/img/<%= (int) (Math.random() * 6)+1 %>.jpg" />
    </div>
    <div class="span4 well">
   		 <%@include file="/WEB-INF/views/Sessions/_form.jspf" %>
    </div>
  </div>
</div>