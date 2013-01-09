package server.classes;

import java.rmi.RemoteException;
import java.util.Date;

public class PublicMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public PublicMessage(int message_id, int sender_id, Date sending,
			String content, int chat_id) throws RemoteException {
		super(message_id, sender_id, chat_id, content, sending, null);
	}
}
