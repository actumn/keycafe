package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;
import kr.ac.konkuk.ccslab.cm.entity.CMObject;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * This class represents a CM event.
 * <br> The CMEvent class is the superclass of every CM event. The CMEvent class includes the common header 
 * fields of a CM event such as sender, receiver, which session and group (handler session and group names) 
 * should handle this event and which session and group members (distribution session and group names) 
 * this event should be forwarded to.  
 * <p> CM nodes (a client or a server) communicate with each other by exchanging CM events.
 * <br> CM application can send and receive a CM event through event-transmission methods 
 * of the CM stub module ({@link kr.ac.konkuk.ccslab.cm.stub.CMStub}).
 * 
 * @author CCSLab, Konkuk University
 * 
 */
public abstract class CMEvent extends CMObject {
	
	protected String m_strSender;
	protected String m_strReceiver;
	protected String m_strHandlerSession;
	protected String m_strHandlerGroup;
	protected String m_strDistributionSession;
	protected String m_strDistributionGroup;
	protected int m_nID;
	protected int m_nByteNum;	// total number of bytes in the event
	protected ByteBuffer m_bytes;

	/**
	 * Creates an empty CMEvent object.
	 */
	public CMEvent()
	{
		m_nType = CMInfo.CM_EVENT;
		m_nID = -1;
		m_strSender = "";
		m_strReceiver = "";
		m_strHandlerSession = "";
		m_strHandlerGroup = "";
		m_strDistributionSession = "";
		m_strDistributionGroup = "";
		m_nByteNum = -1;
		m_bytes = null;
	}
	
	/**
	 * Creates a CMEvent object.
	 * <br> This CM object marshalls a given bytes of a message.
	 * @param msg - the bytes of a message
	 */
	public CMEvent(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}

	/**
	 * Marshals the CM event.
	 * <br> This method changes the event fields into bytes in the ByteBuffer so that the event can be sent 
	 * through the communication channel.
	 * <br> If the subclass of the CMEvent class is instantiated and the object calls this method, it conducts 
	 * marshalling of both the event header and the event body fields.
	 * 
	 * @return a reference to the ByteBuffer object that includes the marshalled event fields.
	 * If the instance of the subclass of the CMEvent calls this method, the ByteBuffer object includes both 
	 * the marshalled event header and body fields.
	 * 
	 */
	public ByteBuffer marshall()
	{
		boolean bResult = false;
		
		bResult = allocate();
		if(bResult)
		{
			m_bytes.clear();
			marshallHeader();
			marshallBody();
		}
		else
			m_bytes = null;
		
		return m_bytes;
	}
	
	/**
	 * Unmarshals the given ByteBuffer to the CM event.
	 * <br> This method changes bytes in the ByteBuffer to the corresponding event fields of this event object.
	 * <br> If the subclass of the CMEvent class is instantiated and the object calls this method, it conducts 
	 * unmarshalling of both the event header and the event body fields.
	 * 
	 * @param msg - the bytes to be unmarshalled
	 * @return a reference to the CMEvent object.
	 * 
	 */
	public CMEvent unmarshall(ByteBuffer msg)
	{
		// should be implemented in sub-classes
		msg.clear();
		unmarshallHeader(msg);
		unmarshallBody(msg);
		return this;
	}

	/**
	 * Sets the event ID field.
	 * <br> When the event object is initialized, the event ID is set to -1.
	 * @param id - the event ID
	 */
	public void setID(int id)
	{
		m_nID = id;
	}
	
	/**
	 * Returns the event ID.
	 * @return an event ID, or -1 if this event does not set any ID.
	 */
	public int getID()
	{
		return m_nID;
	}
	
	/**
	 * Sets the sender name.
	 * @param strName - the sender name
	 */
	public void setSender(String strName)
	{
		if(strName != null)
			m_strSender = strName;
	}
	
	/**
	 * Returns the sender name.
	 * @return a sender name.
	 */
	public String getSender()
	{
		return m_strSender;
	}
	
