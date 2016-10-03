/**
 * Copyright (c) 2013 Skytree Inc.
 */
package com.h2o.online.analytics.util;

import com.h2o.online.analytics.util.statistics.regression.Regression;

public abstract class FunctionEvaluator {
	public static final String LOGARITHMIC_SIGMOID = "LogarithmicSigmoid", LOGISTIC_SIGMOID = "LogisticSigmoid",
			EXPONENTIAL_GROWTH = "ExponentialGrowth", EXPONENTIAL_DECAY = "ExponentialDecay",
			EXPONENTIAL_INVERTED_DECAY = "ExponentialInvertedDecay", MICHAELIS_MENTEN = "MichaelisMenten",
			DAMPED_EXPONENTIAL = "DampedExponential", VANDEEMTER = "VanDeemter", KINETIC = "Kinetic", LOG = "Logarithmic", GIBBS = "Gibbs",
			VAPOR = "VaporPressure", WEIBULL = "Weibull", HOERL = "Hoerl", GOMPERTZ = "Gompertz", PIECEWISE_LINEAR = "PiecewiseLinear",
			PIECEWISE_DISCONTINUOUS_LINEAR = "PiecewiseDiscontinuousLinear", HILL = "Hill", LOG_LOGISTIC = "LogLogistic",
			FOUR_PARAMETER_LOG_LOGISTIC = "FourParameterLogLogistic", ROBUST_LINEAR = "RobustLinear", HYPERBOLIC = "Hyperbolic",
			SINUSOIDAL = "Sinusoidal";

	public boolean isLinear;
	public Regression reg;
	public double[] minmax;

	public abstract double[] getInitialParameterEstimates();

	public abstract String[] getLabels();

	public abstract double function(double[] parms);

	public abstract double function(double[] parms, double[] data);

	public abstract double[] gradient(double[] parms);

	public abstract double[] gradient(double[] parms, double[] data);

	public abstract double[][] hessian(double[] parms);
}