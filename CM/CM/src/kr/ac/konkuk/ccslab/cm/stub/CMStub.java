package kr.ac.konkuk.ccslab.cm.stub;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import kr.ac.konkuk.ccslab.cm.entity.CMChannelInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.handler.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.event.handler.CMMqttEventHandler;
import kr.ac.konkuk.ccslab.cm.event.CMEventSynchronizer;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMEventInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMThreadInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.manager.CMInteractionManager;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.manager.CMServiceManager;
import kr.ac.konkuk.ccslab.cm.thread.CMByteReceiver;
import kr.ac.konkuk.ccslab.cm.thread.CMByteSender;
import kr.ac.konkuk.ccslab.cm.thread.CMEventReceiver;
import kr.ac.konkuk.ccslab.cm.thread.CMOpenChannelTask;
import kr.ac.konkuk.ccslab.cm.thread.CMRemoveChannelTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * This class provides CM APIs of the common communication services that can be called by both the server and the client 
 * applications.
 * This class is the super class of the CMClientStub and the CMServerStub classes.
 * Application developers should generate an instance of the two sub-classes instead of the CMStub class.
 * 
 * @author CCSLab, Konkuk University
 * @see CMClientStub
 * @see CMServerStub
 */
public class CMStub {
	protected CMInfo m_cmInfo;
	
	/**
	 * Creates an instance of the CMStub class.
	 * 
	 * <p> This method is called first when the sub-classes of the CMStub class call the default constructor. 
	 */
	public CMStub()
	{
		m_cmInfo = new CMInfo();
	}
	
