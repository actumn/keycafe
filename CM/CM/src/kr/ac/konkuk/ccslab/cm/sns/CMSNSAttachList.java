package kr.ac.konkuk.ccslab.cm.sns;

import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMObject;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMSNSAttachList extends CMObject {
	//private Vector<CMSNSAttach> m_attachVector;
	private CMList<CMSNSAttach> m_attachList;
	
	////////// event fields of CONTENT_DOWNLOAD_END
	private String m_strUserName;
	private String m_strWriterName;
	private int m_nContentOffset;
	private int m_nNumContents;
	//////////
	
	public CMSNSAttachList()
	{
		//m_attachVector = new Vector<CMSNSAttach>();
		m_attachList = new CMList<CMSNSAttach>();
		m_strUserName = null;
		m_strWriterName = null;
		m_nContentOffset = -1;
		m_nNumContents = -1;
	}

	public CMSNSAttach findSNSAttach(int nContentID)
	{
		/*
		CMSNSAttach attach = null;
		boolean bFound = false;
		Iterator<CMSNSAttach> iter = m_attachVector.iterator();
		while(iter.hasNext() && !bFound)
		{
			attach = iter.next();
			if(nContentID == attach.getContentID())
			{
				bFound = true;
			}
		}
		
		if(bFound)
			return attach;
		
		return null;
		*/
		
		CMSNSAttach attach = null;
		CMSNSAttach inAttach = new CMSNSAttach();
		inAttach.setContentID(nContentID);
		attach = m_attachList.findElement(inAttach);
		
		return attach;
	}
	
	public boolean addSNSAttach(CMSNSAttach attach)
	{
		/*
		if(findSNSAttach(attach.getContentID()) != null)
		{
			System.err.println("CMSNSAttachList.addSNSAttach(), already exists! user("+attach.getUserName()
					+"), contentID("+attach.getContentID()+")");
			return false;
		}
		
		m_attachVector.addElement(attach);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSAttachList.addSNSAttach(), succeeded for user("+attach.getUserName()
					+"), contentID("+attach.getContentID()+")");
		}
		
		return true;
		*/
		
		boolean bResult = m_attachList.addElement(attach);
		return bResult;
	}
	
	public boolean removeSNSAttach(int nContentID)
	{
		/*
		boolean bFound = false;
		Iterator<CMSNSAttach> iter = m_attachVector.iterator();
		CMSNSAttach attach = null;
		while(iter.hasNext() && !bFound)
		{
			attach = iter.next();
			if(nContentID == attach.getContentID())
			{
				iter.remove();
				attach = null;	// not sure
				bFound = true;
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMSNSAttachList.removeSNSAttach(), succeeded for contentID("
							+nContentID+"), #vector elements("+m_attachVector.size()+").");
				}
			}
		}
		
		if(!bFound)
		{
			if(CMInfo._CM_DEBUG)
			{
				System.err.println("CMSNSAttachList.removeSNSAttach(), not found contentID("
						+nContentID+")");
			}
		}
		
		return bFound;
		*/
		
		boolean bResult = false;
		CMSNSAttach inAttach = new CMSNSAttach();
		inAttach.setContentID(nContentID);
		bResult = m_attachList.removeElement(inAttach);
		
		return bResult;
	}
	
	public void removeAllSNSAttach()
	{
		m_attachList.getList().clear();
		return;
	}
	
	public Vector<CMSNSAttach> getSNSAttachList()
	{
		/*
		return m_attachVector;
		*/
		return m_attachList.getList();
	}
	
	//////////////////////////////////////////////////
	// get/set methods
	
	public void setContentDownloadEndEvent(String strUser, String strWriter, int nOffset, int nNumContents)
	{
		m_strUserName = strUser;
		m_strWriterName = strWriter;
		m_nContentOffset = nOffset;
		m_nNumContents = nNumContents;
	}
	
	public void setUserName(String strName)
	{
		m_strUserName = strName;
	}
	
	public String getUserName()
	{
		return m_strUserName;
	}
	
	public void setWriterName(String strName)
	{
		m_strWriterName = strName;
	}
	
	public String getWriterName()
	{
		return m_strWriterName;
	}
	
	public void setContentOffset(int offset)
	{
		m_nContentOffset = offset;
	}
	
	public int getContentOffset()
	{
		return m_nContentOffset;
	}
	
	public void setNumContents(int num)
	{
		m_nNumContents = num;
	}
	
	public int getNumContents()
	{
		return m_nNumContents;
	}
	
}
