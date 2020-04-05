package kr.ac.konkuk.ccslab.cm.entity;
import java.net.InetSocketAddress;
import java.nio.channels.*;

public class CMGroup extends CMGroupInfo {
	private CMMember m_groupUsers;
	private CMChannelInfo<InetSocketAddress> m_mcInfo;
	private MembershipKey m_membershipKey;	// required for leaving a group
	
	public CMGroup()
	{
		super();
		m_groupUsers = new CMMember();
		m_mcInfo = new CMChannelInfo<InetSocketAddress>();
		m_membershipKey = null;
	}
	
	public CMGroup(String strGroupName, String strAddress, int nPort)
	{
		super(strGroupName, strAddress, nPort);
		m_groupUsers = new CMMember();
		m_mcInfo = new CMChannelInfo<InetSocketAddress>();
		m_membershipKey = null;
	}
	
	// set/get methods
	public synchronized CMMember getGroupUsers()
	{
		return m_groupUsers;
	}
	
	public synchronized CMChannelInfo<InetSocketAddress> getMulticastChannelInfo()
	{
		return m_mcInfo;
	}
	
	public synchronized MembershipKey getMembershipKey()
	{
		return m_membershipKey;
	}
	
	public synchronized void setMembershipKey(MembershipKey key)
	{
		m_membershipKey = key;
	}
}
