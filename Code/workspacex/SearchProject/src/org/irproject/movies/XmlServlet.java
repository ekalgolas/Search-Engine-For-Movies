package org.irproject.movies;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class XmlServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		
		String query = request.getParameter("query");

		ExternalAPICalls sm = new ExternalAPICalls();
		try {
			
			/*JSONParser parser = new JSONParser();
			parser.doGetCluster("{}");*/
			
			JSONArray googleJSONObj = ExternalAPICalls.googleCall(query);
			JSONArray bingJSONObj = ExternalAPICalls.bingCall(query);
			JSONArray solrJSONObj = ExternalAPICalls.solrCall(query);

			JSONObject jsonObject = new JSONObject();

			jsonObject.put("google", googleJSONObj);
			jsonObject.put("bing", bingJSONObj);
			jsonObject.put("solr", solrJSONObj);

			response.getWriter().print(jsonObject.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
