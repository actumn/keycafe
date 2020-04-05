package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * The CMDummyEvent represents a simple event that has a string message.
 * 
 * <p> A CM application can generate an instance of this class to simply 
 * send and receive a string message.
 * 
 * @author CCSLab, Konkuk University
 *
 */
public class CMDummyEvent extends CMEvent{
	
	private String m_strDummyInfo;

	/**
	 * Creates a new instance of the CMDummyEvent class.
	 */
	public CMDummyEvent()
	{
		m_nType = CMInfo.CM_DUMMY_EVENT;
		m_strDummyInfo = "";
	}
	
	public CMDummyEvent(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}
	
	// set/get methods
	
	/**
	 * Sets a string message to this event.
	 * 
	 * @param info - the string message
	 */
	public void setDummyInfo(String info)
	{
		if(info != null)
			m_strDummyInfo = info;
	}
	
	/**
	 * Returns the string message in this event.
	 * 
	 * @return the string message.
	 */
	public String getDummyInfo()
	{
		return m_strDummyInfo;
	}
	
	/////////////////////////////////////////////////////
	
	protected int getByteNum()
	{
		int nByteNum = 0;
		nByteNum = super.getByteNum(); // get header length
		
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strDummyInfo.getBytes().length; // get body length
		
		return nByteNum;
	}
	
	protected void marshallBody()
	{
		putStringToByteBuffer(m_strDummyInfo);
	}
	
	protected void unmarshallBody(ByteBuffer msg)
	{
		m_strDummyInfo = getStringFromByteBuffer(msg);
	}
}
