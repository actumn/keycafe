package kr.ac.konkuk.ccslab.cm.manager;
import java.io.IOException;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;

public class CMSessionManager {

	public static void init(CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		if(!confInfo.getSystemType().equals("SERVER"))
		{
			return;
		}
		
		configureGroups(cmInfo);
		CMGroupManager.init(cmInfo);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMSessionManager.init(), succeeded.");
	}
	
	private static void configureGroups(CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		Iterator<CMSession> iter = interInfo.getSessionList().iterator();
		
		while(iter.hasNext())
		{
			CMSession session = iter.next();
			int nGroupNum = -1;
			try {
				nGroupNum = Integer.parseInt(CMConfigurator.getConfiguration(session.getSessionConfFileName(), "GROUP_NUM"));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		
			if(nGroupNum <= 0)
			{
				// default group
				CMGroup group = new CMGroup();
				group.setGroupName("g1");	// default group name
				group.setGroupAddress(confInfo.getMulticastAddress());	// default MA
				group.setGroupPort(confInfo.getMulticastPort());	// default multicast port
			}
			else
			{
				for(int i = 1; i <= nGroupNum; i++)
				{
					// create and set a group
					CMGroup group = new CMGroup();
					String strGroupName = null;
					String strGroupAddr = null;
					int nGroupPort = -1;

					strGroupName = CMConfigurator.getConfiguration(session.getSessionConfFileName(), "GROUP_NAME"+i);
					strGroupAddr = CMConfigurator.getConfiguration(session.getSessionConfFileName(), "GROUP_ADDR"+i);
					nGroupPort = Integer.parseInt(CMConfigurator.getConfiguration(session.getSessionConfFileName(), "GROUP_PORT"+i));

					group.setGroupName(strGroupName);
					group.setGroupAddress(strGroupAddr);
					group.setGroupPort(nGroupPort);
					
					// add a group in current session
					session.addGroup(group);
				}
			}
			
			if(CMInfo._CM_DEBUG)
				System.out.println("CMSessionManager.configureGroups(), succeeded: session("+session.getSessionName()+"), "+nGroupNum+" groups.");
		}				
	}
	
	public static void processEvent(CMMessage msg, CMInfo cmInfo)
	{
		CMEvent cmEvent = null;
		
		// unmarshall an event
		cmEvent = CMEventManager.unmarshallEvent(msg.m_buf);
		if(cmEvent == null)
		{
			System.out.println("CMSessionManager.processEvent(), unmarshalled event is null.");
			return ;
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("---- CMSessionManager.processEvent() starts. event(type: "
					+cmEvent.getType()+", id: "+cmEvent.getID()+").");
		}

		int nEventType = cmEvent.getType();
		int nEventID = cmEvent.getID();
		switch(nEventID)
		{
		case CMSessionEvent.JOIN_SESSION:
			processJOIN_SESSION(msg, cmInfo);
			break;
		case CMSessionEvent.JOIN_SESSION_ACK:
			processJOIN_SESSION_ACK(msg, cmInfo);
			break;
		case CMSessionEvent.LEAVE_SESSION:
			processLEAVE_SESSION(msg, cmInfo);
			break;
		case CMSessionEvent.SESSION_TALK:
			processSESSION_TALK(msg, cmInfo);
			break;
		default:
			System.out.println("CMSessionManager.processEvent(), unknown event(type: "+nEventType+", id:"+nEventID+").");
			cmEvent = null;
			return;
		}
		
		cmEvent = null;
		return;
	}
	
	private static void processJOIN_SESSION(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		if(!confInfo.getSystemType().equals("SERVER"))
			return;
		
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		
		//find login user
		CMUser user = interInfo.getLoginUsers().findMember(se.getUserName());
		if(user == null)
		{
			System.out.println("CMSessionManager.processJOIN_SESSION(), user("+se.getUserName()+") "
					+ "not found in the login user list.");
			se = null;
			return;
		}
		user.setCurrentSession(se.getSessionName());
		user.setCurrentGroup("g1");	// default group name
		
		joinSession(user, cmInfo);
		
		// notify all users of the fact that a user changes a session
		CMSessionEvent tse = new CMSessionEvent();
		tse.setID(CMSessionEvent.CHANGE_SESSION);
		tse.setUserName(se.getUserName());
		tse.setSessionName(se.getSessionName());
		CMEventManager.broadcastEvent(tse, cmInfo);

		tse = null;
		se = null;
	}

	// join session of the default server
	private static boolean joinSession(CMUser user, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		boolean ret = false;
		
		// find session
		CMSession session = interInfo.findSession(user.getCurrentSession());
		if(session == null)
		{
			System.out.println("CMSessionManager.joinSession(), session("+user.getCurrentSession()
					+") not found, for user("+user.getName()+").");
			return false;
		}
		
		// set user info in the session member
		ret = session.getSessionUsers().addMember(user);
		if(!ret)
		{
			return false;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSessionManager.joinSession(), add user("+user.getName()+"), "
					+"# session users("+session.getSessionUsers().getMemberNum()+").");
		}
		
		// send JOIN_SESSION_ACK
		CMSessionEvent seAck = new CMSessionEvent();
		seAck.setID(CMSessionEvent.JOIN_SESSION_ACK);
		seAck.setSender(interInfo.getMyself().getName());
		seAck.setReceiver(user.getName());
		seAck.setHandlerSession(user.getCurrentSession());
		seAck.setGroupNum(session.getGroupList().size());
		Iterator<CMGroup> iterGroup = session.getGroupList().iterator();
		while(iterGroup.hasNext())
		{
			CMGroup tGroup = iterGroup.next();
			CMGroupInfo gInfo = new CMGroupInfo(tGroup.getGroupName(), tGroup.getGroupAddress(), tGroup.getGroupPort());
			seAck.addGroupInfo(gInfo);
		}
		
		ret = CMEventManager.unicastEvent(seAck, user.getName(), cmInfo);
		
		seAck.removeAllGroupInfoObjects();
		seAck = null;
		return ret;
	}
	
