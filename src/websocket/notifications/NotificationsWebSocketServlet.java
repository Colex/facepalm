/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package websocket.notifications;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import server.classes.ISocialmore;
import server.classes.IUser;
import server.classes.Socialmore;
import server.classes.User;

public class NotificationsWebSocketServlet extends WebSocketServlet {

    private static final long serialVersionUID = 1L;

    final CopyOnWriteArraySet<NotificationMessageInbound> connections =
            new CopyOnWriteArraySet<NotificationMessageInbound>();

    private Notifications m_notifier = null;
    public ISocialmore m_sm = null;
    
    @Override
    protected StreamInbound createWebSocketInbound(String subProtocol,
            HttpServletRequest request) {
    	IUser user;
    	ISocialmore sm;
    	
    	if (m_notifier == null) {
    		try {
				m_notifier = new Notifications(this);
    		} catch (RemoteException e) {
    			
    			m_notifier = null;
			}
    	}
    	
    	
    	user 	= User.getSession(request);
    	sm 		= Socialmore.connect(this);
    	if (sm == null || user == null) {
    		return null;
    	} else {
    		m_sm = sm;
    		try {
    			System.out.println("Setting notifier: " + m_notifier);
				sm.setSocialNotifier(m_notifier);
			} catch (RemoteException e) {
			}
    	}
    	
    	
        return new NotificationMessageInbound(this, request, user, sm);
    }
}