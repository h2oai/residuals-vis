/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.anomalies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.analytics.Analytic;
import com.h2o.online.analytics.util.CategoricalToContinuousMap;
import com.h2o.online.analytics.util.Matrices;
import com.h2o.online.analytics.util.Sorts;
import com.h2o.online.analytics.util.statistics.Cognostics;
import com.h2o.online.analytics.util.statistics.Statistics;
import com.h2o.online.analytics.util.statistics.regression.TrimmedRegression;
import com.h2o.online.data.DataTable;
import com.h2o.online.data.util.Aggregator;
import com.h2o.online.graphics.ParallelCoordinates;
import com.h2o.online.graphics.Scatterplots;

@SuppressWarnings("unchecked")
public class Outliers extends Analytic {
	/*
	 * Use Hartigan Leader Algorithm with nearest-neighbor and random
	 * projections to find outliers (Leland Wilkinson July 23, 2015)
	 */
	List<ArrayList<Integer>> clusterMembers;

	public Outliers() {
	}

	public JSONMap compute(DataTable table, JSONMap parameters) {
		int nRows = table.getNumRows();
		int nCols = table.getNumCols();

		/* get parameters */
		double alpha = .05;
		if (parameters != null) {
			Object o = parameters.get("alpha");
			if (o != null)
				alpha = (Double) o;
		}

		JSONArray univariateResults = new JSONArray();
		JSONArray timeSeriesResults = new JSONArray();
		JSONMap multivariateResults = new JSONMap();
		JSONArray idiosyncraticResults = new JSONArray();
		JSONMap outlierResults = new JSONMap();
		JSONMap duplicateResults = new JSONMap();

		int count;
		int nReps = 1000;
		long seed = 3523;
		/*
		 * pre-process categorical variables by mapping them to continuous
		 * scales using multiple correspondence analysis
		 */
		 double[][] mca = CategoricalToContinuousMap.compute(table, nRows,
		 nCols);
		
		/* copy data into work array x */
		boolean hasCategoricalColumns = false;
		double[][] x = new double[nRows][nCols];
		for (int i = 0; i < nRows; i++) {
			int nc = -1;
			for (int j = 0; j < nCols; j++) {
				if (table.isMissing(i, j))
					continue;
				if (table.isCategoricalVariable(j)) {
					hasCategoricalColumns = true;
					nc++;
					x[i][j] = Math.random();
				} else {
					x[i][j] = table.getRawDoubleValue(i, j);
				}
			}
		}

		/* compute idiosyncratic outliers */

		//		 if (!hasCategoricalColumns) {
		//		 JSONArray idiosyncraticOutlierIndices =
		//		 computeIdiosyncraticOutliers(x);
		//		 if (idiosyncraticOutlierIndices != null &&
		//		 idiosyncraticOutlierIndices.size() > 0)
		//		 idiosyncraticResults.add(idiosyncraticOutlierIndices);
		//		 }

		/* compute multivariate outliers */

		count = 0;
			//			System.out.println("nCols before projection " + nCols);
			if (nCols > 10000) {
				/*
				* Note! : projectData sets x to a new and smaller array to deal
				* with curse of dimensionality
				*/
				double eps = .2;
				int m = (int) ((4.0 * Math.log(nRows) / (eps * eps / 2.0 - eps * eps * eps / 3.0)));
				x = Matrices.sparseRandomProjection(x, m);
				nCols = x[0].length;
			}
			//			Globals.random.setSeed(seed);
			//			for (int reps = 0; reps < nReps; reps++) {
			//				nRows = 500;
			//				nCols = 5;
			//				x = new double[nRows][nCols];
			//				for (int i = 0; i < nRows; i++) {
			//					for (int j = 0; j < nCols; j++) {
			//						x[i][j] = Globals.random.nextGaussian();
			//						//									x[i][j] = Globals.random.nextDouble();
			//						//						x[i][j] = -Math.log(x[i][j]) / 1.0;
			//					}
			//				}

			Statistics.unitize(x, null);

			Aggregator compressor = new Aggregator();
			x = compressor.compute(x);
			clusterMembers = compressor.getMemberIndices();
			nRows = x.length;
			//				System.out.println("clusters " + x.length);

			double[] y = computeDistancesToNearestNeighbor(x);
			double bound = Cognostics.outlierUpperBound(y, null, alpha);
			JSONArray multivariateOutlierIndices = identifyOutliers(y, bound);

			if (multivariateOutlierIndices != null && multivariateOutlierIndices.size() > 0) {
				count++;
				multivariateResults.put(Globals.JSON_ROWINDICES_LIST, multivariateOutlierIndices);
				if (table.getNumCols() == 2) {
											Scatterplots scatterplots = new Scatterplots();
											JSONMap scatterplotResults = scatterplots.compute(table, parameters, null);
											multivariateResults.put(Globals.JSON_SCATTERPLOTS_LIST, scatterplotResults);
				} else {
					ArrayList<ArrayList<Integer>> clusterMemberIndices = compressor.getMemberIndices();
					if (clusterMemberIndices != null) {
						JSONArray selectedRows = new JSONArray();
						for (int i = 0; i < clusterMemberIndices.size(); i++) {
							ArrayList<Integer> indices = clusterMemberIndices.get(i);
							selectedRows.add(indices.get(0));
						}
						parameters.put("selectedRows", selectedRows);
					}
					 ParallelCoordinates parallelCoordinates = new
					 ParallelCoordinates();
					 JSONMap parallelCoordinatesResults =
					 parallelCoordinates.compute(table, parameters,
					 compressor);
					 multivariateResults.put(Globals.JSON_PARALLELCOORDINATES_LIST,
					 parallelCoordinatesResults);
				}
			}
		System.out.println("count " + count + " " + (alpha * nReps));

		/* check for duplicates */

		// JSONArray duplicateIndices = computeDuplicates(table);
		// if (duplicateIndices != null && duplicateIndices.size() > 0) {
		// duplicateResults.put("duplicateIndices", duplicateIndices);
		// ParallelCoordinates parallelCoordinates = new ParallelCoordinates();
		// JSONMap parallelCoordinatesResults =
		// parallelCoordinates.compute(table, parameters, null);
		// duplicateResults.put(Globals.JSON_PARALLELCOORDINATES,
		// parallelCoordinatesResults);
		//		}

		if (univariateResults != null && univariateResults.size() > 0)
			outlierResults.put(Globals.JSON_UNIVARIATE_OUTLIERS_LIST, univariateResults);
		if (timeSeriesResults != null && timeSeriesResults.size() > 0)
			outlierResults.put(Globals.JSON_TIME_SERIES_OUTLIERS_LIST, timeSeriesResults);
		if (multivariateResults != null && multivariateResults.size() > 0)
			outlierResults.put(Globals.JSON_MULTIVARIATE_OUTLIERS_LIST, multivariateResults);
		if (idiosyncraticResults != null && idiosyncraticResults.size() > 0)
			outlierResults.put(Globals.JSON_IDIOSYNCRATIC_OUTLIERS_LIST, idiosyncraticResults);
		if (duplicateResults != null && duplicateResults.size() > 0)
			outlierResults.put(Globals.JSON_DUPLICATE_ROWS_LIST, duplicateResults);
		return outlierResults;
	}

