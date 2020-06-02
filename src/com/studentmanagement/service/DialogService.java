package com.studentmanagement.service;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.studentmanagement.constant.Constants;
import com.studentmanagement.util.StudentUtil;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class DialogService {
	@Before(Tx.class)
	public JSONObject clearDialog(String datetime) {
		JSONObject result = new JSONObject();
		try {
			String sql = "delete from t_log_jfinal where t_time like '"+datetime+"%'";
			System.out.println(sql);
			Db.delete(sql);
			result.put("status", Constants.SUCCESS);
			return result;
		}catch(Exception e) {
			e.printStackTrace();
			result.put("status", Constants.DB_ERROR);
			return result;
		}
	}
	
	public JSONArray queryDialog() {
		try {
			String sql = "select id, t_action, t_time from t_log_jfinal";
			List<Record> dialogList = Db.find(sql);
			System.out.println(dialogList);
			JSONArray dialogArray = StudentUtil.recListToArray(dialogList);
			return dialogArray;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
