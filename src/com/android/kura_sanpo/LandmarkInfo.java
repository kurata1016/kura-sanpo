package com.android.kura_sanpo;

public class LandmarkInfo {
	public String landmark; // 1番近いランドマーク
	public double lat; // 緯度
	public double lon; // 経度

	// 2点間の距離算出
	public String getDistance(double presentLat, double presentLon) {
		// 赤道半径(km)
		double r = 6378.137;

		// 駅の緯度と経度
		double lat1 = lat * Math.PI / 180;
		double lng1 = lon * Math.PI / 180;
		// 現在地の緯度と経度
		double lat2 = presentLat * Math.PI / 180;
		double lng2 = presentLon * Math.PI / 180;

		// 2点間の距離(m)
		double distance = 1000 * (r * Math.acos(Math.sin(lat1) * Math.sin(lat2) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1))));

		return Double.toString(Math.round(distance));
	}
}
