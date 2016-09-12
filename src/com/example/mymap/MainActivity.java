package com.example.mymap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import com.example.beans.EndInfo;
import com.example.beans.Info;
import com.example.beans.SearchInfo;
import com.example.beans.StartInfo;
import com.example.iov.BuyActivity;
import com.example.iov.R;
import com.example.sensor.MyOrientationListener;
import com.example.sensor.MyOrientationListener.onOrientationListener;
import com.example.tools.httputil;
import com.example.tools.SystemStatusManager;



public class MainActivity extends Activity implements OnClickListener,OnMarkerClickListener,OnMapClickListener{
	private MapView myMapView;
	private BaiduMap myBaiduMap;
	private float current;
	private View defaultBaiduMapScaleButton=null;
	private View defaultBaiduMapLogo=null;
	private View defaultBaiduMapScaleUnit=null;
	private String[] types={"��ͨ��ͼ","���ǵ�ͼ","������ͼ(�ѹر�)"};
	private ImageView mapRoad,selectMapType,mapPanorama;
	private ImageView addScale,lowScale;
	private ImageView myLoaction;
	private ImageView selectLocationMode;
	private ImageView bus;
	private TextView locationText;
	//��λ���
	private LocationClient myLocationClient;
	private MyLocationListener myListener;
	private String[] LocationModeString={"����ģʽ","��ͨģʽ","����ģʽ","3D����ģʽ(�ѹر�)"};
	private boolean isFirstIn=true;
	private double latitude,longtitude,latjd,longwd;//����ľ��Ⱥ�γ��
	private String locationTextString;//�����λ�õ���Ϣ
	private BitmapDescriptor myBitmapLocation;
	private MyOrientationListener myOrientationListener;
	private float myCurrentX;
	//���������
	private ImageView addMarkers;
	private BitmapDescriptor myMark;
	private LinearLayout markLayout;
	private List<Info>marks;
	private boolean flag=true;
	ArrayList<LatLng> latLnging ;
	private LatLng currentLatLng;
	//����
	//private SearchInfo startinfo=null;
	
	private StartInfo startinfo=new StartInfo("",0,0,"","","");
	private EndInfo endinfo=new EndInfo("",0,0,"","","");
	private EditText searchEdit;
	private ImageView okToSearch;
	private List<SearchInfo> searchInfoLists;
	//ȫ��
	private String uid;
	//��·����
	private ImageButton startGo;
	private BitmapDescriptor bitmapDescriptor;
	private List<BitmapDescriptor> bitmapDescriptors;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext  
		//ע��÷���Ҫ��setContentView����֮ǰʵ�� 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setTranslucentStatus();
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		initMapView();
		changeDefaultBaiduMapView();
		initMapLocation();
		initMapListener();
		initSearchDestination();
	}
