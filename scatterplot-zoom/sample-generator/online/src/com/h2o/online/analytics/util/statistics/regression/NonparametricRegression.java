/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics.regression;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Sorts;
import com.h2o.online.analytics.util.statistics.Statistics;

public class NonparametricRegression extends Regression {
	/*
	 * polySmooth: global polynomial
	 * kernelSmooth: local kernel smoother (includes loess and moving average)
	 * modalSmooth: Scott's conditional mode smoother
	 */
	@Override
	public void compute(double[][] xData, double[] yData, double[] weights, int model) {
	}

	@Override
	public void compute(double[][] data, double[] weight, int iRow1, int iRow2, int degree, int nCols, int model) {
	}

	public static double[][] polySmooth(double[][] data, double[] weights, double[] dataMin, double[] dataMax,
			double[] scaleMin, double[] scaleMax, int numKnots, int smoother, int robust, int degree,
			boolean isBounded, boolean computeResiduals, boolean trim) {
		double xmin = 0., xmax = 0., xdelta = 0., xrange = 0., xval = 0., ymin = 0., ymax = 0., ydelta = 0., yrange = 0., yval = 0.;

		// Compute regions over (x, y) or (x, y, z)

		double[][] pts = sortAndTranspose(data, weights);

		boolean isGridded = numKnots > 0;
		int nRows = pts[0].length;
		int nCols = pts.length;
		int depVar = nCols - 1;
		int np = 0;
		int nx = 0;
		if (isGridded) {
			np = numKnots;
			nx = np;
			if (isBounded) {
				xmin = dataMin[0];
				xmax = dataMax[0];
			} else {
				xmin = scaleMin[0];
				xmax = scaleMax[0];
			}
			xrange = xmax - xmin;
			xdelta = xrange / numKnots;
			xval = xmin - xdelta;
		} else
			np = nRows;

		if (nCols == 3) {
			if (isGridded) {
				np = numKnots * numKnots;
				if (isBounded) {
					ymin = dataMin[1];
					ymax = dataMax[1];
				} else {
					ymin = scaleMin[1];
					ymax = scaleMax[1];
				}
				yrange = ymax - ymin;
				ydelta = yrange / numKnots;
				yval = ymin - ydelta;
			}
		}

		double[][] smooth = new double[np][3];
		Regression reg;
		if (robust == 0)
			reg = new PolynomialRegression();
		else
			reg = new RobustRegression();

		int iRow1 = 0;
		int iRow2 = 0;
		if (trim) { // 50% trimm
			iRow1 = nRows / 4;
			iRow2 = (3 * nRows) / 4;
		}

		if (smoother == PolynomialRegression.LOG || smoother == PolynomialRegression.POWER
				|| smoother == PolynomialRegression.POLYNOMIAL)
			reg.compute(pts, weights, iRow1, iRow2, degree, depVar, smoother);

		for (int i = 0; i < np; i++) {
			if (isGridded) {
				xval += xdelta;
				if (i % nx == 0) {
					xval = xmin;
					yval += ydelta;
				}
			} else {
				xval = pts[0][i];
				yval = pts[1][i];
			}

			double sval = reg.prediction(xval, yval, degree, depVar, smoother);

			if (computeResiduals) {
				smooth[i][0] = sval;
				smooth[i][1] = pts[depVar][i] - sval;
				smooth[i][2] = pts[depVar][i];
			} else {
				smooth[i][0] = xval;
				smooth[i][1] = sval;
				smooth[i][2] = yval;
			}
		}
		return smooth;
	}

