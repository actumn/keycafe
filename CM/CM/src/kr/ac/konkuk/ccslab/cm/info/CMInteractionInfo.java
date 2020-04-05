package kr.ac.konkuk.ccslab.cm.info;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;

public class CMInteractionInfo {
	private CMMember m_loginUsers;
	private Vector<CMSession> m_sessionList;
	private CMUser m_myself;
	private CMServer m_defaultServerInfo;		// default server info
	private Vector<CMServer> m_addServerList;	// additional server info
	
	public CMInteractionInfo()
	{
		m_loginUsers = new CMMember();
		m_sessionList = new Vector<CMSession>();
		m_myself = new CMUser();
		m_defaultServerInfo = new CMServer();
		m_addServerList = new Vector<CMServer>();
/*
		CMUser tuser = new CMUser();
		tuser.setName("mlim");
		m_loginUsers.addMember(tuser);
*/		
	}
	
	// get methods
	public synchronized CMMember getLoginUsers()
	{
		return m_loginUsers;
	}
	
	public synchronized Vector<CMSession> getSessionList()
	{
		return m_sessionList;
	}
	
	public synchronized CMUser getMyself()
	{
		return m_myself;
	}
	
	public synchronized CMServer getDefaultServerInfo()
	{
		return m_defaultServerInfo;
	}
	
	public synchronized Vector<CMServer> getAddServerList()
	{
		return m_addServerList;
	}
	
	//////////////////////////////////////////////////////////
	// session membership management
	
	// check if a session is member or not
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
	
	// create a session and add it to the session list
	// strAddr: address of a server which manages the session
	// nPort: port of a server which manages the session
	public synchronized CMSession createSession(String strSessionName, String strAddr, int nPort)
	{
		CMSession cmSession = null;
		if( isMember(strSessionName) )
		{
			System.err.println("CMInteractionInfo.createSession(), session("+strSessionName+") already exists.");
			return null;
		}
		
		cmSession = new CMSession(strSessionName, strAddr, nPort);
		addSession(cmSession);
		
		return cmSession;
	}

	public synchronized boolean addSession(CMSession session)
	{
		String sname = session.getSessionName();
		
		if(isMember(sname))
		{
			System.err.println("CMInteractionInfo.addSession(), session("+sname+") already exists.");
			return false;
		}
		
		m_sessionList.addElement(session);
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMIntearctionInfo.addSession(), session("+sname+") added.");
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
			if(CMInfo._CM_DEBUG_2)
				System.out.println("CMInteractionInfo.removeSession(), session("+strSessionName+") removed.");
		}
		else
		{
			System.out.println("CMInteractionInfo.removeSession(), session("+strSessionName+") not found.");
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
	
	public synchronized CMSession findSessionWithUserName(String strUserName)
	{
		CMSession tSession = null;
		boolean bFound = false;
		Iterator<CMSession> iter = m_sessionList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tSession = iter.next();
			if(tSession.getSessionUsers().isMember(strUserName))
			{
				bFound = true;
			}
		}
		
		if(!bFound)
			tSession = null;
		
		return tSession;
	}
	
	//////////////////////////////////////////////////////////
	// membership management of additional server info

	// check if a server is member or not
	public synchronized boolean isAddServer(String strServerName)
	{
		CMServer tServer = null;
		boolean bFound = false;
		Iterator<CMServer> iter = m_addServerList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tServer = iter.next();
			String tname = tServer.getServerName();
			if(strServerName.equals(tname))
			{
				bFound = true;
			}
		}
		
		return bFound;
	}
	
	// create additional server info and add it to the add-server list
	public synchronized CMServer createAddServer(String strServerName, String strAddr, int nPort, int nUDPPort)
	{
		CMServer cmServer = null;
		if( isMember(strServerName) )
		{
			System.err.println("CMInteractionInfo.createAddserver(), server("+strServerName+") already exists.");
			return null;
		}
		
		cmServer = new CMServer(strServerName, strAddr, nPort, nUDPPort);
		addAddServer(cmServer);
		
		return cmServer;
	}

	public synchronized boolean addAddServer(CMServer server)
	{
		String sname = server.getServerName();
		
		if(isMember(sname))
		{
			System.err.println("CMInteractionInfo.addAddServer(), server("+sname+") already exists.");
			return false;
		}
		
		m_addServerList.addElement(server);
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMIntearctionInfo.addAddServer(), server("+sname+") added.");
		return true;
	}

	public synchronized boolean removeAddServer(String strServerName)
	{
		CMServer tServer = null;
		boolean bFound = false;
		Iterator<CMServer> iter = m_addServerList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tServer = iter.next();
			String tname = tServer.getServerName();
			if(strServerName.equals(tname))
			{
				iter.remove();
				bFound = true;
			}
		}
		
		if(bFound)
		{
			if(CMInfo._CM_DEBUG_2)
				System.out.println("CMInteractionInfo.removeAddServer(), server("+strServerName+") removed.");
		}
		else
		{
			System.err.println("CMInteractionInfo.removeAddServer(), server("+strServerName+") not found.");
		}
		
		return bFound;
	}

	public synchronized CMServer findAddServer(String strServerName)
	{
		CMServer tServer = null;
		boolean bFound = false;
		Iterator<CMServer> iter = m_addServerList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tServer = iter.next();
			String tname = tServer.getServerName();
			if(strServerName.equals(tname))
			{
				bFound = true;
			}
		}
		
		if(!bFound)
			tServer = null;
		
		return tServer;
	}

}
