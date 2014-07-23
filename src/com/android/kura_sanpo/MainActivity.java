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
	// Map�I�u�W�F�N�g
	private static GoogleMap map;
	// infoMarker�I�u�W�F�N�g
	private Marker info_marker;
	private Marker pic_marker;
	// �}�[�J�[�Ɖw����HashMap
	private HashMap<Marker, StationInfo> stationMarkerMap = new HashMap<>();
	// �}�[�J�[�ƃ����h�}�[�N����HashMap
	private HashMap<Marker, LandmarkInfo> landmarkMarkerMap = new HashMap<>();
	// �}�[�J�[�Ɖ摜����HashMap
	private static HashMap<Marker, PictureInfo> pictureMarkerMap = new HashMap<>();
	// LocationClient�I�u�W�F�N�g
	private LocationClient locationClient = null;
	// ���P�[�V�������N�G�X�g�ݒ�
	private static final LocationRequest FIRST_REQUEST = LocationRequest.create().setInterval(1000).setFastestInterval(16).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	// �J�����ʒu�I�u�W�F�N�g
	CameraPosition centerCameraPos;
	// ���ݒn�̈ܓx�A�o�x
	double presentLat, presentLon;
	// �t���O
	boolean mflag = false;
	boolean info_flag = false;
	// �萔
	String NEAR_BY = "nearby";
	String ROUTE_SERCH = "route";
	// progressDialog
	public static ProgressDialog progressDialog;
	// ���[�g�����pmarker
	ArrayList<LatLng> markerPoints;
	// MarkerOption
	public static MarkerOptions options;
	// ���[�g�����g���x�����[�h�f�t�H���g�l
	public String travelMode = "driving";
	// ���[�g�����X�^�[�g�n�_�Z��
	public static String info_S = "";
	// ���[�g�����S�[���n�_�Z��
	public static String info_G = "";
	// ���[�g�������
	public static String posInfo = "";
	// ���[�g�����|�C���g
	public LatLng point;
	// ���[�g�������@�z��
	public String[] items = { "driving", "walking", "bicycling", "transit" };
	public String[] travelBy = { "������", "�k��", "���]��", "������ʋ@��" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// MapFragment�̎擾
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		// ImageView�̎擾
		ImageView camera = (ImageView) findViewById(R.id.camera);
		camera.setOnClickListener(new CameraOnClickListener());

		// �v���O���X�o�[�ݒ�
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("���[�g������");
		progressDialog.hide();

		// ���[�g�����p�}�[�J�[������
		markerPoints = new ArrayList<LatLng>();

		try {
			// Map�I�u�W�F�N�g�擾
			map = mapFragment.getMap();
			// Activity�����߂Đ������ꂽ�Ƃ�(onCreate�̂��тɏ��������Ȃ��悤��)
			if (savedInstanceState == null) {
				// Fragment��ۑ��ݒ�(Activity�Đ������ɈȑO�̏�Ԃ̃I�u�W�F�N�g������)
				mapFragment.setRetainInstance(true);
			}
		} catch (Exception e) {
			Log.e("Error", "google map ���g�p�ł��܂���");
		}

		// ���݈ʒu�{�^���̕\��
		map.setMyLocationEnabled(true);
		// ���ݒn�擾
		locationClient = new LocationClient(getApplicationContext(), this, this);
		if (locationClient != null)
			// Google Play Services�ɐڑ�
			// �������ڑ�������onConnected���R�[�������
			locationClient.connect();

		// �n�}�̒��S�ʒu���擾
		centerCameraPos = map.getCameraPosition();
		// info_flag��true�Ȃ�
		if (info_flag) {
			// �w���API��URL����������Loader������
			execNearBySta(centerCameraPos);
			// �����h�}�[�N���API��URL����������Loader������
			execNearByLand(centerCameraPos);
		}

		// �J�X�^��infoWindow�ݒ�
		if (!info_flag)
			map.setInfoWindowAdapter(new CustomInfoAdapter(this));

		// �[�����摜�f�[�^��n�}�Ƀv���b�g
		setPicture();

		// �n�}���ړ������Ƃ��̃��X�i�[�ݒ�
		map.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition cameraPos) {

				// �O�񂩂�500���[�g���ȏ㗣�ꂽ��
				if (0.5 < MyCalc.getDistance(centerCameraPos, cameraPos)) {
					// API�擾���\�b�h�N��
					// info_flag��true�Ȃ�
					if (info_flag) {
						execNearBySta(cameraPos);
						execNearByLand(cameraPos);
					}
					centerCameraPos = cameraPos;
				}
			}
		});

		// �����o���̃N���b�N���X�i�[��ǉ�
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// marker��key�ɂ��āA�eHashMap����I�u�W�F�N�g��get����
				StationInfo staOjt = stationMarkerMap.get(marker);
				LandmarkInfo landOjt = landmarkMarkerMap.get(marker);
				PictureInfo picOjt = pictureMarkerMap.get(marker);

				Toast ts = null;
				if (staOjt != null) {
					// �w�����o��
					StationInfo info = stationMarkerMap.get(marker);
					ts = Toast.makeText(getBaseContext(), info.name + "(" + info.getDistance(presentLat, presentLon) + "m)\n" + "�O�̉w:" + info.prev + "\n���̉w:" + info.next + "\n" + info.line,
							Toast.LENGTH_LONG);

				} else if (landOjt != null) {
					// �����h�}�[�N�����o��
					final LandmarkInfo info = landmarkMarkerMap.get(marker);

					// �_�C�A���O��\��
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("�A�N�V������I�����Ă�������");
					builder.setItems(new String[] { "���̏ꏊ��Google�Ō�������", "���̏ꏊ�܂ł̃��[�g����������" }, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							// google����
							case 0:
								Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
								intent.putExtra(SearchManager.QUERY, info.landmark);
								startActivity(intent);
								break;
							// ���[�g����
							case 1:
								// ���ݒn�̈ܓx�o�x�擾
								LatLng presentLatLng = new LatLng(presentLat, presentLon);
								// �����h�}�[�N�̈ܓx�o�x�擾
								LatLng latlng = new LatLng(info.lat, info.lon);
								// ���[�g�����p�}�[�J�[�|�C���g�ɓo�^
								if (markerPoints.size() > 1)
									markerPoints.clear();
								// ���ݒn�̈ܓx�o�x�o�^
								markerPoints.add(presentLatLng);
								// �����h�}�[�N�̈ܓx�o�x�o�^
								markerPoints.add(latlng);
								// ���[�g����
								routeSerch();
								break;
							}
						}
					});

					// �_�C�A���O�\��
					builder.create().show();

				} else if (picOjt != null) {
					// �ʐ^�\��
					PictureInfo info = pictureMarkerMap.get(marker);
					Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
					intent.putExtra("filePath", info.getFilePath());
					startActivity(intent);
				}

				// Toast�\��
				if (ts != null) {
					ts.setGravity(Gravity.TOP, 0, 200);
					ts.show();
				}

			}
		});

		// map���N���b�N�����Ƃ��̃��X�i�[�ݒ�(���[�g����)
		map.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				// �X�^�[�g�n�_�ƃS�[���n�_�ȊO�͕K�v�Ȃ��̂�
				if (markerPoints.size() > 1) {
					markerPoints.clear();
					map.clear();
					if (!info_flag)
						setPicture();
				}

				MainActivity.this.point = point;

				// �_�C�A���O�\��
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
				if (markerPoints.size() == 0) {
					alertDialogBuilder.setTitle("�������X�^�[�g�n�_�ɂ��Čo�H�������s���܂����H");
				} else if (markerPoints.size() == 1) {
					alertDialogBuilder.setTitle("�������S�[���n�_�ɂ��Čo�H�������s���܂����H");
				}
				alertDialogBuilder.setPositiveButton("�͂�", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						// list�Ƀ^�b�v�����|�C���g��ǉ�
						markerPoints.add(MainActivity.this.point);
						options = new MarkerOptions();
						options.position(MainActivity.this.point);

						// �^�b�v�����|�C���g���ЂƂڂȂ�
						if (markerPoints.size() == 1) {
							options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
							options.title("Start");
							// �^�b�v�����|�C���g���ӂ��ڂȂ�
						} else if (markerPoints.size() == 2) {
							options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
							options.title("Goal");
						}

						// �}�[�J�[�ݒu
						map.addMarker(options);

						// �}�[�J�[��2�ݒu���ꂽ�烋�[�g�ē����s
						if (markerPoints.size() >= 2)
							routeSerch();
					}
				});
				alertDialogBuilder.setNegativeButton("������", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				// �_�C�A���O�\��
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
		});
	}

	// �摜�f�[�^��n�}�Ƀv���b�g
	private void setPicture() {
		// filePath���擾
		String[] path = getFilePath();
		// �摜���擾���A�}�[�J�[�ɔ��f
		ParsePicture parsePic = new ParsePicture();
		for (PictureInfo info : parsePic.getPictureInfo(path)) {

			// �}�[�J�[�ݒu
			MarkerOptions options = new MarkerOptions().position(info.getLatLong()).title(info.getDate()).icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_camera));
			pic_marker = map.addMarker(options);

			// �}�[�J�[�Ɖw����ۊ�
			pictureMarkerMap.put(pic_marker, info);
		}
	}

	// Camera�N���b�N���X�i�[
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
		// ���݈ʒu�{�^���̕\��
		map.setMyLocationEnabled(true);
		if (!info_flag)
			// �ʐ^��n�}�Ƀv���b�g
			setPicture();
	}

	// ���[�g�������\�b�h
	private void routeSerch() {
		// �v���O���X�o�[�\��
		progressDialog.show();
		// start�n�_�̈ܓx�o�x�擾
		LatLng origin = markerPoints.get(0);
		// goal�n�_�̈ܓx�o�x�擾
		LatLng dest = markerPoints.get(1);
		// ���[�g����API��URL����
		String url = getDirectionsUrl(origin, dest);
		// bundle�I�u�W�F�N�g�ŏ��󂯓n��
		Bundle bundle = new Bundle();
		// ���[�g����API��URL
		bundle.putString(ROUTE_SERCH, url);
		// LoaderManager��������AonCreateLoader���R�[��
		getLoaderManager().restartLoader(2, bundle, this);
	}

	// ���[�g����API��URL����
	private String getDirectionsUrl(LatLng origin, LatLng dest) {
		String parameters;
		long time;
		// start�n�_�̈ܓx�o�x
		String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
		// goal�n�_�̈ܓx�o�x
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
		// �f�o�C�X����̎擾���ǂ���
		String sensor = "sensor=false";
		// �抷�ē��̂Ƃ�
		// �o������
		if (travelMode.equals(items[3])) {
			time = (long) Math.floor(System.currentTimeMillis() / 1000);
			parameters = str_origin + "&" + str_dest + "&" + sensor + "&language=ja" + "&departure_time=" + time + "&mode=" + travelMode;
		} else {
			// �p�����[�^
			parameters = str_origin + "&" + str_dest + "&" + sensor + "&language=ja" + "&mode=" + travelMode;
		}
		// �f�[�^�󂯓n���`���w��
		String output = "json";
		// API��URL����
		String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
		return url;
	}

	// �n�}�̒��S�ʒu���擾���āA�w���API��URL����������
	public void execNearBySta(CameraPosition cameraPos) {
		// bundle�I�u�W�F�N�g�ŏ��󂯓n��
		Bundle bundle = new Bundle();
		// �ܓx
		bundle.putString("y", Double.toString(cameraPos.target.latitude));
		// �o�x
		bundle.putString("x", Double.toString(cameraPos.target.longitude));
		// webAPI��URL
		bundle.putString(NEAR_BY, "http://express.heartrails.com/api/json?method=getStations&");
		// LoaderManager��������AonCreateLoader���R�[��
		getLoaderManager().restartLoader(0, bundle, this);
	}

	// �n�}�̒��S�ʒu���擾���āA�����h�}�[�N���API��URL����������
	private void execNearByLand(CameraPosition cameraPos) {
		// bundle�I�u�W�F�N�g�ŏ��󂯓n��
		Bundle bundle = new Bundle();
		// �ܓx
		bundle.putString("y", Double.toString(cameraPos.target.latitude));
		// �o�x
		bundle.putString("x", Double.toString(cameraPos.target.longitude));
		// webAPI��URL
		bundle.putString(NEAR_BY, "http://geocode.didit.jp/reverse/?");
		// LoaderManager��������AonCreateLoader���R�[��
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
		// ���[�g�ڍ�
		case R.id.route_info:
			show_routeInfo();
			return true;

			// Legal Notices(�Ɛӎ���)
		case R.id.legal_notices:
			String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(MainActivity.this);
			LicenseDialog.setTitle("Legal Notices");
			LicenseDialog.setMessage(LicenseInfo);
			LicenseDialog.show();
			return true;

			// ���[�g���[�h�ύX
		case R.id.route_mode:
			change_routeMode();
			return true;

			// �ݒ�ύX
		case R.id.change_infoSetting:
			change_setting();
			return true;
		}
		return false;
	}

	// �ݒ�ύX
	private void change_setting() {
		// �_�C�A���O�\��
		// info_flag��true�Ȃ�
		if (info_flag) {
			new AlertDialog.Builder(MainActivity.this).setTitle("�n�}��̉w�E�����h�}�[�N�����\���ɂ��܂����H").setPositiveButton("�͂�", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					info_flag = false;
					Toast.makeText(MainActivity.this, "�ݒ��ύX���܂���", Toast.LENGTH_SHORT).show();
					info_marker.remove();
					map.clear();
					map.setInfoWindowAdapter(new CustomInfoAdapter(MainActivity.this));
					setPicture();
				}
			}).setNegativeButton("������", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
		} else {
			new AlertDialog.Builder(MainActivity.this).setTitle("�n�}��ɉw�E�����h�}�[�N����\�����܂����H").setPositiveButton("�͂�", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					info_flag = true;
					Toast.makeText(MainActivity.this, "�ݒ��ύX���܂���", Toast.LENGTH_SHORT).show();
					map.clear();
					map.setInfoWindowAdapter(null);
					// �n�}�̒��S�ʒu���擾
					centerCameraPos = map.getCameraPosition();
					execNearByLand(centerCameraPos);
					execNearBySta(centerCameraPos);
				}
			}).setNegativeButton("������", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
		}
	}

	// ���[�g���\��
	private void show_routeInfo() {
		// ���łɃ��[�g����������Ă�������\��
		if (markerPoints.size() == 2) {
			// �{���ݒ�
			String route_info = posInfo;
			// �����񑀍�
			route_info = route_info.replaceAll("<br>", "\n");
			route_info = route_info.replaceAll("<b>", "");
			route_info = route_info.replaceAll("</b>", "");
			route_info = route_info.replace("<div style=\"font-size:0.9em\">", "\t");
			route_info = route_info.replace("</div>", "");
			// �_�C�A���O�\��
			new CustomDialog(this, "���[�g���ڍ�", route_info).show();
		} else {
			Toast.makeText(this, "�\�����郋�[�g��񂪂���܂���", Toast.LENGTH_SHORT).show();
		}
	}

	// ���[�g�������@��ύX
	private void change_routeMode() {
		// �_�C�A���O�\��
		new AlertDialog.Builder(MainActivity.this).setTitle("���[�g�������@��I�����Ă�������").setSingleChoiceItems(travelBy, getNum(), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				travelMode = items[which];
				Toast.makeText(MainActivity.this, "���[�g�������@��" + travelBy[which] + "�֕ύX���܂����B", Toast.LENGTH_SHORT).show();
			}
		}).setNegativeButton("����", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

				// �X�^�[�g�n�_�ƃS�[���n�_���ݒ肳��Ă���΍Č���
				if (markerPoints.size() == 2) {
					re_routeSerch();
				}
			}
		}).show();
	}

	// travelMode��items�̉��Ԗڂ�����
	private int getNum() {
		for (int i = 0; i < items.length; i++) {
			if (travelMode.equals(items[i]))
				return i;
		}
		return 0;
	}

	// ���[�g�Č���
	private void re_routeSerch() {
		// �v���O���X�_�C�A���O�\��
		progressDialog.show();

		// start�n�_��goal�n�_�ݒ�
		LatLng origin = markerPoints.get(0);
		LatLng dest = markerPoints.get(1);

		// map����x�N���A
		map.clear();
		setPicture();

		// �}�[�J�[�ݒu
		// start�}�[�J�[
		options = new MarkerOptions();
		options.position(origin);
		options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		options.title("S");
		options.draggable(true);
		map.addMarker(options);

		// goal�}�[�J�[
		options = new MarkerOptions();
		options.position(dest);
		options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		options.title("G");
		options.draggable(true);
		map.addMarker(options);

		// URL����
		String url = getDirectionsUrl(origin, dest);
		// bundle�I�u�W�F�N�g�ŏ��󂯓n��
		Bundle bundle = new Bundle();
		// ���[�g����API��URL
		bundle.putString(ROUTE_SERCH, url);
		// LoaderManager��������AonCreateLoader���R�[��
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
		// �ʒu�擾���N�G�X�g�𓊂���
		// �擾�ł����onLocationChanged���R�[�������
		locationClient.requestLocationUpdates(FIRST_REQUEST, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// �擾����location��������CameraPosition�C���X�^���X����
		// ����̂�
		if (!mflag) {
			// ���ݒn�̈ܓx�o�x�擾
			presentLat = location.getLatitude();
			presentLon = location.getLongitude();
			centerCameraPos = new CameraPosition.Builder().target(new LatLng(presentLat, presentLon)).zoom(16.5f).bearing(0).build();
			// �J�����ړ�
			map.moveCamera(CameraUpdateFactory.newCameraPosition(centerCameraPos));
			// �t���O�ύX
			mflag = true;
		}
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	// LoaderManager��������ɃR�[���o�b�N
	@Override
	public Loader<String> onCreateLoader(int id, Bundle bundle) {
		HttpAsyncLoader loader = null;
		switch (id) {
		// �w���
		case 0:
			// ���N�G�X�gURL�̑g�ݗ���
			String urlSta = bundle.getString(NEAR_BY) + "x=" + bundle.getString("x") + "&" + "y=" + bundle.getString("y");
			// HttpAsyncLoader�̐���
			loader = new HttpAsyncLoader(this, urlSta);

			// WebAPI�ɃA�N�Z�X
			// �A�N�Z�X��AonLoadFinished���R�[��
			loader.forceLoad();
			break;
		// �����h�}�[�N���
		case 1:
			// ���N�G�X�gURL�̑g�ݗ���
			String urlLand = bundle.getString(NEAR_BY) + "lat=" + bundle.getString("y") + "&" + "lon=" + bundle.getString("x");
			// HttpAsyncLoader�̐���
			loader = new HttpAsyncXmlLoader(this, urlLand);

			// WebAPI�ɃA�N�Z�X
			// �A�N�Z�X��AonLoadFinished���R�[��
			loader.forceLoad();
			break;
		// ���[�g����
		case 2:
			// HttpAsyncLoader�̐���
			loader = new HttpAsyncLoader(this, bundle.getString(ROUTE_SERCH));

			// WebAPI�ɃA�N�Z�X
			// �A�N�Z�X��AonLoadFinished���R�[��
			loader.forceLoad();
			break;
		}
		return loader;
	}

	// API�A�N�Z�X��ɃR�[���o�b�N
	@Override
	public void onLoadFinished(Loader<String> loader, String data) {
		// API�擾�Ɏ��s�����ꍇ
		if (data == null) {
			return;
		}

		switch (loader.getId()) {
		// �w���
		case 0:
			// API�̌��ʂ����
			ParseNearBySta parseSta = new ParseNearBySta();
			parseSta.loadJson(data);

			// API�̌��ʂ��}�[�J�[�ɔ��f
			for (StationInfo info : parseSta.getStationInfo()) {
				info_marker = map.addMarker(new MarkerOptions().position(new LatLng(info.y, info.x)).title(info.name).snippet(info.line)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_train)));

				// �}�[�J�[�Ɖw����ۊ�
				stationMarkerMap.put(info_marker, info);
			}
			break;
		// �����h�}�[�N���
		case 1:
			// API�̌��ʂ����
			ParseNearByLand parseLand = new ParseNearByLand();
			parseLand.loadxml(data);

			// API�̌��ʂ��}�[�J�[�ɔ��f
			for (LandmarkInfo info : parseLand.getLandmarkInfo()) {
				info_marker = map.addMarker(new MarkerOptions().position(new LatLng(info.lat, info.lon)).title(info.landmark).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_landmark)));

				// �}�[�J�[�Ɖw����ۊ�
				landmarkMarkerMap.put(info_marker, info);
			}
			break;
		// ���[�g����
		case 2:
			// API�̌��ʂ���͂��Ēn�}�Ƀ��[�g��`��
			ParseTask parseTask = new ParseTask(this);
			parseTask.execute(data);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {
	}

	// map�C���X�^���X�̃Q�b�^�[
	public static GoogleMap getMap() {
		return map;
	}

	// pictureMarkerMap�Q�b�^�[
	public static HashMap<Marker, PictureInfo> getPictureMarkerMap() {
		return pictureMarkerMap;
	}

	// FilePath�擾���\�b�h
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
