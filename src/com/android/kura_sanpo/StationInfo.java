package com.android.kura_sanpo;

public class StationInfo {
	public String name; // Åñw¼
	public String prev; // OÌw¼ in­wÌêÍ nullj
	public String next; // Ìw¼ iIwÌêÍ nullj
	public Double x; // ÅñwÌox i¢Eªnnj
	public Double y; // ÅñwÌÜx i¢Eªnnj
	public int distance; // wèÌê©çÅñwÜÅÌ£ i¸xÍ 10 mj
	public String line; // ÅñwÌ¶Ý·éHü¼

	// 2_ÔÌ£Zo
	public String getDistance(double presentLat, double presentLon) {
		// Ô¹¼a(km)
		double r = 6378.137;

		// wÌÜxÆox
		double lat1 = y * Math.PI / 180;
		double lng1 = x * Math.PI / 180;
		// »ÝnÌÜxÆox
		double lat2 = presentLat * Math.PI / 180;
		double lng2 = presentLon * Math.PI / 180;

		// 2_ÔÌ£(m)
		double distance = 1000*(r * Math.acos(Math.sin(lat1) * Math.sin(lat2) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1))));

		return Double.toString(Math.round(distance));
	}
}
