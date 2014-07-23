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

	// �R���X�g���N�^
	public CustomDialog(Context context, String title, String text) {
		super(context, R.style.Theme_CustomDialog);
		setContentView(R.layout.dialog);

		// View�擾
		this.title = (TextView) findViewById(R.id.dialog_title);
		this.text = (TextView) findViewById(R.id.dialog_text);
		this.button = (Button) findViewById(R.id.dialog_back);
		
		// �^�C�g���ݒ�
		this.title.setText(title);
		// �{���ݒ�
		this.text.setText(text);
		// �{�^���Ƀ��X�i�[�ݒ�
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

}
