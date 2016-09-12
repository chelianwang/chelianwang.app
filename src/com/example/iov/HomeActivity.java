package com.example.iov;

import java.io.Serializable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheshouye.api.client.WeizhangClient;
import com.cheshouye.api.client.WeizhangIntentService;
import com.cheshouye.api.client.json.CarInfo;
import com.cheshouye.api.client.json.CityInfoJson;
import com.cheshouye.api.client.json.InputConfigJson;

import com.deahu.activity.ProvinceList;
import com.deahu.activity.ShortNameList;
import com.deahu.activity.WeizhangResult;


public class HomeActivity extends Activity{
   

	private String defaultChepai = "��"; // ��=�㶫

	private TextView short_name;
	private TextView query_city;
	private View btn_cpsz;
	private Button btn_query;

	private EditText chepai_number;
	private EditText chejia_number;
	private EditText engine_number;
	

	// ��ʻ֤ͼʾ
	private View popXSZ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_home);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.csy_titlebar);

		// ����
		//TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		//txtTitle.setText("����Υ�²�ѯ");

		// ********************************************************
		Log.d("��ʼ���������","");
		Intent weizhangIntent = new Intent(this, WeizhangIntentService.class);
		weizhangIntent.putExtra("appId",2114);// ����appId
		weizhangIntent.putExtra("appKey", "	fc86e683bb06b4c249c2ad9f6dbf15b0");// ����appKey
		startService(weizhangIntent);
		// ********************************************************

		// ѡ��ʡ����д
		query_city = (TextView) findViewById(R.id.cx_city);
		chepai_number = (EditText) findViewById(R.id.chepai_number);
		chejia_number = (EditText) findViewById(R.id.chejia_number);
		engine_number = (EditText) findViewById(R.id.engine_number);
		short_name = (TextView) findViewById(R.id.chepai_sz);

		// ----------------------------------------------

		btn_cpsz = (View) findViewById(R.id.btn_cpsz);
		btn_query = (Button) findViewById(R.id.btn_query);

		btn_cpsz.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(HomeActivity.this, ShortNameList.class);
				intent.putExtra("select_short_name", short_name.getText());
				startActivityForResult(intent, 0);
			}
		});

		query_city.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(HomeActivity.this, ProvinceList.class);
				startActivityForResult(intent, 1);
			}
		});

		btn_query.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// ��ȡΥ����Ϣ
				CarInfo car = new CarInfo();
				String quertCityStr = null;
				String quertCityIdStr = null;

				final String shortNameStr = short_name.getText().toString()
						.trim();
				final String chepaiNumberStr = chepai_number.getText()
						.toString().trim();
				if (query_city.getText() != null
						&& !query_city.getText().equals("")) {
					quertCityStr = query_city.getText().toString().trim();

				}

				if (query_city.getTag() != null
						&& !query_city.getTag().equals("")) {
					quertCityIdStr = query_city.getTag().toString().trim();
					car.setCity_id(Integer.parseInt(quertCityIdStr));
				}
				final String chejiaNumberStr = chejia_number.getText()
						.toString().trim();
				final String engineNumberStr = engine_number.getText()
						.toString().trim();

				Intent intent = new Intent();

				car.setChejia_no(chejiaNumberStr);
				car.setChepai_no(shortNameStr + chepaiNumberStr);

				car.setEngine_no(engineNumberStr);

				Bundle bundle = new Bundle();
				bundle.putSerializable("carInfo", (Serializable) car);
				intent.putExtras(bundle);

				boolean result = checkQueryItem(car);

				if (result) {
					intent.setClass(HomeActivity.this, WeizhangResult.class);
					startActivity(intent);

				}
			}
		});

		// ����Ĭ�ϲ�ѯ�س���id, ��ʼ����ѯ��Ŀ
		// setQueryItem(defaultCityId, defaultCityName);
		short_name.setText(defaultChepai);

		// ��ʾ������ʻ֤ͼʾ
		popXSZ = (View) findViewById(R.id.popXSZ);
		popXSZ.setOnTouchListener(new popOnTouchListener());
		hideShowXSZ();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;

		switch (requestCode) {
		case 0:
			Bundle bundle = data.getExtras();
			String ShortName = bundle.getString("short_name");
			short_name.setText(ShortName);
			break;
		case 1:
			Bundle bundle1 = data.getExtras();
			// String cityName = bundle1.getString("city_name");
			String cityId = bundle1.getString("city_id");
			// query_city.setText(cityName);
			// query_city.setTag(cityId);
			// InputConfigJson inputConfig =
			// WeizhangClient.getInputConfig(Integer.parseInt(cityId));
			// System.out.println(inputConfig.toJson());
			setQueryItem(Integer.parseInt(cityId));

			break;
		}
	}

	// ���ݳ��е��������ò�ѯ��Ŀ
	private void setQueryItem(int cityId) {

		InputConfigJson cityConfig = WeizhangClient.getInputConfig(cityId);

		// û�г�ʼ����ɵ�ʱ��;
		if (cityConfig != null) {
			CityInfoJson city = WeizhangClient.getCity(cityId);

			query_city.setText(city.getCity_name());
			query_city.setTag(cityId);

			int len_chejia = cityConfig.getClassno();
			int len_engine = cityConfig.getEngineno();

			View row_chejia = (View) findViewById(R.id.row_chejia);
			View row_engine = (View) findViewById(R.id.row_engine);

			// ���ܺ�
			if (len_chejia == 0) {
				row_chejia.setVisibility(View.GONE);
			} else {
				row_chejia.setVisibility(View.VISIBLE);
				setMaxlength(chejia_number, len_chejia);
				if (len_chejia == -1) {
					chejia_number.setHint("�������������ܺ�");
				} else if (len_chejia > 0) {
					chejia_number.setHint("�����복�ܺź�" + len_chejia + "λ");
				}
			}

			// ��������
			if (len_engine == 0) {
				row_engine.setVisibility(View.GONE);
			} else {
				row_engine.setVisibility(View.VISIBLE);
				setMaxlength(engine_number, len_engine);
				if (len_engine == -1) {
					engine_number.setHint("��������������������");
				} else if (len_engine > 0) {
					engine_number.setHint("�����뷢������" + len_engine + "λ");
				}
			}
		}
	}

	// �ύ�����
	private boolean checkQueryItem(CarInfo car) {
		if (car.getCity_id() == 0) {
			Toast.makeText(HomeActivity.this, "��ѡ���ѯ��", 0).show();
			return false;
		}

		if (car.getChepai_no().length() != 7) {
			Toast.makeText(HomeActivity.this, "������ĳ��ƺ�����", 0).show();
			return false;
		}

		if (car.getCity_id() > 0) {
			InputConfigJson inputConfig = WeizhangClient.getInputConfig(car
					.getCity_id());
			int engineno = inputConfig.getEngineno();
			int registno = inputConfig.getRegistno();
			int classno = inputConfig.getClassno();

			// ���ܺ�
			if (classno > 0) {
				if (car.getChejia_no().equals("")) {
					Toast.makeText(HomeActivity.this, "���복�ܺŲ�Ϊ��", 0).show();
					return false;
				}

				if (car.getChejia_no().length() != classno) {
					Toast.makeText(HomeActivity.this, "���복�ܺź�" + classno + "λ",
							0).show();
					return false;
				}
			} else if (classno < 0) {
				if (car.getChejia_no().length() == 0) {
					Toast.makeText(HomeActivity.this, "����ȫ�����ܺ�", 0).show();
					return false;
				}
			}

			//������
			if (engineno > 0) {
				if (car.getEngine_no().equals("")) {
					Toast.makeText(HomeActivity.this, "���뷢�����Ų�Ϊ��", 0).show();
					return false;
				}

				if (car.getEngine_no().length() != engineno) {
					Toast.makeText(HomeActivity.this,
							"���뷢�����ź�" + engineno + "λ", 0).show();
					return false;
				}
			} else if (engineno < 0) {
				if (car.getEngine_no().length() == 0) {
					Toast.makeText(HomeActivity.this, "����ȫ����������", 0).show();
					return false;
				}
			}

			// ע��֤����
			if (registno > 0) {
				if (car.getRegister_no().equals("")) {
					Toast.makeText(HomeActivity.this, "����֤���Ų�Ϊ��", 0).show();
					return false;
				}
				
				if (car.getRegister_no().length() != registno) {
					Toast.makeText(HomeActivity.this,
							"����֤���ź�" + registno + "λ", 0).show();
					return false;
				}
			} else if (registno < 0) {
				if (car.getRegister_no().length() == 0) {
					Toast.makeText(HomeActivity.this, "����ȫ��֤����", 0).show();
					return false;
				}
			}
			return true;
		}
		return false;

	}

	// ����/ȡ����󳤶�����
	private void setMaxlength(EditText et, int maxLength) {
		if (maxLength > 0) {
			et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
					maxLength) });
		} else { // ������
			et.setFilters(new InputFilter[] {});
		}
	}

	// ��ʾ������ʻ֤ͼʾ
	private void hideShowXSZ() {
		View btn_help1 = (View) findViewById(R.id.ico_chejia);
		View btn_help2 = (View) findViewById(R.id.ico_engine);
		Button btn_closeXSZ = (Button) findViewById(R.id.btn_closeXSZ);

		btn_help1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				popXSZ.setVisibility(View.VISIBLE);
			}
		});
		btn_help2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				popXSZ.setVisibility(View.VISIBLE);
			}
		});
		btn_closeXSZ.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				popXSZ.setVisibility(View.GONE);
			}
		});
	}

	// ���⴩͸���±�Ԫ��ȡ�ý���
	private class popOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			popXSZ.setVisibility(View.GONE);
			return true;
		}
	}
}
