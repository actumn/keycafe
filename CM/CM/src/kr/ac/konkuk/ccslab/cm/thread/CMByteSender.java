package kr.ac.konkuk.ccslab.cm.thread;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMInteractionManager;

public class CMByteSender extends Thread {

	private CMBlockingEventQueue m_sendQueue = null;
	private CMInfo m_cmInfo;
	
	public CMByteSender(CMInfo cmInfo)
	{
		m_sendQueue = cmInfo.getCommInfo().getSendBlockingEventQueue();
		m_cmInfo = cmInfo;
	}
	
	public void run()
	{
		CMMessage msg = null;
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMByteSender starts to send messages.");
		while(!Thread.currentThread().isInterrupted())
		{
			msg = m_sendQueue.pop();
			
			if(msg == null)
			{
				if(CMInfo._CM_DEBUG_2)
					System.out.println("CMByteSender.run(), msg is null.");
				break;
			}
			
			if(msg.m_ch instanceof SocketChannel)
			{
				CMCommManager.sendMessage(msg.m_buf, (SocketChannel)msg.m_ch);
				// update last event-transmission time of myself (client or server)
				CMInteractionManager.updateMyLastEventTransTime(msg.m_ch, m_cmInfo);
			}
			else if(msg.m_ch instanceof DatagramChannel)
			{
				String addr = ((InetSocketAddress)(msg.m_saddr)).getAddress().getHostAddress();
				int port = ((InetSocketAddress)(msg.m_saddr)).getPort();
				CMCommManager.sendMessage(msg.m_buf, (DatagramChannel)msg.m_ch, addr, port);
			}
			
			msg.m_buf = null;	// clear the sent ByteBuffer
			msg = null;			// clear the message
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMByteSender is terminated.");
	}
}
