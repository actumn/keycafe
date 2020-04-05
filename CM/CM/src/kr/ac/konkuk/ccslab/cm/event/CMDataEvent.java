package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;

import kr.ac.konkuk.ccslab.cm.entity.CMPosition;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * This class represents CM events that are used for notifying a new group user or 
 * existing group members of the information of group membership change.
 * 
 * @author CCSLab, Konkuk University
 *
 */
public class CMDataEvent extends CMEvent{

	/**
	 * The event ID for sending an existing group user from a server to a new group user.
	 * <p>event direction: server -&gt; client
	 * <p>The server CM sends the INHABITANT event to the client CM of the new group user   
	 * after the server processes the {@link CMInterestEvent#USER_ENTER} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMDataEvent#getUserName()}</li>
	 * <li>host address: {@link CMDataEvent#getHostAddress()}</li>
	 * <li>UDP port number: {@link CMDataEvent#getUDPPort()}</li>
	 * </ul>
	 */
	public static final int INHABITANT = 1;
	
	/**
	 * The event ID for sending new group user information from a server to existing 
	 * group members.
	 * <p>event direction: server -&gt; client
	 * <p>The server CM sends the NEW_USER event to the existing group members 
	 * right after the server sends the {@link CMDataEvent#INHABITANT} events.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>new user name: {@link CMDataEvent#getUserName()}</li>
	 * <li>host address: {@link CMDataEvent#getHostAddress()}</li>
	 * <li>UDP port number: {@link CMDataEvent#getUDPPort()}</li> 
	 * </ul>
	 */
	public static final int NEW_USER = 2;
	
	/**
	 * The event ID for sending a group-leaving user information from a server to 
	 * the group members.
	 * <p>event direction: server -&gt; client
	 * <p>The server CM sends the REMOVE_USER event to the group members  
	 * after it processes the {@link CMInterestEvent#USER_LEAVE} event.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMDataEvent#getUserName()}</li>
	 * </ul>
	 */
	public static final int REMOVE_USER = 3;
	
	/*
	 * current not used!
	 */
	public static final int REQUEST_INHABITANT = 4;
	
	private String m_strUserName;
	private String m_strPasswd;
	private String m_strHostAddr;
	private int m_nUDPPort;
	private CMPosition m_pq;
	
	public CMDataEvent()
	{
		m_nType = CMInfo.CM_DATA_EVENT;
		m_strUserName = "?";
		m_strPasswd = "?";
		m_strHostAddr = "?";
		m_nUDPPort = -1;
		
		m_pq = new CMPosition();
	}
	
	public CMDataEvent(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}
	
	// set/get methods
	
	public void setUserName(String name)
	{
		if(name != null)
			m_strUserName = name;
	}
	
	/**
	 * Returns the user name.
	 * @return the user name.
	 */
	public String getUserName()
	{
		return m_strUserName;
	}
	
	public void setPassword(String passwd)
	{
		if(passwd != null)
			m_strPasswd = passwd;
	}
	
	public String getPassword()
	{
		return m_strPasswd;
	}
	
	public void setHostAddress(String host)
	{
		if(host != null)
			m_strHostAddr = host;
	}
	
	/**
	 * Returns the host IP address.
	 * 
	 * @return the host IP address.
	 */
	public String getHostAddress()
	{
		return m_strHostAddr;
	}
	
	public void setUDPPort(int port)
	{
		m_nUDPPort = port;
	}
	
	/**
	 * Returns the UDP port number.
	 * @return the UDP port number.
	 */
	public int getUDPPort()
	{
		return m_nUDPPort;
	}
	
	public void setPosition(CMPosition pq)
	{
		if(pq != null)
		{
			m_pq.m_p.m_x = pq.m_p.m_x;
			m_pq.m_p.m_y = pq.m_p.m_y;
			m_pq.m_p.m_z = pq.m_p.m_z;
			m_pq.m_q.m_w = pq.m_q.m_w;
			m_pq.m_q.m_x = pq.m_q.m_x;
			m_pq.m_q.m_y = pq.m_q.m_y;
			m_pq.m_q.m_z = pq.m_q.m_z;
		}
	}
	
	public CMPosition getPosition()
	{
		return m_pq;
	}
	
	//////////////////////////////////////////////////////////
	
	protected int getByteNum()
	{
		int nByteNum = 0;
		nByteNum = super.getByteNum();
		
		switch(m_nID)
		{
		case CMDataEvent.INHABITANT:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strHostAddr.getBytes().length;
			nByteNum += Integer.BYTES + 7*Float.BYTES;
			break;
		case CMDataEvent.NEW_USER:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
			+ m_strHostAddr.getBytes().length;
			nByteNum += Integer.BYTES;
			break;
		case CMDataEvent.REMOVE_USER:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		case CMDataEvent.REQUEST_INHABITANT:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		default:
			nByteNum = -1;
			break;
		}
		
		return nByteNum;
	}
	
	protected void marshallBody()
	{
		switch(m_nID)
		{
		case CMDataEvent.INHABITANT:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strHostAddr);
			m_bytes.putInt(m_nUDPPort);
			m_bytes.putFloat(m_pq.m_p.m_x);
			m_bytes.putFloat(m_pq.m_p.m_y);
			m_bytes.putFloat(m_pq.m_p.m_z);
			m_bytes.putFloat(m_pq.m_q.m_w);
			m_bytes.putFloat(m_pq.m_q.m_x);
			m_bytes.putFloat(m_pq.m_q.m_y);
			m_bytes.putFloat(m_pq.m_q.m_z);
			break;
		case CMDataEvent.NEW_USER:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strHostAddr);
			m_bytes.putInt(m_nUDPPort);
			break;
		case CMDataEvent.REMOVE_USER:
			putStringToByteBuffer(m_strUserName);
			break;
		case CMDataEvent.REQUEST_INHABITANT:
			putStringToByteBuffer(m_strUserName);
			break;
		default:
			System.out.println("CMDataEvent.marshallBody(), unknown event id("+m_nID+").");
			m_bytes = null;
			break;
		}
		
	}
	
	protected void unmarshallBody(ByteBuffer msg)
	{
		switch(m_nID)
		{
		case CMDataEvent.INHABITANT:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strHostAddr = getStringFromByteBuffer(msg);
			m_nUDPPort = msg.getInt();
			m_pq.m_p.m_x = msg.getFloat();
			m_pq.m_p.m_y = msg.getFloat();
			m_pq.m_p.m_z = msg.getFloat();
			m_pq.m_q.m_w = msg.getFloat();
			m_pq.m_q.m_x = msg.getFloat();
			m_pq.m_q.m_y = msg.getFloat();
			m_pq.m_q.m_z = msg.getFloat();
			break;
		case CMDataEvent.NEW_USER:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strHostAddr = getStringFromByteBuffer(msg);
			m_nUDPPort = msg.getInt();
			break;
		case CMDataEvent.REMOVE_USER:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case CMDataEvent.REQUEST_INHABITANT:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		default:
			System.out.println("CMDataEvent.unmarshallBody(), unknown event id("+m_nID+").");
			break;
		}
	}
	
}
