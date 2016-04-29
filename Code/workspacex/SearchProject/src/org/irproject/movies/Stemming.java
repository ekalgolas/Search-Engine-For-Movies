package org.irproject.movies;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * @author Ekal.Golas
 */
public class Stemming {
	private final Map<String, Set<String>>	stemsMap;
	private final WordnetStemmer			stemmer;
	private final Set<String>				unstemmedSet;
	
	public Stemming(final String wordnet, final String query) throws IOException {
		this.stemsMap = new HashMap<>();
		final Dictionary dict = new Dictionary(new File(wordnet));
		dict.open();
		this.stemmer = new WordnetStemmer(dict);
		
		this.unstemmedSet = new HashSet<>();
		this.unstemmedSet.addAll(Arrays.asList(query.split(" ")));
	}

	public void stem(final HashMap<String, Map<Integer, Integer>> tokenMap) {
		final Set<String> set = new HashSet<String>();
		final Set<String> keySet = tokenMap.keySet();
		set.addAll(keySet);

		Set<String> stems = new HashSet<>();
		final POS[] values = new POS[] { POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB };
		for (final String string1 : set) {
			boolean found = false;
			for (final String string : this.unstemmedSet) {
				if (string.contains(string1) || string1.contains(string)) {
					found = true;
					break;
				}
			}

			if (found) {
				continue;
			}

			for (final POS pos : values) {
				stems.addAll(this.stemmer.findStems(string1, pos));
			}
		}

		for (final String string1 : keySet) {
			boolean found = false;
			for (final POS pos : values) {
				final List<String> findStems = this.stemmer.findStems(string1, pos);
				for (final String string : findStems) {
					if (stems.contains(string)) {
						found = true;
						this.stemsMap.putIfAbsent(string, new HashSet<String>());
						this.stemsMap.get(string).add(string1);
					}
				}
			}

			if (!found) {
				this.stemsMap.putIfAbsent(string1, new HashSet<String>());
				this.stemsMap.get(string1).add(string1);
			}
		}

		stems = new HashSet<>();
		stems.addAll(this.stemsMap.keySet());
		for (final String string : stems) {
			final Set<String> tokensSet = this.stemsMap.get(string);
			boolean found = false;
			if (tokensSet != null && tokensSet.size() > 1) {
				for (final String string2 : tokensSet) {
					if (tokenMap.containsKey(string2)) {
						found = true;
					} else {
						this.stemsMap.remove(string2);
					}
				}

				for (int i = string.length() - 1; i > 0; i--) {
					final String subString = string.substring(0, i);
					if (this.stemsMap.containsKey(subString)) {
						final Set<String> common = new HashSet<>();
						common.addAll(this.stemsMap.get(string));
						common.removeAll(this.stemsMap.get(subString));

						if (common.size() != this.stemsMap.get(string).size()) {
							this.stemsMap.remove(subString);
						}
					}
				}
			} else if (tokensSet != null && tokensSet.size() == 1) {
				for (final String string2 : tokensSet) {
					if (!tokenMap.containsKey(string2)) {
						this.stemsMap.remove(string);
					} else {
						found = true;
					}
				}
			}

			if (!found || string.length() < 3) {
				this.stemsMap.remove(string);
			}
		}
	}

	public Map<String, Set<String>> getStemsMap() {
		return this.stemsMap;
	}
}