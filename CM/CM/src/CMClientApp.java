import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JOptionPane;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMPosition;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.util.CMUtil;


public class CMClientApp {
	private CMClientStub m_clientStub;
	private CMClientEventHandler m_eventHandler;
	private boolean m_bRun;
	private Scanner m_scan = null;
	
	public CMClientApp()
	{
		m_clientStub = new CMClientStub();
		m_eventHandler = new CMClientEventHandler(m_clientStub);
		m_bRun = true;
	}
	
	public CMClientStub getClientStub()
	{
		return m_clientStub;
	}
	
	public CMClientEventHandler getClientEventHandler()
	{
		return m_eventHandler;
	}
	
	///////////////////////////////////////////////////////////////
	// test methods

	public void startTest()
	{
		System.out.println("client application starts.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		m_scan = new Scanner(System.in);
		String strInput = null;
		int nCommand = -1;
		while(m_bRun)
		{
			System.out.println("Type \"0\" for menu.");
			System.out.print("> ");
			try {
				strInput = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			try {
				nCommand = Integer.parseInt(strInput);
			} catch (NumberFormatException e) {
				System.out.println("Incorrect command number!");
				continue;
			}
			
			switch(nCommand)
			{
			case 0:
				printAllMenus();
				break;
			case 100:
				testStartCM();
				break;
			case 999:
				testTerminateCM();
				break;			
			case 1: // connect to default server
				testConnectionDS();
				break;
			case 2: // disconnect from default server
				testDisconnectionDS();
				break;
			case 3: // connect to a designated server
				testConnectToServer();
				break;
			case 4: // disconnect from a designated server
				testDisconnectFromServer();
				break;
			case 10: // asynchronous login to default server
				testLoginDS();
				break;
			case 11: // synchronously login to default server
				testSyncLoginDS();
				break;
			case 12: // logout from default server
				testLogoutDS();
				break;
			case 13: // log in to a designated server
				testLoginServer();
				break;
			case 14: // log out from a designated server
				testLogoutServer();
				break;
			case 20: // request session info from default server
				testSessionInfoDS();
				break;
			case 21: // synchronously request session info from default server
				testSyncSessionInfoDS();
				break;
			case 22: // join a session
				testJoinSession();
				break;
			case 23: // synchronously join a session
				testSyncJoinSession();
				break;
			case 24: // leave the current session
				testLeaveSession();
				break;
			case 25: // change current group
				testChangeGroup();
				break;
			case 26: // print group members
				testPrintGroupMembers();
				break;
			case 27: // request session information from a designated server
				testRequestSessionInfoOfServer();
				break;
			case 28: // join a session of a designated server
				testJoinSessionOfServer();
				break;
			case 29: // leave a session of a designated server
				testLeaveSessionOfServer();
				break;
			case 40: // chat
				testChat();
				break;
			case 41: // test multicast chat in current group
				testMulticastChat();
				break;
			case 42: // test CMDummyEvent
				testDummyEvent();
				break;
			case 43: // test CMUserEvent
				testUserEvent();
				break;
			case 44: // test datagram message
				testDatagram();
				break;			
			case 45: // user position
				testUserPosition();
				break;			
			case 46: // test sendrecv
				testSendRecv();
				break;
			case 47: // test castrecv
				testCastRecv();
				break;
			case 48: // test asynchronous sendrecv
				testAsyncSendRecv();
				break;
			case 49: // test asynchronous castrecv
				testAsyncCastRecv();
				break;
			case 50: // print group info
				testPrintGroupInfo();
				break;
			case 51: // print current information about the client
				testCurrentUserStatus();
				break;
			case 52: 	// print current channels information
				testPrintCurrentChannelInfo();
				break;
			case 53: // request additional server info
				testRequestServerInfo();
				break;
			case 54: // print current group info of a designated server
				testPrintGroupInfoOfServer();
				break;
			case 55: // test input network throughput
				testMeasureInputThroughput();
				break;
			case 56: // test output network throughput
				testMeasureOutputThroughput();
				break;
			case 57: // print all configurations
				testPrintConfigurations();
				break;
			case 58: // change configuration
				testChangeConfiguration();
				break;
			case 60: // add additional channel
				testAddChannel();
				break;
			case 61: // remove additional channel
				testRemoveChannel();
				break;
			case 62: // test blocking channel
				testBlockingChannel();
				break;
			case 70: // set file path
				testSetFilePath();
				break;
			case 71: // request a file
				testRequestFile();
				break;
			case 72: // push a file
				testPushFile();
				break;
			case 73:	// test cancel receiving a file
				cancelRecvFile();
				break;
			case 74:	// test cancel sending a file
				cancelSendFile();
				break;
			case 75:	// print sending/receiving file info
				printSendRecvFileInfo();
				break;
			case 80: // test SNS content download
				testDownloadNewSNSContent();
				break;
			case 81:
				testDownloadNextSNSContent();
				break;
			case 82:
				testDownloadPreviousSNSContent();
				break;
			case 83: // request an attached file of SNS content
				testRequestAttachedFileOfSNSContent();
				break;
			case 84: // test SNS content upload
				testSNSContentUpload();
				break;
			case 90: // register user
				testRegisterUser();
				break;
			case 91: // deregister user
				testDeregisterUser();
				break;
			case 92: // find user
				testFindRegisteredUser();
				break;
			case 93: // add a new friend
				testAddNewFriend();
				break;
			case 94: // remove a friend
				testRemoveFriend();
				break;
			case 95: // request current friends list
				testRequestFriendsList();
				break;
			case 96: // request friend requesters list
				testRequestFriendRequestersList();
				break;
			case 97: // request bi-directional friends
				testRequestBiFriendsList();
				break;
			case 101: // test forwarding schemes (typical vs. internal)
				testForwarding();
				break;
			case 102: // test delay of forwarding schemes
				testForwardingDelay();
				break;
			case 103: // test repeated downloading of SNS content
				testRepeatedSNSContentDownload();
				break;
			case 104: // pull or push multiple files
				testSendMultipleFiles();
				break;
			case 105: // split a file
				testSplitFile();
				break;
			case 106: // merge files
				testMergeFiles();
				break;
			case 107: // distribute a file and merge
				testDistFileProc();
				break;
			case 108: // send an event with wrong # bytes
				testSendEventWithWrongByteNum();
				break;
			case 109: // send an event with wrong event type
				testSendEventWithWrongEventType();
				break;
			case 200: // MQTT connect
				testMqttConnect();
				break;
			case 201: // MQTT publish
				testMqttPublish();
				break;
			case 202: // MQTT subscribe
				testMqttSubscribe();
				break;
			case 203: // print MQTT session info
				testPrintMqttSessionInfo();
				break;
			case 204: // MQTT unsubscribe
				testMqttUnsubscribe();
				break;
			case 205: // MQTT disconnect
				testMqttDisconnect();
				break;
			default:
				System.err.println("Unknown command.");
				break;
			}
		}
		
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_scan.close();
	}
	
	public void printAllMenus()
	{
		System.out.println("---------------------------------- Help");
		System.out.println("0: show all menus");
		System.out.println("---------------------------------- Start/Stop");
		System.out.println("100: start CM, 999: terminate CM");
		System.out.println("---------------------------------- Connection");
		System.out.println("1: connect to default server, 2: disconnect from default server");
		System.out.println("3: connect to designated server, 4: disconnect from designated server");
		System.out.println("---------------------------------- Login");
		System.out.println("10: login to default server, 11: synchronously login to default server");
		System.out.println("12: logout from default server");
		System.out.println("13: login to designated server, 14: logout from designated server");
		System.out.println("---------------------------------- Session/Group");
		System.out.println("20: request session information from default server");
		System.out.println("21: synchronously request session information from default server");
		System.out.println("22: join session of default server, 23: synchronously join session of default server");
		System.out.println("24: leave session of default server, 25: change group of default server");
		System.out.println("26: print group members");
		System.out.println("27: request session information from designated server");
		System.out.println("28: join session of designated server, 29: leave session of designated server");
		System.out.println("---------------------------------- Event Transmission");
		System.out.println("40: chat, 41: multicast chat in current group");
		System.out.println("42: test CMDummyEvent, 43: test CMUserEvent, 44: test datagram event, 45: test user position");
		System.out.println("46: test sendrecv, 47: test castrecv");
		System.out.println("48: test asynchronous sendrecv, 49: test asynchronous castrecv");
		System.out.println("---------------------------------- Information");
		System.out.println("50: show group information of default server, 51: show current user status");
		System.out.println("52: show current channels, 53: show current server information");
		System.out.println("54: show group information of designated server");
		System.out.println("55: measure input network throughput, 56: measure output network throughput");
		System.out.println("57: show all configurations, 58: change configuration");
		System.out.println("---------------------------------- Channel");
		System.out.println("60: add channel, 61: remove channel, 62: test blocking channel");
		System.out.println("---------------------------------- File Transfer");
		System.out.println("70: set file path, 71: request file, 72: push file");
		System.out.println("73: cancel receiving file, 74: cancel sending file");
		System.out.println("75: print sending/receiving file info");
		System.out.println("---------------------------------- Social Network Service");
		System.out.println("80: request content list, 81: request next content list, 82: request previous content list");
		System.out.println("83: request attached file, 84: upload content");
		System.out.println("---------------------------------- User");
		System.out.println("90: register new user, 91: deregister user, 92: find registered user");
		System.out.println("93: add new friend, 94: remove friend, 95: show friends, 96: show friend requesters");
		System.out.println("97: show bi-directional friends");
		System.out.println("---------------------------------- MQTT");
		System.out.println("200: connect, 201: publish, 202: subscribe, 203: print session info");
		System.out.println("204: unsubscribe, 205: disconnect");
		System.out.println("---------------------------------- Other CM Tests");
		System.out.println("101: test forwarding scheme, 102: test delay of forwarding scheme");
		System.out.println("103: test repeated request of SNS content list");
		System.out.println("104: pull/push multiple files, 105: split file, 106: merge files, 107: distribute and merge file");
		System.out.println("108: send event with wrong # bytes, 109: send event with wrong type");
	}
	
	public void testConnectionDS()
	{
		System.out.println("====== connect to default server");
		m_clientStub.connectToServer();
		System.out.println("======");
	}
	
	public void testDisconnectionDS()
	{
		System.out.println("====== disconnect from default server");
		m_clientStub.disconnectFromServer();
		System.out.println("======");
	}
	
	public void testLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}
		
		System.out.println("====== login to default server");
		System.out.print("user name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strUserName = br.readLine();
			if(console == null)
			{
				System.out.print("password: ");
				strPassword = br.readLine();
			}
			else
				strPassword = new String(console.readPassword("password: "));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
		if(bRequestResult)
			System.out.println("successfully sent the login request.");
		else
			System.err.println("failed the login request!");
		System.out.println("======");
	}
	
	public void testSyncLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		CMSessionEvent loginAckEvent = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}
		
