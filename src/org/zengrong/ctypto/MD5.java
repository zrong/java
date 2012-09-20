package org.zengrong.ctypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5
{
	/**
	 * 计算字符串的MD5值
	 * @param $value
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String hash(String $value) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		byte[] __b = $value.getBytes("UTF-8");
		MessageDigest __md = MessageDigest.getInstance("MD5");
		byte[] __hash = __md.digest(__b);
		StringBuilder __sb = new StringBuilder();
		for (byte b : __hash) 
		{
			__sb.append(String.format("%02X", b));
		}
		return __sb.toString();
	}
}
