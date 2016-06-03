/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;

import java.util.Arrays;
import java.util.Random;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Math2;
import com.h2o.online.analytics.util.Sorts;

public class Probabilities {

	private Probabilities() {
	}

	public static final int UNIFORM = 1, NORMAL = 2, T = 3, F = 4, CHISQUARE = 5, GAMMA = 6, BETA = 7, EXPONENTIAL = 8,
			LOGISTIC = 9, STUDENTIZEDRANGE = 10, WEIBULL = 11, BINOMIAL = 12, POISSON = 13;

	public static double random(double p, double q, int itype) {
		/*
		 * Random number generator
		 * Uses an iteration counter to trap probability functions that always return missing
		 * Leland Wilkinson
		 */

		int nn = 0;
		double rand = Globals.MISSING_VALUE;
		do {
			double x = Globals.random.nextDouble();
			/*
			 * in the event that a given distribution always returns missing, give up rather than looping infinitely
			 */
			nn = nn + 1;
			if (nn > 20)
				return rand;
			rand = x;
			if (itype == UNIFORM) {
				rand = x * (q - p) + p;
				return rand;
			}
			rand = cdfinv(rand, p, q, itype);
		} while (Globals.isMissing(rand));
		return rand;
	}

	public static double cdf(double x, double p, double q, int itype, boolean density) {
		/*
		 * Cumulative distribution functions
		 * Adapted from algorithms in Applied Statistics and CACM by Leland Wilkinson
		 */

		double result = Globals.MISSING_VALUE;
		if (Globals.isMissing(x))
			return Globals.MISSING_VALUE;

		int ityp = Math.abs(itype);
		boolean isDice = ityp == BINOMIAL || ityp == POISSON;
		boolean isRepeat;
		double xl = Globals.MISSING_VALUE;
		double yl = xl;
		double xm = x;
		double xx = x;
		double delta = 0.0;
		if (density) {
			delta = Math.max(Math.abs(x) / 1.0e5, Globals.FUZZ);
			if (isDice)
				delta = 1.0;
			if (xm >= delta || !isDice)
				xm = xm - delta;
		}

		do {
			result = 0.0;
			if (itype == UNIFORM) {
				if (q <= p || x < p || x > q)
					return Globals.MISSING_VALUE;
				xx = (x - p) / (q - p);
				if (density) {
					result = 1.0 / (q - p);
				} else {
					result = Math.max(Math.min(1.0, xx), 0.0);
				}
				return result;
			} else if (itype == NORMAL) {
				if (q <= 0.0)
					return Globals.MISSING_VALUE;
				xx = (x - p) / q;
				if (density) {
					result = Math.exp(-xx * xx / 2.0) / (2.5066282746 * q);
				} else {
					result = gauss(xx);
				}
				return result;
			} else if (itype == T) {
				if (p <= 0.0)
					return Globals.MISSING_VALUE;
				if (p > 1.0e7) {
					if (density) {
						result = Math.exp(-xx * xx / 2.0) / (2.5066282746 * q);
					} else {
						result = gauss(xm);
					}
					return result;
				}
				xx = xm * xm;
				if (Math.abs(xx) < p / 1.0e9) {
					xx = p / 1.0e9;
					double xm2 = Math.sqrt(xx);
					double xm1 = -xm2;
					double cdt1 = incompleteBeta(p / (p + xx), p / 2.0, 0.5) / 2.0;
					double cdt2 = 1.0 - cdt1;
					result = cdt1 + (cdt2 - cdt1) * (xm - xm1) / (xm2 - xm1);
				} else {
					result = incompleteBeta(p / (p + xx), p / 2.0, 0.5) / 2.0;
					if (xm > 0.0)
						result = 1.0 - result;
				}
			} else if (itype == F) {
				if (p <= 0.0 || q <= 0.0 || xm < 0.0)
					return Globals.MISSING_VALUE;
				result = incompleteBeta(q / (q + p * xm), q / 2.0, p / 2.0);
			} else if (itype == CHISQUARE) {
				if (p <= 0.0 || xm < 0.0)
					return Globals.MISSING_VALUE;
				result = incompleteGamma(xm / 2.0, p / 2.0);
			} else if (itype == GAMMA) {
				if (p <= 0.0 || xm < 0.0)
					return Globals.MISSING_VALUE;
				result = incompleteGamma(xm, p);
			} else if (itype == BETA) {
				if (p <= 0.0 || q <= 0.0 || xm < 0.0 || xm > 1.0)
					return Globals.MISSING_VALUE;
				result = incompleteBeta(xm, p, q);
			} else if (itype == EXPONENTIAL) {
				if (q <= 0.0 || p > x)
					return Globals.MISSING_VALUE;
				xx = (x - p) / q;
				if (density) {
					result = Math.exp(-xx) / q;
				} else {
					result = 1.0 - Math.exp(-xx);
				}
				return result;
			} else if (itype == LOGISTIC) {
				if (q <= 0.0)
					return Globals.MISSING_VALUE;
				xx = (x - p) / q;
				if (xx < -100) {
					result = 0.0;
				} else if (xx > 100) {
					result = 1.0;
				} else {
					if (density) {
						result = Math.exp(xx) / (Math.pow(1.0 + Math.exp(xx), 2) * q);
					} else {
						result = Math.exp(xx) / (1.0 + Math.exp(xx));
					}
				}
				return result;
			} else if (itype == STUDENTIZEDRANGE) {
				if (p <= 1.0 || q <= 0.0 || xm < 0.0)
					return Globals.MISSING_VALUE;
				result = ptrng(xm, p, q);
			} else if (itype == WEIBULL) {
				if (p < 0.0 || q < 0.0 || xm < 0.0)
					return Globals.MISSING_VALUE;
				result = 1.0 - Math.exp(-p * Math.pow(xm, q));
			} else if (itype == BINOMIAL) {
				double pp = Math.floor(xm * Globals.ONE_PLUS_EPSILON);
				double qq = Math.floor(p * Globals.ONE_PLUS_EPSILON);
				if (qq < 0.0 || pp > qq || q < 0.0 || q > 1.0 || xm < 0.0)
					return Globals.MISSING_VALUE;
				result = incompleteBeta(q, pp + 1.0, qq - pp);
			} else if (itype == POISSON) {
				if (xm < 0.0)
					return Globals.MISSING_VALUE;
				xx = Math.floor(xm * Globals.ONE_PLUS_EPSILON) + 1.0;
				result = incompleteGamma(p, xx);
			}

			if (Globals.isMissing(result))
				return Globals.MISSING_VALUE;
			if (result < 0 && itype == POISSON)
				result = 0;
			if (ityp == F || isDice)
				result = 1.0 - result;
			if (result > Globals.ONE_MINUS_EPSILON)
				result = Globals.ONE_MINUS_EPSILON;
			if (result < Globals.EPSILON)
				result = Globals.EPSILON;

			/* perturb and calculate again if doing density */
			isRepeat = false;
			if (density) {
				if (Globals.isMissing(xl)) {
					isRepeat = true;
					if (isDice && x < 1.0)
						return result;
					xl = xm;
					yl = result;
					if (isDice) {
						xm = xm + delta;
					} else {
						xm = x + delta;
						if (ityp == T) {
							if (xl < 1.0 && xm > 1.0)
								xm = (xl + 1.0) / 2.0;
							if (xl < -1.0 && xm > -1.0)
								xm = (xl - 1.0) / 2.0;
						}
					}
				} else {
					result = (result - yl) / (xm - xl);
				}
			}
		} while (isRepeat);

		return result;
	}

