package org.irproject.movies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;


/*import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;*/
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to gather information about tokens in the Cranfield database
 *
 * @author Ekal.Golas
 */
public class QueryExpansion
{
	/**
	 * Main function
	 *
	 * @param args
	 *            Command line arguments
	 * @throws IOException
	 * @throws JSONException
	 */
	/*public static void main(final String args[]) throws IOException, JSONException
	{
		// Validate command line arguments
		final CommandLine cmd = validateArguments(args);
		final File stopwords = new File(cmd.getOptionValue("stop"));
		final String wordnet = "C:\\dict";

		final String query = "kung fu";
		final long start = System.currentTimeMillis();
		final String expanded = getExpandedQueryString(stopwords, query, wordnet);
		System.out.println("Time taken: " + (System.currentTimeMillis() - start) + " ms\n");
		System.out.println(expanded);
	}*/

	/**
	 * @param stopwords
	 * @param query
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws FileNotFoundException
	 */
	public static String getExpandedQueryString(final File stopwords, final String query, final String wordnet)
			throws IOException,
			MalformedURLException,
			JSONException,
			FileNotFoundException {
		final Element[][] elements = getExpandedQuery(stopwords, query, wordnet);

		final List<Element> list = new ArrayList<>();
		for (final Element[] elements2 : elements) {
			for (final Element element : elements2) {
				if (element != null) {
					list.add(element);
				}
			}
		}

		Collections.sort(list, new Comparator<Element>() {
			@Override
			public int compare(final Element o1, final Element o2) {
				return o1.value >= o2.value ? 1 : -1;
			}
		});

		final LinkedHashSet<String> set = new LinkedHashSet<>();
		for (int i = list.size() - 1; i > 0; i--) {
			set.add(list.get(i).v);
		}

		return query + " " + String.join(" ", set);
	}

	/**
	 * @param stopwords
	 * @param query
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws FileNotFoundException
	 */
	public static Element[][] getExpandedQuery(final File stopwords, final String query, final String wordnet)
			throws IOException,
			MalformedURLException,
			JSONException,
			FileNotFoundException {
		/*final InputStream inputStream = new URL("http://ec2-54-191-183-57.us-west-2.compute.amazonaws.com:8983/solr/collection1/select?q=title%3A" +
				String.join("+", query.split(" ")) + "~10&wt=json&indent=true").openStream();

		String parsed = "";
		try {
			parsed = IOUtils.toString(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}*/
		
		
		final String solrQuery = "http://ec2-54-191-183-57.us-west-2.compute.amazonaws.com:8983/solr/collection1/select?q=title"
				+ URLEncoder.encode(":"+String.join("+", query.split(" ")), "UTF-8")
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
		
		String parsed = response.toString();

		System.out.println(parsed);
		
		
		

		final JSONObject json = new JSONObject(parsed);
		final JSONArray arr = json.getJSONObject("response").getJSONArray("docs");
		final String[] documents = new String[10];
		for (int i = 0; i < 10; i++)
		{
			try {
				documents[i] = arr.getJSONObject(i).getString("content");
			} catch (final Exception e) {
				break;
			}
		}

		// Call document parser
		final Parser parser = new Parser(stopwords);
		parser.parse(Arrays.asList(documents));
		// Display results
		final HashMap<String, Map<Integer, Integer>> tokenMap = parser.getTokenMap();

		// Call stemming
		final Stemming stemming = new Stemming(wordnet, query);
		stemming.stem(tokenMap);
		final Map<String, Set<String>> stemsMap = stemming.getStemsMap();

		return metricClusters(tokenMap, stemsMap, query);
	}

