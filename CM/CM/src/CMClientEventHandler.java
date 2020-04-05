import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import java.io.*;

import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBACK;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContent;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;


public class CMClientEventHandler implements CMAppEventHandler {
	private CMClientStub m_clientStub;
	private long m_lDelaySum;	// for forwarding simulation
	private long m_lStartTime;	// for delay of SNS content downloading, distributed file processing
	private int m_nEstDelaySum;	// for SNS downloading simulation
	private int m_nSimNum;		// for simulation of multiple sns content downloading
	private FileOutputStream m_fos;	// for storing downloading delay of multiple SNS content
	private PrintWriter m_pw;		//
	private int m_nCurrentServerNum;	// for distributed file processing
	private int m_nRecvPieceNum;		// for distributed file processing
	private boolean m_bDistFileProc;	// for distributed file processing
	private String m_strExt;			// for distributed file processing
	private String[] m_filePieces;		// for distributed file processing
	private int m_nMinNumWaitedEvents;  // for checking the completion of asynchronous castrecv service
	private int m_nRecvReplyEvents;		// for checking the completion of asynchronous castrecv service
		
	public CMClientEventHandler(CMClientStub stub)
	{
		m_clientStub = stub;
		m_lDelaySum = 0;
		m_lStartTime = 0;
		m_nEstDelaySum = 0;
		m_nSimNum = 0;
		m_fos = null;
		m_pw = null;
		m_nCurrentServerNum = 0;
		m_nRecvPieceNum = 0;
		m_bDistFileProc = false;
		m_strExt = null;
		m_filePieces = null;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// get/set methods
	
	public void setStartTime(long time)
	{
		m_lStartTime = time;
	}
	
	public void setFileOutputStream(FileOutputStream fos)
	{
		m_fos = fos;
	}
	
	public FileOutputStream getFileOutputStream()
	{
		return m_fos;
	}
	
	public void setPrintWriter(PrintWriter pw)
	{
		m_pw = pw;
	}
	
	public PrintWriter getPrintWriter()
	{
		return m_pw;
	}
	
	public void setSimNum(int num)
	{
		m_nSimNum = num;
	}
	
	public int getSimNum()
	{
		return m_nSimNum;
	}
	
	public void setCurrentServerNum(int num)
	{
		m_nCurrentServerNum = num;
	}
	
	public int getCurrentServerNum()
	{
		return m_nCurrentServerNum;
	}
	
	public void setRecvPieceNum(int num)
	{
		m_nRecvPieceNum = num;
	}
	
	public int getRecvPieceNum()
	{
		return m_nRecvPieceNum;
	}
	
	public void setDistFileProc(boolean b)
	{
		m_bDistFileProc = b;
	}
	
	public boolean isDistFileProc()
	{
		return m_bDistFileProc;
	}
	
	public void setFileExtension(String ext)
	{
		m_strExt = ext;
	}
	
	public String getFileExtension()
	{
		return m_strExt;
	}
	
	public void setFilePieces(String[] pieces)
	{
		m_filePieces = pieces;
	}
	
	public String[] getFilePieces()
	{
		return m_filePieces;
	}
	
	public void setMinNumWaitedEvents(int num)
	{
		m_nMinNumWaitedEvents = num;
	}
	
	public int getMinNumWaitedEvents()
	{
		return m_nMinNumWaitedEvents;
	}
	
	public void setRecvReplyEvents(int num)
	{
		m_nRecvReplyEvents = num;
	}
	
	public int getRecvReplyEvents()
	{
		return m_nRecvReplyEvents;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void processEvent(CMEvent cme) {
		// TODO Auto-generated method stub
		//System.out.println("Client app receives CM event!!");
		switch(cme.getType())
		{
		case CMInfo.CM_SESSION_EVENT:
			processSessionEvent(cme);
			break;
		case CMInfo.CM_INTEREST_EVENT:
			processInterestEvent(cme);
			break;
		case CMInfo.CM_DATA_EVENT:
			processDataEvent(cme);
			break;
		case CMInfo.CM_DUMMY_EVENT:
			processDummyEvent(cme);
			break;
		case CMInfo.CM_USER_EVENT:
			processUserEvent(cme);
			break;
		case CMInfo.CM_FILE_EVENT:
			processFileEvent(cme);
			break;
		case CMInfo.CM_SNS_EVENT:
			processSNSEvent(cme);
			break;
		case CMInfo.CM_MULTI_SERVER_EVENT:
			processMultiServerEvent(cme);
			break;
		case CMInfo.CM_MQTT_EVENT:
			processMqttEvent(cme);
			break;
		default:
			return;
		}
	}
	
	private void processSessionEvent(CMEvent cme)
	{
		CMSessionEvent se = (CMSessionEvent)cme;
		switch(se.getID())
		{
		case CMSessionEvent.LOGIN_ACK:
			if(se.isValidUser() == 0)
			{
				System.err.println("This client fails authentication by the default server!");
			}
			else if(se.isValidUser() == -1)
			{
				System.err.println("This client is already in the login-user list!");
			}
			else
			{
				System.out.println("This client successfully logs in to the default server.");
			}
			break;
		case CMSessionEvent.RESPONSE_SESSION_INFO:
			processRESPONSE_SESSION_INFO(se);
			break;
		case CMSessionEvent.SESSION_TALK:
			System.out.println("("+se.getHandlerSession()+")");
			System.out.println("<"+se.getUserName()+">: "+se.getTalk());
			break;
		case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
			if(se.getReturnCode() == 0)
			{
				System.err.println("Adding a nonblocking SocketChannel("+se.getChannelName()+","+se.getChannelNum()
						+") failed at the server!");
			}
			else
			{
				System.out.println("Adding a nonblocking SocketChannel("+se.getChannelName()+","+se.getChannelNum()
						+") succeeded at the server!");
			}
			break;
		case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK:
			if(se.getReturnCode() == 0)
			{
				System.err.println("Adding a blocking socket channel ("+se.getChannelName()+","+se.getChannelNum()
					+") failed at the server!");
			}
			else
			{
				System.out.println("Adding a blocking socket channel("+se.getChannelName()+","+se.getChannelNum()
					+") succeeded at the server!");
			}
			break;
		case CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
			if(se.getReturnCode() == 0)
			{
				System.err.println("Removing a blocking socket channel ("+se.getChannelName()+","+se.getChannelNum()
					+") failed at the server!");
			}
			else
			{
				System.out.println("Removing a blocking socket channel("+se.getChannelName()+","+se.getChannelNum()
					+") succeeded at the server!");
			}
			break;
		case CMSessionEvent.REGISTER_USER_ACK:
			if( se.getReturnCode() == 1 )
			{
				// user registration succeeded
				System.out.println("User["+se.getUserName()+"] successfully registered at time["
							+se.getCreationTime()+"].");
			}
			else
			{
				// user registration failed
				System.out.println("User["+se.getUserName()+"] failed to register!");
			}
			break;
		case CMSessionEvent.DEREGISTER_USER_ACK:
			if( se.getReturnCode() == 1 )
			{
				// user deregistration succeeded
				System.out.println("User["+se.getUserName()+"] successfully deregistered.");
			}
			else
			{
				// user registration failed
				System.out.println("User["+se.getUserName()+"] failed to deregister!");
			}
			break;
		case CMSessionEvent.FIND_REGISTERED_USER_ACK:
			if( se.getReturnCode() == 1 )
			{
				System.out.println("User profile search succeeded: user["+se.getUserName()
						+"], registration time["+se.getCreationTime()+"].");
			}
			else
			{
				System.out.println("User profile search failed: user["+se.getUserName()+"]!");
			}
			break;
		case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
			System.err.println("Unexpected disconnection from ["+se.getChannelName()
					+"] with key["+se.getChannelNum()+"]!");
			break;
		case CMSessionEvent.INTENTIONALLY_DISCONNECT:
			System.err.println("Intentionally disconnected all channels from ["
					+se.getChannelName()+"]!");
			break;
		default:
			return;
		}
	}
	
	private void processRESPONSE_SESSION_INFO(CMSessionEvent se)
	{
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
	}
	
	private void processInterestEvent(CMEvent cme)
	{
		CMInterestEvent ie = (CMInterestEvent) cme;
		switch(ie.getID())
		{
		case CMInterestEvent.USER_TALK:
			System.out.println("("+ie.getHandlerSession()+", "+ie.getHandlerGroup()+")");
			System.out.println("<"+ie.getUserName()+">: "+ie.getTalk());
			break;
		default:
			return;
		}
	}
	
	private void processDataEvent(CMEvent cme)
	{
		CMDataEvent de = (CMDataEvent) cme;
		switch(de.getID())
		{
		case CMDataEvent.NEW_USER:
			System.out.println("["+de.getUserName()+"] enters group("+de.getHandlerGroup()+") in session("
					+de.getHandlerSession()+").");
			break;
		case CMDataEvent.REMOVE_USER:
			System.out.println("["+de.getUserName()+"] leaves group("+de.getHandlerGroup()+") in session("
					+de.getHandlerSession()+").");
			break;
		default:
			return;
		}
	}
	
	private void processDummyEvent(CMEvent cme)
	{
		CMDummyEvent due = (CMDummyEvent) cme;
		System.out.println("session("+due.getHandlerSession()+"), group("+due.getHandlerGroup()+")");
		System.out.println("dummy msg: "+due.getDummyInfo());
		return;
	}
	
	private void processUserEvent(CMEvent cme)
	{
		int id = -1;
		long lSendTime = 0;
		int nSendNum = 0;
		
		CMUserEvent ue = (CMUserEvent) cme;

		if(ue.getStringID().equals("testForward"))
		{
			id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
			System.out.println("Received user event \'testForward\', id: "+id);
		}
		else if(ue.getStringID().equals("testNotForward"))
		{
			id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
			System.out.println("Received user event 'testNotForward', id("+id+")");
		}
		else if(ue.getStringID().equals("testForwardDelay"))
		{
			id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
			lSendTime = Long.parseLong(ue.getEventField(CMInfo.CM_LONG, "stime"));
			long lDelay = System.currentTimeMillis() - lSendTime;
			m_lDelaySum += lDelay;
			System.out.println("Received user event 'testNotForward', id("+id+"), delay("+lDelay+"), delay_sum("+m_lDelaySum+")");
		}
		else if(ue.getStringID().equals("EndForwardDelay"))
		{
			nSendNum = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "sendnum"));
			System.out.println("Received user envet 'EndForwardDelay', avg delay("+m_lDelaySum/nSendNum+" ms)");
			m_lDelaySum = 0;
		}
		else if(ue.getStringID().equals("repRecv"))
		{
			String strReceiver = ue.getEventField(CMInfo.CM_STR, "receiver");
			int nBlockingChannelType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chType"));
			int nBlockingChannelKey = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chKey"));
			int nRecvPort = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "recvPort"));
			int opt = -1;
			if(nBlockingChannelType == CMInfo.CM_SOCKET_CHANNEL)
				opt = CMInfo.CM_STREAM;
			else if(nBlockingChannelType == CMInfo.CM_DATAGRAM_CHANNEL)
				opt = CMInfo.CM_DATAGRAM;

