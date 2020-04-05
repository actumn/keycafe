package kr.ac.konkuk.ccslab.cm.event.mqttevent;

import java.nio.ByteBuffer;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * This class represents a CM event that belongs to the variable header and payload of 
 * MQTT CONNECT packet.
 * @author CCSLab, Konkuk University
 * @see <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718028">
 * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718028</a>
 */
public class CMMqttEventCONNECT extends CMMqttEventFixedHeader {

	//////////////////////////////////////////////////
	// member variables (variable header)
	private String m_strProtocolName; // encoding with 2 bytes length and the string length
	private byte m_protocolLevel;	// 1 byte
	private byte m_connectFlag;		// 1 byte
	private int m_nKeepAlive;		// 2 bytes
	
	//////////////////////////////////////////////////
	// member variables (payload)
	private String m_strClientID;	// encoding with 2 bytes length and the string length
	private String m_strWillTopic;	// encoding with 2 bytes length and the string length
	private String m_strWillMessage;// encoding with 2 bytes length and the string length
	private String m_strUserName;	// encoding with 2 bytes length and the string length
	private String m_strPassword;	// encoding with 2 bytes length and the string length
	
	//////////////////////////////////////////////////
	// constructors

	/**
	 * Creates an instance of the CMMqttEventCONNECT class.
	 */
	public CMMqttEventCONNECT() {
		// initialize CM event ID
		m_nID = CMMqttEvent.CONNECT;
		// initialize fixed header
		m_packetType = CMMqttEvent.CONNECT;
		m_flag = 0;
		// m_nRemainingLength is determined at getFixedHeaderByteNum()
		
		// initialize variable header
		m_strProtocolName = "MQTT";
		m_protocolLevel = 4;	// MQTT 3.1.1
		m_connectFlag = 0;	// flag bits (user, passwd, will retain, will flag. clean session) and will qos
		m_nKeepAlive = 0;
		
		// initialize payload
		m_strClientID = "";
		m_strWillTopic = "";
		m_strWillMessage = "";
		m_strUserName = "";
		m_strPassword = "";
	}
	
	public CMMqttEventCONNECT(ByteBuffer msg) {
		this();
		unmarshall(msg);
	}
	
	//////////////////////////////////////////////////
	// setter/getter (variable header)
	
	/**
	 * Sets the variable header of MQTT CONNECT packet.
	 * 
	 * @param connectFlag - the connect flags
	 * @param nKeepAlive - the keep-alive time
	 */
	public void setVarHeader(byte connectFlag, int nKeepAlive)
	{
		m_connectFlag = connectFlag;
		m_nKeepAlive = nKeepAlive;
	}
	
	/**
	 * Sets the protocol name.
	 * 
	 * @param strName - the protocol name
	 */
	public void setProtocolName(String strName)
	{
		if(strName != null)
			m_strProtocolName = strName;
	}
	
	/**
	 * Returns the protocol name.
	 * 
	 * @return the protocol name.
	 */
	public String getProtocolName()
	{
		return m_strProtocolName;
	}
	
	/**
	 * Sets the protocol level.
	 * 
	 * @param level - the protocol level.
	 */
	public void setProtocolLevel(byte level)
	{
		m_protocolLevel = level;
	}
	
	/**
	 * Returns the protocol level.
	 * 
	 * @return the protocol level.
	 */
	public byte getProtocolLevel()
	{
		return m_protocolLevel;
	}
	
	/**
	 * Sets the connect flags.
	 * 
	 * @param flag - the connect flags
	 */
	public void setConnFlag(byte flag)
	{
		m_connectFlag = flag;
	}
	
	/**
	 * Returns the connect flags.
	 * 
	 * @return the connect flags.
	 */
	public byte getConnFlag()
	{
		return m_connectFlag;
	}
	
	/**
	 * Sets the user name flag.
	 * 
	 * @param bUser - the user name flag
	 */
	public void setUserNameFlag(boolean bUser)
	{
		// print current connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.setUserNameFlag(): "+bUser);
			System.out.println("connect flag (before): "+getBinaryStringOfByte(m_connectFlag));
		}
		
		// set the user name bit
		if(bUser)
			m_connectFlag |= 0x80;	// 0b1000 0000
		else
			m_connectFlag &= 0x7f;	// 0b0111 1111
		