	public static double cdfinv(double alpha, double p, double q, int itype) {
		/*
		 * Inverse cumulative distribution functions
		 * Adapted from algorithms in Applied Statistics and CACM by Leland Wilkinson
		 */

		if (Globals.isMissing(alpha) || Globals.isMissing(p) || Globals.isMissing(q))
			return Globals.MISSING_VALUE;
		if (itype == UNIFORM && p >= q)
			return Globals.MISSING_VALUE;
		if (itype == NORMAL && q <= 0.0)
			return Globals.MISSING_VALUE;
		if (itype >= T && itype != EXPONENTIAL && itype != LOGISTIC && itype < BINOMIAL && p <= 0.0)
			return Globals.MISSING_VALUE;
		if ((itype == F || itype == BETA || itype == LOGISTIC || itype == STUDENTIZEDRANGE || itype == WEIBULL)
				&& q <= 0.0)
			return Globals.MISSING_VALUE;
		if (itype == BINOMIAL && (q < 0.0 || q > 1.0))
			return Globals.MISSING_VALUE;
		if (alpha <= 0.0 || alpha >= 1.0)
			return Globals.MISSING_VALUE;
		if ((itype == NORMAL || itype == T || itype == LOGISTIC) && alpha < Globals.EPSILON)
			return Globals.MISSING_VALUE;
		if (itype != UNIFORM && itype != BETA && itype != BINOMIAL && alpha > Globals.ONE_MINUS_EPSILON)
			return Globals.MISSING_VALUE;
		if (itype != NORMAL && itype != T && itype != LOGISTIC && alpha < Globals.EPSILON)
			return 0.0;

		double alph = alpha;
		double a = Globals.EPSILON;
		double b = Globals.BIG;
		double fa = Globals.EPSILON;
		double fb = Globals.ONE_MINUS_EPSILON;
		int n = 0;
		int ity = 0;
		double dt, u, v;
		double pp = p;
		double qq = q;
		boolean isDone = false;
		boolean doBisect = false;

		double result = Globals.MISSING_VALUE;
		if (itype == UNIFORM) {
			result = alpha * (q - p) + p;
			return result;
		} else if (itype == NORMAL) {
			result = p + q * inverseGaussian(alpha);
			return result;
		} else if (itype == T) {
			pp = p / 2.0;
			qq = 1.0 / 2.0;
			alph = alpha * 2.0;
			if (alpha > 0.5)
				alph = (1.0 - alpha) * 2.0;
			b = Globals.ONE_MINUS_EPSILON;
			ity = 2;
		} else if (itype == F) {
			pp = q / 2.0;
			qq = p / 2.0;
			alph = 1.0 - alpha;
			b = Globals.ONE_MINUS_EPSILON;
			ity = 2;
		} else if (itype == CHISQUARE) {
			pp = p / 2.0;
			ity = 1;
		} else if (itype == GAMMA) {
			pp = p;
			ity = 1;
		} else if (itype == BETA) {
			pp = p;
			qq = q;
			b = Globals.ONE_MINUS_EPSILON;
			ity = 2;
		} else if (itype == EXPONENTIAL) {
			result = -Math.log(1.0 - alpha);
			result = result * q + p;
			return result;
		} else if (itype == LOGISTIC) {
			result = Math.log(alpha / (1.0 - alpha));
			result = result * q + p;
			return result;
		} else if (itype == STUDENTIZEDRANGE) {
			if (p < 2.0 || q < 1.0)
				return Globals.MISSING_VALUE;
			pp = p;
			qq = q;
			v = 38.0;
			dt = 30.0;
			ity = 4;
			if (q > 1.0) {
				double d = 0.5 + 0.5 * alpha;
				double t = inverseGaussian(d);
				if (pp < 120.0)
					t = t + (t * t * t + t) / pp / 4.0;
				u = .8843 - .2368 * t;
				if (pp < 120.0)
					u = u - 1.214 / pp + 1.208 * t / pp;
				v = t * (u * Math.log(qq - 1.0) + 1.4142);
				dt = 1.0 / (10.0 * pp * (1.0 - alpha));
				if (dt < Globals.FUZZ)
					dt = Globals.FUZZ;
			}
			do {
				a = v - dt;
				b = v + dt;
				if (a < 0.0)
					a = 0.0;
				fa = ptrng(a, pp, qq) - alpha;
				fb = ptrng(b, pp, qq) - alpha;
				dt = dt * 2.0;
			} while (fa * fb > 0.0);
		} else if (itype == WEIBULL) {
			pp = p;
			qq = q;
			ity = 3;
		} else if (itype == BINOMIAL) {
			result = 0.0;
			if (q < Globals.EPSILON)
				return result;
			result = Math.floor(p * Globals.ONE_PLUS_EPSILON);
			if (q > Globals.ONE_MINUS_EPSILON)
				return result;
			pp = p;
			qq = q;
			a = 0;
			b = pp;
			ity = 5;
		} else if (itype == POISSON) {
			pp = p;
			a = 0;
			b = Globals.BIG;
			ity = 6;
		}

		// first step

		do {
			double c = a;
			double fc = fa;
			double d = b - a;
			double e = d;
			do {
				if (Math.abs(fc) < Math.abs(fb)) {
					a = b;
					b = c;
					c = a;
					fa = fb;
					fb = fc;
					fc = fa;
				}

				// compare for convergence

				double x = 0.5 * (c - b);
				n++;
				if (n > 1000)
					return Globals.MISSING_VALUE;
				double db = Math.max(1.0, Math.abs(b));

				if (Math.abs((c - b) / db) <= Globals.FUZZ || Math.abs(fb) < Globals.EPSILON) {
					isDone = true;
					break;
				}

				double r, s;
				if (Math.abs(e) > Globals.EPSILON && Math.abs(fa) > Math.abs(fb)) {
					if (a == c) { // linear interpolation
						s = fb / fa;
						u = (c - b) * s;
						v = 1.0 - s;
					} else { // quadratic interpolation
						v = fa / fc;
						r = fb / fc;
						s = fb / fa;
						u = s * ((c - b) * v * (v - r) - (b - a) * (r - 1.0));
						v = (v - 1.0) * (r - 1.0) * (s - 1.0);
					}

					// adjust signs

					if (u > 0.0)
						v = -v;
					u = Math.abs(u);

					// compare if interpolation acceptable

					if (u + u > 3.0 * x * v - Math.abs(Globals.EPSILON * v) || u >= Math.abs(0.5 * e * v)) {
						doBisect = true;
					} else {
						e = d;
						d = u / v;
						doBisect = false;
					}
				}

				// bisection

				if (doBisect) {
					d = x;
					e = d;
				}

				// complete step

				a = b;
				fa = fb;
				dt = d;
				if (Math.abs(dt) <= Globals.EPSILON)
					dt = x < 0.0 ? -Globals.EPSILON : Globals.EPSILON;
				b = b + dt;
				if (ity == 1)
					fb = incompleteGamma(b, pp) - alph;
				if (ity == 2)
					fb = incompleteBeta(b, pp, qq) - alph;
				if (ity == 3)
					fb = cdf(b, pp, qq, itype, false) - alph;
				if (ity == 4)
					fb = ptrng(b, pp, qq) - alph;
				if (ity == 5)
					fb = 1.0 - incompleteBeta(qq, b + 1.0, pp - b) - alph;
				if (ity == 6)
					fb = 1.0 - incompleteGamma(pp, b) - alph;

				if (isDone)
					break;

			} while (fb * fc <= 0.0);

			if (isDone)
				break;

		} while (true);

		// isDone

		result = b;
		if (itype == T) {
			if (b == 0.0)
				return Globals.MISSING_VALUE;
			result = Math.sqrt(p * (1.0 - b) / b);
		}
		if (itype == T && alpha < 0.5)
			result = -result;
		if (itype == F) {
			if (b == 0.0 || p == 0.0)
				return Globals.MISSING_VALUE;
			result = q * (1.0 - b) / (b * p);
		}
		if (itype == CHISQUARE)
			result = 2.0 * b;
		if (itype == BINOMIAL)
			result = Math.floor((1.0 + b) * (1.0 - Globals.FUZZ));
		if (itype == POISSON)
			result = Math.floor(b * (1.0 - Globals.FUZZ));
		if (result < Globals.BIG) {
			return result;
		} else {
			return Globals.MISSING_VALUE;
		}
	}

