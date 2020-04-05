package kr.ac.konkuk.ccslab.cm.info;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import sun.util.logging.resources.logging;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;

import java.io.*;

public class CMFileTransferInfo {
	// key is the receiver name
	private Hashtable<String, CMList<CMSendFileInfo>> m_sendFileHashtable;
	// key is the sender name
	private Hashtable<String, CMList<CMRecvFileInfo>> m_recvFileHashtable;
	private boolean m_bCancelSend;	// flag for canceling file push with the default channel
	
	// starting time (to send/receive request pushing/pulling a file)
	private long m_lStartRequestTime;
	// starting time of sending a file after the permit granted
	private long m_lStartSendTime;
	// ending time of sending a file (receiving completion ack event)
	private long m_lEndSendTime;
	// starting time of receiving a file after the permit granted
	private long m_lStartRecvTime;
	// ending time of receiving a file (receiving completion event)
	private long m_lEndRecvTime;
	
	public CMFileTransferInfo()
	{
		m_sendFileHashtable = new Hashtable<String, CMList<CMSendFileInfo>>();
		m_recvFileHashtable = new Hashtable<String, CMList<CMRecvFileInfo>>();
		m_bCancelSend = false;
		m_lStartRequestTime = 0;
		m_lStartSendTime = 0;
		m_lEndSendTime = 0;
		m_lStartRecvTime = 0;
		m_lEndRecvTime = 0;
	}
	
	////////// set/get methods
		
	public synchronized void setStartRequestTime(long time)
	{
		m_lStartRequestTime = time;
	}
	
	public synchronized long getStartRequestTime()
	{
		return m_lStartRequestTime;
	}
	
	public synchronized void setStartSendTime(long time)
	{
		m_lStartSendTime = time;
	}
	
	public synchronized long getStartSendTime()
	{
		return m_lStartSendTime;
	}
	
	public synchronized void setEndSendTime(long time)
	{
		m_lEndSendTime = time;
	}
	
	public synchronized long getEndSendTime()
	{
		return m_lEndSendTime;
	}
	
	public synchronized void setStartRecvTime(long time)
	{
		m_lStartRecvTime = time;
	}
	
	public synchronized long getStartRecvTime()
	{
		return m_lStartRecvTime;
	}
	
	public synchronized void setEndRecvTime(long time)
	{
		m_lEndRecvTime = time;
	}
	
	public synchronized long getEndRecvTime()
	{
		return m_lEndRecvTime;
	}
	
	public synchronized void setCancelSend(boolean bCancel)
	{
		m_bCancelSend = bCancel;
		return;
	}
	
	public synchronized boolean isCancelSend()
	{
		return m_bCancelSend;
	}
		
	
	////////// add/remove/find sending file info
	
	public synchronized boolean addSendFileInfo(String uName, String fPath, long lSize, int nContentID)
	{
		CMSendFileInfo sInfo = null;
		String strFileName = null;
		CMList<CMSendFileInfo> sInfoList = null;
		boolean bResult = false;
		
		strFileName = fPath.substring(fPath.lastIndexOf(File.separator)+1);
		sInfo = new CMSendFileInfo();
		sInfo.setFileReceiver(uName);
		sInfo.setFileName(strFileName);
		sInfo.setFilePath(fPath);
		sInfo.setFileSize(lSize);
		sInfo.setContentID(nContentID);
		
		sInfoList = m_sendFileHashtable.get(uName);
		if(sInfoList == null)
		{
			sInfoList = new CMList<CMSendFileInfo>();
			m_sendFileHashtable.put(uName, sInfoList);
		}
		
		bResult = sInfoList.addElement(sInfo);
		if(!bResult)
		{
			System.err.println("CMFileTransferInfo.addSendFileInfo() failed: "+sInfo.toString());
			return false;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.addSendFileInfo() done: "+sInfo.toString());
			System.out.println("# current hashtable elements: "+m_sendFileHashtable.size());
		}

		return true;
	}

