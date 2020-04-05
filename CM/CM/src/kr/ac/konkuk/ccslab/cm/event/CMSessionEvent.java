package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * This class represents CM events that are used for session related tasks.
 * 
 * @author CCSLab, Konkuk University
 * @see CMEvent
 */
public class CMSessionEvent extends CMEvent {

	/**
	 * The event ID for login request from a client to the default server.
	 * <p>event direction: client -&gt; default server
	 * <p>The LOGIN event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#loginCM(String, String)} or 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#syncLoginCM(String, String)}.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name : {@link CMSessionEvent#getUserName()}</li>
	 * <li>password : {@link CMSessionEvent#getPassword()}</li>
	 * <li>host address : {@link CMSessionEvent#getHostAddress()}</li>
	 * <li>UDP port of the user : {@link CMSessionEvent#getUDPPort()}</li>
	 * <li>keep-alive time of the user : {@link CMSessionEvent#getKeepAliveTime()}</li>
	 * </ul>
	 */
	public static final int LOGIN = 1;
	
	/**
	 * The event ID for logout request from a client to the default server.
	 * <p>event direction: client -&gt; default server
	 * <p>The LOGOUT event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#logoutCM()}.
	 * <br>The following field is used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * </ul>
	 */
	public static final int LOGOUT = 2;
	
	/**
	 * The event ID for response to the login request from the default server to the client.
	 * <p>event direction: default server -&gt; client
	 * <p>The default server sends the LOGIN_ACK event to the client as the response to 
	 * the {@link CMSessionEvent#LOGIN} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>response code: {@link CMSessionEvent#isValidUser()}
	 * <br>1: valid user, 0: invalid user</li>
	 * 
	 * <li>communication architecture: {@link CMSessionEvent#getCommArch()}
	 * <br>CM_CS: This CM network is client-server model
	 * <br>CM_PS: This CM network is peer-server model</li>
	 * 
	 * <li>file-transfer scheme: {@link CMSessionEvent#isFileTransferScheme()}
	 * <br>1: to use a separate channel and thread to transfer a file
	 * <br>0: to use the default socket channel to transfer a file</li>
	 * 
	 * <li>login scheme: {@link CMSessionEvent#isLoginScheme()}
	 * <br>1: The default server authenticates the requesting user
	 * <br>0: The default server does not authenticates the requesting user</li>
	 * 
	 * <li>session scheme: {@link CMSessionEvent#isSessionScheme()}
	 * <br>1: The login user should select and join a session.
	 * <br>0: The login user automatically joins a default session.</li>
	 * 
	 * <li>download scheme of attached files in SNS content: 
	 * {@link CMSessionEvent#getAttachDownloadScheme()}
	 * <br>0: full mode, 1: partial mode, 2: prefetch mode, 3: none</li>
	 * 
	 * <li>server UDP port: {@link CMSessionEvent#getUDPPort()}</li>
	 * </ul>
	 */
	public static final int LOGIN_ACK = 3;
	
	/**
	 * The event ID for requesting available session information from the default server.
	 * <p>event direction: client -&gt; default server
	 * <p>The REQUEST_SESSION_INFO event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#requestSessionInfo()}.
	 * <br>The following field is used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}
	 * </ul>
	 */
	public static final int REQUEST_SESSION_INFO = 4;
	
	/**
	 * The event ID for the response to the request of available session information.
	 * <p>event direction: default server -&gt; client
	 * <p>The RESPONSE_SESSION_INFO event is the reply of 
	 * the {@link CMSessionEvent#REQUEST_SESSION_INFO} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>number of sessions: {@link CMSessionEvent#getSessionNum()}</li>
	 * <li>list of session information: {@link CMSessionEvent#getSessionInfoList()}</li>
	 * </ul>
	 */
	public static final int RESPONSE_SESSION_INFO = 5;
	
	/**
	 * The event ID for the request of joining a session.
	 * <p>event direction: client -&gt; default server
	 * <p>The JOIN_SESSION event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#joinSession(String)} and 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#syncJoinSession(String)}.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name : {@link CMSessionEvent#getUserName()}</li>
	 * <li>session name : {@link CMSessionEvent#getSessionName()}</li>
	 * </ul>
	 */
	public static final int JOIN_SESSION = 6;
	
	/**
	 * The event ID for the response to the request of joining a session.
	 * <p>event direction: default session -&gt; client
	 * <p>The JOIN_SESSION_ACK event is the reply of 
	 * the {@link CMSessionEvent#JOIN_SESSION} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>number of groups: {@link CMSessionEvent#getGroupNum()}</li>
	 * <li>list of group information: {@link CMSessionEvent#getGroupInfoList()}</li>
	 * </ul>
	 */
	public static final int JOIN_SESSION_ACK = 7;
	
	/**
	 * The event ID for the request of leaving the current session.
	 * <p>event direction: client -&gt; default server
	 * <p>The LEAVE_SESSION event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#leaveSession()}.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * <li>session name: {@link CMSessionEvent#getSessionName()}</li>
	 * </ul>
	 */
	public static final int LEAVE_SESSION = 8;
	
