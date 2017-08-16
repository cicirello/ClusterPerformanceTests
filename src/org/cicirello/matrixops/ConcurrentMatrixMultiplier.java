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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * A multithreaded matrix multiplier.  Uses a very simple approach to
 * sub-division of work.  Specifically, in computing A*B, distributes 
 * either the rows of matrix A or the columns of matrix B (whichever 
 * is larger) equally among the threads.  Better parallel matrix
 * multiplication algorithms exist.  This class was implemented to serve
 * as a test case for a specific system for a specific testing purpose, 
 * and not intended to be used more generally as a matrix multiplier.
 *  
 * @author Vincent A. Cicirello
 * @version 8.15.2017
 */
public class ConcurrentMatrixMultiplier extends MatrixMultiplier {
	
	private int numThreads;
	protected final ExecutorService es;
	
	/**
	 * Initialize the matrix multiplier.  Uses a cached thread pool 
	 * by default.
	 * 
	 * @param threads The number of threads.
	 */
	public ConcurrentMatrixMultiplier(int threads) {
		this(threads, Executors.newCachedThreadPool());
	}
	
	/**
	 * Initialize the matrix multiplier.
	 * 
	 * @param threads The number of threads.
	 * @param es An executor service, enabling you to specify thread pool
	 * type.
	 */
	public ConcurrentMatrixMultiplier(int threads, ExecutorService es) {
		this.numThreads = threads;
		this.es = es;
	}
	
	/**
	 * Change the number of threads used by this matrix multiplier.
	 * 
	 * @param threads The number of threads.
	 */
	public void setThreads(int threads) {
		this.numThreads = threads;
	}
	
	/**
	  * {@inheritDoc}
	  */
	@Override
	public final double[][] multiply(double[][] a, double[][] b) {
		return multiply(a,b,false);
	}
	
	/**
	  * {@inheritDoc}
	  */
	@Override
	public final double[][] multiply(double[][] a, double[][] b, boolean bIsTransposed) {
		if (!bIsTransposed) b = transpose(b); 
		if (a.length >= b[0].length)
			return multiplyByDistributingRowsOfA(a, b);
		else 
			return multiplyByDistributingColsOfB(a, b);
	}
	
	/**
	 * Gets the number of threads this matrix multiplier uses.
	 * @return number of threads
	 */
	protected final int numThreads() { return numThreads; }
	
	/**
	 * Multiplies matrices a and b by distributing the matrix b
	 * among the threads.  Assumes that b is transposed.
	 * 
	 * @param a First matrix
	 * @param b Second matrix
	 * @return a * b
	 */
	protected double[][] multiplyByDistributingColsOfB(final double[][] a, final double[][] b) {
		int t = (b.length < numThreads) ? b.length : numThreads;
		
		@SuppressWarnings("unchecked")
		Future<double[][]>[] threadFutures = (Future<double[][]>[])new Future[t];
		
		int minColsPerThread = b.length / t;
		int numThreadsWithExtra = b.length % t;
		
		int k = 0;
		for (int i = 0; i < t; i++) {
			double[][] bT = numThreadsWithExtra > 0 ? new double[minColsPerThread+1][] : new double[minColsPerThread][];
			numThreadsWithExtra--;	
			for (int j = 0; j < bT.length; j++) {
				bT[j] = b[k];
				k++;
			}
			threadFutures[i] = es.submit(new MatrixMultThread(a, bT));
		}
		double[][] c = new double[a.length][b.length];
		k = 0;
		for (int i = 0; i < t; i++) {
			double[][] cThread = null;
			try {
				cThread = threadFutures[i].get();
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			} 
			for (int j = 0; j < cThread.length; j++) {
				for (int z = 0; z < cThread[j].length; z++)
				c[j][k+z] = cThread[j][z];
			}
			k += cThread[0].length;
		}
		return c;
	}

	/**
	 * Multiplies matrices a and b by distributing the matrix a
	 * among the threads.  Assumes that b is transposed.
	 * 
	 * @param a First matrix
	 * @param b Second matrix
	 * @return a * b
	 */
	protected double[][] multiplyByDistributingRowsOfA(final double[][] a, final double[][] b) {
		int t = (a.length < numThreads) ? a.length : numThreads;

		@SuppressWarnings("unchecked")
		Future<double[][]>[] threadFutures = (Future<double[][]>[])new Future[t];
		
		int minRowsPerThread = a.length / t;
		int numThreadsWithExtra = a.length % t;
		
		int k = 0;
		for (int i = 0; i < t; i++) {
			double[][] aT = numThreadsWithExtra > 0 ? new double[minRowsPerThread+1][] : new double[minRowsPerThread][];
			numThreadsWithExtra--;	
			for (int j = 0; j < aT.length; j++) {
				aT[j] = a[k];
				k++;
			}
			threadFutures[i] = es.submit(new MatrixMultThread(aT, b));
		}
		double[][] c = new double[a.length][];
		k = 0;
		for (int i = 0; i < t; i++) {
			double[][] cThread = null;
			try {
				cThread = threadFutures[i].get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			for (int j = 0; j < cThread.length; j++) {
				c[k] = cThread[j];
				k++;
			}
		}
		return c;
	}

	private final class MatrixMultThread extends MatrixMultiplier implements Callable<double[][]> {

		private final double[][] a;
		private final double[][] bTranspose;

		public MatrixMultThread(final double[][] a, final double[][] bTranspose) {
			this.a = a;
			this.bTranspose = bTranspose;
		}

		@Override
		public double[][] call() throws Exception {
			return multiply(a,bTranspose,true);
		}
	}
	
}
