package server.rest;

import com.google.gson.internal.StringMap;

public class FacebookPost {
	
	private String id;
	private String created_time;
	private String picture;
	private String description;
	private String message;

	
	public FacebookPost(StringMap<Object> obj) {
		this.id = (String) obj.get("id");
		this.created_time = (String) obj.get("created_time");
		this.picture = (String) obj.get("picture");;
		this.description = (String) obj.get("description");;
		this.message = (String) obj.get("message");;
	}
	
	public String getId() {
		return id;
	}	
	@Override
	public String toString() {
		return "FacebookPost [id=" + id + ", created_time=" + created_time
				+ ", picture=" + picture + ", description=" + description
				+ ", message=" + message + "]";
	}

	public String getCreated_time() {
		return created_time;
	}
	public String getPicture() {
		return picture;
	}
	public String getDescription() {
		return description;
	}
	public String getMessage() {
		return message;
	}
}
