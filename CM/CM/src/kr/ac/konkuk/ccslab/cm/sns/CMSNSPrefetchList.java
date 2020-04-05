package kr.ac.konkuk.ccslab.cm.sns;

import java.util.ArrayList;

import kr.ac.konkuk.ccslab.cm.entity.CMObject;

public class CMSNSPrefetchList extends CMObject {
	private ArrayList<String> m_filePathList;
	
	public CMSNSPrefetchList()
	{
		m_filePathList = new ArrayList<String>();
	}
	
	public ArrayList<String> getFilePathList()
	{
		return m_filePathList;
	}
	
	public boolean existFilePath(String strPath)
	{
		boolean bFound = false;
		for(int i = 0; i < m_filePathList.size() && !bFound; i++)
		{
			String tempPath = m_filePathList.get(i);
			if(strPath.equals(tempPath))
				bFound = true;
		}
		
		return bFound;
	}
	
	// find a path name with a file name
	public String findFilePath(String strName)
	{
		boolean bFound = false;
		String strPath = null;
		for(int i = 0; i < m_filePathList.size() && !bFound; i++)
		{
			strPath = m_filePathList.get(i);
			if(strPath.endsWith(strName))
				bFound = true;
		}
		
		if(bFound)
			return strPath;
		return null;
	}
	
	public boolean addFilePath(String strPath)
	{
		if(existFilePath(strPath))
		{
			System.err.println("CMSNSPrefetchList.addFilePath(), path("+strPath+") already exists!");
			return false;
		}
		
		boolean bRet = m_filePathList.add(strPath);
		return bRet;
	}
	
	public boolean addFilePathList(ArrayList<String> pathList)
	{
		boolean bRet = false;
		boolean bAdded = false;
		for(int i = 0; i < pathList.size(); i++)
		{
			bRet = addFilePath(pathList.get(i));
			if(bRet)	// if at least one element is added
				bAdded = true;
		}
		
		return bAdded;
	}
	
	public boolean removeFilePath(String strPath)
	{
		boolean bFound = false;
		int nIndex = -1;
		
		for(int i = 0; i < m_filePathList.size() && !bFound; i++)
		{
			String tempPath = m_filePathList.get(i);
			if(strPath.equals(tempPath))
			{
				bFound = true;
				nIndex = i;
			}
		}
		
		if(!bFound)
		{
			System.err.println("CMSNSPrefetchList.removeFilePath(), path("+strPath+") not found!");
			return false;
		}
		
		m_filePathList.remove(nIndex);
		return true;
	}
	
	public void removeAllFilePaths()
	{
		m_filePathList.clear();
	}
}
