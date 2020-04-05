package kr.ac.konkuk.ccslab.cm.sns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.math3.distribution.*;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;

public class CMSNSUserAccessSimulator {

	private int m_nUserNum;	// the number of users participating in the simulation
	private int m_nAvgDayAccCount;	// average daily access count
	private int m_nTotalSimDays;	// total number of days of the simulation
	private int m_nAccPattern;	// access pattern (0: random access, 1: skewed access)
	private double m_dNormalMean;	// mean value for normal distribution (for skewed access pattern)
	private double m_dNormalSD;		// standard deviation for normal distribution (for skewed access pattern)
	private CMSNSAttachAccessHistoryList m_accHistoryList;
	
	public CMSNSUserAccessSimulator()
	{
		m_nUserNum = 10;
		m_nAvgDayAccCount = 10;
		m_nTotalSimDays = 100;
		m_nAccPattern = 0;
		m_dNormalMean = 5.0;
		m_dNormalSD = 1.0;
		m_accHistoryList = new CMSNSAttachAccessHistoryList();
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// set/get methods
	public void setUserNum(int nNum)
	{
		m_nUserNum = nNum;
	}
	
	public int getUserNum()
	{
		return m_nUserNum;
	}
	
	public void setAvgDayAccCount(int nCount)
	{
		m_nAvgDayAccCount = nCount;
	}
	
	public int getAvgDayAccCount()
	{
		return m_nAvgDayAccCount;
	}
	
	public void setTotalSimDays(int nDays)
	{
		m_nTotalSimDays = nDays;
	}
	
	public int getTotalSimDays()
	{
		return m_nTotalSimDays;
	}
	
	public void setAccPattern(int nPattern)
	{
		m_nAccPattern = nPattern;
	}
	
	public int getAccPattern()
	{
		return m_nAccPattern;
	}
	
	public void setNormalMean(double mean)
	{
		m_dNormalMean = mean;
	}
	
	public double getNormalMean()
	{
		return m_dNormalMean;
	}
	
	public void setNormalSD(double sd)
	{
		m_dNormalSD = sd;
	}
	
	public double getNormalSD()
	{
		return m_dNormalSD;
	}
	
	public CMSNSAttachAccessHistoryList getAccHisotryList()
	{
		return m_accHistoryList;
	}
	
	///////////////////////////////////////////////////////////////////
	// simulation control
	
	// simulate that users access other users, and collect the access history data
	public void start(int nUserNum, int nAvgDayAccCount, int nTotalSimDays, int nAccPattern, 
			double dNormalMean, double dNormalSD)
	{
		m_nUserNum = nUserNum;
		m_nAvgDayAccCount = nAvgDayAccCount;
		m_nTotalSimDays = nTotalSimDays;
		m_nAccPattern = nAccPattern;
		m_dNormalMean = dNormalMean;
		m_dNormalSD = dNormalSD;
		start();
		return;
	}
	
	public void start()
	{
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("------ Simulation variables:");
			System.out.println("Number of users: "+m_nUserNum);
			System.out.println("Average daily access count: "+m_nAvgDayAccCount);
			System.out.println("Total simulation days: "+m_nTotalSimDays);
			System.out.println("Access pattern: "+m_nAccPattern);
			System.out.println("Mean, sd for normal distribution: "+m_dNormalMean+", "+m_dNormalSD);
			System.out.println("------");
		}
		
		// clear the access history list
		m_accHistoryList.removeAllAccessHistory();
		
		// calculate the starting date of the simulation (from today)
		Calendar startDay = Calendar.getInstance();
		Calendar curDay = (Calendar)startDay.clone();
		curDay.clear();
		curDay.set(startDay.get(Calendar.YEAR), startDay.get(Calendar.MONTH), startDay.get(Calendar.DAY_OF_MONTH));
		startDay.clear();
		startDay.setTime(curDay.getTime());
		
		startDay.add(Calendar.DAY_OF_MONTH, -m_nTotalSimDays+1);
		curDay.add(Calendar.DAY_OF_MONTH, -m_nTotalSimDays);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println("Simulation starts on "+dateFormat.format(startDay.getTime())+".");

		// initialize random variables
		PoissonDistribution poissonDist = new PoissonDistribution((double)m_nAvgDayAccCount);
		UniformIntegerDistribution uniformDist = new UniformIntegerDistribution(0, m_nUserNum-1);
		NormalDistribution normalDist = new NormalDistribution(m_dNormalMean, m_dNormalSD);
		
		// set user names (a user name is 'user-n')
		String[] arrUserName = new String[m_nUserNum];
		for(int i = 0; i < m_nUserNum; i++)
		{
			arrUserName[i] = "user-"+i;
		}

		// for simulation days
		int nAccUser = -1;
		int arrAccCount[][] = new int[m_nUserNum][m_nUserNum];
		for(int nCurDay = 0; nCurDay < m_nTotalSimDays; nCurDay++)
		{
			// set current date
			curDay.add(Calendar.DAY_OF_MONTH, 1);
			
			// clear access count array per day
			for(int i = 0; i < m_nUserNum; i++)
			{
				for(int j = 0; j < m_nUserNum; j++)
					arrAccCount[i][j] = 0;
			}
			
			// for each user
			for(int nCurUser = 0; nCurUser < m_nUserNum; nCurUser++)
			{
				// determine the number of accesses in the current day
				int nAccNum = 0;
				nAccNum = poissonDist.sample();
				System.out.println("date("+dateFormat.format(curDay.getTime())+"), user("+nCurUser 
						+"), accCount("+nAccNum+")");
				System.out.print("accessed user: ");
				
				// determine an accessed user
				for(int i = 0; i < nAccNum; i++)
				{
					if(m_nAccPattern == 0) // random access
					{
						// get the ID of an accessed user randomly
						nAccUser = uniformDist.sample();
					}
					else if(m_nAccPattern == 1) // skewed access
					{
						// get the ID of an accessed user in a skewed access pattern
						nAccUser = (int)normalDist.sample();
						// set user ids in the range of 0 and #users-1
						if(nAccUser > m_nUserNum-1) // exceeds the maximum id
							nAccUser = m_nUserNum-1;
						else if(nAccUser < 0) // lower than the minimum id
							nAccUser = 0;
					}
					else
					{
						System.err.println("Wrong access pattern!: "+m_nAccPattern);
						return;
					}
					System.out.print(nAccUser+" ");
					
					// add access count of all users in a simulation day
					arrAccCount[nCurUser][nAccUser]++;
				}
				System.out.println();
			} // for users
			
			// add access count to the access history list
			for(int nUser = 0; nUser < m_nUserNum; nUser++)
			{
				for(int nWriter = 0; nWriter < m_nUserNum; nWriter++)
				{
					if(arrAccCount[nUser][nWriter] > 0)
					{
						System.out.println("date("+dateFormat.format(curDay.getTime())+"), user("
								+arrUserName[nUser]+"), writer("+arrUserName[nWriter]+"), accCount("
								+arrAccCount[nUser][nWriter]+")");
						
						Calendar tmpDay = (Calendar)curDay.clone(); // create a new Calendar instance
						CMSNSAttachAccessHistory accHistory = new CMSNSAttachAccessHistory(arrUserName[nUser],
								tmpDay, arrUserName[nWriter], arrAccCount[nUser][nWriter]);
						m_accHistoryList.addAccessHistory(accHistory);
					}
				}
			}
						
		} // for simulation days
		
		System.out.println("access history list has ("+m_accHistoryList.getAllAccessHistory().size()+") elements.");
				
		return;
	} // start()

