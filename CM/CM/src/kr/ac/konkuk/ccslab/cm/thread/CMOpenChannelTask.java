package kr.ac.konkuk.ccslab.cm.thread;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;

public class CMOpenChannelTask implements Callable<SelectableChannel> {
	private int m_nChType;
	private String m_strAddress;
	private int m_nPort;
	private boolean m_isBlocking;
	private CMInfo m_cmInfo;
	
	public CMOpenChannelTask(int nChType, String strServerAddress, int nServerPort, 
			boolean isBlocking, CMInfo cmInfo)
	{
		m_nChType = nChType;
		m_strAddress = strServerAddress;
		m_nPort = nServerPort;
		m_isBlocking = isBlocking;
		m_cmInfo = cmInfo;
	}
	
	@Override
	public SelectableChannel call()
	{
		SelectableChannel sc = null;
		try {
			if(m_isBlocking)
				sc = CMCommManager.openBlockChannel(m_nChType, m_strAddress, m_nPort, m_cmInfo);
			else
				sc = CMCommManager.openNonBlockChannel(m_nChType, m_strAddress, m_nPort, m_cmInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sc;
	}
	
	// set/get methods
	public void setChannelType(int nChType)
	{
		m_nChType = nChType;
	}
	
	public int getChannelType()
	{
		return m_nChType;
	}
	
	public void setAddress(String strAddress)
	{
		m_strAddress = strAddress;
	}
	
	public String getAddress()
	{
		return m_strAddress;
	}
	
	public void setPort(int nPort)
	{
		m_nPort = nPort;
	}
	
	public int getPort()
	{
		return m_nPort;
	}
	
	public void setIsBlocking(boolean isBlocking)
	{
		m_isBlocking = isBlocking;
	}
	
	public boolean getIsBlocking()
	{
		return m_isBlocking;
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
