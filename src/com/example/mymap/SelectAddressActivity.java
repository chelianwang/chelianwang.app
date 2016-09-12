package com.example.mymap;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.adapter.MySearchContentListAdapter;
import com.example.beans.SearchInfo;
import com.example.iov.R;
import com.example.tools.HttpUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



public class SelectAddressActivity extends Activity implements OnClickListener{
	private EditText mSearchContent;
	private Button mOkBtn;
	private ArrayList<SearchInfo> searchInfoLists;
	private ListView mSearchContentList;
	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			if (msg.what==0x123) {
				JSONObject object=(JSONObject) msg.obj;
				//toast("json:----->"+object.toString());
				//解析开始:然后把每一个地点信息封装到SearchInfo类中
				try {
					JSONArray array=object.getJSONArray("results");
					for (int i = 0; i < array.length(); i++) {
						JSONObject joObject=array.getJSONObject(i);
						String streetIds;
						String address;
						String name=joObject.getString("name");
						JSONObject  object2=joObject.getJSONObject("location");
						double lat=object2.getDouble("lat");
						double lng=object2.getDouble("lng");
						if (!joObject.isNull("address")) {

							address = joObject.getString("address");
						} else {
							address = "";
						}

						if (!joObject.isNull("street_id")) {
							streetIds = joObject.getString("street_id");
						} else {
							streetIds = "";
						}
						String uids=joObject.getString("uid");
						SearchInfo mInfo=new SearchInfo(name, lat, lng, address, streetIds, uids);
						searchInfoLists.add(mInfo);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mSearchContentList.setAdapter(new MySearchContentListAdapter(searchInfoLists,SelectAddressActivity.this));
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_select_address);
		initSelectAddress(); 
	}

	private void initSelectAddress() {
		registerAllViewId();
		reggisterAllViewEvent();
	}

	private void reggisterAllViewEvent() {
		mOkBtn.setOnClickListener(this);
		mSearchContentList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				    Intent intent=getIntent();
				    intent.putExtra("info", searchInfoLists.get(position));
				    setResult(0,intent);
				    finish();
			}
		});
	}

	private void registerAllViewId() {
		mSearchContent=(EditText) findViewById(R.id.search_content);
		mOkBtn=(Button) findViewById(R.id.ok_btn);
		mSearchContentList=(ListView) findViewById(R.id.show_search_content);
	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok_btn:
			searchInfoLists=new ArrayList<SearchInfo>();
			getSearchDataFromNetWork();
			break;

		default:
			break;
		}		
	}
	/**
	 * @author zhongqihong
	 * 根据输入搜索的信息，从网络获得的JSON数据
	 * 开启一个线程去获取网络数据
	 * getSearchDataFromNetWork
	 * */
	private void getSearchDataFromNetWork() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject jsonObject=HttpUtils.send(mSearchContent.getText().toString(), null);
					Message msg=new Message();
					msg.obj=jsonObject;
					msg.what=0x123;
					handler.sendMessage(msg);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();				
	}
	public void toast(String str){
		Toast.makeText(SelectAddressActivity.this, str, 0).show();
	}
}
