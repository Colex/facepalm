var Notifications = {};

Notifications.socket = null;

Notifications.addUser = function(id, name, pic) {
	if ($("#online_user_" + id).length > 0)
		$("#online_user_" + id).show();
	else
		$("#online_users").append('<a href="\\user/' + id + '"><div id="online_user_' + id + '" style="margin-top: 5px;"><img src="' + pic + '" width="25px" height="25px"/> ' + name + '</div></a>');
};

Notifications.removeUser = function(id) {
	$("#online_user_" + id).hide();
};

Notifications.newPost = function(id, name, pic) {
	var a = '<div style="border-top: 1px solid gray;"><a href="\\post/' + id + '"><div  style="margin-top: 5px;"><img src="' + pic + '" width="25px" height="25px"/> <b>' + name + '</b> has posted a new message</div></a></div>';
	NotifyConsole.log(a);
};

Notifications.newComment = function(id, name, pic) {
	var a = '<div style="border-top: 1px solid gray;"><a href="\\post/' + id + '"><div  style="margin-top: 5px;"><img src="' + pic + '" width="25px" height="25px"/> <b>' + name + '</b> has commented on a post</div></a></div>';
	NotifyConsole.log(a);
};

Notifications.newMyComment = function(id, name, pic) {
	var a = '<div style="border-top: 1px solid gray;"><a href="\\post/' + id + '"><div  style="margin-top: 5px;"><img src="' + pic + '" width="25px" height="25px"/> <b>' + name + '</b> has commented on your post</div></a></div>';
	NotifyConsole.log(a);
};


Notifications.newPM = function(id, name, pic, content, date, my_pm) {
	var a;
	var bg;
	
	
	if ($("#pm_with_" + id).length > 0) {
    	if (my_pm) 
    		bg = "#FFFFFF";
    	else
    		bg = "#EFEFFF";
    	
    	a = '<div style="overflow: hidden; background-color: '+bg+'">';
		a += '<div style="margin-top: 5px;"><div class="span2" style="margin: 5px; width: 10%; height: 60px;">';
		a += '<a href="\\user/'+id+'"><img src="'+pic+'" /></a></div>';
		a += '<div class="span2" style="width: 85%;">';
		a += '<a href="\\user/'+id+'"><b>'+name+'</b></a>';
		a += '<b><i><span style="float: right; color: gray; font-size: 10px;">'+date+'</span></i></b><br>';
		a += content;
		a += '</div></div></div>';
		
		var p = document.createElement('div');
	    p.style.wordWrap = 'break-word';
	    p.innerHTML = a;
		
		var console = document.getElementById("pm_with_" + id);
		console.insertBefore(p, console.firstChild);
	}
	
	if ($("#inbox_" + id).length > 0) {
		if ($("#pm_with_" + id).length == 0)
			$("#inbox_" + id).css("background-color", "#EECCFF");
		$("#last_pm_" + id).html(content);
	} else {
		a = '<a href="\\message/'+id+'">';
		a += '<div id="inbox_'+id+'" class="row message-div" style="background-color: #EECCFF !important;">';
		a += '<div class="span2" style="width:50px; margin-top: 15px; margin-left: 5px;">';
		a += '<img src="'+pic+'" /></div>';
		a += '<div class="span2" style="width: 160px; margin-left: 10px;  margin-top: 15px;">';
		a += name + '<br>';
		a += '<h6 style="color: gray;">'+content+'</h6></div></div></a>';

		var pm = document.createElement('div');
	    pm.style.wordWrap = 'break-word';
	    pm.innerHTML = a;
		
		var inbox = document.getElementById("inbox");
		inbox.insertBefore(pm, inbox.firstChild);
	}
		
};

var showScheduler = function() {
	var now = new Date();
	$('#schedule').toggle();
	if ($('#schedule').is(":visible"))
		$('#schedule').val(now.format('hh:MM dd/mm/yyyy'));
	else
		$('#schedule').val("");
}

