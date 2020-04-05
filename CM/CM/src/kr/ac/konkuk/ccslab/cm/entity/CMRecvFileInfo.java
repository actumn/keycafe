package kr.ac.konkuk.ccslab.cm.entity;

import java.io.RandomAccessFile;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

public class CMRecvFileInfo extends CMTransFileInfo {
	private long m_lRecvSize;
	private RandomAccessFile m_writeFile;		// for writing the received file block to the new file
	private SocketChannel m_recvChannel;	// the dedicated channel for receiving the file
	private Future<CMRecvFileInfo> m_recvTaskResult;	// the result of the submitted receiving task to the thread pool 

	public CMRecvFileInfo()
	{
		super();
		m_lRecvSize = -1;
		m_writeFile = null;
		m_recvChannel = null;
		m_recvTaskResult = null;
	}
	
	public CMRecvFileInfo(String strFile, long lSize)
	{
		super(strFile, lSize, -1);
		m_lRecvSize = -1;
		m_writeFile = null;
		m_recvChannel = null;
		m_recvTaskResult = null;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!super.equals(o)) return false;
		
		CMRecvFileInfo rfInfo = (CMRecvFileInfo) o;
		String strSenderName = rfInfo.getFileSender();
		
		if(strSenderName.equals(m_strFileSender))
			return true;
		return false;
	}

	@Override
	public String toString()
	{
		String str = super.toString();
		str += "; CMRecvFileInfo: received size("+m_lRecvSize+")";
		return str;
	}
	
	// set/get methods
	
	public synchronized void setRecvSize(long lSize)
	{
		m_lRecvSize = lSize;
		return;
	}
	
	public synchronized long getRecvSize()
	{
		return m_lRecvSize;
	}
	
	public synchronized void setWriteFile(RandomAccessFile acf)
	{
		m_writeFile = acf;
	}
	
	public synchronized RandomAccessFile getWriteFile()
	{
		return m_writeFile;
	}
	
	public synchronized void setRecvChannel(SocketChannel channel)
	{
		m_recvChannel = channel;
		return;
	}
	
	public synchronized SocketChannel getRecvChannel()
	{
		return m_recvChannel;
	}
	
	public synchronized void setRecvTaskResult(Future<CMRecvFileInfo> result)
	{
		m_recvTaskResult = result;
		return;
	}
	
	public synchronized Future<CMRecvFileInfo> getRecvTaskResult()
	{
		return m_recvTaskResult;
	}
	
}
