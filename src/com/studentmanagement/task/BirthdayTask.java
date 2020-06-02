package com.studentmanagement.task;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import cn.hutool.core.date.DateUtil;

public class BirthdayTask implements Runnable {

	@Override
	public void run() {
		List<Record> birthdayStuList = Db.find("select * from t_student where s_birthday=?", DateUtil.now());
		for (Record stu : birthdayStuList) {
			//一次返送生日短信
		}
	}
	
}
