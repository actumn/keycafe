package kr.ac.konkuk.ccslab.cm.sns;

import java.io.File;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMObject;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMSNSContent extends CMObject {
	private int m_nContentID;
	private String m_strDate;
	private String m_strWriterName;
	//private String m_strAttachedFileName;
	private String m_strMessage;
	private int m_nNumAttachedFiles;
	private int m_nReplyOf;
	private int m_nLevelOfDisclosure;
	private ArrayList<String> m_filePathList;
	
	public CMSNSContent()
	{
		m_nType = CMInfo.CM_SNS_CONTENT;
		m_nContentID = -1;
		m_strDate = "?";
		m_strWriterName = "?";
		//m_strAttachedFileName = "?";
		m_strMessage = "?";
		m_nNumAttachedFiles = -1;
		m_nReplyOf = -1;
		m_nLevelOfDisclosure = -1;
		m_filePathList = new ArrayList<String>();
	}
	
	public CMSNSContent(int id, String date, String writer, String msg, int nAttachment, int replyID, int lod)
	{
		m_nType = CMInfo.CM_SNS_CONTENT;
		m_nContentID = id;
		m_strDate = date;
		m_strWriterName = writer;
		//m_strAttachedFileName = fname;
		m_strMessage = msg;
		m_nNumAttachedFiles = nAttachment;
		m_nReplyOf = replyID;
		m_nLevelOfDisclosure = lod;
		m_filePathList = new ArrayList<String>();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null) return false;
		if(!this.getClass().equals(o.getClass())) return false;
		if(this == o) return true;

		CMSNSContent cont = (CMSNSContent)o;
		if(m_nContentID == cont.getContentID())
			return true;
		return false;
	}
	
	@Override
	public String toString()
	{
		String str = "CMSNSContent object: ID("+m_nContentID+"), Writer("+m_strWriterName+"), Date("
				+m_strDate+")";
		return str;
	}
	
	/////////////////////////////////// set methods
	public void setContentID(int id)
	{
		m_nContentID = id;
	}
	
	public void setDate(String date)
	{
		m_strDate = date;
	}
	
	public void setWriterName(String name)
	{
		m_strWriterName = name;
	}
	
	/*
	public void setAttachedFileName(String fname)
	{
		m_strAttachedFileName = fname;
	}
	*/
	
	public void setMessage(String msg)
	{
		m_strMessage = msg;
	}
	
	public void setNumAttachedFiles(int num)
	{
		m_nNumAttachedFiles = num;
	}
	
	public void setReplyOf(int id)
	{
		m_nReplyOf = id;
	}
	
	public void setLevelOfDisclosure(int lod)
	{
		m_nLevelOfDisclosure = lod;
	}
	
	public void setFilePathList(ArrayList<String> list)
	{
		m_filePathList = list;
	}
	
	/////////////////////////////////////// get methods
	public int getContentID()
	{
		return m_nContentID;
	}
	
	public String getDate()
	{
		return m_strDate;
	}
	
	public String getWriterName()
	{
		return m_strWriterName;
	}
	
	/*
	public String getAttachedFileName()
	{
		return m_strAttachedFileName;
	}
	*/
	
	public String getMessage()
	{
		return m_strMessage;
	}
	
	public int getNumAttachedFiles()
	{
		return m_nNumAttachedFiles;
	}
	
	public int getReplyOf()
	{
		return m_nReplyOf;
	}
	
	public int getLevelOfDisclosure()
	{
		return m_nLevelOfDisclosure;
	}
	
	public ArrayList<String> getFilePathList()
	{
		return m_filePathList;
	}
	
	///////////////////////////////////////////////////////////////
	
	public ArrayList<String> getFileNameList()
	{
		ArrayList<String> nameList = new ArrayList<String>();
		for(int i = 0; i < m_filePathList.size(); i++)
		{
			String strName = m_filePathList.get(i);
			int index = strName.lastIndexOf(File.separator)+1;
			strName = strName.substring(index);
			nameList.add(strName);
		}
		
		return nameList;
	}
	
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

}