	/* (NOT USED YET!)
	 * The event ID for the response to the request of leaving the current session.
	 * <p>event direction: default server -&gt; client
	 * <p>The LEAVE_SESSION_ACK is the reply of the {@link CMSessionEvent#LEAVE_SESSION} 
	 * event.
	 * <br>The following field is used for this event:
	 * <ul>
	 * <li>return code: {@link CMSessionEvent#getReturnCode()}
	 * </ul>
	 */
	public static final int LEAVE_SESSION_ACK = 9;
	
	/**
	 * The event ID for a chat message of a client which has logged in 
	 * to the server but does not join a session.
	 * <p>event direction: client -&gt; server -&gt; client
	 * <p>The SESSION_TALK event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#chat(String, String)}.
	 * <br>The following fields are used for this event: 
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * <li>chat message: {@link CMSessionEvent#getTalk()}</li>
	 * </ul>
	 */
	public static final int SESSION_TALK = 10;
	
	/**
	 * The event ID for the notification of a new logged-in user.
	 * <p>event direction: default server -&gt; client
	 * <p>The default server sends the SESSION_ADD_USER event to the existing 
	 * users to notify them of a new logged-in user.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * <li>host address: {@link CMSessionEvent#getHostAddress()}</li>
	 * <li>user's current session name: {@link CMSessionEvent#getSessionName()}</li> 
	 * </ul>
	 */
	public static final int SESSION_ADD_USER = 11;
	
	/**
	 * The event ID for the notification of the user logout.
	 * <p>event direction: default server -&gt; client
	 * <p>The default server sends the SESSION_REMOVE_USER event to the existing 
	 * users to notify that a user has logged out from the default server.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * </ul>
	 */
	public static final int SESSION_REMOVE_USER = 12;
	
	/**
	 * The event ID for the notification of a user joining(changing) a session.
	 * <p>event direction: default server -&gt; client
	 * <p>The default server sends the CHANGE_SESSION event to the existing users 
	 * to notify that a user has joined a session.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * <li>session name: {@link CMSessionEvent#getSessionName()}</li>
	 * </ul>
	 */
	public static final int CHANGE_SESSION = 13;
	
	/**
	 * The event ID for the request to add a non-blocking socket channel information.
	 * <p>event direction: client -&gt; server
	 * <p>The ADD_NONBLOCK_SOCKET_CHANNEL is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#addNonBlockSocketChannel(int, String)} 
	 * or 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#syncAddNonBlockSocketChannel(int, String)}.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>channel name: {@link CMSessionEvent#getChannelName()} 
	 * <br>The name of a server to which the client establishes a connection.</li>
	 * <li>channel index: {@link CMSessionEvent#getChannelNum()}
	 * <br>the index(&gt;0) for the socket channel.</li>
	 * </ul>
	 */
	public static final int ADD_NONBLOCK_SOCKET_CHANNEL = 14;	
	
	/**
	 * The event ID for the response to the request of adding a non-blocking socket 
	 * channel information.
	 * <p>event direction: server -&gt; client
	 * <p>The ADD_NONBLOCK_SOCKET_CHANNEL_ACK event is the reply of 
	 * the {@link CMSessionEvent#ADD_NONBLOCK_SOCKET_CHANNEL} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>channel name: {@link CMSessionEvent#getChannelName()}
	 * <br>The name of a server to which the client establishes a connection.</li>
	 * <li>channel index: {@link CMSessionEvent#getChannelNum()}
	 * <br>the index(&gt;0) for the socket channel.</li>
	 * <li>return code: {@link CMSessionEvent#getReturnCode()}
	 * <br>0: channel addition at the server has completed.
	 * <br>other value: channel addition at the server has failed.</li>
	 * </ul>
	 */
	public static final int ADD_NONBLOCK_SOCKET_CHANNEL_ACK = 15;
	
	/**
	 * The event ID for the request to add a blocking socket channel information.
	 * <p>event direction: client -&gt; server
	 * <br>event direction: client -&gt; client in the p2p file transfer
	 * <p>The ADD_BLOCK_SOCKET_CHANNEL is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#addBlockSocketChannel(int, String)} 
	 * or 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#syncAddBlockSocketChannel(int, String)}.
	 * <br> A file sender client also can send this event to a file receiver client 
	 * to make a dedicated channel if the FILE_TRANSFER_SCHEME field is set to 1 in 
	 * the server CM configuration file (cm-server.conf). 
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>channel name: {@link CMSessionEvent#getChannelName()} 
	 * <br>The name of a server to which the client establishes a connection.</li>
	 * <li>channel index: {@link CMSessionEvent#getChannelNum()}
	 * <br>the index(&gt;=0) for the socket channel.</li>
	 * </ul>
	 */
	public static final int ADD_BLOCK_SOCKET_CHANNEL = 22;
	
	/**
	 * The event ID for the response to the request of adding a blocking socket 
	 * channel information.
	 * <p>event direction: server -&gt; client
	 * <p>The ADD_BLOCK_SOCKET_CHANNEL_ACK event is the reply of 
	 * the {@link CMSessionEvent#ADD_BLOCK_SOCKET_CHANNEL} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>channel name: {@link CMSessionEvent#getChannelName()}
	 * <br>The name of a server to which the client establishes a connection.</li>
	 * <li>channel index: {@link CMSessionEvent#getChannelNum()}
	 * <br>the index(&gt;=0) for the socket channel.</li>
	 * <li>return code: {@link CMSessionEvent#getReturnCode()}
	 * <br>0: channel addition at the server has completed.
	 * <br>other value: channel addition at the server has failed.</li>
	 * </ul>
	 */
	public static final int ADD_BLOCK_SOCKET_CHANNEL_ACK = 23;
	
