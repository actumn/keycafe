package kr.ac.konkuk.ccslab.cm.entity;

/**
 * The CMMqttWill class represents information about MQTT will message.
 * @author CCSLab, Konkuk University
 *
 */
public class CMMqttWill {

	private String m_strWillMessage;
	private String m_strWillTopic;
	private byte m_willQoS;
	private boolean m_bWillRetain;
	
	public CMMqttWill()
	{
		m_strWillMessage = "";
		m_strWillTopic = "";
		m_willQoS = 0;
		m_bWillRetain = false;
	}
	
	public CMMqttWill(String strMsg, String strTopic, byte qos, boolean bRetain)
	{
		m_strWillMessage = strMsg;
		m_strWillTopic = strTopic;
		m_willQoS = qos;
		m_bWillRetain = bRetain;
	}
	
	// setter/getter
	public synchronized void setWillMessage(String strMsg)
	{
		m_strWillMessage = strMsg;
	}
	
	public synchronized String getWillMessage()
	{
		return m_strWillMessage;
	}
	
	public synchronized void setWillTopic(String strTopic)
	{
		m_strWillTopic = strTopic;
	}
	
	public synchronized String getWillTopic()
	{
		return m_strWillTopic;
	}
	
	public synchronized void setWillQoS(byte qos)
	{
		m_willQoS = qos;
	}
	
	public synchronized byte getWillQoS()
	{
		return m_willQoS;
	}
	
	public synchronized void setWillRetain(boolean bRetain)
	{
		m_bWillRetain = bRetain;
	}
	
	public synchronized boolean isWillRetain()
	{
		return m_bWillRetain;
	}
	
	// overridden methods
	@Override
	public String toString()
	{
		String str = "CMMqttWill{ \"willMessage\": "+m_strWillMessage+", \"willTopic\": "+m_strWillTopic
				+", \"willQoS\": "+m_willQoS+", \"willRetain\": "+m_bWillRetain+"}";
		return str;
	}
}
