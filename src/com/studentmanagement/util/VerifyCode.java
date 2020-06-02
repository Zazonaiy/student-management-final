package com.studentmanagement.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import com.jfinal.kit.PathKit;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;

public class VerifyCode {
	public static JSONObject drawRandomText(int width, int height, BufferedImage verifyImg) {
		Graphics2D graphics = (Graphics2D) verifyImg.getGraphics();
		graphics.setColor(Color.gray);
		graphics.fillRect(0, 0, width, height);
		graphics.setFont(new Font("微软雅黑", Font.BOLD, 40));
		
		//可选择数字和字母
		String baseNumLetter = "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
		StringBuffer strBuffer = new StringBuffer();
		int x = 10;	//旋转原点的x坐标
		String ch = "";
		Random random = new Random();
		for (int i = 0; i < 4; i++) {
			//随机底色
			graphics.setColor(getRandomColor()); 
			//字体旋转角度
			int degree = random.nextInt() % 30;	//角度小于30度
			int dot = random.nextInt(baseNumLetter.length()); //可选字母里随机一个索引
			ch = baseNumLetter.charAt(dot) + ""; //第 索引 处的字符
			strBuffer.append(ch); //组装
			//正向旋转
			graphics.rotate(degree * Math.PI / 180, x, 45);	//TODO 角度, x坐标, y坐标
			graphics.drawString(ch, x, 45); //TODO 角度, x坐标, y坐标
			//反向旋转
			graphics.rotate(-degree * Math.PI / 180, x, 45);
			x += 35; //下一个字符的x坐标
			
		}
		
		//绘制干扰线
		for (int i = 0; i < 4; i++) {
			//设置随机干扰线颜色
			graphics.setColor(getRandomColor());
			//随机画线
			graphics.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
		}
		
		//添加噪点
		for (int i= 0; i < 15; i++) {
			int x1 = random.nextInt(width);
			int y1 = random.nextInt(height);
			graphics.setColor(getRandomColor());
			graphics.fillRect(x1, y1, 2, 2);//在指定位置绘制指定大小的像素点
		}
		
		
		//TODO 存至本地
		/*
		String path = null;
		try {
			path = saveVerifyImg(verifyImg);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		JSONObject result = new JSONObject();
		result.put("vercodeContent", strBuffer.toString());
		//result.put("path", path);
		return result;
	}
	
	private static Color getRandomColor() {
		Random ran = new Random();
		Color color = new Color(ran.nextInt(256), ran.nextInt(256), ran.nextInt(256));
		return color;
	}
	
	private static String saveVerifyImg(BufferedImage verifyImg) throws IOException {
		String gcode = IdUtil.fastSimpleUUID();
		String path = "/verifycode/" + gcode + ".jpg";
		File savePath = new File(PathKit.getWebRootPath() + path);
		if (!savePath.exists()) {
			try {
				savePath.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("验证码保存文件不存在, 正在自动创建. . .");
		}
		ImageIO.write(verifyImg, "jpg", savePath);
		return path;
	}
	public static void BufferImageToBytes(BufferedImage verifyImg, OutputStream ops) throws IOException {
		ImageIO.write(verifyImg, "verifyImg", ops);
		
	}
}
