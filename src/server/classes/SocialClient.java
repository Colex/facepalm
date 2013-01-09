package server.classes;

import java.io.Console;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class SocialClient extends UnicastRemoteObject implements ISocialClient {

	private static final long serialVersionUID = -5663555584153351899L;
	
	private ISocialmore 					m_socialmore;
	private IUser 							m_user;
	private volatile int					m_status;
	private String 							m_email, m_password;
	private ConcurrentLinkedQueue<String> 	m_notifications;
	
	
	public SocialClient() throws RemoteException {
		super();
		m_notifications = new ConcurrentLinkedQueue<>();
		m_status = 0;
	}
	
	private void connect() throws MalformedURLException, RemoteException, NotBoundException {
		m_socialmore = (ISocialmore)Naming.lookup ("rmi://localhost:7000/Socialmore");
	}
	
	private void reconnect() {
		Boolean connected;
		int tries = 0;
		
		do {
			try {
				connect();
				if (m_user != null) {	
					m_user = m_socialmore.login(m_email, m_password);
					m_socialmore.addOnlineUser(m_user);
					m_user.addConnection(this);
				}
				connected = true;
			} catch (Exception e1) {
				connected = false;
				if (tries++ == 2) {
					System.out.println("We are sorry! The server is down :(\nDon't leave us for the real world :/");
					System.exit(0);
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e2) {}
			}
			
		} while (!connected);
	}


	public void start() {
		Scanner sc = new Scanner(System.in);
		m_user = null;
		while (true) {
			try {
				if (m_user == null) {
					m_user = this.showLoginMenu();
					m_user.addConnection(this);
				}
	
				this.showMainMenu();
				System.exit(0);
			} catch (Exception e) {

		    }
		}
	}
	
	
	public int getStatus() {
		return m_status;
	}
	
	private IUser showLoginMenu() {
		String choice;
	    Scanner sc = new Scanner(System.in);
	    IUser 	user = null;
	    
		System.out.println("''''''''''''''''''''''''''''''''''''''''''''''''");
		System.out.println("'''''   SOCIALMORE - Become More Social!   '''''");
		System.out.println("''''''''''''''''''''''''''''''''''''''''''''''''");
		
		while (user == null) {
	    	System.out.println(" 1 - Login");
			System.out.println(" 2 - Register");
		    choice = sc.nextLine();
		    if (choice.equals("1"))
		    	user = login();
		    else if (choice.equals("2"))
		    	user = register();
	    }

		return user;
	}
	
	private String readPassword() {
		Console cons;
		char[] passwd;
		cons = System.console();
		passwd = cons.readPassword("%s", "Password: ");
		if ( cons!= null &&  passwd!= null) {
			return String.copyValueOf(passwd);
		} else {
			Util.print("Error reading the password");
			return null;
		}
	}
	
	private String crypt(String s) {
		
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
			Util.print("SHA-256 not on this pc!");
			return null;
		}
	}
	
	private IUser login() {
		IUser 	user = null;
		Boolean sent;
		Scanner sc = new Scanner(System.in);
		
		System.out.print("Email: ");
	    m_email = sc.nextLine();
	    System.out.print("Password: ");
	    m_password = this.crypt(sc.nextLine()/*this.readPassword()*/);

	    do {
	    	try {
				user = m_socialmore.login(m_email, m_password);
				sent = true;
				if (user == null)
			    	System.out.println("\nUsername or password are wrong!\n");
			    else
			    	m_socialmore.addOnlineUser(user);
			} catch (Exception e) {
				reconnect();
				sent = false;
			}
	    } while (!sent);
	   
	    
		return user;
	}
	
	private IUser register() {
		

		Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		String 	name, email, password, city, country, birthday, sex, aux;
		Scanner sc = new Scanner(System.in);
		Boolean sent, isPublic;
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		IUser user = null;
		Date bday;
		int i= 0;
		
		/* Name */
		System.out.print("Name: ");
		name = sc.nextLine();
		
		/* Email */
		while (true) {
			
			if (i > 0) {
				System.out.println("Email already taken!!!");
			}
			
			do {
				System.out.print("Email: ");
				email = sc.next();
			} while(!p.matcher(email).matches());
			
			try {
				user = this.m_socialmore.getUserByEmail(email);
				sent = true;
			} catch (RemoteException e) {
				reconnect();
				sent = false;
			}
			
			if (user == null) {
				break;
			}
			
			i++;
		}
		
		/* Password */
		System.out.print("Password: ");
		password = this.crypt(this.readPassword());
		
		/* City */
		do {
			System.out.print("City: ");
			city = sc.nextLine();
		} while(city.length() == 0);
		
		/* Country */
		do {
			System.out.print("Country: ");
			country = sc.nextLine();
		} while(country.length() == 0);
		
		/* Bday */
		while (true) {
			System.out.print("Birthday [dd/mm/aaaa]: ");
			try {
				bday = df.parse(sc.nextLine());
				break;
			} catch (ParseException e) {
				System.out.println("Date is in the wrong format");
			}
		}
		
		/* Sex */
		do {
			System.out.print("Sex [m/f]: ");
			sex = sc.next();
		} while (!sex.equals("m") && !sex.equals("f"));
		
		/* isPublic */
		System.out.print("Do you wish to have a public profile? [Y/n]: ");
		aux = sc.next();
		isPublic = !(aux.equals("n") || aux.equals("N"));
		
		do {
			try {
				user = m_socialmore.register(name, email, city, country, bday, sex.charAt(0), password, isPublic);
				sent = true;
			} catch (Exception e) {
				reconnect();
				sent = false;
			}
		} while (!sent);
		
		if (user != null) {
			System.out.println("You were successfuly registered");
			try {
				if (user == null)
			    	System.out.println("\nUsername or password are wrong!\n");
			    else
			    	m_socialmore.addOnlineUser(m_user);
			} catch (RemoteException e) {}
		} else {
			System.out.println("Ups... We're sorry but an error occurred!");
		}

		return user;
	}	
	
	private void showMainMenu() {
		String choice, name = "";
		int views, got;
	    Scanner sc = new Scanner(System.in);
	    
	    do {
	    	
	    	try {
				name = m_user.getName();
				got = 1;
			} catch (RemoteException e) {
				reconnect();
				got = 0;
			}
	    	
	    } while (got != 1);
	    
	    while (name.length() < 19) {
			name+=" ";
		}
	    
		while (true) {
			m_status = 0;
			System.out.println("\n\n''''''''''''''''''''''''''''''''''''''''''''''''");
			System.out.println("'''''   SOCIALMORE - "+name+"   '''''");
			System.out.println("''''''''''''''''''''''''''''''''''''''''''''''''");
			while (!m_notifications.isEmpty())
				System.out.println("<" + m_notifications.poll() + ">");

	    	System.out.println("\n 1 - Send Message");
			System.out.println(" 2 - View Posts");
			System.out.println(" 3 - Create Post");
			System.out.println(" 4 - Messages");
			System.out.println(" 5 - Online Users");
			System.out.println("");
			System.out.println(" 0 - Logout");
		    choice = sc.nextLine();		    
		    switch (choice) {
		    	case "1":
		    		sendMessage();
		    		break;
		    	case "2":
		    		viewPosts();
		    		break;
		    	case "3":
		    		post();
		    		break;
		    	case "4":
		    		listMessages();
		    		break;
		    	case "5": 
		    		listOnlineUsers();
		    		break;
		    	case "0":
					try {
						m_socialmore.removeOnlineUser(m_user);
					} catch (RemoteException e) {
					}
		    		return;
		    }
	    }
	}
	
	private void listMessages() {
		Scanner sc = new Scanner(System.in);
		ArrayList<PrivateMessage> list = null;
		String author_name, op;
		IUser author;
		int n, index;
		
		while (true) {
			index =	1;
			
			System.out.println("-----------------------");
			System.out.println("   Private Messages");
			System.out.println("-----------------------");
			
			do {
				op = "";
				try {
					list = m_user.getMyMessages();
				} catch (RemoteException e1) {
					reconnect();
					op = "reconnect";
				}
			} while (!op.isEmpty());
			
			if (list != null) { 
				for (IMessage m : list) {
					do {
						op = "";
						try {
							author = m.getSender();
							if (author != null) {
								author_name = author.getName();
								System.out.print("[" + index + "] by " + author_name);
								if (m.getReceiving() == null)
									System.out.print(" *");
								System.out.print("\n");
							}
							index++;
						} catch (RemoteException e) {
							reconnect();
							op = "reconnect";
						}
					} while (!op.isEmpty());
				}
				
				do {	
					op = sc.nextLine();
					
					try {
						n = Integer.parseInt(op);
					} catch (NumberFormatException e) {
						return;
					}
				} while (n <= 0 || n > list.size());
				viewMessage(list.get(n-1));
			} else {
				System.out.println("You have no messages!");
				sc.nextLine();
				return;
			}
		}
	}
	
	private void viewMessage(IMessage m) {
		Scanner sc = new Scanner(System.in);
		String op, name = "";
		IUser author = null;
		
		do {	
			op = "";
			try {
				author = m.getSender();
				if (author != null)
					name = author.getName();
			} catch (RemoteException e) {
				op = "reconnect";
				reconnect();
			}
		} while (!op.isEmpty());
	
		if (author == null) return;
		
		System.out.println("\n--------------------------------------------");
		System.out.println("Sender: " + name);
		try {
			System.out.println(m.read());
		} catch (RemoteException e) {
			reconnect();
		}
		System.out.println("----------------------------------------------");
		System.out.println("[R]eply | [ENTER]Continue");
		
		do {
			op = sc.nextLine().toLowerCase();
		} while (!op.equals("r") && !op.equals("d") && !op.isEmpty());
		
		if (op.equals("r")) {
			try {
				send(m.getSender());
			} catch (RemoteException e) {
				reconnect();
			}
		}
	}
	
	private Boolean isUserInArray(IUser [] arr, IUser user) throws RemoteException {
		for (IUser u : arr) {
			if (u != null && u.getID() == user.getID())
				return true;
		}
		return false;
	}
	
	private void listOnlineUsers() {
		
		Scanner sc = new Scanner(System.in);
		int i, count;
		IUser [] names = null;
		ArrayList<IUser> users = null;

		do {
			try {
				users = m_socialmore.getOnlineUsers();
				count = (users != null) ? users.size() : 0;
				
				
				
				if (count > 0) {
					names = new IUser[count];
					i = 0;
					for (IUser u : users) {
						if (isUserInArray(names, u)) continue;
						names[i++] = u;
					}
				}
			} catch (RemoteException e) {
				reconnect();
				count = -1;
			}
		} while (count == -1);
		
		
		
		System.out.println("-----------------------");
		System.out.println("     Online Users (" + count + ")");
		System.out.println("-----------------------");
		
		if (users != null) {
			for (IUser u : names) {
				try {
					System.out.println("* " + u.getName());
				} catch (RemoteException e) {
				}
			}
		} else {
			System.out.println("There are no users online...");
		}
		
		System.out.println("-----------------------");
		System.out.println("[ENTER]Continue");
		sc.nextLine();
	}
	
	private void viewPosts() {
		
		Scanner sc = new Scanner(System.in);
		Integer n, index;
		String op = "";
		ArrayList<IPost> posts;
		
		do {
			try {
				index = 1;
				
				if (!op.equals("reconnect")) {
					System.out.println("--------------------");
					System.out.println("       Posts");
					System.out.println("--------------------");
				}
				posts = m_socialmore.getPosts(); 
				
				if (posts != null) {
					for (IPost p : posts) {
						System.out.println("[" + (index++) + "]" +p.getContent() + "\n");
					}
				} else {
					System.out.println("There are no posts!\n");
				}
				
				System.out.println("-----------------------------------------------------");
				System.out.println("[#]View Post | [P]ost | [Enter]Continue");
				do {	
					op = sc.nextLine().toLowerCase();	
					try {
						n = Integer.parseInt(op);
					} catch (Exception e) {
						n = -1;
					}
				} while (!(op.equals("p") || op.equals("r") || op.isEmpty() || (n > 0 && n <= posts.size())));
				
				if (op.equals("p"))
					post();
				else if (op.equals("r"))
					return; //Do something
				else if (n > 0 && n <= posts.size())
					viewPost(posts.get(n-1));
				
			} catch (RemoteException e) {
				reconnect();
				op = "reconnect";
			}
		} while (!op.isEmpty());
	
	}
	
	private void viewPost(IPost post) {
		
		Scanner sc = new Scanner(System.in);
		ArrayList<Comment> comments = null;
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String author_name = "";
		Boolean repeat;
		IUser author;
		String op;
		
		do {
			do {
				try {
					comments	= post.getComments();
					author 		= post.getSender();
					author_name = author.getName() ;
					op = "";
				} catch (RemoteException e) {
					reconnect();
					op = "reconnect";
				}
			} while (!op.isEmpty());
			
			
			System.out.println("\n--------------------------------------------");
			try {
				System.out.println("Post: " + post.getContent());
				System.out.println("by " + author_name + " on " + df.format(post.getSending()));
			} catch (RemoteException e1) {
				reconnect();
			}
			System.out.println("----------------------------------------------");

			if (comments == null)
				System.out.println("Be the first one to comment on this post!\n");
			else {
				for (Comment c : comments) {
					do {	
						try {
							author = m_socialmore.getUserByID(c.getUserId());
							System.out.println("> \"" + c.getContent() + "\" by " + author.getName());
							op = "";
						} catch (RemoteException e) {
							reconnect();
							op = "reconnect";
						}
					} while (!op.isEmpty());
				}
			}
			
			System.out.println("------------------------");
			repeat = true;
			try {
				if (this.m_user.getID() == post.getSenderID()) {
					System.out.println("[E]dit | [D]elete | [C]omment | [ENTER]Continue");
					do {
						op = sc.nextLine().toLowerCase();
					} while (!op.equals("d") && !op.equals("e") && !op.equals("c") && !op.isEmpty());
					if (op.equals("c")) {
						repeat = comment(post);
					} else if (op.equals("d")) {
						repeat = false;
						do {	
							try {
								post.delete(this.m_user.getID());
								op = "";
							} catch (RemoteException e) {
								reconnect();
								op = "reconnect";
							}
						} while (!op.isEmpty());
					} else if (op.equals("e")) {
						editPost(post);
					} else {
						repeat = false;
					}
				} else {
					System.out.println("[ENTER]Continue");
					repeat = comment(post);
				}
			} catch (RemoteException e) {
				reconnect();
			}

		} while (repeat);
		
	}
	
	private void editPost(IPost post) {
		Scanner sc = new Scanner(System.in);
		String text;
		Boolean sent = false;
		
		System.out.print("Post: ");
		text = sc.nextLine();
		
		if (text.isEmpty())
			return;
		
		do {
			try {
				post.edit(this.m_user.getID(), text);
				sent = true;
			} catch (RemoteException e) {
				reconnect();
				sent = false;
			}
		} while (!sent);
	}
	
	private Boolean comment(IPost post) {
		String text;
		Boolean sent;
		Scanner sc = new Scanner(System.in);
		
		System.out.print("Comment: ");
		text = sc.nextLine();
		
		if (text.isEmpty())
			return false;
		
		do {
			try {
				m_user.reply(text, post.getID());
				sent = true;
			} catch (RemoteException e) {
				reconnect();
				sent = false;
			}
		} while (!sent);
		
		return true;
	}
	/*
	private void post(Date date) {
		
		String choice;
		Scanner sc = new Scanner(System.in);
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		System.out.println("--------------------");
		System.out.println("       New Post");
		if (date != null)
			System.out.println(dateFormat.format(date));
		System.out.println("--------------------");
		
		postText(date);
		//else if (choice.equals("2"))
		//	postImage();
		//else
		//	editPost();
	}
	*/
	private void post() {
		post(null);
	}

	private void post(Date date) {
		Scanner sc = new Scanner(System.in);
		String text;
		int done;
		
		System.out.print("Text: ");
		text = sc.nextLine();
		
		if (text.isEmpty())
			return;
		
		done = 1;
		do {
			try {
				m_user.post(text);
				done = 1;
			} catch (RemoteException e) {
				reconnect();
				done = 0;
			}
		} while (done == 0);
		
		System.out.println("Your message was successfuly posted!");
	}
	
	private void sendMessage() {
		Scanner sc = new Scanner(System.in);
		ArrayList<IUser> users = null;
		String [] list = null;
		IUser user;
		String u;
		int op, i;
		String receiver, subject, content;
		
		System.out.println("--------------------");
		System.out.println("    New Message");
		System.out.println("--------------------\n");
		
		op = 0;
		do {
			if (op++ > 0) {
				System.out.println("No users were found...\n");
			}
			
			System.out.print("Receiver's name: ");
			receiver = sc.nextLine();
			
			if (receiver.isEmpty())
				return;
		
			do {
				try {
					users = m_socialmore.searchUser(receiver);
					if (users != null) {
						list = new String[users.size()];
						for (i = users.size()-1; i >= 0; i--) {
							list[i] = users.get(i).getName();
						}
					}
					u = "";
				} catch (RemoteException e) {
					reconnect();
					u = "reconnect";
				}
				
			} while (!u.isEmpty());
		} while (users == null);
		
		i = 0;
		for (String n : list)
			System.out.println("[" + (i+1) + "] " + list[i++]);


		System.out.println("----------------------------");
		System.out.println("Choose a user | [0] Back");
		do {
			try {
				op = Integer.parseInt(sc.nextLine());
			} catch (Exception e) {
				op = -1;
			}
		} while (op < 0 || op > users.size());
	
		if (op == 0)
			return;
		
		user = users.get(op-1);
		
		send(user);
	}
	
	private void send(IUser user) {
		
		int op;
		Scanner sc = new Scanner(System.in);
		String content;
		
		System.out.print("\nMessage: ");
		content = sc.nextLine();
		if (content.isEmpty())
			return;
		
		do {
			op = 0;
			try {
				if (m_user.sendPrivateMessage(user.getID(), content))
					System.out.println("Message successfuly sent!");
				else
					System.out.println("Could not send the message...");
			} catch (RemoteException e) {
				reconnect();
				op = -1;
			}
		} while (op == -1);
	}
	
	public static void main(String[] args) {
		
		SocialClient client;
		try {
			client = new SocialClient();
			client.start();
		} catch (RemoteException e) {
			
		}	
	}

	@Override
	public Boolean ping() throws RemoteException {
		return true;
	}
}
