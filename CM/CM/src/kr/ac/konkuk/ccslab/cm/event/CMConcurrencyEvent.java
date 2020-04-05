package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMConcurrencyEvent extends CMEvent {
	public final static int OWNERSHIP_REQUEST = 1;
	public final static int OWNERSHIP = 2;
	public final static int OWNERSHIP_DENY = 3;
	public final static int OWNERSHIP_RELEASE = 4;
	
	
	private int m_nTargetObjectID;
	private String m_strUserName;
	
	public CMConcurrencyEvent()
	{
		m_nType = CMInfo.CM_CONCURRENCY_EVENT;
		m_nTargetObjectID = -1;
		m_strUserName = "";
	}
	
	public CMConcurrencyEvent(ByteBuffer msg)
	{
		this();		
		unmarshall(msg);
	}

	public void setTargetObjectID(int id)
	{
		m_nTargetObjectID = id;
	}
	
	public int getTargetObjectID()
	{
		return m_nTargetObjectID;
	}
	
	public void setUserName(String uName)
	{
		if(uName != null)
			m_strUserName = uName;
	}
	
	public String getUserName()
	{
		return m_strUserName;
	}

	///////////////////////////////////////////////////
	
	protected int getByteNum()
	{
		int nByteNum = 0;
		nByteNum = super.getByteNum();
		nByteNum += Integer.BYTES;	// m_nTargetObjectID
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;
		return nByteNum;
	}
	
	protected void marshallBody()
	{
		putStringToByteBuffer(m_strUserName);
		m_bytes.putInt(m_nTargetObjectID);
	}
	
	protected void unmarshallBody(ByteBuffer msg)
	{
		m_strUserName = getStringFromByteBuffer(msg);
		m_nTargetObjectID = msg.getInt();
	}

}