	public synchronized boolean addSendFileInfo(CMSendFileInfo sInfo)
	{
		String strFileName = null;
		CMList<CMSendFileInfo> sInfoList = null;
		boolean bResult = false;
		
		strFileName = sInfo.getFilePath().substring(sInfo.getFilePath().lastIndexOf(File.separator)+1);
		sInfo.setFileName(strFileName);
		
		sInfoList = m_sendFileHashtable.get(sInfo.getFileReceiver());
		if(sInfoList == null)
		{
			sInfoList = new CMList<CMSendFileInfo>();
			m_sendFileHashtable.put(sInfo.getFileReceiver(), sInfoList);
		}
		
		bResult = sInfoList.addElement(sInfo);
		if(!bResult)
		{
			System.err.println("CMFileTransferInfo.addSendFileInfo() failed: "+sInfo.toString());
			return false;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.addSendFileInfo() done: "+sInfo.toString());
			System.out.println("# current hashtable elements: "+m_sendFileHashtable.size());
		}

		return true;		
	}

	public synchronized CMSendFileInfo findSendFileInfo(String uName, String fName, int nContentID)
	{
		CMSendFileInfo sInfo = null;
		CMList<CMSendFileInfo> sInfoList = null;
		CMSendFileInfo tInfo = null;
		
		sInfoList = m_sendFileHashtable.get(uName);
		if(sInfoList == null)
		{
			System.err.println("CMFileTransferInfo.findSendFileInfo(), list not found for receiver("
					+uName+")");
			return null;
		}
		
		tInfo = new CMSendFileInfo();
		tInfo.setFileReceiver(uName);
		tInfo.setFileName(fName);
		tInfo.setContentID(nContentID);
		
		sInfo = sInfoList.findElement(tInfo);
		
		if(sInfo == null)
		{
			System.err.println("CMFileTransferInfo.findSendFileInfo(), not found!: "+tInfo.toString());
			return null;
		}
		
		return sInfo;
	}

	public synchronized boolean removeSendFileInfo(String uName, String fName, int nContentID)
	{
		CMList<CMSendFileInfo> sInfoList = null;
		CMSendFileInfo sInfo = null;
		boolean bResult = false;

		sInfoList = m_sendFileHashtable.get(uName);
		if(sInfoList == null)
		{
			//System.err.println("CMFileTransferInfo.removeSendFileInfo(), list not found for receiver("
			//		+uName+")");
			return false;
		}
		
		sInfo = new CMSendFileInfo();
		sInfo.setFileReceiver(uName);
		sInfo.setFileName(fName);
		sInfo.setContentID(nContentID);
		bResult = sInfoList.removeElement(sInfo);

		if(!bResult)
		{
			System.err.println("CMFileTransferInfo.removeSendFileInfo() error! : "+sInfo.toString());
			return false;
		}
		
		if(sInfoList.isEmpty())
		{
			m_sendFileHashtable.remove(uName);
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.removeSendFileInfo() done : "+sInfo.toString());
			System.out.println("# current hashtable elements: "+m_sendFileHashtable.size());
		}
		
		return true;
		
	}
	
	public synchronized boolean removeSendFileInfo(CMSendFileInfo sfInfo)
	{
		CMList<CMSendFileInfo> sInfoList = null;
		String strFileReceiver = sfInfo.getFileReceiver();
		boolean bResult = false;

		sInfoList = m_sendFileHashtable.get(strFileReceiver);
		if(sInfoList == null)
		{
			//System.err.println("CMFileTransferInfo.removeSendFileInfo(), list not found for receiver("
			//		+uName+")");
			return false;
		}
		
		bResult = sInfoList.removeElement(sfInfo);

		if(!bResult)
		{
			System.err.println("CMFileTransferInfo.removeSendFileInfo() error! : "+sfInfo.toString());
			return false;
		}
		
		if(sInfoList.isEmpty())
		{
			m_sendFileHashtable.remove(strFileReceiver);
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.removeSendFileInfo() done : "+sfInfo.toString());
			System.out.println("# current hashtable elements: "+m_sendFileHashtable.size());
		}
		
		return true;		
	}
	
