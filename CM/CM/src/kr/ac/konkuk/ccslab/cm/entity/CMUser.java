package kr.ac.konkuk.ccslab.cm.entity;

import java.util.Calendar;
import java.util.Hashtable;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachAccessHistoryList;

/**
 * This class represents the information of a CM user (client).
 * 
 * @author CCSLab, Konkuk University
 *
 */
public class CMUser extends CMObject {
	
	private int m_nID;
	private String m_strName;
	private String m_strPasswd;
	private String m_strHost;
	private int m_nUDPPort;
	private int m_nSSCPort;	// port number for server socket channel (4 p2p file-transfer)
	private CMPosition m_pq;
	private String m_strCurrentSession;
	private String m_strCurrentGroup;
	private int m_nState;
	private CMChannelInfo<Integer> m_nonBlockSocketChannelInfo;
	private CMChannelInfo<Integer> m_blockSocketChannelInfo;
	private int m_nAttachDownloadScheme;	// 4 SERVER
	private CMSNSAttachAccessHistoryList m_historyList;	// 4 SERVER
	private Calendar m_lastLoginDate;		// 4 SERVER
	// 4 server (last event-transmission time of this object(client))
	private long m_lLastEventTransTime;
	// 4 myself (client or server) (last event-transmission time per receiver)
	private Hashtable<String, Long> m_myLastEventTransTimeHashtable;
	private int m_nKeepAliveTime;
	
	/**
	 * Creates an instance of the CMUser class.
	 */
	public CMUser()
	{
		m_nType = CMInfo.CM_USER;
		m_nID = -1;
		m_strName = "";
		m_strPasswd = "?";
		m_strHost = "?";
		m_nUDPPort = -1;
		m_nSSCPort = -1;
		m_strCurrentSession = "?";
		m_strCurrentGroup = "?";
		m_nState = CMInfo.CM_INIT;
		m_nonBlockSocketChannelInfo = new CMChannelInfo<Integer>();
		m_blockSocketChannelInfo = new CMChannelInfo<Integer>();
		m_nAttachDownloadScheme = -1;
		m_historyList = new CMSNSAttachAccessHistoryList();
		m_lastLoginDate = null;
		m_lLastEventTransTime = -1;
		m_myLastEventTransTimeHashtable = new Hashtable<String, Long>();
		m_nKeepAliveTime = 0;
	}
	
	/**
	 * Creates an instance of the CMUser class.
	 * 
	 * @param name - the user name.
	 * @param passwd - the login password.
	 * @param host - the IP address of the user host.
	 */
	public CMUser(String name, String passwd, String host)
	{
		m_nType = CMInfo.CM_USER;
		m_nID = -1;
		m_strName = name;
		m_strPasswd = passwd;
		m_strHost = host;
		m_nUDPPort = -1;
		m_nSSCPort = -1;
		m_strCurrentSession = "";
		m_strCurrentGroup = "";
		m_nState = CMInfo.CM_INIT;
		m_nonBlockSocketChannelInfo = new CMChannelInfo<Integer>();
		m_blockSocketChannelInfo = new CMChannelInfo<Integer>();
		m_nAttachDownloadScheme = -1;
		m_historyList = new CMSNSAttachAccessHistoryList();
		m_lastLoginDate = null;
		m_lLastEventTransTime = -1;
		m_myLastEventTransTimeHashtable = new Hashtable<String, Long>();
		m_nKeepAliveTime = 0;
	}
	
	// set methods
	public synchronized void setID(int id)
	{
		m_nID = id;
	}
	
	public synchronized void setName(String name)
	{
		m_strName = name;
	}
	
	public synchronized void setPasswd(String passwd)
	{
		m_strPasswd = passwd;
	}
	
	public synchronized void setHost(String host)
	{
		m_strHost = host;
	}
	
	public synchronized void setUDPPort(int port)
	{
		m_nUDPPort = port;
	}
	
	public synchronized void setSSCPort(int port)
	{
		m_nSSCPort = port;
	}
	
	public synchronized void setPosition(CMPosition pq)
	{
		m_pq = pq;
	}
	
	public synchronized void setCurrentSession(String sName)
	{
		m_strCurrentSession = sName;
	}
	
	public synchronized void setCurrentGroup(String gName)
	{
		m_strCurrentGroup = gName;
	}

	public synchronized void setState(int state)
	{
		m_nState = state;
		
		switch(m_nState)
		{
		case CMInfo.CM_INIT:
		case CMInfo.CM_CONNECT:
			m_strName = "?";
			m_strPasswd = "?";
			m_strHost = "?";
			m_nUDPPort = -1;
			m_strCurrentSession = "?";
			m_strCurrentGroup = "?";
			break;
		case CMInfo.CM_LOGIN:
			m_strCurrentSession = "?";
			m_strCurrentGroup = "?";
			break;
		case CMInfo.CM_SESSION_JOIN:
			break;
		default:
			System.err.println("CMUser.setState(), invalid state ("+state+")");
		}
		
		return;
	}
	
