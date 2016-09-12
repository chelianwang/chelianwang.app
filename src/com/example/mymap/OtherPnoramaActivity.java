package com.example.mymap;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.model.BaiduPoiPanoData;
import com.baidu.lbsapi.panoramaview.PanoramaRequest;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaView.ImageDefinition;
import com.example.beans.SearchInfo;
import com.example.iov.R;
import com.example.tools.BaseApplication;
import com.example.tools.SystemStatusManager;



import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class OtherPnoramaActivity extends Activity {
	private double latitude,longtiude;
	private PanoramaView panoramaView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTranslucentStatus();
		setContentView(R.layout.activity_other_pnorama);
		initPanoramaView();
	}

	@Override
	protected void onPause() {
		panoramaView.onPause();
		super.onPause();
	}
	@Override
	protected void onResume() {
		panoramaView.onResume();
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		panoramaView.destroy();
		super.onDestroy();
	}
	/**
	 * @author mikyou
	 * 显示全景
	 * */
	private void initPanoramaView() {
		SearchInfo info=(SearchInfo) getIntent().getSerializableExtra("info");//获取从MainActivity中传来的SearchInfo对象
		String uid=info.getUid();//得到设置内景必需的uid
		panoramaView=(PanoramaView) findViewById(R.id.panorama);
		//判断是否有内外景
		PanoramaRequest request=PanoramaRequest.getInstance(OtherPnoramaActivity.this);
		BaiduPoiPanoData poiPanoData=request.getPanoramaInfoByUid(uid);//将该uid设置给内景API
		panoramaView.setPanoramaImageLevel(ImageDefinition.ImageDefinitionMiddle);//表示展示的内景是中等分辨率，（ImageDefinitionHigh）高分辨率，（ImageDefinitionLow）低分辨率
		if (poiPanoData.hasInnerPano()) {//判断该POI是否有内景
			panoramaView.setPanoramaByUid(uid, PanoramaView.PANOTYPE_INTERIOR);
			panoramaView.setIndoorAlbumGone();//除去内景相册
			panoramaView.setIndoorAlbumVisible();//将内景相册显示
		}else if (poiPanoData.hasStreetPano()) {//判断该POI是否有外景,就只能通过经纬度来显示外景
			panoramaView.setPanorama(info.getLongtiude(), info.getLatitude());//没有内景就通过经纬度来展示街景
			//Toast.makeText(OtherPnoramaActivity.this, "有该地方的外景，wait", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(OtherPnoramaActivity.this, "sorry,网络不给力无法加载全景", Toast.LENGTH_SHORT).show();
		}

	}
	private void initBMapManager() {
		BaseApplication app = (BaseApplication) this.getApplication();
		if (app.mBMapManager == null) {
			app.mBMapManager = new BMapManager(app);
			app.mBMapManager.init(new BaseApplication.MyGeneralListener());
		}
	}
	private void setTranslucentStatus() {
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
			Window win=getWindow();
			WindowManager.LayoutParams winParams=win.getAttributes();
			final int bits=WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			winParams.flags |=bits;
			win.setAttributes(winParams);
		}
		SystemStatusManager tintManager = new SystemStatusManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(0);
		tintManager.setNavigationBarTintEnabled(true);

	}
}
