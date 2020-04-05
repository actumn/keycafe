package kr.ac.konkuk.ccslab.cm.entity;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

/**
 * Class CMObject is the root of the class hierarchy. 
 * 
 * @author mlim
 *
 */
public class CMObject {

	protected int m_nType;

	/**
	 * Creates a CM object.
	 * <br> The default object type is {@link CMInfo#CM_OBJECT}.
	 */
	public CMObject()
	{
		m_nType = CMInfo.CM_OBJECT;
	}

	/**
	 * Sets the type of this CM object
	 * @param t - the object type.
	 * <br> The object types are defined in the {@link CMInfo} class.
	 */
	public void setType(int t)
	{
		m_nType = t;
	}
	
	/**
	 * Returns the type of this CM object
	 * @return a type of this CM object.
	 * <br> CM object types are defined in the {@link CMInfo} class.
	 */
	// get type of the class
	public int getType()
	{
		return m_nType;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null) return false;
		if(!this.getClass().equals(obj.getClass())) return false;
		if(this == obj) return true;
		
		CMObject cmo = (CMObject) obj;
		if(m_nType == cmo.getType())
			return true;
		return false;
	}
	
	@Override
	public String toString()
	{
		String strType = "{ \"type\": "+m_nType+" }";
		return strType;
	}
}
