package kr.ac.konkuk.ccslab.cm.thread;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Callable;

import kr.ac.konkuk.ccslab.cm.entity.CMChannelInfo;

public class CMRemoveChannelTask implements Callable<Boolean> {
	private CMChannelInfo<Integer> m_chInfo;
	private int m_nChKey;
	private CMChannelInfo<InetSocketAddress> m_multicastChInfo;
	private InetSocketAddress m_sockAddr;
	
	public CMRemoveChannelTask(CMChannelInfo<Integer> chInfo, int nChKey)
	{
		m_chInfo = chInfo;
		m_nChKey = nChKey;
		m_multicastChInfo = null;
		m_sockAddr = null;
	}
	
	public CMRemoveChannelTask(CMChannelInfo<InetSocketAddress> chInfo, InetSocketAddress sockAddr)
	{
		m_multicastChInfo = chInfo;
		m_sockAddr = sockAddr;
		m_chInfo = null;
		m_nChKey = -1;
	}
	
	@Override
	public Boolean call()
	{
		Boolean bRet = false;
		if(m_chInfo != null)
			bRet = m_chInfo.removeChannel(m_nChKey);
		else if(m_multicastChInfo != null)
			bRet = m_multicastChInfo.removeChannel(m_sockAddr);
		return bRet;
	}
	
	// set/get methods
	public void setChannelInfo(CMChannelInfo<Integer> chInfo)
	{
		m_chInfo = chInfo;
	}
	
	public CMChannelInfo<Integer> getChannelInfo()
	{
		return m_chInfo;
	}
	
	public void setChannelKey(int nChKey)
	{
		m_nChKey = nChKey;
	}
	
	public int getChannelKey()
	{
		return m_nChKey;
	}

}