	/**
	 * The event ID for the request to remove a blocking socket channel information.
	 * <p>event direction: client -&gt; server
	 * <br>event direction: client -&gt; client in the p2p file transfer
	 * <p>The REMOVE_BLOCK_SOCKET_CHANNEL is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#removeBlockSocketChannel(int, String)},
	 * or 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#syncRemoveBlockSocketChannel(int, String)}.
	 * <br> A file sender or receiver client also can send this event 
	 * to finish or cancel the file transfer with a dedicated channel if 
	 * the FILE_TRANSFER_SCHEME field is set to 1 in the server CM configuration file 
	 * (cm-server.conf). 
	 * 
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>channel name: {@link CMSessionEvent#getChannelName()} 
	 * <br>The name of a server to which the client establishes a connection.</li>
	 * <li>channel index: {@link CMSessionEvent#getChannelNum()}
	 * <br>the index(&gt;=0) for the socket channel.</li>
	 * </ul>
	 */
	public static final int REMOVE_BLOCK_SOCKET_CHANNEL = 24;
	
	/**
	 * The event ID for the response to the request of removing a blocking socket 
	 * channel information.
	 * <p>event direction: server -&gt; client
	 * <p>The REMOVE_BLOCK_SOCKET_CHANNEL_ACK event is the reply of 
	 * the {@link CMSessionEvent#REMOVE_BLOCK_SOCKET_CHANNEL} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>channel name: {@link CMSessionEvent#getChannelName()}
	 * <br>The name of a server to which the client establishes a connection.</li>
	 * <li>channel index: {@link CMSessionEvent#getChannelNum()}
	 * <br>the index(&gt;=0) for the socket channel.</li>
	 * <li>return code: {@link CMSessionEvent#getReturnCode()}
	 * <br>0: the channel has been removed successfully.
	 * <br>other value: channel removal at the server has failed.</li>
	 * </ul>
	 */
	public static final int REMOVE_BLOCK_SOCKET_CHANNEL_ACK = 25;	

	/**
	 * The event ID for the request of registering a new user from the default server.
	 * <p>event direction: client -&gt; default server
	 * <p>The REGISTER_USER event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#registerUser(String, String)}.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * <li>password: {@link CMSessionEvent#getPassword()}</li>
	 * </ul>
	 */
	public static final int REGISTER_USER = 16;
	
	/**
	 * The event ID for the response to the request of registering a new user.
	 * <p>event direction: default server -&gt; client
	 * <p>The REGISTER_USER_ACK event is the reply of 
	 * the {@link CMSessionEvent#REGISTER_USER} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>return code: {@link CMSessionEvent#getReturnCode()}
	 * <br>1: registration success.
	 * <br>0: registration failed.</li>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * <li>registration date and time: {@link CMSessionEvent#getCreationTime()}</li>
	 * </ul>
	 */
	public static final int REGISTER_USER_ACK = 17;
	
	/**
	 * The event ID for the request of deregistering a user from the default server.
	 * <p>event direction: client -&gt; default server
	 * <p>The DEREGISTER_USER event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#deregisterUser(String, String)}.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * <li>password: {@link CMSessionEvent#getPassword()}</li>
	 * </ul>
	 * (from here)
	 */
	public static final int DEREGISTER_USER = 18;
	
	/**
	 * The event ID for the response to the request of deregistering a user from 
	 * the default server.
	 * <p>event direction: default server -&gt; client
	 * <p>The DEREGISTER_USER_ACK event is the reply of 
	 * the {@link CMSessionEvent#DEREGISTER_USER} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>return code: {@link CMSessionEvent#getReturnCode()}
	 * <br>0: deregistration failed
	 * <br>1: deregistration success</li>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * </ul>
	 */
	public static final int DEREGISTER_USER_ACK = 19;
	
	/**
	 * The event ID for the request of searching for a registered user from 
	 * the default server.
	 * <p>event direction: client -&gt; default server
	 * <p>The FIND_REGISTERED_USER event is sent when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#findRegisteredUser(String)}.
	 * <br>The following field is used for this event:
	 * <ul>
	 * <li>user name: {@link CMSessionEvent#getUserName()}
	 * </ul>
	 */
	public static final int FIND_REGISTERED_USER = 20;
	
	/**
	 * The event ID for the response to the request of searching for a registered user.
	 * <p>event direction: default server -&gt; client
	 * <p>The FIND_REGISTERED_USER_ACK event is the reply of 
	 * the {@link CMSessionEvent#FIND_REGISTERED_USER} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>return code: {@link CMSessionEvent#getReturnCode()}
	 * <br>0: user not found
	 * <br>1: user found</li>
	 * <li>user name: {@link CMSessionEvent#getUserName()}</li>
	 * <li>registration date and time: {@link CMSessionEvent#getCreationTime()}</li>
	 * </ul>
	 */
	public static final int FIND_REGISTERED_USER_ACK = 21;
	
