package kr.ac.konkuk.ccslab.cm.manager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMConfigurator {
	
	// initialize field values of server configuration or client configuration.
	// set field values of the given configuration file to the CMConfigurationInfo object.
	public static boolean init(String strConfFilePath, CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		
		File confFile = new File(strConfFilePath);
		if(!confFile.exists())
		{
			System.err.println("CMConfigurator.init(): "+strConfFilePath+" does not exist!");
			return false;
		}
		
		confInfo.setSystemType(CMConfigurator.getConfiguration(strConfFilePath, "SYS_TYPE"));
		confInfo.setServerAddress(CMConfigurator.getConfiguration(strConfFilePath, "SERVER_ADDR"));
		confInfo.setServerPort(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "SERVER_PORT")));
		confInfo.setUDPPort(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "UDP_PORT")));
		confInfo.setMulticastAddress(CMConfigurator.getConfiguration(strConfFilePath, "MULTICAST_ADDR"));
		confInfo.setMulticastPort(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "MULTICAST_PORT")));
		//confInfo.setMyAddress(InetAddress.getLocalHost().getHostAddress());
		if(confInfo.getServerAddress().equals("localhost"))
		{
			confInfo.setMyAddress("localhost");
		}
		else if(confInfo.getServerAddress().equals("127.0.0.1"))
		{
			confInfo.setMyAddress("127.0.0.1");
		}
		else
		{
			confInfo.setMyAddress(CMCommManager.getLocalIP());
		}
				
		// default download directory
		String strFilePath = CMConfigurator.getConfiguration(strConfFilePath, "FILE_PATH");
		if(strFilePath == null) strFilePath = "."; // if no default path is set, it is set to the current working directory
		confInfo.setTransferedFileHome(Paths.get(strFilePath));
		
		// default append mode for the file transfer
		confInfo.setFileAppendScheme(Byte.parseByte(CMConfigurator.getConfiguration(strConfFilePath, "FILE_APPEND_SCHEME")));
		
		// default permission mode for file-transfer request
		confInfo.setPermitFileTransferRequest(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "PERMIT_FILE_TRANSFER")));
		
		// added delay value for the simulation of transmission delay
		confInfo.setSimTransDelay(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "SIM_TRANS_DELAY")));

		// log level
		confInfo.setLogLevel(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "LOG_LEVEL")));
		int nLogLevel = confInfo.getLogLevel();
		switch(nLogLevel)
		{
		case 0:
			CMInfo._CM_DEBUG = false;
			CMInfo._CM_DEBUG_2 = false;
			break;
		case 1:
			CMInfo._CM_DEBUG = true;
			CMInfo._CM_DEBUG_2 = false;
			break;
		case 2:
			CMInfo._CM_DEBUG = true;
			CMInfo._CM_DEBUG_2 = true;
			break;
		default:
			CMInfo._CM_DEBUG = true;
			CMInfo._CM_DEBUG_2 = false;
		}
		
		// keep-alive time
		confInfo.setKeepAliveTime(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "KEEP_ALIVE_TIME")));
		
		if( confInfo.getSystemType().equals("SERVER") )
		{
			//confInfo.setMyAddress(CMConfigurator.getConfiguration(fName, "MY_ADDR"));
			confInfo.setMyPort(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "MY_PORT")));
			confInfo.setCommArch(CMConfigurator.getConfiguration(strConfFilePath, "COMM_ARCH"));
			confInfo.setFileTransferScheme(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "FILE_TRANSFER_SCHEME")));
			confInfo.setLoginScheme(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "LOGIN_SCHEME")));
			confInfo.setMaxLoginFailure(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "MAX_LOGIN_FAILURE")));
			confInfo.setSessionScheme(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "SESSION_SCHEME")));
			confInfo.setDownloadScheme(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "DOWNLOAD_SCHEME")));
			confInfo.setDownloadNum(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "DOWNLOAD_NUM")));
			confInfo.setThumbnailHorSize(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "THUMBNAIL_HOR_SIZE")));
			confInfo.setThumbnailVerSize(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "THUMBNAIL_VER_SIZE")));
			confInfo.setAttachDownloadScheme(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "ATTACH_DOWNLOAD_SCHEME")));
			confInfo.setAttachAccessInterval(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "ATTACH_ACCESS_INTERVAL")));
			confInfo.setAttachPrefetchThreshold(Double.parseDouble(CMConfigurator.getConfiguration(strConfFilePath, "ATTACH_PREFETCH_THRESHOLD")));
			confInfo.setSessionNumber(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "SESSION_NUM")));
			
			// store session configuration file names
			if(confInfo.getSessionNumber() > 0)
			{
				confInfo.getSessionConfFileList().clear();
				
				for(int i = 1; i <= confInfo.getSessionNumber(); i++)
				{
					String strFileField = "SESSION_FILE"+i;
					String strFileName = CMConfigurator.getConfiguration(strConfFilePath, strFileField);
					confInfo.getSessionConfFileList().addElement(strFileName);
				}
			}
			
			// DB configuration
			confInfo.setDBUse(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "DB_USE")));
			confInfo.setDBHost(CMConfigurator.getConfiguration(strConfFilePath, "DB_HOST"));
			confInfo.setDBUser(CMConfigurator.getConfiguration(strConfFilePath, "DB_USER"));
			confInfo.setDBPass(CMConfigurator.getConfiguration(strConfFilePath, "DB_PASS"));
			confInfo.setDBPort(Integer.parseInt(CMConfigurator.getConfiguration(strConfFilePath, "DB_PORT")));
			confInfo.setDBName(CMConfigurator.getConfiguration(strConfFilePath, "DB_NAME"));
		}
		else if( confInfo.getSystemType().equals("CLIENT") )
		{
			//confInfo.setMyAddress(InetAddress.getLocalHost().getHostAddress());
		}


		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMConfigurator.init(), Ok");
			System.out.println("SYS_TYPE: "+confInfo.getSystemType());
			System.out.println("SERVER_ADDR: "+confInfo.getServerAddress());
			System.out.println("SERVER_PORT: "+confInfo.getServerPort());
			System.out.println("UDP_PORT: "+confInfo.getUDPPort());
			System.out.println("MULTICAST_ADDR: "+confInfo.getMulticastAddress());
			System.out.println("MULTICAST_PORT: "+confInfo.getMulticastPort());
			System.out.println("MY_ADDR: "+confInfo.getMyAddress());
			System.out.println("FILE_PATH: "+confInfo.getTransferedFileHome());
			System.out.println("FILE_APPEND_SCHEME: "+confInfo.isFileAppendScheme());
			System.out.println("PERMIT_FILE_TRANSFER: "+confInfo.isPermitFileTransferRequest());
			System.out.println("KEEP_ALIVE_TIME: "+confInfo.getKeepAliveTime());
			System.out.println("SIM_TRANS_DELAY: "+confInfo.getSimTransDelay());
			System.out.println("LOG_LEVEL: "+confInfo.getLogLevel());
			if( confInfo.getSystemType().equals("SERVER") )
			{
				//System.out.println("MY_ADDR: "+confInfo.getMyAddress());
				System.out.println("MY_PORT: "+confInfo.getMyPort());
				System.out.println("COMM_ARCH: "+confInfo.getCommArch());
				System.out.println("FILE_TRANSFER_SCHEME: "+confInfo.isFileTransferScheme());
				System.out.println("LOGIN_SCHEME: "+confInfo.isLoginScheme());
				System.out.println("MAX_LOGIN_FAILURE: "+confInfo.getMaxLoginFailure());
				System.out.println("SESSION_SCHEME: "+confInfo.isSessionScheme());
				System.out.println("DOWNLOAD_SCHEME: "+confInfo.isDownloadScheme());
				System.out.println("DOWNLOAD_NUM: "+confInfo.getDownloadNum());
				System.out.println("THUMBNAIL_HOR_SIZE: "+confInfo.getThumbnailHorSize());
				System.out.println("THUMBNAIL_VER_SIZE: "+confInfo.getThumbnailVerSize());
				System.out.println("ATTACH_DOWNLOAD_SCHEME: "+confInfo.getAttachDownloadScheme());
				System.out.println("ATTACH_ACCESS_INTERVAL: "+confInfo.getAttachAccessInterval());
				System.out.println("ATTACH_PREFETCH_THRESHOLD: "+confInfo.getAttachPrefetchThreshold());
				System.out.println("SESSION_NUM: "+confInfo.getSessionNumber());
				
				// session configuration file list
				System.out.print("session conf files: ");
				if(!confInfo.getSessionConfFileList().isEmpty())
				{
					Iterator<String> iter = confInfo.getSessionConfFileList().iterator();
					while(iter.hasNext())
					{
						System.out.print(iter.next()+" ");
					}
				}
				System.out.println();

				// DB configuration
				System.out.println("DB_USE: "+confInfo.isDBUse());
				System.out.println("DB_HOST: "+confInfo.getDBHost());
				System.out.println("DB_USER: "+confInfo.getDBUser());
				System.out.println("DB_PASS: "+confInfo.getDBPass());
				System.out.println("DB_PORT: "+confInfo.getDBPort());
				System.out.println("DB_NAME: "+confInfo.getDBName());

				if( CMConfigurator.isDServer(cmInfo) )
					System.out.println("This server is a default server!!");
				else
					System.out.println("This server is NOT a default server!!");
			}
			else if(confInfo.getSystemType().equals("CLIENT"))
			{
				//System.out.println("MY_ADDR: "+confInfo.getMyAddress());
			}
		}
		
		return true;
	}
	
	// get a configuration field value of the given configuration file
	public static String getConfiguration(String strConfFilePath, String fieldName)
	{
		FileInputStream fis;
		try {
			fis = new FileInputStream(strConfFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		String strLine = null;
		String strValue = null;
		String[] strToken = null;
		String delim = "\\s+";
		Scanner scan = new Scanner(fis);

		while(scan.hasNextLine() && strValue == null)
		{
			strLine = scan.nextLine();
			if(strLine.equals("") || strLine.charAt(0) == '#' || strLine.charAt(0) == '!')
				continue;
			
			strToken = strLine.split(delim);
			if(strToken[0].equals(fieldName))
				strValue = strToken[1];
		}

		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scan.close();
		
		return strValue;
	}
	
	// get all configuration field values of the given configuration file
	public static String[] getConfigurations(String strConfFilePath)
	{ 
		FileInputStream fis;
		int nFieldNum = 0;
		Scanner scan;
		String strLine;
		String[] strFieldValuePairs;
		int nCount = 0;
		
		try {
			fis = new FileInputStream(strConfFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		scan = new Scanner(fis);
		
		// figure out the number of configuration fields
		while(scan.hasNextLine())
		{
			strLine = scan.nextLine();
			if(strLine.equals("") || strLine.charAt(0) == '#' || strLine.charAt(0) == '!')
				continue;

			nFieldNum++;
		}		
		
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scan.close();
		
		if(nFieldNum == 0)
		{
			System.err.println("CMConfigurator.getConfigurations(): there is no configuraion!");
			return null;
		}
		
		// get all the pair of a field and a value
		strFieldValuePairs = new String[nFieldNum];
		
		try {
			fis = new FileInputStream(strConfFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		scan = new Scanner(fis);
		
		nCount = 0;
		while(scan.hasNextLine())
		{
			strLine = scan.nextLine();
			if(strLine.equals("") || strLine.charAt(0) == '#' || strLine.charAt(0) == '!')
				continue;

			strLine = strLine.trim();
			strFieldValuePairs[nCount++] = strLine;
		}
		
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scan.close();
		
		return strFieldValuePairs;
	}
	
	// change a configuration field value in the given configuration file.
	public static boolean changeConfiguration(String strConfFilePath, String strField, String strValue)
	{
		boolean bRet = false;
		BufferedReader fileBufReader = null;
		StringBuffer strFileContentBuffer = null;
		String strLine = null;
		String[] strReadFieldValue = null;
		boolean bModified = false;
		
		try {
			fileBufReader = new BufferedReader(new FileReader(strConfFilePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// read each line of the file and check out if the line includes the requested configuration field.
		// If the line includes the requested field, the value is replaced with the given parameter.
		// Each line is added to the string buffer. 
		strFileContentBuffer = new StringBuffer();
		try {
			while((strLine = fileBufReader.readLine()) != null)
			{
				strLine = strLine.trim();
				// if the read line is the (field,value) pair,
				if(!strLine.equals("") && !strLine.startsWith("#") && !strLine.startsWith("!"))
				{
					strReadFieldValue = strLine.split("\\s+");
					if(strReadFieldValue[0].equals(strField) && !strReadFieldValue[1].equals(strValue))
					{
						strLine = strLine.replace(strReadFieldValue[1], strValue);
						bModified = true;
					}
				}
				strFileContentBuffer.append(strLine+"\n");
			}
			
			fileBufReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// If the requested field value is successfully updated, 
		// the file string (including the updated field value) is written to the file.
		if(bModified)
		{
			Path filePath = Paths.get(strConfFilePath);
			byte[] fileBytes = strFileContentBuffer.toString().getBytes();
			try {
				Files.write(filePath, fileBytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			bRet = true;
			
			if(CMInfo._CM_DEBUG_2)
			{
				System.out.println("CMConfigurator.changeConfiguration(), file modified "
						+ "for (field: "+strField+", value: "+strValue+").");				
			}
		}

		return bRet;
	}

	// check whether the server info in the CMConfigurationInfo object is the default server or not.
	public synchronized static boolean isDServer(CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		boolean ret = false;
		String strServerAddress = confInfo.getServerAddress();
		int nServerPort = confInfo.getServerPort();
		String strMyAddress = confInfo.getMyAddress();
		int nMyPort = confInfo.getMyPort();
		
		// if server info is initialized and two server info is the same

		if( strServerAddress.compareTo("") != 0 && nServerPort != -1	
				&& strServerAddress.equals(strMyAddress) && nServerPort == nMyPort )
			ret = true;
		
		return ret;
	}
	
}
