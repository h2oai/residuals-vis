package com.h2o.online.analytics.util;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.statistics.regression.OrdinaryLeastSquaresRegression;
import com.h2o.online.analytics.util.statistics.regression.Regression;

public class WeibullFunction extends FunctionEvaluator {
	private double[][] xData;
	private double[] yData;
	private double[] weights;
	private static final String formula = "<i>y</i> = 1 - exp(-((<i>x</i>/<i><b>b</b></i>) <sup><i><b>c</b></i></sup>)";
	private static final String name = "Weibull Function";
	int no, np;

	public WeibullFunction(double[][] xData, double[] yData, double[] weights) {
		this.xData = xData;
		this.yData = yData;
		this.weights = weights;
		no = xData.length;
		np = 2;
	}

	public static String getFormula() {
		return formula;
	}

	public static String getName() {
		return name;
	}

	public static double[] getRange() {
		return new double[] { 0, 1 };
	}

	public static double[] getDomain() {
		return new double[] { 0, Double.POSITIVE_INFINITY };
	}

	public static int slope() {
		return 1;
	}

	public double[] getInitialParameterEstimates() {
		/* equation is:
		 *    y = (1 - exp(-(x/a)^b)) , where
		 *    parms[0] is a
		 *    parms[1] is b
		 */
		double[] y = new double[no];
		double[] x = new double[no];
		for (int i = 0; i < no; i++) {
			double mbr = (i - .3) / (no + .4);
			x[i] = Math.log(xData[i][1]);
			y[i] = Math.log(-Math.log(1 - mbr));
		}
		OrdinaryLeastSquaresRegression reg = new OrdinaryLeastSquaresRegression();
		reg.compute(x, y, null, Regression.LINEAR);
		double[] parameters = new double[np];
		double b = reg.coefficients[1];
		double a = Math.exp(-reg.coefficients[0] / b);
		parameters[0] = a;
		parameters[1] = b;
		System.out.println("a = " + parameters[0] + " b = " + parameters[1]);
		return parameters;
	}

	public String[] getLabels() {
		return new String[] { "a", "b" };
	}

	public double function(double[] parms, double[] data) {
		/* estimate f(x), where x is contained in data array*/
		if (data == null)
			return Double.NaN;
		double result = 1 - Math.exp(-Math.pow(data[1] / parms[0], parms[1]));
		return result;
	}

	public double function(double[] parms) {
		/* log likelihood */
		int p = xData.length;
		int m = p - 2;
		double sum1 = 0;
		double sum2 = 0;
		for (int i = 0; i < p; i++) {
			double temp = xData[i][1];
			if (temp <= 0)
				temp = Globals.FUZZ;
			if (i < m) {
				sum1 += Math.log(temp);
			}
			sum2 += Math.pow(temp / parms[0], parms[1]);
		}
		double logLikelihood = m * Math.log(parms[1]) - m * parms[1] * Math.log(parms[0]) + (parms[1] - 1) * sum1 - sum2;
		System.out.println("loss " + -logLikelihood + " " + parms[0] + " " + parms[1]);
		return -logLikelihood;
	}

	public double[] gradient(double[] parms, double[] data) {
		int p = data.length;
		int m = p - 2;
		double[] g = new double[parms.length];
		double sum1 = 0;
		double sum2 = 0;
		double sum3 = 0;
		for (int i = 0; i < p; i++) {
			double temp = data[i];
			if (temp <= 0)
				temp = Globals.FUZZ;
			if (i < m) {
				sum1 += Math.log(temp);
			}
			sum2 += Math.pow(temp / parms[0], parms[1]);
			sum3 += Math.pow(temp / parms[0], parms[1]) * Math.log(temp / parms[0]);
		}
		g[0] = -m * parms[1] / parms[0] + sum2 * parms[1] / parms[0];
		g[1] = m / parms[1] - m * Math.log(parms[0]) + sum1 - sum3;
		return g;
	}

	public double[] gradient(double[] parms) {
		return null;
	}

	public double[][] hessian(double[] parms) {
		return null;
	}
}