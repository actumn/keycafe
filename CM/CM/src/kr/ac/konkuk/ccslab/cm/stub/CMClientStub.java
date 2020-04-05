package kr.ac.konkuk.ccslab.cm.stub;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import kr.ac.konkuk.ccslab.cm.entity.CMChannelInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMPosition;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEventSynchronizer;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMEventInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.info.CMThreadInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMGroupManager;
import kr.ac.konkuk.ccslab.cm.manager.CMInteractionManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttach;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContent;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;
import kr.ac.konkuk.ccslab.cm.thread.CMOpenChannelTask;
import kr.ac.konkuk.ccslab.cm.thread.CMRemoveChannelTask;
import kr.ac.konkuk.ccslab.cm.util.CMUtil;

/**
 * This class provides APIs, through which a client developer can access most of the communication 
 * services of CM.
 * A client application can use this class in order to request client-specific communication services.
 * 
 * @author CCSLab, Konkuk University
 * @see CMStub 
 * @see CMServerStub
 */
public class CMClientStub extends CMStub {

	/**
	 * Creates an instance of the CMClientStub class.
	 * 
	 * <p> This method just called the default constructor of the super class, CMStub. 
	 */
	public CMClientStub()
	{
		super();
	}

	/**
	 * Sets the default file path for file transfer.
	 * <br> This method updates the transfered-file path information in CM and the FILE_PATH field in 
	 * the CM configuration file.
	 * 
	 * <p> CM applications that directly connect to each other can exchange a file with the CMStub class 
	 * that is the parent class of the CMClientStub and the CMServerStub classes. In the client-server architecture, 
	 * a client can push or pull a file to/from a server and vice versa. When CM is initialized by an application, 
	 * the default directory is configured by the path information that is set in the configuration file 
	 * (the FILE_PATH field). If the default directory does not exist, CM creates it. If the FILE_PATH field is not set, 
	 * the default path is set to the current working directory (".").
	 * <p> If the file transfer is requested, a sender (the server or the client) searches for the file 
	 * in the default file path. If a client receives a file, CM stores the file in this file path. 
	 * If a server receives a file, CM stores the file in a sub-directory of the default path. 
	 * The sub-directory name is a sender (client) name.
	 * 
	 * @param dir - the file path
	 * @return true if the file path is successfully updated both in the CMConfigurationInfo class and 
	 * the configuration file (cm-client.conf), or false otherwise.
	 * @see CMStub#getTransferedFileHome()
	 */
	public boolean setTransferedFileHome(Path dir)
	{
		// to set in the CMConfigurationInfo class.
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		confInfo.setTransferedFileHome(dir);
		// to set in the CM configuration file.
		boolean bRet = false;
		
		Path confPath = getConfigurationHome();
		confPath = confPath.resolve("cm-client.conf");
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), "FILE_PATH", dir.toString());
		
		return bRet;
	}
	
	/**
	 * Sets server address to the client configuration file.
	 * 
	 * <p> This method must be called before an application starts CM because it updates the value of "SERVER_ADDR" 
	 * field in the client configuration file (cm-client.conf).
	 * 
	 * 
	 * @param strAddress - the server IP address
	 * @return true if the server address is successfully set, or false otherwise.
	 * @see CMClientStub#setServerPort(int)
	 * @see CMClientStub#setServerInfo(String, int)
	 */
	public boolean setServerAddress(String strAddress)
	{
		boolean bRet = false;
		
		// check the current CM state
		if(m_cmInfo.isStarted())
		{
			System.err.println("CMClientStub.setServerAddress(), CM already has started!");
			return false;
		}
		
		// get the configuration file path
		Path confPath = getConfigurationHome().resolve("cm-client.conf");
		
		// set the server address
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), "SERVER_ADDR", strAddress);
		
		return bRet;
	}
	
	/**
	 * Gets server address from the client configuration file.
	 * 
	 * @return server address
	 * @see CMClientStub#setServerAddress(String)
	 */
	public String getServerAddress()
	{
		String strServerAddress = null;
		Path confPath = getConfigurationHome().resolve("cm-client.conf");
		strServerAddress = CMConfigurator.getConfiguration(confPath.toString(), "SERVER_ADDR");
		return strServerAddress;
	}
	
	/**
	 * Sets server port number to the client configuration file.
	 * 
	 * <p> This method must be called before an application starts CM because it updates the value of "SERVER_PORT" 
	 * field in the client configuration file (cm-client.conf). 
	 * 
	 * @param nPort - the server port number
	 * @return true if the server port is successfully set, or false otherwise.
	 * @see CMClientStub#setServerAddress(String)
	 * @see CMClientStub#setServerInfo(String, int)
	 */
	public boolean setServerPort(int nPort)
	{
		boolean bRet = false;
		
		// check the current CM state
		if(m_cmInfo.isStarted())
		{
			System.err.println("CMClientStub.setServerPort(), CM already has started!");
			return false;
		}
		
		// get the configuration file path
		Path confPath = getConfigurationHome().resolve("cm-client.conf");
				
		// set the server address
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), "SERVER_PORT", String.valueOf(nPort));
		
		return bRet;
	}
	
	/**
	 * Gets server port number from the client configuration file.
	 * 
	 * @return server port number
	 * @see CMClientStub#setServerPort(int)
	 */
	public int getServerPort()
	{
		int nServerPort = -1;
		Path confPath = getConfigurationHome().resolve("cm-client.conf");
		nServerPort = Integer.parseInt(CMConfigurator.getConfiguration(confPath.toString(), "SERVER_PORT"));
		return nServerPort;
	}
	
	/**
	 * Sets server address and port number to the client configuration file.
	 * 
	 * <p> This method must be called before an application starts CM because it updates the values of "SERVER_ADDR"  
	 * and "SERVER_PORT" fields in the client configuration file (cm-client.conf). 
	 * 
	 * @param strAddress - the server IP address
	 * @param nPort - the server port number
	 * @return true if the server address and port number are successfully set, or false otherwise.
	 * @see CMClientStub#setServerAddress(String)
	 * @see CMClientStub#setServerPort(int)
	 */
	public boolean setServerInfo(String strAddress, int nPort)
	{
		boolean bRet = false;
		
		bRet = setServerAddress(strAddress);
		if(!bRet) return false;
		
		bRet = setServerPort(nPort);
		if(!bRet) return false;
		
		return bRet;
	}


	/**
	 * Initializes and starts the client CM.
	 * 
	 * <p> Before the server CM starts, it initializes the configuration and the interaction manager. Then, 
	 * it starts two separate threads for receiving and processing CM events.
	 * <br> After the initialization process, the client CM also establishes a stream(TCP) connection to 
	 * the default server and makes a default datagram(UDP) channel.
	 * 
	 *  
	 * @return true if the initialization of CM succeeds, or false if the initialization of CM fails.
	 * @see CMClientStub#terminateCM()
	 */
	public boolean startCM()
	{
		super.init();	// initialize CMStub
		
		boolean bRet = false;
		
		/*
		if(m_cmInfo.isStarted())
		{
			System.err.println("CMClientStub.startCM(), already started!");
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
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMClientStub.startCM(), executor service created; # available processors("
					+nAvailableProcessors+").");
		}
		*/
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();

		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		// initialize CMConfigurator
		String strConfPath = m_cmInfo.getConfigurationInfo().getConfFileHome().resolve("cm-client.conf").toString();
		
		Callable<Boolean> task = new Callable<Boolean>() {
			@Override
			public Boolean call()
			{
				boolean ret = CMConfigurator.init(strConfPath, m_cmInfo);
				return ret;
			}
		};
		Future<Boolean> future = es.submit(task);
		try {
			bRet = future.get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(!bRet)
			return false;

		// initialize CMInteractionManager
		task = new Callable<Boolean>() {
			@Override
			public Boolean call()
			{
				boolean ret = CMInteractionManager.init(m_cmInfo);
				return ret;
			}
		};
		future = es.submit(task);
		try {
			bRet = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(!bRet)
			return false;

		//////////

		// start processing, sending, and receiving threads
		CMEventManager.startReceivingEvent(m_cmInfo);
		CMCommManager.startReceivingMessage(m_cmInfo);
		CMCommManager.startSendingMessage(m_cmInfo);
		
		m_cmInfo.setStarted(true);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMClientStub.startCM(), succeeded.");
		
		return true;
	}
	
	/**
	 * Terminates the client CM.
	 * <br>A client application calls this method when it does not need to use CM. The client releases all 
	 * the resources, logs out from the server, and disconnects all communication channels.
	 * 
	 * @see CMClientStub#startCM()
	 */
	public void terminateCM()
	{
		disconnectFromServer();
		
		super.terminateCM();

		/*
		CMThreadInfo threadInfo = m_cmInfo.getThreadInfo();
		ExecutorService es = threadInfo.getExecutorService();
		es.shutdown();	// need to check
		*/

		if(CMInfo._CM_DEBUG)
			System.out.println("CMClientStub.terminateCM(), succeeded.");
	}
	
	/**
	 * Connects to the default server.
	 * 
	 * <p> When a client application calls this method, the client CM opens a default stream(TCP)
	 * channel and connects to the server CM used by the default server application.
	 * <br> When the CM client starts by calling the {@link CMClientStub#startCM()} method, it connects 
	 * to the default server ("SERVER") as one of the initialization tasks.
	 * Before the client logs in to the default CM server, it must be connected to the server by calling this method.
	 * The connection to the default server is made with the default TCP channel.
	 * 
	 * @return true if the connection is established successfully, or false otherwise.
	 * @see CMClientStub#connectToServer(String)
	 * @see CMClientStub#disconnectFromServer()
	 */
	public boolean connectToServer()
	{
		// If CM has been terminated, it must start and this task includes the connection task as well.
		if(!m_cmInfo.isStarted())
		{
			return startCM();
		}
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		Callable<Boolean> task = new Callable<Boolean>() {
			@Override
			public Boolean call()
			{
				Boolean bRet = CMInteractionManager.connectDefaultServer(m_cmInfo);
				return bRet;
			}
		};
		
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<Boolean> future = es.submit(task);
		boolean bRet = false;
		try {
			bRet = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//////////
		
		//return CMInteractionManager.connectDefaultServer(m_cmInfo);
		return bRet;
		
	}
	
	/**
	 * Disconnects from the default server.
	 * <br> When a client application calls this method, the client CM tries to disconnect all the  
	 * stream(TCP) channels from the server CM used by the default server application.
	 * 
	 * @return true if the connection is successfully disconnected, or false otherwise.
	 * @see CMClientStub#disconnectFromServer(String)
	 * @see CMClientStub#connectToServer()
	 */
	public boolean disconnectFromServer()
	{
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		Callable<Boolean> task = new Callable<Boolean>() {
			@Override
			public Boolean call()
			{
				Boolean bRet = CMInteractionManager.disconnectFromDefaultServer(m_cmInfo);
				return bRet;
			}
		};
		
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<Boolean> future = es.submit(task);
		boolean bRet = false;
		try {
			bRet = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//////////

		//return CMInteractionManager.disconnectFromDefaultServer(m_cmInfo);
		
		return bRet;
	}
	
	/**
	 * Logs in to the default server.
	 * <br> For logging in to the server, the client first needs to register to the server by calling 
	 * registerUser() method.
	 * <p> The result of the login request can be caught asynchronously by the client event handler 
	 * that deals with all the incoming CM events from the server. To check whether the login request is 
	 * successful or not, the client event handler needs to catch 
	 * the {@link CMSessionEvent#LOGIN_ACK} event.
	 * In the LOGIN_ACK event, a result field of the Integer type is set, and the value can be retrieved by 
	 * the {@link CMSessionEvent#isValidUser()} method. If the value is 1, the login request successfully completes 
	 * and the requesting client is in the CM_LOGIN state. Otherwise, the login process fails.
	 * The LOGIN_ACK event also includes other CM information that can be returned by 
	 * {@link CMSessionEvent#getCommArch()}, {@link CMSessionEvent#isLoginScheme()}, and 
	 * {@link CMSessionEvent#isSessionScheme()}.
	 * 
	 * <p> When the server CM accepts the login request from a client, the server CM also notifies other 
	 * participating clients of the information of the login user with the 
	 * {@link CMSessionEvent#SESSION_ADD_USER} event. 
	 * A client application can catch this event in the event handler routine if it wants to use such 
	 * information. The login user information is the user name and the host address that can be retrieved 
	 * by {@link CMSessionEvent#getUserName()} and {@link CMSessionEvent#getHostAddress()} methods, respectively.
	 * 
	 * @param strUserName - the user name
	 * @param strPassword - the password
	 * @return true if the request is successfully sent to the server; false otherwise.
	 * @see CMClientStub#syncLoginCM(String, String)
	 * @see CMClientStub#loginCM(String, String, String)
	 * @see CMClientStub#logoutCM()
	 * @see CMClientStub#registerUser(String, String)
	 * 
	 */
	public boolean loginCM(String strUserName, String strPassword)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		boolean bRequestResult = false;
		String strEncPassword = null;
		
		// check local state
		int nUserState = getMyself().getState();
		
		// If the user is not connected to the default server, he/she connects to it first.
		if(nUserState == CMInfo.CM_INIT)
		{
			//CMInteractionManager.connectDefaultServer(m_cmInfo);
			if( !connectToServer() )
				return false;
		}
		
		switch( nUserState )
		{
		case CMInfo.CM_LOGIN:
		case CMInfo.CM_SESSION_JOIN:
			System.out.println("You already logged in to the default server."); 
			return false;
		}
		
		String strMyAddr = confInfo.getMyAddress();		// client IP address
		int nMyUDPPort = confInfo.getUDPPort();			// client UDP port
		
		// encrypt the plain password text
		strEncPassword = CMUtil.getSHA1Hash(strPassword);
		
		// make an event
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.LOGIN);
		se.setUserName(strUserName);
		se.setPassword(strEncPassword);
		se.setHostAddress(strMyAddr);
		se.setUDPPort(nMyUDPPort);
		se.setKeepAliveTime(confInfo.getKeepAliveTime());
		
		// set information on the local user
		CMUser myself = getMyself();
		myself.setName(strUserName);
		myself.setPasswd(strEncPassword);
		myself.setHost(strMyAddr);
		myself.setUDPPort(nMyUDPPort);
		
		// send the event
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		bRequestResult = send(se, strDefServer);
		se = null;
		
		// set last event-transmission time of the local user
		myself.setLastEventTransTime(System.currentTimeMillis());
		
		return bRequestResult;
	}
	
	/**
	 * Logs in to the default server synchronously.
	 * 
	 * <p> Unlike the asynchronous login method ({@link CMClientStub#loginCM(String, String)}), 
	 * this method makes the main thread of the client block its execution until it receives and 
	 * returns the reply event ({@link CMSessionEvent#LOGIN_ACK}) from the default server.
	 * <br> For the other detailed information of the login process, please refer to 
	 * the asynchronous login method.
	 * 
	 * @param strUserName - the user name
	 * @param strPassword - the password
	 * @return the reply event (CMSessionEvent.LOGIN_ACK) from the default server.
	 * @see CMClientStub#loginCM(String, String)
	 */
	public CMSessionEvent syncLoginCM(String strUserName, String strPassword)
	{
		CMEventSynchronizer eventSync = m_cmInfo.getEventInfo().getEventSynchronizer();
		CMSessionEvent loginAckEvent = null;
		boolean bRequestResult = false;
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		
		bRequestResult = loginCM(strUserName, strPassword);
		if(!bRequestResult) return null;

		eventSync.init();
		eventSync.setWaitedEvent(CMInfo.CM_SESSION_EVENT, CMSessionEvent.LOGIN_ACK, 
				strDefServer);
		synchronized(eventSync)
		{
			try {
				eventSync.wait(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			loginAckEvent = (CMSessionEvent) eventSync.getReplyEvent();
		}
		
		return loginAckEvent;
	}
	
	/**
	 * Logs out from the default server.
	 * 
	 * <p> There is no result from the server about the logout request. 
	 * <p> When the server CM completes the logout request from a client, the server CM also notifies 
	 * other participating clients of the information of the logout user with 
	 * the {@link CMSessionEvent#SESSION_REMOVE_USER} event.
	 * A client application can catch this event in the event handler routine if it wants to use 
	 * such information. The logout user information is just the user name, which can be returned by 
	 * {@link CMSessionEvent#getUserName()} method.
	 * 
	 * @return true if successfully sent the logout request, false otherwise.
	 * @see CMClientStub#loginCM(String, String)
	 * @see CMClientStub#deregisterUser(String, String)
	 */
	public boolean logoutCM()
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		boolean bRequestResult = false;
		
		// check state of the local user
		CMUser myself = getMyself();
		switch(myself.getState())
		{
		case CMInfo.CM_INIT:
			System.out.println("You should connect and log in to the default server."); return false;
		case CMInfo.CM_CONNECT:
			System.out.println("You should log in to the default server."); return false;
		}
		
		// terminate current group info (multicast channel, group member, Membership key)
		CMGroupManager.terminate(myself.getCurrentSession(), myself.getCurrentGroup(), m_cmInfo);

		// close and remove all additional channels to the default server
		interInfo.getDefaultServerInfo().getNonBlockSocketChannelInfo().removeAllAddedChannels(0);
		interInfo.getDefaultServerInfo().getBlockSocketChannelInfo().removeAllChannels();
		
		// make and send an LOGOUT event
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.LOGOUT);
		se.setUserName(myself.getName());
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		bRequestResult = send(se, strDefServer);
		
		// update local state
		if(bRequestResult)
			myself.setState(CMInfo.CM_CONNECT);
		
		if(bRequestResult)
			System.out.println("["+myself.getName()+"] successfully sent the logout request to the default server.");
		else
			System.err.println("["+myself.getName()+"] failed the logout request!");

		// check and stop the scheduled keep-alive task
		if(CMInteractionManager.getNumLoginServers(m_cmInfo) == 0)
		{
			CMThreadInfo threadInfo = m_cmInfo.getThreadInfo();
			ScheduledFuture<?> future = threadInfo.getScheduledFuture();
			if(future != null){
				future.cancel(true);
				
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMClientStub.logoutCM(), stop the client keep-alive task.");
				}				
			}
		}
		
		se = null;
		return bRequestResult;
	}
	
	/**
	 * Requests available session information from the default server.
	 * <br> For requesting the session information, the client first needs to log in to the server by calling 
	 * loginCM() method. 
	 * 
	 * <p> The result of the session request can be caught asynchronously by the client event handler 
	 * that deals with all the incoming CM events from the server. To receive the available session 
	 * information, the client event handler needs to catch 
	 * the {@link CMSessionEvent#RESPONSE_SESSION_INFO} event.
	 * <br> The RESPONSE_SESSION_INFO event includes the number of available sessions and the vector of 
	 * the {@link CMSessionInfo}. Such event fields can be returned by 
	 * the {@link CMSessionEvent#getSessionNum()} and {@link CMSessionEvent#getSessionInfoList()}.
	 * <br> Each element of the CMSessionInfo object includes information of an available session such as 
	 * the session name, the session address and port number to which a client can join, and the current 
	 * number of session members who already joined the session.
	 * 
	 * @return true if the request is successfully sent to the server; false otherwise.
	 * @see CMClientStub#syncRequestSessionInfo()
	 * @see CMClientStub#joinSession(String)
	 * @see CMClientStub#joinSession(String, String)
	 */
	// request available session information from the default server
	public boolean requestSessionInfo()
	{
		boolean bRequestResult = false;
		
		// check local state
		int nUserState = getMyself().getState();
		if(nUserState == CMInfo.CM_INIT || nUserState == CMInfo.CM_CONNECT)
		{
			System.out.println("CMClientStub.requestSessionInfo(), you should log in to the default server.");
			return false;
		}
		
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.REQUEST_SESSION_INFO);
		se.setUserName(getMyself().getName());
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		bRequestResult = send(se, strDefServer);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMClientStub.requestSessionInfo(), end.");
		se = null;
		return bRequestResult;
	}
	
	/**
	 * Requests available session information from the default server synchronously.
	 * 
	 * <p> Unlike the asynchronous method ({@link CMClientStub#requestSessionInfo()}), this method makes 
	 * the main thread of the client block its execution until it receives and returns the reply event 
	 * ({@link CMSessionEvent#RESPONSE_SESSION_INFO}) from the default server.
	 * <br> For the other detailed information of the session-information-request process, 
	 * please refer to the asynchronous version of the request. 
	 * 
	 * @return the reply event ({@link CMSessionEvent#RESPONSE_SESSION_INFO}) from the default server.
	 * @see CMClientStub#requestSessionInfo()
	 */
	public CMSessionEvent syncRequestSessionInfo()
	{
		CMEventSynchronizer eventSync = m_cmInfo.getEventInfo().getEventSynchronizer();
		CMSessionEvent replyEvent = null;
		boolean bRequestResult = false;
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		
		bRequestResult = requestSessionInfo();
		if(!bRequestResult) return null;

		eventSync.init();
		eventSync.setWaitedEvent(CMInfo.CM_SESSION_EVENT, 
				CMSessionEvent.RESPONSE_SESSION_INFO, strDefServer);
		synchronized(eventSync)
		{
			try {
				eventSync.wait(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			replyEvent = (CMSessionEvent) eventSync.getReplyEvent();
		}
		
		return replyEvent;
	}

	/**
	 * Joins a session in the default server.
	 * <br> For joining a session, the client first needs to log in to the server by calling 
	 * loginCM() method. The client can get available session information by calling 
	 * the requestSessionInfo() method.
	 * 
	 * <p> After the login process has completed, a client application must join a session and a group of 
	 * CM to finish entering the CM network. The session join process is different according to whether 
	 * the server CM adopts single session or multiple sessions in the CM server configuration file 
	 * (cm-server.conf).
	 * <p> When the server CM receives the join-session request, it sends back the client a reply event 
	 * ({@link CMSessionEvent#JOIN_SESSION_ACK}) that contains the group information in the session. If the reply 
	 * event does not have any group information, it implies that the join-session task fails because 
	 * every session in CM must has at least one group.
	 * 
	 * <p> After the client CM completes to join a session, it automatically proceeds to enter the first 
	 * group of the session. For example, if the client joins &quot;session1&quot;, it also enters the group, &quot;g1&quot;
	 * that is the first group of the session, &quot;session1&quot;.
	 * 
	 * <p> When the server CM completes the session joining request from a client, the server CM also 
	 * notifies other participating clients of the information of the new session user with 
	 * the {@link CMSessionEvent#CHANGE_SESSION} event. A client application can catch this event 
	 * in the event handler routine if it wants to use such information. The CHANGE_SESSION event includes 
	 * fields such as the user name and the session name, which can be returned by calling 
	 * the {@link CMSessionEvent#getUserName()} and the {@link CMSessionEvent#getSessionName()} methods, 
	 * respectively.
	 * 
	 * <p> When the server CM completes the group joining request from a client, the server CM also notifies 
	 * other participating clients of the information of the new group user with 
	 * the {@link CMDataEvent#NEW_USER} event.
	 * When the client CM receives this event, it stores the information of a new group user so that it 
	 * can figure out current group members later. A client application also can catch this event in 
	 * the event handler routine if it wants to use such information. The NEW_USER event includes fields 
	 * such as the current session name, the current group name, the name of the new group user, the host 
	 * address of the new group user, and the UDP port number of the new group user. Each event field can be 
	 * returned by calling the {@link CMDataEvent#getHandlerSession()}, {@link CMDataEvent#getHandlerGroup()}, 
	 * {@link CMDataEvent#getUserName()}, {@link CMDataEvent#getHostAddress()}, 
	 * and {@link CMDataEvent#getUDPPort()} methods, respectively.
	 * 
	 * <p> When the server CM completes the group joining request from a client, the server CM also notifies 
	 * the new user of the information of other existing group users with the series of 
	 * {@link CMDataEvent#INHABITANT} events.
	 * When the client CM receives this event, it stores the information of an existing group user so that 
	 * it can figure out current group members later. A client application also can catch this event 
	 * in the event handler routine if it wants to use such information. The INHABITANT event includes fields 
	 * such as the current session name, the current group name, the name of the new group user, the host 
	 * address of the new group user, and the UDP port number of the new group user. Each event field can be 
	 * returned by calling the {@link CMDataEvent#getHandlerSession()}, {@link CMDataEvent#getHandlerGroup()}, 
	 * {@link CMDataEvent#getUserName()}, {@link CMDataEvent#getHostAddress()}, 
	 * and {@link CMDataEvent#getUDPPort()} methods, respectively.
	 * 
	 * @param sname - the session name that a client requests to join
	 * @return true if the request is successful; false otherwise.
	 * @see CMClientStub#syncJoinSession(String)
	 * @see CMClientStub#joinSession(String, String)
	 * @see CMClientStub#leaveSession()
	 * @see CMClientStub#leaveSession(String)
	 */
	public boolean joinSession(String sname)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		boolean bResult = false;
		
		// check local state
		switch( getMyself().getState() )
		{
		case CMInfo.CM_INIT:
			System.out.println("You should connect and login server before session join.\n"); return false;
		case CMInfo.CM_CONNECT:
			System.out.println("You should login server before session join..\n"); return false;
		case CMInfo.CM_SESSION_JOIN:
			System.out.println("You have already joined a session.\n"); return false;
		}
		
		// check selected session
		if( !interInfo.isMember(sname) )
		{
			System.out.println("session("+sname+") not found. You can request session information"
					+" from the default server.");
			return false;
		}

		// make and send an event
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.JOIN_SESSION);
		se.setHandlerSession(sname);
		se.setUserName(getMyself().getName());
		se.setSessionName(sname);
		
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		bResult = send(se, strDefServer);
		if(bResult)
			getMyself().setCurrentSession(sname);
		
		se = null;
		return bResult;
	}
	
	/**
	 * Joins a session in the default server synchronously.
	 * 
	 * <p> Unlike the asynchronous method ({@link CMClientStub#joinSession(String)}), this method makes 
	 * the main thread of the client block its execution until it receives and returns the reply event 
	 * ({@link CMSessionEvent#JOIN_SESSION_ACK}) from the default server.
	 * <br> For the other detailed information of the session-join process, please refer to the asynchronous 
	 * version of the request.  
	 * 
	 * @param sname - the session name that a client requests to join
	 * @return the reply event ({@link CMSessionEvent#JOIN_SESSION_ACK}) from the default server, null if the request fails.
	 * @see CMClientStub#joinSession(String)
	 */
	public CMSessionEvent syncJoinSession(String sname)
	{
		CMEventSynchronizer eventSync = m_cmInfo.getEventInfo().getEventSynchronizer();
		CMSessionEvent replyEvent = null;
		boolean bRequestResult = false;
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		
		bRequestResult = joinSession(sname);
		if(!bRequestResult) return null;

		eventSync.init();
		eventSync.setWaitedEvent(CMInfo.CM_SESSION_EVENT, CMSessionEvent.JOIN_SESSION_ACK, 
				strDefServer);
		synchronized(eventSync)
		{
			try {
				eventSync.wait(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			replyEvent = (CMSessionEvent) eventSync.getReplyEvent();
		}
		
		return replyEvent;		
	}
	
	/**
	 * Leaves the current session in the default server.
	 * 
	 * <p> There is no result from the server about the session-leave request.
	 * 
	 * <p> Before leaving the current session, the server first remove the client from its current group. 
	 * The server notifies group members of the user leave by sending 
	 * the {@link CMDataEvent#REMOVE_USER} event. The REMOVE_USER event includes the user name field, 
	 * which can be returned by the {@link CMDataEvent#getUserName()} method.
	 * 
	 * <p> When the server CM completes the session leaving request from a client, the server CM also 
	 * notifies other participating clients of the information of the leaving user with 
	 * the {@link CMSessionEvent#CHANGE_SESSION} event. 
	 * A client application can catch this event in the event handler 
	 * routine if it wants to use such information. The CHANGE_SESSION event includes 
	 * fields such as the user name and the session name, which can be returned by calling 
	 * the {@link CMSessionEvent#getUserName()} and the {@link CMSessionEvent#getSessionName()} methods, 
	 * respectively.
	 * If the session name field of this event is an empty space, a client can know that the user leaves 
	 * his/her current session. 
	 * 
	 * @return true if successfully sent the leave-session request, false otherwise.
	 * @see CMClientStub#leaveSession(String)
	 * @see CMClientStub#joinSession(String)
	 * @see CMClientStub#joinSession(String, String)
	 */
	public boolean leaveSession()
	{
		boolean bRequestResult = false;
		CMUser myself = getMyself();
		// check local state
		switch(myself.getState())
		{
		case CMInfo.CM_INIT:
			System.out.println("You should connect, log in to the default server, and join a session."); 
			return false;
		case CMInfo.CM_CONNECT:
			System.out.println("You should log in to the default server and join a session.");
			return false;
		case CMInfo.CM_LOGIN:
			System.out.println("You should join a session."); return false;
		}
		
		// terminate current group info (multicast channel, group member, Membership key)
		CMGroupManager.terminate(myself.getCurrentSession(), myself.getCurrentGroup(), m_cmInfo);
		
		// send the leave request to the default server
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.LEAVE_SESSION);
		se.setHandlerSession(myself.getCurrentSession());
		se.setUserName(myself.getName());
		se.setSessionName(myself.getCurrentSession());
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		bRequestResult = send(se, strDefServer);
		
		// update the local state
		myself.setState(CMInfo.CM_LOGIN);
		
		if(bRequestResult)
			System.out.println("["+myself.getName()+"] successfully requested to leave session("+myself.getCurrentSession()+").");
		else
			System.err.println("["+myself.getName()+"] failed the leave-session request!");
		
		se = null;
		return bRequestResult;
	}
	
	/**
	 * Sends location information of the client to the group members.
	 * 
	 * <p> The location information consists of the position and orientation. The position is represented 
	 * by 3D coordinate (x,y,z). The orientation is represented by the quaternion (x,y,z,w) that includes 
	 * the rotation axis and the rotation angle.  
	 * @param pq - the new position and orientation of the client
	 * @see CMPosition
	 */
	// send position info to the group members
	public void sendUserPosition(CMPosition pq)
	{
		CMUser myself = getMyself();
		// check user's local state
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You should join a session and enter a group.");
			return;
		}
		
		// make and send an event
		CMInterestEvent ie = new CMInterestEvent();
		ie.setID(CMInterestEvent.USER_MOVE);
		ie.setHandlerSession(myself.getCurrentSession());
		ie.setHandlerGroup(myself.getCurrentGroup());
		ie.setDistributionSession(myself.getCurrentSession());
		ie.setDistributionGroup(myself.getCurrentGroup());
		ie.setUserName(myself.getName());
		ie.setPosition(pq);
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(ie, strDefServer);
		
		// update user's current pq
		myself.setPosition(pq);
		return;
	}
	
	/**
	 * Sends a chat event.
	 * <p> A CM application can receive the chat event by catching a pre-defined CM event in the event 
	 * handler like other events. There are two types of CM chat events. One is 
	 * the {@link CMSessionEvent#SESSION_TALK} event. A client can receive this event 
	 * if it at least logs in to the default server. 
	 * The SESSION_TALK event includes fields such as the sender name, the text message, and 
	 * the session name of the sender, which can be returned by calling {@link CMSessionEvent#getUserName()}, 
	 * {@link CMSessionEvent#getTalk()}, and {@link CMSessionEvent#getHandlerSession()} methods, respectively. 
	 * 
	 * <br>The other event is the {@link CMInterestEvent#USER_TALK} event. A client can 
	 * receive this event only if it enters a group. The USER_TALK event includes fields such as the sender 
	 * name, the text message, the session name of the sender, and the group name of the sender, which can 
	 * be returned by calling {@link CMInterestEvent#getUserName()}, {@link CMInterestEvent#getTalk()}, 
	 * {@link CMInterestEvent#getHandlerSession()}, and {@link CMInterestEvent#getHandlerGroup()} methods, 
	 * respectively. 
	 * 
	 * @param strTarget - the receiver name.
	 * <br>This parameter must start with &quot;/&quot; character and it specifies the range of recipients of the chat 
	 * message as described below:
	 * <br> /b - The chat message is sent to the all login users.
	 * <br> /s - The chat message is sent to the all session members of the sending user.
	 * <br> /g - The chat message is sent to the all group members of the sending user.
	 * <br> /name - The chat message is sent to a specific CM node of which name is &quot;name&quot;. The name can be 
	 * another user name or a server name. If &quot;name&quot; is SERVER, the message is sent to the default server.
	 * @param strMessage - the chat message.
	 */
	public void chat(String strTarget, String strMessage)
	{
		CMUser myself = getMyself();
		
		// check target
		if(strTarget.equals("/b"))	// broadcast
		{
			if(myself.getState() == CMInfo.CM_CONNECT || myself.getState() == CMInfo.CM_INIT)
			{
				System.out.println("CMClientStub.chat(), You should log in to the default server"
						+" for broadcasting message.");
				return;
			}
			CMSessionEvent se = new CMSessionEvent();
			se.setID(CMSessionEvent.SESSION_TALK);
			se.setUserName(myself.getName());
			se.setTalk(strMessage);
			broadcast(se);
			se = null;
		}
		else if(strTarget.equals("/s"))	// cast to current session members
		{
			if(myself.getState() != CMInfo.CM_SESSION_JOIN)
			{
				System.out.println("CMClientStub.chat(), You should join a session.");
				return;
			}
			CMSessionEvent se = new CMSessionEvent();
			se.setID(CMSessionEvent.SESSION_TALK);
			se.setHandlerSession(myself.getCurrentSession());
			se.setUserName(myself.getName());
			se.setTalk(strMessage);
			cast(se, myself.getCurrentSession(), null);
			se = null;
		}
		else if(strTarget.equals("/g")) // cast to current group members
		{
			if(myself.getState() != CMInfo.CM_SESSION_JOIN)
			{
				System.out.println("CMClientStub.chat(), You should join a session.");
				return;
			}
			CMInterestEvent ie = new CMInterestEvent();
			ie.setID(CMInterestEvent.USER_TALK);
			ie.setHandlerSession(myself.getCurrentSession());
			ie.setHandlerGroup(myself.getCurrentGroup());
			ie.setUserName(myself.getName());
			ie.setTalk(strMessage);
			cast(ie, myself.getCurrentSession(), myself.getCurrentGroup());
			ie = null;
		}
		else	// send to a user of the current group
		{
			// check if the target user name field starts with '/'
			if(!strTarget.startsWith("/"))
			{
				System.out.println("CMClientStub.chat(), the name of target user must start with \"/\".");
				return;
			}
			
			strTarget = strTarget.substring(1); // without '/'
			CMInterestEvent ie = new CMInterestEvent();
			ie.setID(CMInterestEvent.USER_TALK);
			ie.setHandlerSession(myself.getCurrentSession());
			ie.setHandlerGroup(myself.getCurrentGroup());
			ie.setUserName(myself.getName());
			ie.setTalk(strMessage);
			send(ie, strTarget);
			ie = null;
		}
		
		return;
	}
	
	/**
	 * Changes the current group of the client.
	 * 
	 * <p> When a client calls this method, the client first leaves the current group and then requests to 
	 * enter a new group. The CM server notifies previous group members of the left user by sending 
	 * the {@link CMDataEvent#REMOVE_USER} event, and the server also 
	 * notifies new group members of the new user by sending the {@link CMDataEvent#NEW_USER} event. 
	 * The server also notifies the changing user of the existing member information of the new group by 
	 * sending the {@link CMDataEvent#INHABITANT} event.
	 * 
	 * @param gName - the name of a group that the client wants to enter.
	 * @see CMClientStub#joinSession(String)
	 */
	public void changeGroup(String gName)
	{
		CMGroupManager.changeGroup(gName, m_cmInfo);
		return;
	}
	
	/**
	 * Adds asynchronously a nonblocking (TCP) socket channel to a server.
	 * <br> Only the client can add an additional stream socket (TCP) channel. In the case of the datagram 
	 * and multicast channels, both the client and the server can add an additional non-blocking channel 
	 * with the {@link CMStub#addNonBlockDatagramChannel(int)} and 
	 * {@link CMStub#addMulticastChannel(String, String, String, int)} methods in the CMStub class.
	 * 
	 * <p> Although this method returns the reference to the valid socket channel at the client, it is unsafe 
	 * for the client to use the socket before the server also adds the relevant channel information.
	 * The establishment of a new nonblocking socket channel at both sides (the client and the server) completes 
	 * only when the client receives the ack event ({@link CMSessionEvent#ADD_NONBLOCK_SOCKET_CHANNEL_ACK}) 
	 * from the server and the return code in the event is 1.
	 * The client event handler can catch the ack event.
	 * 
	 * @param nChKey - the channel key that must be greater than 0.
	 * The key 0 is occupied by the default TCP channel.
	 * @param strServer - the server name to which the client adds a TCP channel. The name of the default 
	 * server is 'SERVER'.
	 * @return true if the socket channel is successfully created at the client and requested to add 
	 * the (key, socket) pair to the server. 
	 * <br> False, otherwise.
	 * 
	 * @see CMClientStub#syncAddNonBlockSocketChannel(int, String)
	 * @see CMClientStub#addBlockSocketChannel(int, String)
	 * @see CMClientStub#syncAddBlockSocketChannel(int, String)
	 * @see CMClientStub#removeNonBlockSocketChannel(int, String)
	 */
	public boolean addNonBlockSocketChannel(int nChKey, String strServer)
	{
		CMServer serverInfo = null;
		SocketChannel sc = null;
		CMChannelInfo<Integer> scInfo = null;
		
		if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		{
			System.err.println("CMClientStub.addNonBlockSocketChannel(), you must log in to the default server!");
			return false;
		}
		
		serverInfo = CMInteractionManager.findServer(strServer, m_cmInfo);
		if(serverInfo == null)
		{
			System.err.println("CMClientStub.addNonBlockSocketChannel(), server("+strServer+") not found.");
			return false;
		}			
				
		scInfo = serverInfo.getNonBlockSocketChannelInfo();
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc != null)
		{
			System.err.println("CMClientStub.addNonBlockSocketChannel(), channel key("+nChKey
					+") already exists.");
			return false;
		}
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		CMOpenChannelTask task = new CMOpenChannelTask(CMInfo.CM_SOCKET_CHANNEL,
				serverInfo.getServerAddress(), serverInfo.getServerPort(), false, m_cmInfo);
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<SelectableChannel> future = es.submit(task);
		try {
			sc = (SocketChannel) future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//sc = (SocketChannel) CMCommManager.openNonBlockChannel(CMInfo.CM_SOCKET_CHANNEL, 
		//		serverInfo.getServerAddress(), serverInfo.getServerPort(), m_cmInfo);

		//////////
		
		if(sc == null)
		{
			System.err.println("CMClientStub.addNonBlockSocketChannel(), failed!: key("+nChKey+"), server("
					+strServer+")");
			return false;
		}
		scInfo.addChannel(nChKey, sc);
		
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL);
		se.setChannelName(getMyself().getName());
		se.setChannelNum(nChKey);
		send(se, strServer, CMInfo.CM_STREAM, nChKey);
		
		se = null;
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMClientStub.addNonBlockSocketChannel(),successfully requested to add the channel "
					+ "with the key("+nChKey+") to the server("+strServer+")");
		}
		
		return true;
	}
	
	/**
	 * Adds synchronously a nonblocking (TCP) socket channel to a server.
	 * <br> Only the client can add an additional stream socket (TCP) channel. In the case of the datagram 
	 * and multicast channels, both the client and the server can add an additional non-blocking channel 
	 * with the {@link CMStub#addNonBlockDatagramChannel(int)} and 
	 * {@link CMStub#addMulticastChannel(String, String, String, int)} methods in the CMStub class.
	 * 
	 * @param nChKey - the channel key which must be greater than 0.
	 * The key 0 is occupied by the default TCP channel.
	 * @param strServer - the server name to which the client adds a TCP channel. The name of the default 
	 * server is 'SERVER'.
	 * @return true if the socket channel is successfully created both at the client and the server. 
	 * <br> False, otherwise.
	 * 
	 * @see CMClientStub#addNonBlockSocketChannel(int, String)
	 * @see CMClientStub#addBlockSocketChannel(int, String)
	 * @see CMClientStub#syncAddBlockSocketChannel(int, String)
	 * @see CMClientStub#removeNonBlockSocketChannel(int, String)
	 */
	public SocketChannel syncAddNonBlockSocketChannel(int nChKey, String strServer)
	{
		CMServer serverInfo = null;
		SocketChannel sc = null;
		CMChannelInfo<Integer> scInfo = null;
		CMEventInfo eInfo = m_cmInfo.getEventInfo();
		CMEventSynchronizer eventSync = eInfo.getEventSynchronizer();
		CMSessionEvent replyEvent = null;
		int nReturnCode = -1;
		
		if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		{
			System.err.println("CMClientStub.syncAddNonBlockSocketChannel(), you must log in to the default server!");
			return null;
		}
		
		serverInfo = CMInteractionManager.findServer(strServer, m_cmInfo);
		if(serverInfo == null)
		{
			System.err.println("CMClientStub.syncAddNonBlockSocketChannel(), server("+strServer+") not found.");
			return null;
		}			
		
		scInfo = serverInfo.getNonBlockSocketChannelInfo();
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc != null)
		{
			System.err.println("CMClientStub.syncAddNonBlockSocketChannel(), channel key("+nChKey
					+") already exists.");
			return null;
		}
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		CMOpenChannelTask task = new CMOpenChannelTask(CMInfo.CM_SOCKET_CHANNEL,
				serverInfo.getServerAddress(), serverInfo.getServerPort(), false, m_cmInfo);
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<SelectableChannel> future = es.submit(task);
		try {
			sc = (SocketChannel) future.get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//sc = (SocketChannel) CMCommManager.openNonBlockChannel(CMInfo.CM_SOCKET_CHANNEL, 
		//		serverInfo.getServerAddress(), serverInfo.getServerPort(), m_cmInfo);

		//////////
		
		if(sc == null)
		{
			System.err.println("CMClientStub.syncAddNonBlockSocketChannel(), failed!: key("+nChKey+"), server("
					+strServer+")");
			return null;
		}
		scInfo.addChannel(nChKey, sc);

		eventSync.init();
		eventSync.setWaitedEvent(CMInfo.CM_SESSION_EVENT, CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL_ACK, strServer);

		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL);
		se.setChannelName(getMyself().getName());
		se.setChannelNum(nChKey);
		boolean bRequestResult = send(se, strServer, CMInfo.CM_STREAM, nChKey);
		if(!bRequestResult)
			return null;
		
		se = null;
		
		synchronized(eventSync)
		{
			try {
				eventSync.wait(30000);  // timeout 30s
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			replyEvent = (CMSessionEvent) eventSync.getReplyEvent();
		}

		nReturnCode = replyEvent.getReturnCode();
		if(nReturnCode == 1) // successfully add the new channel info (key, channel) at the server
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMClientStub.syncAddNonBlockSocketChannel(), successfully add the channel "
						+ "info at the server: key("+nChKey+"), server("+strServer+")");
			}
		}
		else if(nReturnCode == 0) // failed to add the new channel info (key, channel) at the server
		{
			System.err.println("CMClientStub.syncAddNonBlockSocketChannel(),failed to add the channel info "
					+ "at the server: key("+nChKey+"), server("+strServer+")");
			sc = null;	// the new socket channel is closed and removed at the CMInteractionManager
		}
		else
		{
			System.err.println("CMClientStub.syncAddNonBlockSocketChannel(), failed: return code("+nReturnCode+")");
			////////// for Android client where network-related methods must be called in a separate thread
			////////// rather than the MainActivity thread
			Future<Boolean> futureRemoveChannel = es.submit(new CMRemoveChannelTask(scInfo, nChKey));
			try {
				futureRemoveChannel.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//scInfo.removeChannel(nChKey);
			//////////
			sc = null;
		}
		
		return sc;		
	}
	
	/**
	 * Removes a nonblocking (TCP) socket channel from a server.
	 * 
	 * @param nChKey - the key of the channel that is to be removed. The key must be greater than 0. 
	 * If the default channel (0) is removed, the result is undefined. 
	 * @param strServer - the server name from which the additional channel is removed.
	 * @return true if the client successfully closes and removes the channel, or false otherwise.
	 * <br> If the client removes the nonblocking socket channel, the server CM detects the disconnection and 
	 * removes the channel at the server side as well. 
	 * @see CMClientStub#addNonBlockSocketChannel(int, String)
	 * @see CMClientStub#removeBlockSocketChannel(int, String)
	 */
	public boolean removeNonBlockSocketChannel(int nChKey, String strServer)
	{
		CMServer serverInfo = null;
		CMChannelInfo<Integer> scInfo = null;
		boolean result = false;

		if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		{
			System.err.println("CMClientStub.removeNonBlockSocketChannel(), you must log in to the default server!");
			return false;
		}
		
		serverInfo = CMInteractionManager.findServer(strServer, m_cmInfo);
		if(serverInfo == null)
		{
			System.err.println("CMClientStub.removeNonBlockSocketChannel(), server("+strServer+") not found.");
			return false;
		}			
				
		scInfo = serverInfo.getNonBlockSocketChannelInfo();
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread

		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<Boolean> future = es.submit(new CMRemoveChannelTask(scInfo, nChKey));
		try {
			result = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//result = scInfo.removeChannel(nChKey);
		
		//////////
		
		if(result)
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMClientStub.removeNonBlockSocketChannel(), succeeded. key("+nChKey+"), server ("
					+strServer+").");
			}
		}
		else
		{
			System.err.println("CMClientStub.removeNonBlockSocketChannel(), failed! key("+nChKey+"), server ("
					+strServer+").");			
		}
		
		return result;
	}

	/**
	 * Adds asynchronously a blocking (TCP) socket channel to a target node (server or client).
	 * <br> Only the client can add an additional stream socket (TCP) channel. In the case of the datagram 
	 * channel, both the client and the server can add an additional blocking channel 
	 * with the {@link CMStub#addBlockDatagramChannel(int)} method in the CMStub class.
	 * 
	 * <p> Although this method returns the reference to the valid socket channel, the target side socket channel is 
	 * always created as a nonblocking mode first due to the intrinsic CM architecture of event-driven asynchronous 
	 * communication. The target sends the acknowledgement message after the nonblocking channel is changed 
	 * to the blocking channel. It is unsafe for the client use its socket channel before the channel is changed to 
	 * the blocking mode at the target node.
	 * The establishment of a new blocking socket channel at both sides (the client and the target) completes 
	 * only when the client receives the ack event 
	 * ({@link CMSessionEvent#ADD_BLOCK_SOCKET_CHANNEL_ACK}) from the target 
	 * and the return code in the event is 1. 
	 * The client event handler can catch the ack event.
	 * 
	 * @param nChKey - the channel key. It should be a positive integer (greater than or equal to 0).
	 * @param strTarget - the name of a target (server or client) to which the client creates a connection.
	 * @return a reference to the socket channel if it is successfully created at the client, or null otherwise. 
	 * 
	 * @see CMClientStub#syncAddBlockSocketChannel(int, String)
	 * @see CMClientStub#addNonBlockSocketChannel(int, String)
	 * @see CMClientStub#syncAddNonBlockSocketChannel(int, String)
	 * @see CMClientStub#removeBlockSocketChannel(int, String)
	 * 
	 */
	public boolean addBlockSocketChannel(int nChKey, String strTarget)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		CMServer serverInfo = null;
		CMUser targetUser = null;
		SocketChannel sc = null;
		String strTargetSSCAddress = null;
		int nTargetSSCPort = -1;
		CMChannelInfo<Integer> scInfo = null;
		boolean bRet = false;

		if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		{
			System.err.println("CMClientStub.addBlockSocketChannel(), you must log in to the default server!");
			return false;
		}
		
		commInfo.setStartTime(System.currentTimeMillis());
		
		serverInfo = CMInteractionManager.findServer(strTarget, m_cmInfo);
		if( serverInfo != null )
		{
			scInfo = serverInfo.getBlockSocketChannelInfo();
			strTargetSSCAddress = serverInfo.getServerAddress();
			nTargetSSCPort = serverInfo.getServerPort();			
		}
		else
		{
			targetUser = CMInteractionManager.findGroupMemberOfClient(strTarget, m_cmInfo);
			if(targetUser == null)
			{
				System.err.println("CMClientStub.addBlockSocketChannel(), target user("
						+strTarget+") not found!");
				return false;
			}
			
			scInfo = targetUser.getBlockSocketChannelInfo();
			strTargetSSCAddress = targetUser.getHost();
			nTargetSSCPort = targetUser.getSSCPort();
		}		
	
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc != null)
		{
			System.err.println("CMClientStub.addBlockSocketChannel(), channel key("
					+nChKey+") to the target("+strTarget+") already exists!");
			return false;
		}
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		CMOpenChannelTask task = new CMOpenChannelTask(CMInfo.CM_SOCKET_CHANNEL,
				strTargetSSCAddress, nTargetSSCPort, true, m_cmInfo);
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<SelectableChannel> future = es.submit(task);
		try {
			sc = (SocketChannel) future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//////////
		
		if(sc == null)
		{
			System.err.println("CMClientStub.addBlockSocketChannel(), failed!: key("
					+nChKey+"), target("+strTarget+")");
			return false;
		}
		scInfo.addChannel(nChKey, sc);
		
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL);
		se.setSender(interInfo.getMyself().getName());
		se.setReceiver(strTarget);
		se.setChannelName(getMyself().getName());
		se.setChannelNum(nChKey);
		//send(se, strTarget, CMInfo.CM_STREAM, nChKey, true);
		bRet = CMEventManager.unicastEvent(se, strTarget, CMInfo.CM_STREAM, nChKey, true, m_cmInfo);
		se = null;

		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMClientStub.addBlockSocketChannel(),successfully requested to add the channel "
					+ "with the key("+nChKey+") to the target("+strTarget+")");
		}
				
		return true;				
	}
	
	/**
	 * Adds synchronously a blocking (TCP) socket channel to a target node (server or client).
	 * <br> Only the client can add an additional stream socket (TCP) channel. In the case of the datagram 
	 * channel, both the client and the server can add an additional blocking channel 
	 * with the {@link CMStub#addBlockDatagramChannel(int)} method in the CMStub class.
	 * 
	 * @param nChKey - the channel key. It should be a positive integer (greater than or equal to 0).
	 * @param strTarget - the name of a target (server or client) to which the client creates a connection.
	 * @return a reference to the socket channel if it is successfully created both at the client and the target, 
	 * or null otherwise. 
	 * 
	 * @see CMClientStub#addBlockSocketChannel(int, String)
	 * @see CMClientStub#addNonBlockSocketChannel(int, String)
	 * @see CMClientStub#syncAddNonBlockSocketChannel(int, String)
	 * @see CMClientStub#removeBlockSocketChannel(int, String)
	 */
	public SocketChannel syncAddBlockSocketChannel(int nChKey, String strTarget)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		CMServer serverInfo = null;
		CMUser targetUser = null;
		SocketChannel sc = null;
		String strTargetSSCAddress = null;
		int nTargetSSCPort = -1;
		CMChannelInfo<Integer> scInfo = null;
		CMEventInfo eInfo = m_cmInfo.getEventInfo();
		CMEventSynchronizer eventSync = eInfo.getEventSynchronizer();
		CMSessionEvent replyEvent = null;
		int nReturnCode = -1;
		
		if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		{
			System.err.println("CMClientStub.syncAddBlockSocketChannel(), you must log in to the default server!");
			return null;
		}
		
		commInfo.setStartTime(System.currentTimeMillis());
		
		serverInfo = CMInteractionManager.findServer(strTarget, m_cmInfo);
		if( serverInfo != null )
		{
			scInfo = serverInfo.getBlockSocketChannelInfo();
			strTargetSSCAddress = serverInfo.getServerAddress();
			nTargetSSCPort = serverInfo.getServerPort();			
		}
		else
		{
			targetUser = CMInteractionManager.findGroupMemberOfClient(strTarget, m_cmInfo);
			if(targetUser == null)
			{
				System.err.println("CMClientStub.syncAddBlockSocketChannel(), target user("
						+strTarget+") not found!");
				return null;
			}
			
			scInfo = targetUser.getBlockSocketChannelInfo();
			strTargetSSCAddress = targetUser.getHost();
			nTargetSSCPort = targetUser.getSSCPort();
		}		
	
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc != null)
		{
			System.err.println("CMClientStub.syncAddBlockSocketChannel(), channel key("
					+nChKey+") to the target("+strTarget+") already exists!");
			return null;
		}
		
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		CMOpenChannelTask task = new CMOpenChannelTask(CMInfo.CM_SOCKET_CHANNEL,
				strTargetSSCAddress, nTargetSSCPort, true, m_cmInfo);
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<SelectableChannel> future = es.submit(task);
		try {
			sc = (SocketChannel) future.get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//////////
		
		if(sc == null)
		{
			System.err.println("CMClientStub.syncAddBlockSocketChannel(), failed!: key("
					+nChKey+"), target("+strTarget+")");
			return null;
		}
		scInfo.addChannel(nChKey, sc);

		eventSync.init();
		eventSync.setWaitedEvent(CMInfo.CM_SESSION_EVENT, 
				CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK, strTarget);			

		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL);
		se.setSender(interInfo.getMyself().getName());
		se.setReceiver(strTarget);
		se.setChannelName(getMyself().getName());
		se.setChannelNum(nChKey);
		//boolean bRequestResult = send(se, strTarget, CMInfo.CM_STREAM, nChKey, true);
		
		// The ADD_BLOCK_SOCKET_CHANNEL event is directly sent to the target node 
		// so that the target can be informed which channel should be changed its mode 
		// from non-blocking to blocking mode.
		boolean bRequestResult = CMEventManager.unicastEvent(se, strTarget, 
				CMInfo.CM_STREAM, nChKey, true, m_cmInfo);
		if(!bRequestResult)
			return null;
		
		se = null;

		synchronized(eventSync)
		{
			try {
				eventSync.wait(30000);  // timeout 30s
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			replyEvent = (CMSessionEvent) eventSync.getReplyEvent();
		}

		nReturnCode = replyEvent.getReturnCode();
		if(nReturnCode == 1) // successfully add the new channel info (key, channel) at the server
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMClientStub.syncAddBlockSocketChannel(), "
						+ "successfully add the channel info at the target("+strTarget
						+"), key("+nChKey+")");
			}
		}
		else if(nReturnCode == 0) // failed to add the new channel info (key, channel) at the server
		{
			System.err.println("CMClientStub.syncAddBlockSocketChannel(), "
					+ "failed to add the channel info at the target("+strTarget
					+"), key("+nChKey+")");
			sc = null;	// the new socket channel is closed and removed at the CMInteractionManager
		}
		else
		{
			System.err.println("CMClientStub.syncAddBlockSocketChannel(), failed: "
					+ "return code("+nReturnCode+")");
			////////// for Android client where network-related methods must be called in a separate thread
			////////// rather than the MainActivity thread
			Future<Boolean> futureRemoveChannel = es.submit(new CMRemoveChannelTask(scInfo, nChKey));
			try {
				futureRemoveChannel.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//scInfo.removeChannel(nChKey);
			//////////
			
			sc = null;
		}

		return sc;		
	}
	
	/**
	 * Removes asynchronously the blocking socket (TCP) channel.
	 * 
	 * <p> This method does not immediately remove the requested channel for safe and smooth close procedure 
	 * between the client and the target node (server or client). Before the removal of the client socket channel, the client first sends 
	 * a request CM event to the target that then prepares the channel disconnection and sends the ack event 
	 * ({@link CMSessionEvent#REMOVE_BLOCK_SOCKET_CHANNEL_ACK}) back to the client.
	 * <br> The client closes and removes the target channel only if it receives the ack event and the return code 
	 * is 1. 
	 * The client event handler can catch the event in order to figure out the result of the removal request.
	 * 
	 * @param nChKey - the key of a socket channel that is to be deleted.
	 * @param strTarget - the name of a target (server or client) to which the target socket channel is connected.
	 * @return true if the client successfully requests the removal of the channel from the target, or false otherwise.
	 * <br> The blocking socket channel is closed and removed only when the client receives the ack event from the target.
	 * 
	 * @see CMClientStub#syncRemoveBlockSocketChannel(int, String)
	 * @see CMClientStub#addBlockSocketChannel(int, String)
	 * @see CMClientStub#syncAddBlockSocketChannel(int, String)
	 */
	public boolean removeBlockSocketChannel(int nChKey, String strTarget)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMServer serverInfo = null;
		CMUser targetUser = null;
		CMChannelInfo<Integer> scInfo = null;
		boolean result = false;
		SocketChannel sc = null;
		CMSessionEvent se = null;

		if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		{
			System.err.println("CMClientStub.removeBlockSocketChannel(), you must log in to the default server!");
			return false;
		}
		
		serverInfo = CMInteractionManager.findServer(strTarget, m_cmInfo);
		if(serverInfo != null)
		{
			scInfo = serverInfo.getBlockSocketChannelInfo();			
		}
		else
		{
			targetUser = CMInteractionManager.findGroupMemberOfClient(strTarget, m_cmInfo);
			if(targetUser == null)
			{
				System.err.println("CMClientStub.removeBlockSocketChannel(), target user("
						+strTarget+") not found!");
				return false;
			}
			
			scInfo = targetUser.getBlockSocketChannelInfo();		
		}
				
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc == null)
		{
			System.err.println("CMClientStub.removeBlockSocketChannel(), "
					+ "socket channel not found! key("+nChKey+"), target("+strTarget+").");
			return false;
		}
		
		se = new CMSessionEvent();
		se.setID(CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL);
		se.setChannelNum(nChKey);
		se.setChannelName(interInfo.getMyself().getName());
		result = send(se, strTarget);	// send the event with the default nonblocking socket channel
		se = null;
		
		// The channel will be closed and removed after the client receives the ACK event at the event handler.
		
		return result;
	}
	
	/**
	 * Removes synchronously the blocking socket (TCP) channel.
	 * 
	 * <p> This method does not immediately remove the requested channel for safe and smooth close procedure 
	 * between the client and the target (server or client). Before the removal of the client socket channel, the client first sends 
	 * a request CM event to the target that then prepares the channel disconnection and sends the ack event 
	 * ({@link CMSessionEvent#REMOVE_BLOCK_SOCKET_CHANNEL_ACK}) back to the client.
	 * <br> The client closes and removes the target channel only if it receives the ack event and the return code 
	 * is 1. 
	 * 
	 * @param nChKey - the key of a socket channel that is to be deleted.
	 * @param strTarget - the name of a target (server or client) to which the target socket channel is connected.
	 * @return true if the client successfully closed and removed the channel, false otherwise.
	 * <br> The blocking socket channel is closed and removed only when the client receives the ack event from the target.
	 * 
	 * @see CMClientStub#removeBlockSocketChannel(int, String)
	 * @see CMClientStub#addBlockSocketChannel(int, String)
	 * @see CMClientStub#syncAddBlockSocketChannel(int, String)
	 */
	public boolean syncRemoveBlockSocketChannel(int nChKey, String strTarget)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMServer serverInfo = null;
		CMUser targetUser = null;
		CMChannelInfo<Integer> scInfo = null;
		boolean result = false;
		SocketChannel sc = null;
		CMSessionEvent se = null;
		CMEventInfo eInfo = m_cmInfo.getEventInfo();
		CMEventSynchronizer eventSync = eInfo.getEventSynchronizer();
		CMSessionEvent replyEvent = null;
		int nReturnCode = -1;

		if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		{
			System.err.println("CMClientStub.syncRemoveBlockSocketChannel(), you must log in to the default server!");
			return false;
		}
		
		serverInfo = CMInteractionManager.findServer(strTarget, m_cmInfo);
		if(serverInfo != null)
		{
			scInfo = serverInfo.getBlockSocketChannelInfo();			
		}
		else
		{
			targetUser = CMInteractionManager.findGroupMemberOfClient(strTarget, m_cmInfo);
			if(targetUser == null)
			{
				System.err.println("CMClientStub.syncRemoveBlockSocketChannel(), target user("
						+strTarget+") not found!");
				return false;
			}
			
			scInfo = targetUser.getBlockSocketChannelInfo();			
		}
				
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc == null)
		{
			System.err.println("CMClientStub.syncRemoveBlockSocketChannel(), socket channel not found! key("
					+nChKey+"), server ("+strTarget+").");
			return false;
		}

		eventSync.init();
		eventSync.setWaitedEvent(CMInfo.CM_SESSION_EVENT, 
				CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL_ACK, strTarget);
		
		se = new CMSessionEvent();
		se.setID(CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL);
		se.setChannelNum(nChKey);
		se.setChannelName(interInfo.getMyself().getName());
		result = send(se, strTarget);	// send the event with the default nonblocking socket channel
		if(!result)
			return false;
		
		se = null;
		
		synchronized(eventSync)
		{
			try {
				eventSync.wait(30000);  // timeout 30s
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			replyEvent = (CMSessionEvent) eventSync.getReplyEvent();
		}

		nReturnCode = replyEvent.getReturnCode();
		if(nReturnCode == 1) // successfully remove the new channel info (key, channel) at the server
		{
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMClientStub.syncRemoveBlockSocketChannel(), successfully removed the channel "
						+ "info at the server: "+"key("+nChKey+"), server("+strTarget+")");
			}
		}
		else if(nReturnCode == 0) // failed to remove the new channel info (key, channel) at the server
		{
			System.err.println("CMClientStub.syncRemoveBlockSocketChannel(),failed to remove the channel info "
					+ "at the server: key("+nChKey+"), server("+strTarget+")");
			result = false;
		}
		else
		{
			System.err.println("CMClientStub.syncRemoveBlockSocketChannel(), failed: return code("+nReturnCode+")");
			result = false;
		}
		
		return result;
	}

	/**
	 * Returns a blocking socket (TCP) channel.
	 * 
	 * <p> A client can add a blocking socket channel with {@link CMClientStub#addBlockSocketChannel(int, String)} method, 
	 * and retrieve it later with this method.
	 * 
	 * @param nChKey - the channel key.
	 * @param strServerName - the name of a server to which the socket channel is connected.
	 * <br> If strServerName is null, it implies the socket channel to the default server.
	 * @return the blocking socket channel, or null if the channel is not found.
	 * 
	 * @see CMStub#getBlockDatagramChannel(int)
	 */
	public SocketChannel getBlockSocketChannel(int nChKey, String strServerName)
	{
		SocketChannel sc = null;
		CMServer serverInfo = null;
		CMChannelInfo<Integer> chInfo = null;
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();

		if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		{
			System.err.println("CMClientStub.addNonBlockSocketChannel(), you must log in to the default server!");
			return null;
		}
		
		if(strServerName.equals(strDefServer))
		{
			serverInfo = m_cmInfo.getInteractionInfo().getDefaultServerInfo();
		}
		else
		{
			serverInfo = m_cmInfo.getInteractionInfo().findAddServer(strServerName);
			if(serverInfo == null)
			{
				System.err.println("CMClientStub.getBlockSocketChannel(), additional server info not found! : "
						+"server ("+strServerName+"), key ("+nChKey+")");
				return null;
			}
		}
		
		chInfo = serverInfo.getBlockSocketChannelInfo();
		sc = (SocketChannel) chInfo.findChannel(nChKey);

		if(sc == null)
		{
			System.err.println("CMClientStub.getBlockSocketChannel(), not found! : key ("+nChKey+"), server ("
					+strServerName+")");
			return null;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMClientStub.getBlockSocketChannel(), channel found ("+sc.hashCode()+") : "
					+"key ("+nChKey+"), server ("+strServerName+")");
		}
		
		return sc;
	}

	/**
	 * Requests to download the list of SNS content from the default server.
	 * <p> The number of downloaded content items is determined by the server. In the configuration file of 
	 * the server CM (cm-server.conf), the DOWNLOAD_NUM field specifies the number of downloaded content items.
	 * 
	 * <p> Each of the requested SNS content item is then sent to the requesting client as 
	 * the CONTENT_DOWNLOAD event that belongs to the {@link CMSNSEvent} class, and that can be caught in 
	 * the client event handler. The CONTENT_DOWNLOAD event includes fields as below:
	 * 
	 * <table border=1>
	 * <caption>CMSNSEvent.CONTENT_DOWNLOAD event</caption>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Event type </td> <td> CMInfo.CM_SNS_EVENT </td>
	 *   </tr>
	 *   <tr> 
	 *     <td bgcolor="lightgrey"> Event ID </td> <td> CMSNEEvent.CONTENT_DOWNLOAD </td> 
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Event field </td> <td> Get method </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Requester name </td> <td> {@link CMSNSEvent#getUserName()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Requested content offset </td> <td> {@link CMSNSEvent#getContentOffset()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Content ID </td> <td> {@link CMSNSEvent#getContentID()} </td>
	 *   </tr>
	 *   <tr> 
	 *     <td bgcolor="lightgrey"> Written date and time of the content </td> <td> {@link CMSNSEvent#getDate()} </td> 
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Writer name of the content </td> <td> {@link CMSNSEvent#getWriterName()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Text message of the content </td> <td> {@link CMSNSEvent#getMessage()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Number of attachments </td> <td> {@link CMSNSEvent#getNumAttachedFiles()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Content ID to which this message replies (0 for no reply) </td>
	 *     <td> {@link CMSNSEvent#getReplyOf()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Level of disclosure of the content
	 *          <br> 0: open to public <br> 1: open only to friends <br> 2: open only to bi-friends 
	 *          <br> 3: private 
	 *     </td> 
	 *     <td> {@link CMSNSEvent#getLevelOfDisclosure()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> List of attached file names </td> <td> {@link CMSNSEvent#getFileNameList()} </td>
	 *   </tr>
	 * </table>
	 * 
	 * <p> In most cases, the server sends multiple CONTENT_DOWNLOAD events due to the corresponding number of SNS messages, 
	 * and it sends the CONTENT_DOWNLOAD_END event of {@link CMSNSEvent} as the end signal of current download. 
	 * This event contains a field that is the number of downloaded messages. A client event handler can catch this event, 
	 * and the client can send another download request by updating the offset parameter with the number of previously 
	 * downloaded messages. 
	 * The detailed event fields of the CONTENT_DOWNLOAD_END event is described below.
	 * 
	 * <table border=1>
	 * <caption>CMSNSEvent.CONTENT_DOWNLOAD_END event</caption>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Event type </td> <td> CMInfo.CM_SNS_EVENT </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Event ID </td> <td> CMSNSEvent.CONTENT_DOWNLOAD_END </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Event field </td> <td> Get method </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> User name </td> <td> {@link CMSNSEvent#getUserName()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Offset </td> <td> {@link CMSNSEvent#getContentOffset()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Content ID </td> <td> {@link CMSNSEvent#getContentID()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Number of downloaded items </td> <td> {@link CMSNSEvent#getNumContents()} </td>
	 *   <tr>
	 * </table>
	 * 
	 * @param strWriter - the name of the writer whose content list will be downloaded.
	 * <br> The client can designate a specific writer name or a friend group. If the parameter value is 
	 * a specific user name, the client downloads only content that was uploaded by the specified name 
	 * and that is accessible by the requester. If the parameter value is &quot;CM_MY_FRIEND&quot;, the client 
	 * downloads content that was uploaded by the requester&#39;s friends. If the parameter is &quot;CM_BI_FRIEND&quot;, 
	 * the client downloads content that was uploaded by the requester&#39;s bi-friends. If the &quot;strWriter&quot; 
	 * parameter is an empty string (&quot;&quot;), the client does not specify a writer name and it downloads all 
	 * content that the requester is eligible to access.
	 * @param nOffset - the offset from the beginning of the requested content list.
	 * <br> The client can request to download some number of SNS messages starting from the nOffset-th 
	 * most recent content. The nOffset value is greater than or equal to 0. The requested content list is 
	 * sorted in reverse chronological order (in reverse order of uploading time). If the searched content 
	 * list has 5 items, they have index number starting with 0. The first item (index 0) is the most recent content, 
	 * the second item (index 1) is the second most recent one, and so on.
	 * 
	 * @see CMClientStub#requestSNSContentUpload(String, String, int, int, int, ArrayList)
	 * @see CMClientStub#requestAttachedFileOfSNSContent(String)
	 * @see CMClientStub#requestAttachedFileOfSNSContent(int, String, String)
	 */
	public void requestSNSContent(String strWriter, int nOffset)
	{
		CMUser user = getMyself();
		int nState = user.getState();
		if( nState == CMInfo.CM_INIT || nState == CMInfo.CM_CONNECT )
		{
			System.out.println("CCMClientStub::requestSNSContents(), you must log in to the default server!");
			return;
		}
		
		String strUser = user.getName();
		
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.CONTENT_DOWNLOAD_REQUEST);
		se.setUserName(strUser);
		se.setWriterName(strWriter);
		se.setContentOffset(nOffset);
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);

		se = null;
		return;
	}

	/**
	 * Requests to download the next list of SNS content.
	 * 
	 * <p> This method requests the next list after the last download request of SNS content.
	 * If this method is called without any previous download request, it requests the most recent list of SNS content, 
	 * which is the same as the result of {@link CMClientStub#requestSNSContent(String, int)}.
	 * <br> If there is no more next list of SNS content, the server sends the CONTENT_DOWNLOAD_END event of 
	 * {@link CMSNSEvent} without sending the CONTENT_DOWNLOAD event.
	 * 
	 * @see CMClientStub#requestPreviousSNSContent()
	 * @see CMClientStub#requestSNSContent(String, int)
	 */
	public void requestNextSNSContent()
	{
		CMSNSInfo snsInfo = m_cmInfo.getSNSInfo();
		// get the saved data
		String strWriter = snsInfo.getLastlyReqWriter();
		int nOffset = snsInfo.getLastlyReqOffset();
		int nDownContentNum = snsInfo.getLastlyDownContentNum();
		
		// update next content offset
		nOffset = nOffset+nDownContentNum;
		
		// request SNS content
		requestSNSContent(strWriter, nOffset);
		
		return;
	}
	
	/**
	 * Requests to download the previous list of SNS content.
	 * 
	 * <p> This method requests the previous list before the last download request of SNS content.
	 * If this method is called without any previous download request, it requests the most recent list of SNS content, 
	 * which is the same as the result of {@link CMClientStub#requestSNSContent(String, int)}.
	 * <br> If there is no more previous list of SNS content, the server sends the CONTENT_DOWNLOAD_END event of 
	 * {@link CMSNSEvent} without sending the CONTENT_DOWNLOAD event.
	 * 
	 * @see CMClientStub#requestNextSNSContent()
	 * @see CMClientStub#requestSNSContent(String, int)
	 */
	public void requestPreviousSNSContent()
	{
		CMSNSInfo snsInfo = m_cmInfo.getSNSInfo();
		// get the saved data
		String strWriter = snsInfo.getLastlyReqWriter();
		int nOffset = snsInfo.getLastlyReqOffset();
		int nDownContentNum = snsInfo.getLastlyDownContentNum();
		
		// update next content offset
		nOffset = nOffset-nDownContentNum;
		//if(nOffset < 0) nOffset = 0;
		
		// request SNS content
		requestSNSContent(strWriter, nOffset);
		
		return;
	}
	
	/**
	 * Uploads SNS content.
	 * <br> A client can call this method to upload a message to the default server.
	 * 
	 * <p> If the server receives the content upload request, it stores the requested message with the user name, 
	 * the index of the content, the upload time, the number of attachments, the reply ID, and the level of disclosure. 
	 * If the content has attached files, the client separately transfers them to the server. After the upload task is 
	 * completed, the server sends the CONTENT_UPLOAD_RESPONSE event of {@link CMSNSEvent} to the requesting client 
	 * so that the client handler can catch the result of the request. 
	 * The detailed event fields of the CONTENT_UPLOAD_RESPONSE event is described below:
	 * 
	 * <table border=1>
	 * <caption>CMSNSEvent.CONTENT_UPLOAD_RESPONSE event</caption>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Event type </td> <td> CMInfo.CM_SNS_EVENT </td>
	 *   </tr>
	 *   <tr> 
	 *     <td bgcolor="lightgrey"> Event ID </td> <td> CMSNEEvent.CONTENT_UPLOAD_RESPONSE </td> 
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Event field </td> <td> Get method </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Return code </td> <td> {@link CMSNSEvent#getReturnCode()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Content ID </td> <td> {@link CMSNSEvent#getContentID()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> Date and time </td> <td> {@link CMSNSEvent#getDate()} </td>
	 *   </tr>
	 *   <tr>
	 *     <td bgcolor="lightgrey"> User name </td> <td> {@link CMSNSEvent#getUserName()} </td>
	 *   </tr>
	 * </table>  
	 * 
	 * @param user - the name of a user who uploads a message
	 * @param message - the text message
	 * @param nNumAttachedFiles - the number of attached files in this message
	 * <br> The value of nNumAttachedFiles must be the same as the number of elements in a given file path list 
	 * as the last parameter, filePathList.
	 * @param nReplyOf - an ID (greater than 0) of content to which this message replies
	 * <br> If the value is 0, it means that the uploaded content is not a reply but an original one.
	 * @param nLevelOfDisclosure - the level of disclosure (LoD) of the uploaded content
	 * <br> CM provides four levels of disclosure of content from 0 to 3. LoD 0 is to open the uploaded content to public. 
	 * LoD 1 allows only users who added the uploading user as friends to access the uploaded content. 
	 * LoD 2 allows only bi-friends of the uploading user to access the uploaded content. (Refer to the friend management 
	 * section for details of different friend concepts.) 
	 * LoD 3 does not open the uploaded content and makes it private.
	 * @param filePathList - the list of attached files
	 * 
	 * @see CMClientStub#requestSNSContent(String, int) 
	 */
	public void requestSNSContentUpload(String user, String message, int nNumAttachedFiles, 
			int nReplyOf, int nLevelOfDisclosure, ArrayList<String> filePathList)
	{
		ArrayList<String> fileNameList = null;
		int i = -1;
		int nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT || nState == CMInfo.CM_CONNECT )
		{
			System.out.println("CMClientStub.requestSNSContentUpload(), you must log in to the default server!");
			return;
		}
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.CONTENT_UPLOAD_REQUEST);
		se.setUserName(user);
		se.setMessage(message);
		se.setNumAttachedFiles(nNumAttachedFiles);
		se.setReplyOf(nReplyOf);
		se.setLevelOfDisclosure(nLevelOfDisclosure);
		if(nNumAttachedFiles > 0 && filePathList != null)
		{
			// check the number and the real number of elements
			if(nNumAttachedFiles != filePathList.size())
			{
				System.out.println("CMClientStub.requestSNSContentUpload(), the number of attached files "
						+ "are not consistent!");
				return;
			}
			
			// check if files exist or not
			for(i = 0; i < filePathList.size(); i++)
			{
				String strFile = filePathList.get(i);
				File attachFile = new File(strFile);
				if(!attachFile.exists())
				{
					System.out.println("CMClientStub.requestSNSContentUpload(), file("+strFile+") not found!");
					return;
				}
			}
			
			// store the file path list in the CMSNSInfo class (CMSNSAttach object)
			CMSNSInfo sInfo = m_cmInfo.getSNSInfo();
			CMSNSAttach sendAttach = sInfo.getSendSNSAttach();
			sendAttach.setFilePathList(filePathList);
			
			// create a file name list with the given file path list
			fileNameList = new ArrayList<String>();
			for(i = 0; i < filePathList.size(); i++)
			{
				String strFilePath = filePathList.get(i);
				int startIndex = strFilePath.lastIndexOf(File.separator);
				String strFileName = strFilePath.substring(startIndex+1);
				fileNameList.add(strFileName);
				System.out.println("attached file name: "+strFileName);
			}
			
			se.setFileNameList(fileNameList);
		}
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);

		se = null;
		return;
	}
	
	/**
	 * Requests an attached file of SNS content.
	 * 
	 * <p> The client can request to download a file that is attached to a downloaded SNS content item from the server.
	 * <br> After the client request to download SNS content by the {@link CMClientStub#requestSNSContent(String, int)} 
	 * method, all attached files are automatically downloaded from the server. If an attached file is not available at 
	 * the server, only the file name is downloaded so that the client can separately request such a file from 
	 * the server later.
	 * 
	 * <p> If the server is requested to download an attached file, it sends the RESPONSE_ATTACHED_FILE event of 
	 * the {@link CMSNSEvent}. The client event handler can catch this event, and can figure out the result of 
	 * the request by getting the return code. If the return code is 1, the requested file is available at the server, 
	 * and the server separately sends the requested file to the client using the {@link CMStub#pushFile(String, String)} 
	 * method. If the return code is 0, the requested file does not exist at the server.
	 * <br> The detailed event fields of the RESPONSE_ATTACHED_FILE event are described below:
	 * 
	 * <table border=1>
	 * <caption>CMSNSEvent.RESPONSE_ATTACHED_FILE event</caption>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Event type </td> <td> CMInfo.CM_SNS_EVENT </td> 
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Event ID </td> <td> CMSNSEvent.RESPONSE_ATTACHED_FILE </td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 *   <td> Event field </td> <td> Get method </td> <td> Description </td>
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> User name </td> <td> {@link CMSNSEvent#getUserName()} </td> 
	 *   <td> The requesting user name </td>
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Content ID </td> <td> {@link CMSNSEvent#getContentID()} </td>
	 *   <td> The ID of the SNS content that attached the requested file </td>
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Writer name </td> <td> {@link CMSNSEvent#getWriterName()} </td>
	 *   <td> The writer name of the SNS content </td>
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> File name </td> <td> {@link CMSNSEvent#getFileName()} </td>
	 *   <td> The name of the attached file </td>
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Return code </td> <td> {@link CMSNSEvent#getReturnCode()} </td>
	 *   <td> The request result. If the value is 1, the requested file will be delivered to the client. If the value 
	 *   is 0, the server does nothing further for the request. </td>
	 * </tr>
	 * </table>
	 * 
	 * @param strFileName - the requested file name
	 * <br> The list of attached file names can be got in the CONTENT_DOWNLOAD event of {@link CMSNSEvent} that is sent from 
	 * the server. After the client event handler catch this event and get the list of attached file names, it can choose 
	 * a file name that needs to separately download from the server.
	 * 
	 * @return true if the request is successfully sent, or false otherwise
	 * @see CMClientStub#requestAttachedFileOfSNSContent(int, String, String)
	 */
	public boolean requestAttachedFileOfSNSContent(String strFileName)
	{
		int nContentID = -1;
		String strWriterName = null;
		// A downloaded file name may be a thumbnail file name instead of original name
		int index = strFileName.lastIndexOf(".");
		String strThumbnail = strFileName.substring(0, index) + "-thumbnail"
				+ strFileName.substring(index, strFileName.length());
		// search for content ID and writer name
		CMSNSInfo snsInfo = m_cmInfo.getSNSInfo();
		CMSNSContentList contentList = snsInfo.getSNSContentList();
		Vector<CMSNSContent> contentVector = contentList.getContentList();
		Iterator<CMSNSContent> iter = contentVector.iterator();
		boolean bFound = false;
		while(iter.hasNext() && !bFound)
		{
			CMSNSContent content = iter.next();
			if(content.containsFileName(strFileName) || content.containsFileName(strThumbnail))
			{
				nContentID = content.getContentID();
				strWriterName = content.getWriterName();
				bFound = true;
			}
		}		
		
		if(bFound)
		{
			// send request for the attachment download
			requestAttachedFileOfSNSContent(nContentID, strWriterName, strFileName);			
		}
		else
		{
			System.err.println("CMClientStub.requestAttachedFileOfSNSContent(), "
					+strFileName+" not found in the downloaded content list!\n");
			return false;
		}

		return true;
	}
	
	/**
	 * Requests an attached file of SNS content.
	 * 
	 * <p> The detailed information about the request for an attached file can be found in 
	 * the {@link CMClientStub#requestAttachedFileOfSNSContent(String)} method.
	 * 
	 * @param nContentID - the ID of SNS content to which the requested file is attached
	 * @param strWriterName - the name of a requesting user
	 * @param strFileName - the requested file name
	 * 
	 * @see CMClientStub#requestAttachedFileOfSNSContent(String)
	 */
	public void requestAttachedFileOfSNSContent(int nContentID, String strWriterName, String strFileName)
	{
		String strUserName = getMyself().getName();
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.REQUEST_ATTACHED_FILE);
		se.setUserName(strUserName);
		se.setContentID(nContentID);
		se.setWriterName(strWriterName);
		se.setFileName(strFileName);
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);
		
		se = null;
		return;
	}

	/**
	 * Informs the server that the attached file has been accessed by the client.
	 * 
	 * <p> The client can call this method to report its access history of an attached file to the server. The access 
	 * report is sent to the server as the ACCESS_ATTACHED_FILE event of the {@link CMSNSEvent}.  
	 * If the server receives the event, it can use the access information for the analysis of the history of 
	 * client behavior. The server event handler can catch the event.
	 * 
	 * <p> The detailed event fields of the ACCESS_ATTACHED_FILE event are described below:
	 * 
	 * <table border=1>
	 * <caption>CMSNSEvent.ACCESS_ATTACHED_FILE event</caption>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Event type </td> <td> CMInfo.CM_SNS_EVENT </td> 
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Event ID </td> <td> CMSNSEvent.ACCESS_ATTACHED_FILE </td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 *   <td> Event field </td> <td> Get method </td> <td> Description </td>
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> User name </td> <td> {@link CMSNSEvent#getUserName()} </td> 
	 *   <td> The name of the file-accessing user </td> 
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Content ID </td> <td> {@link CMSNSEvent#getContentID()} </td>
	 *   <td> ID of the SNS content of which attached file is accessed </td>
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Writer name </td> <td> {@link CMSNSEvent#getWriterName()} </td>
	 *   <td> The writer name of the SNS content of which attached file is accessed </td>
	 * </tr>
	 * <tr>
	 *   <td bgcolor="lightgrey"> Attached file name </td> <td> {@link CMSNSEvent#getFileName()} </td>
	 *   <td> The name of an attached file that the user accessed </td>
	 * </tr>
	 * </table>
	 * 
	 * @param strFileName - the name of an attached file that the user accessed
	 * @return true if the file access information is successfully sent to the server and if the corresponding 
	 * SNS content is found at the client. Otherwise, the return value is false.
	 * 
	 * @see CMClientStub#accessAttachedFileOfSNSContent(int, String, String)
	 */
	// find the downloaded content and inform the server that the attached file is accessed by the client
	public boolean accessAttachedFileOfSNSContent(String strFileName)
	{
		int nContentID = -1;
		String strWriterName = null;
		// A downloaded file name may be a thumbnail file name instead of original name
		int index = strFileName.lastIndexOf(".");
		String strThumbnail = strFileName.substring(0, index) + "-thumbnail"
				+ strFileName.substring(index, strFileName.length());
		// search for content ID and writer name
		CMSNSInfo snsInfo = m_cmInfo.getSNSInfo();
		CMSNSContentList contentList = snsInfo.getSNSContentList();
		Vector<CMSNSContent> contentVector = contentList.getContentList();
		Iterator<CMSNSContent> iter = contentVector.iterator();
		boolean bFound = false;
		while(iter.hasNext() && !bFound)
		{
			CMSNSContent content = iter.next();
			if(content.containsFileName(strFileName) || content.containsFileName(strThumbnail))
			{
				nContentID = content.getContentID();
				strWriterName = content.getWriterName();
				bFound = true;
			}
		}		
		
		if(bFound)
		{
			// send request for the attachment download
			accessAttachedFileOfSNSContent(nContentID, strWriterName, strFileName);			
		}
		else
		{
			System.err.println("CMClientStub.accessAttachedFileOfSNSContent(), "
					+strFileName+" not found in the downloaded content list!\n");
			return false;
		}

		return true;
	}

	/**
	 * Informs the server that the attached file has been accessed by the client.
	 * 
	 * <p>	The detailed information about the access report of an attached file to the server can be found in 
	 * the {@link CMClientStub#accessAttachedFileOfSNSContent(String)} method.
	 * 
	 * @param nContentID - the ID of the SNS content of which attached file is accessed
	 * @param strWriterName - the writer name of the SNS content of which attached file is accessed
	 * @param strFileName - the name of an attached file that the user accessed
	 * 
	 * @see CMClientStub#accessAttachedFileOfSNSContent(String)
	 */
	public void accessAttachedFileOfSNSContent(int nContentID, String strWriterName, String strFileName)
	{
		String strUserName = getMyself().getName();
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.ACCESS_ATTACHED_FILE);
		se.setUserName(strUserName);
		se.setContentID(nContentID);
		se.setWriterName(strWriterName);
		se.setFileName(strFileName);
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);
		
		se = null;
		return;
	}

	/**
	 * Requests new additional server information from the default server.
	 * 
	 * <p> When the default server registers an additional server, it then notifies clients of 
	 * the new server information. If a client is a late comer to the CM network, it can also 
	 * explicitly request the information of additional servers from the default server.
	 * <br> In any of the above two cases, the default server sends the NOTIFY_SERVER_INFO event 
	 * of the {@link CMMultiServerEvent} class. This event contains the list of additional server information 
	 * such as a server name, address, port number, and UDP port number. The detailed event fields of 
	 * the NOTIFY_SERVER_INFO event is described below.
	 * 
	 * <table border=1>
	 * <caption>CMMultiServerEvent.NOTIFY_SERVER_INFO event</caption>
	 * <tr>
	 * <td bgcolor="lightgrey">Event type</td><td>CMInfo.CM_MULTI_SERVER_EVENT</td>
	 * </tr>
	 * <tr>
	 * <td bgcolor="lightgrey">Event ID</td><td>CMMultiServerEvent.NOTIFY_SERVER_INFO</td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 * <td>Event field</td><td>Field data type</td><td>Field definition</td><td>Get method</td>
	 * </tr>
	 * <tr>
	 * <td>Number of servers</td><td>int</td><td>Number of additional servers</td><td>getServerNum()</td>
	 * </tr>
	 * <tr>
	 * <td>Server list</td><td>Vector&lt;{@link CMServerInfo}&gt;</td><td>List of additional server information</td>
	 * <td>getServerInfoList()</td>
	 * </tr>
	 * </table>
	 * 
	 * <p> When the default server deletes an additional server by the deregistration request, it then sends 
	 * the NOTIFY_SERVER_LEAVE event to clients. The event fields of the event are described below.
	 * 
	 * <table border=1>
	 * <caption>CMMultiServerEvent.NOTIFY_SERVER_LEAVE event</caption>
	 * <tr>
	 * <td bgcolor="lightgrey">Event type</td><td>CMInfo.CM_MULTI_SERVER_EVENT</td>
	 * </tr>
	 * <tr>
	 * <td bgcolor="lightgrey">Event ID</td><td>CMMultiServerEvent.NOTIFY_SERVER_LEAVE</td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 * <td>Event field</td><td>Field data type</td><td>Field definition</td><td>Get method</td>
	 * </tr>
	 * <tr>
	 * <td>Server name</td><td>String</td><td>Name of an additional server that leaves the CM network</td>
	 * <td>getServerName()</td>
	 * </tr>
	 * </table>
	 * 
	 */
	public void requestServerInfo()
	{
		CMUser myself = getMyself();
		int state = myself.getState();

		if( state == CMInfo.CM_INIT || state == CMInfo.CM_CONNECT )
		{
			System.out.println("CMClientStub.requestServerInfo(), You should login the default server.");
			return;
		}

		CMMultiServerEvent mse = new CMMultiServerEvent();
		mse.setID(CMMultiServerEvent.REQ_SERVER_INFO);
		mse.setUserName( myself.getName() );
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send( mse, strDefServer );

		if(CMInfo._CM_DEBUG)
			System.out.println("CMClientStub::requestServerInfo(), end.");
	
		mse = null;
		return;
	}
	
	/**
	 * Connects to a CM server.
	 * 
	 * <p>If the CM network has multiple servers (the default server and additional servers), 
	 * a CM client can connect to these servers by specifying a server name as the parameter. 
	 * Connection to an additional server is made with an additional TCP channel created.
	 * 
	 * @param strServerName - the server name
	 * @return true if the connection is successfully established; or false otherwise.
	 * @see CMClientStub#connectToServer()
	 * @see CMServerStub#connectToServer()
	 */
	public boolean connectToServer(String strServerName)
	{
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();

		// If CM has been terminated, it must start first.
		if(!m_cmInfo.isStarted())
		{
			if(strServerName.equals(strDefServer))
				return startCM();
			else
			{
				System.err.println("CMClientStub.connectToServer("+strServerName
						+"), CM is terminated and " + "it must start first!");
				return false;
			}
		}

		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		Callable<Boolean> task = new Callable<Boolean>() {
			@Override
			public Boolean call()
			{
				Boolean ret = false;
				if( strServerName.equals(strDefServer) )	// if a default server
				{
					ret = CMInteractionManager.connectDefaultServer(m_cmInfo);
					return ret;
				}
				
				ret = CMInteractionManager.connectAddServer(strServerName, m_cmInfo);
				return ret;
			}
		};
		
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<Boolean> future = es.submit(task);
		boolean bRet = false;
		try {
			bRet = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//////////
		
		return bRet;
	}
	
	/**
	 * Disconnects from a CM server.
	 * 
	 * <p> If the CM network has multiple servers (the default server and additional servers), 
	 * a CM client can disconnect from these servers by specifying a server name as the parameter. 
	 * 
	 * @param strServerName - the server name
	 * @return true if the connection is successfully disconnected; or false otherwise.
	 * @see CMClientStub#disconnectFromServer()
	 * @see CMServerStub#disconnectFromServer()
	 */
	public boolean disconnectFromServer(String strServerName)
	{
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();

		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		
		Callable<Boolean> task = new Callable<Boolean>() {
			@Override
			public Boolean call()
			{
				Boolean ret = false;
				if( strServerName.equals(strDefServer) )	// if a default server
				{
					ret = CMInteractionManager.disconnectFromDefaultServer(m_cmInfo);
					return ret;
				}

				ret = CMInteractionManager.disconnectFromAddServer(strServerName, m_cmInfo);
				return ret;
			}
		};
		
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<Boolean> future = es.submit(task);
		boolean bRet = false;
		try {
			bRet = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//////////
		
		return bRet;
		
	}
	
	/**
	 * Logs in to a CM server.
	 * 
	 * <p> If the CM network has multiple servers (the default server and additional servers), 
	 * a CM client can log in to these servers by specifying a server name.  
	 * <br> The login process to an additional CM server is the almost same as that to the default server 
	 * with the {@link CMClientStub#loginCM(String, String)} method.
	 * Different part is that the login to an additional server requires the target server name, and that 
	 * the multiple-server-related CM event is the {@link CMMultiServerEvent} class instead of the {@link CMSessionEvent}. 
	 * Each event ID of the CMMultiServerEvent is preceded by the "ADD_" and the remaining ID word and its 
	 * role is the same as that of the CMSessionEvent class. Event fields of the CMMultiServerEvent event is also 
	 * the same as those of the CMSessionEvent except an additional field, the server name.
	 * 
	 * @param strServer - the server name
	 * @param strUser - the user name
	 * @param strPasswd - the password
	 * @return true if the request is successfully sent to the server; false otherwise.
	 * @see CMClientStub#loginCM(String, String)
	 */
	public boolean loginCM(String strServer, String strUser, String strPasswd)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMServer tserver = null;
		String myAddress = null;
		int myUDPPort = -1;
		boolean bResult = false;
		String strEncPasswd = null;
		
		// if a server is the default server, call the original function.
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		if( strServer.equals(strDefServer) )
		{
			if( !loginCM(strUser, strPasswd) )
				return false;
		}

		// get a server info
		tserver = interInfo.findAddServer(strServer);
		if( tserver == null )
		{
			System.out.println("CMClientStub.loginCM(..), server("+strServer+") not found!");
			return false;
		}

		// check local state
		
		// If the client is not connected to the server, he/she connects to it first.
		if(tserver.getClientState() == CMInfo.CM_INIT)
		{
			//CMInteractionManager.connectAddServer(strServer, m_cmInfo);
			if(!connectToServer(strServer))
				return false;
		}
		
		switch( tserver.getClientState() )
		{
		//case CMInfo.CM_INIT:
			//System.out.println("You should connect to server("+strServer+") before login."); 
			//return;
		case CMInfo.CM_LOGIN:
		case CMInfo.CM_SESSION_JOIN:
			System.out.println("You already logged in to server("+strServer+").");
			return false;
		}

		// get my ip address and port
		myAddress = confInfo.getMyAddress();
		myUDPPort = confInfo.getUDPPort();
		
		// encrypt password
		strEncPasswd = CMUtil.getSHA1Hash(strPasswd);

		// make an event
		CMMultiServerEvent mse = new CMMultiServerEvent();
		mse.setID(CMMultiServerEvent.ADD_LOGIN);
		mse.setServerName(strServer);
		mse.setUserName(strUser);
		mse.setPassword(strEncPasswd);
		mse.setHostAddress(myAddress);
		mse.setUDPPort(myUDPPort);

		// send the event
		bResult = send(mse, strServer);

		mse = null;
		return bResult;
	}
	
	/**
	 * Logs out from a CM server.
	 * 
	 * <p> If the CM network has multiple servers (the default server and additional servers), 
	 * a CM client can log from one of these servers by specifying a server name.  
	 * <br> The logout process from an additional CM server is the almost same as that from the default server 
	 * with the {@link CMClientStub#logoutCM()} method.
	 * Different part is that the logout from an additional server requires the target server name, and that 
	 * the multiple-server-related CM event is the {@link CMMultiServerEvent} class instead of the {@link CMSessionEvent}. 
	 * Each event ID of the CMMultiServerEvent is preceded by the "ADD_" and the remaining ID word and its 
	 * role is the same as that of the CMSessionEvent class. Event fields of the CMMultiServerEvent event is also 
	 * the same as those of the CMSessionEvent except an additional field, the server name.
	 * 
	 * @param strServer - the server name
	 * @return true if successfully sent the logout request, false otherwise.
	 * @see CMClientStub#logoutCM()
	 */
	public boolean logoutCM(String strServer)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		boolean bResult = false;
		
		// if a server is the default server, call the original function.
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		if(strServer.equals(strDefServer))
		{
			if(!logoutCM())
				return false;
		}

		CMServer tserver = interInfo.findAddServer(strServer);
		if( tserver == null )
		{
			System.out.println("CMClientStub.logoutCM(..), server("+strServer+") info not found "
					+ "in the add-server list!");
			return false;
		}

		// check state of the client of the server
		switch( tserver.getClientState() )
		{
		case CMInfo.CM_INIT:
			System.out.println("You should connect and log in to the server("+strServer+")."); 
			return false;
		case CMInfo.CM_CONNECT:
			System.out.println("You should log in to the server("+strServer+").");
			return false;
		}

		// remove and close all additional channels in EM and CM
		CMChannelInfo<Integer> chInfo = tserver.getNonBlockSocketChannelInfo();
		chInfo.removeAllAddedChannels(0);
		chInfo = tserver.getBlockSocketChannelInfo();
		chInfo.removeAllChannels();

		// make and send event
		CMMultiServerEvent tmse = new CMMultiServerEvent();
		tmse.setID(CMMultiServerEvent.ADD_LOGOUT);
		tmse.setServerName(strServer);
		tmse.setUserName(getMyself().getName());

		bResult = send(tmse, strServer);

		// update the local state of the server
		if(bResult)
			tserver.setClientState(CMInfo.CM_CONNECT);

		// check and stop the scheduled keep-alive task
		if(CMInteractionManager.getNumLoginServers(m_cmInfo) == 0)
		{
			CMThreadInfo threadInfo = m_cmInfo.getThreadInfo();
			ScheduledFuture<?> future = threadInfo.getScheduledFuture();
			future.cancel(true);
			
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMClientStub.logoutCM(), stop the client keep-alive task.");
			}
		}

		tmse = null;
		return bResult;
	}
	
	/**
	 * Requests available session information from a CM server.
	 * 
	 * <p> If the CM network has multiple servers (the default server and additional servers), 
	 * a CM client can request session information from one of these servers by specifying a server name.  
	 * <br> The session-information-request process with an additional CM server is the almost same as that 
	 * with the default server using the {@link CMClientStub#requestSessionInfo()} method.
	 * Different part is that the request from an additional server requires the target server name, and that 
	 * the multiple-server-related CM event is the {@link CMMultiServerEvent} class instead of the {@link CMSessionEvent}. 
	 * Each event ID of the CMMultiServerEvent is preceded by the "ADD_" and the remaining ID word and its 
	 * role is the same as that of the CMSessionEvent class. Event fields of the CMMultiServerEvent event is also 
	 * the same as those of the CMSessionEvent except an additional field, the server name.
	 * 
	 * @param strServerName - the server name
	 * @return true if the request is successfully sent to the server; false otherwise.
	 * @see CMClientStub#requestSessionInfo()
	 */
	// requests available session information of a designated server
	public boolean requestSessionInfo(String strServerName)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMServer tserver = null;
		boolean bResult = false;

		// if a server is the default server, call the original function
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		if( strServerName.equals(strDefServer) )
		{
			if(!requestSessionInfo())
				return false;
		}

		// find a server info
		tserver = interInfo.findAddServer(strServerName);
		if( tserver == null )
		{
			System.out.println("CMClientStub.requestSessionInfo(..), server("+strServerName
					+") info not found in the add-server list.");
			return false;
		}

		int	state = tserver.getClientState();

		if( state == CMInfo.CM_INIT || state == CMInfo.CM_CONNECT )
		{
			System.out.println("CMClientStub.requestSessionInfo(..), You should login the server("
					+strServerName+").");
			return false;
		}

		CMMultiServerEvent mse = new CMMultiServerEvent();
		mse.setID(CMMultiServerEvent.ADD_REQUEST_SESSION_INFO);
		mse.setUserName(getMyself().getName());
		bResult = send(mse, strServerName);

		if(CMInfo._CM_DEBUG)
		{
			if(bResult)
				System.out.println("CMClientStub.requestSessionInfo(..) server("+strServerName+"), Ok");
			else
				System.err.println("CMClientStub.requestSessionInfo(..) server("+strServerName+"), request "
						+ "transmission error!");
		}
	
		mse = null;
		return bResult;
	}
	
	/**
	 * Joins a session in a CM server.
	 * 
	 * <p> If the CM network has multiple servers (the default server and additional servers), 
	 * a CM client can join a session in one of these servers by specifying a server name.  
	 * <br> The session-join process with an additional CM server is the almost same as that 
	 * with the default server using the {@link CMClientStub#joinSession(String)} method.
	 * Different part is that the request from an additional server requires the target server name, and that 
	 * the multiple-server-related CM event is the {@link CMMultiServerEvent} class instead of the {@link CMSessionEvent}. 
	 * Each event ID of the CMMultiServerEvent is preceded by the "ADD_" and the remaining ID word and its 
	 * role is the same as that of the CMSessionEvent class. Event fields of the CMMultiServerEvent event is also 
	 * the same as those of the CMSessionEvent except an additional field, the server name.
	 * 
	 * @param strServer - the server name
	 * @param strSession - the session name
	 * @return true if the request is successful; false otherwise.
	 * @see CMClientStub#joinSession(String)
	 */
	public boolean joinSession(String strServer, String strSession)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMServer tserver = null;
		CMSession tsession = null;
		boolean bResult = false;
		
		// if a server is the default server, call the original function
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		if( strServer.equals(strDefServer) )
		{
			if(!joinSession(strSession))
				return false;
		}

		// find a server info
		tserver = interInfo.findAddServer(strServer);
		if( tserver == null )
		{
			System.out.println("CMClientStub.joinSession(..), server("+strServer+") info "
					+ "not found in the add-server list!");
			return false;
		}
		
		// check local client state of the server
		switch( tserver.getClientState() )
		{
		case CMInfo.CM_INIT:
			System.out.println("You should connect and login server("+strServer+")."); 
			return false;
		case CMInfo.CM_CONNECT:
			System.out.println("You should login server("+strServer+")."); 
			return false;
		case CMInfo.CM_SESSION_JOIN:
			System.out.println("You have already joined a session of a server("+strServer+")."); 
			return false;
		}

		// check selected session
		tsession = tserver.findSession(strSession);
		if(tsession == null)
		{
			System.out.println("CMClientStub.joinSession(..), session("+strSession+") info of server("
					+strServer+") not found! Request session info first!");
			return false;
		}
		
		// make event
		CMMultiServerEvent mse = new CMMultiServerEvent();
		mse.setID(CMMultiServerEvent.ADD_JOIN_SESSION);
		mse.setServerName(strServer);
		mse.setUserName(getMyself().getName());
		mse.setSessionName(strSession);

		// send the event
		bResult = send(mse, strServer);
		// set current session of the client in the server info element
		if(bResult)
			tserver.setCurrentSessionName(strSession);

		mse = null;
		return bResult;
	}
	
	/**
	 * Leaves the current session in a CM server.
	 * 
	 * <p> If the CM network has multiple servers (the default server and additional servers), 
	 * a CM client can leave the current session in one of these servers by specifying a server name.  
	 * <br> The session-leave process in an additional CM server is the almost same as that 
	 * in the default server using the {@link CMClientStub#leaveSession()} method.
	 * Different part is that the request from an additional server requires the target server name, and that 
	 * the multiple-server-related CM event is the {@link CMMultiServerEvent} class instead of the {@link CMSessionEvent}. 
	 * Each event ID of the CMMultiServerEvent is preceded by the "ADD_" and the remaining ID word and its 
	 * role is the same as that of the CMSessionEvent class. Event fields of the CMMultiServerEvent event is also 
	 * the same as those of the CMSessionEvent except an additional field, the server name.
	 * 
	 * @param strServer - the server name
	 * @return true if successfully sent the leave-session request, false otherwise.
	 * @see CMClientStub#leaveSession()
	 */
	public boolean leaveSession(String strServer)
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMServer tserver = null;
		boolean bResult = false;
		
		// if a server is the default server, call the original function
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		if( strServer.equals(strDefServer) )
		{
			if(!leaveSession())
				return false;
		}

		// find a server info
		tserver = interInfo.findAddServer(strServer);
		if(tserver == null)
		{
			System.out.println("CMClientStub.leaveSession(..), server("+strServer+") info not found "
					+ "in the add-server list!");
			return false;
		}

		// check the client state of the server
		switch( tserver.getClientState() )
		{
		case CMInfo.CM_INIT:
			System.out.println("You should connect, log in to server("+strServer+") and join a session.");
			return false;
		case CMInfo.CM_CONNECT:
			System.out.println("You should log in to server("+strServer+") and join a session.");
			return false;
		case CMInfo.CM_LOGIN:
			System.out.println("You should join a session of the server("+strServer+").");
			return false;
		}

		// terminate current group info (multicast channel, group member, Membership key)
		CMGroupManager.terminate(tserver.getCurrentSessionName(), tserver.getCurrentGroupName(), m_cmInfo);

		// make and send event
		CMMultiServerEvent mse = new CMMultiServerEvent();
		mse.setID(CMMultiServerEvent.ADD_LEAVE_SESSION);
		mse.setServerName(tserver.getServerName());
		mse.setUserName(getMyself().getName());
		mse.setSessionName( tserver.getCurrentSessionName() );

		bResult = send(mse, strServer);

		// update the client state of the server
		if(bResult)
			tserver.setClientState(CMInfo.CM_LOGIN);

		mse = null;
		return bResult;
	}
	
	/**
	 * Registers a user to the default server.
	 * 
	 * <p> A user can be registered to CM by the registerUser method of the CM client stub. 
	 * If a CM client is connected to the default server, it can call this method. 
	 * CM uses the registered user information for the user authentication when a user logs in to the default server.
	 * 
	 * <p> Whether the registration request is successful or not is set to a return code of a reply session event, 
	 * {@link CMSessionEvent#REGISTER_USER_ACK}. If the request is successful, 
	 * the reply event also contains the registration time at the server. 
	 * 
	 * @param strName - the user name
	 * @param strPasswd - the password
	 */
	public void registerUser(String strName, String strPasswd)
	{
		int nState = -1;
		String strEncPasswd = null;

		// check if the user is connected to a default server
		nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT )
		{
			System.out.println("CMClientStub.registerUser(), client is not connected to "
					+ "the default server!");
			return;
		}

		// encrypt password
		strEncPasswd = CMUtil.getSHA1Hash(strPasswd);

		// make a request event
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.REGISTER_USER);
		se.setUserName(strName);
		se.setPassword(strEncPasswd);

		// send the request (a default server will send back REGISTER_USER_ACK event)
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);

		if(CMInfo._CM_DEBUG)
			System.out.println("CMClientStub.registerUser(), user("+strName+") requested.");

		se = null;
		return;
	}
	
	/**
	 * Deregisters a user from the default server.
	 * 
	 * <p> A user can cancel his/her registration from CM by the deregisterUser method of 
	 * the CM client stub. If a client is connected to the default server, it can call this method. 
	 * When requested, CM removes the registered user information from the CM DB.
	 * <br> Whether the deregistration request is successful or not is set to a return code of 
	 * a reply session event, {@link CMSessionEvent#DEREGISTER_USER_ACK}.
	 * 
	 * @param strName - the user name
	 * @param strPasswd - the password
	 */
	public void deregisterUser(String strName, String strPasswd)
	{
		int nState = -1;
		String strEncPasswd = null;

		// check if the user is connected to a default server
		nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT )
		{
			System.out.println("CMClientStub.deregisterUser(), client is not connected to "
					+ "the default server!");
			return;
		}

		// encrypt password
		strEncPasswd = CMUtil.getSHA1Hash(strPasswd);

		// make a request event
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.DEREGISTER_USER);
		se.setUserName(strName);
		se.setPassword(strEncPasswd);

		// send the request (a default server will send back DEREGISTER_USER_ACK event)
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);

		if(CMInfo._CM_DEBUG)
		{
			System.out.printf("CMClientStub.deregisterUser(), user("+strName+") requested.");
		}

		se = null;
		return;
	}
	
	/**
	 * Finds a registered user.
	 * 
	 * <p> A user can search for another user by the findRegisteredUser method of the CM client stub. 
	 * If a client is connected to the default server, it can call this method. When requested, 
	 * CM provides the basic profile of the target user such as a name and registration time.
	 * <br> Whether the requested user is found or not is set to a return code of a reply session event, 
	 * {@link CMSessionEvent#FIND_REGISTERED_USER_ACK}.
	 * 
	 * @param strName - the user name
	 */
	public void findRegisteredUser(String strName)
	{
		int nState = -1;

		// check if the user is connected to a default server
		nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT )
		{
			System.out.println("CMClientStub.findRegisteredUser(), client is not connected to "
					+ "the default server!");
			return;
		}

		// make a request event
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.FIND_REGISTERED_USER);
		se.setUserName(strName);

		// send the request (a default server will send back FIND_REGISTERED_USER_ACK event)
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);

		if(CMInfo._CM_DEBUG)
		{
			System.out.printf("CMClientStub.findRegisteredUser(), user("+strName+") requested.");
		}

		se = null;
		return;
	}
	
	/**
	 * Gets current group members in the default server.
	 * 
	 * <p> A CM client can call this method only if it logs in to the default server.
	 * @return group members.
	 */
	public CMMember getGroupMembers()
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMUser myself = interInfo.getMyself();

		// check if the user is connected to a default server
		int nState = getMyself().getState();
		if( nState < CMInfo.CM_LOGIN )
		{
			System.err.println("CMClientStub.getGroupMembers(), you should log in to "
					+ "the default server!");
			return null;
		}
		else if( nState < CMInfo.CM_SESSION_JOIN )
		{
			System.err.println("CMClientStub.getGroupMembers(), you should join a session "
					+ "of the default server!");
			return null;
		}

		String strSession = myself.getCurrentSession();
		String strGroup = myself.getCurrentGroup();
		CMSession session = interInfo.findSession(strSession);
		if(session == null)
		{
			System.err.println("CMClientStub.getGroupMembers(), session("+strSession
					+") not found!");
			return null;
		}
		CMGroup group = session.findGroup(strGroup);
		if(group == null)
		{
			System.err.println("CMClientStub.getGroupMembers(), group("+strGroup
					+") not found in session("+strSession+")!");
			return null;
		}
		
		return group.getGroupUsers();
	}
	
	/**
	 * Adds a new friend user.
	 * 
	 * <p> A client can add a user as its friend only if the user name has been registered to CM. 
	 * When the default server receives the request for adding a new friend, it first checks 
	 * if the friend is a registered user or not. If the friend is a registered user, the server 
	 * adds it to the friend table of the CM DB as a friend of the requesting user. Otherwise, 
	 * the request fails. In any case, the server sends the ADD_NEW_FRIEND_ACK event with a result 
	 * code to the requesting client so that it can figure out the request result.
	 * The detailed information of the ADD_NEW_FRIEND_ACK event is described below.
	 * 
	 * <table border=1>
	 * <caption>CMSNSEvent.ADD_NEW_FRIEND_ACK event</caption>
	 * <tr>
	 * <td bgcolor="lightgrey">Event type</td><td>CMInfo.CM_SNS_EVENT</td>
	 * </tr>
	 * <tr>
	 * <td bgcolor="lightgrey">Event ID</td><td>CMSNSEvent.ADD_NEW_FRIEND_ACK</td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 * <td>Event field</td><td>Field data type</td><td>Field definition</td><td>Get method</td>
	 * </tr>
	 * <tr>
	 * <td>Return code</td><td>int</td>
	 * <td>Result of the request <br>1: succeeded<br>0: failed
	 * </td>
	 * <td>getReturnCode()</td>
	 * </tr>
	 * <tr>
	 * <td>User name</td><td>String</td><td>Requester user name</td><td>getUserName()</td>
	 * </tr>
	 * <tr>
	 * <td>Friend name</td><td>String</td><td>Friend name</td><td>getFriendName()</td>
	 * </tr>
	 * </table>
	 * 
	 * @param strFriendName - the friend name
	 * @see CMClientStub#removeFriend(String)
	 */
	public void addNewFriend(String strFriendName)
	{
		int nState = -1;

		// check if the user is connected to a default server
		nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT || nState == CMInfo.CM_CONNECT)
		{
			System.out.println("CMClientStub.addNewFriend(), you should log in to "
					+ "the default server!");
			return;
		}
		
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.ADD_NEW_FRIEND);
		se.setUserName(getMyself().getName());
		se.setFriendName(strFriendName);
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);
		
		se = null;
		return;
	}
	
	/**
	 * Removes a friend.
	 * 
	 * <p> When the default server receives the request for deleting a friend, it searches for 
	 * the friend of the requesting user. If the friend is found, the server deletes 
	 * the corresponding entry from the friend table. Otherwise, the request fails. 
	 * The result of the request is sent to the requesting client as the REMOVE_FRIEND_ACK event 
	 * with a result code.
	 * The detailed information of the REMOVE_FRIEND_ACK event is described below.
	 * 
	 * <table border=1>
	 * <caption>CMSNSEvent.REMOVE_FRIEND_ACK event</caption>
	 * <tr>
	 * <td bgcolor="lightgrey">Event type</td><td>CMInfo.CM_SNS_EVENT</td>
	 * </tr>
	 * <tr>
	 * <td bgcolor="lightgrey">Event ID</td><td>CMSNSEvent.REMOVE_FRIEND_ACK</td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 * <td>Event field</td><td>Field data type</td><td>Field definition</td><td>Get method</td>
	 * </tr>
	 * <tr>
	 * <td>Return code</td><td>int</td>
	 * <td>Result of the request <br>1: succeeded<br>0: failed
	 * </td>
	 * <td>getReturnCode()</td>
	 * </tr>
	 * <tr>
	 * <td>User name</td><td>String</td><td>Requester user name</td><td>getUserName()</td>
	 * </tr>
	 * <tr>
	 * <td>Friend name</td><td>String</td><td>Friend name</td><td>getFriendName()</td>
	 * </tr>
	 * </table>
	 * 
	 * @param strFriendName - the friend name
	 * @see CMClientStub#addNewFriend(String)
	 */
	public void removeFriend(String strFriendName)
	{
		int nState = -1;

		// check if the user is connected to a default server
		nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT || nState == CMInfo.CM_CONNECT)
		{
			System.out.println("CMClientStub.removeFriend(), you should log in to "
					+ "the default server!");
			return;
		}
		
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.REMOVE_FRIEND);
		se.setUserName(getMyself().getName());
		se.setFriendName(strFriendName);
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);
		
		se = null;
		return;
	}
	
	/**
	 * Requests to retrieve current friends list of this client.
	 * 
	 * <p> Different SNS applications use the concept of a friend in different ways. 
	 * In some applications, a user can add another user in his/her friend list without 
	 * the agreement of the target user. In other applications, a user can add a friend 
	 * only if the other user accepts the friend request. CM supports such different policies 
	 * of the friend management by methods that request different user lists. 
	 * <br>The requestFriendsList method requests the list of users whom the requesting user adds 
	 * as his/her friends regardless of the acceptance of the others. 
	 * <br>The {@link CMClientStub#requestFriendRequestersList()} method requests the list of users 
	 * who add the requesting user as a friend, but whom the requesting user has not added 
	 * as friends yet. 
	 * <br>The {@link CMClientStub#requestBiFriendsList()} method requests the list 
	 * of users who add the requesting user as a friend and whom the requesting user adds as friends. 
	 * 
	 * <p> When the default server receives the request for friends, requesters, or bi-friends 
	 * from a client, it sends corresponding user list as the RESPONSE_FRIEND_LIST, 
	 * RESPONSE_FRIEND_REQUESTER_LIST, or RESPONSE_BI_FRIEND_LIST event to the requesting client. 
	 * The three events have the same event fields as described below. 
	 * One of the event fields is the friend list, but the meaning of the list is different 
	 * according to an event ID. The friend list contains a maximum of 50 user names. 
	 * If the total number exceeds 50, the server then sends the event more than once.
	 * 
	 * <table border=1>
	 * <caption>CMSNSEvent.RESPONSE_FRIEND_LIST, RESPONSE_FRIEND_REQUESTER_LIST, 
	 * RESPONSE_BI_FRIEND_LIST events</caption>
	 * <tr>
	 * <td bgcolor="lightgrey">Event type</td><td>CMInfo.CM_SNS_EVENT</td>
	 * </tr>
	 * <tr>
	 * <td bgcolor="lightgrey">Event ID</td>
	 * <td>CMSNSEvent.RESPONSE_FRIEND_LIST<br>CMSNSEvent.RESPONSE_FRIEND_REQUESTER_LIST
	 * <br>CMSNSEvent.RESPONSE_BI_FRIEND_LIST</td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 * <td>Event field</td><td>Field data type</td><td>Field definition</td><td>Get method</td>
	 * </tr>
	 * <tr>
	 * <td>User name</td><td>String</td><td>Requester user name</td><td>getUserName()</td>
	 * </tr>
	 * <tr>
	 * <td>Total number of friends</td><td>int</td><td>Total number of requested friends</td>
	 * <td>getTotalNumFriends()</td>
	 * </tr>
	 * <tr>
	 * <td>Number of friends</td><td>int</td><td>Number of requested friends in this event</td>
	 * <td>getNumFriends()</td>
	 * </tr>
	 * <tr>
	 * <td>Friend list</td><td>ArrayList&lt;String&gt;</td><td>List of requested friend names</td>
	 * <td>getFriendList()</td>
	 * </tr>
	 * </table>
	 * 
	 * @see CMClientStub#requestFriendRequestersList()
	 * @see CMClientStub#requestBiFriendsList()
	 */
	public void requestFriendsList()
	{
		int nState = -1;

		// check if the user is connected to a default server
		nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT || nState == CMInfo.CM_CONNECT)
		{
			System.out.println("CMClientStub.requestFriendsList(), you should log in to "
					+ "the default server!");
			return;
		}
		
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.REQUEST_FRIEND_LIST);
		se.setUserName(getMyself().getName());
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);
		
		se = null;
		return;
	}
	
	/**
	 * Requests to retrieve current friend-requesters list of this client.
	 * 
	 * <p> The detailed information is described in the {@link CMClientStub#requestFriendsList()} method.
	 * 
	 * @see CMClientStub#requestFriendsList()
	 * @see CMClientStub#requestBiFriendsList()
	 */
	public void requestFriendRequestersList()
	{
		int nState = -1;

		// check if the user is connected to a default server
		nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT || nState == CMInfo.CM_CONNECT)
		{
			System.out.println("CMClientStub.requestFriendRequestersList(), you should log in to "
					+ "the default server!");
			return;
		}
		
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.REQUEST_FRIEND_REQUESTER_LIST);
		se.setUserName(getMyself().getName());
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);
		
		se = null;
		return;
	}
	
	/**
	 * Requests to retrieve current bi-friends list of this client.
	 * 
	 * <p> The detailed information is described in the {@link CMClientStub#requestFriendsList()} method.
	 * 
	 * @see CMClientStub#requestFriendsList()
	 * @see CMClientStub#requestFriendRequestersList()
	 */
	public void requestBiFriendsList()
	{
		int nState = -1;

		// check if the user is connected to a default server
		nState = getMyself().getState();
		if( nState == CMInfo.CM_INIT || nState == CMInfo.CM_CONNECT)
		{
			System.out.println("CMClientStub.requestBiFriendsList(), you should log in to "
					+ "the default server!");
			return;
		}
		
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.REQUEST_BI_FRIEND_LIST);
		se.setUserName(getMyself().getName());
		
		String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		send(se, strDefServer);
		
		se = null;
		return;
	}
	
	/**
	 * gets the string representation of current channels information.
	 * 
	 * <p> This method overrides the {@link CMStub#getCurrentChannelInfo()} method.
	 * It firstly calls the parent method to get the current datagram channel information, 
	 * and then also gets the current blocking/non-blocking socket channel mostly to servers.
	 * 
	 * @return string of current channels information if successful, or null otherwise.
	 * 
	 * @see CMStub#getCurrentChannelInfo()
	 * @see CMServerStub#getCurrentChannelInfo()
	 */
	@Override
	public String getCurrentChannelInfo()
	{
		StringBuffer sb = new StringBuffer();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMServer defaultServer = interInfo.getDefaultServerInfo();
		String strChInfo = null;
		
		// add datagram channel info of the CMStub class
		String strSuperChInfo = super.getCurrentChannelInfo();
		if(strSuperChInfo != null)
			sb.append(strSuperChInfo);
		
		// add socket channel info of the default server
		sb.append("==== default server: "+defaultServer.getServerName()+"\n");
		
		strChInfo = defaultServer.getNonBlockSocketChannelInfo().toString();
		if(strChInfo != null)
		{
			sb.append("-- non-blocking socket channel\n");
			sb.append(strChInfo);
		}
		
		strChInfo = defaultServer.getBlockSocketChannelInfo().toString();
		if(strChInfo != null)
		{
			sb.append("-- blocking socket channel\n");
			sb.append(strChInfo);
		}
		
		// add socket channel info of additional servers
		Vector<CMServer> addServerList = interInfo.getAddServerList();
		if(!addServerList.isEmpty())
		{
			Iterator<CMServer> iter = addServerList.iterator();
			while(iter.hasNext())
			{
				CMServer addServer = iter.next();
				sb.append("==== additional server: "+addServer.getServerName()+"\n");
				strChInfo = addServer.getNonBlockSocketChannelInfo().toString();
				if(strChInfo != null)
				{
					sb.append("-- non-blocking socket channel\n");
					sb.append(strChInfo);
				}
				strChInfo = addServer.getBlockSocketChannelInfo().toString();
				if(strChInfo != null)
				{
					sb.append("-- blocking socket channel\n");
					sb.append(strChInfo);
				}
			}
		}
		
		// add multicast and socket channel info of the current group
		CMUser myself = interInfo.getMyself();
		if(myself == null)
		{
			System.err.println("CMClientStub.getCurrentChannelInfo(): the client info not found!");
			return null;
		}
		String strCurrentSession = myself.getCurrentSession();
		String strCurrentGroup = myself.getCurrentGroup();
		if(strCurrentSession != null && strCurrentGroup != null)
		{
			sb.append("=== session("+strCurrentSession+"), group("+strCurrentGroup+")\n");
			
			CMSession curSession = interInfo.findSession(strCurrentSession);
			if(curSession != null)
			{
				CMGroup curGroup = curSession.findGroup(strCurrentGroup);
				if(curGroup != null)
				{
					strChInfo = curGroup.getMulticastChannelInfo().toString();
					if(strChInfo != null)
					{
						sb.append("--- multicast channels: \n");
						sb.append(strChInfo);
					}
					
					// add socket channel of group members
					CMMember groupMember = curGroup.getGroupUsers();
					Vector<CMUser> groupVector = groupMember.getAllMembers();
					Iterator<CMUser> iter = groupVector.iterator();
					while(iter.hasNext())
					{
						CMUser groupUser = iter.next();
						strChInfo = groupUser.getNonBlockSocketChannelInfo().toString();
						if(strChInfo != null)
						{
							sb.append("--- non-blocking socket channel of group user("
									+groupUser.getName()+")\n");
							sb.append(strChInfo);
						}
						strChInfo = groupUser.getBlockSocketChannelInfo().toString();
						if(strChInfo != null)
						{
							sb.append("--- blocking socket channel of group user("
									+groupUser.getName()+")\n");
							sb.append(strChInfo);
						}
					}
				}
			}
		}
		
		return sb.toString();
	}
	
}
