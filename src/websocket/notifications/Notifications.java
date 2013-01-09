package websocket.notifications;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import server.classes.IUser;


public class Notifications extends UnicastRemoteObject implements INotifications {

	private static final long serialVersionUID = 1801299837722037058L;
	private NotificationsWebSocketServlet m_connector;
	
	protected Notifications(NotificationsWebSocketServlet connector) throws RemoteException {
		super();
		m_connector = connector;
	}

	@Override
	public void notifyOnlineUser(IUser u) throws RemoteException {
		NotificationMessageInbound.connectOutsider(m_connector.m_sm, m_connector, u);
	}

}