var sendPM = function(args) {
	var target 	= $("#rcvid").val();
	var content = $("#pm_content").val();
	var schedule = $("#schedule").val();
	var json = '{"target": '+target+', "data": "'+content+'", "schedule": "'+schedule+'", "attaches": "' + args + '"}';
		
	if (content != '') {
		Notifications.socket.send(json);
		$("#pm_content").val("");
    }
};


$(document).ready(function() {
	$("#send_pm").click(function() {
		var data = new FormData();
    	for (var i = 0; i < $('#attachments').get(0).files.length; i++) {
    		data.append('attach'+i, $('#attachments').get(0).files.item(i));
    	}
    	data.append('count', $('#attachments').get(0).files.length);
    	data.append('rcvid', $("#rcvid").val());
    	$.ajax({
    	    url: '/message/0',
    	    data: data,
    	    cache: false,
    	    contentType: false,
    	    processData: false,
    	    type: 'POST',
    	    success: sendPM
    	});
	});
	
	//change to "content"
	$('#pm_content').keydown(function(event) {
        if (event.keyCode == 13) {
        	var data = new FormData();
        	for (var i = 0; i < $('#attachments').get(0).files.length; i++) {
        		data.append('attach'+i, $('#attachments').get(0).files.item(i));
        	}
        	data.append('count', $('#attachments').get(0).files.length);
        	data.append('rcvid', $("#rcvid").val());
        	$.ajax({
        	    url: '/message/0',
        	    data: data,
        	    cache: false,
        	    contentType: false,
        	    processData: false,
        	    type: 'POST',
        	    success: sendPM
        	});
        	//xmlhttpPost("/message/0", "send_pm", sendPM, "");
            //sendPM();
     		event.stopPropagation();  
     		return false;
        }
    });
});

Notifications.connect = (function(host) {
    if ('WebSocket' in window) {
        Notifications.socket = new WebSocket(host);
    } else if ('MozWebSocket' in window) {
        Notifications.socket = new MozWebSocket(host);
    } else {
       alert('Error: WebSocket is not supported by this browser.');
        return;
    }

    Notifications.socket.onopen = function () {
        
    };
    
	

    Notifications.socket.onclose = function () {
        //document.getElementById('Notifications').onkeydown = null;
        //Console.log('<b><i>You lost connection to the Notifications :( we should go out sometime...</i></b>');
    };

    Notifications.socket.onmessage = function (message) {
    	var obj = JSON.parse(message.data);
    	
    	if (obj.method == "online") {
    		Notifications.addUser(obj.data, obj.message, obj.extra);
    	} else if (obj.method == "offline") {
    		Notifications.removeUser(obj.data);
    	} else if (obj.method == "post") {
    		Notifications.newPost(obj.data, obj.message, obj.extra);
    	} else if (obj.method == "comment") {
    		Notifications.newComment(obj.data, obj.message, obj.extra);
    	} else if (obj.method == "mycomment") {
    		Notifications.newMyComment(obj.data, obj.message, obj.extra);
    	} else if (obj.method == "private") {
    		Notifications.newPM(obj.data, obj.extra, obj.extra2, obj.message, obj.date, false);
    	}  else if (obj.method == "myprivate") {
    		Notifications.newPM(obj.data, obj.extra, obj.extra2, obj.message, obj.date, true);
    	} else if (obj.method == "unread") {
    		$("#unread").html(obj.data);
    	}
    };
});

Notifications.initialize = function() {
    if (window.location.protocol == 'http:') {
        Notifications.connect('ws://' + window.location.host + '/notifications');
    } else {
        Notifications.connect('wss://' + window.location.host + '/notifications');
    }
};

Notifications.sendMessage = (function() {
});

var NotifyConsole = {};

NotifyConsole.log = (function(message) {
    var console = document.getElementById('notifications');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.innerHTML = message;
    console.insertBefore(p, console.firstChild);
    while (console.childNodes.length > 25) {
        console.removeChild(console.lastChild);
    }
    //console.scrollTop = console.scrollHeight;
});

Notifications.initialize();