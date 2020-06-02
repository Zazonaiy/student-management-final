package com.studentmanagement.config;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.jfinal.aop.AopManager;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.studentmanagement.controller.ClassController;
import com.studentmanagement.controller.DialogController;
import com.studentmanagement.controller.LoginController;
import com.studentmanagement.controller.StudentManController;
import com.studentmanagement.controller.WelcomeController;
import com.studentmanagement.interceptor.StudentInterceptor;
import com.studentmanagement.task.BirthdayTask;
import com.studentmanagement.task.MyTask;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;


public class StudentManConfig extends JFinalConfig{

	@Override
	public void configConstant(Constants me) {
		PropKit.use("config.properties");
		boolean devMode = PropKit.getBoolean("dev_mode");
		me.setDevMode(devMode);
		me.setInjectDependency(true);
		

		
	}

	@Override
	public void configRoute(Routes me) {
		me.add("/student", StudentManController.class);
		me.add("/login", LoginController.class); 
		me.add("/student/class", ClassController.class);
		me.add("/dialog", DialogController.class);
		me.add("/welcome", WelcomeController.class);
		
	}

	@Override
	public void configEngine(Engine me) {
		PropKit.use("config.properties");
		boolean devMode = PropKit.getBoolean("dev_mode");
		me.setDevMode(devMode);	//開發模式
		me.setBaseTemplatePath(PathKit.getWebRootPath()+"/WEB-INF/view");
		
	}

	@Override
	public void configPlugin(Plugins me) {
		String url = PropKit.get("url");
		String dbUser = PropKit.get("username");
		String dbPass = PropKit.get("password");
		DruidPlugin dp = new DruidPlugin(url, dbUser, dbPass);
		dp.setFilters(PropKit.get("filters"));
		me.add(dp);
		
		// 添加Active Record Plugin
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
		me.add(arp);
		//arp.addMapping("t_student", Student.class);
		
		Cron4jPlugin cp = new Cron4jPlugin();
		//cp.addTask("* * * * *", new MyTask());
		//cp.addTask("0 12 * * *", new BirthdayTask());
		me.add(cp);
	}

	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new StudentInterceptor());
		
	}

	@Override
	public void configHandler(Handlers me) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart() {
		//加载菜单数据
		try {
			File menuFile = new File(PathKit.getWebRootPath() + "/WEB-INF/menu.xml");
			SAXReader reader = new SAXReader();
			Document doc = reader.read(menuFile);
			
			Element root = doc.getRootElement();
			List<Element> menus = root.elements();
			JSONArray menuJsonArray = com.studentmanagement.constant.Constants.MENUS;
			for (Element menuEle : menus) {
				JSONObject menuJson = new JSONObject();
				menuJson.put("id", menuEle.attributeValue("id"));
				menuJson.put("name", menuEle.attributeValue("name"));
				menuJson.put("icon", menuEle.attributeValue("icon"));
				menuJsonArray.add(menuJson);
				//二级菜单
				JSONArray subMenuArray = new JSONArray();
				List<Element> subMenus = menuEle.elements();
				for (Element subMenuEle : subMenus) {
					JSONObject subMenuJson = new JSONObject();
					subMenuJson.put("id",  subMenuEle.attributeValue("id"));
					subMenuJson.put("name",  subMenuEle.attributeValue("name"));
					subMenuArray.add(subMenuJson);
				}
				menuJson.put("subMenus", subMenuArray);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}


}
