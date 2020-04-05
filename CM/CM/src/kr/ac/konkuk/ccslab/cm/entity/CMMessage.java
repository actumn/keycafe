package kr.ac.konkuk.ccslab.cm.entity;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

// CM message which will be delivered to CMEventReceiver by CMByteReceiver
public class CMMessage {
	public ByteBuffer m_buf;			// received bytes
	public SelectableChannel m_ch;		// receiver channel
	public SocketAddress m_saddr;		// sender address (4 DatagramChannel)
	
	public CMMessage()
	{
		m_buf = null;
		m_ch = null;
		m_saddr = null;
	}
	
	public CMMessage(ByteBuffer buf, SelectableChannel sc)
	{
		m_buf = buf;
		m_ch = sc;
	}
	
	public CMMessage(ByteBuffer buf, SelectableChannel sc, SocketAddress addr)
	{
		m_buf = buf;
		m_ch = sc;
		m_saddr = addr;
	}
}
