package org.zengrong.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 文件相关工具类
 * @author zrong
 * 创建日期：2012-10-09
 */
public class FileUtil
{
	/**
	 * 返回文件的扩展名，如果没有扩展名返回null
	 * @param $file
	 * @return
	 */
	public static String getFileExt(File $file)
	{
		String[] __sp = $file.getName().split("\\.");
		if(__sp.length>1)
		{
			return "." + __sp[__sp.length-1];
		}
		return null;	
	}
	
	/**
	 * 把文件转换成字节数组
	 * @param $file 要转换的文件
	 * @param $length 要转换的数组的长度
	 * @throws IOException 
	 */
	public static byte[] fileToByteArray(File $file, int $length) throws IOException
	{
		BufferedInputStream __input = new BufferedInputStream(new FileInputStream($file));
		byte[] __bytes = new byte[$length];
		__input.read(__bytes);
		__input.close();
		return __bytes;
	}
	
	/**
	 * 把整个文件转换成字节数组
	 * @param $file
	 * @return
	 * @throws IOException
	 */
	public static byte[] fileToByteArray(File $file) throws IOException
	{
		BufferedInputStream __input = new BufferedInputStream(new FileInputStream($file));
		byte[] __bytes = new byte[__input.available()];
		__input.read(__bytes);
		__input.close();
		return __bytes;
	}
}
