package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;

import kr.ac.konkuk.ccslab.cm.entity.CMPoint3f;
import kr.ac.konkuk.ccslab.cm.entity.CMPosition;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * This class represents CM events that are used when a user changes the group membership 
 * or interacts with the other group members.
 * 
 * @author CCSLab, Konkuk University
 *
 */
public class CMInterestEvent extends CMEvent{

	/**
	 * The event ID for the request of joining a group in the default server.
	 * <p>event direction: client -&gt; default server
	 * <p>A client sends the USER_ENTER event after it processes 
	 * {@link CMSessionEvent#JOIN_SESSION_ACK}. This event is also sent when 
	 * the user changes his/her current group.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMInterestEvent#getUserName()}</li>
	 * <li>IP address of the user host: {@link CMInterestEvent#getHostAddress()}</li>
	 * <li>UDP port number of the user: {@link CMInterestEvent#getUDPPort()}</li>
	 * <li>current group name of the user: {@link CMInterestEvent#getCurrentGroup()}</li> 
	 * </ul>
	 */
	public static final int USER_ENTER = 1;
	
	/**
	 * The event ID for leaving a group in the default server.
	 * <p>event direction: client -&gt; default server
	 * <p>A client sends the USER_LEAVE event when it leaves the current group 
	 * so that it can enter a new group.
	 * <br>The following field is used for this event:
	 * <ul>
	 * <li>user name: {@link CMInterestEvent#getUserName()}</li>
	 * </ul>
	 */
	public static final int USER_LEAVE = 2;
	
	public static final int USER_MOVE = 3;
	public static final int USER_COLLIDE = 4;
	
	/**
	 * The event ID for a chatting message in a group.
	 * <p>event direction: client -&gt; server, client -&gt; client
	 * <p>The USER_TALK event is sent when a client which is a group member calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMClientStub#chat(String, String)}.
	 * <br>The following fields are used for this event:
	 * <ul>
	 * <li>user name: {@link CMInterestEvent#getUserName()}</li>
	 * <li>chatting message: {@link CMInterestEvent#getTalk()}</li>
	 * </ul>
	 */
	public static final int USER_TALK = 5;

	private String m_strUserName;
	private String m_strPasswd;
	private String m_strHostAddr;
	private int m_nUDPPort;
	private String m_strCurrentGroup;
	private String m_strTalk;
	private CMPosition m_pq;
	private String m_strCollideObj;

	public CMInterestEvent()
	{
		m_nType = CMInfo.CM_INTEREST_EVENT;
		m_strUserName = "";
		m_strPasswd = "";
		m_strHostAddr = "";
		m_nUDPPort = -1;
		m_strCurrentGroup = "";
		m_strTalk = "";
		m_strCollideObj = "";
	
		m_pq = new CMPosition();
		m_pq.m_p.m_x = 0.0f;
		m_pq.m_p.m_y = 0.0f;
		m_pq.m_p.m_z = 0.0f;
		m_pq.m_q.m_w = 0.0f;
		m_pq.m_q.m_x = 0.0f;
		m_pq.m_q.m_y = 0.0f;
		m_pq.m_q.m_z = 0.0f;
	}
	
	public CMInterestEvent(ByteBuffer msg)
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
	 * 
	 * @return the UDP port number.
	 */
	public int getUDPPort()
	{
		return m_nUDPPort;
	}
	
	public void setCurrentGroup(String gName)
	{
		if(gName != null)
			m_strCurrentGroup = gName;
	}
	
	/**
	 * Returns the current group name.
	 * 
	 * @return the current group name.
	 */
	public String getCurrentGroup()
	{
		return m_strCurrentGroup;
	}
	
	public void setTalk(String talk)
	{
		if(talk != null)
			m_strTalk = talk;
	}
	
	/**
	 * Returns the chatting message.
	 * 
	 * @return the chatting message.
	 */
	public String getTalk()
	{
		return m_strTalk;
	}
	
	public void setPoint3f(CMPoint3f p)
	{
		if(p != null)
			m_pq.m_p.setPoint(p.m_x, p.m_y, p.m_z);
	}
	
	public CMPoint3f getPoint3f()
	{
		return m_pq.m_p;
	}
	
	public void setPosition(CMPosition pq)
	{
		if(pq != null)
		{
			m_pq.m_p.setPoint(pq.m_p.m_x, pq.m_p.m_y, pq.m_p.m_z);
			m_pq.m_q.setQuat(pq.m_q.m_w, pq.m_q.m_x, pq.m_q.m_y, pq.m_q.m_z);
		}
	}
	
	public CMPosition getPosition()
	{
		return m_pq;
	}
	
