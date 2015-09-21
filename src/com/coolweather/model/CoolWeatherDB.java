package com.coolweather.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 封装一些常用的数据库操作方法
 */
import com.coolweather.db.CoolWeatherOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {

	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "cool_weather";
	
	/**
	 * 数据库版本名
	 */
	public static final int VERSION = 1;
	
	private static CoolWeatherDB coolWeatherDB;
	
	private SQLiteDatabase db;
	
	/**
	 * 将构造方法私有化
	 */
	
	private CoolWeatherDB(Context context){
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		
		db = dbHelper.getWritableDatabase();
	}
	
	/**
	 * 获取CoolWeatherDB实例
	 */
	public synchronized static CoolWeatherDB getInstance(Context context){
		if(coolWeatherDB == null){
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	
	/**
	 * 将Province实例存储到数据库
	 */
	public void saveProvince(Province province){
		if(province != null){
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	
	/**
	 * 从数据库中读取全国所有的省份的信息
	 */
	
	public List<Province> loadProvinces(){
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndexOrThrow("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
			}
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
