package server.classes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class SocialServer {

	private static void ping() {
		
		int serverPort = 6511;
		Util.print("Starting stay alive!");
		DatagramSocket aSocket = null;
		byte[] m = ("awake").getBytes();
		DatagramPacket request;
		int timeout = 1000*5;
		
		try {
			request = new DatagramPacket(m, m.length, InetAddress.getByName("localhost"), serverPort);
		} catch (UnknownHostException e1) {
			Util.print("UnknownHostException on ping");
			return;
		}
		
		try {
			aSocket = new DatagramSocket();
			
			while (true) {
				Thread.sleep(timeout);
				aSocket.send(request);
				//Log.print("Ping sent packet");
			}
		} catch (SocketException e) {
			Util.print("SocketException on ping");
			return;
		} catch (IOException e) {
			Util.print("IOException on ping");
			return;
		} catch (InterruptedException e) {
			Util.print("InterruptedException on ping");
			return;
		} finally {
			if(aSocket != null)
				aSocket.close();
		}
	}
	
	private static boolean pong() {
		
		byte[] buffer = new byte[1000];
		DatagramSocket aSocket = null;
		int port = 6511;
		int timeout = (int)(2*1000*5);
		
		try {
			aSocket = new DatagramSocket(port);
			aSocket.setSoTimeout(timeout);
		} catch (SocketException e1) {
			Util.print("SocketException on pong!");
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e2) { }
			return pong();
		}
		
		Util.print("Socket Datagram listening on port: "+port);
		try {
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				Util.print("Theres a active server!");
			}
		} catch (SocketException e) {
			Util.print("SocketException on pong!");
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e1) { }
			return pong();
		} catch (IOException e) {
			Util.print("IOException on pong!");
			return true;
		} finally {
			if(aSocket != null)
				aSocket.close();
		}
	}

	private static void start() {
		Socialmore socialmore;
		
		try {
			socialmore = new Socialmore();
			
			LocateRegistry.createRegistry(7000);
			
			Naming.rebind ("rmi://localhost:7000/Socialmore", socialmore);
		   
			ping();
			
		} catch (Exception e) {
	      System.out.println ("Socialmor Server failed: " + e);
	    }
	}
	
	public static void main(String[] args) {
		start();
		if (pong())
			start();
		else
			Util.print("Unable to keep up!");		
	}
}
