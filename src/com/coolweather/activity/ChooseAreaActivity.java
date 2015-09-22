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
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	
	/**
	 * ���б�
	 */
	private List<City> cityList;
	
	/**
	 * ���б�
	 */
	
	private List<Country> countryList;
	
	/**
	 * ��ѡ�е�ʡ
	 */
	
	private Province selectedProvince;
	
	/**
	 * ��ѡ�е���
	 */
	
	private City selectedCity;
	
	/**
	 * ��ѡ�е���
	 */
	
	private Country selectedCountry;
	
	/**
	 * ��ǰ��ѡ�еļ���
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
		queryProvinces(); //����ʡ������
		
	}
	/**
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
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
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVNCE;
		}else{
			queryFromServer(null,"province");
		}
	}
	
	/**
	 * ��ѯѡ�е�ʡ�����е��У����ȴ����ݿ��в�ѯ�����û����ȥ�������ϲ�ѯ
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
			queryFromServer(selectedProvince.getProvinceCode(),"city");  //������ݿ���ûӴ�ʹӷ������ϲ���
		}
	}
	
	/**
	 * ��ѯѡ�е��������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
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
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ���ص�����
	 */
	
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";     //���codeΪ�վ��������ַ
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
					//ͨ��runOnUiThread()�����ص����̴߳����߼�
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
				//ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					
					public void run() {
						closeProgresDialog();
						Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
						
					}
				});
				
			}
		});
	}
	
	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgresDialog(){
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);  //�ԶԻ��������Ի��򲻻���ʧ
			
		}
		progressDialog.show();
		
	}
	/**
	 * �رս��ȶԻ���
	 */
	
	private void closeProgresDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	
	/**
	 * ����Back���������ݵ�ǰ�ļ������жϣ���ʱӢ������ʲô�б����ֱ���˳�
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





































































