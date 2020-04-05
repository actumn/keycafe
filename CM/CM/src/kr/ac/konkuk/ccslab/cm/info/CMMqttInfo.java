package kr.ac.konkuk.ccslab.cm.info;

import java.util.Hashtable;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMqttSession;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;

/**
 * The CMMqttInfo class stores information required for the MQTT protocol.
 * @author CCSLab, Konkuk University
 *
 */
public class CMMqttInfo {

	// mqtt session information (4 client)
	CMMqttSession m_mqttSession;
	// mqtt session information (4 server)
	Hashtable<String, CMMqttSession> m_mqttSessionHashtable;
	// mqtt retain event (4 server) (topic, PUBLISH event) pair
	Hashtable<String, CMMqttEventPUBLISH> m_mqttRetainHashtable;
	
	public CMMqttInfo()
	{
		m_mqttSession = null;
		m_mqttSessionHashtable = new Hashtable<String, CMMqttSession>();
		m_mqttRetainHashtable = new Hashtable<String, CMMqttEventPUBLISH>();
	}
	
	// setter/getter
	public synchronized void setMqttSession(CMMqttSession session)
	{
		m_mqttSession = session;
	}
	
	public synchronized CMMqttSession getMqttSession()
	{
		return m_mqttSession;
	}
	
	public synchronized void setMqttSessionHashtable(Hashtable<String, CMMqttSession> sessionHashtable)
	{
		m_mqttSessionHashtable = sessionHashtable;
	}
	
	public synchronized Hashtable<String, CMMqttSession> getMqttSessionHashtable()
	{
		return m_mqttSessionHashtable;
	}
	
	public synchronized void setMqttRetainHashtable(Hashtable<String, CMMqttEventPUBLISH> retainHashtable)
	{
		m_mqttRetainHashtable = retainHashtable;
	}
	
	public synchronized Hashtable<String, CMMqttEventPUBLISH> getMqttRetainHashtable()
	{
		return m_mqttRetainHashtable;
	}
}