	/**
	 * Sets the receiver name.
	 * @param strName - the receiver name.
	 */
	public void setReceiver(String strName)
	{
		if(strName != null)
			m_strReceiver = strName;
	}
	
	/**
	 * Returns the receiver name.
	 * @return a receiver name.
	 */
	public String getReceiver()
	{
		return m_strReceiver;
	}

	/**
	 * Sets a session for handling this event.
	 * <br> The session name determines which session deals with this event.
	 * <br> When the event object is initialized, the session for handling this event is set to the empty string (""). 
	 * The empty session name specifies that this event is internally handled by the CMInteractionManager of 
	 * the receiver CM.
	 * 
	 * @param sName - the session name
	 */
	public void setHandlerSession(String sName)
	{
		if(sName != null)
			m_strHandlerSession = sName;
	}
	
	/**
	 * Sets a group for handling this event.
	 * <br> The group name determines which group deals with this event.
	 * <br> When the event object is initialized, the group for handling this event is set to the empty string (""). 
	 * If the group name is empty and the specific session name is set, this event is internally handled by 
	 * the CMSessionManager of the receiver CM.
	 * If both the group and the session are empty, this event is internally handled by the CMInteractionManager of 
	 * the receiver CM. If both the group and the session are set to specific names, this event is internally handled by 
	 * the CMGroupManager of the receiver CM.
	 *  
	 * @param gName - the group name
	 */
	public void setHandlerGroup(String gName)
	{
		if(gName != null)
			m_strHandlerGroup = gName;
	}
	
	/**
	 * Sets a session to which this event will be forwarded by the server.
	 * <br> The session name determines to which session the server will forward this event after it receives and processes 
	 * this event. Normally, session and group for the distribution of the event are determined by CM when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMStub#send(CMEvent, String)}, 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMStub#cast(CMEvent, String, String)}, or 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMStub#broadcast(CMEvent)} methods.
	 * <p> When the event object is initialized, the session for distribution is set to the empty string ("").
	 * If the session name is empty, the server does not forward this event. 
	 * If the session is set to a specific name, the distribution target is determined by the group name for 
	 * the distribution. 
	 * If the session name is "CM_ALL_SESSION", the server forwards this event to members of all sessions and groups.
	 * If the session name is "CM_ONE_USER", the distribution target is one user whose name should be set in the group 
	 * name for the distribution.  
	 *
	 * @param sName - the session name for the distribution of this event.
	 * <br> The sName value can be a session name, "CM_ALL_SESSION", or "CM_ONE_USER".
	 * 
	 * @see CMEvent#setDistributionGroup(String)
	 * @see CMEvent#getDistributionSession()
	 */
	public void setDistributionSession(String sName)
	{
		if(sName != null)
			m_strDistributionSession = sName;
	}
	
	/**
	 * Sets a group to which this event will be forwarded by the server.
	 * <br> The group name determines to which group the server will forward this event after it receives and processes 
	 * this event. Normally, session and group for the distribution of the event are determined by CM when the client calls 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMStub#send(CMEvent, String)}, 
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMStub#cast(CMEvent, String, String)}, or
	 * {@link kr.ac.konkuk.ccslab.cm.stub.CMStub#broadcast(CMEvent)} methods.
	 * <p> When the event object is initialized, the group for distribution is set to the empty string ("").
	 * If the group name is empty, the server does not forward this event.
	 * If the group is set to a specific name, the server forwards this event to the corresponding group members.
	 * If the group name is CM_ALL_GROUP", the server forwards this event to members of all groups.
	 * If the group name is a specific user name (and if the session name is "CM_ONE_USER"), the server forwards this event 
	 * to the corresponding specific user.
	 * 
	 * @param gName - the group name for the distribution of this event.
	 * <br> The gName value can be a group name, "CM_ALL_GROUP", or a specific user name.
	 * 
	 * @see CMEvent#setDistributionGroup(String)
	 * @see CMEvent#getDistributionGroup()
	 */
	public void setDistributionGroup(String gName)
	{
		if(gName != null)
			m_strDistributionGroup = gName;
	}

