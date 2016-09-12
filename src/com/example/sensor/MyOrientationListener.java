package com.example.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MyOrientationListener implements SensorEventListener{
	private SensorManager mySensorManager;
	private Sensor mySensor;
	private Context myContext; 
	private float lastX;
	private onOrientationListener myOrientationListener;
	public void start(){//�������򴫸���
		//��ͨ��ϵͳ�������õ��������������mySensorManager
		mySensorManager=(SensorManager) myContext.getSystemService(Context.SENSOR_SERVICE);
		if (mySensorManager!=null) {//����������������Ϊ�գ������ͨ�������������������÷��򴫸�������
			//��÷��򴫸�������
			mySensor=mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		}
		if (mySensor!=null) {//������򴫸�����Ϊ�գ�����÷��򴫸���ע������¼�
			mySensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_UI);
		}
	}
	public void stop(){//���ע�᷽�򴫸��������¼�
		mySensorManager.unregisterListener(this);
	}
	public MyOrientationListener(Context myContext) {//���򴫸�����һ��������
		super();
		this.myContext = myContext;
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	//�����������仯
	@Override
	public void onSensorChanged(SensorEvent event) {//���ȷ����ı��ʱ��
		if (event.sensor.getType()==Sensor.TYPE_ORIENTATION) {//����Ƿ��򴫸���
			float x=event.values[SensorManager.DATA_X];//��ô�������X�������,���Է���3��ֵ����X������꣬Y�����꣬Z�����꣬����ֻ��ҪX������
			if (Math.abs(x-lastX)>1.0) {//�Աȱ��ε�X����ı仯����һ�εı仯�����1.0��˵���������ı�
				if (myOrientationListener!=null) {//˵���������Ѿ�ע�����¼�������Ϊ�գ�Ȼ�����һ���ص���ʵʱ�仯X������괫��
					//ͨ��һ���ص�������֪ͨ������ȥ����UI
					myOrientationListener.onOrientationChanged(lastX);//����Ҫ����һ�ε�X�����괫�룬��MainActivity�еĻص�������ȥ��ȡ����
				}
			}
			lastX=x;
		}
	}

	public void setMyOrientationListener(onOrientationListener myOrientationListener) {
		this.myOrientationListener = myOrientationListener;
	}
	//дһ���ӿ�ʵ�ַ���ı�ļ��������Ļص�
	public interface onOrientationListener{
		void onOrientationChanged(float x);//�ص��ķ���
	}
}