	/**
	 * The event ID for notifying the client of unexpected disconnection from a server.
	 * <p>event direction: client CM -&gt; client application (local event)
	 * <p>The client CM delivers the UNEXPECTED_SERVER_DISCONNECTION event to 
	 * its application when it detects the disconnection from a server CM.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>channel name: {@link CMSessionEvent#getChannelName()} 
	 * <br>name of disconnected server.</li>
	 * <li>channel index: {@link CMSessionEvent#getChannelNum()} 
	 * <br>index of the disconnected non-blocking socket channel.</li>
	 * </ul> 
	 */
	public static final int UNEXPECTED_SERVER_DISCONNECTION = 99;

	/**
	 * The event ID for notifying the application of intentional disconnection from 
	 * a remote node by CM.
	 * <p>event direction: CM -&gt; application (local event)
	 * <p>CM delivers the INTENTIONALLY_DISCONNECT event to its application when it 
	 * disconnects a problematic non-blocking socket channel. 
	 * A non-blocking socket channel is regarded as problematic if 
	 * <br>(1) CM receives a malformed CM event.
	 * <br>(2) the server keep-alive task enables and it detects the expiration of 
	 * the keep-alive time of the channel. The server CM enables the keep-alive task 
	 * if the KEEP_ALIVE_TIME field of the configuration file is set to a value 
	 * greater than 0.
	 * <p>The following fields are used for this event:
	 * <ul>
	 * <li>channel name: {@link CMSessionEvent#getChannelName()}
	 * <br>name of remote node with the problematic socket channel disconnected by 
	 * the local CM.</li>
	 * <li>channel index: {@link CMSessionEvent#getChannelNum()}
	 * <br>index of the problematic non-blocking socket channel disconnected by 
	 * the local CM.</li>
	 * </ul> 
	 */
	public static final int INTENTIONALLY_DISCONNECT = 100;

	private String m_strUserName;
	private String m_strPasswd;
	private String m_strHostAddr;
	private int m_nUDPPort;

	private int m_bValidUser;
	private String m_strSessionName;

	private String m_strCurrentGroupName;
	private String m_strCurrentAddress;
	private int m_nCurrentPort;
	
	private int m_nSessionNum;
	private Vector< CMSessionInfo > m_sessionList;
	private int m_nGroupNum;
	private Vector< CMGroupInfo > m_groupList;

	private String m_strCommArch;
	private int m_bFileTransferScheme;
	private int m_bLoginScheme;
	private int m_bSessionScheme;
	private int m_nAttachDownloadScheme;
	private int m_nReturnCode;
	private String m_strTalk;
	
	private String m_strChannelName;
	private int m_nChannelNum;

	private String m_strCreationTime;
	
	private int m_nKeepAliveTime;

	public CMSessionEvent()
	{
		m_nType = CMInfo.CM_SESSION_EVENT;
		m_strHostAddr = "?";
		m_strPasswd = "?";
		m_strUserName = "?";
		m_nUDPPort = -1;
		m_bValidUser = -1;
		m_strSessionName = "?";
		m_strCurrentGroupName = "?";
		m_strCurrentAddress = "?";
		m_nCurrentPort = -1;
		m_nSessionNum = -1;
		m_nGroupNum = -1;
		m_strCommArch = "?";
		m_bFileTransferScheme = -1;
		m_bLoginScheme = -1;
		m_bSessionScheme = -1;
		m_nAttachDownloadScheme = -1;
		m_nReturnCode = -1;
		m_strTalk = "?";
		m_strChannelName = "?";
		m_nChannelNum = -1;
		m_strCreationTime = "?";
		m_nKeepAliveTime = 0;
		
		m_sessionList = new Vector<CMSessionInfo>();
		m_groupList = new Vector<CMGroupInfo>();
	}
	
	public CMSessionEvent(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}
	
	// set/get methods
	
	public void setHostAddress(String host)
	{
		if(host != null)
			m_strHostAddr = host;
	}
	
	/**
	 * Returns the host IP address.
	 * 
	 * @return the IP address.
	 */
	public String getHostAddress()
	{
		return m_strHostAddr;
	}
	
	public void setUDPPort(int port)
	{
		m_nUDPPort = port;
	}
	
	/**
	 * Returns UDP port number.
	 * 
	 * @return UDP port number.
	 */
	public int getUDPPort()
	{
		return m_nUDPPort;
	}
	
	public void setPassword(String passwd)
	{
		if(passwd != null)
			m_strPasswd = passwd;
	}
	
	/**
	 * Returns the password.
	 * @return password.
	 */
	public String getPassword()
	{
		return m_strPasswd;
	}
	
	public void setUserName(String uname)
	{
		if(uname != null)
			m_strUserName = uname;
	}
	
	/**
	 * Returns the user name.
	 * 
	 * @return user name.
	 */
	public String getUserName()
	{
		return m_strUserName;
	}
	
	public void setValidUser(int bValid)
	{
		m_bValidUser = bValid;
	}
	
	/**
	 * Returns if the user authentication has succeeded or not.
	 * 
	 * @return 1 if the user is authenticated; 0 otherwise.
	 */
	public int isValidUser()
	{
		return m_bValidUser;
	}
	
	public void setSessionName(String sname)
	{
		if(sname != null)
			m_strSessionName = sname;
	}
	
	/**
	 * Returns the session name.
	 * 
	 * @return session name.
	 */
	public String getSessionName()
	{
		return m_strSessionName;
	}
	
	public void setCommArch(String commArch)
	{
		if(commArch != null)
			m_strCommArch = commArch;
	}
	
