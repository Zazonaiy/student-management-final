package com.studentmanagement.controller;

import org.dom4j.DocumentException;

import com.jfinal.core.Controller;
import com.studentmanagement.constant.Constants;

import cn.hutool.json.JSONArray;

public class WelcomeController extends Controller{
	
	public void index() {
		JSONArray menuJsonArray = Constants.MENUS;
		setAttr("menus", menuJsonArray);
		System.out.println(menuJsonArray);
		render("/student_admin.html");
	}
}
