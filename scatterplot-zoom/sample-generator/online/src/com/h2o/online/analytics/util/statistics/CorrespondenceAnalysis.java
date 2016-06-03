/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.EigenSystems;

public class CorrespondenceAnalysis {
	private double[][] xCoord, yCoord;
	private double[] eigenvalues;

	public CorrespondenceAnalysis() {
	}

	public double[][] getYCoordinates() {
		return yCoord;
	}

	public double[][] getXCoordinates() {
		return xCoord;
	}

	public double[] getEigenvalues() {
		return eigenvalues;
	}

	public void computeSingle(double[][] xx, double[][] yx, double[][] yy, int nTot) {
		int nx = xx.length;
		int ny = yy.length;
		double[][] uv = new double[ny][nx];
		for (int j = 0; j < nx; j++) {
			for (int i = 0; i < ny; i++) {
				yx[i][j] = yx[i][j] - yy[i][i] * xx[j][j] / nTot;
			}
		}
		int pivot = LinearSystems.symdet(xx);
		if (pivot >= 0)
			return;
		for (int i = 0; i < ny; i++)
			LinearSystems.symsol(xx, yx[i], uv[i]);

		/* Y coordinates */
		double[][] uu = new double[ny][ny];
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < ny; j++) {
				for (int k = 0; k < nx; k++) {
					uu[i][j] += yx[i][k] * uv[j][k];
				}
			}
		}
		double[][] eigenvectors = new double[ny][ny];
		eigenvalues = new double[ny];
		EigenSystems.eigenAsymmetric(uu, yy, eigenvectors, eigenvalues);
		int nc = 2;
		if (ny < 2 || eigenvalues[1] < Globals.EPSILON || eigenvalues[0] < Globals.EPSILON)
			return;

		yCoord = new double[ny][nc];
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nc; j++) {
				yCoord[i][j] = eigenvectors[i][j] * Math.sqrt(eigenvalues[j] * nTot);
			}
		}

		xCoord = new double[nx][nc];
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < nc; j++) {
				for (int k = 0; k < ny; k++) {
					xCoord[i][j] += yCoord[k][j] * uv[k][i] / Math.sqrt(eigenvalues[j]);
				}
			}
		}
	}

	public void computeMultiple(double[][] yy, int nTot, int nVars) {
		int ny = yy.length;
		double[][] uu = new double[ny][ny];
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < ny; j++) {
				uu[i][j] = (yy[i][j] - yy[i][i] * yy[j][j] / nTot) / (nVars * Math.sqrt(yy[i][i] * yy[j][j]));
			}
		}
		double[][] eigenvectors = new double[ny][ny];
		eigenvalues = new double[ny];
		EigenSystems.eigenSymmetric(uu, eigenvectors, eigenvalues);
		int nc = 2;
		if (eigenvalues[1] < Globals.EPSILON)
			nc = 1;
		if (eigenvalues[0] < Globals.EPSILON)
			return;

		yCoord = new double[ny][nc];
		for (int j = 0; j < nc; j++) {
			int neg = 0;
			for (int i = 0; i < ny; i++) {
				if (eigenvectors[i][j] < 0)
					neg++;
				yCoord[i][j] = eigenvectors[i][j] * Math.sqrt(nVars * nTot * eigenvalues[j] / yy[i][i]);
			}
			if (neg > ny / 2) {
				for (int i = 0; i < ny; i++)
					yCoord[i][j] = -yCoord[i][j];
			}
		}
	}
}
