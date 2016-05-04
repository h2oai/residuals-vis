/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Sorts;

public abstract class SimilarityFunctions {
	public static final SimilarityFunctions EUCLIDEAN = new Euclidean();
	public static final SimilarityFunctions NORMALIZED_EUCLIDEAN = new NormalizedEuclidean();
	public static final SimilarityFunctions CITY_BLOCK = new CityBlock();
	public static final SimilarityFunctions L_INFINITY = new Linf();
	public static final SimilarityFunctions JACCARD = new Jaccard();
	public static final SimilarityFunctions LEVENSHTEIN = new Levenshtein();
	public static final SimilarityFunctions DAMERAU = new Damerau();
	public static final SimilarityFunctions NCD = new NCD();
	public static final SimilarityFunctions CCF = new CCF();

	public static final SimilarityFunctions COSINE = new Cosine();
	public static final SimilarityFunctions CROSS_PRODUCT = new CrossProduct();
	public static final SimilarityFunctions COVARIANCE = new Covariance();
	public static final SimilarityFunctions PEARSON_CORRELATION = new PearsonCorrelation();
	public static final SimilarityFunctions SPEARMAN_CORRELATION = new SpearmanCorrelation();
	public static final SimilarityFunctions PRE = new PRE();

	protected abstract double update(double difference, double product, double weight);

	protected abstract double normalize(double x, double sumwt, double s1, double s2);

	public abstract boolean isSimilarity();

	public double compute(String a, String b) {
		return 0;
	}

	public double compute(double[] a, double[] b) {
		return 0;
	}

	public static double compute(double[] x1, double[] x2, double weight, SimilarityFunctions sf) {
		if (Globals.isMissing(weight) || weight <= 0)
			return Globals.MISSING_VALUE;
		double[] z1 = null, z2 = null;
		if (sf.equals(NCD)) {
			return NCD.compute(x1, x2);
		} else if (sf.equals(CCF)) {
			return 1 - CCF.compute(x1, x2);
		} else if (sf.equals(SPEARMAN_CORRELATION)) {
			z1 = new double[x1.length];
			z2 = new double[x2.length];
			System.arraycopy(x1, 0, z1, 0, x1.length);
			System.arraycopy(x2, 0, z2, 0, x2.length);
			x1 = Sorts.rank(x1);
			x2 = Sorts.rank(x2);
		}
		int n = 0;
		double coef = 0;
		double xmean1 = 0;
		double xmean2 = 0;
		double sd1 = 0;
		double sd2 = 0;
		int p = x1.length;
		for (int j = 0; j < p; j++) {
			if (!Globals.isMissing(x1[j]) && !Globals.isMissing(x2[j])) {
				n++;
				double xm1 = x1[j] - xmean1;
				double xm2 = x2[j] - xmean2;
				xmean1 += xm1 / n;
				xmean2 += xm2 / n;
				if (sf instanceof Cosine) {
					sd1 += x1[j] * x1[j];
					sd2 += x2[j] * x2[j];
				} else {
					sd1 += xm1 * (x1[j] - xmean1);
					sd2 += xm2 * (x2[j] - xmean2);
				}
				double diff = x1[j] - x2[j];
				double prod;
				if (sf instanceof Cosine)
					prod = x1[j] * x2[j];
				else
					prod = xm1 * (x2[j] - xmean2);
				coef += sf.update(diff, prod, 1);
			}
		}
		if (n == 0)
			return Globals.MISSING_VALUE;
		if (sf.equals(SPEARMAN_CORRELATION)) {
			x1 = z1;
			x2 = z2;
		}
		return sf.normalize(coef, n, sd1, sd2);
	}

