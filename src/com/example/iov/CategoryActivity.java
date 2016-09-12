package com.example.iov;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import java.util.ArrayList;

public class CategoryActivity extends Activity implements View.OnClickListener{

    private ViewPager viewPager;
    private ArrayList<View> pageview;
    private TextView videoLayout;
    private TextView musicLayout;
    // ������ͼƬ
    private ImageView scrollbar;
    // ��������ʼƫ����
    private int offset = 0;
    // ��ǰҳ���
    private int currIndex = 0;
    // ���������
    private int bmpW;
    //һ��������
    private int one;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_category);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        //���Ҳ����ļ���LayoutInflater.inflate
        LayoutInflater inflater =getLayoutInflater();
        View view1 = inflater.inflate(R.layout.carinfo, null);
        View view2 = inflater.inflate(R.layout.gasinfo, null);
        videoLayout = (TextView)findViewById(R.id.videoLayout);
        musicLayout = (TextView)findViewById(R.id.musicLayout);
        scrollbar = (ImageView)findViewById(R.id.scrollbar);
        videoLayout.setOnClickListener(this);
        musicLayout.setOnClickListener(this);
        pageview =new ArrayList<View>();
        //�����Ҫ�л��Ľ���
        pageview.add(view1);
        pageview.add(view2);
        //����������
        PagerAdapter mPagerAdapter = new PagerAdapter(){

            @Override
            //��ȡ��ǰ���������
            public int getCount() {
                // TODO Auto-generated method stub
                return pageview.size();
            }

            @Override
            //�ж��Ƿ��ɶ������ɽ���
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0==arg1;
            }
            //ʹ��ViewGroup���Ƴ���ǰView
            public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewPager) arg0).removeView(pageview.get(arg1));
            }

            //����һ������������������PagerAdapter������ѡ���ĸ�������ڵ�ǰ��ViewPager��
            public Object instantiateItem(View arg0, int arg1){
                ((ViewPager)arg0).addView(pageview.get(arg1));
                return pageview.get(arg1);
            }
        };
        //��������
        viewPager.setAdapter(mPagerAdapter);
        //����viewPager�ĳ�ʼ����Ϊ��һ������
        viewPager.setCurrentItem(0);
        //����л�����ļ�����
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        // ��ȡ�������Ŀ��
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.btn_blue1).getWidth();
        //Ϊ�˻�ȡ��Ļ��ȣ��½�һ��DisplayMetrics����
        DisplayMetrics displayMetrics = new DisplayMetrics();
        //����ǰ���ڵ�һЩ��Ϣ����DisplayMetrics����
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //�õ���Ļ�Ŀ��
        int screenW = displayMetrics.widthPixels;
        //�������������ʼ��ƫ����
        offset = (screenW / 2 - bmpW) / 2;
        //������л�һ������ʱ����������λ����
        one = offset * 2 + bmpW;
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        //���������ĳ�ʼλ�����ó�����߽���һ��offset
        scrollbar.setImageMatrix(matrix);
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                        /**
                         * TranslateAnimation���ĸ����Էֱ�Ϊ
                         * float fromXDelta ������ʼ�ĵ��뵱ǰView X�����ϵĲ�ֵ 
                         * float toXDelta ���������ĵ��뵱ǰView X�����ϵĲ�ֵ 
                         * float fromYDelta ������ʼ�ĵ��뵱ǰView Y�����ϵĲ�ֵ 
                         * float toYDelta ������ʼ�ĵ��뵱ǰView Y�����ϵĲ�ֵ
                        **/
                        animation = new TranslateAnimation(one, 0, 0, 0);
                    break;
                case 1:
                        animation = new TranslateAnimation(offset, one, 0, 0);
                    break;
            }
            //arg0Ϊ�л�����ҳ�ı���
            currIndex = arg0;
            // ������������Ϊtrue����ʹ��ͼƬͣ�ڶ�������ʱ��λ��
            animation.setFillAfter(true);
            //��������ʱ�䣬��λΪ����
            animation.setDuration(200);
            //��������ʼ����
            scrollbar.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

   
	@Override
    public void onClick(View view){
         switch (view.getId()){
             case R.id.videoLayout:
                 //���"��Ƶ��ʱ�л�����һҳ
            	 
                 viewPager.setCurrentItem(0);
                 break;
             case R.id.musicLayout:
                 //��������֡�ʱ�л��ĵڶ�ҳ
            	 
                 viewPager.setCurrentItem(1);
                 break;
         }
    }
}