	public static double[][] kernelSmooth(double[][] data, double[] weights, double[] dataMin, double[] dataMax,
			double[] scaleMin, double[] scaleMax, int numKnots, int smoother, int degree, int kernel, int neighbors,
			int window, double proportion, double bandwidth, boolean isBounded, boolean isMarronCorrection,
			boolean computeResiduals) {
		/*
		 * prp: proportion of range or neighbors (let program set to .5)
		 * bwd: preselected bandwidth
		 * mar: Marron/Nolan canonical correction (default = 0)
		 * smoother: Mean smoothing
		 * kernel: Kernel type: use Tricube kernel (for LOESS)
		 * window: Window type: use KNN window (for LOESS)
		 * idg: degree of polynomial: set to 1 for LOESS
		 * knn: Number of nearest neighbors: let program decide
		 * igr: Number of gridpoints: set to 50 for now
		 * bw: bandwidth used in program
		 * nw: number of points within bandwidth
		 */

		double[][] pts = sortAndTranspose(data, weights);

		boolean isGridded = numKnots > 0;
		int nRows = pts[0].length;
		int nCols = pts.length;
		int depVar = nCols - 1;
		double[] residuals = null, rweights = null;
		int[] rows = new int[2];
		double xmin, xmax, xmean, xsd;
		double ymin, ymax, ymean, ysd;
		double zmean = 0, zsd = 0;
		double rmin, rmax, range;
		double xmn = 0., xmx = 0., xdl = 0., xvl = 0., ymn = 0., ymx = 0., ydl = 0., yvl = 0., zvl = 0.;

		double[] u = new double[nRows];
		double[] v = new double[nRows];
		double[] w = new double[nRows];

		if (smoother == LOESS) {
			residuals = new double[nRows];
			rweights = new double[nRows];
		}

		// Compute basic statistics
		double[] stats = Statistics.compute(pts[0], weights);
		xmean = stats[Statistics.MEAN];
		xsd = stats[Statistics.SD];
		Statistics.standardize(pts[0], xmean, xsd, 0, nRows);

		stats = Statistics.compute(pts[1], weights);
		ymean = stats[Statistics.MEAN];
		ysd = stats[Statistics.SD];
		Statistics.standardize(pts[1], ymean, ysd, 0, nRows);

		if (nCols == 3) {
			stats = Statistics.compute(pts[depVar], weights);
			zmean = stats[Statistics.MEAN];
			zsd = stats[Statistics.SD];
			Statistics.standardize(pts[2], zmean, zsd, 0, nRows);
		}

		if (isBounded) {
			xmin = dataMin[0];
			xmax = dataMax[0];
			ymin = dataMin[1];
			ymax = dataMax[1];
		} else {
			xmin = scaleMin[0];
			xmax = scaleMax[0];
			ymin = scaleMin[1];
			ymax = scaleMax[1];
		}

		rmin = (xmin - xmean) / xsd;
		rmax = (xmax - xmean) / xsd;
		range = rmax - rmin;

		// Compute bandwidth
		double bw = 0.;
		int nw = 0;
		int nx = 0;
		if (window == FIXED) {
			bw = computeBandwidth(proportion, bandwidth, isMarronCorrection, kernel, range, nRows, 3);
			if (bandwidth > 0.0)
				bw = bw / xsd;
		} else if (window == KNN) {
			nw = countNeighbors(neighbors, proportion, nRows);
		}

		// Smooth

		int np;
		if (isGridded) {
			np = numKnots;
			nx = np;
			xmn = (xmin - xmean) / xsd;
			xmx = (xmax - xmean) / xsd;
			xdl = (xmx - xmn) / np;
			xvl = xmn - xdl;
		} else {
			np = nRows;
		}

		if (nCols == 3) {
			if (isGridded) {
				np = numKnots * numKnots;
				ymn = (ymin - ymean) / ysd;
				ymx = (ymax - ymean) / ysd;
				ydl = (ymx - ymn) / numKnots;
				yvl = ymn - ydl;
			}
		}

		double[][] smooth = new double[np][3];

		for (int i = 0; i < np; i++) {
			if (isGridded) {
				xvl += xdl;
				if (i % nx == 0) {
					xvl = xmn;
					yvl += ydl;
				}
			} else {
				xvl = pts[0][i];
				yvl = pts[1][i];
			}
			smooth[i][0] = xmean + xsd * xvl;

			if (nCols == 2) {
				yvl = smooth2D(pts, weights, xvl, u, v, w, residuals, rweights, nRows, nw, rows, bw, rmin, rmax,
						kernel, window, smoother, degree);
				smooth[i][1] = ymean + ysd * yvl;
				if (computeResiduals) {
					double observed = ymean + ysd * pts[depVar][i];
					smooth[i][0] = smooth[i][1];
					smooth[i][1] = observed - smooth[i][1];
					smooth[i][2] = observed;
				}
			} else if (nCols == 3) {
				zvl = smooth3D(pts, weights, xvl, yvl, u, v, w, residuals, rweights, nRows, nw, rows, bw, kernel,
						window, smoother, degree);
				smooth[i][1] = zmean + zsd * zvl;
				smooth[i][2] = ymean + ysd * yvl;
				if (computeResiduals) {
					double observed = zmean + zsd * pts[depVar][i];
					smooth[i][0] = smooth[i][1];
					smooth[i][1] = observed - smooth[i][1];
					smooth[i][2] = observed;
				}
			}
		}
		return smooth;
	}

