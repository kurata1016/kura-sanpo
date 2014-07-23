package com.android.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

public class HttpAsyncLoader extends AsyncTaskLoader<String> {
	// WebAPI��URL
	private String url = null;
	// responsebody�I�u�W�F�N�g
	String responseBody;

	// �R���X�g���N�^
	public HttpAsyncLoader(Context context, String url) {
		super(context);
		this.url = url;
	}

	// �񓯊�����
	@Override
	public String loadInBackground() {
		// DefaultHttpClient�I�u�W�F�N�g�擾
		HttpClient httpClient = new DefaultHttpClient();

		try {
			// HttpGet��WebAPI����f�[�^�擾
			// ResponseHandler�I�u�W�F�N�g��HTTP�ʐM�̉�����M
			responseBody = httpClient.execute(new HttpGet(this.url), new ResponseHandler<String>() {

				// ��M�f�[�^�ɓ��{�ꂪ�܂܂��ꍇ�A�v�f�R�[�h
				// UTF-8�Ńf�R�[�h���邽��handleResponse��Override
				@Override
				// HTTP�ʐM�̉�������handleResponse���\�b�h���R�[���o�b�N�����
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					// ���X�|���X�R�[�h��HttpStatus.SC_OK�iHTTP 200�j�̏ꍇ�̂݁A���ʂ�Ԃ�
					if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
						// �f�R�[�h
						return EntityUtils.toString(response.getEntity(), "UTF-8");
					}
					return null;
				}
			});
			
			return responseBody;
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage());
		} finally {
			// �ʐM�I�����͐ڑ������
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

}
