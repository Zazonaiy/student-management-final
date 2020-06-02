package com.studentmanagement.util;

import java.util.List;

import com.jfinal.plugin.activerecord.Record;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class StudentUtil {
	public static JSONArray recListToArray(List<Record> recList) {
		JSONArray array = new JSONArray();
		for (Record r : recList) {
			JSONObject json = JSONUtil.parseObj(r.toJson());
			array.add(json);
		}
		return array;
	}
	
	public static Record JSONObjectToRecord(JSONObject json) {
		Record rec = new Record();
		rec.setColumns(json);
		return rec;
	}
}
