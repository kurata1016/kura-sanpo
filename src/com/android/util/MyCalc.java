package com.android.util;

import com.google.android.gms.maps.model.CameraPosition;

public class MyCalc {
	// 2“_ŠÔ‚Ì‹——£‚ğ‹‚ß‚é(km)
	public static double getDistance(CameraPosition a, CameraPosition b) {
		double lata = Math.toRadians(a.target.latitude);
		double lnga = Math.toRadians(a.target.longitude);

		double latb = Math.toRadians(b.target.latitude);
		double lngb = Math.toRadians(b.target.longitude);

		double r = 6378.137; // Ô“¹”¼Œa

		return r * Math.acos(Math.sin(lata) * Math.sin(latb) + Math.cos(lata) * Math.cos(latb) * Math.cos(lngb - lnga));
	}
}