			CMDummyEvent due = new CMDummyEvent();
			due.setDummyInfo("This is a test message to test a blocking channel");
			System.out.println("Sending a dummy event to ("+strReceiver+")..");
			
			if(opt == CMInfo.CM_STREAM)
				m_clientStub.send(due, strReceiver, opt, nBlockingChannelKey, true);
			else if(opt == CMInfo.CM_DATAGRAM)
				m_clientStub.send(due, strReceiver, opt, nBlockingChannelKey, nRecvPort, true);
			else
				System.err.println("invalid sending option!: "+opt);
		}
		else if(ue.getStringID().equals("testSendRecv"))
		{
			System.out.println("Received user event from ["+ue.getSender()+"] to ["+ue.getReceiver()+
					"], (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")");
			
			if(!m_clientStub.getMyself().getName().equals(ue.getReceiver()))
				return;
			
			CMUserEvent rue = new CMUserEvent();
			rue.setID(222);
			rue.setStringID("testReplySendRecv");
			boolean ret = m_clientStub.send(rue, ue.getSender());
			if(ret)
				System.out.println("Sent reply event: (id, "+rue.getID()+"), (string id, "+rue.getStringID()+")");
			else
				System.err.println("Failed to send the reply event!");			
		}
		else if(ue.getStringID().equals("testCastRecv"))
		{
			System.out.println("Received user event from ["+ue.getSender()+"], to session["+
					ue.getEventField(CMInfo.CM_STR, "Target Session")+"] and group["+
					ue.getEventField(CMInfo.CM_STR,  "Target Group")+"], (id, "+ue.getID()+
					"), (string id, "+ue.getStringID()+")");
			CMUserEvent rue = new CMUserEvent();
			rue.setID(223);
			rue.setStringID("testReplyCastRecv");
			boolean ret = m_clientStub.send(rue, ue.getSender());
			if(ret)
				System.out.println("Sent reply event: (id, "+rue.getID()+"), (sting id, "+rue.getStringID()+")");
			else
				System.err.println("Failed to send the reply event!");
		}
		else if(ue.getStringID().equals("testReplySendRecv")) // for testing asynchronous sendrecv service
		{
			long lServerResponseDelay = System.currentTimeMillis() - m_lStartTime;
			System.out.println("Asynchronously received reply event from ["+ue.getSender()+"]: (type, "+ue.getType()+
					"), (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")");
			System.out.println("Server response delay: "+lServerResponseDelay+"ms.");

		}
		else if(ue.getStringID().equals("testReplyCastRecv")) // for testing asynchronous castrecv service
		{
			System.out.println("Asynchronously received reply event from ["+ue.getSender()+"]: (type, "+ue.getType()+
					"), (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")");
			m_nRecvReplyEvents++;
			
			if(m_nRecvReplyEvents == m_nMinNumWaitedEvents)
			{
				long lServerResponseDelay = System.currentTimeMillis() - m_lStartTime;
				System.out.println("Complete to receive requested number of reply events.");
				System.out.println("Number of received reply events: "+m_nRecvReplyEvents);
				System.out.println("Server response delay: "+lServerResponseDelay+"ms.");
				m_nRecvReplyEvents = 0;
			}
			
		}
		else
		{
			System.out.println("CMUserEvent received from ["+ue.getSender()+"], strID("+ue.getStringID()+")");
			System.out.format("%-5s%-20s%-10s%-20s%n", "Type", "Field", "Length", "Value");
			System.out.println("-----------------------------------------------------");
			Iterator<CMUserEventField> iter = ue.getAllEventFields().iterator();
			while(iter.hasNext())
			{
				CMUserEventField uef = iter.next();
				if(uef.nDataType == CMInfo.CM_BYTES)
				{
					System.out.format("%-5s%-20s%-10d", uef.nDataType, uef.strFieldName, 
										uef.nValueByteNum);
					for(int i = 0; i < uef.nValueByteNum; i++)
						System.out.print(uef.valueBytes[i]);
					System.out.println();
				}
				else
					System.out.format("%-5d%-20s%-10d%-20s%n", uef.nDataType, uef.strFieldName, 
							uef.strFieldValue.length(), uef.strFieldValue);
			}
		}
		return;
	}
	
	private void processFileEvent(CMEvent cme)
	{
		CMFileEvent fe = (CMFileEvent) cme;
		int nOption = -1;
		switch(fe.getID())
		{
		case CMFileEvent.REQUEST_PERMIT_PULL_FILE:
			String strReq = "["+fe.getFileReceiver()+"] requests file("+fe.getFileName()+
			").\n";
			System.out.print(strReq);
			nOption = JOptionPane.showConfirmDialog(null, strReq, "Request a file", 
					JOptionPane.YES_NO_OPTION);
			if(nOption == JOptionPane.YES_OPTION)
			{
				m_clientStub.replyEvent(fe, 1);
			}
			else
			{
				m_clientStub.replyEvent(fe, 0);
			}
			break;
		case CMFileEvent.REPLY_PERMIT_PULL_FILE:
			if(fe.getReturnCode() == -1)
			{
				System.err.print("["+fe.getFileName()+"] does not exist in the owner!\n");
			}
			else if(fe.getReturnCode() == 0)
			{
				System.err.print("["+fe.getFileSender()+"] rejects to send file("
						+fe.getFileName()+").\n");
			}
			break;
		case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
			StringBuffer strReqBuf = new StringBuffer(); 
			strReqBuf.append("["+fe.getFileSender()+"] wants to send a file.\n");
			strReqBuf.append("file path: "+fe.getFilePath()+"\n");
			strReqBuf.append("file size: "+fe.getFileSize()+"\n");
			System.out.print(strReqBuf.toString());
			nOption = JOptionPane.showConfirmDialog(null, strReqBuf.toString(), 
					"Push File", JOptionPane.YES_NO_OPTION);
			if(nOption == JOptionPane.YES_OPTION)
			{
				m_clientStub.replyEvent(fe, 1);
			}
			else
			{
				m_clientStub.replyEvent(fe, 1);
			}				
			break;
		case CMFileEvent.REPLY_PERMIT_PUSH_FILE:
			if(fe.getReturnCode() == 0)
			{
				System.err.print("["+fe.getFileReceiver()+"] rejected the push-file request!\n");
				System.err.print("file path("+fe.getFilePath()+"), size("+fe.getFileSize()+").\n");
			}
			break;
		case CMFileEvent.START_FILE_TRANSFER:
		case CMFileEvent.START_FILE_TRANSFER_CHAN:
			System.out.println("["+fe.getFileSender()+"] is about to send file("+fe.getFileName()+").");
			break;
		case CMFileEvent.END_FILE_TRANSFER:
		case CMFileEvent.END_FILE_TRANSFER_CHAN:
			System.out.println("["+fe.getFileSender()+"] completes to send file("+fe.getFileName()+", "
					+fe.getFileSize()+" Bytes).");
			if(m_bDistFileProc)
				processFile(fe.getFileName());
			break;
		case CMFileEvent.CANCEL_FILE_SEND:
		case CMFileEvent.CANCEL_FILE_SEND_CHAN:
			System.out.println("["+fe.getFileSender()+"] cancelled the file transfer.");
			break;
		case CMFileEvent.CANCEL_FILE_RECV_CHAN:
			System.out.println("["+fe.getFileReceiver()+"] cancelled the file request.");
			break;
		}
		return;
	}
	
	private void processFile(String strFile)
	{
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		String strMergeName = null;

		// add file name to list and increase index
		if(m_nCurrentServerNum == 1)
		{
			m_filePieces[m_nRecvPieceNum++] = confInfo.getTransferedFileHome().toString()+File.separator+strFile; 
		}
		else
		{
			// Be careful to put a file into an appropriate array member (file piece order)
			// extract piece number from file name ('filename'-'number'.split )
			int nStartIndex = strFile.lastIndexOf("-")+1;
			int nEndIndex = strFile.lastIndexOf(".");
			int nPieceIndex = Integer.parseInt(strFile.substring(nStartIndex, nEndIndex))-1;
			
			m_filePieces[nPieceIndex] = confInfo.getTransferedFileHome().toString()+File.separator+strFile;
			m_nRecvPieceNum++;
		}
		
		
		// if the index is the same as the number of servers, merge the split file
		if( m_nRecvPieceNum == m_nCurrentServerNum )
		{
			if(m_nRecvPieceNum > 1)
			{
				// set the merged file name m-'file name'.'ext'
				int index = strFile.lastIndexOf("-");
				strMergeName = confInfo.getTransferedFileHome().toString()+File.separator+
						strFile.substring(0, index)+"."+m_strExt;

				// merge split pieces
				CMFileTransferManager.mergeFiles(m_filePieces, m_nCurrentServerNum, strMergeName);
			}

			// calculate the total delay
			long lRecvTime = System.currentTimeMillis();
			System.out.println("total delay for ("+m_nRecvPieceNum+") files: "
								+(lRecvTime-m_lStartTime)+" ms");

			// reset m_bDistSendRecv, m_nRecvFilePieceNum
			m_bDistFileProc = false;
			m_nRecvPieceNum = 0;
		}

		return;
	}
	
	private void processSNSEvent(CMEvent cme)
	{
		CMSNSInfo snsInfo = m_clientStub.getCMInfo().getSNSInfo();
		CMSNSContentList contentList = snsInfo.getSNSContentList();
		CMSNSEvent se = (CMSNSEvent) cme;
		int i = 0;
		
		switch(se.getID())
		{
		case CMSNSEvent.CONTENT_UPLOAD_RESPONSE:
			if( se.getReturnCode() == 1 )
			{
				System.out.println("Content upload succeeded.");
			}
			else
			{
				System.out.println("Content upload failed.");
			}
			System.out.println("user("+se.getUserName()+"), seqNum("+se.getContentID()+"), time("
					+se.getDate()+").");
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_RESPONSE:
			contentList.removeAllSNSContents();	// clear the content list to which downloaded contents will be stored
			m_nEstDelaySum = 0;
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD:
			ArrayList<String> fNameList = null;
			if(se.getNumAttachedFiles() > 0)
			{
				ArrayList<String> rcvList = se.getFileNameList();
				fNameList = new ArrayList<String>();
				for(i = 0; i < rcvList.size(); i++)
				{
					fNameList.add(rcvList.get(i));
				}
			}
			contentList.addSNSContent(se.getContentID(), se.getDate(), se.getWriterName(), se.getMessage(),
					se.getNumAttachedFiles(), se.getReplyOf(), se.getLevelOfDisclosure(), fNameList);
			//System.out.println("transmitted delay: "+se.getEstDelay());
			m_nEstDelaySum += se.getEstDelay();
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_END:
			processCONTENT_DOWNLOAD_END(se);
			break;
		case CMSNSEvent.RESPONSE_ATTACHED_FILE:
			if(se.getReturnCode() == 1)
			{
				System.out.println("The request for an attached file ["+se.getFileName()
						+"] of content ID ["+se.getContentID()+"] written by ["+se.getWriterName()
						+"] is succeeded.");
			}
			else
			{
				System.out.println("The request for an attached file ["+se.getFileName()
						+"] of content ID ["+se.getContentID()+"] written by ["+se.getWriterName()
						+"] is failed!");
			}
			break;
		case CMSNSEvent.ADD_NEW_FRIEND_ACK:
			if(se.getReturnCode() == 1)
			{
				System.out.println("["+se.getUserName()+"] succeeds to add a friend["
						+se.getFriendName()+"].");
			}
			else
			{
				System.out.println("["+se.getUserName()+"] fails to add a friend["
						+se.getFriendName()+"].");
			}
			break;
		case CMSNSEvent.REMOVE_FRIEND_ACK:
			if(se.getReturnCode() == 1)
			{
				System.out.println("["+se.getUserName()+"] succeeds to remove a friend["
						+se.getFriendName()+"].");
			}
			else
			{
				System.out.println("["+se.getUserName()+"] fails to remove a friend["
						+se.getFriendName()+"].");
			}
			break;
		case CMSNSEvent.RESPONSE_FRIEND_LIST:
			System.out.println("["+se.getUserName()+"] receives "+se.getNumFriends()+" friends "
					+"of total "+se.getTotalNumFriends()+" friends.");
			System.out.print("Friends: ");
			for(i = 0; i < se.getFriendList().size(); i++)
			{
				System.out.print(se.getFriendList().get(i)+" ");
			}
			System.out.println();
			break;
		case CMSNSEvent.RESPONSE_FRIEND_REQUESTER_LIST:
			System.out.println("["+se.getUserName()+"] receives "+se.getNumFriends()+" requesters "
					+"of total "+se.getTotalNumFriends()+" requesters.");
			System.out.print("Requesters: ");
			for(i = 0; i < se.getFriendList().size(); i++)
			{
				System.out.print(se.getFriendList().get(i)+" ");
			}
			System.out.println();
			break;
		case CMSNSEvent.RESPONSE_BI_FRIEND_LIST:
			System.out.println("["+se.getUserName()+"] receives "+se.getNumFriends()+" bi-friends "
					+"of total "+se.getTotalNumFriends()+" bi-friends.");
			System.out.print("Bi-friends: ");
			for(i = 0; i < se.getFriendList().size(); i++)
			{
				System.out.print(se.getFriendList().get(i)+" ");
			}
			System.out.println();
			break;
		case CMSNSEvent.CHANGE_ATTACH_DOWNLOAD_SCHEME:
			String[] attachScheme = {"Full", "Thumbnail", "Prefetching", "None"};
			System.out.println("Server changes the scheme for attachment download of SNS content to ["
					+attachScheme[se.getAttachDownloadScheme()]+"].");
			break;
		case CMSNSEvent.PREFETCH_COMPLETED:
			processPREFETCH_COMPLETED(se);
			break;
		}
		return;
	}
	
	private void processCONTENT_DOWNLOAD_END(CMSNSEvent se)
	{
		CMSNSInfo snsInfo = m_clientStub.getCMInfo().getSNSInfo();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMSNSContentList contentList = snsInfo.getSNSContentList();
		Iterator<CMSNSContent> iter = null;
		
		System.out.println("# downloaded contents: "+se.getNumContents());
		System.out.println("# contents to be printed: "+contentList.getSNSContentNum());
		
		// writes info to the file
		int nRealDelay = (int)(System.currentTimeMillis()-m_lStartTime);
		int nAccessDelay = nRealDelay + m_nEstDelaySum;
		System.out.println("Real download delay: "+nRealDelay+" ms");

		if(m_pw != null)	// if multiple downloading is requested,
		{
			m_pw.format("%10d %10d%n", nAccessDelay, se.getNumContents());
			m_pw.flush();
		}

		// print out SNS content that is downloaded
		iter = contentList.getContentList().iterator();
		while(iter.hasNext())
		{
			CMSNSContent cont = iter.next();
			System.out.println("-------------------------------------------------------------");
			System.out.println("ID("+cont.getContentID()+"), Date("+cont.getDate()+"), Writer("
					+cont.getWriterName()+"), #attachment("+cont.getNumAttachedFiles()+"), replyID("
					+cont.getReplyOf()+"), lod("+cont.getLevelOfDisclosure()+")");
			System.out.println("Message: "+cont.getMessage());
			if(cont.getNumAttachedFiles() > 0)
			{
				ArrayList<String> fNameList = cont.getFileNameList();
				for(int i = 0; i < fNameList.size(); i++)
				{
					String strPath = confInfo.getTransferedFileHome().toString()+File.separator+fNameList.get(i);
					File file = new File(strPath);
					if(file.exists())
						System.out.println("attachment: "+fNameList.get(i));
					else
						System.out.println("attachment: "+fNameList.get(i)+" (not downloaded)");
				}
			}

		}
		//System.out.println("sum of estimated download delay: "+m_nEstDelaySum +" ms");

		// continue simulation until m_nSimNum = 0
		if( --m_nSimNum > 0 )
		{
			// repeat the request of SNS content downloading
			m_lStartTime = System.currentTimeMillis();
			int nContentOffset = 0;
			String strUserName = m_clientStub.getMyself().getName();
			String strWriterName = "";
			
			m_clientStub.requestSNSContent(strWriterName, nContentOffset);
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("["+strUserName+"] requests content of writer ["+strWriterName
						+"] with offset("+nContentOffset+").");
			}
		}
		else
		{
			if(m_fos != null){
				try {
					m_fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(m_pw != null){
				m_pw.close();
			}
		}
		
		return;
	}

	private void processPREFETCH_COMPLETED(CMSNSEvent se)
	{
		String strUserName = se.getUserName();
		int nDelay = (int)(System.currentTimeMillis()-m_lStartTime);
		System.out.println("["+strUserName+"] prefetching attachments completed, total delay: "+nDelay+" ms");
		
		return;
	}

	private void processMultiServerEvent(CMEvent cme)
	{
		CMMultiServerEvent mse = (CMMultiServerEvent) cme;
		switch(mse.getID())
		{
		case CMMultiServerEvent.NOTIFY_SERVER_INFO:
			System.out.println("New server info received: num servers: "+mse.getServerNum() );
			Iterator<CMServerInfo> iter = mse.getServerInfoList().iterator();
			System.out.format("%-20s %-20s %-10s %-10s%n", "name", "addr", "port", "udp port");
			System.out.println("--------------------------------------------------------------");
			while(iter.hasNext())
			{
				CMServerInfo si = iter.next();
				System.out.format("%-20s %-20s %-10d %-10d%n", si.getServerName(), 
						si.getServerAddress(), si.getServerPort(), si.getServerUDPPort());
			}
			break;
		case CMMultiServerEvent.NOTIFY_SERVER_LEAVE:
			System.out.println("An additional server["+mse.getServerName()+"] left the "
					+ "default server.");
			break;
		case CMMultiServerEvent.ADD_RESPONSE_SESSION_INFO:
			System.out.println("Session information of server["+mse.getServerName()+"]");
			System.out.format("%-60s%n", "------------------------------------------------------------");
			System.out.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num");
			System.out.format("%-60s%n", "------------------------------------------------------------");
			Iterator<CMSessionInfo> iterSI = mse.getSessionInfoList().iterator();

			while(iterSI.hasNext())
			{
				CMSessionInfo tInfo = iterSI.next();
				System.out.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(), 
						tInfo.getPort(), tInfo.getUserNum());
			}
			break;
		case CMMultiServerEvent.ADD_LOGIN_ACK:
			System.out.println("This client successfully logs in to server["+mse.getServerName()+"].");
			break;
		}
		
		return;
	}
	
	private void processMqttEvent(CMEvent cme)
	{
		switch(cme.getID())
		{
		case CMMqttEvent.CONNACK:
			CMMqttEventCONNACK conackEvent = (CMMqttEventCONNACK)cme;
			//System.out.println("received "+conackEvent);
			System.out.println("["+conackEvent.getSender()+"] sent CMMqttEvent.CONNACK, "
					+ "[return code: "+conackEvent.getReturnCode()+"]");
			break;
		case CMMqttEvent.PUBLISH:
			CMMqttEventPUBLISH pubEvent = (CMMqttEventPUBLISH)cme;
			//System.out.println("received "+pubEvent);
			System.out.println("["+pubEvent.getSender()+"] sent CMMqttEvent.PUBLISH, "
					+ "[packet ID: "+pubEvent.getPacketID()+"], [topic: "
					+pubEvent.getTopicName()+"], [msg: "+pubEvent.getAppMessage()
					+"], [QoS: "+pubEvent.getQoS()+"]");
			break;
		case CMMqttEvent.PUBACK:
			CMMqttEventPUBACK pubackEvent = (CMMqttEventPUBACK)cme;
			//System.out.println("received "+pubackEvent);
			System.out.println("["+pubackEvent.getSender()+"] sent CMMqttEvent.PUBACK, "
					+ "[packet ID: "+pubackEvent.getPacketID()+"]");
			break;
		case CMMqttEvent.PUBREC:
			CMMqttEventPUBREC pubrecEvent = (CMMqttEventPUBREC)cme;
			//System.out.println("received "+pubrecEvent);
			System.out.println("["+pubrecEvent.getSender()+"] sent CMMqttEvent.PUBREC, "
					+ "[packet ID: "+pubrecEvent.getPacketID()+"]");
			break;
		case CMMqttEvent.PUBREL:
			CMMqttEventPUBREL pubrelEvent = (CMMqttEventPUBREL)cme;
			//System.out.println("received "+pubrelEvent);
			System.out.println("["+pubrelEvent.getSender()+"] sent CMMqttEventPUBREL, "
					+ "[packet ID: "+pubrelEvent.getPacketID()+"]");
			break;
		case CMMqttEvent.PUBCOMP:
			CMMqttEventPUBCOMP pubcompEvent = (CMMqttEventPUBCOMP)cme;
			//System.out.println("received "+pubcompEvent);
			System.out.println("["+pubcompEvent.getSender()+"] sent CMMqttEvent.PUBCOMP, "
					+ "[packet ID: "+pubcompEvent.getPacketID()+"]");
			break;
		case CMMqttEvent.SUBACK:
			CMMqttEventSUBACK subackEvent = (CMMqttEventSUBACK)cme;
			//System.out.println("received "+subackEvent);
			System.out.println("["+subackEvent.getSender()+"] sent CMMqttEvent.SUBACK, "
					+subackEvent.getReturnCodeList());
			break;
		case CMMqttEvent.UNSUBACK:
			CMMqttEventUNSUBACK unsubackEvent = (CMMqttEventUNSUBACK)cme;
			//System.out.println("received "+unsubackEvent);
			System.out.println("["+unsubackEvent.getSender()+"] sent CMMqttEvent.UNSUBACK");
			break;
		}
		
		return;
	}
}
