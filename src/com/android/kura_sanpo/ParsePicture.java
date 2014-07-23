package com.android.kura_sanpo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.media.ExifInterface;

public class ParsePicture {
	// �ʐ^���̃��X�g
	private List<PictureInfo> pictureInfolist = new ArrayList<>();

	// getter���\�b�h
	public List<PictureInfo> getPictureInfo(String[] path) {
		// path���Ƃɏ��擾
		for (int i = 0; i < path.length; i++) {
			try {
				// ExifInterface�̃C���X�^���X�擾
				ExifInterface exif = new ExifInterface(path[i]);
				// �ܓx�o�x���ƃT���l�C����񂪂���Ή摜���擾
				float[] latLong = new float[2];
				if (exif.getLatLong(latLong) && exif.hasThumbnail()) {
					// PictureInfo�̃C���X�^���X�擾
					PictureInfo pictureInfo = new PictureInfo();
					// �ܓx�o�x�擾
					pictureInfo.setLatlong(latLong);
					// ���t�擾
					String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
					pictureInfo.setDate(date);
					// �T���l�C���擾
					pictureInfo.setImage(exif.getThumbnail());
					// �t�@�C���p�X�ێ�
					pictureInfo.setFilePath(path[i]);

					// list�ɒǉ�
					pictureInfolist.add(pictureInfo);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pictureInfolist;
	}
}