	public synchronized void setAttachDownloadScheme(int scheme)
	{
		m_nAttachDownloadScheme = scheme;
	}
	
	public synchronized void setAttachAccessHistoryList(CMSNSAttachAccessHistoryList list)
	{
		m_historyList = list;
	}
	
	public synchronized void setLastLoginDate(Calendar date)
	{
		m_lastLoginDate = date;
	}
	
	public synchronized void setLastEventTransTime(long lTime)
	{
		m_lLastEventTransTime = lTime;
	}
	
	public synchronized void setMyLastEventTransTimeHashtable(Hashtable<String, Long> ht)
	{
		m_myLastEventTransTimeHashtable = ht;
	}
	
	public synchronized void setKeepAliveTime(int nSecond)
	{
		m_nKeepAliveTime = nSecond;
	}
	
	// get methods
	public synchronized int getID()
	{
		return m_nID;
	}
	
	/**
	 * Returns user name.
	 * 
	 * @return the user name.
	 */
	public synchronized String getName()
	{
		return m_strName;
	}
	
	/**
	 * Returns user login password.
	 * 
	 * <p> The password is encrypted by CM.
	 * 
	 * @return the login password.
	 */
	public synchronized String getPasswd()
	{
		return m_strPasswd;
	}
	
	
	/**
	 * Returns IP address of the user host.
	 * 
	 * @return the IP address.
	 */
	public synchronized String getHost()
	{
		return m_strHost;
	}
	
	/**
	 * Returns UDP port number of the user.
	 * 
	 * <p> The user uses UDP port number to receives a UDP packet a datagram channel.
	 * 
	 * @return the UDP port number.
	 */
	public synchronized int getUDPPort()
	{
		return m_nUDPPort;
	}
	
	/**
	 * Returns port number for server socket channel(SSC) of the user.
	 * 
	 * <p> The user uses this port number to receive a file directly from a sender user 
	 * (P2P file-transfer).
	 * 
	 * @return the SSC port number.
	 */
	public synchronized int getSSCPort()
	{
		return m_nSSCPort;
	}
	
	public synchronized CMPosition getPosition()
	{
		return m_pq;
	}
	
	/**
	 * Returns the current session of the user.
	 * 
	 * @return the session name.
	 * <br> If the user is not in a session, the method returns "?".
	 */
	public synchronized String getCurrentSession()
	{
		return m_strCurrentSession;
	}
	
	/**
	 * Returns the current group of the user.
	 * 
	 * @return the group name.
	 * <br> If the user is not in a group, the method returns "?".
	 */
	public synchronized String getCurrentGroup()
	{
		return m_strCurrentGroup;
	}
	
	/**
	 * Returns the current state of the user.
	 * 
	 * The CMUser object can has a following state.
	 * <p>CMInfo.CM_INIT : The user object has been created.
	 * <br>CMInfo.CM_CONNECT : The user has connected to the default server.
	 * <br>CMInfo.CM_LOGIN : The user has logged in to the default server.
	 * <br>CMInfo.CM_JOIN_SESSION : The user has entered a session or a group.
	 * 
	 * @return the user state.
	 */
	public synchronized int getState()
	{
		return m_nState;
	}
	
	/**
	 * Returns a list of non-blocking socket channel to this user.
	 * <p> When a server application has a CMUser reference, it can get the list of 
	 * non-blocking socket channel.
	 * 
	 * @return the socket channel list.
	 */
	public synchronized CMChannelInfo<Integer> getNonBlockSocketChannelInfo()
	{
		return m_nonBlockSocketChannelInfo;
	}
	
	/**
	 * Returns a list of blocking socket channel to this user.
	 * <p> When a server application has a CMUser reference, it can get the list of 
	 * blocking socket channel.
	 * 
	 * @return the socket channel list.
	 */
	public synchronized CMChannelInfo<Integer> getBlockSocketChannelInfo()
	{
		return m_blockSocketChannelInfo;
	}
	
	public synchronized int getAttachDownloadScheme()
	{
		return m_nAttachDownloadScheme;
	}
	
	public synchronized CMSNSAttachAccessHistoryList getAttachAccessHistoryList()
	{
		return m_historyList;
	}
	
	public synchronized Calendar getLastLoginDate()
	{
		return m_lastLoginDate;
	}
	
	public synchronized long getLastEventTransTime()
	{
		return m_lLastEventTransTime;
	}
	
	public synchronized Hashtable<String, Long> getMyLastEventTransTimeHashtable()
	{
		return m_myLastEventTransTimeHashtable;
	}
	
	public synchronized int getKeepAliveTime()
	{
		return m_nKeepAliveTime;
	}
}
