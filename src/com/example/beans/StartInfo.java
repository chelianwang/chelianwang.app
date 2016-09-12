package com.example.beans;

import java.io.Serializable;

public class StartInfo implements Serializable {
	private String Desname;
	private double latitude;
	private double longtiude;
	private String address;
	private String streetId;
	private String uid;
	public StartInfo(String desname, double latitude, double longtiude,
			String address, String streetId, String uid) {
		super();
		Desname = desname;
		this.latitude = latitude;
		this.longtiude = longtiude;
		this.address = address;
		this.streetId = streetId;
		this.uid = uid;
	}
	public StartInfo() {
		super();
	}
	public String getDesname() {
		return Desname;
	}
	public void setDesname(String desname) {
		Desname = desname;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongtiude() {
		return longtiude;
	}
	public void setLongtiude(double longtiude) {
		this.longtiude = longtiude;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getStreetId() {
		return streetId;
	}
	public void setStreetId(String streetId) {
		this.streetId = streetId;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
