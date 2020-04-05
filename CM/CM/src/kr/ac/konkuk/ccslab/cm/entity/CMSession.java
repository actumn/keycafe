package kr.ac.konkuk.ccslab.cm.entity;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMSession extends CMSessionInfo {
	private Vector<CMGroup> m_groupList;
	private CMMember m_sessionUsers;
	private String m_sessionConfFileName;
	
	public CMSession()
	{
		super();
		m_groupList = new Vector<CMGroup>();
		m_sessionUsers = new CMMember();
		m_sessionConfFileName = null;
	}
	
	public CMSession(String sname, String saddr, int port)
	{
		super(sname, saddr, port);
		m_groupList = new Vector<CMGroup>();
		m_sessionUsers = new CMMember();
		m_sessionConfFileName = null;
	}

	public CMSession(String sname, String saddr, int port, int nUserNum)
	{
		super(sname, saddr, port, nUserNum);
		m_groupList = new Vector<CMGroup>();
		m_sessionUsers = new CMMember();
		m_sessionConfFileName = null;
	}

	// set/get methods
	public synchronized CMMember getSessionUsers()
	{
		return m_sessionUsers;
	}
	
	public synchronized Vector<CMGroup> getGroupList()
	{
		return m_groupList;
	}
	
	public synchronized String getSessionConfFileName()
	{
		return m_sessionConfFileName;
	}
	
	public synchronized void setSessionConfFileName(String fname)
	{
		m_sessionConfFileName = fname;
	}
	
	//////////////////////////////////////////////////////////
	// group membership management
	
	// check if a group is member or not of this session
	public synchronized boolean isMember(String strGroupName)
	{
		CMGroup tGroup = null;
		boolean bFound = false;
		Iterator<CMGroup> iter = m_groupList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tGroup = iter.next();
			String tname = tGroup.getGroupName();
			if(strGroupName.equals(tname))
			{
				bFound = true;
			}
		}
		
		return bFound;
	}
	
	// create a group and add it to the group list
	public synchronized CMGroup createGroup(String strGroupName, String strMA, int nPort)
	{
		CMGroup cmGroup = null;
		if( isMember(strGroupName) )
		{
			System.out.println("CMSession.createGroup(), group("+strGroupName+") already exists in session("
					+getSessionName()+").");
			return null;
		}
		
		cmGroup = new CMGroup(strGroupName, strMA, nPort);
		addGroup(cmGroup);
		
		return cmGroup;
	}
	
	public synchronized boolean addGroup(CMGroup group)
	{
		String gname = group.getGroupName();
		
		if(isMember(gname))
		{
			System.err.println("CMSession.addGroup(), group("+gname+") already exists in session("
					+getSessionName()+").");
			return false;
		}
		
		m_groupList.addElement(group);
		
		if(CMInfo._CM_DEBUG_2)
			System.out.println("CMSession.addGroup(), group("+gname+") added to session("
					+getSessionName()+").");
		return true;
	}
	
	public synchronized boolean removeGroup(String strGroupName)
	{
		CMGroup tGroup = null;
		boolean bFound = false;
		Iterator<CMGroup> iter = m_groupList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tGroup = iter.next();
			String tname = tGroup.getGroupName();
			if(strGroupName.equals(tname))
			{
				iter.remove();
				bFound = true;
			}
		}
		
		if(bFound)
		{
			if(CMInfo._CM_DEBUG_2)
				System.out.println("CMSession.removeGroup(), group("+strGroupName+" removed from session("
						+getSessionName()+").");
		}
		else
		{
			System.err.println("CMSession.removeGroup(), group("+strGroupName+" not found in session("
					+getSessionName()+").");
		}
		
		return bFound;
	}
	
	public synchronized CMGroup findGroup(String strGroupName)
	{
		CMGroup tGroup = null;
		boolean bFound = false;
		Iterator<CMGroup> iter = m_groupList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tGroup = iter.next();
			String tname = tGroup.getGroupName();
			if(strGroupName.equals(tname))
			{
				bFound = true;
			}
		}
		
		if(!bFound)
			tGroup = null;
		
		return tGroup;
	}
	
	public synchronized CMGroup findGroupWithUserName(String strUserName)
	{
		CMGroup tGroup = null;
		boolean bFound = false;
		Iterator<CMGroup> iter = m_groupList.iterator();
		
		while(iter.hasNext() && !bFound)
		{
			tGroup = iter.next();
			if(tGroup.getGroupUsers().isMember(strUserName))
			{
				bFound = true;
			}
		}
		
		if(!bFound)
			tGroup = null;
		
		return tGroup;
	}
}
