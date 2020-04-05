package kr.ac.konkuk.ccslab.cm.info;
import java.io.IOException;
import java.nio.channels.*;

import kr.ac.konkuk.ccslab.cm.entity.CMChannelInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMUnknownChannelInfo;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.thread.CMByteReceiver;
import kr.ac.konkuk.ccslab.cm.thread.CMByteSender;

public class CMCommInfo {
	private ServerSocketChannel m_nonBlockServerSocketChannel; // nonblocking server socket channel
	private ServerSocketChannel m_blockServerSocketChannel; // blocking server socket channel
	private CMChannelInfo<Integer> m_nonBlockDCInfo;	// nonblocking datagram channel list
	private CMChannelInfo<Integer> m_blockDCInfo;		// blocking datagram channel list
	private CMList<CMUnknownChannelInfo> m_unknownChannelInfoList;	// 4 server and client
	
	//private Vector<SocketChannel> m_scList;
	//private Vector<DatagramCha	nnel> m_dcList;
	//private Vector<MulticastChannel> m_mcList;
	private Selector m_selector;
	private CMBlockingEventQueue m_recvQueue;
	private CMBlockingEventQueue m_sendQueue;
	private CMByteReceiver m_byteReceiver;
	private CMByteSender m_byteSender;
	
	//private Vector<SelectableChannel> m_toBeDeletedChannelList; 
	//for datagram
	//private int m_nDatagramID;
	//private Vector<CMDatagramPacket> m_datagramPacketList;
	// for delay
	private long m_lStart;
	private long m_lEnd;
	private long m_lPDelay;
	// for service rate
	private long m_lRecvCount;
	private long m_lTotalByte;
	
	public CMCommInfo()
	{
		m_nonBlockServerSocketChannel = null;
		m_blockServerSocketChannel = null;
		m_nonBlockDCInfo = new CMChannelInfo<Integer>();
		m_blockDCInfo = new CMChannelInfo<Integer>();
		m_unknownChannelInfoList = new CMList<CMUnknownChannelInfo>();
		
		m_byteReceiver = null;
		m_byteSender = null;
		//m_scList = new Vector<SocketChannel>();
		//m_dcList = new Vector<DatagramChannel>();
		//m_mcList = new Vector<MulticastChannel>();
		//m_toBeDeletedChannelList = new Vector<SelectableChannel>();
		//m_datagramPacketList = new Vector<CMDatagramPacket>();
		
		//m_nDatagramID = 0;
		m_lStart = 0;
		m_lEnd = 0;
		m_lPDelay = 0;
		m_lRecvCount = 0;
		m_lTotalByte = 0;
		try {
			m_selector = Selector.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_recvQueue = new CMBlockingEventQueue();
		m_sendQueue = new CMBlockingEventQueue();
	}
	
	// set/get methods
	public synchronized CMBlockingEventQueue getRecvBlockingEventQueue()
	{
		return m_recvQueue;
	}
	
	public synchronized CMBlockingEventQueue getSendBlockingEventQueue()
	{
		return m_sendQueue;
	}
	
	public synchronized void setNonBlockServerSocketChannel(ServerSocketChannel ssc)
	{
		m_nonBlockServerSocketChannel = ssc;
	}
	
	public synchronized ServerSocketChannel getNonBlockServerSocketChannel()
	{
		return m_nonBlockServerSocketChannel;
	}
	
	public synchronized void setBlockServerSocketChannel(ServerSocketChannel ssc)
	{
		m_blockServerSocketChannel = ssc;
	}
	
	public synchronized ServerSocketChannel getBlockServerSocketChannel()
	{
		return m_blockServerSocketChannel;
	}
	
	public synchronized void setUnknownChannelInfoList(CMList<CMUnknownChannelInfo> list)
	{
		m_unknownChannelInfoList = list;
	}
	
	public synchronized CMList<CMUnknownChannelInfo> getUnknownChannelInfoList()
	{
		return m_unknownChannelInfoList;
	}
	
	public synchronized void setByteReceiver(CMByteReceiver receiver)
	{
		m_byteReceiver = receiver;
	}
	
	public synchronized CMByteReceiver getByteReceiver()
	{
		return m_byteReceiver;
	}
	
	public synchronized void setByteSender(CMByteSender sender)
	{
		m_byteSender = sender;
	}
	
	public synchronized CMByteSender getByteSender()
	{
		return m_byteSender;
	}
	
	public synchronized void setStartTime(long start)
	{
		m_lStart = start;
	}
	
	public synchronized long getStartTime()
	{
		return m_lStart;
	}
	
	public synchronized void setEndTime(long end)
	{
		m_lEnd = end;
	}
	
	public synchronized long getEndTime()
	{
		return m_lEnd;
	}
	
	public synchronized void setPDelay(long delay)
	{
		m_lPDelay = delay;
	}
	
	public synchronized long getPDelay()
	{
		return m_lPDelay;
	}
	
	public synchronized void setRecvCount(long count)
	{
		m_lRecvCount = count;
	}
	
	public synchronized long getRecvCount()
	{
		return m_lRecvCount;
	}
	
	public synchronized void setTotalByte(long num)
	{
		m_lTotalByte = num;
	}
	
	public synchronized long getTotalByte()
	{
		return m_lTotalByte;
	}
	
	public synchronized Selector getSelector()
	{
		return m_selector;
	}
	
	public synchronized CMChannelInfo<Integer> getNonBlockDatagramChannelInfo()
	{
		return m_nonBlockDCInfo;
	}
	
	public synchronized CMChannelInfo<Integer> getBlockDatagramChannelInfo()
	{
		return m_blockDCInfo;
	}
		


}
