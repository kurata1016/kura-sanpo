package com.android.kura_sanpo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.android.util.ParseJson;
import com.fasterxml.jackson.databind.JsonNode;

public class ParseNearBySta extends ParseJson {
	// 駅情報のリスト
	private List<StationInfo> stationInfo = new ArrayList<>();

	// getterメソッド
	public List<StationInfo> getStationInfo() {
		return stationInfo;
	}

	@Override
	public void loadJson(String str) {
		JsonNode root = getJsonNode(str);
		if (root != null) {
			//　配列要素からオブジェクトを取り出す
			// 最寄駅のイテレータを取得
			Iterator<JsonNode> ite = root.path("response").path("station").elements();

			// 要素の取り出し
			while (ite.hasNext()) {
				JsonNode j = ite.next();

				// 駅情報のセット
				StationInfo station = new StationInfo();
				station.x = j.path("x").asDouble();
				station.y = j.path("y").asDouble();
				station.name = j.path("name").asText();
				station.next = j.path("next").asText();
				station.prev = j.path("prev").asText();
				station.line = j.path("line").asText();
				
				// 距離を数値に変換
				station.distance = Integer.parseInt(j.path("distance").asText().split("m")[0]);

				// リストに追加
				stationInfo.add(station);
			}
		}
	}
}
