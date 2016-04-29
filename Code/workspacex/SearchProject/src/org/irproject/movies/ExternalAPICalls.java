package org.irproject.movies;

import java.io.BufferedReader;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExternalAPICalls {
	public static JSONArray bingCall(String searchText) throws IOException,
			JSONException {
		final String accountKey = "BkBXGnam30Rdtfk0kxkE+G/yGm90tag9wTGcj1Xo7Ms";
		final String bingUrlPattern = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%%27%s%%27&$format=JSON";

		final String query = URLEncoder.encode(searchText, Charset
				.defaultCharset().name());
		final String bingUrl = String.format(bingUrlPattern, query);

		final String accountKeyEnc = Base64.getEncoder().encodeToString(
				(accountKey + ":" + accountKey).getBytes());

		final URL url = new URL(bingUrl);
		final URLConnection connection = url.openConnection();
		connection
				.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

		try (final BufferedReader in = new BufferedReader(
				new InputStreamReader(connection.getInputStream()))) {
			String inputLine;
			final StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			final JSONObject json = new JSONObject(response.toString());
			final JSONObject d = json.getJSONObject("d");
			final JSONArray results = d.getJSONArray("results");
			return results;

		}
	}

	public static JSONArray googleCall(String searchText) throws IOException,
			JSONException {
		//String key = "AIzaSyCXXsZVYjq7api5ByUre07DQGWOGD0WYlw";
		String key = "AIzaSyAHaRk_K8eTVA9xrCaYEuMjult_zGyhyKM"; //
		String cref = "001501472147766648365:ay-2hmjwumw"; //
		URL url = new URL("https://www.googleapis.com/customsearch/v1?key="
				+ key + "&cx=" + cref + "&q="
				+ URLEncoder.encode(searchText, "UTF-8") + "&alt=json");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/json");
		BufferedReader br = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		final StringBuilder response = new StringBuilder();
		JSONArray items = null;
		while ((line = br.readLine()) != null) {
			response.append(line);
		}
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			items = obj.getJSONArray("items");

		}

		return items;
	}

	public static JSONArray solrCall(String searchText) throws IOException,
			JSONException {

		final String solrQuery = "http://ec2-54-191-183-57.us-west-2.compute.amazonaws.com:8983/solr/collection1/select?q=title"
				+ URLEncoder.encode(":\""+searchText+"\" " , "UTF-8")
				+ "~2&rows=100&wt=json&indent=true";

		System.out.println(solrQuery);
		final URL url = new URL(solrQuery);
		final URLConnection connection = url.openConnection();
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		JSONObject items = null;
		JSONArray dataArray = null;
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			items = obj.getJSONObject("response");
			dataArray = items.getJSONArray("docs");
		}

		return dataArray;
	}

	public static JSONArray solrCallQueryExp(String searchText) throws IOException,
	JSONException {

final String solrQuery = "http://ec2-54-191-183-57.us-west-2.compute.amazonaws.com:8983/solr/collection1/select?q=title"
		+ URLEncoder.encode(":"+searchText, "UTF-8")
		+ "~2&rows=100&wt=json&indent=true";

System.out.println(solrQuery);
final URL url = new URL(solrQuery);
final URLConnection connection = url.openConnection();
final BufferedReader in = new BufferedReader(new InputStreamReader(
		connection.getInputStream()));
String inputLine;
final StringBuilder response = new StringBuilder();
while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
}
JSONObject items = null;
JSONArray dataArray = null;
if (response != null) {
	JSONObject obj = new JSONObject(response.toString());
	// System.out.println(response.toString());
	items = obj.getJSONObject("response");
	dataArray = items.getJSONArray("docs");
}

return dataArray;
}

}
