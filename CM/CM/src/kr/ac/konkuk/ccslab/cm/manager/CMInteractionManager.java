package kr.ac.konkuk.ccslab.cm.manager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import kr.ac.konkuk.ccslab.cm.entity.CMChannelInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUnknownChannelInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.info.CMThreadInfo;
import kr.ac.konkuk.ccslab.cm.thread.CMClientKeepAliveTask;

import java.sql.*;

public class CMInteractionManager {

	// initialize the interaction info object in cmInfo
	public static boolean init(CMInfo cmInfo)
	{
		CMCommInfo commInfo = cmInfo.getCommInfo();
		
		// initialize DB
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		if(confInfo.isDBUse() && CMConfigurator.isDServer(cmInfo))
		{
			CMDBManager.init(cmInfo);
		}
		
		// check the system type
		String strSysType = confInfo.getSystemType();
		if(!strSysType.equals("SERVER") && !strSysType.equals("CLIENT"))
		{
			System.out.println("CMInteractionInfo.init(), wrong system type ("+strSysType+").");
			return false;
		}
		
		// open a server socket channel in the case of a server
		CMUser myself = cmInfo.getInteractionInfo().getMyself();
		if(strSysType.equals("SERVER"))
		{
			if(CMConfigurator.isDServer(cmInfo))
			{
				myself.setName("SERVER");				
			}
			
			ServerSocketChannel ssc = null;
			try {
				ssc = (ServerSocketChannel) CMCommManager.openNonBlockChannel(CMInfo.CM_SERVER_CHANNEL, 
						confInfo.getMyAddress(), confInfo.getMyPort(), cmInfo);
				commInfo.setNonBlockServerSocketChannel(ssc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
		
		// open a stream socket channel in the case of a client or a server which is not a default server
		if(strSysType.equals("CLIENT") || (strSysType.equals("SERVER") && !CMConfigurator.isDServer(cmInfo)))
		{
			boolean ret = connectDefaultServer(cmInfo);
			if(!ret)
			{
				System.out.println("CMInteractionManager.init(), connection to the default server FAILED.");
				return false;
			}
		}
		
		// open a datagram channel
		DatagramChannel dc = null;
		try {
			dc = (DatagramChannel) CMCommManager.openNonBlockChannel(CMInfo.CM_DATAGRAM_CHANNEL, 
					confInfo.getMyAddress(), confInfo.getUDPPort(), cmInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		// store the datagram channel
		commInfo.getNonBlockDatagramChannelInfo().addChannel(confInfo.getUDPPort(), dc);
		
		// set session info
		createSession(cmInfo);
	
		// initialize sessions
		CMSessionManager.init(cmInfo);
		
		// initialize file manager
		CMFileTransferManager.init(cmInfo);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMInteractionManager.init(), succeeded.");
		
		return true;
	}
	
	public static void terminate(CMInfo cmInfo)
	{
		CMFileTransferManager.terminate(cmInfo);		
	}
	
	public static boolean connectDefaultServer(CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMUser myself = interInfo.getMyself();
		SocketChannel sc = null;
		CMServer dServer = null;
		
		// check the user state
		if(myself.getState() != CMInfo.CM_INIT)
		{
			System.out.println("CMInteractionManager.connectDefaultServer(), already connected to the default server.");
			return false;
		}
		
		// connection establishment to the default server
		try {
			sc = (SocketChannel) CMCommManager.openNonBlockChannel(CMInfo.CM_SOCKET_CHANNEL, 
					confInfo.getServerAddress(), confInfo.getServerPort(), cmInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// store server info and channel
		dServer = interInfo.getDefaultServerInfo();
		String strDefServerName = dServer.getServerName();
		if(strDefServerName == null || strDefServerName.isEmpty() 
				|| strDefServerName.contentEquals("?"))
		{
			dServer.setServerName("SERVER");	// name of the default server
		}
		dServer.setServerAddress(confInfo.getServerAddress());
		dServer.setServerPort(confInfo.getServerPort());
		dServer.getNonBlockSocketChannelInfo().addChannel(0, sc);	// default channel number: 0
		
		// update the user's state
		myself.setState(CMInfo.CM_CONNECT);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMInteractionManager.connectDefaultServer(), succeeded.");
		
		return true;
	}
	
	public static boolean disconnectFromDefaultServer(CMInfo cmInfo)
	{
		// check user's state
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMUser myself = interInfo.getMyself();
		if(myself.getState() == CMInfo.CM_INIT)
		{
			System.err.println("Not connected to default server yet.");
			return false;
		}
		
		// remove all channels to the default server
		CMServer dsInfo = interInfo.getDefaultServerInfo();
		dsInfo.getNonBlockSocketChannelInfo().removeAllChannels();
		dsInfo.getBlockSocketChannelInfo().removeAllChannels();
		
		// remove all the ongoing file-transfer info about the default server
		fInfo.removeRecvFileList(interInfo.getDefaultServerInfo().getServerName());
		fInfo.removeSendFileList(interInfo.getDefaultServerInfo().getServerName());
		// remove all the ongoing sns related file-transfer info at the client
		snsInfo.getRecvSNSAttachList().removeAllSNSAttach();
		// remove all session info
		interInfo.getSessionList().removeAllElements();

		// update user's state
		myself.setState(CMInfo.CM_INIT);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.disconnectFromDefautServer(), succeeded.");
		}
		
		return true;
	}
	
	public static int getNumLoginServers(CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		if(!confInfo.getSystemType().contentEquals("CLIENT"))
		{
			System.err.println("CMInteractionManager.getNumLoginServers(), system type "
					+ "is not CLIENT!");
			return -1;
		}
		
		int nNumLogins = 0;
		if(interInfo.getMyself().getState() >= CMInfo.CM_LOGIN)
			nNumLogins++;
		Vector<CMServer> addServerList = interInfo.getAddServerList();
		for(CMServer addServer : addServerList)
		{
			if(addServer.getClientState() >= CMInfo.CM_LOGIN)
				nNumLogins++;
		}
		
		return nNumLogins;
	}
	
	// connect to an additional server
	public static boolean connectAddServer(String strName, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMServer tserver = null;

		tserver = interInfo.findAddServer(strName);
		if( tserver == null )
		{
			System.out.println("CMInteractionManager.connectAddServer(), server("+strName
					+") info not found in the add-server info list.");
			return false;
		}
		
		if( tserver.getClientState() != CMInfo.CM_INIT )
		{
			System.out.println("Already connected to the server("+strName+").");
			return false;
		}

		SelectableChannel sc = null;
		try {
			sc = CMCommManager.openNonBlockChannel(CMInfo.CM_SOCKET_CHANNEL, tserver.getServerAddress(), 
										tserver.getServerPort(), cmInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( sc == null )
		{
			System.out.println("CMInteractionManager.connectAddServer(), fail to connect to "
					+ "the server("+strName+").");
			return false;
		}

		// add channel info
		tserver.getNonBlockSocketChannelInfo().addChannel(0, sc);
		
		// update peer's state
		tserver.setClientState(CMInfo.CM_CONNECT);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.connectAddServer() successfully connect to "
					+ "server("+strName+").");
		}

		return true;
	}
	
	// disconnect from an additional server
	public static boolean disconnectFromAddServer(String strName, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMServer tserver = interInfo.findAddServer(strName);
		if( tserver == null )
		{
			System.out.println("CMInteractionManager.disconnectFromAddServer(), server("
					+strName+") info not found in the add-server info list.");
			return false;
		}

		if( tserver.getClientState() == CMInfo.CM_INIT )
		{
			System.out.println("CMInteractionManager.disconnectFromAddServer(), not yet connected "
					+ "to server("+strName+")!");
			return false;
		}

		// close and delete all channels of the server
		tserver.getNonBlockSocketChannelInfo().removeAllChannels();
		tserver.getBlockSocketChannelInfo().removeAllChannels();
		// remove ongoing file-transfer information
		fInfo.removeRecvFileList(tserver.getServerName());
		fInfo.removeSendFileList(tserver.getServerName());
		// remove session information
		tserver.getSessionList().removeAllElements();

		// update peer's state
		tserver.setClientState(CMInfo.CM_INIT);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.disconnectFromAddServer(), "
					+ "successfully disconnected from server("+strName+").");
		}
		
		return true;
	}
	
	public synchronized static boolean disconnectBadNode(SocketChannel badSC, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		if(confInfo.getSystemType().contentEquals("SERVER"))
			return disconnectBadNodeByServer(badSC, cmInfo);
		else
			return disconnectBadNodeByClient(badSC, cmInfo);
	}
	
	private static boolean disconnectBadNodeByServer(SocketChannel badSC, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		boolean bRet = false;
		
		if(CMConfigurator.isDServer(cmInfo))
		{
			CMServer addServer = findAddServerWithSocketChannel(badSC, interInfo.getAddServerList());
			if(addServer != null)
			{
				// The default server disconnects from the additional server
				return disconnectBadAddServerByDefaultServer(addServer, cmInfo);
			}
		}
		else
		{
			CMServer defServer = interInfo.getDefaultServerInfo();
			if(isChannelBelongsToServer(badSC, defServer))
			{
				// The additional server disconnects from the default server
				return disconnectBadDefaultServerByAddServer(cmInfo);
			}
		}
		
		CMUser user = findUserWithSocketChannel(badSC, interInfo.getLoginUsers());
		if(user != null)
		{
			// The server disconnects from the client
			return disconnectBadClientByServer(user, cmInfo);
		}
		
		CMUnknownChannelInfo unchInfo = commInfo.getUnknownChannelInfoList()
				.findElement(new CMUnknownChannelInfo(badSC));
		if(unchInfo != null)
		{
			// disconnect from the unknown channel
			try {
				badSC.close();
				
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMIntearctionManager.disconnectBadNodeByServer() "
							+"intentionally disconnected from unknown channel: "+badSC);
					System.out.println("channel hash code: "+badSC.hashCode());
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// remove from the unknown channel list
			bRet = commInfo.getUnknownChannelInfoList().removeElement(unchInfo);
			
			if(bRet && CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.disconnectBadNodeByServer() "
						+"removed from unknown-channel list: "+badSC);
				System.out.println("channel hash code: "+badSC.hashCode());
			}
			else {
				System.err.println("CMInteractionManager.disconnectBadNodeByServer() "
						+"error to remove from unknown-channel list: "+badSC);
				System.err.println("channel hash code: "+badSC.hashCode());
			}
			
			return bRet;
		}
		
		System.err.println("CMInteractionManager.disconnectBadNodeByServer(): "+badSC);
		System.err.println("channel hash code: "+badSC.hashCode());
		System.err.println("cannot find a connected client or server information!");
		return false;
	}
	
	public synchronized static boolean disconnectBadAddServerByDefaultServer(CMServer addServer, CMInfo cmInfo)
	{
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		String strAddServerName = addServer.getServerName();
		
		// notify clients of the deregistration
		CMMultiServerEvent mse = new CMMultiServerEvent();
		mse.setID(CMMultiServerEvent.NOTIFY_SERVER_LEAVE);
		mse.setServerName(addServer.getServerName());
		CMEventManager.broadcastEvent(mse, cmInfo);

		// remove all socket channels
		addServer.getNonBlockSocketChannelInfo().removeAllChannels();
		addServer.getBlockSocketChannelInfo().removeAllChannels();
		
		// remove file-transfer info
		fInfo.removeRecvFileList(addServer.getServerName());
		fInfo.removeSendFileList(addServer.getServerName());
		
		// remove add-server info
		cmInfo.getInteractionInfo().removeAddServer(addServer.getServerName());
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMIntearctionManager.disconnectBadAddServerByDefaultServer()"
					+"intentionally disconnected from additional server ("+strAddServerName+").");
		}
		
		// notify the app event handler
		notifyAppEventHandlerOfIntentionalDisconnection(strAddServerName, cmInfo);
		
		return true;
	}
	
	private static void notifyAppEventHandlerOfIntentionalDisconnection(String strChannelName, 
			CMInfo cmInfo)
	{
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.INTENTIONALLY_DISCONNECT);
		se.setChannelName(strChannelName);
		cmInfo.getAppEventHandler().processEvent(se);
	}
	
