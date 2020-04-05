package kr.ac.konkuk.ccslab.cm.event;
import java.nio.*;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * The CMUserEvent is a user-defined event that can consist of different types of 
 * arbitrary fields.
 *  
 * @author CCSLab, Konkuk University
 *
 */
public class CMUserEvent extends CMEvent {
	// instead of int ID inherited from CMEvent, use this for this event ID
	private String m_strID;
	private Vector<CMUserEventField> m_eventFieldList;

	/**
	 * Creates an instance of the CMUserEvent.
	 */
	public CMUserEvent()
	{
		m_nType = CMInfo.CM_USER_EVENT;
		m_strID = "defaultID";
		m_eventFieldList = new Vector<CMUserEventField>();
	}
	
	public CMUserEvent(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}

	// set/get methods
	
	/**
	 * Returns a list of all event fields.
	 * 
	 * @return the list of event fields.
	 */
	public Vector<CMUserEventField> getAllEventFields()
	{
		return m_eventFieldList;
	}
	
	/**
	 * Sets a string ID of this event.
	 * 
	 * @param strID - the string ID.
	 */
	public void setStringID(String strID)
	{
		if(strID != null)
			m_strID = strID;
	}
	
	/**
	 * Returns the string ID of this event.
	 * 
	 * @return - the string ID.
	 * <br> The initial value is "defaultID".
	 */
	public String getStringID()
	{
		return m_strID;
	}
	
	/**
	 * Sets and adds a new event field to this event.
	 * 
	 * @param nDataType - the data type of the field.
	 * <p> The following 5 types are possible:
	 * <br> CMInfo.CM_INT : int type
	 * <br> CMInfo.CM_LONG : long type
	 * <br> CMInfo.CM_FLOAT : float type
	 * <br> CMInfo.CM_DOUBLE : double type
	 * <br> CMInfo.CM_STR : String
	 * 
	 * @param strFieldName - the field name.
	 * @param strFieldValue - the field value.
	 */
	public void setEventField(int nDataType, String strFieldName, String strFieldValue)
	{
		CMUserEventField uef = null;
		uef = findEventField(nDataType, strFieldName);
		if(uef == null)
		{
			uef = new CMUserEventField();
			uef.nDataType = nDataType;
			uef.strFieldName = strFieldName;
			uef.strFieldValue = strFieldValue;
			m_eventFieldList.addElement(uef);
			return;
		}
		uef.strFieldValue = strFieldValue;

		return;
	}
	
	/**
	 * Sets and adds bytes data as a new field to this event.
	 * 
	 * @param strFieldName - the field name.
	 * @param nByteNum - the number of bytes.
	 * @param bytes - the byte array.
	 */
	public void setEventBytesField(String strFieldName, int nByteNum, byte[] bytes)
	{
		CMUserEventField uef = null;
		uef = findEventField(CMInfo.CM_BYTES, strFieldName);
		if(uef == null)
		{
			uef = new CMUserEventField();
			uef.nDataType = CMInfo.CM_BYTES;
			uef.strFieldName = strFieldName;
			uef.nValueByteNum = nByteNum;
			uef.valueBytes = new byte[nByteNum];
			uef.valueBytes = Arrays.copyOf(bytes, nByteNum);
			m_eventFieldList.addElement(uef);
			return;
		}
		uef.nValueByteNum = nByteNum;
		uef.valueBytes = new byte[nByteNum];
		uef.valueBytes = Arrays.copyOf(bytes, nByteNum);
		return;
	}

	/**
	 * Returns the field value in this event.
	 * 
	 * @param nDataType - the field type.
	 * <p> The field type is one of followings:
	 * <br> CMInfo.CM_INT : int type
	 * <br> CMInfo.CM_LONG : long type
	 * <br> CMInfo.CM_FLOAT : float type
	 * <br> CMInfo.CM_DOUBLE : double type
	 * <br> CMInfo.CM_STR : String
	 * 
	 * @param strFieldName - the field name.
	 * @return - the field value if the field is found; null otherwise.
	 */
	public String getEventField(int nDataType, String strFieldName)
	{
		CMUserEventField uef = null;
		uef = findEventField(nDataType, strFieldName);
		if(uef == null) return null;
		
		return uef.strFieldValue;
	}
	
	/**
	 * Returns the byte array of the given field.
	 * 
	 * @param strFieldName - the field name
	 * @return - the byte array if the field is found; null otherwise.
	 */
	public byte[] getEventBytesField(String strFieldName)
	{
		CMUserEventField uef = null;
		uef = findEventField(CMInfo.CM_BYTES, strFieldName);
		if(uef == null) return null;
		
		return uef.valueBytes;
	}
	
