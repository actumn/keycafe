import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Random;

import javax.swing.*;
import javax.swing.text.*;

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
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMWinClient extends JFrame {

	private static final long serialVersionUID = 1L;
	//private JTextArea m_outTextArea;
	private JTextPane m_outTextPane;
	private JTextField m_inTextField;
	private JButton m_startStopButton;
	private JButton m_loginLogoutButton;
	//private JPanel m_leftButtonPanel;
	//private JScrollPane m_westScroll;
	private JButton m_composeSNSContentButton;
	private JButton m_readNewSNSContentButton;
	private JButton m_readNextSNSContentButton;
	private JButton m_readPreviousSNSContentButton;
	private JButton m_findUserButton;
	private JButton m_addFriendButton;
	private JButton m_removeFriendButton;
	private JButton m_friendsButton;
	private JButton m_friendRequestersButton;
	private JButton m_biFriendsButton;
	private MyMouseListener cmMouseListener;
	private CMClientStub m_clientStub;
	private CMWinClientEventHandler m_eventHandler;
	
	CMWinClient()
	{		
		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();
		cmMouseListener = new MyMouseListener();
		setTitle("CM Client");
		setSize(600, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setMenus();
		setLayout(new BorderLayout());

		m_outTextPane = new JTextPane();
		m_outTextPane.setBackground(new Color(245,245,245));
		//m_outTextPane.setForeground(Color.WHITE);
		m_outTextPane.setEditable(false);

		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);
		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane centerScroll = new JScrollPane (m_outTextPane, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//add(centerScroll);
		getContentPane().add(centerScroll, BorderLayout.CENTER);
		
		m_inTextField = new JTextField();
		m_inTextField.addKeyListener(cmKeyListener);
		add(m_inTextField, BorderLayout.SOUTH);
		
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setBackground(new Color(220,220,220));
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);
		
		m_startStopButton = new JButton("Start Client CM");
		//m_startStopButton.setBackground(Color.LIGHT_GRAY);	// not work on Mac
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setEnabled(false);
		//add(startStopButton, BorderLayout.NORTH);
		topButtonPanel.add(m_startStopButton);
		
		m_loginLogoutButton = new JButton("Login");
		m_loginLogoutButton.addActionListener(cmActionListener);
		m_loginLogoutButton.setEnabled(false);
		topButtonPanel.add(m_loginLogoutButton);
		
		/*
		m_leftButtonPanel = new JPanel();
		m_leftButtonPanel.setBackground(new Color(220,220,220));
		m_leftButtonPanel.setLayout(new BoxLayout(m_leftButtonPanel, BoxLayout.Y_AXIS));
		add(m_leftButtonPanel, BorderLayout.WEST);
		m_westScroll = new JScrollPane (m_leftButtonPanel, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//add(westScroll);
		getContentPane().add(m_westScroll, BorderLayout.WEST);

		Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, "SNS");
		JPanel snsPanel = new JPanel();
		snsPanel.setLayout(new BoxLayout(snsPanel, BoxLayout.Y_AXIS));
		snsPanel.setBorder(titledBorder);
		
		m_composeSNSContentButton = new JButton("Compose");
		m_composeSNSContentButton.addActionListener(cmActionListener);
		m_readNewSNSContentButton = new JButton("Read New");
		m_readNewSNSContentButton.addActionListener(cmActionListener);
		m_readNextSNSContentButton = new JButton("Read Next");
		m_readNextSNSContentButton.addActionListener(cmActionListener);
		m_readPreviousSNSContentButton = new JButton("Read Prev");
		m_readPreviousSNSContentButton.addActionListener(cmActionListener);
		m_findUserButton = new JButton("Find user");
		m_findUserButton.addActionListener(cmActionListener);
		m_addFriendButton = new JButton("Add Friend");
		m_addFriendButton.addActionListener(cmActionListener);
		m_removeFriendButton = new JButton("Remove Friend");
		//m_removeFriendButton.setMaximumSize(new Dimension(150,10));
		m_removeFriendButton.addActionListener(cmActionListener);
		m_friendsButton = new JButton("Friends");
		m_friendsButton.addActionListener(cmActionListener);
		m_friendRequestersButton = new JButton("Friend requests");
		//m_friendRequestersButton.setMaximumSize(new Dimension(150,10));
		m_friendRequestersButton.addActionListener(cmActionListener);
		m_biFriendsButton = new JButton("Bi-friends");
		m_biFriendsButton.addActionListener(cmActionListener);
		snsPanel.add(m_composeSNSContentButton);
		snsPanel.add(m_readNewSNSContentButton);
		snsPanel.add(m_readNextSNSContentButton);
		snsPanel.add(m_readPreviousSNSContentButton);
		snsPanel.add(m_findUserButton);
		snsPanel.add(m_addFriendButton);
		snsPanel.add(m_removeFriendButton);
		snsPanel.add(m_friendsButton);
		snsPanel.add(m_friendRequestersButton);
		snsPanel.add(m_biFriendsButton);
		m_leftButtonPanel.add(snsPanel);
		
		m_leftButtonPanel.setVisible(false);
		m_westScroll.setVisible(false);
		*/
		
		setVisible(true);

		// create a CM object and set the event handler
		m_clientStub = new CMClientStub();
		m_eventHandler = new CMWinClientEventHandler(m_clientStub, this);
		
		// start CM
		testStartCM();
		
		m_inTextField.requestFocus();
	}
	
	private void addStylesToDocument(StyledDocument doc)
	{
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");
		
		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);
		
		Style linkStyle = doc.addStyle("link", defStyle);
		StyleConstants.setForeground(linkStyle, Color.BLUE);
		StyleConstants.setUnderline(linkStyle, true);
	}
	
	public CMClientStub getClientStub()
	{
		return m_clientStub;
	}
	
	public CMWinClientEventHandler getClientEventHandler()
	{
		return m_eventHandler;
	}
	
	// set menus
	private void setMenus()
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
		
		JMenu connectSubMenu = new JMenu("Connection");
		JMenuItem connDefaultMenuItem = new JMenuItem("connect to default server");
		connDefaultMenuItem.addActionListener(menuListener);
		connectSubMenu.add(connDefaultMenuItem);
		JMenuItem disconnDefaultMenuItem = new JMenuItem("disconnect from default server");
		disconnDefaultMenuItem.addActionListener(menuListener);
		connectSubMenu.add(disconnDefaultMenuItem);
		JMenuItem connDesigMenuItem = new JMenuItem("connect to designated server");
		connDesigMenuItem.addActionListener(menuListener);
		connectSubMenu.add(connDesigMenuItem);
		JMenuItem disconnDesigMenuItem = new JMenuItem("disconnect from designated server");
		disconnDesigMenuItem.addActionListener(menuListener);
		connectSubMenu.add(disconnDesigMenuItem);

		cmNetworkMenu.add(connectSubMenu);
		
		JMenu loginSubMenu = new JMenu("Login");
		JMenuItem loginDefaultMenuItem = new JMenuItem("login to default server");
		loginDefaultMenuItem.addActionListener(menuListener);
		loginDefaultMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
		loginSubMenu.add(loginDefaultMenuItem);
		JMenuItem syncLoginDefaultMenuItem = new JMenuItem("synchronously login to default server");
		syncLoginDefaultMenuItem.addActionListener(menuListener);
		loginSubMenu.add(syncLoginDefaultMenuItem);
		JMenuItem logoutDefaultMenuItem = new JMenuItem("logout from default server");
		logoutDefaultMenuItem.addActionListener(menuListener);
		loginSubMenu.add(logoutDefaultMenuItem);
		JMenuItem loginDesigMenuItem = new JMenuItem("login to designated server");
		loginDesigMenuItem.addActionListener(menuListener);
		loginSubMenu.add(loginDesigMenuItem);
		JMenuItem logoutDesigMenuItem = new JMenuItem("logout from designated server");
		logoutDesigMenuItem.addActionListener(menuListener);
		loginSubMenu.add(logoutDesigMenuItem);

		cmNetworkMenu.add(loginSubMenu);

		JMenu sessionSubMenu = new JMenu("Session/Group");
		JMenuItem reqSessionInfoDefaultMenuItem = new JMenuItem("request session information from default server");
		reqSessionInfoDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(reqSessionInfoDefaultMenuItem);
		JMenuItem syncReqSessionInfoDefaultMenuItem = new JMenuItem("synchronously request session information "
				+ "from default server");
		syncReqSessionInfoDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(syncReqSessionInfoDefaultMenuItem);
		JMenuItem joinSessionDefaultMenuItem = new JMenuItem("join session of default server");
		joinSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(joinSessionDefaultMenuItem);
		JMenuItem syncJoinSessionDefaultMenuItem = new JMenuItem("synchronously join session of default server");
		syncJoinSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(syncJoinSessionDefaultMenuItem);
		JMenuItem leaveSessionDefaultMenuItem = new JMenuItem("leave session of default server");
		leaveSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(leaveSessionDefaultMenuItem);
		JMenuItem changeGroupDefaultMenuItem = new JMenuItem("change group of default server");
		changeGroupDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(changeGroupDefaultMenuItem);
		JMenuItem printGroupMembersMenuItem = new JMenuItem("print group members");
		printGroupMembersMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(printGroupMembersMenuItem);
		JMenuItem reqSessionInfoDesigMenuItem = new JMenuItem("request session information from designated server");
		reqSessionInfoDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(reqSessionInfoDesigMenuItem);
		JMenuItem joinSessionDesigMenuItem = new JMenuItem("join session of designated server");
		joinSessionDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(joinSessionDesigMenuItem);
		JMenuItem leaveSessionDesigMenuItem = new JMenuItem("leave session of designated server");
		leaveSessionDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(leaveSessionDesigMenuItem);

		cmNetworkMenu.add(sessionSubMenu);
		menuBar.add(cmNetworkMenu);
		
		JMenu cmServiceMenu = new JMenu("Services");
		
		JMenu eventSubMenu = new JMenu("Event Transmission");
		JMenuItem chatMenuItem = new JMenuItem("chat");
		chatMenuItem.addActionListener(menuListener);
		chatMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		eventSubMenu.add(chatMenuItem);
		JMenuItem multicastMenuItem = new JMenuItem("multicast chat in current group");
		multicastMenuItem.addActionListener(menuListener);
		eventSubMenu.add(multicastMenuItem);
		JMenuItem dummyEventMenuItem = new JMenuItem("test CMDummyEvent");
		dummyEventMenuItem.addActionListener(menuListener);
		eventSubMenu.add(dummyEventMenuItem);
		JMenuItem userEventMenuItem = new JMenuItem("test CMUserEvent");
		userEventMenuItem.addActionListener(menuListener);
		eventSubMenu.add(userEventMenuItem);
		JMenuItem datagramMenuItem = new JMenuItem("test datagram event");
		datagramMenuItem.addActionListener(menuListener);
		eventSubMenu.add(datagramMenuItem);
		JMenuItem posMenuItem = new JMenuItem("test user position");
		posMenuItem.addActionListener(menuListener);
		eventSubMenu.add(posMenuItem);
		JMenuItem sendrecvMenuItem = new JMenuItem("test sendrecv");
		sendrecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(sendrecvMenuItem);
		JMenuItem castrecvMenuItem = new JMenuItem("test castrecv");
		castrecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(castrecvMenuItem);
		JMenuItem asyncSendRecvMenuItem = new JMenuItem("test asynchronous sendrecv");
		asyncSendRecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(asyncSendRecvMenuItem);
		JMenuItem asyncCastRecvMenuItem = new JMenuItem("test asynchronous castrecv");
		asyncCastRecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(asyncCastRecvMenuItem);
		
		cmServiceMenu.add(eventSubMenu);
		
		JMenu infoSubMenu = new JMenu("Information");
		JMenuItem groupInfoMenuItem = new JMenuItem("show group information of default server");
		groupInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(groupInfoMenuItem);
		JMenuItem userStatMenuItem = new JMenuItem("show current user status");
		userStatMenuItem.addActionListener(menuListener);
		infoSubMenu.add(userStatMenuItem);
		JMenuItem channelInfoMenuItem = new JMenuItem("show current channels");
		channelInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(channelInfoMenuItem);
		JMenuItem serverInfoMenuItem = new JMenuItem("show current server information");
		serverInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(serverInfoMenuItem);
		JMenuItem groupInfoDesigMenuItem = new JMenuItem("show group information of designated server");
		groupInfoDesigMenuItem.addActionListener(menuListener);
		infoSubMenu.add(groupInfoDesigMenuItem);
		JMenuItem inThroughputMenuItem = new JMenuItem("measure input network throughput");
		inThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(inThroughputMenuItem);
		JMenuItem outThroughputMenuItem = new JMenuItem("measure output network throughput");
		outThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(outThroughputMenuItem);
		JMenuItem showAllConfMenuItem = new JMenuItem("show all configurations");
		showAllConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showAllConfMenuItem);
		JMenuItem changeConfMenuItem = new JMenuItem("change configuration");
		changeConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(changeConfMenuItem);
		
		cmServiceMenu.add(infoSubMenu);
		
		JMenu channelSubMenu = new JMenu("Channel");
		JMenuItem addChannelMenuItem = new JMenuItem("add channel");
		addChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(addChannelMenuItem);
		JMenuItem removeChannelMenuItem = new JMenuItem("remove channel");
		removeChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(removeChannelMenuItem);
		JMenuItem blockChannelMenuItem = new JMenuItem("test blocking channel");
		blockChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(blockChannelMenuItem);
		
		cmServiceMenu.add(channelSubMenu);
		
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
		
		cmServiceMenu.add(fileTransferSubMenu);
		
		JMenu snsSubMenu = new JMenu("Social Network Service");
		JMenuItem reqContentMenuItem = new JMenuItem("request content list");
		reqContentMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqContentMenuItem);
		JMenuItem reqNextMenuItem = new JMenuItem("request next content list");
		reqNextMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqNextMenuItem);
		JMenuItem reqPrevMenuItem = new JMenuItem("request previous content list");
		reqPrevMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqPrevMenuItem);
		JMenuItem reqAttachMenuItem = new JMenuItem("request attached file");
		reqAttachMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqAttachMenuItem);
		JMenuItem uploadMenuItem = new JMenuItem("upload content");
		uploadMenuItem.addActionListener(menuListener);
		snsSubMenu.add(uploadMenuItem);
		
		cmServiceMenu.add(snsSubMenu);
		
		JMenu userSubMenu = new JMenu("User");
		JMenuItem regUserMenuItem = new JMenuItem("register new user");
		regUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(regUserMenuItem);
		JMenuItem deregUserMenuItem = new JMenuItem("deregister user");
		deregUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(deregUserMenuItem);
		JMenuItem findUserMenuItem = new JMenuItem("find registered user");
		findUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(findUserMenuItem);
		JMenuItem addFriendMenuItem = new JMenuItem("add new friend");
		addFriendMenuItem.addActionListener(menuListener);
		userSubMenu.add(addFriendMenuItem);
		JMenuItem removeFriendMenuItem = new JMenuItem("remove friend");
		removeFriendMenuItem.addActionListener(menuListener);
		userSubMenu.add(removeFriendMenuItem);
		JMenuItem showFriendsMenuItem = new JMenuItem("show friends");
		showFriendsMenuItem.addActionListener(menuListener);
		userSubMenu.add(showFriendsMenuItem);
		JMenuItem showRequestersMenuItem = new JMenuItem("show friend requesters");
		showRequestersMenuItem.addActionListener(menuListener);
		userSubMenu.add(showRequestersMenuItem);
		JMenuItem showBiFriendsMenuItem = new JMenuItem("show bi-directional friends");
		showBiFriendsMenuItem.addActionListener(menuListener);
		userSubMenu.add(showBiFriendsMenuItem);
		
		cmServiceMenu.add(userSubMenu);
		
		JMenu pubsubSubMenu = new JMenu("Publish/Subscribe");
		JMenuItem connectMenuItem = new JMenuItem("connect MQTT service");
		connectMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(connectMenuItem);
		JMenuItem pubMenuItem = new JMenuItem("publish");
		pubMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(pubMenuItem);
		JMenuItem subMenuItem = new JMenuItem("subscribe");
		subMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(subMenuItem);
		JMenuItem sessionInfoMenuItem = new JMenuItem("print session info");
		sessionInfoMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(sessionInfoMenuItem);
		JMenuItem unsubMenuItem = new JMenuItem("unsubscribe");
		unsubMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(unsubMenuItem);
		JMenuItem disconMenuItem = new JMenuItem("disconnect MQTT service");
		disconMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(disconMenuItem);
		
		cmServiceMenu.add(pubsubSubMenu);
		
		JMenu otherSubMenu = new JMenu("Other CM Test");
		JMenuItem forwardMenuItem = new JMenuItem("test forwarding scheme");
		forwardMenuItem.addActionListener(menuListener);
		otherSubMenu.add(forwardMenuItem);
		JMenuItem forwardDelayMenuItem = new JMenuItem("test delay of forwarding scheme");
		forwardDelayMenuItem.addActionListener(menuListener);
		otherSubMenu.add(forwardDelayMenuItem);
		JMenuItem repeatSNSMenuItem = new JMenuItem("test repeated request of SNS content list");
		repeatSNSMenuItem.addActionListener(menuListener);
		otherSubMenu.add(repeatSNSMenuItem);
		JMenuItem multiFilesMenuItem = new JMenuItem("pull/push multiple files");
		multiFilesMenuItem.addActionListener(menuListener);
		otherSubMenu.add(multiFilesMenuItem);
		JMenuItem splitFileMenuItem = new JMenuItem("split file");
		splitFileMenuItem.addActionListener(menuListener);
		otherSubMenu.add(splitFileMenuItem);
		JMenuItem mergeFilesMenuItem = new JMenuItem("merge files");
		mergeFilesMenuItem.addActionListener(menuListener);
		otherSubMenu.add(mergeFilesMenuItem);
		JMenuItem distMergeMenuItem = new JMenuItem("distribute and merge file");
		distMergeMenuItem.addActionListener(menuListener);
		otherSubMenu.add(distMergeMenuItem);
		JMenuItem cscFtpMenuItem = new JMenuItem("test csc file transfer");
		cscFtpMenuItem.addActionListener(menuListener);
		otherSubMenu.add(cscFtpMenuItem);
		JMenuItem c2cFtpMenuItem = new JMenuItem("test c2c file transfer");
		c2cFtpMenuItem.addActionListener(menuListener);
		otherSubMenu.add(c2cFtpMenuItem);
		
		cmServiceMenu.add(otherSubMenu);

		menuBar.add(cmServiceMenu);
	
		setJMenuBar(menuBar);

	}
	
	// initialize button titles
	public void initializeButtons()
	{
		m_startStopButton.setText("Start Client CM");
		m_loginLogoutButton.setText("Login");
		//m_leftButtonPanel.setVisible(false);
		//m_westScroll.setVisible(false);
		revalidate();
		repaint();
	}
	
	// set button titles
	public void setButtonsAccordingToClientState()
	{
		int nClientState;
		nClientState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();
		
		// nclientState: CMInfo.CM_INIT, CMInfo.CM_CONNECT, CMInfo.CM_LOGIN, CMInfo.CM_SESSION_JOIN
		switch(nClientState)
		{
		case CMInfo.CM_INIT:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_CONNECT:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_LOGIN:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Logout");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_SESSION_JOIN:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Logout");
			//m_leftButtonPanel.setVisible(true);
			//m_westScroll.setVisible(true);
			break;
		default:
			m_startStopButton.setText("Start Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		}
		revalidate();
		repaint();
	}
	
	public void printMessage(String strText)
	{
		/*
		m_outTextArea.append(strText);
		m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());
		*/
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

		File f = new File(strPath);
		if(!f.exists())
		{
			printMessage(strPath+"\n");
			return;
		}
		
		ImageIcon icon = new ImageIcon(strPath);
		Image image = icon.getImage();
		nImageWidth = image.getWidth(m_outTextPane);
		nImageHeight = image.getHeight(m_outTextPane);
		
		if(nImageWidth > nTextPaneWidth/2)
		{
			nNewWidth = nTextPaneWidth / 2;
			float fRate = (float)nNewWidth/(float)nImageWidth;
			nNewHeight = (int)(nImageHeight * fRate);
			Image newImage = image.getScaledInstance(nNewWidth, nNewHeight, java.awt.Image.SCALE_SMOOTH);
			icon = new ImageIcon(newImage);
		}
		
		m_outTextPane.insertIcon ( icon );
		printMessage("\n");
	}
	
	public void printFilePath(String strPath)
	{
		JLabel pathLabel = new JLabel(strPath);
		pathLabel.addMouseListener(cmMouseListener);
		m_outTextPane.insertComponent(pathLabel);
		printMessage("\n");
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
		case 110: // test and measure delay of c-s-c file transfer
			testCSCFileTransfer();
			break;
		case 111: // test and measure delay of c2c file transfer
			testC2CFileTransfer();
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
	
	public void printAllMenus()
	{
		printMessage("---------------------------------- Help\n");
		printMessage("0: show all menus\n");
		printMessage("---------------------------------- Start/Stop\n");
		printMessage("100: start CM, 999: terminate CM\n");
		printMessage("---------------------------------- Connection\n");
		printMessage("1: connect to default server, 2: disconnect from default server\n");
		printMessage("3: connect to designated server, 4: disconnect from designated server\n");
		printMessage("---------------------------------- Login\n");
		printMessage("10: login to default server, 11: synchronously login to default server\n");
		printMessage("12: logout from default server\n");
		printMessage("13: login to designated server, 14: logout from designated server\n");
		printMessage("---------------------------------- Session/Group\n");
		printMessage("20: request session information from default server\n");
		printMessage("21: synchronously request session information from default server\n");
		printMessage("22: join session of default server, 23: synchronously join session of default server\n");
		printMessage("24: leave session of default server, 25: change group of default server\n");
		printMessage("26: print group members\n");
		printMessage("27: request session information from designated server\n");
		printMessage("28: join session of designated server, 29: leave session of designated server\n");
		printMessage("---------------------------------- Event Transmission\n");
		printMessage("40: chat, 41: multicast chat in current group\n");
		printMessage("42: test CMDummyEvent, 43: test CMUserEvent, 44: test datagram event, 45: test user position\n");
		printMessage("46: test sendrecv, 47: test castrecv\n");
		printMessage("48: test asynchronous sendrecv, 49: test asynchronous castrecv\n");
		printMessage("---------------------------------- Information\n");
		printMessage("50: show group information of default server, 51: show current user status\n");
		printMessage("52: show current channels, 53: show current server information\n");
		printMessage("54: show group information of designated server\n");
		printMessage("55: measure input network throughput, 56: measure output network throughput\n");
		printMessage("57: show all configurations, 58: change configuration\n");
		printMessage("---------------------------------- Channel\n");
		printMessage("60: add channel, 61: remove channel, 62: test blocking channel\n");
		printMessage("---------------------------------- File Transfer\n");
		printMessage("70: set file path, 71: request file, 72: push file\n");
		printMessage("73: cancel receiving file, 74: cancel sending file\n");
		printMessage("75: print sending/receiving file info\n");
		printMessage("---------------------------------- Social Network Service\n");
		printMessage("80: request content list, 81: request next content list, 82: request previous content list\n");
		printMessage("83: request attached file, 84: upload content\n");
		printMessage("---------------------------------- User\n");
		printMessage("90: register new user, 91: deregister user, 92: find registered user\n");
		printMessage("93: add new friend, 94: remove friend, 95: show friends, 96: show friend requesters\n");
		printMessage("97: show bi-directional friends\n");
		printMessage("---------------------------------- MQTT\n");
		printMessage("200: connect, 201: publish, 202: subscribe, 203: print session info\n");
		printMessage("204: unsubscribe, 205: disconnect \n");
		printMessage("---------------------------------- Other CM Tests\n");
		printMessage("101: test forwarding scheme, 102: test delay of forwarding scheme\n");
		printMessage("103: test repeated request of SNS content list\n");
		printMessage("104: pull/push multiple files, 105: split file, 106: merge files, 107: distribute and merge file\n");
		printMessage("108: send event with wrong # bytes, 109: send event with wrong type\n");
		printMessage("110: test csc file transfer, 111: test c2c file transfer\n");
	}
	
	public void testConnectionDS()
	{
		printMessage("====== connect to default server\n");
		boolean ret = m_clientStub.connectToServer();
		if(ret)
		{
			printMessage("Successfully connected to the default server.\n");
		}
		else
		{
			printMessage("Cannot connect to the default server!\n");
		}
		printMessage("======\n");
	}
	
	public void testDisconnectionDS()
	{
		printMessage("====== disconnect from default server\n");
		boolean ret = m_clientStub.disconnectFromServer();
		if(ret)
		{
			printMessage("Successfully disconnected from the default server.\n");
		}
		else
		{
			printMessage("Error while disconnecting from the default server!");
		}
		printMessage("======\n");
		
		setButtonsAccordingToClientState();
		setTitle("CM Client");
	}
	
	public void testLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;

		printMessage("====== login to default server\n");
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = {
				"User Name:", userNameField,
				"Password:", passwordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword()); // security problem?
			
			m_eventHandler.setStartTime(System.currentTimeMillis());
			bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(bRequestResult)
			{
				printMessage("successfully sent the login request.\n");
				printMessage("return delay: "+lDelay+" ms.\n");
			}
			else
			{
				printStyledMessage("failed the login request!\n", "bold");
				m_eventHandler.setStartTime(0);
			}
		}
		
		printMessage("======\n");
	}
	
	public void testSyncLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		CMSessionEvent loginAckEvent = null;

		printMessage("====== synchronous login to default server\n");
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = {
				"User Name:", userNameField,
				"Password:", passwordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword()); // security problem?
			
			m_eventHandler.setStartTime(System.currentTimeMillis());
			loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(loginAckEvent != null)
			{
				// print login result
				if(loginAckEvent.isValidUser() == 0)
				{
					printMessage("This client fails authentication by the default server!\n");		
				}
				else if(loginAckEvent.isValidUser() == -1)
				{
					printMessage("This client is already in the login-user list!\n");
				}
				else
				{
					printMessage("return delay: "+lDelay+" ms.\n");
					printMessage("This client successfully logs in to the default server.\n");
					CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
					
					// Change the title of the client window
					setTitle("CM Client ("+interInfo.getMyself().getName()+")");

					// Set the appearance of buttons in the client frame window
					setButtonsAccordingToClientState();
				}				
			}
			else
			{
				printStyledMessage("failed the login request!\n", "bold");
			}
			
		}
		
		printMessage("======\n");		
	}

	public void testLogoutDS()
	{
		boolean bRequestResult = false;
		printMessage("====== logout from default server\n");
		bRequestResult = m_clientStub.logoutCM();
		if(bRequestResult)
			printMessage("successfully sent the logout request.\n");
		else
			printStyledMessage("failed the logout request!\n", "bold");
		printMessage("======\n");

		// Change the title of the login button
		setButtonsAccordingToClientState();
		setTitle("CM Client");
	}

	public void testStartCM()
	{
		boolean bRet = false;
		
		// get current server info from the server configuration file
		String strCurServerAddress = null;
		int nCurServerPort = -1;
		
		strCurServerAddress = m_clientStub.getServerAddress();
		nCurServerPort = m_clientStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		JTextField serverAddressTextField = new JTextField(strCurServerAddress);
		JTextField serverPortTextField = new JTextField(String.valueOf(nCurServerPort));
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
			if(!strNewServerAddress.equals(strCurServerAddress) || nNewServerPort != nCurServerPort)
				m_clientStub.setServerInfo(strNewServerAddress, nNewServerPort);
		}
		
		bRet = m_clientStub.startCM();
		if(!bRet)
		{
			printStyledMessage("CM initialization error!\n", "bold");
		}
		else
		{
			m_startStopButton.setEnabled(true);
			m_loginLogoutButton.setEnabled(true);
			printStyledMessage("Client CM starts.\n", "bold");
			printStyledMessage("Type \"0\" for menu.\n", "regular");
			// change the appearance of buttons in the client window frame
			setButtonsAccordingToClientState();
		}
	}
	
	public void testTerminateCM()
	{
		//m_clientStub.disconnectFromServer();
		m_clientStub.terminateCM();
		printMessage("Client CM terminates.\n");
		// change the appearance of buttons in the client window frame
		initializeButtons();
		setTitle("CM Client");
	}

	public void testSessionInfoDS()
	{
		boolean bRequestResult = false;
		printMessage("====== request session info from default server\n");
		m_eventHandler.setStartTime(System.currentTimeMillis());
		bRequestResult = m_clientStub.requestSessionInfo();
		long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
		if(bRequestResult)
		{
			printMessage("successfully sent the session-info request.\n");
			printMessage("return delay: "+ lDelay +" ms.\n");
		}
		else
			printStyledMessage("failed the session-info request!\n", "bold");
		printMessage("======\n");
	}
	
	public void testSyncSessionInfoDS()
	{
		CMSessionEvent se = null;
		printMessage("====== synchronous request session info from default server\n");
		m_eventHandler.setStartTime(System.currentTimeMillis());
		se = m_clientStub.syncRequestSessionInfo();
		long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
		if(se == null)
		{
			printStyledMessage("failed the session-info request!\n", "bold");
			return;
		}
		
		printMessage("return delay: "+ lDelay +" ms.\n");
		
		// print the request result
		Iterator<CMSessionInfo> iter = se.getSessionInfoList().iterator();

		printMessage(String.format("%-60s%n", "------------------------------------------------------------"));
		printMessage(String.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num"));
		printMessage(String.format("%-60s%n", "------------------------------------------------------------"));

		while(iter.hasNext())
		{
			CMSessionInfo tInfo = iter.next();
			printMessage(String.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(), 
					tInfo.getPort(), tInfo.getUserNum()));
		}
	
		printMessage("======\n");		
	}

	public void testJoinSession()
	{
		String strSessionName = null;
		boolean bRequestResult = false;
		printMessage("====== join a session\n");
		strSessionName = JOptionPane.showInputDialog("Session Name:");
		if(strSessionName != null)
		{
			m_eventHandler.setStartTime(System.currentTimeMillis());
			bRequestResult = m_clientStub.joinSession(strSessionName);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(bRequestResult)
			{
				printMessage("successfully sent the session-join request.\n");
				printMessage("return delay: "+lDelay+" ms.\n");
			}
			else
				printStyledMessage("failed the session-join request!\n", "bold");
		}
		printMessage("======\n");
	}
	
	public void testSyncJoinSession()
	{
		CMSessionEvent se = null;
		String strSessionName = null;
		printMessage("====== join a session\n");
		strSessionName = JOptionPane.showInputDialog("Session Name:");
		if(strSessionName != null)
		{
			m_eventHandler.setStartTime(System.currentTimeMillis());
			se = m_clientStub.syncJoinSession(strSessionName);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(se != null)
			{
				setButtonsAccordingToClientState();
				// print result of the request
				printMessage("successfully joined a session that has ("+se.getGroupNum()+") groups.\n");
				printMessage("return delay: "+lDelay+" ms.\n");
			}
			else
			{
				printStyledMessage("failed the session-join request!\n", "bold");
			}
		}
				
		printMessage("======\n");		
	}

	public void testLeaveSession()
	{
		boolean bRequestResult = false;
		printMessage("====== leave the current session\n");
		bRequestResult = m_clientStub.leaveSession();
		if(bRequestResult)
			printMessage("successfully sent the leave-session request.\n");
		else
			printStyledMessage("failed the leave-session request!\n", "bold");
		printMessage("======\n");
		setButtonsAccordingToClientState();
	}

	public void testUserPosition()
	{
		CMPosition position = new CMPosition();

		printMessage("====== send user position\n");

		JTextField xField = new JTextField();
		JTextField yField = new JTextField();
		JTextField zField = new JTextField();
		JTextField quatWField = new JTextField();
		JTextField quatXField = new JTextField();
		JTextField quatYField = new JTextField();
		JTextField quatZField = new JTextField();
		Object[] message = {
				"pos(x): ", xField, "pos(y): ", yField, "pos(z): ", zField,
				"quat(w): ", quatWField, "quat(x): ", quatXField, "quat(y): ", quatYField,
				"quat(z): ", quatZField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Position (x,y,z), Quat (w,x,y,z) Input"
				, JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.CANCEL_OPTION) return;
		position.m_p.m_x = Float.parseFloat(xField.getText());
		position.m_p.m_y = Float.parseFloat(yField.getText());
		position.m_p.m_z = Float.parseFloat(zField.getText());
		position.m_q.m_w = Float.parseFloat(quatWField.getText());
		position.m_q.m_x = Float.parseFloat(quatXField.getText());
		position.m_q.m_y = Float.parseFloat(quatYField.getText());
		position.m_q.m_z = Float.parseFloat(quatZField.getText());
		printMessage("Pos input: ("+position.m_p.m_x+", "+position.m_p.m_y+", "+position.m_p.m_z+")\n");
		printMessage("Quat input: ("+position.m_q.m_w+", "+position.m_q.m_x+", "
				+position.m_q.m_y+", "+position.m_q.m_z+")\n");
		
		m_clientStub.sendUserPosition(position);
		
		printMessage("======\n");
	}

	public void testChat()
	{
		String strTarget = null;
		String strMessage = null;

		printMessage("====== chat\n");

		JTextField targetField = new JTextField();
		JTextField messageField = new JTextField();
		Object[] message = {
				"Target(/b, /s, /g, or /username): ", targetField,
				"Message: ", messageField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Chat Input", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strTarget = targetField.getText();
			strMessage = messageField.getText();
			m_clientStub.chat(strTarget, strMessage);
		}
		
		printMessage("======\n");
	}

	public void testDummyEvent()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		String strInput = null;
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			printMessage("You should join a session and a group!\n");
			return;
		}
		
		printMessage("====== test CMDummyEvent in current group\n");

		strInput = JOptionPane.showInputDialog("Input Message: ");
		if(strInput == null) return;
		
		
		CMDummyEvent due = new CMDummyEvent();
		due.setHandlerSession(myself.getCurrentSession());
		due.setHandlerGroup(myself.getCurrentGroup());
		due.setDummyInfo(strInput);
		m_clientStub.cast(due, myself.getCurrentSession(), myself.getCurrentGroup());
		due = null;
		
		printMessage("======\n");
	}

	public void testDatagram()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMUser myself = interInfo.getMyself();

		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			printMessage("You should join a session and a group!\n");
			return;
		}
		
		String strReceiver = null;
		String strMessage = null;
		String strSendPort = null;
		String strRecvPort = null;
		int nSendPort = 0;
		int nRecvPort = 0;
		printMessage("====== test unicast chatting with non-blocking datagram channels\n");

		JTextField receiverField = new JTextField();
		JTextField messageField = new JTextField();
		JTextField sendPortField = new JTextField();
		JTextField recvPortField = new JTextField();
		Object[] message = {
				"Receiver: ", receiverField, 
				"Message: ", messageField,
				"Sender port(empty for default port): ", sendPortField,
				"Receiver port(empty for default port): ", recvPortField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Message Input", JOptionPane.OK_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strReceiver = receiverField.getText();
			strMessage = messageField.getText();
			strSendPort = sendPortField.getText();
			strRecvPort = recvPortField.getText();
			if(strSendPort != null && !strSendPort.isEmpty())
			{
				try {
					nSendPort = Integer.parseInt(strSendPort);					
				}catch(NumberFormatException e) {
					e.printStackTrace();
					nSendPort = confInfo.getUDPPort();
				}
			}
			else
			{
				nSendPort = confInfo.getUDPPort();
			}
			
			if(strRecvPort != null && !strRecvPort.isEmpty())
			{
				try {
					nRecvPort = Integer.parseInt(strRecvPort);					
				}catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
			
			CMInterestEvent ie = new CMInterestEvent();
			ie.setID(CMInterestEvent.USER_TALK);
			ie.setHandlerSession(myself.getCurrentSession());
			ie.setHandlerGroup(myself.getCurrentGroup());
			ie.setUserName(myself.getName());
			ie.setTalk(strMessage);
			//m_clientStub.send(ie, strReceiver, CMInfo.CM_DATAGRAM);
			if(nRecvPort == 0)
				m_clientStub.send(ie, strReceiver, CMInfo.CM_DATAGRAM, nSendPort);
			else
				m_clientStub.send(ie, strReceiver, CMInfo.CM_DATAGRAM, nSendPort, nRecvPort, false);
			ie = null;
		}
		
		printMessage("======\n");
		return;
	}

	public void testUserEvent()
	{
		String strReceiver = null;
		int nValueByteNum = -1;
		CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			printMessage("You should join a session and a group!\n");
			return;
		}

		printMessage("====== test CMUserEvent\n");
		
		String strFieldNum = null;
		int nFieldNum = -1;

		strFieldNum = JOptionPane.showInputDialog("Field Numbers:");
		if(strFieldNum == null) return;
		try{
			nFieldNum = Integer.parseInt(strFieldNum);
		}catch(NumberFormatException e){
			printMessage("Input must be an integer number greater than 0!");
			return;
		}
		
		String strID = null;
		JTextField strIDField = new JTextField();
		JTextField strReceiverField = new JTextField();
		String[] dataTypes = {"CM_INT", "CM_LONG", "CM_FLOAT", "CM_DOUBLE", "CM_CHAR", "CH_STR", "CM_BYTES"};
		JComboBox<String>[] dataTypeBoxes = new JComboBox[nFieldNum]; 
		JTextField[] eventFields = new JTextField[nFieldNum*2];
		Object[] message = new Object[4+nFieldNum*3*2];
		
		for(int i = 0; i < nFieldNum; i++)
		{
			dataTypeBoxes[i] = new JComboBox<String>(dataTypes);
		}
		
		for(int i = 0; i < nFieldNum*2; i++)
		{
			eventFields[i] = new JTextField();
		}
		
		message[0] = "event ID: ";
		message[1] = strIDField;
		message[2] = "Receiver Name: ";
		message[3] = strReceiverField;
		for(int i = 4, j = 0, k = 1; i < 4+nFieldNum*3*2; i+=6, j+=2, k++)
		{
			message[i] = "Data type "+k+":";
			message[i+1] = dataTypeBoxes[k-1];
			message[i+2] = "Field Name "+k+":";
			message[i+3] = eventFields[j];
			message[i+4] = "Field Value "+k+":";
			message[i+5] = eventFields[j+1];
		}
		int option = JOptionPane.showConfirmDialog(null, message, "User Event Input", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strID = strIDField.getText();
			strReceiver = strReceiverField.getText();
			
			CMUserEvent ue = new CMUserEvent();
			ue.setStringID(strID);
			ue.setHandlerSession(myself.getCurrentSession());
			ue.setHandlerGroup(myself.getCurrentGroup());
			
			for(int i = 0, j = 0; i < nFieldNum*2; i+=2, j++)
			{
				if(dataTypeBoxes[j].getSelectedIndex() == CMInfo.CM_BYTES)
				{
					nValueByteNum = Integer.parseInt(eventFields[i+1].getText());
					if(nValueByteNum < 0)
					{
						printMessage("CMClientApp.testUserEvent(), Invalid nValueByteNum("
								+nValueByteNum+")\n");
						ue.removeAllEventFields();
						ue = null;
						return;
					}
					byte[] valueBytes = new byte[nValueByteNum];
					for(int k = 0; k < nValueByteNum; k++)
						valueBytes[k] = 1;	// dummy data
					ue.setEventBytesField(eventFields[i].getText(), nValueByteNum, valueBytes);	
				}
				else
				{
					ue.setEventField(dataTypeBoxes[j].getSelectedIndex(),
							eventFields[i].getText(), eventFields[i+1].getText());
				}
				
			}

			m_clientStub.send(ue, strReceiver);
			ue.removeAllEventFields();
			ue = null;
		}
		
		printMessage("======\n");
		
		return;
	}
	
	// test sendrecv
	public void testSendRecv()
	{
		CMUserEvent ue = new CMUserEvent();
		CMUserEvent rue = null;
		String strTargetName = null;
		int nTimeout = 10000;
		
		// a user event: (id, 111) (string id, "testSendRecv")
		// a reply user event: (id, 222) (string id, "testReplySendRecv")
		
		printMessage("====== test sendrecv\n");
		
		// set a user event
		ue.setID(111);
		ue.setStringID("testSendRecv");
		
		// set event target name
		JTextField targetNameTextField = new JTextField();
		Object[] message = {
				"user event to be sent: (id, 111), (string id, \"testSendRecv\")", 
				"reply event to be received: (id, 222), (string id, \"testReplySendRecv\")",
				"Target name(empty for \"SERVER\"): ", targetNameTextField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Test sendrecv()", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strTargetName = targetNameTextField.getText().trim();
			if(strTargetName.isEmpty())
				strTargetName = m_clientStub.getDefaultServerName();
			printMessage("Target name: "+strTargetName+"\n");
			printMessage("Waiting timeout: "+nTimeout+" ms\n");

			long lStartTime = System.currentTimeMillis();
			rue = (CMUserEvent) m_clientStub.sendrecv(ue, strTargetName, CMInfo.CM_USER_EVENT, 222, nTimeout);
			long lServerResponseDelay = System.currentTimeMillis() - lStartTime;

			if(rue == null)
				printStyledMessage("The reply event is null!\n", "bold");
			else
			{
				printMessage("Synchronously received reply event from ["+rue.getSender()+"]: (type, "+rue.getType()+
						"), (id, "+rue.getID()+"), (string id, "+rue.getStringID()+")\n");
				printMessage("Server response delay: "+lServerResponseDelay+"ms.\n");
			}
			printMessage("======\n");
		}
		
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
		
		printMessage("====== test castrecv\n");
		// set a user event
		ue.setID(112);
		ue.setStringID("testCastRecv");
		
		// set event target session and group
		JTextField targetSessionTextField = new JTextField();
		JTextField targetGroupTextField = new JTextField();
		JTextField minNumReplyEventsTextField = new JTextField();
		Object[] message = {
				"user event to be sent: (id, 112), (string id, \"testCastRecv\")", 
				"reply event to be received: (id, 223), (string id, \"testReplyCastRecv\")",
				"Target session(empty for null): ", targetSessionTextField,
				"Target group(empty for null): ", targetGroupTextField,
				"Minimum number of reply events(empty for 0): ", minNumReplyEventsTextField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Test castrecv()", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strTargetSession = targetSessionTextField.getText().trim();
			ue.setEventField(CMInfo.CM_STR, "Target Session", strTargetSession);
			strTargetGroup = targetGroupTextField.getText().trim();
			ue.setEventField(CMInfo.CM_STR, "Target Group", strTargetGroup);
			strMinNumReplyEvents = minNumReplyEventsTextField.getText().trim();
			
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
				printStyledMessage("Wrong number format!\n", "bold");
				return;
			}
			
			printMessage("Target session: "+strTargetSession+"\n");
			printMessage("Target group: "+strTargetGroup+"\n");
			printMessage("Minimum number of reply events: "+nMinNumReplyEvents+"\n");
			printMessage("Waiting timeout: "+nTimeout+" ms\n");

			long lStartTime = System.currentTimeMillis();
			rueArray = m_clientStub.castrecv(ue, strTargetSession, strTargetGroup, 
					CMInfo.CM_USER_EVENT, 223, nMinNumReplyEvents, nTimeout);
			long lServerResponseDelay = System.currentTimeMillis() - lStartTime;
			if(rueArray == null)
			{
				printStyledMessage("Error in castrecv()!\n", "bold");
				return;
			}
			
			printMessage("Number of synchronously received reply events: "+rueArray.length+"\n");
			printMessage("Reply from: ");
			for(int i = 0; i < rueArray.length; i++)
				printMessage(rueArray[i].getSender()+" ");
			printMessage("\n");
			printMessage("Server response delay: "+lServerResponseDelay+"ms.\n");
			printMessage("======\n");
		}
	
	}
	
	// test asynchronous sendrecv
	public void testAsyncSendRecv()
	{
		CMUserEvent ue = new CMUserEvent();
		boolean bRet = false;
		String strTargetName = null;
		
		// a user event: (id, 111) (string id, "testSendRecv")
		// a reply user event: (id, 222) (string id, "testReplySendRecv")
		
		printMessage("====== test asynchronous sendrecv\n");
		
		// set a user event
		ue.setID(111);
		ue.setStringID("testSendRecv");
		
		// set event target name
		JTextField targetNameTextField = new JTextField();
		Object[] message = {
				"user event to be sent: (id, 111), (string id, \"testSendRecv\")", 
				"reply event to be received: (id, 222), (string id, \"testReplySendRecv\")",
				"Target name(empty for \"SERVER\"): ", targetNameTextField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Test asynchronous sendrecv()", 
				JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strTargetName = targetNameTextField.getText().trim();
			if(strTargetName.isEmpty())
				strTargetName = m_clientStub.getDefaultServerName();
			printMessage("Target name: "+strTargetName+"\n");

			m_eventHandler.setStartTime(System.currentTimeMillis());
			bRet = m_clientStub.send(ue, strTargetName);

			if(!bRet)
				printStyledMessage("Error in asynchronous sendrecv service!\n", "bold");
			printMessage("======\n");
		}
		
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
		
		printMessage("====== test asynchronous castrecv\n");
		// set a user event
		ue.setID(112);
		ue.setStringID("testCastRecv");
		
		// set event target session and group
		JTextField targetSessionTextField = new JTextField();
		JTextField targetGroupTextField = new JTextField();
		JTextField minNumReplyEventsTextField = new JTextField();
		Object[] message = {
				"user event to be sent: (id, 112), (string id, \"testCastRecv\")", 
				"reply event to be received: (id, 223), (string id, \"testReplyCastRecv\")",
				"Target session(empty for null): ", targetSessionTextField,
				"Target group(empty for null): ", targetGroupTextField,
				"Minimum number of reply events(empty for 0): ", minNumReplyEventsTextField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Test asynchronous castrecv()", 
				JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strTargetSession = targetSessionTextField.getText().trim();
			ue.setEventField(CMInfo.CM_STR, "Target Session", strTargetSession);
			strTargetGroup = targetGroupTextField.getText().trim();
			ue.setEventField(CMInfo.CM_STR, "Target Group", strTargetGroup);
			strMinNumReplyEvents = minNumReplyEventsTextField.getText().trim();
			
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
				printStyledMessage("Wrong number format!\n", "bold");
				return;
			}
			
			printMessage("Target session: "+strTargetSession+"\n");
			printMessage("Target group: "+strTargetGroup+"\n");
			printMessage("Minimum number of reply events: "+nMinNumReplyEvents+"\n");

			m_eventHandler.setStartTime(System.currentTimeMillis());
			m_eventHandler.setMinNumWaitedEvents(nMinNumReplyEvents);
			m_eventHandler.setRecvReplyEvents(0);
			bRet = m_clientStub.cast(ue, strTargetSession, strTargetGroup);
			if(!bRet)
			{
				printStyledMessage("Error in asynchronous castrecv service!\n", "bold");
				return;
			}
			
			printMessage("======\n");
		}
	}

	// print group information provided by the default server
	public void testPrintGroupInfo()
	{
		// check local state
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			printMessage("You should join a session and a group.\n");
			return;
		}
		
		CMSession session = interInfo.findSession(myself.getCurrentSession());
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		printMessage("---------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port"));
		printMessage("---------------------------------------------------------\n");
		
		while(iter.hasNext())
		{
			CMGroupInfo gInfo = iter.next();
			printMessage(String.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort()));
		}
		
		return;
	}
	
	public void testCurrentUserStatus()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();

		printMessage("------ for the default server\n");
		printMessage("name("+myself.getName()+"), session("+myself.getCurrentSession()+"), group("
				+myself.getCurrentGroup()+"), udp port("+myself.getUDPPort()+"), state("
				+myself.getState()+"), attachment download scheme("+confInfo.getAttachDownloadScheme()+").\n");
		
		// for additional servers
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while(iter.hasNext())
		{
			CMServer tserver = iter.next();
			if(tserver.getNonBlockSocketChannelInfo().findChannel(0) != null)
			{
				printMessage("------ for additional server["+tserver.getServerName()+"]\n");
				printMessage("current session("+tserver.getCurrentSessionName()+
						"), current group("+tserver.getCurrentGroupName()+"), state("
						+tserver.getClientState()+").");
				
			}
		}
		
		return;
	}

	public void testChangeGroup()
	{
		String strGroupName = null;
		printMessage("====== change group\n");

		strGroupName = JOptionPane.showInputDialog("Group Name: ");
		if(strGroupName != null)
			m_clientStub.changeGroup(strGroupName);
		
		printMessage("======\n");
		return;
	}
	
	public void testPrintGroupMembers()
	{
		printMessage("====== print group members\n");
		CMMember groupMembers = m_clientStub.getGroupMembers();
		CMUser myself = m_clientStub.getMyself();
		printMessage("My name: "+myself.getName()+"\n");
		if(groupMembers == null || groupMembers.isEmpty())
		{
			printStyledMessage("No group member yet!\n", "bold");
			return;
		}
		printMessage(groupMembers.toString()+"\n");
	}

	// ServerSocketChannel is not supported.
	// A server cannot add SocketChannel.
	// For the SocketChannel, available server name must be given as well.
	// For the MulticastChannel, session name and group name known by this client/server must be given. 
	public void testAddChannel()
	{
		int nChType = -1;
		int nChKey = -1; // the channel key for the socket channel
		String strServerName = null;
		String strChAddress = null; // the channel key for the multicast address is the (address, port) pair
		int nChPort = -1; // the channel key for the datagram socket channel, or the multicast port number
		String strSessionName = null;
		String strGroupName = null;
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		boolean result = false;
		boolean isBlock = false;
		SocketChannel sc = null;
		DatagramChannel dc = null;
		boolean isSyncCall = false;
		//long lDelay = -1;
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			CMUser myself = interInfo.getMyself();
			if(myself.getState() != CMInfo.CM_SESSION_JOIN && myself.getState() != CMInfo.CM_LOGIN)
			{
				printMessage("You should login to the default server.\n");
				return;
			}
		}
		
		printMessage("====== add additional channel\n");
		
		// ask channel type, (server name), channel index (integer greater than 0), addr, port
		
		String[] chTypes = {"SocketChannel", "DatagramChannel", "MulticastChannel"};
		JComboBox<String> chTypeBox = new JComboBox<String>(chTypes);
		Object[] message = {
				"Channel Type: ", chTypeBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Channel type", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		nChType = chTypeBox.getSelectedIndex() + 2;

		if(nChType == CMInfo.CM_SOCKET_CHANNEL)
		{
			JRadioButton blockRadioButton = new JRadioButton("Blocking Channel");
			JRadioButton nonBlockRadioButton = new JRadioButton("NonBlocking Channel");
			nonBlockRadioButton.setSelected(true);
			ButtonGroup bGroup = new ButtonGroup();
			bGroup.add(blockRadioButton);
			bGroup.add(nonBlockRadioButton);
			String[] syncAsync = {"synchronous call", "asynchronous call"};
			JComboBox<String> syncAsyncComboBox = new JComboBox<String>(syncAsync);
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
				strServerName = m_clientStub.getDefaultServerName(); // default server name
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
		case CMInfo.CM_SOCKET_CHANNEL:
			if(isBlock)
			{
				if(isSyncCall)
				{
					//m_eventHandler.setStartTime(System.currentTimeMillis());
					sc = m_clientStub.syncAddBlockSocketChannel(nChKey, strServerName);
					//lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
					if(sc != null)
					{
						printMessage("Successfully added a blocking socket channel both "
								+ "at the client and the server: key("+nChKey+"), server("+strServerName+")\n");
						//printMessage("return delay: "+lDelay+" ms.\n");
					}
					else
						printMessage("Failed to add a blocking socket channel both at "
								+ "the client and the server: key("+nChKey+"), server("+strServerName+")\n");					
				}
				else
				{
					//m_eventHandler.setStartTime(System.currentTimeMillis());
					result = m_clientStub.addBlockSocketChannel(nChKey, strServerName);
					//lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
					if(result)
					{
						printMessage("Successfully added a blocking socket channel at the client and "
								+"requested to add the channel info to the server: key("+nChKey+"), server("
								+strServerName+")\n");
						//printMessage("return delay: "+lDelay+" ms.\n");
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
		case CMInfo.CM_DATAGRAM_CHANNEL:
			if(isBlock)
			{
				dc = m_clientStub.addBlockDatagramChannel(nChPort);
				if(dc != null)
					printMessage("Successfully added a blocking datagram socket channel: port("+nChPort+")\n");
				else
					printMessage("Failed to add a blocking datagram socket channel: port("+nChPort+")\n");								
			}
			else
			{
				dc = m_clientStub.addNonBlockDatagramChannel(nChPort);
				if(dc != null)
					printMessage("Successfully added a non-blocking datagram socket channel: port("+nChPort+")\n");
				else
					printMessage("Failed to add a non-blocking datagram socket channel: port("+nChPort+")\n");				
			}
						
			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			result = m_clientStub.addMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
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
		boolean isBlock = false;
		boolean isSyncCall = false;
		//long lDelay = 0;
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			CMUser myself = interInfo.getMyself();
			if(myself.getState() != CMInfo.CM_SESSION_JOIN && myself.getState() != CMInfo.CM_LOGIN)
			{
				printMessage("You should login to the default server.\n");
				return;
			}
		}
		
		printMessage("====== remove additional channel\n");
				
		String[] chTypes = {"SocketChannel", "DatagramChannel", "MulticastChannel"};
		JComboBox<String> chTypeBox = new JComboBox<String>(chTypes);
		Object[] message = {
				"Channel Type: ", chTypeBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Removal of Additional Channel", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		nChType = chTypeBox.getSelectedIndex() + 2;

		if(nChType == CMInfo.CM_SOCKET_CHANNEL)
		{
			JRadioButton blockRadioButton = new JRadioButton("Blocking Channel");
			JRadioButton nonBlockRadioButton = new JRadioButton("NonBlocking Channel");
			nonBlockRadioButton.setSelected(true);
			ButtonGroup bGroup = new ButtonGroup();
			bGroup.add(blockRadioButton);
			bGroup.add(nonBlockRadioButton);
			String syncAsync[] = {"synchronous call", "asynchronous call"};
			JComboBox<String> syncAsyncComboBox = new JComboBox<String>(syncAsync);
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
				strServerName = m_clientStub.getDefaultServerName(); // default server name
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
		case CMInfo.CM_SOCKET_CHANNEL:
			if(isBlock)
			{
				if(isSyncCall)
				{
					//m_eventHandler.setStartTime(System.currentTimeMillis());
					result = m_clientStub.syncRemoveBlockSocketChannel(nChKey, strServerName);
					//lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
					if(result)
					{
						printMessage("Successfully removed a blocking socket channel both "
								+ "at the client and the server: key("+nChKey+"), server ("+strServerName+")\n");
						//printMessage("return delay: "+lDelay+" ms.\n");
					}
					else
						printMessage("Failed to remove a blocking socket channel both at the client "
								+ "and the server: key("+nChKey+"), server ("+strServerName+")\n");					
				}
				else
				{
					//m_eventHandler.setStartTime(System.currentTimeMillis());
					result = m_clientStub.removeBlockSocketChannel(nChKey, strServerName);
					//lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
					if(result)
					{
						printMessage("Successfully removed a blocking socket channel at the client and " 
								+ "requested to remove it at the server: key("+nChKey+"), server("+strServerName+")\n");
						//printMessage("return delay: "+lDelay+" ms.\n");
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
		case CMInfo.CM_DATAGRAM_CHANNEL:
			if(isBlock)
			{
				result = m_clientStub.removeBlockDatagramChannel(nChPort);
				if(result)
					printMessage("Successfully removed a blocking datagram socket channel: port("+nChPort+")\n");
				else
					printMessage("Failed to remove a blocking datagram socket channel: port("+nChPort+")\n");								
			}
			else
			{
				result = m_clientStub.removeNonBlockDatagramChannel(nChPort);
				if(result)
					printMessage("Successfully removed a non-blocking datagram socket channel: port("+nChPort+")\n");
				else
					printMessage("Failed to remove a non-blocking datagram socket channel: port("+nChPort+")\n");				
			}
			
			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			result = m_clientStub.removeAdditionalMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
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

	public void testSetFilePath()
	{
		printMessage("====== set file path\n");
		String strPath = null;
		
		strPath = JOptionPane.showInputDialog("file path: ");
		if(strPath == null) return;
		
		m_clientStub.setTransferedFileHome(Paths.get(strPath));
		
		printMessage("======\n");
	}

	public void testRequestFile()
	{
		boolean bReturn = false;
		String strFileName = null;
		String strFileOwner = null;
		byte byteFileAppendMode = -1;
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		
		printMessage("====== request a file\n");

		JTextField fnameField = new JTextField();
		JTextField fownerField = new JTextField();
		String[] fAppendMode = {"Default", "Overwrite", "Append"};		
		JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

		Object[] message = { 
				"File Name: ", fnameField, 
				"File Owner(empty for default server): ", fownerField,
				"File Append Mode: ", fAppendBox 
				};
		int option = JOptionPane.showConfirmDialog(null, message, "File Request", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
		{
			printMessage("canceled!\n");
			return;
		}
		
		strFileName = fnameField.getText().trim();
		if(strFileName.isEmpty())
		{
			printMessage("File name is empty!\n");
			return;
		}
		strFileOwner = fownerField.getText().trim();
		if(strFileOwner.isEmpty())
			strFileOwner = interInfo.getDefaultServerInfo().getServerName();
		
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
		
		bReturn = m_clientStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);
					
		if(!bReturn)
			printMessage("Request file error! file("+strFileName+"), owner("+strFileOwner+").\n");
		
		printMessage("======\n");
	}

	public void testPushFile()
	{
		String strFilePath = null;
		File[] files = null;
		String strReceiver = null;
		byte byteFileAppendMode = -1;
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		boolean bReturn = false;

		printMessage("====== push a file\n");
		
		/*
		strReceiver = JOptionPane.showInputDialog("Receiver Name: ");
		if(strReceiver == null) return;
		*/
		JTextField freceiverField = new JTextField();
		String[] fAppendMode = {"Default", "Overwrite", "Append"};		
		JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

		Object[] message = { 
				"File Receiver(empty for default server): ", freceiverField,
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
			strReceiver = interInfo.getDefaultServerInfo().getServerName();
		
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
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		File curDir = new File(confInfo.getTransferedFileHome().toString());
		fc.setCurrentDirectory(curDir);
		int fcRet = fc.showOpenDialog(this);
		if(fcRet != JFileChooser.APPROVE_OPTION) return;
		files = fc.getSelectedFiles();
		if(files.length < 1) return;
		for(int i=0; i < files.length; i++)
		{
			strFilePath = files[i].getPath();
			bReturn = m_clientStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);
			if(!bReturn)
			{
				printMessage("push file error! file("+strFilePath+"), receiver("
						+strReceiver+")\n");
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
		
		bReturn = m_clientStub.cancelPullFile(strSender);
		
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
		
		bReturn = m_clientStub.cancelPushFile(strReceiver);
		
		if(bReturn)
		{
			if(strReceiver == null)
				strReceiver = "all receivers";
			printMessage("Successfully requested to cancel sending a file to ["+strReceiver+"].\n");
		}
		else
			printMessage("Request failed to cancel sending a file to ["+strReceiver+"]!\n");
		
		return;
	}
	
	public void printSendRecvFileInfo()
	{
		CMFileTransferInfo fInfo = m_clientStub.getCMInfo().getFileTransferInfo();
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
			printMessage("You must log in to the default server.\n");
			return;
		}
		
		printMessage("====== typical/internal forwarding test\n");
		
		String[] ftypes = {"Typical", "Internal"};
		JComboBox<String> ftypeBox = new JComboBox<String>(ftypes);
		JTextField frateField = new JTextField();
		JTextField simnumField = new JTextField();
		Object[] message = {
				"Forwarding Type: ", ftypeBox,
				"Forwarding Rate (0 ~ 1): ", frateField,
				"Simulation Number: ", simnumField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Event Forwarding Test", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		nForwardType = ftypeBox.getSelectedIndex();
		fForwardRate = Float.parseFloat(frateField.getText());
		nSimNum = Integer.parseInt(simnumField.getText());
		
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
					printMessage("Invalid forwarding type: "+nForwardType+"\n");
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
			printMessage("You must log in to the default server.\n");
			return;
		}

		printMessage("====== test delay of forwarding schemes (typical vs. internal\n");
		
		String[] fTypes = {"Typical", "Internal"};
		JComboBox<String> forwardTypeBox = new JComboBox<String>(fTypes);
		JTextField sendNumField = new JTextField();
		Object[] message = {
				"Forward Type: ", forwardTypeBox,
				"Number of Transmission: ", sendNumField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Test Forwarding Delay", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		nForwardType = forwardTypeBox.getSelectedIndex();
		nSendNum = Integer.parseInt(sendNumField.getText());

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
				printMessage("Invalid forward type: "+nForwardType+"\n");
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
		
		printMessage("======\n");
		
		ue = null;
		return;
	}

	public void testDownloadNewSNSContent()
	{
		printMessage("====== request downloading of SNS content (offset 0)\n");

		int nContentOffset = 0;
		String strWriterName = null;
		String strUserName = m_clientStub.getMyself().getName();
		
		JTextField offsetField = new JTextField();
		JTextField writerField = new JTextField();
		Object[] message = {
				"Offset ( >= 0 ): ", offsetField,
				"Content Writer (Empty for no designation, CM_MY_FRIEND for my friends, CM_BI_FRIEND for my bi-friends, "
				+ "or a specific name): ", 
				writerField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Request content download", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		if(offsetField.getText().isEmpty())
			nContentOffset = 0;
		else
			nContentOffset = Integer.parseInt(offsetField.getText());
		strWriterName = writerField.getText();

		// start time of downloading contents
		m_eventHandler.setStartTime(System.currentTimeMillis());

		m_clientStub.requestSNSContent(strWriterName, nContentOffset);
		if(CMInfo._CM_DEBUG)
		{
			printMessage("["+strUserName+"] requests content of writer["+strWriterName+"] with offset("
					+nContentOffset+").\n");
		}

		printMessage("======\n");
		return;
	}
	
	public void testRequestAttachedFileOfSNSContent()
	{
		printMessage("====== request an attached file of SNS content\n");
		String strFileName = null;

		JTextField fileNameField = new JTextField();
		Object[] message = {
				"Attachment File Name: ", fileNameField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Request an attached file of SNS content", 
				JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;

		strFileName = fileNameField.getText();
		
//		m_clientStub.requestAttachedFileOfSNSContent(nContentID, strWriterName, strFileName);
		m_clientStub.requestAttachedFileOfSNSContent(strFileName);
		return;
	}

	public void testRepeatedSNSContentDownload()
	{
		//System.out.println("====== Repeated downloading of SNS content");
		printMessage("====== Repeated downloading of SNS content\n");
		
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
		printMessage("===== Request the next SNS content list\n");
		// start time of downloading contents
		m_eventHandler.setStartTime(System.currentTimeMillis());
		m_clientStub.requestNextSNSContent();
		
		return;		
	}
	
	// download the previous SNS content list
	// if this method is called without any previous download request, it requests the most recent list
	public void testDownloadPreviousSNSContent()
	{
		printMessage("===== Request the previous SNS content list\n");
		// start time of downloading contents
		m_eventHandler.setStartTime(System.currentTimeMillis());
		m_clientStub.requestPreviousSNSContent();
		
		return;		
	}

	public void testSNSContentUpload()
	{
		String strMessage = null;
		ArrayList<String> filePathList = null;
		int nNumAttachedFiles = 0;
		int nReplyOf = 0;
		int nLevelOfDisclosure = 0;
		File[] files = null;

		printMessage("====== test SNS content upload\n");
		
		JTextField msgField = new JTextField();
		JCheckBox attachedFilesBox = new JCheckBox();
		JTextField replyOfField = new JTextField();
		String[] lod = {"Everyone", "My Followers", "Bi-Friends", "Nobody"};
		JComboBox<String> lodBox = new JComboBox<String>(lod);
		Object[] message = {
				"Input Message: ", msgField,
				"File Attachment: ", attachedFilesBox,
				"Content ID to which this content replies(0 for no reply): ", replyOfField,
				//"Level of Disclosure(0: to everyone, 1: to my followers, 2: to bi-friends, 3: nobody): ", lodField
				"Level of Disclosure: ", lodBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "SNS Content Upload", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strMessage = msgField.getText();
			String strReplyOf = replyOfField.getText();
			if(!strReplyOf.isEmpty())
				nReplyOf = Integer.parseInt(strReplyOf);
			else
				nReplyOf = 0;

			nLevelOfDisclosure = lodBox.getSelectedIndex();
			System.out.println("selected lod: "+nLevelOfDisclosure);
			
			if(attachedFilesBox.isSelected())
			{
				JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(true);
				CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
				File curDir = new File(confInfo.getTransferedFileHome().toString());
				fc.setCurrentDirectory(curDir);
				int fcRet = fc.showOpenDialog(this);
				if(fcRet == JFileChooser.APPROVE_OPTION)
				{
					files = fc.getSelectedFiles();
					if(files.length > 0)
					{
						nNumAttachedFiles = files.length;
						filePathList = new ArrayList<String>();
						for(int i = 0; i < nNumAttachedFiles; i++)
						{
							String strPath = files[i].getPath();
							filePathList.add(strPath);
						}
					}
				}
			}			
			
			String strUser = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
			m_clientStub.requestSNSContentUpload(strUser, strMessage, nNumAttachedFiles, nReplyOf, nLevelOfDisclosure, 
					filePathList);
		}

		return;
	}

	public void testRegisterUser()
	{
		String strName = null;
		String strPasswd = null;
		String strRePasswd = null;
		
		printMessage("====== register a user\n");
		JTextField nameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		JPasswordField rePasswordField = new JPasswordField();
		Object[] message = {
				"Input User Name: ", nameField,
				"Input Password: ", passwordField,
				"Retype Password: ", rePasswordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "User Registration", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		strName = nameField.getText();
		strPasswd = new String(passwordField.getPassword());	// security problem?
		strRePasswd = new String(rePasswordField.getPassword());// security problem?

		if(!strPasswd.equals(strRePasswd))
		{
			printMessage("Password input error!\n");
			return;
		}

		m_clientStub.registerUser(strName, strPasswd);
		printMessage("======\n");
		
		return;
	}

	public void testDeregisterUser()
	{
		String strName = null;
		String strPasswd = null;
		
		printMessage("====== Deregister a user\n");
		JTextField nameField = new JTextField();
		JPasswordField passwdField = new JPasswordField();
		Object[] message = {
				"Input User Name: ", nameField,
				"Input Password: ", passwdField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "User Deregistration", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		strName = nameField.getText();
		strPasswd = new String(passwdField.getPassword());	// security problem?
		
		m_clientStub.deregisterUser(strName, strPasswd);

		printMessage("======\n");
		
		return;
	}

	public void testFindRegisteredUser()
	{
		String strName = null;
		
		printMessage("====== search for a registered user\n");
		strName = JOptionPane.showInputDialog("Input User Name: ");
		if(strName != null)
			m_clientStub.findRegisteredUser(strName);

		printMessage("======\n");
		
		return;
	}

	public void testAddNewFriend()
	{
		String strFriendName = null;
		
		printMessage("====== add a new friend\n");
		printMessage("A friend must be a registered user in CM\n");
		strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
		if(strFriendName != null)
			m_clientStub.addNewFriend(strFriendName);
		
		return;
	}

	public void testRemoveFriend()
	{
		String strFriendName = null;
		
		printMessage("====== remove a friend\n");
		strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
		if(strFriendName != null)
			m_clientStub.removeFriend(strFriendName);
		
		return;
	}
	
	public void testRequestFriendsList()
	{
		printMessage("====== request current friends list\n");
		m_clientStub.requestFriendsList();
		return;
	}

	public void testRequestFriendRequestersList()
	{
		printMessage("====== request friend requesters list\n");
		m_clientStub.requestFriendRequestersList();
		return;
	}

	public void testRequestBiFriendsList()
	{
		printMessage("====== request bi-directional friends list\n");
		m_clientStub.requestBiFriendsList();
		return;
	}

	public void testRequestServerInfo()
	{
		printMessage("====== request additional server information\n");
		m_clientStub.requestServerInfo();
	}

	public void testConnectToServer()
	{
		printMessage("====== connect to a designated server\n");
		String strServerName = null;
		
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName != null)
			m_clientStub.connectToServer(strServerName);
		
		return;
	}

	public void testDisconnectFromServer()
	{
		printMessage("===== disconnect from a designated server\n");
		
		String strServerName = null;
		
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName != null)
			m_clientStub.disconnectFromServer(strServerName);

		return;
	}

	public void testLoginServer()
	{
		String strServerName = null;
		String user = null;
		String password = null;
						
		printMessage("====== log in to a designated server\n");
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if(strServerName == null) return;

		if( strServerName.equals(m_clientStub.getDefaultServerName()) )	// login to a default server
		{
			JTextField userNameField = new JTextField();
			JPasswordField passwordField = new JPasswordField();
			Object[] message = {
					"User Name:", userNameField,
					"Password:", passwordField
			};
			int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION)
			{
				user = userNameField.getText();
				String strPassword = new String(passwordField.getPassword()); // security problem?

				m_clientStub.loginCM(user, strPassword);
			}
		}
		else // use the login info for the default server
		{
			CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
			user = myself.getName();
			password = myself.getPasswd();
			m_clientStub.loginCM(strServerName, user, password);
		}
		
		printMessage("======\n");
		
		return;
	}

	public void testLogoutServer()
	{
		String strServerName = null;
		
		printMessage("====== log out from a designated server\n");
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if(strServerName != null)
			m_clientStub.logoutCM(strServerName);
		
		printMessage("======\n");
	}

	public void testRequestSessionInfoOfServer()
	{
		String strServerName = null;
		printMessage("====== request session informatino of a designated server\n");
		
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if(strServerName != null)
			m_clientStub.requestSessionInfo(strServerName);
		
		printMessage("======\n");
		
		return;
	}

	public void testJoinSessionOfServer()
	{
		String strServerName = null;
		String strSessionName = null;
		
		printMessage("====== join a session of a designated server\n");
		JTextField serverField = new JTextField();
		JTextField sessionField = new JTextField();
		Object[] message = {
				"Server Name", serverField, "Session Name", sessionField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Join Session", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strServerName = serverField.getText();
			strSessionName = sessionField.getText();
			m_clientStub.joinSession(strServerName, strSessionName);
		}
		
		printMessage("======\n");
		
		return;
	}

	public void testLeaveSessionOfServer()
	{
		String strServerName = null;
		
		printMessage("====== leave a session of a designated server\n");
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName != null)
			m_clientStub.leaveSession(strServerName);
		
		printMessage("======\n");
		
		return;
	}

	public void testPrintGroupInfoOfServer()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		String strServerName = null;
		
		printMessage("====== print group information a designated server\n");
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName == null) return;
		
		if(strServerName.equals(m_clientStub.getDefaultServerName()))
		{
			testPrintGroupInfo();
			return;
		}
		
		CMServer server = interInfo.findAddServer(strServerName);
		if(server == null)
		{
			printMessage("server("+strServerName+") not found in the add-server list!\n");
			return;
		}
		
		CMSession session = server.findSession(myself.getCurrentSession());
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		printMessage("---------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port"));
		printMessage("---------------------------------------------------------\n");
		
		while(iter.hasNext())
		{
			CMGroupInfo gInfo = iter.next();
			printMessage(String.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort()));
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
		
		printMessage("====== pull/push multiple files\n");
		
		String[] modes = {"Push", "Pull"};
		JComboBox<String> modeBox = new JComboBox<String>(modes);
		JTextField targetField = new JTextField();
		JTextField fileNumField = new JTextField();
		JTextField fileNamesField = new JTextField();
		Object[] message = {
				"Transmission Mode", modeBox,
				"File Receiver or Owner", targetField,
				"Number of Files", fileNumField,
				"File Names Separated with Space", fileNamesField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Push/Pull Multiple Files", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		nMode = modeBox.getSelectedIndex();
		strTarget = targetField.getText();
		try{
			nFileNum = Integer.parseInt(fileNumField.getText());
		}catch(NumberFormatException e){
			printMessage("Number of files must be an integer!\n");
			return;
		}
		strFileList = fileNamesField.getText();
		
		strFileList.trim();
		strFiles = strFileList.split("\\s+");
		if(strFiles.length != nFileNum)
		{
			printMessage("The number of files incorrect!\n");
			return;
		}
		
		for(int i = 0; i < nFileNum; i++)
		{
			switch(nMode)
			{
			case 0: // push
				CMFileTransferManager.pushFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
				break;
			case 1: // pull
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
		
		printMessage("====== split a file\n");
		
		try {
			JTextField fileField = new JTextField();
			JTextField splitNumField = new JTextField();
			Object[] message = {
					"Source File Name", fileField,
					"Number of Split Files", splitNumField
			};
			int option = JOptionPane.showConfirmDialog(null, message, "Split a File", JOptionPane.OK_CANCEL_OPTION);
			if(option != JOptionPane.OK_OPTION) return;
			strSrcFile = fileField.getText();
			try{
				nSplitNum = Integer.parseInt(splitNumField.getText());
			}catch(NumberFormatException ne){
				printMessage("Number of split files must be an integer!");
				return;
			}
			
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
		
		printMessage("====== merge split files\n");
		
		JTextField splitNumField = new JTextField();
		JTextField prefixField = new JTextField();
		JTextField mergeFileNameField = new JTextField();
		Object[] message = {
				"Number of split files", splitNumField,
				"Prefix name of split files", prefixField,
				"Merge file name", mergeFileNameField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Merge Split Files", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		try{
			nFileNum = Integer.parseInt(splitNumField.getText());
		}catch(NumberFormatException e){
			printMessage("Number of split files must be an integer!");
			return;
		}
		strFilePrefix = prefixField.getText();
		strMergeFileName = mergeFileNameField.getText();
				
		// make list of split file names
		strFiles = new String[nFileNum];
		for(int i = 0; i < nFileNum; i++)
		{
			strFiles[i] = strFilePrefix + "-" + (i+1) + ".split";
		}
		
		lMergeFileSize = CMFileTransferManager.mergeFiles(strFiles, nFileNum, strMergeFileName);
		printMessage("Size of merged file("+strMergeFileName+"): "+lMergeFileSize+" Bytes.\n");
		return;
	}

	public void testDistFileProc()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		String strFile = null;
		long lFileSize = 0;
		CMFileEvent fe = null;
		printMessage("====== split a file, distribute to multiple servers, and merge\n");
		
		// check if the client logs in to all available servers
		int nClientState = interInfo.getMyself().getState();
		if(nClientState == CMInfo.CM_INIT || nClientState == CMInfo.CM_CONNECT)
		{
			printMessage("You must log in the default server!\n");
			return;
		}
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while(iter.hasNext())
		{
			CMServer tserver = iter.next();
			nClientState = tserver.getClientState();
			if(nClientState == CMInfo.CM_INIT || nClientState == CMInfo.CM_CONNECT)
			{
				printMessage("You must log in the additional server("+tserver.getServerName()
						+")!\n");
				return;
			}
		}

		// input file name
		strFile = JOptionPane.showInputDialog("Source file path");
		if(strFile == null) return;

		// print the file size
		//strFile = fileInfo.getFilePath()+"/"+strFile;
		File srcFile = new File(strFile);
		lFileSize = srcFile.length();
		printMessage("Source file ("+strFile+"): "+lFileSize+" Bytes.\n");

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
		//System.out.println("Source file extension: "+m_eventHandler.getFileExtension());
		printMessage("Source file extension: "+m_eventHandler.getFileExtension()+"\n");

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
		//fe.setReceiverName(interInfo.getMyself().getName());
		fe.setFileSender(interInfo.getMyself().getName());

		// for pieces except the last piece
		for( i = 0; i < m_eventHandler.getCurrentServerNum()-1; i++)
		{
			// get the piece name
			strPieceName = strPrefix+"-"+(i+1)+".split";
			//System.out.println("File piece name: "+strPieceName);
			printMessage("File piece name: "+strPieceName+"\n");

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
			//System.out.println("File piece name: "+strPieceName);
			printMessage("File piece name: "+strPieceName+"\n");

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
		//System.out.println("====== test multicast chat in current group");
		printMessage("====== test multicast chat in current group\n");

		// check user state
		CMUser myself = interInfo.getMyself();
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			//System.out.println("You must join a session and a group for multicasting.");
			printMessage("You must join a session and a group for multicasting.\n");
			return;
		}

		// check communication architecture
		if(!confInfo.getCommArch().equals("CM_PS"))
		{
			//System.out.println("CM must start with CM_PS mode which enables multicast per group!");
			printMessage("CM must start with CM_PS mode which enables multicast per group!\n");
			return;
		}

		// receive a user input message
		String strMessage = JOptionPane.showInputDialog("Chat Message");
		if(strMessage == null) return;
		
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
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			CMUser myself = interInfo.getMyself();
			if(myself.getState() != CMInfo.CM_SESSION_JOIN && myself.getState() != CMInfo.CM_LOGIN)
			{
				printMessage("You should login to the default server.\n");
				return;
			}
		}
		
		printMessage("============= test blocking channel\n");

		JRadioButton socketRadioButton = new JRadioButton("socket channel");
		JRadioButton datagramRadioButton = new JRadioButton("datagram channel");
		socketRadioButton.setSelected(true);
		ButtonGroup chButtonGroup = new ButtonGroup();
		chButtonGroup.add(socketRadioButton);
		chButtonGroup.add(datagramRadioButton);
		
		JTextField chKeyField = new JTextField();
		JTextField serverField = new JTextField();
		JTextField recvPortField = new JTextField();
		Object[] scMessage = {
				"", socketRadioButton,
				"", datagramRadioButton,
				"Channel key (>=0 or sender port for datagram channel)", chKeyField,
				"Server name(empty for the default server)", serverField,
				"receiver port (only for datagram channel)", recvPortField
		};
		
		int scResponse = JOptionPane.showConfirmDialog(null, scMessage, "test blocking channel", 
				JOptionPane.OK_CANCEL_OPTION);
		if(scResponse != JOptionPane.OK_OPTION) return;
		
		try {
			nChKey = Integer.parseInt(chKeyField.getText());
			if(nChKey < 0)
			{
				System.err.println("Invalid channel key: "+nChKey);
				return;
			}
		}catch(NumberFormatException ne)
		{
			nChKey = -1;
		}
		
		try {
			nRecvPort = Integer.parseInt(recvPortField.getText());						
		}catch(NumberFormatException ne)
		{
			nRecvPort = -1;
		}

		strServerName = serverField.getText();
		if(strServerName == null || strServerName.equals(""))
			strServerName = m_clientStub.getDefaultServerName(); // default server name

		if(socketRadioButton.isSelected())
		{
			sc = m_clientStub.getBlockSocketChannel(nChKey, strServerName);
			if(sc == null)
			{
				printStyledMessage("Blocking socket channel not found: key("+nChKey+"), server("+strServerName+")\n", "bold");
				return;
			}
			printMessage("Blocking socket channel found: key("+nChKey+"), server("+strServerName+")\n");
		}
		else
		{
			dc = m_clientStub.getBlockDatagramChannel(nChKey);
			if(dc == null)
			{
				printStyledMessage("Blocking datagram channel not found: key("+nChKey+")\n", "bold");
				return;
			}
			printMessage("Blocking datagram channel found: key("+nChKey+")\n");
		}
		
		CMUserEvent ue = new CMUserEvent();
		ue.setStringID("reqRecv");
		ue.setEventField(CMInfo.CM_STR, "user", m_clientStub.getMyself().getName());
		if(socketRadioButton.isSelected())
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
		printMessage("========== test input network throughput\n");
		
		strTarget = JOptionPane.showInputDialog("Target node (empty for the default server)");
		if(strTarget == null) 
			return;
		else if(strTarget.equals(""))
			strTarget = m_clientStub.getDefaultServerName();

		fSpeed = m_clientStub.measureInputThroughput(strTarget);
		if(fSpeed == -1)
			printMessage("Test failed!\n");
		else
			printMessage(String.format("Input network throughput from [%s] : %.2f MBps%n", strTarget, fSpeed));
	}
	
	public void testMeasureOutputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		printMessage("========== test output network throughput\n");
		
		strTarget = JOptionPane.showInputDialog("Target node (empty for the default server)");
		if(strTarget == null) 
			return;
		else if(strTarget.equals(""))
			strTarget = m_clientStub.getDefaultServerName();

		fSpeed = m_clientStub.measureOutputThroughput(strTarget);
		if(fSpeed == -1)
			printMessage("Test failed!\n");
		else
			printMessage(String.format("Output network throughput to [%s] : %.2f MBps%n", strTarget, fSpeed));
	}
	
	public void testPrintCurrentChannelInfo()
	{
		printMessage("========== print current channel info\n");
		String strChannels = m_clientStub.getCurrentChannelInfo();
		printMessage(strChannels);
	}
	
	public void testPrintConfigurations()
	{
		String[] strConfigurations;
		printMessage("========== print all current configurations\n");
		Path confPath = m_clientStub.getConfigurationHome().resolve("cm-client.conf");
		strConfigurations = CMConfigurator.getConfigurations(confPath.toString());
		
		printMessage("configuration file path: "+confPath.toString()+"\n");
		for(String strConf : strConfigurations)
		{
			String[] strFieldValuePair;
			strFieldValuePair = strConf.split("\\s+");
			printMessage(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
		}
		
	}
	
	public void testChangeConfiguration()
	{
		boolean bRet = false;
		String strField = null;
		String strValue = null;
		printMessage("========== change configuration\n");
		Path confPath = m_clientStub.getConfigurationHome().resolve("cm-client.conf");
		
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
			printMessage("cm-client.conf file is successfully updated: ("+strField+"="+strValue+")\n");
		}
		else
		{
			printStyledMessage("The configuration change is failed!: ("+strField+"="+strValue+")\n", "bold");
		}
		
		return;
	}
	
	public void testMqttConnect()
	{
		printMessage("========== MQTT connect\n");
		JTextField willTopicTextField = new JTextField();
		JTextField willMessageTextField = new JTextField();
		JCheckBox willRetainBox = new JCheckBox();
		String[] qosArray = {"0", "1", "2"};
		JComboBox<String> willQoSComboBox = new JComboBox<String>(qosArray);
		JCheckBox willFlagBox = new JCheckBox();
		JCheckBox cleanSessionBox = new JCheckBox();
		Object[] msg = {
				"will Topic", willTopicTextField,
				"will message", willMessageTextField,
				"will retain", willRetainBox,
				"will QoS", willQoSComboBox,
				"will flag", willFlagBox,
				"clean session", cleanSessionBox
		};
		int nRet = JOptionPane.showConfirmDialog(null, msg, "MQTT connect", 
				JOptionPane.OK_CANCEL_OPTION);
		if(nRet != JOptionPane.OK_OPTION) return;

		String strWillTopic = willTopicTextField.getText().trim();
		String strWillMessage = willMessageTextField.getText().trim();
		boolean bWillRetain = willRetainBox.isSelected();
		byte willQoS = (byte) willQoSComboBox.getSelectedIndex();
		boolean bWillFlag = willFlagBox.isSelected();
		boolean bCleanSession = cleanSessionBox.isSelected();
		
		CMMqttManager mqttManager = (CMMqttManager) m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		mqttManager.connect(strWillTopic, strWillMessage, bWillRetain, willQoS, bWillFlag, 
				bCleanSession);
		
	}
	
	public void testMqttPublish()
	{
		printMessage("========== MQTT publish\n");
		JTextField topicTextField = new JTextField();
		JTextField messageTextField = new JTextField();
		String[] qosArray = {"0", "1", "2"};
		JComboBox<String> qosComboBox = new JComboBox<String>(qosArray);
		JCheckBox dupFlagBox = new JCheckBox();
		JCheckBox retainFlagBox = new JCheckBox();
		Object[] msg = {
				"topic", topicTextField,
				"message", messageTextField,
				"QoS", qosComboBox,
				"dup flag", dupFlagBox,
				"retain flag", retainFlagBox
		};
		int nRet = JOptionPane.showConfirmDialog(null, msg, "MQTT publish", 
				JOptionPane.OK_CANCEL_OPTION);
		if(nRet != JOptionPane.OK_OPTION) return;

		String strTopic = topicTextField.getText().trim();
		String strMessage = messageTextField.getText().trim();
		byte qos = (byte) qosComboBox.getSelectedIndex();
		boolean bDupFlag = dupFlagBox.isSelected();
		boolean bRetainFlag = retainFlagBox.isSelected();
		
		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		//mqttManager.publish(1, "/CM/test", "This is a test message.", (byte)1);
		mqttManager.publish(strTopic, strMessage, qos, bDupFlag, bRetainFlag);
	}
	
	public void testMqttSubscribe()
	{
		printMessage("========== MQTT subscribe\n");
		JTextField topicFilterTextField = new JTextField();
		String[] qosArray = {"0", "1", "2"};
		JComboBox<String> qosComboBox = new JComboBox<String>(qosArray);
		Object[] msg = {
				"topic filter", topicFilterTextField,
				"QoS", qosComboBox
		};
		int nRet = JOptionPane.showConfirmDialog(null, msg, "MQTT subscribe", 
				JOptionPane.OK_CANCEL_OPTION);
		if(nRet != JOptionPane.OK_OPTION) return;

		String strTopicFilter = topicFilterTextField.getText().trim();
		byte qos = (byte) qosComboBox.getSelectedIndex();

		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		mqttManager.subscribe(strTopicFilter, qos);
	}
	
	public void testPrintMqttSessionInfo()
	{
		printMessage("========== print MQTT session info\n");
		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		printMessage(mqttManager.getMySessionInfo()+"\n");
	}
	
	public void testMqttUnsubscribe()
	{
		printMessage("========== MQTT unsubscribe\n");
		String strTopic = null;
		strTopic = JOptionPane.showInputDialog("Topic to unsubscribe").trim();
		if(strTopic == null || strTopic.equals("")) 
			return;

		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		mqttManager.unsubscribe(strTopic);
	}
	
	public void testMqttDisconnect()
	{
		printMessage("========== MQTT disconnect\n");
		CMMqttManager mqttManager = (CMMqttManager)m_clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		mqttManager.disconnect();
	}
	
	public void testSendEventWithWrongByteNum()
	{
		printMessage("========== send a CMDummyEvent with wrong # bytes to a server\n");
		
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
				printStyledMessage("No server["+strServer+"] found!\n", "bold");
				return;
			}
			ch = addServer.getNonBlockSocketChannelInfo().findChannel(0);
		}
		
		CMDummyEvent due = new CMDummyEvent();
		ByteBuffer buf = CMEventManager.marshallEvent(due);
		buf.clear();
		buf.putInt(-1).clear();
		CMMessage msg = new CMMessage(buf, ch);
		sendQueue.push(msg);
	}
	
	public void testSendEventWithWrongEventType()
	{
		printMessage("========== send a CMDummyEvent with wrong event type to a server\n");
		
		String strServer = JOptionPane.showInputDialog("server name: ").trim();
		
		CMDummyEvent due = new CMDummyEvent();
		due.setType(-1);	// set wrong event type
		m_clientStub.send(due, strServer);
	}
	
	public void testCSCFileTransfer()
	{
		printMessage("========== test and measure delay of c-s-c file transfer\n");
		printMessage("Before the measurement, check the CM configuration files:\n");
		printMessage("(cm-server.conf) PERMIT_FILE_TRANSFER 1\n");
		printMessage("(cm-client.conf of file receiver) PERMIT_FILE_TRANSFER 1\n");
		printMessage("(cm-server.conf) FILE_TRANSFER_SCHEME 0 for non-blocking channel\n");
		printMessage("(cm-server.conf) FILE_TRANSFER_SCHEME 1 for blocking channel\n");
		printMessage("The overwrite mode will be used in the file-transfer task.\n");
		
		// user inputs for the csc-ftp experiment
		String strFileReceiver = null;
		int nNumSessions = 0;
		
		JTextField fileReceiverField = new JTextField();
		JTextField numSessionField = new JTextField();

		Object[] message = { 
				"File receiver client: ", fileReceiverField,
				"Number of file transfer sessions: ", numSessionField 
				};
		int option = JOptionPane.showConfirmDialog(null, message, "C-S-C File Push", 
				JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
		{
			printMessage("canceled.\n");
			return;
		}
		
		strFileReceiver = fileReceiverField.getText().trim();
		try {
			nNumSessions = Integer.parseInt(numSessionField.getText().trim());
		}catch(NumberFormatException e) {
			e.printStackTrace();
			return;
		}

		// select files
		File[] files = null;
		String strFilePath = null;
		boolean bReturn = false;
		
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		File curDir = new File(confInfo.getTransferedFileHome().toString());
		fc.setCurrentDirectory(curDir);
		int fcRet = fc.showOpenDialog(this);
		if(fcRet != JFileChooser.APPROVE_OPTION) return;
		files = fc.getSelectedFiles();
		if(files.length < 1) return;
		
		// send start_csc_ftp_session to the server
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		String strMyName = interInfo.getMyself().getName();
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		CMUserEvent userEvent = new CMUserEvent();
		userEvent.setStringID("start_csc_ftp_session");
		userEvent.setEventField(CMInfo.CM_STR, "strFileSender", strMyName);
		userEvent.setEventField(CMInfo.CM_STR, "strFileReceiver", strFileReceiver);
		userEvent.setEventField(CMInfo.CM_INT, "nNumFilesPerSession", 
				Integer.toString(files.length));
		bReturn = m_clientStub.send(userEvent, strDefServer);
		
		if(!bReturn)
		{
			printMessage("error sending start_csc_ftp_session event!\n");
			return;
		}
		
		// store information in the event handler
		m_eventHandler.setFileSender(strMyName);
		m_eventHandler.setFileReceiver(strFileReceiver);
		m_eventHandler.setSendFileArray(files);
		m_eventHandler.setTotalNumFTPSessions(nNumSessions);
		m_eventHandler.setCurNumFTPSessions(0);
		m_eventHandler.setStartTime(System.currentTimeMillis());

		// send files to the default server
		for(int i=0; i < files.length; i++)
		{
			strFilePath = files[i].getPath();
			bReturn = m_clientStub.pushFile(strFilePath, strDefServer, CMInfo.FILE_OVERWRITE);
			if(!bReturn)
			{
				printMessage("push file error! file("+strFilePath+"), receiver("
						+strDefServer+")\n");
			}
		}

		return;
	}
	
	public void testC2CFileTransfer()
	{
		printMessage("========== test and measure delay of c2c file transfer\n");
		printMessage("Before the measurement, check the CM configuration files:\n");
		printMessage("(cm-server.conf) PERMIT_FILE_TRANSFER 1\n");
		printMessage("(cm-client.conf of file receiver) PERMIT_FILE_TRANSFER 1\n");
		printMessage("(cm-server.conf) FILE_TRANSFER_SCHEME 0 for indirect c2cftp\n");
		printMessage("(cm-server.conf) FILE_TRANSFER_SCHEME 1 for direct c2cftp\n");
		printMessage("The overwrite mode will be used in the file-transfer task.\n");

		// user inputs for the c2c-ftp experiment
		String strFileReceiver = null;
		int nNumSessions = 0;		
		JTextField fileReceiverField = new JTextField();
		JTextField numSessionField = new JTextField();

		Object[] message = { 
				"File receiver client: ", fileReceiverField,
				"Number of file transfer sessions: ", numSessionField 
				};
		int option = JOptionPane.showConfirmDialog(null, message, "C2C File Push", 
				JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
		{
			printMessage("canceled.\n");
			return;
		}
		
		strFileReceiver = fileReceiverField.getText().trim();
		try {
			nNumSessions = Integer.parseInt(numSessionField.getText().trim());
		}catch(NumberFormatException e) {
			e.printStackTrace();
			return;
		}

		// select files
		File[] files = null;
		String strFilePath = null;
		boolean bReturn = false;
		
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		File curDir = new File(confInfo.getTransferedFileHome().toString());
		fc.setCurrentDirectory(curDir);
		int fcRet = fc.showOpenDialog(this);
		if(fcRet != JFileChooser.APPROVE_OPTION) return;
		files = fc.getSelectedFiles();
		if(files.length < 1) return;
		
		// store information in the event handler
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		String strMyName = interInfo.getMyself().getName();
		
		m_eventHandler.setFileSender(strMyName);
		m_eventHandler.setFileReceiver(strFileReceiver);
		m_eventHandler.setSendFileArray(files);
		m_eventHandler.setTotalNumFTPSessions(nNumSessions);
		m_eventHandler.setCurNumFTPSessions(0);
		m_eventHandler.setStartTime(System.currentTimeMillis());
		m_eventHandler.setIsStartC2CFTPSession(true);
		m_eventHandler.setTotalNumFilesPerSession(files.length);
		m_eventHandler.setCurNumFilesPerSession(0);

		// send files to the receiver
		for(int i=0; i < files.length; i++)
		{
			strFilePath = files[i].getPath();
			bReturn = m_clientStub.pushFile(strFilePath, strFileReceiver, CMInfo.FILE_OVERWRITE);
			if(!bReturn)
			{
				printMessage("push file error! file("+strFilePath+"), receiver("
						+strFileReceiver+")\n");
			}
		}

		return;
	}
		
	private void requestAttachedFile(String strFileName)
	{		
		boolean bRet = m_clientStub.requestAttachedFileOfSNSContent(strFileName);
		if(bRet)
			m_eventHandler.setReqAttachedFile(true);
		else
			printMessage(strFileName+" not found in the downloaded content list!\n");
			
		return;
	}

	private void accessAttachedFile(String strFileName)
	{
		boolean bRet = m_clientStub.accessAttachedFileOfSNSContent(strFileName);
		if(!bRet)
			printMessage(strFileName+" not found in the downloaded content list!\n");
		
		return;
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
			else if(key == KeyEvent.VK_ALT)
			{
				
			}
		}
		
		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
	}
	
	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			JButton button = (JButton) e.getSource();
			if(button.getText().equals("Start Client CM"))
			{
				testStartCM();
			}
			else if(button.getText().equals("Stop Client CM"))
			{
				testTerminateCM();
			}
			else if(button.getText().equals("Login"))
			{
				// login to the default cm server
				testLoginDS();
			}
			else if(button.getText().equals("Logout"))
			{
				// logout from the default cm server
				testLogoutDS();
			}
			else if(button.equals(m_composeSNSContentButton))
			{
				testSNSContentUpload();
			}
			else if(button.equals(m_readNewSNSContentButton))
			{
				testDownloadNewSNSContent();
			}
			else if(button.equals(m_readNextSNSContentButton))
			{
				testDownloadNextSNSContent();
			}
			else if(button.equals(m_readPreviousSNSContentButton))
			{
				testDownloadPreviousSNSContent();
			}
			else if(button.equals(m_findUserButton))
			{
				testFindRegisteredUser();
			}
			else if(button.equals(m_addFriendButton))
			{
				testAddNewFriend();
			}
			else if(button.equals(m_removeFriendButton))
			{
				testRemoveFriend();
			}
			else if(button.equals(m_friendsButton))
			{
				testRequestFriendsList();
			}
			else if(button.equals(m_friendRequestersButton))
			{
				testRequestFriendRequestersList();
			}
			else if(button.equals(m_biFriendsButton))
			{
				testRequestBiFriendsList();
			}

			m_inTextField.requestFocus();
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
				testStartCM();
				break;
			case "terminate CM":
				testTerminateCM();
				break;
			case "connect to default server":
				testConnectionDS();
				break;
			case "disconnect from default server":
				testDisconnectionDS();
				break;
			case "connect to designated server":
				testConnectToServer();
				break;
			case "disconnect from designated server":
				testDisconnectFromServer();
				break;
			case "login to default server":
				testLoginDS();
				break;
			case "synchronously login to default server":
				testSyncLoginDS();
				break;
			case "logout from default server":
				testLogoutDS();
				break;
			case "login to designated server":
				testLoginServer();
				break;
			case "logout from designated server":
				testLogoutServer();
				break;
			case "request session information from default server":
				testSessionInfoDS();
				break;
			case "synchronously request session information from default server":
				testSyncSessionInfoDS();
				break;
			case "join session of default server":
				testJoinSession();
				break;
			case "synchronously join session of default server":
				testSyncJoinSession();
				break;
			case "leave session of default server":
				testLeaveSession();
				break;
			case "change group of default server":
				testChangeGroup();
				break;
			case "print group members":
				testPrintGroupMembers();
				break;
			case "request session information from designated server":
				testRequestSessionInfoOfServer();
				break;
			case "join session of designated server":
				testJoinSessionOfServer();
				break;
			case "leave session of designated server":
				testLeaveSessionOfServer();
				break;
			case "chat":
				testChat();
				break;
			case "multicast chat in current group":
				testMulticastChat();
				break;
			case "test CMDummyEvent":
				testDummyEvent();
				break;
			case "test CMUserEvent":
				testUserEvent();
				break;
			case "test datagram event":
				testDatagram();
				break;
			case "test user position":
				testUserPosition();
				break;
			case "test sendrecv":
				testSendRecv();
				break;
			case "test castrecv":
				testCastRecv();
				break;
			case "test asynchronous sendrecv":
				testAsyncSendRecv();
				break;
			case "test asynchronous castrecv":
				testAsyncCastRecv();
				break;
			case "show group information of default server":
				testPrintGroupInfo();
				break;
			case "show current user status":
				testCurrentUserStatus();
				break;
			case "show current channels":
				testPrintCurrentChannelInfo();
				break;
			case "show current server information":
				testRequestServerInfo();
				break;
			case "show group information of designated server":
				testPrintGroupInfoOfServer();
				break;
			case "measure input network throughput":
				testMeasureInputThroughput();
				break;
			case "measure output network throughput":
				testMeasureOutputThroughput();
				break;
			case "show all configurations":
				testPrintConfigurations();
				break;
			case "change configuration":
				testChangeConfiguration();
				break;
			case "add channel":
				testAddChannel();
				break;
			case "remove channel":
				testRemoveChannel();
				break;
			case "test blocking channel":
				testBlockingChannel();
				break;
			case "set file path":
				testSetFilePath();
				break;
			case "request file":
				testRequestFile();
				break;
			case "push file":
				testPushFile();
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
			case "request content list":
				testDownloadNewSNSContent();
				break;
			case "request next content list":
				testDownloadNextSNSContent();
				break;
			case "request previous content list":
				testDownloadPreviousSNSContent();
				break;
			case "request attached file":
				testRequestAttachedFileOfSNSContent();
				break;
			case "upload content":
				testSNSContentUpload();
				break;
			case "register new user":
				testRegisterUser();
				break;
			case "deregister user":
				testDeregisterUser();
				break;
			case "find registered user":
				testFindRegisteredUser();
				break;
			case "add new friend":
				testAddNewFriend();
				break;
			case "remove friend":
				testRemoveFriend();
				break;
			case "show friends":
				testRequestFriendsList();
				break;
			case "show friend requesters":
				testRequestFriendRequestersList();
				break;
			case "show bi-directional friends":
				testRequestBiFriendsList();
				break;
			case "test forwarding scheme":
				testForwarding();
				break;
			case "test delay of forwarding scheme":
				testForwardingDelay();
				break;
			case "test repeated request of SNS content list":
				testRepeatedSNSContentDownload();
				break;
			case "pull/push multiple files":
				testSendMultipleFiles();
				break;
			case "split file":
				testSplitFile();
				break;
			case "merge files":
				testMergeFiles();
				break;
			case "distribute and merge file":
				testDistFileProc();
				break;
			case "connect MQTT service":
				testMqttConnect();
				break;
			case "publish":
				testMqttPublish();
				break;
			case "subscribe":
				testMqttSubscribe();
				break;
			case "print session info":
				testPrintMqttSessionInfo();
				break;
			case "unsubscribe":
				testMqttUnsubscribe();
				break;
			case "disconnect MQTT service":
				testMqttDisconnect();
				break;
			case "test csc file transfer":
				testCSCFileTransfer();
				break;
			case "test c2c file transfer":
				testC2CFileTransfer();
				break;
			}
		}
	}
	
	public class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				JLabel pathLabel = (JLabel)e.getSource();
				String strPath = pathLabel.getText();
				File fPath = new File(strPath);
				try {
					int index = strPath.lastIndexOf(File.separator);
					String strFileName = strPath.substring(index+1, strPath.length()); 
					if(fPath.exists())
					{
						accessAttachedFile(strFileName);
						Desktop.getDesktop().open(fPath);
					}
					else
					{
						requestAttachedFile(strFileName);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				setCursor(cursor);
			}
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				Cursor cursor = Cursor.getDefaultCursor();
				setCursor(cursor);
			}
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CMWinClient client = new CMWinClient();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setAppEventHandler(client.getClientEventHandler());
	}

}