	/**
	 * Returns the communication architecture of the server.
	 * 
	 * @return communication architecture.
	 * <br>"CM_CS" or "CM_PS".
	 */
	public String getCommArch()
	{
		return m_strCommArch;
	}
	
	public void setFileTransferScheme(int bFileTransferScheme)
	{
		m_bFileTransferScheme = bFileTransferScheme;
	}
	
	/**
	 * Returns if the new file-transfer scheme is on or off.
	 * 
	 * @return 1 if the new file-transfer scheme is on; 0 otherwise.
	 */
	public int isFileTransferScheme()
	{
		return m_bFileTransferScheme;
	}
	
	public void setLoginScheme(int bLoginScheme)
	{
		m_bLoginScheme = bLoginScheme;
	}
	
	/**
	 * Returns if the server authenticates a user or not.
	 * 
	 * @return 1 if the user authentication is used; 0 otherwise.
	 */
	public int isLoginScheme()
	{
		return m_bLoginScheme;
	}
	
	public void setSessionScheme(int bSessionScheme)
	{
		m_bSessionScheme = bSessionScheme;
	}
	
	/**
	 * Returns if the server uses multiple sessions or not.
	 * 
	 * @return 1 if multiple sessions are used; 0 otherwise.
	 */
	public int isSessionScheme()
	{
		return m_bSessionScheme;
	}
	
	public void setAttachDownloadScheme(int nScheme)
	{
		m_nAttachDownloadScheme = nScheme;
	}
	
	/**
	 * Returns the current download mode of an image file attached to SNS content.
	 * 
	 * @return the download mode.
	 * <br>0: full (original image)
	 * <br>1: partial (thumbnail image)
	 * <br>2: prefetch (prefetch original image)
	 * <br>3: none (text information)
	 */
	public int getAttachDownloadScheme()
	{
		return m_nAttachDownloadScheme;
	}
	
	public void setReturnCode(int code)
	{
		m_nReturnCode = code;
	}
	
	/**
	 * Returns the return code.
	 * 
	 * @return 1 for successful return code; 0 otherwise.
	 */
	public int getReturnCode()
	{
		return m_nReturnCode;
	}
	
	public void setTalk(String talk)
	{
		if(talk != null)
			m_strTalk = talk;
	}
	
	/**
	 * Returns the chatting message.
	 * 
	 * @return chatting message.
	 */
	public String getTalk()
	{
		return m_strTalk;
	}
	
	public void setCurrentGroupName(String gname)
	{
		if(gname != null)
			m_strCurrentGroupName = gname;
	}
	
	/**
	 * Returns the current group name.
	 * 
	 * @return the current group name.
	 */
	public String getCurrentGroupName()
	{
		return m_strCurrentGroupName;
	}
	
	public void setCurrentAddress(String addr)
	{
		if(addr != null)
			m_strCurrentAddress = addr;
	}
	
	/**
	 * Returns the current address.
	 * 
	 * @return the current address.
	 */
	public String getCurrentAddress()
	{
		return m_strCurrentAddress;
	}
	
	public void setCurrentPort(int port)
	{
		m_nCurrentPort = port;
	}
	
	/**
	 * Returns the current port number.
	 * 
	 * @return the current port number.
	 */
	public int getCurrentPort()
	{
		return m_nCurrentPort;
	}
	
	public void setChannelName(String name)
	{
		if(name != null)
			m_strChannelName = name;
	}
	
	/**
	 * Returns the channel name.
	 * 
	 * @return the channel name.
	 */
	public String getChannelName()
	{
		return m_strChannelName;
	}
	
	public void setChannelNum(int num)
	{
		m_nChannelNum = num;
	}
	
	/**
	 * Returns the channel index.
	 * 
	 * @return the channel index.
	 */
	public int getChannelNum()
	{
		return m_nChannelNum;
	}
	
	public void setSessionNum(int num)
	{
		m_nSessionNum = num;
	}
	
	/**
	 * Returns the number of sessions.
	 * 
	 * @return the number of sessions.
	 */
	public int getSessionNum()
	{
		return m_nSessionNum;
	}
	
	public void setGroupNum(int num)
	{
		m_nGroupNum = num;
	}
	
	/**
	 * Returns the number of groups.
	 * 
	 * @return the number of groups.
	 */
	public int getGroupNum()
	{
		return m_nGroupNum;
	}
	
	public void setCreationTime(String time)
	{
		if(time != null)
			m_strCreationTime = time;
	}
	
	/**
	 * Returns the user registration date and time.
	 * 
	 * @return the user registration date and time.
	 */
	public String getCreationTime()
	{
		return m_strCreationTime;
	}
	
	public void setKeepAliveTime(int nSecond)
	{
		m_nKeepAliveTime = nSecond;
	}
	
	/**
	 * Returns the keep-alive time.
	 * 
	 * @return the keep-alive time.
	 */
	public int getKeepAliveTime()
	{
		return m_nKeepAliveTime;
	}
	
	public boolean addSessionInfo(CMSessionInfo si)
	{
		if(si == null) return false;
		
		CMSessionInfo tsi = findSessionInfo(si.getSessionName());
		
		if( tsi != null )
		{
			if(CMInfo._CM_DEBUG_2)
				System.err.println("CMSessionEvent.addSessionInfo(), already exists: "+si.getSessionName());
			return false;
		}
		
		m_sessionList.addElement(si);
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMSessionEvent.addSessionInfo(), ok session: "+si.getSessionName());

		return true;
	}
	