	public synchronized boolean removeSendFileList(String strReceiver)
	{
		CMList<CMSendFileInfo> sInfoList = null;
		sInfoList = m_sendFileHashtable.remove(strReceiver);
		if(sInfoList == null)
		{
			//System.err.println("CMFileTransferInfo.removeSendFileList(); list not found for receiver("
			//		+strReceiver+")!");
			return false;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.removeSendFileList() done : receiver("+strReceiver+").");
			System.out.println("# current hashtable elements: "+m_sendFileHashtable.size());
		}
		
		return true;
	}
	
	public synchronized boolean clearSendFileHashtable()
	{
		m_sendFileHashtable.clear();
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMFileTransferInfo.clearSendFileHashtable(); current size("
					+m_sendFileHashtable.size()+").");
		return true;
	}
	
	public synchronized CMList<CMSendFileInfo> getSendFileList(String strReceiver)
	{
		CMList<CMSendFileInfo> sendFileList = null;
		sendFileList = m_sendFileHashtable.get(strReceiver);
		
		return sendFileList;
	}
	
	public synchronized Hashtable<String, CMList<CMSendFileInfo>> getSendFileHashtable()
	{
		return m_sendFileHashtable;
	}
	
	//////////////////// methods for the investigation of current state of the sending file info
	//////////////////// (used by the file transfer with separate channels and threads)

	// find the CMSendFileInfo of which file is currently being sent to the receiver
	public synchronized CMSendFileInfo findSendFileInfoOngoing(String strReceiver)
	{
		CMSendFileInfo sInfo = null;
		CMList<CMSendFileInfo> sInfoList = m_sendFileHashtable.get(strReceiver);
		boolean bFound = false;
		
		if(sInfoList == null) return null;
		
		Iterator<CMSendFileInfo> iter = sInfoList.getList().iterator();
		while(iter.hasNext() && !bFound)
		{
			sInfo = iter.next();
			if(sInfo.getSendTaskResult() != null)
			{
				bFound = true;
				if(CMInfo._CM_DEBUG)
					System.out.println("CMFileTransferInfo.findSendFileInfoOngoing(); ongoing send info found: "
							+sInfo.toString());
			}
		}
		
		if(!bFound)
			sInfo = null;
		
		return sInfo;
	}
	