		System.out.println("====== login to default server");
		System.out.print("user name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strUserName = br.readLine();
			if(console == null)
			{
				System.out.print("password: ");
				strPassword = br.readLine();
			}
			else
				strPassword = new String(console.readPassword("password: "));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);
		if(loginAckEvent != null)
		{
			// print login result
			if(loginAckEvent.isValidUser() == 0)
			{
				System.err.println("This client fails authentication by the default server!");
			}
			else if(loginAckEvent.isValidUser() == -1)
			{
				System.err.println("This client is already in the login-user list!");
			}
			else
			{
				System.out.println("This client successfully logs in to the default server.");
			}			
		}
		else
		{
			System.err.println("failed the login request!");
		}

		System.out.println("======");		
	}
	
	public void testLogoutDS()
	{
		boolean bRequestResult = false;
		System.out.println("====== logout from default server");
		bRequestResult = m_clientStub.logoutCM();
		if(bRequestResult)
			System.out.println("successfully sent the logout request.");
		else
			System.err.println("failed the logout request!");
		System.out.println("======");
	}
	
	public void testStartCM()
	{
		// get current server info from the server configuration file
		String strCurServerAddress = null;
		int nCurServerPort = -1;
		String strNewServerAddress = null;
		String strNewServerPort = null;
		
		strCurServerAddress = m_clientStub.getServerAddress();
		nCurServerPort = m_clientStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("========== start CM");
		System.out.println("current server address: "+strCurServerAddress);
		System.out.println("current server port: "+nCurServerPort);
		
		try {
			System.out.print("new server address (enter for current value): ");
			strNewServerAddress = br.readLine().trim();
			System.out.print("new server port (enter for current value): ");
			strNewServerPort = br.readLine().trim();

			// update the server info if the user would like to do
			if(!strNewServerAddress.isEmpty() && !strNewServerAddress.equals(strCurServerAddress))
				m_clientStub.setServerAddress(strNewServerAddress);
			if(!strNewServerPort.isEmpty() && Integer.parseInt(strNewServerPort) != nCurServerPort)
				m_clientStub.setServerPort(Integer.parseInt(strNewServerPort));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean bRet = m_clientStub.startCM();
		if(!bRet)
		{
			System.err.println("CM initialization error!");
			return;
		}
		startTest();
	}
	
	public void testTerminateCM()
	{
		m_clientStub.terminateCM();
		m_bRun = false;
	}

	public void testSessionInfoDS()
	{
		boolean bRequestResult = false;
		System.out.println("====== request session info from default server");
		bRequestResult = m_clientStub.requestSessionInfo();
		if(bRequestResult)
			System.out.println("successfully sent the session-info request.");
		else
			System.err.println("failed the session-info request!");
		System.out.println("======");
	}
	
	public void testSyncSessionInfoDS()
	{
		CMSessionEvent se = null;
		System.out.println("====== synchronous request session info from default server");
		se = m_clientStub.syncRequestSessionInfo();
		if(se == null)
		{
			System.err.println("failed the session-info request!");
			return;
		}

		// print the request result
		Iterator<CMSessionInfo> iter = se.getSessionInfoList().iterator();

		System.out.format("%-60s%n", "------------------------------------------------------------");
		System.out.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num");
		System.out.format("%-60s%n", "------------------------------------------------------------");

		while(iter.hasNext())
		{
			CMSessionInfo tInfo = iter.next();
			System.out.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(), 
					tInfo.getPort(), tInfo.getUserNum());
		}

		System.out.println("======");		
	}
	
	public void testJoinSession()
	{
		String strSessionName = null;
		boolean bRequestResult = false;
		System.out.println("====== join a session");
		System.out.print("session name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strSessionName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bRequestResult = m_clientStub.joinSession(strSessionName);
		if(bRequestResult)
			System.out.println("successfully sent the session-join request.");
		else
			System.err.println("failed the session-join request!");
		System.out.println("======");
	}
	
	public void testSyncJoinSession()
	{
		CMSessionEvent se = null;
		String strSessionName = null;
		System.out.println("====== join a session");
		System.out.print("session name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strSessionName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		se = m_clientStub.syncJoinSession(strSessionName);
		if(se != null)
		{
			System.out.println("successfully joined a session that has ("+se.getGroupNum()+") groups.");
		}
		else
		{
			System.err.println("failed the session-join request!");
		}
		
		System.out.println("======");		
	}
	
	public void testLeaveSession()
	{
		boolean bRequestResult = false;
		System.out.println("====== leave the current session");
		bRequestResult = m_clientStub.leaveSession();
		if(bRequestResult)
			System.out.println("successfully sent the leave-session request.");
		else
			System.err.println("failed the leave-session request!");
		System.out.println("======");
	}
	
	public void testUserPosition()
	{
		CMPosition position = new CMPosition();
		String strLine = null;
		String strDelim = "\\s+";
		String[] strTokens;
		System.out.println("====== send user position");
		System.out.print("pos (x,y,z): ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strLine = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		strLine.trim();
		strTokens = strLine.split(strDelim);
		position.m_p.m_x = Float.parseFloat(strTokens[0]);
		position.m_p.m_y = Float.parseFloat(strTokens[1]);
		position.m_p.m_z = Float.parseFloat(strTokens[2]);
		System.out.println("Pos input: ("+position.m_p.m_x+", "+position.m_p.m_y+", "+position.m_p.m_z+")");

		System.out.print("quat (w,x,y,z): ");
		try {
			strLine = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		strLine.trim();
		strTokens = strLine.split(strDelim);
		position.m_q.m_w = Float.parseFloat(strTokens[0]);
		position.m_q.m_x = Float.parseFloat(strTokens[1]);
		position.m_q.m_y = Float.parseFloat(strTokens[2]);
		position.m_q.m_z = Float.parseFloat(strTokens[3]);
		System.out.println("Quat input: ("+position.m_q.m_w+", "+position.m_q.m_x+", "+position.m_q.m_y+", "+position.m_q.m_z+")");
		
		m_clientStub.sendUserPosition(position);
		
		System.out.println("======");
	}
	
	public void testChat()
	{
		String strTarget = null;
		String strMessage = null;
		System.out.println("====== chat");
		System.out.print("target(/b, /s, /g, or /username): ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strTarget = br.readLine();
			strTarget = strTarget.trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print("message: ");
		try {
			strMessage = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.chat(strTarget, strMessage);
		
		System.out.println("======");
	}

	public void testDummyEvent()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You should join a session and a group!");
			return;
		}
		
		System.out.println("====== test CMDummyEvent in current group");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("input message: ");
		String strInput = null;
		try {
			strInput = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CMDummyEvent due = new CMDummyEvent();
		due.setHandlerSession(myself.getCurrentSession());
		due.setHandlerGroup(myself.getCurrentGroup());
		due.setDummyInfo(strInput);
		m_clientStub.cast(due, myself.getCurrentSession(), myself.getCurrentGroup());
		due = null;
		
		System.out.println("======");
	}
	
	public void testDatagram()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMUser myself = interInfo.getMyself();

		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You should join a session and a group!");
			return;
		}
		
		String strReceiver = null;
		String strMessage = null;
		String strSendPort = null;
		String strRecvPort = null;
		int nSendPort = 0;
		int nRecvPort = 0;
		System.out.println("====== test unicast chatting with non-blocking datagram channels");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("receiver: ");
			strReceiver = br.readLine();
			System.out.print("message: ");
			strMessage = br.readLine();
			System.out.print("sender port(enter for default port): ");
			strSendPort = br.readLine();
			System.out.print("receiver port(enter for default port): ");
			strRecvPort = br.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if(strSendPort.isEmpty())
				nSendPort = confInfo.getUDPPort();
			else
				nSendPort = Integer.parseInt(strSendPort);
		}catch(NumberFormatException ne) {
			ne.printStackTrace();
			nSendPort = confInfo.getUDPPort();
		}
			
		try {
			if(strRecvPort.isEmpty())
				nRecvPort = 0;
			else
				nRecvPort = Integer.parseInt(strRecvPort);			
		}catch(NumberFormatException ne)
		{
			ne.printStackTrace();
			nRecvPort = 0;
		}
		
		
		CMInterestEvent ie = new CMInterestEvent();
		ie.setID(CMInterestEvent.USER_TALK);
		ie.setHandlerSession(myself.getCurrentSession());
		ie.setHandlerGroup(myself.getCurrentGroup());
		ie.setUserName(myself.getName());
		ie.setTalk(strMessage);
		
		if(nRecvPort == 0)
			m_clientStub.send(ie, strReceiver, CMInfo.CM_DATAGRAM, nSendPort);
		else
			m_clientStub.send(ie, strReceiver, CMInfo.CM_DATAGRAM, nSendPort, nRecvPort, false);
		ie = null;
		
		System.out.println("======");
		return;
	}
	
	public void testUserEvent()
	{
		String strInput = null;
		String strReceiver = null;
		boolean bEnd = false;
		String[] strTokens = null;
		int nValueByteNum = -1;
		CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You should join a session and a group!");
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== test CMUserEvent");
		System.out.println("data type: CM_INT(0) CM_LONG(1) CM_FLOAT(2) CM_DOUBLE(3) CM_CHAR(4) CM_STR(5) CM_BYTES(6)");
		System.out.println("Type \"end\" to stop.");
		
		CMUserEvent ue = new CMUserEvent();
		ue.setStringID("testID");
		ue.setHandlerSession(myself.getCurrentSession());
		ue.setHandlerGroup(myself.getCurrentGroup());
		while(!bEnd)
		{
			System.out.println("If the data type is CM_BYTES, the number of bytes must be given "
					+ "in the third parameter.");
			System.out.print("(data type, field name, value): ");
			try {
				strInput = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ue.removeAllEventFields();
				ue = null;
				return;
			}
			
			if(strInput.equals("end"))
			{
				bEnd = true;
			}
			else
			{
				strInput.trim();
				strTokens = strInput.split("\\s+");
				if(Integer.parseInt(strTokens[0]) == CMInfo.CM_BYTES)
				{
					nValueByteNum = Integer.parseInt(strTokens[2]);
					if(nValueByteNum < 0)
					{
						System.out.println("CMClientApp.testUserEvent(), Invalid nValueByteNum("
								+nValueByteNum+")");
						ue.removeAllEventFields();
						ue = null;
						return;
					}
					byte[] valueBytes = new byte[nValueByteNum];
					for(int i = 0; i < nValueByteNum; i++)
						valueBytes[i] = 1;	// dummy data
					ue.setEventBytesField(strTokens[1], nValueByteNum, valueBytes);	
				}
				else
					ue.setEventField(Integer.parseInt(strTokens[0]), strTokens[1], strTokens[2]);
			}
		}
		
		System.out.print("receiver: ");
		try {
			strReceiver = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.send(ue, strReceiver);

		System.out.println("======");
		
		ue.removeAllEventFields();
		ue = null;
		return;
	}
	
	// test sendrecv
	public void testSendRecv()
	{
		CMUserEvent ue = new CMUserEvent();
		CMUserEvent rue = null;
		String strTargetName = null;
		
		// a user event: (id, 111) (string id, "testSendRecv")
		// a reply user event: (id, 222) (string id, "testReplySendRecv")
		
		System.out.println("====== test sendrecv");
		
		// create a user event
		ue.setID(111);
		ue.setStringID("testSendRecv");
		
		// get target name
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("user event to be sent: (id, 111), (string id, \"testSendRecv\")");
		System.out.println("reply event to be received: (id, 222), (string id, \"testReplySendRecv\")");
		System.out.print("Target name(empty for \"SERVER\"): ");

		try {
			strTargetName = br.readLine().trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if(strTargetName.isEmpty())
		{
			strTargetName = m_clientStub.getDefaultServerName();
		}
		
		long lStartTime = System.currentTimeMillis();
		rue = (CMUserEvent) m_clientStub.sendrecv(ue, strTargetName, CMInfo.CM_USER_EVENT, 222, 10000);
		long lServerResponseDelay = System.currentTimeMillis() - lStartTime;

		if(rue == null)
			System.err.println("The reply event is null!");
		else
		{
			System.out.println("Received reply event from ["+rue.getSender()+"]: (type, "+rue.getType()+
					"), (id, "+rue.getID()+"), (string id, "+rue.getStringID()+")");
			System.out.println("Server response delay: "+lServerResponseDelay+"ms.");
		}
		
		System.out.println("======");
	}
	
	// test castrecv
	public void testCastRecv()
	{
		CMUserEvent ue = new CMUserEvent();
		CMEvent[] rueArray = null;
		String strTargetSession = null;
		String strTargetGroup = null;
		String strMinNumReplyEvents = null;
		int nMinNumReplyEvents = 0;
		int nTimeout = 10000;

		// a user event: (id, 112) (string id, "testCastRecv")
		// a reply user event: (id, 223) (string id, "testReplyCastRecv")
		
		System.out.println("====== test castrecv");
		// set a user event
		ue.setID(112);
		ue.setStringID("testCastRecv");
		
		// set event target session and group
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("user event to be sent: (id, 112), (string id, \"testCastRecv\")");
		System.out.println("reply event to be received: (id, 223), (string id, \"testReplyCastRecv\")");
		
		try {
			System.out.print("Target session(empty for null): ");
			strTargetSession = br.readLine().trim();
			System.out.print("Target group(empty for null): ");
			strTargetGroup = br.readLine().trim();
			System.out.print("Minimum number of reply events(empty for 0): ");
			strMinNumReplyEvents = br.readLine().trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if(strTargetSession.isEmpty())
			strTargetSession = null;
		if(strTargetGroup.isEmpty())
			strTargetGroup = null;
		if(strMinNumReplyEvents.isEmpty())
			strMinNumReplyEvents = "0";

		try {
			nMinNumReplyEvents = Integer.parseInt(strMinNumReplyEvents);
		}catch(NumberFormatException e) {
			e.printStackTrace();
			System.err.println("Wrong number format!");
			return;
		}
		
		System.out.println("Target session: "+strTargetSession);
		System.out.println("Target group: "+strTargetGroup);
		System.out.println("Minimum number of reply events: "+nMinNumReplyEvents);
		System.out.println("Waiting timeout: "+nTimeout+" ms");

		long lStartTime = System.currentTimeMillis();
		rueArray = m_clientStub.castrecv(ue, strTargetSession, strTargetGroup, 
				CMInfo.CM_USER_EVENT, 223, nMinNumReplyEvents, nTimeout);
		long lServerResponseDelay = System.currentTimeMillis() - lStartTime;

		if(rueArray == null)
		{
			System.err.println("Error in castrecv()!");
			return;
		}
		
		System.out.println("Number of received reply events: "+rueArray.length);
		System.out.print("Reply from: ");
		for(int i = 0; i < rueArray.length; i++)
			System.out.print(rueArray[i].getSender()+" ");
		System.out.println();
		System.out.println("Server response delay: "+lServerResponseDelay+"ms.");
		System.out.println("======");

	}
	
	// test asynchronous sendrecv
	public void testAsyncSendRecv()
	{
		CMUserEvent ue = new CMUserEvent();
		boolean bRet = false;
		String strTargetName = null;
		
		// a user event: (id, 111) (string id, "testSendRecv")
		// a reply user event: (id, 222) (string id, "testReplySendRecv")
		
		System.out.println("====== test asynchronous sendrecv");
		
		// create a user event
		ue.setID(111);
		ue.setStringID("testSendRecv");
		
		// get target name
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("user event to be sent: (id, 111), (string id, \"testSendRecv\")");
		System.out.println("reply event to be received: (id, 222), (string id, \"testReplySendRecv\")");
		System.out.print("Target name(empty for \"SERVER\"): ");

		try {
			strTargetName = br.readLine().trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if(strTargetName.isEmpty())
			strTargetName = m_clientStub.getDefaultServerName();

		m_eventHandler.setStartTime(System.currentTimeMillis());
		bRet = m_clientStub.send(ue, strTargetName);

		if(!bRet)
			System.err.println("Error in asynchronous sendrecv service!");
		
		System.out.println("======");
	}
	
	// test asynchronous castrecv
	public void testAsyncCastRecv()
	{
		CMUserEvent ue = new CMUserEvent();
		boolean bRet = false;
		String strTargetSession = null;
		String strTargetGroup = null;
		String strMinNumReplyEvents = null;
		int nMinNumReplyEvents = 0;

		// a user event: (id, 112) (string id, "testCastRecv")
		// a reply user event: (id, 223) (string id, "testReplyCastRecv")
		
		System.out.println("====== test asynchronous castrecv");
		// set a user event
		ue.setID(112);
		ue.setStringID("testCastRecv");
		
		// set event target session and group
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("user event to be sent: (id, 112), (string id, \"testCastRecv\")");
		System.out.println("reply event to be received: (id, 223), (string id, \"testReplyCastRecv\")");
		
		try {
			System.out.print("Target session(empty for null): ");
			strTargetSession = br.readLine().trim();
			System.out.print("Target group(empty for null): ");
			strTargetGroup = br.readLine().trim();
			System.out.print("Minimum number of reply events(empty for 0): ");
			strMinNumReplyEvents = br.readLine().trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if(strTargetSession.isEmpty())
			strTargetSession = null;
		if(strTargetGroup.isEmpty())
			strTargetGroup = null;
		if(strMinNumReplyEvents.isEmpty())
			strMinNumReplyEvents = "0";

		try {
			nMinNumReplyEvents = Integer.parseInt(strMinNumReplyEvents);
		}catch(NumberFormatException e) {
			e.printStackTrace();
			System.err.println("Wrong number format!");
			return;
		}
		
		System.out.println("Target session: "+strTargetSession);
		System.out.println("Target group: "+strTargetGroup);
		System.out.println("Minimum number of reply events: "+nMinNumReplyEvents);

		m_eventHandler.setStartTime(System.currentTimeMillis());
		m_eventHandler.setMinNumWaitedEvents(nMinNumReplyEvents);
		m_eventHandler.setRecvReplyEvents(0);
		bRet = m_clientStub.cast(ue, strTargetSession, strTargetGroup);
		
		if(!bRet)
		{
			System.err.println("Error in asynchronous castrecv service!");
			return;
		}
		System.out.println("======");
		
	}
	
	// print group information provided by the default server
	public void testPrintGroupInfo()
	{
		// check local state
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You should join a session and a group.");
			return;
		}
		
		CMSession session = interInfo.findSession(myself.getCurrentSession());
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		System.out.println("---------------------------------------------------------");
		System.out.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port");
		System.out.println("---------------------------------------------------------");
		while(iter.hasNext())
		{
			CMGroupInfo gInfo = iter.next();
			System.out.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort());
		}
		
		return;
	}
	
	public void testCurrentUserStatus()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		System.out.println("------ for the default server");
		System.out.println("name("+myself.getName()+"), session("+myself.getCurrentSession()+"), group("
				+myself.getCurrentGroup()+"), udp port("+myself.getUDPPort()+"), state("
				+myself.getState()+"), attachment download scheme("+confInfo.getAttachDownloadScheme()+").");
		
		// for additional servers
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while(iter.hasNext())
		{
			CMServer tserver = iter.next();
			if(tserver.getNonBlockSocketChannelInfo().findChannel(0) != null)
			{
				System.out.println("------ for additional server["+tserver.getServerName()+"]");
				System.out.println("current session("+tserver.getCurrentSessionName()+
						"), current group("+tserver.getCurrentGroupName()+"), state("
						+tserver.getClientState()+").");
				
			}
		}
		
		return;
	}
	
	public void testChangeGroup()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String strGroupName = null;
		System.out.println("====== change group");
		try {
			System.out.print("input target group name: ");
			strGroupName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_clientStub.changeGroup(strGroupName);
		
		System.out.println("======");
		return;
	}
	
	public void testPrintGroupMembers()
	{
		System.out.print("====== print group members\n");
		CMMember groupMembers = m_clientStub.getGroupMembers();
		CMUser myself = m_clientStub.getMyself();
		System.out.print("My name: "+myself.getName()+"\n");
		if(groupMembers == null || groupMembers.isEmpty())
		{
			System.err.println("No group member yet!");
			return;
		}
		System.out.print(groupMembers.toString()+"\n");
	}
	
	// ServerSocketChannel is not supported.
	// A server cannot add SocketChannel.
	// For the SocketChannel, available server name must be given as well.
	// For the MulticastChannel, session name and group name known by this client/server must be given. 
	public void testAddChannel()
	{
		int nChType = -1;
		int nChKey = -1;
		String strServerName = null;
		String strChAddress = null;
		int nChPort = -1;
		String strSessionName = null;
		String strGroupName = null;
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		boolean bResult = false;
		String strBlock = null;
		boolean isBlock = false;
		SocketChannel sc = null;
		DatagramChannel dc = null;
		String strSync = null;
		boolean isSyncCall = false;
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			CMUser myself = interInfo.getMyself();
			if(myself.getState() != CMInfo.CM_SESSION_JOIN && myself.getState() != CMInfo.CM_LOGIN)
			{
				System.out.println("You should login to the default server.");
				return;
			}
		}
		
		System.out.println("====== add additional channel");

		// ask channel type, (server name), channel index (integer greater than 0), addr, port
		try{
			System.out.print("Select channel type (SocketChannel:2, DatagramChannel:3, MulticastChannel:4): ");
			nChType = m_scan.nextInt();
			if(nChType == CMInfo.CM_SOCKET_CHANNEL)
			{
				System.out.print("is it a blocking channel? (\"y\": yes, \"n\": no): ");
				strBlock = m_scan.next();
				if(strBlock.equals("y")) isBlock = true;
				else if(strBlock.equals("n")) isBlock = false;
				else
				{
					System.err.println("invalid answer! : "+strBlock);
					return;
				}
			
				if(isBlock)
				{
					System.out.print("Channel key(>=0): ");
					nChKey = m_scan.nextInt();
					if(nChKey < 0)
					{
						System.err.println("testAddChannel(), invalid blocking socket channel key ("+nChKey+")!");
						return;
					}
				}
				else
				{
					System.out.print("Channel key(integer greater than 0): ");
					nChKey = m_scan.nextInt();
					if(nChKey <= 0)
					{
						System.err.println("testAddChannel(), invalid nonblocking socket channel key ("+nChKey+")!");
						return;
					}
				}
				
				System.out.print("Is the addition synchronous? (\"y\": yes, \"n\": no): ");
				strSync = m_scan.next();
				if(strSync.equals("y")) isSyncCall = true;
				else if(strSync.equals("n")) isSyncCall =false;
				else
				{
					System.err.println("invalid answer! :" + strSync);
					return;
				}
				
				System.out.print("Server name(\"SERVER\" for the default server): ");
				strServerName = m_scan.next();
			}
			else if(nChType == CMInfo.CM_DATAGRAM_CHANNEL)
			{
				System.out.print("is it a blocking channel? (\"y\": yes, \"n\": no): ");
				strBlock = m_scan.next();
				if(strBlock.equals("y")) isBlock = true;
				else if(strBlock.equals("n")) isBlock = false;
				else
				{
					System.err.println("invalid answer! : "+strBlock);
					return;
				}
			
				if(isBlock)
				{
					System.out.print("Channel udp port: ");
					nChPort = m_scan.nextInt();
					if(nChPort < 0)
					{
						System.err.println("testAddChannel(), invalid blocking datagram channel key ("+nChPort+")!");
						return;
					}
				}
				else
				{
					System.out.print("Channel udp port: ");
					nChPort = m_scan.nextInt();
					if(nChPort <= 0)
					{
						System.err.println("testAddChannel(), invalid nonblocking datagram channel key ("+nChPort+")!");
						return;
					}
				}

			}
			else if(nChType == CMInfo.CM_MULTICAST_CHANNEL)
			{
				System.out.print("Target session name: ");
				strSessionName = m_scan.next();
				System.out.print("Target group name: ");
				strGroupName = m_scan.next();
				System.out.print("Channel multicast address: ");
				strChAddress = m_scan.next();
				System.out.print("Channel multicast port: ");
				nChPort = m_scan.nextInt();
			}
		}catch(InputMismatchException e){
			System.err.println("Invalid input type!");
			m_scan.next();
			return;
		}
					
		switch(nChType)
		{
		case CMInfo.CM_SOCKET_CHANNEL:
			if(isBlock)
			{
				if(isSyncCall)
				{
					sc = m_clientStub.syncAddBlockSocketChannel(nChKey, strServerName);
					if(sc != null)
						System.out.println("Successfully added a blocking socket channel both "
								+ "at the client and the server: key("+nChKey+"), server("+strServerName+")");
					else
						System.err.println("Failed to add a blocking socket channel both at "
								+ "the client and the server: key("+nChKey+"), server("+strServerName+")");					
				}
				else
				{
					bResult = m_clientStub.addBlockSocketChannel(nChKey, strServerName);
					if(bResult)
						System.out.println("Successfully added a blocking socket channel at the client and "
								+"requested to add the channel info to the server: key("+nChKey+"), server("
								+strServerName+")");
					else
						System.err.println("Failed to add a blocking socket channel at the client or "
								+"failed to request to add the channel info to the server: key("+nChKey
								+"), server("+strServerName+")");
					
				}
			}
			else
			{
				if(isSyncCall)
				{
					sc = m_clientStub.syncAddNonBlockSocketChannel(nChKey, strServerName);
					if(sc != null)
						System.out.println("Successfully added a nonblocking socket channel both at the client "
								+ "and the server: key("+nChKey+"), server("+strServerName+")");
					else
						System.err.println("Failed to add a nonblocking socket channel both at the client "
								+ "and the server: key("+nChKey+"), server("+strServerName+")");										
				}
				else
				{
					bResult = m_clientStub.addNonBlockSocketChannel(nChKey, strServerName);
					if(bResult)
						System.out.println("Successfully added a nonblocking socket channel at the client and "
								+ "requested to add the channel info to the server: key("+nChKey+"), server("
								+strServerName+")");
					else
						System.err.println("Failed to add a nonblocking socket channel at the client or "
								+ "failed to request to add the channel info to the server: key("+nChKey
								+"), server("+strServerName+")");					
				}
			}
				
			break;
		case CMInfo.CM_DATAGRAM_CHANNEL:
			if(isBlock)
			{
				dc = m_clientStub.addBlockDatagramChannel(nChPort);
				if(dc != null)
					System.out.println("Successfully added a blocking datagram socket channel: port("+nChPort+")");
				else
					System.err.println("Failed to add a blocking datagram socket channel: port("+nChPort+")");								
			}
			else
			{
				dc = m_clientStub.addNonBlockDatagramChannel(nChPort);
				if(dc != null)
					System.out.println("Successfully added a non-blocking datagram socket channel: port("+nChPort+")");
				else
					System.err.println("Failed to add a non-blocking datagram socket channel: port("+nChPort+")");				
			}
			
			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			bResult = m_clientStub.addMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
			if(bResult)
			{
				System.out.println("Successfully added a multicast channel: session("+strSessionName+"), group("
						+strGroupName+"), address("+strChAddress+"), port("+nChPort+")");
			}
			else
			{
				System.err.println("Failed to add a multicast channel: session("+strSessionName+"), group("
						+strGroupName+"), address("+strChAddress+"), port("+nChPort+")");
			}
			break;
		default:
			System.out.println("Channel type is incorrect!");
			break;
		}
		
		System.out.println("======");
	}
	
	public void testRemoveChannel()
	{
		int nChType = -1;
		int nChKey = -1;
		int nChPort = -1;
		String strChAddress = null;
		String strServerName = null;
		String strSessionName = null;
		String strGroupName = null;
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		boolean result = false;
		String strBlock = null;
		boolean isBlock = false;
		String strSync = null;
		boolean isSyncCall = false;
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			CMUser myself = interInfo.getMyself();
			if(myself.getState() != CMInfo.CM_SESSION_JOIN && myself.getState() != CMInfo.CM_LOGIN)
			{
				System.out.println("You should login to the default server.");
				return;
			}
		}
		
		System.out.println("====== remove additional channel");
		try{
			System.out.print("Select channel type (SocketChannel:2, DatagramChannel:3, MulticastChannel:4): ");
			nChType = m_scan.nextInt();
			if(nChType == CMInfo.CM_SOCKET_CHANNEL)
			{
				System.out.print("is it a blocking channel? (\"y\": yes, \"n\": no): ");
				strBlock = m_scan.next();
				if(strBlock.equals("y")) isBlock = true;
				else if(strBlock.equals("n")) isBlock = false;
				else
				{
					System.err.println("invalid answer! : "+strBlock);
					return;
				}
			
				if(isBlock)
				{
					System.out.print("Channel key(>=0): ");
					nChKey = m_scan.nextInt();
					if(nChKey < 0)
					{
						System.err.println("testRemoveChannel(), invalid socket channel key ("+nChKey+")!");
						return;
					}
					System.out.print("Is the removal synchronous? (\"y\": yes, \"n\": no); ");
					strSync = m_scan.next();
					if(strSync.equals("y")) isSyncCall = true;
					else if(strSync.equals("n")) isSyncCall = false;
					else
					{
						System.err.println("Invalid answer! : "+strSync);
						return;
					}
				}
				else
				{
					System.out.print("Channel key(integer greater than 0): ");
					nChKey = m_scan.nextInt();
					if(nChKey <= 0)
					{
						System.err.println("testRemoveChannel(), invalid socket channel key ("+nChKey+")!");
						return;
					}
				}
				System.out.print("Server name(\"SERVER\" for the default server): ");
				strServerName = m_scan.next();
			}
			else if(nChType ==CMInfo.CM_DATAGRAM_CHANNEL)
			{
				System.out.print("is it a blocking channel? (\"y\": yes, \"n\": no): ");
				strBlock = m_scan.next();
				if(strBlock.equals("y")) isBlock = true;
				else if(strBlock.equals("n")) isBlock = false;
				else
				{
					System.err.println("invalid answer! : "+strBlock);
					return;
				}

				System.out.print("Channel udp port: ");
				nChPort = m_scan.nextInt();			
			}
			else if(nChType == CMInfo.CM_MULTICAST_CHANNEL)
			{
				System.out.print("Target session name: ");
				strSessionName = m_scan.next();
				System.out.print("Target group name: ");
				strGroupName = m_scan.next();
				System.out.print("Multicast address: ");
				strChAddress = m_scan.next();
				System.out.print("Multicast port: ");
				nChPort = m_scan.nextInt();
			}
		}catch(InputMismatchException e){
			System.err.println("Invalid input type!");
			m_scan.next();
			return;
		}

		switch(nChType)
		{
		case CMInfo.CM_SOCKET_CHANNEL:
			if(isBlock)
			{
				if(isSyncCall)
				{
					result = m_clientStub.syncRemoveBlockSocketChannel(nChKey, strServerName);
					if(result)
						System.out.println("Successfully removed a blocking socket channel both "
								+ "at the client and the server: key("+nChKey+"), server ("+strServerName+")");
					else
						System.err.println("Failed to remove a blocking socket channel both at the client "
								+ "and the server: key("+nChKey+"), server ("+strServerName+")");					
				}
				else
				{
					result = m_clientStub.removeBlockSocketChannel(nChKey, strServerName);
					if(result)
						System.out.println("Successfully removed a blocking socket channel at the client and " 
								+ "requested to remove it at the server: key("+nChKey+"), server("+strServerName+")");
					else
						System.err.println("Failed to remove a blocking socket channel at the client or "
								+ "failed to request to remove it at the server: key("+nChKey+"), server("
								+strServerName+")");
				}
			}
			else
			{
				result = m_clientStub.removeNonBlockSocketChannel(nChKey, strServerName);
				if(result)
					System.out.println("Successfully removed a nonblocking socket channel: key("+nChKey
							+"), server("+strServerName+")");
				else
					System.err.println("Failed to remove a nonblocing socket channel: key("+nChKey
							+"), server("+strServerName+")");
			}
			
			break;
		case CMInfo.CM_DATAGRAM_CHANNEL:
			if(isBlock)
			{
				result = m_clientStub.removeBlockDatagramChannel(nChPort);
				if(result)
					System.out.println("Successfully removed a blocking datagram socket channel: port("+nChPort+")");
				else
					System.err.println("Failed to remove a blocking datagram socket channel: port("+nChPort+")");				
			}
			else
			{
				result = m_clientStub.removeNonBlockDatagramChannel(nChPort);
				if(result)
					System.out.println("Successfully removed a non-blocking datagram socket channel: port("+nChPort+")");
				else
					System.err.println("Failed to remove a non-blocking datagram socket channel: port("+nChPort+")");				
			}

			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			result = m_clientStub.removeAdditionalMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
			if(result)
			{
				System.out.println("Successfully removed a multicast channel: session("+strSessionName+"), group("
						+strGroupName+"), address("+strChAddress+"), port("+nChPort+")");
			}
			else
			{
				System.err.println("Failed to remove a multicast channel: session("+strSessionName+"), group("
						+strGroupName+"), address("+strChAddress+"), port("+nChPort+")");
			}
			break;
		default:
			System.out.println("Channel type is incorrect!");
			break;
		}
		
		System.out.println("======");
	}
	
	public void testSetFilePath()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== set file path");
		String strPath = null;
		System.out.print("file path: ");
		try {
			strPath = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.setTransferedFileHome(Paths.get(strPath));
		
		System.out.println("======");
	}
	
	public void testRequestFile()
	{
		boolean bReturn = false;
		String strFileName = null;
		String strFileOwner = null;
		String strFileAppend = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== request a file");
		try {
			System.out.print("File name: ");
			strFileName = br.readLine();
			System.out.print("File owner(enter for \"SERVER\"): ");
			strFileOwner = br.readLine();
			if(strFileOwner.isEmpty())
				strFileOwner = m_clientStub.getDefaultServerName();
			System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
			strFileAppend = br.readLine();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(strFileAppend.isEmpty())
			bReturn = m_clientStub.requestFile(strFileName, strFileOwner);
		else if(strFileAppend.equals("y"))
			bReturn = m_clientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_APPEND);
		else if(strFileAppend.equals("n"))
			bReturn = m_clientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_OVERWRITE);
		else
			System.err.println("wrong input for the file append mode!");
		
		if(!bReturn)
			System.err.println("Request file error! file("+strFileName+"), owner("+strFileOwner+").");
		
		System.out.println("======");
	}
	
	public void testPushFile()
	{
		String strFilePath = null;
		String strReceiver = null;
		String strFileAppend = null;
		boolean bReturn = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== push a file");
		
		try {
			System.out.print("File path name: ");
			strFilePath = br.readLine();
			System.out.print("File receiver (enter for \"SERVER\"): ");
			strReceiver = br.readLine();
			if(strReceiver.isEmpty())
				strReceiver = m_clientStub.getDefaultServerName();
			System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
			strFileAppend = br.readLine();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(strFileAppend.isEmpty())
			bReturn = m_clientStub.pushFile(strFilePath, strReceiver);
		else if(strFileAppend.equals("y"))
			bReturn = m_clientStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_APPEND);
		else if(strFileAppend.equals("n"))
			bReturn = m_clientStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_OVERWRITE);
		else
			System.err.println("wrong input for the file append mode!");
		
		if(!bReturn)
			System.err.println("Push file error! file("+strFilePath+"), receiver("+strReceiver+")");
		
		System.out.println("======");
	}
	
	public void cancelRecvFile()
	{
		String strSender = null;
		boolean bReturn = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== cancel receiving a file");
		
		System.out.print("Input sender name (enter for all senders): ");
		try {
			strSender = br.readLine();
			if(strSender.isEmpty())
				strSender = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bReturn = m_clientStub.cancelPullFile(strSender);
		
		if(bReturn)
		{
			if(strSender == null)
				strSender = "all senders";
			System.out.println("Successfully requested to cancel receiving a file to ["+strSender+"].");
		}
		else
			System.err.println("Request failed to cancel receiving a file to ["+strSender+"]!");
		
		return;
	}
	
	public void cancelSendFile()
	{
		String strReceiver = null;
		boolean bReturn = false;
		System.out.println("====== cancel sending a file");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input receiver name (enter for all receivers): ");
		
		try {
			strReceiver = br.readLine();
			if(strReceiver.isEmpty())
				strReceiver = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bReturn = m_clientStub.cancelPushFile(strReceiver);
		
		if(bReturn)
		{
			if(strReceiver == null)
				strReceiver = "all receivers";
			System.out.println("Successfully requested to cancel sending a file to ["+strReceiver+"].");
		}
		else
			System.err.println("Request failed to cancel sending a file to ["+strReceiver+"]!");
		
		return;
	}
	
	public void printSendRecvFileInfo()
	{
		CMFileTransferInfo fInfo = m_clientStub.getCMInfo().getFileTransferInfo();
		Hashtable<String, CMList<CMSendFileInfo>> sendHashtable = fInfo.getSendFileHashtable();
		Hashtable<String, CMList<CMRecvFileInfo>> recvHashtable = fInfo.getRecvFileHashtable();
		Set<String> sendKeySet = sendHashtable.keySet();
		Set<String> recvKeySet = recvHashtable.keySet();
		
		System.out.print("==== sending file info\n");
		for(String receiver : sendKeySet)
		{
			CMList<CMSendFileInfo> sendList = sendHashtable.get(receiver);
			System.out.print(sendList+"\n");
		}

		System.out.print("==== receiving file info\n");
		for(String sender : recvKeySet)
		{
			CMList<CMRecvFileInfo> recvList = recvHashtable.get(sender);
			System.out.print(recvList+"\n");
		}
	}
	
	public void testForwarding()
	{
		int nForwardType = 0;
		float fForwardRate = 0;
		int nSimNum = 0;
		int nEventTypeNum = 10;
		int nEventRange = -1;
		int nEventID = -1;
		String strUserName = null;
		CMUserEvent ue = null;
		
		int nUserState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();
		if(nUserState != CMInfo.CM_LOGIN && nUserState != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You must log in to the default server.");
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("====== typical/internal forwarding test");
		try {
			System.out.print("Forwarding type (0: typical, 1: internal): ");
			nForwardType = Integer.parseInt(br.readLine());
			System.out.print("Forwarding rate (0 ~ 1): ");
			fForwardRate = Float.parseFloat(br.readLine());
			System.out.print("Simulation num: ");
			nSimNum = Integer.parseInt(br.readLine());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		nEventRange = (int) (nEventTypeNum * fForwardRate); // number of event types which must be forwarded
		strUserName = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
		Random rnd = new Random();
		ue = new CMUserEvent();
		
		for(int i = 0; i < nSimNum; i++)
		{
			for(int j = 0; j < 100; j++)
			{
				ue = new CMUserEvent();
				nEventID = rnd.nextInt(10);	// 0 ~ 9
				if(nEventID >= 0 && nEventID < nEventRange)
					ue.setStringID("testForward");
				else
					ue.setStringID("testNotForward");
				ue.setEventField(CMInfo.CM_INT, "id", String.valueOf(nEventID));
				ue.setEventField(CMInfo.CM_INT, "ftype", String.valueOf(nForwardType));
				ue.setEventField(CMInfo.CM_STR, "user", strUserName);
				
				// send the event to a server
				if(nForwardType == 0)
					m_clientStub.send(ue, m_clientStub.getDefaultServerName());
				else if(nForwardType == 1)
				{
					if(ue.getStringID().equals("testForward"))
						m_clientStub.send(ue, strUserName);
					else
						m_clientStub.send(ue, m_clientStub.getDefaultServerName());
				}
				else
				{
					System.out.println("Invalid forwarding type: "+nForwardType);
					return;
				}
			}
		}
		
		// send an end event to a server (id: EndSim, int: simnum)
		ue = new CMUserEvent();
		ue.setStringID("EndSim");
		ue.setEventField(CMInfo.CM_INT, "simnum", String.valueOf(nSimNum));
		m_clientStub.send(ue, m_clientStub.getDefaultServerName());
		
		ue = null;
		return;
	}
	
	public void testForwardingDelay()
	{
		int nForwardType = 0;
		int nSendNum = 0;
		String strUserName = null;
		long lSendTime = 0;
		CMUserEvent ue = null;

		int nUserState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();
		if(nUserState != CMInfo.CM_LOGIN && nUserState != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You must log in to the default server.");
			return;
		}

		System.out.println("====== test delay of forwarding schemes (typical vs. internal");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("forward type(0:typical, 1:internal): ");
			nForwardType = Integer.parseInt(br.readLine());
			System.out.print("Send num: ");
			nSendNum = Integer.parseInt(br.readLine());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		strUserName = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();

		for(int i=0; i < nSendNum; i++)
		{
			
			// generate a test event
			ue = new CMUserEvent();
			ue.setStringID("testForwardDelay");
			ue.setEventField(CMInfo.CM_INT, "id", String.valueOf(i));
			ue.setEventField(CMInfo.CM_INT, "ftype", String.valueOf(nForwardType));
			ue.setEventField(CMInfo.CM_STR, "user", strUserName);
				
			lSendTime = System.currentTimeMillis();
			ue.setEventField(CMInfo.CM_LONG, "stime", String.valueOf(lSendTime));

			// send an event to a server
			if(nForwardType == 0)
				m_clientStub.send(ue, m_clientStub.getDefaultServerName());
			else if(nForwardType == 1)
			{
				m_clientStub.send(ue, strUserName);
			}
			else
			{
				System.out.println("Invalid forward type: "+nForwardType);
				return;
			}
		}
		
		// send end event to a server (id: EndSim, int: simnum)
		ue = new CMUserEvent();
		ue.setStringID("EndForwardDelay");
		ue.setEventField(CMInfo.CM_INT, "ftype", String.valueOf(nForwardType));
		ue.setEventField(CMInfo.CM_STR, "user", strUserName);
		ue.setEventField(CMInfo.CM_INT, "sendnum", String.valueOf(nSendNum));
		
		if(nForwardType == 0)
			m_clientStub.send(ue, m_clientStub.getDefaultServerName());
		else
			m_clientStub.send(ue, strUserName);
		
		System.out.println("======");
		
		ue = null;
		return;
	}
	
	public void testDownloadNewSNSContent()
	{
		System.out.println("====== request downloading of SNS content (offset 0)");

		String strWriterName = null;
		String strInput = null;
		int nContentOffset = 0;
		String strUserName = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("Input offset(>= 0, Enter for 0): ");
			strInput = br.readLine();
			if(strInput.compareTo("") != 0)
				nContentOffset = Integer.parseInt(strInput);
			System.out.print("Content writer(Enter for no designation, "
					+ "CM_MY_FRIEND for my friends, CM_BI_FRIEND for bi-friends, or specify a name): ");
			strWriterName = br.readLine();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("Input data is not a number!");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		// start time of downloading contents
		m_eventHandler.setStartTime(System.currentTimeMillis());

		m_clientStub.requestSNSContent(strWriterName, nContentOffset);
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("["+strUserName+"] requests content of writer["+strWriterName
					+"] with offset("+nContentOffset+").");
		}

		System.out.println("======");
		return;
	}
	
	public void testRequestAttachedFileOfSNSContent()
	{
		System.out.println("===== Request an attached file of SNS content");
//		int nContentID = 0;
//		String strWriterName = null;
		String strFileName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
//			System.out.print("SNS content ID: ");
//			nContentID = Integer.parseInt(br.readLine());
//			System.out.print("Writer name: ");
//			strWriterName = br.readLine();
			System.out.print("Attached file name: ");
			strFileName = br.readLine();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
//		m_clientStub.requestAttachedFileOfSNSContent(nContentID, strWriterName, strFileName);
		m_clientStub.requestAttachedFileOfSNSContent(strFileName);
		return;
	}
	
	public void testRepeatedSNSContentDownload()
	{
		System.out.println("====== Repeated downloading of SNS content");
		// open a file for writing the access delay and # downloaded contents
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = new FileOutputStream("SNSContentDownload.txt");
			pw = new PrintWriter(fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_eventHandler.setFileOutputStream(fos);
		m_eventHandler.setPrintWriter(pw);
		m_eventHandler.setSimNum(100);

		m_clientStub.requestSNSContent("", 0);	// no specific writer, offset = 0

		return;
	}
	
	// download the next SNS content list
	// if this method is called without any previous download request, it requests the most recent list
	public void testDownloadNextSNSContent()
	{
		System.out.println("===== Request the next SNS content list");
		// start time of downloading contents
		m_eventHandler.setStartTime(System.currentTimeMillis());
		m_clientStub.requestNextSNSContent();
		
		return;
	}

	// download the previous SNS content list
	// if this method is called without any previous download request, it requests the most recent list
	public void testDownloadPreviousSNSContent()
	{
		System.out.println("===== Request the previous SNS content list");
		// start time of downloading contents
		m_eventHandler.setStartTime(System.currentTimeMillis());
		m_clientStub.requestPreviousSNSContent();
		
		return;
	}
	
	public void testSNSContentUpload()
	{
		String strMessage = null;
		int nNumAttachedFiles = 0;
		int nReplyOf = 0;
		int nLevelOfDisclosure = 0;
		ArrayList<String> filePathList = null;
		System.out.println("====== test SNS content upload");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("Input message: ");
			strMessage = br.readLine();
			System.out.print("Number of attached files: ");
			nNumAttachedFiles = Integer.parseInt(br.readLine());
			System.out.print("Content ID to which this content replies (0 for no reply): ");
			nReplyOf = Integer.parseInt(br.readLine());
			System.out.print("Level of Disclosure (0: to everyone, 1: to my followers, 2: to bi-friends, 3: nobody): ");
			nLevelOfDisclosure = Integer.parseInt(br.readLine());

			if(nNumAttachedFiles > 0)
			{
				String strPath = null;
				filePathList = new ArrayList<String>();
				System.out.println("Input path names of attahced files..");
				for(int i = 0; i < nNumAttachedFiles; i++)
				{
					System.out.print(i+": ");
					strPath = br.readLine();
					filePathList.add(strPath);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		String strUser = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
		m_clientStub.requestSNSContentUpload(strUser, strMessage, nNumAttachedFiles, nReplyOf, nLevelOfDisclosure, 
				filePathList);

		return;
	}
	
	public void testRegisterUser()
	{
		String strName = null;
		String strPasswd = null;
		String strRePasswd = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("====== register a user");
		try {
			System.out.print("Input user name: ");
			strName = br.readLine();
			if(console == null)
			{
				System.out.print("Input password: ");
				strPasswd = br.readLine();
				System.out.print("Retype password: ");
				strRePasswd = br.readLine();
			}
			else
			{
				strPasswd = new String(console.readPassword("Input password: "));
				strRePasswd = new String(console.readPassword("Retype password: "));
			}
			
			if(!strPasswd.equals(strRePasswd))
			{
				System.err.println("Password input error");
				return;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_clientStub.registerUser(strName, strPasswd);
		System.out.println("======");
		return;
	}
	
	public void testDeregisterUser()
	{
		String strName = null;
		String strPasswd = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("====== Deregister a user");
		try {
			System.out.print("Input user name: ");
			strName = br.readLine();
			if(console == null)
			{
				System.out.print("Input password: ");
				strPasswd = br.readLine();
			}
			else
			{
				strPasswd = new String(console.readPassword("Input password: "));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.deregisterUser(strName, strPasswd);
		System.out.println("======");
		return;
	}
	
	public void testFindRegisteredUser()
	{
		String strName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== search for a registered user");
		try {
			System.out.print("Input user name: ");
			strName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_clientStub.findRegisteredUser(strName);
		System.out.println("======");
		return;
	}
	
	public void testAddNewFriend()
	{
		String strFriendName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== add a new friend");
		System.out.println("A friend must be a registered user in CM");
		try {
			System.out.print("Input a friend name: ");
			strFriendName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		m_clientStub.addNewFriend(strFriendName);
		return;
	}
	
	public void testRemoveFriend()
	{
		String strFriendName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== remove a friend");
		try {
			System.out.print("Input a friend name: ");
			strFriendName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		m_clientStub.removeFriend(strFriendName);
		return;
	}
	
	public void testRequestFriendsList()
	{
		System.out.println("====== request current friends list");
		m_clientStub.requestFriendsList();
		return;
	}
	
	public void testRequestFriendRequestersList()
	{
		System.out.println("====== request friend requesters list");
		m_clientStub.requestFriendRequestersList();
		return;
	}
	
	public void testRequestBiFriendsList()
	{
		System.out.println("====== request bi-directional friends list");
		m_clientStub.requestBiFriendsList();
		return;
	}
	
	public void testRequestServerInfo()
	{
		System.out.println("====== request additional server information");
		m_clientStub.requestServerInfo();
	}
	
	public void testConnectToServer()
	{
		System.out.println("====== connect to a designated server");
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input a server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.connectToServer(strServerName);
		return;
	}
	
	public void testDisconnectFromServer()
	{
		System.out.println("===== disconnect from a designated server");
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input a server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_clientStub.disconnectFromServer(strServerName);
		return;
	}
	
	public void testLoginServer()
	{
		String strServerName = null;
		String user = null;
		String password = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("====== log in to a designated server");
		try {
			System.out.print("Input server name: ");
			strServerName = br.readLine();
			if( strServerName.equals(m_clientStub.getDefaultServerName()) )	// login to a default server
			{
				System.out.print("User name: ");
				user = br.readLine();
				if(console == null)
				{
					System.out.print("Password: ");
					password = br.readLine();
				}
				else
				{
					password = new String(console.readPassword("Password: "));
				}
				
				m_clientStub.loginCM(user, password);
			}
			else // use the login info for the default server
			{
				CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
				user = myself.getName();
				password = myself.getPasswd();
				m_clientStub.loginCM(strServerName, user, password);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("======");
		return;
	}
	
	public void testLogoutServer()
	{
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== log out from a designated server");
		System.out.print("Input server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.logoutCM(strServerName);
		System.out.println("======");
	}
	
	public void testRequestSessionInfoOfServer()
	{
		String strServerName = null;
		System.out.println("====== request session informatino of a designated server");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_clientStub.requestSessionInfo(strServerName);
		System.out.println("======");
		return;
	}
	
	public void testJoinSessionOfServer()
	{
		String strServerName = null;
		String strSessionName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== join a session of a designated server");
		try {
			System.out.print("Input server name: ");
			strServerName = br.readLine();
			System.out.print("Input session name: ");
			strSessionName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_clientStub.joinSession(strServerName, strSessionName);
		System.out.println("======");
		return;
	}
	
	public void testLeaveSessionOfServer()
	{
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== leave a session of a designated server");
		System.out.print("Input server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_clientStub.leaveSession(strServerName);
		System.out.println("======");
		return;
	}
	
	public void testPrintGroupInfoOfServer()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== print group information a designated server");
		System.out.print("Input server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(strServerName.equals(m_clientStub.getDefaultServerName()))
		{
			testPrintGroupInfo();
			return;
		}
		
		CMServer server = interInfo.findAddServer(strServerName);
		if(server == null)
		{
			System.out.println("server("+strServerName+") not found in the add-server list!");
			return;
		}
		
		CMSession session = server.findSession(myself.getCurrentSession());
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		System.out.println("---------------------------------------------------------");
		System.out.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port");
		System.out.println("---------------------------------------------------------");
		while(iter.hasNext())
		{
			CMGroupInfo gInfo = iter.next();
			System.out.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort());
		}

		return;
	}
	
	public void testSendMultipleFiles()
	{
		String[] strFiles = null;
		String strFileList = null;
		int nMode = -1; // 1: push, 2: pull
		int nFileNum = -1;
		String strTarget = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== pull/push multiple files");
		try {
			System.out.print("Select mode (1: push, 2: pull): ");
			nMode = Integer.parseInt(br.readLine());
			if(nMode == 1)
			{
				System.out.print("Input receiver name: ");
				strTarget = br.readLine();
			}
			else if(nMode == 2)
			{
				System.out.print("Input file owner name: ");
				strTarget = br.readLine();
			}
			else
			{
				System.out.println("Incorrect transmission mode!");
				return;
			}

			System.out.print("Number of files: ");
			nFileNum = Integer.parseInt(br.readLine());
			System.out.print("Input file names separated with space: ");
			strFileList = br.readLine();
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		strFileList.trim();
		strFiles = strFileList.split("\\s+");
		if(strFiles.length != nFileNum)
		{
			System.out.println("The number of files incorrect!");
			return;
		}
		
		for(int i = 0; i < nFileNum; i++)
		{
			switch(nMode)
			{
			case 1: // push
				CMFileTransferManager.pushFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
				break;
			case 2: // pull
				CMFileTransferManager.requestPermitForPullFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
				break;
			}
		}
		
		return;
	}
	
	public void testSplitFile()
	{
		String strSrcFile = null;
		String strSplitFile = null;
		long lFileSize = -1;
		long lFileOffset = 0;
		long lSplitSize = -1;
		long lSplitRemainder = -1;
		int nSplitNum = -1;
		RandomAccessFile raf = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("====== split a file");
		try {
			System.out.print("Input source file name: ");
			strSrcFile = br.readLine();
			System.out.print("Input the number of splitted files: ");
			nSplitNum = Integer.parseInt(br.readLine());
			raf = new RandomAccessFile(strSrcFile, "r");
			lFileSize = raf.length();

			lSplitSize = lFileSize / nSplitNum;
			lSplitRemainder = lFileSize % lSplitSize;
			
			for(int i = 0; i < nSplitNum; i++)
			{
				// get the name of split file ('srcfile'-i.split)
				int index = strSrcFile.lastIndexOf(".");
				strSplitFile = strSrcFile.substring(0, index)+"-"+(i+1)+".split";
				
				// update offset
				lFileOffset = i*lSplitSize;
				
				if(i+1 != nSplitNum)
					CMFileTransferManager.splitFile(raf, lFileOffset, lSplitSize, strSplitFile);
				else
					CMFileTransferManager.splitFile(raf, lFileOffset, lSplitSize+lSplitRemainder, strSplitFile);
				
			}
			
			raf.close();
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		return;
	}
	
	public void testMergeFiles()
	{
		String[] strFiles = null;
		//String strFileList = null;
		String strFilePrefix = null;
		String strMergeFileName = null;
		int nFileNum = -1;
		long lMergeFileSize = -1;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("====== merge split files");
		try {
			System.out.print("Number of split files: ");
			nFileNum = Integer.parseInt(br.readLine());
			//System.out.print("Input split files in order: ");
			//strFileList = br.readLine();
			System.out.print("Input prefix of split files: ");
			strFilePrefix = br.readLine();
			System.out.print("Input merged file name: ");
			strMergeFileName = br.readLine();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		/*
		strFileList.trim();
		strFiles = strFileList.split("\\s+");
		if(nFileNum != strFiles.length)
		{
			System.out.println("Wrong number of input files!");
			return;
		}
		*/
		
		// make list of split file names
		strFiles = new String[nFileNum];
		for(int i = 0; i < nFileNum; i++)
		{
			strFiles[i] = strFilePrefix + "-" + (i+1) + ".split";
		}
		
		lMergeFileSize = CMFileTransferManager.mergeFiles(strFiles, nFileNum, strMergeFileName);
		System.out.println("Size of merged file("+strMergeFileName+"): "+lMergeFileSize+" Bytes.");
		return;
	}
	
	public void testDistFileProc()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		//CMFileTransferInfo fileInfo = m_clientStub.getCMInfo().getFileTransferInfo();
		String strFile = null;
		long lFileSize = 0;
		CMFileEvent fe = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("====== split a file, distribute to multiple servers, and merge");
		
		// check if the client logs in to all available servers
		int nClientState = interInfo.getMyself().getState();
		if(nClientState == CMInfo.CM_INIT || nClientState == CMInfo.CM_CONNECT)
		{
			System.out.println("You must log in the default server!");
			return;
		}
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while(iter.hasNext())
		{
			CMServer tserver = iter.next();
			nClientState = tserver.getClientState();
			if(nClientState == CMInfo.CM_INIT || nClientState == CMInfo.CM_CONNECT)
			{
				System.out.println("You must log in the additional server("+tserver.getServerName()
						+")!");
				return;
			}
		}

		// input file name
		try {
			//System.out.println("A source file must exists in the file path configured in CM");
			System.out.print("Input a source file path: ");
			strFile = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// print the file size
		//strFile = fileInfo.getFilePath()+"/"+strFile;
		File srcFile = new File(strFile);
		lFileSize = srcFile.length();
		System.out.println("Source file ("+strFile+"): "+lFileSize+" Bytes.");

		// get current number of servers ( default server + add servers )
		m_eventHandler.setCurrentServerNum(interInfo.getAddServerList().size() + 1);
		String[] filePieces = new String[interInfo.getAddServerList().size()+1];
		m_eventHandler.setFilePieces(filePieces);
		
		// initialize the number of modified pieces
		m_eventHandler.setRecvPieceNum(0);

		// set m_bDistSendRecv to true
		m_eventHandler.setDistFileProc(true);

		// set send time
		m_eventHandler.setStartTime(System.currentTimeMillis());

		// extract the extension of the file
		String strPrefix = null;
		String strExt = null;
		int index = strFile.lastIndexOf(".");
		strPrefix = strFile.substring(0, index);
		strExt = strFile.substring(index+1);
		m_eventHandler.setFileExtension(strExt);
		System.out.println("Source file extension: "+m_eventHandler.getFileExtension());

		// split a file into pieces with the number of servers. each piece has the name of 'file name'-x.split
		// and send each piece to different server
		long lPieceSize = lFileSize / m_eventHandler.getCurrentServerNum();
		int i = 0;
		String strPieceName = null;
		long lOffset = 0;
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(strFile, "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// make a file event (REQUEST_DIST_FILE_PROC)
		fe = new CMFileEvent();
		fe.setID(CMFileEvent.REQUEST_DIST_FILE_PROC);
		//fe.setFileReceiver(interInfo.getMyself().getName());
		fe.setFileSender(interInfo.getMyself().getName());

		// for pieces except the last piece
		for( i = 0; i < m_eventHandler.getCurrentServerNum()-1; i++)
		{
			// get the piece name
			strPieceName = strPrefix+"-"+(i+1)+".split";
			System.out.println("File piece name: "+strPieceName);

			// split the file with a piece
			CMFileTransferManager.splitFile(raf, lOffset, lPieceSize, strPieceName);
			// update offset
			lOffset += lPieceSize;

			// send piece to the corresponding additional server
			String strAddServer = interInfo.getAddServerList().elementAt(i).getServerName();
			fe.setFileReceiver(strAddServer);
			
			m_clientStub.send(fe, strAddServer);
			
			CMFileTransferManager.pushFile(strPieceName, strAddServer, m_clientStub.getCMInfo());
		}
		// for the last piece
		if( i == 0 )
		{
			// no split
			strPieceName = strFile;
		}
		else
		{
			// get the last piece name
			strPieceName = strPrefix+"-"+(i+1)+".split";
			System.out.println("File piece name: "+strPieceName);

			// get the last piece
			CMFileTransferManager.splitFile(raf, lOffset, lFileSize-lPieceSize*i, strPieceName);
		}
		// send the last piece to the default server
		fe.setFileReceiver(m_clientStub.getDefaultServerName());
		m_clientStub.send(fe, m_clientStub.getDefaultServerName());
		
		CMFileTransferManager.pushFile(strPieceName, m_clientStub.getDefaultServerName(), 
				m_clientStub.getCMInfo());
		
		try {
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// The next process proceeds when a modified piece is transferred from a server.

		// Whenever a modified piece(m-'file name'-x.split) is transferred, if m_bDistSendRecv is true, 
		// increase the number of pieces and its name is stored in an array.
		// When all modified pieces arrive, they are merged to a file (m-'file name').
		// After the file is merged, set the received time, calculate the elapsed time, set m_bDistSendRecv to false
		// and print the result.

		fe = null;
		return;
	}
	
	public void testMulticastChat()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		System.out.println("====== test multicast chat in current group");

		// check user state
		CMUser myself = interInfo.getMyself();
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You must join a session and a group for multicasting.");
			return;
		}

		// check communication architecture
		if(!confInfo.getCommArch().equals("CM_PS"))
		{
			System.out.println("CM must start with CM_PS mode which enables multicast per group!");
			return;
		}

		// receive a user input message
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input message: ");
		String strMessage = null;
		try {
			strMessage = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// make a CMInterestEvent.USER_TALK event
		CMInterestEvent ie = new CMInterestEvent();
		ie.setID(CMInterestEvent.USER_TALK);
		ie.setHandlerSession(myself.getCurrentSession());
		ie.setHandlerGroup(myself.getCurrentGroup());
		ie.setUserName(myself.getName());
		ie.setTalk(strMessage);
		
		m_clientStub.multicast(ie, myself.getCurrentSession(), myself.getCurrentGroup());

		ie = null;
		return;
	}
	
	public void testBlockingChannel()
	{
		int nChKey = -1;
		int nRecvPort = -1;
		String strServerName = null;
		SocketChannel sc = null;
		DatagramChannel dc = null;
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		boolean isSocketChannel = false;
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			CMUser myself = interInfo.getMyself();
			if(myself.getState() != CMInfo.CM_SESSION_JOIN && myself.getState() != CMInfo.CM_LOGIN)
			{
				System.err.println("You should login to the default server.");
				return;
			}
		}
		
		System.out.println("============= test blocking channel");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			System.out.print("Do you want to use a socket channel? (\"y\" or enter: yes, \"n\": no): ");
			String strIsSocketChannel = br.readLine();
			if(strIsSocketChannel.isEmpty() || strIsSocketChannel.equals("y") || strIsSocketChannel.equals("yes"))
			{
				isSocketChannel = true;
			}
			System.out.print("Channel key (>=0 or sender port for datagram channel): ");
			String strChKey = br.readLine();
			nChKey = Integer.parseInt(strChKey);
			System.out.print("Server name(empty for the default server): ");
			strServerName = br.readLine();
			if(strServerName.isEmpty()) strServerName = m_clientStub.getDefaultServerName();
			if(!isSocketChannel)
			{
				System.out.print("receiver port (only for datagram channel): ");
				String strRecvPort = br.readLine();
				nRecvPort = Integer.parseInt(strRecvPort);				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (NumberFormatException ne) {
			ne.printStackTrace();
			return;
		}
		
		if(isSocketChannel)
		{
			sc = m_clientStub.getBlockSocketChannel(nChKey, strServerName);
			if(sc == null)
			{
				System.err.println("Blocking socket channel not found: key("+nChKey+"), server("+strServerName+")");
				return;
			}
			System.out.println("Blocking socket channel found: key("+nChKey+"), server("+strServerName+")");
		}
		else
		{
			dc = m_clientStub.getBlockDatagramChannel(nChKey);
			if(dc == null)
			{
				System.err.println("Blocking datagram channel not found: key("+nChKey+")");
				return;
			}
			System.out.println("Blocking datagram channel found: key("+nChKey+")");
		}
		
		CMUserEvent ue = new CMUserEvent();
		ue.setStringID("reqRecv");
		ue.setEventField(CMInfo.CM_STR, "user", m_clientStub.getMyself().getName());
		if(isSocketChannel)
			ue.setEventField(CMInfo.CM_INT, "chType", Integer.toString(CMInfo.CM_SOCKET_CHANNEL));
		else
			ue.setEventField(CMInfo.CM_INT, "chType", Integer.toString(CMInfo.CM_DATAGRAM_CHANNEL));
		
		ue.setEventField(CMInfo.CM_INT, "chKey", Integer.toString(nChKey));
		ue.setEventField(CMInfo.CM_INT, "recvPort", Integer.toString(nRecvPort));
		m_clientStub.send(ue, strServerName);
		
		return;		
	}
	
	public void testMeasureInputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		System.out.println("========== test input network throughput");
		System.out.print("target node (\"SERVER\" for the default server): ");
		strTarget = m_scan.next();
		fSpeed = m_clientStub.measureInputThroughput(strTarget);
		if(fSpeed == -1)
			System.err.println("Test failed!");
		else
			System.out.format("Input network throughput from [%s] : %.2f%n", strTarget, fSpeed);
	}
	
	public void testMeasureOutputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		System.out.println("========== test output network throughput");
		System.out.print("target node (\"SERVER\" for the default server): ");
		strTarget = m_scan.next();
		fSpeed = m_clientStub.measureOutputThroughput(strTarget);
		if(fSpeed == -1)
			System.err.println("Test failed!");
		else
			System.out.format("Output network throughput to [%s] : %.2f%n", strTarget, fSpeed);
	}

	public void testPrintCurrentChannelInfo()
	{
		System.out.println("========== print current channel info");
		String strChannels = m_clientStub.getCurrentChannelInfo();
		System.out.println(strChannels);
	}

	public void testPrintConfigurations()
	{
		String[] strConfigurations;
		System.out.print("========== print all current configurations\n");
		Path confPath = m_clientStub.getConfigurationHome().resolve("cm-client.conf");
		strConfigurations = CMConfigurator.getConfigurations(confPath.toString());
		
		System.out.print("configuration file path: "+confPath.toString()+"\n");
		for(String strConf : strConfigurations)
		{
			String[] strFieldValuePair;
			strFieldValuePair = strConf.split("\\s+");
			System.out.print(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
		}
		
	}
	
	public void testChangeConfiguration()
	{
		boolean bRet = false;
		String strField = null;
		String strValue = null;
		System.out.println("========== change configuration");
		Path confPath = m_clientStub.getConfigurationHome().resolve("cm-client.conf");
		
		System.out.print("Field name: ");
		strField = m_scan.next();
		System.out.print("Value: ");
		strValue = m_scan.next();
		
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), strField, strValue);
		if(bRet)
		{
			System.out.println("cm-client.conf file is successfully updated: ("+strField+"="+strValue+")");
		}
		else
		{
			System.err.println("The configuration change is failed!: ("+strField+"="+strValue+")");
		}
		
		return;
	}
	
	public void testMqttConnect()
	{
		System.out.println("========== MQTT connect");

		String strWillTopic = null;
		String strWillMessage = null;
		boolean bWillRetain = false;
		byte willQoS = (byte)0;
		boolean bWillFlag = false;
		boolean bCleanSession = false;
		
		boolean bDetail = false;
		System.out.print("Need all parameters? (\"y\" or \"n\", Enter for no): ");
		String strDetail = m_scan.nextLine().trim();
		if(strDetail.contentEquals("y"))
			bDetail = true;
		
		if(bDetail)
		{
			System.out.print("Will Topic (Enter for empty string): ");
			strWillTopic = m_scan.nextLine().trim();
			System.out.print("Will Message Enter for empty string): ");
			strWillMessage = m_scan.nextLine().trim();
			System.out.print("Will Retain Flag (\"true\" or \"false\", Enter for false): ");
			String strWillRetain = m_scan.nextLine().trim();
			if(strWillRetain.contentEquals("true"))
				bWillRetain = true;
			System.out.print("Will QoS (0,1, or 2, Enter for 0): ");
			String strWillQoS = m_scan.nextLine().trim();
			if(strWillQoS.contentEquals("1") || strWillQoS.contentEquals("2"))
				willQoS = Byte.parseByte(strWillQoS);
			else if(!strWillQoS.contentEquals("0") && !strWillQoS.isEmpty())
			{
				System.err.println("Wrong QoS! QoS is set to 0!");
			}
			System.out.print("Will Flag (\"true\" or \"false\", Enter for false): ");
			String strWillFlag = m_scan.nextLine().trim();
			if(strWillFlag.contentEquals("true"))
				bWillFlag = true;
			System.out.print("Clean Session Flag (\"true\" or \"false\", Enter for false): ");
			String strCleanSession = m_scan.nextLine().trim();
			if(strCleanSession.contentEquals("true"))
				bCleanSession = true;			
		}
		
		CMMqttManager mqttManager = (CMMqttManager) m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		
		if(bDetail)
		{
			mqttManager.connect(strWillTopic, strWillMessage, bWillRetain, willQoS, bWillFlag, 
					bCleanSession);
		}
		else {
			mqttManager.connect();
		}

	}
	
	public void testMqttPublish()
	{
		System.out.println("========== MQTT publish");
		
		String strTopic = null;
		String strMessage = null;
		byte qos = (byte)0;
		boolean bDupFlag = false;
		boolean bRetainFlag = false;
		
		boolean bDetail = false;
		System.out.print("Need all parameters? (\"y\" or \"n\", Enter for no): ");
		String strDetail = m_scan.nextLine().trim();
		if(strDetail.contentEquals("y"))
			bDetail = true;

		System.out.print("Topic Name: ");
		strTopic = m_scan.nextLine().trim();
		System.out.print("Application Message: ");
		strMessage = m_scan.nextLine().trim();
		
		if(bDetail)
		{
			System.out.print("QoS (0,1, or 2, Enter for 0): ");
			String strQoS = m_scan.nextLine().trim();
			if(strQoS.contentEquals("1") || strQoS.contentEquals("2"))
				qos = Byte.parseByte(strQoS);
			else if(!strQoS.contentEquals("0") && !strQoS.isEmpty())
			{
				System.err.println("Wrong QoS! QoS is set to 0!");
			}
			System.out.print("DUP Flag (\"true\" or \"false\", Enter for false): ");
			String strDupFlag = m_scan.nextLine().trim();
			if(strDupFlag.contentEquals("true"))
				bDupFlag = true;
			System.out.print("Retain Flag (\"true\" or \"false\", Enter for false): ");
			String strRetainFlag = m_scan.nextLine().trim();
			if(strRetainFlag.contentEquals("true"))
				bRetainFlag = true;
		}
		
		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		
		if(bDetail)
		{
			mqttManager.publish(strTopic, strMessage, qos, bDupFlag, bRetainFlag);			
		}
		else
		{
			mqttManager.publish(strTopic, strMessage);
		}

	}
	
	public void testMqttSubscribe()
	{
		System.out.println("========== MQTT subscribe");
		String strTopicFilter;
		byte qos = (byte)0;
		
		System.out.print("Topic Filter: ");
		strTopicFilter = m_scan.nextLine().trim();
		String strQoS = null;
		strQoS = m_scan.nextLine().trim();
		if(strQoS.contentEquals("1") || strQoS.contentEquals("2"))
			qos = Byte.parseByte(strQoS);
		else if(!strQoS.contentEquals("0") && !strQoS.isEmpty())
		{
			System.err.println("Wrong QoS! QoS is set to 0!");
		}
		
		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		mqttManager.subscribe(strTopicFilter, qos);

	}
	
	public void testPrintMqttSessionInfo()
	{
		System.out.println("========== print MQTT session info");
		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		System.out.println(mqttManager.getMySessionInfo());
		
	}
	
	public void testMqttUnsubscribe()
	{
		System.out.println("========== MQTT unsubscribe");
		String strTopic = null;
		System.out.print("Topic to unsubscribe: ");
		strTopic = m_scan.nextLine().trim();
		if(strTopic == null || strTopic.isEmpty())
			return;

		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		mqttManager.unsubscribe(strTopic);

	}
	
	public void testMqttDisconnect()
	{
		System.out.println("========== MQTT disconnect");
		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		mqttManager.disconnect();

	}
	
	public void testSendEventWithWrongByteNum()
	{
		System.out.println("========== send a CMDummyEvent with wrong # bytes to default server");
		
		CMCommInfo commInfo = m_clientStub.getCMInfo().getCommInfo();
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMBlockingEventQueue sendQueue = commInfo.getSendBlockingEventQueue();

		String strServer = JOptionPane.showInputDialog("server name: ").trim();
		SelectableChannel ch = null;
		if(strServer.contentEquals(m_clientStub.getDefaultServerName()))
		{
			CMServer defServer = interInfo.getDefaultServerInfo();
			ch = defServer.getNonBlockSocketChannelInfo().findChannel(0);
		}
		else {
			CMServer addServer = interInfo.findAddServer(strServer);
			if(addServer == null)
			{
				System.err.println("No server["+strServer+"] found!");
				return;
			}
			ch = addServer.getNonBlockSocketChannelInfo().findChannel(0);
		}		
		
		CMDummyEvent due = new CMDummyEvent();
		ByteBuffer buf = due.marshall();
		buf.clear();
		buf.putInt(-1).clear();
		CMMessage msg = new CMMessage(buf, ch);
		sendQueue.push(msg);
	}
	
	public void testSendEventWithWrongEventType()
	{
		System.out.println("========== send a CMDummyEvent with wrong event type");
		
		String strServer = JOptionPane.showInputDialog("server name: ").trim();

		CMDummyEvent due = new CMDummyEvent();
		due.setType(-1);	// set wrong event type
		m_clientStub.send(due, strServer);
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CMClientApp client = new CMClientApp();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setAppEventHandler(client.getClientEventHandler());
		client.testStartCM();
		
		System.out.println("Client application is terminated.");
	}

}
