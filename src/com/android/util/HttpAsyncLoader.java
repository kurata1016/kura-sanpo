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
	// WebAPIのURL
	private String url = null;
	// responsebodyオブジェクト
	String responseBody;

	// コンストラクタ
	public HttpAsyncLoader(Context context, String url) {
		super(context);
		this.url = url;
	}

	// 非同期処理
	@Override
	public String loadInBackground() {
		// DefaultHttpClientオブジェクト取得
		HttpClient httpClient = new DefaultHttpClient();

		try {
			// HttpGetでWebAPIからデータ取得
			// ResponseHandlerオブジェクトでHTTP通信の応答受信
			responseBody = httpClient.execute(new HttpGet(this.url), new ResponseHandler<String>() {

				// 受信データに日本語が含まれる場合、要デコード
				// UTF-8でデコードするためhandleResponseをOverride
				@Override
				// HTTP通信の応答時にhandleResponseメソッドがコールバックされる
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					// レスポンスコードがHttpStatus.SC_OK（HTTP 200）の場合のみ、結果を返す
					if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
						// デコード
						return EntityUtils.toString(response.getEntity(), "UTF-8");
					}
					return null;
				}
			});
			
			return responseBody;
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage());
		} finally {
			// 通信終了時は接続を閉じる
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

}
