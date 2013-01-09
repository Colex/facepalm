package server.classes;

import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailDispatcher implements Runnable {
	
	private Session session;
	private String username;
	private Message message;
	private Thread t;
	
	private LinkedBlockingQueue<String[]> pool;
	
	public EmailDispatcher (final String username, final String password) {
		
		this.pool = new LinkedBlockingQueue<>();
		this.username = username;
		
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		
		this.session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		this.message = new MimeMessage(session);
		try {
			this.message.setFrom(new InternetAddress(this.username + "@gmail.com"));
		} catch (MessagingException e) {
		}
		
		this.t = new Thread(this);
		this.t.start();
	}
	
	public void run() {
		
		String[] data = null;
		
		while(true) {
			try {
				data = this.pool.take();
				Util.print("Still got " + this.pool.size() + " emails to send!");
			} catch (InterruptedException e) {
			}
			
			this.sendMail(data[0], data[1], data[2]);
		}
	}
	
	public void add(String... data) {
		
		if (data.length != 3) {
			System.out.println("Error adding");
			return;
		}
		
		this.pool.add(data);
	}
	
	private boolean sendMail(String destiny, String subject, String text) {
		
		try {
			this.message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destiny));
			this.message.setSubject("[Facepalm] " + subject);
			text += "\n\n---------------------------------------------------------------------------------------\n" +
					"This message was sent to: " + destiny + ".\n";
			message.setText(text);
			Transport.send(message);
				
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
