package org.zengrong.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * 位图相关工具类
 * @author zrong
 * 创建日期：2012-10-09
 */
public class BitmapUtil
{

	/**
	 * 将Alpha通道从ARGB颜色中分离出来
	 * @param $argb
	 * @return
	 */
	public static int getAlpha(int $argb)
	{
		return $argb >> 24 & 0xFF;
	}
	
	/**
	 * 将Alpha通道值转换成RGB颜色
	 * @param $alpha
	 * @return
	 */
	public static int getRGB(int $alpha)
	{
		return $alpha <<  16 | $alpha << 8 | $alpha;
	}
	
	public static int[] getMixRGBs(int[] $alphas)
	{
		int[] __rgbs = new int[$alphas.length];
		for(int i=0;i<$alphas.length;i++)
		{
			__rgbs[i] = getRGB(getAlpha($alphas[i]));
		}
		return __rgbs;
	}
	
	/**
	 * 将32位色彩转换成24位色彩（丢弃Alpha通道）
	 * @param $argb
	 * @return
	 */
	public static int[] getRGBs(int[] $argb)
	{
		int[] __rgbs = new int[$argb.length];
		for(int i=0;i<$argb.length;i++)
		{
			__rgbs[i] = $argb[i] & 0xFFFFFF;
		}
		return __rgbs;
	}
	
	/**
	 * 使用去掉alpha值的方式去掉图像的alpha属性
	 * @param $image
	 * @return
	 */
	public static BufferedImage get24BitImage(BufferedImage $image)
	{
		int __w = $image.getWidth();
		int __h = $image.getHeight();
		int[] __imgARGB = getRGBs($image.getRGB(0, 0, __w, __h, null, 0, __w));
		BufferedImage __newImg = new BufferedImage(__w, __h, BufferedImage.TYPE_INT_RGB);
		__newImg.setRGB(0, 0, __w, __h, __imgARGB, 0, __w);
		return __newImg;
	}
	
	/**
	 * 使用绘制的方式去掉图像的alpha值
	 * @param $image
	 * @param $bgColor
	 * @return
	 */
	public static BufferedImage get24BitImage(BufferedImage $image, Color $bgColor)
	{
		int $w = $image.getWidth();
		int $h = $image.getHeight();
		BufferedImage __image = new BufferedImage($w, $h, BufferedImage.TYPE_INT_RGB);
		Graphics2D __graphic = __image.createGraphics();
		__graphic.setColor($bgColor);
		__graphic.fillRect(0,0,$w,$h);
		__graphic.drawRenderedImage($image, null);
		__graphic.dispose();
		return __image; 
	}
	
	/**
	 * 生成带有蒙版的图像
	 * @param $image
	 * @param $isHorizonal true使用横向蒙版，false使用纵向蒙版
	 */
	public static BufferedImage getMaskedImage(BufferedImage $image, boolean $isHorizonal)
	{
		int __w = $image.getWidth();
		int __h = $image.getHeight();
		int[] __sourceARGB = $image.getRGB(0, 0, __w, __h, null, 0, __w);
		
		int[] __imgARGB = BitmapUtil.getRGBs(__sourceARGB);
		int[] __maskARGB = BitmapUtil.getMixRGBs(__sourceARGB);
		if($isHorizonal)
		{
			BufferedImage __mix = new BufferedImage(__w*2, __h, BufferedImage.TYPE_INT_RGB);
			__mix.setRGB(0, 0, __w, __h, __imgARGB, 0, __w);
			__mix.setRGB(__w, 0, __w, __h, __maskARGB, 0, __w);
			return __mix;
		}
		BufferedImage __mix = new BufferedImage(__w, __h*2, BufferedImage.TYPE_INT_RGB);
		__mix.setRGB(0, 0, __w, __h, __imgARGB, 0, __w);
		__mix.setRGB(0, __h, __w, __h, __maskARGB, 0, __w);
		return __mix;
	}
}
