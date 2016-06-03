/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util;

public class FastFourierTransform {
	/* Fast Fourier Transform */
	public static double[][] compute(final double[] real, double[] imag, boolean inverse) {
		int n = real.length; // n must be power of 2
		int logN = (int) (Math.log(n) / Math.log(2));
		int n2 = n / 2;
		int n1 = logN - 1;
		double[] xReal = new double[n];
		double[] xImag = new double[n];
		double constant = -2 * Math.PI;
		if (inverse)
			constant = -constant;
		for (int i = 0; i < n; i++) {
			xReal[i] = real[i];
			xImag[i] = imag[i];
		}

		double tReal, tImag;
		int k = 0;
		for (int i = 0; i < logN; i++) {
			while (k < n) {
				for (int j = 0; j < n2; j++) {
					double r = reverse(k >> n1, logN);
					double t = constant * r / n;
					double c = Math.cos(t);
					double s = Math.sin(t);
					tReal = xReal[k + n2] * c + xImag[k + n2] * s;
					tImag = xImag[k + n2] * c - xReal[k + n2] * s;
					xReal[k + n2] = xReal[k] - tReal;
					xImag[k + n2] = xImag[k] - tImag;
					xReal[k] += tReal;
					xImag[k] += tImag;
					k++;
				}
				k += n2;
			}
			k = 0;
			n1--;
			n2 /= 2;
		}

		k = 0;
		while (k < n) {
			int m = reverse(k, logN);
			if (m > k) {
				tReal = xReal[k];
				tImag = xImag[k];
				xReal[k] = xReal[m];
				xImag[k] = xImag[m];
				xReal[m] = tReal;
				xImag[m] = tImag;
			}
			k++;
		}

		return new double[][] { xReal, xImag };
	}

	private static int reverse(int j, int n) {
		int j1 = j;
		int j2 = 0;
		int k = 0;
		for (int i = 0; i < n; i++) {
			j2 = j1 / 2;
			k = 2 * k + j1 - 2 * j2;
			j1 = j2;
		}
		return k;
	}
}