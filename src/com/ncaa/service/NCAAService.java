package com.ncaa.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ncaa.dao.DataDaoImpl;

@Transactional
@Service
public class NCAAService {
	
	@Autowired
	private DataDaoImpl dataDaoImpl;
	
	 public JSONArray getHistorical(String year) {
		 return dataDaoImpl.getHistorical(year);
	 }

	 public JSONArray getPredictions(String year) {
		 return dataDaoImpl.getPredictions(year);
	 }

}
