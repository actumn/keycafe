package kr.ac.konkuk.ccslab.cm.util;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

public class CMUtil {

	public static byte[] intToBytes(int x) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(x);
		dos.close();
		byte[] int_bytes = baos.toByteArray();
		baos.close();
		
		//for(int i=0;i<int_bytes.length;i++)System.out.print(int_bytes[i]+" ");
		//System.out.println();
		
		return int_bytes;
	}
	
	public static String getSHA1Hash(String s)
	{
		try {
			// create hash
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			
			// create Hex string
			StringBuffer hexString = new StringBuffer();
			for(int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// need to consider I/O overhead as it takes time to read an image
	public static boolean isImageFile(String strPath)
	{
		boolean ret = false;
		File file = new File(strPath);
		if(!file.exists())
		{
			System.err.println("CMUtil.isImageFile(), "+strPath+" not found!");
			return false;
		}
		
		int index = strPath.lastIndexOf(".");
		String strExt = strPath.substring(index+1, strPath.length());
		if(strExt.equalsIgnoreCase("jpg") || strExt.equalsIgnoreCase("png") 
				|| strExt.equalsIgnoreCase("bmp") || strExt.equalsIgnoreCase("gif") 
				|| strExt.equalsIgnoreCase("wbmp"))
		{
			ret = true;
		}
		/*
		try {
			Image image = ImageIO.read(file);
			if(image != null)
			{
				ret = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			ret = false;
		}
		*/
		
		return ret;
	}
	
	public static boolean createScaledImage(String inPath, int nWidth, int nHeight, String outPath)
	{
		boolean ret = true;
		BufferedImage sourceImage;
		try {
			sourceImage = ImageIO.read(new File(inPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		double xScale = 1;
		double yScale = 1;
		if(nWidth == 0 && nHeight > 0)
		{
			yScale = (double) nHeight / sourceImage.getHeight();
			xScale = yScale;
			nWidth = (int) (sourceImage.getWidth() * xScale);
		}
		else if(nWidth > 0 && nHeight == 0)
		{
			xScale = (double) nWidth / sourceImage.getWidth();
			yScale = xScale;
			nHeight = (int) (sourceImage.getHeight() * yScale);
		}
		else
		{
			xScale = (double) nWidth / sourceImage.getWidth();
			yScale = (double) nHeight / sourceImage.getHeight();			
		}

		BufferedImage bi = getCompatibleImage(nWidth, nHeight);
		Graphics2D g2d = bi.createGraphics();

		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g2d.drawRenderedImage(sourceImage, at);
		g2d.dispose();
		
		// write to disk
		int index = outPath.lastIndexOf(".");
		String strFileExt = outPath.substring(index+1, outPath.length());
		try {
			ImageIO.write(bi,  strFileExt, new File(outPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return ret;
	}
	
	private static BufferedImage getCompatibleImage(int w, int h)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		return image;
	}
}
