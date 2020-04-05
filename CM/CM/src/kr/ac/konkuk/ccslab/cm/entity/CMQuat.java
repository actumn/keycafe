package kr.ac.konkuk.ccslab.cm.entity;

/**
 * This class represents orientation information of an object.
 * @author mlim
 *
 */
public class CMQuat {
	public	float m_w, m_x, m_y, m_z;

	/**
	 * creates a default orientation object.
	 * <br> The quarternion (x,y,z,w) is set to (0.0f, 0.0f, 0.0f, 0.0f)
	 */
	public CMQuat()
	{
		m_w = 0.0f;
		m_x = 0.0f;
		m_y = 0.0f;
		m_z = 0.0f;
	}

	/**
	 * creates an orientation object and set the quarternion.
	 * <br> The quarternion is set to (x,y,z,w).
	 * @param w - the rotation angle
	 * @param x - the x axis for rotation 
	 * @param y - the y axis for rotation
	 * @param z - the z axis for rotation
	 */
	public CMQuat(float w, float x, float y, float z)
	{
		m_w = w;
		m_x = x;
		m_y = y;
		m_z = z;
	}

	/**
	 * sets quarternion of the orientation object.
	 * 
	 * @param w - the rotation angle
	 * @param x - the x axis for rotation
	 * @param y - the y axis for rotation
	 * @param z - the z axis for rotation
	 */
	public void setQuat(float w, float x, float y, float z)
	{
		m_w = w;
		m_x = x;
		m_y = y;
		m_z = z;
	}
}