	// find the sending file info that is not yet started by the thread pool
	public synchronized CMSendFileInfo findSendFileInfoNotStarted(String strReceiver)
	{
		CMSendFileInfo sfInfo = null;
		CMList<CMSendFileInfo> sfInfoList = m_sendFileHashtable.get(strReceiver);
		boolean bFound = false;
		
		if(sfInfoList == null) return null;
		
		Iterator<CMSendFileInfo> iter = sfInfoList.getList().iterator();
		while(iter.hasNext() && !bFound)
		{
			sfInfo = iter.next();
			if(sfInfo.getSendTaskResult() == null)
			{
				bFound = true;
				if(CMInfo._CM_DEBUG)
					System.out.println("CMFileTransferInfo.findSendFileInfoNotStarted(); found: "+sfInfo.toString());
			}
		}
		
		if(bFound)
			return sfInfo;
		else
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMFileTransferInfo.findRecvFileInfoNotStarted(); not found!");
			return null;
		}
		
	}

	////////// add/remove/find receiving file info

	public synchronized boolean addRecvFileInfo(String senderName, String fName, 
			long lSize, int nContentID,	long lRecvSize, RandomAccessFile writeFile)
	{
		CMRecvFileInfo rInfo = null;
		CMList<CMRecvFileInfo> rInfoList = null;
		boolean bResult = false;

		rInfo = null;
		rInfo = new CMRecvFileInfo();
		rInfo.setFileSender(senderName);
		rInfo.setFileName(fName);
		rInfo.setFileSize(lSize);
		rInfo.setContentID(nContentID);
		rInfo.setRecvSize(lRecvSize);
		rInfo.setWriteFile(writeFile);
		
		rInfoList = m_recvFileHashtable.get(senderName);
		if(rInfoList == null)
		{
			rInfoList = new CMList<CMRecvFileInfo>();
			m_recvFileHashtable.put(senderName, rInfoList);
		}
		
		bResult = rInfoList.addElement(rInfo);
		if(!bResult)
		{
			System.err.println("CMFileTransferInfo.addRecvFileInfo() failed: "+rInfo.toString());
			return false;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.addRecvFileInfo() done: "+rInfo.toString());
			System.out.println("# current hashtable elements: "+m_recvFileHashtable.size());
		}
		
		return true;
	}
	
	public synchronized boolean addRecvFileInfo(CMRecvFileInfo rInfo)
	{
		CMList<CMRecvFileInfo> rInfoList = null;
		boolean bResult = false;
		
		rInfoList = m_recvFileHashtable.get(rInfo.getFileSender());
		if(rInfoList == null)
		{
			rInfoList = new CMList<CMRecvFileInfo>();
			m_recvFileHashtable.put(rInfo.getFileSender(), rInfoList);
		}
		
		bResult = rInfoList.addElement(rInfo);
		if(!bResult)
		{
			System.err.println("CMFileTransferInfo.addRecvFileInfo() failed: "+rInfo.toString());
			return false;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.addRecvFileInfo() done: "+rInfo.toString());
			System.out.println("# current hashtable elements: "+m_recvFileHashtable.size());
		}
		
		return true;		
	}

	public synchronized CMRecvFileInfo findRecvFileInfo(String senderName, String fName, int nContentID)
	{	
		CMRecvFileInfo rInfo = null;
		CMList<CMRecvFileInfo> rInfoList = null;
		CMRecvFileInfo tInfo = null;
		
		rInfoList = m_recvFileHashtable.get(senderName);
		if(rInfoList == null)
		{
			System.err.println("CMFileTransferInfo.findRecvFileInfo(), list not found for sender("
					+senderName+")");
			return null;
		}
		
		tInfo = new CMRecvFileInfo();
		tInfo.setFileSender(senderName);
		tInfo.setFileName(fName);
		tInfo.setContentID(nContentID);
		
		rInfo = rInfoList.findElement(tInfo);
		
		if(rInfo == null)
		{
			System.err.println("CMFileTransferInfo.findRecvFileInfo(), not found!: "
					+tInfo.toString());
			return null;
		}
				
		return rInfo;

	}

	public synchronized boolean removeRecvFileInfo(String senderName, String fName, 
			int nContentID)
	{
		CMList<CMRecvFileInfo> rInfoList = null;
		CMRecvFileInfo rInfo = null;
		boolean bResult = false;

		rInfoList = m_recvFileHashtable.get(senderName);
		if(rInfoList == null)
		{
			//System.err.println("CMFileTransferInfo.removeRecvFileInfo(), list not found for sender("
			//		+senderName+")");
			return false;
		}
		
		rInfo = new CMRecvFileInfo();
		rInfo.setFileSender(senderName);
		rInfo.setFileName(fName);
		rInfo.setContentID(nContentID);
		bResult = rInfoList.removeElement(rInfo);

		if(!bResult)
		{
			System.err.println("CMFileTransferInfo.removeRecvFileInfo() error! : "+rInfo.toString());
			return false;
		}
		
		if(rInfoList.isEmpty())
		{
			m_recvFileHashtable.remove(senderName);
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.removeRecvFileInfo() done : "+rInfo.toString());
			System.out.println("# current hashtable elements: "+m_recvFileHashtable.size());
		}
		
		return true;
		
	}

	public synchronized boolean removeRecvFileList(String strSender)
	{
		CMList<CMRecvFileInfo> rInfoList = null;
		rInfoList = m_recvFileHashtable.remove(strSender);
		if(rInfoList == null)
		{
			//System.err.println("CMFileTransferInfo.removeRecvFileList(); list not found for sender("
			//		+strSender+")!");
			return false;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMFileTransferInfo.removeRecvFileList() done : sender("+strSender+").");
			System.out.println("# current hashtable elements: "+m_recvFileHashtable.size());
		}
		
		return true;
	}

	public synchronized boolean clearRecvFileHashtable()
	{
		m_recvFileHashtable.clear();
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMFileTransferInfo.clearRecvFileHashtable(); current size("
					+m_sendFileHashtable.size()+").");
		return true;
	}
	

	public synchronized CMList<CMRecvFileInfo> getRecvFileList(String strSender)
	{
		CMList<CMRecvFileInfo> recvFileList = null;
		recvFileList = m_recvFileHashtable.get(strSender);
		
		return recvFileList;
	}
	
	public synchronized Hashtable<String, CMList<CMRecvFileInfo>> getRecvFileHashtable()
	{
		return m_recvFileHashtable;
	}
	
	//////////////////// methods for the investigation of current state of the receiving file info
	//////////////////// (used by the file transfer with separate channels and threads)
	
	// find the receiving file info that is not yet started by the thread pool 
	public synchronized CMRecvFileInfo findRecvFileInfoNotStarted(String strSender)
	{
		CMRecvFileInfo rfInfo = null;
		CMList<CMRecvFileInfo> rfInfoList = m_recvFileHashtable.get(strSender);
		boolean bFound = false;
		
		if(rfInfoList == null) return null;
		
		Iterator<CMRecvFileInfo> iter = rfInfoList.getList().iterator();
		while(iter.hasNext() && !bFound)
		{
			rfInfo = iter.next();
			if(rfInfo.getRecvTaskResult() == null)
			{
				bFound = true;
				if(CMInfo._CM_DEBUG)
					System.out.println("CMFileTransferInfo.findRecvFileInfoNotStarted(); found: "+rfInfo.toString());
			}
		}
		
		if(bFound)
			return rfInfo;
		else
		{
			if(CMInfo._CM_DEBUG)
				System.out.println("CMFileTransferInfo.findRecvFileInfoNotStarted(); not found!");
			return null;
		}
	}

	// check whether there is the receiving file info that is being used 
	public synchronized boolean isRecvOngoing(String strSender)
	{
		CMRecvFileInfo rfInfo = null;
		CMList<CMRecvFileInfo> rfInfoList = m_recvFileHashtable.get(strSender);
		boolean bFound = false;
		
		if(rfInfoList == null) return false;
		
		Iterator<CMRecvFileInfo> iter = rfInfoList.getList().iterator();
		while(iter.hasNext() && !bFound)
		{
			rfInfo = iter.next();
			if(rfInfo.getRecvTaskResult() != null)
			{
				bFound = true;
				if(CMInfo._CM_DEBUG)
					System.out.println("CMFileTransferInfo.isRecvOngoing(); ongoing recv info found: "+rfInfo.toString());
			}
		}
		
		return bFound;
	}
	
	// find the CMRecvFileInfo of which file is currently being received from the sender
	public synchronized CMRecvFileInfo findRecvFileInfoOngoing(String strSender)
	{
		CMRecvFileInfo rInfo = null;
		CMList<CMRecvFileInfo> rInfoList = m_recvFileHashtable.get(strSender);
		boolean bFound = false;
		
		if(rInfoList == null) return null;
		
		Iterator<CMRecvFileInfo> iter = rInfoList.getList().iterator();
		while(iter.hasNext() && !bFound)
		{
			rInfo = iter.next();
			if(rInfo.getRecvTaskResult() != null)
			{
				bFound = true;
				if(CMInfo._CM_DEBUG)
					System.out.println("CMFileTransferInfo.findRecvFileInfoOngoing(); ongoing recv info found: "
							+rInfo.toString());
			}
		}
		
		if(!bFound)
			rInfo = null;
		
		return rInfo;
	}	
}