	public static double ptrng(double q, double r, double v) {

		double c, ehj, g, gmid, gstep, h, hj, pcutj, pcutk, pj, pk, pk1, pk2, ptrn, r1, step, v2, vmax, w0, x;
		double gk, gkmq, gkmqw, ptrngv, pz;
		double[] qw = new double[100];
		double[] vw = new double[100];

		/*
		 * Algorithm AS 190 Applied Statistics (1983) Vol. 32, No. 2 Arrays vw and qw store transient values used in the
		 * quadrature summation. Node spacing is controlled by step. pcutj and pcutk control truncation. Minimum and
		 * maximum
		 * number of steps are controlled by jmin, jmax, kmin, and kmax. Accuracy can be increased by use of a finer
		 * grid -
		 * increase sizes of arrays vw and qw, and jmin, jmax, kmin, kmax, and 1/step proportionally.
		 */

		pcutj = .000001;
		pcutk = .00001;
		step = .45;
		vmax = 120.;
		int jmin = 3;
		int jmax = 15;
		int kmin = 7;
		int kmax = 15;
		h = 0;
		if (r <= 3.0 || v <= 2.0) {
			pcutj = .0000001;
			pcutk = .000001;
			step = .15;
			jmin = 9;
			jmax = 45;
			kmin = 15;
			kmax = 45;
		}

		// check initial values

		ptrn = 0.;
		if (q <= 0.)
			return ptrn;
		if (v <= 0. || r <= 1.)
			return Globals.MISSING_VALUE;

		// computing constants, locating midpoint, adjusting steps

		g = step * Math.pow(r, -.2);
		gmid = .5 * Math.log(r);
		r1 = r - 1.;
		c = Math.log(r * g * .39894228);
		if (v < vmax) {
			h = step * Math.pow(v, -.5);
			v2 = v * .5;
			if (v <= 1.) {
				c = .1930647052601;
			} else if (v <= 2.) {
				c = .2935253263475;
			} else if (v <= 3.) {
				c = .3690543141898;
			} else if (v <= 4.) {
				c = .4319277321055;
			} else {
				c = Math.sqrt(v2) * .31830989 / (1. + ((-.26813272e-2 / v2 + .34722222e-2) / v2 + .83333333e-1) / v2);
			}
			c = Math.log(c * r * g * h);
		}

		/*
		 * computing integral. given a row k, the procedure starts at the midpoint and works outward (index j) in
		 * calculating the
		 * probability at nodes symmetric about the midpoint. the rows (index k) are also processed outwards
		 * symmetrically about
		 * the midpoint. the center row is unpaired.
		 */
		gstep = g;
		int jmx1 = jmax + 1;
		qw[0] = -1.;
		qw[jmx1 - 1] = -1.;
		pk1 = 1.;
		pk2 = 1.;
		for (int k = 1; k < kmax; k++) {
			gstep = gstep - g;
			do {
				gstep = -gstep;
				gk = gmid + gstep;
				pk = 0.;
				if (pk2 > pcutk || k <= kmin) {
					w0 = c - gk * gk * .5;
					pz = 1.0 - gauss(gk);
					gkmq = gk - q;
					x = 1.0 - gauss(gkmq) - pz;
					if (x > 0.)
						pk = Math.exp(w0 + r1 * Math.log(x));
					if (v <= vmax) {
						int jump = -jmax;
						do {
							jump += jmax;
							for (int j = 0; j < jmax; j++) {
								int jj = j + jump;
								int jjp1 = jj + 1;
								if (qw[jj] <= 0.) {
									hj = h * j;
									if (j < jmax)
										qw[jjp1] = -1.;
									ehj = Math.exp(hj);
									qw[jj] = q * ehj;
									vw[jj] = v * (hj + .5 - ehj * ehj * .5);
								}
								pj = 0.;
								gkmqw = gk - qw[jj];
								x = 1.0 - gauss(gkmqw) - pz;
								if (x > 0.)
									pj = Math.exp(w0 + vw[jj] + r1 * Math.log(x));
								pk = pk + pj;
								if (pj <= pcutj) {
									if (jj > jmin || k > kmin)
										break;
								}
							}
							h = -h;
						} while (h < 0.);
					}
				}

				ptrn = ptrn + pk;
				if (k > kmin && pk <= pcutk && pk1 <= pcutk)
					return ptrn;
				pk2 = pk1;
				pk1 = pk;
			} while (gstep > 0.);
		}
		ptrngv = ptrn;
		return ptrngv;
	}

