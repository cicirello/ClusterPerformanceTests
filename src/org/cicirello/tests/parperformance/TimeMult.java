/*
 * Copyright 2017 Vincent A. Cicirello.
 *
 * TimeMult is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * TimeMult is distributed in the hope 
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
package org.cicirello.tests.parperformance;


import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cicirello.matrixops.ConcurrentMatrixMultiplier;
import org.cicirello.matrixops.DistributedMatrixMultiplier;
import org.cicirello.matrixops.MatrixMultiplier;


/**
 * This program generates timing data for exploring the performance
 * of a small cluster as the number of nodes and threads per node scales.
 * 
 * Times the multiplication of a matrix by a vector: M * V.
 * 
 * Assumes cluster has 8 nodes, with hostnames as seen in the field serverNames.
 * 
 * @author Vincent A. Cicirello
 * @version 8.15.2017
 */
public class TimeMult {
	
	/**
	 * Number of rows of matrix A.
	 */
	public static final int ROWS = 3000;
	
	/**
	 * Number of columns of matrix A.
	 */
	public static final int COLS = 3000;
	
	/**
	 * Seed for random number generator to ensure tests are repeatable.
	 * Samples use the sequence of seeds 42, 43, ...
	 */
	public static final int SEED = 42;
	
	/**
	 * An array of arrays of RMI server names.
	 * Each array is for one experimental condition (e.g., list of servers).
	 * For example, the first array has a single
	 * server (for runs with 1 remote server), the second has two (for runs with 2 remote
	 * servers), etc.  Note rpi0.local is the master node of the cluster, so the runs
	 * that include it use a mix of remote and local threads.
	 */
	public final static String[][] serverNames = {
			{"rpi1.local"},
			{"rpi2.local", "rpi1.local"},
			{"rpi3.local", "rpi2.local", "rpi1.local"},
			{"rpi4.local", "rpi3.local", "rpi2.local", "rpi1.local"},
			{"rpi5.local", "rpi4.local", "rpi3.local", "rpi2.local", "rpi1.local"},
			{"rpi6.local", "rpi5.local", "rpi4.local", "rpi3.local", "rpi2.local", "rpi1.local"},
			{"rpi7.local", "rpi6.local", "rpi5.local", "rpi4.local", "rpi3.local", "rpi2.local", "rpi1.local"},
			{"rpi7.local", "rpi6.local", "rpi5.local", "rpi4.local", "rpi3.local", "rpi2.local", "rpi1.local", "rpi0.local"}
	};


	/**
	 * Generates timing data for multiplying a matrix by a vector for: 
	 * (a) sequential implementation, (b) concurrent threads executing locally, 
	 * and (c) remote threads started via RMI calls.
	 * 
	 * Note: This assumes that the RMI servers have been started already.
	 * Otherwise, this will throw an exception at run time when it attempts to
	 * make RMI calls.
	 * 
	 * @param args No command line arguments.  Ignored.
	 */
	public static void main(String[] args) {
		ExecutorService es = Executors.newCachedThreadPool();
		
		MatrixMultiplier m = new MatrixMultiplier();
		ConcurrentMatrixMultiplier c = new ConcurrentMatrixMultiplier(4);
		
		//warmup
		// For fair comparison to remote execution, warmup the VM (i.e., encourage
		// JIT compiler to compile natively).  The RMI servers do this upon startup,
		// so doing this here as well to avoid unfairly biasing results in favor of
		// remote execution.
		m.multiply(new double[64][64], new double[64][64]);
		c.multiply(new double[64][64], new double[64][64]);
		// end warmup
		
		System.out.println("NumRMIServers\tNumThreadsPerServer\tTimeSeconds");
		for (int samples = 0; samples < 10; samples++) {
			Random r = new Random(SEED+samples);
			double[][] A = getRandMatrix(ROWS,COLS,r);
			double[][] B = getRandMatrix(COLS,1,r);
			long start = System.nanoTime();
			m.multiply(A, B, false);
			long end = System.nanoTime();
			System.out.println(0 + "\t" + 0 + "\t" + (end-start)/1000000000.0);
			for (int t = 1; t <= 4; t++) {
				c.setThreads(t);
				start = System.nanoTime();
				c.multiply(A, B, false);
				end = System.nanoTime();
				System.out.println(0 + "\t" + t + "\t" +(end-start)/1000000000.0);
				for (int s = 1; s <= serverNames.length; s++) {
					DistributedMatrixMultiplier d = new DistributedMatrixMultiplier(serverNames[s-1], t, 0);
					start = System.nanoTime();
					d.multiply(A, B, false);
					end = System.nanoTime();
					System.out.println(s + "\t" + t + "\t" +(end-start)/1000000000.0);
				}
			}
		}
		
		es.shutdown();
	}
	
	
	
	
	private static double[][] getRandMatrix(int row, int col, Random r) {
		double[][] m = new double[row][col];
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				m[i][j] = r.nextDouble();
			}
		}
		return m;
	}

}
