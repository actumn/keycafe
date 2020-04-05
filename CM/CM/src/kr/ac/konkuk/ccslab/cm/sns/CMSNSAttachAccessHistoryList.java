package kr.ac.konkuk.ccslab.cm.sns;

import java.util.ArrayList;
import java.util.Calendar;

import kr.ac.konkuk.ccslab.cm.entity.CMObject;

public class CMSNSAttachAccessHistoryList extends CMObject {
	private ArrayList<CMSNSAttachAccessHistory> m_historyList;
	
	public CMSNSAttachAccessHistoryList()
	{
		m_historyList = new ArrayList<CMSNSAttachAccessHistory>();
	}
	
	
	public CMSNSAttachAccessHistory findAccessHistory(String strUser, Calendar date, String strWriter)
	{
		CMSNSAttachAccessHistory tempHistory = null;
		boolean bFound = false;
		for(int i = 0; i < m_historyList.size() && !bFound; i++)
		{
			tempHistory = m_historyList.get(i);
			if(strUser.equals(tempHistory.getUserName()) && strWriter.equals(tempHistory.getWriterName())
					&& date.get(Calendar.YEAR) == tempHistory.getDate().get(Calendar.YEAR)
					&& date.get(Calendar.MONTH) == tempHistory.getDate().get(Calendar.MONTH)
					&& date.get(Calendar.DATE) == tempHistory.getDate().get(Calendar.DATE))
			{
				bFound = true;
			}
		}
		
		if(bFound)
			return tempHistory;
		
		return null;
	}
	
	// key is (user name, date(yyyy-MM-dd), writer name)
	public CMSNSAttachAccessHistory findAccessHistory(CMSNSAttachAccessHistory history)
	{
		return findAccessHistory(history.getUserName(), history.getDate(), history.getWriterName());
	}
	
	public boolean addAccessHistory(CMSNSAttachAccessHistory history)
	{
		if(findAccessHistory(history) != null)
		{
			System.err.println("CMSNSAttachAccessHisotryList().addAccessHistory(), the access history for "
					+"user("+history.getUserName()+"), date("+history.getDate().get(Calendar.YEAR)+"-"
					+history.getDate().get(Calendar.MONTH)+"-"+history.getDate().get(Calendar.DATE)
					+"), writer("+history.getWriterName()+") already exists!");
			return false;
		}
		
		boolean ret = m_historyList.add(history);
		return ret;
	}
	
	public boolean removeAccessHistory(CMSNSAttachAccessHistory history)
	{
		CMSNSAttachAccessHistory tempHistory = findAccessHistory(history);
		if(tempHistory == null)
		{
			System.err.println("CMSNSAttachAccessHisotryList().removeAccessHistory(), the access history for "
					+"user("+history.getUserName()+"), date("+history.getDate().get(Calendar.YEAR)+"-"
					+history.getDate().get(Calendar.MONTH)+"-"+history.getDate().get(Calendar.DATE)
					+"), writer("+history.getWriterName()+") not found!");
			return false;
		}
		
		boolean ret = m_historyList.remove(tempHistory);
		return ret;
	}
	
	public void removeAllAccessHistory()
	{
		m_historyList.clear();
		return;
	}
	
	public ArrayList<CMSNSAttachAccessHistory> getAllAccessHistory()
	{
		return m_historyList;
	}
}