	public static double[][] modalSmooth(double[][] data, double[] weights, int numKnots, double tns) {
		/* modal smooth */
		double[][] pts = sortAndTranspose(data, weights);
		int np = numKnots;
		int nc = 50;

		// Compute basic statistics
		double[] stats = Statistics.compute(pts[0], weights);
		double xmean = stats[Statistics.MEAN];
		double xsd = stats[Statistics.SD];
		double xmin = stats[Statistics.MIN];
		double xrange = stats[Statistics.RANGE];

		stats = Statistics.compute(pts[1], weights);
		double ymean = stats[Statistics.MEAN];
		double ysd = stats[Statistics.SD];
		double ymin = stats[Statistics.MIN];
		double yrange = stats[Statistics.RANGE];

		double[][] smooth = new double[np][3];
		double[][] grd = new double[nc][nc];
		double[] ym1 = new double[np];
		double[] ym2 = new double[np];
		int cut = nc - 1;
		double deltay = yrange / cut;
		double deltax = xrange / cut;
		double yp = ymin;
		for (int i = 0; i < nc; i++) {
			double xp = xmin;
			for (int j = 0; j < nc; j++) {
				double zp = kernel(pts[0], pts[1], weights, xmean, xsd, ymean, ysd, xp, yp, tns);
				grd[i][j] = zp;
				xp += deltax;
			}
			yp += deltay;
		}
		/* compute fitted values over np points */
		double xd = 0;
		deltax = 1.0 / (np - 1);
		deltay = 1.0 / cut;
		/* search for mode by quadratically interpolating triples */
		for (int j = 0; j < np; j++) {
			double zmd1 = 0;
			double zmd2 = 0;
			ym1[j] = Globals.MISSING_VALUE;
			ym2[j] = Globals.MISSING_VALUE;
			double yd = deltay;
			int ncm1 = nc - 1;
			for (int i = 1; i < ncm1; i++) {
				double y1 = yd - deltay;
				double y2 = yd;
				double y3 = yd + deltay;
				yd = y3;
				double z1 = interpolate(xd, y1, grd, cut, nc);
				double z2 = interpolate(xd, y2, grd, cut, nc);
				double z3 = interpolate(xd, y3, grd, cut, nc);
				if (z2 > z1 && z2 > z3) {
					double y12 = y1 - y2;
					double y13 = y1 - y3;
					double y122 = y1 * y1 - y2 * y2;
					double a = (z1 - z2) * y13 / y12 - (z1 - z3);
					a = a / (y122 * y13 / y12 - (y1 * y1 - y3 * y3));
					double b = (z1 - z2 - a * y122) / y12;
					double c = z1 - a * y1 * y1 - b * y1;
					if (a != 0) {
						double yh = -b / (2 * a);
						double zhat = a * yh * yh + b * yh + c;
						if (zhat > .05 && zhat > zmd1) {
							if (zmd1 > zmd2) {
								zmd2 = zmd1;
								ym2[j] = ym1[j];
							}
							zmd1 = zhat;
							ym1[j] = yh;
						}
					}
				}
			}
			if (Globals.isMissing(ym2[j]))
				ym2[j] = ym1[j];
			if (j > 0) {
				if (Math.abs(ym1[j - 1] - ym1[j]) > Math.abs(ym1[j - 1] - ym2[j])) {
					double temp = ym1[j];
					ym1[j] = ym2[j];
					ym2[j] = temp;
				}
			}
			xd += deltax;
		}
		double xx = xmin;
		deltax = xrange / (np - 1);
		for (int i = 0; i < np; i++) {
			smooth[i][0] = xx;
			smooth[i][1] = ymin + ym1[i] * yrange;
			smooth[i][2] = ymin + ym2[i] * yrange;
			xx += deltax;
		}

		return smooth;
	}

	private static double kernel(double[] x, double[] y, double[] weights, double xmean, double xsd, double ymean,
			double ysd, double dx, double dy, double tns) {
		int n = x.length;
		double zn = n;
		double tens = 2.04 * Math.pow(zn, -1.0 / 6.0);
		double t = 2 * tns * tens;
		double t2 = t * t;
		double z2 = .9549297;
		double zx = (dx - xmean) / xsd;
		double zy = (dy - ymean) / ysd;
		double zk = 0;
		for (int i = 0; i < n; i++) {
			double wt = 1;
			if (weights != null)
				wt = weights[i];
			if (wt > 0) {
				double xi = (x[i] - xmean) / xsd;
				double yi = (y[i] - ymean) / ysd;
				double zp = ((zx - xi) * (zx - xi) + (zy - yi) * (zy - yi)) / t2;
				if (zp < 1)
					zk += (1 - zp) * (1 - zp);
			}
		}
		return (zk * z2) / (zn * t2);
	}

	private static double interpolate(double x, double y, double[][] grd, double cut, int n) {
		/* interpolation over grid */
		double zx = Math.max(Math.min(x, 1), 0) * cut * .9999999;
		double zy = Math.max(Math.min(y, 1), 0) * cut * .9999999;
		int i = (int) zy;
		int j = (int) zx;
		int ip1 = Math.min(i + 1, n);
		int jp1 = Math.min(j + 1, n);
		double zi = i;
		double zj = j;
		/* interpolate horizontally */
		double wt1 = zx - zj;
		double wt2 = 1 - wt1;
		double z1 = wt2 * grd[i][j] + wt1 * grd[i][jp1];
		double z2 = wt2 * grd[ip1][j] + wt1 * grd[ip1][jp1];
		/* interpolate vertically */
		wt1 = zy - zi;
		wt2 = 1 - wt1;
		return wt1 * z1 + wt1 * z2;
	}

