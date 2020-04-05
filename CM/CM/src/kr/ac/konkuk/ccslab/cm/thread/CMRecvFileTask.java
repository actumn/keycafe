package kr.ac.konkuk.ccslab.cm.thread;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;

public class CMRecvFileTask implements Runnable {

	CMRecvFileInfo m_recvFileInfo;
	CMInfo m_cmInfo;
	
	public CMRecvFileTask(CMRecvFileInfo recvFileInfo, CMInfo cmInfo)
	{
		m_recvFileInfo = recvFileInfo;
		m_cmInfo = cmInfo;
	}
	
	@Override
	public void run() {
		
		RandomAccessFile raf = null;
		FileChannel fc = null;
		long lRecvSize = m_recvFileInfo.getRecvSize();
		long lFileSize = m_recvFileInfo.getFileSize();
		SocketChannel recvSC = m_recvFileInfo.getRecvChannel();
		int nRecvBytes = -1;
		int nWrittenBytes = -1;
		int nWrittenBytesSum = -1;
		ByteBuffer buf = ByteBuffer.allocateDirect(CMInfo.FILE_BLOCK_LEN);
		boolean bInterrupted = false;
		
		// open the file
		try {
			raf = new RandomAccessFile(m_recvFileInfo.getFilePath(), "rw");
			fc = raf.getChannel();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendErrorToProcThread();
			return;
		}
		
		// skip file offset by the previously received size
		if(lRecvSize > 0)
		{
			try {
				//raf.seek(lRecvSize);
				fc.position(lRecvSize);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				closeRandomAccessFile(raf);
				sendErrorToProcThread();
				return;
			}
		}
		
		// main loop for receiving and writing file blocks
		nRecvBytes = 0;
		while(lRecvSize < lFileSize && !bInterrupted)
		{
			// check for interrupt by other thread
			if(Thread.currentThread().isInterrupted())
			{
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMRecvFileTask.run(); interrupted at the outer loop! file name("
							+m_recvFileInfo.getFileName()+"), file size("+lFileSize+"), recv size("+lRecvSize+").");
				}
				bInterrupted = true;
				continue;
			}
			
			// initialize the ByteBuffer
			buf.clear();
			
			// receive a file block
			try {
				nRecvBytes = recvSC.read(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				closeRandomAccessFile(raf);
				try {
					recvSC.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				sendErrorToProcThread();
				return;
			}
			
			// write the received block to the file
			buf.flip();
			nWrittenBytesSum = 0;
			while(nWrittenBytesSum < nRecvBytes && !bInterrupted)
			{
				if(Thread.currentThread().isInterrupted())
				{
					if(CMInfo._CM_DEBUG)
					{
						System.out.println("CMRecvFileTask.run(); interrupted at the inner loop! file name("
								+m_recvFileInfo.getFileName()+"), file size("+lFileSize+"), recv size("+lRecvSize+").");						
					}
					bInterrupted = true;
					continue;
				}
				
				try {
					nWrittenBytes = fc.write(buf);
					
					// update the size of received and written file blocks
					lRecvSize += nWrittenBytes;
					m_recvFileInfo.setRecvSize(lRecvSize);
					nWrittenBytesSum += nWrittenBytes;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					closeRandomAccessFile(raf);
					sendErrorToProcThread();
					return;
				}	
			} // inner while loop
			
		} // outer while loop
		
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
		fe.setID(CMFileEvent.ERR_RECV_FILE_CHAN);
		fe.setFileSender(m_recvFileInfo.getFileSender());
		fe.setFileReceiver(m_cmInfo.getInteractionInfo().getMyself().getName());
		fe.setFileName(m_recvFileInfo.getFileName());
		fe.setContentID(m_recvFileInfo.getContentID());
		ByteBuffer byteBuf = CMEventManager.marshallEvent(fe);
		
		CMBlockingEventQueue recvQueue = m_cmInfo.getCommInfo().getRecvBlockingEventQueue();
		recvQueue.push(new CMMessage(byteBuf, null));		
	}

}
