package com.android.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.kura_sanpo.MainActivity;
import com.android.kura_sanpo.PictureInfo;
import com.android.kura_sanpo.R;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoAdapter implements com.google.android.gms.maps.GoogleMap.InfoWindowAdapter {
	
	private View mWindow;
	
	public CustomInfoAdapter(MainActivity mainActivity){
		mWindow = mainActivity.getLayoutInflater().inflate(R.layout.info_window, null);
	}

	@Override
	public View getInfoContents(Marker marker) {
		// タイトル設定
		TextView title = (TextView)mWindow.findViewById(R.id.info_title);
		title.setText(marker.getTitle());
		// 画像設定
		ImageView img = (ImageView) mWindow.findViewById(R.id.info_image);
		PictureInfo info = MainActivity.getPictureMarkerMap().get(marker);
		img.setImageBitmap(info.getBitmap());
		return mWindow;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
