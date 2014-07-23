package com.android.kura_sanpo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

public class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
	// �|�����C�����
	private PolylineOptions lineOptions = new PolylineOptions();
	// Context�ێ�
	Context context;

	// �R���X�g���N�^
	public ParseTask(Context context) {
		this.context = context;
	}

	@Override
	protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
		JSONObject jObject;
		List<List<HashMap<String, String>>> routes = null;

		try {
			jObject = new JSONObject(jsonData[0]);
			ParseJsonpOfDirectionAPI parser = new ParseJsonpOfDirectionAPI();

			// JSON�f�[�^����͂��ă��[�g����List�ŕԂ�
			routes = parser.parse(jObject);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return routes;
	}

	// ���[�g�����œ������W���g���Čo�H�\��
	@Override
	protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
		ArrayList<LatLng> points = null;
		lineOptions = null;

		if (routes.size() != 0) {
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<>();
				lineOptions = new PolylineOptions();

				// ���[�g��񂩂�|�C���g���Ƃ̈ܓx�o�x�擾���A���X�g��
				List<HashMap<String, String>> path = routes.get(i);
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					// �ܓx�擾
					double lat = Double.parseDouble(point.get("lat"));
					// �o�x�擾
					double lng = Double.parseDouble(point.get("lng"));
					// �ܓx�o�x�����|�C���g�o�^
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}

				// �|�����C���ݒ�
				lineOptions.addAll(points);
				lineOptions.width(10);
				lineOptions.color(Color.BLUE);
			}

			// ���[�g�`��
			MainActivity.getMap().addPolyline(lineOptions);

			// �v���O���X�o�[��\��
			MainActivity.progressDialog.hide();

		} else {
			// Toast�\��
			Toast.makeText(context, "���[�g�������ł��܂���ł���", Toast.LENGTH_SHORT).show();
			// �v���O���X�o�[��\��
			MainActivity.progressDialog.hide();
		}

	}

	// polyline�̃Q�b�^�[���\�b�h
	public PolylineOptions getPolyline() {
		return lineOptions;
	}
}