	public static double gauss(double x) {
		/*
		 * normal cumulative distribution function (lower tail). from Collected Algorithms of the ACM, Number 209
		 * extreme tail
		 * quadratic approximation added by Leland Wilkinson
		 */
		double result;
		double w, y, z = 0.0;
		if (x != 0.0) {
			z = Globals.ONE_MINUS_EPSILON;
			y = Math.abs(x) / 2.0;
			if (y < 1.0) { // central approximation (0<abs(z)<2)
				w = y * y;
				z = ((((((((.000124818987 * w - .001075204047) * w + .005198775019) * w - .019198292004) * w + .059054035642)
						* w - .151968751364)
						* w + .319152932694)
						* w - .5319230073)
						* w + .797884560593)
						* y * 2.0;
			} else if (y < 2.0) { // tail approximation (2<abs(z)<4)
				y = y - 2.0;
				z = (((((((((((((-.000045255659 * y + .00015252929) * y - .000019538132) * y - .000676904986) * y + .001390604284)
						* y - .00079462082)
						* y - .002034254874)
						* y + .006549791214)
						* y - .010557625006)
						* y + .011630447319)
						* y - .009279453341)
						* y + .005353579108)
						* y - .002141268741)
						* y + .000535310849)
						* y + .999936657524;
			} else if (y < 4.0) { // extreme tail approximation (4<abs(z)<8) */
				y = y * 2.0;
				w = -.560159336 - .139082759 * y - .21143111 * y * y;
				z = 1.0 - 2.0 * Math.pow(10.0, w);
			}
		}
		if (x < 0.0) {
			result = (1.0 - z) / 2.0;
		} else {
			result = (1.0 + z) / 2.0;
		}
		if (result < 0.0)
			result = 0.0;
		if (result > 1.0)
			result = 1.0;
		return result;
	}

