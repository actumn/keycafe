package kr.ac.konkuk.ccslab.cm.info;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class CMThreadInfo {
	private ExecutorService m_executorService;
	private ScheduledExecutorService m_schedExecutorService;
	private ScheduledFuture<?> m_scheduledFuture;

	public CMThreadInfo()
	{
		m_executorService = null;
		m_schedExecutorService = null;
		m_scheduledFuture = null;
	}

	///// set/get methods
	
	public synchronized void setExecutorService(ExecutorService es)
	{
		m_executorService = es;
	}
	
	public synchronized ExecutorService getExecutorService()
	{
		return m_executorService;
	}
	
	public synchronized void setScheduledExecutorService(ScheduledExecutorService ses)
	{
		m_schedExecutorService = ses;
	}
	
	public synchronized ScheduledExecutorService getScheduledExecutorService()
	{
		return m_schedExecutorService;
	}

	public synchronized void setScheduledFuture(ScheduledFuture<?> future)
	{
		m_scheduledFuture = future;
	}
	
	public synchronized ScheduledFuture<?> getScheduledFuture()
	{
		return m_scheduledFuture;
	}

}
