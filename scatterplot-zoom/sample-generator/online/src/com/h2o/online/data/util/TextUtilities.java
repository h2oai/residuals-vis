/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.data.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.h2o.online.Globals;
import com.h2o.online.data.DataTable;

public class TextUtilities {
	private static Pattern quotes = Pattern.compile("\"(.*?)\"");
	private static Pattern punctuation = Pattern.compile("\\W|\\d", Pattern.MULTILINE);
	private static Stemmer1 stemmer1 = new Stemmer1();
	private static Stemmer2 stemmer2 = new Stemmer2();
	private static Set<String> dictionary;
	private static Set<String> missingValueSymbols = new HashSet<String>();
	static {
		missingValueSymbols.add("");
		missingValueSymbols.add(".");
		missingValueSymbols.add("*");
		missingValueSymbols.add("?");
		missingValueSymbols.add("-");
		missingValueSymbols.add("--");
		missingValueSymbols.add("..");
		missingValueSymbols.add("null");
	}

	private TextUtilities() {
	}

	private static final String IP_ADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	private static final String ZIP_PATTERN = "^\\d{5}(-\\d{4})?$";

	private static final Pattern patternIP = Pattern.compile(IP_ADDRESS_PATTERN);
	private static final Pattern patternZIP = Pattern.compile(ZIP_PATTERN);

	public static void stemWordList(DataTable text) {
		//		List<String> wordList = text.getWords();
		//		Set<String> rejects = new TreeSet<String>();
		//		List<String> stemmedWordList = new ArrayList<String>();
		//		for (int i = 0; i < wordList.size(); i++) {
		//			String word = wordList.get(i);
		//			stem(rejects, stemmedWordList, word);
		//		}
		//		text.replaceWords(stemmedWordList);
		//		saveRejects(rejects);
	}

	public static void buildPhraseMap(DataTable text, int document) {
		//		List<String> wordList = text.getWords(document);
		//		for (int phraseLength = 9; phraseLength > 3; phraseLength--) {
		//			Map<String, Integer> documentPhraseMap = new HashMap<String, Integer>();
		//			for (int i = 0; i < wordList.size() - phraseLength; i++) {
		//				String phrase = "";
		//				for (int j = i; j < i + phraseLength; j++) {
		//					String word = wordList.get(j);
		//					phrase += word + " ";
		//				}
		//				phrase = phrase.trim();
		//				if (phrase.length() == 0)
		//					continue;
		//				if (documentPhraseMap.containsKey(phrase)) {
		//					Integer count = documentPhraseMap.get(phrase);
		//					int c = count.intValue() + 1;
		//					documentPhraseMap.put(phrase, new Integer(c));
		//				} else {
		//					documentPhraseMap.put(phrase, new Integer(1));
		//				}
		//			}
		//
		//			if (documentPhraseMap.size() == 0)
		//				return;
		//
		//			List<Integer> values = new ArrayList<Integer>(documentPhraseMap.values());
		//			List<String> keys = new ArrayList<String>(documentPhraseMap.keySet());
		//			double[] v = new double[values.size()];
		//			for (int i = 0; i < values.size(); i++)
		//				v[i] = (values.get(i)).doubleValue();
		//			int[] index = Sorts.indexedDoubleArraySort(v, 0, 0);
		//
		//			LinkedHashMap<String, Integer> phraseMap = (LinkedHashMap<String, Integer>) text.phraseMap;
		//			if (phraseMap == null)
		//				phraseMap = new LinkedHashMap<String, Integer>();
		//			int maxSearch = 50;
		//			for (int i = 1; i < maxSearch; i++) {
		//				int iSearch = index.length - i;
		//				String key = keys.get(index[iSearch]);
		//				Integer value = values.get(index[iSearch]);
		//				if (value.intValue() < 5)
		//					break;
		//				boolean isNew = true;
		//				List<String> keys2 = new ArrayList<String>(phraseMap.keySet());
		//				for (int j = 0; j < phraseMap.size(); j++) {
		//					String key2 = keys2.get(j);
		//					String commonSubstring = longestCommonSubstring(key, key2);
		//					if ((double) commonSubstring.length() / key2.length() > .5) {
		//						isNew = false;
		//						break;
		//					}
		//				}
		//				if (isNew)
		//					phraseMap.put(key, value);
		//			}
		//			if (phraseMap.size() > 4) {
		//				text.phraseMap = phraseMap;
		//				return;
		//			}
		//		}
	}

