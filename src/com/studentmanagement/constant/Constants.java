package com.studentmanagement.constant;

import cn.hutool.json.JSONArray;

/**
 * 常量定义区域
 * @author 杨伟豪
 *
 */
public interface Constants {
	public static final JSONArray MENUS = new JSONArray();//菜单数据
	
	public static final int DB_ERROR = 100;	//数据库错误
	public static final int SUCCESS = 0;	//正常
	
	public static final int PASS_ERROR = 2; //登录密码错误
	public static final int NULL_USER = 1;  //登录账户不存在
	public static final String CURRENT_USER_KEY = "currentUser";	//session里的user的键
	public static final int INIT_STATE = -1;	//初始态
	public static final int DO_REGIST = 4;		//进行注册
	public static final int REGIST_ERROR = 3;	//注册用户已被注册
	public static final int REGIST_FORMAT_ERROR = 8; //注册的账号格式错误
	
	public static final int NULL_RES = 5;	//没有查到指定信息
	public static final int SNUM_EXIST = 6; //	学号已存在
	
	public static final int VERIFYCODE_ERROR = 7;//验证码错误
	public static final int NULL_PHONE = 9;//无该手机的用户
	
	public static final int NULL_PARAM_ERROR = 10; //参数空错误
	
	public static final int PROP_OUT_OF_BOUNDS = 500; //权限越界
	
}