	private static void processJOIN_SESSION_ACK(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		if(!confInfo.getSystemType().equals("CLIENT"))
		{
			return;
		}
		
		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		if(se.getGroupInfoList().size() < 1)
		{
			System.out.println("CMSessionManager.processJOIN_SESSION_ACK(), group information is empty.");
			se = null;
			return;
		}
		
		// find a session
		CMSession session = interInfo.findSession(se.getHandlerSession());
		if(session == null)
		{
			// create a new session
			session = new CMSession();
			session.setSessionName(se.getHandlerSession());			
			interInfo.addSession(session);
		}
				
		// add group information
		Iterator<CMGroupInfo> iter = se.getGroupInfoList().iterator();
		CMGroup tGroup = null;
		while(iter.hasNext())
		{
			CMGroupInfo tInfo = iter.next();
			tGroup = new CMGroup(tInfo.getGroupName(), tInfo.getGroupAddress(), tInfo.getGroupPort());
			session.addGroup(tGroup);
		}
		
		// set current group of user
		tGroup = session.getGroupList().elementAt(0);	// first group
		interInfo.getMyself().setCurrentGroup(tGroup.getGroupName());
		// initialize current group
		CMGroupManager.init(session.getSessionName(), tGroup.getGroupName(), cmInfo);
		
		// update user's state
		interInfo.getMyself().setState(CMInfo.CM_SESSION_JOIN);
		
		// join the current group
		CMInterestEvent ie = new CMInterestEvent();
		ie.setID(CMInterestEvent.USER_ENTER);
		ie.setHandlerSession(session.getSessionName());
		ie.setHandlerGroup(tGroup.getGroupName());
		ie.setUserName(interInfo.getMyself().getName());
		ie.setHostAddress(interInfo.getMyself().getHost());
		ie.setUDPPort(interInfo.getMyself().getUDPPort());
		ie.setCurrentGroup(interInfo.getMyself().getCurrentGroup());
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		CMEventManager.unicastEvent(ie, strDefServer, cmInfo);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMSessionManager.processJOIN_SESSION_ACK() succeeded.");
		
		se.removeAllGroupInfoObjects();
		se = null;
		ie = null;
		return;
	}
	