	public static double incompleteBeta(double x, double p1, double q1) {

		/*
		 * incomplete beta integral (lower tail) adapted from Majumder and Bhattacharjee, AS63, Applied Statistics
		 */

		double p = Math.max(p1, 0.001);
		double q = Math.max(q1, 0.001);
		double result = Globals.MISSING_VALUE;
		if (Globals.isMissing(x))
			return result;
		if (p > 1.0e8 || q > 1.0e8)
			return result;
		result = 0.0;
		if (x <= 0.0 || q <= 0.0)
			return result;
		result = 1.0;
		if (x >= 1.0 || p <= 0.0)
			return result;

		double e = 1.0e-8;
		double beta = Math2.logGamma(p) + Math2.logGamma(q) - Math2.logGamma(p + q);
		boolean done = false;
		boolean reverse = false;
		boolean upper = p < (p + q) * x;

		double xx, cx, pp, qq, pq, term, ai, rx;
		do {
			if (upper) { // upper tail
				xx = 1.0 - x;
				cx = x;
				pp = q;
				qq = p;
				reverse = true;
			} else { // lower tail
				xx = x;
				cx = 1.0 - x;
				pp = p;
				qq = q;
				reverse = false;
			}

			term = 1.0; // setup
			ai = 1.0;
			result = 1.0;
			pq = p + q;
			int ns = (int) (qq + cx * pq);
			rx = xx / cx;
			double temp = qq - ai;
			if (ns == 0)
				rx = xx;

			do { // accumulate terms
				term *= temp * rx / (pp + ai);
				temp = Math.abs(term);
				if (temp < Globals.BIG) {
					result = result + term;
					if (temp <= e * result || ns < -100000) {
						done = true;
						break;
					}
					ai += 1.0;
					ns--;
					if (ns < 0) {
						temp = pq;
						pq += 1.0;
					} else {
						temp = qq - ai;
						if (ns == 0)
							rx = xx;
					}
				} else {
					upper = !reverse;
					break;
				}
			} while (true);
			if (done)
				break;
		} while (true);

		result = 1.0 - result * Math.exp(pp * Math.log(xx) + (qq - 1.0) * Math.log(cx) - beta) / pp;
		if (!reverse)
			result = 1.0 - result;
		return result;
	}

