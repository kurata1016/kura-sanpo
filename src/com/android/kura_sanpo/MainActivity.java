package com.android.kura_sanpo;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.util.CustomDialog;
import com.android.util.CustomInfoAdapter;
import com.android.util.HttpAsyncLoader;
import com.android.util.HttpAsyncXmlLoader;
import com.android.util.MyCalc;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnCameraChangeListener, OnMyLocationButtonClickListener,
		ConnectionCallbacks, LoaderCallbacks<String> {
	// Mapオブジェクト
	private static GoogleMap map;
	// infoMarkerオブジェクト
	private Marker info_marker;
	private Marker pic_marker;
	// マーカーと駅情報のHashMap
	private HashMap<Marker, StationInfo> stationMarkerMap = new HashMap<>();
	// マーカーとランドマーク情報のHashMap
	private HashMap<Marker, LandmarkInfo> landmarkMarkerMap = new HashMap<>();
	// マーカーと画像情報のHashMap
	private static HashMap<Marker, PictureInfo> pictureMarkerMap = new HashMap<>();
	// LocationClientオブジェクト
	private LocationClient locationClient = null;
	// ロケーションリクエスト設定
	private static final LocationRequest FIRST_REQUEST = LocationRequest.create().setInterval(1000).setFastestInterval(16).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	// カメラ位置オブジェクト
	CameraPosition centerCameraPos;
	// 現在地の緯度、経度
	double presentLat, presentLon;
	// フラグ
	boolean mflag = false;
	boolean info_flag = false;
	// 定数
	String NEAR_BY = "nearby";
	String ROUTE_SERCH = "route";
	// progressDialog
	public static ProgressDialog progressDialog;
	// ルート検索用marker
	ArrayList<LatLng> markerPoints;
	// MarkerOption
	public static MarkerOptions options;
	// ルート検索トラベルモードデフォルト値
	public String travelMode = "driving";
	// ルート検索スタート地点住所
	public static String info_S = "";
	// ルート検索ゴール地点住所
	public static String info_G = "";
	// ルート検索情報
	public static String posInfo = "";
	// ルート検索ポイント
	public LatLng point;
	// ルート検索方法配列
	public String[] items = { "driving", "walking", "bicycling", "transit" };
	public String[] travelBy = { "自動車", "徒歩", "自転車", "公共交通機関" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// MapFragmentの取得
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		// ImageViewの取得
		ImageView camera = (ImageView) findViewById(R.id.camera);
		camera.setOnClickListener(new CameraOnClickListener());

		// プログレスバー設定
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("ルート検索中");
		progressDialog.hide();

		// ルート検索用マーカー初期化
		markerPoints = new ArrayList<LatLng>();

		try {
			// Mapオブジェクト取得
			map = mapFragment.getMap();
			// Activityが初めて生成されたとき(onCreateのたびに初期化しないように)
			if (savedInstanceState == null) {
				// Fragmentを保存設定(Activity再生成時に以前の状態のオブジェクトが復元)
				mapFragment.setRetainInstance(true);
			}
		} catch (Exception e) {
			Log.e("Error", "google map が使用できません");
		}

		// 現在位置ボタンの表示
		map.setMyLocationEnabled(true);
		// 現在地取得
		locationClient = new LocationClient(getApplicationContext(), this, this);
		if (locationClient != null)
			// Google Play Servicesに接続
			// 正しく接続されるとonConnectedがコールされる
			locationClient.connect();

		// 地図の中心位置を取得
		centerCameraPos = map.getCameraPosition();
		// info_flagがtrueなら
		if (info_flag) {
			// 駅情報APIのURLを準備してLoader初期化
			execNearBySta(centerCameraPos);
			// ランドマーク情報APIのURLを準備してLoader初期化
			execNearByLand(centerCameraPos);
		}

		// カスタムinfoWindow設定
		if (!info_flag)
			map.setInfoWindowAdapter(new CustomInfoAdapter(this));

		// 端末内画像データを地図にプロット
		setPicture();

		// 地図を移動したときのリスナー設定
		map.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition cameraPos) {

				// 前回から500メートル以上離れたら
				if (0.5 < MyCalc.getDistance(centerCameraPos, cameraPos)) {
					// API取得メソッド起動
					// info_flagがtrueなら
					if (info_flag) {
						execNearBySta(cameraPos);
						execNearByLand(cameraPos);
					}
					centerCameraPos = cameraPos;
				}
			}
		});

		// 吹き出しのクリックリスナーを追加
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// markerをkeyにして、各HashMapからオブジェクトをgetする
				StationInfo staOjt = stationMarkerMap.get(marker);
				LandmarkInfo landOjt = landmarkMarkerMap.get(marker);
				PictureInfo picOjt = pictureMarkerMap.get(marker);

				Toast ts = null;
				if (staOjt != null) {
					// 駅情報取り出し
					StationInfo info = stationMarkerMap.get(marker);
					ts = Toast.makeText(getBaseContext(), info.name + "(" + info.getDistance(presentLat, presentLon) + "m)\n" + "前の駅:" + info.prev + "\n次の駅:" + info.next + "\n" + info.line,
							Toast.LENGTH_LONG);

				} else if (landOjt != null) {
					// ランドマーク情報取り出し
					final LandmarkInfo info = landmarkMarkerMap.get(marker);

					// ダイアログを表示
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("アクションを選択してください");
					builder.setItems(new String[] { "この場所をGoogleで検索する", "この場所までのルートを検索する" }, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							// google検索
							case 0:
								Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
								intent.putExtra(SearchManager.QUERY, info.landmark);
								startActivity(intent);
								break;
							// ルート検索
							case 1:
								// 現在地の緯度経度取得
								LatLng presentLatLng = new LatLng(presentLat, presentLon);
								// ランドマークの緯度経度取得
								LatLng latlng = new LatLng(info.lat, info.lon);
								// ルート検索用マーカーポイントに登録
								if (markerPoints.size() > 1)
									markerPoints.clear();
								// 現在地の緯度経度登録
								markerPoints.add(presentLatLng);
								// ランドマークの緯度経度登録
								markerPoints.add(latlng);
								// ルート検索
								routeSerch();
								break;
							}
						}
					});

					// ダイアログ表示
					builder.create().show();

				} else if (picOjt != null) {
					// 写真表示
					PictureInfo info = pictureMarkerMap.get(marker);
					Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
					intent.putExtra("filePath", info.getFilePath());
					startActivity(intent);
				}

				// Toast表示
				if (ts != null) {
					ts.setGravity(Gravity.TOP, 0, 200);
					ts.show();
				}

			}
		});

		// mapをクリックしたときのリスナー設定(ルート検索)
		map.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				// スタート地点とゴール地点以外は必要ないので
				if (markerPoints.size() > 1) {
					markerPoints.clear();
					map.clear();
					if (!info_flag)
						setPicture();
				}

				MainActivity.this.point = point;

				// ダイアログ表示
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
				if (markerPoints.size() == 0) {
					alertDialogBuilder.setTitle("ここをスタート地点にして経路検索を行いますか？");
				} else if (markerPoints.size() == 1) {
					alertDialogBuilder.setTitle("ここをゴール地点にして経路検索を行いますか？");
				}
				alertDialogBuilder.setPositiveButton("はい", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						// listにタップしたポイントを追加
						markerPoints.add(MainActivity.this.point);
						options = new MarkerOptions();
						options.position(MainActivity.this.point);

						// タップしたポイントがひとつ目なら
						if (markerPoints.size() == 1) {
							options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
							options.title("Start");
							// タップしたポイントがふたつ目なら
						} else if (markerPoints.size() == 2) {
							options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
							options.title("Goal");
						}

						// マーカー設置
						map.addMarker(options);

						// マーカーが2つ設置されたらルート案内実行
						if (markerPoints.size() >= 2)
							routeSerch();
					}
				});
				alertDialogBuilder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				// ダイアログ表示
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
		});
	}

	// 画像データを地図にプロット
	private void setPicture() {
		// filePathを取得
		String[] path = getFilePath();
		// 画像情報取得し、マーカーに反映
		ParsePicture parsePic = new ParsePicture();
		for (PictureInfo info : parsePic.getPictureInfo(path)) {

			// マーカー設置
			MarkerOptions options = new MarkerOptions().position(info.getLatLong()).title(info.getDate()).icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_camera));
			pic_marker = map.addMarker(options);

			// マーカーと駅情報を保管
			pictureMarkerMap.put(pic_marker, info);
		}
	}

	// Cameraクリックリスナー
	public class CameraOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			startActivity(intent);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// 現在位置ボタンの表示
		map.setMyLocationEnabled(true);
		if (!info_flag)
			// 写真を地図にプロット
			setPicture();
	}

	// ルート検索メソッド
	private void routeSerch() {
		// プログレスバー表示
		progressDialog.show();
		// start地点の緯度経度取得
		LatLng origin = markerPoints.get(0);
		// goal地点の緯度経度取得
		LatLng dest = markerPoints.get(1);
		// ルート検索APIのURL生成
		String url = getDirectionsUrl(origin, dest);
		// bundleオブジェクトで情報受け渡し
		Bundle bundle = new Bundle();
		// ルート検索APIのURL
		bundle.putString(ROUTE_SERCH, url);
		// LoaderManager初期化後、onCreateLoaderをコール
		getLoaderManager().restartLoader(2, bundle, this);
	}

	// ルート検索APIのURL生成
	private String getDirectionsUrl(LatLng origin, LatLng dest) {
		String parameters;
		long time;
		// start地点の緯度経度
		String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
		// goal地点の緯度経度
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
		// デバイスからの取得かどうか
		String sensor = "sensor=false";
		// 乗換案内のとき
		// 出発時刻
		if (travelMode.equals(items[3])) {
			time = (long) Math.floor(System.currentTimeMillis() / 1000);
			parameters = str_origin + "&" + str_dest + "&" + sensor + "&language=ja" + "&departure_time=" + time + "&mode=" + travelMode;
		} else {
			// パラメータ
			parameters = str_origin + "&" + str_dest + "&" + sensor + "&language=ja" + "&mode=" + travelMode;
		}
		// データ受け渡し形式指定
		String output = "json";
		// APIのURL生成
		String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
		return url;
	}

	// 地図の中心位置を取得して、駅情報APIのURLを準備する
	public void execNearBySta(CameraPosition cameraPos) {
		// bundleオブジェクトで情報受け渡し
		Bundle bundle = new Bundle();
		// 緯度
		bundle.putString("y", Double.toString(cameraPos.target.latitude));
		// 経度
		bundle.putString("x", Double.toString(cameraPos.target.longitude));
		// webAPIのURL
		bundle.putString(NEAR_BY, "http://express.heartrails.com/api/json?method=getStations&");
		// LoaderManager初期化後、onCreateLoaderをコール
		getLoaderManager().restartLoader(0, bundle, this);
	}

	// 地図の中心位置を取得して、ランドマーク情報APIのURLを準備する
	private void execNearByLand(CameraPosition cameraPos) {
		// bundleオブジェクトで情報受け渡し
		Bundle bundle = new Bundle();
		// 緯度
		bundle.putString("y", Double.toString(cameraPos.target.latitude));
		// 経度
		bundle.putString("x", Double.toString(cameraPos.target.longitude));
		// webAPIのURL
		bundle.putString(NEAR_BY, "http://geocode.didit.jp/reverse/?");
		// LoaderManager初期化後、onCreateLoaderをコール
		getLoaderManager().restartLoader(1, bundle, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// ルート詳細
		case R.id.route_info:
			show_routeInfo();
			return true;

			// Legal Notices(免責事項)
		case R.id.legal_notices:
			String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(MainActivity.this);
			LicenseDialog.setTitle("Legal Notices");
			LicenseDialog.setMessage(LicenseInfo);
			LicenseDialog.show();
			return true;

			// ルートモード変更
		case R.id.route_mode:
			change_routeMode();
			return true;

			// 設定変更
		case R.id.change_infoSetting:
			change_setting();
			return true;
		}
		return false;
	}

	// 設定変更
	private void change_setting() {
		// ダイアログ表示
		// info_flagがtrueなら
		if (info_flag) {
			new AlertDialog.Builder(MainActivity.this).setTitle("地図上の駅・ランドマーク情報を非表示にしますか？").setPositiveButton("はい", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					info_flag = false;
					Toast.makeText(MainActivity.this, "設定を変更しました", Toast.LENGTH_SHORT).show();
					info_marker.remove();
					map.clear();
					map.setInfoWindowAdapter(new CustomInfoAdapter(MainActivity.this));
					setPicture();
				}
			}).setNegativeButton("いいえ", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
		} else {
			new AlertDialog.Builder(MainActivity.this).setTitle("地図上に駅・ランドマーク情報を表示しますか？").setPositiveButton("はい", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					info_flag = true;
					Toast.makeText(MainActivity.this, "設定を変更しました", Toast.LENGTH_SHORT).show();
					map.clear();
					map.setInfoWindowAdapter(null);
					// 地図の中心位置を取得
					centerCameraPos = map.getCameraPosition();
					execNearByLand(centerCameraPos);
					execNearBySta(centerCameraPos);
				}
			}).setNegativeButton("いいえ", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
		}
	}

	// ルート情報表示
	private void show_routeInfo() {
		// すでにルートが検索されていたら情報表示
		if (markerPoints.size() == 2) {
			// 本文設定
			String route_info = posInfo;
			// 文字列操作
			route_info = route_info.replaceAll("<br>", "\n");
			route_info = route_info.replaceAll("<b>", "");
			route_info = route_info.replaceAll("</b>", "");
			route_info = route_info.replace("<div style=\"font-size:0.9em\">", "\t");
			route_info = route_info.replace("</div>", "");
			// ダイアログ表示
			new CustomDialog(this, "ルート情報詳細", route_info).show();
		} else {
			Toast.makeText(this, "表示するルート情報がありません", Toast.LENGTH_SHORT).show();
		}
	}

	// ルート検索方法を変更
	private void change_routeMode() {
		// ダイアログ表示
		new AlertDialog.Builder(MainActivity.this).setTitle("ルート検索方法を選択してください").setSingleChoiceItems(travelBy, getNum(), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				travelMode = items[which];
				Toast.makeText(MainActivity.this, "ルート検索方法を" + travelBy[which] + "へ変更しました。", Toast.LENGTH_SHORT).show();
			}
		}).setNegativeButton("閉じる", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

				// スタート地点とゴール地点が設定されていれば再検索
				if (markerPoints.size() == 2) {
					re_routeSerch();
				}
			}
		}).show();
	}

	// travelModeがitemsの何番目か得る
	private int getNum() {
		for (int i = 0; i < items.length; i++) {
			if (travelMode.equals(items[i]))
				return i;
		}
		return 0;
	}

	// ルート再検索
	private void re_routeSerch() {
		// プログレスダイアログ表示
		progressDialog.show();

		// start地点とgoal地点設定
		LatLng origin = markerPoints.get(0);
		LatLng dest = markerPoints.get(1);

		// mapを一度クリア
		map.clear();
		setPicture();

		// マーカー設置
		// startマーカー
		options = new MarkerOptions();
		options.position(origin);
		options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		options.title("S");
		options.draggable(true);
		map.addMarker(options);

		// goalマーカー
		options = new MarkerOptions();
		options.position(dest);
		options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		options.title("G");
		options.draggable(true);
		map.addMarker(options);

		// URL生成
		String url = getDirectionsUrl(origin, dest);
		// bundleオブジェクトで情報受け渡し
		Bundle bundle = new Bundle();
		// ルート検索APIのURL
		bundle.putString(ROUTE_SERCH, url);
		// LoaderManager初期化後、onCreateLoaderをコール
		getLoaderManager().restartLoader(2, bundle, this);
	}

	@Override
	public boolean onMyLocationButtonClick() {
		return false;
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onConnected(Bundle arg0) {
		// 位置取得リクエストを投げる
		// 取得できるとonLocationChangedがコールされる
		locationClient.requestLocationUpdates(FIRST_REQUEST, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// 取得したlocationをつかってCameraPositionインスタンス生成
		// 初回のみ
		if (!mflag) {
			// 現在地の緯度経度取得
			presentLat = location.getLatitude();
			presentLon = location.getLongitude();
			centerCameraPos = new CameraPosition.Builder().target(new LatLng(presentLat, presentLon)).zoom(16.5f).bearing(0).build();
			// カメラ移動
			map.moveCamera(CameraUpdateFactory.newCameraPosition(centerCameraPos));
			// フラグ変更
			mflag = true;
		}
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	// LoaderManager初期化後にコールバック
	@Override
	public Loader<String> onCreateLoader(int id, Bundle bundle) {
		HttpAsyncLoader loader = null;
		switch (id) {
		// 駅情報
		case 0:
			// リクエストURLの組み立て
			String urlSta = bundle.getString(NEAR_BY) + "x=" + bundle.getString("x") + "&" + "y=" + bundle.getString("y");
			// HttpAsyncLoaderの生成
			loader = new HttpAsyncLoader(this, urlSta);

			// WebAPIにアクセス
			// アクセス後、onLoadFinishedをコール
			loader.forceLoad();
			break;
		// ランドマーク情報
		case 1:
			// リクエストURLの組み立て
			String urlLand = bundle.getString(NEAR_BY) + "lat=" + bundle.getString("y") + "&" + "lon=" + bundle.getString("x");
			// HttpAsyncLoaderの生成
			loader = new HttpAsyncXmlLoader(this, urlLand);

			// WebAPIにアクセス
			// アクセス後、onLoadFinishedをコール
			loader.forceLoad();
			break;
		// ルート検索
		case 2:
			// HttpAsyncLoaderの生成
			loader = new HttpAsyncLoader(this, bundle.getString(ROUTE_SERCH));

			// WebAPIにアクセス
			// アクセス後、onLoadFinishedをコール
			loader.forceLoad();
			break;
		}
		return loader;
	}

	// APIアクセス後にコールバック
	@Override
	public void onLoadFinished(Loader<String> loader, String data) {
		// API取得に失敗した場合
		if (data == null) {
			return;
		}

		switch (loader.getId()) {
		// 駅情報
		case 0:
			// APIの結果を解析
			ParseNearBySta parseSta = new ParseNearBySta();
			parseSta.loadJson(data);

			// APIの結果をマーカーに反映
			for (StationInfo info : parseSta.getStationInfo()) {
				info_marker = map.addMarker(new MarkerOptions().position(new LatLng(info.y, info.x)).title(info.name).snippet(info.line)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_train)));

				// マーカーと駅情報を保管
				stationMarkerMap.put(info_marker, info);
			}
			break;
		// ランドマーク情報
		case 1:
			// APIの結果を解析
			ParseNearByLand parseLand = new ParseNearByLand();
			parseLand.loadxml(data);

			// APIの結果をマーカーに反映
			for (LandmarkInfo info : parseLand.getLandmarkInfo()) {
				info_marker = map.addMarker(new MarkerOptions().position(new LatLng(info.lat, info.lon)).title(info.landmark).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_landmark)));

				// マーカーと駅情報を保管
				landmarkMarkerMap.put(info_marker, info);
			}
			break;
		// ルート検索
		case 2:
			// APIの結果を解析して地図にルートを描画
			ParseTask parseTask = new ParseTask(this);
			parseTask.execute(data);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {
	}

	// mapインスタンスのゲッター
	public static GoogleMap getMap() {
		return map;
	}

	// pictureMarkerMapゲッター
	public static HashMap<Marker, PictureInfo> getPictureMarkerMap() {
		return pictureMarkerMap;
	}

	// FilePath取得メソッド
	public String[] getFilePath() {
		Uri image_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = getContentResolver().query(image_uri, null, null, null, null);
		cursor.moveToFirst();
		String[] path = new String[cursor.getCount()];
		for (int i = 0; i < path.length; i++) {
			path[i] = cursor.getString(1);
			cursor.moveToNext();
		}
		cursor.close();
		return path;
	}
}
