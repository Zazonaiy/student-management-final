package com.studentmanagement.task;

import cn.hutool.core.date.DateUtil;

public class MyTask implements Runnable{
	@Override
	public void run() {
		System.out.println(DateUtil.now());
		
	}
}
