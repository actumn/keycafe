package kr.ac.konkuk.ccslab.cm.entity;

import java.util.Iterator;
import java.util.Vector;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * The CMList&lt;T&gt; class represents a list of CM objects.
 * 
 * @author CCSLab, Konkuk University
 *
 * @param <T> - T is the class of a list element.
 */
public class CMList<T> {
	private Vector<T> m_list;
	
	/**
	 * Creates an instance of the CMList class.
	 */
	public CMList()
	{
		m_list = new Vector<T>();
	}
	
	/**
	 * Adds an element to the list.
	 * 
	 * @param element - an element of T type
	 * @return true if the element is successfully added to the list; false otherwise.
	 */
	public synchronized boolean addElement(T element)
	{
		if(m_list.contains(element))
		{
			System.err.println("CMList.addElement(): already exists !: "+element);
			return false;
		}
		
		m_list.addElement(element);
		
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMList.addElement() done: "+element);
			System.out.println("# current element: "+m_list.size());
		}
		
		return true;
	}
	
	/**
	 * Removes an element from the list.
	 * 
	 * @param element - an element of T type
	 * @return true if the element is successfully removed from the list; false otherwise.
	 */
	public synchronized boolean removeElement(T element)
	{
		boolean bResult = false;
		
		bResult = m_list.removeElement(element);
		if(!bResult)
		{
			System.err.println("CMList.removeElement() failed! : "+element);
		}

		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMList.removeElement() done: "+element);
			System.out.println("# current element: "+m_list.size());
		}

		return bResult;
	}
	
	/**
	 * Removes all elements from the list.
	 */
	public synchronized void removeAllElements()
	{
		m_list.removeAllElements();
	}
	
	/**
	 * Finds an element in the list.
	 * 
	 * @param element - an element of type T
	 * @return true if the element is found; false otherwise.
	 */
	public synchronized T findElement(T element)
	{
		T tempElement = null;
		boolean bFound = false;
		
		Iterator<T> iterList = m_list.iterator();
		while(iterList.hasNext() && !bFound)
		{
			tempElement = iterList.next();
			if(element.equals(tempElement))
				bFound = true;
		}
		
		if(bFound)
			return tempElement;
		return null;
	}
	
	/**
	 * Returns the size of list.
	 * @return the size of list.
	 */
	public synchronized int getSize()
	{
		return m_list.size();
	}
	
	/**
	 * Tests if this list has no element.
	 * 
	 * @return true if this list has no element; false otherwise.
	 */
	public synchronized boolean isEmpty()
	{
		return m_list.isEmpty();
	}
	
	/**
	 * Returns the Vector of this list.
	 * @return the Vector of this list.
	 */
	public synchronized Vector<T> getList()
	{
		return m_list;
	}
	
	@Override
	public String toString()
	{
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("[");
		int nCount = 0;
		for(T element : m_list)
		{
			strBuffer.append(element.toString());
			nCount++;
			if(nCount < m_list.size())
				strBuffer.append(", ");
		}
		strBuffer.append("]");
		return strBuffer.toString();
	}
	
}
