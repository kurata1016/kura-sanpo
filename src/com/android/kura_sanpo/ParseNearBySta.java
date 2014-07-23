package com.android.kura_sanpo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.android.util.ParseJson;
import com.fasterxml.jackson.databind.JsonNode;

public class ParseNearBySta extends ParseJson {
	// �w���̃��X�g
	private List<StationInfo> stationInfo = new ArrayList<>();

	// getter���\�b�h
	public List<StationInfo> getStationInfo() {
		return stationInfo;
	}

	@Override
	public void loadJson(String str) {
		JsonNode root = getJsonNode(str);
		if (root != null) {
			//�@�z��v�f����I�u�W�F�N�g�����o��
			// �Ŋ�w�̃C�e���[�^���擾
			Iterator<JsonNode> ite = root.path("response").path("station").elements();

			// �v�f�̎��o��
			while (ite.hasNext()) {
				JsonNode j = ite.next();

				// �w���̃Z�b�g
				StationInfo station = new StationInfo();
				station.x = j.path("x").asDouble();
				station.y = j.path("y").asDouble();
				station.name = j.path("name").asText();
				station.next = j.path("next").asText();
				station.prev = j.path("prev").asText();
				station.line = j.path("line").asText();
				
				// �����𐔒l�ɕϊ�
				station.distance = Integer.parseInt(j.path("distance").asText().split("m")[0]);

				// ���X�g�ɒǉ�
				stationInfo.add(station);
			}
		}
	}
}
