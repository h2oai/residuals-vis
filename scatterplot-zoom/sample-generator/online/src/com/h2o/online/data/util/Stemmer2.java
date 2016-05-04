/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.data.util;

public class Stemmer2 {
	public Stemmer2() {
	}

	public String stem(String word) {
		/* British */
		if (word.endsWith("our") && !word.endsWith("dour") && !word.endsWith("iour") && !word.endsWith("pour")
				&& !word.endsWith("tour") && !word.endsWith("vour"))
			return word.replace("our", "or");
		if (word.endsWith("ise")) {
			if (word.endsWith("dise") || word.endsWith("gise") || word.endsWith("lise") || word.endsWith("nise")
					|| word.endsWith("tise"))
				return word.replace("ise", "ize");
		}
		/* ness endings */
		if (word.endsWith("iness"))
			return word.substring(0, word.length() - 5) + "y";
		if (word.endsWith("ness"))
			return word.substring(0, word.length() - 4);
		/* plural to singular */
		if (word.endsWith("ies")) {
			if (word.endsWith("bies") || word.endsWith("fies") || word.endsWith("gies") || word.endsWith("hies")
					|| word.endsWith("jies") || word.endsWith("kies") || word.endsWith("lies") || word.endsWith("mies")
					|| word.endsWith("nies") || word.endsWith("pies") || word.endsWith("ries") || word.endsWith("sies")
					|| word.endsWith("ties") || word.endsWith("vies") || word.endsWith("wies") || word.endsWith("xies")
					|| word.endsWith("zies"))
				return word.substring(0, word.length() - 3) + "y";
		}

		if (word.endsWith("men")) {
			if (!word.equals("amen") && !word.equals("albumen") && !word.equals("abdomen")
					&& !word.equals("catechumen") && !word.equals("hymen") && !word.equals("lumen")
					&& !word.equals("limen") && !word.equals("lumen") && !word.equals("rumen") && !word.equals("vimen")
					&& !word.equals("yamen") && !word.equals("nomen") && !word.equals("numen") && !word.equals("semen")
					&& !word.equals("stamen") && !word.equals("specimen") && !word.equals("acumen"))
				return word.substring(0, word.length() - 3) + "man";
		}

		if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("sses") || word.endsWith("zzes")
				|| word.endsWith("xes"))
			return word.substring(0, word.length() - 2);
		if (word.endsWith("bs") || word.endsWith("cs") || word.endsWith("ds") || word.endsWith("gs")
				|| word.endsWith("ks") || word.endsWith("ls") || word.endsWith("ms") || word.endsWith("ns")
				|| word.endsWith("os") || word.endsWith("ps") || word.endsWith("rs") || word.endsWith("ts")
				|| word.endsWith("ws"))
			return word.substring(0, word.length() - 1);
		if (word.endsWith("ces") || word.endsWith("des") || word.endsWith("ges") || word.endsWith("kes")
				|| word.endsWith("les") || word.endsWith("mes") || word.endsWith("nes") || word.endsWith("pes")
				|| word.endsWith("res") || word.endsWith("ses") || word.endsWith("tes") || word.endsWith("ves"))
			return word.substring(0, word.length() - 1);
		if (word.endsWith("oes"))
			return word.substring(0, word.length() - 2);
		/* adverbs */
		if (word.endsWith("ly") && word.length() > 5)
			return word.substring(0, word.length() - 2);
		/* past tense to present */
		if (word.endsWith("ed")) {
			if (word.endsWith("bbed") || word.endsWith("gged") || word.endsWith("mmed") || word.endsWith("nned")
					|| word.endsWith("pped") || word.endsWith("rred") || word.endsWith("tted"))
				return word.substring(0, word.length() - 3);
			if (word.endsWith("ased") || word.endsWith("ized") || word.endsWith("osed") || word.endsWith("ated")
					|| word.endsWith("ured"))
				return word.substring(0, word.length() - 1);
			if (word.endsWith("ered"))
				return word.substring(0, word.length() - 2);
			if (word.endsWith("ied"))
				return word.substring(0, word.length() - 3) + "y";
			else
				return word.substring(0, word.length() - 1);
		}
		/* gerunds */
		if (word.endsWith("ing")) {
			if (word.endsWith("bbing") || word.endsWith("dding") || word.endsWith("gging") || word.endsWith("mming")
					|| word.endsWith("nning") || word.endsWith("pping") || word.endsWith("rring")
					|| word.endsWith("ssing") || word.endsWith("tting"))
				return word.substring(0, word.length() - 4);
			if (word.endsWith("lling") || word.endsWith("ding") || word.endsWith("ning") || word.endsWith("hing")
					|| word.endsWith("wing") || word.endsWith("ying"))
				return word.substring(0, word.length() - 3);
			if (word.endsWith("ibing") || word.endsWith("iding") || word.endsWith("obing") || word.endsWith("ping"))
				return word.substring(0, word.length() - 3) + "e";
			else
				return word.substring(0, word.length() - 3);
		}
		/* miscellaneous */
		if (word.endsWith("istic"))
			return word.substring(0, word.length() - 2);
		if (word.endsWith("ical"))
			return word.substring(0, word.length() - 2);

		return word;
	}
}
