package com.studentmanagement.util;

import java.io.File;
import java.util.regex.Matcher;

import com.jfinal.kit.PathKit;

public class Test {
	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files!=null) {
			for (File f : files) {
				//if (f.isDirectory()) {
				//	deleteFolder(f);
				//}
				if (!f.isDirectory()) {
					f.delete();
					System.out.println("删除成功");
				}
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println(Matcher.quoteReplacement(File.separator));
	}
}
