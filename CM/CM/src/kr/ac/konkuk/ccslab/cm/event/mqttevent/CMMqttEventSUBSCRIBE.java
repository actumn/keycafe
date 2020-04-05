package kr.ac.konkuk.ccslab.cm.event.mqttevent;

import java.nio.ByteBuffer;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMqttTopicQoS;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * This class represents a CM event that is the variable header and payload of 
 * MQTT SUBSCRIBE packet.
 * @author CCSLab, Konkuk University
 * @see <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718063">
 * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718063</a>
 */
public class CMMqttEventSUBSCRIBE extends CMMqttEventFixedHeader {

	//////////////////////////////////////////////////
	// member variables (variable header)
	int m_nPacketID;	// 2 bytes

	//////////////////////////////////////////////////
	// member variables (payload)
	CMList<CMMqttTopicQoS> m_topicQoSList;
	
	//////////////////////////////////////////////////
	// constructors

	/**
	 * Creates an instance of the CMMqttEventSUBSCRIBE class.
	 */
	public CMMqttEventSUBSCRIBE()
	{
		// initialize CM event ID
		m_nID = CMMqttEvent.SUBSCRIBE;
		// initialize fixed header
		m_packetType = CMMqttEvent.SUBSCRIBE;
		m_flag = 2;
		// initialize variable header
		m_nPacketID = 0;
		// initialize payload
		m_topicQoSList = new CMList<CMMqttTopicQoS>();
	}
	
	public CMMqttEventSUBSCRIBE(ByteBuffer msg)
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
	 * Sets the list of Topic Filter / QoS pairs.
	 * 
	 * @param topicQoSList - the list of Topic Filter / QoS pairs.
	 */
	public void setTopicQoSList(CMList<CMMqttTopicQoS> topicQoSList)
	{
		if(topicQoSList != null)
			m_topicQoSList = topicQoSList;
	}

	/**
	 * Returns the list of Topic Filter / QoS pairs.
	 * 
	 * @return the list of Topic Filter / QoS pairs.
	 */
	public CMList<CMMqttTopicQoS> getTopicQoSList()
	{
		return m_topicQoSList;
	}
	
	/**
	 * Adds the Topic Filter / QoS pair to the list.
	 * 
	 * @param strTopic - the Topic Filter
	 * @param qos - the QoS
	 * @return true if the Topic Filter / QoS pair is successfully added; false otherwise.
	 */
	public boolean addTopicQoS(String strTopic, byte qos)
	{
		CMMqttTopicQoS topicQoS = new CMMqttTopicQoS(strTopic, qos);
		return m_topicQoSList.addElement(topicQoS);
	}
	
	/**
	 * Removes the Topic Filter / QoS pair from the list.
	 * 
	 * @param strTopic - the Topic Filter
	 * @return true if the Topic Filter / QoS pair is successfully removed; false otherwise.
	 */
	public boolean removeTopicQoS(String strTopic)
	{
		CMMqttTopicQoS topicQoS = new CMMqttTopicQoS(strTopic, (byte)0);
		return m_topicQoSList.removeElement(topicQoS);
	}
	
	/**
	 * Removes all Topic Filter / QoS pairs from the list.
	 */
	public void removeAllTopicQoS()
	{
		m_topicQoSList.removeAllElements();
	}

	//////////////////////////////////////////////////
	// overridden methods (payload)

	@Override
	protected int getPayloadByteNum()
	{
		int nPayloadByteNum = 0;
		for(CMMqttTopicQoS topicQoS : m_topicQoSList.getList())
		{
			// string length bytes (2 bytes) + string length + qos byte 
			nPayloadByteNum += CMInfo.STRING_LEN_BYTES_LEN + topicQoS.getTopic().getBytes().length + 1;
		}
		return nPayloadByteNum;
	}

	@Override
	protected void marshallPayload()
	{
		for(CMMqttTopicQoS topicQoS : m_topicQoSList.getList())
		{
			putInt2BytesToByteBuffer(topicQoS.getTopic().getBytes().length);
			m_bytes.put(topicQoS.getTopic().getBytes());
			m_bytes.put(topicQoS.getQoS());
		}
	}

	@Override
	protected void unmarshallPayload(ByteBuffer buf)
	{
		m_topicQoSList.removeAllElements();
		String strTopic = null;
		byte qos = -1;
		
		while(buf.hasRemaining())
		{
			strTopic = getStringFromByteBuffer(buf);
			qos = buf.get();
			m_topicQoSList.addElement(new CMMqttTopicQoS(strTopic, qos));
		}
	}
	
	//////////////////////////////////////////////////
	// overridden methods

	@Override
	public String toString()
	{
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("CMMqttEventSUBSCRIBE {");
		strBuf.append(super.toString()+", ");
		strBuf.append("\"packetID\": "+m_nPacketID+"}");
		strBuf.append("{ \"topicQoS\": [");
		
		for(CMMqttTopicQoS topicQoS : m_topicQoSList.getList())
		{
			strBuf.append("{ \"topic\": \""+topicQoS.getTopic()+"\", ");
			strBuf.append("\"qos\": "+topicQoS.getQoS()+"}");
		}
		
		strBuf.append("]");
		strBuf.append("}");

		return strBuf.toString();
	}
}