	private static double smooth2D(double[][] pts, double[] weights, double sx, double[] u, double[] v, double[] w,
			double[] residuals, double[] rweights, int nc, int nw, int[] rows, double bw, double rmin, double rmax,
			int kernel, int window, int smoother, int degree) {

		int iRow1, iRow2;
		int[] bounds = new int[2];

		/*
		 * 2D WINDOW SMOOTHING AT POINT SX
		 * assumes x and y are sorted ascending on values of x
		 * x: x
		 * y: y
		 * sx: value for which smoothed value is to be computed
		 * u,v: work arrays
		 * w: kernel weights for polynomial smoothers
		 * weight: array of case weights
		 * residuals,rweights: loess work arrays
		 * nc: length of x,y (all values assumed nonmissing)
		 * nw: number of elements in current window
		 * bw: bandwidth (half width of window)
		 * rmin: minimum value in x
		 * rmax: maximum value in x
		 * kernel: index of kernel function
		 * 1=1 uniform
		 * 2=(1-x^2) epanechnikov
		 * 3=(1-x^2)^2 biweight
		 * 4=(1-x^2)^3 triweight
		 * 5=(1-abs(x)^3)^3 tricube
		 * 6=exp(-x^2) gaussian
		 * 7=1/(1+x^2) cauchy
		 * window: window type
		 * 1=fixed
		 * 2=nearest neighbor
		 * smoother: smoothing estimate in window
		 * 1=mean
		 * 2=50 percent trimmed mean
		 * 3=median
		 * 4=polynomial fit
		 * 5=loess fit
		 * idg: degree of polynomial (1, 2, or 3)
		 */

		// FIND ALL POINTS IN WINDOW AND COMPUTE BW FOR LOCAL WINDOW METHODS

		int depVar = 1;
		int nX = 1;
		nw = nearNeighbors1D(pts, u, sx, rmin, rmax, window, bounds, bw, nc, nw);
		double sf = Globals.MISSING_VALUE;
		if (nw < 1)
			return sf;

		int ix1 = bounds[0];
		int ix2 = bounds[1];
		iRow1 = ix1;
		iRow2 = ix2 + 1;
		int n1, n2;
		double wt = 1;
		double sumwt = 0;
		// COMPUTE BANDWIDTH FOR KNN BASED ON FARTHEST POINT FOUND IN win2()
		if (window == KNN)
			bw = Math.max(sx - pts[0][ix1], pts[0][ix2] - sx);

		// COMPUTE WEIGHTED MEAN OF Y VALUES INSIDE SMOOTHING WINDOW
		if (smoother == MEAN) {
			sf = 0.;
			sumwt = 0.;
			for (int i = iRow1; i < iRow2; i++) {
				wt = weights[i] * wtFunc1D(pts[0][i], sx, bw, kernel);
				sf = sf + pts[1][i] * wt;
				sumwt = sumwt + wt;
			}
			if (sumwt > 0.0)
				sf = sf / sumwt;
			else
				sf = Globals.MISSING_VALUE;
		}

		// COMPUTE 50 PERCENT TRIMMED WEIGHTED MEAN OR MEDIAN OF Y VALUES
		else if (smoother == TRIM || smoother == MEDIAN) {
			nw = 0;
			for (int i = iRow1; i < iRow2; i++) {
				u[nw] = pts[0][i];
				v[nw] = pts[1][i];
				nw++;
			}

			// SORT ON Y VALUES
			sort0(v, u, nw);

			// TRIMMED MEAN
			if (smoother == TRIM) {
				n2 = (3 * nw) / 4;
				n1 = nw - n2 + 1;
				sf = 0.;
				sumwt = 0.;
				for (int i = n1 - 1; i < n2; i++) {
					wt = weights[i] * wtFunc1D(u[i], sx, bw, kernel);
					sf = sf + v[i] * wt;
					sumwt = sumwt + wt;
				}
				if (sumwt > 0.)
					sf = sf / sumwt;
				else
					sf = Globals.MISSING_VALUE;
			}
			// MEDIAN
			if (smoother == MEDIAN) {
				n1 = (nw + 1) / 2;
				n2 = nw - n1 + 1;
				sf = (v[n1 - 1] + v[n2 - 1]) / 2.0;
			}
		}

		// COMPUTE LOCAL POLYNOMIAL SMOOTHER
		else {
			PolynomialRegression poly = new PolynomialRegression();
			for (int i = iRow1; i < iRow2; i++)
				w[i] = weights[i] * wtFunc1D(pts[0][i], sx, bw, kernel);
			// WEIGHTED LINEAR REGRESSION
			if (smoother == PolynomialRegression.POLYNOMIAL) {
				poly.compute(pts, w, iRow1, iRow2, degree, 2, smoother);
				sf = poly.prediction(sx, sx, degree, nX, smoother);
			}
			// ROBUST LINEAR REGRESSION (LOESS)
			else if (smoother == LOESS)
				sf = loess(pts, w, residuals, rweights, sx, sx, iRow1, iRow2, 1, degree);
		}

		rows[0] = iRow1;
		rows[1] = iRow2;

		return sf;
	}

