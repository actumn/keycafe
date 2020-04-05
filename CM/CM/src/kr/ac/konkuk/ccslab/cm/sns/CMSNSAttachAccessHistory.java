package kr.ac.konkuk.ccslab.cm.sns;

import java.util.Calendar;

import kr.ac.konkuk.ccslab.cm.entity.CMObject;

public class CMSNSAttachAccessHistory extends CMObject {
	private String m_strUserName;
	private Calendar m_date;		// access date (yyyy-mm-dd)
	private String m_strWriterName;	// target of which attachment file is accessed
	private int m_nAccessCount;		// access count (>=0)
	
	private boolean m_bAdded;		// whether this is newly added after DB load or not
	private boolean m_bUpdated;		// whether this is updated after DB load or not
	
	public CMSNSAttachAccessHistory()
	{
		m_strUserName = null;
		m_date = null;
		m_strWriterName = null;
		m_nAccessCount = -1;
		
		m_bAdded = false;
		m_bUpdated = false;
	}
	
	public CMSNSAttachAccessHistory(String strUser, Calendar date, String strWriter, int nCount)
	{
		m_strUserName = strUser;
		m_date = date;
		m_strWriterName = strWriter;
		m_nAccessCount = nCount;
		
		m_bAdded = false;
		m_bUpdated = false;
	}
	
	///////////////////////////////////////////////////////////
	// set/get methods
	
	public void setUserName(String strName)
	{
		m_strUserName = strName;
	}
	
	public String getUserName()
	{
		return m_strUserName;
	}
	
	public void setDate(Calendar date)
	{
		m_date = date;
	}
	
	public Calendar getDate()
	{
		return m_date;
	}
	
	public void setWriterName(String strName)
	{
		m_strWriterName = strName;
	}
	
	public String getWriterName()
	{
		return m_strWriterName;
	}
	
	public void setAccessCount(int nCount)
	{
		m_nAccessCount = nCount;
	}
	
	public int getAccessCount()
	{
		return m_nAccessCount;
	}
	
	public void setAdded(boolean bAdded)
	{
		m_bAdded = bAdded;
	}
	
	public boolean isAdded()
	{
		return m_bAdded;
	}
	
	public void setUpdated(boolean bUpdated)
	{
		m_bUpdated = bUpdated;
	}
	
	public boolean isUpdated()
	{
		return m_bUpdated;
	}
}
