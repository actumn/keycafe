package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMConsistencyEvent extends CMEvent {
	public static final int TRANSLATION = 1;
	public static final int ROTATION = 2;
	public static final int TRANS_ROTATION = 3;
	
	private float[] m_fTranslation = new float[3];
	private float[] m_fRotation = new float[4];
	private int m_nEventSource;
	
	public CMConsistencyEvent()
	{
		m_nType = CMInfo.CM_CONSISTENCY_EVENT;
		m_fTranslation[0] = 0.0f;
		m_fTranslation[1] = 0.0f;
		m_fTranslation[2] = 0.0f;
		m_fRotation[0] = 0.0f;
		m_fRotation[1] = 0.0f;
		m_fRotation[2] = 0.0f;
		m_fRotation[3] = 0.0f;
		m_nEventSource = -1;
	}
	
	public CMConsistencyEvent(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}
	
	public void setEventSource(int src)
	{
		m_nEventSource = src;
	}
	
	public int getEventSource()
	{
		return m_nEventSource;
	}
	
	public void setTranslation(float[] trans)
	{
		m_fTranslation[0] = trans[0];
		m_fTranslation[1] = trans[1];
		m_fTranslation[2] = trans[2];
	}
	
	public float[] getTranslation()
	{
		return m_fTranslation;
	}
	
	public void setRotation(float[] rotate)
	{
		m_fRotation[0] = rotate[0];
		m_fRotation[1] = rotate[1];
		m_fRotation[2] = rotate[2];
		m_fRotation[3] = rotate[3];
	}
	
	public float[] getRotation()
	{
		return m_fRotation;
	}
	
	///////////////////////////////////////////
	
	protected int getByteNum()
	{
		int nByteNum = 0;
		nByteNum = super.getByteNum();
		
		switch(m_nID)
		{
		case CMConsistencyEvent.TRANSLATION:
			nByteNum += Integer.BYTES + 3*Float.BYTES;
			break;
		case CMConsistencyEvent.ROTATION:
			nByteNum += Integer.BYTES + 4*Float.BYTES;
			break;
		case CMConsistencyEvent.TRANS_ROTATION:
			nByteNum += Integer.BYTES + 7*Float.BYTES;
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
		case CMConsistencyEvent.TRANSLATION:
			m_bytes.putInt(m_nEventSource);
			m_bytes.putFloat(m_fTranslation[0]);
			m_bytes.putFloat(m_fTranslation[1]);
			m_bytes.putFloat(m_fTranslation[2]);
			break;
		case CMConsistencyEvent.ROTATION:
			m_bytes.putInt(m_nEventSource);
			m_bytes.putFloat(m_fRotation[0]);
			m_bytes.putFloat(m_fRotation[1]);
			m_bytes.putFloat(m_fRotation[2]);
			m_bytes.putFloat(m_fRotation[3]);
			break;
		case CMConsistencyEvent.TRANS_ROTATION:
			m_bytes.putInt(m_nEventSource);
			m_bytes.putFloat(m_fTranslation[0]);
			m_bytes.putFloat(m_fTranslation[1]);
			m_bytes.putFloat(m_fTranslation[2]);
			m_bytes.putFloat(m_fRotation[0]);
			m_bytes.putFloat(m_fRotation[1]);
			m_bytes.putFloat(m_fRotation[2]);
			m_bytes.putFloat(m_fRotation[3]);
			break;
		default:
			System.out.println("CMConsistencyEvent.marshallBody(), unknown event id("+m_nID+").");
			m_bytes = null;
			break;
		}
	}
	
	protected void unmarshallBody(ByteBuffer msg)
	{
		switch(m_nID)
		{
		case CMConsistencyEvent.TRANSLATION:
			m_nEventSource = msg.getInt();
			m_fTranslation[0] = msg.getFloat();
			m_fTranslation[1] = msg.getFloat();
			m_fTranslation[2] = msg.getFloat();
			break;
		case CMConsistencyEvent.ROTATION:
			m_nEventSource = msg.getInt();
			m_fRotation[0] = msg.getFloat();
			m_fRotation[1] = msg.getFloat();
			m_fRotation[2] = msg.getFloat();
			m_fRotation[3] = msg.getFloat();
			break;
		case CMConsistencyEvent.TRANS_ROTATION:
			m_nEventSource = msg.getInt();
			m_fTranslation[0] = msg.getFloat();
			m_fTranslation[1] = msg.getFloat();
			m_fTranslation[2] = msg.getFloat();
			m_fRotation[0] = msg.getFloat();
			m_fRotation[1] = msg.getFloat();
			m_fRotation[2] = msg.getFloat();
			m_fRotation[3] = msg.getFloat();
			break;
		default:
			System.out.println("CMConsistencyEvent.unmarshallBody(), unknown event id("+m_nID+").");
			break;
		}
	}
}
