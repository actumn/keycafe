package kr.ac.konkuk.ccslab.cm.info;

import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttach;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachHashtable;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachList;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSPrefetchHashMap;

public class CMSNSInfo {
	private CMSNSContentList m_contentList;	// downloaded content for client, global content list for server
											// if DB is not used
	private CMSNSAttach m_sendAttach;	// by client for content upload
	private CMSNSAttachHashtable m_recvAttachHashtable;	// by server for content upload
	private CMSNSAttachHashtable m_sendAttachHashtable;	// by server for content download
	private CMSNSAttachList m_recvAttachList;	// by client for content download	
	private CMSNSPrefetchHashMap m_prefetchMap;		// by server for prefetching attached files
	
	private String m_strLastlyReqWriter;	// by client for saving the information on the last content download request
	private int m_nLastlyReqOffset;			// by client for saving the information on the last content download request
	private int m_nLastlyDownContentNum;	// by client for saving the information on the last content download request
	
	public CMSNSInfo()
	{
		m_contentList = new CMSNSContentList();
		m_sendAttach = new CMSNSAttach();
		m_recvAttachHashtable = new CMSNSAttachHashtable();
		m_sendAttachHashtable = new CMSNSAttachHashtable();
		m_recvAttachList = new CMSNSAttachList();
		m_prefetchMap = new CMSNSPrefetchHashMap();
		m_strLastlyReqWriter = ""; // initial (or default) writer is an empty string
		m_nLastlyReqOffset = 0;		// initial (or default) offset is 0 (the most recent content)
		m_nLastlyDownContentNum = 0;
	}
	
	/////////////////////////////////////////////////////////////
	// get/set methods
	
	public synchronized void setSNSContentList(CMSNSContentList contList)
	{
		m_contentList = contList;
	}
	
	public synchronized CMSNSContentList getSNSContentList()
	{
		return m_contentList;
	}
	
	public synchronized void setSendSNSAttach(CMSNSAttach attach)
	{
		m_sendAttach = attach;
	}
	
	public synchronized CMSNSAttach getSendSNSAttach()
	{
		return m_sendAttach;
	}
	
	public synchronized void setRecvSNSAttachHashtable(CMSNSAttachHashtable attachTable)
	{
		m_recvAttachHashtable = attachTable;
	}
	
	public synchronized CMSNSAttachHashtable getRecvSNSAttachHashtable()
	{
		return m_recvAttachHashtable;
	}
	
	public synchronized void setSendSNSAttachHashtable(CMSNSAttachHashtable attachTable)
	{
		m_sendAttachHashtable = attachTable;
	}
	
	public synchronized CMSNSAttachHashtable getSendSNSAttachHashtable()
	{
		return m_sendAttachHashtable;
	}
	
	public synchronized void setRecvSNSAttachList(CMSNSAttachList attachList)
	{
		m_recvAttachList = attachList;
	}
	
	public synchronized CMSNSAttachList getRecvSNSAttachList()
	{
		return m_recvAttachList;
	}
	
	public synchronized void setPrefetchMap(CMSNSPrefetchHashMap prefetchMap)
	{
		m_prefetchMap = prefetchMap;
	}
	
	public synchronized CMSNSPrefetchHashMap getPrefetchMap()
	{
		return m_prefetchMap;
	}
	
	public synchronized void setLastlyReqWriter(String strWriter)
	{
		m_strLastlyReqWriter = strWriter;
	}
	
	public synchronized String getLastlyReqWriter()
	{
		return m_strLastlyReqWriter;
	}
	
	public synchronized void setLastlyReqOffset(int nOffset)
	{
		m_nLastlyReqOffset = nOffset;
	}
	
	public synchronized int getLastlyReqOffset()
	{
		return m_nLastlyReqOffset;
	}
	
	public synchronized void setLastlyDownContentNum(int nNum)
	{
		m_nLastlyDownContentNum = nNum;
	}
	
	public synchronized int getLastlyDownContentNum()
	{
		return m_nLastlyDownContentNum;
	}
}
