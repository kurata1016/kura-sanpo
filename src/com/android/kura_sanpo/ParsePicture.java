package com.android.kura_sanpo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.media.ExifInterface;

public class ParsePicture {
	// 写真情報のリスト
	private List<PictureInfo> pictureInfolist = new ArrayList<>();

	// getterメソッド
	public List<PictureInfo> getPictureInfo(String[] path) {
		// pathごとに情報取得
		for (int i = 0; i < path.length; i++) {
			try {
				// ExifInterfaceのインスタンス取得
				ExifInterface exif = new ExifInterface(path[i]);
				// 緯度経度情報とサムネイル情報があれば画像情報取得
				float[] latLong = new float[2];
				if (exif.getLatLong(latLong) && exif.hasThumbnail()) {
					// PictureInfoのインスタンス取得
					PictureInfo pictureInfo = new PictureInfo();
					// 緯度経度取得
					pictureInfo.setLatlong(latLong);
					// 日付取得
					String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
					pictureInfo.setDate(date);
					// サムネイル取得
					pictureInfo.setImage(exif.getThumbnail());
					// ファイルパス保持
					pictureInfo.setFilePath(path[i]);

					// listに追加
					pictureInfolist.add(pictureInfo);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pictureInfolist;
	}
}