	/**
	 * Returns a session name for handling this event.
	 * <br> The session name determines which session deals with this event.
	 * 
	 * @return - a session name for handling this event.
	 * @see CMEvent#getHandlerSession()
	 */
	public String getHandlerSession()
	{
		return m_strHandlerSession;
	}
	
	/**
	 * Returns a group name for handling this event.
	 * <br> The group name determines which group deals with this event.
	 * 
	 * @return - a group name for handling this event.
	 * @see CMEvent#getHandlerGroup()
	 */
	public String getHandlerGroup()
	{
		return m_strHandlerGroup;
	}
	
	/**
	 * Returns a target session to which the server forwards this event.
	 * <br> The session name determines to which session the server will forward this event after it receives and processes 
	 * this event.
	 * @return - a session name for distributing this event.
	 * @see CMEvent#setDistributionSession(String)
	 */
	public String getDistributionSession()
	{
		return m_strDistributionSession;
	}
	
	/**
	 * Returns a target group to which the server forwards this event.
	 * <br> The group name determines to which group the server will forward this event after it receives and processes 
	 * this event.
	 * @return -  a group name for distributing this event.
	 * @see CMEvent#setDistributionGroup(String)
	 */
	public String getDistributionGroup()
	{
		return m_strDistributionGroup;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null) return false;
		if(!this.getClass().equals(obj.getClass())) return false;
		if(this == obj) return true;

		//if(!super.equals(obj)) return false;
		CMEvent cme = (CMEvent) obj;
		
		if(m_nID == cme.getID() && m_nByteNum == cme.getByteNum() &&
				m_strSender.equals(cme.getSender()) &&
				m_strReceiver.equals(cme.getReceiver()) &&
				m_strHandlerSession.equals(cme.getHandlerSession()) &&
				m_strHandlerGroup.equals(cme.getHandlerGroup()) &&
				m_strDistributionSession.equals(cme.getDistributionSession()) &&
				m_strDistributionGroup.equals(cme.getDistributionGroup()))
			return true;
		
