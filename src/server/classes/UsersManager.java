package server.classes;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpSession;

import org.scribe.builder.api.FacebookApi;

import server.rest.FacebookAPI;
import server.rest.FacebookPost;

public class UsersManager implements Runnable {

	private Socialmore m_sm;
	
	public UsersManager(Socialmore sm) {
		m_sm = sm;
	}
	
	@Override
	public void run() {
		String token;
		ArrayList<FacebookPost> posts;
		Post post;
		IPost new_post;
		int postid;
		/*
		while (true) {	
			CopyOnWriteArrayList<IUser> users = m_sm.getOnlineUsers();
			System.out.println("Syncing Facebook....");
			for (IUser u : users) {
				try {
					 System.out.println("Getting token...");
					 token = u.getToken();
					 System.out.println("Token collected (" + token + ")...");
					 if (token != null) {
						System.out.println("Reading posts..."); 
						posts = FacebookAPI.getPosts("me", token);
						for (FacebookPost p : posts) {
							if (p.getMessage() != null) {
								post = m_sm.getPostFromFacebook(p.getId());
								if (post != null) continue;
								System.out.println("Got new post: " + p);
								postid = u.post(p.getMessage());
								if (postid > -1) {
									new_post = m_sm.getPost(postid);
									new_post.setFacebook(p.getId());
								}
							} else {
								System.out.println("Garbage: " + p);
							}
						} 
					 }
				} catch (RemoteException e) {
					System.out.println("Erro :(");
				}
			}
			
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
			}
		}*/
	}

}