	private static double smooth3D(double[][] pts, double[] weights, double sx, double sy, double[] u, double[] v,
			double[] w, double[] residuals, double[] rweights, int nc, int nw, int[] rows, double bw, int kernel,
			int window, int smoother, int degree) {

		/*
		 * 3D WINDOW SMOOTHING WITH RADIAL KERNEL AT POINT (SX,SY)
		 * assumes x, y, and z are standardized
		 * x: x
		 * y: y
		 * z: z
		 * sx: value on x for which smoothed value is to be computed
		 * sy: value on y for which smoothed value is to be computed
		 * u,v: work arrays
		 * w: kernel weights
		 * weight: array of case weights
		 * residuals,rweights: loess work arrays
		 * nc: length of x,y,z (all values assumed nonmissing)
		 * nw: number of elements in current window
		 * bw: bandwidth (radius of window)
		 * kernel: index of kernel function
		 * 1=1 uniform
		 * 2=(1-x^2) epanechnikov
		 * 3=(1-x^2)^2 biweight
		 * 4=(1-x^2)^3 triweight
		 * 5=(1-abs(x)^3)^3 tricube
		 * 6=exp(-x^2) gaussian
		 * 7=1/(1+x^2) cauchy
		 * window: window type
		 * 1=fixed
		 * 2=nearest neighbor
		 * smoother: smoothing estimate in window
		 * 1=mean
		 * 2=50 percent trimmed mean
		 * 3=median
		 * 4=polynomial fit
		 * 5=loess fit
		 * idg: degree of polynomial (1, 2, or 3)
		 */

		// ORDER ARRAYS BY DISTANCE FROM (SX,SY)
		int nX = pts.length;
		int depVar = nX - 1;
		nw = nearNeighbors2D(pts, u, sx, sy, window, bw, nc, nw);
		double sf = Globals.MISSING_VALUE;

		if (nw < 1)
			return sf;
		int iRow1 = 0;
		int iRow2 = nw;

		// COMPUTE BANDWIDTH FOR K-NEAREST NEIGHBORS WINDOW AND STANDARDIZE
		// VALUES
		if (window == KNN) {
			bw = u[nw - 1];
			for (int i = 0; i < nw; i++)
				u[i] = u[i] / bw;
		}

		// COMPUTE WEIGHTED MEAN OF Z VALUES INSIDE SMOOTHING WINDOW
		if (smoother == MEAN) {
			sf = 0.;
			double sumwt = 0.;
			for (int i = 0; i < nw; i++) {
				double wt = weights[i] * wtFunc2D(u[i], kernel);
				sf = sf + pts[2][i] * wt;
				sumwt = sumwt + wt;
			}
			if (sumwt > 0.0)
				sf = sf / sumwt;
			else
				sf = Globals.MISSING_VALUE;
		}
		// COMPUTE 50 PERCENT TRIMMED WEIGHTED MEAN OR MEDIAN OF Z VALUES
		else if (smoother == TRIM || smoother == MEDIAN) {
			System.arraycopy(pts[2], 0, v, 0, nw);

			sort0(v, u, nw);

			int n1, n2;
			if (smoother == TRIM) {
				n2 = (3 * nw) / 4;
				n1 = nw - n2 + 1;
				sf = 0.;
				double sumwt = 0.;
				for (int i = n1 - 1; i < n2; i++) {
					double wt = weights[i] * wtFunc2D(u[i], kernel);
					sf = sf + v[i] * wt;
					sumwt = sumwt + wt;
				}
				if (sumwt > 0.)
					sf = sf / sumwt;
				else
					sf = Globals.MISSING_VALUE;
			}
			if (smoother == MEDIAN) {
				n1 = (nw + 1) / 2;
				n2 = nw - n1 + 1;
				sf = (v[n1 - 1] + v[n2 - 1]) / 2.0;
			}
		}
		// COMPUTE LOCAL LINEAR SMOOTHER
		else {
			PolynomialRegression poly = new PolynomialRegression();
			for (int i = 0; i < nw; i++)
				w[i] = weights[i] * wtFunc2D(u[i], kernel);
			// WEIGHTED LINEAR REGRESSION
			if (smoother == PolynomialRegression.POLYNOMIAL) {
				poly.compute(pts, w, iRow1, iRow2, degree, nX, smoother);
				sf = poly.prediction(sx, sy, degree, nX, smoother);
			}
			// ROBUST LINEAR REGRESSION (LOESS)
			else if (smoother == LOESS)
				sf = loess(pts, w, residuals, rweights, sx, sy, iRow1, iRow2, depVar, degree);
		}

		rows[0] = iRow1;
		rows[1] = iRow2;

		return sf;
	}

	private static int countNeighbors(int neighbors, double proportion, int nz) {

		// COMPUTE NUMBER OF DATA POINTS FOR NEAREST NEIGHBOR WINDOW

		int nw = 0;
		if (neighbors <= 0) {
			if (proportion <= 0.)
				nw = nz / 2;
			else {
				if (proportion < 1.)
					nw = (int) (proportion * nz);
				else
					nw = nz;
			}
		} else
			nw = neighbors;

		if (nw < 2)
			nw = 2;
		if (nw > nz)
			nw = nz;

		return nw;
	}

