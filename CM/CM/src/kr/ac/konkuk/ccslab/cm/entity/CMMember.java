package kr.ac.konkuk.ccslab.cm.entity;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * This class represents a list of users.
 * 
 * @author CCSLab, Konkuk University
 *
 */
public class CMMember extends CMObject{
	private Vector<CMUser> m_memberList;

	/**
	 * creates an instance of the CMMember class.
	 */
	public CMMember()
	{
		m_nType = CMInfo.CM_MEMBER;
		m_memberList = new Vector<CMUser>();
	}
	
	/**
	 * Adds a user to this member list.
	 * 
	 * @param user - an added user.
	 * @return true if the user is successfully added, or false.
	 */
	public synchronized boolean addMember(CMUser user)
	{
		if(isMember(user.getName()))
		{
			System.out.println("CMMember.addMember(), user("+user.getName()+") already exists.");
			return false;
		}
		
		m_memberList.addElement(user);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMember.addMember(), Ok with user("+user.getName()+").");
		}
		
		return true;
	}
	
	/**
	 * Removes a user from this member list.
	 * 
	 * <p> If the member list has a user object that has the same name as the given user.
	 * 
	 * @param user - a removed user.
	 * @return true if the user is successfully removed, or false.
	 */
	public synchronized boolean removeMember(CMUser user)
	{
		Iterator<CMUser> iter = m_memberList.iterator();
		int nRemovedNum = 0;
		while(iter.hasNext())
		{
			CMUser tuser = iter.next();
			if(user.getName().equals(tuser.getName()))
			{
				iter.remove();
				nRemovedNum++;
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMMember.removeMember(), user("+user.getName()+") deleted.");
				}
			}
		}
		
		if(nRemovedNum == 0)
		{
			System.out.println("CMMember.removeMember(), user("+user.getName()+") not found.");
			return false;
		}
		
		return true;
	}

	// remove member reference and the member object
	public synchronized boolean removeMemberObject(CMUser user)
	{
		Iterator<CMUser> iter = m_memberList.iterator();
		int nRemovedNum = 0;
		boolean bFound = false;
		while(iter.hasNext() && !bFound)
		{
			CMUser tuser = iter.next();
			if(user.getName().equals(tuser.getName()))
			{
				iter.remove();
				tuser = null;
				nRemovedNum++;
				bFound = true;
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMMember.removeMemberObject(), user("+user.getName()+") deleted.");
				}
			}
		}
		
		if(nRemovedNum == 0)
		{
			System.out.println("CMMember.removeMemberObject(), user("+user.getName()+") not found.");
			return false;
		}
		
		return true;
	}

	/**
	 * Removes a user with the given name from this member list.
	 * 
	 * @param name - the name of a removed user.
	 * @return true if the user is successfully removed, or false.
	 */
	public synchronized boolean removeMember(String name)
	{
		Iterator<CMUser> iter = m_memberList.iterator();
		int nRemovedNum = 0;
		boolean bFound = false;
		while(iter.hasNext() && !bFound)
		{
			CMUser tuser = iter.next();
			if(name.equals(tuser.getName()))
			{
				iter.remove();
				nRemovedNum++;
				bFound = true;
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMMember.removeMember(), user("+name+") deleted.");
				}
			}
		}
		
		if(nRemovedNum == 0)
		{
			System.out.println("CMMember.removeMember(), user("+name+") not found.");
			return false;
		}
		
		return true;
	}

	// remove member item (reference) and the member object with user name
	public synchronized boolean removeMemberObject(String name)
	{
		Iterator<CMUser> iter = m_memberList.iterator();
		int nRemovedNum = 0;
		boolean bFound = false;
		while(iter.hasNext() && !bFound)
		{
			CMUser tuser = iter.next();
			if(name.equals(tuser.getName()))
			{
				iter.remove();
				tuser = null;
				nRemovedNum++;
				bFound = true;
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMMember.removeMemberObject(), user("+name+") deleted.");
				}
			}
		}
		
		if(nRemovedNum == 0)
		{
			System.out.println("CMMember.removeMemberObject(), user("+name+") not found.");
			return false;
		}
		
		return true;
	}

	/**
	 * Removes all users in this member list.
	 * 
	 * @return true if this member list is not empty and cleared; 
	 * false if the list is already empty.
	 */
	public synchronized boolean removeAllMembers()
	{
		if( m_memberList.isEmpty() )
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMember.removeAllMembers(), already empty.");
			return false;
		}
		
		m_memberList.removeAllElements();
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMember.removeAllMembers(), Ok");
		}
		
		return true;
	}

	// remove all member items (references) and the member objects
	public synchronized boolean removeAllMemberObjects()
	{
		if( m_memberList.isEmpty() )
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMMember.removeAllMemberObjects(), already empty.");
			return false;
		}
		
		/*
		Iterator<CMUser> iter = m_memberList.iterator();
		while(iter.hasNext())
		{
			CMUser tuser = iter.next();
			iter.remove();
			tuser = null;	// not clear
		}
		*/
		
		m_memberList.removeAllElements();
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMember.removeAllMemberObjects(), Ok");
		}
		
		return true;
	}

	/**
	 * Checks if a given user is in this member list.
	 * 
	 * <p> The given user is regarded as a member if the list has a member with the same name.
	 * 
	 * @param user - a given user.
	 * @return true if the user is a member, or false.
	 */
	public synchronized boolean isMember(CMUser user)
	{
		Iterator<CMUser> iter = m_memberList.iterator();
		while(iter.hasNext())
		{
			CMUser tuser = iter.next();
			if(user.getName().equals(tuser.getName()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if a given name is in this member list.
	 * 
	 * <p> The given name is regarded as a member if the list has a member with the same name.
	 * 
	 * @param name - a given name.
	 * @return true if the name is a member, or false.
	 */
	public synchronized boolean isMember(String name)
	{
		Iterator<CMUser> iter = m_memberList.iterator();
		while(iter.hasNext())
		{
			CMUser tuser = iter.next();
			if(name.equals(tuser.getName()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if the member list is empty or not.
	 * 
	 * @return true if the member list is empty, or false.
	 */
	public synchronized boolean isEmpty()
	{
		return m_memberList.isEmpty();
	}
	

	/**
	 * Finds a member with a given name.
	 * 
	 * @param name - a member name
	 * @return the user with the name if found; null otherwise.
	 */
	public synchronized CMUser findMember(String name)
	{
		CMUser tuser;
		Iterator<CMUser> iter = m_memberList.iterator();
		while(iter.hasNext())
		{
			tuser = iter.next();
			if(name.equals(tuser.getName()))
				return tuser;
		}
		
		return null;
	}
	
	/**
	 * Returns the number of current users in this member list.
	 * 
	 * @return the number of members.
	 */
	public synchronized int getMemberNum()
	{
		return m_memberList.size();
	}
	
	
	/**
	 * Returns the Vector reference of this member list.
	 * 
	 * @return the Vector of this member list.
	 */
	public synchronized Vector<CMUser> getAllMembers()
	{
		return m_memberList;
	}
	
	@Override
	public String toString()
	{
		if(m_memberList.isEmpty())
			return null;
		
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("session("+m_memberList.get(0).getCurrentSession()+"), group("
				+m_memberList.get(0).getCurrentGroup()+")\n");
		for(CMUser user : m_memberList)
		{
			strBuf.append(user.getName()+" ");
		}
		
		return strBuf.toString();
	}
}
