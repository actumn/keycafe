import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSUserAccessSimulator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JOptionPane;

public class CMServerApp {
	private CMServerStub m_serverStub;
	private CMServerEventHandler m_eventHandler;
	private boolean m_bRun;
	private CMSNSUserAccessSimulator m_uaSim;
	private Scanner m_scan = null;
	
	public CMServerApp()
	{
		m_serverStub = new CMServerStub();
		m_eventHandler = new CMServerEventHandler(m_serverStub);
		m_bRun = true;
		m_uaSim = new CMSNSUserAccessSimulator();
	}
	
	public CMServerStub getServerStub()
	{
		return m_serverStub;
	}
	
	public CMServerEventHandler getServerEventHandler()
	{
		return m_eventHandler;
	}
	
	///////////////////////////////////////////////////////////////
	// test methods
	public void startTest()
	{
		System.out.println("Server application starts.");
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
				startCM();
				break;
			case 999:
				terminateCM();
				return;
			case 1: // print session information
				printSessionInfo();
				break;
			case 2: // print selected group information
				printGroupInfo();
				break;
			case 3:	// test input network throughput
				measureInputThroughput();
				break;
			case 4:	// test output network throughput
				measureOutputThroughput();
				break;
			case 5:	// print current channels information
				printCurrentChannelInfo();
				break;
			case 6: // print current login users
				printLoginUsers();
				break;
			case 7: // print all current configurations
				printConfigurations();
				break;
			case 8: // change a field value in the configuration file
				changeConfiguration();
				break;
			case 20: // set file path
				setFilePath();
				break;
			case 21: // request a file
				requestFile();
				break;
			case 22: // push a file
				pushFile();
				break;
			case 23:	// test cancel receiving a file
				cancelRecvFile();
				break;
			case 24:	// test cancel sending a file
				cancelSendFile();
				break;
			case 25:	// print sending/receiving file info
				printSendRecvFileInfo();
				break;
			case 30: // request registration to the default server
				requestServerReg();
				break;
			case 31: // request deregistration from the default server
				requestServerDereg();
				break;
			case 32: // connect to the default server
				connectToDefaultServer();
				break;
			case 33: // disconnect from the default server
				disconnectFromDefaultServer();
				break;
			case 40: // set a scheme for attachement download of SNS content
				setAttachDownloadScheme();
				break;
			case 50: 	// test add channel
				addChannel();
				break;
			case 51: 	// test remove channel
				removeChannel();
				break;	
			case 60:	// find session info
				findMqttSessionInfo();
				break;
			case 61:	// print all session info
				printAllMqttSessionInfo();
				break;
			case 62:	// print all retain info
				printAllMqttRetainInfo();
				break;
			case 101:	// configure variables of user access simulation
				configureUserAccessSimulation();
				break;
			case 102: 	// start user access simulation
				startUserAccessSimulation();
				break;
			case 103:	// start user access simulation and calculate prefetch precision and recall
				startUserAccessSimulationAndCalPrecRecall();
				break;
			case 104: 	// configure, simulate and write recent history to CMDB
				writeRecentAccHistoryToDB();
				break;
			case 105:	// send event with wrong # bytes
				sendEventWithWrongByteNum();
				break;
			case 106:	// send event with wrong type
				sendEventWithWrongEventType();
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
		System.out.print("---------------------------------- Help\n");
		System.out.print("0: show all menus\n");
		System.out.print("---------------------------------- Start/Stop\n");
		System.out.print("100: strat CM, 999: terminate CM\n");
		System.out.print("---------------------------------- Information\n");
		System.out.print("1: show session information, 2: show group information\n");
		System.out.print("3: test input network throughput, 4: test output network throughput\n");
		System.out.print("5: show current channels, 6: show login users\n");
		System.out.print("7: show all configurations, 8: change configuration\n");
		System.out.print("---------------------------------- File Transfer\n");
		System.out.print("20: set file path, 21: request file, 22: push file\n");
		System.out.print("23: cancel receiving file, 24: cancel sending file\n");
		System.out.print("25: print sending/receiving file info\n");
		System.out.print("---------------------------------- Multi-server\n");
		System.out.print("30: register to default server, 31: deregister from default server\n");
		System.out.print("32: connect to default server, 33: disconnect from default server\n");
		System.out.print("---------------------------------- Social Network Service\n");
		System.out.print("40: set attachment download scheme\n");
		System.out.print("---------------------------------- Channel\n");
		System.out.print("50: add channel, 51: remove channel\n");
		System.out.print("---------------------------------- MQTT\n");
		System.out.print("60: find session info, 61: print all session info, 62: print all retain info\n");
		System.out.print("---------------------------------- Other CM Tests\n");
		System.out.print("101: configure SNS user access simulation, 102: start SNS user access simulation\n");
		System.out.print("103: start SNS user access simulation and measure prefetch accuracy\n");
		System.out.print("104: start and write recent SNS access history simulation to CM DB\n");
		System.out.print("105: send event with wrong bytes, 106: send event with wrong type\n");
	}
	
