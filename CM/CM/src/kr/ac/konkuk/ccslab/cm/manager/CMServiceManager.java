package kr.ac.konkuk.ccslab.cm.manager;

import kr.ac.konkuk.ccslab.cm.entity.CMObject;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * The CMServiceManager class represents a CM service object.
 * 
 * An application or other CM objects can use a CM service with this class. 
 * 
 * @author CCSLab, Konkuk University
 *
 */
public abstract class CMServiceManager extends CMObject {
	protected CMInfo m_cmInfo;
	
	public CMServiceManager(CMInfo cmInfo)
	{
		m_nType = -1;
		m_cmInfo = cmInfo;
	}
	
	public void setCMInfo(CMInfo cmInfo)
	{
		m_cmInfo = cmInfo;
	}
	
	public CMInfo getCMInfo()
	{
		return m_cmInfo;
	}
	
}