//������������API�õ���JSON����
	/**
	 * @author zhongqihong
	 * �������߳������еõ����������ݣ�����Handler������
	 * ���߳�(��UI�߳�)
	 * */
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			latLnging = new ArrayList<LatLng>();
			if (msg.what==0x1234) {
				JSONObject object=(JSONObject) msg.obj;
				//toast("json:----->"+object.toString());
				//������ʼ:Ȼ���ÿһ���ص���Ϣ��װ��SearchInfo����
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
						
						LatLng cLatLng = new LatLng(lat, lng);
						latLnging.add(cLatLng);
						
						searchInfoLists.add(mInfo);
						
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 myBaiduMap.clear();
				//displayInDialog();
				 currentLatLng = new LatLng(latjd, longwd);//�������ĵ㣬��ǰѡ��λ��
			     MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(currentLatLng, 16);
				 myBaiduMap.setMapStatus(mapStatusUpdate);	
				 drawMarker(latLnging);
				
			}
			//����վ����
			if (msg.what==0x1231){
				JSONObject object=(JSONObject) msg.obj;
				//toast("json:----->"+object.toString());
				//������ʼ:Ȼ���ÿһ���ص���Ϣ��װ��SearchInfo����
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
						
						LatLng cLatLng = new LatLng(lat, lng);
						latLnging.add(cLatLng);
						
						searchInfoLists.add(mInfo);
						
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 currentLatLng = new LatLng(latjd, longwd);//�������ĵ㣬��ǰѡ��λ��
				//myBaiduMap.clear();
				MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(currentLatLng, 16);
				myBaiduMap.setMapStatus(mapStatusUpdate);	
				drawMarker(latLnging);
			}
			/*if(latLnging.size!=0){
			LatLng currentLatLng = new LatLng(latitude, longtitude);
			myBaiduMap.clear();
			MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(currentLatLng, 16);
			myBaiduMap.setMapStatus(mapStatusUpdate);	
			drawMarker(latLnging);
			}
			else{
				displayInDialog();
			}*/
		}
		/**
		 * 
		 * ��ʾ��������Ϣ���Զ����б���Ի����Լ��Ի������¼��Ĵ���
		 * */
		private void displayInDialog() {
			if (searchInfoLists!=null) {
				AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
				builder.setIcon(R.drawable.calibration_arrow)
				.setTitle("��ѡ�����ѯ���ĵص�")
				.setAdapter(new myDialogListAdapter(), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						final SearchInfo mInfos=searchInfoLists.get(which);
						uid=mInfos.getUid();
						myBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
							@Override
							public boolean onMarkerClick(Marker PnoramaMark) {
								//double jd=mInfos.getLatitude();
								//double wd=mInfos.getLongtiude();
								//getMyLatestLocation(jd,wd);
								Intent intent=new Intent(MainActivity.this, OtherPnoramaActivity.class);
								intent.putExtra("info", mInfos);
								startActivity(intent);
								return true;
							}
						});
						addPnoramaLayout(mInfos);//
					}
				}).show();
			}else{
				toast("δ��ѯ����صص�");
			}
		}
	};
	/**
	 * @author mikyou
	 * ���ȫ���������ȫ����ͼ�꣬Ѹ�ٶ�λ���õص��ڵ�ͼ�ϵ�λ��
	 * */
	public void addPnoramaLayout(SearchInfo mInfos) {
		myBaiduMap.clear();	
		LatLng latLng=new LatLng(mInfos.getLatitude(), mInfos.getLongtiude());
		Marker pnoramaMarker=null;
		OverlayOptions options;
		BitmapDescriptor mPnoramaIcon=BitmapDescriptorFactory.fromResource(R.drawable.icon_card_streetscape_blue);
		options=new MarkerOptions().position(latLng).icon(mPnoramaIcon).zIndex(6);
		pnoramaMarker=(Marker) myBaiduMap.addOverlay(options);
		MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);
		myBaiduMap.animateMapStatus(msu);
	}
	//��ͼ����
	public void initMapListener() {
        myBaiduMap.setOnMapClickListener(new OnMapClickListener() { 
			@Override
			public boolean onMapPoiClick(MapPoi mapPoi) {
				LatLng latLng = mapPoi.getPosition();
				endinfo.setLatitude(latLng.latitude);
				endinfo.setLongtiude(latLng.longitude);
				endinfo.setDesname(mapPoi.getName());
				latjd=latLng.latitude;
				longwd=latLng.longitude;
				currentLatLng = new LatLng(latitude, longtitude);
				String distance = String.valueOf("   ������"+(int)DistanceUtil.getDistance(currentLatLng, latLng))+"��";// ����POI����ǰλ�õľ���							
				
				locationText.setText(mapPoi.getName()+distance);
				
				myBaiduMap.clear();
				MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, 16);
				myBaiduMap.setMapStatus(mapStatusUpdate);	
				ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
				latLngs.add(latLng);
				drawMarker(latLngs);
				return true;
			}
			
			@Override
			public void onMapClick(LatLng latLng) {

			}
		});        
        
       
	}
	 public void drawMarker(ArrayList<LatLng> latLngs) {
	    	int count = latLngs.size();
	    	
	    	if (count == 1) {
	    		bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);
	    		OverlayOptions markerOptions = new MarkerOptions().position(latLngs.get(0)).icon(bitmapDescriptor);
	    		myBaiduMap.addOverlay(markerOptions);
	    	}
	    	if (count > 1) {
        	// ��ʼ��ȫ�� bitmap ��Ϣ������ʱ��ʱ recycle
          	   bitmapDescriptors = new ArrayList<BitmapDescriptor>();
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark1));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark2));
	           bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark3));
	           bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark4));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark5));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark6));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark7));
       	       bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark8));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark9));
	           bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark10));
	           bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark11));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark12));
       	       bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark13));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark14));
	           bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark15));
	           bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_markb));
       	       bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_markc));
        	   bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_markd));
	           bitmapDescriptors.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_marke));
			for (int i = 0; i < count; i++) {
					OverlayOptions markerOptions = new MarkerOptions()
							.position(latLngs.get(i))
						.icon(bitmapDescriptors.get(i)).zIndex(i)
							.title(searchInfoLists.get(i).getDesname())
							.perspective(true);
					    myBaiduMap.addOverlay(markerOptions);
					   
			}
				 myBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
					@Override
					public boolean onMarkerClick(Marker marker) {
						LatLng latLng = marker.getPosition();
						endinfo.setLatitude(latLng.latitude);
						endinfo.setLongtiude(latLng.longitude);
						endinfo.setDesname(marker.getTitle());
						LatLng currentLatLng = new LatLng(latitude, longtitude);
						String distance = String.valueOf("   ������"+(int)DistanceUtil.getDistance(currentLatLng, latLng))+"��";// ����POI����ǰλ�õľ���							
						
						locationText.setText(marker.getTitle()+distance);
						
						
						return true;
					}
				});
	    	} 
		}
	/**
	 * @author zhongqihong
	 * �������
	 * */
	private void initSearchDestination() {
		searchEdit=(EditText) findViewById(R.id.search_panorama);
		okToSearch=(ImageView) findViewById(R.id.ok_to_search);
		okToSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchInfoLists=new ArrayList<SearchInfo>();
				getSearchDataFromNetWork();
				
			}
			/**
			 * @author mikyou
			 * ����������������Ϣ���������õ�JSON����
			 * ����һ���߳�ȥ��ȡ��������
			 * getSearchDataFromNetWork
			 * */
			private void getSearchDataFromNetWork() {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							JSONObject jsonObject=httputil.send(searchEdit.getText().toString(),latjd,longwd, null);
							Message msg=new Message();
							msg.obj=jsonObject;
							msg.what=0x1234;
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
		});
	}
	/**
	 * @author zhongqihong
	 * ��������������Ϣ���������JSON���ݽ����󣬷�װ��һ��SearchInfo����
	 * Ȼ����Щ����չʾ��һ���Զ�����б���ĶԻ����У����¾�Ϊ�����б����������
	 * ListAdapter
	 * */
	class myDialogListAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return searchInfoLists.size();
		}

		@Override
		public Object getItem(int position) {
			return getItem(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SearchInfo mSearchInfo=searchInfoLists.get(position);
			View view=View.inflate(MainActivity.this, R.layout.dialog_list_item, null);
			TextView desnameTv=(TextView) view.findViewById(R.id.desname);
			TextView addressTv=(TextView) view.findViewById(R.id.address);
			desnameTv.setText(mSearchInfo.getDesname());
			addressTv.setText(mSearchInfo.getAddress());
			return view;
		}

	}

	/**
	 * @author zhongqihong
	 * �޸�Ĭ�ϰٶȵ�ͼ��View
	 * 
	 * */
	private void changeDefaultBaiduMapView() {
		changeInitialzeScaleView();//�ı�Ĭ�ϰٶȵ�ͼ��ʼ���صĵ�ͼ����
		//�����������ź�����İٶȵ�ͼ��Ĭ�ϵı�����ť
		for (int i = 0; i < myMapView.getChildCount(); i++) {//�����ٶȵ�ͼ�е�������View,�ҵ������������ŵİ�ť�ؼ�View��Ȼ����������View����
			View child=myMapView.getChildAt(i);
			if (child instanceof ZoomControls) {
				defaultBaiduMapScaleButton=child;//��defaultBaiduMapScaleButton��View��ָ�ٶȵ�ͼĬ�ϲ����ķŴ����С�İ�ť���õ����View
				break;
			}
		}
		defaultBaiduMapScaleButton.setVisibility(View.GONE);//Ȼ�󽫸�View��Visiblity��Ϊ�����ںͲ��ɼ���������
		defaultBaiduMapLogo =myMapView.getChildAt(1);//��View��ָ�ٶȵ�ͼ��Ĭ�ϵİٶȵ�ͼ��Logo,�õ����View
		defaultBaiduMapLogo.setPadding(300, -10, 100, 100);//���ø�Ĭ��Logo View��λ�ã���Ϊ�����View��λ�û�Ӱ������Ŀ̶ȳߵ�λView��ʾ��λ��
		myMapView.removeViewAt(1);//����Ƴ�Ĭ�ϰٶȵ�ͼ��logo View
		defaultBaiduMapScaleUnit=myMapView.getChildAt(2);//�õ��ٶȵ�ͼ��Ĭ�ϵ�λ�̶ȵ�View
		defaultBaiduMapScaleUnit.setPadding(100, 0, 115,200);//������õ����ٶȵ�ͼ��Ĭ�ϵ�λ�̶�View��λ��
	}
	/**
	 * @author zhongqihong
	 * �޸İٶȵ�ͼĬ�Ͽ�ʼ��ʼ�����ص�ͼ������С
	 * */
	private void changeInitialzeScaleView() {
		myBaiduMap=myMapView.getMap();//�ı�ٶȵ�ͼ�ķŴ����,���״μ��ص�ͼ�Ϳ�ʼ����500�׵ľ���
		MapStatusUpdate factory=MapStatusUpdateFactory.zoomTo(15.0f);
		myBaiduMap.animateMapStatus(factory);		
	}
	protected void onDestroy() {
		super.onDestroy();
		//��Activityִ��onDestoryʱִ��mapView(��ͼ)�������ڹ���
		myMapView.onDestroy();
	}
	@Override
	protected void onStart() {//��Activity����onStart������������λ�Լ��������򴫸�����������λ�ķ��񡢷��򴫸�����Activity�������ڰ���һ��
		myBaiduMap.setMyLocationEnabled(true);//��������λ
		if (!myLocationClient.isStarted()) {
			myLocationClient.start();//������λ
		}
		//�������򴫸���
		myOrientationListener.start();
		super.onStart();
	}
	@Override
	protected void onStop() {//��Activity����onStop�������رն�λ�Լ��رշ��򴫸���
		myBaiduMap.setMyLocationEnabled(false);
		myLocationClient.stop();//�رն�λ
		myOrientationListener.stop();//�رշ��򴫸���
		super.onStop();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//��Activityִ��onResume��ִ��MapView(��ͼ)�������ڹ���
		myMapView.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		myMapView.onPause();
	}
	/**
	 * @author zhongqihong
	 * ��ʼ����ͼ
	 * */
	private void initMapView() {
		registerAllViewsId();
		registerAllViewsEvent();
	}
	/**
	 * @author zhongqihong
	 * ע������View��ص�id
	 * */
	private void registerAllViewsId() {
		myMapView=(MapView) findViewById(R.id.mymap_view);
		mapRoad=(ImageView) findViewById(R.id.road_condition);
		selectMapType=(ImageView) findViewById(R.id.map_type);
		addScale=(ImageView) findViewById(R.id.add_scale);
		lowScale=(ImageView) findViewById(R.id.low_scale);
		myLoaction=(ImageView) findViewById(R.id.my_location);
		locationText=(TextView) findViewById(R.id.mylocation_text);
		selectLocationMode=(ImageView) findViewById(R.id.map_location);
		bus=(ImageView) findViewById(R.id.map_gotogether);
		addMarkers=(ImageView) findViewById(R.id.map_marker);
		markLayout=(LinearLayout) findViewById(R.id.mark_layout);
		mapPanorama=(ImageView) findViewById(R.id.map_panorama);
		startGo=(ImageButton) findViewById(R.id.start_go);
	}
	/**
	 * @author zhongqihong
	 * ע������View��ص��¼�
	 * */
	private void registerAllViewsEvent() {
		mapRoad.setOnClickListener(this);
		selectMapType.setOnClickListener(this);
		addScale.setOnClickListener(this);
		lowScale.setOnClickListener(this);
		myLoaction.setOnClickListener(this);
		selectLocationMode.setOnClickListener(this);
		addMarkers.setOnClickListener(this);
		mapPanorama.setOnClickListener(this);
		bus.setOnClickListener(this);
		startGo.setOnClickListener(this);
	}
	/**
	 * @author zhongqihong
	 * ��ʼ����λ
	 * */
	private void initMapLocation() {
		myLocationClient=new LocationClient(this);//����һ����λ�ͻ��˶���
		myListener=new MyLocationListener();//����һ����λ�¼���������
		myLocationClient.registerLocationListener(myListener);//�����ö�λ�ͻ��˶���ע������¼�
		//��LocaitonClient����һЩ��Ҫ������
		LocationClientOption option=new LocationClientOption();
		option.setCoorType("bd09ll");//�������������
		option.setIsNeedAddress(true);//���ص�ǰ��λ����Ϣ�����������Ϊtrue��Ĭ�Ͼ�Ϊfalse�����޷����λ�õ���Ϣ
		option.setOpenGps(true);//��GPS
		option.setScanSpan(1000);//��ʾ1s�н���һ�ζ�λ����
		myLocationClient.setLocOption(option);
		useLocationOrientationListener();//���÷��򴫸���
	}
	/**
	 * @author zhongqihong
	 * ��λ��Ϸ��򴫸������Ӷ�����ʵʱ��⵽X������ı仯���Ӷ��Ϳ��Լ�⵽
	 * ��λͼ�귽��仯��ֻ��Ҫ�������̬�仯��X����������myCurrentXֵ��
	 * �����MyLocationData data.driection(myCurrentX);
	 * */
	private void useLocationOrientationListener() {
		myOrientationListener=new MyOrientationListener(MainActivity.this);
		myOrientationListener.setMyOrientationListener(new onOrientationListener() {
			public void onOrientationChanged(float x) {//��������ĸı䣬����ı�ʱ����Ҫ�õ���ͼ�Ϸ���ͼ���λ��
				myCurrentX=x;
				
			}
		});		
	}
	/**
	 * @author zhongqihong
	 * ������¶�λ��λ��,���ҵ�ͼ�����ĵ�����Ϊ�ҵ�λ��
	 * */
	private void getMyLatestLocation(double lat,double lng) {
		LatLng latLng=new LatLng(lat, lng);
		latjd=latLng.latitude;
		longwd=latLng.longitude;//����һ����γ�ȶ�����Ҫ���뵱ǰ�ľ��Ⱥ�γ����������ֵ����
		MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);//����һ����ͼ���¸��µ�״̬������Ҫ����һ�����¾�γ�ȶ���
		myBaiduMap.animateMapStatus(msu);//��ʾʹ�ö�����Ч�����룬ͨ������һ����ͼ����״̬����Ȼ�����ðٶȵ�ͼ������չ�ֺͻ�ԭ�Ǹ���ͼ����״̬������ʱ�ĵ�ͼ��ʾ��Ϊ�����ڵ�λ��
	}
	/**
	 * @author zhongqihong
	 * BDLocationListener��ʵ����:MyLocationListener
	 * */
	private class MyLocationListener implements BDLocationListener{
		@Override
		public void onReceiveLocation(BDLocation 
				location) {
			
			//�õ�һ��MyLocationData������Ҫ��BDLocation����ת����MyLocationData����
			MyLocationData data=new MyLocationData.Builder()
			.accuracy(location.getRadius())//���Ȱ뾶
			.direction(myCurrentX)//����
			.latitude(location.getLatitude())//����
			.longitude(location.getLongitude())//γ��
			.build();
			myBaiduMap.setMyLocationData(data);
			//�����Զ���Ķ�λͼ��,��Ҫ�ڽ�����setMyLocationData��������
			//�����Զ��嶨λͼ��
			changeLocationIcon();
			latitude=location.getLatitude();//�õ���ǰ�ľ���
			longtitude=location.getLongitude();//�õ���ǰ��γ��
			latjd=latitude;
			longwd=longtitude;
			startinfo.setDesname(location.getAddrStr());
			startinfo.setLatitude(latitude);
			startinfo.setLongtiude(longtitude);
			
			//toast("���ȣ�"+latitude+"     γ��:"+longtitude);
			if (isFirstIn) {//��ʾ�û���һ�δ򿪣��Ͷ�λ���û���ǰλ�ã�����ʱֻҪ����ͼ�����ĵ�����Ϊ�û���ʱ��λ�ü���

				getMyLatestLocation(latitude,longtitude);//������¶�λ��λ��,���ҵ�ͼ�����ĵ�����Ϊ�ҵ�λ��
				isFirstIn=false;//��ʾ��һ�βŻ�ȥ��λ�����ĵ�
				locationTextString=""+location.getAddrStr();//����õ���ַ������Ҫ������LocationOption��ʱ����Ҫ����isNeedAddressΪtrue;
				toast(locationTextString);
				locationText.setText(locationTextString);
			}
		}
	}
	/**
	 * @author zhongqihong
	 * �Զ��嶨λͼ��
	 * */
	private void changeLocationIcon() {
		myBitmapLocation=BitmapDescriptorFactory
				.fromResource(R.drawable.icon_landing_arrow);//�����Լ���ͼ��
		if (isFirstIn) {//��ʾ��һ�ζ�λ��ʾ��ͨģʽ
			MyLocationConfiguration config=new 
					MyLocationConfiguration(LocationMode.NORMAL, true, myBitmapLocation);
			myBaiduMap.setMyLocationConfigeration(config);
		}			
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.road_condition:
			if (myBaiduMap.isTrafficEnabled()) {
				myBaiduMap.setTrafficEnabled(false);
				mapRoad.setImageResource(R.drawable.main_icon_roadcondition_off);
			}else{
				myBaiduMap.setTrafficEnabled(true);
				mapRoad.setImageResource(R.drawable.main_icon_roadcondition_on);
			}
			break;
		case R.id.map_type:
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setIcon(R.drawable.icon)
			.setTitle("��ѡ���ͼ������")
			.setItems(types, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String select=types[which];
					if (select.equals("��ͨ��ͼ")) {
						myBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
					}else if (select.equals("���ǵ�ͼ")) {
						myBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
					}else if (select.equals("������ͼ(�ѹر�)")||select.equals("������ͼ(�Ѵ�)")) {
						if(myBaiduMap.isBaiduHeatMapEnabled()) {
							myBaiduMap.setBaiduHeatMapEnabled(false);
							Toast.makeText(MainActivity.this, "������ͼ�ѹر�", 0).show();
							types[which]="������ͼ(�ѹر�)";
						}else {
							myBaiduMap.setBaiduHeatMapEnabled(true);
							Toast.makeText(MainActivity.this, "������ͼ�Ѵ�", 0).show();
							types[which]="������ͼ(�Ѵ�)";
						}
					}
				}
			}).show();
			break;
		case R.id.add_scale:
			current+=0.5f;
			MapStatusUpdate 
			factory=MapStatusUpdateFactory.zoomTo(15.0f+current);
			myBaiduMap.animateMapStatus(factory);//���ðٶȵ�ͼ������Ч������animateMapStatus
			break;
		case R.id.low_scale:
			current-=0.5f;
			MapStatusUpdate 
			factory2=MapStatusUpdateFactory.zoomTo(15.0f+current);
			myBaiduMap.animateMapStatus(factory2);
			break;
		case R.id.my_location://��λ���ܣ���Ҫ�õ�LocationClient���ж�λ
			//BDLocationListener
			myBaiduMap.clear();
			getMyLatestLocation(latitude,longtitude);//��ȡ���µ�λ��
			break;
		case R.id.map_location:
			AlertDialog.Builder builder2=new AlertDialog.Builder(this);
			builder2.setIcon(R.drawable.track_collect_running)
			.setTitle("��ѡ��λ��ģʽ")
			.setItems(LocationModeString, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String mode=LocationModeString[which];
					if (mode.equals("����ģʽ")) {
						MyLocationConfiguration config=new 
								MyLocationConfiguration(LocationMode.COMPASS, true, myBitmapLocation);
						myBaiduMap.setMyLocationConfigeration(config);
					}else if (mode.equals("����ģʽ")) {
						MyLocationConfiguration config=new 
								MyLocationConfiguration(LocationMode.FOLLOWING, true, myBitmapLocation);
						myBaiduMap.setMyLocationConfigeration(config);
					}else if (mode.equals("��ͨģʽ")) {
						MyLocationConfiguration config=new 
								MyLocationConfiguration(LocationMode.NORMAL, true, myBitmapLocation);
						myBaiduMap.setMyLocationConfigeration(config);
					}else if (mode.equals("3D����ģʽ(�ѹر�)")||mode.equals("3D����ģʽ(�Ѵ�)")) {
						if (mode.equals("3D����ģʽ(�Ѵ�)")) {
							UiSettings	 mUiSettings = myBaiduMap.getUiSettings();
							mUiSettings.setCompassEnabled(true);
							LocationModeString[which]="3D����ģʽ(�ѹر�)";
							toast("3Dģʽ�ѹر�");
						}else{
							MyLocationConfiguration config=new 
									MyLocationConfiguration(LocationMode.COMPASS, true, myBitmapLocation);
							myBaiduMap.setMyLocationConfigeration(config);
							MyLocationConfiguration config2=new 
									MyLocationConfiguration(LocationMode.NORMAL, true, myBitmapLocation);
							myBaiduMap.setMyLocationConfigeration(config2);
							LocationModeString[which]="3D����ģʽ(�Ѵ�)";
							toast("3Dģʽ�Ѵ�");
						}
					}
				}
			}).show();
			break;
		case R.id.map_marker://������Ϣ����
			Intent intent3=new Intent(MainActivity.this, BuyActivity.class);
			intent3.putExtra("gasinfo", endinfo);
			startActivity(intent3);
			break;
		case R.id.map_panorama:
			Intent intent=new Intent(MainActivity.this, PanoramaActivity.class);
			intent.putExtra("panoramaLatLng", new double []{latitude,longtitude});
			startActivity(intent);
			break;
		case R.id.map_gotogether://��������վ
			
			searchInfoLists=new ArrayList<SearchInfo>();
			getsearchDataFromNetWork();
			break;
		case R.id.start_go://����
			Intent intent2=new Intent(MainActivity.this, NaViPathActivity.class);
			intent2.putExtra("startwd", startinfo);
			intent2.putExtra("endwd", endinfo);
			startActivity(intent2);
			break;
		default:
			break;
		}
	}
	//����վ����
	private void getsearchDataFromNetWork() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject jsonObject=httputil.send("����",latjd,longwd, null);
					Message msg=new Message();
					msg.obj=jsonObject;
					msg.what=0x1231;
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
	/**
	 * @author zhongqihong
	 * ��Ӹ����ﵽͼ����(Ҳ���ǽ���������ӵ���ͼ����)
	 * */
	private void addOverLayer() {
		myBaiduMap.clear();//�����һ��ͼ��
		LatLng latLng=null;
		Marker marker=null;
		OverlayOptions options;
		myMark=BitmapDescriptorFactory.fromResource(R.drawable.mark);//�����Զ���ĸ�����ͼ�꣬����ת����һ��BitmapDescriptor����
		//����Info��Listһ��Info����һ��Mark
		for (int i = 0; i < marks.size(); i++) {
			//��γ�ȶ���
			latLng=new LatLng(marks.get(i).getLatitude(), marks.get(i).getLongitude());
			//ͼ��
			options=new MarkerOptions().position(latLng).icon(myMark).zIndex(6);
			marker=(Marker) myBaiduMap.addOverlay(options);
			Bundle bundle=new Bundle();
			bundle.putSerializable("mark", marks.get(i));
			marker.setExtraInfo(bundle);
		}
		MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);
		myBaiduMap.animateMapStatus(msu);
	}

	/**
	 * @author zhongqihong
	 * ��ʼ��������
	 * ��ֱ��������ȣ�Խ���Ͼ���Խ����ԽС
	 * ˮƽ�������γ�ȣ�Խ����γ��Խ����ԽС
	 * */
	private void initMapMarks() {
		marks=new ArrayList<Info>();//�����еĸ��������Ϣ�ķ�װ��һ��List<Info>������
		marks.add(new Info(32.079254, 118.787623, R.drawable.pic1, "Ӣ�׹���С�ù�", "����209��", 1888));
		marks.add(new Info(32.064355, 118.787624, R.drawable.pic2, "ɳ�����ʸ߼�����", "����459��", 388));
		marks.add(new Info(28.7487420000, 115.8748860000, R.drawable.pic4, "������ͨ��ѧ����", "����5��", 888));
		marks.add(new Info(28.7534890000, 115.8767960000, R.drawable.pic3, "������ͨ��ѧ����", "����10��", 188));
		myBaiduMap.setOnMarkerClickListener(this);
		myBaiduMap.setOnMapClickListener(this);	
	}
	public void toast(String str){
		Toast.makeText(MainActivity.this, str, 0).show();
	}
	/**
	 * @author zhongqihong
	 * ��Ӻõĸ�����ĵ���¼�
	 * */
	@Override
	public boolean onMarkerClick(Marker marker) {//��ʾ�ѵ����ĸ����������
		Bundle bundle=  marker.getExtraInfo();
		Info MyMarker=(Info) bundle.getSerializable("mark");
		ImageView iv=(ImageView) markLayout.findViewById(R.id.mark_image);
		TextView distanceTv=(TextView) markLayout.findViewById(R.id.distance);
		TextView nameTv=(TextView) markLayout.findViewById(R.id.name);
		TextView zanNumsTv=(TextView) markLayout.findViewById(R.id.zan_nums);
		iv.setImageResource(MyMarker.getImageId());
		distanceTv.setText(MyMarker.getDistance()+"");
		nameTv.setText(MyMarker.getName());
		zanNumsTv.setText(MyMarker.getZanNum()+"");
		//��ʼ��һ��InfoWindow
		initInfoWindow(MyMarker,marker);
		markLayout.setVisibility(View.VISIBLE);
		return true;
	}
	/**
	 *@author zhongqihong
	 *��ʼ����һ��InfoWindow
	 * 
	 * */
	private void initInfoWindow(Info MyMarker,Marker marker) {
		// TODO Auto-generated method stub
		InfoWindow infoWindow;
		//InfoWindow����ʾ��View������ʽ����ʾһ��TextView
		TextView infoWindowTv=new TextView(MainActivity.this);
		infoWindowTv.setBackgroundResource(R.drawable.location_tips);
		infoWindowTv.setPadding(30, 20, 30, 50);
		infoWindowTv.setText(MyMarker.getName());
		infoWindowTv.setTextColor(Color.parseColor("#FFFFFF"));

		final LatLng latLng=marker.getPosition();
		Point p=myBaiduMap.getProjection().toScreenLocation(latLng);//����ͼ�ϵľ�γ��ת������Ļ��ʵ�ʵĵ�
		p.y-=47;//������Ļ�е��Y�������ƫ����
		LatLng ll=myBaiduMap.getProjection().fromScreenLocation(p);//���޸ĺ����Ļ�ĵ���ת���ɵ�ͼ�ϵľ�γ�ȶ���
		/**
		 * @author zhongqihong
		 * ʵ����һ��InfoWindow�Ķ���
		 * public InfoWindow(View view,LatLng position, int yOffset)ͨ������� view ����һ�� InfoWindow, ��ʱֻ�����ø�view����һ��Bitmap�����ڵ�ͼ�У������¼��ɿ�����ʵ�֡�
		 *	����:
		 * view - InfoWindow չʾ�� view
		 * position - InfoWindow ��ʾ�ĵ���λ��
		 * yOffset - InfoWindow Y ��ƫ����
		 * */
		infoWindow=new InfoWindow(infoWindowTv, ll, 10);
		myBaiduMap.showInfoWindow(infoWindow);//��ʾInfoWindow
	}
	/**
	 * @author zhongqihong
	 * ��������ͼ��ӵĵ���¼�
	 * */
	@Override
	public void onMapClick(LatLng arg0) {//��ʾ�����ͼ�����ĵط�ʹ�ø������������ܵĲ������أ����ǵ������ʾ�ĸ��������鲼���ϣ��򲻻���ʧ����Ϊ�����鲼���������Clickable=true
		//�����¼��Ĵ������ƣ���Ϊ����¼����Ȼ��ڸ����ﲼ�ֵĸ�����(map)��,����map�ǿ��Ե���ģ�map���ѵ���¼������ѵ����������Clickable=true��ʾ����¼������鲼���Լ���������map������
		markLayout.setVisibility(View.GONE);
		myBaiduMap.hideInfoWindow();//����InfoWindow
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
