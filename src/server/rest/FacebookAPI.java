package server.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.Verifier;
import org.scribe.oauth.*;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

public class FacebookAPI {

	private final static String apiKey = "--";
	private final static String apiSecret = "--";
	private static OAuthService service = new ServiceBuilder()
							.provider(FacebookApi.class)
						    .apiKey(apiKey)
						    .apiSecret(apiSecret)
						    .callback("http://student.dei.uc.pt/~acs")
						    .scope("publish_stream")
						    .build();
	
	public static FacebookUser getUser(String userid, String token) {
		
		String json = "";
		FacebookUser result = null;
		
		try {
			URLConnection fbAPI = new URL( "https://graph.facebook.com/" + userid + "?access_token=" + token).openConnection();
			HttpURLConnection fbConnection = ((HttpURLConnection ) fbAPI);
			
			if(fbConnection.getResponseCode() != 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(fbConnection.getErrorStream()));
				
				String input;
				while((input = reader.readLine()) != null)
					json += input;
				
				reader.close();
				throw new Exception(json);
			}
			
			BufferedReader reader = new BufferedReader( new InputStreamReader( fbAPI.getInputStream() ) );
			
			String input;
			while((input = reader.readLine()) != null)
				json += input;
			
			reader.close();
			System.out.println("[GOT]"+json);
			Gson gson = new Gson();
			return gson.fromJson(json, FacebookUser.class);
			
		} catch (Exception e) {
			
		}
		
		return result;
	}
	
	private static FacebookObject request(HttpURLConnection connection, URLConnection fbAPI) {

		String json = "";
		
		try {
	
			if(connection.getResponseCode() != 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
	
				String input;
				while((input = reader.readLine()) != null) {

					System.out.println("[GOT]"+json);
					json += input;
				}
	
				reader.close();
	
				throw new Exception(json);
			} else {
				System.out.println("Success");
			}
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(fbAPI.getInputStream()));
	
			String input;
			while((input = reader.readLine()) != null)
				json += input;
	
			reader.close();
	
			Gson gson = new Gson();
			System.out.println("[GOT]"+json);
			return (gson.fromJson(json, FacebookObject.class));
		} catch (Exception e) {
			
		}

		return null;
	}
	
	private static String create( String UID, String token, String op, String content) {
		
		content = content.replace(" ", "+");
		URLConnection fbAPI;
		
		try {
			fbAPI = new URL("https://graph.facebook.com/"+ UID + "/" + op + "?message=" + content + "&access_token=" + token).openConnection();
			HttpURLConnection connection = ((HttpURLConnection) fbAPI);
			System.out.println(fbAPI.getURL().toString());
			connection.setRequestMethod("POST");
	        
			return request(connection, fbAPI).getID();
		} catch (Exception e) {
		}
		
	    return null;
	}
	
	private static String delete(String userid, String token) {
		
		try {
			URLConnection fbAPI = new URL( "https://graph.facebook.com/" + userid + "?access_token=" + token).openConnection();
	        HttpURLConnection connection = ((HttpURLConnection) fbAPI);
	        connection.setRequestMethod("DELETE");
	        
	        return request(connection, fbAPI).toString();
		} catch(Exception e) {
		}
		
		return null;
	}
	
	private static ArrayList<StringMap<Object>> getPsts(String userid, String token) {
		
		URLConnection fbAPI;
		String json = "";
		
		try {
			String url = "https://graph.facebook.com/"+ userid + "/feed?fields=id,message,description,created_time,picture&type=status&access_token=" + token;
			fbAPI = new URL(url).openConnection();
			HttpURLConnection connection = ((HttpURLConnection) fbAPI);
	        

			if(connection.getResponseCode() != 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
	
				String input;
				while((input = reader.readLine()) != null)
					json += input;
	
				reader.close();
	
				throw new Exception(json);
			} else {
				System.out.println("Success");
			}
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(fbAPI.getInputStream()));
	
			String input;
			while((input = reader.readLine()) != null)
				json += input;
	
			reader.close();
	
			Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			StringMap<Object> smap = (StringMap<Object>) (gson.fromJson(json, StringMap.class));
			
			@SuppressWarnings("unchecked")
			ArrayList<StringMap<Object>> posts = (ArrayList<StringMap<Object>>) smap.get("data"); 
			
			return posts;

		} catch (Exception e) {
			
		}
		
	    return null;
	}
	
	public static String post(String userid, String fb_token, String content) {
		return create(userid, fb_token, "feed", content);
	}
	
	public static String comment(String postid, String fb_token, String content) {
		return create(postid, fb_token, "comments", content);
	}
	
	public static void deletePost(String postid, String fb_token) {
		delete(postid, fb_token);
	}
	
	public static ArrayList<FacebookPost> getPosts(String userid, String fb_token) {
		
		ArrayList<FacebookPost> posts = new ArrayList<>();
		
		for (StringMap<Object> elem: getPsts(userid, fb_token)) {
			posts.add(new FacebookPost(elem));
		}
		
		return posts;
	}
	
	public static String getAuthorizationURL() {
		return service.getAuthorizationUrl(null);
	}
	
	
	public static String getToken(String code) {
		
		Verifier verifier = new Verifier(code);
	    return service.getAccessToken(null, verifier).getToken().toString();
	}
}
