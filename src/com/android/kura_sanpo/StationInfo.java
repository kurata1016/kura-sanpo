package com.android.kura_sanpo;

public class StationInfo {
	public String name; // 最寄駅名
	public String prev; // 前の駅名 （始発駅の場合は null）
	public String next; // 次の駅名 （終着駅の場合は null）
	public Double x; // 最寄駅の経度 （世界測地系）
	public Double y; // 最寄駅の緯度 （世界測地系）
	public int distance; // 指定の場所から最寄駅までの距離 （精度は 10 m）
	public String line; // 最寄駅の存在する路線名

	// 2点間の距離算出
	public String getDistance(double presentLat, double presentLon) {
		// 赤道半径(km)
		double r = 6378.137;

		// 駅の緯度と経度
		double lat1 = y * Math.PI / 180;
		double lng1 = x * Math.PI / 180;
		// 現在地の緯度と経度
		double lat2 = presentLat * Math.PI / 180;
		double lng2 = presentLon * Math.PI / 180;

		// 2点間の距離(m)
		double distance = 1000*(r * Math.acos(Math.sin(lat1) * Math.sin(lat2) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1))));

		return Double.toString(Math.round(distance));
	}
}
