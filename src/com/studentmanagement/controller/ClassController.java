package com.studentmanagement.controller;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.studentmanagement.constant.Constants;
import com.studentmanagement.service.ClassService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;

public class ClassController extends Controller{
	@Inject
	private ClassService service;
	
	public void index() {
		render("/class_view_vue.html");
	}
	
	public void deleteClass() {
		String id = getPara("id");
		JSONObject result =service.deleteClassById(id);
		
		renderJson (result);
	}
	
	private boolean isAllNotNull(String ...param) {
		for (String str : param) {
			if (str == null) {
				return false;
			}
		}
		return true;
	}
	public void updateClass() {
		String classNo = getPara("classNo");
		String className = getPara("className");
		String enterYear = getPara("enterYear");
		String managerUserFk = getPara("managerUserFk");
		
		String[] str = enterYear.split("-");
		enterYear = str[0];
		JSONObject json = new JSONObject();
		if (!isAllNotNull(classNo, className, enterYear, managerUserFk)) {
			json.put("status", Constants.NULL_PARAM_ERROR);
			renderJson(json);
		}
		json = service.updateClass(classNo, className, enterYear, managerUserFk);
		renderJson(json);
	}
	
	public void addClass() {
		String classNo = getPara("classNo");
		String className = getPara("className");
		String enterYear = getPara("enterYear");
		String managerUserFk = getPara("managerUserFk");
		
		String[] str = enterYear.split("-");
		enterYear = str[0];
		JSONObject json = new JSONObject();
		if (!isAllNotNull(classNo, className, enterYear)) {
			json.put("status", Constants.NULL_PARAM_ERROR);
			renderJson(json);
		}
		json = service.addClass(classNo, className, enterYear, managerUserFk);
		renderJson(json);
	}
	
	public void queryClass() {
		String keyword = getPara("keyword");
		if (keyword == null) {
			keyword = "";
		}
		String queryBy = getPara("queryBy");
		if (queryBy == null || queryBy == "") {
			queryBy = "class_no";
		}
		String orderParam1 = getPara("orderParam1", "class_no");
		String orderParam2 = getPara("orderParam2", "asc");
		String paggingParam1 = getPara("paggingParam1", "1");
		String paggingParam2 = getPara("paggingParam2", "5");
		System.out.println(orderParam1 + orderParam2);
		JSONObject stuJson = service.queryClass(keyword, queryBy, orderParam1, orderParam2, paggingParam1, paggingParam2);
		stuJson.put("paggingParam1", paggingParam1);
		stuJson.put("paggingParam2", paggingParam2);
		
		renderText(stuJson.toString());
		
	}
}
