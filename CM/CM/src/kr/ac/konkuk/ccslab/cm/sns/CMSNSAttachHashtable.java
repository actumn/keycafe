package kr.ac.konkuk.ccslab.cm.sns;

import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMObject;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMSNSAttachHashtable extends CMObject {
	//private HashMap<String, CMSNSAttachList> m_attachMap;
	private Hashtable<String, CMSNSAttachList> m_attachHashtable; // key is the requester name
	
	public CMSNSAttachHashtable()
	{
		//m_attachMap = new HashMap<String, CMSNSAttachList>();
		m_attachHashtable = new Hashtable<String, CMSNSAttachList>();
	}
	
	public CMSNSAttachList findSNSAttachList(String strUserName)
	{
		//CMSNSAttachList attachList = m_attachMap.get(strUserName);
		CMSNSAttachList attachList = m_attachHashtable.get(strUserName);
		return attachList;
	}

	public boolean addSNSAttachList(String strUserName, CMSNSAttachList attachList)
	{
		// check if parameters are null or not
		if(strUserName == null)
		{
			System.err.println("CMSNSAttachHashtable.addSNSAttachList(), the user name is null!");
			return false;
		}
		if(attachList == null)
		{
			System.err.println("CMSNSAttachHashtable.addSNSAttachList(), the attach list is null!");
			return false;
		}
		
		// check if the key already exists or not
		//if(m_attachMap.containsKey(strUserName))
		if(m_attachHashtable.containsKey(strUserName))
		{
			System.err.println("CMSNSAttachHashtable.addSNSAttachList(), the key("+strUserName+") already exists!");
			return false;
		}
		
		//m_attachMap.put(strUserName, attachList);
		m_attachHashtable.put(strUserName, attachList);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSAttachHashtable.addSNSAttachList(), succeeded for user("+strUserName+").");
		}
		
		return true;
	}
	
	public boolean removeSNSAttachList(String strUserName)
	{
		//CMSNSAttachList attachList = null;
		
		//if(!m_attachMap.containsKey(strUserName))
		if(!m_attachHashtable.containsKey(strUserName))
		{
			System.err.println("CMSNSAttachHashtable.removeSNSAttachList(), key("+strUserName+") not found!");
			return false;
		}
		
		//m_attachMap.remove(strUserName);
		m_attachHashtable.remove(strUserName);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSAttachHashtable.removeSNSAttachList(), succeeded for key("+strUserName+").");
		}
		
		return true;
	}
	
	public void removeAllSNSAttachList()
	{
		//m_attachMap.clear();
		m_attachHashtable.clear();
		return;
	}
}
