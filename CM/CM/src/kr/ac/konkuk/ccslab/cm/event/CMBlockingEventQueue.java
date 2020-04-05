package kr.ac.konkuk.ccslab.cm.event;

import java.util.List;

import kr.ac.konkuk.ccslab.cm.entity.CMMessage;

import java.util.ArrayList;

public class CMBlockingEventQueue {
	private final Object monitor = new Object();
	private final List<CMMessage> queue = new ArrayList<CMMessage>();

	public CMMessage pop()
	{
		synchronized(monitor)
		{
			if(queue.isEmpty())
			{
				try {
					monitor.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					return null;
				}
			}
			return queue.remove(0);
		}
	}
	
	public void push(CMMessage msg)
	{
		synchronized(monitor)
		{
			queue.add(msg);
			monitor.notify();
		}
	}
}
