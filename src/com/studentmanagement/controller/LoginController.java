package com.studentmanagement.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.studentmanagement.cache.StudentCache;
import com.studentmanagement.constant.Constants;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.util.CipherMachine;
import com.studentmanagement.util.UserUtil;
import com.studentmanagement.util.VerifyCode;

import cn.hutool.json.JSONObject;

public class LoginController extends Controller{
	@Inject
	private StudentService service;
	
	public void index() {
		render("/login.html");
	}
	
	/**
	 * 登录验证
	 */
	public void doLogin() {
		String username = getPara("username").trim();
		String password = getPara("password").trim();
		try {
			password = CipherMachine.encryption(password);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//JSONObject loginParam = new JSONObject();
		//loginParam.put("username", username);
		//loginParam.put("password", password);
		JSONObject result = service.validateUser(username, password);
		
		JSONObject userMess = result;
		setSessionAttr(Constants.CURRENT_USER_KEY, userMess);
		
		//若登录成功了，将已存在session里的验证码给删除了
		if (result.getInt("status").equals(Constants.SUCCESS)) {
			if (getSessionAttr("vercode")!=null) {
				removeSessionAttr("vercode");
			}
		}
		
		renderJson(result);
		
	}
	
	/**
	 * 去主页
	 */
	public void toHome() {
		//String status = getPara("status");
		//if ("0".equals(status)) {
			render("/Homepage.html");
		//}else {
		//	render("/login.html");
		//}
	}
	
	/**
	 * 返回当前用户信息
	 */
	public void getThisUserMess() {
		JSONObject userJson = getSessionAttr(Constants.CURRENT_USER_KEY);
		//String username = userJson.getStr("username");
		String userphone = service.getUserPhonenum(userJson.getStr("username"));
		if (userJson.getStr("phonenum") == null) {
			if (userphone != null) {
				userJson.put("phonenum", userphone);
			}else {
				userJson.put("phonenum", Constants.NULL_PHONE);
			}
		}
		
		renderJson(userJson);
	}
	
	/**
	 * 注销
	 */
	public void cancellation() {
		removeSessionAttr(Constants.CURRENT_USER_KEY);
		String ctxPath = getPara("ctxPath");
		redirect("/login");
	}
	
	/**
	 * 绘制并返回验证码
	 */
	public void getVercode() {

		int width = 165;
		int height = 58;
			
		BufferedImage verifyImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//初始图片
		JSONObject json = VerifyCode.drawRandomText(width, height, verifyImg);
		
		//把验证码的值放到session
		setSessionAttr("vercode", json.getStr("vercodeContent"));
		OutputStream ops;
		try {
			ops = getResponse().getOutputStream();
			//this.getResponse().setContentType("./login.html");
			
			ImageIO.write(verifyImg, "jpg", ops);
			//try {
				ops.flush();
			//} catch (IOException e1) {
			//	e1.printStackTrace();
			//}
			
			this.renderNull();
		}catch(Exception e) {
			e.printStackTrace();
			//renderText("error");
		}
		//把验证码存储的地址返回前端
		//System.out.println("pppppppppppppppppppppp" + json.getStr("path"));
		//renderText(json.getStr("path"));
		
		
		
	}
	
	/**
	 * 验证码验证
	 */
	public void doVerification() {
		String input = getPara("inputCode");
		String relCode = getSessionAttr("vercode");
		System.out.println("============= input: " + input + "==============");
		System.out.println("============= relCode: " + relCode + "==============");
		JSONObject result = new JSONObject();
		
		if (input == null) {
			result.put("status", Constants.VERIFYCODE_ERROR);
			renderJson(result);
			return;
		}
		
		if (input.equalsIgnoreCase(relCode)) {
			result.put("status", Constants.SUCCESS);
		}else {
			result.put("status", Constants.VERIFYCODE_ERROR);
		}
		renderJson(result);
	}
	
	/**
	 * 跳转至注册页面
	 */
	public void toRegister() {
		render("/Register.html");
	}
	
	/**
	 * 注册账户
	 */
	public void doRegist() {
		String username = getPara("username");
		String password = getPara("password");
		if (!UserUtil.strIsUsername(username)) {
			JSONObject er = new JSONObject();
			er.put("status", Constants.REGIST_FORMAT_ERROR);
			renderJson(er);
			return ;
		}
		try {
			password = CipherMachine.encryption(password);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject result = service.addUser(username, password);
		renderJson(result);
	}
	
	/**
	 * 跳转至修改密码页面
	 */
	public void toUpdateUser() {
		render("/updateUser.html");
	}
	
	/**
	 * 修改密码
	 */
	public void doUpdateUser() {
		String username = getPara("username");
		String oldpass = getPara("oldpassword");
		String newpass = getPara("newpassword");
		try {
			oldpass = CipherMachine.encryption(oldpass);
			newpass = CipherMachine.encryption(newpass);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (StudentCache.USER_CACHE.get(username) != null) {
			StudentCache.USER_CACHE.remove(username);
		}
		JSONObject result = new JSONObject();
		JSONObject valiJson = service.validateUser(username, oldpass);
		//System.out.println("=====================" +valiJson.getInt("status")+ "========================");
		if (!valiJson.getInt("status").equals(Constants.SUCCESS)) {
			result.put("status",Constants.PASS_ERROR);
			System.out.println("=====================" +valiJson.getInt("status")+ "========================");
			renderJson(result);
			return;
		}
		
		result = service.updateUser(username, newpass);
		renderJson(result);
	}
}
