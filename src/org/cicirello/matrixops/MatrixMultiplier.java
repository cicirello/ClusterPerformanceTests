/*
 * Copyright 2017 Vincent A. Cicirello.
 *
 * This file is part of package org.cicirello.matrixops.
 *
 * Java package org.cicirello.matrixops is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * Java package org.cicirello.matrixops is distributed in the hope 
 * that it will be useful, but WITHOUT ANY WARRANTY; without even 
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java package org.cicirello.permutations.  If not, 
 * see <http://www.gnu.org/licenses/>.
 *
 */

package org.cicirello.matrixops;

/**
 * An implementation of matrix multiplication.
 * 
 * @author Vincent A. Cicirello
 * @version 8.15.2017
 */
public class MatrixMultiplier {

	/**
	 * For matrices a and b, compute a * b.
	 * The number of columns of a must be equal to the number of
	 * rows of b.  This method assumes dimensions of a and b are
	 * consistent for a legal multiplication, and may throw a 
	 * bounds exception if they are not.
	 * @param a First matrix
	 * @param b Second matrix
	 * @return a * b
	 */
	public double[][] multiply(double[][] a, double[][] b) {
		double[][] c = new double[a.length][b[0].length];
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c[i].length; j++) {			
				for (int k = 0; k < b.length; k++) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return c;
	}
	
	/**
	 * For matrices a and b, computes either a * b, or a * transpose(b).
	 * The number of columns of a must be equal to the number of
	 * rows of b (if b is not transposed) or the number of columns of b
	 * (if b is transposed).  This method assumes dimensions of a and b are
	 * consistent for a legal multiplication, and may throw a 
	 * bounds exception if they are not.
	 * @param a First matrix
	 * @param b Second matrix
	 * @param bIsTransposed If true, then b is actually the transpose of b. 
	 * @return a * b
	 */
	public double[][] multiply(double[][] a, double[][] b, boolean bIsTransposed) {
		if (!bIsTransposed) b = transpose(b);
		double[][] c = new double[a.length][b.length];
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c[i].length; j++) {			
				for (int k = 0; k < b[0].length; k++) {
					c[i][j] += a[i][k] * b[j][k];
				}
			}
		}
		return c;
	}
	
	/**
	 * Computes the transpose of a matrix m.
	 * @param m The matrix
	 * @return transpose of m
	 */
	public final double[][] transpose(double[][] m) {
		double[][] c = new double[m[0].length][m.length];
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < m.length; j++) {
				c[i][j] = m[j][i];
			}
		}
		return c;
	}
	
}
