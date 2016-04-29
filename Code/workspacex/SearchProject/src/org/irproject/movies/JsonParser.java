package org.irproject.movies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
	static Map<Integer, TreeSet<String>> mainmap;
	public static Map<String, Integer> clustermap;

	public static JSONObject doCluster(JSONArray jsonData,
			Map<String, Integer> clusterMap) throws JSONException {
		clustermap = clusterMap;
		int maxvalue = 0;
		int finalcluster = 0;
		Map<Integer, Integer> countmap = new TreeMap<Integer, Integer>();
		Integer clustervalue = 0;
		//JSONObject j = jsonData.getJSONObject("response");
		JSONArray myarray = jsonData;

		int i = 0;
		
	//	System.out.println(myarray.toString());
		// Finding the clusters in top 10 results
		while(i<5 && i< myarray.length()) {
			JSONObject b = myarray.getJSONObject(i);
			String url = b.getString("id");
			clustervalue = checkcluster(url);

			if (countmap.containsKey(clustervalue)) {
				countmap.put(clustervalue, countmap.get(clustervalue) + 1);
			} else {
				countmap.put(clustervalue, 1);
			}
			i++;
		}
		// Finding which value in countmap is repeated most, to find the most
		// relevant cluster and assign to finalcluster

		for (Map.Entry<Integer, Integer> entry : countmap.entrySet()) {
			if (entry.getValue() > maxvalue && entry.getKey()!=13) {
				maxvalue = entry.getValue();
				finalcluster = entry.getKey();
			}
		}

	/*	if (finalcluster == 0) {
			for (Map.Entry<Integer, Integer> entry : countmap.entrySet()) {
				if (entry.getValue() > maxvalue) {
					maxvalue = entry.getValue();
					finalcluster = entry.getKey();
					break;
				}
			}
		} */
		// Creating the return JSON object

		JSONArray docarray = new JSONArray();
		for (int j = 0; j < myarray.length(); j++) {
			JSONObject b = myarray.getJSONObject(j);
			String url = b.getString("id");
			// Filtering results based on only predominant cluster
			if (checkcluster(url) == finalcluster) {
				docarray.put(b);
				
			} 
		}

		JSONObject response = new JSONObject();
		response.put("docs", docarray);
		JSONObject fullresponse = new JSONObject();
		fullresponse.put("response", response);

		
		
		return fullresponse;
	}

	/*
	 * Method to find the cluster number
	 */
	private static Integer checkcluster(String url) {
		Integer cluster = 0;
		if (clustermap.containsKey(url)) {
			cluster = clustermap.get(url);
		} else
			cluster = 0;

		if (cluster != null)
			return cluster;
		else
			return 0;
	}

	/*
	 * public static JSONObject getCluster(String jsonData) throws JSONException
	 * { JSONObject jsonObj = new JSONObject(jsonData); String pageName =
	 * jsonObj.getString("name"); JSONObject returnObj = new JSONObject();
	 * 
	 * 
	 * return null; }
	 */

	public static void main(String[] args) throws JSONException {
		mainmap = new HashMap<Integer, TreeSet<String>>();
		clustermap = new HashMap<String, Integer>();

	}

	
}
