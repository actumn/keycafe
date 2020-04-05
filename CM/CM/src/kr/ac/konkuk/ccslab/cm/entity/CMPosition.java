package kr.ac.konkuk.ccslab.cm.entity;

/**
 * This class represents location information.
 * <br> The location information consists of the position and orientation.
 * @author mlim
 *
 */
public class CMPosition {
	public CMPoint3f m_p;
	public CMQuat m_q;
	
	public CMPosition()
	{
		m_p = new CMPoint3f();
		m_q = new CMQuat();
	}
}
