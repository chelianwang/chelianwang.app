package com.example.tools;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author zhongqihong
 * 封装get和post方式请求
 * HttpClient 维护session
 * 一个HttpClient对象代表一个浏览器
 * 高度封装了客户端与服务端交互的，可以直接返回一个JSONObject对象
 * */
public class HttpUtils {
	private static 	JSONObject jsonObjects;
	private static String entityString;
	private static HttpResponse res=null;
	private static String APPKEY="jE1gjLCTpdUKYIdX4V2N9lDhbYlrpZji";
	private static String outputForm="json";
	//http://api.map.baidu.com/place/v2/search?ak=3QIYhSeKPK770bpwzepo9GI1&output=json&query=华东交通大学北区&page_size=10&page_num=0&scope=1&region=江西&mcode=F5:3E:06:3E:FC:E8:ED:19:60:2E:99:63:D8:78:85:2E:EB:12:9D:BE;com.zhongqihong.mymap
	private static String Head="http://api.map.baidu.com/place/v2/search?ak=";
	private static String IP=null;
	private static int pageSize=20;
	private static String mcode="97:C2:2B:1D:46:06:7D:6B:FE:04:B3:11:01:DF:75:03:40:2E:40:A6;com.zhongqihong.mymap";
	private static HttpClient clients=new DefaultHttpClient();
	@SuppressWarnings("unused")
	public static JSONObject send(String destination,List<NameValuePair> params) throws ClientProtocolException, IOException, JSONException{

		if (params==null) {//表示发送get方式请求，否则就发送Post发送请求(因为get方式不需要params)
			IP=Head+APPKEY+"&output="+outputForm+"&query="+destination+"&page_size="+pageSize+"&page_num=0&scope=1&region=全国&mcode="+mcode;
			System.out.println("IP_Address:--------->"+IP);
			HttpGet get=new HttpGet(IP);
			res=clients.execute(get);
		}else{
			HttpPost post=new HttpPost(IP);
			post.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
			res=clients.execute(post);
		}
		if (res.getStatusLine().getStatusCode()==200) {
			HttpEntity entity=res.getEntity();
			entityString=EntityUtils.toString(entity,HTTP.UTF_8);
			System.out.println("httpUtils--------------->"+entityString);
			jsonObjects=new JSONObject(entityString);
			
		}
		return jsonObjects;
	}
	public static String getEntityString() {
		return entityString;
	}
	public static void setEntityString(String entityString) {
		HttpUtils.entityString = entityString;
	}
}
