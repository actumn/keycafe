package kr.ac.konkuk.ccslab.cm.sns;

import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMObject;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMSNSAttach extends CMObject {
	private int m_nNumCompleted;
	private ArrayList<String> m_filePathList;
	// Event fields of CMSNSEvent.CONTENT_UPLOAD_RESPONSE
	private int m_nReturnCode;
	private String m_strRequesterName;
	private int m_nContentID;
	private String m_strDate;
	//////////////////////////
	
	public CMSNSAttach()
	{
		m_nNumCompleted = 0;
		m_filePathList = new ArrayList<String>();
		m_nReturnCode = -1;
		m_strRequesterName = null;
		m_nContentID = -1;
		m_strDate = null;
	}
	
	@Override
	public String toString()
	{
		String str = "CMSNSAttach object: content ID("+m_nContentID+")";
		return str;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null) return false;
		if(!this.getClass().equals(o.getClass())) return false;
		if(this == o) return true;

		CMSNSAttach attach = (CMSNSAttach)o;
		if(m_nContentID == attach.getContentID())
			return true;
		return false;
	}
	
	/////////////////////////////////////////////
	// get/set methods
	
	public void setNumCompleted(int num)
	{
		m_nNumCompleted = num;
	}
	
	public int getNumCompleted()
	{
		return m_nNumCompleted;
	}
	
	public void setFilePathList(ArrayList<String> filePathList)
	{
		m_filePathList = filePathList;
	}
	
	public ArrayList<String> getFilePathList()
	{
		return m_filePathList;
	}
	
	public void setReturnCode(int code)
	{
		m_nReturnCode = code;
	}
	
	public int getReturnCode()
	{
		return m_nReturnCode;
	}

	public void setRequesterName(String name)
	{
		m_strRequesterName = name;
	}
	
	public String getRequesterName()
	{
		return m_strRequesterName;
	}

	public void setContentID(int id)
	{
		m_nContentID = id;
	}
	
	public int getContentID()
	{
		return m_nContentID;
	}
	
	public void setCreationTime(String date)
	{
		m_strDate = date;
	}
	
	public String getCreationTime()
	{
		return m_strDate;
	}
	
	public void setContentUploadResponseEvent(int nReturnCode, int nContentID, String strDate, String strRequesterName)
	{
		m_nReturnCode = nReturnCode;
		m_strRequesterName = strRequesterName;
		m_nContentID = nContentID;
		m_strDate = strDate;
	}
	
	////////////////////////////////////////////////////////
	
	public boolean containsFileName(String strFileName)
	{
		boolean bRet = false;
		if(m_filePathList == null) return bRet;
		for(int i = 0; i < m_filePathList.size() && !bRet; i++)
		{
			String strFilePath = m_filePathList.get(i);
			if(strFilePath.endsWith(strFileName))
				bRet = true;
		}
		
		return bRet;
	}
	
	public void init()
	{
		m_nNumCompleted = 0;
		m_filePathList = new ArrayList<String>();
		m_nReturnCode = -1;
		m_strRequesterName = null;
		m_nContentID = -1;
		m_strDate = null;
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMSNSAttach().init() initialized.");
	}
}
