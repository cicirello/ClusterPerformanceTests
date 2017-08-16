/*
 * Copyright 2017 Vincent A. Cicirello.
 *
 * This file is part of package org.cicirello.math.
 *
 * Java package org.cicirello.math is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * Java package org.cicirello.math is distributed in the hope 
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


package org.cicirello.math;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.cicirello.algengine.RemoteParallelAlgorithms;

/**
 * Estimates the value of Pi using Monte Carlo integration.
 * 
 * @author Vincent A. Cicirello
 * @version 8.15.2017
 */
public class Pi {

	/**
	 * Estimates the value of Pi using Monte Carlo integration and n samples.
	 * @param n The number of samples.
	 * @return Estimate of Pi
	 */
	public static double pi(int n) {
		double mean = 0;
		for (int i = 1; i <= n; i++) {
			double x = ThreadLocalRandom.current().nextDouble();
			mean += (Math.sqrt(1-x*x)-mean)/i;
		}
		return 4*mean;
	}
	
	/**
	 * Multithreaded Monte Carlo estimate of Pi.
	 * Divides the number of samples, n, equally among the threads.
	 * If n is not divisible by the number of threads, uses the smallest
	 * n' &#62; n such that n' is divisible by the number of threads.
	 * Uses a cached thread pool by default.
	 * @param n The minimum number of samples.
	 * @param threadCount The number of threads.
	 * @return An estimate of Pi.
	 */
	public static double concurrentPi(int n, int threadCount) {
		return concurrentPi(n, threadCount, Executors.newCachedThreadPool());
	}
	
	/**
	 * Multithreaded Monte Carlo estimate of Pi.
	 * Divides the number of samples, n, equally among the threads.
	 * If n is not divisible by the number of threads, uses the smallest
	 * n' &#62; n such that n' is divisible by the number of threads.
	 * @param n The minimum number of samples.
	 * @param threadCount The number of threads.
	 * @param es An executor service to enable specifying type of 
	 * thread pool.
	 * @return An estimate of Pi.
	 */
	public static double concurrentPi(int n, int threadCount, ExecutorService es) {
		
		class PiWorker implements Callable<Double> {
			private int n;
			public PiWorker(int n) {
				this.n = n;
			}
			@Override
			public Double call() throws Exception {
				return pi(n);
			}
		}
		int perT = n / threadCount;
		if (n % threadCount != 0) perT++;
		@SuppressWarnings("unchecked")
		Future<Double>[] threadFutures = (Future<Double>[])new Future[threadCount];
		int i;
		for (i = 0; i < threadCount; i++) {
			threadFutures[i] = es.submit(new PiWorker(perT));
		}
		double mean = 0;
		for (i = 0; i < threadCount; i++) {
			try {
				mean += (threadFutures[i].get()-mean) / (i+1);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return mean;
	}
	
	/**
	 * Parallel Monte Carlo estimation of Pi, using Java RMI to
	 * distribute the computation among multiple RMI servers.
	 * Divides the number of samples, n, equally among the available
	 * RMI servers. If n is not divisible by the number of servers, 
	 * uses the smallest n' &#62; n such that n' is divisible by the number 
	 * of servers.  The RMI servers likewise will increase the number of samples
	 * in a similar fashion when distributing the work among their threads.
	 * Uses a cached thread pool by default to manage the threads
	 * used locally for the RMI calls.
	 * @param n The minimum number of samples.
	 * @param threadsPerServer The number of threads to execute on each RMI server.
	 * @param serverNames The list of RMI server names.
	 * @return Estimate of Pi.
	 */
	public static double distributedPi(int n, int threadsPerServer, String[] serverNames) {
		return distributedPi(n, threadsPerServer, serverNames, Executors.newCachedThreadPool());
	}
	
	/**
	* Parallel Monte Carlo estimation of Pi, using Java RMI to
	 * distribute the computation among multiple RMI servers.
	 * Divides the number of samples, n, equally among the available
	 * RMI servers. If n is not divisible by the number of servers, 
	 * uses the smallest n' &#62; n such that n' is divisible by the number 
	 * of servers.  The RMI servers likewise will increase the number of samples
	 * in a similar fashion when distributing the work among their threads.
	 * Uses a cached thread pool by default to manage the threads
	 * used locally for the RMI calls.
	 * @param n The minimum number of samples.
	 * @param threadsPerServer The number of threads to execute on each RMI server.
	 * @param serverNames The list of RMI server names.
	 * @param es An executor service enabling specifying a different type of thread pool.
	 * @return Estimate of Pi.
	 */
	public static double distributedPi(int n, int threadsPerServer, String[] serverNames, ExecutorService es) {
		int threadCount = serverNames.length;
		class PiWorker implements Callable<Double> {
			private String serverName;
			private int n;
			public PiWorker(int n, String serverName) {
				this.n = n;
				this.serverName = serverName;
				if (System.getSecurityManager() == null) {
					System.setSecurityManager(new SecurityManager());
		        }
			}
			@Override
			public Double call() throws Exception {
				try {
					Registry registry = LocateRegistry.getRegistry(serverName);
					RemoteParallelAlgorithms comp = (RemoteParallelAlgorithms) registry.lookup("Alg");
					return comp.pi(n, threadsPerServer);
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
					return null;
				}	
			}
		}
		int perT = n / threadCount;
		if (n % threadCount != 0) perT++;
		@SuppressWarnings("unchecked")
		Future<Double>[] threadFutures = (Future<Double>[])new Future[threadCount];
		int i;
		for (i = 0; i < threadCount; i++) {
			threadFutures[i] = es.submit(new PiWorker(perT, serverNames[i]));
		}
		double mean = 0;
		for (i = 0; i < threadCount; i++) {
			try {
				mean += (threadFutures[i].get()-mean) / (i+1);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return mean;
	}

}