	public void setCollideObj(String oName)
	{
		if(oName != null)
			m_strCollideObj = oName;
	}
	
	public String getCollideObj()
	{
		return m_strCollideObj;
	}
	
	///////////////////////////////////////////////////////////////////////
	protected int getByteNum()
	{
		int nByteNum = 0;
		nByteNum = super.getByteNum();
		
		switch(m_nID)
		{
		case CMInterestEvent.USER_ENTER:
			nByteNum += 3*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strHostAddr.getBytes().length + m_strCurrentGroup.getBytes().length;
			nByteNum += Integer.BYTES + 7*Float.BYTES;
			break;
		case CMInterestEvent.USER_LEAVE:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
			break;
		case CMInterestEvent.USER_MOVE:
			nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length + 7*Float.BYTES;
			break;
		case CMInterestEvent.USER_COLLIDE:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strCollideObj.getBytes().length;
			break;
		case CMInterestEvent.USER_TALK:
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length
				+ m_strTalk.getBytes().length;
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
		case CMInterestEvent.USER_ENTER:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strHostAddr);
			m_bytes.putInt(m_nUDPPort);
			putStringToByteBuffer(m_strCurrentGroup);
			m_bytes.putFloat(m_pq.m_p.m_x);
			m_bytes.putFloat(m_pq.m_p.m_y);
			m_bytes.putFloat(m_pq.m_p.m_z);
			m_bytes.putFloat(m_pq.m_q.m_w);
			m_bytes.putFloat(m_pq.m_q.m_x);
			m_bytes.putFloat(m_pq.m_q.m_y);
			m_bytes.putFloat(m_pq.m_q.m_z);
			break;
		case CMInterestEvent.USER_LEAVE:
			putStringToByteBuffer(m_strUserName);
			break;
		case CMInterestEvent.USER_MOVE:
			putStringToByteBuffer(m_strUserName);
			m_bytes.putFloat(m_pq.m_p.m_x);
			m_bytes.putFloat(m_pq.m_p.m_y);
			m_bytes.putFloat(m_pq.m_p.m_z);
			m_bytes.putFloat(m_pq.m_q.m_w);
			m_bytes.putFloat(m_pq.m_q.m_x);
			m_bytes.putFloat(m_pq.m_q.m_y);
			m_bytes.putFloat(m_pq.m_q.m_z);
			break;
		case CMInterestEvent.USER_COLLIDE:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strCollideObj);
			break;
		case CMInterestEvent.USER_TALK:
			putStringToByteBuffer(m_strUserName);
			putStringToByteBuffer(m_strTalk);
			break;
		default:
			System.out.println("CMInterestEvent.marshallBody(), unknown event id("+m_nID+").");
			m_bytes = null;
			break;
		}
	}
	
	protected void unmarshallBody(ByteBuffer msg)
	{
		switch(m_nID)
		{
		case CMInterestEvent.USER_ENTER:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strHostAddr = getStringFromByteBuffer(msg);
			m_nUDPPort = msg.getInt();
			m_strCurrentGroup = getStringFromByteBuffer(msg);
			
			m_pq.m_p.m_x = msg.getFloat();
			m_pq.m_p.m_y = msg.getFloat();
			m_pq.m_p.m_z = msg.getFloat();
			m_pq.m_q.m_w = msg.getFloat();
			m_pq.m_q.m_x = msg.getFloat();
			m_pq.m_q.m_y = msg.getFloat();
			m_pq.m_q.m_z = msg.getFloat();
			
			break;
		case CMInterestEvent.USER_LEAVE:
			m_strUserName = getStringFromByteBuffer(msg);
			break;
		case CMInterestEvent.USER_MOVE:
			m_strUserName = getStringFromByteBuffer(msg);

			m_pq.m_p.m_x = msg.getFloat();
			m_pq.m_p.m_y = msg.getFloat();
			m_pq.m_p.m_z = msg.getFloat();
			m_pq.m_q.m_w = msg.getFloat();
			m_pq.m_q.m_x = msg.getFloat();
			m_pq.m_q.m_y = msg.getFloat();
			m_pq.m_q.m_z = msg.getFloat();
			
			break;
		case CMInterestEvent.USER_COLLIDE:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strCollideObj = getStringFromByteBuffer(msg);
			break;
		case CMInterestEvent.USER_TALK:
			m_strUserName = getStringFromByteBuffer(msg);
			m_strTalk = getStringFromByteBuffer(msg);
			break;
		default:
			System.out.println("CMInterestEvent.unmarshallBody(), unknown event id("+m_nID+").");
			break;
		}
	}
}