		// print modified connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("connect flag (after): "+getBinaryStringOfByte(m_connectFlag));
		}
	}
	
	/**
	 * Returns the user name flag.
	 * 
	 * @return true if the user name flag is set; false otherwise.
	 */
	public boolean isUserNameFlag()
	{
		if((m_connectFlag & 0x80) == 0) return false;
		else return true;
	}
	
	/**
	 * Sets the password flag.
	 * 
	 * @param bPassword the password flag
	 */
	public void setPasswordFlag(boolean bPassword)
	{
		// print current connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.setPasswordFlag(): "+bPassword);
			System.out.println("connect flag (before): "+getBinaryStringOfByte(m_connectFlag));
		}
		
		// set the password bit
		if(bPassword)
			m_connectFlag |= 0x40;	// 0b0100 0000
		else
			m_connectFlag &= 0xbf;	// 0b1011 1111
		
		// print modified connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("connect flag (after): "+getBinaryStringOfByte(m_connectFlag));
		}
	}
	
	/**
	 * Returns the password flag.
	 * 
	 * @return true if the password flag is set; false otherwise.
	 */
	public boolean isPasswordFlag()
	{
		if((m_connectFlag & 0x40) == 0) return false;
		else return true;
	}
	
	/**
	 * Sets the will retain flag.
	 * 
	 * @param bWillRetain - the will retain flag
	 */
	public void setWillRetainFlag(boolean bWillRetain)
	{
		// print current connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.setWillRetainFlag(): "+bWillRetain);
			System.out.println("connect flag (before): "+getBinaryStringOfByte(m_connectFlag));
		}
		
		// set the will-retain flag
		if(bWillRetain)
			m_connectFlag |= 0x20;	// 0b0010 0000
		else
			m_connectFlag &= 0xdf;	// 0b1101 1111
		
		// print modified connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("connect flag (after): "+getBinaryStringOfByte(m_connectFlag));
		}
	}
	
	/**
	 * Returns the will retain flag.
	 * 
	 * @return true if the will retain flag is set; false otherwise.
	 */
	public boolean isWillRetainFlag()
	{
		if((m_connectFlag & 0x20) == 0) return false;
		else return true;
	}
	
	/**
	 * Sets the will QoS.
	 * 
	 * @param qos - the will QoS
	 */
	public void setWillQoS(byte qos)
	{
		// print current connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.setWillQoS(): "+qos);
			System.out.println("connect flag (before): "+getBinaryStringOfByte(m_connectFlag));
		}
		
		// set will-QoS flag
		m_connectFlag &= 0xe7;	// 0b1110 0111
		m_connectFlag |= (qos << 3);
		
		// print modified connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("connect flag (after): "+getBinaryStringOfByte(m_connectFlag));
		}
	}
	
	/**
	 * Returns the will QoS.
	 * 
	 * @return the will QoS.
	 */
	public byte getWillQoS()
	{
		// print current connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.getWillQoS(): ");
			System.out.println("connect flag: "+getBinaryStringOfByte(m_connectFlag));
		}
		// get will qos
		byte willQoS = 0;
		willQoS = (byte)((m_connectFlag & 0x18) >> 3);
		
		// print will qos
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("will QoS: "+willQoS);
		}
		
		return willQoS;
	}
	
	/**
	 * Sets the will flag.
	 * 
	 * @param bWill - the will flag
	 */
	public void setWillFlag(boolean bWill)
	{
		// print current connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.setWillFlag(): "+bWill);
			System.out.println("connect flag (before): "+getBinaryStringOfByte(m_connectFlag));
		}
		
		// set will flag
		if(bWill)
			m_connectFlag |= 0x04;	// 0b0000 0100
		else
			m_connectFlag &= 0xfb;	// 0b1111 1011
		
		// print modified connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("connect flag (after): "+getBinaryStringOfByte(m_connectFlag));
		}
	}
	
	/**
	 * Returns the will flag.
	 * 
	 * @return the will flag.
	 */
	public boolean isWillFlag()
	{
		if((m_connectFlag & 0x04) == 0) return false;
		else return true;
	}
	
	/**
	 * Sets the clean session flag.
	 * 
	 * @param bCleanSession - the clean session flag
	 */
	public void setCleanSessionFlag(boolean bCleanSession)
	{
		// print current connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.setCleanSessionFlag(): "+bCleanSession);
			System.out.println("connect flag (before): "+getBinaryStringOfByte(m_connectFlag));
		}
		
		// set clean-session flag
		if(bCleanSession)
			m_connectFlag |= 0x02;	// 0b0000 0010
		else
			m_connectFlag &= 0xfd;	// 0b1111 1101
				
		// print modified connect flag
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("connect flag (after): "+getBinaryStringOfByte(m_connectFlag));
		}
	}
	
	/**
	 * Returns the clean session flag.
	 * 
	 * @return the clean session flag.
	 */
	public boolean isCleanSessionFlag()
	{
		if((m_connectFlag & 0x02) == 0) return false;
		else return true;
	}
	
	/**
	 * Sets the keep-alive time.
	 * 
	 * @param seconds - the keep-alive time
	 */
	public void setKeepAlive(int seconds)
	{
		m_nKeepAlive = seconds;
	}
	
	/**
	 * Returns the keep-alive time.
	 * 
	 * @return the keep-alive time.
	 */
	public int getKeepAlive()
	{
		return m_nKeepAlive;
	}

	/*
	private String getConnectFlagString()
	{
		String strConnFlag = String.format("%8s", Integer.toBinaryString(m_connectFlag & 0xff))
				.replace(' ', '0');
		return strConnFlag;
	}
	*/
	//////////////////////////////////////////////////
	// overridden methods (variable header)

	@Override
	protected int getVarHeaderByteNum() {
		// TODO Auto-generated method stub
		int nByteNum = 0;
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strProtocolName.getBytes().length;	// protocol name
		nByteNum += 1+1+2;	// protocol level, flag, keep alive

		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.getVarHeaderByteNum: "+nByteNum);
		}

		return nByteNum;
	}

	@Override
	protected void marshallVarHeader() {
		// TODO Auto-generated method stub
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.marshallVarHeader(): ");
			System.out.println("protocol name: "+m_strProtocolName+", protocol level: "
					+m_protocolLevel+", connect flags: "+getBinaryStringOfByte(m_connectFlag)
					+", keep alive: "+m_nKeepAlive);
		}
		putStringToByteBuffer(m_strProtocolName);	// protocol name
		m_bytes.put(m_protocolLevel);	// protocol level
		m_bytes.put(m_connectFlag);		// connect flags
		putInt2BytesToByteBuffer(m_nKeepAlive);		// keep alive
	}

	@Override
	protected void unmarshallVarHeader(ByteBuffer buf) {
		// TODO Auto-generated method stub
		m_strProtocolName = getStringFromByteBuffer(buf);
		m_protocolLevel = buf.get();
		m_connectFlag = buf.get();
		m_nKeepAlive = getInt2BytesFromByteBuffer(buf);
		
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.unmarshallVarHeader(): ");
			System.out.println("protocol name: "+m_strProtocolName+", protocol level: "
					+m_protocolLevel+", connect flags: "+getBinaryStringOfByte(m_connectFlag)
					+", keep alive: "+m_nKeepAlive);
		}

	}

	//////////////////////////////////////////////////
	// setter/getter (payload)
	
	/**
	 * Sets the payload of the MQTT CONNECT packet.
	 * 
	 * @param strClientID - the client ID
	 * @param strWillTopic - the will topic
	 * @param strWillMessage - the will message
	 * @param strUserName - the user name
	 * @param strPassword - the password
	 */
	public void setPayload(String strClientID, String strWillTopic, String strWillMessage, 
			String strUserName, String strPassword)
	{
		if(strClientID != null)
			m_strClientID = strClientID;
		if(strWillTopic != null)
			m_strWillTopic = strWillTopic;
		if(strWillMessage != null)
			m_strWillMessage = strWillMessage;
		if(strUserName != null)
			m_strUserName = strUserName;
		if(strPassword != null)
			m_strPassword = strPassword;
	}
	
	/**
	 * Sets the client ID.
	 * 
	 * @param strClientID - the client ID
	 */
	public void setClientID(String strClientID)
	{
		if(strClientID != null)
			m_strClientID = strClientID;
	}
	
	/**
	 * Returns the client ID.
	 * 
	 * @return the client ID.
	 */
	public String getClientID()
	{
		return m_strClientID;
	}
	
	/**
	 * Sets the will topic.
	 * 
	 * @param strWillTopic - the will topic
	 */
	public void setWillTopic(String strWillTopic)
	{
		if(strWillTopic != null)
			m_strWillTopic = strWillTopic;
	}
	
	/**
	 * Returns the will topic.
	 * 
	 * @return the will topic.
	 */
	public String getWillTopic()
	{
		return m_strWillTopic;
	}
	
	/**
	 * Sets the will message.
	 * 
	 * @param strWillMessage - the will message
	 */
	public void setWillMessage(String strWillMessage)
	{
		if(strWillMessage != null)
			m_strWillMessage = strWillMessage;
	}
	
	/**
	 * Returns the will message.
	 * 
	 * @return the will message.
	 */
	public String getWillMessage()
	{
		return m_strWillMessage;
	}
	
	/**
	 * Sets the user name.
	 * 
	 * @param strUserName - the user name
	 */
	public void setUserName(String strUserName)
	{
		if(strUserName != null)
			m_strUserName = strUserName;
	}
	
	/**
	 * Returns the user name.
	 * 
	 * @return the user name.
	 */
	public String getUserName()
	{
		return m_strUserName;
	}
	
	/**
	 * Sets the password.
	 * 
	 * @param strPassword - the password
	 */
	public void setPassword(String strPassword)
	{
		if(strPassword != null)
			m_strPassword = strPassword;
	}
	
	/**
	 * Returns the password.
	 * 
	 * @return the password.
	 */
	public String getPassword()
	{
		return m_strPassword;
	}
	
	//////////////////////////////////////////////////
	// overridden methods (payload)

	@Override
	protected int getPayloadByteNum() {
		// TODO Auto-generated method stub
		int nByteNum = 0;
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strClientID.getBytes().length;	// client id
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strWillTopic.getBytes().length;	// will topic
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strWillMessage.getBytes().length;	// will message
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strUserName.getBytes().length;	// user name
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + m_strPassword.getBytes().length;	// password
		
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.getPayloadByteNum(): "+ nByteNum);
		}
		
		return nByteNum;
	}

	@Override
	protected void marshallPayload() {
		// TODO Auto-generated method stub
		
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.marshallPayload(): ");
			System.out.println("client id: "+m_strClientID+", will topic: "+m_strWillTopic
					+", will message: "+m_strWillMessage+", user name: "+m_strUserName
					+", password: "+m_strPassword);
		}
		
		putStringToByteBuffer(m_strClientID);	// client id
		putStringToByteBuffer(m_strWillTopic);	// will topic
		putStringToByteBuffer(m_strWillMessage);	// will message
		putStringToByteBuffer(m_strUserName);	// user name
		putStringToByteBuffer(m_strPassword);	// password
	}

	@Override
	protected void unmarshallPayload(ByteBuffer buf) {
		// TODO Auto-generated method stub
		m_strClientID = getStringFromByteBuffer(buf);	// client id
		m_strWillTopic = getStringFromByteBuffer(buf);	// will topic
		m_strWillMessage = getStringFromByteBuffer(buf);	// will message
		m_strUserName = getStringFromByteBuffer(buf);	// user name
		m_strPassword = getStringFromByteBuffer(buf);	// password
		
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMMqttEventCONNECT.unmarshallPayload(): ");
			System.out.println("client id: "+m_strClientID+", will topic: "+m_strWillTopic
					+", will message: "+m_strWillMessage+", user name: "+m_strUserName
					+", password: "+m_strPassword);
		}
	}

	//////////////////////////////////////////////////
	// overridden methods

	@Override
	public String toString()
	{
		StringBuffer strBufVarHeader = new StringBuffer();
		strBufVarHeader.append("CMMqttEventCONNECT {");
		strBufVarHeader.append(super.toString()+", ");
		strBufVarHeader.append("\"protocolName\": \""+m_strProtocolName+"\", ");
		strBufVarHeader.append("\"protocolLevel\": "+m_protocolLevel+", ");
		strBufVarHeader.append("\"connectFlag\": \""+getBinaryStringOfByte(m_connectFlag)+
				"\", ");
		strBufVarHeader.append("\"keepAlive\": "+m_nKeepAlive+", ");
		strBufVarHeader.append("}");
		
		StringBuffer strBufCONNECTBuffer = new StringBuffer();
		strBufCONNECTBuffer.append(strBufVarHeader);
		strBufCONNECTBuffer.append("{");
		strBufCONNECTBuffer.append("\"clientID\": \""+m_strClientID+"\", ");
		strBufCONNECTBuffer.append("\"willTopic\": \""+m_strWillTopic+"\", ");
		strBufCONNECTBuffer.append("\"willMessage\": \""+m_strWillMessage+"\", ");
		strBufCONNECTBuffer.append("\"userName\": \""+m_strUserName+"\", ");
		strBufCONNECTBuffer.append("\"password\": \""+m_strPassword+"\"");
		strBufCONNECTBuffer.append("}");
		
		return strBufCONNECTBuffer.toString();
	}

}
