package examples;

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

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class CMWinServer extends JFrame {

	private static final long serialVersionUID = 1L;

	//private JTextArea m_outTextArea;
	private JTextPane m_outTextPane;
	private JTextField m_inTextField;
	private JButton m_startStopButton;
	private CMServerStub m_serverStub;
	private CMWinServerEventHandler m_eventHandler;
	private CMSNSUserAccessSimulator m_uaSim;

	CMWinServer()
	{

		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();
		setTitle("CM Server");
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setMenus();
		setLayout(new BorderLayout());

		m_outTextPane = new JTextPane();
		m_outTextPane.setEditable(false);

		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);

		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane scroll = new JScrollPane (m_outTextPane,
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(scroll);

		m_inTextField = new JTextField();
		m_inTextField.addKeyListener(cmKeyListener);
		add(m_inTextField, BorderLayout.SOUTH);

		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);

		m_startStopButton = new JButton("Start Server CM");
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setEnabled(false);
		//add(startStopButton, BorderLayout.NORTH);
		topButtonPanel.add(m_startStopButton);

		setVisible(true);

		// create CM stub object and set the event handler
		m_serverStub = new CMServerStub();
		m_eventHandler = new CMWinServerEventHandler(m_serverStub, this);
		m_uaSim = new CMSNSUserAccessSimulator();

		// start cm
		startCM();
	}

	private void addStylesToDocument(StyledDocument doc)
	{
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");

		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);
	}

	public CMServerStub getServerStub()
	{
		return m_serverStub;
	}

	public CMWinServerEventHandler getServerEventHandler()
	{
		return m_eventHandler;
	}

	public void setMenus()
	{
		MyMenuListener menuListener = new MyMenuListener();
		JMenuBar menuBar = new JMenuBar();

		JMenu helpMenu = new JMenu("Help");
		//helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem showAllMenuItem = new JMenuItem("show all menus");
		showAllMenuItem.addActionListener(menuListener);
		showAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));

		helpMenu.add(showAllMenuItem);
		menuBar.add(helpMenu);

		JMenu cmNetworkMenu = new JMenu("Network Participation");

		JMenu startStopSubMenu = new JMenu("Start/Stop");
		JMenuItem startMenuItem = new JMenuItem("start CM");
		startMenuItem.addActionListener(menuListener);
		startStopSubMenu.add(startMenuItem);
		JMenuItem terminateMenuItem = new JMenuItem("terminate CM");
		terminateMenuItem.addActionListener(menuListener);
		startStopSubMenu.add(terminateMenuItem);

		cmNetworkMenu.add(startStopSubMenu);

		JMenu multiServerSubMenu = new JMenu("Multi-server");
		JMenuItem connectDefaultMenuItem = new JMenuItem("connect to default server");
		connectDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(connectDefaultMenuItem);
		JMenuItem disconnectDefaultMenuItem = new JMenuItem("disconnect from default server");
		disconnectDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(disconnectDefaultMenuItem);
		JMenuItem regDefaultMenuItem = new JMenuItem("register to default server");
		regDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(regDefaultMenuItem);
		JMenuItem deregDefaultMenuItem = new JMenuItem("deregister from default server");
		deregDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(deregDefaultMenuItem);

		cmNetworkMenu.add(multiServerSubMenu);
		menuBar.add(cmNetworkMenu);

		JMenu serviceMenu = new JMenu("Services");

		JMenu infoSubMenu = new JMenu("Information");
		JMenuItem showSessionMenuItem = new JMenuItem("show session information");
		showSessionMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showSessionMenuItem);
		JMenuItem showGroupMenuItem = new JMenuItem("show group information");
		showGroupMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showGroupMenuItem);
		JMenuItem showChannelMenuItem = new JMenuItem("show current channels");
		showChannelMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showChannelMenuItem);
		JMenuItem showUsersMenuItem = new JMenuItem("show login users");
		showUsersMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showUsersMenuItem);
		JMenuItem inputThroughputMenuItem = new JMenuItem("test input network throughput");
		inputThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(inputThroughputMenuItem);
		JMenuItem outputThroughputMenuItem = new JMenuItem("test output network throughput");
		outputThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(outputThroughputMenuItem);
		JMenuItem showAllConfMenuItem = new JMenuItem("show all configurations");
		showAllConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showAllConfMenuItem);
		JMenuItem changeConfMenuItem = new JMenuItem("change configuration");
		changeConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(changeConfMenuItem);

		serviceMenu.add(infoSubMenu);

		JMenu fileTransferSubMenu = new JMenu("File Transfer");
		JMenuItem setPathMenuItem = new JMenuItem("set file path");
		setPathMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(setPathMenuItem);
		JMenuItem reqFileMenuItem = new JMenuItem("request file");
		reqFileMenuItem.addActionListener(menuListener);
		reqFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
		fileTransferSubMenu.add(reqFileMenuItem);
		JMenuItem pushFileMenuItem = new JMenuItem("push file");
		pushFileMenuItem.addActionListener(menuListener);
		pushFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		fileTransferSubMenu.add(pushFileMenuItem);
		JMenuItem cancelRecvMenuItem = new JMenuItem("cancel receiving file");
		cancelRecvMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(cancelRecvMenuItem);
		JMenuItem cancelSendMenuItem = new JMenuItem("cancel sending file");
		cancelSendMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(cancelSendMenuItem);
		JMenuItem printSendRecvFileInfoMenuItem = new JMenuItem("print sending/receiving file info");
		printSendRecvFileInfoMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(printSendRecvFileInfoMenuItem);

		serviceMenu.add(fileTransferSubMenu);

		JMenu snsSubMenu = new JMenu("Social Network Service");
		JMenuItem attachSchemeMenuItem = new JMenuItem("set attachment download scheme");
		attachSchemeMenuItem.addActionListener(menuListener);
		snsSubMenu.add(attachSchemeMenuItem);

		serviceMenu.add(snsSubMenu);

		JMenu channelSubMenu = new JMenu("Channel");
		JMenuItem addChannelMenuItem = new JMenuItem("add channel");
		addChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(addChannelMenuItem);
		JMenuItem removeChannelMenuItem = new JMenuItem("remove channel");
		removeChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(removeChannelMenuItem);

		serviceMenu.add(channelSubMenu);

		JMenu pubsubSubMenu = new JMenu("Publish/Subscribe");
		JMenuItem findSessionMenuItem = new JMenuItem("find session info");
		findSessionMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(findSessionMenuItem);
		JMenuItem printAllSessionMenuItem = new JMenuItem("print all session info");
		printAllSessionMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(printAllSessionMenuItem);
		JMenuItem printAllRetainInfoMenuItem = new JMenuItem("print all retain info");
		printAllRetainInfoMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(printAllRetainInfoMenuItem);

		serviceMenu.add(pubsubSubMenu);

		JMenu otherSubMenu = new JMenu("Other CM Tests");
		JMenuItem configUserAccessSimMenuItem = new JMenuItem("configure SNS user access simulation");
		configUserAccessSimMenuItem.addActionListener(menuListener);
		otherSubMenu.add(configUserAccessSimMenuItem);
		JMenuItem startUserAccessSimMenuItem = new JMenuItem("start SNS user access simulation");
		startUserAccessSimMenuItem.addActionListener(menuListener);
		otherSubMenu.add(startUserAccessSimMenuItem);
		JMenuItem prefetchAccSimMenuItem = new JMenuItem("start SNS user access simulation and measure prefetch accuracy");
		prefetchAccSimMenuItem.addActionListener(menuListener);
		otherSubMenu.add(prefetchAccSimMenuItem);
		JMenuItem recentAccHistorySimMenuItem = new JMenuItem("start and write recent SNS access history simulation to CM DB");
		recentAccHistorySimMenuItem.addActionListener(menuListener);
		otherSubMenu.add(recentAccHistorySimMenuItem);

		serviceMenu.add(otherSubMenu);
		menuBar.add(serviceMenu);

		setJMenuBar(menuBar);
	}

	public void processInput(String strInput)
	{
		int nCommand = -1;
		try {
			nCommand = Integer.parseInt(strInput);
		} catch (NumberFormatException e) {
			printMessage("Incorrect command number!\n");
			return;
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
			//System.out.println("Unknown command.");
			printStyledMessage("Unknown command.\n", "bold");
			break;
		}
	}

	public void printAllMenus()
	{
		printMessage("---------------------------------- Help\n");
		printMessage("0: show all menus\n");
		printMessage("---------------------------------- Start/Stop\n");
		printMessage("100: start CM, 999: terminate CM\n");
		printMessage("---------------------------------- Information\n");
		printMessage("1: show session information, 2: show group information\n");
		printMessage("3: test input network throughput, 4: test output network throughput\n");
		printMessage("5: show current channels, 6: show login users\n");
		printMessage("7: show all configurations, 8: change configuration\n");
		printMessage("---------------------------------- File Transfer\n");
		printMessage("20: set file path, 21: request file, 22: push file\n");
		printMessage("23: cancel receiving file, 24: cancel sending file\n");
		printMessage("25: print sending/receiving file info\n");
		printMessage("---------------------------------- Multi-server\n");
		printMessage("30: register to default server, 31: deregister from default server\n");
		printMessage("32: connect to default server, 33: disconnect from default server\n");
		printMessage("---------------------------------- Social Network Service\n");
		printMessage("40: set attachment download scheme\n");
		printMessage("---------------------------------- Channel\n");
		printMessage("50: add channel, 51: remove channel\n");
		printMessage("---------------------------------- MQTT\n");
		printMessage("60: find session info, 61: print all session info, 62: print all retain info\n");
		printMessage("---------------------------------- Other CM Tests\n");
		printMessage("101: configure SNS user access simulation, 102: start SNS user access simulation\n");
		printMessage("103: start SNS user access simulation and measure prefetch accuracy\n");
		printMessage("104: start and write recent SNS access history simulation to CM DB\n");
		printMessage("105: send event with wrong bytes, 106: send event with wrong type\n");
	}

	public void updateTitle()
	{
		CMUser myself = m_serverStub.getMyself();
		if(CMConfigurator.isDServer(m_serverStub.getCMInfo()))
		{
			setTitle("CM Default Server [\""+myself.getName()+"\"]");
		}
		else
		{
			if(myself.getState() < CMInfo.CM_LOGIN)
			{
				setTitle("CM Additional Server [\"?\"]");
			}
			else
			{
				setTitle("CM Additional Server [\""+myself.getName()+"\"]");
			}
		}
	}

	public void startCM()
	{
		boolean bRet = false;

		// get current server info from the server configuration file
		String strSavedServerAddress = null;
		String strCurServerAddress = null;
		int nSavedServerPort = -1;

		strSavedServerAddress = m_serverStub.getServerAddress();
		strCurServerAddress = CMCommManager.getLocalIP();
		nSavedServerPort = m_serverStub.getServerPort();

		// ask the user if he/she would like to change the server info
		JTextField serverAddressTextField = new JTextField(strCurServerAddress);
		JTextField serverPortTextField = new JTextField(String.valueOf(nSavedServerPort));
		Object msg[] = {
				"Server Address: ", serverAddressTextField,
				"Server Port: ", serverPortTextField
		};
		int option = JOptionPane.showConfirmDialog(null, msg, "Server Information", JOptionPane.OK_CANCEL_OPTION);

		// update the server info if the user would like to do
		if (option == JOptionPane.OK_OPTION)
		{
			String strNewServerAddress = serverAddressTextField.getText();
			int nNewServerPort = Integer.parseInt(serverPortTextField.getText());
			if(!strNewServerAddress.equals(strSavedServerAddress) || nNewServerPort != nSavedServerPort)
				m_serverStub.setServerInfo(strNewServerAddress, nNewServerPort);
		}

		// start cm
		bRet = m_serverStub.startCM();
		if(!bRet)
		{
			printStyledMessage("CM initialization error!\n", "bold");
		}
		else
		{
			printStyledMessage("Server CM starts.\n", "bold");
			printMessage("Type \"0\" for menu.\n");
			// change button to "stop CM"
			m_startStopButton.setEnabled(true);
			m_startStopButton.setText("Stop Server CM");
			updateTitle();
		}

		m_inTextField.requestFocus();

	}

	public void terminateCM()
	{
		m_serverStub.terminateCM();
		printMessage("Server CM terminates.\n");
		m_startStopButton.setText("Start Server CM");
		updateTitle();
	}

	public void printSessionInfo()
	{
		printMessage("------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-10s%-10s%n", "session name", "session addr", "port", "#users"));
		printMessage("------------------------------------------------------\n");

		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		Iterator<CMSession> iter = interInfo.getSessionList().iterator();
		while(iter.hasNext())
		{
			CMSession session = iter.next();
			printMessage(String.format("%-20s%-20s%-10d%-10d%n", session.getSessionName(), session.getAddress()
					, session.getPort(), session.getSessionUsers().getMemberNum()));
		}
		return;
	}

	public void printGroupInfo()
	{
		String strSessionName = null;

		printMessage("====== print group information\n");
		strSessionName = JOptionPane.showInputDialog("Session Name");
		if(strSessionName == null)
		{
			return;
		}

		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		CMSession session = interInfo.findSession(strSessionName);
		if(session == null)
		{
			printMessage("Session("+strSessionName+") not found.\n");
			return;
		}

		printMessage("------------------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-10s%-10s%n", "group name", "multicast addr", "port", "#users"));
		printMessage("------------------------------------------------------------------\n");

		Iterator<CMGroup> iter = session.getGroupList().iterator();
		while(iter.hasNext())
		{
			CMGroup gInfo = iter.next();
			printMessage(String.format("%-20s%-20s%-10d%-10d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort(), gInfo.getGroupUsers().getMemberNum()));
		}

		printMessage("======\n");
		return;
	}

	public void setFilePath()
	{
		printMessage("====== set file path\n");
		String strPath = null;

		strPath = JOptionPane.showInputDialog("file path: ");
		if(strPath == null)
		{
			return;
		}

		m_serverStub.setTransferedFileHome(Paths.get(strPath));

		printMessage("======\n");
	}

	public void requestFile()
	{
		boolean bReturn = false;
		String strFileName = null;
		String strFileOwner = null;
		byte byteFileAppendMode = -1;

		printMessage("====== request a file\n");
		JTextField fileNameField = new JTextField();
		JTextField fileOwnerField = new JTextField();
		String[] fAppendMode = {"Default", "Overwrite", "Append"};
		JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

		Object[] message = {
		    "File Name:", fileNameField,
		    "File Owner:", fileOwnerField,
			"File Append Mode: ", fAppendBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "File Request Input", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
		{
			printMessage("canceled!\n");
			return;
		}

		strFileName = fileNameField.getText().trim();
		if(strFileName.isEmpty())
		{
			printMessage("File name is empty!\n");
			return;
		}

		strFileOwner = fileOwnerField.getText().trim();
		if(strFileOwner.isEmpty())
		{
			printMessage("File owner is empty!\n");
			return;
		}

		switch(fAppendBox.getSelectedIndex())
		{
		case 0:
			byteFileAppendMode = CMInfo.FILE_DEFAULT;
			break;
		case 1:
			byteFileAppendMode = CMInfo.FILE_OVERWRITE;
			break;
		case 2:
			byteFileAppendMode = CMInfo.FILE_APPEND;
			break;
		}

		bReturn = m_serverStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);

		if(!bReturn)
			printMessage("Request file error! file("+strFileName+"), owner("+strFileOwner+").\n");

		printMessage("======\n");
	}

	public void pushFile()
	{
		String strFilePath = null;
		File[] files;
		String strReceiver = null;
		byte byteFileAppendMode = -1;
		boolean bReturn = false;

		/*
		strReceiver = JOptionPane.showInputDialog("Receiver Name: ");
		if(strReceiver == null) return;
		*/
		JTextField freceiverField = new JTextField();
		String[] fAppendMode = {"Default", "Overwrite", "Append"};
		JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

		Object[] message = {
				"File Receiver: ", freceiverField,
				"File Append Mode: ", fAppendBox
				};
		int option = JOptionPane.showConfirmDialog(null, message, "File Push", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
		{
			printMessage("canceled.\n");
			return;
		}

		strReceiver = freceiverField.getText().trim();
		if(strReceiver.isEmpty())
		{
			printMessage("File receiver is empty!\n");
			return;
		}

		switch(fAppendBox.getSelectedIndex())
		{
		case 0:
			byteFileAppendMode = CMInfo.FILE_DEFAULT;
			break;
		case 1:
			byteFileAppendMode = CMInfo.FILE_OVERWRITE;
			break;
		case 2:
			byteFileAppendMode = CMInfo.FILE_APPEND;
			break;
		}

		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		File curDir = new File(confInfo.getTransferedFileHome().toString());
		fc.setCurrentDirectory(curDir);
		int fcRet = fc.showOpenDialog(this);
		if(fcRet != JFileChooser.APPROVE_OPTION) return;
		files = fc.getSelectedFiles();
		if(files.length < 1) return;
		for(int i=0; i < files.length; i++)
		{
			strFilePath = files[i].getPath();
			bReturn = m_serverStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);
			if(!bReturn)
			{
				printMessage("push file error! file("+strFilePath+"), receiver("
						+strReceiver+").\n");
			}
		}

		printMessage("======\n");
	}

	public void cancelRecvFile()
	{
		String strSender = null;
		boolean bReturn = false;
		printMessage("====== cancel receiving a file\n");

		strSender = JOptionPane.showInputDialog("Input sender name (enter for all senders)");
		if(strSender.isEmpty())
			strSender = null;

		bReturn = m_serverStub.cancelPullFile(strSender);

		if(bReturn)
		{
			if(strSender == null)
				strSender = "all senders";
			printMessage("Successfully requested to cancel receiving a file to ["+strSender+"].\n");
		}
		else
			printMessage("Request failed to cancel receiving a file to ["+strSender+"]!\n");

		return;
	}

	public void cancelSendFile()
	{
		String strReceiver = null;
		boolean bReturn = false;
		printMessage("====== cancel sending a file\n");

		strReceiver = JOptionPane.showInputDialog("Input receiver name (enter for all receivers)");
		if(strReceiver.isEmpty())
			strReceiver = null;

		bReturn = m_serverStub.cancelPushFile(strReceiver);

		if(bReturn)
			printMessage("Successfully requested to cancel sending a file to ["+strReceiver+"]");
		else
			printMessage("Request failed to cancel sending a file to ["+strReceiver+"]!");

		return;
	}

	public void printSendRecvFileInfo()
	{
		CMFileTransferInfo fInfo = m_serverStub.getCMInfo().getFileTransferInfo();
		Hashtable<String, CMList<CMSendFileInfo>> sendHashtable = fInfo.getSendFileHashtable();
		Hashtable<String, CMList<CMRecvFileInfo>> recvHashtable = fInfo.getRecvFileHashtable();
		Set<String> sendKeySet = sendHashtable.keySet();
		Set<String> recvKeySet = recvHashtable.keySet();

		printMessage("==== sending file info\n");
		for(String receiver : sendKeySet)
		{
			CMList<CMSendFileInfo> sendList = sendHashtable.get(receiver);
			printMessage(sendList+"\n");
		}

		printMessage("==== receiving file info\n");
		for(String sender : recvKeySet)
		{
			CMList<CMRecvFileInfo> recvList = recvHashtable.get(sender);
			printMessage(recvList+"\n");
		}
	}

	public void requestServerReg()
	{
		String strServerName = null;

		printMessage("====== request registration to the default server\n");
		strServerName = JOptionPane.showInputDialog("Enter registered server name");
		if(strServerName != null)
		{
			m_serverStub.requestServerReg(strServerName);
		}

		printMessage("======\n");
		return;
	}

	public void requestServerDereg()
	{
		printMessage("====== request deregistration from the default server\n");
		boolean bRet = m_serverStub.requestServerDereg();
		printMessage("======\n");
		if(bRet)
			updateTitle();

		return;
	}

	public void connectToDefaultServer()
	{
		printMessage("====== connect to the default server\n");
		boolean bRet = m_serverStub.connectToServer();
		printMessage("======\n");
		if(bRet)
			updateTitle();
		return;
	}

	public void disconnectFromDefaultServer()
	{
		printMessage("====== disconnect from the default server\n");
		boolean bRet = m_serverStub.disconnectFromServer();
		printMessage("======\n");
		if(bRet)
			updateTitle();
		return;
	}

	public void setAttachDownloadScheme()
	{
		String strUserName = null;
		int nScheme = -1;
		JTextField userField = new JTextField();
		String[] attachLod = {"Full", "Thumbnail", "Prefetching", "None"};
		JComboBox<String> lodBox = new JComboBox<String>(attachLod);
		Object[] message = {
				"Target user name (Enter for all users)", userField,
				"Image QoS: ", lodBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Attachement Download Scheme", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strUserName = userField.getText();
			nScheme = lodBox.getSelectedIndex();
			printMessage("The attachment download scheme of user["+strUserName
					+"] is set to ["+lodBox.getItemAt(nScheme)+"].\n");
			if(strUserName.isEmpty())
				strUserName = null;
			m_serverStub.setAttachDownloadScheme(strUserName, nScheme);
		}

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

		// retrieve current values
		nUserNum = m_uaSim.getUserNum();
		nAvgDayAccCount = m_uaSim.getAvgDayAccCount();
		nTotalSimDays = m_uaSim.getTotalSimDays();
		nAccPattern = m_uaSim.getAccPattern();
		dNormalMean = m_uaSim.getNormalMean();
		dNormalSD = m_uaSim.getNormalSD();

		printMessage("====== Configure variables of user access simulation\n");
		JTextField userNumField = new JTextField();
		userNumField.setText(Integer.toString(nUserNum));
		JTextField avgDayAccCountField = new JTextField();
		avgDayAccCountField.setText(Integer.toString(nAvgDayAccCount));
		JTextField totalSimDaysField = new JTextField();
		totalSimDaysField.setText(Integer.toString(nTotalSimDays));
		String[] accPattern = {"Random", "Skewed"};
		JComboBox<String> accPatternBox = new JComboBox<String>(accPattern);
		accPatternBox.setSelectedIndex(0);

		Object[] message = {
		    "Number of users:", userNumField,
		    "Average daily access count:", avgDayAccCountField,
		    "Total number of simulation days:", totalSimDaysField,
		    "Access pattern:", accPatternBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Configuration of simulation variables",
				JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			int nInput;
			nInput = Integer.parseInt(userNumField.getText());
			if(nInput != nUserNum)
				m_uaSim.setUserNum(nInput);
			nInput = Integer.parseInt(avgDayAccCountField.getText());
			if(nInput != nAvgDayAccCount)
				m_uaSim.setAvgDayAccCount(nInput);
			nInput = Integer.parseInt(totalSimDaysField.getText());
			if(nInput != nTotalSimDays)
				m_uaSim.setTotalSimDays(nInput);
			nInput = accPatternBox.getSelectedIndex();
			if(nInput != nAccPattern)
				m_uaSim.setAccPattern(nInput);
		}

		if(accPatternBox.getSelectedIndex() == 1) // skewed access pattern
		{
			JTextField normalMeanField = new JTextField();
			normalMeanField.setText(Double.toString(dNormalMean));
			JTextField normalSDField = new JTextField();
			normalSDField.setText(Double.toString(dNormalSD));
			Object[] messageNormal = {
					"Mean value:", normalMeanField,
					"Standard deviation:", normalSDField
			};
			option = JOptionPane.showConfirmDialog(null, messageNormal, "Config for normal distribution",
					JOptionPane.OK_CANCEL_OPTION);
			if(option == JOptionPane.OK_OPTION)
			{
				double dInput;
				dInput = Double.parseDouble(normalMeanField.getText());
				if(dInput != dNormalMean)
					m_uaSim.setNormalMean(dInput);
				dInput = Double.parseDouble(normalSDField.getText());
				if(dInput != dNormalSD)
					m_uaSim.setNormalSD(dInput);
			}
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
			printMessage("Successful update of user access table of CMDB\n");
		else
			printMessage("Error for update of user access table of CMDB!\n");

		return;
	}

	public void measureInputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		printMessage("========== test input network throughput\n");

		strTarget = JOptionPane.showInputDialog("Target node");
		if(strTarget == null || strTarget.equals(""))
			return;

		fSpeed = m_serverStub.measureInputThroughput(strTarget);
		if(fSpeed == -1)
			printMessage("Test failed!\n");
		else
			printMessage(String.format("Input network throughput from [%s] : %.2f MBps%n", strTarget, fSpeed));
	}

	public void measureOutputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		printMessage("========== test output network throughput\n");

		strTarget = JOptionPane.showInputDialog("Target node");
		if(strTarget == null || strTarget.equals(""))
			return;

		fSpeed = m_serverStub.measureOutputThroughput(strTarget);
		if(fSpeed == -1)
			printMessage("Test failed!\n");
		else
			printMessage(String.format("Output network throughput to [%s] : %.2f MBps%n", strTarget, fSpeed));
	}

	public void addChannel()
	{
		int nChType = -1;
		String strChAddress = null; // the channel key for the multicast address is the (address, port) pair
		int nChPort = -1; // the channel key for the datagram socket channel, or the multicast port number
		String strSessionName = null;
		String strGroupName = null;
		boolean result = false;
		boolean isBlock = false;
		DatagramChannel dc = null;

		printMessage("====== add additional channel\n");

		// ask channel type, (server name), channel index (integer greater than 0), addr, port

		String[] chTypes = {"SocketChannel(not yet supported)", "DatagramChannel", "MulticastChannel"};
		JComboBox<String> chTypeBox = new JComboBox<String>(chTypes);
		chTypeBox.setSelectedIndex(1);
		Object[] message = {
				"Channel Type: ", chTypeBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Channel type", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		nChType = chTypeBox.getSelectedIndex() + 2;

		if(nChType == CMInfo.CM_SOCKET_CHANNEL) // not yet supported
		{
			printStyledMessage("add socket channel at the server not supported yet!\n", "bold");
			return;
			/*
			JRadioButton blockRadioButton = new JRadioButton("Blocking Channel");
			JRadioButton nonBlockRadioButton = new JRadioButton("NonBlocking Channel");
			nonBlockRadioButton.setSelected(true);
			ButtonGroup bGroup = new ButtonGroup();
			bGroup.add(blockRadioButton);
			bGroup.add(nonBlockRadioButton);
			String[] syncAsync = {"synchronous call", "asynchronous call"};
			JComboBox syncAsyncComboBox = new JComboBox(syncAsync);
			syncAsyncComboBox.setSelectedIndex(1); // default value is asynchronous call

			JTextField chIndexField = new JTextField();
			JTextField strServerField = new JTextField();
			Object[] scMessage = {
					"", blockRadioButton,
					"", nonBlockRadioButton,
					"syncronous or asynchronous call", syncAsyncComboBox,
					"Channel key (> 0 for nonblocking ch, >=0 for blocking ch)", chIndexField,
					"Server name(empty for the default server)", strServerField
			};

			int scResponse = JOptionPane.showConfirmDialog(null, scMessage, "Socket Channel", JOptionPane.OK_CANCEL_OPTION);

			if(scResponse != JOptionPane.OK_OPTION) return;
			nChKey = Integer.parseInt(chIndexField.getText());

			if(blockRadioButton.isSelected()) isBlock = true;
			else isBlock = false;

			if(!isBlock && nChKey <= 0)
			{
				printMessage("testAddChannel(), invalid nonblocking socket channel key ("+nChKey+")!\n");
				return;
			}
			else if(isBlock && nChKey < 0)
			{
				printMessage("testAddChannel(), invalid blocking socket channel key ("+nChKey+")!\n");
				return;
			}

			if(syncAsyncComboBox.getSelectedIndex() == 0)
				isSyncCall = true;
			else
				isSyncCall = false;

			strServerName = strServerField.getText();
			if(strServerName == null || strServerName.equals(""))
				strServerName = "SERVER"; // default server name
			*/
		}
		else if(nChType == CMInfo.CM_DATAGRAM_CHANNEL)
		{
			JRadioButton blockRadioButton = new JRadioButton("Blocking Channel");
			JRadioButton nonBlockRadioButton = new JRadioButton("NonBlocking Channel");
			nonBlockRadioButton.setSelected(true);
			ButtonGroup bGroup = new ButtonGroup();
			bGroup.add(blockRadioButton);
			bGroup.add(nonBlockRadioButton);

			JTextField chIndexField = new JTextField();

			Object[] scMessage = {
					"", blockRadioButton,
					"", nonBlockRadioButton,
					"Port number (key of the datagram channel)", chIndexField
			};

			int scResponse = JOptionPane.showConfirmDialog(null, scMessage, "Add Datagram Channel", JOptionPane.OK_CANCEL_OPTION);
			if(scResponse != JOptionPane.OK_OPTION) return;

			try {
				nChPort = Integer.parseInt(chIndexField.getText());
			}catch(NumberFormatException e) {
				printMessage("The channel UDP port must be a number !\n");
				return;
			}

			if(blockRadioButton.isSelected()) isBlock = true;
			else isBlock = false;
		}
		else if(nChType == CMInfo.CM_MULTICAST_CHANNEL)
		{
			JTextField snameField = new JTextField();
			JTextField gnameField = new JTextField();
			JTextField chAddrField = new JTextField();
			JTextField chPortField = new JTextField();
			Object[] multicastMessage = {
					"Target Session Name: ", snameField,
					"Target Group Name: ", gnameField,
					"Channel Multicast Address: ", chAddrField,
					"Channel Multicast Port: ", chPortField
			};
			int multicastResponse = JOptionPane.showConfirmDialog(null, multicastMessage, "Additional Multicast Input",
					JOptionPane.OK_CANCEL_OPTION);
			if(multicastResponse != JOptionPane.OK_OPTION) return;

			strSessionName = snameField.getText();
			strGroupName = gnameField.getText();
			strChAddress = chAddrField.getText();
			nChPort = Integer.parseInt(chPortField.getText());
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
					lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
					if(sc != null)
					{
						printMessage("Successfully added a blocking socket channel both "
								+ "at the client and the server: key("+nChKey+"), server("+strServerName+")\n");
						printMessage("return delay: "+lDelay+" ms.\n");
					}
					else
						printMessage("Failed to add a blocking socket channel both at "
								+ "the client and the server: key("+nChKey+"), server("+strServerName+")\n");
				}
				else
				{
					m_eventHandler.setStartTime(System.currentTimeMillis());
					result = m_clientStub.addBlockSocketChannel(nChKey, strServerName);
					lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
					if(result)
					{
						printMessage("Successfully added a blocking socket channel at the client and "
								+"requested to add the channel info to the server: key("+nChKey+"), server("
								+strServerName+")\n");
						printMessage("return delay: "+lDelay+" ms.\n");
					}
					else
						printMessage("Failed to add a blocking socket channel at the client or "
								+"failed to request to add the channel info to the server: key("+nChKey
								+"), server("+strServerName+")\n");
				}
			}
			else
			{
				if(isSyncCall)
				{
					sc = m_clientStub.syncAddNonBlockSocketChannel(nChKey, strServerName);
					if(sc != null)
						printMessage("Successfully added a nonblocking socket channel both at the client "
								+ "and the server: key("+nChKey+"), server("+strServerName+")\n");
					else
						printMessage("Failed to add a nonblocking socket channel both at the client "
								+ "and the server: key("+nChKey+") to server("+strServerName+")\n");
				}
				else
				{
					result = m_clientStub.addNonBlockSocketChannel(nChKey, strServerName);
					if(result)
						printMessage("Successfully added a nonblocking socket channel at the client and "
								+ "requested to add the channel info to the server: key("+nChKey+"), server("
								+strServerName+")\n");
					else
						printMessage("Failed to add a nonblocking socket channe at the client or "
								+ "failed to request to add the channel info to the server: key("+nChKey
								+") to server("+strServerName+")\n");
				}
			}

			break;
		*/
		case CMInfo.CM_DATAGRAM_CHANNEL:
			if(isBlock)
			{
				dc = m_serverStub.addBlockDatagramChannel(nChPort);
				if(dc != null)
					printMessage("Successfully added a blocking datagram socket channel: port("+nChPort+")\n");
				else
					printMessage("Failed to add a blocking datagram socket channel: port("+nChPort+")\n");
			}
			else
			{
				dc = m_serverStub.addNonBlockDatagramChannel(nChPort);
				if(dc != null)
					printMessage("Successfully added a non-blocking datagram socket channel: port("+nChPort+")\n");
				else
					printMessage("Failed to add a non-blocking datagram socket channel: port("+nChPort+")\n");
			}

			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			result = m_serverStub.addMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
			if(result)
			{
				printMessage("Successfully added a multicast channel: session("+strSessionName+"), group("
						+strGroupName+"), address("+strChAddress+"), port("+nChPort+")\n");
			}
			else
			{
				printMessage("Failed to add a multicast channel: session("+strSessionName+"), group("
						+strGroupName+"), address("+strChAddress+"), port("+nChPort+")\n");
			}
			break;
		default:
			printMessage("Channel type is incorrect!\n");
			break;
		}

		printMessage("======\n");
	}

	public void removeChannel()
	{
		int nChType = -1;
		int nChPort = -1;
		String strChAddress = null;
		String strSessionName = null;
		String strGroupName = null;
		boolean result = false;
		boolean isBlock = false;

		printMessage("====== remove additional channel\n");

		String[] chTypes = {"SocketChannel(not yet supported)", "DatagramChannel", "MulticastChannel"};
		JComboBox<String> chTypeBox = new JComboBox<String>(chTypes);
		chTypeBox.setSelectedIndex(1);
		Object[] message = {
				"Channel Type: ", chTypeBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Removal of Additional Channel", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		nChType = chTypeBox.getSelectedIndex() + 2;

		if(nChType == CMInfo.CM_SOCKET_CHANNEL)
		{
			printStyledMessage("remove socket channel not yet supported!", "bold");
			return;
			/*
			JRadioButton blockRadioButton = new JRadioButton("Blocking Channel");
			JRadioButton nonBlockRadioButton = new JRadioButton("NonBlocking Channel");
			nonBlockRadioButton.setSelected(true);
			ButtonGroup bGroup = new ButtonGroup();
			bGroup.add(blockRadioButton);
			bGroup.add(nonBlockRadioButton);
			String syncAsync[] = {"synchronous call", "asynchronous call"};
			JComboBox syncAsyncComboBox = new JComboBox(syncAsync);
			syncAsyncComboBox.setSelectedIndex(1);	//default value is asynchronous call

			JTextField chIndexField = new JTextField();
			JTextField strServerField = new JTextField();
			Object[] scMessage = {
					"", blockRadioButton,
					"", nonBlockRadioButton,
					"Synchronous or asynchronous call", syncAsyncComboBox,
					"Channel key (> 0 for nonblocking ch, >=0 for blocking ch)", chIndexField,
					"Server name(empty for the default server)", strServerField
			};

			int scResponse = JOptionPane.showConfirmDialog(null, scMessage, "Socket Channel", JOptionPane.OK_CANCEL_OPTION);

			if(scResponse != JOptionPane.OK_OPTION) return;
			nChKey = Integer.parseInt(chIndexField.getText());

			if(blockRadioButton.isSelected()) isBlock = true;
			else isBlock = false;

			if(!isBlock && nChKey <= 0)
			{
				printMessage("testRemoveChannel(), invalid nonblocking socket channel key ("+nChKey+")!\n");
				return;
			}
			else if(isBlock && nChKey < 0)
			{
				printMessage("testRemoveChannel(), invalid blocking socket channel key ("+nChKey+")!\n");
				return;
			}

			if(syncAsyncComboBox.getSelectedIndex() == 0)
				isSyncCall = true;
			else
				isSyncCall = false;

			strServerName = strServerField.getText();
			if(strServerName == null || strServerName.equals(""))
				strServerName = "SERVER"; // default server name
			*/
		}
		else if(nChType == CMInfo.CM_DATAGRAM_CHANNEL)
		{
			JRadioButton blockRadioButton = new JRadioButton("Blocking Channel");
			JRadioButton nonBlockRadioButton = new JRadioButton("NonBlocking Channel");
			nonBlockRadioButton.setSelected(true);
			ButtonGroup bGroup = new ButtonGroup();
			bGroup.add(blockRadioButton);
			bGroup.add(nonBlockRadioButton);

			JTextField chIndexField = new JTextField();
			Object[] scMessage = {
					"", blockRadioButton,
					"", nonBlockRadioButton,
					"Port number (key of the datagram channel):", chIndexField
			};

			int scResponse = JOptionPane.showConfirmDialog(null, scMessage, "Remove Datagram Channel",
					JOptionPane.OK_CANCEL_OPTION);

			if(scResponse != JOptionPane.OK_OPTION) return;
			try {
				nChPort = Integer.parseInt(chIndexField.getText());
			}catch(NumberFormatException e){
				printMessage("The channel UDP port must be a number!\n");
				return;
			}

			if(blockRadioButton.isSelected()) isBlock = true;
			else isBlock = false;

		}
		else if(nChType == CMInfo.CM_MULTICAST_CHANNEL)
		{
			JTextField snameField = new JTextField();
			JTextField gnameField = new JTextField();
			JTextField chAddrField = new JTextField();
			JTextField chPortField = new JTextField();
			Object[] sgMessage = {
					"Target Session Name: ", snameField,
					"Target Group Name: ", gnameField,
					"Channel Multicast Address: ", chAddrField,
					"Channel Multicast Port: ", chPortField
			};
			int sgOption = JOptionPane.showConfirmDialog(null, sgMessage, "Target Session and Group", JOptionPane.OK_CANCEL_OPTION);
			if(sgOption != JOptionPane.OK_OPTION) return;
			strSessionName = snameField.getText();
			strGroupName = gnameField.getText();
			strChAddress = chAddrField.getText();
			nChPort = Integer.parseInt(chPortField.getText());
		}

		switch(nChType)
		{
		/*
		case CMInfo.CM_SOCKET_CHANNEL:
			if(isBlock)
			{
				if(isSyncCall)
				{
					m_eventHandler.setStartTime(System.currentTimeMillis());
					result = m_clientStub.syncRemoveBlockSocketChannel(nChKey, strServerName);
					lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
					if(result)
					{
						printMessage("Successfully removed a blocking socket channel both "
								+ "at the client and the server: key("+nChKey+"), server ("+strServerName+")\n");
						printMessage("return delay: "+lDelay+" ms.\n");
					}
					else
						printMessage("Failed to remove a blocking socket channel both at the client "
								+ "and the server: key("+nChKey+"), server ("+strServerName+")\n");
				}
				else
				{
					m_eventHandler.setStartTime(System.currentTimeMillis());
					result = m_clientStub.removeBlockSocketChannel(nChKey, strServerName);
					lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
					if(result)
					{
						printMessage("Successfully removed a blocking socket channel at the client and "
								+ "requested to remove it at the server: key("+nChKey+"), server("+strServerName+")\n");
						printMessage("return delay: "+lDelay+" ms.\n");
					}
					else
						printMessage("Failed to remove a blocking socket channel at the client or "
								+ "failed to request to remove it at the server: key("+nChKey+"), server("
								+strServerName+")\n");
				}
			}
			else
			{
				result = m_clientStub.removeNonBlockSocketChannel(nChKey, strServerName);
				if(result)
					printMessage("Successfully removed a nonblocking socket channel: key("+nChKey
							+"), server("+strServerName+")\n");
				else
					printMessage("Failed to remove a nonblocing socket channel: key("+nChKey
							+"), server("+strServerName+")\n");
			}

			break;
		*/
		case CMInfo.CM_DATAGRAM_CHANNEL:
			if(isBlock)
			{
				result = m_serverStub.removeBlockDatagramChannel(nChPort);
				if(result)
					printMessage("Successfully removed a blocking datagram socket channel: port("+nChPort+")\n");
				else
					printMessage("Failed to remove a blocking datagram socket channel: port("+nChPort+")\n");
			}
			else
			{
				result = m_serverStub.removeNonBlockDatagramChannel(nChPort);
				if(result)
					printMessage("Successfully removed a non-blocking datagram socket channel: port("+nChPort+")\n");
				else
					printMessage("Failed to remove a non-blocking datagram socket channel: port("+nChPort+")\n");
			}

			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			result = m_serverStub.removeAdditionalMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
			if(result)
			{
				printMessage("Successfully removed a multicast channel: session("+strSessionName+"), group("
						+strGroupName+"), address("+strChAddress+"), port("+nChPort+")\n");
			}
			else
			{
				printMessage("Failed to remove a multicast channel: session("+strSessionName+"), group("
						+strGroupName+"), address("+strChAddress+"), port("+nChPort+")\n");
			}
			break;
		default:
			printMessage("Channel type is incorrect!\n");
			break;
		}

		printMessage("======\n");
	}

	public void printCurrentChannelInfo()
	{
		printMessage("========== print current channel info\n");
		String strChannels = m_serverStub.getCurrentChannelInfo();
		printMessage(strChannels);
	}

	public void printLoginUsers()
	{
		printMessage("========== print login users\n");
		CMMember loginUsers = m_serverStub.getLoginUsers();
		if(loginUsers == null)
		{
			printStyledMessage("The login users list is null!\n", "bold");
			return;
		}

		printMessage("Currently ["+loginUsers.getMemberNum()+"] users are online.\n");
		Vector<CMUser> loginUserVector = loginUsers.getAllMembers();
		Iterator<CMUser> iter = loginUserVector.iterator();
		int nPrintCount = 0;
		while(iter.hasNext())
		{
			CMUser user = iter.next();
			printMessage(user.getName()+" ");
			nPrintCount++;
			if((nPrintCount % 10) == 0)
			{
				printMessage("\n");
				nPrintCount = 0;
			}
		}
	}

	public void printConfigurations()
	{
		String[] strConfigurations;
		printMessage("========== print all current configurations\n");
		Path confPath = m_serverStub.getConfigurationHome().resolve("cm-server.conf");
		strConfigurations = CMConfigurator.getConfigurations(confPath.toString());

		printMessage("configuration file path: "+confPath.toString()+"\n");
		for(String strConf : strConfigurations)
		{
			String[] strFieldValuePair;
			strFieldValuePair = strConf.split("\\s+");
			printMessage(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
		}
	}

	public void changeConfiguration()
	{
		boolean bRet = false;
		String strField = null;
		String strValue = null;
		printMessage("========== change configuration\n");
		Path confPath = m_serverStub.getConfigurationHome().resolve("cm-server.conf");

		JTextField fieldTextField = new JTextField();
		JTextField valueTextField = new JTextField();
		Object[] msg = {
			"Field Name:", fieldTextField,
			"Value:", valueTextField
		};
		int nRet = JOptionPane.showConfirmDialog(null, msg, "Change Configuration", JOptionPane.OK_CANCEL_OPTION);
		if(nRet != JOptionPane.OK_OPTION) return;
		strField = fieldTextField.getText().trim();
		strValue = valueTextField.getText().trim();
		if(strField.isEmpty() || strValue.isEmpty())
		{
			printStyledMessage("There is an empty input!\n", "bold");
			return;
		}

		bRet = CMConfigurator.changeConfiguration(confPath.toString(), strField, strValue);
		if(bRet)
		{
			printMessage("cm-server.conf file is successfully updated: ("+strField+"="+strValue+")\n");
		}
		else
		{
			printStyledMessage("The configuration change is failed!: ("+strField+"="+strValue+")\n", "bold");
		}

		return;
	}

	public void findMqttSessionInfo()
	{
		printMessage("========== find MQTT session info\n");
		String strUser = null;
		strUser = JOptionPane.showInputDialog("User name").trim();
		if(strUser == null || strUser.equals(""))
			return;

		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		printMessage("MQTT session of \""+strUser+"\" is \n");
		printMessage(mqttManager.getSessionInfo(strUser)+"\n");

		return;
	}

	public void printAllMqttSessionInfo()
	{
		printMessage("========== print all MQTT session info\n");
		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		printMessage(mqttManager.getAllSessionInfo());

		return;
	}

	public void printAllMqttRetainInfo()
	{
		printMessage("=========== print all MQTT retain info\n");
		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		printMessage(mqttManager.getAllRetainInfo());

		return;
	}

	public void sendEventWithWrongByteNum()
	{
		printMessage("========== send a CMDummyEvent with wrong # bytes to a client\n");

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
				printStyledMessage("["+strTarget+"] not found!\n", "bold");
				return;
			}
		}

		CMDummyEvent due = new CMDummyEvent();
		ByteBuffer buf = due.marshall();
		buf.clear();
		buf.putInt(-1).clear();
		CMMessage msg = new CMMessage(buf, ch);
		sendQueue.push(msg);

	}

	public void sendEventWithWrongEventType()
	{
		printMessage("========== send a CMDummyEvent with wrong event type\n");

		String strTarget = JOptionPane.showInputDialog("target client or server name: ").trim();

		CMDummyEvent due = new CMDummyEvent();
		due.setType(-1);	// set wrong event type
		m_serverStub.send(due, strTarget);
	}

	public void printMessage(String strText)
	{
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, null);
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

	public void printStyledMessage(String strText, String strStyleName)
	{
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

	public void printImage(String strPath)
	{
		int nTextPaneWidth = m_outTextPane.getWidth();
		int nImageWidth;
		int nImageHeight;
		int nNewWidth;
		int nNewHeight;
		ImageIcon icon = new ImageIcon(strPath);
		Image image = icon.getImage();
		nImageWidth = image.getWidth(m_outTextPane);
		nImageHeight = image.getHeight(m_outTextPane);

		if(nImageWidth > nTextPaneWidth/2)
		{
			nNewWidth = nTextPaneWidth / 2;
			float fRate = (float)nNewWidth/(float)nImageWidth;
			nNewHeight = (int)(nImageHeight * fRate);
			Image newImage = image.getScaledInstance(nNewWidth, nNewHeight, Image.SCALE_SMOOTH);
			icon = new ImageIcon(newImage);
		}

		m_outTextPane.insertIcon ( icon );
		printMessage("\n");
	}


	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e)
		{
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_ENTER)
			{
				JTextField input = (JTextField)e.getSource();
				String strText = input.getText();
				printMessage(strText+"\n");
				// parse and call CM API
				processInput(strText);
				input.setText("");
				input.requestFocus();
			}
		}

		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
	}

	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			JButton button = (JButton) e.getSource();
			if(button.getText().equals("Start Server CM"))
			{
				// start cm
				boolean bRet = m_serverStub.startCM();
				if(!bRet)
				{
					printStyledMessage("CM initialization error!\n", "bold");
				}
				else
				{
					printStyledMessage("Server CM starts.\n", "bold");
					printMessage("Type \"0\" for menu.\n");
					// change button to "stop CM"
					button.setText("Stop Server CM");
				}
				// check if default server or not
				if(CMConfigurator.isDServer(m_serverStub.getCMInfo()))
				{
					setTitle("CM Default Server (\"SERVER\")");
				}
				else
				{
					setTitle("CM Additional Server (\"?\")");
				}
				m_inTextField.requestFocus();
			}
			else if(button.getText().equals("Stop Server CM"))
			{
				// stop cm
				m_serverStub.terminateCM();
				printMessage("Server CM terminates.\n");
				// change button to "start CM"
				button.setText("Start Server CM");
			}
		}
	}

	public class MyMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			String strMenu = e.getActionCommand();
			switch(strMenu)
			{
			case "show all menus":
				printAllMenus();
				break;
			case "start CM":
				startCM();
				break;
			case "terminate CM":
				terminateCM();
				break;
			case "connect to default server":
				connectToDefaultServer();
				break;
			case "disconnect from default server":
				disconnectFromDefaultServer();
				break;
			case "register to default server":
				requestServerReg();
				break;
			case "deregister from default server":
				requestServerDereg();
				break;
			case "show session information":
				printSessionInfo();
				break;
			case "show group information":
				printGroupInfo();
				break;
			case "show current channels":
				printCurrentChannelInfo();
				break;
			case "show login users":
				printLoginUsers();
				break;
			case "show all configurations":
				printConfigurations();
				break;
			case "change configuration":
				changeConfiguration();
				break;
			case "test input network throughput":
				measureInputThroughput();
				break;
			case "test output network throughput":
				measureOutputThroughput();
				break;
			case "set file path":
				setFilePath();
				break;
			case "request file":
				requestFile();
				break;
			case "push file":
				pushFile();
				break;
			case "cancel receiving file":
				cancelRecvFile();
				break;
			case "cancel sending file":
				cancelSendFile();
				break;
			case "print sending/receiving file info":
				printSendRecvFileInfo();
				break;
			case "set attachment download scheme":
				setAttachDownloadScheme();
				break;
			case "add channel":
				addChannel();
				break;
			case "remove channel":
				removeChannel();
				break;
			case "configure SNS user access simulation":
				configureUserAccessSimulation();
				break;
			case "start SNS user access simulation":
				startUserAccessSimulation();
				break;
			case "start SNS user access simulation and measure prefetch accuracy":
				startUserAccessSimulationAndCalPrecRecall();
				break;
			case "start and write recent SNS access history simulation to CM DB":
				writeRecentAccHistoryToDB();
				break;
			case "find session info":
				findMqttSessionInfo();
				break;
			case "print all session info":
				printAllMqttSessionInfo();
				break;
			case "print all retain info":
				printAllMqttRetainInfo();
				break;
			}
		}
	}

	public static void main(String[] args)
	{
		CMWinServer server = new CMWinServer();
		CMServerStub cmStub = server.getServerStub();
		cmStub.setAppEventHandler(server.getServerEventHandler());
	}
}