	private static double computeBandwidth(double proportion, double bandwidth, boolean marron, int kernel,
			double range, int nz, int k) {

		/*
		 * COMPUTE SMOOTHING BANDWIDTH FROM INPUT PARAMETERS
		 * presumes data have been standardized (sd=1)
		 * prp: proportion factor
		 * bwd: input kernel bandwidth parameter
		 * mar: Marron and Nolan canonical bandwidth adjustment factor delta
		 * 0=do not use
		 * 1=use
		 * Marron, J.S., and Nolan, D. (1989). Canonical kernels for density
		 * estimation. Statistics & Probability Letters, 7, 195-199.
		 * (The values for TRICUBE and CAUCHY are approximate, not in article.)
		 * 
		 * kernel: kernel function
		 * 1=1 uniform
		 * 2=(1-x^2) epanechnikov
		 * 3=(1-x^2)^2 biweight
		 * 4=(1-x^2)^3 triweight
		 * 5=(1-abs(x)^3)^3 tricube
		 * 6=exp(-x^2) gaussian
		 * 7=1/(1+x^2) cauchy
		 * range: range of raw data
		 * n: sample size
		 * k: type of plug-in computation
		 * 1=1d density
		 * 2=2d density
		 * 3=single predictor kernel smooth
		 * 4=two predictor kernel smooth
		 * output: bandwidth (computed kernel bandwidth)
		 */

		double[] dmar = { 1.351, 1.7188, 2.0362, 2.3122, 2.0000, 0.7764, .5 };
		double bw = Globals.MISSING_VALUE;
		if (bandwidth > 0.0)
			bw = bandwidth;
		else if (proportion <= 0.0) {
			if (k == 1)
				bw = Math.pow(nz, -.2);
			if (k == 2)
				bw = 2.0 * Math.pow(nz, -.16666667);
			if (k == 3)
				bw = 2.0 * Math.pow(nz, -.2);
			if (k == 4)
				bw = 5.0 * Math.pow(nz, -.16666667);
		} else
			bw = proportion * range;

		// Marron & Nolan adjustment
		if (marron)
			bw = bw * dmar[kernel - 1];
		// CLAMP BECAUSE THE DATA ARE STANDARDIZED
		if (bw < 1.0e-3)
			bw = 1.0e-3;

		return bw;
	}

	private static int nearNeighbors1D(double[][] pts, double[] u, double sx, double rmin, double rmax, int window,
			int[] bounds, double bw, int nc, int nw) {

		/*
		 * FIND DATA POINTS WITHIN 1D WINDOW BOUNDING SX
		 * 
		 * compute nw (number of points in window) if fixed width window
		 * 
		 * input:
		 * x: array of sorted values
		 * sx: value
		 * u: work array
		 * v: work array
		 * rmin: minimum value in x
		 * rmax: maximum value in x
		 * window: window type
		 * 1=fixed
		 * 2=nearest neighbor
		 * bw: bandwidth
		 * nc: number of data points in x (all nonmissing)
		 * nw: number of data points in window (reset for fixed window)
		 * output:
		 * ix1: index of smallest point in window (0<=ix1<=ix2)
		 * ix2: index of largest point in window (ix1<=ix2<n)
		 * nw: number of data points in window, counted for fixed window, unchanged for k-nn
		 * 
		 * if window is wider than half the range, search begins from extremes
		 * else search begins from middle
		 */

		int ix1 = 0;
		int ix2 = 0;
		double x1 = 0;
		double x2 = 0;
		double xi = 0;
		// FIXED WINDOW: FIND POINTS INSIDE BOUNDS
		if (window == FIXED) {
			x1 = sx - bw;
			x2 = sx + bw;
			if (bw < (rmax - rmin) / 2.0) {
				// EXPAND SEARCH FROM CENTER
				// FIND CLOSEST DATA POINT TO SX
				int ix = nearestNeighbor(pts, sx, rmin, rmax, nc);
				bounds[0] = ix;
				bounds[1] = ix;
				nw = 0;
				if (Math.abs(pts[0][ix] - sx) > bw)
					return nw;
				// WIDEN RANGE TO INCLUDE ALL POINTS IN BOUNDS OF WINDOW (use
				// one-line looping to compute ix1, ix2)

				for (ix1 = ix; ix1 > 0 && pts[0][ix1] >= x1; ix1--)
					;
				if (pts[0][ix1] < x1)
					ix1++;

				for (ix2 = ix; ix2 < nc - 1 && pts[0][ix2] <= x2; ix2++)
					;
				if (pts[0][ix2] > x2)
					ix2--;

				nw = ix2 - ix1 + 1;
			}
			// CONTRACT FROM ENDS
			else {
				for (ix1 = 0; ix1 < nc - 1 && pts[0][ix1] < x1; ix1++)
					;
				for (ix2 = nc - 1; ix2 > 0 && pts[0][ix2] > x2; ix2--)
					;

				nw = ix2 - ix1 + 1;
			}
		}

		// K-NN METHOD: FIND NEAREST NW POINTS
		else if (window == KNN) {
			// perturb points slightly
			for (int i = 0; i < nc; i++) {
				xi = pts[0][i] + Globals.FUZZ * Globals.random.nextDouble();
				u[i] = Math.abs(xi - sx);
			}
			int[] k = Sorts.indexedDoubleArraySort(u, 0, nc);
			ix1 = nc;
			ix2 = -1;

			for (int i = 0; i < nw; i++) {
				if (ix1 > k[i])
					ix1 = k[i];
				if (ix2 < k[i])
					ix2 = k[i];
			}
		}

		bounds[0] = ix1;
		bounds[1] = ix2;
		return nw;
	}

