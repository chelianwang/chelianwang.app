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
	 * ��ʾȫ��
	 * */
	private void initPanoramaView() {
		SearchInfo info=(SearchInfo) getIntent().getSerializableExtra("info");//��ȡ��MainActivity�д�����SearchInfo����
		String uid=info.getUid();//�õ������ھ������uid
		panoramaView=(PanoramaView) findViewById(R.id.panorama);
		//�ж��Ƿ������⾰
		PanoramaRequest request=PanoramaRequest.getInstance(OtherPnoramaActivity.this);
		BaiduPoiPanoData poiPanoData=request.getPanoramaInfoByUid(uid);//����uid���ø��ھ�API
		panoramaView.setPanoramaImageLevel(ImageDefinition.ImageDefinitionMiddle);//��ʾչʾ���ھ����еȷֱ��ʣ���ImageDefinitionHigh���߷ֱ��ʣ���ImageDefinitionLow���ͷֱ���
		if (poiPanoData.hasInnerPano()) {//�жϸ�POI�Ƿ����ھ�
			panoramaView.setPanoramaByUid(uid, PanoramaView.PANOTYPE_INTERIOR);
			panoramaView.setIndoorAlbumGone();//��ȥ�ھ����
			panoramaView.setIndoorAlbumVisible();//���ھ������ʾ
		}else if (poiPanoData.hasStreetPano()) {//�жϸ�POI�Ƿ����⾰,��ֻ��ͨ����γ������ʾ�⾰
			panoramaView.setPanorama(info.getLongtiude(), info.getLatitude());//û���ھ���ͨ����γ����չʾ�־�
			//Toast.makeText(OtherPnoramaActivity.this, "�иõط����⾰��wait", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(OtherPnoramaActivity.this, "sorry,���粻�����޷�����ȫ��", Toast.LENGTH_SHORT).show();
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
