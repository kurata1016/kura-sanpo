package com.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

public class HttpAsyncXmlLoader extends HttpAsyncLoader {
	// WebAPI��URL
	private String url = null;
	// responsebody�I�u�W�F�N�g
	String responseBody;

	// �R���X�g���N�^
	public HttpAsyncXmlLoader(Context context, String url) {
		super(context, url);
		this.url = url;
	}

	// �񓯊�����
	@Override
	public String loadInBackground() {
		// HTTP�N���C�A���g�ݒ�
		HttpClient httpClient = new DefaultHttpClient();
		InputStream in = null;

		try {
			// xml�Q�b�g
			HttpResponse res = httpClient.execute(new HttpGet(this.url));
			// inputStream�Ŏ擾
			in = res.getEntity().getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// �ʐM�I�����͐ڑ������
			httpClient.getConnectionManager().shutdown();
		}

		// inputStream����String��
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		try {
			// 1�s����StringBuilder�֒ǉ�
			while ((responseBody = br.readLine()) != null) {
				sb.append(responseBody);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
}
