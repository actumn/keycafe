package kr.ac.konkuk.ccslab.cm.thread;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMUnknownChannelInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPINGREQ;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMInteractionManager;

public class CMServerKeepAliveTask implements Runnable {

	private CMInfo m_cmInfo;
	private static final Logger LOG = Logger.getLogger(CMServerKeepAliveTask.class.getName());
	
	public CMServerKeepAliveTask(CMInfo cmInfo)
	{
		m_cmInfo = cmInfo;
	}
	
	@Override
	public void run()
	{
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		if(confInfo.getLogLevel() == 0)
			LOG.setLevel(Level.SEVERE);

		long lCurTime = System.currentTimeMillis();
		long lElapsedTime = 0;
		int nKeepAliveTime = 0;
		int i = 0;
		
		// for each login user
		CMMember loginMembers = m_cmInfo.getInteractionInfo().getLoginUsers();
		Vector<CMUser> loginUsersVector = loginMembers.getAllMembers();
		for(i = loginUsersVector.size()-1; i >= 0; i--)
		{
			CMUser user = loginUsersVector.elementAt(i);
			lElapsedTime = lCurTime - user.getLastEventTransTime();
			nKeepAliveTime = user.getKeepAliveTime();
			if(lElapsedTime/1000.0 > nKeepAliveTime*1.5)
			{
				LOG.info("for user("+user.getName()+"), elapsed time("
						+(lElapsedTime/1000.0)+"), keep-alive time*1.5("
						+(nKeepAliveTime*1.5)+").");
				
				CMInteractionManager.disconnectBadClientByServer(user, m_cmInfo);

				LOG.info("disconnect user("+user.getName()+"), # login users: "
						+loginUsersVector.size());
			}
		}
		
		// for each unknown channel
		CMList<CMUnknownChannelInfo> unchList = m_cmInfo.getCommInfo()
				.getUnknownChannelInfoList();
		Vector<CMUnknownChannelInfo> unchVector = unchList.getList();
		Iterator<CMUnknownChannelInfo> iter = unchVector.iterator();
		while(iter.hasNext())
		{
			CMUnknownChannelInfo unch = iter.next();
			lElapsedTime = lCurTime - unch.getLastEventTransTime();
			nKeepAliveTime = m_cmInfo.getConfigurationInfo().getKeepAliveTime();
			if(lElapsedTime/1000.0 > nKeepAliveTime*1.5)
			{
				LOG.info("for unknown-channel, elapsed time("+(lElapsedTime/1000.0)
						+"), keep-alive time*1.5("+(nKeepAliveTime*1.5)+")");
				
				try {
					unch.getUnknownChannel().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				iter.remove();
				
				LOG.info("removed from unknown-channel list: "+unch.getUnknownChannel()+"\n"
						+"channel hash code: "+unch.getUnknownChannel().hashCode()+"\n"
						+"# unknown-channel list: "+unchVector.size());
			}
		}
		
		// process of the default server
		if(CMConfigurator.isDServer(m_cmInfo))
		{
			// for each additional server
			Vector<CMServer> addServerVector = m_cmInfo.getInteractionInfo()
					.getAddServerList();
			for(i = addServerVector.size()-1; i >= 0; i--)
			{
				CMServer addServer = addServerVector.elementAt(i);
				lElapsedTime = lCurTime - addServer.getLastEventTransTime();
				nKeepAliveTime = addServer.getKeepAliveTime();
				if(lElapsedTime/1000.0 > nKeepAliveTime*1.5)
				{
					LOG.info("for add-server("+addServer.getServerName()+"), cur time("
							+lCurTime+"), last event-trans time("
							+addServer.getLastEventTransTime()+"), \n"
							+"elapsed time("+(lElapsedTime/1000.0)+"), keep-alive time("
							+nKeepAliveTime+").");

					CMInteractionManager.disconnectBadAddServerByDefaultServer(addServer, 
							m_cmInfo);
					
					LOG.info("disconnected add-server("+addServer.getServerName()+").\n"
							+"# add-servers: "+addServerVector.size());
				}
			}
		}
		else	// process of an additional server
		{
			CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
			if(myself.getState() >= CMInfo.CM_LOGIN)
			{
				String strDefServer = m_cmInfo.getInteractionInfo().getDefaultServerInfo()
						.getServerName();
				long lMyLastEventTransTime = myself.getMyLastEventTransTimeHashtable()
						.get(strDefServer);
				lElapsedTime = lCurTime - lMyLastEventTransTime;
				nKeepAliveTime = m_cmInfo.getConfigurationInfo().getKeepAliveTime();
				
				if(lElapsedTime/1000.0 > nKeepAliveTime)
				{
					LOG.info("cur time("+lCurTime+"), my last event-trans time("
							+lMyLastEventTransTime+"), \n"
							+ "elapsed time("+(lElapsedTime/1000.0)
							+"), keep-alive time("+nKeepAliveTime+")");

					CMMqttEventPINGREQ reqPingEvent = new CMMqttEventPINGREQ();
					reqPingEvent.setSender(myself.getName());
					CMEventManager.unicastEvent(reqPingEvent, strDefServer, m_cmInfo);
				}
			}
		} // else
		
	}	// run()

}
