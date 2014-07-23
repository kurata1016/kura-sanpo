package com.android.kura_sanpo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.LatLng;

public class PictureInfo {
	private byte[] image; // 画像
	private float[] latlong; // 緯度経度
	private String date; // 日付
	private String filePath; // ファイルパス

	// getterメソッド
	public byte[] getImage() {
		return image;
	}

	public LatLng getLatLong() {
		LatLng latlng = new LatLng(latlong[0], latlong[1]);
		return latlng;
	}

	public String getDate() {
		return date;
	}

	public Bitmap getBitmap() {
		return BitmapFactory.decodeByteArray(getImage(), 0, getImage().length);
	}

	public String getFilePath() {
		return filePath;
	}

	// setterメソッド
	public void setImage(byte[] image) {
		this.image = image;
	}

	public void setLatlong(float[] latlong) {
		this.latlong = latlong;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
