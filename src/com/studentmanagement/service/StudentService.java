package com.studentmanagement.service;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.studentmanagement.cache.StudentCache;
import com.studentmanagement.constant.Constants;
import com.studentmanagement.util.StudentUtil;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class StudentService {
	
	/**
	 * 查询所有班级信息
	 * @return
	 */
	public List<Record> findAllClass(){
		String sql = "select id, concat(class_no, '-', class_name) class_full_name "
				+ "from t_class "
				+ "where id in (select distinct class_fk from t_student) "
				+ "order by class_no ";
		List<Record> classList = Db.find(sql);
		return classList;
	}
	
	/**
	 * 查询学生信息
	 * @param keyword
	 * @return
	 */
	public JSONObject queryStudent(String keyword, String classId, String orderParam1, String orderParam2, 
			String paggingParam1, String paggingParam2) {
		JSONObject result = new JSONObject();
		//String sql = "select * from t_student where s_num like '%"+keyword+"%'"+
		//		" or s_name like '%"+keyword+"%'" + " order by " + orderParam1 + " " + orderParam2;
		//String selectSql = "select * ";
		String classCondition = classId==null ? "" : "class_fk=? and";
		String selectSql = "select t_student.*, u_name, class_no, class_name, enter_year, manager_user_fk ";
		String fromSql = "from t_student left JOIN t_class ON t_student.class_fk = t_class.id " + 
				"left JOIN t_user ON t_class.manager_user_fk = t_user.id "
				+ "where "+classCondition+" (s_num like '%"+keyword+"%'" + " or s_name like '%" + keyword + "%')" + " order by " + orderParam1 +
				" " + orderParam2;
		/*String fromSql = "from t_student where s_num like '%"+keyword+"%'" + " or s_name like '%" + keyword + "%'" + " order by " + orderParam1 +
				" " + orderParam2;*/
		System.out.println("----------------------------" + keyword);
		Page<Record> stuPage = classId==null ? 
				Db.paginate(Integer.valueOf(paggingParam1), Integer.valueOf(paggingParam2), selectSql, fromSql) : 
				Db.paginate(Integer.valueOf(paggingParam1), Integer.valueOf(paggingParam2), selectSql, fromSql, classId);
		//List<Record> stuList = Db.find(sql);
		List<Record> stuList = stuPage.getList();
		
		Integer recCount = stuPage.getTotalRow();
		Integer pageCount = stuPage.getTotalPage();
		result.put("recCount", recCount);
		result.put("pageCount", pageCount);
		JSONArray stuArray = StudentUtil.recListToArray(stuList);
		result.put("stuArray", stuArray);
		if (stuList.size() == 0) {
			result.put("status", Constants.NULL_RES);
		}else {
			result.put("status", Constants.SUCCESS);
		}
		System.out.println("status: ------------------- "+result.getStr("status"));
		return result;
	}
	
	/**
	 * 删除、批量删除  考虑事务
	 * @param stuNum
	 * @return
	 */
	public String deleteStudent(String stuNum) {
		final StringBuffer error = new StringBuffer();
		Db.tx(()->{
			try {
				String[] stuNums = stuNum.split(" ");
				for (String num : stuNums) {
					Db.delete("delete from t_student where s_num=?", num);
				}
				return true;
			}catch (Exception e) {
				e.printStackTrace();
				error.append("事务操作异常");
				return false;
			}
			
		});
		if (error.length() == 0) {
			return null;
		}
		return error.toString();
	}
	
	private void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files!=null) {
			for (File f : files) {
				//if (f.isDirectory()) {
				//	deleteFolder(f);
				//}
				if (!f.isDirectory()) {
					f.delete();
				}
			}
		}
	}
	
	/**
	 * 无事务控制的添加学生
	 * @param studentJson
	 */
	private JSONObject doAddStudent(JSONObject studentJson) {
		String stuNum = studentJson.getStr("s_num");
		String stuName = studentJson.getStr("s_name");
		String stuBirth = studentJson.getStr("s_birthday");
		String stuSex = studentJson.getStr("s_sex");
		String classNo = StrUtil.trimToEmpty(studentJson.getStr("class_no"));
		System.out.println(stuNum);
		System.out.println(stuName);
		System.out.println(stuBirth);
		System.out.println(stuSex);
		
		JSONObject result = new JSONObject();

		Record existStu = Db.findFirst("select s_num from t_student_test where s_num=?", stuNum);
		if (existStu != null) {
			String existSnum = existStu.getStr("s_num");
			result.put("status", Constants.NULL_RES);	//数据已存在
			result.put("error", "学生：" + existSnum + "已存在");
			return result;
		}
		
		
		try {
			JSONObject json = new JSONObject();
			json.put("s_num", stuNum);
			json.put("s_name", stuName);
			json.put("s_birthday", stuBirth);
			json.put("s_sex", stuSex);
			if (classNo.length()>0) {
				Integer classId = Db.queryInt("select id from t_class where class_no=?", classNo);
				if (classId != null) {
					json.put("class_fk", classId);
				}else {
					//未找到班级主键
					result.put("status",Constants.NULL_RES);
					result.put("error", "班级：" + classNo + "不存在");
					return result;
				}
			}
			Record studentRec = StudentUtil.JSONObjectToRecord(json);
			Db.save("t_student", studentRec);
			
			result.put("status", Constants.SUCCESS);
			return result;
			
		}catch (Exception e) {
			e.printStackTrace();
			result.put("status", Constants.DB_ERROR);
			return result;
		}
	}
	
	public String addStudentBatch(List<JSONObject> stuList) {
		StringBuffer error = new StringBuffer();
		//Db.tx(() ->{
		//	try {
		//		
		//		return true;
		//	}catch(Exception e) {
		//		e.printStackTrace();
		//		return false;
		//	}
		//});
		Db.tx(() -> {
			try {
				for (JSONObject studentJson : stuList) {
					JSONObject res = doAddStudent(studentJson);
					int status = res.getInt("status");
					String err = res.getStr("error");
					if (status != Constants.SUCCESS) {
						//有错误
						error.append(status + ": " + err +";");
					}
				}
				if (error.length()>0) { //有错误
					error.deleteCharAt(error.length()-1);//去掉最后一个分号
					return false; //回滚
				}
				return true;
			}catch(Exception e) {
				error.append(e.getMessage());
				return false;
			}
		});
		if (error.length()>0) {
			return error.toString();
		}
		return null;
	
	}
	
	public JSONObject addStudent(String stuNum, String stuName, String birthday, String sex, String stuPhoto) {
		JSONObject result = new JSONObject();
		//查询学号是否存在
		//2种方式查看是否存在
		//1直接查看是否为空
		//2看count结果是否为0,性能更好,略麻烦
		Record existStu = Db.findFirst("select s_num from t_student where s_num=?", stuNum);
		if (existStu != null) {
			result.put("status", Constants.NULL_RES);	//数据已存在
			return result;
		}
		//Long count = Db.queryLong("select count(*) from t_student where s_num=?", stuNum); //方法二
		
		try {
			JSONObject json = new JSONObject();
			json.put("s_num", stuNum);
			json.put("s_name", stuName);
			json.put("s_birthday", birthday);
			json.put("s_sex", sex);
			json.put("s_photo", stuPhoto);
			Record studentRec = StudentUtil.JSONObjectToRecord(json);
			Db.save("t_student", studentRec);
			result.put("status", Constants.SUCCESS);
			return result;
			
		}catch (Exception e) {
			e.printStackTrace();
			result.put("status", Constants.DB_ERROR);
			return result;
		}
		
		
		//JSONObject result = new JSONObject();
		//String sql = "insert into t_student (s_num, s_name, s_birthday, s_sex) values ('"+stuNum+"','"+stuName+"','"+birthday+"','"+sex+"')";
		//int rec = Db.update(sql);
		//result.put("rec", rec);
		//return result;
	}
	
	@Before(Tx.class)	//事务
	public JSONObject updateStudent(String stuNum, String stuName, String birthday, String sex, String stuPhoto) {
		JSONObject result = new JSONObject();
		//String sql = "update t_student set s_name='"+stuName+"',s_birthday='"+birthday+"',s_sex='"+sex+"' where s_num='"+stuNum+"'";
		String selectSql = "select count(*) from t_student where s_num=?";
		String updateSql = "update t_student set s_name=?, s_birthday=?, s_sex=?, s_photo=? where s_num=?";
		int num = Db.queryInt(selectSql, stuNum);
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!" + stuPhoto);
		if (num > 1) {
			result.put("status", Constants.SNUM_EXIST);
			return result;
		}
		int rec = Db.update(updateSql, stuName, birthday, sex, stuPhoto, stuNum);
		result.put("status", Constants.SUCCESS);
		return result;
	}
	
	/**
	 * 手机登录验证
	 * @param phonenum 手机号
	 * @param password 密码
	 * @param cacheJson 缓存
	 * @return
	 */
	private JSONObject validateUserByPhoneNum(String phonenum, String password, JSONObject cacheJson) {
		JSONObject result = new JSONObject();
		//System.out.println("=====================" +phonenum+ "========================");
		String selectSql = "select count(*) from t_user where u_phone_num=?";
		if (cacheJson == null) {
			int num = Db.queryInt(selectSql, phonenum);
			//System.out.println("=====================num: " +num+ "========================");
			if (num < 1) {
				//没有找到改手机号
				result.put("isSuccess", false);
				return result;
			}
			
			selectSql = "select u_pass, u_name from t_user where u_phone_num=?";
			Record rec = Db.findFirst(selectSql, phonenum);
			String relpass = rec.get("u_pass");
			String username = rec.getStr("u_name");
			//System.out.println("=====================" +relpass+ "========================");
			if (relpass.equals(password)) {
				//数据库验证通过
				result.put("isSuccess", true);
				result.put("username", username);
				return result;
			}else {
				//数据库验证失败
				result.put("isSuccess", false);
				return result;
			}
		}else {
			//从缓存中验证
			if (!cacheJson.getStr("password").equals(password)) {
				//缓存验证失败
				result.put("isSuccess", false);
				return result;
			}else {
				//缓存验证正确
				result.put("isSuccess", true);
				return result;
			}
		}
		
	}
	/**
	 * 登录验证
	 * @param param 所需的所有参数 : 账号/手机号  密码
	 * @return
	 */
	public JSONObject validateUser(String username, String password) {
		JSONObject cacheJson = StudentCache.USER_CACHE.get("username");
		JSONObject param = new JSONObject();//返回的结果
		param.put("username", username);
		param.put("password", password);
		//String username = param.getStr("username");
		//String password = param.getStr("password");
		if (cacheJson == null) {
			
			String selectSql = "select count(*) from t_user where u_name=?";
			int num = Db.queryInt(selectSql, username);
			if (num < 1) {
				JSONObject phoneValidateResult = validateUserByPhoneNum(username, password, null);
				//账号不存在于数据库中，现在去找这个账号是否是手机号
				if (phoneValidateResult.getBool("isSuccess")) {
					//手机号验证成功
					param.put("phonenum", username);//现在的username为手机号，把它放入手机号属性
					param.remove("username");//去除原来的username(原来的是手机号)
					param.put("username", phoneValidateResult.getStr("username"));//添加真正的username	
					StudentCache.USER_CACHE.put(username, param);	//账号/手机号做为键可知是以什么方式登录的
					param.put("status", Constants.SUCCESS);
				}else {
					//手机号验证失败
					StudentCache.USER_CACHE.put(username, param);
					param.put("status", Constants.NULL_USER);
				}
				//StudentCache.USER_CACHE.put(username, param);
				//param.put("status", Constants.NULL_USER);
				param.remove("password");
				return param;
			}
			
			//账号存在，手机号就不可能同时存在了,找到了账号故username不可能是手机号
			selectSql = "select * from t_user where u_name=?";
			Record rec = Db.findFirst(selectSql, username);
			String relpass = rec.get("u_pass");
			if (relpass.equals(password)) {
				//密码正确，通过
				StudentCache.USER_CACHE.put(username, param);
				param.put("status", Constants.SUCCESS);
				
			}else {
			//数据库账号验证失败(密码错误)
				//手机验证未通过
				//StudentCache.USER_CACHE.put(username, param);
				param.put("status", Constants.PASS_ERROR);
				
			}
			param.remove("password");
			return param;
		}else {
			//从缓存中进行账号/手机验证
			if (!cacheJson.getStr("password").equals(password)) {
				//缓存账号/手机号验证失败	
				param.put("status", Constants.PASS_ERROR);
			}else {
				//缓存账号/手机号验证通过
				param.put("status", Constants.SUCCESS);
			}
			param.remove("password");
			return param;
		}
		
	}
	
	/**
	 * 注册用户
	 * @param username 用户名
	 * @param password 密码
	 * @return
	 */
	public JSONObject addUser(String username, String password) {
		JSONObject result = new JSONObject();
		Record existStu = Db.findFirst("select u_name from t_user where u_name=?", username);
		if (existStu != null) {
			result.put("status", Constants.NULL_RES);	//数据已存在
			return result;
		}
		//Long count = Db.queryLong("select count(*) from t_student where s_num=?", stuNum); //方法二
		
		try {
			JSONObject json = new JSONObject();
			json.put("u_name", username);
			json.put("u_pass", password);
			Record userRec = StudentUtil.JSONObjectToRecord(json);
			Db.save("t_user", userRec);
			result.put("status", Constants.SUCCESS);
			return result;
			
		}catch (Exception e) {
			e.printStackTrace();
			result.put("status", Constants.DB_ERROR);
			return result;
		}
	}
	
	public JSONObject updateUser(String username, String newpass) {
		JSONObject result = new JSONObject();
		String selectSql = "select count(*) from t_user where u_name=?";
		String updateSql = "update t_user set u_pass=? where u_name=?";
		//System.out.println("=====================" +selectSql+ "========================");
		//System.out.println("=====================" +updateSql+ "========================");
		int num = Db.queryInt(selectSql, username);
		if (num < 1) {
			result.put("status", Constants.NULL_USER);
			return result;
		}
		//System.out.println("=====================" +num+ "========================");
		Db.update(updateSql, username, newpass);
		result.put("status", Constants.SUCCESS);
		return result;
	}
	
	public String getUserPhonenum(String username) {
		JSONObject result = new JSONObject();
		String sql = "select u_phone_num from t_user where u_name=?";
		Record rec = Db.findFirst(sql, username);
		if (rec.get("u_phone_num") != null) {
			return rec.getStr("u_phone_num");
		}
		return null;
		
	}
	
}
