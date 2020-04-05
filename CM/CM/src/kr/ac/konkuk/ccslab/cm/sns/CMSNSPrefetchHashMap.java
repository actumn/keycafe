package kr.ac.konkuk.ccslab.cm.sns;

import java.util.HashMap;

import kr.ac.konkuk.ccslab.cm.entity.CMObject;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMSNSPrefetchHashMap extends CMObject {
	private HashMap<String, CMSNSPrefetchList> m_prefetchMap;
	
	public CMSNSPrefetchHashMap()
	{
		m_prefetchMap = new HashMap<String, CMSNSPrefetchList>();
	}

	public CMSNSPrefetchList findPrefetchList(String strUserName)
	{
		CMSNSPrefetchList prefetchList = m_prefetchMap.get(strUserName);
		return prefetchList;
	}

	public boolean addPrefetchList(String strUserName, CMSNSPrefetchList prefetchList)
	{
		// check if parameters are null or not
		if(strUserName == null)
		{
			System.err.println("CMSNSPrefetchHashMap.addPrefetchList(), user is null!");
			return false;
		}
		
		if(prefetchList == null)
		{
			System.err.println("CMSNSPrefetchHashMap.addPrefetchList(), prefetch list is null!");
			return false;
		}
		
		// check if the key already exists or not
		if(m_prefetchMap.containsKey(strUserName))
		{
			System.err.println("CMSNSPrefetchHashMap.addPrefetchList(), key("+strUserName+") already exists!");
			return false;
		}
		
		m_prefetchMap.put(strUserName, prefetchList);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSPrefetchHashMap.addPrefetchList(), succeeded for user("+strUserName+").");
		}
		return true;
	}
	
	public boolean removePrefetchList(String strUserName)
	{
		//CMSNSPrefetchList prefetchList = null;
		
		if(!m_prefetchMap.containsKey(strUserName))
		{
			System.err.println("CMSNSPrefetchHashMap.removePrefetchList(), key("+strUserName+") not found!");
			return false;
		}
		
		//prefetchList = m_prefetchMap.get(strUserName);
		m_prefetchMap.remove(strUserName);
		//prefetchList = null;
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSPrefetchHashMap.removePrefetchList(), succeeded for key("+strUserName+").");
		}
		
		return true;
	}
	
	public void removeAllPrefetchList()
	{
		/*
		CMSNSPrefetchList prefetchList = null;
		Iterator<Entry<String, CMSNSPrefetchList>> iter = m_prefetchMap.entrySet().iterator();
		while(iter.hasNext())
		{
			Map.Entry<String, CMSNSPrefetchList> e = (Map.Entry<String, CMSNSPrefetchList>) iter.next();
			prefetchList = e.getValue();
			prefetchList = null;
		}
		*/
		m_prefetchMap.clear();
		return;
	}

}
