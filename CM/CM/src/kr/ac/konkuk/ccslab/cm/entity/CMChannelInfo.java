package kr.ac.konkuk.ccslab.cm.entity;

import java.util.*;
import java.util.Map.Entry;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

import java.io.IOException;
import java.nio.channels.*;

// Information of map of pair (channel number, SelectableChannel)
// Used by CMUser (user channels), CMCommInfo (datagram channels), CMGroup (multicast channels),
// and CMServer (server channels)
public class CMChannelInfo<K> {
	private Hashtable<K, SelectableChannel> m_chHashtable;
	
	public CMChannelInfo()
	{
		m_chHashtable = new Hashtable<K, SelectableChannel>();
	}
	
	// management of the HashMap of channels

	public synchronized boolean addChannel(K key, SelectableChannel ch)
	{
		
		if(ch == null)
		{
			System.err.println("CMChannelInfo.addChannel(), channel is null.");
			return false;
		}
		
		// check if key already exists or not
		if(m_chHashtable.containsKey(key))
		{
			System.err.println("CMChannelInfo.addChannel(), channel key("
					+key+") already exists.");
			return false;
		}
		
		// check if the same ch already exists or not
		if(m_chHashtable.containsValue(ch))
		{
			System.err.println("CMChannelInfo.addChanel(), channel already exists.");
			return false;
		}
		
		m_chHashtable.put(key, ch);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMChannelInfo.addChannel(), succeeded: "+ch+"\n"
					+"ch hash code("+ch.hashCode()+"), key("+key+"), current # channels ("
					+m_chHashtable.size()+").");
		}

		return true;
	}

	public synchronized boolean removeChannel(K key)
	{
		SelectableChannel ch = null;
		
		if(!m_chHashtable.containsKey(key))
		{
			System.err.println("CMChannelInfo.removeChannel(), channel key("
					+key.toString()+") does not exists.");
			return false;
		}
		
		ch = m_chHashtable.get(key);
		if(ch != null && ch.isOpen())
		{
			try {
				ch.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		m_chHashtable.remove(key);
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMChannelInfo.removeChannel(), succeeded: "+ch+"\n"
					+"ch hash code("+ch.hashCode()+"), key("+key+"), current # channels("
					+m_chHashtable.size()+").");
		}

		return true;
	}

	public synchronized void removeAllChannels()
	{
		SelectableChannel ch = null;
		Iterator<Entry<K, SelectableChannel>> iter = m_chHashtable.entrySet().iterator();
		
		while( iter.hasNext() )
		{
			Map.Entry<K, SelectableChannel> e = (Map.Entry<K, SelectableChannel>) iter.next();
			ch = e.getValue();
			if(ch != null && ch.isOpen())
			{
				try {
					ch.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		m_chHashtable.clear();
	}

	// removes all channels except the default channel that is specified by the default key (defaultKey)
	public synchronized boolean removeAllAddedChannels(K defaultKey)
	{
		SelectableChannel ch = null;
		
		Iterator<Entry<K, SelectableChannel>> iter = m_chHashtable.entrySet().iterator();
		if(m_chHashtable.isEmpty())
		{
			System.out.println("CMChannelInfo.removeAllAddedChannels(), channel map is empty.");
			return false;
		}
		
		while( iter.hasNext() )
		{
			Map.Entry<K, SelectableChannel> e = (Map.Entry<K, SelectableChannel>) iter.next();
			if( !e.getKey().equals(defaultKey) ) // if key is not the default key
			{
				ch = e.getValue();
				if(ch != null && ch.isOpen())
				{
					try {
						ch.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				iter.remove();
			}
		}
		
		return true;
	}

	public synchronized SelectableChannel findChannel(K key)
	{
		return m_chHashtable.get(key);
	}
	
	public synchronized K findChannelKey(SelectableChannel ch)
	{
		boolean bFound = false;
		K ret = null;
		SelectableChannel tch = null;
		Iterator<Entry<K, SelectableChannel>> iter = m_chHashtable.entrySet().iterator();
		
		while(iter.hasNext() && !bFound)
		{
			Map.Entry<K, SelectableChannel> e = (Map.Entry<K, SelectableChannel>) iter.next();
			tch = e.getValue();
			//System.out.println("ch code: "+ch.hashCode()+", tch code: "+tch.hashCode());
			if( tch.equals(ch) )
			{
				ret = e.getKey();
				bFound = true;
			}
		}
		
		return ret;
	}
	
	public synchronized int getSize()
	{
		return m_chHashtable.size();
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = null;
		if(getSize() == 0) return null;
		
		sb = new StringBuffer();
		Set<Entry<K, SelectableChannel>> hashMapSet = m_chHashtable.entrySet();
		Iterator<Entry<K, SelectableChannel>> iter = hashMapSet.iterator();
		while(iter.hasNext())
		{
			Map.Entry<K, SelectableChannel> entry = (Map.Entry<K, SelectableChannel>) iter.next();
			sb.append(entry.toString()+"\n");
		}
		
		return sb.toString();
	}

}
