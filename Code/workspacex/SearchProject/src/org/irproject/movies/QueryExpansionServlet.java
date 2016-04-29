package org.irproject.movies;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryExpansionServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			String data = request.getParameter("dat");
			System.out.println(data);
			String result = "";
			
			File stopFile = new File("C:\\stopwords");
			String wordnet = "C:\\dict";
			result = QueryExpansion.getExpandedQueryString(stopFile, data, wordnet);
			
			System.out.println(result);
			
			JSONArray solrJSONObj = ExternalAPICalls.solrCallQueryExp(result);
			
			
			JSONObject Jobj = new JSONObject();
			Jobj.put("eQuery", result);
			Jobj.put("qExp", solrJSONObj);
			
			
			response.getWriter().print(Jobj.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

}