	private static void processLEAVE_SESSION(CMMessage msg, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		if(!confInfo.getSystemType().equals("SERVER"))
			return;

		CMSessionEvent se = new CMSessionEvent(msg.m_buf);
		CMUser user = interInfo.getLoginUsers().findMember(se.getUserName());
		if(user == null)
		{
			System.out.println("CMSessionManager.leaveSession(), user("+se.getUserName()+") not found "
					+ "in the login user list.");
			return;
		}
		
		if(!se.getSessionName().equals(user.getCurrentSession()))
		{
			System.out.println("CMSessionManager.processLEAVE_SESSION(), Oops! requested session name"
					+" and current session of the user are different!");
			return;
		}
		
		leaveSession(user, cmInfo);
		
		// notify login users of the session leave
		CMSessionEvent tse = new CMSessionEvent();
		tse.setID(CMSessionEvent.CHANGE_SESSION);
		tse.setUserName(user.getName());
		tse.setSessionName("");
		CMEventManager.broadcastEvent(tse, cmInfo);
		
		//// do not send LEAVE_SESSION_ACK (?)
		
		se = null;
		tse = null;
		return;
	}
	
	// leave current session of the default server
	public static void leaveSession(CMUser user, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		String strCurrentSession = user.getCurrentSession();
		
		// leave from the current group and notify group members
		CMGroupManager.leaveGroup(user, cmInfo);
		
		// leave from the current session
		CMSession session = interInfo.findSession(strCurrentSession);
		if(session == null)
		{
			System.out.println("CMSessionManager.leaveSession(), session("+strCurrentSession
					+") not found for leaving user("+user.getName()+").");
			return;
		}
		session.getSessionUsers().removeMember(user.getName());
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSessionManager.leaveSession(), user("+user.getName()+"), session("
					+strCurrentSession+"), # remaining session member ("
					+session.getSessionUsers().getMemberNum()+").");
		}
		
		user.setCurrentSession("");

		return;
	}
	
	private static void processSESSION_TALK(CMMessage msg, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			CMSessionEvent se = new CMSessionEvent(msg.m_buf);
			System.out.println("CMSessionManager.processSESSION_TALK(), casted by user("
					+se.getUserName()+"), session("+se.getDistributionSession()+").");
			System.out.println("chat: "+se.getTalk());
			se = null;
		}
		
		return;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	
	// join a session of an additional server
	public static boolean addJoinSession(CMUser user, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMSession session = null;
		boolean ret = false;
		
		// add user info to the session user list
		session = interInfo.findSession(user.getCurrentSession());
		ret = session.getSessionUsers().addMember(user);
		
		if(!ret)
		{
			System.out.println("CMSessionManager.addJoinSession(), fail to add user("+user.getName()+") "
					+ "to session("+user.getCurrentSession()+").");
			return false;
		}
		
		// ADD_JOIN_SESSION_ACK
		CMMultiServerEvent mseAck = new CMMultiServerEvent();
		mseAck.setID(CMMultiServerEvent.ADD_JOIN_SESSION_ACK);
		mseAck.setServerName(interInfo.getMyself().getName());	// this server name

		// group info
		mseAck.setGroupNum( session.getGroupList().size() );
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		while(iter.hasNext())
		{
			CMGroup tgroup = iter.next();
			CMGroupInfo gi = new CMGroupInfo(tgroup.getGroupName(), tgroup.getGroupAddress(),
					tgroup.getGroupPort());
			mseAck.addGroupInfo(gi);
		}

		CMEventManager.unicastEvent(mseAck, user.getName(), cmInfo);

		mseAck.removeAllGroupInfoObjects();
		mseAck = null;
		return true;
	}
	
	// leave a session of an additional server
	public static void addLeaveSession(CMUser user, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		String strCurrentSession = user.getCurrentSession();
		
		// leave from the current group and notify group members
		CMGroupManager.leaveGroup(user, cmInfo);
		
		// leave from the current session
		CMSession session = interInfo.findSession(strCurrentSession);
		if(session == null)
		{
			System.out.println("CMSessionManager.addLeaveSession(), session("+strCurrentSession
					+") not found for leaving user("+user.getName()+")!");
			return;
		}
		session.getSessionUsers().removeMember(user.getName());
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSessionManager.addLeaveSession(), user("+user.getName()+"), session("
					+strCurrentSession+"), # remaining session member ("
					+session.getSessionUsers().getMemberNum()+").");
		}
		
		user.setCurrentSession("");
		
		return;
	}
}
