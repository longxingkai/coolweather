package com.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.model.City;
import com.coolweather.model.CoolWeatherDB;
import com.coolweather.model.Country;
import com.coolweather.model.Province;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVNCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listview;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	
	/**
	 * 市列表
	 */
	private List<City> cityList;
	
	/**
	 * 县列表
	 */
	
	private List<Country> countryList;
	
	/**
	 * 被选中的省
	 */
	
	private Province selectedProvince;
	
	/**
	 * 被选中的市
	 */
	
	private City selectedCity;
	
	/**
	 * 被选中的县
	 */
	
	private Country selectedCountry;
	
	/**
	 * 当前被选中的级别
	 */
	
	private int currentLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listview = (ListView) this.findViewById(R.id.list_view);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		titleText = (TextView) this.findViewById(R.id.title_text);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				if(currentLevel == LEVEL_PROVNCE){
					selectedProvince = provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					queryCounties();
				}
				
			}
		});
		queryProvinces(); //加载省级数据
		
	}
	/**
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
	 */

	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVNCE;
		}else{
			queryFromServer(null,"province");
		}
	}
	
	/**
	 * 查询选中的省内所有的市，优先从数据库中查询，如果没有再去服务器上查询
	 */
	
	private void queryCities(){
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");  //如果数据库中没哟就从服务器上查找
		}
	}
	
	/**
	 * 查询选中的市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	
	private void queryCounties(){
		countryList = coolWeatherDB.loadCounties(selectedCity.getId());
		if(countryList.size()>0){
			dataList.clear();
			for(Country country:countryList){
				dataList.add(country.getCountryName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTRY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"country");
		}
	}
	
	
	/**
	 * 根据传入的代号和类型从服务器上查询省市县的数据
	 */
	
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";     //如果code为空就用这个地址
		}
		showProgresDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			public void onFinish(String response) {
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("country".equals(type)){
					result = Utility.handleCountriesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){
					//通过runOnUiThread()犯法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						public void run() {
							closeProgresDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("country".equals(type)){
								queryCounties();
							}
							
						}
					});
				}
			}
			
			public void onError(Exception e) {
				//通过runOnUiThread()犯法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					
					public void run() {
						closeProgresDialog();
						Toast.makeText(getApplicationContext(), "加载失败", Toast.LENGTH_SHORT).show();
						
					}
				});
				
			}
		});
	}
	
	/**
	 * 显示进度对话框
	 */
	private void showProgresDialog(){
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);  //对对话框外点击对话框不会消失
			
		}
		progressDialog.show();
		
	}
	/**
	 * 关闭进度对话框
	 */
	
	private void closeProgresDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	
	/**
	 * 补货Back按键，根据当前的级别来判断，此时英爱返回什么列表或者直接退出
	 */
	@Override
	public void onBackPressed() {
		if(currentLevel == LEVEL_COUNTRY){
			
			queryCities();
			adapter.setNotifyOnChange(true);
			listview.setSelection(0);
		}else if(currentLevel == LEVEL_CITY){
			
			queryProvinces();
			adapter.setNotifyOnChange(true);
			listview.setSelection(0);
		}else{
			finish();
		}
	}
	
	
	
	
	
	
}





































































