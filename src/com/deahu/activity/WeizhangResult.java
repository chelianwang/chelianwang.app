package com.deahu.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cheshouye.api.client.WeizhangClient;
import com.cheshouye.api.client.json.CarInfo;
import com.cheshouye.api.client.json.CityInfoJson;
import com.cheshouye.api.client.json.WeizhangResponseHistoryJson;
import com.cheshouye.api.client.json.WeizhangResponseJson;
import com.deahu.adapter.WeizhangResponseAdapter;
import com.example.iov.R;


/**
 * title����ѯΥ����Ϣ
 * 
 * @author paul
 * 
 */
public class WeizhangResult extends Activity {
	final Handler cwjHandler = new Handler();
	WeizhangResponseJson info = null;

	private View popLoader;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ѭ������б�������ʾ �Զ���adapter
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.csy_activity_result);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.csy_titlebar);

		// ����
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtTitle.setText("Υ�²�ѯ���");

		// ���ذ�ť
		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		popLoader = (View) findViewById(R.id.popLoader);
		popLoader.setVisibility(View.VISIBLE);

	

		Intent intent = this.getIntent();

		CarInfo car = (CarInfo) intent.getSerializableExtra("carInfo");

		step4(car);

		// ��ѯ����: ����+��ѯ��
		TextView query_chepai = (TextView) findViewById(R.id.query_chepai);
		TextView query_city = (TextView) findViewById(R.id.query_city);
		query_chepai.setText(car.getChepai_no());
		CityInfoJson citys = WeizhangClient.getCity(car.getCity_id());
		query_city.setText(citys.getCity_name());
	}

	public void step4(final CarInfo car) {
		// ����һ�����߳�
		new Thread() {
			@Override
			public void run() {
				try {
					// ����д�����߳���Ҫ���Ĺ���
					info = WeizhangClient.getWeizhang(car);
					cwjHandler.post(mUpdateResults); // ����UI�߳̿��Ը��½����
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};

	private void updateUI() {
		TextView result_null = (TextView) findViewById(R.id.result_null);
		TextView result_title = (TextView) findViewById(R.id.result_title);
		ListView result_list = (ListView) findViewById(R.id.result_list);

		popLoader.setVisibility(View.GONE);

		Log.d("��������", info.toJson());
		
		// ֱ�ӽ���Ϣ������ Activity��
		if (info.getStatus() == 2001) {
			result_null.setVisibility(View.GONE);
			result_title.setVisibility(View.VISIBLE);
			result_list.setVisibility(View.VISIBLE);

			result_title.setText("��Υ��" + info.getCount() + "��, ��"
					+ info.getTotal_score() + "��, ���� " + info.getTotal_money()
					+ "Ԫ");

			WeizhangResponseAdapter mAdapter = new WeizhangResponseAdapter(
					this, getData());
			result_list.setAdapter(mAdapter);

		} else {
			// û�в鵽Ϊ�¼�¼

			if (info.getStatus() == 5000) {
				result_null.setText("����ʱ�����Ժ�����");
			} else if (info.getStatus() == 5001) {
				result_null.setText("���ܾ�ϵͳ����æµ�У����Ժ�����");
			} else if (info.getStatus() == 5002) {
				result_null.setText("��ϲ����ǰ���н��ܾ���������Υ�¼�¼");
			} else if (info.getStatus() == 5003) {
				result_null.setText("�����쳣�������²�ѯ");
			} else if (info.getStatus() == 5004) {
				result_null.setText("ϵͳ�������Ժ�����");
			} else if (info.getStatus() == 5005) {
				result_null.setText("������ѯ������������");
			} else if (info.getStatus() == 5006) {
				result_null.setText("����ʵ��ٶȹ���, �������");
			} else if (info.getStatus() == 5008) {
				result_null.setText("����ĳ�����Ϣ�������֤����������");
			} else {
				result_null.setText("��ϲ, û�в鵽Υ�¼�¼��");
			}

			result_title.setVisibility(View.GONE);
			result_list.setVisibility(View.GONE);
			result_null.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * title:��ֵ
	 * 
	 * @return
	 */
	private List getData() {
		List<WeizhangResponseHistoryJson> list = new ArrayList();

		for (WeizhangResponseHistoryJson weizhangResponseHistoryJson : info
				.getHistorys()) {
			WeizhangResponseHistoryJson json = new WeizhangResponseHistoryJson();
			json.setFen(weizhangResponseHistoryJson.getFen());
			json.setMoney(weizhangResponseHistoryJson.getMoney());
			json.setOccur_date(weizhangResponseHistoryJson.getOccur_date());
			json.setOccur_area(weizhangResponseHistoryJson.getOccur_area());
			json.setInfo(weizhangResponseHistoryJson.getInfo());
			list.add(json);
		}

		return list;
	}

}