	private static String longestCommonSubstring(String first, String second) {
		String tmp = "";
		String max = "";
		for (int i = 0; i < first.length(); i++) {
			for (int j = 0; j < second.length(); j++) {
				for (int k = 1; k + i <= first.length() && k + j <= second.length(); k++) {
					if (first.substring(i, k + i).equals(second.substring(j, k + j))) {
						tmp = first.substring(i, k + i);
					} else {
						if (tmp.length() > max.length())
							max = tmp;
						tmp = "";
					}
				}
				if (tmp.length() > max.length())
					max = tmp;
				tmp = "";
			}
		}
		return max;
	}

	public static void stem(Set<String> rejects, List<String> words, String word) {
		String stem1, stem2;
		stem1 = stemmer1.stem(word);
		if (dictionary.contains(stem1)) {
			stem2 = stemmer2.stem(stem1);
			if (dictionary.contains(stem2))
				words.add(stem2);
			else
				words.add(stem1);
		} else {
			stem2 = stemmer2.stem(word);
			if (dictionary.contains(stem2))
				words.add(stem2);
			else if (dictionary.contains(word))
				words.add(word);
			else
				rejects.add(word);
		}
	}

	public static void saveRejects(Set<String> rejects) {
		PrintWriter textOut;
		try {
			textOut = new PrintWriter(new FileWriter("text/rejectedWords.txt"));
		} catch (IOException e) {
			return;
		}
		Iterator<String> it = rejects.iterator();
		while (it.hasNext()) {
			textOut.println(it.next());
		}
		textOut.close();
	}

	public static void readDictionary() {
		java.io.BufferedReader fin;
		dictionary = new HashSet<String>();
		try {
			fin = new java.io.BufferedReader(new java.io.FileReader("text/dictionary.txt"));
			String record;
			while ((record = fin.readLine()) != null) {
				String[] rec = record.split(" ");
				if (rec.length < 2 && !record.contains("'"))
					dictionary.add(record.toLowerCase());
			}
			fin.close();
		} catch (java.io.IOException ie) {
		}
	}

