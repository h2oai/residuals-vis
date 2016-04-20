/**
 * Copyright (c) 2016 H2O Inc.
 * Leland Wilkinson
 */
package com.h2o.online.data.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Aggregator {
	/*
	 * Hartigan (1975) Leader Algorithm
	 */
	private int nRows, nCols;
	private double delta, radius;
	private List<double[]> exemplars;
	private List<Integer> counts;
	private ArrayList<ArrayList<Integer>> memberIndices;

	public Aggregator() {
	}

	public ArrayList<ArrayList<Integer>> getMemberIndices() {
		/* indices of cases that are close to each exemplar */
		return memberIndices;
	}

	public int[] getCounts() {
		Object[] countsArray = counts.toArray();
		int[] result = new int[counts.size()];
		for (int i = 0; i < counts.size(); i++) {
			result[i] = (Integer) countsArray[i];
		}
		return result;
	}

	public double[][] compute(double[][] data) {
		nRows = data.length;
		nCols = data[0].length;
		if (nRows < 10000)
			return data;

		radius = .1 / Math.pow(Math.log(nRows), 1.0 / nCols);

		initializeParameters(data);
		/* an exemplar is a case that represents a set of members */
		for (int i = 1; i < nRows; i++) {
			/* find closest exemplar to this case */
			double distanceToNearestExemplar = Double.POSITIVE_INFINITY;
			Iterator<double[]> it = exemplars.iterator();
			int closestExemplarIndex = 0;
			int index = 0;
			while (it.hasNext()) {
				double[] e = it.next();
				double d = squaredEuclideanDistance(e, data[i]);
				if (d < distanceToNearestExemplar) {
					distanceToNearestExemplar = d;
					closestExemplarIndex = index;
				}
				/* do not need to look further even if some other exemplar is closer */
				if (distanceToNearestExemplar < delta)
					break;
				index++;
			}
			/* found a close exemplar, so add to list */
			if (distanceToNearestExemplar < delta) {
				Integer count = counts.get(closestExemplarIndex);
				counts.set(closestExemplarIndex, count + 1);
				memberIndices.get(closestExemplarIndex).add(i);
				/* otherwise, assign a new exemplar */
			} else {
				exemplars.add(data[i]);
				counts.add(1);
				ArrayList<Integer> member = new ArrayList<Integer>();
				member.add(i);
				memberIndices.add(member);
			}
		}

		Object[] exemplarArray = exemplars.toArray();
		double[][] result = new double[exemplars.size()][nCols];
		for (int i = 0; i < exemplars.size(); i++) {
			result[i] = (double[]) exemplarArray[i];
		}
		return result;
	}

	private void initializeParameters(double[][] data) {
		exemplars = new ArrayList<double[]>();
		counts = new ArrayList<Integer>();
		memberIndices = new ArrayList<ArrayList<Integer>>();

		exemplars.add(data[0]);
		counts.add(1);
		memberIndices.add(new ArrayList<Integer>());
		memberIndices.get(0).add(0);
		delta = radius * radius; 
	}

	private double squaredEuclideanDistance(double[] e1, double[] e2) {
		double sum = 0;
		int n = 0;
		for (int j = 0; j < nCols; j++) {
			double d1 = e1[j];
			double d2 = e2[j];
			if (!isMissing(d1) && !isMissing(d2)) {
				sum += (d1 - d2) * (d1 - d2);
				n++;
			}
		}
		sum *= (double) nCols / n;
		return sum;
	}
	
	private boolean isMissing(double x) {
		return Double.isNaN(x);
	}
}
