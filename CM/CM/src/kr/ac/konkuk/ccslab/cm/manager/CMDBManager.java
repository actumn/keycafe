package kr.ac.konkuk.ccslab.cm.manager;

import java.io.File;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMDBInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachAccessHistory;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSAttachAccessHistoryList;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContent;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;

public class CMDBManager {
	
	// initialize DB url
	public static void init(CMInfo cmInfo)
	{
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		CMDBInfo dbInfo = cmInfo.getDBInfo();
		
		// this will load the MySQL driver, each DB has its own driver
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// setup the connection with the DB
		String url = "jdbc:mysql://"+confInfo.getDBHost()+":3306/"+confInfo.getDBName();
		url += "?useUnicode=true&characterEncoding=euckr";	// for using Korean character
		dbInfo.setDBURL(url);
				
		return;
	}
	
	// connect DB
	public static boolean connectDB(CMInfo cmInfo)
	{
		Connection connect = null;
		Statement st = null;
		CMDBInfo dbInfo = cmInfo.getDBInfo();
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		String url = dbInfo.getDBURL();
		String user = confInfo.getDBUser();
		String pass = confInfo.getDBPass();
		
		try {
			connect = DriverManager.getConnection(url, user, pass);
			// statements allow to issue SQL queries to the database
			st = connect.createStatement();
			dbInfo.setStatement(st);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.connectDB(), MySQL connection succeeded. url("+url
					+"), user("+confInfo.getDBUser()+").");	

		return true;
	}
	
	// close DB
	public static void closeDB(CMInfo cmInfo)
	{
		CMDBInfo dbInfo = cmInfo.getDBInfo();
		Connection connect = dbInfo.getConnection();
		Statement st = dbInfo.getStatement();
		
		try {
			if(connect != null)
			{
				connect.close();
			}
			if(st != null)
			{
				st.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	// close the ResultSet object
	public static void closeRS(ResultSet rs)
	{
		if(rs != null)
		{
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	// send SELECT query
	// The caller of this method should close the ResultSet object after it completes to use!
	public static ResultSet sendSelectQuery(String strQuery, CMInfo cmInfo)
	{
		CMDBInfo dbInfo = cmInfo.getDBInfo();
		ResultSet rs = null;
		
		if(!connectDB(cmInfo))
			return null;
		
		try {
			// resultSet gets the result of the SQL query
			rs = dbInfo.getStatement().executeQuery(strQuery);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		// ResultSet should be closed at the caller!!
		
		return rs;
	}
	
	// send data manipulation query (INSERT, UPDATE, DELETE, etc..)
	public static int sendUpdateQuery(String strQuery, CMInfo cmInfo)
	{
		CMDBInfo dbInfo = cmInfo.getDBInfo();
		int ret = -1;
		
		if(!connectDB(cmInfo))
			return -1;
		
		try {
			ret = dbInfo.getStatement().executeUpdate(strQuery);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		closeDB(cmInfo);
		
		return ret;
	}
	
	//////////////////////////////////////////////////////// user_table management
	//	row: seqNum, userName, password, creationTime

	public static int queryInsertUser(String name, String password, CMInfo cmInfo)
	{
		String strQuery = "insert into user_table (userName, password, creationTime) values ('"
							+name+"', PASSWORD('"+password+"'), NOW());";
		int ret = sendUpdateQuery(strQuery, cmInfo);
		
		if(ret == -1)
		{
			System.out.println("CMDBManager.queryInsertUser(), error!");
			return ret;
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryInsertUser(), return value("+ret+").");

		return ret;
	}
	
	// authenticate user with a user name and password
	public static boolean authenticateUser(String strUserName, String strPassword, CMInfo cmInfo)
	{
		boolean bValidUser = false;
		String strQuery = "select * from user_table where userName='"+strUserName
				+"' and password=PASSWORD('"+strPassword+"');";
		ResultSet rs = sendSelectQuery(strQuery, cmInfo);
		try {
			if(rs != null && rs.next())
				bValidUser = true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CMDBManager.closeDB(cmInfo);
			CMDBManager.closeRS(rs);
		}
		
		return bValidUser;
	}
	
	// not yet called
	// Send a query to retrieve 'num' user rows from 'index' and store the result
	// If 'index'== 0 and 'num' == -1, all the rows are retrieved
	public static ResultSet queryGetUsers(int index, int num, CMInfo cmInfo)
	{
		String strQuery = null;
		ResultSet rs = null;
		if(index == 0 && num == -1)
			strQuery = "select * from user_table;";
		else
			strQuery = "select * from user_table limit "+index+", "+num+";";

		rs = sendSelectQuery(strQuery, cmInfo);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryGetUsers(), end.");

		return rs;
	}

	// not yet called
	// Currently only field which can be updated is password.
	// Password must be an input with PASSWORD() macro.
	// All the updated fields are String type because they are given as a query string
	public static int queryUpdateUser(String name, String fieldName, String value, CMInfo cmInfo)
	{
		String strQuery = null;
		if( fieldName.equals("password") )
		{
			strQuery = "update user_table set "+fieldName+"=PASSWORD('"+value+"') where userName='"
						+name+"';";
		}
		else
		{
			strQuery = "update user_table set "+fieldName+"='"+value+"' where userName='"+name+"';";
		}
		int ret = sendUpdateQuery(strQuery, cmInfo);
		
		if(ret == -1)
		{
			System.out.println("CMDBManager.queryUpdateUser(), error!");
			return ret;
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryUpdateUser(), return value("+ret+").");

		return ret;
	}

	public static int queryDeleteUser(String name, CMInfo cmInfo)
	{
		ResultSet rs = null;
		String strQuery = "select seqNum from user_table where userName='"+name+"';";
		rs = sendSelectQuery(strQuery, cmInfo);
		int nSeqNum = -1;
		try {
			if(rs != null && rs.next())
			{
				nSeqNum = rs.getInt("seqNum");
				if(CMInfo._CM_DEBUG)
					System.out.println("User requested to be deleted: "+name+", seqNum: "+nSeqNum);
			}
			else
			{
				System.out.println("CMDBManager.queryDeleteUser(), user("+name+") not found.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CMDBManager.closeDB(cmInfo);
			CMDBManager.closeRS(rs);
		}
		
		strQuery = "delete from user_table where seqNum="+nSeqNum+";";
		int ret = sendUpdateQuery(strQuery, cmInfo);
		
		if(ret == -1)
		{
			System.out.println("CMDBManager.queryDeleteUser(), delete error!");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryDeleteUser(), return value("+ret+").");

		return ret;
	}

	// not yet called
	public static void queryTruncateUserTable(CMInfo cmInfo)
	{
		sendUpdateQuery("truncate table user_table;", cmInfo);
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryTruncateUserTable(), end.");
		return;
	}

	//////////////////////////////////////////////////////////sns_content_table management
	//row: seqNum, creationTime, userName, textMessage, numAttachedFiles, replyOf, levelOfDisclosure

	public static int queryInsertSNSContent(String userName, String text, int nNumAttachedFiles,
											int nReplyOf, int nLevelOfDisclosure, CMInfo cmInfo)
	{
		int ret = -1;
		String strQuery = "insert into sns_content_table (creationTime, userName, textMessage, "
				+ "numAttachedFiles, replyOf, levelOfDisclosure) values (NOW(), '"+userName+"', '"
				+text+"', "+nNumAttachedFiles+", "+nReplyOf+", "+nLevelOfDisclosure+");";
		ret = sendUpdateQuery(strQuery, cmInfo);
		if(ret == -1)
		{
			System.out.println("CMDBManager.queryInsertSNSContent(), error for user("+userName+").");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryInsertSNSContent(), user("+userName
								+"), return value("+ret+").");
		}
		
		return ret;
	}

	// not yet called
	// send a query to retrieve 'num' contents from 'index' in decsending order of seqNum, 
	// and store the result.
	// Regardless of a writer and the level of disclosure of content
	public static ResultSet queryGetSNSContent(int index, int num, CMInfo cmInfo)
	{
		String strQuery = null;
		ResultSet rs = null;
		if(index < 0)
		{
			System.err.println("CMDBManager.queryGetSNSContent(), invalid offset("+index+")!");
			return null;
		}
		else
		{
			strQuery = "select * from sns_content_table order by seqNum desc limit "+index+", "
						+num+";";
		}
		
		rs = sendSelectQuery(strQuery, cmInfo);

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CCMDBManager.queryGetSNSContents(), end (index: "+index
								+", num: "+num+").");
		}
		return rs;
	}
	
	// strReqeuster: a name of a client which requests content
	// strWriter: a name of a content writer
	// index: offset (starting with 0) in the list of found content set (it must be equal to or greater than 0)
	// num: the number of content to be retrieved
	public static CMSNSContentList queryGetSNSContent(String strRequester, String strWriter, int index, int num, CMInfo cmInfo)
	{
		String strQuery = null;
		ResultSet rs = null;
		CMSNSContentList contentList = null;
		int nContID = -1;
		String strDate = null;
		String strMsg = null;
		int nNumAttachedFiles = 0;
		int nReplyOf = -1;
		int nLevelOfDisclosure = -1;
		
		
		if(index < 0)
		{
			System.err.println("CMDBManager.queryGetSNSContent(), invalid offset("+index+")!");
			return null;
		}
		
		// No specific writer designated. Retrieve all content that is open to the requester
		if(strWriter == null || strWriter.equals(""))
		{
			strQuery = "select * from sns_content_table where (levelOfDisclosure=0) OR "
					+ "(levelOfDisclosure=1 AND exists (select 1 from friend_table where userName='"
					+ strRequester + "' AND friendName=sns_content_table.userName)) OR "
					+ "(levelOfDisclosure=2 AND exists (select 1 from friend_table where userName='"
					+ strRequester + "' AND friendName=sns_content_table.userName) AND "
					+ "exists (select 1 from friend_table where userName=sns_content_table.userName "
					+ "AND friendName='"+ strRequester + "')) OR "
					+ "(userName='" + strRequester + "') "
					+ "order by seqNum desc limit "+ index + ", " + num + ";";
		}
		else if(strWriter.equals("CM_MY_FRIEND")) // retrieve all content of friends of the requester
		{
			strQuery = "select * from sns_content_table where (levelOfDisclosure<=1 AND "
					+ "exists (select 1 from friend_table where userName='" + strRequester + "' AND "
					+ "friendName=sns_content_table.userName)) OR "
					+ "(levelOfDisclosure<=2 AND exists (select 1 from friend_table where userName='"
					+ strRequester + "' AND friendName=sns_content_table.userName) AND "
					+ "exists (select 1 from friend_table where userName=sns_content_table.userName AND "
					+ "friendName='" + strRequester + "'))"
					+ "order by seqNum desc limit "+ index + ", "+ num + ";";
		}
		else if(strWriter.equals("CM_BI_FRIEND")) // retrieve all content of bi-friends of the requester
		{
			strQuery = "select * from sns_content_table where levelOfDisclosure<=2 AND "
					+ "exists (select 1 from friend_table where userName='" + strRequester + "' AND "
					+ "friendName=sns_content_table.userName) AND "
					+ "exists (select 1 from friend_table where friendName='" + strRequester + "' AND "
					+ "userName=sns_content_table.userName) "
					+ "order by seqNum desc limit "+ index + ", " + num + ";";
		}
		else if(strWriter.equals(strRequester)) // retrieve all content of the requester
		{
			strQuery = "select * from sns_content_table where userName='"+strRequester+"' "
					+ "order by seqNum desc limit "+ index + ", "+num + ";";
		}
		else	// retrieve all the writer's content that is open to the requester
		{
			strQuery = "select * from sns_content_table where "
					+ "(levelOfDisclosure=0 AND userName='"+strWriter+"') OR "
					+ "(levelOfDisclosure=1 AND userName='"+strWriter+"' AND "
					+ "exists (select 1 from friend_table where userName='"
					+ strRequester + "' AND friendName='"+strWriter+"')) OR "
					+ "(levelOfDisclosure=2 AND userName='"+strWriter+"' AND "
					+ "exists (select 1 from friend_table where userName='"
					+ strRequester + "' AND friendName='"+strWriter+"') AND "
					+ "exists (select 1 from friend_table where userName='"+strWriter+"' "
					+ "AND friendName='"+ strRequester + "')) "
					+ "order by seqNum desc limit "+ index + ", " + num + ";";
		}

		rs = sendSelectQuery(strQuery, cmInfo);
		if(rs != null)
			contentList = new CMSNSContentList();
		
		try {
			while( rs != null && rs.next() )
			{
				nContID = rs.getInt("seqNum");
				strDate = rs.getString("creationTime");
				strWriter = rs.getString("userName");
				strMsg = rs.getString("textMessage");
				nNumAttachedFiles = rs.getInt("numAttachedFiles");
				nReplyOf = rs.getInt("replyOf");
				nLevelOfDisclosure = rs.getInt("levelOfDisclosure");
				contentList.addSNSContent(nContID, strDate, strWriter, strMsg, nNumAttachedFiles, 
						nReplyOf, nLevelOfDisclosure, null);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CMDBManager.closeDB(cmInfo);
			CMDBManager.closeRS(rs);
		}
		

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CCMDBManager.queryGetSNSContents(), end requester("+strRequester+"), writer("
					+strWriter+"), (index: "+index+", num: "+num+").");
		}

		return contentList;
	}

	// not yet called
	// Fields that are updatable are textMessage and attachedFilePath.
	// All the updated fields are char* type because they are given as a query string
	public static int queryUpdateSNSContent(int seqNum, String fieldName, String value, CMInfo cmInfo)
	{
		int ret = -1;
		String strQuery = "update sns_content_table set "+fieldName+"='"+value+"' where seqNum="
							+seqNum+";";
		ret = sendUpdateQuery(strQuery, cmInfo);
		if( ret == -1 )
		{
			System.out.println("CMDBManager.queryUpdateSNSContent(), error for seqNum("+seqNum+").");
			return ret;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryUpdateSNSContent(), end, seqNum("+seqNum+").");
		}
		return ret;
	}

	//not yet called
	public static int queryDeleteSNSContent(int seqNum, CMInfo cmInfo)
	{
		int ret = -1;
		String strQuery = "delete from sns_content_table where seqNum="+seqNum+";";
		ret = sendUpdateQuery(strQuery, cmInfo);
		if(ret == -1)
		{
			System.out.println("CMDBManager.queryDeleteSNSContent(), error for seqNum("+seqNum+").");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryDeleteSNSContent(), end, seqNum("+seqNum+").");
		}
		
		return ret;
	}

	// not yet called
	// It must be called only after SELECT query
	public static void showResultSNSContent(ResultSet rs)
	{
		System.out.println("------ sns_content_table");
		try {
			while( rs.next() )
			{
				int id = rs.getInt("seqNum");
				String ctime = rs.getString("creationTime");
				String uname = rs.getString("userName");
				String msg = rs.getString("textMessage");
				int nNumAttachedFiles = rs.getInt("numAttachedFiles");
				int nReplyOf = rs.getInt("replyOf");
				int nLevelOfDisclosure = rs.getInt("levelOfDisclosure");

				System.out.format("%-3d Date: %s, User: %s%n", id, ctime, uname);
				System.out.format("%-3s Message: %s%n", " ", msg);
				System.out.format("%-3s # Attached Files: %d, ID of replied content: %d, Level of Disclosure: %d%n", 
						nNumAttachedFiles, nReplyOf, nLevelOfDisclosure);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("---------------");
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.showResultSNSContent(), end.");
		}
		return;
	}

	// not yet called
	public static void queryTruncateSNSContentTable(CMInfo cmInfo)
	{
		sendUpdateQuery("truncate table sns_content_table;", cmInfo);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryTruncateSNSContentTable(), end.");
		}
		return;
	}
	
	/////////////////////////////////////////////////////////////// attached_file_table management
	// row: seqNum, contentID, filePath, fileName
	
	public static int queryInsertSNSAttachedFile(int nContentID, String strFilePath, String strFileName, CMInfo cmInfo)
	{
		int ret = -1;
		String strQuery = "insert into attached_file_table (contentID, filePath, fileName) "
				+ "values ("+nContentID+", '"+strFilePath+"', '"+strFileName+"');";
		ret = sendUpdateQuery(strQuery, cmInfo);
		if(ret == -1)
		{
			System.out.println("CMDBManager.queryInsertSNSAttachedFile(), error for content ("+nContentID+").");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryInsertSNSContent(), content("+nContentID
					+"), fpath("+strFilePath+"), fname("+strFileName+"), return value("+ret+").");
		}

		return ret;
	}

	// get the list of attached files of a specific content
	public static ArrayList<String> queryGetSNSAttachedFile(int nContentID, CMInfo cmInfo)
	{
		ArrayList<String> attachList = null;
		String strQuery = null;
		ResultSet rs = null;
		strQuery = "select * from attached_file_table where contentID="+nContentID+";";

		rs = sendSelectQuery(strQuery, cmInfo);
		if(rs != null)
			attachList = new ArrayList<String>();
		
		try {
			while(rs != null && rs.next()) {
				String strPathFile = rs.getString("filePath") + File.separator + rs.getString("fileName");
				attachList.add(strPathFile);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			CMDBManager.closeDB(cmInfo);
			CMDBManager.closeRS(rs);
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryGetSNSAttachedFile(), end.");

		return attachList;
	}
	
	// not yet called
	// Columns that can be updated are contentID, filePath and fileName.
	// All the updated fields are String type because they are given as a query string
	public static int queryUpdateSNSAttachedFile(int seqNum, String fieldName, String value, CMInfo cmInfo)
	{
		int ret = -1;
		String strQuery = "update attached_file_table set "+fieldName+"='"+value+"' where seqNum="+seqNum+";";
		ret = sendUpdateQuery(strQuery, cmInfo);
		if( ret == -1 )
		{
			System.out.println("CMDBManager.queryUpdateSNSAttachedFile(), error for seqNum("+seqNum+").");
			return ret;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryUpdateSNSAttachedFile(), end, seqNum("+seqNum+"), "
					+ "field("+fieldName+"), value("+value+").");
		}
		return ret;
	}

	// not yet called
	public static int queryDeleteSNSAttachedFile(int seqNum, CMInfo cmInfo)
	{
		int ret = -1;
		String strQuery = "delete from attached_file_table where seqNum="+seqNum+";";
		ret = sendUpdateQuery(strQuery, cmInfo);
		if(ret == -1)
		{
			System.out.println("CMDBManager.queryDeleteSNSAttachedFile(), error for seqNum("+seqNum+").");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryDeleteSNSAttachedFile(), end, seqNum("+seqNum+").");
		}
		
		return ret;
	}

	// not yet called
	// It must be called only after SELECT query
	public static void showResultSNSAttachedFile(ResultSet rs)
	{
		System.out.println("------ attached_file_table");
		try {
			while( rs.next() )
			{
				int id = rs.getInt("seqNum");
				int nContentID = rs.getInt("contentID");
				String strFilePath = rs.getString("filePath");
				String strFileName = rs.getString("fileName");

				System.out.println("seqNum: "+id+", contentID: "+nContentID+", path:"+strFilePath+", name:"+strFileName);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("---------------");
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.showResultSNSAttachedFile(), end.");
		}
		return;
	}

	// not yet called
	public static void queryTruncateSNSAttachedFileTable(CMInfo cmInfo)
	{
		sendUpdateQuery("truncate table attached_file_table;", cmInfo);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryTruncateSNSAttachedFileTable(), end.");
		}
		return;
	}

	////////////////////////////////////////////////////////////////////////////
	
	// not yet called
	public static long sendRowNumQuery(String table, CMInfo cmInfo)
	{
		long num = -1;
		String strQuery = "select count(*) from "+table;
		ResultSet rs = sendSelectQuery(strQuery, cmInfo);

		try {
			if(rs.next())
			{
				num = rs.getLong(1);	// column index starts with 1
			}
			else
			{
				System.out.println("CMDBManager.sendRowNumQuery(), error!");
				return -1;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CMDBManager.closeDB(cmInfo);
			CMDBManager.closeRS(rs);
		}

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.sendRowNumQuery(), row num: "+num+", table: "+table);
		}

		return num;
	}
	
	//////////////////////////////////////////////////////////friend_table management
	//row: userName, friendName

	public static int queryInsertFriend(String strUserName, String strFriendName, CMInfo cmInfo)
	{
		String strQuery = "insert into friend_table (userName, friendName) values ('"
				+strUserName+"', '"+strFriendName+"');";
		int ret = sendUpdateQuery(strQuery, cmInfo);

		if(ret == -1)
		{
			System.out.println("CMDBManager.queryInsertFriend(), error!");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryInsertFriend(), return value("+ret+").");

		return ret;
	}
	
	public static int queryDeleteFriend(String strUserName, String strFriendName, CMInfo cmInfo)
	{
		int ret = -1;
		String strQuery = "delete from friend_table where userName='"+strUserName+"' and "
				+"friendName='"+strFriendName+"';";
		ret = sendUpdateQuery(strQuery, cmInfo);

		if(ret == -1)
		{
			System.out.println("CMDBManager.queryDeleteFriend(), error!");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryDeleteFriend(), return value("+ret+").");
		
		return ret;
	}
	
	// get the list of users whom 'strUserName' added as friends
	public static ArrayList<String> queryGetFriendsList(String strUserName, CMInfo cmInfo)
	{
		ResultSet rs = null;
		ArrayList<String> myFriendList = null;
		String strQuery = "select * from friend_table where userName='"+strUserName+"';";

		rs = sendSelectQuery(strQuery, cmInfo);
		if(rs != null)
			myFriendList = new ArrayList<String>();
		
		try {
			while(rs != null && rs.next())
			{
				myFriendList.add(rs.getString("friendName"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CMDBManager.closeDB(cmInfo);
			CMDBManager.closeRS(rs);
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryGetFriendsList(), end for user("+strUserName+").");
	
		return myFriendList;
	}
	// get the list of users who added 'strUserName' as a friend
	public static ArrayList<String> queryGetRequestersList(String strUserName, CMInfo cmInfo)
	{
		ResultSet rs = null;
		ArrayList<String> candidateList = null;
		String strQuery = "select * from friend_table where friendName='"+strUserName+"';";
		
		rs = sendSelectQuery(strQuery, cmInfo);
		if(rs != null)
			candidateList = new ArrayList<String>();
		
		try {
			while(rs != null && rs.next()){
				candidateList.add(rs.getString("userName"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			CMDBManager.closeDB(cmInfo);
			CMDBManager.closeRS(rs);
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryGetRequestersList(), end for user("+strUserName+").");
		
		return candidateList;		
	}
	
	//////////////////////////////////////////////////////// sns_attach_access_history_table management
	//	row: userName, date, writerName, accessCount
	
	public static int queryInsertAccessHistory(String strUserName, Calendar date, String strWriterName, 
			int nAccessCount, CMInfo cmInfo)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(date.getTime());
		
		String strQuery = "insert into sns_attach_access_history_table (userName, date, writerName, accessCount) "
				+ "values ('"+strUserName+"', '"+strDate+"', '"+strWriterName+"', "+nAccessCount+");";
		int ret = sendUpdateQuery(strQuery, cmInfo);

		if(ret == -1)
		{
			System.err.println("CMDBManager.queryInsertAccessHistory(), error!");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryInsertAccessHistory(), return value("+ret+").");

		return ret;

	}
	
	// not yet called
	public static int queryDeleteAccessHistory(String strUserName, Calendar date, String strWriterName, 
			CMInfo cmInfo)
	{
		int ret = -1;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(date.getTime());
		
		String strQuery = "delete from sns_attach_access_history_table where userName='"+strUserName+"' and "
				+"date='"+strDate+"' and writerName='"+strWriterName+"';";
		ret = sendUpdateQuery(strQuery, cmInfo);

		if(ret == -1)
		{
			System.err.println("CMDBManager.queryDeleteAccessHistory(), error!");
			return ret;
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryDeleteAccessHistory(), return value("+ret+").");
		
		return ret;
	}
	
	// To update an accessCount which is an only updatable field.
	public static int queryUpdateAccessCount(String strUserName, Calendar date, 
			String strWriterName, int nAccessCount, CMInfo cmInfo)
	{
		int ret = -1;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(date.getTime());

		String strQuery = "update sns_attach_access_history_table set accessCount="+nAccessCount+" where userName='"
				+strUserName+"' and date='"+strDate+"' and writerName='"+strWriterName+"';";
		ret = sendUpdateQuery(strQuery, cmInfo);
		if( ret == -1 )
		{
			System.err.println("CMDBManager.queryUpdateAccessCount(), error for userName("+strUserName
					+"), date("+strDate+"), writerName("+strWriterName+").");
			return ret;
		}
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryUpdateAccessCount(), end, userName("+strUserName
					+"), date("+strDate+"), writerName("+strWriterName+").");
		}
		return ret;
	}

	// get the list of access history of a user in the range of two dates
	public static CMSNSAttachAccessHistoryList queryGetAccessHistory(String strUserName, Calendar startDate, Calendar endDate, 
			String strWriterName, CMInfo cmInfo)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strStartDate = dateFormat.format(startDate.getTime());
		String strEndDate = dateFormat.format(endDate.getTime());
		
		String strQuery = null;
		ResultSet rs = null;
		
		// different queries according to null parameters
		if(strWriterName == null)
		{
			strQuery = "select * from sns_attach_access_history_table where userName='"+strUserName
					+"' and date >= '"+strStartDate+"' and date <= '"+strEndDate+"';";			
		}
		else // every parameters are not null
		{
			strQuery = "select * from sns_attach_access_history_table where userName='"+strUserName
					+"' and date >= '"+strStartDate+"' and date <= '"+strEndDate+"' and writerName='"
					+strWriterName+"';";
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryGetAccessHistory(), start.");

		rs = sendSelectQuery(strQuery, cmInfo);
		
		CMSNSAttachAccessHistoryList historyList = null;
		if(rs != null)
			historyList = new CMSNSAttachAccessHistoryList();
		
		try {
			while(rs != null && rs.next())
			{
				CMSNSAttachAccessHistory tempHistory = null;
				String strDate = rs.getString("date");
				Calendar date = Calendar.getInstance();
				try {
					date.setTime(dateFormat.parse(strDate));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String strHistoryWriterName = rs.getString("writerName");
				int nAccessCount = rs.getInt("accessCount");
				
				tempHistory = new CMSNSAttachAccessHistory(strUserName, date, strHistoryWriterName, nAccessCount); 
				historyList.addAccessHistory(tempHistory);
				
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("userName: "+strUserName+", date: "+date+", writerName:"
							+strHistoryWriterName+", accessCount:"+nAccessCount);					
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CMDBManager.closeDB(cmInfo);
			CMDBManager.closeRS(rs);
		}

		if(CMInfo._CM_DEBUG)
			System.out.println("CMDBManager.queryGetAccessHistory(), end.");

		return historyList;
	}

	public static void queryTruncateAccessHistoryTable(CMInfo cmInfo)
	{
		sendUpdateQuery("truncate table sns_attach_access_history_table;", cmInfo);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMDBManager.queryTruncateAccessHistoryTable(), end.");
		}
		return;
	}

}
