package kr.ac.konkuk.ccslab.cm.info;

import kr.ac.konkuk.ccslab.cm.event.CMEventSynchronizer;
import kr.ac.konkuk.ccslab.cm.thread.CMEventReceiver;

public class CMEventInfo {
	private CMEventReceiver m_eventReceiver;
	private CMEventSynchronizer m_eventSynchronizer;
	
	public CMEventInfo()
	{
		m_eventReceiver = null;
		m_eventSynchronizer = new CMEventSynchronizer();		
	}
	
	// set/get methods

	public synchronized void setEventReceiver(CMEventReceiver receiver)
	{
		m_eventReceiver = receiver;
	}
	
	public synchronized CMEventReceiver getEventReceiver()
	{
		return m_eventReceiver;
	}
	
	public synchronized void setEventSynchronizer(CMEventSynchronizer sync)
	{
		m_eventSynchronizer = sync;
	}
	
	public synchronized CMEventSynchronizer getEventSynchronizer()
	{
		return m_eventSynchronizer;
	}
	
}
