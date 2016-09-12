package com.example.tools;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;

import com.baidu.mapapi.SDKInitializer;

public class BaseApplication extends Application {
	private static Context mContext;
	public static float sScale;
	public static int sWidthDp;
	public static int sWidthPix;
	public BMapManager mBMapManager = null;
	private static BaseApplication mInstance;
	public static BaseApplication getInstance() {
		return mInstance;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);
		initEngineManager(this);
		mContext = this;
		mInstance = this;
		sScale = getResources().getDisplayMetrics().density;
		sWidthPix = getResources().getDisplayMetrics().widthPixels;
		sWidthDp = (int) (sWidthPix / sScale);
	}
	public void initEngineManager(Context context) {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(context);
		}
		if (!mBMapManager.init(new MyGeneralListener())) {
			Toast.makeText(
					BaseApplication.getInstance().getApplicationContext(),
					"BMapManager!", Toast.LENGTH_LONG).show();
		}
	}
	public static class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetPermissionState(int iError) {
		
			if (iError != 0) {
			} else {
			}
		}
	}
}
