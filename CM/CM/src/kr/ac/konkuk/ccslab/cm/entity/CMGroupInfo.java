package kr.ac.konkuk.ccslab.cm.entity;

/**
 * The CMGroupInfo class represents a group in a session.
 * 
 * @author CCSLab, Konkuk University
 *
 */
public class CMGroupInfo {
	protected String m_strGroupName;
	protected String m_strGroupAddress;
	protected int m_nGroupPort;

	/**
	 * Creates an instance of the CMGroupInfo class.
	 */
	public CMGroupInfo()
	{
		m_strGroupName = "";
		m_strGroupAddress = "";
		m_nGroupPort = -1;
	}
	
	/**
	 * Creates an instance of the CMGroupInfo class.
	 * 
	 * @param gname - group name
	 * @param address - multicast address assigned to this group
	 * @param port - multicast port number of this group
	 */
	public CMGroupInfo(String gname, String address, int port)
	{
		m_strGroupName = gname;
		m_strGroupAddress = address;
		m_nGroupPort = port;
	}

	public void setGroupName(String name)
	{
		m_strGroupName = name;
	}
	
	public void setGroupAddress(String addr)
	{
		m_strGroupAddress = addr;
	}
	
	public void setGroupPort(int port)
	{
		m_nGroupPort = port;
	}
	
	/**
	 * Returns the group name.
	 * 
	 * @return the group name.
	 */
	public String getGroupName()
	{
		return m_strGroupName;
	}
	
	/**
	 * Returns the multicast address assigned to this group.
	 * 
	 * @return the multicast address
	 */
	public String getGroupAddress()
	{
		return m_strGroupAddress;
	}
	
	/**
	 * Returns the multicast port number of this group.
	 * 
	 * @return the port number
	 */
	public int getGroupPort()
	{
		return m_nGroupPort;
	}
}
