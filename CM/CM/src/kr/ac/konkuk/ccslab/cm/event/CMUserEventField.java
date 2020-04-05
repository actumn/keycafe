package kr.ac.konkuk.ccslab.cm.event;

/**
 * The CMUserEventField class represents a field of the {@link CMUserEvent}.
 * 
 * @author CCSLab, Konkuk University
 *
 */
public class CMUserEventField {
	/**
	 * The field type.
	 * <p> The field type is one of followings:
	 * <br> CMInfo.CM_INT : int type
	 * <br> CMInfo.CM_LONG : long type
	 * <br> CMInfo.CM_FLOAT : float type
	 * <br> CMInfo.CM_DOUBLE : double type
	 * <br> CMInfo.CM_STR : String
	 */
	public int nDataType;
	/**
	 * The field name.
	 */
	public String strFieldName;
	/**
	 * The field value.
	 *  
	 */
	public String strFieldValue;
	/**
	 * The number of bytes.
	 * <p>This variable is used if this field has the byte array.
	 */
	public int nValueByteNum;
	/**
	 * The byte array.
	 * <p>This variable is used if this field represents a byte array.
	 */
	public byte[] valueBytes;
}
