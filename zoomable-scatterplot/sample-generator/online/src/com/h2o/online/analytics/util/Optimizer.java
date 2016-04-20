package com.h2o.online.analytics.util;

import java.util.Random;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.statistics.LinearSystems;

public class Optimizer {
	/* Collection of Minimizers */
	public double MAX_ITERATIONS = 200; // Maximum number of iterations.
	public double MAX_STEP = 100; // Maximum step length in line searches.
	private static Random random = Globals.random;
	private FunctionEvaluator evaluator;
	private int pivot = -1;
	private int nRestarts;
	public double[][] hessian;

	public Optimizer(FunctionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public int getPivot() {
		return pivot;
	}

	public boolean newton(double[] parms, boolean isFisherScoring) {
		/* Newton-Raphson Minimization */
		int np = parms.length;
		double[] d = new double[np];
		double[] xmin = new double[np];
		double fmin = evaluator.function(parms);
		System.arraycopy(parms, 0, xmin, 0, np);
		for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
			double[] g = evaluator.gradient(parms);
			double[][] h = evaluator.hessian(parms);

			h = LinearSystems.positiveDefinite(h);
			LinearSystems.symdet(h);
			LinearSystems.symsol(h, g, d);

			boolean isConverged = true;
			for (int j = 0; j < np; j++) {
				if (isFisherScoring)
					parms[j] += d[j];
				else
					parms[j] -= d[j];
				if (Math.abs(d[j] / parms[j]) > Globals.FUZZ)
					isConverged = false;
			}
			double f = evaluator.function(parms);

			if (Double.isNaN(f) || Double.isInfinite(f)) {
				System.arraycopy(xmin, 0, parms, 0, np);
				isConverged = false;
			}

			if (f / fmin < Globals.FUZZ)
				isConverged = true;

			if (f < fmin)
				System.arraycopy(parms, 0, xmin, 0, np);
			else
				System.arraycopy(xmin, 0, parms, 0, np);

			if (isConverged)
				return true;

			fmin = f;
			//						System.out.println(iter + " fit: " + fmin + " coef: " + parms[0] + " " + parms[1]);
		}
		return false;
	}

