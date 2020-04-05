package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * This class represents CM events that are related to additional servers.
 * @author mlim
 * @see CMEvent
 */
public class CMMultiServerEvent extends CMEvent{
	public static final int REQ_SERVER_REG = 1;				// S->DS
	public static final int RES_SERVER_REG = 2;				// DS->C
	public static final int REQ_SERVER_DEREG = 3;			// S->DS
	public static final int RES_SERVER_DEREG = 4;			// DS->S
	public static final int NOTIFY_SERVER_INFO = 5;			// DS->C
	public static final int NOTIFY_SERVER_LEAVE = 6;		// DS->C
	public static final int REQ_SERVER_INFO = 7;			// C->DS

	public static final int ADD_LOGIN = 8;					// C->S
	public static final int ADD_LOGIN_ACK = 9;				// S->C
	public static final int ADD_LOGOUT = 10;				// C->S
	public static final int ADD_JOIN_SESSION = 11;			// C->S
	public static final int ADD_JOIN_SESSION_ACK = 12;		// S->C
	public static final int ADD_LEAVE_SESSION = 13;			// C->S
	public static final int ADD_LEAVE_SESSION_ACK = 14;		// S->C
	public static final int ADD_CHANGE_SESSION = 15;		// S->All C
	public static final int ADD_SESSION_ADD_USER = 16;		// S->All C
	public static final int ADD_SESSION_REMOVE_USER = 17;	// S->All C
	public static final int ADD_JOIN_GROUP = 18;			// C->S
	public static final int ADD_LEAVE_GROUP = 19;			// C->S
	public static final int ADD_GROUP_INHABITANT = 20;		// S->C
	public static final int ADD_NEW_GROUP_USER = 21;		// S->All C
	public static final int ADD_REMOVE_GROUP_USER = 22;		// S->All C
	public static final int ADD_REQUEST_SESSION_INFO = 23;	// C->S
	public static final int ADD_RESPONSE_SESSION_INFO = 24;	// S->C
	
	String m_strServerName;
	String m_strServerAddress;
	int m_nServerPort;
	int m_nServerUDPPort;
	int m_nReturnCode;
	int m_nServerNum;
	Vector<CMServerInfo> m_serverList;	// CServerInfo is different from CCMServerInfo.
	int m_nSessionNum;
	Vector<CMSessionInfo> m_sessionList;

	String m_strUserName;
	String m_strPassword;
	String m_strHostAddress;
	int m_nUDPPort;
	int m_bValidUser;		// 0: false, 1: true
	int m_nUserID;
	String m_strCommArch;
	int m_bLoginScheme;		// 0: false, 1: true
	int m_bSessionScheme;	// 0: false, 1: true
	String m_strSessionName;
	int m_nGroupNum;
	Vector<CMGroupInfo> m_groupList;
	String m_strGroupName;

	private int m_nKeepAliveTime;

	public CMMultiServerEvent()
	{
		m_nType = CMInfo.CM_MULTI_SERVER_EVENT;

		m_strServerName = "";
		m_strServerAddress = "";
		m_nServerPort = -1;
		m_nServerUDPPort = -1;
		m_nReturnCode = -1;
		m_nServerNum = -1;
		m_nSessionNum = -1;

		m_strUserName = "";
		m_strPassword = "";
		m_strHostAddress = "";
		m_nUDPPort = -1;
		m_bValidUser = -1;
		m_nUserID = -1;
		m_strCommArch = "";
		m_bLoginScheme = -1;
		m_bSessionScheme = -1;
		m_strSessionName = "";
		m_nGroupNum = -1;
		m_strGroupName = "";
		m_nKeepAliveTime = 0;

		m_serverList = new Vector<CMServerInfo>();
		m_sessionList = new Vector<CMSessionInfo>();
		m_groupList = new Vector<CMGroupInfo>();
				
	}
	
	public CMMultiServerEvent(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}
	
	// set/get methods
	public void setServerName(String sname)
	{
		if(sname != null)
			m_strServerName = sname;
	}
	
