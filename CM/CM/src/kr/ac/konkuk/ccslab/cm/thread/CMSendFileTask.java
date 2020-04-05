package kr.ac.konkuk.ccslab.cm.thread;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
//import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;

public class CMSendFileTask implements Runnable {

	CMSendFileInfo m_sendFileInfo;
	CMBlockingEventQueue m_sendQueue;
	CMInfo m_cmInfo;
	
	public CMSendFileTask(CMSendFileInfo sendFileInfo, CMInfo cmInfo)
	{
		m_sendFileInfo = sendFileInfo;
		m_cmInfo = cmInfo;
		m_sendQueue = cmInfo.getCommInfo().getSendBlockingEventQueue();
	}
	
	@Override
	public void run() {
		RandomAccessFile raf = null;
		FileChannel fc = null;
		long lSentSize = m_sendFileInfo.getSentSize();
		long lFileSize = m_sendFileInfo.getFileSize();
		SocketChannel sendSC = m_sendFileInfo.getSendChannel();
		int nReadBytes = -1;
		int nSendBytes = -1;
		int nSendBytesSum = -1;
		ByteBuffer buf = ByteBuffer.allocateDirect(CMInfo.FILE_BLOCK_LEN);
		CMFileEvent fe = null;
		boolean bInterrupted = false;
		CMInteractionInfo interInfo = m_cmInfo.getInteractionInfo();

		// open the file
		try {
			raf = new RandomAccessFile(m_sendFileInfo.getFilePath(), "rw");
			fc = raf.getChannel();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendErrorToProcThread();
			return;
		}
		
		// skip file offset by the previously sent size
		if(lSentSize > 0)
		{
			try {
				//raf.seek(lRecvSize);
				fc.position(lSentSize);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				closeRandomAccessFile(raf);
				sendErrorToProcThread();
				return;
			}
		}

		// main loop for receiving and writing file blocks
		nSendBytes = 0;
		while( lSentSize < lFileSize && !bInterrupted)
		{
			// check for interrupt by other thread
			if(Thread.currentThread().isInterrupted())
			{
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMSendFileTask.run(); interrupted at the outer loop! file name("
							+m_sendFileInfo.getFileName()+"), file size("+lFileSize+"), sent size("+lSentSize+").");
				}

				bInterrupted = true;
				continue;
			}
			
			// initialize the ByteBuffer
			buf.clear();
			
			// read a file block
			try {
				nReadBytes = fc.read(buf);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				closeRandomAccessFile(raf);
				sendErrorToProcThread();
				return;
			}
			
			// send a file block
			buf.flip();
			nSendBytesSum = 0;
			while(nSendBytesSum < nReadBytes && !bInterrupted)
			{
				if(Thread.currentThread().isInterrupted())
				{
					if(CMInfo._CM_DEBUG)
					{
						System.out.println("CMSendFileTask.run(); interrupted at the inner loop! file name("
								+m_sendFileInfo.getFileName()+"), file size("+lFileSize+"), sent size("+lSentSize+").");
					}
					bInterrupted = true;
					continue;
				}
				
				try {
					nSendBytes = sendSC.write(buf);
					
					// update the size of read and sent file blocks
					lSentSize += nSendBytes;
					m_sendFileInfo.setSentSize(lSentSize);
					nSendBytesSum += nSendBytes;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					closeRandomAccessFile(raf);
					try {
						sendSC.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					sendErrorToProcThread();
					return;
				}
			} // inner while loop
			
		} // outer while loop

		//if(!bInterrupted)
		if(lSentSize >= lFileSize)
		{
			if(lSentSize > lFileSize)
			{
				System.err.println("CMSendFileTask.run(); the receiver("+m_sendFileInfo.getFileReceiver()+") already has "
						+ "a bigger size file("+m_sendFileInfo.getFileName()+"); sender size("+lFileSize
						+ "), receiver size("+lSentSize+")");
			}
			
			// send END_FILE_TRANSFER_CHAN with the default TCP socket channel
			fe = new CMFileEvent();
			fe.setID(CMFileEvent.END_FILE_TRANSFER_CHAN);			
			fe.setFileSender(m_sendFileInfo.getFileSender());
			fe.setFileReceiver(m_sendFileInfo.getFileReceiver());
			fe.setFileName(m_sendFileInfo.getFileName());
			fe.setFileSize(m_sendFileInfo.getFileSize());
			fe.setContentID(m_sendFileInfo.getContentID());
			
			if(CMFileTransferManager.isP2PFileTransfer(fe, m_cmInfo))
			{
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMSendFileTask.run(), isP2PFileTransfer() "
							+ "returns true.");
				}
				// set event sender and receiver
				fe.setSender(interInfo.getMyself().getName());
				String strDefServer = interInfo.getDefaultServerInfo().getServerName();
				fe.setReceiver(strDefServer);
				// set distribution fields
				fe.setDistributionSession("CM_ONE_USER");
				fe.setDistributionGroup(m_sendFileInfo.getFileReceiver());
			}
			else
			{
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMSendFileTask.run(), isP2PFileTransfer() "
							+ "returns false.");
				}
				// set event sender and receiver
				fe.setSender(interInfo.getMyself().getName()); // event sender
				fe.setReceiver(m_sendFileInfo.getFileReceiver()); // event receiver
			}
			
			CMMessage msg = new CMMessage(CMEventManager.marshallEvent(fe), m_sendFileInfo.getDefaultChannel());
			m_sendQueue.push(msg);
			//CMCommManager.sendMessage(CMEventManager.marshallEvent(fe), m_sendFileInfo.getDefaultChannel());
			//fe = null;
		}

		closeRandomAccessFile(raf);
		
		return;
	}
	
	private void closeRandomAccessFile(RandomAccessFile raf)
	{
		try {
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendErrorToProcThread()
	{
		CMFileEvent fe = new CMFileEvent();
		fe.setID(CMFileEvent.ERR_SEND_FILE_CHAN);
		fe.setFileSender(m_cmInfo.getInteractionInfo().getMyself().getName());
		fe.setFileReceiver(m_sendFileInfo.getFileReceiver());
		fe.setFileName(m_sendFileInfo.getFileName());
		fe.setContentID(m_sendFileInfo.getContentID());
		ByteBuffer byteBuf = CMEventManager.marshallEvent(fe);
		
		CMBlockingEventQueue recvQueue = m_cmInfo.getCommInfo().getRecvBlockingEventQueue();
		recvQueue.push(new CMMessage(byteBuf, null));
	}

}
