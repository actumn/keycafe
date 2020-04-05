package kr.ac.konkuk.ccslab.cm.thread;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import kr.ac.konkuk.ccslab.cm.entity.CMChannelInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMMqttSession;
import kr.ac.konkuk.ccslab.cm.entity.CMMqttWill;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMUnknownChannelInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEventSynchronizer;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMMqttInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.info.CMThreadInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMInteractionManager;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;

public class CMEventReceiver extends Thread {
	private CMBlockingEventQueue m_queue;
	private CMInfo m_cmInfo;
	
	public CMEventReceiver(CMInfo cmInfo)
	{
		m_cmInfo = cmInfo;
		m_queue = cmInfo.getCommInfo().getRecvBlockingEventQueue();
	}
	
	public void run()
	{
		CMMessage msg = null;
		boolean bForwardToApp = true;
		CMEventSynchronizer eventSync = m_cmInfo.getEventInfo().getEventSynchronizer();
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMEventReceiver starts to receive events.");
		while(!Thread.currentThread().isInterrupted())
		{
			msg = m_queue.pop();
			
			if(msg == null)
			{
				if(CMInfo._CM_DEBUG_2)
					System.out.println("CMEventReceiver.run(), msg is null.");
				continue;
			}
			
			if(msg.m_buf == null)
			{
				/*
				String strUserName = CMEventManager.findUserWithChannel(msg.m_ch, m_cmInfo.getInteractionInfo().getLoginUsers());
				if(strUserName == null)
					System.out.println("CMEventReceiver.run(), user not found.");
				else
					System.out.println("CMEventRecevier.run(), user: "+strUserName);
				*/
				if(CMInfo._CM_DEBUG_2)
					System.out.println("CMEventRecevier.run(), ByteBuffer is null.");
				
				processUnexpectedDisconnection(msg.m_ch);
				continue;
			}
			
			// deliver msg to interaction manager
			bForwardToApp = CMInteractionManager.processEvent(msg, m_cmInfo);

			// check whether the main thread is waiting for an event
			CMEvent cme = CMEventManager.unmarshallEvent(msg.m_buf);
			if(cme == null)
			{
				System.err.println("CMEventReceiver.run(): invalid CM event received!");
				continue;
			}

			if(eventSync.isWaiting() && !cme.getSender().isEmpty() && 
					cme.getSender().equals(eventSync.getWaitedReceiver()) &&
					cme.getType() == eventSync.getWaitedEventType() && 
					cme.getID() == eventSync.getWaitedEventID())
			{
				// waiting for a single reply event
				eventSync.setReplyEvent(cme);
				synchronized(eventSync)
				{
					eventSync.notify();
				}
				// initialize waited event info
				eventSync.setWaitedEvent(-1, -1, null);
			}
			else if(eventSync.isWaiting() && 
					eventSync.getWaitedEventType() == cme.getType() &&
					eventSync.getWaitedEventID() == cme.getID() && 
					eventSync.getMinNumWaitedEvents() >= 0 )
			{
				// waiting for more than one reply event
				eventSync.addReplyEvent(cme);
				synchronized(eventSync)
				{
					if(eventSync.isCompleteReplyEvents())
					{
						eventSync.notify();
						// initialize waited event info
						eventSync.setWaitedEvent(-1, -1, null);
						eventSync.setMinNumWaitedEvents(0);
					}
				}
			}
			else
			{
				if(bForwardToApp)
				{
					// deliver msg to stub module
					CMAppEventHandler appEventHandler = m_cmInfo.getAppEventHandler();
					if(appEventHandler == null)
						System.err.println("CMEventReceiver.run(), CMAppEventHandler is null!");
					else
						appEventHandler.processEvent(cme);
					
					if(cme.getType() == CMInfo.CM_USER_EVENT)
					{
						((CMUserEvent)cme).removeAllEventFields();	// clear all event fields
					}
					else if(cme.getType() == CMInfo.CM_FILE_EVENT)
					{
						((CMFileEvent)cme).setFileBlock(null);	// clear the file block
					}
					cme = null;			// clear the event				
				}				
			}
			//msg.m_buf = null;	// clear the received ByteBuffer

		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMEventReceiver is terminated.");
	}
	
	// handle unexpected disconnection with the server or the client
	private void processUnexpectedDisconnection(SelectableChannel ch)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			processDisconnectionFromServerAtClient(ch);
		}
		else if(confInfo.getSystemType().equals("SERVER"))
		{
			// find user with channel
			CMUser user = CMInteractionManager.findUserWithSocketChannel(ch, interInfo.getLoginUsers());
			// find unknown channel
			if(user == null)
			{
				CMList<CMUnknownChannelInfo> unchInfoList = commInfo.getUnknownChannelInfoList();
				CMUnknownChannelInfo unchInfo = unchInfoList.findElement(new CMUnknownChannelInfo((SocketChannel)ch));
				if(unchInfo != null)
				{
					boolean bRet = unchInfoList.removeElement(unchInfo);
					if(bRet && CMInfo._CM_DEBUG)
					{
						System.out.println("CMEventReceiver.processUnexpectedDisconnection(), removed from "
								+"unknown-channel list: "+ch);
						System.out.println("unknown-channel-list members: : "+unchInfoList.getSize());
					}
					if(!bRet)
					{
						System.err.println("CMEventReceiver.processUnexpectedDisconnection(), error to remove "
								+"from unknown-channel list: "+ch+" !");
						System.err.println("unknown-channel-list members: : "+unchInfoList.getSize());
					}
				}
			}
			
			if(user != null)
			{
				processDisconnectionFromClientAtServer(user.getName(), ch);
			}
			else if(CMConfigurator.isDServer(m_cmInfo))
			{
				processDisconnectionFromAddServerAtDefaultServer(ch);
			}
			else
			{
				processDisconnectionFromDefaultServerAtAddServer(ch);
			}
		}
				