	public static double incompleteGamma(double x, double a) throws ArithmeticException {

		double ans, ax, c, r;

		if (x <= 0 || a <= 0)
			return 0.0;

		if (x > 1.0 && x > a)
			return 1.0 - incompleteGammaComplement(x, a);

		/* Compute x**a * exp(-x) / gamma(a) */
		ax = a * Math.log(x) - x - Math2.logGamma(a);
		if (ax < -Globals.MAXLOG)
			return 0;

		ax = Math.exp(ax);

		/* power series */
		r = a;
		c = 1.0;
		ans = 1.0;

		do {
			r += 1.0;
			c *= x / r;
			ans += c;
		} while (c / ans > Globals.EPSILON);

		return ans * ax / a;

	}

	public static double incompleteGammaComplement(double x, double a) {
		double big = 4.503599627370496e15;
		double biginv = 2.22044604925031308085e-16;
		double ans, ax, c, yc, r, t, y, z;
		double pk, pkm1, pkm2, qk, qkm1, qkm2;

		if (x <= 0 || a <= 0)
			return 1.0;

		if (x < 1.0 || x < a)
			return 1.0 - incompleteGamma(x, a);

		ax = a * Math.log(x) - x - Math2.logGamma(a);
		if (ax < -Globals.MAXLOG)
			return 0.0;

		ax = Math.exp(ax);

		/* continued fraction */
		y = 1.0 - a;
		z = x + y + 1.0;
		c = 0.0;
		pkm2 = 1.0;
		qkm2 = x;
		pkm1 = x + 1.0;
		qkm1 = z * x;
		ans = pkm1 / qkm1;

		do {
			c += 1.0;
			y += 1.0;
			z += 2.0;
			yc = y * c;
			pk = pkm1 * z - pkm2 * yc;
			qk = qkm1 * z - qkm2 * yc;
			if (qk == 0)
				t = 1.0;
			else {
				r = pk / qk;
				t = Math.abs((ans - r) / r);
				ans = r;
			}

			pkm2 = pkm1;
			pkm1 = pk;
			qkm2 = qkm1;
			qkm1 = qk;
			if (Math.abs(pk) > big) {
				pkm2 *= biginv;
				pkm1 *= biginv;
				qkm2 *= biginv;
				qkm1 *= biginv;
			}
		} while (t > Globals.EPSILON);

		return ans * ax;
	}