	public boolean levenbergMarquardt(double[][] x, double[] y, double[] parms) {
		/* Levenberg-Marquardt Minimization */
		int np = parms.length;
		int no = y.length;
		hessian = new double[np][np];
		double[][] hessianPlusLambda = new double[np][np];
		double[][] jacobian = new double[no][np];
		double[] gradient = new double[np];
		double[] delta = new double[np];
		double[] previousParms = new double[np];
		double lambda = .01;
		boolean isConverged = false;
		double loss = 0;
		for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
			loss = evaluator.function(parms);
			if (loss >= Globals.OVERFLOW || Double.isNaN(loss))
				return false;

			double previousLoss = loss;
			System.arraycopy(parms, 0, previousParms, 0, np);
			for (int j = 0; j < np; j++) {
				for (int i = 0; i < no; i++) {
					jacobian[i] = evaluator.gradient(parms, x[i]);
				}
			}

			for (int k = 0; k < np; k++) {
				for (int j = 0; j < np; j++) {
					hessian[j][k] = 0.0;
					for (int i = 0; i < no; i++) {
						hessian[j][k] += jacobian[i][j] * jacobian[i][k];
					}
				}
			}

			hessian = LinearSystems.positiveDefinite(hessian);

			for (int k = 0; k < np; k++) {
				gradient[k] = 0.0;
				for (int i = 0; i < no; i++)
					gradient[k] -= (evaluator.function(parms, x[i]) - y[i]) * jacobian[i][k];
			}

			double decrement = 0;
			/* marquardting loop */
			do {
				for (int j = 0; j < np; j++) {
					System.arraycopy(hessian[j], 0, hessianPlusLambda[j], 0, np);
					hessianPlusLambda[j][j] += lambda;
				}

				int sing = LinearSystems.symdet(hessianPlusLambda);
				//				if (sing >= 0)
				//					System.err.println("Singular in levenbergMarquardt");
				LinearSystems.symsol(hessianPlusLambda, gradient, delta);

				for (int j = 0; j < np; j++)
					parms[j] += delta[j];
				loss = evaluator.function(parms);
				if (loss >= Globals.OVERFLOW || Double.isNaN(loss))
					return false;

				decrement = (loss - previousLoss) / (1 + loss);
				if (decrement <= 0.0) {
					lambda /= 10;
					break;
				}

				for (int j = 0; j < np; j++)
					parms[j] -= delta[j];
				evaluator.function(parms);

				if (decrement < Globals.FUZZ)
					break;

				lambda *= 2;
			} while (lambda < 1.0e10);

			isConverged = true;
			for (int j = 0; j < np; j++) {
				if (Math.abs((parms[j] - previousParms[j]) / parms[j]) > Globals.FUZZ)
					isConverged = false;
			}
			if (isConverged) {
				break;
			}
			//			System.out.println(iter + " loss " + loss + " coef: " + Arrays.toString(parms));
		}
		//		if (!isConverged)
		//			System.out.println("did not converge ");
		return isConverged;
	}

	public boolean BFGS(double[] parms) {
		/*
		 * Broyden-Fletcher-Goldfarb-Shanno (BFGS) Quasi-Newton minimization of a multivariate function. Dennis, D.E. Jr. and
		 * Schnabel, R.B. (1983). Numerical Methods for Unconstrained Optimization and Nonlinear Equations. Englewood Cliffs:
		 * Prentice-Hall.
		 */
		int n = parms.length;
		hessian = new double[n][n];
		for (int j = 0; j < n; j++)
			hessian[j][j] = 1;

		double fp = evaluator.function(parms);
		double[] g = evaluator.gradient(parms);
		double[] gp = Matrices.product(-1, g);

		for (int iter = 1; iter <= MAX_ITERATIONS; iter++) {
			double[] pn = lineSearch(parms, fp, g, gp, evaluator);
			fp = evaluator.function(pn);
			gp = Matrices.difference(pn, parms);
			//			System.out.println(iter + " fit: " + fp + " coef: " + Arrays.toString(parms));
			System.arraycopy(pn, 0, parms, 0, n);
			double test = 0;
			for (int j = 0; j < n; j++)
				test = Math.max(test, Math.abs(gp[j]) / Math.max(Math.abs(parms[j]), 1));
			if (test < Globals.EPSILON)
				return true;

			double[] dg = new double[n];
			System.arraycopy(g, 0, dg, 0, n);
			g = evaluator.gradient(parms);

			test = 0;
			for (int j = 0; j < n; j++)
				test = Math.max(test, Math.abs(g[j]) * Math.max(Math.abs(parms[j]), 1)) / Math.max(fp, 1);
			if (test < Globals.EPSILON)
				return true;

			dg = Matrices.difference(g, dg);
			double[] hdg = Matrices.product(hessian, dg);
			double fac = Matrices.product(dg, gp);
			double fae = Matrices.product(dg, hdg);
			double dg2 = Matrices.product(dg, dg);
			double gp2 = Matrices.product(gp, gp);

			if (fac > Math.sqrt(Globals.EPSILON * dg2 * gp2)) {
				fac = 1 / fac;
				double fad = 1 / fae;
				dg = Matrices.difference(Matrices.product(fac, gp), Matrices.product(fad, hdg));
				hessian = Matrices.sum(hessian, Matrices.outerProduct(gp, Matrices.product(fac, gp)));
				hessian = Matrices.sum(hessian, Matrices.outerProduct(hdg, Matrices.product(fad, hdg)));
				hessian = Matrices.sum(hessian, Matrices.outerProduct(dg, Matrices.product(fae, dg)));
			}

			gp = Matrices.product(-1, Matrices.product(hessian, g));
		}
		return false;
	}

	private double[] lineSearch(double[] xOld, double fOld, double[] g, double[] p, FunctionEvaluator fe) {
		double ALPHA = 0.0001;
		int n = xOld.length;
		double[] xNew = new double[n];
		double ss = Matrices.norm(p);
		double maxStep = MAX_STEP * Math.max(ss, n);
		if (ss > maxStep) {
			for (int j = 0; j < n; j++)
				p[j] = p[j] * (maxStep / ss);
		}
		double slope = Matrices.norm(g);
		double test = 0;
		for (int j = 0; j < n; j++)
			test = Math.max(test, Math.abs(p[j]) / Math.max(Math.abs(xOld[j]), 1));
		double lambdaMin = Globals.FUZZ / test;
		double lambda = 1;
		double lambda2 = lambda;
		double f = 0;
		double f2 = f;

		for (int iter = 0; iter < 10; iter++) {
			double tlambda;
			for (int j = 0; j < n; j++)
				xNew[j] = xOld[j] + p[j] * lambda;
			f = fe.function(xNew);
			if (lambda < lambdaMin) {
				return xOld;
			} else if (f <= fOld + ALPHA * lambda * slope) {
				return xNew;
			} else {
				if (lambda == 1) {
					tlambda = -slope / (2 * (f - fOld - slope));
				} else {
					double rhs1 = f - fOld - lambda * slope;
					double rhs2 = f2 - fOld - lambda2 * slope;
					double a = (rhs1 / (lambda * lambda) - rhs2 / (lambda2 * lambda2)) / (lambda - lambda2);
					double b = (-lambda2 * rhs1 / (lambda * lambda) + lambda * rhs2 / (lambda2 * lambda2)) / (lambda - lambda2);
					if (a == 0) {
						tlambda = -slope / (2 * b);
					} else {
						double disc = b * b - 3 * a * slope;
						if (disc < 0)
							tlambda = 0.5 * lambda;
						else if (b <= 0)
							tlambda = (-b + Math.sqrt(disc)) / (3 * a);
						else
							tlambda = -slope / (b + Math.sqrt(disc));
					}
					if (tlambda > 0.5 * lambda)
						tlambda = 0.5 * lambda;
				}
			}
			lambda2 = lambda;
			f2 = f;
			lambda = Math.max(tlambda, 0.1 * lambda);
		}
		return xOld;
	}

	public boolean simplex(double[] parms, double[] bounds) {
		/*
		 * Nelder, J.A. & Mead, R. (1965). A simplex method for function minimization. Computer Journal, 7, 308-313.
		 */
		int nParameters = parms.length;
		int nVertices = nParameters + 1;
		double[] loss = new double[nVertices];
		double[][] simplex = new double[nVertices][nParameters];

		generateStartingSimplex(simplex, parms, bounds, nVertices, nParameters);
		int ilo = 0;
		int ihi = 0;
		for (int i = 0; i < nVertices; i++)
			loss[i] = evaluator.function(simplex[i]);

		double best = Double.POSITIVE_INFINITY;
		for (int iter = 1; iter < MAX_ITERATIONS; iter++) {
			bound(simplex, bounds);
			double flo = loss[0];
			double fhi = flo;
			ilo = 0;
			ihi = 0;
			for (int i = 1; i < nVertices; i++) {
				if (loss[i] < flo) {
					flo = loss[i];
					ilo = i;
				}
				if (loss[i] > fhi) {
					fhi = loss[i];
					ihi = i;
				}
			}

			double ghi = flo;
			for (int i = 0; i < nVertices; i++) {
				if (i != ihi && loss[i] > ghi) {
					ghi = loss[i];
				}
			}

			/* check for convergence */
			if (iter % (2 * nParameters) == 0) {
				if (Double.isNaN(loss[ilo]) || loss[ilo] > best * Globals.ONE_PLUS_FUZZ) { // loss increasing
					nRestarts++;
					if (nRestarts > 10)
						break;
					generateStartingSimplex(simplex, parms, bounds, nVertices, nParameters);
				} else if (loss[ilo] > best * Globals.ONE_MINUS_FUZZ)
					break;
				best = loss[ilo];
			}

			double[] centroid = new double[nParameters];
			for (int i = 0; i < nVertices; i++) {
				if (i != ihi) {
					for (int j = 0; j < nParameters; j++)
						centroid[j] += simplex[i][j];
				}
			}
			for (int j = 0; j < nParameters; j++)
				centroid[j] /= nParameters;

			/* reflection */
			double[] r = new double[nParameters];
			for (int j = 0; j < nParameters; j++)
				r[j] = 2 * centroid[j] - simplex[ihi][j];
			double fr = evaluator.function(r);

			if (flo <= fr && fr < ghi) {
				System.arraycopy(r, 0, simplex[ihi], 0, nParameters);
				loss[ihi] = fr;
				continue;
			}

			/* expansion */
			if (fr < flo) {
				double[] e = new double[nParameters];
				for (int j = 0; j < nParameters; j++)
					e[j] = 3 * centroid[j] - 2 * simplex[ihi][j];
				double fe = evaluator.function(e);
				if (fe < fr) {
					System.arraycopy(e, 0, simplex[ihi], 0, nParameters);
					loss[ihi] = fe;
					continue;
				} else {
					System.arraycopy(r, 0, simplex[ihi], 0, nParameters);
					loss[ihi] = fr;
					continue;
				}
			}

			/* contraction of side */
			if (fr < fhi) {
				double[] c = new double[nParameters];
				for (int j = 0; j < nParameters; j++)
					c[j] = 1.5 * centroid[j] - 0.5 * simplex[ihi][j];
				double fc = evaluator.function(c);
				if (fc <= fr) {
					System.arraycopy(c, 0, simplex[ihi], 0, nParameters);
					loss[ihi] = fc;
					continue;
				} else {
					for (int i = 0; i < nVertices; i++) {
						if (i != ilo) {
							for (int j = 0; j < nParameters; j++)
								simplex[i][j] = 0.5 * simplex[ilo][j] + 0.5 * simplex[i][j];
							loss[i] = evaluator.function(simplex[i]);
						}
					}
					continue;
				}
			}

			/* contraction of whole */
			if (fr >= fhi) {
				double[] c = new double[nParameters];
				for (int j = 0; j < nParameters; j++)
					c[j] = 0.5 * centroid[j] + 0.5 * simplex[ihi][j];
				double fc = evaluator.function(c);
				if (fc < fhi) {
					System.arraycopy(c, 0, simplex[ihi], 0, nParameters);
					loss[ihi] = fc;
					continue;
				} else {
					for (int i = 0; i < nVertices; i++) {
						if (i != ilo) {
							for (int j = 0; j < nParameters; j++)
								simplex[i][j] = 0.5 * simplex[ilo][j] + 0.5 * simplex[i][j];
							loss[i] = evaluator.function(simplex[i]);
						}
					}
				}
			}
		}
		System.arraycopy(simplex[ilo], 0, parms, 0, nParameters);
		return true;
	}

	private void generateStartingSimplex(double[][] simplex, double[] x, double[] bounds, int nVertices, int nParameters) {
		for (int i = 0; i < nVertices; i++) {
			for (int j = 0; j < nParameters; j++) {
				double d = Math.min(x[j] - bounds[0], bounds[1] - x[j]);
				simplex[i][j] = x[j] + random.nextDouble() * d;
			}
		}
	}

	private void bound(double[][] simplex, double[] bounds) {
		if (bounds == null)
			return;
		for (int i = 0; i < simplex.length; i++) {
			for (int j = 0; j < simplex[0].length; j++) {
				simplex[i][j] = Math.max(Math.min(simplex[i][j], bounds[1]), bounds[0]);
			}
		}
	}

	public boolean anneal(double[] parms, double[] bounds) {
		/* Simulated Annealing Minimization */
		double temperature = 1;
		double currentFit = Double.POSITIVE_INFINITY;
		double previousFit;
		int np = parms.length;

		for (int iterations = 0; iterations < MAX_ITERATIONS; iterations++) {
			int numMoves = 0;
			for (int attemptMove = 0; attemptMove < 10; attemptMove++) {
				int ranloc = random.nextInt(np);
				double preLoss = evaluator.function(parms);
				double oldValue = move(ranloc, parms, bounds);
				double postLoss = evaluator.function(parms);
				double de = (postLoss - preLoss) / preLoss;
				if (acceptMove(de, temperature))
					numMoves++;
				else
					parms[ranloc] = oldValue;
			}
			previousFit = currentFit;
			currentFit = evaluator.function(parms);
			if (numMoves == 0)
				break;
			temperature *= .9;
			//			System.out.println(iterations + " fit: " + currentFit + " coef: " + Arrays.toString(parms));
		}
		return true;
	}

	private double move(int ranloc, double[] x, double[] bounds) {
		double oldValue = x[ranloc];
		x[ranloc] += (random.nextDouble() - .5) / 10;
		if (bounds != null)
			x[ranloc] = Math.max(Math.min(x[ranloc], bounds[1]), bounds[0]);
		return oldValue;
	}

	private boolean acceptMove(double de, double t) {
		if (de < 0) {
			return true; // improvement
		} else if (de == 0) {
			return false; // pointless move
		} else
			return Globals.random.nextDouble() < Math.exp(-de / t); // give it a try
	}
}
