package com.studentmanagement.service;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.studentmanagement.constant.Constants;
import com.studentmanagement.util.StudentUtil;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class ClassService {
	public JSONObject deleteClassById(String classId) {
		JSONObject result = new JSONObject();
		final StringBuffer error = new StringBuffer();
		Db.tx(()->{
			try {
				String[] ids= classId.split(" ");
				for (String id : ids) {
					Db.delete("delete from t_class where id=?", id);
				}
				return true;
			}catch(Exception e) {
				e.printStackTrace();
				error.append("事务操作异常");
				return false;
			}
		});
		if (error.length() == 0) {
			result.put("status", Constants.SUCCESS);
		}else{
			result.put("status", Constants.DB_ERROR);
			result.put("errorMes", error);
		}
		return result;
	}
	
	public boolean isClassExist(String classNo) {
		String sql = "select count(*) as count from t_class where class_no=?";
		int res = Db.queryInt(sql, classNo);
		if (res > 0) {
			return true;
		}else {
			return false;
		}
	}
	
	public JSONObject updateClass(String classNo, String className, String enterYear, String managerUserFk) {
		JSONObject result = new JSONObject();
		if (!isClassExist(classNo)) {
			 result.put("status", Constants.NULL_RES);
			 return result;
		}
		
		if (managerUserFk != null && managerUserFk != "") {
			//修改信息里有老师
			if (Db.queryInt("select count(*) from t_user where u_name=?", managerUserFk) < 1) {
				//老师不存在
				result.put("status", Constants.NULL_USER);
				return result;
			}
		}else {
			//修改信息里没老师
			managerUserFk = null;
		}
		
		try {
			String updateSql = "update t_class set class_name=?, enter_year=?, manager_user_fk=? where class_no=?";
			int rec = Db.update(updateSql, className, enterYear, managerUserFk, classNo);
			result.put("status", Constants.SUCCESS);
			return result;
		}catch(Exception e) {
			result.put("status", Constants.DB_ERROR);
			return result;
		}
	}
	
	public JSONObject addClass(String classNo, String className, String enterYear, String managerUserFk) {
		JSONObject result = new JSONObject();
		if (isClassExist(classNo)) {
			result.put("status", Constants.NULL_RES);
			return result;
		}
		if (managerUserFk == null || managerUserFk == "") {
			managerUserFk = null;
		}
		
		try {
			JSONObject json = new JSONObject();
			json.put("class_no", classNo);
			json.put("class_name", className);
			json.put("enter_year", enterYear);
			json.put("manager_user_fk", managerUserFk);
			Record classRec = StudentUtil.JSONObjectToRecord(json);
			Db.save("t_class", classRec);
			result.put("status", Constants.SUCCESS);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("status", Constants.DB_ERROR);
		}
		return result;
	}
	
	
	public JSONObject queryClass(String keyword,String queryBy, String orderParam1, String orderParam2, String paggingParam1, String paggingParam2) {
		JSONObject result = new JSONObject();
		String selectSql = "select t_class.* ";
		String fromSql = "from t_class where " +queryBy+" like '%"+keyword+"%' order by " + orderParam1 + " " + orderParam2;
		System.out.println(selectSql + fromSql);
		System.out.println("queryBy " + queryBy);
		Page<Record> classPage = Db.paginate(Integer.valueOf(paggingParam1), Integer.valueOf(paggingParam2), selectSql, fromSql);
		List<Record> classList = classPage.getList();
		
		Integer recCount = classPage.getTotalRow();
		Integer pageCount = classPage.getTotalPage();
		result.put("recCount", recCount);
		result.put("pageCount", pageCount);
		JSONArray classArray = StudentUtil.recListToArray(classList);
		result.put("classArray", classArray);
		if (classList.size() == 0) {
			result.put("status", Constants.NULL_RES);
		}else {
			result.put("status", Constants.SUCCESS);
		}
		return result;
		
	}
}
