/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;


public class OneWayANOVA {

	public static double[] compute(double[][] data) {
		/* ignores empty cell */
		int nGroups = data.length;
		double[][] stats = new double[nGroups][];
		double nCases = 0;
		double grandMean = 0;
		int ng = 0;
		for (int k = 0; k < nGroups; k++) {
			stats[k] = Statistics.compute(data[k], null);
			if (stats[k][Statistics.COUNT] > 0) {
				grandMean += stats[k][Statistics.SUM];
				nCases += stats[k][Statistics.COUNT];
				ng++;
			}
		}
		grandMean /= nCases;
		double dfBetween = ng - 1;
		double dfWithin = nCases - ng;
		double ssw = 0;
		double ssb = 0;
		for (int k = 0; k < nGroups; k++) {
			if (stats[k][Statistics.COUNT] > 0) {
				ssb += stats[k][Statistics.COUNT] * (stats[k][Statistics.MEAN] - grandMean)
						* (stats[k][Statistics.MEAN] - grandMean);
				ssw += (stats[k][Statistics.COUNT] - 1) * stats[k][Statistics.VARIANCE];
			}
		}
		double f = (dfWithin * ssb) / (dfBetween * ssw);
		double p = 1 - Probabilities.cdf(f, dfBetween, dfWithin, Probabilities.F, false);
		return new double[] { f, dfBetween, dfWithin, p };
	}
}