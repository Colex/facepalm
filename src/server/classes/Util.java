package server.classes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Random;

public class Util {

	public static void print(String s) {
		String time = new Timestamp(new java.util.Date().getTime()).toString();
		
		while (time.length() < 23) {
			time+="0";
		}
		
		System.out.println("["+time+"] "+s);
	}
	
	public static String generateString() {
		
		Random rng = new Random();
		int length = 64;
		String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; 
		char[] text = new char[length];
	    
	    for (int i = 0; i < length; i++)
	    	text[i] = characters.charAt(rng.nextInt(characters.length()));
	    
	    return new String(text);
	}
	
	public static String crypt(String s) {
		
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(s.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1,digest);
			String hashtext = bigInt.toString(16);
			while(hashtext.length() < 32 )
				hashtext = "0"+hashtext;
			
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static String filter(String message) {
		if (message == null)
			return (null);
		// filter characters that are sensitive in HTML
		char content[] = new char[message.length()];
		message.getChars(0, message.length(), content, 0);
		StringBuilder result = new StringBuilder(content.length + 50);
		for (int i = 0; i < content.length; i++) {
			switch (content[i]) {
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '"':
				result.append("&quot;");
				break;
			default:
				result.append(content[i]);
			}
		}
		return (result.toString());
	}
	

	public static String getWelcomeMessage(String name) {
		return "Hello " + name + ",\n\n" +
				"We are happy to see that you joined our social network!\n\n" + 
				"Please remember that FacePalm uses your Gravatar account to associate a picture with your profile.\n" +
				"If you dont own a Gravatar account you can do it under this domain: https://en.gravatar.com/site/signup\n\n" + 
				"If you have any doubt about the safety of our network or if you have any problem within our website, please contact the FacePalm Team, through facepalmserver@gmail.com.\n\n"+
				"Lets hope to see you soon! :)\n\n" +
				"Best regards,\n" +
				"FacePalm Team";
	}
	
	public static String getChangePassword(String name, String hash) {
		return "Hello " + name + ",\n\n" +
				"We received a request to reset a password from the account associated with this email.\n" +
				"To reset your password you should access rfrr.no-ip.org:8080/recover?code=" + hash + "\n\n" +
				"If you have any doubt about the safety of our network or if you have any problem within our website, please contact the FacePalm Team, through facepalmserver@gmail.com.\n\n"+
				"Lets hope to see you soon! :)\n\n" +
				"Best regards,\n" +
				"FacePalm Team";
	}
}