	/**
	 * Searches for the event field.
	 * 
	 * @param nDataType - the field type.
	 * <p> The field type is one of followings:
	 * <br> CMInfo.CM_INT : int type
	 * <br> CMInfo.CM_LONG : long type
	 * <br> CMInfo.CM_FLOAT : float type
	 * <br> CMInfo.CM_DOUBLE : double type
	 * <br> CMInfo.CM_STR : String
	 * 
	 * @param strFieldName - the field name.
	 * @return the event field if found; null otherwise.
	 */
	public CMUserEventField findEventField(int nDataType, String strFieldName)
	{
		CMUserEventField uef = null;
		boolean bFound = false;
		Iterator<CMUserEventField> iterEventFieldList = m_eventFieldList.iterator();
		
		if(strFieldName == null) return null;
		
		while(iterEventFieldList.hasNext() && !bFound)
		{
			uef = iterEventFieldList.next();
			if( nDataType == uef.nDataType && strFieldName.equals(uef.strFieldName) )
				bFound = true;
		}
		
		if(!bFound) return null;
		
		return uef;
	}
	
	/**
	 * Removes an event field from this event.
	 * 
	 * @param nDataType - the field type.
	 * <p> The field type is one of followings:
	 * <br> CMInfo.CM_INT : int type
	 * <br> CMInfo.CM_LONG : long type
	 * <br> CMInfo.CM_FLOAT : float type
	 * <br> CMInfo.CM_DOUBLE : double type
	 * <br> CMInfo.CM_STR : String
 
	 * @param strFieldName - field name.
	 */
	public void removeEventField(int nDataType, String strFieldName)
	{
		CMUserEventField uef = null;
		boolean bFound = false;
		Iterator<CMUserEventField> iterEventFieldList = m_eventFieldList.iterator();
		
		if(strFieldName == null) return;
		
		while(iterEventFieldList.hasNext() && !bFound)
		{
			uef = iterEventFieldList.next();
			if( nDataType == uef.nDataType && strFieldName.equals(uef.strFieldName) )
			{
				if(nDataType == CMInfo.CM_BYTES)
					uef.valueBytes = null;
				iterEventFieldList.remove();
				bFound = true;
			}
		}
		
		return;
	}
	
	/**
	 * Removes all fields of this event.
	 */
	public void removeAllEventFields()
	{
		CMUserEventField uef = null;
		Iterator<CMUserEventField> iterEventFieldList = m_eventFieldList.iterator();
		
		while(iterEventFieldList.hasNext())
		{
			uef = iterEventFieldList.next();
			if( uef.nDataType == CMInfo.CM_BYTES )
			{
				uef.valueBytes = null;
			}
		}

		m_eventFieldList.removeAllElements();
		return;
	}
	
	///////////////////////////////////////////////////////
	
	protected int getByteNum()
	{
		int nByteNum = 0;
		CMUserEventField uef = null;
		Iterator<CMUserEventField> iterEventFieldList = m_eventFieldList.iterator();
		
		// header byte num
		nByteNum = super.getByteNum();
		
		// body byte num
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strID.getBytes().length;
		while(iterEventFieldList.hasNext())
		{
			uef = iterEventFieldList.next();
			nByteNum += Integer.BYTES; // nDataType
			nByteNum += 2*CMInfo.STRING_LEN_BYTES_LEN; // field name len, field value len
			if(uef.nDataType == CMInfo.CM_BYTES)
				nByteNum += uef.strFieldName.getBytes().length + uef.nValueByteNum;
			else
				nByteNum += uef.strFieldName.getBytes().length + uef.strFieldValue.getBytes().length;
		}
		
		return nByteNum;
	}
	
	protected void marshallBody()
	{
		CMUserEventField uef = null;
		Iterator<CMUserEventField> iterEventFieldList = m_eventFieldList.iterator();
		
		putStringToByteBuffer(m_strID);
		
		while(iterEventFieldList.hasNext())
		{
			uef = iterEventFieldList.next();
			
			m_bytes.putInt(uef.nDataType);
			putStringToByteBuffer(uef.strFieldName);
			if(uef.nDataType == CMInfo.CM_BYTES)
			{
				m_bytes.putInt(uef.nValueByteNum);
				m_bytes.put(uef.valueBytes);
			}
			else
			{
				putStringToByteBuffer(uef.strFieldValue);
			}
		}
		
		return;
	}
	
	protected void unmarshallBody(ByteBuffer msg)
	{
		int nDataType;
		String strFieldName;
		String strFieldValue;
		int nValueByteNum;
		byte[] valueBytes;
		
		removeAllEventFields();
		
		m_strID = getStringFromByteBuffer(msg);
		
		while( msg.remaining() > 0 )
		{
			nDataType = msg.getInt();
			strFieldName = getStringFromByteBuffer(msg);
			
			if(nDataType == CMInfo.CM_BYTES)
			{
				nValueByteNum = msg.getInt();
				valueBytes = new byte[nValueByteNum];
				msg.get(valueBytes);
				setEventBytesField(strFieldName, nValueByteNum, valueBytes);
			}
			else
			{
				strFieldValue = getStringFromByteBuffer(msg);
				setEventField(nDataType, strFieldName, strFieldValue);
			}
		}
		
		return;
	}
}
