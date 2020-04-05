package kr.ac.konkuk.ccslab.cm.stub;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMThreadInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMInteractionManager;
import kr.ac.konkuk.ccslab.cm.manager.CMSNSManager;
import kr.ac.konkuk.ccslab.cm.thread.CMServerKeepAliveTask;

/**
 * This class provides APIs, through which a server developer can access the communication services of CM.
 * A server application can use this class in order to request service-specific communication services.
 * 
 * @author CCSLab, Konkuk University
 * @see CMClientStub
 * @see CMStub
 */
public class CMServerStub extends CMStub {

	/**
	 * Creates an instance of the CMServerStub class.
	 * 
	 * <p> This method just called the default constructor of the super class, CMStub. 
	 */
	public CMServerStub()
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
	 * the configuration file (cm-server.conf), or false otherwise.
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
		confPath = confPath.resolve("cm-server.conf");
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), "FILE_PATH", dir.toString());
		
		return bRet;
	}
	
	/**
	 * Sets server address to the server configuration file.
	 * 
	 * <p> This method must be called before an application starts CM because it updates the value of "SERVER_ADDR" 
	 * field in the server configuration file (cm-server.conf).
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
			System.err.println("CMServerStub.setServerAddress(), CM already has started!");
			return false;
		}
		
		// get the configuration file path
		Path confPath = getConfigurationHome().resolve("cm-server.conf");
		
		// set the server address
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), "SERVER_ADDR", strAddress);
		
		return bRet;
	}
	
	/**
	 * Gets server address from the server configuration file.
	 * 
	 * @return server address
	 * @see CMServerStub#setServerAddress(String)
	 */
	public String getServerAddress()
	{
		String strServerAddress = null;
		Path confPath = getConfigurationHome().resolve("cm-server.conf");
		strServerAddress = CMConfigurator.getConfiguration(confPath.toString(), "SERVER_ADDR");
		return strServerAddress;
	}
	
	/**
	 * Sets server port number to the server configuration file.
	 * 
	 * <p> This method must be called before an application starts CM because it updates the value of "SERVER_PORT" 
	 * field in the server configuration file (cm-server.conf). 
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
			System.err.println("CMServerStub.setServerPort(), CM already has started!");
			return false;
		}
		
		// get the configuration file path
		Path confPath = getConfigurationHome().resolve("cm-server.conf");
				
		// set the server address
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), "SERVER_PORT", String.valueOf(nPort));
		
		return bRet;
	}
	
	/**
	 * Gets server port number from the server configuration file.
	 * 
	 * @return server port number
	 * @see CMServerStub#setServerPort(int)
	 */
	public int getServerPort()
	{
		int nServerPort = -1;
		Path confPath = getConfigurationHome().resolve("cm-server.conf");
		nServerPort = Integer.parseInt(CMConfigurator.getConfiguration(confPath.toString(), "SERVER_PORT"));
		return nServerPort;
	}
	
	/**
	 * Sets server address and port number to the server configuration file.
	 * 
	 * <p> This method must be called before an application starts CM because it updates the values of "SERVER_ADDR"  
	 * and "SERVER_PORT" fields in the server configuration file (cm-server.conf). 
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
	 * Initializes and starts the server CM.
	 * <p> Before the server CM starts, it initializes the configuration and the interaction manager. Then, 
	 * it starts two separate threads for receiving and processing CM events.
	 *  
	 * @return true if the initialization of CM succeeds, or false if the initialization of CM fails.
	 * @see CMServerStub#terminateCM()
	 */
	public boolean startCM()
	{
		super.init();	// initialize CMStub
		
		boolean bRet = false;
		
		/*
		if(m_cmInfo.isStarted())
		{
			System.err.println("CMServerStub.startCM(), already started!");
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

		String strConfPath = m_cmInfo.getConfigurationInfo().getConfFileHome().resolve("cm-server.conf").toString();
		bRet = CMConfigurator.init(strConfPath, m_cmInfo);
		if(!bRet)
			return false;
		

		bRet = CMInteractionManager.init(m_cmInfo);
		if(!bRet)
			return false;

		CMEventManager.startReceivingEvent(m_cmInfo);
		CMCommManager.startReceivingMessage(m_cmInfo);
		CMCommManager.startSendingMessage(m_cmInfo);
		
		int nKeepAliveTime = m_cmInfo.getConfigurationInfo().getKeepAliveTime();
		if(nKeepAliveTime > 0)
		{
			// start keep-alive task
			CMThreadInfo threadInfo = m_cmInfo.getThreadInfo();
			ScheduledExecutorService ses = threadInfo.getScheduledExecutorService();
			CMServerKeepAliveTask keepAliveTask = new CMServerKeepAliveTask(m_cmInfo);
			ScheduledFuture<?> future = ses.scheduleWithFixedDelay(keepAliveTask, 
					1, 1, TimeUnit.SECONDS);
			threadInfo.setScheduledFuture(future);
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMServerStub.startCM(), keep-alive time("+nKeepAliveTime
						+"), start the server keep-alive task.");
			}
		}
		
		m_cmInfo.setStarted(true);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMServerStub.startCM(), succeeded.");
		
		return true;
	}
	
	/**
	 * Terminates the server CM.
	 * <br>A server application calls this method when it does not need to use CM any more. 
	 * The server releases all the resources that are used by CM.
	 * 
	 * @see CMServerStub#startCM()
	 */
	public void terminateCM()
	{
		super.terminateCM();
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMServerStub.terminateCM(), succeeded.");
	}
	
	/**
	 * Registers an additional server to the default server.
	 * 
	 * <p> When an additional server starts, it automatically connects to the default server. 
	 * The additional server then needs to request for registration to the default server in order to 
	 * participate in current CM network.
	 * <br> Only an additional server should call the requestServerReg method with a desired server name. 
	 * Because the default server has the reserved name, "SERVER", the additional server must specify 
	 * a different name as the parameter of this method.
	 * <br> In order for a requesting server to check the result of the registration request, 
	 * the server can catch the RES_SERVER_REG event of the CMMultiServerEvent class in its event handler routine. 
	 * The event fields of this event are described below.
	 * 
	 * <table border=1>
	 * <caption>CMMultiServerEvent.RES_SERVER_REG event</caption>
	 * <tr>
	 * <td bgcolor="lightgrey">Event type</td><td>CMInfo.CM_MULTI_SERVER_EVENT</td>
	 * </tr>
	 * <tr>
	 * <td bgcolor="lightgrey">Event ID</td><td>CMMultiServerEvent.RES_SERVER_REG</td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 * <td>Event field</td><td>Field data type</td><td>Field definition</td><td>Get method</td>
	 * </tr>
	 * <tr>
	 * <td>server name</td><td>String</td><td>the requester server name</td><td>getServerName()</td>
	 * </tr>
	 * <tr>
	 * <td>return code</td><td>int</td>
	 * <td>result code of the registration request
	 * <br>1: succeeded<br>0: failed</td>
	 * <td>getReturnCode()</td>
	 * </tr>
	 * </table> 
	 * 
	 * @param server - the server name
	 * @return true if the request is successfully sent to the default server; false otherwise.
	 * @see CMServerStub#requestServerDereg()
	 */
	public boolean requestServerReg(String server)
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		if(server == null)
		{
			System.err.println("CMServerStub.requestServerReg(), the requesting server name is null.");
			return false;
		}
		if(CMConfigurator.isDServer(m_cmInfo))
		{
			System.err.println("CMServerStub.requestServerReg(), This is the default server!");
			return false;
		}
		if(myself.getState() >= CMInfo.CM_LOGIN)
		{
			System.err.println("CMServerStub.requestServerReg(), already registered as ("
					+myself.getName()+")!");
			return false;
		}
		if(myself.getState() < CMInfo.CM_CONNECT)
		{
			connectToServer();
		}

		CMMultiServerEvent mse = new CMMultiServerEvent();

		mse.setID(CMMultiServerEvent.REQ_SERVER_REG);
		mse.setServerName(server);
		mse.setServerAddress( confInfo.getMyAddress() );
		mse.setServerPort( confInfo.getMyPort() );
		mse.setServerUDPPort( confInfo.getUDPPort() );
		mse.setKeepAliveTime(confInfo.getKeepAliveTime());

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMServerStub.reqServerReg(), server("+server+"), addr("
					+confInfo.getMyAddress()+"), port("+confInfo.getMyPort()+"), udp port("
					+confInfo.getUDPPort()+"), keep-alive("+confInfo.getKeepAliveTime()
					+").");
		}

		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		boolean bRet = CMEventManager.unicastEvent(mse, strDefServer, m_cmInfo);
		if(bRet)
			myself.setName(server);	// to set my server name

		mse = null;
		return bRet;
	}
	
	/**
	 * Deregisters an additional server from the default server.
	 * 
	 * <p> If an additional server leaves current CM network, it can request to deregister from the default server.
	 * Although it leaves the CM network, the additional server still maintains the connection with the default server. 
	 * If required, this connection can also be managed by the {@link CMServerStub#connectToServer()} and 
	 * the {@link CMServerStub#disconnectFromServer()} methods.
	 * 
	 * <br> In order for a requesting server to check the result of the deregistration request, 
	 * the server can catch the RES_SERVER_DEREG event of the CMMultiServerEvent class in its event handler routine. 
	 * The event fields of this event are described below.
	 * 
	 * <table border=1>
	 * <caption>CMMultiServerEvent.RES_SERVER_DEREG event</caption>
	 * <tr>
	 * <td bgcolor="lightgrey">Event type</td><td>CMInfo.CM_MULTI_SERVER_EVENT</td>
	 * </tr>
	 * <tr>
	 * <td bgcolor="lightgrey">Event ID</td><td>CMMultiServerEvent.RES_SERVER_DEREG</td>
	 * </tr>
	 * <tr bgcolor="lightgrey">
	 * <td>Event field</td><td>Field data type</td><td>Field definition</td><td>Get method</td>
	 * </tr>
	 * <tr>
	 * <td>server name</td><td>String</td><td>the requester server name</td><td>getServerName()</td>
	 * </tr>
	 * <tr>
	 * <td>return code</td><td>int</td>
	 * <td>result code of the deregistration request
	 * <br>1: succeeded<br>0: failed</td>
	 * <td>getReturnCode()</td>
	 * </tr>
	 * </table> 
	 * 
	 * @return true is the request is successfully sent to the default server; false otherwise
	 * @see CMServerStub#requestServerReg(String)
	 */
	public boolean requestServerDereg()
	{
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();

		if( CMConfigurator.isDServer(m_cmInfo) )
		{
			System.out.println("CMServerStub.requestServerDereg(), this server is the default "
					+ "server!");
			return false;
		}

		CMUser myself = interInfo.getMyself();
		if( myself.getState() < CMInfo.CM_LOGIN )
		{
			System.out.println("CMServerStub.requestServerDereg(), not registered yet!");
			return false;
		}

		CMMultiServerEvent mse = new CMMultiServerEvent();
		mse.setID(CMMultiServerEvent.REQ_SERVER_DEREG);
		mse.setServerName( myself.getName() );

		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		boolean bRet = CMEventManager.unicastEvent(mse, strDefServer, m_cmInfo);
		if(bRet)
		{
			myself.setState(CMInfo.CM_CONNECT);
		}
		mse = null;
		return bRet;
	}
	
	/**
	 * Connects to the default server.
	 * 
	 * <p> An additional server can call this method to establish a connection to 
	 * the default server.
	 * 
	 * @return true if the connection is successfully established, or false otherwise.
	 * @see CMServerStub#disconnectFromServer()
	 */
	public boolean connectToServer()
	{
		boolean result = false;
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		if( CMConfigurator.isDServer(m_cmInfo) )
		{
			System.out.println("CMServerStub.connectToServer(), this is the default server!");
			return false;
		}
		result = CMInteractionManager.connectDefaultServer(m_cmInfo);
		if(result)
		{
			myself.setState(CMInfo.CM_CONNECT);
		}
		
		return result;
	}

	/**
	 * Disconnects from the default server.
	 * 
	 * <p> An additional server can call this method to disconnect the connection from 
	 * the default server.
	 * 
	 * @return true if the connection is successfully disconnected, or false otherwise.
	 * @see CMServerStub#connectToServer()
	 */
	public boolean disconnectFromServer()
	{
		boolean result = false;
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		if( CMConfigurator.isDServer(m_cmInfo) )
		{
			System.out.println("CMServerStub.disconnectFromServer(), this is the default server!");
			return false;
		}
		result = CMInteractionManager.disconnectFromDefaultServer(m_cmInfo);
		if(result)
		{
			myself.setState(CMInfo.CM_INIT);
		}
		return result;
	}
	
	/**
	 * Sets the download scheme for attached images of SNS content.
	 *
	 * <p> The detailed information about the attachment download scheme can be found 
	 * in the following reference: 
	 * <br> <i>Mingyu Lim, "Multi-level Content Transmission Mechanism for Intelligent Quality of Service 
	 * in Social Networking Services," The Transactions on the Korean Institute of Electrical Engineers, 
	 * Vol. 65, No. 8, August 2016, pp.1407-1417.</i>
	 * 
	 * @param strUserName - the target user name
	 * <br> The attachment download scheme is applied to 'strUserName'. If the value is null, 
	 * the download scheme is applied to all users.
	 * @param nScheme - the download scheme
	 * <br> The possible value is CMInfo.SNS_ATTACH_FULL(or 0), CMInfo.SNS_ATTACH_PARTIAL(or 1), 
	 * CMInfo.SNS_ATTACH_PREFETCH(or 2) and CMInfo.SNS_ATTACH_NONE(or 3).
	 * <table border=1>
	 * <caption>Download scheme of attached images of SNS content</caption>
	 * 	<tr bgcolor=lightgrey>
	 * 		<td>download scheme</td><td>description</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>CMInfo.SNS_ATTACH_FULL</td>
	 * 		<td>
	 * 			The CM server sends images with the original quality to the client.
	 * 		</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>CMInfo.SNS_ATTACH_PARTIAL</td>
	 * 		<td>
	 * 			The server sends thumbnail images instead of the original images.
	 * 		</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>CMInfo.SNS_ATTACH_PREFETCH</td>
	 * 		<td>
	 * 			The server sends thumbnail images to the client, and sends also original 
	 * 			images that the client is interested in.
	 * 		</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>CMInfo.SNS_ATTACH_NONE</td>
	 * 		<td>
	 * 			The server sends only text links to images.
	 * 		</td>
	 * 	</tr>
	 * </table>
	 * @see CMClientStub#requestSNSContent(String, int)
	 */
	// change the download scheme for the attachment of SNS content
	public void setAttachDownloadScheme(String strUserName, int nScheme)
	{
		// set the scheme for the user
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		CMMember loginUsers = interInfo.getLoginUsers();
		CMUser tuser = null;
		int nPrevScheme = -1;
				
		// make an event
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.CHANGE_ATTACH_DOWNLOAD_SCHEME);
		se.setAttachDownloadScheme(nScheme);

		if(strUserName == null) // change all users
		{
			// change current scheme in the configuration info for late comers
			confInfo.setAttachDownloadScheme(nScheme);

			for(int i=0; i<loginUsers.getMemberNum(); i++)
			{
				tuser = loginUsers.getAllMembers().elementAt(i);
				nPrevScheme = tuser.getAttachDownloadScheme();
				tuser.setAttachDownloadScheme(nScheme);
				if(nPrevScheme != CMInfo.SNS_ATTACH_PREFETCH && nScheme == CMInfo.SNS_ATTACH_PREFETCH)
				{
					// load history info for attachment access of this user
					CMSNSManager.loadAccessHistory(tuser, m_cmInfo);
				}
				else if(nPrevScheme == CMInfo.SNS_ATTACH_PREFETCH && nScheme != CMInfo.SNS_ATTACH_PREFETCH)
				{
					// save the updated or newly added history info for attachment access of this user
					CMSNSManager.saveAccessHistory(tuser, m_cmInfo);
				}

			}
			broadcast(se);
		}
		else
		{
			tuser = loginUsers.findMember(strUserName);
			if(tuser == null)
			{
				System.err.println("CMServerStub.setAttachDownloadScheme(), user("+strUserName+") not found!");
				se = null;
				return;
			}
			
			nPrevScheme = tuser.getAttachDownloadScheme();
			tuser.setAttachDownloadScheme(nScheme);
			if(nPrevScheme != CMInfo.SNS_ATTACH_PREFETCH && nScheme == CMInfo.SNS_ATTACH_PREFETCH)
			{
				// load history info for attachment access of this user
				CMSNSManager.loadAccessHistory(tuser, m_cmInfo);
			}
			else if(nPrevScheme == CMInfo.SNS_ATTACH_PREFETCH && nScheme != CMInfo.SNS_ATTACH_PREFETCH)
			{
				// save the updated or newly added history info for attachment access of this user
				CMSNSManager.saveAccessHistory(tuser, m_cmInfo);
			}

			send(se, strUserName);
		}

		se = null;
		return;
	}
	
	/**
	 * Returns a blocking socket (TCP) channel to a client.
	 * 
	 * <p> When a client adds a blocking socket channel, the server also creates and adds a corresponding blocking 
	 * socket channel to communicate with the client. The server can retrieve such a blocking socket channel 
	 * with this method.
	 * 
	 * @param nChKey - the channel key.
	 * @param strUserName - the user name to which the socket channel is connected.
	 * @return the blocking socket channel, or null if the channel is not found.
	 * 
	 * @see CMStub#getBlockDatagramChannel(int)
	 */
	public SocketChannel getBlockSocketChannel(int nChKey, String strUserName)
	{
		SocketChannel sc = null;
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMMember loginUsers = interInfo.getLoginUsers();
		CMUser user = loginUsers.findMember(strUserName);
		if(user == null)
		{
			System.err.println("user("+strUserName+") not found!");
			return null;
		}
		
		sc = (SocketChannel) user.getBlockSocketChannelInfo().findChannel(nChKey);
		return sc;
	}
	
	/**
	 * gets the string representation of current channels information.
	 * 
	 * <p> This method overrides the {@link CMStub#getCurrentChannelInfo()} method.
	 * It firstly calls the parent method to get the current datagram channel information, 
	 * and then also gets the current blocking/non-blocking socket channel mostly to connected clients.
	 * 
	 * @return string of current channels information if successful, or null otherwise.
	 * 
	 * @see CMStub#getCurrentChannelInfo()
	 * @see CMClientStub#getCurrentChannelInfo()
	 */
	@Override
	public String getCurrentChannelInfo()
	{
		StringBuffer sb = new StringBuffer();
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		CMCommInfo commInfo = m_cmInfo.getCommInfo();
		String strChInfo = null;
		
		// add datagram channel info of the CMStub class
		String strSuperChInfo = super.getCurrentChannelInfo();
		if(strSuperChInfo != null)
			sb.append(strSuperChInfo);
				
		// add socket channel info of the login users
		CMMember loginUsers = interInfo.getLoginUsers();
		if(!loginUsers.isEmpty())
		{
			Vector<CMUser> loginUserList = loginUsers.getAllMembers();
			Iterator<CMUser> iter = loginUserList.iterator();

			while(iter.hasNext())
			{
				CMUser user = iter.next();
				sb.append("==== user: "+user.getName()+"\n");
				strChInfo = user.getNonBlockSocketChannelInfo().toString();
				if(strChInfo != null)
				{
					sb.append("-- non-blocking socket channel\n");
					sb.append(strChInfo);
				}
				strChInfo = user.getBlockSocketChannelInfo().toString();
				if(strChInfo != null)
				{
					sb.append("-- blocking socket channel\n");
					sb.append(strChInfo);
				}
			}
		}
		
		// add multicast channel info of every session and group
		Vector<CMSession> sessionList = interInfo.getSessionList();
		Iterator<CMSession> sessionIter = null;
		if(sessionList == null)
		{
			System.err.println("CMServerStub.getCurrentChannelInfo(): There is no session!");
			return null;
		}
		sb.append("==== multicast channels\n");
		sessionIter = sessionList.iterator();
		while(sessionIter.hasNext())
		{
			CMSession session = sessionIter.next();
			if(session != null)
			{
				
				Vector<CMGroup> groupList = session.getGroupList();
				if(groupList != null)
				{
					Iterator<CMGroup> groupIter = groupList.iterator();
					while(groupIter.hasNext())
					{
						CMGroup group = groupIter.next();
						if(group != null)
						{
							strChInfo = group.getMulticastChannelInfo().toString();
							if(strChInfo != null)
							{
								sb.append("-- session("+session.getSessionName()+"), group("
										+group.getGroupName()+")\n");
								sb.append(strChInfo);
							}
						}
					}
				}
				
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Returns the list of currently logged-in users in this server.
	 * 
	 * @return the list of logged-in users.
	 */
	public CMMember getLoginUsers()
	{
		CMMember loginUsers = null;
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();
		loginUsers = interInfo.getLoginUsers();
		
		return loginUsers;
	}
	
}
