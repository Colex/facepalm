package server.rest;

public class FacebookUser implements IFacebookUser {
		
	private String id;
	private String name;
	private String first_name;
	private String last_name;
	private String link;
	private String username;
	private String gender;
	private String locale;
	private String type;
	private FacebookObject location;  
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFirstName() {
		return first_name;
	}
	
	public String getLastName() {
		return last_name;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getGender() {
		if (gender == null) return "m";
		return gender;
	}
	
	public String getLocale() {
		return locale;
	}
	
	public String getType() {
		return type;
	}
	
	public String getLocation() {
		return location.getName();
	}
	
	public String getCity() {
		if (location == null) return "Unknown";
		return location.getName().split(",")[0];
	}
	
	public String getCountry() {
		if (location == null) return "Unknown";
		return location.getName().split(",")[1].substring(1);
	}
}