package com.android.kura_sanpo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ParseNearByLand {
	// ランドマーク情報のリスト
	private List<LandmarkInfo> landmarkInfo = new ArrayList<>();

	// getterメソッド
	public List<LandmarkInfo> getLandmarkInfo() {
		return landmarkInfo;
	}

	// xml解析メソッド
	public void loadxml(String str) {
		// 情報格納用
		LandmarkInfo landmark = new LandmarkInfo();
		LandmarkInfo landmark2 = new LandmarkInfo();
		try {
			// 初期化
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			// 文字列読み込み
			parser.setInput(new StringReader(str));

			// イベント取得
			int eventType = parser.getEventType();
			// ドキュメント終端までループ処理
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
				} else if (eventType == XmlPullParser.START_TAG) {
					// ランドマーク名取得
					if (parser.getName().equals("Landmark1")) {
						landmark.landmark = parser.nextText();
						// 緯度取得
					} else if (parser.getName().equals("Landmark1Lat")) {
						landmark.lat = Double.parseDouble(parser.nextText());
						// 経度取得
					} else if (parser.getName().equals("Landmark1Lon")) {
						landmark.lon = Double.parseDouble(parser.nextText());
						// ランドマーク名取得
					} else if (parser.getName().equals("Landmark2")) {
						landmark2.landmark = parser.nextText();
						// 緯度取得
					} else if (parser.getName().equals("Landmark2Lat")) {
						landmark2.lat = Double.parseDouble(parser.nextText());
						// 経度取得
					} else if (parser.getName().equals("Landmark2Lon")) {
						landmark2.lon = Double.parseDouble(parser.nextText());
					}
				}
				// 次のタグへ遷移しループ
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Listに情報追加
		landmarkInfo.add(landmark);
		landmarkInfo.add(landmark2);
	}
}