	public String getServerName()
	{
		return m_strServerName;
	}
	
	public void setServerAddress(String saddr)
	{
		if(saddr != null)
			m_strServerAddress = saddr;
	}
	
	public String getServerAddress()
	{
		return m_strServerAddress;
	}
	
	public void setServerPort(int port)
	{
		m_nServerPort = port;
	}
	
	public int getServerPort()
	{
		return m_nServerPort;
	}
	
	public void setServerUDPPort(int port)
	{
		m_nServerUDPPort = port;
	}
	
	public int getServerUDPPort()
	{
		return m_nServerUDPPort;
	}
	
	public void setReturnCode(int code)
	{
		m_nReturnCode = code;
	}
	
	public int getReturnCode()
	{
		return m_nReturnCode;
	}
	
	public void setServerNum(int num)
	{
		m_nServerNum = num;
	}
	
	public int getServerNum()
	{
		return m_nServerNum;
	}
	
	public void setSessionNum(int num)
	{
		m_nSessionNum = num;
	}
	
	public int getSessionNum()
	{
		return m_nSessionNum;
	}
	
	public void setUserName(String name)
	{
		if(name != null)
			m_strUserName = name;
	}
	
	public String getUserName()
	{
		return m_strUserName;
	}
	
	public void setPassword(String passwd)
	{
		if(passwd != null)
			m_strPassword = passwd;
	}
	
	public String getPassword()
	{
		return m_strPassword;
	}
	
	public void setHostAddress(String host)
	{
		if(host != null)
			m_strHostAddress = host;
	}
	
	public String getHostAddress()
	{
		return m_strHostAddress;
	}
	
	public void setUDPPort(int port)
	{
		m_nUDPPort = port;
	}
	
	public int getUDPPort()
	{
		return m_nUDPPort;
	}
	
	public void setValidUser(int b)
	{
		m_bValidUser = b;
	}
	
	public int isValidUser()
	{
		return m_bValidUser;
	}
	
	public void setCommArch(String arch)
	{
		if(arch != null)
			m_strCommArch = arch;
	}
	
	public String getCommArch()
	{
		return m_strCommArch;
	}
	
	public void setLoginScheme(int b)
	{
		m_bLoginScheme = b;
	}
	
	public int isLoginScheme()
	{
		return m_bLoginScheme;
	}
	
	public void setSessionScheme(int b)
	{
		m_bSessionScheme = b;
	}
	
	public int isSessionScheme()
	{
		return m_bSessionScheme;
	}
	
	public void setSessionName(String name)
	{
		if(name != null)
			m_strSessionName = name;
	}
	
	public String getSessionName()
	{
		return m_strSessionName;
	}
	
	public void setGroupNum(int num)
	{
		m_nGroupNum = num;
	}
	
	public int getGroupNum()
	{
		return m_nGroupNum;
	}
	
	public void setGroupName(String name)
	{
		if(name != null)
			m_strGroupName = name;
	}
	
	public String getGroupName()
	{
		return m_strGroupName;
	}
	
	public void setKeepAliveTime(int nSecond)
	{
		m_nKeepAliveTime = nSecond;
	}
	
	public int getKeepAliveTime()
	{
		return m_nKeepAliveTime;
	}

	// control vectors of server info, session info, group info
	public boolean addServerInfo(CMServerInfo si)
	{
		if(si == null) return false;
		
		CMServerInfo tsi = findServerInfo(si.getServerName());
		
		if( tsi != null )
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.addServerInfo(), already exists: "+si.getServerName());
			return false;
		}
		
		m_serverList.addElement(si);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.addServerInfo(), ok server name: "+si.getServerName());

