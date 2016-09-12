package com.example.mymap;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.lbsapi.panoramaview.PanoramaView.ImageDefinition;
import com.baidu.lbsapi.tools.CoordinateConverter;
import com.baidu.lbsapi.tools.CoordinateConverter.COOR_TYPE;
import com.baidu.mapapi.model.LatLng;
import com.example.iov.R;
import com.example.tools.BaseApplication;
import com.example.tools.SystemStatusManager;


import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.Application;
import android.graphics.Point;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class PanoramaActivity extends Activity implements PanoramaViewListener {

	private double latitude,longtiude;
	private PanoramaView panoramaView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTranslucentStatus();
		initBMapManager();
		setContentView(R.layout.activity_panorama);
		initPanoramaView();
	}

	private void initBMapManager() {
		BaseApplication app = (BaseApplication) this.getApplication();
		if (app.mBMapManager == null) {
			app.mBMapManager = new BMapManager(app);
			app.mBMapManager.init(new BaseApplication.MyGeneralListener());
		}
	}

	private void initPanoramaView() {
		double[] lats=getIntent().getDoubleArrayExtra("panoramaLatLng");
		Toast.makeText(PanoramaActivity.this, "latitude:"+lats[0]+"    longtiude:"+lats[1], Toast.LENGTH_LONG).show();
		latitude=lats[0];
		longtiude=lats[1];
		panoramaView=(PanoramaView) findViewById(R.id.panorama);
		panoramaView.setPanorama(longtiude, latitude);
		panoramaView.setPanoramaViewListener(this);
		panoramaView.setPanoramaImageLevel(ImageDefinition.ImageDefinitionMiddle);
	//	panoramaView.setPanoramaByUid("54f6515b173ab6cc0d3dda17", PanoramaView.PANOTYPE_INTERIOR);
		panoramaView.setIndoorAlbumGone();
		panoramaView.setIndoorAlbumVisible();
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
	@Override
	protected void onPause() {
		super.onPause();
		panoramaView.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		panoramaView.destroy();
	}
	@Override
	protected void onResume() {
		super.onResume();
		panoramaView.onResume();
	}

	@Override
	public void onLoadPanoramaBegin() {

	}

	@Override
	public void onLoadPanoramaEnd(String arg0) {

	}

	@Override
	public void onLoadPanoramaError(String arg0) {

	}
}