	private static int nearNeighbors2D(double[][] pts, double[] u, double sx, double sy, int window, double bw, int nc,
			int nw) {

		/*
		 * FIND DATA POINTS WITHIN 2D WINDOW BOUNDING SX
		 * 
		 * order x,y,z arrays by distance from (sx,sy)
		 * compute nw (number of points in window) if fixed width window
		 * compute bw (bandwidth) if k-nearest neighbors window
		 * 
		 * input:
		 * x,y,z: arrays of values
		 * u: work array to hold sorted distances
		 * sx,sy: prediction point coordinate (center of kernel)
		 * window: window type
		 * 1=fixed
		 * 2=nearest neighbor
		 * bw: bandwidth (reset for k-nn window)
		 * nc: number of data points in x
		 * nw: number of data points in window (reset for fixed window)
		 * output:
		 * x,y,z: input arrays sorted by distance
		 * u: sorted array of distances
		 * bw: bandwidth
		 * nw: number of data points in window, counted for fixed window, unchanged for k-nn
		 */

		if (window == KNN)
			bw = 1.0;
		for (int i = 0; i < nc; i++) {
			double d1 = (pts[0][i] - sx) / bw;
			double d2 = (pts[1][i] - sy) / bw;
			u[i] = Math.sqrt(d1 * d1 + d2 * d2);
		}

		sort0(u, pts, nc);

		// COMPUTE NW FOR FIXED WINDOW
		if (window == FIXED)
			for (nw = 0; nw < nc && u[nw] < bw; nw++)
				;

		return nw;
	}

	private static int nearestNeighbor(double[][] pts, double sx, double rmin, double rmax, int n) {

		// FIND CLOSEST DATA POINT TO SX

		int ix = (int) ((n - 1) * (sx - rmin) / (rmax - rmin));
		if (pts[0][ix] == sx)
			return ix;

		if (pts[0][ix] < sx) {
			for (; ix < n - 1 && pts[0][ix] < sx; ix++)
				;
			if (sx - pts[0][ix - 1] < pts[0][ix] - sx)
				ix--;
		} else {
			for (; ix > 0 && pts[0][ix] > sx; ix--)
				;
			if (sx - pts[0][ix] > pts[0][ix + 1] - sx)
				ix++;
		}

		return ix;
	}

	private static double wtFunc1D(double x, double a, double h, int kernel) {

		/*
		 * SMOOTHING KERNEL FUNCTION
		 * 
		 * kernel: index of kernel function
		 * 1=1 uniform
		 * 2=(1-x^2) epanechnikov
		 * 3=(1-x^2)^2 biweight
		 * 4=(1-x^2)^3 triweight
		 * 5=(1-abs(x)^3)^3 tricube
		 * 6=exp(-x^2) gaussian
		 * 7=1/(1+x^2) cauchy
		 * 
		 * Scaling factors not included because they are not needed for regression smoothing
		 * If this routine is used for density estimation, scale these
		 * weights by multiplying them by the following constants
		 * 1: .5
		 * 2: .75
		 * 3: .9375
		 * 4: 1.09375
		 * 5: .864197531
		 * 6: .3989423
		 * 7: 1.0
		 */

		double wt = 0.0;
		if (h <= 0.0)
			return wt;

		double d = (x - a) / h;
		if (kernel != GAUSSIAN && kernel != CAUCHY && Math.abs(d) > 1.0)
			return wt;
		else {
			if (kernel == UNIFORM)
				wt = 1.0;
			else if (kernel == EPANECHNIKOV)
				wt = 1.0 - d * d;
			else if (kernel == BIWEIGHT)
				wt = Math.pow(1.0 - Math.pow(d, 2.0), 2.0);
			else if (kernel == TRIWEIGHT)
				wt = Math.pow(1.0 - Math.pow(d, 2.0), 3.0);
			else if (kernel == TRICUBE)
				wt = Math.pow(1.0 - Math.pow(Math.abs(d), 3.0), 3.0);
			else if (kernel == GAUSSIAN)
				wt = Math.exp(-.5 * Math.pow(d, 2.0));
			else if (kernel == CAUCHY)
				wt = 1.0 / (1.0 + Math.pow(d, 2.0));
		}

		return wt;
	}