	public boolean removeSessionInfo(String sname)
	{
		boolean found = false;
		CMSessionInfo tsi;
		Iterator<CMSessionInfo> iter = m_sessionList.iterator();
		
		if(sname == null) return false;
		
		while(iter.hasNext() && !found)
		{
			tsi = iter.next();
			if( sname.equals(tsi.getSessionName()) )
			{
				iter.remove();
				found = true;
			}
		}

		if(!found)
		{
			if(CMInfo._CM_DEBUG_2)
				System.err.println("CMSessionEvent.removeSessionInfo(), not found: "+sname);
			
			return false;
		}

		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMSessionEvent.removeSessionInfo(), Ok session: "+sname);

		return true;		
	}
	
	public void removeAllSessionInfoObjects()
	{
		/*
		Iterator<CMSessionInfo> iter = m_sessionList.iterator();
		while(iter.hasNext())
		{
			CMSessionInfo si = iter.next();
			si = null;
		}
		*/
		m_sessionList.removeAllElements();
		return;
	}
	
	public CMSessionInfo findSessionInfo(String sname)
	{
		boolean found = false;
		CMSessionInfo tsi = null;
		Iterator<CMSessionInfo> iter = m_sessionList.iterator();
		
		if(sname == null) return null;
		
		while(iter.hasNext() && !found)
		{
			tsi = iter.next();
			if( sname.equals(tsi.getSessionName()) )
			{
				found = true;
			}
		}

		if(!found)
		{
			//if(CMInfo._CM_DEBUG)
			//	System.out.println("CMSessionEvent.findSessionInfo(), not found: "+sname);

			return null;
		}
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMSessionEvent.findSessionInfo(), Ok session: "+sname);

		return tsi;
	}
	
	/**
	 * Returns the list of CMSessionInfo objects.
	 * 
	 * @return the list of CMSessionInfo objects.
	 */
	public Vector<CMSessionInfo> getSessionInfoList()
	{
		return m_sessionList;
	}
	
	public boolean addGroupInfo(CMGroupInfo gi)
	{
		if(gi == null) return false;
		
		CMGroupInfo tgi = findGroupInfo(gi.getGroupName());
		
		if( tgi != null )
		{
			if(CMInfo._CM_DEBUG_2)
				System.err.println("CMSessionEvent.addGroupInfo(), already exists: "+gi.getGroupName());
			return false;
		}
		
		m_groupList.addElement(gi);
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMSessionEvent.addGroupInfo(), ok group: "+gi.getGroupName());

		return true;
	}
	
	public boolean removeGroupInfo(String gname)
	{
		boolean found = false;
		CMGroupInfo tgi;
		Iterator<CMGroupInfo> iter = m_groupList.iterator();
		
		if(gname == null) return false;
		
		while(iter.hasNext() && !found)
		{
			tgi = iter.next();
			if( gname.equals(tgi.getGroupName()) )
			{
				iter.remove();
				found = true;
			}
		}

		if(!found)
		{
			if(CMInfo._CM_DEBUG_2)
				System.err.println("CMSessionEvent.removeGroupInfo(), not found: "+gname);
			
			return false;
		}

		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMSessionEvent.removeGroupInfo(), Ok session: "+gname);

		return true;		
	}
	
	public void removeAllGroupInfoObjects()
	{
		/*
		Iterator<CMGroupInfo> iter = m_groupList.iterator();
		while(iter.hasNext())
		{
			CMGroupInfo gi = iter.next();
			gi = null;
		}
		*/
		m_groupList.removeAllElements();
		return;
	}
	
	public CMGroupInfo findGroupInfo(String gname)
	{
		boolean found = false;
		CMGroupInfo tgi = null;
		Iterator<CMGroupInfo> iter = m_groupList.iterator();
		
		if(gname == null) return null;
		
		while(iter.hasNext() && !found)
		{
			tgi = iter.next();
			if( gname.equals(tgi.getGroupName()) )
			{
				found = true;
			}
		}

		if(!found)
		{
			//if(CMInfo._CM_DEBUG)
			//	System.out.println("CMSessionEvent.findGroupInfo(), not found: "+gname);

			return null;
		}
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMSessionEvent.findGroupInfo(), Ok session: "+gname);

		return tgi;
	}
	
	/**
	 * Returns the list of CMGroupInfo objects.
	 * 
	 * @return the list of CMGroupInfo objects.
	 */
	public Vector<CMGroupInfo> getGroupInfoList()
	{
		return m_groupList;
	}

	/////////////////////////////////////////////
	
