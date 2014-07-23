package com.android.kura_sanpo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class ParseJsonpOfDirectionAPI {
	// JSON�f�[�^����͂���List�ŕԂ�
	public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
		String temp = "";

		List<List<HashMap<String, String>>> routes = new ArrayList<>();
		JSONArray jsonRoutes = null;
		JSONArray jsonLegs = null;
		JSONArray jsonSteps = null;

		try {
			jsonRoutes = jObject.getJSONArray("routes");

			// routes�v�f�����o��
			for (int i = 0; i < jsonRoutes.length(); i++) {
				jsonLegs = ((JSONObject) jsonRoutes.get(i)).getJSONArray("legs");

				// start�n�_�̏Z��
				String s_address = ((JSONObject) (JSONObject) jsonLegs.get(i)).getString("start_address");
				MainActivity.info_S = s_address;

				// goal�n�_�̏Z��
				String g_address = (String) ((JSONObject) (JSONObject) jsonLegs.get(i)).getString("end_address");
				MainActivity.info_G = g_address;

				// �����ikm)
				String distance_txt = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("distance")).getString("text");
				temp += "�ړ�����:" + distance_txt + "<br><br>";

				// �����im�j
				String distance_val = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("distance")).getString("value");
				temp += distance_val + "m" +  "<br><br>";
				
				// ���v����(��)
				String total_duration_txt = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("duration")).getString("text");
				temp += "���v����:" + total_duration_txt + "<br><br>";

				// ���v����(�b)
				String total_duration_val = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("duration")).getString("value");
				temp += total_duration_val + "�b" + "<br><br>";

				List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

				// legs�v�f�����o��
				for (int j = 0; j < jsonLegs.length(); j++) {
					jsonSteps = ((JSONObject) jsonLegs.get(j)).getJSONArray("steps");

					// steps�v�f�����o��
					for (int k = 0; k < jsonSteps.length(); k++) {
						// ���W�f�[�^�擾
						String polyline = "";
						polyline = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("polyline")).get("points");

						String instructions = (String) ((JSONObject) (JSONObject) jsonSteps.get(k)).getString("html_instructions");
						String duration_txt = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("duration")).getString("text");
						String duration_val = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("duration")).getString("value");

						temp += instructions + "/" + duration_val + "m/" + duration_txt + "<br><br>";

						// ���W�f�[�^���f�R�[�h
						List<LatLng> list = decodePolyline(polyline);

						// ���W�f�[�^�����X�g��HashMap�Ƃ��ēo�^
						for (int l = 0; l < list.size(); l++) {
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
							hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
							path.add(hm);
						}
					}

					// ���[�g���W�o�^
					routes.add(path);
				}

				// ���[�g���o�^
				MainActivity.posInfo = temp;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return routes;

	}

	// ���W�f�[�^���f�R�[�h
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
