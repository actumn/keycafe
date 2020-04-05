package kr.ac.konkuk.ccslab.cm.event.mqttevent;

import java.nio.ByteBuffer;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMqttTopicQoS;

/**
 * This class represents a CM event that is the variable header and payload of 
 * MQTT SUBACK packet.
 * @author CCSLab, Konkuk University
 * @see <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718068">
 * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718068</a>
 */
public class CMMqttEventSUBACK extends CMMqttEventFixedHeader {

	//////////////////////////////////////////////////
	// member variables (variable header)
	int m_nPacketID;	// 2 bytes

	//////////////////////////////////////////////////
	// member variables (payload)
	
	// list of return codes (byte: X000 00XX)
	// 0x00 : success - maximum qos 0
	// 0x01 : success - maximum qos 1
	// 0x02 : success - maximum qos 2
	// 0x80 : failure
	CMList<Byte> m_returnCodeList;
	
	//////////////////////////////////////////////////
	// constructors

	/**
	 * Creates an instance of the CMMqttEventSUBACK class.
	 */
	public CMMqttEventSUBACK()
	{
		// initialize CM event ID
		m_nID = CMMqttEvent.SUBACK;
		// initialize fixed header
		m_packetType = CMMqttEvent.SUBACK;
		m_flag = 0;
		// initialize variable header
		m_nPacketID = 0;
		// initialize payload
		m_returnCodeList = new CMList<Byte>();
	}
	
	public CMMqttEventSUBACK(ByteBuffer msg)
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
	// setter/getter (payload)

	/**
	 * Sets the list of return codes.
	 * 
	 * @param returnCodeList - the list of return codes
	 */
	public void setReturnCodeList(CMList<Byte> returnCodeList)
	{
		if(returnCodeList != null)
			m_returnCodeList = returnCodeList;
	}
	
	/**
	 * Returns the list of return codes.
	 * 
	 * @return the list of return codes.
	 */
	public CMList<Byte> getReturnCodeList()
	{
		return m_returnCodeList;
	}
	
	/**
	 * Adds the return code to the list.
	 * 
	 * @param returnCode - the return code
	 * @return true if the return code is successfully added; false otherwise.
	 */
	public boolean addReturnCode(byte returnCode)
	{
		return m_returnCodeList.addElement(returnCode);
	}
	
	/**
	 * Removes the return code from the list.
	 * 
	 * @param returnCode - the return code
	 * @return true if the return code is successfully removed; false otherwise.
	 */
	public boolean removeReturnCode(byte returnCode)
	{
		return m_returnCodeList.removeElement(returnCode);
	}
	
	/**
	 * Removes all return codes from the list.
	 */
	public void removeAllReturnCode()
	{
		m_returnCodeList.removeAllElements();
	}

	//////////////////////////////////////////////////
	// overridden methods (payload)
	
	@Override
	protected int getPayloadByteNum()
	{
		return m_returnCodeList.getSize();
	}

	@Override
	protected void marshallPayload()
	{
		for(Byte returnCode : m_returnCodeList.getList())
		{
			m_bytes.put(returnCode);
		}
	}

	@Override
	protected void unmarshallPayload(ByteBuffer buf)
	{
		m_returnCodeList.removeAllElements();
		Byte returnCode = -1;
		while(buf.hasRemaining())
		{
			returnCode = buf.get();
			m_returnCodeList.addElement(returnCode);
		}
	}
	
	//////////////////////////////////////////////////
	// overridden methods

	@Override
	public String toString()
	{
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("CMMqttEventSUBACK {");
		strBuf.append(super.toString()+", ");
		strBuf.append("\"packetID\": "+m_nPacketID+"}");
		strBuf.append("{ \"returnCode\": [");
		
		int nCount = 0;
		for(Byte returnCode : m_returnCodeList.getList())
		{
			nCount++;
			strBuf.append(returnCode);
			if(nCount < m_returnCodeList.getSize())
				strBuf.append(", ");
		}
		
		strBuf.append("]");
		strBuf.append("}");

		return strBuf.toString();
	}

}
