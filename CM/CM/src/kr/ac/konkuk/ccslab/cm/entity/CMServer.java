package kr.ac.konkuk.ccslab.cm.entity;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

// server information managed by a client and the default server
public class CMServer extends CMServerInfo {
	private CMChannelInfo<Integer> m_nonBlockSocketChannelInfo;
	private CMChannelInfo<Integer> m_blockSocketChannelInfo;
	private long m_lLastEventTransTime; // when this object(server) sends the last event
	private int m_nKeepAliveTime;		// keep-alive time of this object(server)
	
	private String m_strCurrentSessionName;		// only 4 client
	private String m_strCurrentGroupName;		// only 4 client
	private int m_nClientState;					// only 4 client
	private String m_strCommArch;				// only 4 client
	private boolean m_bLoginScheme;				// only 4 client
	private boolean m_bSessionScheme;			// only 4 client
	private Vector<CMSession> m_sessionList;	// only 4 client

	
	public CMServer()
	{
		super();
		m_nonBlockSocketChannelInfo = new CMChannelInfo<Integer>();
		m_blockSocketChannelInfo = new CMChannelInfo<Integer>();
		m_lLastEventTransTime = 0;
		m_nKeepAliveTime = 0;
		
		m_strCurrentSessionName = "";
		m_strCurrentGroupName = "";
		m_nClientState = CMInfo.CM_INIT;
		m_strCommArch = "";
		m_bLoginScheme = false;
		m_bSessionScheme = false;
		m_sessionList = new Vector<CMSession>();
	}
	
	public CMServer(String sname, String saddr, int nPort, int nUDPPort)
	{
		super(sname, saddr, nPort, nUDPPort);
		m_nonBlockSocketChannelInfo = new CMChannelInfo<Integer>();
		m_blockSocketChannelInfo = new CMChannelInfo<Integer>();
		m_lLastEventTransTime = 0;
		m_nKeepAliveTime = 0;
		
		m_strCurrentSessionName = "?";
		m_strCurrentGroupName = "?";
		m_nClientState = CMInfo.CM_INIT;
		m_strCommArch = "?";
		m_bLoginScheme = false;
		m_bSessionScheme = false;
		m_sessionList = new Vector<CMSession>();
	}
	
	// set/get methods
	
	public synchronized CMChannelInfo<Integer> getNonBlockSocketChannelInfo()
	{
		return m_nonBlockSocketChannelInfo;
	}
	
	public synchronized CMChannelInfo<Integer> getBlockSocketChannelInfo()
	{
		return m_blockSocketChannelInfo;
	}
	
	public synchronized void setLastEventTransTime(long lTime)
	{
		m_lLastEventTransTime = lTime;
	}
	
	public synchronized long getLastEventTransTime()
	{
		return m_lLastEventTransTime;
	}
	
	public synchronized void setKeepAliveTime(int nSecond)
	{
		m_nKeepAliveTime = nSecond;
	}
	
	public synchronized int getKeepAliveTime()
	{
		return m_nKeepAliveTime;
	}
	
	public synchronized void setCurrentSessionName(String name)
	{
		m_strCurrentSessionName = name;
	}
	
	public synchronized String getCurrentSessionName()
	{
		return m_strCurrentSessionName;
	}
	
	public synchronized void setCurrentGroupName(String name)
	{
		m_strCurrentGroupName = name;
	}
	
	public synchronized String getCurrentGroupName()
	{
		return m_strCurrentGroupName;
	}
	
	public synchronized void setClientState(int state)
	{
		m_nClientState = state;
		
		switch(m_nClientState)
		{
		case CMInfo.CM_INIT:
		case CMInfo.CM_CONNECT:
			m_strCurrentSessionName = "?";
			m_strCurrentGroupName = "?";
			m_strCommArch = "?";
			m_bLoginScheme = false;
			m_bSessionScheme = false;
			break;
		case CMInfo.CM_LOGIN:
			m_strCurrentSessionName = "?";
			m_strCurrentGroupName = "?";
			break;
		case CMInfo.CM_SESSION_JOIN:
			break;
		default:
			System.err.println("CMServer.setClientState(), invalid state ("+state+")");
		}

	}
	
	public synchronized int getClientState()
	{
		return m_nClientState;
	}
	
	public synchronized void setCommArch(String ca)
	{
		m_strCommArch = ca;
	}
	
	public synchronized String getCommArch()
	{
		return m_strCommArch;
	}
	
	public synchronized void setLoginScheme(boolean bLogin)
	{
		m_bLoginScheme = bLogin;
	}
	
	public synchronized boolean isLoginScheme()
	{
		return m_bLoginScheme;
	}
	
	public synchronized void setSessionScheme(boolean bSession)
	{
		m_bSessionScheme = bSession;
	}
	
	public synchronized boolean isSessionScheme()
	{
		return m_bSessionScheme;
	}
	
	// session management
	public synchronized int getSessionNum()
	{
		return m_sessionList.size();
	}
	
	public synchronized boolean isMember(String strSessionName)
	{
		CMSession tSession = null;
		boolean bFound = false;
		Iterator<CMSession> iter = m_sessionList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tSession = iter.next();
			String tname = tSession.getSessionName();
			if(strSessionName.equals(tname))
			{
				bFound = true;
			}
		}
		
		return bFound;
	}

	public synchronized boolean addSession(CMSession session)
	{
		String sname = session.getSessionName();
		
		if(isMember(sname))
		{
			System.out.println("CMServer.addSession(), session("+sname+") already exists in server("
					+m_strServerName+").");
			return false;
		}
		
		m_sessionList.addElement(session);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMServer.addSession(), session("+sname+") added in server("
						+m_strServerName+").");
		return true;
	}

	public synchronized boolean removeSession(String strSessionName)
	{
		CMSession tSession = null;
		boolean bFound = false;
		Iterator<CMSession> iter = m_sessionList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tSession = iter.next();
			String tname = tSession.getSessionName();
			if(strSessionName.equals(tname))
			{
				iter.remove();
				bFound = true;
			}
		}
		
		if(bFound)
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMServer.removeSession(), session("+strSessionName+") removed in server("
						+m_strServerName+").");
		}
		else
		{
			System.out.println("CMServer.removeSession(), session("+strSessionName+" not found in server("
					+m_strServerName+").");
		}
		
		return bFound;
	}

	public synchronized CMSession findSession(String strSessionName)
	{
		CMSession tSession = null;
		boolean bFound = false;
		Iterator<CMSession> iter = m_sessionList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tSession = iter.next();
			String tname = tSession.getSessionName();
			if(strSessionName.equals(tname))
			{
				bFound = true;
			}
		}
		
		if(!bFound)
			tSession = null;
		
		return tSession;
	}
	
	public synchronized Vector<CMSession> getSessionList()
	{
		return m_sessionList;
	}

}
