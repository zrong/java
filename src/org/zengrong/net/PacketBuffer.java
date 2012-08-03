package org.zengrong.net;

import java.io.IOException;
import java.util.Random;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketBuffer
{
	
	public static final String TAG = "org.zengrong.net.PacketBuffer";
	/**
	 * 4个校验码
	 */
	public static int MASK1 = 0x59;
	public static int MASK2 = 0x7a;
	public static int MASK3 = 0x7a;
	public static int MASK4 = 0x59;
	
	/**
	 * buffer的容量
	 */
	public static int CAPACITY = 50000;
	
	/**
	 * 随机数的基准值
	 */	
	public static int RANDOM_BASE = 10000;
	
	/**
	 * 前面的校验码长度
	 */
	public static int SUF_MASK_LEN = 2;

	/**
	 * 后面的校验码长度
	 */
	public static int PRE_MASK_LEN = 2;

	/**
	 * 消息体长度使用32位整数保存，占用4字节
	 */
	public static int BODY_LEN = 4;
	
	/**
	 * 命令代码的长度使用16位整数保存，占用2字节
	 */	
	public static int METHOD_CODE_LEN = 2;
	
	/**
	 * 字节顺序
	 */
	public static ByteOrder endian = ByteOrder.BIG_ENDIAN;
	
	public static int getRandom()
	{
		return new Random().nextInt(RANDOM_BASE);
	}
	
	/**
	 * 获取一个基本的ByteBuffer
	 * @param $length
	 * @return
	 */
	public static ByteBuffer getBaseBA(int $length)
	{
		ByteBuffer __bf = ByteBuffer.allocate($length);
		__bf.order(endian);
		return __bf;
	}
	
	/**
	 * 返回最终要发送的数据，在要发送的数据中加入首位验证码和时间戳
	 * @param $methodCode 发送的方法代码
	 * @param $bytes 要发送的数据body
	 * @throws IOException 
	 */	
	public static byte[] getSendBA(int $methodCode, byte[] $bytes) throws IOException
	{
		//写入信息主体的长度
		int __bodyLen = $bytes.length;
		//所有信息的长度
		int __size = PRE_MASK_LEN + SUF_MASK_LEN + METHOD_CODE_LEN + 4 + __bodyLen;
		ByteBuffer __buffer = getBaseBA(__size);
		//写入前置校验码
		__buffer.put((byte) (getRandom() & MASK1));
		__buffer.put((byte) (getRandom() & MASK2));
		//写入信息的整体长度，整体长度为非主体长度+主体长度
		__buffer.putInt(METHOD_CODE_LEN + __bodyLen);
		//写入方法代码
		__buffer.putShort((short) $methodCode);
		//写入信息主体
		__buffer.put($bytes);
		//写入后置校验码
		__buffer.put((byte) (getRandom() & MASK3));
		__buffer.put((byte) (getRandom() & MASK4));
		__buffer.position(0);
		byte[] __byte = new byte[__size];
		__buffer.get(__byte);
		return __byte;
	}
	
	public PacketBuffer()
	{
		_buf = getBaseBA(CAPACITY);
	}
	
	/**
	 * 获取数据包
	 * @$ba socket服务器发来的未经拆包的数据
	 * @return 返回一个经过拆包的数组。<br />
	 * 这个数组的length一般是1，如果两个包一起发过来，则值可能是2。因此需要用循环来处理这个结果。
	 * @throws IOException 
	 */
	public VO[] getPackets(byte[] $ba) throws IOException
	{  			
		//最终取得的信息列表
		VO[] __msgs = new VO[10];
		//信息的数量
		int __size = 0;
		//当前处理到的指针位置
		int __pos = 0;  

		//保存前置校验码占用的字节长度＋信息主题占用的长度
		int __preMaskAndBody_len = PRE_MASK_LEN + BODY_LEN;
		
		_buf.put($ba);
		//准备读取缓冲区
		_buf.flip();
		int __flag1;
		int __flag2;
		System.out.println("开始解析，整个缓冲区大小:"+Integer.toString(_buf.position()));
		//读取整个缓冲区
		while (_buf.remaining() >= __preMaskAndBody_len)
		{
			//读取第一个校验码
			__flag1 = _buf.get();
			//判断第一个校验码是否匹配，不匹配就继续读取
			if ((__flag1 & MASK1) != __flag1) continue;
			//读取第二个校验码
			__flag2 = _buf.get();
			System.out.println("__flag2:"+Integer.toString(__flag2));
			//判断第二个校验码是否匹配，不匹配就继续读
			if ((__flag2 & MASK2) != __flag2) continue;
			//trace('前置校验码读取正确,pos:', __pos);
			//读取信息主体的长度
			int __bodyLen = _buf.getInt();
			//暂存信息主体开头所在位置的指针
			__pos = _buf.position();
			System.out.println("信息主体长度:"+Integer.toString(__bodyLen)+",pos:"+Integer.toString(__pos)+"_buf.remaining:"+Integer.toString(_buf.remaining())+"_buf.limit:"+Integer.toString(_buf.limit()));
			int __readLimit = __pos + __bodyLen + SUF_MASK_LEN;
			//如果没有将数据包的所有数据接受完全（即当前可用数据总长度小于当前位置＋消息主体长度＋尾部校验码长度）则等待下一次处理
			if (_buf.limit() < __readLimit) break;
			_buf.get(__pos+__bodyLen);
			_buf.get(__pos+__bodyLen+1);
			//数据包长度足够，就读取尾部校验码
			__flag1 = _buf.get(__pos+__bodyLen);
			__flag2 = _buf.get(__pos+__bodyLen+1);
			//trace('_buf.pos:', _buf.position);
			//如果后置校验码正确则提取消息体加入队列
			if ((__flag1 & MASK3) == __flag1 && (__flag2 & MASK4) == __flag2)
			{
				//trace('校验码正确');
				//长度在允许的范围内就析取数据包
				if (__bodyLen <= Integer.MAX_VALUE)
				{
					//建立一个VO，将数据包的method和body放在其中
					VO __msg = new VO();
					//读取信息主体中的方法名称，使用16位整数保存
					__msg.method = _buf.getShort();
					//读取信息主体中的数据，读取的长度从主体信息长度中减去方法名占用的长度
					byte[] __msgBody = new byte[__bodyLen - METHOD_CODE_LEN];
					_buf.get(__msgBody, 0, __msgBody.length);  
					ByteBuffer __bodyBuffer = getBaseBA(__msgBody.length);
					__bodyBuffer.order(endian);
					__bodyBuffer.put(__msgBody);
					__bodyBuffer.clear();
					__msg.body = __bodyBuffer;
					//放入数组
					__msgs[__size] = __msg;
					__size ++;
					//将指针位置移动到后置校验码的后方
					_buf.position(_buf.position()+SUF_MASK_LEN);
				}
			}
		}
		__pos = _buf.position();
		//如果缓冲区数据全部处理完则清空缓冲区
		if (_buf.remaining() <= 0) clear();
		else
		{
			//如果缓冲区没有处理完
			//将剩余的数据读入一个临时数组
			byte[] __tmp = new byte[_buf.remaining()];
			_buf.get(__tmp);
			//清空缓冲区
			clear();
			//将临时数组中保存的数据写入新的空白缓冲区
			_buf.put(__tmp);
		}
		VO[] __useMsgs = new VO[__size];
		System.arraycopy(__msgs, 0, __useMsgs, 0, __size);
		return __useMsgs;
	} 
	
	/**
	 * 临时保存服务器发来的消息，如果这次消息被拆包了，就保存拆包的前一部分，一直到包完整
	 */
	private ByteBuffer _buf;
	
	/**
	 * 清空缓冲区，让缓冲区可以用于新的处理
	 */
	public void clear()
	{
		_buf.clear();
	}
	
	/**
	 * 包含PackerBuffer解析出的包内容
	 * @author zrong
	 * 创建日期：2012-08-01
	 */
	public static class VO
	{
		public int method;
		public ByteBuffer body;
	}
}