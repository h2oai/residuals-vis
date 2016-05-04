/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util;

import java.util.Arrays;
import java.util.Comparator;

import com.h2o.online.Globals;

public class Sorts {

	private Sorts() {
	}

	public static void doubleArraySort(double[] x, int fromIndex, int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Arrays.sort(x, fromIndex, toIndex);
	}

	public static int[] indexedDoubleArraySort(final double[] x, int fromIndex, int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Integer[] sortOrder = new Integer[toIndex - fromIndex];
		for (int i = 0; i < sortOrder.length; i++)
			sortOrder[i] = new Integer(i);

		Arrays.sort(sortOrder, fromIndex, toIndex, new Comparator<Integer>() {
			@Override
			public int compare(Integer index1, Integer index2) {
				return Double.compare(x[index1], x[index2]);
			}
		});

		int[] result = new int[sortOrder.length];
		for (int i = 0; i < result.length; i++)
			result[i] = sortOrder[i].intValue();

		return result;
	}

	public static void stringArraySort(String[] x, int fromIndex, int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Arrays.sort(x, fromIndex, toIndex);
	}

	public static int[] indexedStringArraySort(final String[] x, int fromIndex, int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Integer[] sortOrder = new Integer[toIndex - fromIndex];
		for (int i = 0; i < sortOrder.length; i++)
			sortOrder[i] = new Integer(i);

		Arrays.sort(sortOrder, fromIndex, toIndex, new Comparator<Integer>() {
			@Override
			public int compare(Integer index1, Integer index2) {
				return x[index1].compareTo(x[index2]);
			}
		});

		int[] result = new int[sortOrder.length];
		for (int i = 0; i < result.length; i++)
			result[i] = sortOrder[i].intValue();

		return result;
	}

	public static void doubleMatrixSort(double[][] x, final int colIndex, int fromIndex, int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Arrays.sort(x, fromIndex, toIndex, new Comparator<Object>() {
			@Override
			public int compare(Object object1, Object object2) {
				double[] x1 = (double[]) object1;
				double[] x2 = (double[]) object2;
				return Double.compare(x1[colIndex], x2[colIndex]);
			}
		});
	}

	public static int[] indexedDoubleMatrixSort(final double[][] x, final int colIndex, int fromIndex, int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Integer[] sortOrder = new Integer[toIndex - fromIndex];
		for (int i = 0; i < sortOrder.length; i++)
			sortOrder[i] = new Integer(i);

		Arrays.sort(sortOrder, fromIndex, toIndex, new Comparator<Integer>() {
			@Override
			public int compare(Integer index1, Integer index2) {
				return Double.compare(x[index1][colIndex], x[index2][colIndex]);
			}
		});

		int[] result = new int[sortOrder.length];
		for (int i = 0; i < result.length; i++)
			result[i] = sortOrder[i].intValue();

		return result;
	}

	public static void nestedDoubleMatrixSort(double[][] x, final int colIndex1, final int colIndex2, int fromIndex,
			int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Arrays.sort(x, fromIndex, toIndex, new Comparator<Object>() {
			@Override
			public int compare(Object object1, Object object2) {
				double[] x1 = (double[]) object1;
				double[] x2 = (double[]) object2;
				int i = Double.compare(x1[colIndex1], x2[colIndex1]);
				if (i != 0)
					return i;
				i = Double.compare(x1[colIndex2], x2[colIndex2]);
				return i;
			}
		});
	}

	public static void integerArraySort(int[] x, int fromIndex, int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Arrays.sort(x, fromIndex, toIndex);
	}

	public static int[] indexedIntegerArraySort(final int[] x, int fromIndex, int toIndex) {
		if (fromIndex == toIndex) {
			fromIndex = 0;
			toIndex = x.length;
		}
		Integer[] sortOrder = new Integer[toIndex - fromIndex];
		for (int i = 0; i < sortOrder.length; i++)
			sortOrder[i] = new Integer(i);

		Arrays.sort(sortOrder, fromIndex, toIndex, new Comparator<Integer>() {
			@Override
			public int compare(Integer index1, Integer index2) {
				if (x[index1] > x[index2]) {
					return 1;
				} else if (x[index1] < x[index2]) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		int[] result = new int[sortOrder.length];
		for (int i = 0; i < result.length; i++)
			result[i] = sortOrder[i].intValue();

		return result;
	}

	public static int[] indexedArraySort(final Object[] a) {
		int fromIndex = 0;
		int toIndex = a.length;
		Integer[] sortOrder = new Integer[toIndex - fromIndex];
		for (int i = 0; i < sortOrder.length; i++)
			sortOrder[i] = new Integer(i);

		Arrays.sort(sortOrder, fromIndex, toIndex, new Comparator() {
			@Override
			public int compare(Object object1, Object object2) {
				int firstIndex = ((Integer) object1).intValue();
				int secondIndex = ((Integer) object2).intValue();
				Double x1 = (Double) a[firstIndex];
				Double x2 = (Double) a[secondIndex];
				return x1.compareTo(x2);
			}
		});

		int[] result = new int[sortOrder.length];
		for (int i = 0; i < result.length; i++)
			result[i] = sortOrder[i].intValue();

		return result;
	}

	public static void loadingsSort(double[][] a, String[] labels) {
		int m = a.length;
		int n = a[0].length;
		int l = 0;
		int jf = 0;
		for (int j = 0; j < n; j++) {
			for (jf = l; jf < m; jf++) {
				int max = jf;
				for (int i = jf; i < m; i++) {
					if (Math.abs(a[i][j]) > Math.abs(a[max][j]))
						max = i;
				}
				if (Math.abs(a[max][j]) < .5 && j < n - 1)
					break;
				for (int k = 0; k < n; k++) {
					double temp = a[jf][k];
					a[jf][k] = a[max][k];
					a[max][k] = temp;
				}
				String lab = labels[jf];
				labels[jf] = labels[max];
				labels[max] = lab;
				if (jf == m - 1)
					return;
			}
			l = jf;
		}
	}

	public static double[] rank(double[] a) {

		int n = a.length;
		double[] ranks = new double[n];
		int[] index = indexedDoubleArraySort(a, 0, n);

		int lind = index[0];
		double am = a[lind];
		int k1 = 0;
		int k2 = 0;
		double ak = 1.0;
		/* kms allows for missing data */
		int kms = 1;
		for (int k = 1; k < n; k++) {
			int kind = index[k];
			boolean insert = true;
			if (!Globals.isMissing(am)) {
				kms++;
				if (a[kind] == am) {
					k2 = k;
					ak += 0.5;
					if (k < n - 1)
						insert = false;
				}
				if (insert) {
					for (int l = k1; l <= k2; l++) {
						lind = index[l];
						ranks[lind] = ak;
					}
					if (k2 != n - 1 && k == n - 1)
						ranks[kind] = kms;
				}
			}
			if (insert) {
				k1 = k;
				k2 = k;
				ak = kms;
				am = a[kind];
			}
		}
		return ranks;
	}
}
