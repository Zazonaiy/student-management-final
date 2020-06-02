package com.studentmanagement.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.studentmanagement.service.DialogService;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class DialogController extends Controller{
	@Inject
	private DialogService service;
	
	public void index() {
		render("/dialog_view.html");
	}
	
	public void queryDialog() {
		JSONObject result = new JSONObject();
		JSONArray dialogArray = service.queryDialog();
		result.put("dialogArray", dialogArray);
		renderJson(result);
	}
	
	public void clearDialog() {
		String datetime = getPara("datetime");
		if (datetime == null) {
			datetime = "";
		}
		JSONObject result = service.clearDialog(datetime);
		renderJson(result);
	}
}