	/**
	 * @param tokenMap
	 * @param stemsMap
	 * @return
	 */
	public static Element[][] metricClusters(final HashMap<String, Map<Integer, Integer>> tokenMap, final Map<String, Set<String>> stemsMap, final String query) {
		final Element[][] matrix = new Element[stemsMap.size()][stemsMap.size()];
		final String[] stems = stemsMap.keySet().toArray(new String[stemsMap.size()]);
		for (int i = 0; i < stems.length; i++) {
			for (int j = 0; j < stems.length; j++) {
				if (i == j) {
					continue;
				}

				double cuv = 0.0;
				final Set<String> iStrings = stemsMap.get(stems[i]);
				final Set<String> jStrings = stemsMap.get(stems[j]);
				for (final String string1 : iStrings) {
					for (final String string2 : jStrings) {
						final Map<Integer, Integer> iMap = tokenMap.get(string1);
						final Map<Integer, Integer> jMap = tokenMap.get(string2);
						for (final Integer integer : iMap.keySet()) {
							if (jMap.containsKey(integer)) {
								cuv += 1.0 / Math.abs(iMap.get(integer) - jMap.get(integer));
							}
						}
					}
				}

				matrix[i][j] = new Element(stems[i], stems[j], cuv);
			}
		}

		final Element[][] norm = new Element[stemsMap.size()][stemsMap.size()];
		for (int i = 0; i < stems.length; i++) {
			for (int j = 0; j < stems.length; j++) {
				if (i == j) {
					continue;
				}

				double cuv = 0.0;
				if (matrix[i][j] != null) {
					cuv = matrix[i][j].value / (stemsMap.get(stems[i]).size() * stemsMap.get(stems[j]).size());
				}

				norm[i][j] = new Element(stems[i], stems[j], cuv);
			}
		}

		return printTopN(norm, stems, query, tokenMap, stemsMap);
	}

	/**
	 * @param metric
	 * @param stems
	 * @param stemsMap
	 * @param tokenMap
	 * @return
	 */
	static Element[][] printTopN(final Element[][] metric,
			final String[] stems,
			final String query,
			final HashMap<String, Map<Integer, Integer>> tokenMap,
			final Map<String, Set<String>> stemsMap) {
		final Set<String> strings = new HashSet<>();
		strings.addAll(Arrays.asList(query.split(" ")));

		final Element[][] elements = new Element[strings.size()][3];
		int index = 0;
		for (final String string : strings) {
			final PriorityQueue<Element> queue = new PriorityQueue<>(3, new Comparator<Element>() {

				@Override
				public int compare(final Element o1, final Element o2) {
					return o1.value >= o2.value ? 1 : -1;
				}
			});

			final int i = find(stems, string);
			if (i == -1) {
				continue;
			}

			for (int j = 0; j < metric[i].length; j++) {
				if (metric[i][j] == null || strings.contains(metric[i][j].u) && !metric[i][j].u.equals(string) || strings.contains(metric[i][j].v) &&
						!metric[i][j].v.equals(string)) {
					continue;
				}

				if (tokenMap.containsKey(metric[i][j].v)) {
					queue.add(metric[i][j]);
				} else {
					queue.add(new Element(metric[i][j].u, stemsMap.get(metric[i][j].v).iterator().next(), metric[i][j].value));
				}
				if (queue.size() > 3) {
					queue.poll();
				}
			}

			elements[index++] = queue.toArray(new Element[3]);
		}

		return elements;
	}

	public static int find(final String[] arr, final String string) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equalsIgnoreCase(string)) {
				return i;
			}
		}

		return -1;
	}

	static class Element {
		String	u;
		String	v;
		double	value;

		public Element() {
		}

		public Element(final String u, final String v, final double value) {
			this.u = u;
			this.v = v;
			this.value = value;
		}

		@Override
		public String toString() {
			return this.u + " " + this.v + " : " + this.value;
		}
	}

	/**
	 * Validates and gets the command line arguments provided
	 *
	 * @param args
	 *            Command-line arguments
	 * @return Data path
	 */
	/*static CommandLine validateArguments(final String[] args) {
		// Get options
		final Options options = new Options();
		options.addOption("stop", "stopWords", true, "Absolute or relative path to the Stop Words file");

		// Parse arguments
		final CommandLineParser commandLineParser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = commandLineParser.parse(options, args, false);
		} catch (final ParseException e1) {
			System.out.println("Invalid arguments provided");
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Tokenization", options);
			System.exit(1);
		}

		return cmd;
	}*/
}