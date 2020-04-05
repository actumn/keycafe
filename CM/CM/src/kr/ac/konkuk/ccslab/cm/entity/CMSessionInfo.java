package kr.ac.konkuk.ccslab.cm.entity;

/**
 * This class represents information of a session.
 * <p> The session information is the session name, the session address and port number, and the current 
 * number of session members. The session name is the identifier of the session that is managed by a CM server 
 * that can be accessed by the session address and port number.
 *  
 * @author CCSLab, Konkuk University
 *
 */
public class CMSessionInfo {
	protected String m_strSessionName;
	protected String m_strAddress;
	protected int m_nPort;
	protected int m_nUserNum;
	
	/**
	 * Creates an empty CMSessionInfo object.
	 */
	public CMSessionInfo()
	{
		m_strSessionName = "";
		m_strAddress = "";
		m_nPort = -1;
		m_nUserNum = -1;
	}
	
	/**
	 * Creates a CMSessionInfo object with the session name, the session address and the port number.
	 * @param sname - the session name
	 * @param address - the session address (The IP address of a CM server that manages the session.)
	 * @param port - the session port number (The port number of a CM server that manages the session.)
	 */
	public CMSessionInfo(String sname, String address, int port)
	{
		m_strSessionName = sname;
		m_strAddress = address;
		m_nPort = port;
		m_nUserNum = -1;
	}

	/**
	 * Creates a CMSessionInfo object with the session name, the session address and the port number, and 
	 * the number of session members.
	 * @param sname - the session name
	 * @param address - the session address (The IP address of a CM server that manages the session.)
	 * @param port - the session port number (The port number of a CM server that manages the session.)
	 * @param nUserNum - the current number of users who belong to the session.
	 */
	public CMSessionInfo(String sname, String address, int port, int nUserNum)
	{
		m_strSessionName = sname;
		m_strAddress = address;
		m_nPort = port;
		m_nUserNum = nUserNum;
	}

	/**
	 * Sets the session name.
	 * @param name - the session name
	 */
	public synchronized void setSessionName(String name)
	{
		m_strSessionName = name;
	}
	
	/**
	 * Sets the session address.
	 * @param addr - the session address (The IP address of a CM server that manages the session.)
	 */
	public synchronized void setAddress(String addr)
	{
		m_strAddress = addr;
	}
	
	/**
	 * Sets the session port number.
	 * @param port - the session port number (The port number of a CM server that manages the session.)
	 */
	public synchronized void setPort(int port)
	{
		m_nPort = port;
	}

	/**
	 * Sets the current number of session members.
	 * @param num - the current number of users who belong to the session.
	 */
	public synchronized void setUserNum(int num)
	{
		m_nUserNum = num;
	}

	/**
	 * Gets the session name.
	 * @return - the session name, or an empty string if the session name is not available.
	 */
	public synchronized String getSessionName()
	{
		return m_strSessionName;
	}
	
	/**
	 * Gets the session address.
	 * @return - the session address, or an empty string if the session address is not available.
	 */
	public synchronized String getAddress()
	{
		return m_strAddress;
	}
	
	/**
	 * Gets the session port number.
	 * @return - the session port number, or -1 if the port number is not available.
	 */
	public synchronized int getPort()
	{
		return m_nPort;
	}

	/**
	 * Gets the current number of session members.
	 * @return - the current number of session members, or -1 if the current number is not available.
	 */
	public synchronized int getUserNum()
	{
		return m_nUserNum;
	}
}
