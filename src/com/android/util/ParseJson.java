package com.android.util;

import java.io.IOException;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Json��̓N���X
public class ParseJson {
	protected String content;
	
	protected JsonNode getJsonNode(String str){
		try {
			// JSON��������AJsonNode�I�u�W�F�N�g�ɕϊ�����
			return new ObjectMapper().readTree(str);
		} catch (IOException e) {
			Log.e(getClass().getName(), e.getMessage());
		}
		return null;
	}
	
	public void loadJson(String str){
	}
}