		return true;
	}
	
	public boolean removeServerInfo(String serverName)
	{
		boolean found = false;
		CMServerInfo tsi;
		Iterator<CMServerInfo> iter = m_serverList.iterator();
		
		if(serverName == null) return false;
		
		while(iter.hasNext() && !found)
		{
			tsi = iter.next();
			if( serverName.equals(tsi.getServerName()) )
			{
				iter.remove();
				found = true;
			}
		}

		if(!found)
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.removeServerInfo(), not found: "+serverName);
			
			return false;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.removeServerInfo(), Ok server: "+serverName);

		return true;		
	}
	
	public void removeAllServerInfoObjects()
	{
		/*
		Iterator<CMServerInfo> iter = m_serverList.iterator();
		while(iter.hasNext())
		{
			CMServerInfo si = iter.next();
			si = null;
		}
		*/
		m_serverList.removeAllElements();
		return;
	}
	
	public CMServerInfo findServerInfo(String serverName)
	{
		boolean found = false;
		CMServerInfo tsi = null;
		Iterator<CMServerInfo> iter = m_serverList.iterator();
		
		if(serverName == null) return null;
		
		while(iter.hasNext() && !found)
		{
			tsi = iter.next();
			if( serverName.equals(tsi.getServerName()) )
			{
				found = true;
			}
		}

		if(!found)
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.findServerInfo(), not found: "+serverName);

			return null;
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.findServerInfo(), Ok server: "+serverName);

		return tsi;
	}
	
	public Vector<CMServerInfo> getServerInfoList()
	{
		return m_serverList;
	}
	
	public boolean addSessionInfo(CMSessionInfo si)
	{
		if(si == null) return false;
		
		CMSessionInfo tsi = findSessionInfo(si.getSessionName());
		
		if( tsi != null )
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.addSessionInfo(), already exists: "+si.getSessionName());
			return false;
		}
		
		m_sessionList.addElement(si);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.addSessionInfo(), ok session: "+si.getSessionName());

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
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.removeSessionInfo(), not found: "+sname);
			
			return false;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.removeSessionInfo(), Ok session: "+sname);

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
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.findSessionInfo(), not found: "+sname);

			return null;
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.findSessionInfo(), Ok session: "+sname);

		return tsi;
	}
	
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
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.addGroupInfo(), already exists: "+gi.getGroupName());
			return false;
		}
		
		m_groupList.addElement(gi);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.addGroupInfo(), ok group: "+gi.getGroupName());

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
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.removeGroupInfo(), not found: "+gname);
			
			return false;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.removeGroupInfo(), Ok session: "+gname);

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
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMultiServerEvent.findGroupInfo(), not found: "+gname);

			return null;
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMMultiServerEvent.findGroupInfo(), Ok session: "+gname);

		return tgi;
	}
	
	public Vector<CMGroupInfo> getGroupInfoList()
	{
		return m_groupList;
	}

	/////////////////////////////////////////////////
	
	protected int getByteNum()
	{
		Iterator<CMServerInfo> iterServerList;
		Iterator<CMSessionInfo> iterSessionList;
		Iterator<CMGroupInfo> iterGroupList;
		int nElementByteNum = 0;
		
		int nByteNum = 0;
		nByteNum = super.getByteNum();
		
		switch(m_nID)
		{
		case REQ_SERVER_REG:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length 
					+ m_strServerAddress.getBytes().length;
			nByteNum += 2*Integer.BYTES;
			nByteNum += Integer.BYTES;	// keep-alive time of the add-server
			break;
		case RES_SERVER_REG:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case REQ_SERVER_DEREG:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length;
			break;
		case RES_SERVER_DEREG:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case NOTIFY_SERVER_INFO:
			nByteNum += Integer.BYTES; // server num
			nElementByteNum = 0;
			iterServerList = m_serverList.iterator();
			while(iterServerList.hasNext())
			{
				CMServerInfo tsi = iterServerList.next();
				nElementByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + tsi.getServerName().getBytes().length 
								+ tsi.getServerAddress().getBytes().length;
				nElementByteNum += 2*Integer.BYTES;
			}
			nByteNum += nElementByteNum;
			break;
		case NOTIFY_SERVER_LEAVE:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length;
			break;
		case REQ_SERVER_INFO:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		case ADD_LOGIN:
			nByteNum += 4*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strPassword.getBytes().length
				+ m_strHostAddress.getBytes().length;
			nByteNum += Integer.BYTES;
			nByteNum += Integer.BYTES;	// keep-alive time
			break;
		case ADD_LOGIN_ACK:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strCommArch.getBytes().length;
			nByteNum += 4*Integer.BYTES;
			break;
		case ADD_LOGOUT:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length;
			break;
		case ADD_JOIN_SESSION:
			nByteNum += 3*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strSessionName.getBytes().length;
			break;
		case ADD_JOIN_SESSION_ACK:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length; // server name
			nByteNum += Integer.BYTES;	// group num
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
		case ADD_LEAVE_SESSION:
			nByteNum += 3*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length 
				+ m_strUserName.getBytes().length + m_strSessionName.getBytes().length;
			break;
		case ADD_LEAVE_SESSION_ACK:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case ADD_CHANGE_SESSION:
			nByteNum += 3*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strSessionName.getBytes().length;
			break;
		case ADD_SESSION_ADD_USER:
			nByteNum += 4*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strHostAddress.getBytes().length
				+ m_strSessionName.getBytes().length;
			break;
		case ADD_SESSION_REMOVE_USER:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length;
			break;
		case ADD_JOIN_GROUP:
			nByteNum += 5*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strHostAddress.getBytes().length
				+ m_strSessionName.getBytes().length + m_strGroupName.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case ADD_LEAVE_GROUP:
			nByteNum += 4*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strSessionName.getBytes().length
				+ m_strGroupName.getBytes().length;
			break;
		case ADD_GROUP_INHABITANT:
			nByteNum += 5*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strHostAddress.getBytes().length
				+ m_strSessionName.getBytes().length + m_strGroupName.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case ADD_NEW_GROUP_USER:
			nByteNum += 5*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strHostAddress.getBytes().length
				+ m_strSessionName.getBytes().length + m_strGroupName.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case ADD_REMOVE_GROUP_USER:
			nByteNum += 4*CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length
				+ m_strUserName.getBytes().length + m_strSessionName.getBytes().length
				+ m_strGroupName.getBytes().length;
			break;
		case ADD_REQUEST_SESSION_INFO:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		case ADD_RESPONSE_SESSION_INFO:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strServerName.getBytes().length;
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
		default:
			nByteNum = -1;
			break;
		}
		
		return nByteNum;
	}
	
	protected void marshallBody()
	{
		Iterator<CMServerInfo> iterServerList;
		Iterator<CMSessionInfo> iterSessionList;
		Iterator<CMGroupInfo> iterGroupList;
		
		switch(m_nID)
		{
		case REQ_SERVER_REG:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strServerAddress);
			m_bytes.putInt(m_nServerPort);
			m_bytes.putInt(m_nServerUDPPort);
			m_bytes.putInt(m_nKeepAliveTime);
			break;
		case RES_SERVER_REG:
			putStringToByteBuffer(m_strServerName);
			m_bytes.putInt(m_nReturnCode);
			break;
		case REQ_SERVER_DEREG:
			putStringToByteBuffer(m_strServerName);
			break;
		case RES_SERVER_DEREG:
			putStringToByteBuffer(m_strServerName);
			m_bytes.putInt(m_nReturnCode);
			break;
		case NOTIFY_SERVER_INFO:
			if(m_nServerNum != m_serverList.size())
			{
				System.out.println("CMMultiServerEvent.marshallBody(), incorrect number of server info.");
				m_bytes = null;
				return;
			}
			
			m_bytes.putInt(m_nServerNum);
			iterServerList = m_serverList.iterator();
			while(iterServerList.hasNext())
			{
				CMServerInfo tsi = iterServerList.next();
				putStringToByteBuffer(tsi.getServerName());
				putStringToByteBuffer(tsi.getServerAddress());
				m_bytes.putInt(tsi.getServerPort());
				m_bytes.putInt(tsi.getServerUDPPort());
			}
			break;
		case NOTIFY_SERVER_LEAVE:
			putStringToByteBuffer(m_strServerName);
			break;
		case REQ_SERVER_INFO:
			putStringToByteBuffer(m_strUserName);
			break;
		case ADD_LOGIN:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strPassword);
			putStringToByteBuffer(m_strHostAddress);
			m_bytes.putInt(m_nUDPPort);
			m_bytes.putInt(m_nKeepAliveTime);
			break;
		case ADD_LOGIN_ACK:
			putStringToByteBuffer(m_strServerName);
			m_bytes.putInt(m_bValidUser);
			putStringToByteBuffer(m_strCommArch);
			m_bytes.putInt(m_bLoginScheme);
			m_bytes.putInt(m_bSessionScheme);
			m_bytes.putInt(m_nServerUDPPort);
			break;
		case ADD_LOGOUT:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			break;
		case ADD_JOIN_SESSION:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strSessionName);
			break;
		case ADD_JOIN_SESSION_ACK:
			if(m_nGroupNum != m_groupList.size())
			{
				System.out.println("CMMultiServerEvent.marshallBody(), incorrect number of group info.");
				m_bytes = null;
				return;
			}
			putStringToByteBuffer(m_strServerName);
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
		case ADD_LEAVE_SESSION:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strSessionName);
			break;
		case ADD_LEAVE_SESSION_ACK:
			putStringToByteBuffer(m_strServerName);
			m_bytes.putInt(m_nReturnCode);
			break;
		case ADD_CHANGE_SESSION:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strSessionName);
			break;
		case ADD_SESSION_ADD_USER:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strHostAddress);
			putStringToByteBuffer(m_strSessionName);
			break;
		case ADD_SESSION_REMOVE_USER:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			break;
		case ADD_JOIN_GROUP:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strHostAddress);
			m_bytes.putInt(m_nUDPPort);
			putStringToByteBuffer(m_strSessionName);
			putStringToByteBuffer(m_strGroupName);
			break;
		case ADD_LEAVE_GROUP:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strSessionName);
			putStringToByteBuffer(m_strGroupName);
			break;
		case ADD_GROUP_INHABITANT:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strHostAddress);
			m_bytes.putInt(m_nUDPPort);
			putStringToByteBuffer(m_strSessionName);
			putStringToByteBuffer(m_strGroupName);
			break;
		case ADD_NEW_GROUP_USER:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strHostAddress);
			m_bytes.putInt(m_nUDPPort);
			putStringToByteBuffer(m_strSessionName);
			putStringToByteBuffer(m_strGroupName);
			break;
		case ADD_REMOVE_GROUP_USER:
			putStringToByteBuffer(m_strServerName);
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strSessionName);
			putStringToByteBuffer(m_strGroupName);
			break;
		case ADD_REQUEST_SESSION_INFO:
			putStringToByteBuffer(m_strUserName);
			break;
		case ADD_RESPONSE_SESSION_INFO:
			if(m_nSessionNum != m_sessionList.size())
			{
				System.out.println("CMMultiServerEvent.marshallBody(), incorrect number of session info.");
				m_bytes = null;
				return;
			}
			
			putStringToByteBuffer(m_strServerName);
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
		default:
			System.out.println("CMMultiServerEvent.marshallBody(), unknown event id("+m_nID+").");
			m_bytes = null;
			break;
		}
	}

	protected void unmarshallBody(ByteBuffer msg)
	{
		int i = 0;

		switch(m_nID)
		{
		case REQ_SERVER_REG:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strServerAddress = getStringFromByteBuffer(msg);
			m_nServerPort = msg.getInt();
			m_nServerUDPPort = msg.getInt();
			m_nKeepAliveTime = msg.getInt();
			break;
		case RES_SERVER_REG:
			m_strServerName = getStringFromByteBuffer(msg);
			m_nReturnCode = msg.getInt();
			break;
		case REQ_SERVER_DEREG:
			m_strServerName = getStringFromByteBuffer(msg);
			break;
		case RES_SERVER_DEREG:
			m_strServerName = getStringFromByteBuffer(msg);
			m_nReturnCode = msg.getInt();
			break;
		case NOTIFY_SERVER_INFO:
			m_serverList.removeAllElements();
			m_nServerNum = msg.getInt();
			for(i = 0; i < m_nServerNum; i++)
			{
				CMServerInfo tsi = new CMServerInfo();
				tsi.setServerName(getStringFromByteBuffer(msg));
				tsi.setServerAddress(getStringFromByteBuffer(msg));
				tsi.setServerPort(msg.getInt());
				tsi.setServerUDPPort(msg.getInt());
				addServerInfo(tsi);
			}
			break;
		case NOTIFY_SERVER_LEAVE:
			m_strServerName = getStringFromByteBuffer(msg);
			break;
		case REQ_SERVER_INFO:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case ADD_LOGIN:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strPassword = getStringFromByteBuffer(msg);
			m_strHostAddress = getStringFromByteBuffer(msg);
			m_nUDPPort = msg.getInt();
			m_nKeepAliveTime = msg.getInt();
			break;
		case ADD_LOGIN_ACK:
			m_strServerName = getStringFromByteBuffer(msg);
			m_bValidUser = msg.getInt();
			m_strCommArch = getStringFromByteBuffer(msg);
			m_bLoginScheme = msg.getInt();
			m_bSessionScheme = msg.getInt();
			m_nServerUDPPort = msg.getInt();
			break;
		case ADD_LOGOUT:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case ADD_JOIN_SESSION:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			break;
		case ADD_JOIN_SESSION_ACK:
			m_groupList.removeAllElements();
			m_strServerName = getStringFromByteBuffer(msg);
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
		case ADD_LEAVE_SESSION:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			break;
		case ADD_LEAVE_SESSION_ACK:
			m_strServerName = getStringFromByteBuffer(msg);
			m_nReturnCode = msg.getInt();
			break;
		case ADD_CHANGE_SESSION:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			break;
		case ADD_SESSION_ADD_USER:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strHostAddress = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			break;
		case ADD_SESSION_REMOVE_USER:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case ADD_JOIN_GROUP:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strHostAddress = getStringFromByteBuffer(msg);
			m_nUDPPort = msg.getInt();
			m_strSessionName = getStringFromByteBuffer(msg);
			m_strGroupName = getStringFromByteBuffer(msg);
			break;
		case ADD_LEAVE_GROUP:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			m_strGroupName = getStringFromByteBuffer(msg);
			break;
		case ADD_GROUP_INHABITANT:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strHostAddress = getStringFromByteBuffer(msg);
			m_nUDPPort = msg.getInt();
			m_strSessionName = getStringFromByteBuffer(msg);
			m_strGroupName = getStringFromByteBuffer(msg);
			break;
		case ADD_NEW_GROUP_USER:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strHostAddress = getStringFromByteBuffer(msg);
			m_nUDPPort = msg.getInt();
			m_strSessionName = getStringFromByteBuffer(msg);
			m_strGroupName = getStringFromByteBuffer(msg);
			break;
		case ADD_REMOVE_GROUP_USER:
			m_strServerName = getStringFromByteBuffer(msg);
			m_strUserName = getStringFromByteBuffer(msg);
			m_strSessionName = getStringFromByteBuffer(msg);
			m_strGroupName = getStringFromByteBuffer(msg);
			break;
		case ADD_REQUEST_SESSION_INFO:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case ADD_RESPONSE_SESSION_INFO:
			m_sessionList.removeAllElements();
			m_strServerName = getStringFromByteBuffer(msg);
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
		default:
			System.out.println("CMMultiServerEvent.unmarshallBody(), unknown event id("+m_nID+").");
			m_bytes = null;
			break;
		}
	}
}