	public void startCM()
	{
		// get current server info from the server configuration file
		String strSavedServerAddress = null;
		String strCurServerAddress = null;
		int nSavedServerPort = -1;
		String strNewServerAddress = null;
		String strNewServerPort = null;
		int nNewServerPort = -1;
		
		strSavedServerAddress = m_serverStub.getServerAddress();
		strCurServerAddress = CMCommManager.getLocalIP();
		nSavedServerPort = m_serverStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("========== start CM");
		System.out.println("detected server address: "+strCurServerAddress);
		System.out.println("saved server port: "+nSavedServerPort);
		
		try {
			System.out.print("new server address (enter for detected value): ");
			strNewServerAddress = br.readLine().trim();
			if(strNewServerAddress.isEmpty()) strNewServerAddress = strCurServerAddress;

			System.out.print("new server port (enter for saved value): ");
			strNewServerPort = br.readLine().trim();
			try {
				if(strNewServerPort.isEmpty()) 
					nNewServerPort = nSavedServerPort;
				else
					nNewServerPort = Integer.parseInt(strNewServerPort);				
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}
			
			// update the server info if the user would like to do
			if(!strNewServerAddress.equals(strSavedServerAddress))
				m_serverStub.setServerAddress(strNewServerAddress);
			if(nNewServerPort != nSavedServerPort)
				m_serverStub.setServerPort(Integer.parseInt(strNewServerPort));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		boolean bRet = m_serverStub.startCM();
		if(!bRet)
		{
			System.err.println("CM initialization error!");
			return;
		}
		startTest();
	}
	
	public void terminateCM()
	{
		m_serverStub.terminateCM();
		m_bRun = false;
	}
	
	public void printSessionInfo()
	{
		System.out.println("------------------------------------------------------");
		System.out.format("%-20s%-20s%-10s%-10s%n", "session name", "session addr", "port", "#users");
		System.out.println("------------------------------------------------------");
		
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		Iterator<CMSession> iter = interInfo.getSessionList().iterator();
		while(iter.hasNext())
		{
			CMSession session = iter.next();
			System.out.format("%-20s%-20s%-10d%-10d%n", session.getSessionName(), session.getAddress()
					, session.getPort(), session.getSessionUsers().getMemberNum());
		}
		return;
	}
	
	public void printGroupInfo()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String strSessionName = null;
		
		System.out.println("====== print group information");
		System.out.print("Session name: ");
		try {
			strSessionName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		CMSession session = interInfo.findSession(strSessionName);
		if(session == null)
		{
			System.out.println("Session("+strSessionName+") not found.");
			return;
		}
		
		System.out.println("------------------------------------------------------------------");
		System.out.format("%-20s%-20s%-10s%-10s%n", "group name", "multicast addr", "port", "#users");
		System.out.println("------------------------------------------------------------------");

		Iterator<CMGroup> iter = session.getGroupList().iterator();
		while(iter.hasNext())
		{
			CMGroup gInfo = iter.next();
			System.out.format("%-20s%-20s%-10d%-10d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort(), gInfo.getGroupUsers().getMemberNum());
		}

		System.out.println("======");
		return;
	}
	
	public void setFilePath()
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
		
		m_serverStub.setTransferedFileHome(Paths.get(strPath));
		
		System.out.println("======");
	}
	
	public void requestFile()
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
			System.out.print("File owner(user name): ");
			strFileOwner = br.readLine();
			System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
			strFileAppend = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(strFileAppend.isEmpty())
			bReturn = m_serverStub.requestFile(strFileName, strFileOwner);
		else if(strFileAppend.equals("y"))
			bReturn = m_serverStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_APPEND);
		else if(strFileAppend.equals("n"))
			bReturn = m_serverStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_OVERWRITE);
		else
			System.err.println("wrong input for the file append mode!");
		
		if(!bReturn)
			System.err.println("Request file error! file("+strFileName+"), owner("+strFileOwner+").");

		System.out.println("======");
	}
	
	public void pushFile()
	{
		boolean bReturn = false;
		String strFilePath = null;
		String strReceiver = null;
		String strFileAppend = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== push a file");
		
		try {
			System.out.print("File path name: ");
			strFilePath = br.readLine();
			System.out.print("File receiver (user name): ");
			strReceiver = br.readLine();
			System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
			strFileAppend = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(strFileAppend.isEmpty())
			bReturn = m_serverStub.pushFile(strFilePath, strReceiver);
		else if(strFileAppend.equals("y"))
			bReturn = m_serverStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_APPEND);
		else if(strFileAppend.equals("n"))
			bReturn = m_serverStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_OVERWRITE);
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

		bReturn = m_serverStub.cancelPullFile(strSender);
		
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
		
		bReturn = m_serverStub.cancelPushFile(strReceiver);
		
		if(bReturn)
			System.out.println("Successfully requested to cancel sending a file to ["+strReceiver+"]");
		else
			System.err.println("Request failed to cancel sending a file to ["+strReceiver+"]!");
		
		return;
	}
	
	public void printSendRecvFileInfo()
	{
		CMFileTransferInfo fInfo = m_serverStub.getCMInfo().getFileTransferInfo();
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
	
	public void requestServerReg()
	{
		String strServerName = null;
		System.out.println("====== request registration to the default server");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Enter registered server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_serverStub.requestServerReg(strServerName);
		System.out.println("======");
		return;
	}
	
	public void requestServerDereg()
	{
		System.out.println("====== request deregistration from the default server");
		m_serverStub.requestServerDereg();
		System.out.println("======");
		return;
	}
	
	public void connectToDefaultServer()
	{
		System.out.println("====== connect to the default server");
		m_serverStub.connectToServer();
		System.out.println("======");
		return;
	}
	
	public void disconnectFromDefaultServer()
	{
		System.out.println("====== disconnect from the default server");
		m_serverStub.disconnectFromServer();
		System.out.println("======");
		return;
	}
	
	public void setAttachDownloadScheme()
	{
		String strUserName = null;
		int nScheme;
		System.out.println("====== set a scheme for attachement download of SNS content");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("Input target user name(Enter for all users): ");
			strUserName = br.readLine();
			if(strUserName.isEmpty())
				strUserName = null;
			System.out.println("0: full download, 1: partial(thumbnail file) download, "
					+ "2: prefetching download, 3: none (only file name)");
			System.out.print("Enter scheme number: ");
			nScheme = Integer.parseInt(br.readLine());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		m_serverStub.setAttachDownloadScheme(strUserName, nScheme);
		return;
	}
	
	public void configureUserAccessSimulation()
	{
		int nUserNum = -1;
		int nAvgDayAccCount = -1;
		int nTotalSimDays = -1;
		int nAccPattern = -1;
		double dNormalMean = -1.0;
		double dNormalSD = -1.0;
		String strInput = null;
		
		// retrieve current values
		nUserNum = m_uaSim.getUserNum();
		nAvgDayAccCount = m_uaSim.getAvgDayAccCount();
		nTotalSimDays = m_uaSim.getTotalSimDays();
		nAccPattern = m_uaSim.getAccPattern();
		dNormalMean = m_uaSim.getNormalMean();
		dNormalSD = m_uaSim.getNormalSD();
		
		System.out.println("====== Configure variables of user access simulation");
		System.out.println("The value in () is the current value.");
		System.out.println("Enter in each variable to keep the current value.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("Number of users("+nUserNum+"): ");
			strInput = br.readLine();
			if(!strInput.isEmpty())
			{
				nUserNum = Integer.parseInt(strInput);
				m_uaSim.setUserNum(nUserNum);
			}
			System.out.print("Average daily access count("+nAvgDayAccCount+"): ");
			strInput = br.readLine();
			if(!strInput.isEmpty())
			{
				nAvgDayAccCount = Integer.parseInt(strInput);
				m_uaSim.setAvgDayAccCount(nAvgDayAccCount);
			}
			System.out.print("Total number of simulation days("+nTotalSimDays+"): ");
			strInput = br.readLine();
			if(!strInput.isEmpty())
			{
				nTotalSimDays = Integer.parseInt(strInput);
				m_uaSim.setTotalSimDays(nTotalSimDays);
			}
			System.out.print("Access pattern("+nAccPattern+") (0: random, 1: skewed): ");
			strInput = br.readLine();
			if(!strInput.isEmpty())
			{
				nAccPattern = Integer.parseInt(strInput);
				if(nAccPattern < 0 || nAccPattern > 1)
				{
					System.err.println("Invalid access pattern!");
					return;
				}
				m_uaSim.setAccPattern(nAccPattern);
			}
			
			if(nAccPattern == 1) // skewed access pattern
			{
				System.out.print("Mean value("+dNormalMean+"): ");
				strInput = br.readLine();
				if(!strInput.isEmpty())
				{
					dNormalMean = Double.parseDouble(strInput);
					m_uaSim.setNormalMean(dNormalMean);
				}
				System.out.println("Standard deviation("+dNormalSD+"): ");
				strInput = br.readLine();
				if(!strInput.isEmpty())
				{
					dNormalSD = Double.parseDouble(strInput);
					m_uaSim.setNormalSD(dNormalSD);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}
	
	// simulate user access history according to previous configuration
	public void startUserAccessSimulation()
	{
		System.out.println("====== Start user access simulation");
		m_uaSim.start();
		return;
	}
	
	// simulate user access history and calculate prefetch precision and recall
	public void startUserAccessSimulationAndCalPrecRecall()
	{
		int nUserNum = 0;
		int nAvgDayAccCount = 0;
		int nTotalSimDays = 0;
		int nAccPattern = 0;
		double dNormalMean = 0.0;
		double dNormalSD = 0.0;
		double dPrefThreshold;
		int nPrefInterval;
		double[] dAvgPrecRecall; //[0]: precision, [1]: recall
		FileOutputStream fo = null;
		PrintWriter pw = null;


		System.out.println("====== Start user access simulation");
		
		//////// execute simulation
		nUserNum = 10;
		nAvgDayAccCount = 10;
		nTotalSimDays = 100;
		nAccPattern = 0;
		dNormalMean = 5.0;
		dNormalSD = 1.0;
		
		for(nAccPattern = 0; nAccPattern <= 1; nAccPattern++)
		{
			m_uaSim.start(nUserNum, nAvgDayAccCount, nTotalSimDays, nAccPattern, dNormalMean, dNormalSD);

			///// calculate the prefetch precision and recall varying the prefetch threshold
			try {
				fo = new FileOutputStream("precision-recall-int7-ap"+nAccPattern+".txt");
				pw = new PrintWriter(fo);
				pw.println("Number of users: "+nUserNum);
				pw.println("Average daily access count: "+nAvgDayAccCount);
				pw.println("Total simulation days: "+nTotalSimDays);
				pw.println("Access pattern: "+nAccPattern);
				if(nAccPattern == 1)
				{
					pw.println("Normal mean: "+dNormalMean);
					pw.println("Normal SD: "+dNormalSD);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			nPrefInterval = 7;
			pw.println("Prefetch Interval: "+nPrefInterval);
			dPrefThreshold = 0.0;
			while(dPrefThreshold < 1.0)
			{
				dAvgPrecRecall = m_uaSim.calPrecisionRecall(dPrefThreshold, nPrefInterval);
				pw.format("%.2f\t%.4f\t%.4f\n", dPrefThreshold, dAvgPrecRecall[0], dAvgPrecRecall[1]);
				dPrefThreshold += 0.1;
			}

			pw.close();
			try {
				fo.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			///// calculate the prefetch precision and recall varying the prefetch interval
			try {
				if(nAccPattern == 0)
					fo = new FileOutputStream("precision-recall-thr0.1-ap"+nAccPattern+".txt");
				else
					fo = new FileOutputStream("precision-recall-thr0.2-ap"+nAccPattern+".txt");
				pw = new PrintWriter(fo);
				pw.println("Number of users: "+nUserNum);
				pw.println("Average daily access count: "+nAvgDayAccCount);
				pw.println("Total simulation days: "+nTotalSimDays);
				pw.println("Access pattern: "+nAccPattern);
				if(nAccPattern == 1)
				{
					pw.println("Normal mean: "+dNormalMean);
					pw.println("Normal SD: "+dNormalSD);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(nAccPattern == 0)
				dPrefThreshold = 0.1;
			else
				dPrefThreshold = 0.2;
			pw.println("Prefetch Threshold: "+dPrefThreshold);
			for(nPrefInterval = 7; nPrefInterval <= nTotalSimDays; nPrefInterval+=7)
			{
				dAvgPrecRecall = m_uaSim.calPrecisionRecall(dPrefThreshold, nPrefInterval);
				pw.format("%d\t%.4f\t%.4f\n", nPrefInterval, dAvgPrecRecall[0], dAvgPrecRecall[1]);			
			}

			pw.close();
			try {
				fo.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		
		//////// execute simulation of skewed access pattern with varying standard deviation of normal distribution
		nUserNum = 10;
		nAvgDayAccCount = 10;
		nTotalSimDays = 100;
		nAccPattern = 1;
		dNormalMean = 5.0;
		dNormalSD = 1.0;
		
		for(dNormalSD = 0.1; dNormalSD <= 2.0; dNormalSD += 0.2)
		{
			m_uaSim.start(nUserNum, nAvgDayAccCount, nTotalSimDays, nAccPattern, dNormalMean, dNormalSD);

			///// calculate the prefetch precision and recall varying the prefetch threshold
			try {
				fo = new FileOutputStream("precision-recall-int7-ap1-mean"+dNormalMean+"-sd"+dNormalSD+".txt");
				pw = new PrintWriter(fo);
				pw.println("Number of users: "+nUserNum);
				pw.println("Average daily access count: "+nAvgDayAccCount);
				pw.println("Total simulation days: "+nTotalSimDays);
				pw.println("Access pattern: "+nAccPattern);
				pw.println("Normal mean: "+dNormalMean);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			nPrefInterval = 7;
			pw.println("Prefetch Interval: "+nPrefInterval);
			dPrefThreshold = 0.0;
			while(dPrefThreshold < 1.0)
			{
				dAvgPrecRecall = m_uaSim.calPrecisionRecall(dPrefThreshold, nPrefInterval);
				pw.format("%.2f\t%.4f\t%.4f\n", dPrefThreshold, dAvgPrecRecall[0], dAvgPrecRecall[1]);
				dPrefThreshold += 0.1;
			}

			pw.close();
			try {
				fo.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		return;
	}
	
	public void writeRecentAccHistoryToDB()
	{
		CMInfo cmInfo = m_serverStub.getCMInfo();
		boolean bRet = false;
		
		// configure user access simulation
		configureUserAccessSimulation();
		// start simulation
		startUserAccessSimulation();
		// wrtie recent access history to DB
		bRet = m_uaSim.writeRecentAccHistoryToDB(cmInfo);
		if(bRet)
			System.out.println("Successful update of user access table of CMDB");
		else
			System.err.println("Error for update of user access table of CMDB!");
		
		return;
	}
	
	public void measureInputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		System.out.println("========== test input network throughput");
		System.out.print("target user: ");
		strTarget = m_scan.next();
		fSpeed = m_serverStub.measureInputThroughput(strTarget);
		if(fSpeed == -1)
			System.err.println("Test failed!");
		else
			System.out.format("Input network throughput from [%s] : %.2f%n", strTarget, fSpeed);		
	}
	
	public void measureOutputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		System.out.println("========== test output network throughput");
		System.out.print("target user: ");
		strTarget = m_scan.next();
		fSpeed = m_serverStub.measureOutputThroughput(strTarget);
		if(fSpeed == -1)
			System.err.println("Test failed!");
		else
			System.out.format("Output network throughput to [%s] : %.2f%n", strTarget, fSpeed);		
	}
	
	public void addChannel()
	{
		int nChType = -1;
		int nChKey = -1;
		String strServerName = null;
		String strChAddress = null;
		int nChPort = -1;
		String strSessionName = null;
		String strGroupName = null;
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		boolean bResult = false;
		String strBlock = null;
		boolean isBlock = false;
		SocketChannel sc = null;
		DatagramChannel dc = null;
		String strSync = null;
		boolean isSyncCall = false;
		
		System.out.println("====== add additional channel");

		// ask channel type, (server name), channel index (integer greater than 0), addr, port
		try{
			//System.out.print("Select channel type (SocketChannel:2, DatagramChannel:3, MulticastChannel:4): ");
			System.out.print("Select channel type (DatagramChannel:3, MulticastChannel:4): ");
			nChType = m_scan.nextInt();
			if(nChType == CMInfo.CM_SOCKET_CHANNEL)
			{
				System.err.println("socket channel not yet supported!");
				return;
				/*
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
				*/
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
		/*
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
		*/
		case CMInfo.CM_DATAGRAM_CHANNEL:
			if(isBlock)
			{
				dc = m_serverStub.addBlockDatagramChannel(nChPort);
				if(dc != null)
					System.out.println("Successfully added a blocking datagram socket channel: port("+nChPort+")");
				else
					System.err.println("Failed to add a blocking datagram socket channel: port("+nChPort+")");								
			}
			else
			{
				dc = m_serverStub.addNonBlockDatagramChannel(nChPort);
				if(dc != null)
					System.out.println("Successfully added a non-blocking datagram socket channel: port("+nChPort+")");
				else
					System.err.println("Failed to add a non-blocking datagram socket channel: port("+nChPort+")");				
			}
			
			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			bResult = m_serverStub.addMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
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
	
	public void removeChannel()
	{
		int nChType = -1;
		int nChKey = -1;
		int nChPort = -1;
		String strChAddress = null;
		String strServerName = null;
		String strSessionName = null;
		String strGroupName = null;
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		boolean result = false;
		String strBlock = null;
		boolean isBlock = false;
		String strSync = null;
		boolean isSyncCall = false;
		
		System.out.println("====== remove additional channel");
		try{
			//System.out.print("Select channel type (SocketChannel:2, DatagramChannel:3, MulticastChannel:4): ");
			System.out.print("Select channel type (DatagramChannel:3, MulticastChannel:4): ");
			nChType = m_scan.nextInt();
			if(nChType == CMInfo.CM_SOCKET_CHANNEL)
			{
				System.err.println("socket channel not yet supported!");
				return;
				/*
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
				*/
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
		/*
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
		*/
		case CMInfo.CM_DATAGRAM_CHANNEL:
			if(isBlock)
			{
				result = m_serverStub.removeBlockDatagramChannel(nChPort);
				if(result)
					System.out.println("Successfully removed a blocking datagram socket channel: port("+nChPort+")");
				else
					System.err.println("Failed to remove a blocking datagram socket channel: port("+nChPort+")");				
			}
			else
			{
				result = m_serverStub.removeNonBlockDatagramChannel(nChPort);
				if(result)
					System.out.println("Successfully removed a non-blocking datagram socket channel: port("+nChPort+")");
				else
					System.err.println("Failed to remove a non-blocking datagram socket channel: port("+nChPort+")");				
			}

			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			result = m_serverStub.removeAdditionalMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
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

	public void printCurrentChannelInfo()
	{
		System.out.println("========== print current channel info");
		String strChannels = m_serverStub.getCurrentChannelInfo();
		System.out.println(strChannels);
	}

	public void printLoginUsers()
	{
		System.out.println("========== print login users");
		CMMember loginUsers = m_serverStub.getLoginUsers();
		if(loginUsers == null)
		{
			System.err.println("The login users list is null!");
			return;
		}
		
		System.out.println("Currently ["+loginUsers.getMemberNum()+"] users are online.");
		Vector<CMUser> loginUserVector = loginUsers.getAllMembers();
		Iterator<CMUser> iter = loginUserVector.iterator();
		int nPrintCount = 0;
		while(iter.hasNext())
		{
			CMUser user = iter.next();
			System.out.print(user.getName()+" ");
			nPrintCount++;
			if((nPrintCount % 10) == 0)
			{
				System.out.println();
				nPrintCount = 0;
			}
		}
	}

	public void printConfigurations()
	{
		String[] strConfigurations;
		System.out.print("========== print all current configurations\n");
		Path confPath = m_serverStub.getConfigurationHome().resolve("cm-server.conf");
		strConfigurations = CMConfigurator.getConfigurations(confPath.toString());
		
		System.out.print("configuration file path: "+confPath.toString()+"\n");
		for(String strConf : strConfigurations)
		{
			String[] strFieldValuePair;
			strFieldValuePair = strConf.split("\\s+");
			System.out.print(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
		}
	}
	
	public void changeConfiguration()
	{
		boolean bRet = false;
		String strField = null;
		String strValue = null;
		System.out.println("========== change configuration");
		Path confPath = m_serverStub.getConfigurationHome().resolve("cm-server.conf");
		
		System.out.print("Field name: ");
		strField = m_scan.next();
		System.out.print("Value: ");
		strValue = m_scan.next();
		
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), strField, strValue);
		if(bRet)
		{
			System.out.println("cm-server.conf file is successfully updated: ("+strField+"="+strValue+")");
		}
		else
		{
			System.err.println("The configuration change is failed!: ("+strField+"="+strValue+")");
		}
		
		return;	
	}
	
	public void findMqttSessionInfo()
	{
		System.out.println("========== find MQTT session info");
		String strUser = null;
		System.out.print("User Name: ");
		strUser = m_scan.nextLine().trim();
		if(strUser == null || strUser.isEmpty()) 
			return;
		
		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		System.out.println("MQTT session of \""+strUser+"\" is ");
		System.out.println(mqttManager.getSessionInfo(strUser));
		
		return;
	}
	
	public void printAllMqttSessionInfo()
	{
		System.out.println("========== print all MQTT session info");
		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		System.out.println(mqttManager.getAllSessionInfo());
		
		return;
		
	}
	
	public void printAllMqttRetainInfo()
	{
		System.out.println("=========== print all MQTT retain info");
		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttManager is null!");
			return;
		}
		System.out.println(mqttManager.getAllRetainInfo());
		
		return;
	}
	
	public void sendEventWithWrongByteNum()
	{
		System.out.println("========== send a CMDummyEvent with wrong # bytes to a client");
		
		CMCommInfo commInfo = m_serverStub.getCMInfo().getCommInfo();
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		CMBlockingEventQueue sendQueue = commInfo.getSendBlockingEventQueue();
		
		String strTarget = JOptionPane.showInputDialog("target client or server name: ").trim();
		SelectableChannel ch = null;
		CMUser user = interInfo.getLoginUsers().findMember(strTarget);
		CMServer server = null;

		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		if(user != null)
		{
			ch = user.getNonBlockSocketChannelInfo().findChannel(0);
		}
		else if(strTarget.contentEquals(strDefServer))
		{
			ch = interInfo.getDefaultServerInfo().getNonBlockSocketChannelInfo().findChannel(0);
		}
		else
		{
			server = interInfo.findAddServer(strTarget);
			if(server != null)
			{
				ch = server.getNonBlockSocketChannelInfo().findChannel(0);
			}
			else {
				System.err.println("["+strTarget+"] not found!");
				return;
			}
		}
		
		CMDummyEvent due = new CMDummyEvent();
		ByteBuffer buf = due.marshall();
		CMMessage msg = new CMMessage(buf, ch);
		sendQueue.push(msg);

	}
	
	public void sendEventWithWrongEventType()
	{
		System.out.println("========== send a CMDummyEvent with wrong event type");
		
		System.out.print("target server or client name: ");
		String strTarget = m_scan.next().trim();

		CMDummyEvent due = new CMDummyEvent();
		due.setType(-1);	// set wrong event type
		m_serverStub.send(due, strTarget);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CMServerApp server = new CMServerApp();
		CMServerStub cmStub = server.getServerStub();
		cmStub.setAppEventHandler(server.getServerEventHandler());
		server.startCM();
		
		System.out.println("Server application is terminated.");
	}

}
