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

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.cicirello.algengine.RemoteParallelAlgorithms;

/**
 * A parallel matrix multiplier that uses Java RMI to distribute
 * the computation of the matrix multiplication among multiple
 * RMI servers.  
 * 
 * Uses a very simple approach to sub-division of work.  Specifically, 
 * in computing A*B, distributes either the rows of matrix A or the 
 * columns of matrix B (whichever is larger) equally among the RMI servers.
 * These RMI servers implement a multithreaded matrix multiplication, which
 * then distributed the larger of its two matrices among the available threads.
 * 
 * Better parallel matrix multiplication algorithms exist.  This class 
 * was implemented to serve as a test case for a specific system for a 
 * specific testing purpose, and not intended to be used more generally as a 
 * matrix multiplier.
 *
 * @author Vincent A. Cicirello
 * @version 8.15.2017
 */
public final class DistributedMatrixMultiplier extends ConcurrentMatrixMultiplier {

	private final String[] serverNames;
	private final int masterThreads;
	
	/**
	 * Initialize the matrix multiplier. A cached thread pool is used locally to
	 * manage the local threads that handle interaction with the RMI servers.
	 * 
	 * @param serverNames Array of RMI server names to distribute the work.
	 * @param threadsPerServer Number of threads to use on each RMI server.
	 * @param masterThreads Number of threads to use on the master node of
	 * the cluster.
	 */
	public DistributedMatrixMultiplier(String[] serverNames, int threadsPerServer, int masterThreads) {
		super(threadsPerServer);
		this.serverNames = serverNames;
		this.masterThreads = masterThreads;
	}
	
	/**
	 * Initialize the matrix multiplier.
	 * 
	 * @param serverNames Array of RMI server names to distribute the work.
	 * @param threadsPerServer Number of threads to use on each RMI server.
	 * @param masterThreads Number of threads to use on the master node of
	 * the cluster.
	 * @param es An executor service, enabling you to specify thread pool
	 * type.
	 */
	public DistributedMatrixMultiplier(String[] serverNames, int threadsPerServer, int masterThreads, ExecutorService es) {
		super(threadsPerServer, es);
		this.serverNames = serverNames;
		this.masterThreads = masterThreads;
	}
	
	/**
	  * {@inheritDoc}
	  */
	@Override
	protected final double[][] multiplyByDistributingColsOfB(final double[][] a, final double[][] b) {
		int t = masterThreads == 0 ? serverNames.length : serverNames.length + 1;
		if (b.length < t) t = b.length;
		//if (t>2 && (t & (t - 1)) == 0) return multiplyBySubdivision(a, b);
		
		@SuppressWarnings("unchecked")
		Future<double[][]>[] threadFutures = (Future<double[][]>[])new Future[t];
		
		
		int minColsPerThread = b.length / t;
		int numThreadsWithExtra = b.length % t;
		
		int remoteT = (masterThreads > 0) ? t-1 : t;
		int k = 0;
		for (int i = 0; i < remoteT; i++) {
			double[][] bT = numThreadsWithExtra > 0 ? new double[minColsPerThread+1][] : new double[minColsPerThread][];
			numThreadsWithExtra--;	
			for (int j = 0; j < bT.length; j++) {
				bT[j] = b[k];
				k++;
			}
			threadFutures[i] = es.submit(new LocalHandlerThread(a, bT, serverNames[i]));
		}
		if (masterThreads > 0) {
			double[][] bT = numThreadsWithExtra > 0 ? new double[minColsPerThread+1][] : new double[minColsPerThread][];
			numThreadsWithExtra--;	
			for (int j = 0; j < bT.length; j++) {
				bT[j] = b[k];
				k++;
			}
			threadFutures[t-1] = es.submit(new MasterNodeThread(a, bT));
		}
		double[][] c = new double[a.length][b.length];
		k = 0;
		for (int i = 0; i < t; i++) {
			double[][] cThread = null;
			try {
				cThread = threadFutures[i].get();
			} catch (InterruptedException | ExecutionException e) {
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
	  * {@inheritDoc}
	  */
	@Override
	protected final double[][] multiplyByDistributingRowsOfA(final double[][] a, final double[][] b) {
		int t = masterThreads == 0 ? serverNames.length : serverNames.length + 1;
		if (a.length < t) t = a.length;
		//if (t>2 && (t & (t - 1)) == 0) return multiplyBySubdivision(a, b);
		
		@SuppressWarnings("unchecked")
		Future<double[][]>[] threadFutures = (Future<double[][]>[])new Future[t];
		
		
		int minColsPerThread = a.length / t;
		int numThreadsWithExtra = a.length % t;
		
		int remoteT = (masterThreads > 0) ? t-1 : t;
		int k = 0;
		for (int i = 0; i < remoteT; i++) {
			double[][] aT = numThreadsWithExtra > 0 ? new double[minColsPerThread+1][] : new double[minColsPerThread][];
			numThreadsWithExtra--;	
			for (int j = 0; j < aT.length; j++) {
				aT[j] = a[k];
				k++;
			}
			threadFutures[i] = es.submit(new LocalHandlerThread(aT, b, serverNames[i]));
		}
		if (masterThreads > 0) {
			double[][] aT = numThreadsWithExtra > 0 ? new double[minColsPerThread+1][] : new double[minColsPerThread][];
			numThreadsWithExtra--;
			for (int j = 0; j < aT.length; j++) {
				aT[j] = a[k];
				k++;
			}
			threadFutures[t-1] = es.submit(new MasterNodeThread(aT, b));
		}
		double[][] c = new double[a.length][];
		k = 0;
		for (int i = 0; i < t; i++) {
			double[][] cThread = null;
			try {
				cThread = threadFutures[i].get();
			}  catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			for (int j = 0; j < cThread.length; j++) {
				c[k] = cThread[j];
				k++;
			}
		}
		return c;
	}
	
	private final class LocalHandlerThread implements Callable<double[][]> { 
		
		private final String serverName;
		private final double[][] a;
		private final double[][] bTranspose;
		
		public LocalHandlerThread(final double[][] a, final double[][] bTranspose, final String serverName) {
			this.serverName = serverName;
			this.a = a;
			this.bTranspose = bTranspose;
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
	        }
		}

		@Override
		public double[][] call() throws Exception {
			try {
				Registry registry = LocateRegistry.getRegistry(serverName);
				RemoteParallelAlgorithms comp = (RemoteParallelAlgorithms) registry.lookup("Alg");
				return comp.multiply(numThreads(), a, bTranspose);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
				return null;
			}	
		}
	}

	private final class MasterNodeThread implements Callable<double[][]> { 
		private final double[][] a;
		private final double[][] bTranspose;
		
		public MasterNodeThread(final double[][] a, final double[][] bTranspose) {
			this.a = a;
			this.bTranspose = bTranspose;
		}

		@Override
		public double[][] call() throws Exception {
			final ConcurrentMatrixMultiplier mult = new ConcurrentMatrixMultiplier(masterThreads);
			return mult.multiply(a, bTranspose, true);
		}
	}

}
