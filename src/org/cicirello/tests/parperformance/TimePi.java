/*
 * Copyright 2017 Vincent A. Cicirello.
 *
 * TimePi is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * TimePi is distributed in the hope 
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cicirello.math.Pi;

/**
 * This program generates timing data for exploring the performance
 * of a small cluster as the number of nodes and threads per node scales.
 * 
 * Times the estimation of Pi using Monte Carlo integration.
 * 
 * Assumes cluster has 8 nodes, with hostnames as seen in the field serverNames.
 * 
 * @author Vincent A. Cicirello
 * @version 8.15.2017
 */
public class TimePi {

	/**
	 * Number of samples for longest run.
	 */
	public final static int MAX = 1200000000;
	
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
	 * Generates timing data for estimating Pi via Monte Carlo integration for: 
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
		
		//warmup
		// For fair comparison to remote execution, warmup the VM (i.e., encourage
		// JIT compiler to compile natively).  The RMI servers do this upon startup,
		// so doing this here as well to avoid unfairly biasing results in favor of
		// remote execution.
		Pi.pi(1000);
		Pi.concurrentPi(1000, 4, es);
		// end warmup
		
		System.out.println("NumRMIServers\tNumThreadsPerServer\tNumSamples\tTimeSeconds\tAccuracy");
		for (int samples = 0; samples < 10; samples++) {
			for (int i = 12; i <= MAX; i*=10) {
				long start = System.nanoTime();
				double pi = Pi.pi(i);
				long end = System.nanoTime();
				System.out.println(0 + "\t" + 0 + "\t" + i + "\t" + (end-start)/1000000000.0 + "\t" + Math.abs(Math.PI-pi));
				for (int t = 1; t <= 4; t++) {
					start = System.nanoTime();
					pi = Pi.concurrentPi(i, t, es);
					end = System.nanoTime();
					System.out.println(0 + "\t" + t + "\t" + i + "\t" +(end-start)/1000000000.0 + "\t" + Math.abs(Math.PI-pi));
					for (int r = 1; r <= serverNames.length; r++) {
						start = System.nanoTime();
						pi = Pi.distributedPi(i, t, serverNames[r-1], es);
						end = System.nanoTime();
						System.out.println(r + "\t" + t + "\t" + i + "\t" + (end-start)/1000000000.0 + "\t" + Math.abs(Math.PI-pi));
					}
				}
				if (i==MAX) break;
			}
		}
		es.shutdown();
	}

}