	private static boolean disconnectBadDefaultServerByAddServer(CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMServer defServer = interInfo.getDefaultServerInfo();
		defServer.getNonBlockSocketChannelInfo().removeAllChannels();
		defServer.getBlockSocketChannelInfo().removeAllChannels();
		
		fInfo.removeRecvFileList(defServer.getServerName());
		fInfo.removeSendFileList(defServer.getServerName());

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMIntearctionManager.disconnectBadDefaultServerByAddServer()"
					+"intentionally disconnected from the default server.");
		}

		// notify the app event handler
		notifyAppEventHandlerOfIntentionalDisconnection(defServer.getServerName(), cmInfo);

		return true;
	}
	
	public synchronized static boolean disconnectBadClientByServer(CMUser user, CMInfo cmInfo)
	{
		String strUser = user.getName();
		
		// send MQTT will event
		CMMqttManager mqttManager = (CMMqttManager) cmInfo.getServiceManagerHashtable()
				.get(CMInfo.CM_MQTT_MANAGER);
		mqttManager.sendMqttWill(user.getName());

		SocketChannel sc = (SocketChannel)user.getNonBlockSocketChannelInfo().findChannel(0);
		try {
			sc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// trigger the logout process
		CMSessionEvent tse = new CMSessionEvent();
		tse.setID(CMSessionEvent.LOGOUT);
		tse.setUserName(user.getName());
		CMMessage msg = new CMMessage();
		msg.m_buf = CMEventManager.marshallEvent(tse);
		CMInteractionManager.processEvent(msg, cmInfo);
		cmInfo.getAppEventHandler().processEvent(tse);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMIntearctionManager.disconnectBadClientByServer()"
					+"intentionally disconnected from client ("+strUser+").");
		}

		// notify the app event handler
		notifyAppEventHandlerOfIntentionalDisconnection(strUser, cmInfo);

		return true;
	}
	
	private static boolean disconnectBadNodeByClient(SocketChannel badSC, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		CMServer defServer = interInfo.getDefaultServerInfo();
		boolean bRet = false;
		
		if(isChannelBelongsToServer(badSC, defServer))
		{
			// ch belongs to the default server

			CMChannelInfo<Integer> chInfo = defServer.getNonBlockSocketChannelInfo();
			CMSNSInfo snsInfo = cmInfo.getSNSInfo();
			
			// remove all non-blocking channels
			chInfo.removeAllChannels();
			// remove all blocking channels
			defServer.getBlockSocketChannelInfo().removeAllChannels();
			
			// For the clarity, the client must be back to initial state (not yet)
			// stop all the file-transfer threads
			//List<Runnable> ftList = fInfo.getExecutorService().shutdownNow();
			// remove all the ongoing file-transfer info about the default server
			fInfo.removeRecvFileList(interInfo.getDefaultServerInfo().getServerName());
			fInfo.removeSendFileList(interInfo.getDefaultServerInfo().getServerName());
			// remove all the ongoing sns related file-transfer info at the client
			snsInfo.getRecvSNSAttachList().removeAllSNSAttach();
			// remove all session info
			interInfo.getSessionList().removeAllElements();
			// initialize the client state
			interInfo.getMyself().setState(CMInfo.CM_INIT);
			
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.disconnectBadNodeByClient(): "
						+ "intentionally disconnected from the default server: "+badSC);
				System.out.println("channel hash code: "+badSC.hashCode());
			}

			// notify the app event handler
			notifyAppEventHandlerOfIntentionalDisconnection(defServer.getServerName(), cmInfo);

			// check and stop the scheduled keep-alive task
			if(CMInteractionManager.getNumLoginServers(cmInfo) == 0)
			{
				CMThreadInfo threadInfo = cmInfo.getThreadInfo();
				ScheduledFuture<?> future = threadInfo.getScheduledFuture();
				if(future != null)
				{
					future.cancel(true);
					
					if(CMInfo._CM_DEBUG)
					{
						System.out.println("CMInteractionManager.disconnectBadNode"
								+ "ByClient(), stop the client keep-alive task.");
					}					
				}
			}

			return true;
		}
		
		CMServer addServer = findAddServerWithSocketChannel(badSC, interInfo.getAddServerList());
		if(addServer != null)
		{
			// ch belongs to an additional server
			String strAddServer = addServer.getServerName();
			addServer.getNonBlockSocketChannelInfo().removeAllChannels();
			addServer.getBlockSocketChannelInfo().removeAllChannels();
			fInfo.removeRecvFileList(addServer.getServerName());
			fInfo.removeSendFileList(addServer.getServerName());
			addServer.getSessionList().removeAllElements();
			addServer.setClientState(CMInfo.CM_INIT);
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.disconnectBadNodeByClient(): "
						+ "intentionally disconnected from an additional server("
						+strAddServer+"): "+badSC);
				System.out.println("channel hash code: "+badSC.hashCode());
			}
			
			// notify the app event handler
			notifyAppEventHandlerOfIntentionalDisconnection(strAddServer, cmInfo);

			// check and stop the scheduled keep-alive task
			if(CMInteractionManager.getNumLoginServers(cmInfo) == 0)
			{
				CMThreadInfo threadInfo = cmInfo.getThreadInfo();
				ScheduledFuture<?> future = threadInfo.getScheduledFuture();
				future.cancel(true);
				
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMInteractionManager.disconnectBadNodeByClient(),"
							+"stop the client keep-alive task.");
				}
			}

			return true;
		}
		
		// check unknown-channel list
		CMUnknownChannelInfo unchInfo = commInfo.getUnknownChannelInfoList()
				.findElement(new CMUnknownChannelInfo(badSC));
		if(unchInfo != null)
		{
			// disconnect from the unknown channel
			try {
				badSC.close();
				
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMIntearctionManager.disconnectBadNodeByClient() "
							+"intentionally disconnected from unknown channel: "+badSC);
					System.out.println("channel hash code: "+badSC.hashCode());
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// remove from the unknown channel list
			bRet = commInfo.getUnknownChannelInfoList().removeElement(unchInfo);
			
			if(bRet && CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.disconnectBadNodeByClient() "
						+"removed from unknown-channel list: "+badSC);
				System.out.println("channel hash code: "+badSC.hashCode());
			}
			else {
				System.err.println("CMInteractionManager.disconnectBadNodeByClient() "
						+"error to remove from unknown-channel list: "+badSC);
				System.err.println("channel hash code: "+badSC.hashCode());
			}
			
			return bRet;
		}
		
		// check group member of this client
		CMUser groupUser = findGroupMemberOfClientWithSocketChannel(badSC, cmInfo);
		if(groupUser != null)
		{
			String strGroupUserName = groupUser.getName();
			// remove all socket channels
			groupUser.getNonBlockSocketChannelInfo().removeAllChannels();
			groupUser.getBlockSocketChannelInfo().removeAllChannels();

			// remove file-transfer info

			// notify the app event handler
			notifyAppEventHandlerOfIntentionalDisconnection(strGroupUserName, cmInfo);
		}
		
		System.err.println("CMInteractionManager.disconnectBadNodeByClient(): "+badSC);
		System.err.println("channel hash code: "+badSC.hashCode());
		System.err.println("cannot find the connected default or additional server information!");
		return false;
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	
	public static boolean processEvent(CMMessage msg, CMInfo cmInfo)
	{
		boolean bReturn = true;	// the flag of whether the event will be forwarded to the application or not
		CMEvent cmEvent = null;

		// unmarshall an event
		cmEvent = CMEventManager.unmarshallEvent(msg.m_buf);
		if(cmEvent == null)
		{
			System.err.println("CMInteractionManager.processEvent(), unmarshalled event is null.");
			if(msg.m_ch instanceof SocketChannel)
				CMInteractionManager.disconnectBadNode((SocketChannel)msg.m_ch, cmInfo);
			return false;
		}
		
		// update the last event-transmission time of the sender
		if(msg.m_ch instanceof SocketChannel)
			updateLastEventTransTime(msg.m_ch, cmInfo);

		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("---- CMInteractionManager.processEvent() starts. event(type: "
					+cmEvent.getType()+", id: "+cmEvent.getID()+").");
		}

		// check session handler
		String strHandlerSession = cmEvent.getHandlerSession();
		String strHandlerGroup = cmEvent.getHandlerGroup();
		boolean bProcessed = false;
		if(strHandlerSession != null && !strHandlerSession.equals(""))
		{
			// deliver msg to session manager

			if(strHandlerGroup != null && !strHandlerGroup.equals(""))
				CMGroupManager.processEvent(msg, cmInfo);
			else
				CMSessionManager.processEvent(msg, cmInfo);
			
			bProcessed = true;
		}
		else
		{
			int nEventType = cmEvent.getType();
			switch(nEventType)
			{
			case CMInfo.CM_FILE_EVENT:
				bReturn = CMFileTransferManager.processEvent(msg, cmInfo);
				bProcessed = true;
				break;
			case CMInfo.CM_SNS_EVENT:
				CMSNSManager.processEvent(msg, cmInfo);
				bProcessed = true;
				break;
			case CMInfo.CM_SESSION_EVENT:
				bReturn = processSessionEvent(msg, cmInfo);
				bProcessed = true;
				break;
			case CMInfo.CM_MULTI_SERVER_EVENT:
				processMultiServerEvent(msg, cmInfo);
				bProcessed = true;
				break;
			case CMInfo.CM_USER_EVENT:
				if(CMInfo._CM_DEBUG)
					System.out.println("CMInteractionManager.processEvent(), user event, nothing to do.");
				bProcessed = true;
				break;
			default:
				bProcessed = false;
				/*
				System.err.println("CMInteractionManager.processEvent(), unknown event type: "
						+nEventType);
				cmEvent = null;
				return true;
				*/
				
			}
		}
		
		// The above process of finding an event handler will be changed to as follows:
		if(!bProcessed)
		{
			Hashtable<Integer, CMEventHandler> handlerHashtable = cmInfo.getEventHandlerHashtable();
			CMEventHandler handler = handlerHashtable.get(cmEvent.getType());
			if(handler != null)
			{
				bReturn = handler.processEvent(cmEvent);
			}
			else {
				System.err.println("CMInteractionManager.processEvent(), unknown event type: "
						+cmEvent.getType());
				cmEvent = null;
				return true;
			}
			
		}
		
		// distribution to other session members or group members, if required
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		if(confInfo.getSystemType().equals("SERVER"))
		{
			if(msg.m_ch instanceof SocketChannel)
			{
				distributeEvent(cmEvent.getDistributionSession(), cmEvent.getDistributionGroup(), 
						cmEvent, CMInfo.CM_STREAM, cmInfo);
			}
			else if(msg.m_ch instanceof DatagramChannel)
			{
				distributeEvent(cmEvent.getDistributionSession(), cmEvent.getDistributionGroup(),
						cmEvent, CMInfo.CM_DATAGRAM, cmInfo);
			}
		}
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMInteractionManager.processEvent() ends.");
		
		// clear event object (message object is cleared at the EventReceiver)
		cmEvent = null;
		
		return bReturn;
	}
	
	// find a user who connects with a socket channel in loginUsers.
	public synchronized static CMUser findUserWithSocketChannel(SelectableChannel ch, CMMember loginUsers)
	{
		String strUserName = null;
		boolean isBlock = false;
		boolean bFound = false;
		Integer returnKey = null;
		
		if(ch == null)
		{
			System.err.println("CMInteractionManager.findUserWithSocketChannel(), "
					+ "channel is null!");
			return null;
		}
		if(loginUsers == null)
		{
			System.err.println("CMInteractionManager.findUserWithSocketChannel(), "
					+ "login member list is null!");
			return null;
		}
		
		isBlock = ch.isBlocking();
		
		Iterator<CMUser> iter = loginUsers.getAllMembers().iterator();
		CMUser tuser = null;
		while(iter.hasNext() && !bFound)
		{
			tuser = iter.next();
			if(isBlock)
				returnKey = tuser.getBlockSocketChannelInfo().findChannelKey(ch);
			else
				returnKey = tuser.getNonBlockSocketChannelInfo().findChannelKey(ch);
			
			if(returnKey != null)
			{
				strUserName = tuser.getName();
				bFound = true;
				if(CMInfo._CM_DEBUG_2)
					System.out.println("CMInteractionManager.findUserWithSocketChannel(), user("+strUserName+") found.");
			}
		}
		
		if(bFound)
			return tuser;
		else
			return null;
	}
	
	// find group member with the session name, group name, and member name (called only by the server)
	public synchronized static CMUser findGroupMemberOfServer(String strSession, String strGroup, String strUser, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMSession session = interInfo.findSession(strSession);
		if(session == null)
		{
			System.err.println("CMInteractionManager.findGroupMemberOfServer(), session("
					+strSession+") not found!");
			return null;
		}
		CMGroup group = session.findGroup(strGroup);
		if(group == null)
		{
			System.err.println("CMInteractionManager.findGroupMemberOfServer(), group("
					+strGroup+") not found!");
			return null;
		}
		CMUser user = group.getGroupUsers().findMember(strUser);
		if(user == null)
		{
			System.err.println("CMInteractionManager.findGroupMemberOfServer(, user("
					+strUser+") not found!");
			return null;
		}
		
		return user;
	}
	
	// find my group member (called only by the client)
	public synchronized static CMUser findGroupMemberOfClient(String strUser, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		String strSession = interInfo.getMyself().getCurrentSession();
		String strGroup = interInfo.getMyself().getCurrentGroup();
		
		CMSession session = interInfo.findSession(strSession);
		if(session == null)
		{
			System.err.println("CMInteractionManager.findGroupMemberOfClient(), session("
					+strSession+") not found!");
			return null;
		}
		CMGroup group = session.findGroup(strGroup);
		if(group == null)
		{
			System.err.println("CMInteractionManager.findGroupMemberOfClient(), group("
					+strGroup+") not found!");
			return null;
		}
		CMUser user = group.getGroupUsers().findMember(strUser);
		if(user == null)
		{
			System.err.println("CMInteractionManager.findGroupMemberOfClient(), user("
					+strUser+") not found!");
			return null;
		}
		
		return user;
	}
	
	// find my group member with channel (called only by the client)
	public synchronized static CMUser findGroupMemberOfClientWithSocketChannel(SelectableChannel ch, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		String strSession = interInfo.getMyself().getCurrentSession();
		String strGroup = interInfo.getMyself().getCurrentGroup();
		
		String strUserName = null;
		boolean isBlock = false;
		boolean bFound = false;
		Integer returnKey = null;
		
		CMSession session = interInfo.findSession(strSession);
		if(session == null)
		{
			System.err.println("CMInteractionManager.findGroupMemberOfClient(), session("
					+strSession+") not found!");
			return null;
		}
		CMGroup group = session.findGroup(strGroup);
		if(group == null)
		{
			System.err.println("CMInteractionManager.findGroupMemberOfClient(), group("
					+strGroup+") not found!");
			return null;
		}

		isBlock = ch.isBlocking();
		Iterator<CMUser> iter = group.getGroupUsers().getAllMembers().iterator();
		CMUser tuser = null;
		while(iter.hasNext() && !bFound)
		{
			tuser = iter.next();
			if(isBlock)
				returnKey = tuser.getBlockSocketChannelInfo().findChannelKey(ch);
			else
				returnKey = tuser.getNonBlockSocketChannelInfo().findChannelKey(ch);
			
			if(returnKey != null)
			{
				strUserName = tuser.getName();
				bFound = true;
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager."
							+ "findGroupMemberOfClientWithSocketChannel(), user("
							+strUserName+") found.");
				}
			}
		}
		
		if(bFound)
			return tuser;
		else
			return null;
	}
	
	// find (default or additional) server info
	public synchronized static CMServer findServer(String strTarget, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		CMServer serverInfo = null;
		
		if(strTarget.equals(strDefServer))
		{
			serverInfo = interInfo.getDefaultServerInfo();
		}
		else 
		{
			serverInfo = interInfo.findAddServer(strTarget);
		}
		
		if(serverInfo == null && CMInfo._CM_DEBUG)
		{
			System.err.println("CMInteractionManager.findServer, server("+strTarget+") not found!");
			return null;
		}
		
		return serverInfo;
	}
	
	// find an additional server with socket channel
	public synchronized static CMServer findAddServerWithSocketChannel(SelectableChannel ch, Vector<CMServer> list)
	{
		String strServerName = null;
		boolean isBlock = false;
		boolean bFound = false;
		Integer returnKey = null;
		
		if(ch == null)
		{
			System.err.println("CMIneteractionManager.findAddServerWithSocketChannel(), "
					+"the channel is null!");
			return null;
		}
		
		if(list == null)
		{
			System.err.println("CMInteractionManager.findAddServerWithSocketChannel(), "
					+"the add-server list is null!");
			return null;
		}
		
		isBlock = ch.isBlocking();

		Iterator<CMServer> iter = list.iterator();
		CMServer tServer = null;
		while(iter.hasNext() && !bFound)
		{
			tServer = iter.next();
			if(isBlock)
				returnKey = tServer.getBlockSocketChannelInfo().findChannelKey(ch);
			else
				returnKey = tServer.getNonBlockSocketChannelInfo().findChannelKey(ch);
			
			if(returnKey != null)
			{
				strServerName = tServer.getServerName();
				bFound = true;
				if(CMInfo._CM_DEBUG_2)
					System.out.println("CMInteractionManager.findAddServerWithSocketChannel(), "
							+ "server("+strServerName+") found.");
			}
		}
		
		if(bFound)
			return tServer;
		else
			return null;
	}
	
	public synchronized static boolean isChannelBelongsToServer(SelectableChannel ch, CMServer server)
	{
		boolean isBlock = false;
		Integer chKey = null;
		
		if(ch == null)
		{
			System.err.println("CMInteractionManager.isChannelBelongsToServer(), channel is null!");
			return false;
		}
		if(server == null)
		{
			System.err.println("CMInteractionManager.isChannelBelongsToServer(), server is null!");
			return false;
		}
		
		isBlock = ch.isBlocking();
		if(isBlock)
			chKey = server.getBlockSocketChannelInfo().findChannelKey(ch);
		else
			chKey = server.getNonBlockSocketChannelInfo().findChannelKey(ch);
		
		if(chKey != null)
		{
			if(CMInfo._CM_DEBUG_2)
			{
				System.out.println("CMInteractionManager.isChannelBelongsToServer(): YES, "
						+ch+", server("+server.getServerName()+")");
			}
			return true;
		}
		else
		{
			if(CMInfo._CM_DEBUG_2)
			{
				System.out.println("CMInteractionManager.isChannelBelongsToServer(): NO, "
						+ch+", server("+server.getServerName()+")");
			}
			return false;					
		}
		
	}
	
	// update the last event transmission time when CM sends an event
	public synchronized static void updateMyLastEventTransTime(SelectableChannel ch, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMServer defServer = interInfo.getDefaultServerInfo();
		CMServer addServer = null;
		
		long lCurTime = System.currentTimeMillis();
		
		if(confInfo.getSystemType().contentEquals("SERVER"))
		{
			// find client with ch
			CMUser user = findUserWithSocketChannel(ch, interInfo.getLoginUsers());
			if(user != null)
			{
				myself.getMyLastEventTransTimeHashtable().put(user.getName(), lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateMyLastEventTransTime(),"
							+"user("+user.getName()+"), time: "+lCurTime);
				}
				return;
			}
			
			// find additional server with ch
			addServer = findAddServerWithSocketChannel(ch, interInfo.getAddServerList());
			if(addServer != null)
			{
				myself.getMyLastEventTransTimeHashtable().put(addServer.getServerName(), lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateMyLastEventTransTime(),"
							+"server("+addServer.getServerName()+"), time: "+lCurTime);
				}
				return;
			}
			
			// if this server is not the default server, it checks whether ch belongs to 
			// the default server
			if(!CMConfigurator.isDServer(cmInfo))
			{
				if(isChannelBelongsToServer(ch, defServer))
				{
					myself.getMyLastEventTransTimeHashtable().put(defServer
							.getServerName(), lCurTime);
					if(CMInfo._CM_DEBUG_2)
					{
						System.out.println("CMInteractionManager.updateMyLastEventTransTime(), "
								+"server("+defServer.getServerName()+"), time: "+lCurTime);
					}
					return;
				}
			}
			
			// do not need to find unknown channel with ch here
		}
		else	// client
		{
			// check whether ch belongs to the default server
			if(isChannelBelongsToServer(ch, defServer))
			{
				myself.getMyLastEventTransTimeHashtable().put(defServer.getServerName(), 
						lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateMyLastEventTransTime(), "
							+"server("+defServer.getServerName()+"), time: "+lCurTime);
				}
				return;
			}
			
			// find additional server with ch
			addServer = findAddServerWithSocketChannel(ch, interInfo.getAddServerList());
			if(addServer != null)
			{
				myself.getMyLastEventTransTimeHashtable().put(addServer.getServerName(), 
						lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateMyLastEventTransTime(), "
							+"server("+addServer.getServerName()+"), time: "+lCurTime);
				}
				return;
			}
		}
		
		System.err.println("CMInteractionManager.updateMyLastEventTransTime(), receiver "
				+"not found with ch !: "+ch);
	}
	
	/////////////////////////////////////////////////////////////////////
	// private methods
	
	// update the last event transmission time when CM receives an event
	private static void updateLastEventTransTime(SelectableChannel ch, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		CMServer defServer = interInfo.getDefaultServerInfo();
		CMServer addServer = null;
		
		long lCurTime = System.currentTimeMillis();
		
		if(confInfo.getSystemType().contentEquals("SERVER"))
		{
			// find client with ch
			CMUser user = findUserWithSocketChannel(ch, interInfo.getLoginUsers());
			if(user != null)
			{
				user.setLastEventTransTime(lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateLastEventTransTime(), "
							+"user("+user.getName()+"), time: "+lCurTime);
				}
				return;
			}
			
			// find additional server with ch
			addServer = findAddServerWithSocketChannel(ch, interInfo.getAddServerList());
			if(addServer != null)
			{
				addServer.setLastEventTransTime(lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateLastEventTransTime(), "
							+"server("+addServer.getServerName()+"), time: "+lCurTime);
				}
				return;
			}
			
			// find unknown channel with ch
			CMList<CMUnknownChannelInfo> unchInfoList = commInfo.getUnknownChannelInfoList();
			CMUnknownChannelInfo unchInfo = unchInfoList.findElement(new CMUnknownChannelInfo((SocketChannel)ch));
			if(unchInfo != null)
			{
				unchInfo.setLastEventTransTime(lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateLastEventTransTime(), "
							+"unknown channel, time: "+lCurTime);
				}
				return;
			}
			
			// if this server is not the default server, it also check whether ch is for
			// the default server
			if(!CMConfigurator.isDServer(cmInfo))
			{
				if(isChannelBelongsToServer(ch, defServer))
				{
					defServer.setLastEventTransTime(lCurTime);
					if(CMInfo._CM_DEBUG_2)
					{
						System.out.println("CMInteractionManager.updateLastEventTransTime(), "
								+"server("+defServer.getServerName()+"), time: "+lCurTime);
					}
					return;
				}
			}
		}
		else	// client
		{
			// check whether ch belongs to the default server
			if(isChannelBelongsToServer(ch, defServer))
			{
				defServer.setLastEventTransTime(lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateLastEventTransTime(), "
							+"default server, time: "+lCurTime);
				}
				return;
			}
			// find additional server with ch
			addServer = findAddServerWithSocketChannel(ch, interInfo.getAddServerList());
			if(addServer != null)
			{
				addServer.setLastEventTransTime(lCurTime);
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("CMInteractionManager.updateLastEventTransTime(), "
							+"server("+addServer.getServerName()+"), time: "+lCurTime);
				}
				return;
			}		
		}
		
		System.err.println("CMInteractionManager.updateLastEventTransTime(), sender not "
				+"found with ch !: "+ch);
		
	}
	
	private static void createSession(CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		if(confInfo.getSystemType().equals("SERVER"))
		{
			int nSessionNum = confInfo.getSessionNumber();
			if(nSessionNum <= 0)
			{
				System.out.println("CMInteractionManager.createSession(), incorrect number of sessions: "
						+nSessionNum);
				return;
			}
			
			String strConfPath = confInfo.getConfFileHome().resolve("cm-server.conf").toString();
			
			for(int i=1; i <= nSessionNum; i++)
			{
				CMSession session = new CMSession();
				String strSessionName = null;
				String strSessionConfFileName = null;
				strSessionName = CMConfigurator.getConfiguration(strConfPath, "SESSION_NAME"+i);
				strSessionConfFileName = CMConfigurator.getConfiguration(strConfPath, "SESSION_FILE"+i);

					session.setSessionName(strSessionName);
				session.setAddress(confInfo.getMyAddress());
				session.setPort(confInfo.getMyPort());
				session.setSessionConfFileName(strSessionConfFileName);
				interInfo.addSession(session);
			}
		}
		else if(confInfo.getSystemType().equals("CLIENT"))
		{
			// default session (without name) is added
			//CMSession session = new CMSession();
			//interInfo.addSession(session);
			
			// a session will be created when this client receives JOIN_SESSION_ACK event
		}
	}

	private static boolean processSessionEvent(CMMessage msg, CMInfo cmInfo)
	{
		boolean bForward = true;
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		int nEventID = se.getID();
		switch(nEventID)
		{
		case CMSessionEvent.LOGIN:
			bForward = processLOGIN(msg, cmInfo);
			break;
		case CMSessionEvent.LOGIN_ACK:
			processLOGIN_ACK(msg, cmInfo);
			break;
		case CMSessionEvent.LOGOUT:
			processLOGOUT(msg, cmInfo);
			break;
		case CMSessionEvent.SESSION_ADD_USER:
			processSESSION_ADD_USER(msg, cmInfo);
			break;
		case CMSessionEvent.SESSION_REMOVE_USER:
			processSESSION_REMOVE_USER(msg, cmInfo);
			break;
		case CMSessionEvent.CHANGE_SESSION:
			processCHANGE_SESSION(msg, cmInfo);
			break;
		case CMSessionEvent.REQUEST_SESSION_INFO:
			processREQUEST_SESSION_INFO(msg, cmInfo);
			break;
		case CMSessionEvent.RESPONSE_SESSION_INFO:
			processRESPONSE_SESSION_INFO(msg, cmInfo);
			break;
		case CMSessionEvent.SESSION_TALK:
			processSESSION_TALK(msg, cmInfo);
			break;
		case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL:
			processADD_NONBLOCK_SOCKET_CHANNEL(msg, cmInfo);
			break;
		case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
			processADD_NONBLOCK_SOCKET_CHANNEL_ACK(msg, cmInfo);
			break;
		case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL:
			processADD_BLOCK_SOCKET_CHANNEL(msg, cmInfo);
			break;
		case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK:
			processADD_BLOCK_SOCKET_CHANNEL_ACK(msg, cmInfo);
			break;
		case CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL:
			processREMOVE_BLOCK_SOCKET_CHANNEL(msg, cmInfo);
			break;
		case CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
			processREMOVE_BLOCK_SOCKET_CHANNEL_ACK(msg, cmInfo);
			break;
		case CMSessionEvent.REGISTER_USER:
			processREGISTER_USER(msg, cmInfo);
			break;
		case CMSessionEvent.REGISTER_USER_ACK:
			processREGISTER_USER_ACK(se, cmInfo);
			break;
		case CMSessionEvent.DEREGISTER_USER:
			processDEREGISTER_USER(msg, cmInfo);
			break;
		case CMSessionEvent.DEREGISTER_USER_ACK:
			processDEREGISTER_USER_ACK(se, cmInfo);
			break;
		case CMSessionEvent.FIND_REGISTERED_USER:
			processFIND_REGISTERED_USER(msg, cmInfo);
			break;
		case CMSessionEvent.FIND_REGISTERED_USER_ACK:
			processFIND_REGISTERED_USER_ACK(se, cmInfo);
			break;
		default:
			System.out.println("CMInteractionManager.processSessionEvent(), unknown event ID: "
					+nEventID);
			se = null;
			return false;
		}
		
		se = null;
		return bForward;
	}

	private static boolean processLOGIN(CMMessage msg, CMInfo cmInfo)
	{
		boolean bForward = true;
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMMember loginUsers = null;
		
		if(confInfo.getSystemType().equals("SERVER"))
		{
			// check if the user already has logged in or not
			CMSessionEvent se = new CMSessionEvent(msg.m_buf);
			loginUsers = interInfo.getLoginUsers();
			if(loginUsers.isMember(se.getUserName()))
			{
				// send LOGIN_ACK event saying that the user already has logged into the server
				bForward = false;
				if(CMInfo._CM_DEBUG)
				{
					System.err.println("CMInteractionManager.processLOGIN(), user("+
							se.getUserName()+") already has logged into the server!");
				}
				
				CMSessionEvent seAck = new CMSessionEvent();
				seAck.setID(CMSessionEvent.LOGIN_ACK);
				seAck.setValidUser(-1);	// already-logged user
				CMEventManager.unicastEvent(seAck, (SocketChannel)msg.m_ch, cmInfo);
			}
			else
			{
				CMUser tuser = new CMUser();

				tuser.setName(se.getUserName());
				tuser.setPasswd(se.getPassword());
				tuser.setHost(se.getHostAddress());
				tuser.setUDPPort(se.getUDPPort());
				tuser.setLastEventTransTime(System.currentTimeMillis());
				tuser.setKeepAliveTime(confInfo.getKeepAliveTime());
				
				tuser.getNonBlockSocketChannelInfo().addChannel(0, msg.m_ch);
				loginUsers.addMember(tuser);
				
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMInteractionManager.processLOGIN(), add new user("+
							se.getUserName()+"), # longin users("+loginUsers.getMemberNum()+").");
				}

				if(!confInfo.isLoginScheme())
					replyToLOGIN(se, 1, cmInfo);				
			}

			se = null;
			return bForward;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.err.println("CMInteractionManager.processLOGIN(); system type is not SERVER!");
		}
		return false;
	}

	public static boolean replyToLOGIN(CMSessionEvent se, int nValidUser, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		CMSessionEvent seAck = new CMSessionEvent();
		CMUser user = interInfo.getLoginUsers().findMember(se.getUserName());
		boolean bRet = false;
		
		if(user == null)
		{
			System.out.println("CMInteractionManager.replyToLOGIN(), user("+se.getUserName()
					+") not found.");
			return false;
		}

		seAck.setID(CMSessionEvent.LOGIN_ACK);
		seAck.setSender(interInfo.getMyself().getName());
		seAck.setReceiver(se.getSender());
		seAck.setValidUser(nValidUser);
		seAck.setCommArch(confInfo.getCommArch());
		
		if(confInfo.isFileTransferScheme())
			seAck.setFileTransferScheme(1);
		else
			seAck.setFileTransferScheme(0);
		
		if(confInfo.isLoginScheme())
			seAck.setLoginScheme(1);
		else
			seAck.setLoginScheme(0);
		
		if(confInfo.isSessionScheme())
			seAck.setSessionScheme(1);
		else
			seAck.setSessionScheme(0);
		
		seAck.setAttachDownloadScheme(confInfo.getAttachDownloadScheme());	// default value
		
		seAck.setUDPPort(confInfo.getUDPPort());
		
		bRet = CMEventManager.unicastEvent(seAck, user.getName(), cmInfo);
		seAck = null;
		
		if(nValidUser == 1)
		{
			// set default scheme for attachment download
			user.setAttachDownloadScheme(confInfo.getAttachDownloadScheme());
			// set login date
			user.setLastLoginDate(Calendar.getInstance());
			if(confInfo.getAttachDownloadScheme() == CMInfo.SNS_ATTACH_PREFETCH && confInfo.isDBUse())
			{
				// load history info for attachment access of this user
				CMSNSManager.loadAccessHistory(user, cmInfo);
			}
			
			// set last event transmission time
			user.setLastEventTransTime(System.currentTimeMillis());
			// set keep-alive time
			int nKeepAliveTime = se.getKeepAliveTime();
			if(nKeepAliveTime > 0)
				user.setKeepAliveTime(nKeepAliveTime);
			else {
				user.setKeepAliveTime(confInfo.getKeepAliveTime());
			}
			
			// remove from unknown-channel list
			SocketChannel sc = (SocketChannel) user.getNonBlockSocketChannelInfo().findChannel(0);
			CMList<CMUnknownChannelInfo> unknownChInfoList = commInfo.getUnknownChannelInfoList();
			bRet = unknownChInfoList.removeElement(new CMUnknownChannelInfo(sc));
			if(bRet && CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.replyToLogin(), remove "+sc
						+" from the unknown-channel list.");
				System.out.println("# unknown-channel list elemetns: "+unknownChInfoList.getSize());
			}
			if(!bRet)
			{
				System.err.println("CMInteractionManager.replyToLogin(), error to remove "+sc
						+" from the unknown-channel list!");
				System.err.println("# unknown-channel list elements: "+unknownChInfoList.getSize());
			}

			// send inhabitants who already logged on the system to the new user
			distributeLoginUsers(user.getName(), cmInfo);
			
			// notify info. on new user who logged in
			CMSessionEvent tse = new CMSessionEvent();
			tse.setID(CMSessionEvent.SESSION_ADD_USER);
			tse.setUserName(user.getName());
			tse.setHostAddress(user.getHost());
			tse.setSessionName(user.getCurrentSession());
			CMEventManager.broadcastEvent(tse, cmInfo);
		}
		else
		{
			// remove temporary user information
			user.getNonBlockSocketChannelInfo().removeAllAddedChannels(0);
			user.getBlockSocketChannelInfo().removeAllChannels();
			interInfo.getLoginUsers().removeMember(user.getName());
			
			// update login failure count of this channel
			SocketChannel sc = (SocketChannel) user.getNonBlockSocketChannelInfo().findChannel(0);
			CMUnknownChannelInfo unchInfo = commInfo.getUnknownChannelInfoList()
					.findElement(new CMUnknownChannelInfo(sc));
			if(unchInfo == null)
			{
				System.err.println("CMInteractionManager.replyToLOGIN(), unknown channel "
						+"not found for "+sc+" !");
				return false;
			}
			int nLoginCount = unchInfo.getNumLoginFailure();
			unchInfo.setNumLoginFailure(++nLoginCount);
			// check if the count of login failure exceeds a threshold
			if(nLoginCount > confInfo.getMaxLoginFailure())
			{
				System.err.println("CMInteractionManager.replyToLOGIN(), the unknown "
						+"channel fails to login "+nLoginCount+" times: "+sc);
				try {
					sc.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bRet = commInfo.getUnknownChannelInfoList().removeElement(unchInfo);
				if(bRet && CMInfo._CM_DEBUG)
				{
					System.out.println("CMInteractionManager.replyToLOGIN(), removed "
							+"from unknown-channel list: "+sc);
				}
				if(!bRet)
				{
					System.out.println("CMInteractionManager.replyToLOGIN(), error to "
							+"remove from unknown-channel list: "+sc);
				}
				return bRet;
			}
		}

		return bRet;
	}
	
	private static void distributeLoginUsers(String targetUser, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMSessionEvent tse = null;
		CMUser loginUser = null;
		
		Iterator<CMUser> iterUser = interInfo.getLoginUsers().getAllMembers().iterator();
		while(iterUser.hasNext())
		{
			loginUser = iterUser.next();
			if(!targetUser.equals(loginUser.getName()))
			{
				tse = new CMSessionEvent();
				tse.setID(CMSessionEvent.SESSION_ADD_USER);
				tse.setUserName(loginUser.getName());
				tse.setHostAddress(loginUser.getHost());
				tse.setSessionName(loginUser.getCurrentSession());
				CMEventManager.unicastEvent(tse, targetUser, cmInfo);
				tse = null;
			}
		}
		
	}
	
	private static void processLOGIN_ACK(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		SocketChannel sc = null;
		CMServer serverInfo = null;
		
		if(!confInfo.getSystemType().equals("CLIENT"))
			return;
		
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		confInfo.setCommArch(se.getCommArch());
		confInfo.setFileTransferScheme(se.isFileTransferScheme());
		confInfo.setLoginScheme(se.isLoginScheme());
		confInfo.setSessionScheme(se.isSessionScheme());
		confInfo.setAttachDownloadScheme(se.getAttachDownloadScheme());
		interInfo.getDefaultServerInfo().setServerName(se.getSender());
		interInfo.getDefaultServerInfo().setServerUDPPort(se.getUDPPort());
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processLOGIN_ACK(), received.");
			System.out.println("bValidUser("+se.isValidUser()+"), comm arch("+se.getCommArch()
					+"), bFileTransferScheme("+se.isFileTransferScheme()+"), bLoginScheme("
					+se.isLoginScheme()+"), bSessionScheme("+se.isSessionScheme()
					+"), nAttachDwonloadScheme("+se.getAttachDownloadScheme()+"), server udp port("
					+se.getUDPPort()+").");
		}
		
		if(se.isValidUser() == 1)
		{
			// update client's state
			interInfo.getMyself().setState(CMInfo.CM_LOGIN);
			// set client's attachment download scheme
			interInfo.getMyself().setAttachDownloadScheme(se.getAttachDownloadScheme());
			// set client's keep-alive time
			interInfo.getMyself().setKeepAliveTime(confInfo.getKeepAliveTime());
			// if the file trasnfer scheme is set, create a blocking TCP socket channel
			serverInfo = interInfo.getDefaultServerInfo();
			if(confInfo.isFileTransferScheme())
			{
				/*
				scInfo = serverInfo.getBlockSocketChannelInfo();
				try {
					sc = (SocketChannel) CMCommManager.openBlockChannel(CMInfo.CM_SOCKET_CHANNEL, 
							serverInfo.getServerAddress(), serverInfo.getServerPort(), cmInfo);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				if(sc == null)
				{
					System.err.println("CMInteractionMaanger.processLOGIN_ACK(), failed to create a blocking "
							+ "TCP socket channel to the default server!");
					return;
				}
				scInfo.addChannel(0, sc); // key for the default blocking TCP socket channel is 1

				CMSessionEvent tse = new CMSessionEvent();
				tse.setID(CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL);
				tse.setChannelName(interInfo.getMyself().getName());
				tse.setChannelNum(0);
				CMEventManager.unicastEvent(tse, serverInfo.getServerName(), CMInfo.CM_STREAM, 0, true, cmInfo);
				se = null;
				*/
				
				sc = CMCommManager.addBlockSocketChannel(0, serverInfo.getServerName(), cmInfo);

				if(sc != null && CMInfo._CM_DEBUG)
				{
					System.out.println("CMInteractionManager.processLOGIN_ACK(),successfully requested to add "
							+ "the channel with the key(0) to the default server.");
				}				
			}
			
			// check whether the keep-alive scheduler should start or not
			int nKeepAlive = interInfo.getMyself().getKeepAliveTime();
			if(nKeepAlive > 0 && getNumLoginServers(cmInfo) == 1)
			{
				CMThreadInfo threadInfo = cmInfo.getThreadInfo();
				ScheduledExecutorService ses = threadInfo.getScheduledExecutorService();
				CMClientKeepAliveTask keepAliveTask = new CMClientKeepAliveTask(cmInfo);
				ScheduledFuture<?> future = ses.scheduleAtFixedRate(keepAliveTask, 
						nKeepAlive/3, nKeepAlive/3, TimeUnit.SECONDS);
				threadInfo.setScheduledFuture(future);
				
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMInteractionManager.processLOGIN_ACK(), "
							+"# logins("+getNumLoginServers(cmInfo)+"), start "
							+"keep-alive task.");
				}
			}

			// request session information if session scheme is not used.
			if(!confInfo.isSessionScheme())
			{
				// set session name as default name (session1)
				//CMSession tsession = interInfo.getSessionList().elementAt(0);
				//tsession.setSessionName("session1");
				
				CMSessionEvent tse = new CMSessionEvent();
				tse.setID(CMSessionEvent.JOIN_SESSION);
				tse.setHandlerSession("session1");
				tse.setUserName(interInfo.getMyself().getName());
				tse.setSessionName("session1");
				
				CMEventManager.unicastEvent(tse, serverInfo.getServerName(), cmInfo);
				interInfo.getMyself().setCurrentSession("session1");
				tse = null;
			}
		}
		else
		{
			System.out.println("CMInteractionManager.processLOGIN_ACK(), invalid user.");
		}
		
		se = null;
		return;
	}
	
	private static void processSESSION_ADD_USER(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processSESSION_ADD_USER(), user("+se.getUserName()
						+"), host("+se.getHostAddress()+"), session("+se.getSessionName()+").");
			}
		}
		se = null;
		return;
	}
	
	private static void processSESSION_REMOVE_USER(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processSESSION_REMOVE_USER(), user("
						+se.getUserName()+").");
			}
		}
		se = null;
		return;
	}
	
	private static void processCHANGE_SESSION(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processCHANGE_SESSION(), user("+se.getUserName()
					+"), session("+se.getSessionName()+").");
		}

		if(confInfo.getSystemType().equals("SERVER"))	// Currently, this event is sent only to users (not servers) (not clear)
		{
			CMEventManager.broadcastEvent(se, cmInfo);
		}
		
		se = null;
		return;
	}
	
	private static void processLOGOUT(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		boolean bRet = false;
		
		if(!confInfo.getSystemType().equals("SERVER"))
			return;
		
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		// find the user in the login user list
		CMUser user = interInfo.getLoginUsers().findMember(se.getUserName());
		if(user == null)
		{
			System.out.println("CMInteractionManager.processLOGOUT, user("+se.getUserName()
					+") not found.");
			return;
		}
		
		// stop all the file-transfer threads
		//List<Runnable> ftList = fInfo.getExecutorService().shutdownNow();	// wrong
		//if(CMInfo._CM_DEBUG)
		//	System.out.println("CMInteractionManager.processLOGOUT(); # shutdown threads: "+ftList.size());
		// remove all the ongoing file-transfer info with the user
		fInfo.removeRecvFileList(user.getName());
		fInfo.removeSendFileList(user.getName());
		// remove all the ongoing sns related file-transfer info about the user
		snsInfo.getPrefetchMap().removePrefetchList(user.getName());
		snsInfo.getRecvSNSAttachHashtable().removeSNSAttachList(user.getName());
		snsInfo.getSendSNSAttachHashtable().removeSNSAttachList(user.getName());
		
		if(confInfo.getAttachDownloadScheme() == CMInfo.SNS_ATTACH_PREFETCH && confInfo.isDBUse())
		{
			// save newly added or updated access history for the attachment of SNS content
			CMSNSManager.saveAccessHistory(user, cmInfo);
		}
		
		// leave session and group
		CMSessionManager.leaveSession(user, cmInfo);
		
		// close and remove all additional nonblocking socket channels of the user
		user.getNonBlockSocketChannelInfo().removeAllAddedChannels(0);
		// close and remove all blocking socket channels of the user
		user.getBlockSocketChannelInfo().removeAllChannels();
		// remove the user from login user list
		//interInfo.getLoginUsers().removeMember(user);
		//user = null;
		interInfo.getLoginUsers().removeMemberObject(user);
		
		// notify login users of the logout user
		CMSessionEvent tse = new CMSessionEvent();
		tse.setID(CMSessionEvent.SESSION_REMOVE_USER);
		tse.setUserName(se.getUserName());
		CMEventManager.broadcastEvent(tse, cmInfo);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processLOGOUT(), user("+se.getUserName()
					+"), # login users("+interInfo.getLoginUsers().getMemberNum()+").");
		}
		
		// move the default channel to the unknown-channel list
		CMList<CMUnknownChannelInfo> unchInfoList = commInfo.getUnknownChannelInfoList();
		SocketChannel sc = (SocketChannel)user.getNonBlockSocketChannelInfo().findChannel(0);
		if(sc.isOpen())
		{
			bRet = unchInfoList.addElement(new CMUnknownChannelInfo(sc));
			if(bRet && CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processLOGOUT(), add channel to "
						+"unknown-channel list: "+sc);
				System.out.println("# unknown-channel list members: "+unchInfoList.getSize());
			}
			if(!bRet)
			{
				System.err.println("CMInteractionManager.processLOGOUT(), error to add channel "
						+"to unknown-channel list: "+sc);
				System.err.println("# unknown-channel list members: "+unchInfoList.getSize());
			}			
		}
		else if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processLOGOUT(), the client channel is also closed.");
		}
		
		se = null;
		tse = null;
		return;
	}
	
	private static void processREQUEST_SESSION_INFO(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		if(!confInfo.getSystemType().equals("SERVER"))
		{
			return;
		}
		
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		if(!interInfo.getLoginUsers().isMember(se.getUserName()))
		{
			System.out.println("CMInteractinManager.processREQUEST_SESSION_INFO(), user("+se.getUserName()
					+") not found in the login user list.");
			se = null;
			return;
		}
		
		CMSessionEvent seAck = new CMSessionEvent();
		seAck.setID(CMSessionEvent.RESPONSE_SESSION_INFO);
		seAck.setSender(interInfo.getMyself().getName());
		seAck.setReceiver(se.getSender());
		seAck.setSessionNum(confInfo.getSessionNumber());
		Iterator<CMSession> iterSession = interInfo.getSessionList().iterator();
		while(iterSession.hasNext())
		{
			CMSession tsession = iterSession.next();
			CMSessionInfo tInfo = new CMSessionInfo(tsession.getSessionName(), confInfo.getServerAddress(),
									confInfo.getServerPort(), tsession.getSessionUsers().getMemberNum());
			seAck.addSessionInfo(tInfo);
		}
		CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);

		seAck.removeAllGroupInfoObjects();
		seAck = null;
		se = null;
		return;
	}
	
	private static void processRESPONSE_SESSION_INFO(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();

		if(!confInfo.getSystemType().equals("CLIENT"))
		{
			return;
		}
		
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		Iterator<CMSessionInfo> iter = se.getSessionInfoList().iterator();

		if(CMInfo._CM_DEBUG)
		{
			System.out.format("%-60s%n", "------------------------------------------------------------");
			System.out.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num");
			System.out.format("%-60s%n", "------------------------------------------------------------");
		}

		while(iter.hasNext())
		{
			CMSessionInfo tInfo = iter.next();
			CMSession tSession = interInfo.findSession(tInfo.getSessionName());
			if(tSession == null)
			{
				tSession = new CMSession(tInfo.getSessionName(), tInfo.getAddress(), tInfo.getPort(),
						tInfo.getUserNum());
				interInfo.addSession(tSession);
			}
			else
			{
				tSession.setAddress(tInfo.getAddress());
				tSession.setPort(tInfo.getPort());
				tSession.setUserNum(tInfo.getUserNum());
			}
			
			if(CMInfo._CM_DEBUG)
			{
				System.out.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(), tInfo.getPort(), tInfo.getUserNum());
			}
		}
		
		se.removeAllSessionInfoObjects();
		se = null;
		return;
	}
	
	private static void processSESSION_TALK(CMMessage msg, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			CMSessionEvent se = new CMSessionEvent(msg.m_buf);
			System.out.println("CMInteractionManager.processSESSION_TALK(), broadcasted by user("
					+se.getUserName()+")");
			System.out.println("chat: "+se.getTalk());
			se = null;
		}
		
		return;
	}
	
	private static void processADD_NONBLOCK_SOCKET_CHANNEL(CMMessage msg, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		String strChannelName = se.getChannelName();
		int nChIndex = se.getChannelNum();
		
		CMSessionEvent seAck = new CMSessionEvent();
		seAck.setID(CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL_ACK);
		seAck.setSender(interInfo.getMyself().getName());
		seAck.setReceiver(se.getSender());
		seAck.setChannelName(interInfo.getMyself().getName());
		seAck.setChannelNum(nChIndex);

		CMUser user = interInfo.getLoginUsers().findMember(strChannelName);
		if(user == null)
		{
			System.out.println("CMInteractionManager.processADD_NONBLOCK_SOCKET_CHANNEL(), user("+strChannelName
					+") not found in the login user list.");
			seAck.setReturnCode(0);
			CMEventManager.unicastEvent(seAck, (SocketChannel) msg.m_ch, cmInfo);
			seAck = null;
			se = null;
			return;
		}
		boolean ret = user.getNonBlockSocketChannelInfo().addChannel(nChIndex, msg.m_ch);
		if(ret)
			seAck.setReturnCode(1);
		else
			seAck.setReturnCode(0);
		
		// remove channel from the unknown-channel list
		CMList<CMUnknownChannelInfo> unknownChInfoList = commInfo.getUnknownChannelInfoList();
		ret = unknownChInfoList.removeElement(new CMUnknownChannelInfo((SocketChannel)msg.m_ch));
		if(ret && CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_NONBLOCK_SOCKET_CHANNEL() remove "+msg.m_ch
					+" from the unknown-channel list.");
			System.out.println("# unknown-channel list elements: "+unknownChInfoList.getSize());
		}
		if(!ret)
		{
			System.err.println("CMInteractionManager.processADD_NONBLOCK_SOCKET_CHANNEL() error to remove "
					+msg.m_ch+" from the unknown-channel list!");
			System.err.println("# unknown-channel list elements: "+unknownChInfoList.getSize());
		}
		
		CMEventManager.unicastEvent(seAck, user.getName(), cmInfo);
		
		se = null;
		seAck = null;
		return;
	}
	
	private static void processADD_NONBLOCK_SOCKET_CHANNEL_ACK(CMMessage msg, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMServer serverInfo = null;
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		
		if(se.getReturnCode() == 0)
		{
			String strServer = se.getChannelName();
			int nChIndex = se.getChannelNum();
			System.out.println("CMInteractionManager.processADD_NONBLOCK_SOCKET_CHANNEL_ACK() failed to add channel,"
					+"server("+strServer+"), channel key("+nChIndex+").");
			
			if(strServer.equals(interInfo.getDefaultServerInfo().getServerName()))
			{
				serverInfo = interInfo.getDefaultServerInfo();
			}
			else
			{
				serverInfo = interInfo.findAddServer(strServer);
			}
			serverInfo.getNonBlockSocketChannelInfo().removeChannel(nChIndex);
		}
		else
		{
			System.out.println("CMInteractionManager.processADD_NONBLOCK_SOCKET_CHANNEL_ACK(), succeeded for server("
					+se.getChannelName()+") channel key("+se.getChannelNum()+").");
		}
				
		se = null;
		return;
	}
	
	private static void processADD_BLOCK_SOCKET_CHANNEL(CMMessage msg, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		String strChannelName = se.getChannelName();
		int nChKey = se.getChannelNum();
		String strMyName = interInfo.getMyself().getName();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMUser user = null;
		
		// If this node is not the receiver of this event,
		if(!se.getReceiver().contentEquals(strMyName))
		{
			System.err.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL(), "
					+"receiver("+se.getReceiver()+") is not me("+strMyName+").");
			return;
		}

		if(confInfo.getSystemType().contentEquals("SERVER"))
		{
			user = interInfo.getLoginUsers().findMember(strChannelName);
			if(user == null)
			{
				System.err.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL(), "
						+"user("+strChannelName+") not found in the login user list!");
				
				return;
			}			
		}
		else
		{
			/*
			String strCurrentSession = interInfo.getMyself().getCurrentSession();
			String strCurrentGroup = interInfo.getMyself().getCurrentGroup();
			
			CMSession session = interInfo.findSession(strCurrentSession);
			if(session == null)
			{
				System.err.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL(),"
						+"session("+strCurrentSession+") not found!");
				return;
			}
			CMGroup group = session.findGroup(strCurrentGroup);
			if(group == null)
			{
				System.err.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL(),"
						+"group("+strCurrentGroup+") not found!");
				return;
			}
			user = group.getGroupUsers().findMember(strChannelName);
			*/
			user = findGroupMemberOfClient(strChannelName, cmInfo);
			if(user == null)
			{
				System.err.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL(),"
						+"user("+strChannelName+") not found!");
				return;
			}
		}
		
		CMSessionEvent seAck = new CMSessionEvent();
		seAck.setID(CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK);
		seAck.setSender(strMyName);
		seAck.setReceiver(se.getSender());
		seAck.setChannelName(interInfo.getMyself().getName());
		seAck.setChannelNum(nChKey);

		// The receiving channel is included in the Selector with the nonblocking mode.
		// This channel must be taken out from the Selector and changed to the blocking mode.
		synchronized(Selector.class){
			SelectionKey selKey = msg.m_ch.keyFor(commInfo.getSelector());
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL(); # registered ky in "
						+ "the selector before the cancel request of the key: "
						+ commInfo.getSelector().keys().size());
			}
			selKey.cancel();
			while(msg.m_ch.isRegistered())
			{
				try {
					Selector.class.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL(); # registered key in "
						+ "the selector after the completion of the key cancellation: "
						+ commInfo.getSelector().keys().size());
			}
		}
		
		try {
			msg.m_ch.configureBlocking(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			seAck.setReturnCode(0);
			
			if(confInfo.getCommArch().contentEquals("CM_CS") && 
					confInfo.getSystemType().contentEquals("CLIENT"))
			{
				// If this node is the client type, the requester is another client 
				// and this reply event should be forwarded by the default server.
				// The server never sends the ADD_BLOCK_SOCKET_CHANNEL event to the client.
				seAck.setDistributionSession("CM_ONE_USER");
				seAck.setDistributionGroup(user.getName());
				CMEventManager.unicastEvent(seAck, interInfo.getDefaultServerInfo()
						.getServerName(), cmInfo);
			}
			else
			{
				CMEventManager.unicastEvent(seAck, user.getName(), cmInfo);				
			}
			seAck = null;
			se = null;
			return;
		}
		
		boolean ret = user.getBlockSocketChannelInfo().addChannel(nChKey, msg.m_ch);
		if(ret)
			seAck.setReturnCode(1);
		else
			seAck.setReturnCode(0);

		// remove channel from the unknown-channel list
		CMList<CMUnknownChannelInfo> unknownChInfoList = commInfo.getUnknownChannelInfoList();
		ret = unknownChInfoList.removeElement(new CMUnknownChannelInfo((SocketChannel)msg.m_ch));
		if(ret && CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL() remove "+msg.m_ch
					+" from the unknown-channel list.");
			System.out.println("# unknown-channel list elements: "+unknownChInfoList.getSize());
		}
		if(!ret)
		{
			System.err.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL() error to remove "
					+msg.m_ch+" from the unknown-channel list!");
			System.err.println("# unknown-channel list elements: "+unknownChInfoList.getSize());
		}

		if(confInfo.getCommArch().contentEquals("CM_CS") && 
				confInfo.getSystemType().contentEquals("CLIENT"))
		{
			// If this node is the client type, the requester is another client 
			// and this reply event should be forwarded by the default server.
			// The server never sends the ADD_BLOCK_SOCKET_CHANNEL event to the client.
			seAck.setDistributionSession("CM_ONE_USER");
			seAck.setDistributionGroup(user.getName());
			ret = CMEventManager.unicastEvent(seAck, interInfo.getDefaultServerInfo()
					.getServerName(), cmInfo);
		}
		else
		{
			ret = CMEventManager.unicastEvent(seAck, user.getName(), cmInfo);			
		}
		
		se = null;
		seAck = null;
		return;
	}
	
	private static void processADD_BLOCK_SOCKET_CHANNEL_ACK(CMMessage msg, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		String strMyName = interInfo.getMyself().getName();
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMServer targetServer = null;
		CMUser targetUser = null;
		CMChannelInfo<Integer> scList = null;
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		String strChannel = se.getChannelName();
		int nChKey = se.getChannelNum();
		CMSendFileInfo sfInfo = null;
		
		// If this node is not the receiver of this event,
		if(!se.getReceiver().contentEquals(strMyName))
		{
			System.err.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL_ACK(), "
					+"receiver("+se.getReceiver()+") is not me("+strMyName+").");
			return;
		}

		// find the target node (server or client)
		targetServer = findServer(strChannel, cmInfo);
		if(targetServer != null)
		{
			scList = targetServer.getBlockSocketChannelInfo();
		}
		else
		{
			targetUser = findGroupMemberOfClient(strChannel, cmInfo);
			if(targetUser == null)
			{
				System.err.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL_ACK(), "
						+"target("+strChannel+") not found!");
				return;
			}
			scList = targetUser.getBlockSocketChannelInfo();
		}
		
		if(se.getReturnCode() == 0)
		{
			System.out.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL_ACK() failed to add channel,"
					+"server("+strChannel+"), channel key("+nChKey+").");
			
			scList.removeChannel(nChKey);
			return;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_BLOCK_SOCKET_CHANNEL_ACK(), succeeded for server("
					+se.getChannelName()+") channel key("+se.getChannelNum()+").");
			System.out.println("delay: "+(System.currentTimeMillis()-commInfo.getStartTime()));
		}

		// check whether there is sending file information that is not started to be sent
		sfInfo = fInfo.findSendFileInfoNotStarted(strChannel);
		if(sfInfo != null && (fInfo.findSendFileInfoOngoing(strChannel) == null))
		{
			// set the dedicated channel to the sending file info
			sfInfo.setSendChannel((SocketChannel)scList.findChannel(nChKey));
			// resume the file-transfer by calling sendSTART_FILE_TRANSFER_CHAN() method
			CMFileTransferManager.sendSTART_FILE_TRANSFER_CHAN(sfInfo, cmInfo);
		}
		se = null;
		return;
	}
	
	private static void processREMOVE_BLOCK_SOCKET_CHANNEL(CMMessage msg, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser user = null;
		CMChannelInfo<Integer> scInfo = null;
		SocketChannel sc = null;
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		String strChannelName = se.getChannelName();
		int nChKey = se.getChannelNum();
		ByteBuffer recvBuf = null;
		int nRecvBytes = -1;
		String strMyName = interInfo.getMyself().getName();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		
		// If this node is not the receiver of the received event,
		if(!se.getReceiver().contentEquals(strMyName))
		{
			System.err.println("CMInteractionManager.processREMOVE_BLOCK_CHANNEL(), "
					+"receiver("+se.getReceiver()+") is not me("+strMyName+").");
			return;
		}
		
		CMSessionEvent seAck = new CMSessionEvent();
		seAck.setID(CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL_ACK);
		seAck.setSender(strMyName);
		seAck.setReceiver(se.getSender());
		seAck.setChannelName(strMyName);
		seAck.setChannelNum(nChKey);
		
		if(confInfo.getSystemType().contentEquals("SERVER"))
		{
			user = interInfo.getLoginUsers().findMember(strChannelName);
			if( user == null )
			{
				System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL(), user("+strChannelName
						+") not found!");
				seAck.setReturnCode(0);			
				CMEventManager.unicastEvent(seAck, (SocketChannel)msg.m_ch, cmInfo);				
				seAck = null;
				se = null;
				return;
			}			
		}
		else
		{
			String strCurrentSession = interInfo.getMyself().getCurrentSession();
			String strCurrentGroup = interInfo.getMyself().getCurrentGroup();
			
			CMSession session = interInfo.findSession(strCurrentSession);
			if(session == null)
			{
				System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL(),"
						+"session("+strCurrentSession+") not found!");
				return;
			}
			CMGroup group = session.findGroup(strCurrentGroup);
			if(group == null)
			{
				System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL(),"
						+"group("+strCurrentGroup+") not found!");
				return;
			}
			user = group.getGroupUsers().findMember(strChannelName);
			if(user == null)
			{
				System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL(),"
						+"user("+strChannelName+") not found in the current session("
						+strCurrentSession+") and current group("+strCurrentGroup+")!");
				return;
			}
		}
		
		scInfo = user.getBlockSocketChannelInfo();
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc == null)
		{
			System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL(), channel not found! "
					+"user("+strChannelName+"), channel key("+nChKey+")");
			seAck.setReturnCode(0);
			
			if(confInfo.getCommArch().contentEquals("CM_CS") && 
					confInfo.getSystemType().contentEquals("CLIENT"))
			{
				// If this node is the client type, the requester is another client 
				// and this reply event should be forwarded by the default server.
				// The server never sends the REMOVE_BLOCK_SOCKET_CHANNEL event to the client.
				seAck.setDistributionSession("CM_ONE_USER");
				seAck.setDistributionGroup(user.getName());
				CMEventManager.unicastEvent(seAck, interInfo.getDefaultServerInfo()
						.getServerName(), cmInfo);
			}
			else
			{
				CMEventManager.unicastEvent(seAck,  user.getName(), cmInfo);				
			}

			seAck = null;
			se = null;
			return;
		}
		
		// found the blocking channel that will be disconnected
		seAck.setReturnCode(1);	// ok
		
		if(confInfo.getCommArch().contentEquals("CM_CS") && 
				confInfo.getSystemType().contentEquals("CLIENT"))
		{
			// If this node is the client type, the requester is another client 
			// and this reply event should be forwarded by the default server.
			// The server never sends the REMOVE_BLOCK_SOCKET_CHANNEL event to the client.
			seAck.setDistributionSession("CM_ONE_USER");
			seAck.setDistributionGroup(user.getName());
			CMEventManager.unicastEvent(seAck, interInfo.getDefaultServerInfo()
					.getServerName(), cmInfo);
		}
		else
		{
			CMEventManager.unicastEvent(seAck, user.getName(), cmInfo);			
		}

		seAck = null;
		se = null;
		
		try {
			recvBuf = ByteBuffer.allocate(Integer.BYTES);
			System.out.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL(),waiting for disconnection "
					+"from the client.");
			nRecvBytes = sc.read(recvBuf);	// wait for detecting the disconnection of this channel from the client
			if(CMInfo._CM_DEBUG)
				System.out.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL(), the number "
						+"of received bytes: "+nRecvBytes+" Bytes.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL(), disconnection detected "
					+"by the IOException!");
		} 

		// close the channel and remove the channel info
		try {
			sc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scInfo.removeChannel(nChKey);
		
		return;
	}
	
	private static void processREMOVE_BLOCK_SOCKET_CHANNEL_ACK(CMMessage msg, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		String strMyName = interInfo.getMyself().getName();
		CMServer serverInfo = null;
		CMChannelInfo<Integer> scInfo = null;
		SocketChannel sc = null;
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		int nChKey = se.getChannelNum();
		String strTarget = se.getChannelName();
		boolean result = false;
		
		// If this node is not the receiver of the received event,
		if(!se.getReceiver().contentEquals(strMyName))
		{
			System.err.println("CMInteractionManager.processREMOVE_BLOCK_CHANNEL_ACK(), "
					+"receiver("+se.getReceiver()+") is not me("+strMyName+").");
			return;
		}

		if(se.getReturnCode() == 1)
		{
			/*
			if(strServer.equals(interInfo.getDefaultServerInfo().getServerName()))
				serverInfo = interInfo.getDefaultServerInfo();
			else
				serverInfo = interInfo.findAddServer(strServer);
				
			if(serverInfo == null)
			{
				System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL_ACK(), "
						+"server information not found: server("+strServer+"), channel key("+nChKey+")");
					
				return;
			}
			*/
			serverInfo = CMInteractionManager.findServer(strTarget, cmInfo);
			if(serverInfo != null)
			{
				scInfo = serverInfo.getBlockSocketChannelInfo();
			}
			else
			{
				CMUser targetUser = CMInteractionManager.findGroupMemberOfClient(strTarget, 
						cmInfo);
				if(targetUser == null)
				{
					System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL_ACK(), "
							+" target("+strTarget+") not found!");
					return;
				}
				scInfo = targetUser.getBlockSocketChannelInfo();
			}
				
			//scInfo = serverInfo.getBlockSocketChannelInfo();
			sc = (SocketChannel) scInfo.findChannel(nChKey);
				
			if(sc == null)
			{
				System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL_ACK(), "
						+"the socket channel not found: channel key("
						+nChKey+"), target("+strTarget+")");
				
				return;
			}
			
			result = scInfo.removeChannel(nChKey);
			if(result)
			{
				if(CMInfo._CM_DEBUG)
					System.out.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL_ACK(), "
							+"succeeded : channel key("+nChKey+"), target("+strTarget+")");
			}
			else
			{
				System.err.println("CMInteractionManager.processREMOVE_BLOCK_SOCKET_CHANNEL_ACK(), "
						+"failed to remove the channel : channel key("
						+nChKey+"), target("+strTarget+")");
			}
			
			return;
		}
		else
		{
			System.err.println("CMInteractionManager.processREMOVE_BLOCK_CHANNEL_ACK(), the server fails to accept "
					+" the removal request of the channel: key("
					+nChKey+"), target("+strTarget+")");
			return;			
		}
			
	}
	
	private static void processREGISTER_USER(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		String strQuery = null;
		ResultSet rs = null;
		int ret = -1;
		int nReturnCode = 0;
		String strCreationTime = "";
		
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		
		// process the request only if CM DB is configured to be used
		if( confInfo.isDBUse() )
		{
			// find if the user name already exists or not
			strQuery = "select * from  user_table where userName='"+se.getUserName()+"';";
			rs = CMDBManager.sendSelectQuery(strQuery, cmInfo);
			try {
				if( rs != null && rs.next() )
				{
					// the requested user already exists
					System.out.println("CMInteractionManager.processREGISTER_USER(), user("
							+se.getUserName()+") already exists in DB!");
				}
				else
				{
					// insert a new user
					ret = CMDBManager.queryInsertUser(se.getUserName(), se.getPassword(), cmInfo);
					if( ret == 1 )	// not clear
					{
						// get the inserted creationTime from DB
						strQuery = "select creationTime from user_table where userName='"
								+se.getUserName()+"';";
						rs = CMDBManager.sendSelectQuery(strQuery, cmInfo);
						rs.next();
						strCreationTime = rs.getString("creationTime");
						nReturnCode = 1;
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				CMDBManager.closeDB(cmInfo);
				CMDBManager.closeRS(rs);
			}

		}
		else
		{
			System.out.println("CMInteractionManager.processREGISTER_USER(), CM DB not used!");
		}

		// send back an ack event
		CMSessionEvent seAck = new CMSessionEvent();
		seAck.setID(CMSessionEvent.REGISTER_USER_ACK);
		seAck.setReturnCode(nReturnCode);
		seAck.setUserName(se.getUserName());
		seAck.setCreationTime(strCreationTime);
		CMEventManager.unicastEvent(seAck, (SocketChannel) msg.m_ch, cmInfo);

		se = null;
		seAck = null;
		return;
	}

	private static void processREGISTER_USER_ACK(CMSessionEvent se, CMInfo cmInfo)
	{

		if( se.getReturnCode() == 1 )
		{
			// user registration succeeded
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processREGISTER_USER_ACK(), user("
						+se.getUserName()+") registered at time("+se.getCreationTime()+").");
			}
		}
		else
		{
			// user registration failed
			System.out.println("CMInteractionManager.processREGISTER_USER_ACK(), FAILED for user("
					+se.getUserName()+")!");
		}
		
		return;
	}
	
	private static void processDEREGISTER_USER(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		String strQuery = null;
		ResultSet rs = null;
		int ret = -1;
		int nReturnCode = 0;	// 0 is error code

		CMSessionEvent se = new CMSessionEvent(msg.m_buf);

		// process the request only if CM DB is configured to be used
		if( confInfo.isDBUse() )
		{
			// check the user authentication (if user name and password are correct or not)
			strQuery = "select * from user_table where userName='"+se.getUserName()+"' and "
					+"password=PASSWORD('"+se.getPassword()+"');";
			rs = CMDBManager.sendSelectQuery(strQuery, cmInfo);
			try {
				if( rs != null && !rs.next() )
				{
					// authentication failed
					System.out.println("CMInteractionManager.processDEREGISTER_USER(), user name or "
							+ "password not correct! user("+se.getUserName()+").");
				}
				else
				{
					// delete a user from DB
					ret = CMDBManager.queryDeleteUser(se.getUserName(), cmInfo);
					if( ret == 1 ) // not clear
					{
						nReturnCode = 1;
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				CMDBManager.closeDB(cmInfo);
				CMDBManager.closeRS(rs);
			}

		}
		else
		{
			System.out.println("CMInteractionManager.processDEREGISTER_USER(), CM DB not used!");
		}

		// send back an ack event
		CMSessionEvent seAck = new CMSessionEvent();
		seAck.setID(CMSessionEvent.DEREGISTER_USER_ACK);
		seAck.setReturnCode(nReturnCode);
		seAck.setUserName(se.getUserName());
		CMEventManager.unicastEvent(seAck, (SocketChannel) msg.m_ch, cmInfo);
		
		se = null;
		seAck = null;
		return;
	}

	private static void processDEREGISTER_USER_ACK(CMSessionEvent se, CMInfo cmInfo)
	{
		if( se.getReturnCode() == 1 )
		{
			// user deregistration succeeded
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processDEREGISTER_USER_ACK(), user("
						+se.getUserName()+") deregistered.");
			}
		}
		else
		{
			// user registration failed
			System.out.println("CMInteractionManager.processDEREGISTER_USER_ACK(), FAILED for user("
					+se.getUserName()+")!");
		}
		
		return;
	}
	
	private static void processFIND_REGISTERED_USER(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		String strQuery = null;
		ResultSet rs = null;
		int nReturnCode = 0;
		String strCreationTime = "";
		
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);

		// process the request only if CM DB is configured to be used
		if( confInfo.isDBUse() )
		{

			// make a search query
			strQuery = "select * from user_table where userName='"+se.getUserName()+"';";
			rs = CMDBManager.sendSelectQuery(strQuery, cmInfo);

			try {
				if( rs != null && !rs.next() )
				{
					// search failed
					System.out.println("CMInteractionManager.processFIND_REGISTERED_USER(), user("
							+se.getUserName()+") not found!");
				}
				else
				{
					// found the user
					System.out.println("CMInteractionManager.processFIND_REGISTERED_USER(), succeeded "
							+ "for user("+se.getUserName()+").");
					nReturnCode = 1;
					strCreationTime = rs.getString("creationTime");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				CMDBManager.closeDB(cmInfo);
				CMDBManager.closeRS(rs);
			}
		}
		else
		{
			System.out.println("CMInteractionManager.processFIND_REGISTERED_USER(), CM DB is not "
					+ "used!");
		}

		// send back an ack event
		CMSessionEvent seAck = new CMSessionEvent();
		seAck.setID(CMSessionEvent.FIND_REGISTERED_USER_ACK);
		seAck.setReturnCode(nReturnCode);
		seAck.setUserName(se.getUserName());
		seAck.setCreationTime(strCreationTime);
		CMEventManager.unicastEvent(seAck, (SocketChannel) msg.m_ch, cmInfo);

		se = null;
		seAck = null;
		return;
	}
	
	private static void processFIND_REGISTERED_USER_ACK(CMSessionEvent se, CMInfo cmInfo)
	{
		if( se.getReturnCode() == 1 )
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processFIND_REGISTERED_USER_ACK(),"
						+ "succeeded: user("+se.getUserName()+"), registration time("
						+se.getCreationTime()+").");
			}
		}
		else
		{
			System.out.println("CMInteractionManager.processFIND_REGISTERED_USER_ACK(), "
					+ "failed for user("+se.getUserName()+")!");
		}
		return;
	}
	
	private static void processMultiServerEvent(CMMessage msg, CMInfo cmInfo)
	{
		CMMultiServerEvent mse = new CMMultiServerEvent(msg.m_buf);
		int nEventID = mse.getID();
		switch(nEventID)
		{
		case CMMultiServerEvent.REQ_SERVER_REG:
			processREQ_SERVER_REG(msg, cmInfo);
			break;
		case CMMultiServerEvent.RES_SERVER_REG:
			processRES_SERVER_REG(mse, cmInfo);
			break;
		case CMMultiServerEvent.REQ_SERVER_DEREG:
			processREQ_SERVER_DEREG(mse, cmInfo);
			break;
		case CMMultiServerEvent.RES_SERVER_DEREG:
			processRES_SERVER_DEREG(mse, cmInfo);
			break;
		case CMMultiServerEvent.NOTIFY_SERVER_INFO:
			processNOTIFY_SERVER_INFO(mse, cmInfo);
			break;
		case CMMultiServerEvent.NOTIFY_SERVER_LEAVE:
			processNOTIFY_SERVER_LEAVE(mse, cmInfo);
			break;
		case CMMultiServerEvent.REQ_SERVER_INFO:
			processREQ_SERVER_INFO(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_LOGIN:
			processADD_LOGIN(msg, cmInfo);
			break;
		case CMMultiServerEvent.ADD_LOGIN_ACK:
			processADD_LOGIN_ACK(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_LOGOUT:
			processADD_LOGOUT(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_SESSION_ADD_USER:
			processADD_SESSION_ADD_USER(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_SESSION_REMOVE_USER:
			processADD_SESSION_REMOVE_USER(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_REQUEST_SESSION_INFO:
			processADD_REQUEST_SESSION_INFO(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_RESPONSE_SESSION_INFO:
			processADD_RESPONSE_SESSION_INFO(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_JOIN_SESSION:
			processADD_JOIN_SESSION(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_JOIN_SESSION_ACK:
			processADD_JOIN_SESSION_ACK(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_LEAVE_SESSION:
			processADD_LEAVE_SESSION(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_CHANGE_SESSION:
			processADD_CHANGE_SESSION(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_JOIN_GROUP:
			processADD_JOIN_GROUP(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_GROUP_INHABITANT:
			processADD_GROUP_INHABITANT(mse, cmInfo);
			break;
		case CMMultiServerEvent.ADD_NEW_GROUP_USER:
			processADD_NEW_GROUP_USER(mse, cmInfo);
			break;
		default:
			System.out.println("CMInteractionManager.processMultiServerEvent(), unknown event ID: "
					+nEventID);
			mse = null;
			return;
		}
		
		mse = null;
		return;
	}
	
	private static void processREQ_SERVER_REG(CMMessage msg, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		String strServerName = null;
		String strServerAddress = null;
		int nServerPort = -1;
		int nServerUDPPort = -1;
		boolean bRet = false;
		CMCommInfo commInfo = cmInfo.getCommInfo();
		
		CMMultiServerEvent mse = new CMMultiServerEvent(msg.m_buf);

		strServerName = mse.getServerName();
		strServerAddress = mse.getServerAddress();
		nServerPort = mse.getServerPort();
		nServerUDPPort = mse.getServerUDPPort();

		// add a new server info
		CMServer server = new CMServer(strServerName, strServerAddress, nServerPort, nServerUDPPort);
		server.getNonBlockSocketChannelInfo().addChannel(0, msg.m_ch);	// add default channel to the new server
		int nKeepAliveTime = mse.getKeepAliveTime();
		if(nKeepAliveTime > 0)
			server.setKeepAliveTime(nKeepAliveTime);
		else
			server.setKeepAliveTime(cmInfo.getConfigurationInfo().getKeepAliveTime());
		
		server.setLastEventTransTime(System.currentTimeMillis());
		bRet = interInfo.addAddServer(server);
		
		// remove channel from unknown-channel list
		CMList<CMUnknownChannelInfo> unchInfoList = commInfo.getUnknownChannelInfoList();
		bRet = unchInfoList.removeElement(new CMUnknownChannelInfo((SocketChannel)msg.m_ch));
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processREQ_SERVER_REG(), removed "
					+"from unknown-channel list: "+msg.m_ch);
		}
		if(!bRet)
		{
			System.err.println("CMInteractionManager.processREQ_SERVER_REG(), error to "
					+"remove from unknown-channel list: "+msg.m_ch);
		}

		// send response event
		CMMultiServerEvent mseAck = new CMMultiServerEvent();
		mseAck.setID( CMMultiServerEvent.RES_SERVER_REG );
		mseAck.setSender(interInfo.getMyself().getName());
		mseAck.setServerName( strServerName );
		if(bRet)
			mseAck.setReturnCode(1);
		else
			mseAck.setReturnCode(0);
		CMEventManager.unicastEvent(mseAck, (SocketChannel) msg.m_ch, cmInfo);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processREQ_SERVER_REG(), server("+strServerName
					+"), return code("+mseAck.getReturnCode()+").");
		}

		// notify it to existing users of the default server
		CMServerInfo sinfo = new CMServerInfo();
		sinfo.setServerName(strServerName);
		sinfo.setServerAddress(strServerAddress);
		sinfo.setServerPort(nServerPort);
		sinfo.setServerUDPPort(nServerUDPPort);

		mseAck = new CMMultiServerEvent();
		mseAck.setID( CMMultiServerEvent.NOTIFY_SERVER_INFO );
		mseAck.setServerNum(1);
		mseAck.addServerInfo(sinfo);
		CMEventManager.broadcastEvent(mseAck, cmInfo);

		mse = null;
		sinfo = null;
		mseAck = null;
		return;
	}
	
	private static void processRES_SERVER_REG(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		interInfo.getDefaultServerInfo().setServerName(mse.getSender());
		
		if(mse.getReturnCode() == 1)
		{
			interInfo.getMyself().setState(CMInfo.CM_LOGIN);
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processRES_SERVER_REG(), server("
						+mse.getServerName()+") is successfully registered to the default server.");
			}
		}
		else
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processRES_SERVER_REG(), server("
						+mse.getServerName()+") was not registered to the default server.");
			}
		}

		return;
	}
	
	private static void processREQ_SERVER_DEREG(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		boolean bRet = false;

		// delete a server info
		String serverName = mse.getServerName();
		if( !interInfo.isAddServer(serverName) )
			bRet = false;
		else
			bRet = true;

		// send a response event
		CMMultiServerEvent mseAck = new CMMultiServerEvent();
		mseAck.setID(CMMultiServerEvent.RES_SERVER_DEREG);
		mseAck.setServerName(serverName);
		if(bRet)
			mseAck.setReturnCode(1);
		else
			mseAck.setReturnCode(0);
		CMEventManager.unicastEvent(mseAck, serverName, cmInfo);
		
		if(bRet)
		{
			// add channel to the unknown-channel list
			CMServer addServer = interInfo.findAddServer(serverName);
			SelectableChannel ch = addServer.getNonBlockSocketChannelInfo().findChannel(0);
			CMList<CMUnknownChannelInfo> unchInfoList = commInfo.getUnknownChannelInfoList();
			boolean bAdded = unchInfoList.addElement(new CMUnknownChannelInfo((SocketChannel)ch));
			if(bAdded && CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processREQ_SERVER_DEREG(), added "
						+"to unknown-channel list: "+ch);
				System.out.println("# unknown-channel list members: "+unchInfoList.getSize());
			}
			if(!bAdded)
			{
				System.err.println("CMInteractionManager.processREQ_SERVER_DEREG(), error "
						+"to add to unknown-channel list: "+ch);
				System.err.println("# unknown-channel list members: "+unchInfoList.getSize());
			}
		
			// remove the requested server from the additional-server list
			interInfo.removeAddServer(serverName);	
			
			// notify a client of the deregistration
			mseAck = new CMMultiServerEvent();
			mseAck.setID(CMMultiServerEvent.NOTIFY_SERVER_LEAVE);
			mseAck.setServerName(serverName);
			CMEventManager.broadcastEvent(mseAck, cmInfo);
		}

		mseAck = null;
		return;
	}

	private static void processRES_SERVER_DEREG(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		if(mse.getReturnCode() == 1)
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processRES_SERVER_DEREG(), server("
						+mse.getServerName()+") is successfully deregistered from the default server.");
			}
		}
		else
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processRES_SERVER_DEREG(), server("
						+mse.getServerName()+") was not deregistered from the default server.");
			}
		}

		return;
	}
	
	private static void processNOTIFY_SERVER_INFO(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		//A client must receive this event
		if( confInfo.getSystemType().equals("SERVER") )
		{
			System.out.println("CMInteractionManager.processNOTIFY_SERVER_INFO(), "
					+ "SERVER type does not need this event.");
			return;
		}

		Iterator<CMServerInfo> iter = mse.getServerInfoList().iterator();
		if(mse.getServerNum() != mse.getServerInfoList().size())
		{
			System.out.println("CMInteractionManager::processNOTIFY_SERVER_INFO(), "
					+ "server num field("+mse.getServerNum()+") and # list member("+mse.getServerInfoList().size()
					+") are different!");
			return;
		}

		//A client adds new server info (# server can be more than one)
		while(iter.hasNext())
		{
			CMServerInfo si = iter.next();
			if(interInfo.isAddServer(si.getServerName()))
			{
				System.out.println("CMInteractionManager.processNOTIFY_SERVER_INFO(), additional"
						+"server ("+si.getServerName()+") already exists!");
				continue;
			}
			else
			{
				CMServer addServer = new CMServer(si.getServerName(), si.getServerAddress(),
						si.getServerPort(), si.getServerUDPPort());
				interInfo.addAddServer(addServer);
			}
		}

		mse.removeAllServerInfoObjects();
		return;
	}
	
	private static void processNOTIFY_SERVER_LEAVE(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		//A client must receive this event
		if( confInfo.getSystemType().equals("SERVER") )
		{
			System.out.println("CMInteractionManager.processNOTIFY_SERVER_LEAVE(), SERVER type "
					+ "does not need this event.");
			return;
		}

		//A client removes the server info
		String serverName = mse.getServerName();
		if(!interInfo.isAddServer(serverName))
		{
			System.out.println("CMInteractionManager.processNOTIFY_SERVER_LEAVE(), additional "
					+"server ("+serverName+") not found.");
			return;
		}
		
		interInfo.removeAddServer(serverName);

		return;
	}
	
	private static void processREQ_SERVER_INFO(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		// send the list of server info
		CMMultiServerEvent mseAck = new CMMultiServerEvent();
		mseAck.setID(CMMultiServerEvent.NOTIFY_SERVER_INFO);
		mseAck.setServerNum( interInfo.getAddServerList().size() );
	
		Iterator<CMServer> iterAddServer = interInfo.getAddServerList().iterator();
		while(iterAddServer.hasNext())
		{
			CMServer tserver = iterAddServer.next();
			CMServerInfo si = new CMServerInfo(tserver.getServerName(), tserver.getServerAddress(),
					tserver.getServerPort(), tserver.getServerUDPPort());
			mseAck.addServerInfo(si);
		}
		CMEventManager.unicastEvent(mseAck, mse.getUserName(), cmInfo);

		mseAck.removeAllServerInfoObjects();
		mseAck = null;
		return;
	}
	
	private static void processADD_LOGIN(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();

		if(!confInfo.getSystemType().equals("SERVER"))
		{
			return;
		}
		
		CMMultiServerEvent mse = new CMMultiServerEvent(msg.m_buf);

		// omit the authentication process of the user name and password

		CMUser user = new CMUser();
		user.setName(mse.getUserName());
		user.setPasswd(mse.getPassword());
		user.setHost(mse.getHostAddress());
		user.setUDPPort(mse.getUDPPort());

		user.getNonBlockSocketChannelInfo().addChannel(0, msg.m_ch);
		interInfo.getLoginUsers().addMember(user);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_LOGIN(), add new user("
								+user.getName()+").");
		}

		if( !confInfo.isLoginScheme() )
			replyToADD_LOGIN(mse, 1, cmInfo);

		mse = null;
		return;
	}
	
	public static boolean replyToADD_LOGIN(CMMultiServerEvent mse, int nValidUser, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		CMUser user = interInfo.getLoginUsers().findMember(mse.getUserName());
		boolean bRet = false;
		
		CMMultiServerEvent mseAck = new CMMultiServerEvent();
		mseAck.setID(CMMultiServerEvent.ADD_LOGIN_ACK);
		mseAck.setServerName(mse.getServerName());
		if(nValidUser == 1)
		{
			mseAck.setValidUser(1);
			mseAck.setCommArch(confInfo.getCommArch());
			if(confInfo.isLoginScheme())
				mseAck.setLoginScheme(1);
			else
				mseAck.setLoginScheme(0);
			if(confInfo.isSessionScheme())
				mseAck.setSessionScheme(1);
			else
				mseAck.setSessionScheme(0);
			mseAck.setServerUDPPort(confInfo.getUDPPort());
		}
		else
		{
			mseAck.setValidUser(0);
			mseAck.setCommArch("");
			mseAck.setLoginScheme(-1);
			mseAck.setSessionScheme(-1);
			mseAck.setUDPPort(-1);
		}
		
		bRet = CMEventManager.unicastEvent(mseAck, mse.getUserName(), cmInfo);

		if(nValidUser == 1)
		{
			// set last event transmission time
			user.setLastEventTransTime(System.currentTimeMillis());
			// set keep-alive time
			int nKeepAliveTime = mse.getKeepAliveTime();
			if(nKeepAliveTime > 0)
				user.setKeepAliveTime(nKeepAliveTime);
			else {
				user.setKeepAliveTime(confInfo.getKeepAliveTime());
			}
			
			// remove from unknown-channel list
			SocketChannel sc = (SocketChannel) user.getNonBlockSocketChannelInfo().findChannel(0);
			CMList<CMUnknownChannelInfo> unknownChInfoList = commInfo.getUnknownChannelInfoList();
			bRet = unknownChInfoList.removeElement(new CMUnknownChannelInfo(sc));
			if(bRet && CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.replyToADD_LOGIN(), remove "+sc
						+" from the unknown-channel list.");
				System.out.println("# unknown-channel list elemetns: "+unknownChInfoList.getSize());
			}
			if(!bRet)
			{
				System.err.println("CMInteractionManager.replyToADD_LOGIN(), error to remove "+sc
						+" from the unknown-channel list!");
				System.err.println("# unknown-channel list elements: "+unknownChInfoList.getSize());
			}
			
			// send inhabitants who already logged on the system
			distributeAddLoginUsers(mse.getUserName(), cmInfo);

			// notify info. on new user who logged in
			CMMultiServerEvent tmse = new CMMultiServerEvent();
			tmse.setID(CMMultiServerEvent.ADD_SESSION_ADD_USER);
			tmse.setServerName(mse.getServerName());
			tmse.setUserName( mse.getUserName() );
			tmse.setHostAddress( mse.getHostAddress() );
			tmse.setSessionName("?");
			CMEventManager.broadcastEvent(tmse, cmInfo);
			tmse = null;
		}
		else
		{
			user.getNonBlockSocketChannelInfo().removeAllChannels();
			user.getBlockSocketChannelInfo().removeAllChannels();
			interInfo.getLoginUsers().removeMember(mse.getUserName());
		}
		
		mseAck = null;
		return bRet;
	}
	
	private static void distributeAddLoginUsers(String strUser, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		Iterator<CMUser> iter = interInfo.getLoginUsers().getAllMembers().iterator();
		CMMultiServerEvent tmse = null;
		
		while(iter.hasNext())
		{
			CMUser tuser = iter.next();
			if(!strUser.equals(tuser.getName()))
			{
				tmse = new CMMultiServerEvent();
				tmse.setID(CMMultiServerEvent.ADD_SESSION_ADD_USER);
				tmse.setServerName(interInfo.getMyself().getName());
				tmse.setUserName(tuser.getName());
				tmse.setHostAddress(tuser.getHost());
				tmse.setSessionName(tuser.getCurrentSession());
				CMEventManager.unicastEvent(tmse, strUser, cmInfo);
			}
		}

		tmse = null;
		return;
	}
	
	private static void processADD_LOGIN_ACK(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMServer tserver = null;
		CMMultiServerEvent tmse = null;
		
		if(!confInfo.getSystemType().equals("CLIENT"))
		{
			return;
		}
		
		// get a corresponding server info
		tserver = interInfo.findAddServer(mse.getServerName());
		if( tserver == null )
		{
			System.out.println("CMInteractionManager.processADD_LOGIN_ACK(), "
					+ "server("+mse.getServerName()+") info not found!");
			return;
		}

		// set other info on the server in the server info instance
		tserver.setCommArch(mse.getCommArch());
		if(mse.isLoginScheme() == 1)
			tserver.setLoginScheme(true);
		else
			tserver.setLoginScheme(false);
		if(mse.isSessionScheme() == 1)
			tserver.setSessionScheme(true);
		else
			tserver.setSessionScheme(false);
		tserver.setServerUDPPort(mse.getServerUDPPort());

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_LOGIN_ACK(),");
			System.out.println("bValidUser("+mse.isValidUser()+"), commArch("+mse.getCommArch()
					+"), bLoginScheme("+mse.isLoginScheme()+"), bSessionScheme("+mse.isSessionScheme()
					+"), server udp port("+mse.getServerUDPPort()+").");
		}

		// update peer's state in the server info instance
		if( mse.isValidUser() == 1 )
		{
			tserver.setClientState(CMInfo.CM_LOGIN);
			// request session info. if no session scheme
			if( !tserver.isSessionScheme() )
			{
				tmse = new CMMultiServerEvent();
				// send the event
				tmse.setID(CMMultiServerEvent.ADD_JOIN_SESSION);
				tmse.setServerName( mse.getServerName() );
				tmse.setUserName( interInfo.getMyself().getName() );
				tmse.setSessionName("session1");	// default session name

				CMEventManager.unicastEvent(tmse, mse.getServerName(), cmInfo);
				tserver.setCurrentSessionName("session1");
				tmse = null;
			}
		}
		else
		{
			System.out.println("CMInteractionManager.processADD_LOGIN_ACK(), invalid user.");
		}

		return;
	}
	
	private static void processADD_SESSION_ADD_USER(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		
		if(!confInfo.getSystemType().equals("CLIENT"))
			return;
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_SESSION_ADD_USER(), nothing to do at CM");
			System.out.println("server("+mse.getServerName()+"), user("+mse.getUserName()+"), host("
					+mse.getHostAddress()+"), session("+mse.getSessionName()+").");
		}
		return;
	}
	
	private static void processADD_LOGOUT(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMCommInfo commInfo = cmInfo.getCommInfo();
		boolean bRet = false;
		
		if(!confInfo.getSystemType().equals("SERVER"))
			return;
		
		CMUser user = interInfo.getLoginUsers().findMember(mse.getUserName());
		if(user == null)
		{
			System.out.println("CMInteractionManager.processADD_LOGOUT(), user("
					+mse.getUserName()+") not found in the login user list!");
			return;
		}
		
		CMSession session = interInfo.findSession(user.getCurrentSession());
		if(session != null)
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processADD_LOGOUT(), user("
						+mse.getUserName()+") should leave session("+user.getCurrentSession()+").");
			}
			CMSessionManager.leaveSession(user, cmInfo);
		}
		
		user.getNonBlockSocketChannelInfo().removeAllAddedChannels(0); // main channel remained
		user.getBlockSocketChannelInfo().removeAllChannels();
		interInfo.getLoginUsers().removeMemberObject(mse.getUserName());
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_LOGOUT(), user("
					+mse.getUserName()+") removed from the login user list, member num(" 
					+interInfo.getLoginUsers().getMemberNum()+").");
		}

		// notify that a user logged out
		CMMultiServerEvent tmse = new CMMultiServerEvent();
		tmse.setID(CMMultiServerEvent.ADD_SESSION_REMOVE_USER);
		tmse.setServerName(mse.getServerName());
		tmse.setUserName( mse.getUserName() );

		CMEventManager.broadcastEvent(tmse, cmInfo);

		// move the default channel to the unknown-channel list
		CMList<CMUnknownChannelInfo> unchInfoList = commInfo.getUnknownChannelInfoList();
		SocketChannel sc = (SocketChannel)user.getNonBlockSocketChannelInfo().findChannel(0);
		if(sc.isOpen())
		{
			bRet = unchInfoList.addElement(new CMUnknownChannelInfo(sc));
			if(bRet && CMInfo._CM_DEBUG)
			{
				System.out.println("CMInteractionManager.processADD_LOGOUT(), add channel to "
						+"unknown-channel list: "+sc);
				System.out.println("# unknown-channel list members: "+unchInfoList.getSize());
			}
			if(!bRet)
			{
				System.err.println("CMInteractionManager.processADD_LOGOUT(), error to add channel "
						+"to unknown-channel list: "+sc);
				System.err.println("# unknown-channel list members: "+unchInfoList.getSize());
			}			
		}
		else if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_LOGOUT(), the client channel is also closed.");
		}

		tmse = null;
		return;
	}
	
	private static void processADD_SESSION_REMOVE_USER(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		
		if(!confInfo.getSystemType().equals("CLIENT"))
			return;
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_SESSION_REMOVE_USER(), nothing to do at CM");
			System.out.println("server("+mse.getServerName()+"), user("+mse.getUserName()+").");
		}
		return;
	}
	
	private static void processADD_REQUEST_SESSION_INFO(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		if(!confInfo.getSystemType().equals("SERVER"))
			return;
		
		CMUser user = interInfo.getLoginUsers().findMember(mse.getUserName()); 
		if( user == null )
		{
			System.out.println("CMInteractionManager.processADD_REQUEST_SESSION_INFO(), "
					+ "user("+mse.getUserName()+") not found in the login user list!");
			return;
		}
		
		CMMultiServerEvent tmse = new CMMultiServerEvent();

		tmse.setID(CMMultiServerEvent.ADD_RESPONSE_SESSION_INFO);
		tmse.setServerName(interInfo.getMyself().getName());
		tmse.setSessionNum(interInfo.getSessionList().size());
		Iterator<CMSession> iter = interInfo.getSessionList().iterator();
		while(iter.hasNext())
		{
			CMSession session = iter.next();
			CMSessionInfo si = new CMSessionInfo();
			si.setSessionName(session.getSessionName());
			si.setAddress(session.getAddress());
			si.setPort(session.getPort());
			si.setUserNum(session.getSessionUsers().getMemberNum());
			tmse.addSessionInfo(si);
		}
		CMEventManager.unicastEvent(tmse, mse.getUserName(), cmInfo);

		tmse.removeAllSessionInfoObjects();
		tmse = null;
		return;
	}
	
	private static void processADD_RESPONSE_SESSION_INFO(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMServer server = null;
		
		if(!confInfo.getSystemType().equals("CLIENT"))
			return;
		
		// find server info of the client
		server = interInfo.findAddServer(mse.getServerName());
		if( server == null )
		{
			System.out.println("CMInteractionManager.processADD_RESPONSE_SESSION_INFO(), "
					+ "server("+mse.getServerName()+") info not found in the add-server list!");
			return;
		}

		Iterator<CMSessionInfo> iter = mse.getSessionInfoList().iterator();

		if(CMInfo._CM_DEBUG)
		{
			System.out.format("%-60s%n", "------------------------------------------------------------");
			System.out.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num");
			System.out.format("%-60s%n", "------------------------------------------------------------");
		}

		while(iter.hasNext())
		{
			CMSessionInfo tInfo = iter.next();
			CMSession tSession = server.findSession(tInfo.getSessionName());
			if(tSession == null)
			{
				tSession = new CMSession(tInfo.getSessionName(), tInfo.getAddress(), tInfo.getPort(),
						tInfo.getUserNum());
				server.addSession(tSession);
			}
			else
			{
				tSession.setAddress(tInfo.getAddress());
				tSession.setPort(tInfo.getPort());
				tSession.setUserNum(tInfo.getUserNum());
			}
			
			if(CMInfo._CM_DEBUG)
			{
				System.out.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(), tInfo.getPort(), tInfo.getUserNum());
			}
		}
		
		mse.removeAllSessionInfoObjects();
		return;
	}
	
	private static void processADD_JOIN_SESSION(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser user = null;
		CMSession session = null;
		CMGroup group = null;
		
		if(!confInfo.getSystemType().equals("SERVER"))
			return;
		
		// find login user info
		user = interInfo.getLoginUsers().findMember(mse.getUserName());
		if(user == null)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_SESSION(), user("
					+mse.getUserName()+") not found in the login user list of server("
					+mse.getServerName()+").");
			return;
		}

		session = interInfo.findSession(mse.getSessionName());
		if(session == null)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_SESSION(), session("
					+mse.getSessionName()+") not found, user("+mse.getUserName()+"), server("
					+mse.getServerName()+").");
			return;
		}
		
		user.setCurrentSession( mse.getSessionName() );
		
		group = session.getGroupList().elementAt(0);	// first group
		if(group == null)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_SESSION(), no group info"
					+" in session("+session.getSessionName()+"), server("+mse.getServerName()+").");
			return;
		}

		user.setCurrentGroup(group.getGroupName());
		
		// request join process to the SM
		CMSessionManager.addJoinSession(user, cmInfo);

		// notify that a user changes session to all other users
		CMMultiServerEvent tmse = new CMMultiServerEvent();
		tmse.setID(CMMultiServerEvent.ADD_CHANGE_SESSION);
		tmse.setServerName( mse.getServerName() );
		tmse.setUserName( mse.getUserName() );
		tmse.setSessionName( mse.getSessionName() );

		CMEventManager.broadcastEvent(tmse, cmInfo);

		tmse = null;
		return;
	}
	
	private static void processADD_JOIN_SESSION_ACK(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMServer server = null;
		CMSession session = null;
		CMGroup group = null;
		
		if(!confInfo.getSystemType().equals("CLIENT"))
			return;
		
		server = interInfo.findAddServer(mse.getServerName());
		if(server == null)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_SESSION_ACK(), server("
					+mse.getServerName()+") not found in the add-server list.");
			return;
		}
		
		if( mse.getGroupInfoList().size() < 1 )
		{
			System.out.println("CMInteractionManager.processADD_JOIN_SESSION_ACK(), group info "
					+ "empty.");
			return;
		}
		
		// find a current session of the add-server
		session = server.findSession(server.getCurrentSessionName());
		if(session == null)
		{
			// create a new session
			session = new CMSession();
			session.setSessionName(server.getCurrentSessionName());
			server.addSession(session);
		}

		Iterator<CMGroupInfo> iter = mse.getGroupInfoList().iterator();
		while(iter.hasNext())
		{
			CMGroupInfo gi = iter.next();
			CMGroup tgroup = new CMGroup(gi.getGroupName(), gi.getGroupAddress(), gi.getGroupPort());
			session.addGroup(tgroup);
		}
		
		group = session.getGroupList().elementAt(0);	// first group
		// set current group name of the server
		server.setCurrentGroupName( group.getGroupName() );
		// initialize current group
		CMGroupManager.init(session.getSessionName(), group.getGroupName(), cmInfo);
		// update client state of the server
		server.setClientState(CMInfo.CM_SESSION_JOIN);

		// enter the current group
		CMMultiServerEvent tmse = new CMMultiServerEvent();
		tmse.setID(CMMultiServerEvent.ADD_JOIN_GROUP);
		tmse.setServerName( mse.getServerName() );
		tmse.setUserName( myself.getName() );
		tmse.setHostAddress( myself.getHost() );
		tmse.setUDPPort( myself.getUDPPort() );
		tmse.setSessionName( server.getCurrentSessionName() );
		tmse.setGroupName( server.getCurrentGroupName() );

		CMEventManager.unicastEvent(tmse, mse.getServerName(), cmInfo);

		tmse = null;
		return;
	}
	
	private static void processADD_LEAVE_SESSION(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMSession session = null;
		CMUser user = null;
		
		if(!confInfo.getSystemType().equals("SERVER"))
			return;
		
		// find a session
		session = interInfo.findSession(mse.getSessionName());
		if(session == null)
		{
			System.out.println("CMInteractionManager.processADD_LEAVE_SESSION(), session("
					+mse.getSessionName()+") not found in this server("+interInfo.getMyself().getName()
					+")!");
			return;
		}
		
		// find a session user
		user = session.getSessionUsers().findMember(mse.getUserName());
		if(user == null)
		{
			System.out.println("CMIntractionManager.processADD_LEAVE_SESSION(), user("
					+mse.getUserName()+") not found in session("+session.getSessionName()
					+") of this server("+interInfo.getMyself().getName()+")!");
			return;
		}
		
		CMSessionManager.addLeaveSession(user, cmInfo);
		
		// notify login users of the session leave
		CMMultiServerEvent tmse = new CMMultiServerEvent();
		tmse.setID(CMMultiServerEvent.ADD_CHANGE_SESSION);
		tmse.setServerName(interInfo.getMyself().getName());
		tmse.setUserName(user.getName());
		tmse.setSessionName("");
		CMEventManager.broadcastEvent(tmse, cmInfo);
		
		//// do not send LEAVE_SESSION_ACK (?)

		tmse = null;
		return;
	}
	
	private static void processADD_CHANGE_SESSION(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		// nothing to do with this event here
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_CHANGE_SESSION(), nothing to do "
					+"in the CM.");
			System.out.println("server("+mse.getServerName()+"), user("+mse.getUserName()+
					"), session("+mse.getSessionName()+").");
		}
		return;
	}
	
	private static void processADD_JOIN_GROUP(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMSession session = null;
		CMGroup group = null;
		CMUser user = null;
		
		// find a session
		session = interInfo.findSession(mse.getSessionName());
		if(session == null)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_GROUP(), session("
					+mse.getSessionName()+") not found!");
			return;
		}
		// find a group
		group = session.findGroup(mse.getGroupName());
		if(group == null)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_GROUP(), session("
					+mse.getSessionName()+") found, but group("+mse.getGroupName()+") not found!");
			return;
		}
		// find a user
		user = interInfo.getLoginUsers().findMember(mse.getUserName());
		if(user == null)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_GROUP(), user("
					+mse.getUserName()+") not found in the login user list.");
			return;
		}
		user.setCurrentGroup(mse.getGroupName());
		boolean ret = group.getGroupUsers().addMember(user);
		if(!ret)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_GROUP(), fail to add user("
					+user.getName()+") to group("+group.getGroupName()+") of session("
					+session.getSessionName()+").");
			return;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_JOIN_GROUP(), add user("
					+mse.getUserName()+") to group("+group.getGroupName()+") of session("
					+session.getSessionName()+"), # group users("
					+group.getGroupUsers().getMemberNum()+").");
		}

		// send the new user existing group member information
		CMGroupManager.addDistributeGroupUsers(user, cmInfo);
		
		// send group members the new user information
		CMGroupManager.addNotifyGroupUsersOfNewUser(user, cmInfo);

		return;
	}
	
	private static void processADD_GROUP_INHABITANT(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMServer tserver = null;
		CMSession tsession = null;
		CMGroup tgroup = null;
		
		if( confInfo.getSystemType().equals("SERVER") )
		{
			// If a server receives this event
			System.out.println("CMInteractionManager.processADD_GROUP_INHABITANT(), a server "
					+ "does not need this event.");
			return;
		}

		// find server info of the client
		tserver = interInfo.findAddServer(mse.getServerName());
		if(tserver == null)
		{
			System.out.println("CMInteractionManager.processADD_GROUP_INHABITANT(), server("
					+mse.getServerName()+") info not found!");
			return;
		}

		// check if the session name is the same as that of event
		tsession = tserver.findSession(mse.getSessionName());
		if(tsession == null)
		{
			System.out.println("CMInteractionManager.processADD_GROUP_INHABITANT(), session("
					+mse.getSessionName()+") not found in server("+tserver.getServerName()+").");
			return;
		}

		// find group
		tgroup = tsession.findGroup(mse.getGroupName());
		if(tgroup == null)
		{
			System.out.println("CMInteractionManager.processADD_GROUP_INHABITANT(), group("
					+mse.getGroupName()+" not found in session("+tsession.getSessionName()
					+"), server("+tserver.getServerName()+").");
			return;
		}

		CMUser myself = interInfo.getMyself();
		if(myself.getName().equals(mse.getUserName()))
		{
			System.out.println("CMInteractionManager.processADD_GROUP_INHABITANT(), user("
					+mse.getUserName()+") is myself. group("+tgroup.getGroupName()+"), session("
					+tsession.getSessionName()+"), server("+tserver.getServerName()+").");
			return;
		}
		
		// add the existing group member to the group of session of the server
		CMUser user = new CMUser();
		user.setName(mse.getUserName());
		user.setHost(mse.getHostAddress());
		user.setUDPPort(mse.getUDPPort());
		user.setCurrentSession(mse.getSessionName());
		user.setCurrentGroup(mse.getGroupName());

		tgroup.getGroupUsers().addMember(user);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_GROUP_INHABITANT(), user("
					+user.getName()+"), host("+user.getHost()+"), udpport("+user.getUDPPort()
					+"), current session("+user.getCurrentSession()+"), current group("
					+user.getCurrentGroup()+").");
		}
		
		return;
	}
	
	private static void processADD_NEW_GROUP_USER(CMMultiServerEvent mse, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMServer tserver = null;
		CMSession tsession = null;
		CMGroup tgroup = null;
		
		if( confInfo.getSystemType().equals("SERVER") )
		{
			// If a server receives this event
			System.out.println("CMInteractionManager.processADD_NEW_GROUP_USER(), a server "
					+ "does not need this event.");
			return;
		}

		// find server info of the client
		tserver = interInfo.findAddServer(mse.getServerName());
		if(tserver == null)
		{
			System.out.println("CMInteractionManager.processADD_NEW_GROUP_USER(), server("
					+mse.getServerName()+") info not found!");
			return;
		}

		// check if the session name is the same as that of event
		tsession = tserver.findSession(mse.getSessionName());
		if(tsession == null)
		{
			System.out.println("CMInteractionManager.processADD_NEW_GROUP_USER(), session("
					+mse.getSessionName()+") not found in server("+tserver.getServerName()+").");
			return;
		}

		// find group
		tgroup = tsession.findGroup(mse.getGroupName());
		if(tgroup == null)
		{
			System.out.println("CMInteractionManager.processADD_NEW_GROUP_USER(), group("
					+mse.getGroupName()+" not found in session("+tsession.getSessionName()
					+"), server("+tserver.getServerName()+").");
			return;
		}

		CMUser myself = interInfo.getMyself();
		if(myself.getName().equals(mse.getUserName()))
		{
			System.out.println("CMInteractionManager.processADD_NEW_GROUP_USER, user("
					+mse.getUserName()+") is myself. group("+tgroup.getGroupName()+"), session("
					+tsession.getSessionName()+"), server("+tserver.getServerName()+").");
			return;
		}
		
		// add the existing group member to the group of session of the server
		CMUser user = new CMUser();
		user.setName(mse.getUserName());
		user.setHost(mse.getHostAddress());
		user.setUDPPort(mse.getUDPPort());
		user.setCurrentSession(mse.getSessionName());
		user.setCurrentGroup(mse.getGroupName());

		tgroup.getGroupUsers().addMember(user);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMInteractionManager.processADD_NEW_GROUP_USER(), user("
					+user.getName()+"), host("+user.getHost()+"), udpport("+user.getUDPPort()
					+"), current session("+user.getCurrentSession()+"), current group("
					+user.getCurrentGroup()+").");
		}
		
		return;
	}

	// distribute an event to members according to session/group specifier in the event header
	private static void distributeEvent(String strDistSession, String strDistGroup, CMEvent cme, int opt, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser tuser = null;
		CMSession tSession = null;
		CMMember tMember = null;
		CMGroup tGroup = null;
		
		if(strDistSession != null && !strDistSession.equals(""))
		{
			if(strDistSession.equals("CM_ONE_USER"))	// distribute to one user
			{
				tuser = interInfo.getLoginUsers().findMember(strDistGroup);
				if(tuser == null)
				{
					System.out.println("CMInteractionManager.distributeEvent(), target user("
							+strDistGroup+") not found.");
					return;
				}
				CMEventManager.unicastEvent(cme, strDistGroup, opt, cmInfo);
			}
			else if(strDistSession.equals("CM_ALL_SESSION")) // distribute to all session members
			{
				Iterator<CMSession> iterSession = interInfo.getSessionList().iterator();
				while(iterSession.hasNext())
				{
					tSession = iterSession.next();
					tMember = tSession.getSessionUsers();
					CMEventManager.castEvent(cme, tMember, opt, cmInfo);
				}
			}
			else
			{
				tSession = interInfo.findSession(strDistSession);
				if(tSession == null)
				{
					System.out.println("CMInteractionManager.distributeEvent(), session("
							+strDistSession+") not found.");
					return;
				}

				if(strDistGroup.equals("CM_ALL_GROUP"))	// distribute to all group members of a session
				{
					Iterator<CMGroup> iterGroup = tSession.getGroupList().iterator();
					while(iterGroup.hasNext())
					{
						tGroup = iterGroup.next();
						tMember = tGroup.getGroupUsers();
						CMEventManager.castEvent(cme, tMember, opt, cmInfo);
					}
				}
				else	// distribute to specific group members
				{
					tGroup = tSession.findGroup(strDistGroup);
					if(tGroup == null)
					{
						System.out.println("CMInteractionManager.distributeEvent(), group("
								+strDistGroup+") not found.");
						return;
					}
					tMember = tGroup.getGroupUsers();
					CMEventManager.castEvent(cme, tMember, opt, cmInfo);
				}
			}
		}
		
		return;
	}
	
}