	public static double inverseGaussian(double p) {

		double pp, x, y;

		/*
		 * inverse normal distribution from odeh and evans algorithm as70 applied statistics, 1974.
		 */

		if (p <= Globals.EPSILON || p >= Globals.ONE_MINUS_EPSILON)
			return Globals.MISSING_VALUE;
		if (p == 0.5)
			return 0.;
		pp = p;
		if (p > 0.5)
			pp = 1.0 - p;
		y = Math.sqrt(Math.log(1.0 / (pp * pp)));
		x = y + ((((-y * .45364221e-4 - .02042312) * y - .34224209) * y - 1.0) * y - .32223243)
				/ ((((y * .00385607 + .10353775) * y + .53110346) * y + .58858157) * y + .09934846);

		if (p < 0.5)
			x = -x;
		return x;
	}

	public static double gammaFunction(double x) {

		double b, c, d, dgammav, g, y;

		// gamma function from Collected Algorithms of the ACM

		c = 1.0;
		y = x;

		if (y > 10.0) {
			d = 1.0 / y;
			c = d * d;
			g = (((((((((-1.392432216905901 * c + .1796443723688306) * c - .02955065359477124) * c + .00641025641025641)
					* c - .0019175269175269175)
					* c + .8417508417508418e-3)
					* c - .5952380952380952e-3)
					* c + .79365079365079365e-3)
					* c - .002777777777777778)
					* c + .08333333333333333)
					* d + .9189385332046727 + (y - 0.5) * Math.log(y) - y;
			dgammav = Math.exp(g);
		} else {
			if (y < 2.0) {
				do {
					c = y * c;
					y = y + 1.0;
				} while (y < 2.0);
				c = 1.0 / c;
			} else if (y > 3.0) {
				do {
					y = y - 1.0;
					c = c * y;
				} while (y > 3.0);
			}

			b = y - 2.0;
			g = ((((((((((((((-.5113262726698e-6 * b + .51063592072582e-5) * b - .248410053848712e-4) * b + .815530498066373e-4)
					* b - .2064476319159326e-3)
					* b + .4677678114964956e-3)
					* b - .9083465574200521e-3)
					* b + .002099759035077063)
					* b - .002851501243034649)
					* b + .0111538196719067)
					* b - .2669510287555266e-3)
					* b + .07424900794340127)
					* b + .08157691940138868)
					* b + .4118403304219815)
					* b + .4227843350985181)
					* b + .9999999999999999;
			dgammav = g * c;
		}

		return dgammav;
	}

	public static double durbinWatson(int nCases, int nPredictors) {
		/* Approximate lower 1% bound for Durban-Watson statistic */
		double n = nCases;
		double p = nPredictors;
		return (.84418321 + .031723895 * n + .089692688 * p) / (1 + .01651243 * n + .03189049 * p);
	}

	public static double cook05(int nCases, int nPredictors) {
		/*
		 * Approximate Bonferroni-corrected .05 critical values of Cook's D statistic Interpolated from tables in
		 * Muller, K.E. and
		 * Mok, M.C. (1997). The distribution of Cook's D statistic. Communications in Statistics: Theory & Methods, 26,
		 * 525-546.
		 */
		double n = nCases;
		double p = nPredictors;
		double z = 23.53989485 - 5.61214967 * Math.log(n) + 3.314147196 * Math.log(p) + 1.231564044 * Math.log(n)
				* Math.log(n) + 0.647522133 * Math.log(p) * Math.log(p) - 1.77419419 * Math.log(n) * Math.log(p);
		return 1.4 * z / (n - p);
	}

	public static double bowleySkewness01(int nCases) {
		/*
		 * Approximate .01 critical value of Bowley skewness statistic using Normal distribution as null.
		 */
		return 1.0 / Math.sqrt(nCases);
	}

