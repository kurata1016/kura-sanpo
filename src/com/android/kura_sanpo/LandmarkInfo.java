package com.android.kura_sanpo;

public class LandmarkInfo {
	public String landmark; // 1�ԋ߂������h�}�[�N
	public double lat; // �ܓx
	public double lon; // �o�x

	// 2�_�Ԃ̋����Z�o
	public String getDistance(double presentLat, double presentLon) {
		// �ԓ����a(km)
		double r = 6378.137;

		// �w�̈ܓx�ƌo�x
		double lat1 = lat * Math.PI / 180;
		double lng1 = lon * Math.PI / 180;
		// ���ݒn�̈ܓx�ƌo�x
		double lat2 = presentLat * Math.PI / 180;
		double lng2 = presentLon * Math.PI / 180;

		// 2�_�Ԃ̋���(m)
		double distance = 1000 * (r * Math.acos(Math.sin(lat1) * Math.sin(lat2) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1))));

		return Double.toString(Math.round(distance));
	}
}
