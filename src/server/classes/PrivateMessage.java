package server.classes;

import java.rmi.RemoteException;
import java.util.Date;

public class PrivateMessage extends Message implements IMessage {

	private static final long serialVersionUID = 1L;
	
	private Date m_receiving;
	private IUser m_sender;
	
	public PrivateMessage(int message_id, int sender_id, Date sending,
			String content, int receiver_id, Date receiving) throws RemoteException {
		super(message_id, sender_id, receiver_id, content, sending, null);
		this.m_receiving = receiving;
		this.m_sender = Socialmore.instance.getUserByID(this.m_sender_id);
	}

	public Date getReceiving() throws RemoteException {
		return m_receiving;
	}

	public void setReceiving(Date m_receiving) {
		this.m_receiving = m_receiving;
	}

	public IUser getReceiver() {
		return m_sender;
	}

	public void setReceiver(IUser sender) {
		this.m_sender = sender;
	}
}