	public boolean init()
	{
		Hashtable<Integer, CMServiceManager> managerHashtable = m_cmInfo.getServiceManagerHashtable();
		Hashtable<Integer, CMEventHandler> handlerHashtable = m_cmInfo.getEventHandlerHashtable();
		
		// add cm service managers
		managerHashtable.put(CMInfo.CM_MQTT_MANAGER, new CMMqttManager(m_cmInfo));
		
		// add cm event handlers
		handlerHashtable.put(CMInfo.CM_MQTT_EVENT, new CMMqttEventHandler(m_cmInfo));
		
		if(m_cmInfo.isStarted())
		{
			System.err.println("CMStub.init(), already started!");
			return false;
		}

		// Korean encoding
		System.setProperty("file.encoding", "UTF-8");
		Field charset;
		try {
			charset = Charset.class.getDeclaredField("defaultCharset");
			charset.setAccessible(true);
			try {
				charset.set(null, null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchFieldException | SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// create an executor service object
		CMThreadInfo threadInfo = m_cmInfo.getThreadInfo();
		ExecutorService es = threadInfo.getExecutorService();
		int nAvailableProcessors = Runtime.getRuntime().availableProcessors();
		es = Executors.newFixedThreadPool(nAvailableProcessors);
		threadInfo.setExecutorService(es);
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMStub.init(), executor service created; # available processors("
					+nAvailableProcessors+").");
		}
		
		// create an scheduled executor service object
		ScheduledExecutorService ses = threadInfo.getScheduledExecutorService();
		ses = Executors.newScheduledThreadPool(1);
		threadInfo.setScheduledExecutorService(ses);
		
		m_cmInfo.getInteractionInfo().getMyself().setState(CMInfo.CM_INIT);
		return true;
	}

	/**
	 * Terminates CM.
	 * 
	 * <p> This method is called first when the sub-classes of the CMStub class call the overridden methods.
	 * All the termination procedure is processed by this method. 
	 */
	public void terminateCM()
	{
		if(!m_cmInfo.isStarted())
		{
			System.err.println("CMStub.terminate(), CM is not started yet!");
			return;
		}
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		// terminate the interaction manager
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Runnable task = new Runnable() {
			@Override
			public void run()
			{
				CMInteractionManager.terminate(m_cmInfo);
			}
		};
		Future<?> future = es.submit(task);
		try {
			future.get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//CMInteractionManager.terminate(m_cmInfo);		
		//////////
		
		// terminate threads
		CMEventInfo eventInfo = m_cmInfo.getEventInfo();
		CMEventReceiver er = eventInfo.getEventReceiver();
		if(er != null)
			er.interrupt();
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		CMByteReceiver byteReceiver = commInfo.getByteReceiver();
		if(byteReceiver != null)
			byteReceiver.interrupt();
		CMByteSender byteSender = commInfo.getByteSender();
		if(byteSender != null)
			byteSender.interrupt();
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		// close all channels

		task = new Runnable() {
			@Override
			public void run() {
				CMCommManager.terminate(m_cmInfo);
			}
		};
		future = es.submit(task);
		try {
			future.get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//CMCommManager.terminate(m_cmInfo);

		//////////
		
		// deregister all cancelled keys in the selector
		Selector sel = commInfo.getSelector();
		try {
			sel.select(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// terminate executor service
		es.shutdownNow();
		ScheduledExecutorService ses = m_cmInfo.getThreadInfo().getScheduledExecutorService();
		ses.shutdownNow();
		
		m_cmInfo.setStarted(false);
	}
	
	// set/get methods

	/**
	 * Returns a reference to the CMInfo object that the CMStub object has created.
	 * 
	 * <p> The CMInfo object includes all kinds of state information on the current CM.
	 * 
	 * @return a reference to the CMInfo object.
	 * @see CMInfo
	 */
	public CMInfo getCMInfo()
	{
		return m_cmInfo;
	}

	///////////////////////// service manager
	
	public CMServiceManager addServiceManager(int nType, CMServiceManager manager)
	{
		Hashtable<Integer, CMServiceManager> managerHashtable = m_cmInfo.getServiceManagerHashtable();
		return managerHashtable.put(nType, manager);
	}

	/**
	 * Returns a CMServiceManager reference with the given type.
	 * 
	 * @param nType - the type of CMServiceManager.
	 * <p> Currently available CMServiceManager types: 
	 * <br> CMInfo.CM_MQTT_MANAGER : CMMqttManager for the publish-subscribe service
	 * <p>The other CM service managers such as CMFileTransferManager, CMSNSManager, 
	 * CMDBManager and so on will be available. Now, those services are partly available 
	 * only through the CM stub modules.
	 * 
	 * @return the CMServiceManager reference if found; null otherwise.
	 * 
	 */
	public CMServiceManager findServiceManager(int nType)
	{
		Hashtable<Integer, CMServiceManager> managerHashtable = m_cmInfo.getServiceManagerHashtable();
		return managerHashtable.get(nType);
	}

	public CMServiceManager removeServiceManager(int nType)
	{
		Hashtable<Integer, CMServiceManager> managerHashtable = m_cmInfo.getServiceManagerHashtable();
		return managerHashtable.remove(nType);
	}
	
	public void removeAllServiceManager()
	{
		Hashtable<Integer, CMServiceManager> managerHashtable = m_cmInfo.getServiceManagerHashtable();
		managerHashtable.clear();
		return;
	}
	
	/////////////////////////// event handler
	
	public CMEventHandler addEventHandler(int nEventType, CMEventHandler handler)
	{
		Hashtable<Integer, CMEventHandler> handlerHashtable = m_cmInfo.getEventHandlerHashtable();
		return handlerHashtable.put(nEventType, handler);
	}
	
	public CMEventHandler findEventHandler(int nEventType)
	{
		Hashtable<Integer, CMEventHandler> handlerHashtable = m_cmInfo.getEventHandlerHashtable();
		return handlerHashtable.get(nEventType);
	}
	
	public CMEventHandler removeEventHandler(int nEventType)
	{
		Hashtable<Integer, CMEventHandler> handlerHashtable = m_cmInfo.getEventHandlerHashtable();
		return handlerHashtable.remove(nEventType);
	}
	
	public void removeAllEventHandler()
	{
		Hashtable<Integer, CMEventHandler> handlerHashtable = m_cmInfo.getEventHandlerHashtable();
		handlerHashtable.clear();
	}
	
	
	/**
	 * Registers the event handler of the application.
	 * 
	 * <p> An event handler has a role of receiving a CM event whenever it is received. A developer can define an event 
	 * handler class which includes application codes so that the application can do any task when a CM event is received.
	 * The event handler class must implement the {@link CMAppEventHandler} interface which defines an event processing 
	 * method, {@link CMAppEventHandler#processEvent(CMEvent)}. 
	 *
	 * @param handler - the event handler of the application.
	 */
	public void setAppEventHandler(CMAppEventHandler handler)
	{
		m_cmInfo.setAppEventHandler(handler);
	}
	
	/**
	 * Returns the registered event handler of the application.
	 * 
	 * @return the registered event handler of the application.
	 */
	public CMAppEventHandler getAppEventHandler()
	{
		return m_cmInfo.getAppEventHandler();
	}
	
	/**
	 * Returns the current client or server information.
	 * 
	 * <p> The {@link CMUser} class includes the user information such as the name, the host address, 
	 * the UDP port number, the current session and group names, and so on.
	 *  
	 * @return the current client or the current server information.
	 */
	public CMUser getMyself()
	{
		CMUser user = m_cmInfo.getInteractionInfo().getMyself();
		return user;
	}
	
	/**
	 * Returns the default server name.
	 * 
	 * @return the default server name.
	 */
	public String getDefaultServerName()
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		String strDefServer = null;
		
		if(confInfo.getSystemType().contentEquals("SERVER"))
		{
			if(CMConfigurator.isDServer(m_cmInfo))
			{
				strDefServer = getMyself().getName();
			}
			else
			{
				strDefServer = interInfo.getDefaultServerInfo().getServerName();
			}
		}
		else
		{
			strDefServer = interInfo.getDefaultServerInfo().getServerName();
		}
		
		return strDefServer;
	}
	
	/**
	 * Replies a request from a remote CM node.
	 * 
	 * <p> Some CM events require that a requested application instead of CM needs to 
	 * determine an answer. In this case, the application can call this method to reply 
	 * the request with its answer.
	 * <br> Currently, following CM events can be used to ask an application its answer.
	 * 
	 * <ul>
	 * <li>{@link CMSessionEvent#LOGIN} : A client requests user authentication from 
	 * the default server.</li>
	 * <li>{@link CMMultiServerEvent#ADD_LOGIN} : A client request user authentication 
	 * from an additional server.</li>
	 * <li>{@link CMFileEvent#REQUEST_PERMIT_PULL_FILE} : A file receiver node requests 
	 * to receive a file from a file owner node.</li>
	 * <li>{@link CMFileEvent#REQUEST_PERMIT_PUSH_FILE} : A file sender node requests 
	 * to send a file to a file receiver node.</li>
	 * </ul>
	 * 
	 * @param event - the request event
	 * @param nReturnCode - the reply to the request.
	 * <br>1 : accepts the request.
	 * <br>0 : rejects the request.
	 * @return true if the reply is successfully sent to the requester; false otherwise.
	 */
	public boolean replyEvent(CMEvent event, int nReturnCode)
	{
		int nType = event.getType();
		int nID = event.getID();
		boolean bRet = false;
		
		if(nType == CMInfo.CM_SESSION_EVENT && nID == CMSessionEvent.LOGIN)
		{
			CMSessionEvent se = (CMSessionEvent)event;
			bRet = CMInteractionManager.replyToLOGIN(se, nReturnCode, m_cmInfo);
		}
		else if(nType == CMInfo.CM_MULTI_SERVER_EVENT && nID == CMMultiServerEvent.ADD_LOGIN)
		{
			CMMultiServerEvent mse = (CMMultiServerEvent)event;
			bRet = CMInteractionManager.replyToADD_LOGIN(mse, nReturnCode, m_cmInfo);
		}
		else if(nType == CMInfo.CM_FILE_EVENT && nID == CMFileEvent.REQUEST_PERMIT_PUSH_FILE)
		{
			CMFileEvent fe = (CMFileEvent)event;
			bRet = CMFileTransferManager.replyPermitForPushFile(fe, nReturnCode, m_cmInfo);
		}
		else if(nType == CMInfo.CM_FILE_EVENT && nID == CMFileEvent.REQUEST_PERMIT_PULL_FILE)
		{
			CMFileEvent fe = (CMFileEvent)event;
			bRet = CMFileTransferManager.replyPermitForPullFile(fe, nReturnCode, m_cmInfo);
		}
		
		return bRet;
	}
	
	////////////////////////////////////////////////////////////////////////
	// add/remove channel (DatagramChannel or MulticastChannel)
	// SocketChannel is available only in the ClientStub module
	
	/**
	 * Adds a non-blocking datagram (UDP) channel to this CM node.
	 * 
	 * <p> A developer must note that the given port number is unique and different from 
	 * that of the default channel in the configuration file. A UDP channel is identified 
	 * by the port number as an index.
	 * 
	 * @param nChPort - the port number for the new datagram (UDP) channel
	 * @return a reference to the datagram channel if it is successfully created, or null otherwise.
	 * @see CMStub#removeNonBlockDatagramChannel(int)
	 * @see CMStub#addBlockDatagramChannel(int)
	 */
	public DatagramChannel addNonBlockDatagramChannel(int nChPort)
	{
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMChannelInfo<Integer> nonBlockDCInfo = commInfo.getNonBlockDatagramChannelInfo();
		DatagramChannel dc = null;
		boolean result = false;
		
		if(nonBlockDCInfo.findChannel(nChPort) != null)
		{
			System.err.println("CMStub.addNonBlockDatagramChannel(), channel key("+nChPort+") already exists.");
			return null;
		}
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		CMOpenChannelTask task = new CMOpenChannelTask(CMInfo.CM_DATAGRAM_CHANNEL,
				confInfo.getMyAddress(), nChPort, false, m_cmInfo);
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<SelectableChannel> future = es.submit(task);
		try {
			dc = (DatagramChannel) future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//dc = (DatagramChannel) CMCommManager.openNonBlockChannel(CMInfo.CM_DATAGRAM_CHANNEL, 
		//		confInfo.getMyAddress(), nChPort, m_cmInfo);

		//////////
		
		if(dc == null)
		{
			System.err.println("CMStub.addNonBlockDatagramChannel(), failed.");
			return null;
		}
		
		result = nonBlockDCInfo.addChannel(nChPort, dc);
		if(result)
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMStub.addNonBlockDatagramChannel(), succeeded. port("+nChPort+")");
		}
		else
		{
			System.err.println("CMStub.addNonBlockDatagramChannel(), failed! port("+nChPort+")");
			return null;
		}
		
		return dc;
	}
	
	/**
	 * Removes a non-blocking datagram (UDP) channel from this CM node.
	 * 
	 * <p> Like the stream (TCP) channel case, a developer should be careful 
	 * not to remove the default channel.
	 * 
	 * @param nChPort - the port number of the channel to be removed
	 * @return true if the channel is successfully removed, or false otherwise.
	 * @see CMStub#addNonBlockDatagramChannel(int)
	 * @see CMStub#removeBlockDatagramChannel(int)
	 */
	public boolean removeNonBlockDatagramChannel(int nChPort)
	{
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		CMChannelInfo<Integer> dcInfo = commInfo.getNonBlockDatagramChannelInfo();
		boolean result = false;

		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<Boolean> future = es.submit(new CMRemoveChannelTask(dcInfo, nChPort));
		try {
			result = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//result = dcInfo.removeChannel(nChPort);
		
		//////////
		
		if(result)
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMStub.removeNonBlockDatagramChannel(), succeeded. port("+nChPort+")");
		}
		else
		{
			System.err.println("CMStub.removeNonBlockDatagramChannel(), failed! port("+nChPort+")");
		}
		return result;
	}
	
	/**
	 * Adds a blocking datagram (UDP) channel to this CM node.
	 * 
	 * <p> A UDP channel is identified by the port number as an index.
	 * 
	 * @param nChPort - the port number for the new datagram (UDP) channel
	 * @return a reference to the datagram channel if it is successfully created, or null otherwise.
	 * @see CMStub#removeBlockDatagramChannel(int)
	 * @see CMStub#addNonBlockDatagramChannel(int)
	 */
	public DatagramChannel addBlockDatagramChannel(int nChPort)
	{
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMChannelInfo<Integer> blockDCInfo = commInfo.getBlockDatagramChannelInfo();
		DatagramChannel dc = null;
		boolean result = false;
		
		if(blockDCInfo.findChannel(nChPort) != null)
		{
			System.err.println("CMStub.addBlockDatagramChannel(), channel key("+nChPort+") already exists.");
			return null;
		}
		CMOpenChannelTask task = new CMOpenChannelTask(CMInfo.CM_DATAGRAM_CHANNEL,
				confInfo.getMyAddress(), nChPort, true, m_cmInfo);
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<SelectableChannel> future = es.submit(task);
		try {
			dc = (DatagramChannel) future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//dc = (DatagramChannel) CMCommManager.openBlockChannel(CMInfo.CM_DATAGRAM_CHANNEL, 
		//		confInfo.getMyAddress(), nChPort, m_cmInfo);
		
		//////////
		
		if(dc == null)
		{
			System.err.println("CMStub.addBlockDatagramChannel(), failed.");
			return null;
		}

		result = blockDCInfo.addChannel(nChPort, dc);
		if(result)
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMStub.addBlockDatagramChannel(), succeeded. port("+nChPort+")");
		}
		else
		{
			System.err.println("CMStub.addBlockDatagramChannel(), failed! port("+nChPort+")");
			return null;
		}
		
		return dc;		
	}
	
	/**
	 * Removes a blocking datagram (UDP) channel from this CM node.
	 * 
	 * <p> Like the stream (TCP) channel case, a developer should be careful 
	 * not to remove the default channel.
	 * 
	 * @param nChPort - the port number of the channel to be removed
	 * @return true if the channel is successfully removed, or false otherwise.
	 * @see CMStub#addBlockDatagramChannel(int)
	 * @see CMStub#removeNonBlockDatagramChannel(int)
	 */
	public boolean removeBlockDatagramChannel(int nChPort)
	{
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		CMChannelInfo<Integer> dcInfo = commInfo.getBlockDatagramChannelInfo();
		boolean result = false;

		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<Boolean> future = es.submit(new CMRemoveChannelTask(dcInfo, nChPort));
		try {
			result = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//result = dcInfo.removeChannel(nChPort);
		
		//////////
		
		if(result)
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMStub.removeBlockDatagramChannel(), succeeded. port("+nChPort+")");
		}
		else
		{
			System.err.println("CMStub.removeBlockDatagramChannel(), failed! port("+nChPort+")");
		}
		return result;
	}
	
	/**
	 * Returns a blocking datagram (UDP) channel.
	 *  
	 * @param nChPort - the channel port number.
	 * @return the blocking datagram channel, or null if the channel is not found.
	 */
	public DatagramChannel getBlockDatagramChannel(int nChPort)
	{
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		CMChannelInfo<Integer> dcInfo = commInfo.getBlockDatagramChannelInfo();
		DatagramChannel dc = null;
		
		dc = (DatagramChannel) dcInfo.findChannel(nChPort);
		
		return dc;
	}

	/**
	 * Adds an additional multicast channel to this CM node.
	 * 
	 * <p> The first and second parameters specify a session and group to which a new channel is assigned, 
	 * because a multicast channel is always mapped to a specific group in a session. The third and fourth 
	 * parameters are the multicast address and the port number of the new channel. A developer must note 
	 * that the given address and the port number are unique and different from that of the default channel 
	 * in a corresponding session configuration file. A multicast channel is identified by the four-tuple: 
	 * a session name, a group name, a multicast address, and a port number.
	 * 
	 * @param strSession - the session name which the new channel belongs to
	 * @param strGroup - the group name which the new channel belongs to
	 * @param strChAddress - the multicast address
	 * @param nChPort - the port number
	 * @return true if the channel is successfully open, or false otherwise.
	 * @see CMStub#removeAdditionalMulticastChannel(String, String, String, int)
	 */
	public boolean addMulticastChannel(String strSession, String strGroup, String strChAddress, int nChPort)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		DatagramChannel mc = null;
		InetSocketAddress sockAddress = new InetSocketAddress(strChAddress, nChPort);
		boolean result = false;
		
		CMSession session = interInfo.findSession(strSession);
		if(session == null)
		{
			System.err.println("CMStub.addMulticastChannel(), session("+strSession+") not found.");
			return false;
		}
		CMGroup group = session.findGroup(strGroup);
		if(group == null)
		{
			System.err.println("CMStub.addMulticastChannel(), group("+strGroup+") not found.");
			return false;
		}
		CMChannelInfo<InetSocketAddress> mcInfo = group.getMulticastChannelInfo();
		if(mcInfo.findChannel(sockAddress) != null)
		{
			System.err.println("CMStub.addMulticastChannel(), channel index("+sockAddress.toString()+") already exists.");
			return false;
		}
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread

		CMOpenChannelTask task = new CMOpenChannelTask(CMInfo.CM_MULTICAST_CHANNEL,
				strChAddress, nChPort, false, m_cmInfo);
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<SelectableChannel> future = es.submit(task);
		try {
			mc = (DatagramChannel) future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//mc = (DatagramChannel) CMCommManager.openNonBlockChannel(CMInfo.CM_MULTICAST_CHANNEL, strChAddress, 
		//		nChPort, m_cmInfo);
		
		//////////
		
		if(mc == null)
		{
			System.err.println("CMStub.addMulticastChannel() failed.");
			return false;
		}
		
		result = mcInfo.addChannel(sockAddress, mc); 
		if(result)
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMStub.addMulticastChannel(), succeeded. session("+strSession+"), group("
						+strGroup+"), address("+strChAddress+"), port("+nChPort+")");					
			}
		}
		else
		{
			System.err.println("CMStub.addMulticastChannel(), failed! session("+strSession+"), group("
					+strGroup+"), address("+strChAddress+"), port("+nChPort+")");				
		}
		
		return result;
	}
	
	/**
	 * Removes an additional multicast channel from this CM node.
	 * 
	 * <p> Like the other additional channel cases, a developer should be careful not to remove the default channel.
	 * 
	 * @param strSession - the session name which the target channel belongs to
	 * @param strGroup - the group name which the target channel belongs to
	 * @param strChAddress - the multicast address
	 * @param nChPort - the port number
	 * @return true if the channel is successfully removed, or false otherwise.
	 * @see CMStub#addMulticastChannel(String, String, String, int)
	 */
	public boolean removeAdditionalMulticastChannel(String strSession, String strGroup, String strChAddress, int nChPort)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		InetSocketAddress sockAddress = null;
		boolean result = false;
		
		CMSession session = interInfo.findSession(strSession);
		if(session == null)
		{
			System.err.println("CMStub.removeAdditionalMulticastChannel(), session("+strSession+") not found.");
			return false;
		}
		CMGroup group = session.findGroup(strGroup);
		if(group == null)
		{
			System.err.println("CMStub.removeAdditionalMulticastChannel(), group("+strGroup+") not found.");
			return false;
		}
		CMChannelInfo<InetSocketAddress> mcInfo = group.getMulticastChannelInfo();
		sockAddress = new InetSocketAddress(strChAddress, nChPort);
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<Boolean> future = es.submit(new CMRemoveChannelTask(mcInfo, sockAddress));
		try {
			result = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//result = mcInfo.removeChannel(sockAddress);
		
		//////////
		
		if(result)
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMStub.removeAdditionalMulticastChannel(), succeeded. session("+strSession+"), group("
						+strGroup+"), address("+strChAddress+"), port("+nChPort+")");				
			}
		}
		else
		{
			System.out.println("CMStub.removeAdditionalMulticastChannel(), failed! session("+strSession+"), group("
					+strGroup+"), address("+strChAddress+"), port("+nChPort+")");			
		}
		
		return result;
	}

