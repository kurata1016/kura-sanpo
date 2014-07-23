package com.android.kura_sanpo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ParseNearByLand {
	// �����h�}�[�N���̃��X�g
	private List<LandmarkInfo> landmarkInfo = new ArrayList<>();

	// getter���\�b�h
	public List<LandmarkInfo> getLandmarkInfo() {
		return landmarkInfo;
	}

	// xml��̓��\�b�h
	public void loadxml(String str) {
		// ���i�[�p
		LandmarkInfo landmark = new LandmarkInfo();
		LandmarkInfo landmark2 = new LandmarkInfo();
		try {
			// ������
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			// ������ǂݍ���
			parser.setInput(new StringReader(str));

			// �C�x���g�擾
			int eventType = parser.getEventType();
			// �h�L�������g�I�[�܂Ń��[�v����
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
				} else if (eventType == XmlPullParser.START_TAG) {
					// �����h�}�[�N���擾
					if (parser.getName().equals("Landmark1")) {
						landmark.landmark = parser.nextText();
						// �ܓx�擾
					} else if (parser.getName().equals("Landmark1Lat")) {
						landmark.lat = Double.parseDouble(parser.nextText());
						// �o�x�擾
					} else if (parser.getName().equals("Landmark1Lon")) {
						landmark.lon = Double.parseDouble(parser.nextText());
						// �����h�}�[�N���擾
					} else if (parser.getName().equals("Landmark2")) {
						landmark2.landmark = parser.nextText();
						// �ܓx�擾
					} else if (parser.getName().equals("Landmark2Lat")) {
						landmark2.lat = Double.parseDouble(parser.nextText());
						// �o�x�擾
					} else if (parser.getName().equals("Landmark2Lon")) {
						landmark2.lon = Double.parseDouble(parser.nextText());
					}
				}
				// ���̃^�O�֑J�ڂ����[�v
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// List�ɏ��ǉ�
		landmarkInfo.add(landmark);
		landmarkInfo.add(landmark2);
	}
}
