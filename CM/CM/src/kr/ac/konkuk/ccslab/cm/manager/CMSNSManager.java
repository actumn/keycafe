package kr.ac.konkuk.ccslab.cm.manager;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttach;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachAccessHistory;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachAccessHistoryList;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachHashtable;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachList;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContent;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSPrefetchHashMap;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSPrefetchList;
import kr.ac.konkuk.ccslab.cm.util.CMUtil;

import java.text.*;

public class CMSNSManager {
	private static final int DELAY_THRESHOLD = 150;
	private static final int REDUCTION_RATE = 2;
	private static final int MIN_CONTENT_NUM = 1;
	private static final int MAX_CONTENT_NUM = 100;
	
	// get the list of users whom the user, 'strUserName', adds as his/her friends 
	public static ArrayList<String> getFriendList(String strUserName, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		
		// Only the default server which connects to DB can get a friend list
		if(!CMConfigurator.isDServer(cmInfo))
		{
			System.err.println("CMSNSManager.getFriendList(), this is not the default server!");
			return null;
		}
		// Check the usage of DB
		if(!confInfo.isDBUse())
		{
			System.err.println("CMSNSManager.getFriendList(), DB is not used!");
			return null;
		}
		
		ArrayList<String> friendList = null;
		// get users whom I added as friends
		friendList = CMDBManager.queryGetFriendsList(strUserName, cmInfo);

		return friendList;
	}
	
	// get the list of users who added the user, 'strUserName', as their friend, but the user has not add them yet
	public static ArrayList<String> getFriendRequesterList(String strUserName, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		
		// Only the default server which connects to DB can get a friend list
		if(!CMConfigurator.isDServer(cmInfo))
		{
			System.err.println("CMSNSManager.getFriendRequesterList(), this is not the default server!");
			return null;
		}
		// Check the usage of DB
		if(!confInfo.isDBUse())
		{
			System.err.println("CMSNSManager.getFriendRequesterList(), DB is not used!");
			return null;
		}

		ArrayList<String> candidateList = null;
		ArrayList<String> myFriendList = null;
		ArrayList<String> requesterList = null;
		
		// get users who added me as a friend
		candidateList = CMDBManager.queryGetRequestersList(strUserName, cmInfo);
		
		// get users whom I added as friends
		myFriendList = CMDBManager.queryGetFriendsList(strUserName, cmInfo);
		
		// If users in the list are not my friends, add them to a new list
		// because current 'candidateList' includes users whom I added as friends as well.
		if(candidateList != null)
		{
			requesterList = new ArrayList<String>();
			for(int i = 0; i < candidateList.size(); i++)
			{
				String strUser = candidateList.get(i);
				if(myFriendList == null || !myFriendList.contains(strUser))
				{
					requesterList.add(strUser);
				}
			}			
		}
		
		candidateList = null;
		myFriendList = null;
		
		return requesterList;
	}
	
	// get the list of users who added the user, 'strUserName', as their friend, and the user added them as well
	public static ArrayList<String> getBiFriendList(String strUserName, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		
		// Only the default server which connects to DB can get a friend list
		if(!CMConfigurator.isDServer(cmInfo))
		{
			System.err.println("CMSNSManager.getBiFriendList(), this is not the default server!");
			return null;
		}
		// Check the usage of DB
		if(!confInfo.isDBUse())
		{
			System.err.println("CMSNSManager.getBiFriendList(), DB is not used!");
			return null;
		}
		
		ArrayList<String> candidateList = null;
		ArrayList<String> myFriendList = null;
		ArrayList<String> biFriendList = null;

		// get users who added me as a friend
		candidateList = CMDBManager.queryGetRequestersList(strUserName, cmInfo);		
		// get users whom I added as friends
		myFriendList = CMDBManager.queryGetFriendsList(strUserName, cmInfo);
		
		// If users in the list are not my friends, add them to a new list
		// because current 'candidateList' includes users whom I added as friends as well.
		if(candidateList != null && myFriendList != null)
		{
			biFriendList = new ArrayList<String>();
			for(int i = 0; i < candidateList.size(); i++)
			{
				String strUser = candidateList.get(i);
				if(myFriendList.contains(strUser))
				{
					biFriendList.add(strUser);
				}
			}			
		}

		candidateList = null;
		myFriendList = null;
		
		return biFriendList;
	}
	
	// check the completion of receiving attached file of SNS content
	protected static void checkCompleteRecvAttachedFiles(CMFileEvent fe, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMSNSAttachList attachList = null;
		CMSNSAttach attach = null;
		int nContentID = -1;
		String strFileName = null;
		int nCompleted = 0;
		CMSNSEvent se = null;
		
		if(confInfo.getSystemType().equals("SERVER"))
		{
			// find attachment info to be received
			CMSNSAttachHashtable attachHashtable = snsInfo.getRecvSNSAttachHashtable();
			attachList = attachHashtable.findSNSAttachList(fe.getFileSender());
			if(attachList == null) return;
			attach = attachList.findSNSAttach(fe.getContentID());
			if(attach == null) return;
			if(!attach.containsFileName(fe.getFileName())) return;
			// add attached file info in attached_file_table of DB
			String strFilePath = confInfo.getTransferedFileHome().toString() + File.separator + fe.getFileSender();
			nContentID = attach.getContentID();
			strFileName = fe.getFileName();
			
			// create a thumbnail image
			String strInputPath = strFilePath + File.separator + strFileName;
			if(CMUtil.isImageFile(strInputPath))
			{
				int index = strFileName.lastIndexOf(".");
				String strName = strFileName.substring(0, index)+"-thumbnail";
				String strExt = strFileName.substring(index+1, strFileName.length());
				strName = strName + "." + strExt;
				String strOutPath = strFilePath + File.separator + strName;
				
				int nWidth = confInfo.getThumbnailHorSize();
				int nHeight = confInfo.getThumbnailVerSize();
				CMUtil.createScaledImage(strInputPath, nWidth, nHeight, strOutPath);
			}
			
			if(confInfo.isDBUse())
			{
				CMDBManager.queryInsertSNSAttachedFile(nContentID, strFilePath, strFileName, cmInfo);
			}
			else
			{
				CMSNSContentList contentList = snsInfo.getSNSContentList();
				CMSNSContent content = contentList.findSNSContent(nContentID);
				if(content == null)
				{
					System.err.println("CMSNSManager.checkCompleteRecvAttachedFiles(), content("+nContentID
							+") not found!");
					return;
				}
				content.getFilePathList().add(strFilePath+File.separator+strFileName);
			}
			
			// increase the number of completed attached files
			nCompleted = attach.getNumCompleted() + 1;
			attach.setNumCompleted(nCompleted);
			// check if all attached files of the content have been transfered or not
			if(nCompleted < attach.getFilePathList().size()) return;
			// send the response event to the content upload request
			se = new CMSNSEvent();
			se.setID(CMSNSEvent.CONTENT_UPLOAD_RESPONSE);
			se.setReturnCode(attach.getReturnCode());
			se.setContentID(attach.getContentID());
			se.setDate(attach.getCreationTime());
			se.setUserName(attach.getRequesterName());
			CMEventManager.unicastEvent(se, fe.getFileSender(), cmInfo);
			se = null;
			
			// remove the completed attachment info
			attachList.removeSNSAttach(nContentID);
			attach = null;
			if(attachList.getSNSAttachList().isEmpty())
			{
				attachHashtable.removeSNSAttachList(fe.getFileSender());
				attachList = null;
			}

		}
		else // CLIENT
		{
			// find attachment info to be received
			attachList = snsInfo.getRecvSNSAttachList();
			nContentID = fe.getContentID();
			attach = attachList.findSNSAttach(nContentID);
			if(attach == null) return;
			if(!attach.containsFileName(fe.getFileName())) return;
			// increase the number of completed attached files
			nCompleted = attach.getNumCompleted() + 1;
			attach.setNumCompleted(nCompleted);
			// check if all attached files of the content have been transfered or not
			if(nCompleted < attach.getFilePathList().size()) return;

			// remove the completed attachment info
			attachList.removeSNSAttach(nContentID);
			attach = null;
		}
		
		return;
	}
	
