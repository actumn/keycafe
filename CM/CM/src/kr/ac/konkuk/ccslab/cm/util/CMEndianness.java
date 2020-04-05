package kr.ac.konkuk.ccslab.cm.util;

import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMEndianness {
	// test byte order of the current system
	public static int testByteOrder()
	{
		int ret;
		short word = 0x0001;
		if( (word & 0x00ff) == 1 )
			ret = CMInfo.CM_BIG_ENDIAN;
		else
			ret = CMInfo.CM_LITTLE_ENDIAN;
		return ret;
	}
	
	// test whether endian of the current system is big endian
	public static boolean isBigEndian()
	{
		boolean ret;
		short word = 0x0001;
		if( (word & 0x00ff) == 1 )
			ret = true;
		else
			ret = false;
		return ret;
	}
	
	// swap byte order of a variable of any type.
	// The byte order of the parameter b itself is changed.
	public static byte[] swapByte(byte[] b)
	{
		byte[] swappedByte = new byte[b.length];

		for(int i=0; i < b.length; i++)
		{
			swappedByte[i] = b[b.length-i-1];
		}
		
		return swappedByte;
	}
		
	// If host is little endian, host(little) endian -> network(big) endian
	public static byte[] hton(byte[] b)
	{
		byte[] netByte;
		
		if(!isBigEndian())
		{
			netByte = swapByte(b);
		}
		else
			netByte = b;
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.print("hton: ");
			for(int j=0; j<b.length;j++) System.out.print(b[j]+" ");
			System.out.print("->");
			for(int i=0; i<netByte.length;i++) System.out.print(netByte[i]+" ");
			System.out.println();
		}
		return netByte;	
	}
	
	// If host is little endian, network(big) endian -> host(little) endian
	public static byte[] ntoh(byte[] b)
	{
		byte[] hostByte;
		
		if(!isBigEndian())
		{
			hostByte = swapByte(b);
		}
		else
			hostByte = b;
		
		if(CMInfo._CM_DEBUG)
		{
			System.out.print("ntoh: ");
			for(int j=0; j<b.length;j++) System.out.print(b[j]+" ");
			System.out.print("->");
			for(int i=0; i<hostByte.length;i++) System.out.print(hostByte[i]+" ");
			System.out.println();
		}
		return hostByte;
	}

}
