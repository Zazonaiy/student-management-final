package com.studentmanagement.startserver;

import com.jfinal.core.JFinal;

public class StartServer {
	public static void main(String[] args) {
		JFinal.start("WebContent", 9999, "/", 0);
	}
}
