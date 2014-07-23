package com.android.kura_sanpo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class ImageViewActivity extends Activity {

	// プリファレンス
	SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_window);

		// intent取得
		Intent intent = getIntent();
		// intentからファイルパス取得
		final String filePath = intent.getStringExtra("filePath");
		// ImageView取得
		ImageView imgV = (ImageView) findViewById(R.id.image);
		// 画像のオプション指定
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		// ImageViewに画像設定
		imgV.setImageBitmap(BitmapFactory.decodeFile(filePath, options));

		// ImageViewにリスナー設定
		imgV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 画像クリックでActivity終了
				finish();
			}
		});

		// RatingBar取得
		RatingBar rb = (RatingBar) findViewById(R.id.ratingBar);
		// Rating数設定
		rb.setNumStars(5);
		// ユーザーが評価できるように設定
		rb.setIsIndicator(false);

		// プリファレンスのインスタンス取得
		pref = getSharedPreferences("rating", MODE_PRIVATE);
		// rating設定
		float rating = pref.getFloat(filePath, 0);
		rb.setRating(rating);

		// ratingが変更されたときのリスナー設定
		rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				// プリファレンスに登録
				Editor editor = pref.edit();
				editor.putFloat(filePath, rating);
				editor.commit();
			}
		});
		
	}
}