	private static double wtFunc2D(double d, int kernel) {

		/*
		 * BIVARIATE SMOOTHING KERNEL FUNCTION
		 * 
		 * kernel: index of kernel function
		 * 1=1 uniform
		 * 2=(1-x^2) epanechnikov
		 * 3=(1-x^2)^2 biweight
		 * 4=(1-x^2)^3 triweight
		 * 5=(1-abs(x)^3)^3 tricube
		 * 6=exp(-x^2) gaussian
		 * 7=1/(1+x^2) cauchy
		 */

		double wt = 0.0;
		if (kernel != GAUSSIAN && kernel != CAUCHY && Math.abs(d) > 1.0)
			return wt;
		else {
			if (kernel == UNIFORM)
				wt = 1.0;
			else if (kernel == EPANECHNIKOV)
				wt = 1.0 - Math.pow(d, 2.0);
			else if (kernel == BIWEIGHT)
				wt = Math.pow(1.0 - Math.pow(d, 2), 2.0);
			else if (kernel == TRIWEIGHT)
				wt = Math.pow(1.0 - Math.pow(d, 2.0), 3.0);
			else if (kernel == TRICUBE)
				wt = Math.pow(1.0 - Math.pow(Math.abs(d), 3.0), 3.0);
			else if (kernel == GAUSSIAN)
				wt = Math.exp(-.5 * Math.pow(d, 2.0));
			else if (kernel == CAUCHY)
				wt = 1.0 / (1.0 + Math.pow(d, 2.0));
		}
		return wt;
	}

	private static double loess(double[][] pts, double[] weights, double[] residuals, double[] rweights, double sx,
			double sy, int iRow1, int iRow2, int depVar, int degree) {

		/*
		 * WEIGHTED LEAST SQUARES ITERATIVE ROBUST FIT AT (SX) OR AT (SX,SY)
		 * 
		 * When used with tricube kernel and k-nn, yields Cleveland and Devlin LOESS
		 * 
		 * Cleveland, W.S. (1979). Robust locally weighted regression and smoothing
		 * scatterplots. Journal of the American Statistical Association, 74, 829-836.
		 * Cleveland, W.S., and Devlin, S. (1988). Locally weighted regression:
		 * An approach to regression analysis by local fitting.
		 * Journal of the American Statistical Association, 83, 596-640.
		 * Fan, J., and Gijbels, I. (1996). Local Polynomial Modelling and its
		 * Applications. London: Chapman & Hall.
		 * 
		 * x,y,z: data points
		 * u: array of distances (for 3d kernel computation)
		 * weights: kernel weights
		 * residuals: work array for residuals
		 * rweights: work array for loess robustness weights
		 * sx,sy: smoothing location
		 * sf: smoothed value
		 * b: estimated coefficients of polynomial function
		 * bw: bandwidth
		 * nw: number of data points in window
		 * nd: number of dimensions (2 or 3)
		 * if nd=3, be sure to alias z with y in call
		 * idg: degree of polynomial (1, 2, or 3)
		 */

		// DO ROBUSTNESS ITERATIONS

		int nX = pts.length - 1;
		int niter = 5;
		double sf = 0;
		double rmed6 = 0;
		PolynomialRegression poly = new PolynomialRegression();
		for (int iter = 1; iter <= niter; iter++) {

			// compute estimate at sx or (sx, sy)
			if (iter == 1)
				poly.compute(pts, weights, iRow1, iRow2, degree, nX, PolynomialRegression.POLYNOMIAL);
			else
				poly.compute(pts, rweights, iRow1, iRow2, degree, nX, PolynomialRegression.POLYNOMIAL);

			// compute robustness weights from residuals to use in next
			// iteration
			if (iter < niter) {
				int nw = 0;
				for (int i = iRow1; i < iRow2; i++) {
					sf = poly.prediction(pts[0][i], pts[depVar][i], degree, nX, PolynomialRegression.POLYNOMIAL);
					rweights[i] = pts[depVar][i] - sf;
					residuals[nw] = Math.abs(rweights[i]);
					nw++;
				}

				Sorts.doubleArraySort(residuals, 0, nw);
				int n1 = (nw + 1) / 2 - 1;
				int n2 = nw - n1;
				rmed6 = 3.0 * (residuals[n1] + residuals[n2]);
				for (int i = iRow1; i < iRow2; i++)
					rweights[i] = weights[i] * wtFunc1D(rweights[i], 0, rmed6, TRICUBE);
			}
		}
		return poly.prediction(sx, sy, degree, nX, PolynomialRegression.POLYNOMIAL);
	}

	private static void sort0(double[] a, double[][] b, int nc) {
		int[] ind = Sorts.indexedDoubleArraySort(a, 0, nc);
		double[] b2 = new double[nc];
		for (int j = 0; j < nc; j++)
			b2[j] = a[ind[j]];
		System.arraycopy(b2, 0, a, 0, nc);

		double[][] pts2 = new double[3][nc];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < nc; j++) {
				b2[j] = a[ind[j]];
				pts2[i][j] = b[i][ind[j]];
			}
			System.arraycopy(pts2[i], 0, b[i], 0, nc);
		}
	}

	private static void sort0(double[] a, double[] b, int nc) {
		int[] ind = Sorts.indexedDoubleArraySort(a, 0, nc);
		double[] a2 = new double[nc];
		double[] b2 = new double[nc];
		for (int j = 0; j < nc; j++) {
			a2[j] = a[ind[j]];
			b2[j] = b[ind[j]];
		}
		System.arraycopy(a2, 0, a, 0, nc);
		System.arraycopy(b2, 0, b, 0, nc);
	}
}
