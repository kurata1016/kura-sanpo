package com.android.kura_sanpo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
	// ポリライン情報
	private PolylineOptions lineOptions = new PolylineOptions();

	@Override
	protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
		JSONObject jObject;
		List<List<HashMap<String, String>>> routes = null;

		try {
			jObject = new JSONObject(jsonData[0]);
			ParseJsonpOfDirectionAPI parser = new ParseJsonpOfDirectionAPI();

			// JSONデータを解析してルート情報をListで返す
			routes = parser.parse(jObject);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return routes;
	}

	// ルート検索で得た座標を使って経路表示
	@Override
	protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
		ArrayList<LatLng> points = null;
		lineOptions = null;

		if (routes.size() != 0) {
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<>();
				lineOptions = new PolylineOptions();

				// ルート情報からポイントごとの緯度経度取得し、リスト化
				List<HashMap<String, String>> path = routes.get(i);
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					// 緯度取得
					double lat = Double.parseDouble(point.get("lat"));
					// 経度取得
					double lng = Double.parseDouble(point.get("lng"));
					// 緯度経度情報をポイント登録
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}

				// ポリライン設定
				lineOptions.addAll(points);
				lineOptions.width(10);
				lineOptions.color(Color.BLUE);
			}

			// ルート描画
			MainActivity.getMap().addPolyline(lineOptions);
			
			// プログレスバー非表示
			MainActivity.progressDialog.hide();

		} else {
			Log.d("ルート取得エラー", "ルートを取得できませんでした");
		}

	}

	// polylineのゲッターメソッド
	public PolylineOptions getPolyline() {
		return lineOptions;
	}
}