		return false;
	}
	
	@Override
	public String toString()
	{
		StringBuffer strEventHeader = new StringBuffer();
		strEventHeader.append("{");
		strEventHeader.append(super.toString()+", "); // m_nType of CMObject
		strEventHeader.append("\"ID\": "+m_nID+", ");
		strEventHeader.append("\"ByteNum\": "+m_nByteNum+", ");
		strEventHeader.append("\"sender\": \""+m_strSender+"\", ");
		strEventHeader.append("\"receiver\": \""+m_strReceiver+"\", ");
		strEventHeader.append("\"handlerSession\": \""+m_strHandlerSession+"\", ");
		strEventHeader.append("\"handlerGroup\": \""+m_strHandlerGroup+"\", ");
		strEventHeader.append("\"distributionSession\": \""+m_strDistributionSession+"\", ");
		strEventHeader.append("\"distributionGroup\": \""+m_strDistributionGroup+"\"");
		strEventHeader.append("}");
		
		return strEventHeader.toString();
	}
	
	/////////////////////////////////////////////////
	
	protected boolean allocate()
	{
		m_nByteNum = getByteNum();
		if(m_nByteNum > CMInfo.MAX_EVENT_SIZE)
		{
			System.err.println("CMEvent.allocate(): the byte number("+m_nByteNum
					+") is greater than the maximum event size ("+CMInfo.MAX_EVENT_SIZE+")!");
			return false;
		}
		m_bytes = ByteBuffer.allocate(m_nByteNum);
		return true;
		// this allocated object should be deallocated after the event is sent by a sending method.
	}
	
	protected void marshallHeader()
	{
		
		m_bytes.putInt(m_nByteNum);
		m_bytes.putInt(m_nType);
		m_bytes.putInt(m_nID);
		putStringToByteBuffer(m_strSender);
		putStringToByteBuffer(m_strReceiver);
		putStringToByteBuffer(m_strHandlerSession);
		putStringToByteBuffer(m_strHandlerGroup);
		putStringToByteBuffer(m_strDistributionSession);
		putStringToByteBuffer(m_strDistributionGroup);
		//m_bytes.rewind();

	}
	
	protected void unmarshallHeader(ByteBuffer msg)
	{
		m_nByteNum = msg.getInt();
		m_nType = msg.getInt();
		m_nID = msg.getInt();

		m_strSender = getStringFromByteBuffer(msg);
		m_strReceiver = getStringFromByteBuffer(msg);
		m_strHandlerSession = getStringFromByteBuffer(msg);
		m_strHandlerGroup = getStringFromByteBuffer(msg);
		m_strDistributionSession = getStringFromByteBuffer(msg);
		m_strDistributionGroup = getStringFromByteBuffer(msg);

	}
	
	protected abstract void marshallBody();
	
	protected abstract void unmarshallBody(ByteBuffer msg);

	protected void setByteNum(int nByteNum)
	{
		m_nByteNum = nByteNum;
	}
	
	protected int getByteNum()
	{
		// can be re-implemented by sub-class
		int nSize;
		nSize = (Integer.BYTES)*3;	// 3 int fields
		// 6 string fields
		nSize += 6*CMInfo.STRING_LEN_BYTES_LEN + m_strSender.getBytes().length
				+ m_strReceiver.getBytes().length + m_strHandlerSession.getBytes().length
				+ m_strHandlerGroup.getBytes().length + m_strDistributionSession.getBytes().length
				+ m_strDistributionGroup.getBytes().length;
		return nSize;
	}
	
	protected void putStringToByteBuffer(String str)
	{
		int nStrLength = 0;
		if(str != null)
			nStrLength = str.getBytes().length;
		putInt2BytesToByteBuffer(nStrLength);
		m_bytes.put(str.getBytes());
	}
	
	protected String getStringFromByteBuffer(ByteBuffer buf)
	{
		int nStrLength = 0;
		byte[] strBytes;
		String str = null;
		
		nStrLength = getInt2BytesFromByteBuffer(buf);
		strBytes = new byte[nStrLength];
		buf.get(strBytes);
		str = new String(strBytes);
		/*
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMEvent.getStringFromByteBuffer(), str(\""+str+"\")");
		}
		*/
		return str;
	}

	// put 2 bytes number to ByteBuffer
	protected void putInt2BytesToByteBuffer(int num)
	{
		byte[] numBytes = new byte[2];
		numBytes[0] = 0;
		numBytes[1] = 0;
		
		numBytes[0] = (byte)((num & 0x0000ff00) >>> 8);
		numBytes[1] = (byte)(num & 0x000000ff);
		
		m_bytes.put(numBytes);
		/*
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMEvent.putInt2BytesToByteBuffer(), num("+num
					+"), numBytes[0]("+numBytes[0]+"), numBytes[1]("+numBytes[1]+")");
			System.out.println("numBytes[0]: "+Integer.toBinaryString(numBytes[0]));
			System.out.println("numBytes[1]: "+Integer.toBinaryString(numBytes[1]));
		}
		*/
	}	
	
	// get (unsigned) integer with 2 bytes from ByteBuffer
	protected int getInt2BytesFromByteBuffer(ByteBuffer buf)
	{
		int num = 0;
		byte[] numBytes = new byte[2];
		buf.get(numBytes);
		
		num = (num | ((int)numBytes[0] & 0x000000ff) ) << 8;
		num = num | ((int)numBytes[1] & 0x000000ff);
		
		/*
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMEvent.getInt2BytesFromByteBuffer(), numBytes[0]("
					+numBytes[0]+"), numBytes[1]("+numBytes[1]+"), num("+num+")");
			System.out.println("numBytes[0]: "+Integer.toBinaryString(numBytes[0]));
			System.out.println("numBytes[1]: "+Integer.toBinaryString(numBytes[1]));
			System.out.println("num: "+Integer.toBinaryString(num));
		}
		*/
		
		return num;
	}
}