	private JSONArray identifyOutliers(double[] y, double bound) {
		JSONArray outlierIndices = new JSONArray();
		if (Globals.isMissing(bound))
			return outlierIndices;
		for (int i = 0; i < y.length; i++) {
			if (y[i] > bound) {
				ArrayList<Integer> member = new ArrayList<Integer>();
				if (clusterMembers != null)
					member = clusterMembers.get(i);
				else
					member.add(new Integer(i));
				outlierIndices.add(member);
			}
		}
		return outlierIndices;
	}

	private double[] computeDistancesToNearestNeighbor(double[][] x) {
		int n = x.length;
		double[] distancesToNearestNeighbor = new double[n];
		if (x[0].length == 1) {
			for (int i = 1; i < n; i++) {
				distancesToNearestNeighbor[i] = x[i][0];
			}
		} else {
			for (int i = 0; i < n; i++) {
				distancesToNearestNeighbor[i] = Math.sqrt(findDistanceToNearestNeighbor(x, i));
			}
		}
		return distancesToNearestNeighbor;
	}

	private double findDistanceToNearestNeighbor(double[][] x, int index) {
		double[] exemplar = x[index];
		double smallestDistance = Double.POSITIVE_INFINITY;
		for (int i = 0; i < x.length; i++) {
			if (i == index)
				continue;
			double[] e = x[i];
			double d = squaredEuclideanDistance(e, exemplar);
			if (d < smallestDistance) {
				smallestDistance = d;
			}
		}
		return smallestDistance;
	}