	//////////////////// check the completion of sending attached file of SNS content
	//////////////////// and check the completion of prefetching an attached file of SNS content
	protected static void checkCompleteSendAttachedFiles(CMFileEvent fe, CMInfo cmInfo)
	{
		int nContentID = fe.getContentID();
		String strFileName = fe.getFileName();
		String strReceiverName = fe.getFileReceiver();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			checkClientCompleteSendAttachedFiles(nContentID, strFileName, cmInfo);
		}
		else	// SERVER
		{
			checkServerCompleteSendAttachedFiles(strReceiverName, nContentID, strFileName, cmInfo);
			
			// check the completion of prefetching process
			CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
			CMUser user = interInfo.getLoginUsers().findMember(strReceiverName);
			if(user != null && user.getAttachDownloadScheme() == CMInfo.SNS_ATTACH_PREFETCH)
			{
				checkServerCompletePrefetch(strReceiverName, strFileName, cmInfo);
			}			
		}
		return;
	}
	
	// check whether an attached file from a client is completed to be transferred or not
	// called when a client completes its file transfer
	private static void checkClientCompleteSendAttachedFiles(int nContentID, String strFileName, CMInfo cmInfo)
	{
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMSNSAttach attach = null;
		int nCompleted = 0;

		attach = snsInfo.getSendSNSAttach();
		if(attach.getContentID() != nContentID) return;
		if(!attach.containsFileName(strFileName)) return;
		nCompleted = attach.getNumCompleted() + 1;
		attach.setNumCompleted(nCompleted);
		if(nCompleted < attach.getFilePathList().size()) return;
		attach.init();
		
		return;
	}
	
	// check whether an attached file from a server is completed to be transferred or not
	// called when a server completes its file transfer
	private static void checkServerCompleteSendAttachedFiles(String strUserName, int nContentID, String strFileName, 
			CMInfo cmInfo)
	{
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMSNSAttach attach = null;
		int nCompleted = 0;

		// find and update the completed attachment info
		CMSNSAttachHashtable attachHashtable = snsInfo.getSendSNSAttachHashtable();
		CMSNSAttachList attachList = attachHashtable.findSNSAttachList(strUserName);
		if(attachList == null) return;
		attach = attachList.findSNSAttach(nContentID);
		if(attach == null) return;
		if(!attach.containsFileName(strFileName)) return;
		nCompleted = attach.getNumCompleted() + 1;
		attach.setNumCompleted(nCompleted);
		if(nCompleted < attach.getFilePathList().size()) return;
		
		// remove the completed attachment info
		attachList.removeSNSAttach(nContentID);
		attach = null;
		if(attachList.getSNSAttachList().isEmpty())
		{
			// send CONTENT_DOWNLOAD_END event to the client
			CMSNSEvent sevent = new CMSNSEvent();
			sevent.setID(CMSNSEvent.CONTENT_DOWNLOAD_END);
			sevent.setUserName( attachList.getUserName() );
			sevent.setWriterName(attachList.getWriterName());
			sevent.setContentOffset( attachList.getContentOffset() );
			sevent.setNumContents( attachList.getNumContents() );

			// send the end event
			CMEventManager.unicastEvent(sevent, strUserName, cmInfo);
			sevent = null;
			
			// remove the completed attachment list info
			attachHashtable.removeSNSAttachList(strUserName);
			attachList = null;
		}
		
		return;
	}
	
	// check whether an attached file from a server is completed to be prefetched or not
	// called when a server completes its file transfer
	private static void checkServerCompletePrefetch(String strUserName, String strFileName, CMInfo cmInfo)
	{
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMSNSPrefetchHashMap prefetchMap = snsInfo.getPrefetchMap();
		CMSNSPrefetchList prefetchList = prefetchMap.findPrefetchList(strUserName);
		if(prefetchList == null) return;
		String strPath = prefetchList.findFilePath(strFileName);
		if(strPath == null) return;
		prefetchList.removeFilePath(strPath);
		if(!prefetchList.getFilePathList().isEmpty()) return;
		prefetchMap.removePrefetchList(strUserName);
		if(CMInfo._CM_DEBUG)
			System.out.println("CMSNSManager.checkServerPrefetchCompletion(), "
					+"prefetching for user("+strUserName+") completes.");
		
		// notify the user of the prefetching completion?
		CMSNSEvent se = new CMSNSEvent();
		se.setID(CMSNSEvent.PREFETCH_COMPLETED);
		se.setUserName(strUserName);
		CMEventManager.unicastEvent(se, strUserName, cmInfo);
		se = null;
		
		return;
	}
	
	// check whether the original files are currently being prefetched to the client or not
	private static boolean isServerPrefetchOngoing(String strUserName, CMInfo cmInfo)
	{
		boolean bFound = false;
		// find the prefetch list of the client
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMSNSPrefetchHashMap  prefetchMap = snsInfo.getPrefetchMap();
		CMSNSPrefetchList prefetchList = prefetchMap.findPrefetchList(strUserName);
		if(prefetchList == null) return false;
		
		// find the ongoing file sending info
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMSendFileInfo sendFileInfo = fInfo.findSendFileInfoOngoing(strUserName);
		if(sendFileInfo != null)
		{
			// check if the ongoing file is one of the prefetch list member
			String strPrefetchFilePath = prefetchList.findFilePath(sendFileInfo.getFileName());
			if(strPrefetchFilePath != null)
				bFound = true;
			else
				bFound = false;
		}
		
		return bFound;
	}
	
	// load access history of this user from DB from a specified date to his/her last login date
	public static void loadAccessHistory(CMUser user, CMInfo cmInfo)
	{
		String strUserName = user.getName();
		
		// set a starting and ending dates for loading access history 
		Calendar endDate = user.getLastLoginDate();

		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		int amount = -(confInfo.getAttachAccessInterval()); // change to a negative number

		Calendar startDate = Calendar.getInstance();
		startDate.setTime(endDate.getTime());
		startDate.add(Calendar.DAY_OF_MONTH, amount);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		// load access history of the user from DB
		CMSNSAttachAccessHistoryList historyList = null;
		historyList = CMDBManager.queryGetAccessHistory(strUserName, startDate, endDate, null, cmInfo);
		
		// store history to the history list of the user
		user.setAttachAccessHistoryList(historyList);
		
		if(CMInfo._CM_DEBUG)
		{
			ArrayList<CMSNSAttachAccessHistory> historyArrayList = historyList.getAllAccessHistory();
			String strStartDate = dateFormat.format(startDate.getTime());
			String strEndDate = dateFormat.format(endDate.getTime());
			System.out.println("CMSNSManager.loadAccessHistory(), user("+strUserName+
					"), from("+strStartDate+"), to("+strEndDate+")");
			System.out.println("Loaded access history: "+historyArrayList.size()+"elements");
			for(int i = 0; i < historyArrayList.size(); i++)
			{
				CMSNSAttachAccessHistory temp = historyArrayList.get(i);
				System.out.println("user("+temp.getUserName()+"), date("
				+dateFormat.format(temp.getDate().getTime())+"), writer("+temp.getWriterName()
				+"), accessCount("+temp.getAccessCount()+")");
			}
		}
		
		return;
	}
	
	// save the updated or newly added access history in DB since the login date
	public static void saveAccessHistory(CMUser user, CMInfo cmInfo)
	{
		CMSNSAttachAccessHistoryList historyList = null;
		ArrayList<CMSNSAttachAccessHistory> arrayList = null;
		String strUserName = null;
		Calendar date = null;
		String strWriterName = null;
		int nAccessCount = 0;
		int nRet = 0;
		int nAdded = 0;
		int nUpdated = 0;
		
		if(user == null)
		{
			System.err.println("CMSNSManager.saveAccessHistory(), user is null!");
			return;
		}

		historyList = user.getAttachAccessHistoryList();
		arrayList = historyList.getAllAccessHistory();

		for(int i = 0; i < arrayList.size(); i++)
		{
			CMSNSAttachAccessHistory tempHistory = arrayList.get(i);
			strUserName = tempHistory.getUserName();
			date = tempHistory.getDate();
			strWriterName = tempHistory.getWriterName();
			nAccessCount = tempHistory.getAccessCount();

			if(tempHistory.isAdded())
			{				
				// add new access history since login time
				nRet = CMDBManager.queryInsertAccessHistory(strUserName, date, strWriterName, nAccessCount, cmInfo);
				if(nRet == 1) // DB query succeeded
				{
					nAdded++;
				}
			}
			else if(tempHistory.isUpdated())
			{
				// update existing access history since login time
				nRet = CMDBManager.queryUpdateAccessCount(strUserName, date, strWriterName, nAccessCount, cmInfo);
				if(nRet == 1) // DB query succeeded
				{
					nUpdated++;
				}
			}
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.saveAccessHistory() user("+user.getName()+"), access history added("
					+nAdded+"), updated("+nUpdated+").");
		}
		
		return;
	}
	
	public static void processEvent(CMMessage msg, CMInfo cmInfo)
	{
		CMSNSEvent se = new CMSNSEvent(msg.m_buf);
		switch(se.getID())
		{
		case CMSNSEvent.CONTENT_DOWNLOAD_REQUEST:	// c -> s
			processCONTENT_DOWNLOAD_REQUEST(se, cmInfo);
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_RESPONSE:	// s -> c
			processCONTENT_DOWNLOAD_RESPONSE(se, cmInfo);
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_READY:		// c -> s
			processCONTENT_DOWNLOAD_READY(se, cmInfo);
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD:			// s -> c
			processCONTENT_DOWNLOAD(se, cmInfo);
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_END:		// s -> c
			processCONTENT_DOWNLOAD_END(se, cmInfo);
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_END_RESPONSE:	// c -> s
			processCONTENT_DOWNLOAD_END_RESPONSE(se, cmInfo);
			break;
		case CMSNSEvent.CONTENT_UPLOAD_REQUEST:		// c -> s
			processCONTENT_UPLOAD_REQUEST(se, cmInfo);
			break;
		case CMSNSEvent.CONTENT_UPLOAD_RESPONSE:	// s -> c
			processCONTENT_UPLOAD_RESPONSE(se, cmInfo);
			break;
		case CMSNSEvent.ADD_NEW_FRIEND:	// c -> s
			processADD_NEW_FRIEND(se, cmInfo);
			break;
		case CMSNSEvent.ADD_NEW_FRIEND_ACK:	// s -> c
			processADD_NEW_FRIEND_ACK(se, cmInfo);
			break;
		case CMSNSEvent.REMOVE_FRIEND:	// c -> s
			processREMOVE_FRIEND(se, cmInfo);
			break;
		case CMSNSEvent.REMOVE_FRIEND_ACK:	// s -> c
			processREMOVE_FRIEND_ACK(se, cmInfo);
			break;
		case CMSNSEvent.REQUEST_FRIEND_LIST:	// c -> s
			processREQUEST_FRIEND_LIST(se, cmInfo);
			break;
		case CMSNSEvent.RESPONSE_FRIEND_LIST:	// s -> c
			processRESPONSE_FRIEND_LIST(se, cmInfo);
			break;
		case CMSNSEvent.REQUEST_FRIEND_REQUESTER_LIST:	// c -> s
			processREQUEST_FRIEND_REQUESTER_LIST(se, cmInfo);
			break;
		case CMSNSEvent.RESPONSE_FRIEND_REQUESTER_LIST:	// s -> c
			processRESPONSE_FRIEND_REQUESTER_LIST(se, cmInfo);
			break;
		case CMSNSEvent.REQUEST_BI_FRIEND_LIST:	// c -> s
			processREQUEST_BI_FRIEND_LIST(se, cmInfo);
			break;
		case CMSNSEvent.RESPONSE_BI_FRIEND_LIST:	// s -> c
			processRESPONSE_BI_FRIEND_LIST(se, cmInfo);
			break;
		case CMSNSEvent.REQUEST_ATTACHED_FILES:		// s <-> c
			processREQUEST_ATTACHED_FILES(se, cmInfo);
			break;
		case CMSNSEvent.ATTACHED_FILES_NOT_FOUND:	// s -> c
			processATTACHED_FILES_NOT_FOUND(se, cmInfo);
			break;
		case CMSNSEvent.REQUEST_ATTACHED_FILE:		// c -> s
			processREQUEST_ATTACHED_FILE(se, cmInfo);
			break;
		case CMSNSEvent.RESPONSE_ATTACHED_FILE:		// s -> c
			processRESPONSE_ATTACHED_FILE(se, cmInfo);
			break;
		case CMSNSEvent.CHANGE_ATTACH_DOWNLOAD_SCHEME:	// s -> c
			processCHANGE_ATTACH_DOWNLOAD_SCHEME(se, cmInfo);
			break;
		case CMSNSEvent.ACCESS_ATTACHED_FILE:		// c -> s
			processACCESS_ATTACHED_FILE(se, cmInfo);
			break;
		case CMSNSEvent.PREFETCH_COMPLETED:			// s -> c
			processPREFETCH_COMPLETED(se, cmInfo);
			break;
		default:
			System.out.println("CMSNSManager.processEvent(), unknown event ID("+se.getID()+").");
			se = null;
			return;
		}
		
		se = null;
		return;
	}

	// for server
	private static void processCONTENT_DOWNLOAD_REQUEST(CMSNSEvent se, CMInfo cmInfo)
	{
		CMSNSEvent seAck = null;
		int nReturnCode = -1;
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		String strUser = se.getUserName();

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_REQUEST(), user("+strUser
					+"), writer("+se.getWriterName()+"), offset("+se.getContentOffset()+").");
		}
		
		// If the sns attachment download scheme is the prefetch mode, the incomplete prefetch should be canceled
		if(confInfo.getAttachDownloadScheme() == CMInfo.SNS_ATTACH_PREFETCH)
		{
			CMSNSInfo snsInfo = cmInfo.getSNSInfo();
			
			// check for the ongoing prefetch to the user, and cancel the prefetched file transfer
			if(isServerPrefetchOngoing(strUser, cmInfo))
			{
				if(CMInfo._CM_DEBUG)
					System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_REQUEST(); previous prefetch ongoing "
							+ "to the user("+strUser+")");
				CMFileTransferManager.cancelPushFile(strUser, cmInfo);	// not clear
			}
			
			// clear the prefetch list of the user
			CMSNSPrefetchList prefetchList = snsInfo.getPrefetchMap().findPrefetchList(strUser);
			if(prefetchList != null)
				snsInfo.getPrefetchMap().removePrefetchList(strUser);
		}
	
		nReturnCode = 1;
		long lStartTime = System.currentTimeMillis();
		
		// create a response event (requester name, content offset, return code, server time value)
		seAck = new CMSNSEvent();
		seAck.setID(CMSNSEvent.CONTENT_DOWNLOAD_RESPONSE);
		seAck.setUserName( strUser );
		seAck.setWriterName(se.getWriterName());
		seAck.setContentOffset( se.getContentOffset() );
		seAck.setReturnCode( nReturnCode );	// 1: ok, 0: error
		seAck.setServerTime( lStartTime );
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_REQUEST(), starting server time: "
					+lStartTime+" ms.");
		}
	
		// send the response event
		CMEventManager.unicastEvent(seAck, strUser, cmInfo);
		
		seAck = null;
		return;
	}
	
	// for client
	private static void processCONTENT_DOWNLOAD_RESPONSE(CMSNSEvent se, CMInfo cmInfo)
	{
		CMSNSEvent seAck = new CMSNSEvent();

		// create a ready event
		seAck.setID(CMSNSEvent.CONTENT_DOWNLOAD_READY);
		seAck.setUserName( se.getUserName() );
		seAck.setWriterName(se.getWriterName());
		seAck.setContentOffset( se.getContentOffset() );
		seAck.setServerTime( se.getServerTime() );

		// send the ready event
		String strDefServer = cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		CMEventManager.unicastEvent(seAck, strDefServer, cmInfo);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_RESPONSE(), ok");
		}

		seAck = null;
		return;
	}
	
	// for server
	private static void processCONTENT_DOWNLOAD_READY(CMSNSEvent se, CMInfo cmInfo)
	{
		CMSNSEvent sevent = null;
		long lDelay = 0;
		int nContNum = 0;
		int nOffset = -1;
		int nContID = -1;
		String strDate = null;
		String strWriter = null;
		String strMsg = null;
		int nNumAttachedFiles = 0;
		int nReplyOf = -1;
		int nLevelOfDisclosure = -1;
		ArrayList<String> nameList = null;
		ArrayList<String> pathList = null;
		ArrayList<String> prefetchPathList = null;
		boolean bExistAttachment = false;
		CMSNSContentList contentList = null;
		Vector<CMSNSContent> contentVector = null;
		int nForStart = -1;
		int nForEnd = -1;
		
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		int nDefDownloadNum = confInfo.getDownloadNum();
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser user = interInfo.getLoginUsers().findMember(se.getUserName());
		int nAttachDownloadScheme = user.getAttachDownloadScheme();

		if(confInfo.isDownloadScheme())
		{
			// calculate round-trip delay
			long lCurTime = System.currentTimeMillis();
			lDelay = lCurTime - se.getServerTime();

			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMSNSManager::processCONTENT_DOWNLOAD_READY(): "+se.getServerTime()
						+" ~ "+lCurTime+", delay("+lDelay+" ms).");
			}
		}

		// for simulation, generate delay with exponential distribution
		/*
		lDelay = (long)(getExpRandVar(0.5)*100);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager::processCONTENT_DOWNLOAD_READY(): sim delay: "+lDelay
					+" ms.");
		}
		*/

		// get number of contents to be downloaded
		if(confInfo.isDownloadScheme())
		{
			int tmp = (int)lDelay/DELAY_THRESHOLD;
			if( tmp < 1 ) // increase the number of contents to be downloaded
			{
				int nEstDefDownDelay = (DELAY_THRESHOLD/2)*nDefDownloadNum;	// estimated default download delay of contents
				if( (int)lDelay/2 == 0 )
					nContNum = MAX_CONTENT_NUM;
				else
					nContNum = nEstDefDownDelay / ((int)lDelay/2);
				if( nContNum > MAX_CONTENT_NUM ) nContNum = MAX_CONTENT_NUM;	// maximum number is defined as MAX_CONTENT_NUM
			}
			else if( tmp > 1)	// decrease the number of contents to be downloaded
			{
				nContNum = nDefDownloadNum/((int)Math.pow((double)REDUCTION_RATE, (double)tmp));
				if( nContNum < MIN_CONTENT_NUM ) nContNum = MIN_CONTENT_NUM;	// minimum number is defined as MIN_CONTENT_NUM
			}
			else
			{
				nContNum = nDefDownloadNum;
			}
		}
		else
		{
			nContNum = nDefDownloadNum;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_READY(): num content: "+nContNum);

		contentList = snsInfo.getSNSContentList();
		contentVector = contentList.getContentList();
		nOffset = se.getContentOffset();

		// for simulation delay
		//Random rnd = new Random();
		
		if(confInfo.isDBUse())
		{
			// if CM DB is used (the content list must contain only the retrieved rows from DB),
			// clear the content list and retrieve rows in descending order of time from DB and store them in the content list

			/*
			// get row num of sns_content_table
			int nRowNum = (int)CMDBManager.sendRowNumQuery("sns_content_table", cmInfo);
			// adjust nContNum if it is greater than the real remaining number from the offset
			if( nOffset > nRowNum )	// it means an error
				nContNum = 0;
			else if( nOffset + nContNum > nRowNum )
				nContNum = nRowNum - nOffset;
			// retrieve rows from nOffset with the number of nContNum in descending order of time from DB
			CMDBManager.queryGetSNSContent(nOffset, nContNum, cmInfo);
			*/
			
			contentList = CMDBManager.queryGetSNSContent(se.getUserName(), se.getWriterName(), nOffset, nContNum, cmInfo);
			contentVector = contentList.getContentList();
			snsInfo.setSNSContentList(contentList);

			nForStart = 0;
			nForEnd = contentList.getSNSContentNum();

			// create and send content events
			for(int i = nForStart; i < nForEnd; i++)
			{
				sevent = new CMSNSEvent();
				sevent.setID(CMSNSEvent.CONTENT_DOWNLOAD);
				sevent.setUserName( se.getUserName() );
				sevent.setContentOffset( se.getContentOffset() );
			
				nContID = contentVector.elementAt(i).getContentID();
				sevent.setContentID( nContID );
				strDate = contentVector.elementAt(i).getDate();
				sevent.setDate( strDate );
				strWriter = contentVector.elementAt(i).getWriterName();
				sevent.setWriterName( strWriter );
				//strFileName = contentVector.elementAt(i).getAttachedFileName();
				//if( strFileName != null )
				//	sevent.setAttachedFileName( strFileName );
				strMsg = contentVector.elementAt(i).getMessage();
				sevent.setMessage( strMsg );
				nNumAttachedFiles = contentVector.elementAt(i).getNumAttachedFiles();
				sevent.setNumAttachedFiles(nNumAttachedFiles);
				nReplyOf = contentVector.elementAt(i).getReplyOf();
				sevent.setReplyOf(nReplyOf);
				nLevelOfDisclosure = contentVector.elementAt(i).getLevelOfDisclosure();
				sevent.setLevelOfDisclosure(nLevelOfDisclosure);

				// if the content includes attached files,
				if(nNumAttachedFiles > 0)
				{
					bExistAttachment = true;
					
					// get the names of attached files
					ArrayList<String> attachFileList = null;
					attachFileList = CMDBManager.queryGetSNSAttachedFile(nContID, cmInfo);
					nameList = new ArrayList<String>();
					pathList = new ArrayList<String>();
					prefetchPathList = new ArrayList<String>();
					
					for(int j = 0; attachFileList != null && j < attachFileList.size(); j++)
					{
						String strAttachPathFile = attachFileList.get(j);
						boolean bOriginal = true;
						boolean bThumbnail = false;
						String strFilePath = strAttachPathFile.substring(0, strAttachPathFile.lastIndexOf(File.separator));
						String strFileName = strAttachPathFile.substring(strAttachPathFile.lastIndexOf(File.separator)+1);
						String strThumbnailName = null;
						if(CMUtil.isImageFile(strFilePath+File.separator+strFileName))
						{
							// change the image file name to its thumbnail image name
							int index = strFileName.lastIndexOf(".");
							strThumbnailName = strFileName.substring(0, index)+"-thumbnail"
								+strFileName.substring(index, strFileName.length());

							if(nAttachDownloadScheme == CMInfo.SNS_ATTACH_PARTIAL)
							{
								bOriginal = false;
								bThumbnail = true;
							}
							else if(nAttachDownloadScheme == CMInfo.SNS_ATTACH_PREFETCH)
							{
								bOriginal = false;
								bThumbnail = true;
								if(CMInfo._CM_DEBUG)
								{
									System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_READY(), "
											+ "call isPrefetchEnabled() for content("+nContID+").");
								}
								// check prefetch threshold (interest of a user in a writer)
								if(isPrefetchEnabled(se.getUserName(), strWriter, cmInfo))
								{
									// add a file path to the prefetching list
									// avoid duplicate path
									if(!prefetchPathList.contains(strFilePath+File.separator+strFileName))
										prefetchPathList.add(strFilePath+File.separator+strFileName);
								}
							}
						}
						
						if(bOriginal)
						{
							nameList.add(strFileName);
							pathList.add(strFilePath+File.separator+strFileName);								
						}
						if(bThumbnail)
						{
							nameList.add(strThumbnailName);
							pathList.add(strFilePath+File.separator+strThumbnailName);								
						}
						
					}
										
					// add the file name list to the CONTENT_DOWNLOAD event
					sevent.setFileNameList(nameList);
										
					if(nAttachDownloadScheme != CMInfo.SNS_ATTACH_NONE)
					{
						// add the attached file path to the attachMapToBeSent
						CMSNSAttachHashtable sendAttachHashtable = snsInfo.getSendSNSAttachHashtable();
						CMSNSAttach attach = new CMSNSAttach();
						attach.setContentID(nContID);
						attach.setFilePathList(pathList);
						CMSNSAttachList attachList = sendAttachHashtable.findSNSAttachList(se.getUserName());
						if(attachList != null)
						{
							attachList.addSNSAttach(attach);
						}
						else
						{
							attachList = new CMSNSAttachList();
							attachList.addSNSAttach(attach);
							attachList.setContentDownloadEndEvent(se.getUserName(), se.getWriterName(), 
									se.getContentOffset(), contentList.getSNSContentNum());
							sendAttachHashtable.addSNSAttachList(se.getUserName(), attachList);
						}
						
						// save the prefetch list if current mode is the prefetch mode
						if(nAttachDownloadScheme == CMInfo.SNS_ATTACH_PREFETCH && !prefetchPathList.isEmpty())
						{
							CMSNSPrefetchHashMap prefetchMap = snsInfo.getPrefetchMap();
							CMSNSPrefetchList prefetchList = prefetchMap.findPrefetchList(se.getUserName());
							if(prefetchList == null)
							{
								prefetchList = new CMSNSPrefetchList();
							}
							prefetchList.addFilePathList(prefetchPathList);
							prefetchMap.addPrefetchList(se.getUserName(), prefetchList);
						}
					}
				}

				// add estimated download delay of this content for simulation
				/*
				int maxDelta = ((int)lDelay/2)/3; // 30% of estimated one-way delay
				int delta = 0;
				if( maxDelta != 0 )
				{
					delta = rnd.nextInt(maxDelta);	// 0 ~ maxDelta
				}
				int incdec = rnd.nextInt(2);	// 0 or 1
				if( incdec == 1 )
				{
					sevent.setEstDelay( (int)lDelay/2 + delta );
				}
				else
				{
					sevent.setEstDelay( (int)lDelay/2 - delta );
				}
				*/
		
				CMEventManager.unicastEvent(sevent, se.getUserName(), cmInfo);
				sevent = null;
				nameList = null;
			}
		}
		else	// if DB is not used,
		{
			//////////////////////////////////
			////// not yet (Currently, only offset and number of content are considered!
			
			// if CM DB is not used (content list contains all the uploaded contents in ascending order of seqNum)
			// adjust nContNum if it is greater than the real remaining number from the offset in the content list
			if( nOffset > contentList.getSNSContentNum() )	// it means an error
				nContNum = 0;
			else if( nOffset + nContNum > contentList.getSNSContentNum() )
				nContNum = contentList.getSNSContentNum() - nOffset;
		
			nForStart = contentList.getSNSContentNum() - nOffset -1;
			nForEnd = contentList.getSNSContentNum() - (nOffset+nContNum) - 1;

			// create and send content events
			for(int i = nForStart; i > nForEnd; i--)
			{
				sevent = new CMSNSEvent();
				sevent.setID(CMSNSEvent.CONTENT_DOWNLOAD);
				sevent.setUserName( se.getUserName() );
				sevent.setContentOffset( se.getContentOffset() );
			
				nContID = contentVector.elementAt(i).getContentID();
				sevent.setContentID( nContID );
				strDate = contentVector.elementAt(i).getDate();
				sevent.setDate( strDate );
				strWriter = contentVector.elementAt(i).getWriterName();
				sevent.setWriterName( strWriter );
				//strFileName = contentVector.elementAt(i).getAttachedFileName();
				//if( strFileName != null )
				//	sevent.setAttachedFileName( strFileName );
				strMsg = contentVector.elementAt(i).getMessage();
				sevent.setMessage( strMsg );
					
				// add estimated download delay of this content for simulation
				/*
				int maxDelta = ((int)lDelay/2)/3; // 30% of estimated one-way delay
				int delta = 0;
				if( maxDelta != 0 )
				{
					delta = rnd.nextInt(maxDelta); // 0 ~ maxDelta
				}
				int incdec = rnd.nextInt(2);	// 0 or 1
				if( incdec == 1 )
				{
					sevent.setEstDelay( (int)lDelay/2 + delta );
				}
				else
				{
					sevent.setEstDelay( (int)lDelay/2 - delta );
				}
				*/
		
				CMEventManager.unicastEvent(sevent, se.getUserName(), cmInfo);
				sevent = null;
			}
		}

		if(!bExistAttachment || nAttachDownloadScheme == CMInfo.SNS_ATTACH_NONE)
		{
			// create a download end event
			sevent = new CMSNSEvent();
			sevent.setID(CMSNSEvent.CONTENT_DOWNLOAD_END);
			sevent.setUserName( se.getUserName() );
			sevent.setWriterName(se.getWriterName());
			sevent.setContentOffset( se.getContentOffset() );
			sevent.setNumContents( contentList.getSNSContentNum() );

			// send the end event
			CMEventManager.unicastEvent(sevent, se.getUserName(), cmInfo);
			sevent = null;
		}
		
		return;
	}
	
	/*
	private static double getExpRandVar(double lambda)
	{
		double u, x;
		Random rnd = new Random();
		
		do{
			u = rnd.nextDouble();
		}while( u == 0 );
		
		x = (-1/lambda)*Math.log(u);
		return x;
	}
	*/
	
	// check how much 'strUserName' has interest in 'strWriterName' in terms of access count during specified dates  
	private static boolean isPrefetchEnabled(String strUserName, String strWriterName, CMInfo cmInfo)
	{
		boolean bEnable = false;
		int nTotalAccessCount = 0;
		int nAccessCount = 0;
		double dAccessRate = 0.0;
		
		// get prefetching threshold
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		double dThreshold = confInfo.getAttachPrefetchThreshold();
		
		// get access history of 'strUserName' during specified dates (already retrieved at the login time)
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser user = interInfo.getLoginUsers().findMember(strUserName);
		if(user == null)
		{
			System.err.println("CMSNSManager.isPrefetchEnabled(), the requesting user is null!");
			return false;
		}
		CMSNSAttachAccessHistoryList historyList = user.getAttachAccessHistoryList();
		ArrayList<CMSNSAttachAccessHistory> historyArrayList = historyList.getAllAccessHistory();
		// calculate access counts for 'strWriterName' and total access counts
		for(int i = 0; i < historyArrayList.size(); i++)
		{
			CMSNSAttachAccessHistory history = historyArrayList.get(i);
			nTotalAccessCount += history.getAccessCount();
			if(strWriterName.equals(history.getWriterName()))
				nAccessCount += history.getAccessCount();
		}
		
		// estimate access rate
		if(nTotalAccessCount > 0)
			dAccessRate = (double)nAccessCount / (double)nTotalAccessCount;
		else
			dAccessRate = 0.0;
		
		if(CMInfo._CM_DEBUG)
		{
			DecimalFormat df = new DecimalFormat("0.0#");
			System.out.println("CMSNSManager.isPrefetchEnabled(), user("+strUserName+"), writer("
					+strWriterName+"), accessCount("+nAccessCount+"), TotalCount("+nTotalAccessCount
					+"), accessRate("+df.format(dAccessRate)+"), threshold("+dThreshold+").");
			
		}
		
		if(dAccessRate >= dThreshold)	// Prefetch enabled
		{
			bEnable = true;
		}
		
		return bEnable;
	}
	
	// for client
	private static void processCONTENT_DOWNLOAD(CMSNSEvent se, CMInfo cmInfo)
	{
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		
		if(se.getNumAttachedFiles() > 0 && confInfo.getAttachDownloadScheme() != CMInfo.SNS_ATTACH_NONE)
		{
			CMSNSAttachList attachList = snsInfo.getRecvSNSAttachList();
			CMSNSAttach attach = new CMSNSAttach();
			attach.setContentID(se.getContentID());
			ArrayList<String> fileNameList = new ArrayList<String>();
			for(int i = 0; i < se.getFileNameList().size(); i++)
			{
				fileNameList.add(se.getFileNameList().get(i));
			}
			attach.setFilePathList(fileNameList);
			attachList.addSNSAttach(attach);
			
			CMSNSEvent seReq = new CMSNSEvent();
			seReq.setID(CMSNSEvent.REQUEST_ATTACHED_FILES);
			seReq.setUserName(interInfo.getMyself().getName());
			seReq.setContentID(se.getContentID());
			CMEventManager.unicastEvent(seReq, strDefServer, cmInfo);
		}
	}
	
	// for client
	private static void processCONTENT_DOWNLOAD_END(CMSNSEvent se, CMInfo cmInfo)
	{
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		int nReturnCode = -1;
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_END(), requester("
					+se.getUserName()+"), offset("+se.getContentOffset()+"), content num("
					+se.getNumContents()+").");
		}
		
		// if there is at least one content,...
		if(se.getNumContents() > 0)
		{
			// save the information of the last content request
			snsInfo.setLastlyReqWriter(se.getWriterName());
			snsInfo.setLastlyReqOffset(se.getContentOffset());
			snsInfo.setLastlyDownContentNum(se.getNumContents());			
		}

		
		// check if all attached files have been received or not
		CMSNSAttachList attachList = snsInfo.getRecvSNSAttachList();
		if(attachList.getSNSAttachList().isEmpty())
			nReturnCode = 1;
		else
			nReturnCode = 0;

		// create a response event
		CMSNSEvent sevent = new CMSNSEvent();
		sevent.setID(CMSNSEvent.CONTENT_DOWNLOAD_END_RESPONSE);
		sevent.setUserName( se.getUserName() );
		sevent.setContentOffset( se.getContentOffset() );
		sevent.setReturnCode( nReturnCode );	// 1 for ok, 0 for error

		// send a response event
		String strDefServer = cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		CMEventManager.unicastEvent(sevent, strDefServer, cmInfo);

		sevent = null;
		return;
	}
	
	private static void processCONTENT_DOWNLOAD_END_RESPONSE(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_END_RESPONSE(), requester("
					+se.getUserName()+"), offset("+se.getContentOffset()+"), return code("
					+se.getReturnCode()+").");
		}
		
		// check whether prefetching starts or not
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		if(confInfo.getAttachDownloadScheme() == CMInfo.SNS_ATTACH_PREFETCH)
		{
			CMSNSInfo snsInfo = cmInfo.getSNSInfo();
			CMSNSPrefetchHashMap prefetchMap = snsInfo.getPrefetchMap();
			CMSNSPrefetchList prefetchList = prefetchMap.findPrefetchList(se.getUserName());
			if(prefetchList == null) return;
			ArrayList<String> preArrayList = prefetchList.getFilePathList();
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMSNSManager.processCONTENT_DOWNLOAD_END_RESPONSE(), for user("
						+se.getUserName()+") prefetch list size("+preArrayList.size()+")");
				for(int i = 0; i < preArrayList.size(); i++)
					System.out.println(preArrayList.get(i));
			}
			
			// start prefetching to the user
			for(int i = 0; i < preArrayList.size(); i++)
			{
				CMFileTransferManager.pushFile(preArrayList.get(i), se.getUserName(), cmInfo);
			}
		}
		
		return;
	}
	
	// for server
	private static void processCONTENT_UPLOAD_REQUEST(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMSNSContentList contentList = snsInfo.getSNSContentList();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		// initialize nReturnCode, seqNum and creationTime
		int nReturnCode = 0;
		int ret = -1;
		int nSeqNum = -1;
		String strCreationTime = "";
		boolean bRet = false;
		CMSNSEvent tse = null;

		// check if CM uses DB or not
		if( confInfo.isDBUse() )
		{
			// insert sns content to DB
			ret = CMDBManager.queryInsertSNSContent(se.getUserName(), se.getMessage(), se.getNumAttachedFiles(), 
					se.getReplyOf(), se.getLevelOfDisclosure(), cmInfo);
			if( ret == 1 )
			{
				// get the last insert ID
				String strQuery = "select last_insert_id() from sns_content_table;";
				ResultSet rs = CMDBManager.sendSelectQuery(strQuery, cmInfo);
				try {
					if(rs != null && rs.next())
					{
						nSeqNum = rs.getInt(1); 
					}
					// get seq number, creation time from DB
					strQuery = "select creationTime from sns_content_table where seqNum="+nSeqNum+";";
					rs = CMDBManager.sendSelectQuery(strQuery, cmInfo);

					if(rs != null && rs.next())
					{
						strCreationTime = rs.getString("creationTime");
						if(CMInfo._CM_DEBUG)
						{
							System.out.println("CMSNSMaager.processCONTENT_UPLOAD_REQUEST(), seqNum("
									+nSeqNum+"), creation time("+strCreationTime+"), user("+se.getUserName()
									+"), message("+se.getMessage()+"), #attachment("+se.getNumAttachedFiles()
									+"), replyID("+se.getReplyOf()+"), levelOfDisclosure("+se.getLevelOfDisclosure()
									+").");
						}
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					CMDBManager.closeDB(cmInfo);
					CMDBManager.closeRS(rs);
				}
			}
			else
			{
				System.out.println("CMSNSManager.processCONTENT_UPLOAD_REQUEST(), DB insert error!");
			}
			
		}
		else	// DB is not used
		{
			// get the seqNum and creationTime locally
			nSeqNum = contentList.getSNSContentNum() + 1;
			Date now = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE yyyy.MM.dd hh:mm:ss a", Locale.US);
			strCreationTime = dateFormat.format(now);
			if(CMInfo._CM_DEBUG)
			{
				System.out.println("CMSNSManager.processCONTENT_UPLOAD_REQUEST(), DB not used, seqNum("
						+nSeqNum+"), creationTime("+strCreationTime+").");
			}
			// add the content to the content list only if the previous logic is successful
			bRet = contentList.addSNSContent(nSeqNum, strCreationTime, se.getUserName(), se.getMessage()
					, se.getNumAttachedFiles(), se.getReplyOf(), se.getLevelOfDisclosure()
					, se.getFileNameList());
		}

		if(ret == 1 || bRet)	// Any case is correct
			nReturnCode = 1;
		
		if(se.getNumAttachedFiles() < 1)	// if there is no attachment
		{
			// make a CONTENT_UPLOAD_RESPONSE event and send back to the client
			tse = new CMSNSEvent();
			tse.setID(CMSNSEvent.CONTENT_UPLOAD_RESPONSE);
			tse.setReturnCode(nReturnCode);
			tse.setContentID(nSeqNum);
			tse.setDate(strCreationTime);
			tse.setUserName(se.getUserName());
			CMEventManager.unicastEvent(tse, se.getUserName(), cmInfo);
			tse = null;
		}
		else	// if there is attachment
		{
			////////////// store the response event and the file name list
			CMSNSAttach attach = new CMSNSAttach();
			attach.setContentUploadResponseEvent(nReturnCode, nSeqNum, strCreationTime, se.getUserName());
			attach.setFilePathList(se.getFileNameList());	// only file names
			CMSNSAttachList attachList = new CMSNSAttachList();
			attachList.addSNSAttach(attach);
			CMSNSAttachHashtable attachHashtable = snsInfo.getRecvSNSAttachHashtable();
			attachHashtable.addSNSAttachList(se.getUserName(), attachList);
			/////////////// request for the attached files			
			tse = new CMSNSEvent();
			tse.setID(CMSNSEvent.REQUEST_ATTACHED_FILES);
			tse.setUserName(interInfo.getMyself().getName());	// requester is default server
			tse.setContentID(nSeqNum);
			CMEventManager.unicastEvent(tse, se.getUserName(), cmInfo);
			tse = null;
		}
		
		return;
	}
	
	private static void processCONTENT_UPLOAD_RESPONSE(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processCONTENT_UPLOAD_RESPONSE(), return code("
					+se.getReturnCode()+"), seqNum("+se.getContentID()+"), creation time("
					+se.getDate()+"), user name("+se.getUserName()+")."); 
		}
		return;
	}
	
	private static void processADD_NEW_FRIEND(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		int nReturnCode = 0;	// 0: error, 1: succeeded
		
		// check if DB is used or not
		if(confInfo.isDBUse())
		{
			int ret = -1;
			ResultSet rs = null;
			String strQuery = null;
			
			// check if the friend is registered user or not
			strQuery = "select * from user_table where userName='"+se.getFriendName()+"';";
			rs = CMDBManager.sendSelectQuery(strQuery, cmInfo);
			try {
				if(rs != null && rs.next())
				{
					// insert (user, friend)
					ret = CMDBManager.queryInsertFriend(se.getUserName(), se.getFriendName(), cmInfo);
					if(ret == 1) // not clear
					{
						nReturnCode = 1;
					}
				}
				else
				{
					System.out.println("CMSNSManager.processADD_NEW_FRIEND(), friend("+se.getFriendName()
							+") is not a registered user.");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				CMDBManager.closeDB(cmInfo);
				CMDBManager.closeRS(rs);
			}
		}
		else
		{
			System.out.println("CMSNSManager.processADD_NEW_FRIEND(), CM DB is not used");
		}
		
		CMSNSEvent seAck = new CMSNSEvent();
		seAck.setID(CMSNSEvent.ADD_NEW_FRIEND_ACK);
		seAck.setReturnCode(nReturnCode);
		seAck.setUserName(se.getUserName());
		seAck.setFriendName(se.getFriendName());
		
		CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
		
		seAck = null;
		return;
	}
	
	private static void processADD_NEW_FRIEND_ACK(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.procedssADD_NEW_FRIEND_ACK(), return code("
					+se.getReturnCode()+"), user("+se.getUserName()+"), friend("
					+se.getFriendName()+").");
		}
		return;
	}
	
	private static void processREMOVE_FRIEND(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		int nReturnCode = 0;	// 0: error, 1: succeeded
		
		// check if DB is used or not
		if(confInfo.isDBUse())
		{
			int ret = -1;
			
			ret = CMDBManager.queryDeleteFriend(se.getUserName(), se.getFriendName(), cmInfo);
			if(ret == 1) // not clear
			{
				nReturnCode = 1;
			}
		}
		else
		{
			System.out.println("CMSNSManager.processREMOVE_FRIEND(), CM DB is not used");
		}
		
		CMSNSEvent seAck = new CMSNSEvent();
		seAck.setID(CMSNSEvent.REMOVE_FRIEND_ACK);
		seAck.setReturnCode(nReturnCode);
		seAck.setUserName(se.getUserName());
		seAck.setFriendName(se.getFriendName());
		
		CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
		
		seAck = null;
		return;
	}
	
	private static void processREMOVE_FRIEND_ACK(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.procedssREMOVE_FRIEND_ACK(), return code("
					+se.getReturnCode()+"), user("+se.getUserName()+"), friend("
					+se.getFriendName()+").");
		}
		return;
	}
	
	private static void processREQUEST_FRIEND_LIST(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		int nTotalNumFriends = 0;
		ArrayList<String> friendList = null;
		CMSNSEvent seAck = null;

		// check if DB is used or not
		if(confInfo.isDBUse())
		{
			/*
			CMDBInfo dbInfo = cmInfo.getDBInfo();
			ResultSet rs = null;
			// get users whom I added as friends
			CMDBManager.queryGetFriendsList(se.getUserName(), cmInfo);
			rs = dbInfo.getResultSet();
			try {
				while(rs.next())
				{
					friendList.add(rs.getString("friendName"));
					nTotalNumFriends++;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			*/
			friendList = CMSNSManager.getFriendList(se.getUserName(), cmInfo);
			if(friendList != null)
				nTotalNumFriends = friendList.size();
			else
				nTotalNumFriends = 0;
			int nRemain = nTotalNumFriends;
			int nIndex = 0;
			int i = 0;
			ArrayList<String> curList = new ArrayList<String>();
			
			if(nRemain == 0)
			{
				seAck = new CMSNSEvent();
				seAck.setID(CMSNSEvent.RESPONSE_FRIEND_LIST);
				seAck.setUserName(se.getUserName());
				seAck.setTotalNumFriends(0);
				seAck.setNumFriends(0);
				CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
				seAck = null;
			}
			
			while(nRemain > 0)
			{
				curList.add(friendList.get(nIndex++));
				i++;
				nRemain--;
				if(i >= 50)
				{
					seAck = new CMSNSEvent();
					seAck.setID(CMSNSEvent.RESPONSE_FRIEND_LIST);
					seAck.setUserName(se.getUserName());
					seAck.setTotalNumFriends(nTotalNumFriends);
					seAck.setNumFriends(i);
					seAck.setFriendList(curList);
					CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);

					seAck = null;
					i = 0;
					curList.clear();
				}
			}
			if(i > 0)
			{
				seAck = new CMSNSEvent();
				seAck.setID(CMSNSEvent.RESPONSE_FRIEND_LIST);
				seAck.setUserName(se.getUserName());
				seAck.setTotalNumFriends(nTotalNumFriends);
				seAck.setNumFriends(i);
				seAck.setFriendList(curList);
				CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);

				seAck = null;
			}
			
			curList = null;
		}
		else
		{
			System.out.println("CMSNSManager.processREQUEST_FRIEND_LIST(), CM DB is not used");

			seAck = new CMSNSEvent();
			seAck.setID(CMSNSEvent.RESPONSE_FRIEND_LIST);
			seAck.setUserName(se.getUserName());
			seAck.setTotalNumFriends(0);
			seAck.setNumFriends(0);
			CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
			seAck = null;
		}
		
		friendList = null;
		return;
	}
	
	private static void processRESPONSE_FRIEND_LIST(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processRESPONSE_FRIEND_LIST(), user("+se.getUserName()
					+"), total # friends("+se.getTotalNumFriends()+"), cur # friends("
					+se.getNumFriends()+").");
			System.out.print("Friends: ");
			for(int i = 0; i < se.getFriendList().size(); i++)
			{
				System.out.print(se.getFriendList().get(i)+" ");
			}
			System.out.println();
		}

		return;
	}
	
	private static void processREQUEST_FRIEND_REQUESTER_LIST(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		ArrayList<String> requesterList = new ArrayList<String>();
		int nNumRequesters = 0;
		CMSNSEvent seAck = null;

		// check if DB is used or not
		if(confInfo.isDBUse())
		{
			/*
			int nNumCandidates = 0;
			ArrayList<String> candidateList = new ArrayList<String>();
			ArrayList<String> myFriendList = new ArrayList<String>();

			CMDBInfo dbInfo = cmInfo.getDBInfo();
			ResultSet rs = null;
			int i = 0;
			
			// get users who added me as a friend
			CMDBManager.queryGetRequestersList(se.getUserName(), cmInfo);
			rs = dbInfo.getResultSet();
			try {
				while(rs.next())
				{
					candidateList.add(rs.getString("userName"));
					nNumCandidates++;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			// get users whom I added as friends
			CMDBManager.queryGetFriendsList(se.getUserName(), cmInfo);
			rs = dbInfo.getResultSet();
			try {
				while(rs.next())
				{
					myFriendList.add(rs.getString("friendName"));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			// If users in the list are not my friends, add them to a new list
			// because current 'candidateList' includes users whom I added as friends as well. 
			for(i = 0; i < nNumCandidates; i++)
			{
				String strUser = candidateList.get(i);
				if(!myFriendList.contains(strUser))
				{
					requesterList.add(strUser);
					nNumRequesters++;
				}
			}
			*/
			requesterList = CMSNSManager.getFriendRequesterList(se.getUserName(), cmInfo);
			if(requesterList != null)
				nNumRequesters = requesterList.size();
			else
				nNumRequesters = 0;
			int nRemain = nNumRequesters;
			int nIndex = 0;
			int i = 0;
			ArrayList<String> curList = new ArrayList<String>();
			
			if(nRemain == 0)
			{
				seAck = new CMSNSEvent();
				seAck.setID(CMSNSEvent.RESPONSE_FRIEND_REQUESTER_LIST);
				seAck.setUserName(se.getUserName());
				seAck.setTotalNumFriends(0);
				seAck.setNumFriends(0);
				CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
				seAck = null;
			}
			
			while(nRemain > 0)
			{
				curList.add(requesterList.get(nIndex++));
				i++;
				nRemain--;
				if(i >= 50)
				{
					seAck = new CMSNSEvent();
					seAck.setID(CMSNSEvent.RESPONSE_FRIEND_REQUESTER_LIST);
					seAck.setUserName(se.getUserName());
					seAck.setTotalNumFriends(nNumRequesters);
					seAck.setNumFriends(i);
					seAck.setFriendList(curList);
					CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);

					seAck = null;
					i = 0;
					curList.clear();
				}
			}
			if(i > 0)
			{
				seAck = new CMSNSEvent();
				seAck.setID(CMSNSEvent.RESPONSE_FRIEND_REQUESTER_LIST);
				seAck.setUserName(se.getUserName());
				seAck.setTotalNumFriends(nNumRequesters);
				seAck.setNumFriends(i);
				seAck.setFriendList(curList);
				CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);

				seAck = null;
			}
			
			curList = null;
			requesterList = null;
		}
		else
		{
			System.out.println("CMSNSManager.processREQUEST_FRIEND_REQUESTER_LIST(), CM DB is not used");

			seAck = new CMSNSEvent();
			seAck.setID(CMSNSEvent.RESPONSE_FRIEND_REQUESTER_LIST);
			seAck.setUserName(se.getUserName());
			seAck.setTotalNumFriends(0);
			seAck.setNumFriends(0);
			CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
			seAck = null;
		}

		return;
	}
	
	private static void processRESPONSE_FRIEND_REQUESTER_LIST(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processRESPONSE_FRIEND_REQUESTER_LIST(), user("+se.getUserName()
					+"), total # requesters("+se.getTotalNumFriends()+"), cur # requesters("
					+se.getNumFriends()+").");
			System.out.print("Requesters: ");
			for(int i = 0; i < se.getFriendList().size(); i++)
			{
				System.out.print(se.getFriendList().get(i)+" ");
			}
			System.out.println();
		}

		return;
	}
	
	private static void processREQUEST_BI_FRIEND_LIST(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		ArrayList<String> biFriendList = new ArrayList<String>();
		int nNumBiFriends = 0;
		CMSNSEvent seAck = null;

		// check if DB is used or not
		if(confInfo.isDBUse())
		{
			/*
			int nNumCandidates = 0;
			ArrayList<String> candidateList = new ArrayList<String>();
			ArrayList<String> myFriendList = new ArrayList<String>();
			ArrayList<String> requesterList = new ArrayList<String>();

			CMDBInfo dbInfo = cmInfo.getDBInfo();
			ResultSet rs = null;
			int i = 0;
			
			// get users who added me as a friend
			CMDBManager.queryGetRequestersList(se.getUserName(), cmInfo);
			rs = dbInfo.getResultSet();
			try {
				while(rs.next())
				{
					candidateList.add(rs.getString("userName"));
					nNumCandidates++;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			// get users whom I added as friends
			CMDBManager.queryGetFriendsList(se.getUserName(), cmInfo);
			rs = dbInfo.getResultSet();
			try {
				while(rs.next())
				{
					myFriendList.add(rs.getString("friendName"));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			// If users in the list are not my friends, add them to a new list
			// because current 'candidateList' includes users whom I added as friends as well. 
			for(i = 0; i < nNumCandidates; i++)
			{
				String strUser = candidateList.get(i);
				if(myFriendList.contains(strUser))
				{
					requesterList.add(strUser);
					nNumBiFriends++;
				}
			}
			*/
			biFriendList = CMSNSManager.getBiFriendList(se.getUserName(), cmInfo);
			if(biFriendList != null)
				nNumBiFriends = biFriendList.size();
			else
				nNumBiFriends = 0;
			int nRemain = nNumBiFriends;
			int nIndex = 0;
			int i = 0;
			ArrayList<String> curList = new ArrayList<String>();
			
			if(nRemain == 0)
			{
				seAck = new CMSNSEvent();
				seAck.setID(CMSNSEvent.RESPONSE_BI_FRIEND_LIST);
				seAck.setUserName(se.getUserName());
				seAck.setTotalNumFriends(0);
				seAck.setNumFriends(0);
				CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
				seAck = null;
			}
			
			while(nRemain > 0)
			{
				curList.add(biFriendList.get(nIndex++));
				i++;
				nRemain--;
				if(i >= 50)
				{
					seAck = new CMSNSEvent();
					seAck.setID(CMSNSEvent.RESPONSE_BI_FRIEND_LIST);
					seAck.setUserName(se.getUserName());
					seAck.setTotalNumFriends(nNumBiFriends);
					seAck.setNumFriends(i);
					seAck.setFriendList(curList);
					CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);

					seAck = null;
					i = 0;
					curList.clear();
				}
			}
			if(i > 0)
			{
				seAck = new CMSNSEvent();
				seAck.setID(CMSNSEvent.RESPONSE_BI_FRIEND_LIST);
				seAck.setUserName(se.getUserName());
				seAck.setTotalNumFriends(nNumBiFriends);
				seAck.setNumFriends(i);
				seAck.setFriendList(curList);
				CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);

				seAck = null;
			}
			
			curList = null;
			biFriendList = null;
		}
		else
		{
			System.out.println("CMSNSManager.processREQUEST_FRIEND_REQUESTER_LIST(), CM DB is not used");

			seAck = new CMSNSEvent();
			seAck.setID(CMSNSEvent.RESPONSE_BI_FRIEND_LIST);
			seAck.setUserName(se.getUserName());
			seAck.setTotalNumFriends(0);
			seAck.setNumFriends(0);
			CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
			seAck = null;
		}

		return;
	}
	
	private static void processRESPONSE_BI_FRIEND_LIST(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processRESPONSE_BI_FRIEND_LIST(), user("+se.getUserName()
					+"), total # bi-friends("+se.getTotalNumFriends()+"), cur # bi-friends("
					+se.getNumFriends()+").");
			System.out.print("Bi-friends: ");
			for(int i = 0; i < se.getFriendList().size(); i++)
			{
				System.out.print(se.getFriendList().get(i)+" ");
			}
			System.out.println();
		}

		return;
	}
	
	private static void processREQUEST_ATTACHED_FILES(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		CMSNSAttach sendAttach = null;
		CMSNSAttachList attachList = null;
		CMSNSAttach attach = null;
		CMSNSAttachHashtable sendAttachHashtable = null;
		ArrayList<String> filePathList = null;
		ArrayList<String> naFileNameList = null;
		int i = 0;
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processREQUEST_ATTACHED_FILES(), user("+se.getUserName()
					+"), contentID("+se.getContentID()+")");
		}
		
		if(confInfo.getSystemType().equals("CLIENT"))
		{
			// set content ID to the attachment object
			sendAttach = snsInfo.getSendSNSAttach();
			sendAttach.setContentID(se.getContentID());
			filePathList = sendAttach.getFilePathList();
			for(i = 0; i < filePathList.size(); i++)
			{				
				CMFileTransferManager.pushFile(filePathList.get(i), se.getUserName(), CMInfo.FILE_DEFAULT, 
						se.getContentID(), cmInfo);
			}
		}
		else if(confInfo.getSystemType().equals("SERVER"))
		{
			// find sendAttachHashtable
			sendAttachHashtable = snsInfo.getSendSNSAttachHashtable();
			attachList = sendAttachHashtable.findSNSAttachList(se.getUserName());
			if(attachList == null)
			{
				System.err.println("CMSNSManager.processREQUEST_ATTACHED_FILES(), attach list for user("
						+se.getUserName()+") not found!");
				return;
			}
			attach = attachList.findSNSAttach(se.getContentID());
			if(attach == null)
			{
				System.err.println("CMSNSManager.processREQUEST_ATTACHED_FILES(), attach for user("
						+se.getUserName()+"), contentID("+se.getContentID()+") not found!");
				return;
			}
			// get the file path list from the found attachment
			filePathList = attach.getFilePathList();
			naFileNameList = new ArrayList<String>();
			for(i = 0; i < filePathList.size(); i++)
			{
				String strFilePath = filePathList.get(i);
				File file = new File(strFilePath);
				if(!file.exists())
				{
					if(CMInfo._CM_DEBUG)
					{
						System.err.println("CMSNSManager.processREQUEST_ATTACHED_FILES(), "
								+ "content id("+se.getContentID()+"), file("+strFilePath
								+") not found");
					}
					String strFileName = CMFileTransferManager.getFileNameFromPath(strFilePath);
					naFileNameList.add(strFileName);
				}
				else
				{
					CMFileTransferManager.pushFile(strFilePath, se.getUserName(), CMInfo.FILE_DEFAULT, 
							se.getContentID(), cmInfo);
				}
				file = null;
			}
			
			if(naFileNameList.size() > 0)
			{
				// send the list of file names that are not found.
				CMSNSEvent seAck = new CMSNSEvent();
				seAck.setID(CMSNSEvent.ATTACHED_FILES_NOT_FOUND);
				seAck.setUserName(se.getUserName());
				seAck.setContentID(se.getContentID());
				seAck.setNumAttachedFiles(naFileNameList.size());
				seAck.setFileNameList(naFileNameList);
				CMEventManager.unicastEvent(seAck, se.getUserName(), cmInfo);
				seAck = null;
			}
			naFileNameList = null;
		}
		else
		{
			System.err.println("CMSNSManager.processREQUEST_ATTACHED_FILES(), wrong CM system type!");
		}
	}
	
	private static void processATTACHED_FILES_NOT_FOUND(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMSNSInfo snsInfo = cmInfo.getSNSInfo();
		String strDefServer = cmInfo.getInteractionInfo().getDefaultServerInfo()
				.getServerName();
		CMSNSAttach attach = null;
		CMSNSAttachList attachList = null;
		int nContentID = -1;
		ArrayList<String> strFileNameList = null;
		int nCompleted = -1;
		int i = 0;
		
		if(!confInfo.getSystemType().equals("CLIENT"))
		{
			System.err.println("CMSNSManager.processATTACHED_FILES_NOT_FOUND(), wrong system type!");
			return;
		}
		
		// send end_file_transfer_ack
		strFileNameList = se.getFileNameList();
		for(i = 0; i < strFileNameList.size(); i++)
		{
			CMFileEvent fe = new CMFileEvent();
			fe.setID(CMFileEvent.END_FILE_TRANSFER_ACK);
			fe.setFileSender(strDefServer);
			fe.setFileReceiver(se.getUserName());
			fe.setFileName(strFileNameList.get(i));
			fe.setReturnCode(0);	// the file is not received
			fe.setContentID(se.getContentID());
			CMEventManager.unicastEvent(fe, strDefServer, cmInfo);
			fe = null;
		}
		
		// update attachment info to be received
		attachList = snsInfo.getRecvSNSAttachList();
		nContentID = se.getContentID();
		attach = attachList.findSNSAttach(nContentID);
		if(attach == null) return;

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processATTACHED_FILES_NOT_FOUND(), "
					+"("+se.getNumAttachedFiles()+") files not found for requester("
					+se.getUserName()+"), contentID("+se.getContentID()+"): ");
			for(i = 0; i < strFileNameList.size(); i++)
			{
				System.out.println(strFileNameList.get(i));
			}
			System.out.println("#attachement: "+attach.getFilePathList().size()+"#completed: "
					+attach.getNumCompleted()+":");
			for(i = 0; i < attach.getFilePathList().size(); i++)
			{
				System.out.println(attach.getFilePathList().get(i));
			}
			
		}
		
		// increase the number of completed attached files
		nCompleted = attach.getNumCompleted() + strFileNameList.size();
		attach.setNumCompleted(nCompleted);
		// check if all attached files of the content have been transfered or not
		if(nCompleted == attach.getFilePathList().size())
		{
			// remove the completed attachment info
			attachList.removeSNSAttach(nContentID);
			attach = null;
		}

		return;
	}
	
	private static void processREQUEST_ATTACHED_FILE(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		String strRequester = se.getUserName();
		int nContentID = se.getContentID();
		String strWriter = se.getWriterName();
		String strFileName = se.getFileName();

		CMSNSEvent seAck = new CMSNSEvent();
		seAck.setID(CMSNSEvent.RESPONSE_ATTACHED_FILE);
		seAck.setUserName(strRequester);
		seAck.setContentID(nContentID);
		seAck.setWriterName(strWriter);
		seAck.setFileName(strFileName);

		// check whether the requested file exists or not
		String strFilePath = confInfo.getTransferedFileHome().toString() + File.separator + 
				strWriter + File.separator + strFileName;
		int nReturnCode = -1;
		File file = new File(strFilePath);
		if(!file.exists())
		{
			System.err.println("CMSNSManager.processREQUEST_ATTACHED_FILE(), "
					+"file("+strFilePath+") not found!");
			nReturnCode = 0;
			seAck.setReturnCode(nReturnCode);
			CMEventManager.unicastEvent(seAck, strRequester, cmInfo);
			seAck = null;
			file = null;
			return;
		}
		
		// check whether the requested file is the attachment of the given content ID or not
		ArrayList<String> attachFileList = null;
		attachFileList = CMDBManager.queryGetSNSAttachedFile(nContentID, cmInfo);
		boolean bFound = false;
		
		for(int i = 0; attachFileList != null && i < attachFileList.size(); i++)
		{
			String strAttachPath = attachFileList.get(i);
			String strAttachFileName = strAttachPath.substring(strAttachPath.lastIndexOf(File.separator)+1);
			if(strFileName.equals(strAttachFileName))
				bFound = true;
		}
		
		if(!bFound)
		{
			System.err.println("CMSNSManager.processREQUEST_ATTACHED_FILE(), file("+strFileName
					+") is not the attachment of content ID("+nContentID+")!");
			nReturnCode = 0;
			seAck.setReturnCode(nReturnCode);
			CMEventManager.unicastEvent(seAck, strRequester, cmInfo);
			seAck = null;
			file = null;
			return;			
		}

		// check the prefetching mode
		CMUser user = interInfo.getLoginUsers().findMember(strRequester);
		if(user == null)
		{
			System.err.println("CMSNSManager.processREQUEST_ATTACHED_FILE(), user("+strRequester+") is null!");
			return;
		}
		if(user.getAttachDownloadScheme() == CMInfo.SNS_ATTACH_PREFETCH)
		{
			Calendar date = Calendar.getInstance();
			CMSNSAttachAccessHistoryList historyList = user.getAttachAccessHistoryList();
			CMSNSAttachAccessHistory history = null;
			history = historyList.findAccessHistory(strRequester, date, strWriter);
			if(history != null)
			{
				// update the access history
				int nAccessCount = history.getAccessCount() + 1;
				history.setAccessCount(nAccessCount);
				history.setUpdated(true);
				if(CMInfo._CM_DEBUG)
				{
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					System.out.println("CMSNSManager.processREQUEST_ATTACHED_FILE(), access history updated for "
							+"user("+strRequester+"), date("+dateFormat.format(date.getTime())+"), writer("
							+strWriter+"), accessCount("+nAccessCount+").");
				}
			}
			else
			{
				// add a new access history
				history = new CMSNSAttachAccessHistory(strRequester, date, strWriter, 1);
				history.setAdded(true);
				historyList.addAccessHistory(history);
				if(CMInfo._CM_DEBUG)
				{
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					System.out.println("CMSNSManager.processREQUEST_ATTACHED_FILE(), access history added for "
							+"user("+strRequester+"), date("+dateFormat.format(date.getTime())+"), writer("
							+strWriter+"), accessCount(1).");
				}
			}
		}
		
		// send the requested file
		CMFileTransferManager.pushFile(strFilePath, strRequester, cmInfo);
		
		// send the response event
		nReturnCode = 1;
		seAck.setReturnCode(nReturnCode);
		CMEventManager.unicastEvent(seAck, strRequester, cmInfo);
		seAck = null;
		file = null;
		
		return;
	}
	
	private static void processRESPONSE_ATTACHED_FILE(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processRESPONSE_ATTACHED_FILE(): ");
			System.out.println("requester("+se.getUserName()+"), contentID("+se.getContentID()
					+"), writer("+se.getWriterName()+"), return code("+se.getReturnCode()+")");
		}

		return;
	}
	
	private static void processCHANGE_ATTACH_DOWNLOAD_SCHEME(CMSNSEvent se, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		confInfo.setAttachDownloadScheme(se.getAttachDownloadScheme());
		//CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		//CMUser myself = interInfo.getMyself();
		//myself.setAttachDownloadScheme(se.getAttachDownloadScheme());
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processEvent(), server changes the scheme for "
					+"attachement download of SNS content to ("+se.getAttachDownloadScheme()+").");
		}
		return;
	}
	
	private static void processACCESS_ATTACHED_FILE(CMSNSEvent se, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		
		String strRequester = se.getUserName();
		//int nContentID = se.getContentID();
		String strWriter = se.getWriterName();
		//String strFileName = se.getFileName();

		// check the prefetching mode
		CMUser user = interInfo.getLoginUsers().findMember(strRequester);
		if(user == null)
		{
			System.err.println("CMSNSManager.processACCESS_ATTACHED_FILE(), user("+strRequester+") is null!");
			return;
		}
		if(user.getAttachDownloadScheme() == CMInfo.SNS_ATTACH_PREFETCH)
		{
			Calendar date = Calendar.getInstance();
			CMSNSAttachAccessHistoryList historyList = user.getAttachAccessHistoryList();
			CMSNSAttachAccessHistory history = null;
			history = historyList.findAccessHistory(strRequester, date, strWriter);
			if(history != null)
			{
				// update the access history
				int nAccessCount = history.getAccessCount() + 1;
				history.setAccessCount(nAccessCount);
				history.setUpdated(true);
				if(CMInfo._CM_DEBUG)
				{
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					System.out.println("CMSNSManager.processACCESS_ATTACHED_FILE(), access history updated for "
							+"user("+strRequester+"), date("+dateFormat.format(date.getTime())+"), writer("
							+strWriter+"), accessCount("+nAccessCount+").");
				}
			}
			else
			{
				// add a new access history
				history = new CMSNSAttachAccessHistory(strRequester, date, strWriter, 1);
				history.setAdded(true);
				historyList.addAccessHistory(history);
				if(CMInfo._CM_DEBUG)
				{
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					System.out.println("CMSNSManager.processACCESS_ATTACHED_FILE(), access history added for "
							+"user("+strRequester+"), date("+dateFormat.format(date.getTime())+"), writer("
							+strWriter+"), accessCount(1).");
				}
			}
		}

	}
	
	private static void processPREFETCH_COMPLETED(CMSNSEvent se, CMInfo cmInfo)
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMSNSManager.processPREFETCH_COMPLETED(), user("+se.getUserName()+") !");
		}
		return;
	}

}
