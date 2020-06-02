package com.studentmanagement.interceptor;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.studentmanagement.constant.Constants;
import com.studentmanagement.controller.LoginController;
import com.studentmanagement.util.StudentUtil;

import cn.hutool.json.JSONObject;

public class StudentInterceptor implements Interceptor{

	@Override
	public void intercept(Invocation inv) {
		Controller c = inv.getController();
		//读取配置文件，获取title
		String sysTitle = PropKit.get("sys_title");
		c.setAttr("sysTitle", sysTitle);
		
		//获取context path
		HttpServletRequest request = c.getRequest();
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int port = request.getServerPort();
		String ctxPath = request.getContextPath();
		if (port == 80) {
			ctxPath = scheme+"://"+serverName+ctxPath;
		}else {
			ctxPath = scheme+"://"+serverName+":"+port+ctxPath;
		}
		c.setAttr("ctxPath", ctxPath);
		
		//判断用户是否登录
		//非login controller都要登录后访问
		if (c.getClass() != LoginController.class) {
			JSONObject currentUser = c.getSessionAttr(Constants.CURRENT_USER_KEY);
			if (currentUser == null){
				c.redirect(ctxPath + "/login");
			}
			c.setAttr(Constants.CURRENT_USER_KEY, currentUser);
		}
		
		System.out.println(inv.getActionKey());
		doDialog(inv.getActionKey());
		
		inv.invoke();
		
	}
	
	private void doDialog(String action) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(new Date());
		try {
			JSONObject json = new JSONObject();
			json.put("t_action", action);
			json.put("t_time", time);
			Db.save("t_log_jfinal", StudentUtil.JSONObjectToRecord(json));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
