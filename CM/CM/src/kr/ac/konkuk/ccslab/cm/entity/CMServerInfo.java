package kr.ac.konkuk.ccslab.cm.entity;
import kr.ac.konkuk.ccslab.cm.stub.*;
////////////////////////////////////////////////////////
//CMServerInfo (server info to be transfered in an event)

/**
 * This class represents CM server information that needs to be transferred to a remote CM node.
 * 
 * @author CCSLab, Konkuk University
 * @see kr.ac.konkuk.ccslab.cm.stub.CMClientStub#requestServerInfo()
 */
public class CMServerInfo {
	protected String m_strServerName;
	protected String m_strServerAddress;
	protected int m_nServerPort;
	protected int m_nServerUDPPort;
	
	public CMServerInfo()
	{
		m_strServerName = "";
		m_strServerAddress = "";
		m_nServerPort = -1;
		m_nServerUDPPort = -1;
	}
	
	public CMServerInfo(String sname, String saddr, int sport, int sudpport)
	{
		m_strServerName = sname;
		m_strServerAddress = saddr;
		m_nServerPort = sport;
		m_nServerUDPPort = sudpport;
	}

	public synchronized void setServerName(String sname)
	{
		m_strServerName = sname;
	}
	
	public synchronized void setServerAddress(String saddr)
	{
		m_strServerAddress = saddr;
	}
	
	public synchronized void setServerPort(int port)
	{
		m_nServerPort = port;
	}
	
	public synchronized void setServerUDPPort(int port)
	{
		m_nServerUDPPort = port;
	}

	public synchronized String getServerName()
	{
		return m_strServerName;
	}
	
	public synchronized String getServerAddress()
	{
		return m_strServerAddress;
	}
	
	public synchronized int getServerPort()
	{
		return m_nServerPort;
	}
	
	public synchronized int getServerUDPPort()
	{
		return m_nServerUDPPort;
	}
}