	// get prefetched writers and prefetch accuracy for each user during simulation days
	public double[] calPrecisionRecall(double dPrefThreshold, int nPrefInterval)
	{
		
		System.out.println("========== Prefetched writers");
		Calendar today = Calendar.getInstance();
		Calendar startDay = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strUser = null;
		ArrayList<String> prefList = null;
		double dPrefPrecision = 0.0;
		double dPrefRecall = 0.0;
		int nTotalCount = 0;
		double dSumPrecision = 0.0;
		double dSumRecall = 0.0;
		double dAvgPrecision = 0.0;
		double dAvgRecall = 0.0;
		double[] dAvgPrecRecall = new double[2];
		
		dAvgPrecRecall[0] = 0.0;	// precision return value
		dAvgPrecRecall[1] = 0.0;	// recall return value
		
		
		for(int i = 0; i < m_nUserNum; i++)
		{
			strUser = "user-"+i;
			// initialize the starting day
			startDay = (Calendar)today.clone();
			startDay.add(Calendar.DAY_OF_MONTH, -m_nTotalSimDays+nPrefInterval+1);

			for(int nDays = nPrefInterval+1; nDays <= m_nTotalSimDays; nDays++)
			{
				prefList = getPrefetchedUsers(startDay, strUser, dPrefThreshold, nPrefInterval);
				System.out.println("("+dateFormat.format(startDay.getTime())+"), "+strUser+", threshold: "
						+dPrefThreshold+", interval: "+nPrefInterval+" days");
				if(prefList != null)
				{
					System.out.print("Prefetched writers(num: "+prefList.size()+"): ");
					for(int j = 0; j < prefList.size(); j++)
						System.out.print(prefList.get(j)+" ");
					System.out.println();
					dPrefPrecision = calPrefPrecision(startDay, strUser, prefList);
					dPrefRecall = calPrefRecall(startDay, strUser, prefList);
				
					dSumPrecision += dPrefPrecision;
					dSumRecall += dPrefRecall;
				}
				else
				{
					System.out.println("No prefetched writers!");
					dPrefPrecision = 0.0;
					dPrefRecall = 0.0;
				}
				
				nTotalCount++;
				startDay.add(Calendar.DAY_OF_MONTH, 1); // add one day

				System.out.format("Precision: %.4f\n", dPrefPrecision);
				System.out.format("Recall: %.4f\n", dPrefRecall);
			
			}
		}
		
		if(nTotalCount > 0)
		{
			dAvgPrecision = dSumPrecision/nTotalCount;
			dAvgRecall = dSumRecall/nTotalCount;			
		}
		System.out.println("----------");
		System.out.format("Average precision: %.4f\n", dAvgPrecision);
		System.out.format("Average recall: %.4f\n", dAvgRecall);
		System.out.println("----------");
		
		dAvgPrecRecall[0] = dAvgPrecision;
		dAvgPrecRecall[1] = dAvgRecall;

		return dAvgPrecRecall;
	}
	
