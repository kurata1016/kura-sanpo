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
	// WebAPIのURL
	private String url = null;
	// responsebodyオブジェクト
	String responseBody;

	// コンストラクタ
	public HttpAsyncXmlLoader(Context context, String url) {
		super(context, url);
		this.url = url;
	}

	// 非同期処理
	@Override
	public String loadInBackground() {
		// HTTPクライアント設定
		HttpClient httpClient = new DefaultHttpClient();
		InputStream in = null;

		try {
			// xmlゲット
			HttpResponse res = httpClient.execute(new HttpGet(this.url));
			// inputStreamで取得
			in = res.getEntity().getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 通信終了時は接続を閉じる
			httpClient.getConnectionManager().shutdown();
		}

		// inputStreamからStringへ
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		try {
			// 1行ずつStringBuilderへ追加
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
