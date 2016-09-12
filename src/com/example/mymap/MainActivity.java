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
	private String[] types={"普通地图","卫星地图","热力地图(已关闭)"};
	private ImageView mapRoad,selectMapType,mapPanorama;
	private ImageView addScale,lowScale;
	private ImageView myLoaction;
	private ImageView selectLocationMode;
	private ImageView bus;
	private TextView locationText;
	//定位相关
	private LocationClient myLocationClient;
	private MyLocationListener myListener;
	private String[] LocationModeString={"罗盘模式","普通模式","跟随模式","3D俯视模式(已关闭)"};
	private boolean isFirstIn=true;
	private double latitude,longtitude,latjd,longwd;//定义的经度和纬度
	private String locationTextString;//定义的位置的信息
	private BitmapDescriptor myBitmapLocation;
	private MyOrientationListener myOrientationListener;
	private float myCurrentX;
	//覆盖物相关
	private ImageView addMarkers;
	private BitmapDescriptor myMark;
	private LinearLayout markLayout;
	private List<Info>marks;
	private boolean flag=true;
	ArrayList<LatLng> latLnging ;
	private LatLng currentLatLng;
	//搜索
	//private SearchInfo startinfo=null;
	
	private StartInfo startinfo=new StartInfo("",0,0,"","","");
	private EndInfo endinfo=new EndInfo("",0,0,"","","");
	private EditText searchEdit;
	private ImageView okToSearch;
	private List<SearchInfo> searchInfoLists;
	//全景
	private String uid;
	//线路导航
	private ImageButton startGo;
	private BitmapDescriptor bitmapDescriptor;
	private List<BitmapDescriptor> bitmapDescriptors;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext  
		//注意该方法要再setContentView方法之前实现 
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
//搜索网络请求API得到的JSON数据
	/**
	 * @author zhongqihong
	 * 利用子线程请求中得到的网络数据，利用Handler来更新
	 * 主线程(即UI线程)
	 * */
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			latLnging = new ArrayList<LatLng>();
			if (msg.what==0x1234) {
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
				 currentLatLng = new LatLng(latjd, longwd);//设置中心点，当前选中位置
			     MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(currentLatLng, 16);
				 myBaiduMap.setMapStatus(mapStatusUpdate);	
				 drawMarker(latLnging);
				
			}
			//加油站搜索
			if (msg.what==0x1231){
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
						
						LatLng cLatLng = new LatLng(lat, lng);
						latLnging.add(cLatLng);
						
						searchInfoLists.add(mInfo);
						
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 currentLatLng = new LatLng(latjd, longwd);//设置中心点，当前选中位置
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
		 * 显示搜索后信息的自定义列表项对话框，以及对话框点击事件的处理
		 * */
		private void displayInDialog() {
			if (searchInfoLists!=null) {
				AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
				builder.setIcon(R.drawable.calibration_arrow)
				.setTitle("请选择你查询到的地点")
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
				toast("未查询到相关地点");
			}
		}
	};
	/**
	 * @author mikyou
	 * 添加全景覆盖物，即全景的图标，迅速定位到该地点在地图上的位置
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
	//地图单击
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
				String distance = String.valueOf("   距离我"+(int)DistanceUtil.getDistance(currentLatLng, latLng))+"米";// 计算POI到当前位置的距离							
				
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
        	// 初始化全局 bitmap 信息，不用时及时 recycle
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
						String distance = String.valueOf("   距离我"+(int)DistanceUtil.getDistance(currentLatLng, latLng))+"米";// 计算POI到当前位置的距离							
						
						locationText.setText(marker.getTitle()+distance);
						
						
						return true;
					}
				});
	    	} 
		}
	/**
	 * @author zhongqihong
	 * 搜索相关
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
			 * 根据输入搜索的信息，从网络获得的JSON数据
			 * 开启一个线程去获取网络数据
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
	 * 将我们搜索的信息来自网络的JSON数据解析后，封装在一个SearchInfo类中
	 * 然后将这些数据展示在一个自定义的列表项的对话框中，以下就为定义列表项的适配器
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
	 * 修改默认百度地图的View
	 * 
	 * */
	private void changeDefaultBaiduMapView() {
		changeInitialzeScaleView();//改变默认百度地图初始加载的地图比例
		//设置隐藏缩放和扩大的百度地图的默认的比例按钮
		for (int i = 0; i < myMapView.getChildCount(); i++) {//遍历百度地图中的所有子View,找到这个扩大和缩放的按钮控件View，然后设置隐藏View即可
			View child=myMapView.getChildAt(i);
			if (child instanceof ZoomControls) {
				defaultBaiduMapScaleButton=child;//该defaultBaiduMapScaleButton子View是指百度地图默认产生的放大和缩小的按钮，得到这个View
				break;
			}
		}
		defaultBaiduMapScaleButton.setVisibility(View.GONE);//然后将该View的Visiblity设为不存在和不可见，即隐藏
		defaultBaiduMapLogo =myMapView.getChildAt(1);//该View是指百度地图中默认的百度地图的Logo,得到这个View
		defaultBaiduMapLogo.setPadding(300, -10, 100, 100);//设置该默认Logo View的位置，因为这个该View的位置会影响下面的刻度尺单位View显示的位置
		myMapView.removeViewAt(1);//最后移除默认百度地图的logo View
		defaultBaiduMapScaleUnit=myMapView.getChildAt(2);//得到百度地图的默认单位刻度的View
		defaultBaiduMapScaleUnit.setPadding(100, 0, 115,200);//最后设置调整百度地图的默认单位刻度View的位置
	}
	/**
	 * @author zhongqihong
	 * 修改百度地图默认开始初始化加载地图比例大小
	 * */
	private void changeInitialzeScaleView() {
		myBaiduMap=myMapView.getMap();//改变百度地图的放大比例,让首次加载地图就开始扩大到500米的距离
		MapStatusUpdate factory=MapStatusUpdateFactory.zoomTo(15.0f);
		myBaiduMap.animateMapStatus(factory);		
	}
	protected void onDestroy() {
		super.onDestroy();
		//在Activity执行onDestory时执行mapView(地图)生命周期管理
		myMapView.onDestroy();
	}
	@Override
	protected void onStart() {//当Activity调用onStart方法，开启定位以及开启方向传感器，即将定位的服务、方向传感器和Activity生命周期绑定在一起
		myBaiduMap.setMyLocationEnabled(true);//开启允许定位
		if (!myLocationClient.isStarted()) {
			myLocationClient.start();//开启定位
		}
		//开启方向传感器
		myOrientationListener.start();
		super.onStart();
	}
	@Override
	protected void onStop() {//当Activity调用onStop方法，关闭定位以及关闭方向传感器
		myBaiduMap.setMyLocationEnabled(false);
		myLocationClient.stop();//关闭定位
		myOrientationListener.stop();//关闭方向传感器
		super.onStop();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//在Activity执行onResume是执行MapView(地图)生命周期管理
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
	 * 初始化地图
	 * */
	private void initMapView() {
		registerAllViewsId();
		registerAllViewsEvent();
	}
	/**
	 * @author zhongqihong
	 * 注册所有View相关的id
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
	 * 注册所有View相关的事件
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
	 * 初始化定位
	 * */
	private void initMapLocation() {
		myLocationClient=new LocationClient(this);//创建一个定位客户端对象
		myListener=new MyLocationListener();//创建一个定位事件监听对象
		myLocationClient.registerLocationListener(myListener);//并给该定位客户端对象注册监听事件
		//对LocaitonClient进行一些必要的设置
		LocationClientOption option=new LocationClientOption();
		option.setCoorType("bd09ll");//设置坐标的类型
		option.setIsNeedAddress(true);//返回当前的位置信息，如果不设置为true，默认就为false，就无法获得位置的信息
		option.setOpenGps(true);//打开GPS
		option.setScanSpan(1000);//表示1s中进行一次定位请求
		myLocationClient.setLocOption(option);
		useLocationOrientationListener();//调用方向传感器
	}
	/**
	 * @author zhongqihong
	 * 定位结合方向传感器，从而可以实时监测到X轴坐标的变化，从而就可以检测到
	 * 定位图标方向变化，只需要将这个动态变化的X轴的坐标更新myCurrentX值，
	 * 最后在MyLocationData data.driection(myCurrentX);
	 * */
	private void useLocationOrientationListener() {
		myOrientationListener=new MyOrientationListener(MainActivity.this);
		myOrientationListener.setMyOrientationListener(new onOrientationListener() {
			public void onOrientationChanged(float x) {//监听方向的改变，方向改变时，需要得到地图上方向图标的位置
				myCurrentX=x;
				
			}
		});		
	}
	/**
	 * @author zhongqihong
	 * 获得最新定位的位置,并且地图的中心点设置为我的位置
	 * */
	private void getMyLatestLocation(double lat,double lng) {
		LatLng latLng=new LatLng(lat, lng);
		latjd=latLng.latitude;
		longwd=latLng.longitude;//创建一个经纬度对象，需要传入当前的经度和纬度两个整型值参数
		MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);//创建一个地图最新更新的状态对象，需要传入一个最新经纬度对象
		myBaiduMap.animateMapStatus(msu);//表示使用动画的效果传入，通过传入一个地图更新状态对象，然后利用百度地图对象来展现和还原那个地图更新状态，即此时的地图显示就为你现在的位置
	}
	/**
	 * @author zhongqihong
	 * BDLocationListener的实现类:MyLocationListener
	 * */
	private class MyLocationListener implements BDLocationListener{
		@Override
		public void onReceiveLocation(BDLocation 
				location) {
			
			//得到一个MyLocationData对象，需要将BDLocation对象转换成MyLocationData对象
			MyLocationData data=new MyLocationData.Builder()
			.accuracy(location.getRadius())//精度半径
			.direction(myCurrentX)//方向
			.latitude(location.getLatitude())//经度
			.longitude(location.getLongitude())//纬度
			.build();
			myBaiduMap.setMyLocationData(data);
			//配置自定义的定位图标,需要在紧接着setMyLocationData后面设置
			//调用自定义定位图标
			changeLocationIcon();
			latitude=location.getLatitude();//得到当前的经度
			longtitude=location.getLongitude();//得到当前的纬度
			latjd=latitude;
			longwd=longtitude;
			startinfo.setDesname(location.getAddrStr());
			startinfo.setLatitude(latitude);
			startinfo.setLongtiude(longtitude);
			
			//toast("经度："+latitude+"     纬度:"+longtitude);
			if (isFirstIn) {//表示用户第一次打开，就定位到用户当前位置，即此时只要将地图的中心点设置为用户此时的位置即可

				getMyLatestLocation(latitude,longtitude);//获得最新定位的位置,并且地图的中心点设置为我的位置
				isFirstIn=false;//表示第一次才会去定位到中心点
				locationTextString=""+location.getAddrStr();//这里得到地址必须需要在设置LocationOption的时候需要设置isNeedAddress为true;
				toast(locationTextString);
				locationText.setText(locationTextString);
			}
		}
	}
	/**
	 * @author zhongqihong
	 * 自定义定位图标
	 * */
	private void changeLocationIcon() {
		myBitmapLocation=BitmapDescriptorFactory
				.fromResource(R.drawable.icon_landing_arrow);//引入自己的图标
		if (isFirstIn) {//表示第一次定位显示普通模式
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
			.setTitle("请选择地图的类型")
			.setItems(types, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String select=types[which];
					if (select.equals("普通地图")) {
						myBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
					}else if (select.equals("卫星地图")) {
						myBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
					}else if (select.equals("热力地图(已关闭)")||select.equals("热力地图(已打开)")) {
						if(myBaiduMap.isBaiduHeatMapEnabled()) {
							myBaiduMap.setBaiduHeatMapEnabled(false);
							Toast.makeText(MainActivity.this, "热力地图已关闭", 0).show();
							types[which]="热力地图(已关闭)";
						}else {
							myBaiduMap.setBaiduHeatMapEnabled(true);
							Toast.makeText(MainActivity.this, "热力地图已打开", 0).show();
							types[which]="热力地图(已打开)";
						}
					}
				}
			}).show();
			break;
		case R.id.add_scale:
			current+=0.5f;
			MapStatusUpdate 
			factory=MapStatusUpdateFactory.zoomTo(15.0f+current);
			myBaiduMap.animateMapStatus(factory);//设置百度地图的缩放效果动画animateMapStatus
			break;
		case R.id.low_scale:
			current-=0.5f;
			MapStatusUpdate 
			factory2=MapStatusUpdateFactory.zoomTo(15.0f+current);
			myBaiduMap.animateMapStatus(factory2);
			break;
		case R.id.my_location://定位功能，需要用到LocationClient进行定位
			//BDLocationListener
			myBaiduMap.clear();
			getMyLatestLocation(latitude,longtitude);//获取最新的位置
			break;
		case R.id.map_location:
			AlertDialog.Builder builder2=new AlertDialog.Builder(this);
			builder2.setIcon(R.drawable.track_collect_running)
			.setTitle("请选择定位的模式")
			.setItems(LocationModeString, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String mode=LocationModeString[which];
					if (mode.equals("罗盘模式")) {
						MyLocationConfiguration config=new 
								MyLocationConfiguration(LocationMode.COMPASS, true, myBitmapLocation);
						myBaiduMap.setMyLocationConfigeration(config);
					}else if (mode.equals("跟随模式")) {
						MyLocationConfiguration config=new 
								MyLocationConfiguration(LocationMode.FOLLOWING, true, myBitmapLocation);
						myBaiduMap.setMyLocationConfigeration(config);
					}else if (mode.equals("普通模式")) {
						MyLocationConfiguration config=new 
								MyLocationConfiguration(LocationMode.NORMAL, true, myBitmapLocation);
						myBaiduMap.setMyLocationConfigeration(config);
					}else if (mode.equals("3D俯视模式(已关闭)")||mode.equals("3D俯视模式(已打开)")) {
						if (mode.equals("3D俯视模式(已打开)")) {
							UiSettings	 mUiSettings = myBaiduMap.getUiSettings();
							mUiSettings.setCompassEnabled(true);
							LocationModeString[which]="3D俯视模式(已关闭)";
							toast("3D模式已关闭");
						}else{
							MyLocationConfiguration config=new 
									MyLocationConfiguration(LocationMode.COMPASS, true, myBitmapLocation);
							myBaiduMap.setMyLocationConfigeration(config);
							MyLocationConfiguration config2=new 
									MyLocationConfiguration(LocationMode.NORMAL, true, myBitmapLocation);
							myBaiduMap.setMyLocationConfigeration(config2);
							LocationModeString[which]="3D俯视模式(已打开)";
							toast("3D模式已打开");
						}
					}
				}
			}).show();
			break;
		case R.id.map_marker://加油信息传递
			Intent intent3=new Intent(MainActivity.this, BuyActivity.class);
			intent3.putExtra("gasinfo", endinfo);
			startActivity(intent3);
			break;
		case R.id.map_panorama:
			Intent intent=new Intent(MainActivity.this, PanoramaActivity.class);
			intent.putExtra("panoramaLatLng", new double []{latitude,longtitude});
			startActivity(intent);
			break;
		case R.id.map_gotogether://搜索加油站
			
			searchInfoLists=new ArrayList<SearchInfo>();
			getsearchDataFromNetWork();
			break;
		case R.id.start_go://导航
			Intent intent2=new Intent(MainActivity.this, NaViPathActivity.class);
			intent2.putExtra("startwd", startinfo);
			intent2.putExtra("endwd", endinfo);
			startActivity(intent2);
			break;
		default:
			break;
		}
	}
	//加油站搜索
	private void getsearchDataFromNetWork() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject jsonObject=httputil.send("加油",latjd,longwd, null);
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
	 * 添加覆盖物到图层上(也就是将覆盖物添加到地图表面)
	 * */
	private void addOverLayer() {
		myBaiduMap.clear();//先清除一下图层
		LatLng latLng=null;
		Marker marker=null;
		OverlayOptions options;
		myMark=BitmapDescriptorFactory.fromResource(R.drawable.mark);//引入自定义的覆盖物图标，将其转化成一个BitmapDescriptor对象
		//遍历Info的List一个Info就是一个Mark
		for (int i = 0; i < marks.size(); i++) {
			//经纬度对象
			latLng=new LatLng(marks.get(i).getLatitude(), marks.get(i).getLongitude());
			//图标
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
	 * 初始化覆盖物
	 * 竖直方向代表经度，越往上经度越大，下越小
	 * 水平方向代表纬度，越往右纬度越大，左越小
	 * */
	private void initMapMarks() {
		marks=new ArrayList<Info>();//将所有的覆盖物的信息的封装到一个List<Info>集合中
		marks.add(new Info(32.079254, 118.787623, R.drawable.pic1, "英伦贵族小旅馆", "距离209米", 1888));
		marks.add(new Info(32.064355, 118.787624, R.drawable.pic2, "沙井国际高级会所", "距离459米", 388));
		marks.add(new Info(28.7487420000, 115.8748860000, R.drawable.pic4, "华东交通大学南区", "距离5米", 888));
		marks.add(new Info(28.7534890000, 115.8767960000, R.drawable.pic3, "华东交通大学北区", "距离10米", 188));
		myBaiduMap.setOnMarkerClickListener(this);
		myBaiduMap.setOnMapClickListener(this);	
	}
	public void toast(String str){
		Toast.makeText(MainActivity.this, str, 0).show();
	}
	/**
	 * @author zhongqihong
	 * 添加好的覆盖物的点击事件
	 * */
	@Override
	public boolean onMarkerClick(Marker marker) {//显示已点击后的覆盖物的详情
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
		//初始化一个InfoWindow
		initInfoWindow(MyMarker,marker);
		markLayout.setVisibility(View.VISIBLE);
		return true;
	}
	/**
	 *@author zhongqihong
	 *初始化出一个InfoWindow
	 * 
	 * */
	private void initInfoWindow(Info MyMarker,Marker marker) {
		// TODO Auto-generated method stub
		InfoWindow infoWindow;
		//InfoWindow中显示的View内容样式，显示一个TextView
		TextView infoWindowTv=new TextView(MainActivity.this);
		infoWindowTv.setBackgroundResource(R.drawable.location_tips);
		infoWindowTv.setPadding(30, 20, 30, 50);
		infoWindowTv.setText(MyMarker.getName());
		infoWindowTv.setTextColor(Color.parseColor("#FFFFFF"));

		final LatLng latLng=marker.getPosition();
		Point p=myBaiduMap.getProjection().toScreenLocation(latLng);//将地图上的经纬度转换成屏幕中实际的点
		p.y-=47;//设置屏幕中点的Y轴坐标的偏移量
		LatLng ll=myBaiduMap.getProjection().fromScreenLocation(p);//把修改后的屏幕的点有转换成地图上的经纬度对象
		/**
		 * @author zhongqihong
		 * 实例化一个InfoWindow的对象
		 * public InfoWindow(View view,LatLng position, int yOffset)通过传入的 view 构造一个 InfoWindow, 此时只是利用该view生成一个Bitmap绘制在地图中，监听事件由开发者实现。
		 *	参数:
		 * view - InfoWindow 展示的 view
		 * position - InfoWindow 显示的地理位置
		 * yOffset - InfoWindow Y 轴偏移量
		 * */
		infoWindow=new InfoWindow(infoWindowTv, ll, 10);
		myBaiduMap.showInfoWindow(infoWindow);//显示InfoWindow
	}
	/**
	 * @author zhongqihong
	 * 给整个地图添加的点击事件
	 * */
	@Override
	public void onMapClick(LatLng arg0) {//表示点击地图其他的地方使得覆盖物的详情介绍的布局隐藏，但是点击已显示的覆盖物详情布局上，则不会消失，因为在详情布局上添加了Clickable=true
		//由于事件的传播机制，因为点击事件首先会在覆盖物布局的父布局(map)中,由于map是可以点击的，map则会把点击事件给消费掉，如果加上Clickable=true表示点击事件由详情布局自己处理，不由map来消费
		markLayout.setVisibility(View.GONE);
		myBaiduMap.hideInfoWindow();//隐藏InfoWindow
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
