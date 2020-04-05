package kr.ac.konkuk.ccslab.cm.event.mqttevent;

import java.nio.ByteBuffer;

/**
 * This class represents a CM event that is the variable header and payload of 
 * MQTT UNSUBACK packet.
 * @author CCSLab, Konkuk University
 * @see <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718077">
 * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718077</a>
 */
public class CMMqttEventUNSUBACK extends CMMqttEventFixedHeader {

	//////////////////////////////////////////////////
	// member variables (variable header)
	int m_nPacketID;	// 2 bytes


	//////////////////////////////////////////////////
	// constructors

	/**
	 * Creates an instance of the CMMqttEventUNSUBACK class.
	 */
	public CMMqttEventUNSUBACK()
	{
		// initialize CM event ID
		m_nID = CMMqttEvent.UNSUBACK;
		// initialize fixed header
		m_packetType = CMMqttEvent.UNSUBACK;
		m_flag = 0;
		// initialize variable header
		m_nPacketID = 0;
	}
	
	public CMMqttEventUNSUBACK(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}

	//////////////////////////////////////////////////
	// setter/getter (variable header)

	/**
	 * sets MQTT Packet ID.
	 * @param nID - Packet ID.
	 */
	public void setPacketID(int nID)
	{
		m_nPacketID = nID;
	}
	
	/**
	 * gets MQTT Packet ID.
	 * @return Packet ID.
	 */
	@Override
	public int getPacketID()
	{
		return m_nPacketID;
	}
	
	//////////////////////////////////////////////////
	// overridden methods (variable header)
	
	@Override
	protected int getVarHeaderByteNum()
	{
		return 2;	// packet ID
	}

	@Override
	protected void marshallVarHeader()
	{
		putInt2BytesToByteBuffer(m_nPacketID);
	}

	@Override
	protected void unmarshallVarHeader(ByteBuffer buf)
	{
		m_nPacketID = getInt2BytesFromByteBuffer(buf);
	}

	//////////////////////////////////////////////////
	// overridden methods (payload)
	
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
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("CMMqttEventUNSUBACK {");
		strBuf.append(super.toString()+", ");
		strBuf.append("\"packetID\": "+m_nPacketID+"}");

		return strBuf.toString();
	}

}
