package com.android.kura_sanpo;

public class StationInfo {
	public String name; // �Ŋ�w��
	public String prev; // �O�̉w�� �i�n���w�̏ꍇ�� null�j
	public String next; // ���̉w�� �i�I���w�̏ꍇ�� null�j
	public Double x; // �Ŋ�w�̌o�x �i���E���n�n�j
	public Double y; // �Ŋ�w�̈ܓx �i���E���n�n�j
	public int distance; // �w��̏ꏊ����Ŋ�w�܂ł̋��� �i���x�� 10 m�j
	public String line; // �Ŋ�w�̑��݂���H����

	// 2�_�Ԃ̋����Z�o
	public String getDistance(double presentLat, double presentLon) {
		// �ԓ����a(km)
		double r = 6378.137;

		// �w�̈ܓx�ƌo�x
		double lat1 = y * Math.PI / 180;
		double lng1 = x * Math.PI / 180;
		// ���ݒn�̈ܓx�ƌo�x
		double lat2 = presentLat * Math.PI / 180;
		double lng2 = presentLon * Math.PI / 180;

		// 2�_�Ԃ̋���(m)
		double distance = 1000*(r * Math.acos(Math.sin(lat1) * Math.sin(lat2) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1))));

		return Double.toString(Math.round(distance));
	}
}
