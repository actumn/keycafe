package kr.ac.konkuk.ccslab.cm.event.mqttevent;

import java.nio.ByteBuffer;

/**
 * This class represents a CM event that belongs to the variable header and payload of 
 * MQTT CONNACK packet.
 * @author CCSLab, Konkuk University
 * @see <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718033">
 * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718033</a>
 */
public class CMMqttEventCONNACK extends CMMqttEventFixedHeader {

	//////////////////////////////////////////////////
	// member variables (variable header)
	byte m_connAckFlag;	// 1 byte, 0000000X
	byte m_connReturnCode;	// 1 byte, 0 ~ 5
	
	//////////////////////////////////////////////////
	// constructors

	/**
	 * Creates an instance of the CMMqttEventCONNACK class.
	 */
	public CMMqttEventCONNACK()
	{
		// initialize CM event ID
		m_nID = CMMqttEvent.CONNACK;	// 2
		// initialize fixed header
		m_packetType = CMMqttEvent.CONNACK;	// 2
		m_flag = 0;
		
		// initialize variable header
		m_connAckFlag = 0;
		m_connReturnCode = 0;
	}
	
	public CMMqttEventCONNACK(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}
	
	//////////////////////////////////////////////////
	// setter/getter (variable header)

	/**
	 * Sets the variable header of the MQTT CONNACK packet.
	 * 
	 * @param bConnAckFlag - session present flag 
	 * @param returnCode - return code
	 */
	public void setVarHeader(boolean bConnAckFlag, byte returnCode)
	{
		if(bConnAckFlag) m_connAckFlag = 0x01;
		else m_connAckFlag = 0x00;
		
		m_connReturnCode = returnCode;
	}
	
	/**
	 * Sets the session present flag.
	 * 
	 * @param bFlag - session present flag
	 */
	public void setConnAckFlag(boolean bFlag)
	{
		if(bFlag) m_connAckFlag = 0x01;
		else m_connAckFlag = 0x00;
	}
	
	/**
	 * Returns if the session present flag is set or not.
	 * 
	 * @return true if the session present flag is set; false otherwise.
	 */
	public boolean isConnAckFlag()
	{
		if((m_connAckFlag & 0x01) == 0) return false;
		else return true;
	}
	
	/**
	 * Sets the return code.
	 *  
	 * @param code - the return code.
	 */
	public void setReturnCode(byte code)
	{
		m_connReturnCode = code;
	}
	
	/**
	 * Returns the return code.
	 * 
	 * @return the return code.
	 */
	public byte getReturnCode()
	{
		return m_connReturnCode;
	}
	
	//////////////////////////////////////////////////
	// overridden methods (variable header)
	
	@Override
	protected int getVarHeaderByteNum()
	{
		int nByteNum = 2;	// conn ack flag (1 byte) + return code (1 byte)
		return nByteNum;
	}

	@Override
	protected void marshallVarHeader()
	{
		m_bytes.put(m_connAckFlag);
		m_bytes.put(m_connReturnCode);
	}

	@Override
	protected void unmarshallVarHeader(ByteBuffer buf)
	{
		m_connAckFlag = buf.get();
		m_connReturnCode = buf.get();
	}

	//////////////////////////////////////////////////
	// overridden methods (payload)

	// The CONNACK packet has no payload.
	@Override
	protected int getPayloadByteNum()
	{
		return 0;
	}

	@Override
	protected void marshallPayload(){}

	@Override
	protected void unmarshallPayload(ByteBuffer buf){}
	
	//////////////////////////////////////////////////
	// overridden methods

	@Override
	public String toString()
	{
		StringBuffer strBufVarHeader = new StringBuffer();
		strBufVarHeader.append("CMMqttEventCONNACK {");
		strBufVarHeader.append(super.toString()+", ");
		strBufVarHeader.append("\"connAckFlag\": "+m_connAckFlag+", ");
		strBufVarHeader.append("\"connReturnCode\": "+m_connReturnCode);
		strBufVarHeader.append("}");
		return strBufVarHeader.toString();
	}

}
