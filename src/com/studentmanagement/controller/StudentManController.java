package com.studentmanagement.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.studentmanagement.service.StudentService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;

/**
 * 学生管理系统-控制器
 * @author 杨伟豪
 *
 */
public class StudentManController extends Controller{
	@Inject
	private StudentService service;
	
	public void index() {
		List<Record> classList = service.findAllClass();
		setAttr("classList", classList);
		//render("/student_view.html");
		//render("/student_view_vue.html");
		render("/student_view_local.html");
		
	}
	
	
	
	public void queryStudent() {
		String keyword = getPara("keyword");
		String classId = StrUtil.trimToNull(getPara("classId"));
		if (keyword == null) {
			keyword = "";
		}
		String orderParam1 = getPara("orderParam1", "s_num");
		String orderParam2 = getPara("orderParam2", "asc");
		String paggingParam1 = getPara("paggingParam1", "1");
		String paggingParam2 = getPara("paggingParam2", "5");
		System.out.println(orderParam1 + orderParam2);
		JSONObject stuJson = service.queryStudent(keyword, classId, orderParam1, orderParam2, paggingParam1, paggingParam2);
		stuJson.put("paggingParam1", paggingParam1);
		stuJson.put("paggingParam2", paggingParam2);
		/*
		try {
			Thread.sleep(1000);
		}catch (Exception e) {
			e.printStackTrace();
		}
		*/
		//renderJson(stuJson);
		renderText(stuJson.toString());
	}
	
	public void deleteStudent() {
		System.out.println(getPara("s_num"));
		//String[] stuNums = getPara("s_num").split(" ");
		//for (String str : stuNums) {
		//	service.deleteStudent(str);
		//}
		//JSONObject result = new JSONObject();
		//result.put("status", 0);
		JSONObject result = new JSONObject();
		String str = service.deleteStudent(getPara("s_num"));
		if (str == null) {
			result.put("status", 0);
		}else {
			result.put("error", str);
		}
		
		renderJson(result);
	}
	
	public void addStudent() {
		String stuNum = getPara("s_num");
		String stuName = getPara("s_name");
		String stuBirth = getPara("s_birthday");
		String stuSex = getPara("s_sex");
		String stuPhoto = getPara("s_photo");
		System.out.println(stuNum);
		System.out.println(stuName);
		System.out.println(stuBirth);
		System.out.println(stuSex);
		
		renderJson(service.addStudent(stuNum, stuName, stuBirth, stuSex, stuPhoto));
	}
	
	
	public void updateStudent() {
		String stuNum = getPara("s_num");
		String stuName = getPara("s_name");
		String stuBirth = getPara("s_birthday");
		String stuSex = getPara("s_sex");
		String stuPhoto = getPara("s_photo");
		renderJson(service.updateStudent(stuNum, stuName, stuBirth, stuSex, stuPhoto));
	}
	
	/**
	 * 处理文件上传请求
	 */
	public void uploadFiles() {
		//要保存的唯一目录
		String dir = IdUtil.fastSimpleUUID();
		getFiles(dir);
		//默认的保存路径
		File savePath = new File(PathKit.getWebRootPath() + "/upload/" + dir);
		File[] files = savePath.listFiles();
		if (files.length == 0) {
			//没有上传任何文件
			renderText("error:上传文件为空! ");
			return ;
		}
		StringBuffer ret = new StringBuffer();
		for (File file : files) {
			String path = "/upload/" + dir + "/" + file.getName() + "|";
			ret.append(path);
		}
		if (ret.length()>0) {
			ret.deleteCharAt(ret.length() - 1);	//删除最后的分隔符
		}
		renderText(ret.toString());
		
	}
	
	
	
	/**
	 * 将学生的map转为json
	 * @param stuXlsMap
	 * @return
	 */
	private static final Map<String, String> TITLE_MAP = new HashMap<>();;
	static {
		TITLE_MAP.put("学号", "s_num");
		TITLE_MAP.put("姓名", "s_name");
		TITLE_MAP.put("出生日期", "s_birthday");
		TITLE_MAP.put("所在班级", "class_no");
	}
	private JSONObject stuXlsMapToJson(Map<String, Object> stuXlsMap) {
		JSONObject studentJson = new JSONObject();
		Set<String> keySet = stuXlsMap.keySet();
		for (String key : keySet) {
			String jsonKey = TITLE_MAP.get(key);
			Object value = stuXlsMap.get(key);
			if (value instanceof Date) {
				value = DateUtil.format((Date)value, "yyyy-MM-dd");
			}
			studentJson.put(jsonKey, value);
		}
		return studentJson;
	}
	
	/**
	 * excel导入
	 */
	public void importStudent() {
		String dir = "xls_import/" + IdUtil.fastSimpleUUID();
		getFiles(dir);
		File savePath = new File(PathKit.getWebRootPath() + "/upload/" + dir);
		File[] files = savePath.listFiles();
		if (files.length == 0) {
			//没有上传任何文件
			renderText("error: 上传文件为空！");
			return ;
		}
		File xlsFile = files[0];
		ExcelReader reader = ExcelUtil.getReader(xlsFile);
		List<Map<String, Object>> readAll = reader.readAll();
		List<JSONObject> stuList = new ArrayList<>();
		for (Map<String, Object> data : readAll) {
			JSONObject studentJson = stuXlsMapToJson(data);
			stuList.add(studentJson);
		}
	
		System.out.println(readAll);
		System.out.println(stuList);
		reader.close();
		FileUtil.del(savePath);
		
		String error = service.addStudentBatch(stuList);
		if (error != null) {
			renderText("error:"+error);
		}else {
			renderText("success");
		}
		
		
	}
}