	private double squaredEuclideanDistance(double[] e1, double[] e2) {
		double sum = 0;
		int n = 0;
		for (int j = 0; j < e1.length; j++) {
			double d1 = e1[j];
			double d2 = e2[j];
			if (!Globals.isMissing(d1) && !Globals.isMissing(d2)) {
				sum += (d1 - d2) * (d1 - d2);
				n++;
			}
		}
		sum *= (double) e1.length / n;
		return sum;
	}

	private JSONArray computeTimeSeriesOutliers(double[] data) {
		JSONArray outlierIndices = new JSONArray();
		int nRows = data.length;
		double[] z = new double[nRows];
		System.arraycopy(data, 0, z, 0, nRows);
		TrimmedRegression trim = new TrimmedRegression();
		double[][] values = trim.compute(z, .7);
		for (int i = 0; i < nRows; i++)
			z[i] = values[i][1]; // smoother residuals
		double[] cutoffs = Cognostics.outlierBounds(z, .01);
		for (int i = 0; i < nRows; i++) {
			if (z[i] < cutoffs[0] || z[i] > cutoffs[1])
				outlierIndices.add(i);
		}
		return outlierIndices;
	}

	private JSONArray computeIdiosyncraticOutliers(double[][] data) {
		int nRows = data.length;
		int nCols = data[0].length;
		if (nRows > Globals.TOO_BIG || nCols < Globals.TOO_SMALL)
			return null;
		JSONArray outlierIndices = new JSONArray();
		Statistics.standardize(data, null);
		double alpha = .05 / nRows;
		for (int i = 0; i < nRows; i++) {
			double[] cutoffs = Cognostics.outlierBounds(data[i], alpha);
			for (int j = 0; j < nCols; j++) {
				if (Math.abs(data[i][j]) > 4 && (data[i][j] < cutoffs[0] || data[i][j] > cutoffs[1]))
					outlierIndices.add(i);
			}
		}
		return outlierIndices;
	}

	private JSONArray computeDuplicates(DataTable table) {
		int nRows = table.getNumRows();
		int nCols = table.getNumCols();
		if (nCols < Globals.TOO_SMALL)
			return null;
		JSONArray results = new JSONArray();
		Map duplicates = new HashMap();
		int maxSize = 0;
		for (int i = 0; i < nRows; i++) {
			Object[] row = table.getRawRow(i);
			ArrayList<Object> key = new ArrayList<Object>(Arrays.asList(row));
			if (duplicates.containsKey(key)) {
				JSONArray cases = (JSONArray) duplicates.get(key);
				cases.add(new Integer(i));
				if (cases.size() > maxSize)
					maxSize = cases.size();
			} else {
				JSONArray cases = new JSONArray();
				cases.add(new Integer(i));
				duplicates.put(key, cases);
			}
			if (maxSize < 2 && duplicates.size() > 10000)
				return null;
		}
		Object[] keys = duplicates.keySet().toArray();
		for (int k = 0; k < keys.length; k++) {
			Object key = keys[k];
			JSONArray dups = (JSONArray) duplicates.get(key);
			if (dups.size() > 1)
				results.add(dups);
		}
		return results;
	}
}