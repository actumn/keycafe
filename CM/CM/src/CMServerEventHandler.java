import java.util.Iterator;
import java.io.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

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
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventDISCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;


public class CMServerEventHandler implements CMAppEventHandler {
	private CMServerStub m_serverStub;
	private int m_nCheckCount;	// for internal forwarding simulation
	private boolean m_bDistFileProc;	// for distributed file processing

	public CMServerEventHandler(CMServerStub serverStub)
	{
		m_serverStub = serverStub;
		m_nCheckCount = 0;
		m_bDistFileProc = false;
	}
	
	@Override
	public void processEvent(CMEvent cme) {
		// TODO Auto-generated method stub
		switch(cme.getType())
		{
		case CMInfo.CM_SESSION_EVENT:
			processSessionEvent(cme);
			break;
		case CMInfo.CM_INTEREST_EVENT:
			processInterestEvent(cme);
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
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		CMSessionEvent se = (CMSessionEvent) cme;
		switch(se.getID())
		{
		case CMSessionEvent.LOGIN:
			System.out.println("["+se.getUserName()+"] requests login.");
			if(confInfo.isLoginScheme())
			{
				// user authentication...
				// CM DB must be used in the following authentication..
				boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(), 
						m_serverStub.getCMInfo());
				if(!ret)
				{
					System.out.println("["+se.getUserName()+"] authentication fails!");
					m_serverStub.replyEvent(se, 0);
				}
				else
				{
					System.out.println("["+se.getUserName()+"] authentication succeeded.");
					m_serverStub.replyEvent(se, 1);
				}
			}
			break;
		case CMSessionEvent.LOGOUT:
			System.out.println("["+se.getUserName()+"] logs out.");
			break;
		case CMSessionEvent.REQUEST_SESSION_INFO:
			System.out.println("["+se.getUserName()+"] requests session information.");
			break;
		case CMSessionEvent.CHANGE_SESSION:
			System.out.println("["+se.getUserName()+"] changes to session("+se.getSessionName()+").");
			break;
		case CMSessionEvent.JOIN_SESSION:
			System.out.println("["+se.getUserName()+"] requests to join session("+se.getSessionName()+").");
			break;
		case CMSessionEvent.LEAVE_SESSION:
			System.out.println("["+se.getUserName()+"] leaves a session("+se.getSessionName()+").");
			break;
		case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL:
			System.out.println("["+se.getChannelName()+"] request to add a nonblocking SocketChannel with key("
					+se.getChannelNum()+").");
			break;
		case CMSessionEvent.REGISTER_USER:
			System.out.println("User registration requested by user["+se.getUserName()+"].");
			break;
		case CMSessionEvent.DEREGISTER_USER:
			System.out.println("User deregistration requested by user["+se.getUserName()+"].");
			break;
		case CMSessionEvent.FIND_REGISTERED_USER:
			System.out.println("User profile requested for user["+se.getUserName()+"].");
			break;
		case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
			System.err.println("Unexpected disconnection from ["
					+se.getChannelName()+"] with key["+se.getChannelNum()+"]!");
			break;
		case CMSessionEvent.INTENTIONALLY_DISCONNECT:
			System.err.println("Intentionally disconnected all channels from ["
					+se.getChannelName()+"]!");
			break;
		default:
			return;
		}
	}
	
