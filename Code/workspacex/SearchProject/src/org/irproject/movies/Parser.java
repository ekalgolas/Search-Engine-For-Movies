package org.irproject.movies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to parse input data
 *
 * @author Ekal.Golas
 */
public class Parser {
	private final LinkedHashMap<String, Map<Integer, Integer>>	tokenMap;
	private final Set<String>									stopwords;
	private final List<HashMap<String, Integer>>				documentMap;
	private static int											index;

	/**
	 * Default constructor
	 *
	 * @param file
	 *            Stop words file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Parser(final File file) throws FileNotFoundException, IOException {
		this.tokenMap = new LinkedHashMap<>();
		this.stopwords = new HashSet<>();
		this.documentMap = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			for (String line; (line = reader.readLine()) != null;) {
				this.stopwords.add(line.trim());
			}
		}
	}

	/**
	 * Parses the data of all the files in the path and its subdirectories
	 *
	 * @param list
	 *            Path to be parsed
	 * @throws IOException
	 */
	public void parse(final List<String> list) throws IOException {
		// Go through every entry in the root path
		index = 0;
		for (final String doc : list) {
			this.readFile(doc);
		}
	}

	/**
	 * Parses a file and tokenizes it
	 *
	 * @param string
	 *            File to parse
	 * @throws IOException
	 */
	private void readFile(final String string) throws IOException {
		// Validate input
		if (string == null || string.isEmpty()) {
			return;
		}

		// Create document map
		this.documentMap.add(new HashMap<String, Integer>());

		index++;
		int num = 0;
		// Transform the word in order to tokenize it
		final String line = this.transformText(string);

		// Get and read each token
		final String[] words = line.split(" ");
		for (final String word : words) {
			num++;

			// Skip if word is empty
			if (word == null || word.length() < 3 || this.stopwords.contains(word)) {
				continue;
			}

			// Increment occurrence of this word in token map
			if (!this.tokenMap.containsKey(word)) {
				this.tokenMap.put(word, new HashMap<Integer, Integer>());
			}

			this.tokenMap.get(word).put(index, num);
			this.documentMap.get(index - 1).put(word, this.documentMap.get(index - 1).getOrDefault(word, 0) + 1);
		}
	}

	/**
	 * <pre>
	 *  Handles:-
	 *
	 *  A. Upper and lower case words (e.g. "People", "people", "Apple", "apple")
	 *  B. Words with dashes (e.g. "1996-97", "middle-class", "30-year", "tean-ager")
	 *  C. Possessives (e.g. "sheriff's", "university's")
	 *  D. Acronyms (e.g., "U.S.", "U.N.")
	 *  E. SGML tags are not considered words, so they should not be included in any of the information your program gathers. The SGML tags in this data follow the conventional style:
	 * 		<[/]?tag> | >[/]?tag (attr[=value])+>
	 * </pre>
	 *
	 * @param text
	 *            Text to transform
	 * @return Transformed text
	 */
	public String transformText(String text) {
		// Replacing the SGML tags with space.
		text = text.replaceAll("\\<.*?>", " ");

		// Remove digits
		text = text.replaceAll("[\\d+]", "");

		// Remove possessives
		text = text.replaceAll("\\'s", "");

		// Remove the special characters
		text = text.replaceAll("[\"`'<>»+^:,?;=%#&~`$!@*_)/(}{\\.]", "");

		// Replace - with space to count two words
		text = text.replaceAll("-", " ");
		text = text.replaceAll("_", " ");

		// Remove multiple white spaces
		text = text.replaceAll("\\s+", " ");

		// Trim and set text to lower case
		text = text.trim().toLowerCase();
		return text;
	}

	/**
	 * @return the token map
	 */
	public LinkedHashMap<String, Map<Integer, Integer>> getTokenMap() {
		return this.tokenMap;
	}

	/**
	 * @return the documentMap
	 */
	public final List<HashMap<String, Integer>> getDocumentMap() {
		return this.documentMap;
	}
}