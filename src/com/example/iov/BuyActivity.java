package com.example.iov;


import com.example.beans.EndInfo;
import com.google.zxing.WriterException;
import com.zxing.activity.CaptureActivity;
import com.zxing.encoding.EncodingHandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BuyActivity  extends Activity{
	private TextView resultTextView;
	private EditText qrStrEditText;
	private ImageView qrImgImageView;
	private EndInfo gasinfo;
   @Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main_buy);
	 resultTextView = (TextView) this.findViewById(R.id.tv_scan_result);
     qrStrEditText = (EditText) this.findViewById(R.id.et_qr_string);
     qrImgImageView = (ImageView) this.findViewById(R.id.iv_qr_image);
     
     gasinfo=(EndInfo)getIntent().getSerializableExtra("gasinfo");
     qrStrEditText.setText(gasinfo.getDesname());
     Button scanBarCodeButton = (Button) this.findViewById(R.id.btn_scan_barcode);
     scanBarCodeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//��ɨ�����ɨ����������ά��
				System.out.println(1);
				Intent openCameraIntent = new Intent(BuyActivity.this,CaptureActivity.class);
				System.out.println(2);
				startActivityForResult(openCameraIntent, 0);
				System.out.println(3);
			}
		});
     
     Button generateQRCodeButton = (Button) this.findViewById(R.id.btn_add_qrcode);
     generateQRCodeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					String contentString = qrStrEditText.getText().toString();
					if (!contentString.equals("")) {
						//�����ַ������ɶ�ά��ͼƬ����ʾ�ڽ����ϣ��ڶ�������ΪͼƬ�Ĵ�С��350*350��
						Bitmap qrCodeBitmap = EncodingHandler.createQRCode(contentString, 350);
						qrImgImageView.setImageBitmap(qrCodeBitmap);
					}else {
						Toast.makeText(BuyActivity.this, "��Ϣ����Ϊ�գ�", Toast.LENGTH_SHORT).show();
					}
					
				} catch (WriterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
 }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//����ɨ�������ڽ�������ʾ��
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			resultTextView.setText(scanResult);
		}
	}
}