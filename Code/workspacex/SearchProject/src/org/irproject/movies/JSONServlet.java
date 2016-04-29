package org.irproject.movies;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			String data = request.getParameter("dat");
			//System.out.println(data);
			JSONArray jsonArr = new JSONArray(data);

			JsonParser jsonParser = new JsonParser();
			JSONObject result = jsonParser.doCluster(jsonArr, Config.clustermap);
			//System.out.println(result.toString());
			JSONObject items = result.getJSONObject("response");
			JSONArray dataArray = items.getJSONArray("docs");
			
			//System.out.println(dataArray.toString());
			
			Hits hits = new Hits();
			JSONArray hitsJson = hits.getQueryResults(jsonArr.toString());
			
			//String expandedQuery = QueryExpansion.getExpandedQueryString(stopwords, query, wordnet);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("cluster", dataArray);
			jsonObject.put("hits", hitsJson);
			
			response.getWriter().print(jsonObject.toString());
			
		} catch (JSONException e) {
			
			e.printStackTrace();
		}

	}

}