	private void processInterestEvent(CMEvent cme)
	{
		CMInterestEvent ie = (CMInterestEvent) cme;
		switch(ie.getID())
		{
		case CMInterestEvent.USER_ENTER:
			System.out.println("["+ie.getUserName()+"] enters group("+ie.getCurrentGroup()+") in session("
					+ie.getHandlerSession()+").");
			break;
		case CMInterestEvent.USER_LEAVE:
			System.out.println("["+ie.getUserName()+"] leaves group("+ie.getHandlerGroup()+") in session("
					+ie.getHandlerSession()+").");
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
		int nForwardType = -1;
		int id = -1;
		String strUser = null;
		
		CMUserEvent ue = (CMUserEvent) cme;
		
		if(ue.getStringID().equals("testNotForward"))
		{
			m_nCheckCount++;
			id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
			System.out.println("Received user event 'testNotForward', id("+id+"), checkCount("+m_nCheckCount+")");
		}
		else if(ue.getStringID().equals("testForward"))
		{
			nForwardType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "ftype"));
			if(nForwardType == 0)	// typical forwarding
			{
				m_nCheckCount++;
				id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
				System.out.println("Received user evnet 'testForward', id("+id+"), checkCount("+m_nCheckCount+")");
				strUser = ue.getEventField(CMInfo.CM_STR, "user");
				m_serverStub.send(cme, strUser);
			}
		}
		else if(ue.getStringID().equals("EndSim"))
		{
			int nSimNum = 0;
			nSimNum = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "simnum"));
			System.out.println("Received user event 'EndSim', simulation num("+nSimNum+")");
			if(nSimNum == 0)
			{
				System.out.println("divided by 0 error.");
				return;
			}
			int nAvgCount = m_nCheckCount / nSimNum;
			System.out.println("Total count("+m_nCheckCount+"), average count("+nAvgCount+").");
			m_nCheckCount = 0;
		}
		else if(ue.getStringID().equals("testForwardDelay"))
		{
			nForwardType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "ftype"));
			if(nForwardType == 0)
			{
				id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
				System.out.println("Received user event 'testForwardDelay', id("+id+")");
				strUser = ue.getEventField(CMInfo.CM_STR, "user");
				m_serverStub.send(cme, strUser);
			}
		}
		else if(ue.getStringID().equals("EndForwardDelay"))
		{
			nForwardType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "ftype"));
			if(nForwardType == 0)
			{
				System.out.println("Received user event 'EndForwardDelay'");
				strUser = ue.getEventField(CMInfo.CM_STR, "user");
				m_serverStub.send(cme, strUser);
			}
			
		}
		else if(ue.getStringID().equals("reqRecv"))
		{
			strUser = ue.getEventField(CMInfo.CM_STR, "user");
			int nChType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chType"));
			int nChKey = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chKey"));
			int nRecvPort = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "recvPort"));
			CMUserEvent userEvent = new CMUserEvent();
			userEvent.setStringID("repRecv");
			userEvent.setEventField(CMInfo.CM_STR, "receiver", m_serverStub.getMyself().getName());
			userEvent.setEventField(CMInfo.CM_INT, "chType", Integer.toString(nChType));
			userEvent.setEventField(CMInfo.CM_INT, "chKey", Integer.toString(nChKey));
			userEvent.setEventField(CMInfo.CM_INT, "recvPort", Integer.toString(nRecvPort));
			m_serverStub.send(userEvent, strUser);
			
			System.out.print("["+strUser+"] requested to receive a dummy event ");
			
			SocketChannel sc = null;
			DatagramChannel dc = null;
			CMDummyEvent due = null;
			if(nChType == CMInfo.CM_SOCKET_CHANNEL)
			{
				System.out.println("with the blocking socket channel ("+nChKey+").");
				sc = m_serverStub.getBlockSocketChannel(nChKey, strUser);
				if(sc == null)
				{
					System.err.println("CMWinServerEventHandler.processUserEvent(): reqRecv, socket channel not found, key("
							+nChKey+"), user("+strUser+")!");
					return;
				}
				
				due = (CMDummyEvent) m_serverStub.receive(sc);
				if(due == null)
				{
					System.err.println("CMWinServerEventHandler.processUserEvent(): reqRecv, failed to receive a dummy event!");
					return;
				}
				System.out.println("received dummy info: "+due.getDummyInfo());

			}
			else if(nChType == CMInfo.CM_DATAGRAM_CHANNEL)
			{
				System.out.println("with the blocking datagram channel port("+nRecvPort+")");
				dc = m_serverStub.getBlockDatagramChannel(nRecvPort);
				if(dc == null)
				{
					System.err.println("CMWinServerEventHandler.processUserEvent(): reqRecv, datagram channel not found, recvPort("
							+nRecvPort+")!");
					return;
				}
				
				due = (CMDummyEvent) m_serverStub.receive(dc);
				if(due == null)
				{
					System.err.println("CMWinServerEventHandler.processUserEvent(): reqRecv, failed to receive a dummy event!");
					return;
				}
				System.out.println("received dummy info: "+due.getDummyInfo());				
			}
			
		}
		else if(ue.getStringID().equals("testSendRecv"))
		{
			System.out.println("Received user event from ["+ue.getSender()+"] to ["+ue.getReceiver()+
					"], (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")");

			if(!m_serverStub.getMyself().getName().equals(ue.getReceiver()))
				return;

			CMUserEvent rue = new CMUserEvent();
			rue.setID(222);
			rue.setStringID("testReplySendRecv");
			boolean ret = m_serverStub.send(rue, ue.getSender());
			if(ret)
				System.out.println("Sent reply event: (id, "+rue.getID()+"), (string id, "+rue.getStringID()+")");
			else
				System.err.println("Failed to send the reply event!");			
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
		switch(fe.getID())
		{
		case CMFileEvent.REQUEST_PERMIT_PULL_FILE:
			System.out.println("["+fe.getFileReceiver()+"] requests file("+fe.getFileName()+").");
			System.err.print("["+fe.getFileReceiver()+"] requests file("+fe.getFileName()+").\n");
			System.err.print("The pull-file request is not automatically permitted!\n");
			System.err.print("To change to automatically permit the pull-file request, \n");
			System.err.print("set the PERMIT_FILE_TRANSFER field to 1 in the cm-server.conf file\n");
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
			System.out.println("["+fe.getFileSender()+"] wants to send a file("+fe.getFilePath()+
					").");
			System.err.print("The push-file request is not automatically permitted!\n");
			System.err.print("To change to automatically permit the push-file request, \n");
			System.err.print("set the PERMIT_FILE_TRANSFER field to 1 in the cm-server.conf file\n");
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
			String strFile = fe.getFileName();
			if(m_bDistFileProc)
			{
				processFile(fe.getFileSender(), strFile);
				m_bDistFileProc = false;
			}
			break;
		case CMFileEvent.REQUEST_DIST_FILE_PROC:
			System.out.println("["+fe.getFileReceiver()+"] requests the distributed file processing.");
			m_bDistFileProc = true;
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
	
	private void processFile(String strSender, String strFile)
	{
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		String strFullSrcFilePath = null;
		String strModifiedFile = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		byte[] fileBlock = new byte[CMInfo.FILE_BLOCK_LEN];

		long lStartTime = System.currentTimeMillis();

		// change the modified file name
		strModifiedFile = "m-"+strFile;
		strModifiedFile = confInfo.getTransferedFileHome().toString()+File.separator+strSender+
				File.separator+strModifiedFile;

		// stylize the file
		strFullSrcFilePath = confInfo.getTransferedFileHome().toString()+File.separator+strSender+
				File.separator+strFile;
		File srcFile = new File(strFullSrcFilePath);
		long lFileSize = srcFile.length();
		long lRemainBytes = lFileSize;
		int readBytes = 0;

		try {
			fis = new FileInputStream(strFullSrcFilePath);
			fos = new FileOutputStream(strModifiedFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		try {
			
			while( lRemainBytes > 0 )
			{
				if( lRemainBytes >= CMInfo.FILE_BLOCK_LEN )
				{
					readBytes = fis.read(fileBlock);
				}
				else
				{
					readBytes = fis.read(fileBlock, 0, (int)lRemainBytes);
				}
			
				fos.write(fileBlock, 0, readBytes);
				lRemainBytes -= readBytes;
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// add some process delay here
		for(long i = 0; i < lFileSize/50; i++)
		{
			for(long j = 0; j < lFileSize/50; j++)
			{
				// 
			}
		}

		long lEndTime = System.currentTimeMillis();
		System.out.println("processing delay: "+(lEndTime-lStartTime)+" ms");

		// send the modified file to the sender
		CMFileTransferManager.pushFile(strModifiedFile, strSender, m_serverStub.getCMInfo());

		return;
	}
	
	private void processSNSEvent(CMEvent cme)
	{
		CMSNSEvent se = (CMSNSEvent) cme;
		switch(se.getID())
		{
		case CMSNSEvent.CONTENT_DOWNLOAD_REQUEST:
			System.out.println("["+se.getUserName()+"] requests SNS contents starting at: offset("
					+se.getContentOffset()+").");
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_END_RESPONSE:
			if(se.getReturnCode() == 1)
			{
				System.out.println("["+se.getUserName()+" has received SNS contents starting at "
						+se.getContentOffset()+" successfully.");
			}
			else
			{
				System.out.println("!! ["+se.getUserName()+" had a problem while receiving SNS "
						+ "contents starting at "+se.getContentOffset()+".");
			}
			break;
		case CMSNSEvent.CONTENT_UPLOAD_REQUEST:
			System.out.println("content upload requested by ("+se.getUserName()+"), message("
					+se.getMessage()+"), #attachement("+se.getNumAttachedFiles()+"), replyID("
					+se.getReplyOf()+"), lod("+se.getLevelOfDisclosure()+")");
			break;
		case CMSNSEvent.REQUEST_ATTACHED_FILE:
			System.out.println("["+se.getUserName()+"] requests an attached file ["
					+se.getFileName()+"] of SNS content ID["+se.getContentID()+"] written by ["
					+se.getWriterName()+"].");
			break;
		}
		return;
	}
	
	private void processMultiServerEvent(CMEvent cme)
	{
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		CMMultiServerEvent mse = (CMMultiServerEvent) cme;
		switch(mse.getID())
		{
		case CMMultiServerEvent.REQ_SERVER_REG:
			System.out.println("server ("+mse.getServerName()+") requests registration: ip("
					+mse.getServerAddress()+"), port("+mse.getServerPort()+"), udpport("
					+mse.getServerUDPPort()+").");
			break;
		case CMMultiServerEvent.RES_SERVER_REG:
			if( mse.getReturnCode() == 1 )
			{
				System.out.println("server["+mse.getServerName()+"] is successfully registered "
						+ "to the default server.");
			}
			else
			{
				System.out.println("server["+mse.getServerName()+"] is not registered to the "
						+ "default server.");
			}
			break;
		case CMMultiServerEvent.REQ_SERVER_DEREG:
			System.out.println("server["+mse.getServerName()+"] requests deregistration.");
			break;
		case CMMultiServerEvent.RES_SERVER_DEREG:
			if( mse.getReturnCode() == 1 )
			{
				System.out.println("server["+mse.getServerName()+"] is successfully deregistered "
						+ "from the default server.");
			}
			else
			{
				System.out.println("server["+mse.getServerName()+"] is not deregistered from the "
						+ "default server.");
			}
			break;
		case CMMultiServerEvent.ADD_LOGIN:
			if( confInfo.isLoginScheme() )
			{
				// user authentication omitted for the login to an additional server
				//CMInteractionManager.replyToADD_LOGIN(mse, true, m_serverStub.getCMInfo());
				m_serverStub.replyEvent(mse, 1);
			}
			System.out.println("["+mse.getUserName()+"] requests login to this server("
								+mse.getServerName()+").");
			break;
		case CMMultiServerEvent.ADD_LOGOUT:
			System.out.println("["+mse.getUserName()+"] log out this server("+mse.getServerName()
					+").");
			break;
		case CMMultiServerEvent.ADD_REQUEST_SESSION_INFO:
			System.out.println("["+mse.getUserName()+"] requests session information.");
			break;
		}

		return;
	}
	
	private void processMqttEvent(CMEvent cme)
	{
		switch(cme.getID())
		{
		case CMMqttEvent.CONNECT:
			CMMqttEventCONNECT conEvent = (CMMqttEventCONNECT)cme;
			//System.out.println("received "+conEvent);
			System.out.println("["+conEvent.getUserName()
				+"] requests to connect MQTT service.");
			break;
		case CMMqttEvent.PUBLISH:
			CMMqttEventPUBLISH pubEvent = (CMMqttEventPUBLISH)cme;
			//System.out.println("received "+pubEvent);
			System.out.print("["+pubEvent.getSender()+"] requests to publish, ");
			System.out.println("[packet ID: "+pubEvent.getPacketID()
					+"], [topic: "+pubEvent.getTopicName()+"], [msg: "
					+pubEvent.getAppMessage()+"], [qos: "+pubEvent.getQoS()+"]");
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
		case CMMqttEvent.SUBSCRIBE:
			CMMqttEventSUBSCRIBE subEvent = (CMMqttEventSUBSCRIBE)cme;
			//System.out.println("received "+subEvent);
			System.out.println("["+subEvent.getSender()+"] requests to subscribe, "
					+ subEvent.getTopicQoSList());
			break;
		case CMMqttEvent.UNSUBSCRIBE:
			CMMqttEventUNSUBSCRIBE unsubEvent = (CMMqttEventUNSUBSCRIBE)cme;
			//System.out.println("received "+unsubEvent);
			System.out.println("["+unsubEvent.getSender()+"] requests to unsubscribe, "
					+ unsubEvent.getTopicList());
			break;
		case CMMqttEvent.DISCONNECT:
			CMMqttEventDISCONNECT disconEvent = (CMMqttEventDISCONNECT)cme;
			//System.out.println("received "+disconEvent);
			System.out.println("["+disconEvent.getSender()
				+"] requests to disconnect MQTT service.");
			break;
		}
		
		return;
	}
}
