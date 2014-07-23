package com.android.kura_sanpo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class ParseJsonpOfDirectionAPI {
	// JSONデータを解析してListで返す
	public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
		String temp = "";

		List<List<HashMap<String, String>>> routes = new ArrayList<>();
		JSONArray jsonRoutes = null;
		JSONArray jsonLegs = null;
		JSONArray jsonSteps = null;

		try {
			jsonRoutes = jObject.getJSONArray("routes");

			// routes要素内取り出し
			for (int i = 0; i < jsonRoutes.length(); i++) {
				jsonLegs = ((JSONObject) jsonRoutes.get(i)).getJSONArray("legs");

				// start地点の住所
				String s_address = ((JSONObject) (JSONObject) jsonLegs.get(i)).getString("start_address");
				MainActivity.info_S = s_address;

				// goal地点の住所
				String g_address = (String) ((JSONObject) (JSONObject) jsonLegs.get(i)).getString("end_address");
				MainActivity.info_G = g_address;

				// 距離（km)
				String distance_txt = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("distance")).getString("text");
				temp += "移動距離:" + distance_txt + "<br><br>";

				// 距離（m）
				String distance_val = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("distance")).getString("value");
				temp += distance_val + "m" +  "<br><br>";
				
				// 所要時間(分)
				String total_duration_txt = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("duration")).getString("text");
				temp += "所要時間:" + total_duration_txt + "<br><br>";

				// 所要時間(秒)
				String total_duration_val = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("duration")).getString("value");
				temp += total_duration_val + "秒" + "<br><br>";

				List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

				// legs要素内取り出し
				for (int j = 0; j < jsonLegs.length(); j++) {
					jsonSteps = ((JSONObject) jsonLegs.get(j)).getJSONArray("steps");

					// steps要素内取り出し
					for (int k = 0; k < jsonSteps.length(); k++) {
						// 座標データ取得
						String polyline = "";
						polyline = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("polyline")).get("points");

						String instructions = (String) ((JSONObject) (JSONObject) jsonSteps.get(k)).getString("html_instructions");
						String duration_txt = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("duration")).getString("text");
						String duration_val = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("duration")).getString("value");

						temp += instructions + "/" + duration_val + "m/" + duration_txt + "<br><br>";

						// 座標データをデコード
						List<LatLng> list = decodePolyline(polyline);

						// 座標データをリストへHashMapとして登録
						for (int l = 0; l < list.size(); l++) {
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
							hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
							path.add(hm);
						}
					}

					// ルート座標登録
					routes.add(path);
				}

				// ルート情報登録
				MainActivity.posInfo = temp;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return routes;

	}

	// 座標データをデコード
	private List<LatLng> decodePolyline(String encoded) {
		List<LatLng> polyline = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
			polyline.add(p);
		}

		return polyline;
	}
}
