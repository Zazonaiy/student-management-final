package com.studentmanagement.util;


import java.io.IOException;


import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

public class CipherMachine {
	
	//加密 base64 + md5 + 余9位添加顺序添加字母加密 + aes + 盐
	public static String encryption(String password) throws IOException {
		//base64加密
		String cryptStr = Base64.encode(password);
		//md5
		cryptStr = SecureUtil.md5(cryptStr);
		//余9位
		String keypool="asdfzxcas";
		int cryptSize = cryptStr.length();
		int keypoolSize = keypool.length();
		StringBuffer addStr = new StringBuffer();
		for (int i = 0; i < cryptSize; i++) {
			int charIndex = i % keypoolSize;
			addStr.append(keypool.charAt(charIndex));
		}
		cryptStr += addStr.toString();
		//aes
		//byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded(); //随机生成密钥
		byte[] key = CipherMachine.getKey();
		AES aes = SecureUtil.aes(key); //构建
		byte[] encryptBytes = aes.encrypt(cryptStr); //加密
		//加盐
		StringBuffer buffer = new StringBuffer();
		for (byte b : encryptBytes) {
			int number = b & 0xff;
			String str = Integer.toHexString(number);
			if (str.length() == 1) {
				buffer.append(0);
			}
			buffer.append(str);
		}
		//得到暗文
		return buffer.toString();
		
	}
	
	//随机生成密钥并存储
	public static void newKey() throws IOException {
		byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
		CipherMachine.saveKey(key);
	}
	
	//保存key
	private static void saveKey(byte[] key) throws IOException {
		MyIO.write(key, "./resources/secret_key.obj");
	}
	
	private static byte[] getKey() throws IOException {
		return (byte[]) MyIO.read("./resources/secret_key.obj");
		//return (byte[]) MyIO.read("./secret_key.obj");
	}
	
	/*
	public static void main(String[] args) {
		
		//String str = "aaa";
		//String s1 = CipherMachine.encryption(str);
		//System.out.println(s1);
		//String str2 = "aaa";
		//String s2 = CipherMachine.encryption(str2);
		//System.out.println(s2);
		//System.out.println(s1.equals(s2));
		
		//byte[] key = CipherMachine.newKey();
		//System.out.println(key);
		try {
		//	CipherMachine.saveKey(key);
			//byte[] key = CipherMachine.getKey();
			String str = "aiywh3";
			String str2 = "aiywh3";
			String s1 = CipherMachine.encryption(str);
			String s2 = CipherMachine.encryption(str2);
			System.out.println(s1);
			System.out.println(s2);
			System.out.println(s1.equals(s2));
			System.out.println("----------" + CipherMachine.getKey());
		}catch (Exception e) {
			//e.printStackTrace();
		}
		
	}
	*/
	
}