	// get the list of target users for prefetching
	// This method must be called after 'start()'
	public ArrayList<String> getPrefetchedUsers(Calendar date, String strUser, double dPrefThreshold, 
			int nAccInterval)
	{
		ArrayList<String> prefList = null;
		ArrayList<CMSNSAttachAccessHistory> accHistoryList = m_accHistoryList.getAllAccessHistory();
		int nTotalAccCount = 0;
		int userAccCount[] = new int[m_nUserNum]; // access count for each user
		int i;
		Calendar startDate = (Calendar)date.clone();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		startDate.clear();
		startDate.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		date.clear();
		date.setTime(startDate.getTime());
		startDate.add(Calendar.DAY_OF_MONTH, -nAccInterval);
		
		for(i = 0; i < m_nUserNum; i++)
			userAccCount[i] = 0;
		
		// calculate the access count for each interesting user and the total count
		for(i = 0; i < accHistoryList.size(); i++)
		{
			CMSNSAttachAccessHistory accHistory = accHistoryList.get(i);
			if(accHistory.getDate().compareTo(startDate) >= 0 && accHistory.getDate().compareTo(date) < 0
					&& accHistory.getUserName().equals(strUser))
			{
				nTotalAccCount += accHistory.getAccessCount();
				String strWriter = accHistory.getWriterName();
				// get a writer ID from a user name (user-nn)
				int nWriter = Integer.parseInt(strWriter.substring(strWriter.lastIndexOf("-")+1));
				userAccCount[nWriter] += accHistory.getAccessCount();
			}
		}
		
		if(nTotalAccCount == 0)	// no access history
		{
			System.err.println("No access history!");
			return null;
		}

		//// print out the result
		System.out.println("---- date("+dateFormat.format(date.getTime())+"), user("+strUser+"), "
				+"pref threshold("+dPrefThreshold+"), acc interval("+nAccInterval+")");
		System.out.println("Total access count: "+nTotalAccCount);
		System.out.print("Access count per user: ");
		for(i = 0; i < m_nUserNum; i++)
			System.out.print(userAccCount[i]+" ");
		System.out.println();
		System.out.print("Access rates per user: ");
		for(i = 0; i < m_nUserNum; i++)
			System.out.format("%.4f ", (double)userAccCount[i]/(double)nTotalAccCount);
		System.out.println();
		////

		// select writers whose content will be prefetched
		prefList = new ArrayList<String>();
		for(i = 0; i < m_nUserNum; i++)
		{
			double dAccRate = (double)userAccCount[i]/(double)nTotalAccCount;
			if(dAccRate >= dPrefThreshold) // satisfy the prefetch condition
			{
				// generate the corresponding user name 'user-nn'
				String strWriter = "user-"+i;
				prefList.add(strWriter);
			}
		}
		
		if(prefList.isEmpty()) prefList = null;

		return prefList;
	}
	
