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

	// �v���t�@�����X
	SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_window);

		// intent�擾
		Intent intent = getIntent();
		// intent����t�@�C���p�X�擾
		final String filePath = intent.getStringExtra("filePath");
		// ImageView�擾
		ImageView imgV = (ImageView) findViewById(R.id.image);
		// �摜�̃I�v�V�����w��
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		// ImageView�ɉ摜�ݒ�
		imgV.setImageBitmap(BitmapFactory.decodeFile(filePath, options));

		// ImageView�Ƀ��X�i�[�ݒ�
		imgV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// �摜�N���b�N��Activity�I��
				finish();
			}
		});

		// RatingBar�擾
		RatingBar rb = (RatingBar) findViewById(R.id.ratingBar);
		// Rating���ݒ�
		rb.setNumStars(5);
		// ���[�U�[���]���ł���悤�ɐݒ�
		rb.setIsIndicator(false);

		// �v���t�@�����X�̃C���X�^���X�擾
		pref = getSharedPreferences("rating", MODE_PRIVATE);
		// rating�ݒ�
		float rating = pref.getFloat(filePath, 0);
		rb.setRating(rating);

		// rating���ύX���ꂽ�Ƃ��̃��X�i�[�ݒ�
		rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				// �v���t�@�����X�ɓo�^
				Editor editor = pref.edit();
				editor.putFloat(filePath, rating);
				editor.commit();
			}
		});
		
	}
}