	private static final class Euclidean extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return difference * difference * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return Math.sqrt(x);
		}

		@Override
		public boolean isSimilarity() {
			return false;
		}
	}

	private static final class NormalizedEuclidean extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return difference * difference * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return Math.sqrt(x) / sumwt;
		}

		@Override
		public boolean isSimilarity() {
			return false;
		}
	}

	private static final class Cosine extends SimilarityFunctions {
		/* notice difference from Pearson in calling values of product and s1 and s2 */
		@Override
		public final double update(double difference, double product, double weight) {
			return product * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			if (s1 <= 0 || s2 <= 0)
				return Globals.MISSING_VALUE;
			else
				return x / Math.sqrt(s1 * s2);
		}

		@Override
		public boolean isSimilarity() {
			return true;
		}
	}

	private static final class CrossProduct extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return product * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return x;
		}

		@Override
		public boolean isSimilarity() {
			return true;
		}
	}

	private static final class Covariance extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return product * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			if (sumwt < 1)
				return Globals.MISSING_VALUE;
			else
				return x / (sumwt - 1);
		}

		@Override
		public boolean isSimilarity() {
			return true;
		}
	}

	private static final class PearsonCorrelation extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return product * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			if (s1 <= 0 || s2 <= 0)
				return Globals.MISSING_VALUE;
			else
				return x / Math.sqrt(s1 * s2);
		}

		@Override
		public boolean isSimilarity() {
			return true;
		}
	}

	private static final class SpearmanCorrelation extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return product * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			if (s1 <= 0 || s2 <= 0)
				return Globals.MISSING_VALUE;
			else
				return x / Math.sqrt(s1 * s2);
		}

		@Override
		public boolean isSimilarity() {
			return true;
		}
	}

	private static final class CityBlock extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return Math.abs(difference) * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return x;
		}

		@Override
		public boolean isSimilarity() {
			return false;
		}
	}

	private static final class Linf extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return Math.pow(difference, 10) * weight;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			if (sumwt == 0)
				return Globals.MISSING_VALUE;
			else
				return Math.pow(x, 0.1);
		}

		@Override
		public boolean isSimilarity() {
			return false;
		}
	}

	private static final class Jaccard extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public boolean isSimilarity() {
			return true;
		}

		public static double compute(Set a, Set b) {
			Set intersection = new HashSet(a);
			intersection.retainAll(b);
			Set union = new HashSet(a);
			union.addAll(b);
			if (union.size() == 0)
				return 0;
			else
				return (double) intersection.size() / (double) union.size();
		}
	}

	private static final class CCF extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public boolean isSimilarity() {
			return false;
		}

		@Override
		public final double compute(double[] x1, double[] x2) {
			double[] ccf = Statistics.crossCorrelationFunction(x1, x2, null, 0);
			double cmax = 0;
			for (int j = 0; j < ccf.length; j++) {
				if (!Globals.isMissing(ccf[j]))
					cmax = Math.max(cmax, ccf[j]);
			}
			return 1 - cmax;
		}
	}

	private static final class NCD extends SimilarityFunctions {
		/* Normalized Compression Distance, Li et al. (2004) NCD = (abSize - min(aSize, bSize)) / max(aSize, bSize) */
		/* see also Compression Dissimilarity Measure, Keogh et al. (2004) CDM = abSize / (aSize + bSize) */

		@Override
		public final double update(double difference, double product, double weight) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public boolean isSimilarity() {
			return false;
		}

		@Override
		public double compute(double[] a, double[] b) {
			int aLen = a.length;
			int bLen = b.length;
			int na = 0;
			for (int j = 0; j < aLen; j++) {
				if (!Globals.isMissing(a[j]))
					na++;
			}
			int nb = 0;
			for (int j = 0; j < bLen; j++) {
				if (!Globals.isMissing(b[j]))
					nb++;
			}
			double[] aa = new double[na];
			double[] bb = new double[nb];
			na = 0;
			for (int j = 0; j < aLen; j++) {
				if (!Globals.isMissing(a[j])) {
					aa[na] = a[j];
					na++;
				}
			}
			nb = 0;
			for (int j = 0; j < bLen; j++) {
				if (!Globals.isMissing(b[j])) {
					bb[nb] = b[j];
					nb++;
				}
			}
			Statistics.quantize(aa, null, 5);
			Statistics.quantize(bb, null, 5);
			double[] ab = new double[na + nb];
			System.arraycopy(aa, 0, ab, 0, na);
			System.arraycopy(bb, 0, ab, na, nb);

			double aSize = compress(aa);
			double bSize = compress(bb);
			double abSize = compress(ab);

			return (abSize - Math.min(aSize, bSize)) / Math.max(aSize, bSize);
		}

		private static double compress(double[] a) {
			NumberFormat formatter = new DecimalFormat("#");
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < a.length; i++)
				buffer.append(formatter.format(a[i]));
			String inputString = buffer.toString();
			byte[] input = null;
			try {
				input = inputString.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				return Globals.MISSING_VALUE;
			}
			byte[] output = new byte[input.length];
			Deflater compresser = new Deflater();
			compresser.setInput(input);
			compresser.finish();
			double compressedDataLength = compresser.deflate(output);
			compresser.end();
			return compressedDataLength;
		}

		@Override
		public double compute(String a, String b) {
			String ab = a + b;

			double aSize = compress(a);
			double bSize = compress(b);
			double abSize = compress(ab);

			return (abSize - Math.min(aSize, bSize)) / Math.max(aSize, bSize);
		}

		private static double compress(String inputString) {
			byte[] input = null;
			try {
				input = inputString.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				return Globals.MISSING_VALUE;
			}
			byte[] output = new byte[input.length];
			Deflater compresser = new Deflater();
			compresser.setInput(input);
			compresser.finish();
			double compressedDataLength = compresser.deflate(output);
			compresser.end();
			return compressedDataLength;
		}
	}

	private static final class PRE extends SimilarityFunctions {
		/* the heavy lifting is done in SymmetricMatrices */
		@Override
		public final double update(double difference, double product, double weight) {
			return 0;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return x;
		}

		@Override
		public boolean isSimilarity() {
			return true;
		}
	}

	private static final class Levenshtein extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public boolean isSimilarity() {
			return false;
		}

		@Override
		public double compute(String s1, String s2) {
			/* Levenshtein edit distance */
			char[] s = s1.toCharArray();
			char[] t = s2.toCharArray();

			int m = s.length + 1;
			int n = t.length + 1;
			int d[][] = new int[m][n];

			for (int i = 0; i < m; i++)
				d[i][0] = i;
			for (int j = 0; j < n; j++)
				d[0][j] = j;

			for (int j = 1; j < n; j++) {
				for (int i = 1; i < m; i++) {
					if (s[i - 1] == t[j - 1]) {
						d[i][j] = d[i - 1][j - 1];
					} else {
						int dij = Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1);
						d[i][j] = Math.min(dij, d[i - 1][j - 1] + 1);
					}
				}
			}
			return d[m - 1][n - 1];
		}
	}

	private static final class Damerau extends SimilarityFunctions {
		@Override
		public final double update(double difference, double product, double weight) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public final double normalize(double x, double sumwt, double s1, double s2) {
			return Globals.MISSING_VALUE;
		}

		@Override
		public boolean isSimilarity() {
			return false;
		}

		/*
		 * Copyright (c) 2012 Kevin L. Stern
		 * 
		 * Permission is hereby granted, free of charge, to any person obtaining a copy
		 * of this software and associated documentation files (the "Software"), to deal
		 * in the Software without restriction, including without limitation the rights
		 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
		 * copies of the Software, and to permit persons to whom the Software is
		 * furnished to do so, subject to the following conditions:
		 * 
		 * The above copyright notice and this permission notice shall be included in
		 * all copies or substantial portions of the Software.
		 * 
		 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
		 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
		 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
		 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
		 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
		 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
		 * SOFTWARE.
		 */

		/*
		 * The Damerau-Levenshtein Algorithm is an extension to the Levenshtein
		 * Algorithm which solves the edit distance problem between a source string and
		 * a target string with the following operations:
		 * 
		 * <ul>
		 * <li>Character Insertion</li>
		 * <li>Character Deletion</li>
		 * <li>Character Replacement</li>
		 * <li>Adjacent Character Swap</li>
		 * </ul>
		 * 
		 * Note that the adjacent character swap operation is an edit that may be
		 * applied when two adjacent characters in the source string match two adjacent
		 * characters in the target string, but in reverse order, rather than a general
		 * allowance for adjacent character swaps.
		 * <p>
		 * 
		 * This implementation allows the client to specify the costs of the various
		 * edit operations with the restriction that the cost of two swap operations
		 * must not be less than the cost of a delete operation followed by an insert
		 * operation. This restriction is required to preclude two swaps involving the
		 * same character being required for optimality which, in turn, enables a fast
		 * dynamic programming solution.
		 * <p>
		 * 
		 * The running time of the Damerau-Levenshtein algorithm is O(n*m) where n is
		 * the length of the source string and m is the length of the target string.
		 * This implementation consumes O(n*m) space.
		 * 
		 * @author Kevin L. Stern
		 */
		@Override
		public double compute(String source, String target) {
			int deleteCost = 1;
			int insertCost = 1;
			int replaceCost = 1;
			int swapCost = 1;

			if (source.length() == 0) {
				return target.length() * insertCost;
			}
			if (target.length() == 0) {
				return source.length() * deleteCost;
			}
			int[][] table = new int[source.length()][target.length()];
			Map<Character, Integer> sourceIndexByCharacter = new HashMap<Character, Integer>();
			if (source.charAt(0) != target.charAt(0)) {
				table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
			}
			sourceIndexByCharacter.put(source.charAt(0), 0);
			for (int i = 1; i < source.length(); i++) {
				int deleteDistance = table[i - 1][0] + deleteCost;
				int insertDistance = (i + 1) * deleteCost + insertCost;
				int matchDistance = i * deleteCost + (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
				table[i][0] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
			}
			for (int j = 1; j < target.length(); j++) {
				int deleteDistance = table[0][j - 1] + insertCost;
				int insertDistance = (j + 1) * insertCost + deleteCost;
				int matchDistance = j * insertCost + (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
				table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
			}
			for (int i = 1; i < source.length(); i++) {
				int maxSourceLetterMatchIndex = source.charAt(i) == target.charAt(0) ? 0 : -1;
				for (int j = 1; j < target.length(); j++) {
					Integer candidateSwapIndex = sourceIndexByCharacter.get(target.charAt(j));
					int jSwap = maxSourceLetterMatchIndex;
					int deleteDistance = table[i - 1][j] + deleteCost;
					int insertDistance = table[i][j - 1] + insertCost;
					int matchDistance = table[i - 1][j - 1];
					if (source.charAt(i) != target.charAt(j)) {
						matchDistance += replaceCost;
					} else {
						maxSourceLetterMatchIndex = j;
					}
					int swapDistance;
					if (candidateSwapIndex != null && jSwap != -1) {
						int iSwap = candidateSwapIndex;
						int preSwapCost;
						if (iSwap == 0 && jSwap == 0) {
							preSwapCost = 0;
						} else {
							preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
						}
						swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost + (j - jSwap - 1) * insertCost
								+ swapCost;
					} else {
						swapDistance = Integer.MAX_VALUE;
					}
					table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance),
							swapDistance);
				}
				sourceIndexByCharacter.put(source.charAt(i), i);
			}
			return table[source.length() - 1][target.length() - 1];
		}
	}
}