	// calculate the prefetch precision of a user
	// This method must be called after 'start()'
	public double calPrefPrecision(Calendar date, String strUser, ArrayList<String> prefList)
	{
		double dPrecision = 0.0;
		ArrayList<CMSNSAttachAccessHistory> accHistoryList = m_accHistoryList.getAllAccessHistory();
		int nPrefUsersNum = prefList.size();
		int nNumAccPrefUsers = 0;
		
		if(prefList == null || prefList.isEmpty())
		{
			System.out.println("No prefetched writers!");
			return 0.0;
		}
		
		for(int i = 0; i < accHistoryList.size(); i++)
		{
			CMSNSAttachAccessHistory tmpAccess = accHistoryList.get(i);
			Calendar accDate = tmpAccess.getDate();
			String strAccUser = tmpAccess.getUserName();
			String strWriter = tmpAccess.getWriterName();
			
			if(accDate.compareTo(date) == 0 && strAccUser.equals(strUser) && prefList.contains(strWriter))
			{
				nNumAccPrefUsers++;
			}
		}
		
		dPrecision = (double)nNumAccPrefUsers/(double)nPrefUsersNum;

		return dPrecision;
	}
	
	// calculate the prefetch recall of a user
	// This method must be called after 'start()'
	public double calPrefRecall(Calendar date, String strUser, ArrayList<String> prefList)
	{
		double dRecall = 0.0;
		ArrayList<CMSNSAttachAccessHistory> accHistoryList = m_accHistoryList.getAllAccessHistory();
		int nNumAccPrefUsers = 0;
		int nNumTotalAccUsers = 0;

		for(int i = 0; i < accHistoryList.size(); i++)
		{
			CMSNSAttachAccessHistory tmpAccess = accHistoryList.get(i);
			Calendar accDate = tmpAccess.getDate();
			String strAccUser = tmpAccess.getUserName();
			String strWriter = tmpAccess.getWriterName();
			
			if(accDate.compareTo(date) == 0 && strAccUser.equals(strUser))
			{
				nNumTotalAccUsers++;
				if(prefList.contains(strWriter))
					nNumAccPrefUsers++;
			}
		}
		
		if(nNumTotalAccUsers == 0)
		{
			System.out.println(strUser+" does not access any user!");
			return 0.0;
		}
		
		dRecall = (double)nNumAccPrefUsers/(double)nNumTotalAccUsers;
		
		return dRecall;
	}
	
	// write recent access history to CMDB
	public boolean writeRecentAccHistoryToDB(CMInfo cmInfo)
	{
		boolean bRet = true;
		int nPrefInterval = cmInfo.getConfigurationInfo().getAttachAccessInterval();
		Calendar startDate = null;
		int nIndex = -1;
		ArrayList<CMSNSAttachAccessHistory> historyList = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		boolean bFound = false;
		int nRet = 1;
		
		// find the starting date (only yyyy-MM-dd)
		Calendar today = Calendar.getInstance();
		startDate = (Calendar)today.clone();
		startDate.clear();
		startDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
		startDate.add(Calendar.DAY_OF_MONTH, -nPrefInterval);
		System.out.println("Starting date for writing to DB: "+dateFormat.format(startDate.getTime()));
		
		// find the starting index of the access history list
		historyList = m_accHistoryList.getAllAccessHistory();
		nIndex = 0;
		while(nIndex < historyList.size() && !bFound)
		{
			CMSNSAttachAccessHistory history = historyList.get(nIndex);
			if(history.getDate().compareTo(startDate) >= 0)
				bFound = true;
			else
				nIndex++;
		}
		
		if(!bFound)
		{
			System.err.println("Access history to be written to DB NOT FOUND!");
			return false;
		}

		System.out.println("Found access history index: "+nIndex+", date: "
				+dateFormat.format(historyList.get(nIndex).getDate().getTime()));
		
		// truncate the user access history table of CMDB
		CMDBManager.queryTruncateAccessHistoryTable(cmInfo);
		// write access histories to CMDB
		System.out.println("start to write access history to CMDB");
		for(int i = nIndex; i < historyList.size() && nRet == 1; i++)
		{
			CMSNSAttachAccessHistory history = historyList.get(i);
			nRet = CMDBManager.queryInsertAccessHistory(history.getUserName(), history.getDate(), 
					history.getWriterName(), history.getAccessCount(), cmInfo);
			
			System.out.println("date("+dateFormat.format(history.getDate().getTime())
					+"), user("+history.getUserName()+"), writer("+history.getWriterName()+"), access count("
					+history.getAccessCount()+")");
		}
		
		if(nRet != 1)
		{
			bRet = false;
		}
		
		return bRet;
	}
	
}
