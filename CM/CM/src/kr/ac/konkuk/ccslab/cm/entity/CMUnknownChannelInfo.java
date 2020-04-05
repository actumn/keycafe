package kr.ac.konkuk.ccslab.cm.entity;

import java.nio.channels.SocketChannel;

public class CMUnknownChannelInfo {

	private SocketChannel m_unknownChannel;
	private long m_lLastEventTransTime;	// when this channel sent the last event
	private int m_nNumLoginFailure;
	
	public CMUnknownChannelInfo(SocketChannel ch)
	{
		m_unknownChannel = ch;
		m_lLastEventTransTime = System.currentTimeMillis();
		m_nNumLoginFailure = 0;
	}
	
	public synchronized void setUnknownChannel(SocketChannel ch)
	{
		m_unknownChannel = ch;
	}
	
	public synchronized SocketChannel getUnknownChannel()
	{
		return m_unknownChannel;
	}
	
	public synchronized void setLastEventTransTime(long lTime)
	{
		m_lLastEventTransTime = lTime;
	}
	
	public synchronized long getLastEventTransTime()
	{
		return m_lLastEventTransTime;
	}
	
	public synchronized void setNumLoginFailure(int nCount)
	{
		m_nNumLoginFailure = nCount;
	}
	
	public synchronized int getNumLoginFailure()
	{
		return m_nNumLoginFailure;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null) return false;
		if(!this.getClass().equals(obj.getClass())) return false;
		if(this == obj) return true;

		CMUnknownChannelInfo unch = (CMUnknownChannelInfo) obj;
		
		if( m_unknownChannel.equals(unch.getUnknownChannel()) )
			return true;
		
		return false;
	}

}
