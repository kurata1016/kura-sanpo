package com.android.kura_sanpo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.LatLng;

public class PictureInfo {
	private byte[] image; // �摜
	private float[] latlong; // �ܓx�o�x
	private String date; // ���t
	private String filePath; // �t�@�C���p�X

	// getter���\�b�h
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

	// setter���\�b�h
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
