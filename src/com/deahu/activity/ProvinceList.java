package com.deahu.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cheshouye.api.client.WeizhangClient;
import com.cheshouye.api.client.json.ProvinceInfoJson;
import com.deahu.adapter.ListAdapter;
import com.deahu.model.ListModel;
import com.example.iov.R;


public class ProvinceList extends Activity {
	private ListView lv_list;
	private ListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.csy_activity_citys);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.csy_titlebar);
		
		//����
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtTitle.setText("ѡ���ѯ��-ʡ��");
		
		//���ذ�ť
		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		
		lv_list = (ListView) findViewById(R.id.lv_1ist);

		mAdapter = new ListAdapter(this, getData2());
		lv_list.setAdapter(mAdapter);

		lv_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				TextView txt_name = (TextView) view.findViewById(R.id.txt_name);

				Intent intent = new Intent();
				intent.putExtra("province_name", txt_name.getText());
				intent.putExtra("province_id", txt_name.getTag().toString());

				intent.setClass(ProvinceList.this, CityList.class);
				startActivityForResult(intent, 20);
			}
		});

	}

	/**
	 * title:��ȡʡ����Ϣ
	 * 
	 * @return
	 */
	private List<ListModel> getData2() {

		List<ListModel> list = new ArrayList<ListModel>();
		List<ProvinceInfoJson> provinceList = WeizhangClient.getAllProvince();
		
		//��ͨ������ʾ
		TextView txtListTip = (TextView) findViewById(R.id.list_tip);
		txtListTip.setText("ȫ���ѿ�ͨ"+provinceList.size()+"��ʡ��, ����ʡ��½������");
		
		for (ProvinceInfoJson provinceInfoJson : provinceList) {
			String provinceName = provinceInfoJson.getProvinceName();
			int provinceId = provinceInfoJson.getProvinceId();
			
			ListModel model = new ListModel();
			model.setTextName(provinceName);
			model.setNameId(provinceId);
			list.add(model);
		}
		return list;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		// ��ȡ����name
		String cityName = bundle.getString("city_name");
		String cityId = bundle.getString("city_id");

		Intent intent = new Intent();
		intent.putExtra("city_name", cityName);
		intent.putExtra("city_id", cityId);
		setResult(1, intent);
		finish();
	}
}