	protected int getByteNum()
	{
		Iterator<CMSessionInfo> iterSessionList;
		Iterator<CMGroupInfo> iterGroupList;
		int nElementByteNum = 0;
		
		int nByteNum = 0;
		nByteNum = super.getByteNum();
		
		switch(m_nID)
		{
		case LOGIN:
			nByteNum += 3*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strPasswd.getBytes().length	+ m_strHostAddr.getBytes().length;
			nByteNum += Integer.BYTES;
			nByteNum += Integer.BYTES;	// keep-alive time
			break;
		case LOGOUT:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		case LOGIN_ACK:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strCommArch.getBytes().length;
			nByteNum += 6*Integer.BYTES;
			break;
		case REQUEST_SESSION_INFO:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		case RESPONSE_SESSION_INFO:
			nByteNum += Integer.BYTES;
			nElementByteNum = 0;
			iterSessionList = m_sessionList.iterator();
			while(iterSessionList.hasNext())
			{
				CMSessionInfo tsi = iterSessionList.next();
				nElementByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + tsi.getSessionName().getBytes().length
								+ tsi.getAddress().getBytes().length;
				nElementByteNum += 2*Integer.BYTES;
			}
			nByteNum += nElementByteNum;
			break;
		case JOIN_SESSION:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strSessionName.getBytes().length;
			break;
		case JOIN_SESSION_ACK:
			nByteNum += Integer.BYTES; // group num
			nElementByteNum = 0;
			iterGroupList = m_groupList.iterator();
			while(iterGroupList.hasNext())
			{
				CMGroupInfo tgi = iterGroupList.next();
				nElementByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + tgi.getGroupName().getBytes().length 
								+ tgi.getGroupAddress().getBytes().length;
				nElementByteNum += Integer.BYTES;
			}
			nByteNum += nElementByteNum;
			break;
		case LEAVE_SESSION:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strSessionName.getBytes().length;
			break;
		case LEAVE_SESSION_ACK:
			nByteNum += Integer.BYTES;
			break;
		case SESSION_TALK:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strTalk.getBytes().length;
			break;
		case SESSION_ADD_USER:
			nByteNum += 3*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strHostAddr.getBytes().length + m_strSessionName.getBytes().length;
			break;
		case SESSION_REMOVE_USER:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		case CHANGE_SESSION:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strSessionName.getBytes().length;
			break;
		case ADD_NONBLOCK_SOCKET_CHANNEL:
		case ADD_BLOCK_SOCKET_CHANNEL:
		case REMOVE_BLOCK_SOCKET_CHANNEL:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strChannelName.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
		case ADD_BLOCK_SOCKET_CHANNEL_ACK:
		case REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strChannelName.getBytes().length;
			nByteNum += 2*Integer.BYTES;
			break;
		case REGISTER_USER:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strPasswd.getBytes().length;
			break;
		case REGISTER_USER_ACK:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length 
					+ m_strCreationTime.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case DEREGISTER_USER:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strPasswd.getBytes().length;
			break;
		case DEREGISTER_USER_ACK:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case FIND_REGISTERED_USER:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		case FIND_REGISTERED_USER_ACK:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length 
					+ m_strCreationTime.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		default:
			nByteNum = -1;
			break;
		}
		
		return nByteNum;
	}
	
	protected void marshallBody()
	{
		Iterator<CMSessionInfo> iterSessionList;
		Iterator<CMGroupInfo> iterGroupList;
		
		switch(m_nID)
		{
		case LOGIN:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strPasswd);
			putStringToByteBuffer(m_strHostAddr);
			m_bytes.putInt(m_nUDPPort);
			m_bytes.putInt(m_nKeepAliveTime);
			break;
		case LOGOUT:
			putStringToByteBuffer(m_strUserName);
			break;
		case LOGIN_ACK:
			m_bytes.putInt(m_bValidUser);
			putStringToByteBuffer(m_strCommArch);
			m_bytes.putInt(m_bFileTransferScheme);
			m_bytes.putInt(m_bLoginScheme);
			m_bytes.putInt(m_bSessionScheme);
			m_bytes.putInt(m_nAttachDownloadScheme);
			m_bytes.putInt(m_nUDPPort);			// server udp port
			break;
		case REQUEST_SESSION_INFO:
			putStringToByteBuffer(m_strUserName);
			break;
		case RESPONSE_SESSION_INFO:
			if(m_nSessionNum != m_sessionList.size())
			{
				System.err.println("CMSessionEvent.marshallBody(), incorrect number of session info.");
				m_bytes = null;
				return;
			}
			
			m_bytes.putInt(m_nSessionNum);
			
			iterSessionList = m_sessionList.iterator();
			while(iterSessionList.hasNext())
			{
				CMSessionInfo tsi = iterSessionList.next();
				putStringToByteBuffer(tsi.getSessionName());
				putStringToByteBuffer(tsi.getAddress());
				m_bytes.putInt(tsi.getPort());
				m_bytes.putInt(tsi.getUserNum());
			}
			break;
		case JOIN_SESSION:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strSessionName);
			break;
		case JOIN_SESSION_ACK:
			if(m_nGroupNum != m_groupList.size())
			{
				System.err.println("CMSessionEvent.marshallBody(), incorrect number of group info.");
				m_bytes = null;
				return;
			}

			m_bytes.putInt(m_nGroupNum);
			
