package com.wazh.shawn.cement.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.wazh.shawn.cement.util.CrashHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyApplication extends Application{

	private static Map<String, Activity> destoryMap = new HashMap<>();

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		getUserInfo(this);
	}

	//本地保存用户信息参数
	public static String USERNAME = "";
	public static String PASSWORD = "";
	public static String TOKEN = "";

	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		private static final String userName = "uName";
		private static final String passWord = "pwd";
		private static final String token = "token";
	}

	/**
	 * 清除用户信息
	 */
	public static void clearUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
		USERNAME = "";
		PASSWORD = "";
		TOKEN = "";
	}

	/**
	 * 保存用户信息
	 */
	public static void saveUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(UserInfo.userName, USERNAME);
		editor.putString(UserInfo.passWord, PASSWORD);
		editor.putString(UserInfo.token, TOKEN);
		editor.apply();
	}

	/**
	 * 获取用户信息
	 */
	public static void getUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		USERNAME = sharedPreferences.getString(UserInfo.userName, "");
		PASSWORD = sharedPreferences.getString(UserInfo.passWord, "");
		TOKEN = sharedPreferences.getString(UserInfo.token, "");
	}

	/**
	 * 添加到销毁队列
	 * @param activity 要销毁的activity
	 */
	public static void addDestoryActivity(Activity activity,String activityName) {
		destoryMap.put(activityName,activity);
	}

	/**
	 *销毁指定Activity
	 */
	public static void destoryActivity() {
		Set<String> keySet=destoryMap.keySet();
		for (String key:keySet){
			destoryMap.get(key).finish();
		}
	}

}