	////////////////////////////////////////////////////////////////////////
	// event transmission methods (if required, the default server is used.)
	
	/**
	 * Sends a CM event to a single node.
	 * 
	 * <p> This method is the same as calling send(cme, strTarget, CMInfo.CM_STREAM, 0, false) 
	 * of the {@link CMStub#send(CMEvent, String, int, int, boolean)} method.
	 * 
	 * @param cme - the CM event
	 * @param strTarget - the receiver name. 
	 * <br> The receiver can be either the server or the client. In the CM network, 
	 * all the participating nodes (servers and clients) have a string name that can be the value of this parameter. 
	 * For example, the name of the default server is "SERVER".
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#send(CMEvent, String, int)
	 * @see CMStub#send(CMEvent, String, int, int)
	 * @see CMStub#send(CMEvent, String, int, int, boolean)
	 * @see CMStub#send(CMEvent, String, int, int, int, boolean)
	 * 
	 * @see CMStub#send(CMEvent, String, String)
	 * @see CMStub#send(CMEvent, String, String, int)
	 * @see CMStub#send(CMEvent, String, String, int, int)
	 */
	public boolean send(CMEvent cme, String strTarget)
	{
		return send(cme, strTarget, CMInfo.CM_STREAM, 0, false);
	}
	
	/**
	 * Sends a CM event to a single node.
	 * 
	 * <p> This method is the same as calling send(cme, strTarget, opt, 0, false)
	 * of the {@link CMStub#send(CMEvent, String, int, int, boolean)} method.
	 *  
	 * @param cme - the CM event
	 * @param strTarget - the receiver name. 
	 * <br> The receiver can be either the server or the client. In the CM network, 
	 * all the participating nodes (servers and clients) have a string name that can be the value of this parameter. 
	 * For example, the name of the default server is "SERVER".
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#send(CMEvent, String)
	 * @see CMStub#send(CMEvent, String, int, int)
	 * @see CMStub#send(CMEvent, String, int, int, boolean)
	 * @see CMStub#send(CMEvent, String, int, int, int, boolean)
	 * 
	 * @see CMStub#send(CMEvent, String, String)
	 * @see CMStub#send(CMEvent, String, String, int)
	 * @see CMStub#send(CMEvent, String, String, int, int)
	 */
	public boolean send(CMEvent cme, String strTarget, int opt)
	{
		boolean bReturn = false;
		
		if(opt == CMInfo.CM_STREAM)
			bReturn = send(cme, strTarget, opt, 0, false);
		else if(opt == CMInfo.CM_DATAGRAM)
			bReturn = send(cme, strTarget, opt, m_cmInfo.getConfigurationInfo().getUDPPort(), false);
		else
		{
			System.err.println("CMStub.send(), invalid option !");
			return false;
		}
		
		return bReturn;
	}
	
	/**
	 * Sends a CM event to a single node.
	 * 
	 * <p> This method is the same as calling send(cme, strTarget, opt, nChNum, false)
	 * of the {@link CMStub#send(CMEvent, String, int, int, boolean)} method.
	 *  
	 * @param cme - the CM event
	 * @param strTarget - the receiver name. 
	 * <br> The receiver can be either the server or the client. In the CM network, 
	 * all the participating nodes (servers and clients) have a string name that can be the value of this parameter. 
	 * For example, the name of the default server is "SERVER".
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @param nChNum - the channel key. 
	 * <br> If the application adds additional TCP or UDP channels, they are identified 
	 * by the channel key. The key of the TCP channel should be greater than 1 (0 for the default channel), 
	 * and the key of the UDP channel is the locally bound port number.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#send(CMEvent, String)
	 * @see CMStub#send(CMEvent, String, int)
	 * @see CMStub#send(CMEvent, String, int, int, boolean)
	 * @see CMStub#send(CMEvent, String, int, int, int, boolean)
	 * 
	 * @see CMStub#send(CMEvent, String, String)
	 * @see CMStub#send(CMEvent, String, String, int)
	 * @see CMStub#send(CMEvent, String, String, int, int) 
	 */
	public boolean send(CMEvent cme, String strTarget, int opt, int nChNum)
	{
		return send(cme, strTarget, opt, nChNum, false);
	}
	
	/**
	 * Sends a CM event to a single node.
	 * 
	 * <p> This method and the other shortened forms ({@link CMStub#send(CMEvent, String)}, 
	 * {@link CMStub#send(CMEvent, String, int)}, and {@link CMStub#send(CMEvent, String, int, int)}) use 
	 * the default server if the receiver is a client, because they assume that the receiver belongs to 
	 * the default server. (If the receiver is a server, these send methods can be called without a problem.) 
	 * If the client wants to send the event to a client that belongs to another server, the client should call 
	 * the {@link CMStub#send(CMEvent, String, String, int, int)} method. (CM applications can add multiple servers 
	 * in addition to the default server.) 
	 *  
	 * @param cme - the CM event
	 * @param strTarget - the receiver name. 
	 * <br> The receiver can be either the server or the client. In the CM network, 
	 * all the participating nodes (servers and clients) have a string name that can be the value of this parameter. 
	 * For example, the name of the default server is "SERVER". 
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @param nChNum - the channel key. 
	 * <br> If the application adds additional TCP or UDP channels, they are identified 
	 * by the channel key. The key of the TCP channel should be greater than 1 (0 for the default channel), 
	 * and the key of the UDP channel is the locally bound port number.
	 * @param isBlock - the blocking option. 
	 * <br> If isBlock is true, this method uses a blocking channel to send the event. 
	 * If isBlock is false, this method uses a nonblocking channel to send the event. The CM uses nonblocking channels 
	 * by default, but the application can also add blocking channels if required.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#send(CMEvent, String)
	 * @see CMStub#send(CMEvent, String, int)
	 * @see CMStub#send(CMEvent, String, int, int)
	 * @see CMStub#send(CMEvent, String, int, int, int, boolean)
	 * 
	 * @see CMStub#send(CMEvent, String, String)
	 * @see CMStub#send(CMEvent, String, String, int) 
	 * @see CMStub#send(CMEvent, String, String, int, int)
	 */
	public boolean send(CMEvent cme, String strTarget, int opt, int nChNum, boolean isBlock)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		String strDefServer = null;
		boolean ret = false;
		
		// set sender and receiver
		cme.setSender(getMyself().getName());
		cme.setReceiver(strTarget);

		if(confInfo.getSystemType().contentEquals("CLIENT") 
				|| (confInfo.getSystemType().contentEquals("SERVER") 
						&& !CMConfigurator.isDServer(m_cmInfo)))
		{
			strDefServer = interInfo.getDefaultServerInfo().getServerName();
		}
		else
		{
			strDefServer = getMyself().getName();
		}

		// if a client in the c/s model, use internal forwarding by a server
		if(confInfo.getCommArch().equals("CM_CS") && confInfo.getSystemType().equals("CLIENT")
				&& !strTarget.equals(strDefServer) && !interInfo.isAddServer(strTarget))
		{
			cme.setDistributionSession("CM_ONE_USER");
			cme.setDistributionGroup(strTarget);
			ret = CMEventManager.unicastEvent(cme, strDefServer, opt, nChNum, m_cmInfo);
			cme.setDistributionSession("");
			cme.setDistributionGroup("");
		}
		else
		{
			ret = CMEventManager.unicastEvent(cme, strTarget, opt, nChNum, isBlock, m_cmInfo);
		}

