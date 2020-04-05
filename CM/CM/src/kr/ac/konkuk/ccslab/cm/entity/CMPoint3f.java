package kr.ac.konkuk.ccslab.cm.entity;

/**
 * This class represents position information in 3D coordinate.
 * @author mlim
 *
 */
public class CMPoint3f {
	public float m_x, m_y, m_z;
	
	/**
	 * creates a default point object.
	 * (x,y,z) is set to (0.0f, 0.0f, 0.0f)
	 */
	public CMPoint3f()
	{
		m_x = 0.0f;
		m_y = 0.0f;
		m_z = 0.0f;
	}
	
	/**
	 * creates an point object.
	 * 
	 * @param x - the value of x axis
	 * @param y - the value of y axis
	 * @param z - the value of z axis
	 */
	public CMPoint3f(float x, float y, float z)
	{
		m_x = x;
		m_y = y;
		m_z = z;
	}
	
	/**
	 * sets the position information of the point object.
	 * @param x - the value of x axis
	 * @param y - the value of y axis
	 * @param z - the value of z axis
	 */
	public void setPoint(float x, float y, float z)
	{
		m_x = x;
		m_y = y;
		m_z = z;
	}
	
	/**
	 * calculates the Euclidean distance between this point and another point (p1).
	 * 
	 * @param p1 - the point object from which this object calculates distance.
	 * @return - the distance between this object and p1.
	 */
	public float distance(CMPoint3f p1)
	{
		double sq;
		float rt;
		sq = Math.pow((double)m_x-(double)(p1.m_x), 2) + Math.pow((double)m_y-(double)(p1.m_y), 2) + Math.pow((double)m_z-(double)(p1.m_z), 2);
		rt = (float)(Math.sqrt(sq));
		
		return rt;
	}

	/**
	 * calculates the Euclidean squared distance between this point and another point (p1).
	 * 
	 * @param p1 - the point object from which this object calculates distance.
	 * @return - the squared distance between this object and p1.
	 */
	public float distanceSquared(CMPoint3f p1)
	{
		double sq;
		sq = Math.pow((double)m_x-(double)(p1.m_x), 2) + Math.pow((double)m_y-(double)(p1.m_y), 2) + Math.pow((double)m_z-(double)(p1.m_z), 2);
		
		return (float)sq;
	}
}
