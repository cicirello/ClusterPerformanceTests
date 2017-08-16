/*
 * Copyright 2017 Vincent A. Cicirello.
 *
 * This file is part of package org.cicirello.algengine.
 *
 * Java package org.cicirello.algengine is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * Java package org.cicirello.algengine is distributed in the hope 
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
package org.cicirello.algengine;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to distributed algorithms available via Java RMI.
 * 
 * @author Vincent A. Cicirello
 * @version 8.15.2017
 */
public interface RemoteParallelAlgorithms extends Remote {
	
	/**
	 * Matrix multiplication.  Computes a*b.
	 * Assumes that the dimensions of a and b are such that a*b is a legal
	 * operation.  Otherwise, may throw a bounds exception.
	 * 
	 * @param numThreads The number of threads to use for the multiplication
	 * on the remote server.
	 * @param a The first matrix,
	 * @param bTranspose The transpose of the second matrix.
	 * @return a*b
	 * @throws RemoteException When exceptional behavior occurs on the RMI server.
	 */
	double[][] multiply(int numThreads, double[][] a, double[][] bTranspose) throws RemoteException;
	
	/**
	 * A multithreaded Monte Carlo estimate of Pi.
	 * Divides the number of samples, n, equally among the threads.
	 * If n is not divisible by the number of threads, uses the smallest
	 * n' &#62; n such that n' is divisible by the number of threads. 
	 * 
	 * @param n The minimum number of samples.
	 * @param numThreads The number of threads to use.
	 * @return Estimate of Pi.
	 * @throws RemoteException When exceptional behavior occurs on the RMI server.
	 */
	double pi(int n, int numThreads) throws RemoteException;
}