	public static boolean[] isSignificantBenjaminiHochberg(double[] pValues) {
		/*
		 * Benjamini, Yoav; Hochberg, Yosef (1995). Controlling the false discovery rate: a practical and powerful
		 * approach to
		 * multiple testing. Journal of the Royal Statistical Society, Series B (Methodological) 57 (1): 289-300.
		 */
		int p = pValues.length;
		int[] indices = Sorts.indexedDoubleArraySort(pValues, 0, p);
		boolean[] isSignificant = new boolean[p];
		for (int j = 0; j < p; j++) {
			double bh = .05 * (p - j) / p; // Benjamini-Hochberg p value
			if (pValues[indices[j]] < bh)
				isSignificant[indices[j]] = true;
		}
		return isSignificant;
	}

	public static double[][] randomCovarianceMatrix(double[][] sigma, double df) {
		/*
		 * Odell, P. L. and Feiveson, A. H. (1966). A Numerical Procedure to Generate a Sample Covariance Matrices.
		 * Journal of the
		 * American Statistical Association, 61, 199-203. sigma is population matrix.
		 */
		int p = sigma.length;
		boolean isCorrelation = true;
		for (int j = 0; j < p; j++) {
			if (Math.abs(sigma[j][j] - 1) > Globals.FUZZ)
				isCorrelation = false;
		}

		/* put cholesky factor of sigma in lower triangle of c */
		double[][] c = LinearSystems.cholesky(sigma);
		if (c == null)
			return null;

		/* matrix of normals with chi-squares on diagonal */
		double[][] z = new double[p][];
		for (int i = 0; i < p; i++) {
			z[i] = new double[i + 1];
			for (int j = 0; j < i; j++) {
				z[i][j] = Globals.random.nextGaussian();
			}
			z[i][i] = random(df - i, 1, Probabilities.CHISQUARE);
		}

		/* make Wishart matrix */
		double[][] w = new double[p][p];
		for (int i = 0; i < p; i++) {
			for (int j = 0; j <= i; j++) {
				double z2 = 0;
				if (i == j) {
					for (int k = 0; k < i - 1; k++)
						z2 += z[i][k] * z[i][k];
					w[i][i] = z[i][i] + z2;
				} else {
					for (int k = 0; k < j - 1; k++)
						z2 += z[i][k] * z[j][k];
					w[i][j] = z[i][j] * Math.sqrt(z[i][i]) + z2;
				}
				w[j][i] = w[i][j];
			}
		}

		/* (1 / df) * transpose(T) * W * T */
		double[][] t = new double[p][p];
		for (int i = 0; i < p; i++) {
			for (int j = 0; j <= i; j++) {
				for (int k = 0; k < p; k++)
					t[i][j] += c[i][k] * w[k][j];
				t[j][i] = t[i][j];
			}
		}

		double[][] result = new double[p][p];
		for (int i = 0; i < p; i++) {
			for (int j = 0; j <= i; j++) {
				for (int k = 0; k < p; k++)
					result[i][j] += t[i][k] * c[k][j] / df;
				result[j][i] = result[i][j];
			}
			/* not Kosher for generating a correlation matrix, but OK as rough approximation */
			if (isCorrelation)
				result[i][i] = 1;
		}

		return result;
	}

	public static double[][] computePareto(int k, int n, double p, boolean isCumulative) {
		/* Wilkinson, L. (2006). Revising the Pareto Chart. The American Statistician, 60, 332-334. */
		if (p == 0)
			p = .05;
		int samp = 10000;
		Random random = new Random(13579);
		int[][] samples = new int[k][samp];
		for (int i = 0; i < samp; i++) {
			int[] s = new int[k];
			for (int j = 0; j < n; j++) {
				int m = random.nextInt(k);
				s[m]++;
			}
			Arrays.sort(s);
			if (isCumulative) {
				for (int j = 1; j < k; j++) {
					s[k - j - 1] += s[k - j];
				}
			}
			for (int j = 0; j < k; j++)
				samples[k - j - 1][i] = s[j];
		}

		double[][] result = new double[k][2];
		for (int j = 0; j < k; j++) {
			Arrays.sort(samples[j]);
			int n1 = (int) ((p / 2. * samp) / 100.);
			int n2 = (int) (((1.0 - p / 2) * samp) / 100.);
			result[j][0] = samples[j][n1];
			result[j][1] = samples[j][n2];
			if (result[j][0] == result[j][1])
				result[j][1]++;
		}
		return result;
	}
}