		return ret;
	}
	
	/**
	 * Sends a CM event to a single node.
	 * 
	 * <p> This method can be called only in the following conditions. First, this method uses only the datagram 
	 * (udp) channel. Therefore, the opt parameter must be always set to CMInfo.CM_DATAGRAM (or 1).
	 * Second, this method can send an event to a CM node of which a target port number (that is not the default 
	 * port number) is known by the sender. Unlike the other send() methods, this method cannot use 
	 * the internal forwarding scheme of CM. 
	 *  
	 * @param cme - the CM event
	 * @param strTarget - the receiver name. 
	 * <br> The receiver can be either the server or the client. In the CM network, 
	 * all the participating nodes (servers and clients) have a string name that can be the value of this parameter. 
	 * For example, the name of the default server is "SERVER". 
	 * @param opt - the reliability option. 
	 * <br> The opt value of this method must be CMInfo.CM_DATAGRAM (or 1), because this method must be called 
	 * only with the UDP datagram socket channel.
	 * @param nSendPort - the datagram channel key. 
	 * <br> If the application adds additional UDP channels, they are identified 
	 * by the channel key. The key of the UDP channel is the locally bound port number.
	 * @param nRecvPort - the receiver port number.
	 * <br> If nRecvPort is 0, then CM uses the default UDP port of the receiver.
	 * @param isBlock - the blocking option. 
	 * <br> If isBlock is true, this method uses a blocking channel to send the event. 
	 * If isBlock is false, this method uses a nonblocking channel to send the event. The CM uses nonblocking channels 
	 * by default, but the application can also add blocking channels if required.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#send(CMEvent, String)
	 * @see CMStub#send(CMEvent, String, int)
	 * @see CMStub#send(CMEvent, String, int, int)
	 * @see CMStub#send(CMEvent, String, int, int, boolean)
	 * 
	 * @see CMStub#send(CMEvent, String, String)
	 * @see CMStub#send(CMEvent, String, String, int) 
	 * @see CMStub#send(CMEvent, String, String, int, int)
	 */
	public boolean send(CMEvent cme, String strTarget, int opt, int nSendPort, int nRecvPort, boolean isBlock)
	{
		boolean ret = false;

		// set sender and receiver
		cme.setSender(getMyself().getName());
		cme.setReceiver(strTarget);

		ret = CMEventManager.unicastEvent(cme, strTarget, opt, nSendPort, nRecvPort, isBlock, m_cmInfo);
		return ret;
	}
	
	/**
	 * Receives a CM event using a blocking socket channel.
	 * 
	 * <p> An application can call this method when it needs to synchronously receive a CM event 
	 * explicitly with a blocking socket channel.
	 * When an application calls the receive method, the application blocks its execution until it receives a CM event 
	 * through the channel parameter. When an event is received, the method returns the event and the application resumes 
	 * its execution. 
	 * <br> Only a client can add a blocking socket channel with {@link CMClientStub#addBlockSocketChannel(int, String)} method.
	 * Furthermore, an application can retrieve its blocking socket channels with 
	 * {@link CMClientStub#getBlockSocketChannel(int, String)} method.  
	 * 
	 * @param sc - the blocking socket channel through which the caller receives an event.
	 * @return a CM event if this method successfully receives, or null otherwise.
	 * @see CMStub#receive(DatagramChannel)
	 */
	public CMEvent receive(SocketChannel sc)
	{
		CMEvent event = null;
		ByteBuffer bufByteNum = null;
		ByteBuffer bufEvent = null;
		int ret = 0;
		int nByteNum = -1;
		int nTotalReadBytes = 0;
		
		// create ByteBuffer
		bufByteNum = ByteBuffer.allocate(Integer.BYTES);
		bufByteNum.clear();
		//ret = readStreamBytes(sc, bufByteNum);
		while(bufByteNum.hasRemaining() && ret != -1)
		{
			try {
				ret = sc.read(bufByteNum);
				nTotalReadBytes += ret;
				if(CMInfo._CM_DEBUG_2)
					System.out.println("CMStub.receive(), read "+ret+" bytes.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}			
		}
		
		// if a channel is disconnected
		if( ret == 0 || ret == -1 )
		{
			if(CMInfo._CM_DEBUG_2)
			{
				System.out.println("---- CMStub.receive(), "+sc.toString()
							+" is disconnected.");
				System.out.println("is open: "+sc.isOpen());
				System.out.println("hash code: "+sc.hashCode());
			}
			if(sc.isOpen())
			{
				try {
					sc.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return null;
		}
		else
		{
			// create a main ByteBuffer
			bufByteNum.flip();
			nByteNum = bufByteNum.getInt();
			if(CMInfo._CM_DEBUG_2)
				System.out.println("#### event byte num: "+nByteNum+" bytes, read byte num: "+ret+" bytes.");
			if(nByteNum > CMInfo.MAX_EVENT_SIZE)
			{
				System.err.println("CMStub.receive(): nByteNum("+nByteNum
						+") is greater than the maximum event size("+CMInfo.MAX_EVENT_SIZE+")!");
				return null;
			}
			else if(nByteNum < CMInfo.MIN_EVENT_SIZE)
			{
				System.err.println("CMStub.receive(): nByteNum("+nByteNum
						+") is less than the minimum event size("+CMInfo.MIN_EVENT_SIZE+")!");
				return null;
			}
			//bufEvent = ByteBuffer.allocateDirect(nByteNum);
			bufEvent = ByteBuffer.allocate(nByteNum);
			bufEvent.clear();
			bufEvent.putInt(nByteNum);	// put the first 4 bytes
			// read remaining event bytes
			//ret = readStreamBytes(sc, bufEvent);
			ret = 0;
			while(bufEvent.hasRemaining() && ret != -1)
			{
				try {
					ret = sc.read(bufEvent);
					nTotalReadBytes += ret;
					if(CMInfo._CM_DEBUG_2)
						System.out.println("CMStub.receive(), read "+ret+" bytes.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}			
			}

			if(CMInfo._CM_DEBUG_2)
				System.out.println("---- CMStub.receive(), total read bytes: "
								+nTotalReadBytes+" bytes.");
		}
		
		// If the channel is disconnected, null message is delivered.
		//if( ret == 0 || ret == -1 )
		if( ret == -1 )
		{
			if(bufEvent != null)
				bufEvent = null;
			
			return null;
		}
		else
		{
			bufEvent.flip();
		}
		
		event = CMEventManager.unmarshallEvent(bufEvent);
		
		return event;
	}
	
	/**
	 * Receives a CM event using a blocking datagram channel.
	 * 
	 * <p> An application can call this method when it needs to synchronously receive a CM event 
	 * explicitly with a blocking datagram channel.
	 * When an application calls the receive method, the application blocks its execution until it receives a CM event 
	 * through the channel parameter. When an event is received, the method returns the event and the application resumes 
	 * its execution.  
	 * <br> The application (server or client) can add a blocking datagram channel with 
	 * {@link CMStub#addBlockDatagramChannel(int)} method. 
	 * Furthermore, an application can retrieve its blocking datagram channels with 
	 * {@link CMStub#getBlockDatagramChannel(int)} method. 
	 * 
	 * @param dc - the datagram channel through which the caller receives an event.
	 * @return a CM event if this method successfully receives, or null otherwise.
	 * @see CMStub#receive(SocketChannel)
	 */
	public CMEvent receive(DatagramChannel dc)
	{
		CMEvent event = null;
		int nByteNum = 0;
		ByteBuffer bufEvent = ByteBuffer.allocate(CMInfo.SO_RCVBUF_LEN);
		bufEvent.clear();	// initialize the ByteBuffer
		
		try {
			dc.receive(bufEvent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bufEvent.flip();				// limit: cur position, position: 0
		nByteNum = bufEvent.getInt();	// get the # bytes of the message
		bufEvent.rewind();				// position: 0
		
		// check the completeness of the received message
		if(nByteNum != bufEvent.remaining())
		{
			System.err.println("CMStub.receive(), receive incomplete message. "
					+ "nByteNum("+nByteNum+" bytes), received byte num ("+bufEvent.remaining()+" bytes).");
			bufEvent = null;
			return null;
		}
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("---- CMStub.receive(), read "+bufEvent.remaining()+" bytes.");
		
		event = CMEventManager.unmarshallEvent(bufEvent);
		
		return event;
	}
	
	/**
	 * Sends a CM event and receive a reply event.
	 * 
	 * <p> This method is used when a sender node needs to synchronously communicates with 
	 * a receiver through the default non-blocking communication channel. 
	 *  
	 * Two CM nodes conduct the synchronous communication as follows:
	 * <p> 1. A sender node sends an event to a receiver.
	 * <br> 2. The sender waits until the receiver sends a reply event.
	 * <br> 3. The sender receives the reply event.
	 * <p> In the step 2, the application main thread suspends its execution after it calls this method. 
	 * However, the sender still can receive events because CM consists of multiple threads, and 
	 * the other threads can deal with the reception and process of events.
	 * 
	 * @param cme - the event to be sent
	 * @param strReceiver - the target name
	 * <br> The target node can be a server or a client. If the target is a client, the event and its 
	 * reply event are delivered through the default server.
	 * @param nWaitEventType - the waited event type of the reply event from 'strReceiver'
	 * @param nWaitEventID - the waited event ID of the reply event from 'strReceiver'
	 * @param nTimeout - the maximum time to wait in milliseconds.
	 * <br> If nTimeout is greater than 0, the main thread is suspended until the timeout time elapses.
	 * <br> If nTimeout is 0, the main thread is suspended until the reply event arrives without the timeout.
	 * @return a reply CM event if it is successfully received, or null otherwise.
	 * @see CMStub#castrecv(CMEvent, String, String, int, int, int, int)
	 */
	public CMEvent sendrecv(CMEvent cme, String strReceiver, int nWaitEventType, int nWaitEventID, 
			int nTimeout)
	{
		CMEventSynchronizer eventSync = m_cmInfo.getEventInfo().getEventSynchronizer();
		CMEvent replyEvent = null;

		eventSync.init();
		eventSync.setWaitedEvent(nWaitEventType, nWaitEventID, strReceiver);

		boolean bSendResult = send(cme, strReceiver);
		if(!bSendResult) return null;

		synchronized(eventSync)
		{
			try {
				eventSync.wait(nTimeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		replyEvent = eventSync.getReplyEvent();

		return replyEvent;
	}
	
	/**
	 * Sends a CM event to a node group.
	 * 
	 * <p> This method is the same as calling cast(cme, sessionName, groupName, CMInfo.CM_STREAM, 0)
	 * of the {@link CMStub#cast(CMEvent, String, String, int, int)} method.
	 * 
	 * @param cme - the CM event
	 * @param sessionName - the target session name
	 * <br> If sessionName is null (not specified), the event is sent to all sessions. In this case, 
	 * the groupName parameter must be also null.
	 * @param groupName - the target group name
	 * <br> If groupName is null (not specified), the event is sent to all groups in a session.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#cast(CMEvent, String, String, int)
	 * @see CMStub#cast(CMEvent, String, String, int, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String, String)
	 * @see CMStub#cast(CMEvent, String, String, String, int)
	 * @see CMStub#cast(CMEvent, String, String, String, int, int)
	 */
	public boolean cast(CMEvent cme, String sessionName, String groupName)
	{
		return cast(cme, sessionName, groupName, CMInfo.CM_STREAM, 0);
	}

	/**
	 * Sends a CM event to a node group.
	 * 
	 * <p> This method is the same as calling cast(cme, sessionName, groupName, opt, 0)
	 * of the {@link CMStub#cast(CMEvent, String, String, int, int)} method.
	 * 
	 * @param cme - the CM event
	 * @param sessionName - the target session name
	 * <br> If sessionName is null (not specified), the event is sent to all sessions. In this case, 
	 * the groupName parameter must be also null.
	 * @param groupName - the target group name
	 * <br> If groupName is null (not specified), the event is sent to all groups in a session.
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#cast(CMEvent, String, String)
	 * @see CMStub#cast(CMEvent, String, String, int, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String, String)
	 * @see CMStub#cast(CMEvent, String, String, String, int)
	 * @see CMStub#cast(CMEvent, String, String, String, int, int)
	 */
	public boolean cast(CMEvent cme, String sessionName, String groupName, int opt)
	{
		boolean bReturn = false;
		
		if(opt == CMInfo.CM_STREAM)
			cast(cme, sessionName, groupName, opt, 0);
		else if(opt == CMInfo.CM_DATAGRAM)
			cast(cme, sessionName, groupName, opt, m_cmInfo.getConfigurationInfo().getUDPPort());
		else
		{
			System.err.println("CMStub.cast(), invalid option!");
			return false;
		}
		
		return bReturn;
	}

	/**
	 * Sends a CM event to a node group.
	 * 
	 * <p> The cast method sends a CM event to a receivers group using multiple one-to-one transmission. 
	 * The range of receivers can be set with a session and a group. That is, this method sends an event 
	 * to a specific session members or group members.
	 * With the different combination of the sessionName and groupName parameters, the range of receivers is 
	 * different as described below.
	 * 
	 * <table border=1>
	 * <caption>Receiver ranges of the chat() method</caption>
	 * <tr bgcolor="lightgrey">
	 * <td>sessionName</td><td>groupName</td><td>receiver range</td>
	 * </tr>
	 * <tr>
	 * <td>not null</td><td>not null</td><td>all members of a group (groupName) in a session (sessionName)</td>
	 * </tr>
	 * <tr>
	 * <td>not null</td><td>null</td><td>all members of all groups in a session (sessionName)</td>
	 * </tr>
	 * <tr>
	 * <td>null</td><td>null</td><td>all members of all groups in all sessions (= all login users in the default server)</td>
	 * </tr>
	 * </table>
	 * 
	 * @param cme - the CM event
	 * @param sessionName - the target session name
	 * <br> If sessionName is null (not specified), the event is sent to all sessions. In this case, 
	 * the groupName parameter must be also null.
	 * @param groupName - the target group name
	 * <br> If groupName is null (not specified), the event is sent to all groups in a session.
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @param nChNum - the channel key. 
	 * <br> If the application adds additional TCP or UDP channels, they are identified 
	 * by the channel key. The key of the TCP channel should be greater than 1 (0 for the default channel), 
	 * and the key of the UDP channel is the locally bound port number.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#cast(CMEvent, String, String)
	 * @see CMStub#cast(CMEvent, String, String, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String, String)
	 * @see CMStub#cast(CMEvent, String, String, String, int)
	 * @see CMStub#cast(CMEvent, String, String, String, int, int)
	 * 
	 * @see CMStub#multicast(CMEvent, String, String)
	 * @see CMStub#broadcast(CMEvent, int, int)
	 */
	public boolean cast(CMEvent cme, String sessionName, String groupName, int opt, int nChNum)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		CMSession session = null;
		CMGroup group = null;
		CMMember member = null;
		boolean ret = false;
		
		// set sender
		cme.setSender(getMyself().getName());

		// if a client in the c/s model, use internal forwarding by a server
		//if(confInfo.getCommArch().equals("CM_CS") && confInfo.getSystemType().equals("CLIENT"))
		// if a client, use the internal forwarding by the server
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			// if the sessionName is null, broadcast
			if(sessionName == null)
			{
				cme.setDistributionSession("CM_ALL_SESSION");
				cme.setDistributionGroup("CM_ALL_GROUP");
			}
			// if the sessionName is not null, and the groupName is null, cast to all session members
			else if(groupName == null)
			{
				cme.setDistributionSession(sessionName);
				cme.setDistributionGroup("CM_ALL_GROUP");
			}
			// if both the sessionName and groupName is not null, cast to group members
			else
			{
				cme.setDistributionSession(sessionName);
				cme.setDistributionGroup(groupName);
			}
			
			ret = CMEventManager.unicastEvent(cme, strDefServer, opt, nChNum, m_cmInfo);
		}
		else	// if a server,
		{
			// if the sessionName is null, broadcast
			if(sessionName == null)
			{
				ret = CMEventManager.broadcastEvent(cme, opt, nChNum, m_cmInfo);
			}
			// if the sessionName is not null, and the groupName is null, cast to all session members
			else if(groupName == null)
			{
				session = interInfo.findSession(sessionName);
				if(session == null)
				{
					System.out.println("CMStub.cast(), session("+sessionName+") not found.");
					return false;
				}
				member = session.getSessionUsers();
				if(member == null)
				{
					System.out.println("CCMStub.cast(), session("+sessionName+") member is null.");
					return false;
				}
				ret = CMEventManager.castEvent(cme, member, opt, nChNum, m_cmInfo);
			}
			// if both the sessionName and groupName is not null, cast to group members
			else
			{
				session = interInfo.findSession(sessionName);
				if(session == null)
				{
					System.out.println("CCMStub.cast(), session("+sessionName+") not found.");
					return false;
				}
				group = session.findGroup(groupName);
				if( group == null )
				{
					System.out.println("CCMStub.cast(), grouop("+groupName+") not foudn.");
					return false;
				}
				member = group.getGroupUsers();
				if(member == null)
				{
					System.out.println("CCMStub.cast(), session("+sessionName+"), group("
										+groupName+") member is null");
					return false;
				}
				ret = CMEventManager.castEvent(cme, member, opt, nChNum, m_cmInfo);
			}
			
		}

		return ret;
	}
	
	/**
	 * Sends a CM event and receive multiple reply events.
	 * 
	 * <p> This method is used when a sender node needs to synchronously communicates with 
	 * multiple receivers (CM group members, session members, or all login users) through 
	 * the default non-blocking communication channel.
	 *  
	 * A sender node and multiple receiver nodes conduct the synchronous communication as follows:
	 * <p> 1. A sender node sends an event to receivers that are specified by a session name and a group name.
	 * <br> 2. The sender waits until at least the given minimum number of receivers send reply events.
	 * <br> 3. The sender receives an array of received reply events.
	 * <p> In the step 2, the application main thread suspends its execution after it calls this method. 
	 * However, the sender still can receive events because CM consists of multiple threads, and 
	 * the other threads can deal with the reception and process of events.
	 * 
	 * @param event - the event to be sent
	 * @param strSessionName - the target session name
	 * <br> If this parameter is null, it implies all sessions. If 'strGroupName' is not null, this parameter 
	 * must not be null, either.
	 * @param strGroupName - the target group name
	 * <br> If this parameter is null, it implies all groups.
	 * @param nWaitedEventType - the waited event type of the reply event
	 * @param nWaitedEventID - the waited event ID of the reply event
	 * @param nMinNumWaitedEvents - the minimum number of waited events
	 * <br> The sender waits until it receives at least 'nMinNumWaitedEvents' events.
	 * @param nTimeout - the maximum time to wait in milliseconds.
	 * <br> If nTimeout is greater than 0, the main thread is suspended until the timeout time elapses.
	 * <br> If nTimeout is 0, the main thread is suspended until the all reply events arrives without the timeout.
	 * @return an array of reply CM events if the minimum number (nMinNumWaitedEvents) of reply events are 
	 * successfully received, or null otherwise.
	 * <br> The size of the array can be greater than 'nMinNumWaitedEvents'.
	 * @see CMStub#sendrecv(CMEvent, String, int, int, int)
	 */
	public CMEvent[] castrecv(CMEvent event, String strSessionName, String strGroupName, 
			int nWaitedEventType, int nWaitedEventID, int nMinNumWaitedEvents, int nTimeout)
	{
		CMEventSynchronizer eventSync = m_cmInfo.getEventInfo().getEventSynchronizer();
		CMEvent[] eventArray = null;
		
		eventSync.init();
		eventSync.setWaitedEventType(nWaitedEventType);
		eventSync.setWaitedEventID(nWaitedEventID);
		if(nMinNumWaitedEvents < 0)
		{
			System.err.println("CMStub.castrecv(), nMinNumWaitedEvents = "+nMinNumWaitedEvents);
			return null;
		}
		eventSync.setMinNumWaitedEvents(nMinNumWaitedEvents);
		
		boolean bCastResult = cast(event, strSessionName, strGroupName);
		if(!bCastResult)
		{
			System.err.println("CMStub.castrecv(), error in cast()!");
			return null;
		}
		
		synchronized(eventSync)
		{
			try {
				eventSync.wait(nTimeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		eventArray = eventSync.getReplyEventListAsArray();
		if(eventArray != null && eventArray.length < nMinNumWaitedEvents)
		{
			System.err.println("CMStub.castrecv(), the number of received reply events ("+
					eventArray.length+") is less than the given minimum number("+nMinNumWaitedEvents+")!");
			return null;
		}
		
		return eventArray;
	}
	
	/**
	 * Sends a CM event to a node group via multicast.
	 * 
	 * <p> If a CM server application sets the communication architecture as the CM_PS in the server configuration file, 
	 * it creates a multicast channel assigned to each group. In this case, the server and client applications can send 
	 * an event using a multicast channel.
	 * <br> Unlike the cast method, both the session and group name values must not be null, 
	 * because the multicast is enabled only for a specific group.
	 * 
	 * @param cme - the CM event
	 * @param sessionName - the target session name
	 * @param groupName - the target group name
	 * @return true if the event is successfully multicasted; false otherwise.
	 * 
	 * @see CMStub#cast(CMEvent, String, String, int, int)
	 * @see CMStub#broadcast(CMEvent, int, int)
	 */
	public boolean multicast(CMEvent cme, String sessionName, String groupName)
	{
		// set sender
		cme.setSender(getMyself().getName());

		boolean ret = false;
		ret = CMEventManager.multicastEvent(cme, sessionName, groupName, m_cmInfo);
		return ret;
	}
	
	/**
	 * Sends a CM event to all connected nodes.
	 * 
	 * <p> This method is the same as calling broadcast(cme, CMInfo.CM_STREAM, 0)
	 * of the {@link CMStub#broadcast(CMEvent, int, int)} method.
	 * 
	 * @param cme - the CM event
	 * @return true if the event is successfully broadcasted; false otherwise.
	 * 
	 * @see CMStub#broadcast(CMEvent, int)
	 * @see CMStub#broadcast(CMEvent, int, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String, int, int)
	 * @see CMStub#multicast(CMEvent, String, String)
	 */
	public boolean broadcast(CMEvent cme)
	{
		return broadcast(cme, CMInfo.CM_STREAM, 0);
	}
	
	/**
	 * Sends a CM event to all connected nodes.
	 * 
	 * <p> This method is the same as calling broadcast(cme, opt, 0)
	 * of the {@link CMStub#broadcast(CMEvent, int, int)} method.
	 * 
	 * @param cme the CM event
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @return true if the event is successfully broadcasted; false otherwise.
	 * 
	 * @see CMStub#broadcast(CMEvent)
	 * @see CMStub#broadcast(CMEvent, int, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String, int, int)
	 * @see CMStub#multicast(CMEvent, String, String) 
	 */
	public boolean broadcast(CMEvent cme, int opt)
	{
		boolean bReturn = false;
		
		if(opt == CMInfo.CM_STREAM)
			broadcast(cme, opt, 0);
		else if(opt == CMInfo.CM_DATAGRAM)
			broadcast(cme, opt, m_cmInfo.getConfigurationInfo().getUDPPort());
		else
		{
			System.err.println("CMStub.broadcast(), invalid option!");
			return false;
		}
		
		return bReturn;
	}

	/**
	 * Sends a CM event to all connected nodes.
	 * 
	 * <p> This method is the same as calling broadcast(cme, opt, 0)
	 * of the {@link CMStub#broadcast(CMEvent, int, int)} method.
	 * <br> The broadcast method sends a CM event to all users who currently log in to the default server via multiple 
	 * one-to-one transmissions. 
	 * 
	 * @param cme the CM event
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @param nChNum - the channel key. 
	 * <br> If the application adds additional TCP or UDP channels, they are identified 
	 * by the channel key. The key of the TCP channel should be greater than 1 (0 for the default channel), 
	 * and the key of the UDP channel is the locally bound port number.
	 * @return true if the event is successfully broadcasted; false otherwise.
	 * 
	 * @see CMStub#broadcast(CMEvent)
	 * @see CMStub#broadcast(CMEvent, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String, int, int)
	 * @see CMStub#multicast(CMEvent, String, String) 
	 */
	public boolean broadcast(CMEvent cme, int opt, int nChNum)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		boolean ret = false;
		
		// set sender
		cme.setSender(getMyself().getName());

		// in the case of a client in the C/S model, use internal forwarding by all servers
		//if(confInfo.getCommArch().equals("CM_CS") && confInfo.getSystemType().equals("CLIENT"))
		// if a client, use internal forwarding by all servers
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			cme.setDistributionSession("CM_ALL_SESSION");
			cme.setDistributionGroup("CM_ALL_GROUP");
			String strDefServer = interInfo.getDefaultServerInfo().getServerName();
			ret = CMEventManager.unicastEvent(cme, strDefServer, opt, nChNum, m_cmInfo);
			
			// if there are additional servers connected
			Iterator<CMServer> iterAddServer = interInfo.getAddServerList().iterator();
			for(int i = 0; i < interInfo.getAddServerList().size(); i++)
			{
				CMServer tServer = iterAddServer.next();
				CMEventManager.unicastEvent(cme, tServer.getServerName(), opt, nChNum, m_cmInfo);
			}
		}
		else	// if a server
		{
			ret = CMEventManager.broadcastEvent(cme, opt, nChNum, m_cmInfo);
		}

		return ret;
	}

	////////////////////////////////////////////////////////////////////////
	// event transmission methods with a designated server

	/**
	 * Sends a CM event to a single node via a designated server.
	 * 
	 * <p> This method is the same as calling send(cme, serverName, userName, CMInfo.CM_STREAM, 0)
	 * of the {@link CMStub#send(CMEvent, String, String, int, int)} method.
	 * 
	 * @param cme - the CM event
	 * @param serverName - the server name
	 * @param userName - the target user name
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#send(CMEvent, String, String, int) 
	 * @see CMStub#send(CMEvent, String, String, int, int)
	 * 
	 * @see CMStub#send(CMEvent, String)
	 * @see CMStub#send(CMEvent, String, int)
	 * @see CMStub#send(CMEvent, String, int, int)
	 * @see CMStub#send(CMEvent, String, int, int, boolean) 
	 * @see CMStub#send(CMEvent, String, int, int, int, boolean)
	 */
	public boolean send(CMEvent cme, String serverName, String userName)
	{
		return send(cme, serverName, userName, CMInfo.CM_STREAM, 0);
	}

	/**
	 * Sends a CM event to a single node via a designated server.
	 * 
	 * <p> This method is the same as calling send(cme, serverName, userName, opt, 0)
	 * of the {@link CMStub#send(CMEvent, String, String, int, int)} method.
	 * 
	 * @param cme - the CM event
	 * @param serverName - the server name
	 * @param userName - the target user name
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#send(CMEvent, String, String) 
	 * @see CMStub#send(CMEvent, String, String, int, int)
	 * 
	 * @see CMStub#send(CMEvent, String)
	 * @see CMStub#send(CMEvent, String, int)
	 * @see CMStub#send(CMEvent, String, int, int)
	 * @see CMStub#send(CMEvent, String, int, int, boolean)
	 * @see CMStub#send(CMEvent, String, int, int, int, boolean)
	 */
	public 	boolean send(CMEvent cme, String serverName, String userName, int opt)
	{
		boolean bReturn = false;
		
		if(opt == CMInfo.CM_STREAM)
			send(cme, serverName, userName, opt, 0);
		else if(opt == CMInfo.CM_DATAGRAM)
			send(cme, serverName, userName, opt, m_cmInfo.getConfigurationInfo().getUDPPort());
		else
		{
			System.err.println("CMStub.send(), invalid option!");
			return false;
		}
		
		return bReturn;
	}
	
	/**
	 * Sends a CM event to a single node via a designated server.
	 * 
	 * <p> This method is usually used to send an event to a participating user node through the designated server.
	 * This method is the same as calling send(cme, userName, opt, 0, false)
	 * of the {@link CMStub#send(CMEvent, String, int, int, boolean)} method except that this method uses the designated server.
	 * <br> This method is used especially if the application uses multiple servers and the client wants to send an event to 
	 * another client. The target client (user) is connected to a server which is not the default server.
	 * 
	 * @param cme - the CM event
	 * @param serverName - the server name
	 * @param userName - the target user name
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @param nChNum - the channel key. 
	 * <br> If the application adds additional TCP or UDP channels, they are identified 
	 * by the channel key. The key of the TCP channel should be greater than 1 (0 for the default channel), 
	 * and the key of the UDP channel is the locally bound port number.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#send(CMEvent, String, String) 
	 * @see CMStub#send(CMEvent, String, String, int)
	 * 
	 * @see CMStub#send(CMEvent, String)
	 * @see CMStub#send(CMEvent, String, int)
	 * @see CMStub#send(CMEvent, String, int, int)
	 * @see CMStub#send(CMEvent, String, int, int, boolean)
	 * @see CMStub#send(CMEvent, String, int, int, int, boolean)
	 */
	public 	boolean send(CMEvent cme, String serverName, String userName, int opt, int nChNum)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		boolean ret = false;
		
		String strDefServer = null;
		if(confInfo.getSystemType().contentEquals("SERVER"))
		{
			if(CMConfigurator.isDServer(m_cmInfo))
				strDefServer = getMyself().getName();
			else
			{
				strDefServer = interInfo.getDefaultServerInfo().getServerName();
			}
		}
		else
		{
			strDefServer = interInfo.getDefaultServerInfo().getServerName();
		}

		// if a server name cannot be found, error
		if(!serverName.equals(strDefServer) && !interInfo.isAddServer(serverName))
		{
			System.out.println("CMStub::send(), server("+serverName+") not found.");
			return false;
		}

		// set sender
		cme.setSender(getMyself().getName());

		// if a client in the c/s model and a user is not null, use internal forwarding by a server
		//if(confInfo.getCommArch().equals("CM_CS") && confInfo.getSystemType().equals("CLIENT")
		//		&& userName != null)
		// if the system type is a client and a user is not null, use internal forwarding by a server
		if(confInfo.getSystemType().equals("CLIENT") && userName != null)
		{
			// set receiver
			cme.setReceiver(userName);

			cme.setDistributionSession("CM_ONE_USER");
			cme.setDistributionGroup(userName);
			ret = CMEventManager.unicastEvent(cme, serverName, opt, nChNum, m_cmInfo);
			cme.setDistributionSession("");
			cme.setDistributionGroup("");
		}
		else
		{
			// set receiver
			cme.setReceiver(serverName);

			ret = CMEventManager.unicastEvent(cme, serverName, opt, nChNum, m_cmInfo);
		}
		
		return ret;
	}

	/**
	 * Sends a CM event to a node group via a designated server.
	 * 
	 * <p> This method is the same as calling cast(cme, serverName, sessionName, groupName, CMInfo.CM_STREAM, 0)
	 * of the {@link CMStub#cast(CMEvent, String, String, String, int, int)} method.
	 *  
	 * @param cme - the CM event
	 * @param serverName - the server name
	 * @param sessionName - the target session name
	 * <br> If sessionName is null (not specified), the event is sent to all sessions. In this case, 
	 * the groupName parameter must be also null.
	 * @param groupName - the target group name
	 * <br> If groupName is null (not specified), the event is sent to all groups in a session.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#cast(CMEvent, String, String, String, int)
	 * @see CMStub#cast(CMEvent, String, String, String, int, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String)
	 * @see CMStub#cast(CMEvent, String, String, int)
	 * @see CMStub#cast(CMEvent, String, String, int, int) 
	 */
	public 	boolean cast(CMEvent cme, String serverName, String sessionName, String groupName)
	{
		return cast(cme, serverName, sessionName, groupName, CMInfo.CM_STREAM, 0);
	}
	
	/**
	 * Sends a CM event to a node group via a designated server.
	 * 
	 * <p> This method is the same as calling cast(cme, serverName, sessionName, groupName, opt, 0)
	 * of the {@link CMStub#cast(CMEvent, String, String, String, int, int)} method.
	 *   
	 * @param cme - the CM event
	 * @param serverName - the server name
	 * @param sessionName - the target session name
	 * <br> If sessionName is null (not specified), the event is sent to all sessions. In this case, 
	 * the groupName parameter must be also null.
	 * @param groupName - the target group name
	 * <br> If groupName is null (not specified), the event is sent to all groups in a session.
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#cast(CMEvent, String, String, String)
	 * @see CMStub#cast(CMEvent, String, String, String, int, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String)
	 * @see CMStub#cast(CMEvent, String, String, int)
	 * @see CMStub#cast(CMEvent, String, String, int, int)  
	 */
	public 	boolean cast(CMEvent cme, String serverName, String sessionName, String groupName, int opt)
	{
		boolean bReturn = false;
		
		if(opt == CMInfo.CM_STREAM)
			cast(cme, serverName, sessionName, groupName, opt, 0);
		else if(opt == CMInfo.CM_DATAGRAM)
			cast(cme, serverName, sessionName, groupName, opt, m_cmInfo.getConfigurationInfo().getUDPPort());
		else
		{
			System.err.println("CMStub.cast(), invalid option!");
			return false;
		}
		
		return bReturn;
	}
	
	/**
	 * Sends a CM event to a node group via a designated server.
	 * 
	 * <p> This method is usually used to send an event to a participating user node group through the designated server.
	 * This method is the same as calling cast(cme, sessionName, groupName, opt, nChNum)
	 * of the {@link CMStub#cast(CMEvent, String, String, int, int)} method except that this method uses the designated server.
	 * <br> This method is used especially if the application uses multiple servers and the client wants to send an event to 
	 * a client group. The target client (user) group is connected to a server which is not the default server. 
	 * 
	 * @param cme - the CM event
	 * @param serverName - the server name
	 * @param sessionName - the target session name
	 * <br> If sessionName is null (not specified), the event is sent to all sessions. In this case, 
	 * the groupName parameter must be also null.
	 * @param groupName - the target group name
	 * <br> If groupName is null (not specified), the event is sent to all groups in a session.
	 * @param opt - the reliability option. 
	 * <br> If opt is CMInfo.CM_STREAM (or 0), CM uses TCP socket channel to send 
	 * the event. If opt is CMInfo.CM_DATAGRAM (or 1), CM uses UDP datagram socket channel.
	 * @param nChNum - the channel key. 
	 * <br> If the application adds additional TCP or UDP channels, they are identified 
	 * by the channel key. The key of the TCP channel should be greater than 1 (0 for the default channel), 
	 * and the key of the UDP channel is the locally bound port number.
	 * @return true if the event is successfully sent; false otherwise.
	 * 
	 * @see CMStub#cast(CMEvent, String, String, String)
	 * @see CMStub#cast(CMEvent, String, String, String, int)
	 * 
	 * @see CMStub#cast(CMEvent, String, String)
	 * @see CMStub#cast(CMEvent, String, String, int)
	 * @see CMStub#cast(CMEvent, String, String, int, int)   
	 */
	public 	boolean cast(CMEvent cme, String serverName, String sessionName, String groupName, int opt, int nChNum)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		boolean ret = false;

		// check server validity
		if(!confInfo.getSystemType().equals("SERVER") && !interInfo.isAddServer(serverName))
		{
			System.out.println("CMStub::cast(), server("+serverName+") not found.");
			return false;
		}

		// set sender
		cme.setSender(getMyself().getName());

		// if a client in the c/s model, use internal forwarding by a server
		//if(confInfo.getCommArch().equals("CM_CS") && confInfo.getSystemType().equals("CLIENT"))
		// if a client, use internal forwarding by a server
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			// if the sessionName is null, broadcast
			if(sessionName == null)
			{
				cme.setDistributionSession("CM_ALL_SESSION");
				cme.setDistributionGroup("CM_ALL_GROUP");
			}
			// if the sessionName is not null, and the groupName is null, cast to all session members
			else if(groupName == null)
			{
				cme.setDistributionSession(sessionName);
				cme.setDistributionGroup("CM_ALL_GROUP");
			}
			// if both the sessionName and groupName is not null, cast to group members
			else
			{
				cme.setDistributionSession(sessionName);
				cme.setDistributionGroup(groupName);
			}

			ret = CMEventManager.unicastEvent(cme, serverName, opt, nChNum, m_cmInfo);
		}

		return ret;
	}
	
	/////////////////////////////////////////////////////////////////////
	// file transfer 

	
	/**
	 * Gets the default file path for file transfer.
	 * 
	 * @return the default file path for file transfer
	 * @see CMClientStub#setTransferedFileHome(Path)
	 * @see CMServerStub#setTransferedFileHome(Path)
	 */
	public Path getTransferedFileHome()
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		return confInfo.getTransferedFileHome();
	}
	
	/**
	 * Requests to transfer a file from a owner (pull mode).
	 * 
	 * <p> This method is the same as calling requestFile(strFileName, strFileOwner, CMInfo.FILE_DEFAULT) 
	 * of the {@link CMStub#requestFile(String, String, byte)}.
	 * 
	 * @param strFileName - the requested file name
	 * @param strFileOwner - the file owner name
	 * @return true if the permit for receiving a file is successfully requested to 
	 * the file owner, or false otherwise.
	 * @see CMStub#requestFile(String, String, byte)
	 * @see CMStub#pushFile(String, String)
	 * @see CMStub#pushFile(String, String, byte)
	 */
	public boolean requestFile(String strFileName, String strFileOwner)
	{
		boolean bReturn = false;
		bReturn = requestFile(strFileName, strFileOwner, CMInfo.FILE_DEFAULT);
		return bReturn;
	}
	
	/**
	 * Requests to transfer a file from a owner (pull mode).
	 * 
	 * <p> A client or a server can request a file from an owner node. The owner can also 
	 * be a client or server.
	 * <br> If the requester calls this method, the file owner receives the request event 
	 * ({@link CMFileEvent#REQUEST_PERMIT_PULL_FILE}). The file owner then accepts or rejects 
	 * to send the requested file automatically or manually. If the PERMIT_FILE_TRANSFER 
	 * field of the owner's CM configuration file (cm-server.conf or cm-client.conf) is 
	 * set to 1, the owner automatically accepts the request and proceeds the next step 
	 * to send the file. If the PERMIT_FILE_TRANSFER field is set to 0, the owner 
	 * application should notify CM of the answer of the request by calling 
	 * {@link CMStub#replyEvent(CMEvent, int)}. The second parameter of this method 
	 * indicates the answer. If the parameter value is 1, the request is accepted. If 
	 * the parameter is 0, the request is rejected. If the requested file does not exist 
	 * in the owner, the reply parameter is set to -1.
	 * <br> The requester application can catch the reply event 
	 * ({@link CMFileEvent#REPLY_PERMIT_PULL_FILE}) to check whether the request is 
	 * accepted or not.
	 * <p> CM uses two strategies in the file-transfer service. If the FILE_TRANSFER_SCHEME 
	 * field of the server configuration file (cm-server.conf) is set to 0, CM uses the 
	 * default communication channel between a sender and a receiver to transfer a file. 
	 * If both the sender and the receiver are clients in the client-server model, then 
	 * the server relays all the events required to transfer a file.
	 * <br> If the FILE_TRANSFER_SCHEME field is set to 1, CM uses a separate and dedicated 
	 * communication channel and thread to transfer a file. The dedicated channel is 
	 * directly connected between the sender and the receiver before the file-transfer task. 
	 * If a receiver node is located in a different private network, a sender cannot make 
	 * a dedicated channel and cannot transfer a file.
	 * <br> According to the file-transfer method, CM nodes exchanges different CM events. 
	 * An easy way to figure out to which file-transfer method a CM event belongs is to 
	 * check whether the event name ends with "CHAN" or "CHAN_ACK". If so, such an event belongs 
	 * to the file-transfer using a dedicated channel; otherwise, the event belongs to 
	 * the file-transfer using the default channel. 
	 *  
	 * <p> When the requested file is completely transferred, the sender CM sends 
	 * {@link CMFileEvent#END_FILE_TRANSFER} or {@link CMFileEvent#END_FILE_TRANSFER_CHAN} 
	 * events to the requester according to the file-transfer method. 
	 * The requester can catch the completion event in the event handler if it needs to be 
	 * notified when the entire file is transferred.
	 *  
	 * @param strFileName - the requested file name
	 * @param strFileOwner - the file owner name
	 * @param byteFileAppend - the file reception mode
	 * <br> The file reception mode specifies the behavior of the receiver if it already has the entire 
	 * or part of the requested file. CM provides three file reception modes which are default, overwrite, 
	 * and append mode. The byteFileAppend parameter can be one of these three modes which are CMInfo.FILE_DEFAULT(-1), 
	 * CMInfo.FILE_OVERWRITE(0), and CMInfo.FILE_APPEND(1). If the byteFileAppend parameter is CMInfo.FILE_DEFAULT, 
	 * the file reception mode is determined by the FILE_APPEND_SCHEME field of the CM configuration file. 
	 * If the byteFileAppend parameter is set to one of the other two values, the reception mode of this requested file 
	 * does not follow the CM configuration file, but this parameter value. The CMInfo.FILE_OVERWRITE is the overwrite mode 
	 * where the receiver always receives the entire file even if it already has the same file. 
	 * The CMInfo.FILE_APPEND is the append mode where the receiver skips existing file blocks 
	 * and receives only remaining blocks.
	 * @return true if the permit for receiving a file is successfully requested to 
	 * the file owner, or false otherwise.
	 * @see CMStub#requestFile(String, String)
	 * @see CMStub#pushFile(String, String)
	 * @see CMStub#pushFile(String, String, byte)
	 */
	public boolean requestFile(String strFileName, String strFileOwner, byte byteFileAppend)
	{
		boolean bReturn = false;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		
		if(confInfo.getSystemType().equals("CLIENT") && myself.getState() < CMInfo.CM_LOGIN) 
		{
			System.err.println("CMFileTransferManager.requestFile(), Client must log in to the default server.");
			return false;
		}

		bReturn = CMFileTransferManager.requestPermitForPullFile(strFileName, strFileOwner, 
				byteFileAppend, m_cmInfo);
		return bReturn;
	}
	
	/**
	 * Sends a file to a receiver (push mode).
	 * 
	 * <p> This method is the same as calling pushFile(strFilePath, strFileReceiver, 
	 * CMInfo.FILE_DEFAULT) of the {@link CMStub#pushFile(String, String, byte)}.
	 * 
	 * @param strFilePath - the path name of a file to be sent
	 * @param strFileReceiver - the file receiver name
	 * @return true if the permit for pushing a file is successfully requested to 
	 * the receiver, or false otherwise.
	 * @see CMStub#pushFile(String, String, byte)
	 * @see CMStub#requestFile(String, String)
	 * @see CMStub#requestFile(String, String, byte)
	 */
	public boolean pushFile(String strFilePath, String strFileReceiver)
	{
		boolean bReturn = false;
		bReturn = pushFile(strFilePath, strFileReceiver, CMInfo.FILE_DEFAULT);		
		return bReturn;
	}
	
	/**
	 * Sends a file to a receiver (push mode).
	 * 
	 * <p> Unlike the pull mode, in the push mode, a CM client or server can send a file 
	 * to another remote CM client or server if the push-file request is accepted.
	 * <br> If the requester calls this method, the file receiver receives the request 
	 * event ({@link CMFileEvent#REQUEST_PERMIT_PUSH_FILE}). The file receiver then 
	 * accepts or rejects to receive the file automatically or manually. If 
	 * the PERMIT_FILE_TRANSFER field of the receiver's CM configuration file 
	 * (cm-server.conf or cm-client.conf) is set to 1, the receiver automatically accepts 
	 * the request and proceeds the next step to receive the file. 
	 * If the PERMIT_FILE_TRANSFER field is set to 0, the receiver application should 
	 * notify CM of the answer of the request by calling 
	 * {@link CMStub#replyEvent(CMEvent, int)}. The second parameter of this method 
	 * indicates the answer. If the parameter value is 1, the request is accepted. If 
	 * the parameter is 0, the request is rejected.
	 * <br> The requester application can catch the reply event 
	 * ({@link CMFileEvent#REPLY_PERMIT_PUSH_FILE}) to check whether the request is 
	 * accepted or not.
	 * 
	 * <p> CM uses two strategies in the file-transfer service. If the FILE_TRANSFER_SCHEME 
	 * field of the server configuration file (cm-server.conf) is set to 0, CM uses the 
	 * default communication channel between a sender and a receiver to transfer a file. 
	 * If both the sender and the receiver are clients in the client-server model, then 
	 * the server relays all the events required to transfer a file.
	 * <br> If the FILE_TRANSFER_SCHEME field is set to 1, CM uses a separate and dedicated 
	 * communication channel and thread to transfer a file. The dedicated channel is 
	 * directly connected between the sender and the receiver before the file-transfer task. 
	 * If a receiver node is located in a different private network, a sender cannot make 
	 * a dedicated channel and cannot transfer a file.
	 * <br> According to the file-transfer method, CM nodes exchanges different CM events. 
	 * An easy way to figure out to which file-transfer method a CM event belongs is to 
	 * check whether the event name ends with "CHAN" or "CHAN_ACK". If so, such an event belongs 
	 * to the file-transfer using a dedicated channel; otherwise, the event belongs to 
	 * the file-transfer using the default channel.
	 * 
	 * <p> When the receiver receives the file completely from the requester (sender), 
	 * the receiver CM sends {@link CMFileEvent#END_FILE_TRANSFER_ACK} or 
	 * {@link CMFileEvent#END_FILE_TRANSFER_CHAN_ACK} events to the requester 
	 * according to the file-transfer method. 
	 * The requester can catch the completion event in the event handler if it needs to be 
	 * notified when the receiver receives the entire file blocks.
	 *  
	 * @param strFilePath - the path name of a file to be sent
	 * @param strReceiver - the file receiver name
	 * @param byteFileAppend - the file reception mode
	 * <br> The file reception mode specifies the behavior of the receiver if it already has the entire 
	 * or part of the requested file. CM provides three file reception modes which are default, overwrite, 
	 * and append mode. The byteFileAppend parameter can be one of these three modes which are CMInfo.FILE_DEFAULT(-1), 
	 * CMInfo.FILE_OVERWRITE(0), and CMInfo.FILE_APPEND(1). If the byteFileAppend parameter is CMInfo.FILE_DEFAULT, 
	 * the file reception mode is determined by the FILE_APPEND_SCHEME field of the CM configuration file. 
	 * If the byteFileAppend parameter is set to one of the other two values, the reception mode of this requested file 
	 * does not follow the CM configuration file, but this parameter value. The CMInfo.FILE_OVERWRITE is the overwrite mode 
	 * where the receiver always receives the entire file even if it already has the same file. 
	 * The CMInfo.FILE_APPEND is the append mode where the receiver skips existing file blocks 
	 * and receives only remaining blocks.
	 * @return true if the permit for pushing a file is successfully requested to the receiver, or false otherwise.
	 * @see CMStub#pushFile(String, String)
	 * @see CMStub#requestFile(String, String)
	 * @see CMStub#requestFile(String, String, byte)
	 */
	public boolean pushFile(String strFilePath, String strReceiver, byte byteFileAppend)
	{
		boolean bReturn = false;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		
		if(confInfo.getSystemType().equals("CLIENT") && myself.getState() < CMInfo.CM_LOGIN) 
		{
			System.err.println("CMFileTransferManager.pushFile(), Client must log in to "
					+ "the default server.");
			return false;
		}

		//bReturn = CMFileTransferManager.pushFile(strFilePath, strReceiver, m_cmInfo);
		bReturn = CMFileTransferManager.requestPermitForPushFile(strFilePath, strReceiver, 
				byteFileAppend, m_cmInfo);
		return bReturn;
	}
	
	/**
	 * Cancels sending (or pushing) a file.
	 * 
	 * <p> A sender can cancel all of its sending tasks to the receiver by calling this method. 
	 * The file pushing task can be cancelled regardless of the file transfer scheme that is determined 
	 * by the FILE_TRANSFER_SCHEME field of the server CM configuration file. The cancellation is also 
	 * notified to the receiver. The result of the receiver's cancellation is sent to the sender 
	 * as {@link CMFileEvent#CANCEL_FILE_SEND_ACK} or 
	 * {@link CMFileEvent#CANCEL_FILE_SEND_CHAN_ACK} event.
	 * 
	 * @param strReceiver - the receiver name
	 * @return true if the cancellation is succeeded, or false otherwise.
	 * @see CMStub#cancelPullFile(String)
	 */
	public boolean cancelPushFile(String strReceiver)
	{
		boolean bReturn = false;
		bReturn = CMFileTransferManager.cancelPushFile(strReceiver, m_cmInfo);
		return bReturn;
	}
	
	/**
	 * Cancels receiving (or pulling) a file.
	 * 
	 * <p> A receiver can cancel all of its receiving tasks from the sender by calling this method. 
	 * Unlike the cancellation of the file pushing task, the file pulling task can be cancelled 
	 * only if the file transfer scheme uses the separate channel (, that is, 
	 * if the FILE_TRANSFER_SCHEME field is 1 in the server CM configuration file). 
	 * The cancellation is also notified to the sender. The result of the sender's cancellation 
	 * is sent to the receiver as {@link CMFileEvent#CANCEL_FILE_RECV_CHAN_ACK} event.
	 * 
	 * @param strSender - the sender name
	 * @return true if the cancellation is succeeded, or false otherwise.
	 * @see CMStub#cancelPushFile(String) 
	 */
	public boolean cancelPullFile(String strSender)
	{
		boolean bReturn = false;
		bReturn = CMFileTransferManager.cancelPullFile(strSender, m_cmInfo);
		return bReturn;
	}

	/////////////////////////////////////////////////////////////////////
	// network service
	
	// measure synchronously the end-to-end input throughput from the target node to this node
	/**
	 * measures the incoming network throughput from a CM node.
	 * 
	 * <p> A CM application can measure the incoming/outgoing network throughput from/to another CM application 
	 * (server or client).
	 * The incoming network throughput of a CM node A from B measures how many bytes A can receive from B in a second. 
	 * The outgoing network throughput of a CM node A to B measures how many bytes A can send to B.
	 * 
	 * @param strTarget - the target CM node
	 * <br> The target CM node should be directly connected to the calling node.
	 * @return the network throughput value by the unit of Megabytes per second (MBps) 
	 * if successfully measured, or -1 otherwise.
	 * @see CMStub#measureOutputThroughput(String)
	 */
	public float measureInputThroughput(String strTarget)
	{
		boolean bReturn = false;
		float fSpeed = -1;
		long lFileSize = -1;	// the size of a file to measure the transmission delay
		long lTransDelay = -1;
		CMFileTransferInfo fInfo = m_cmInfo.getFileTransferInfo();
		CMEventInfo eInfo = m_cmInfo.getEventInfo();
		CMEventSynchronizer eventSync = eInfo.getEventSynchronizer();
		CMFileEvent replyEvent = null;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		
		bReturn = CMFileTransferManager.requestPermitForPullFile("throughput-test.jpg", 
				strTarget, CMInfo.FILE_OVERWRITE, m_cmInfo);
		
		if(!bReturn)
			return -1;

		eventSync.init();
		if(confInfo.isFileTransferScheme())
			eventSync.setWaitedEvent(CMInfo.CM_FILE_EVENT, CMFileEvent.END_FILE_TRANSFER_CHAN, strTarget);
		else
			eventSync.setWaitedEvent(CMInfo.CM_FILE_EVENT, CMFileEvent.END_FILE_TRANSFER, strTarget);
		
		synchronized(eventSync)
		{
			try {
				eventSync.wait(20000);					
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			replyEvent = (CMFileEvent) eventSync.getReplyEvent();
			if(replyEvent == null)
			{
				System.err.println("CMStub.measureInputThroughput(), timeout expired!");
				CMFileTransferManager.cancelPullFile(strTarget, m_cmInfo);
				return -1;
			}
			
			lFileSize = replyEvent.getFileSize();
		}
		
		if(replyEvent.getID() == CMFileEvent.REPLY_PERMIT_PULL_FILE)
		{
			return -1;
		}
				
		lTransDelay = fInfo.getEndRecvTime() - fInfo.getStartRecvTime();	// millisecond
		fSpeed = ((float)lFileSize / 1000000) / ((float)lTransDelay / 1000);	// MBps
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMStub.measureInputThroughput(); received file size("
					+lFileSize+"), delay("+lTransDelay+" ms), speed("+fSpeed+" MBps)");
		}

		return fSpeed;
	}
	
	/**
	 * measures the outgoing network throughput to a CM node.
	 * 
	 * @param strTarget - the target CM node
	 * @return the network throughput value by the unit of Megabytes per second (MBps) 
	 * if successfully measured, or -1 otherwise.
	 * @see CMStub#measureInputThroughput(String)
	 */
	// measure synchronously the end-to-end output throughput from this node to the target node
	public float measureOutputThroughput(String strTarget)
	{
		boolean bReturn = false;
		float fSpeed = -1;
		long lFileSize = -1;	// the size of a file to measure the transmission delay
		long lTransDelay = -1;
		CMFileTransferInfo fInfo = m_cmInfo.getFileTransferInfo();
		CMEventInfo eInfo = m_cmInfo.getEventInfo();
		CMEventSynchronizer eventSync = eInfo.getEventSynchronizer();
		CMFileEvent replyEvent = null;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		String strFilePath = confInfo.getTransferedFileHome().toString() + File.separator + "throughput-test.jpg";
		
		//bReturn = CMFileTransferManager.pushFile(strFilePath, strTarget, CMInfo.FILE_OVERWRITE, m_cmInfo);
		bReturn = CMFileTransferManager.requestPermitForPushFile(strFilePath, strTarget, 
				CMInfo.FILE_OVERWRITE, -1, m_cmInfo);
		
		if(!bReturn)
			return -1;
	
		eventSync.init();
		if(confInfo.isFileTransferScheme())
			eventSync.setWaitedEvent(CMInfo.CM_FILE_EVENT, CMFileEvent.END_FILE_TRANSFER_CHAN_ACK, strTarget);
		else
			eventSync.setWaitedEvent(CMInfo.CM_FILE_EVENT, CMFileEvent.END_FILE_TRANSFER_ACK, strTarget);
		
		synchronized(eventSync)
		{
			try {
				eventSync.wait(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			replyEvent = (CMFileEvent) eventSync.getReplyEvent();
			if(replyEvent == null)
			{
				System.err.println("CMStub.measureOutputThroughput(), timeout expired!");
				CMFileTransferManager.cancelPushFile(strTarget, m_cmInfo);
				return -1;
			}
			
			lFileSize = replyEvent.getFileSize();
		}
				
		lTransDelay = fInfo.getEndSendTime() - fInfo.getStartSendTime();	// millisecond
		fSpeed = ((float)lFileSize / 1000000) / ((float)lTransDelay / 1000);	// MBps
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMStub.measureOutputThroughput(); received file size("+lFileSize+"), delay("
					+lTransDelay+" ms), speed("+fSpeed+" MBps)");
		}

		return fSpeed;
	}
	
	/**
	 * gets the string representation of current channels information.
	 * 
	 * <p> This method in the CMStub class gets only the blocking/non-blocking datagram channel information.
	 * The {@link CMClientStub#getCurrentChannelInfo()} and {@link CMServerStub#getCurrentChannelInfo()} 
	 * methods get the current blocking/non-blocking socket channel information as well as the datagram channels.
	 * 
	 * @return string of current channels information if successful, or null otherwise.
	 * 
	 * @see CMClientStub#getCurrentChannelInfo()
	 * @see CMServerStub#getCurrentChannelInfo()
	 */
	public String getCurrentChannelInfo()
	{
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		StringBuffer sb = null;
		String strNonBlockDCMap = null;
		String strBlockDCMap = null;
		CMChannelInfo<Integer> nonBlockDCMap = commInfo.getNonBlockDatagramChannelInfo();
		CMChannelInfo<Integer> blockDCMap = commInfo.getBlockDatagramChannelInfo();
		ServerSocketChannel ssc = commInfo.getNonBlockServerSocketChannel();

		// non-blocking datagram channel info
		strNonBlockDCMap = nonBlockDCMap.toString();
		strBlockDCMap = blockDCMap.toString();

		if( strNonBlockDCMap == null && strBlockDCMap == null)
			return null;
		
		sb = new StringBuffer();
		
		if(strNonBlockDCMap != null)
		{
			sb.append("-- non-blocking datagram channel\n");
			sb.append(strNonBlockDCMap);
		}
		
		// blocking datagram channel info
		if(strBlockDCMap != null)
		{
			sb.append("-- blocking datagram channel\n");
			sb.append(strBlockDCMap);
		}
		
		// add server socket channel info
		if(ssc != null)
		{
			sb.append("==== server socket channel\n");
			sb.append(commInfo.getNonBlockServerSocketChannel().toString()+"\n");	
		}
		
		return sb.toString();
	}
	
	/**
	 * sets a path to the CM configuration file.
	 * 
	 * <p> After calling this method, when CM needs to access the configuration file, 
	 * CM finds it (cm-server.conf for the server CM and cm-client.conf for the client CM) in this path. 
	 *  
	 * @param homePath the path to the CM configuration file
	 * @see CMStub#getConfigurationHome()
	 */
	public void setConfigurationHome(Path homePath)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		confInfo.setConfFileHome(homePath);
	}
	
	/**
	 * gets the path to the CM configuration file.
	 * 
	 * <p>If the CM client needs to access the configuration file, the path to it can be retrieved as follows:
	 * <br><center> cmClientStub.getConfigurationHome().resolve("cm-client.conf"); </center>
	 * <br> cmClientStub is the reference to a CM client-stub object.
	 * <br> If the CM server needs to access its configuration file, the path to it can be retrieved as follows:
	 * <p><center> cmServerStub.getConfigurationHome().resolve("cm-server.conf"); </center>
	 * <br> cmServerStub is the reference to a CM server-stub object. 
	 * 
	 * @return the path to the CM configuration file
	 * @see CMStub#setConfigurationHome(Path)
	 */
	public Path getConfigurationHome()
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		
		return confInfo.getConfFileHome();
	}
	
}