		return;
	}
	
	private void processDisconnectionFromServerAtClient(SelectableChannel ch)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMChannelInfo<Integer> chInfo = null;
		Integer chKey = null;
		Iterator<CMServer> iterAddServer = null;
		boolean bFound = false;
		CMServer tserver = null;
		CMFileTransferInfo fInfo = m_cmInfo.getFileTransferInfo();
		CMSNSInfo snsInfo = m_cmInfo.getSNSInfo();		
		
		// find channel from default server
		chInfo = interInfo.getDefaultServerInfo().getNonBlockSocketChannelInfo();
		chKey = chInfo.findChannelKey(ch);
		if(chKey == null) // ch key not found
		{
			// find channel from additional server list
			iterAddServer = interInfo.getAddServerList().iterator();
			bFound = false;
			while(iterAddServer.hasNext() && !bFound)
			{
				tserver = iterAddServer.next();
				chInfo = tserver.getNonBlockSocketChannelInfo();
				chKey = chInfo.findChannelKey(ch);
				if(chKey != null)
					bFound = true;
			}
			if(bFound)
			{
				if(chKey.intValue() == 0)
				{
					chInfo.removeAllChannels();
					tserver.getBlockSocketChannelInfo().removeAllChannels();
					fInfo.removeRecvFileList(tserver.getServerName());
					fInfo.removeSendFileList(tserver.getServerName());
					tserver.getSessionList().removeAllElements();
					tserver.setClientState(CMInfo.CM_INIT);
				}
				else if(chKey.intValue() > 0)
				{
					chInfo.removeChannel(chKey);
				}

				CMSessionEvent se = new CMSessionEvent();
				se.setID(CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION);
				se.setChannelName(tserver.getServerName());
				se.setChannelNum(chKey);
				m_cmInfo.getAppEventHandler().processEvent(se);
			}				
		}
		else	// default channel
		{
			if(chKey.intValue() == 0)
			{
				// remove all non-blocking channels
				chInfo.removeAllChannels();
				// remove all blocking channels
				interInfo.getDefaultServerInfo().getBlockSocketChannelInfo().removeAllChannels();
				
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
				
				System.err.println("CMEventReceiver.processDisconnectionFromServerAtClient(): "
						+ "The default server is disconnected!");
				
			}
			else if(chKey.intValue() > 0) // additional channel
			{
				chInfo.removeChannel(chKey);
			}

			// notify to the application
			String strDefServer = interInfo.getDefaultServerInfo().getServerName();
			CMSessionEvent se = new CMSessionEvent();
			se.setID(CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION);
			se.setChannelName(strDefServer);
			se.setChannelNum(chKey);
			m_cmInfo.getAppEventHandler().processEvent(se);
			
			// check and stop the scheduled keep-alive task
			if(CMInteractionManager.getNumLoginServers(m_cmInfo) == 0)
			{
				CMThreadInfo threadInfo = m_cmInfo.getThreadInfo();
				ScheduledFuture<?> future = threadInfo.getScheduledFuture();
				if(future != null)
				{
					future.cancel(true);
					
					if(CMInfo._CM_DEBUG)
					{
						System.out.println("CMEventReceiver.processDisconnectionFromServer"
								+ "AtClient(), stop the client keep-alive task.");
					}
				}
			}

		}

	}
	
	private void processDisconnectionFromClientAtServer(String strUser, SelectableChannel ch)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		Integer chKey = null;

		CMUser user = interInfo.getLoginUsers().findMember(strUser);
		// find channel index
		chKey = user.getNonBlockSocketChannelInfo().findChannelKey(ch);
		if(chKey == null)
		{
			System.err.println("CMEventReceiver.processDisconnectionFromClientAtServe(), "
					+ "key not found for the channel(hash code: "+ ch.hashCode() +")!");
		}
		else if(chKey.intValue() == 0)
		{
			// send MQTT will event
			CMMqttManager mqttManager = (CMMqttManager)m_cmInfo.getServiceManagerHashtable()
					.get(CMInfo.CM_MQTT_MANAGER);			
			mqttManager.sendMqttWill(strUser);
			
			// if the removed channel is default channel (#ch:0), process logout of the user
			CMSessionEvent tse = new CMSessionEvent();
			tse.setID(CMSessionEvent.LOGOUT);
			tse.setUserName(user.getName());
			CMMessage msg = new CMMessage();
			msg.m_buf = CMEventManager.marshallEvent(tse);
			CMInteractionManager.processEvent(msg, m_cmInfo);
			m_cmInfo.getAppEventHandler().processEvent(tse);
			tse = null;
			msg.m_buf = null;
		}
		else if(chKey.intValue() > 0)
		{
			// remove the channel
			user.getNonBlockSocketChannelInfo().removeChannel(chKey);
		}
	}
	
	private void processDisconnectionFromAddServerAtDefaultServer(SelectableChannel ch)
	{
		Iterator<CMServer> iterAddServer = null;
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		boolean bFound = false;
		CMChannelInfo<Integer> chInfo = null;
		Integer chKey = null;
		CMServer tserver = null;

		// process disconnection with additional server
		iterAddServer = interInfo.getAddServerList().iterator();
		bFound = false;
		while(iterAddServer.hasNext() && !bFound)
		{
			tserver = iterAddServer.next();
			chInfo = tserver.getNonBlockSocketChannelInfo();
			chKey = chInfo.findChannelKey(ch);
			if(chKey != null)
				bFound = true;
		}
		if(bFound)
		{
			if(chKey.intValue() == 0)
			{
				// notify clients of the deregistration
				CMMultiServerEvent mse = new CMMultiServerEvent();
				mse.setID(CMMultiServerEvent.NOTIFY_SERVER_LEAVE);
				mse.setServerName(tserver.getServerName());
				CMEventManager.broadcastEvent(mse, m_cmInfo);

				chInfo.removeAllChannels();
				interInfo.removeAddServer(tserver.getServerName());
				mse = null;
			}
			else if(chKey.intValue() > 0)
				chInfo.removeChannel(chKey);
			
			CMSessionEvent se = new CMSessionEvent();
			se.setID(CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION);
			se.setChannelName(tserver.getServerName());
			se.setChannelNum(chKey);
			m_cmInfo.getAppEventHandler().processEvent(se);

		}

	}
	
	private void processDisconnectionFromDefaultServerAtAddServer(SelectableChannel ch)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMChannelInfo<Integer> chInfo = null;
		Integer chKey = null;

		// process disconnection with the default server
		// find channel from default server
		chInfo = interInfo.getDefaultServerInfo().getNonBlockSocketChannelInfo();
		chKey = chInfo.findChannelKey(ch);
		
		if(chKey == null)
		{
			System.err.println("CMEventReceiver.processDisconnectionFromDefaultServerAtAddServer(); "
					+ "key not found for the channel(hash code: "+ch.hashCode()+")!");
		}
		else if(chKey == 0)	// default channel
		{
			chInfo.removeAllChannels();
			// For the clarity, the client must be back to initial state (not yet)
			interInfo.getMyself().setState(CMInfo.CM_INIT);
			System.err.println("The default server is disconnected.");
		}
		else if(chKey > 0) // additional channel
		{
			chInfo.removeChannel(chKey);
		}
		
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION);
		se.setChannelName(interInfo.getDefaultServerInfo().getServerName());
		se.setChannelNum(chKey);
		m_cmInfo.getAppEventHandler().processEvent(se);

	}

	/*
	// send MQTT will event if the disconnected client has will information
	private boolean sendMqttWill(String strUser)
	{
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		CMMqttSession session = mqttInfo.getMqttSessionHashtable().get(strUser);
		if(session == null) return false;
		CMMqttWill mqttWill = session.getMqttWill();
		if(mqttWill == null) return false;
		
		CMMqttManager mqttManager = (CMMqttManager)m_cmInfo.getServiceManagerHashtable()
				.get(CMInfo.CM_MQTT_MANAGER);
		boolean bRet = false;
		bRet = mqttManager.publish(mqttWill.getWillTopic(), mqttWill.getWillMessage(), 
				mqttWill.getWillQoS());
		return bRet;
	}
	*/
}
