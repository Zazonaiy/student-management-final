package com.studentmanagement.util;

import java.util.regex.Pattern;

public class UserUtil {
	private static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}
	private static boolean isLetterDigit(String str) {
		String regex = "^[a-z0-9A-Z]+$";
		return str.matches(regex);
	}

	public static boolean strIsUsername(String str) {
		return ((!UserUtil.isNumeric(str))&&UserUtil.isLetterDigit(str));

	}

	
	
	/*
	public static void main(String[] args) {
		String str = "aaa111";
		System.out.println(UserUtil.strIsUsername(str));
	}*/
}
