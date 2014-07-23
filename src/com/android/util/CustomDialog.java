package com.android.util;

import com.android.kura_sanpo.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialog extends Dialog {
	// TitleView
	private TextView title;
	// TextView
	private TextView text;
	// ButtonView
	private Button button;

	// コンストラクタ
	public CustomDialog(Context context, String title, String text) {
		super(context, R.style.Theme_CustomDialog);
		setContentView(R.layout.dialog);

		// View取得
		this.title = (TextView) findViewById(R.id.dialog_title);
		this.text = (TextView) findViewById(R.id.dialog_text);
		this.button = (Button) findViewById(R.id.dialog_back);
		
		// タイトル設定
		this.title.setText(title);
		// 本文設定
		this.text.setText(text);
		// ボタンにリスナー設定
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

}