			iterGroupList = m_groupList.iterator();
			while(iterGroupList.hasNext())
			{
				CMGroupInfo tgi = iterGroupList.next();
				putStringToByteBuffer(tgi.getGroupName());
				putStringToByteBuffer(tgi.getGroupAddress());
				m_bytes.putInt(tgi.getGroupPort());
			}
			break;
		case LEAVE_SESSION:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strSessionName);
			break;
		case LEAVE_SESSION_ACK:
			m_bytes.putInt(m_nReturnCode);
			break;
		case SESSION_TALK:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strTalk);
			break;
		case SESSION_ADD_USER:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strHostAddr);
			putStringToByteBuffer(m_strSessionName);
			break;
		case SESSION_REMOVE_USER:
			putStringToByteBuffer(m_strUserName);
			break;
		case CHANGE_SESSION:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strSessionName);
			break;
		case ADD_NONBLOCK_SOCKET_CHANNEL:
		case ADD_BLOCK_SOCKET_CHANNEL:
		case REMOVE_BLOCK_SOCKET_CHANNEL:
			putStringToByteBuffer(m_strChannelName);
			m_bytes.putInt(m_nChannelNum);
			break;
		case ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
		case ADD_BLOCK_SOCKET_CHANNEL_ACK:
		case REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
			putStringToByteBuffer(m_strChannelName);
			m_bytes.putInt(m_nChannelNum);
			m_bytes.putInt(m_nReturnCode);
			break;
		case REGISTER_USER:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strPasswd);
			break;
		case REGISTER_USER_ACK:
			m_bytes.putInt(m_nReturnCode);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strCreationTime);
			break;
		case DEREGISTER_USER:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strPasswd);
			break;
		case DEREGISTER_USER_ACK:
			m_bytes.putInt(m_nReturnCode);
			putStringToByteBuffer(m_strUserName);
			break;
		case FIND_REGISTERED_USER:
			putStringToByteBuffer(m_strUserName);
			break;
		case FIND_REGISTERED_USER_ACK:
			m_bytes.putInt(m_nReturnCode);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strCreationTime);
			break;
		default:
			System.err.println("CMSessionEvent.marshallBody(), unknown event id("+m_nID+").");
			m_bytes = null;
			break;
		}
	}
	
	protected void unmarshallBody(ByteBuffer msg)
	{
		int i;
		
		switch(m_nID)
		{
		case LOGIN:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strPasswd = getStringFromByteBuffer(msg);
			m_strHostAddr = getStringFromByteBuffer(msg);
			m_nUDPPort = msg.getInt();
			m_nKeepAliveTime = msg.getInt();
			break;
		case LOGOUT:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case LOGIN_ACK:
			m_bValidUser = msg.getInt();
			m_strCommArch = getStringFromByteBuffer(msg);
			m_bFileTransferScheme = msg.getInt();
			m_bLoginScheme = msg.getInt();
			m_bSessionScheme = msg.getInt();
			m_nAttachDownloadScheme = msg.getInt();
			m_nUDPPort = msg.getInt();
			break;
		case REQUEST_SESSION_INFO:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case RESPONSE_SESSION_INFO:
			m_sessionList.removeAllElements();
			m_nSessionNum = msg.getInt();
			for(i = 0; i < m_nSessionNum; i++)
			{
				CMSessionInfo tsi = new CMSessionInfo();
				tsi.setSessionName(getStringFromByteBuffer(msg));
				tsi.setAddress(getStringFromByteBuffer(msg));
				tsi.setPort(msg.getInt());
				tsi.setUserNum(msg.getInt());
				addSessionInfo(tsi);
			}
			break;
		case JOIN_SESSION:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			break;
		case JOIN_SESSION_ACK:
			m_groupList.removeAllElements();
			m_nGroupNum = msg.getInt();
			for(i = 0; i < m_nGroupNum; i++)
			{
				CMGroupInfo tgi = new CMGroupInfo();
				tgi.setGroupName(getStringFromByteBuffer(msg));
				tgi.setGroupAddress(getStringFromByteBuffer(msg));
				tgi.setGroupPort(msg.getInt());
				addGroupInfo(tgi);
			}
			break;
		case LEAVE_SESSION:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			break;
		case LEAVE_SESSION_ACK:
			m_nReturnCode = msg.getInt();
			break;
		case SESSION_TALK:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strTalk = getStringFromByteBuffer(msg);
			break;
		case SESSION_ADD_USER:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strHostAddr = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			break;
		case SESSION_REMOVE_USER:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case CHANGE_SESSION:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			break;
		case ADD_NONBLOCK_SOCKET_CHANNEL:
		case ADD_BLOCK_SOCKET_CHANNEL:
		case REMOVE_BLOCK_SOCKET_CHANNEL:
			m_strChannelName = getStringFromByteBuffer(msg);
			m_nChannelNum = msg.getInt();
			break;
		case ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
		case ADD_BLOCK_SOCKET_CHANNEL_ACK:
		case REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
			m_strChannelName = getStringFromByteBuffer(msg);
			m_nChannelNum = msg.getInt();
			m_nReturnCode = msg.getInt();
			break;
		case REGISTER_USER:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strPasswd = getStringFromByteBuffer(msg);
			break;
		case REGISTER_USER_ACK:
			m_nReturnCode = msg.getInt();
			m_strUserName = getStringFromByteBuffer(msg);
			m_strCreationTime = getStringFromByteBuffer(msg);
			break;
		case DEREGISTER_USER:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strPasswd = getStringFromByteBuffer(msg);
			break;
		case DEREGISTER_USER_ACK:
			m_nReturnCode = msg.getInt();
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case FIND_REGISTERED_USER:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case FIND_REGISTERED_USER_ACK:
			m_nReturnCode = msg.getInt();
			m_strUserName = getStringFromByteBuffer(msg);
			m_strCreationTime = getStringFromByteBuffer(msg);
			break;
		default:
			System.err.println("CMSessionEvent.unmarshallBody(), unknown event id("+m_nID+").");
			m_bytes = null;
			break;
		}
	}
}