	public static void writeDictionary() {
		PrintWriter textOut;
		Object[] sortedArray = dictionary.toArray();
		Arrays.sort(sortedArray);
		Arrays.sort(sortedArray, new Comparator<Object>() {
			@Override
			public int compare(Object object1, Object object2) {
				int length1 = ((String) object1).length();
				int length2 = ((String) object2).length();
				if (length1 > length2) {
					return 1;
				} else if (length1 < length2) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		try {
			textOut = new PrintWriter(new FileWriter("text/filteredDictionary.txt"));
		} catch (IOException e) {
			return;
		}
		for (int i = 0; i < sortedArray.length; i++) {
			textOut.println((String) sortedArray[i]);
		}
		textOut.close();
	}

	public static String compressSpaces(String record) {
		return record.replaceAll(" {2,}", " ").trim();
	}

	public static String trimSpaces(String s) {
		return s.trim();
	}

	public static String trimQuotes(String s) {
		if (s.startsWith("\""))
			s = s.substring(1, s.length());
		if (s.endsWith("\""))
			s = s.substring(0, s.length() - 1);
		return s;
	}

	public static String grabFirstStringInsideQuotes(String string) {
		Matcher matcher = quotes.matcher(string);
		return matcher.group(1);
	}

	public static String deletePunctuation(String record) {
		record = record.toLowerCase();
		record = record.replaceAll("- ", "");
		record = record.replaceAll(",", " ");
		record = record.replaceAll("'", "");
		record = punctuation.matcher(record).replaceAll(" ");
		record = record.replaceAll(" {2,}", " ");
		return record;
	}

	public static String deleteHTML(String record) {
		return record.replaceAll("<.*?>", "");
	}

	public static String[] split(String record, boolean removeQuotes, String separator) {
		if (record == null || record.length() == 0)
			return new String[] { "" };
		List<Object> cols = new ArrayList<Object>();
		boolean isUnquoted = true;
		int i0 = 0;
		record = compressSpaces(record);
		String field;
		for (int j = 0; j < record.length(); j++) {
			if (record.charAt(j) == '"')
				isUnquoted = !isUnquoted;
			if (isUnquoted) {
				if (record.substring(j, j + 1).equals(separator)) {
					field = record.substring(i0, j);
					field = trimSpaces(field);
					if (removeQuotes)
						field = trimQuotes(field);
					cols.add(field);
					i0 = j + 1;
				} else if (j == record.length() - 1) {
					field = record.substring(i0, j + 1);
					field = trimSpaces(field);
					if (removeQuotes)
						field = trimQuotes(field);
					cols.add(field);
				}
			}
		}
		int nc = Math.max(1, cols.size());
		String[] fields = new String[nc];
		fields[0] = "";
		if (cols.size() > 0) {
			for (int i = 0; i < nc; i++)
				fields[i] = (String) cols.get(i);
		}
		return fields;
	}

	public static boolean isMissingValueSymbol(String s) {
		return missingValueSymbols.contains(s.toLowerCase().trim());
	}

	public static boolean isNumber(String s) {
		if (s == null || s.length() == 0 || s.trim().equals(""))
			return false;
		s = s.toLowerCase().trim();

		/* hours:minutes:seconds */
		String[] t = s.split(":");
		if (t.length == 3) {
			boolean isOK = true;
			try {
				Integer.parseInt(t[0]);
				Integer.parseInt(t[1]);
				Integer.parseInt(t[2]);
			} catch (Exception e) {
				isOK = false;
			}
			if (isOK)
				return true;
		}

		/* currency */
		if (CurrencyUtilities.isCurrency(s))
			return true;

		/* scientific notation */
		if (parseScientific(s).equals("."))
			return false;

		return true;
	}

	public static String parseNumber(String s, String units) {
		if (s == null)
			return s;

		/* missing */
		s = s.toLowerCase().trim();
		if (TextUtilities.isMissingValueSymbol(s))
			return ".";

		/* real number */
		if (units.equals(Units.NUMBER))
			return parseScientific(s);

		s = TextUtilities.trimQuotes(s);
		s = CurrencyUtilities.asNumericString(s, units);
		s = s.replaceAll("[^(\\.\\w\\-)]", "");

		return s;
	}

	public static String parseScientific(String s) {
		ParsePosition pp = new ParsePosition(0);
		Number number = Globals.numberFormat.parse(s, pp);
		if (number == null)
			return ".";
		double x = number.doubleValue();
		if (Double.isInfinite(x) || Globals.isMissing(x))
			return ".";

		int loc = pp.getIndex();
		if (s.length() == loc)
			return number.toString();

		if (s.substring(loc, loc + 1).equalsIgnoreCase("e")) {
			loc++;
			int sign = 1;
			if (s.substring(loc, loc + 1).equals("+")) {
				sign = 1;
				loc++;
			} else if (s.substring(loc, loc + 1).equals("-")) {
				sign = -1;
				loc++;
			}
			pp.setIndex(loc);
			number = Globals.numberFormat.parse(s, pp);
			if (number != null) {
				int exponent = sign * number.intValue();
				x *= Math.pow(10, exponent);
				return (new Double(x)).toString();
			}
		}
		return ".";
	}

	public static String parseUSPhoneNumber(String s) {
		if (s.contains(".") || !s.contains("-"))
			return null;
		String t = s.replaceAll("[\\-() ]", "");
		if (t == null || t.length() < 10)
			return null;
		if (t.startsWith("1"))
			t = t.substring(1);
		t = t.trim();
		if (t.length() != 10)
			return null;
		try {
			int areacode = Integer.parseInt(t.substring(0, 3));
			if (areacode > 200 && areacode < 999) {
				String number = t.substring(0, 3) + "-" + t.substring(3, 6) + "-" + t.substring(6, 10);
				return number;
			}
		} catch (NumberFormatException e) {
		}
		return null;
	}

	public static boolean isPercent(String s) {
		try {
			ParsePosition pp = new ParsePosition(0);
			Number number = Globals.numberFormat.parse(s, pp);
			if (number != null
					&& pp.getIndex() == s.length() - 1
					&& s.endsWith("%")
					&& ((number.doubleValue() >= 0 && number.doubleValue() <= 1.) || (number.doubleValue() >= 0 && number
							.doubleValue() <= 100.)))
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

	public static boolean isIPAddress(String s) {
		Matcher matcher = patternIP.matcher(s);
		return matcher.matches();
	}

	public static boolean isZipCode(String s) {
		Matcher matcher = patternZIP.matcher(s);
		return matcher.matches();
	}

	public static String metaPhone(String txt) {
		/* This is metaPhone 2 open-source with no copyright. See class MetaPhone 3 for more recent version. */
		String vowels = "AEIOU";
		String frontv = "EIY";
		String varson = "CSPTG";
		int maxCodeLen = 4;

		int mtsz = 0;
		boolean hard = false;
		if ((txt == null) || (txt.length() == 0))
			return "";
		// single character is itself
		if (txt.length() == 1)
			return txt.toUpperCase();
		//
		char[] inwd = txt.toUpperCase().toCharArray();
		//
		String tmpS;
		StringBuffer local = new StringBuffer(40); // manipulate
		StringBuffer code = new StringBuffer(10); //   output
		// handle initial 2 characters exceptions
		switch (inwd[0]) {
		case 'K':
		case 'G':
		case 'P': /* looking for KN, etc */
			if (inwd[1] == 'N')
				local.append(inwd, 1, inwd.length - 1);
			else
				local.append(inwd);
			break;
		case 'A': /* looking for AE */
			if (inwd[1] == 'E')
				local.append(inwd, 1, inwd.length - 1);
			else
				local.append(inwd);
			break;
		case 'W': /* looking for WR or WH */
			if (inwd[1] == 'R') { // WR -> R
				local.append(inwd, 1, inwd.length - 1);
				break;
			}
			if (inwd[1] == 'H') {
				local.append(inwd, 1, inwd.length - 1);
				local.setCharAt(0, 'W'); // WH -> W
			} else
				local.append(inwd);
			break;
		case 'X': /* initial X becomes S */
			inwd[0] = 'S';
			local.append(inwd);
			break;
		default:
			local.append(inwd);
		} // now local has working string with initials fixed
		int wdsz = local.length();
		int n = 0;
		while ((mtsz < maxCodeLen) && // max code size of 4 works well
				(n < wdsz)) {
			char symb = local.charAt(n);
			// remove duplicate letters except C
			if ((symb != 'C') && (n > 0) && (local.charAt(n - 1) == symb))
				n++;
			else { // not dup
				switch (symb) {
				case 'A':
				case 'E':
				case 'I':
				case 'O':
				case 'U':
					if (n == 0) {
						code.append(symb);
						mtsz++;
					}
					break; // only use vowel if leading char
				case 'B':
					if ((n > 0) && !(n + 1 == wdsz) && // not MB at end of word
							(local.charAt(n - 1) == 'M')) {
						code.append(symb);
					} else
						code.append(symb);
					mtsz++;
					break;
				case 'C': // lots of C special cases
					/* discard if SCI, SCE or SCY */
					if ((n > 0) && (local.charAt(n - 1) == 'S') && (n + 1 < wdsz)
							&& (frontv.indexOf(local.charAt(n + 1)) >= 0)) {
						break;
					}
					tmpS = local.toString();
					if (tmpS.indexOf("CIA", n) == n) { // "CIA" -> X
						code.append('X');
						mtsz++;
						break;
					}
					if ((n + 1 < wdsz) && (frontv.indexOf(local.charAt(n + 1)) >= 0)) {
						code.append('S');
						mtsz++;
						break; // CI,CE,CY -> S
					}
					if ((n > 0) && (tmpS.indexOf("SCH", n - 1) == n - 1)) { // SCH->sk
						code.append('K');
						mtsz++;
						break;
					}
					if (tmpS.indexOf("CH", n) == n) { // detect CH
						if ((n == 0) && (wdsz >= 3) && // CH consonant -> K consonant
								(vowels.indexOf(local.charAt(2)) < 0)) {
							code.append('K');
						} else {
							code.append('X'); // CHvowel -> X
						}
						mtsz++;
					} else {
						code.append('K');
						mtsz++;
					}
					break;
				case 'D':
					if ((n + 2 < wdsz) && // DGE DGI DGY -> J
							(local.charAt(n + 1) == 'G') && (frontv.indexOf(local.charAt(n + 2)) >= 0)) {
						code.append('J');
						n += 2;
					} else {
						code.append('T');
					}
					mtsz++;
					break;
				case 'G': // GH silent at end or before consonant
					if ((n + 2 == wdsz) && (local.charAt(n + 1) == 'H'))
						break;
					if ((n + 2 < wdsz) && (local.charAt(n + 1) == 'H') && (vowels.indexOf(local.charAt(n + 2)) < 0))
						break;
					tmpS = local.toString();
					if ((n > 0) && (tmpS.indexOf("GN", n) == n) || (tmpS.indexOf("GNED", n) == n))
						break; // silent G
					if ((n > 0) && (local.charAt(n - 1) == 'G'))
						hard = true;
					else
						hard = false;
					if ((n + 1 < wdsz) && (frontv.indexOf(local.charAt(n + 1)) >= 0) && (!hard))
						code.append('J');
					else
						code.append('K');
					mtsz++;
					break;
				case 'H':
					if (n + 1 == wdsz)
						break; // terminal H
					if ((n > 0) && (varson.indexOf(local.charAt(n - 1)) >= 0))
						break;
					if (vowels.indexOf(local.charAt(n + 1)) >= 0) {
						code.append('H');
						mtsz++;// Hvowel
					}
					break;
				case 'F':
				case 'J':
				case 'L':
				case 'M':
				case 'N':
				case 'R':
					code.append(symb);
					mtsz++;
					break;
				case 'K':
					if (n > 0) { // not initial
						if (local.charAt(n - 1) != 'C') {
							code.append(symb);
						}
					} else
						code.append(symb); // initial K
					mtsz++;
					break;
				case 'P':
					if ((n + 1 < wdsz) && // PH -> F
							(local.charAt(n + 1) == 'H'))
						code.append('F');
					else
						code.append(symb);
					mtsz++;
					break;
				case 'Q':
					code.append('K');
					mtsz++;
					break;
				case 'S':
					tmpS = local.toString();
					if ((tmpS.indexOf("SH", n) == n) || (tmpS.indexOf("SIO", n) == n) || (tmpS.indexOf("SIA", n) == n))
						code.append('X');
					else
						code.append('S');
					mtsz++;
					break;
				case 'T':
					tmpS = local.toString(); // TIA TIO -> X
					if ((tmpS.indexOf("TIA", n) == n) || (tmpS.indexOf("TIO", n) == n)) {
						code.append('X');
						mtsz++;
						break;
					}
					if (tmpS.indexOf("TCH", n) == n)
						break;
					// substitute numeral 0 for TH (resembles theta after all)
					if (tmpS.indexOf("TH", n) == n)
						code.append('0');
					else
						code.append('T');
					mtsz++;
					break;
				case 'V':
					code.append('F');
					mtsz++;
					break;
				case 'W':
				case 'Y': // silent if not followed by vowel
					if ((n + 1 < wdsz) && (vowels.indexOf(local.charAt(n + 1)) >= 0)) {
						code.append(symb);
						mtsz++;
					}
					break;
				case 'X':
					code.append('K');
					code.append('S');
					mtsz += 2;
					break;
				case 'Z':
					code.append('S');
					mtsz++;
					break;
				} // end switch
				n++;
			} // end else from symb != 'C'
			if (mtsz > 4)
				code.setLength(4);
		}
		return (code.toString()).toLowerCase();
	}

	public static String formatDouble(double x, int digits) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		if (x == 0)
			return "0";
		if (Double.isNaN(x))
			return " ";
		boolean isScientific = useScientificNotation(x);
		if (digits < 0)
			isScientific = true;
		if (isScientific) {
			df = new DecimalFormat("0.###E0");
		} else {
			df.setMaximumFractionDigits(digits);
			df.setMinimumFractionDigits(digits);
			df.setGroupingUsed(false);
		}
		return df.format(x);
	}

	public static String formatInteger(int x) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.setGroupingUsed(false);
		return df.format(x);
	}

	public static String formatDoubleAsIntegerWithoutCommas(double x) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.setGroupingUsed(false);
		return df.format((int) x);
	}

	public static boolean useScientificNotation(double data) {
		double ld = Math.abs(Math.log10(Math.abs(data)));
		if (ld < Globals.MAXDIGITS)
			return false;
		else
			return true;
	}

	public static boolean useScientificNotation(double[] data) {
		int nearZero = 0;
		for (int i = 0; i < data.length; i++) {
			double ld = Math.log10(Math.abs(data[i]));
			if (ld > Globals.MAXDIGITS)
				return true;
			else if (ld < -Globals.MAXDIGITS)
				nearZero++;
		}
		if (nearZero == data.length)
			return true;
		else
			return false;
	}
}
